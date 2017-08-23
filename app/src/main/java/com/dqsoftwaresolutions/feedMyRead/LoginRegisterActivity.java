package com.dqsoftwaresolutions.feedMyRead;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dqsoftwaresolutions.feedMyRead.database.UserContract;
import com.dqsoftwaresolutions.feedMyRead.webservices.WebServiceTask;
import com.dqsoftwaresolutions.feedMyRead.webservices.WebServiceUtils;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import static android.provider.Settings.System.canWrite;
import static com.dqsoftwaresolutions.feedMyRead.LoginRegisterActivity.LOGIN_ACTION.FORGET;
import static com.dqsoftwaresolutions.feedMyRead.LoginRegisterActivity.LOGIN_ACTION.REGISTER;
import static com.dqsoftwaresolutions.feedMyRead.LoginRegisterActivity.LOGIN_ACTION.SIGN_IN;
import static com.dqsoftwaresolutions.feedMyRead.R.id.email;
import static com.dqsoftwaresolutions.feedMyRead.R.id.password;
import static com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype.Flipv;

public class LoginRegisterActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private UserLoginRegisterTask mUserLoginRegisterTask = null;
    private EditText mEmailView;
    private EditText mPasswordView;

    private ContentResolver mContentResolver;
    private String token;
    private Utils mUtils;
    private NiftyDialogBuilder dialogBuilder;
    private static final int CODE_WRITE_SETTINGS_PERMISSION = 111;
    private Context mContext;


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public enum LOGIN_ACTION {
        SIGN_IN, REGISTER, FORGET
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initViews();
        mUtils = new Utils(LoginRegisterActivity.this);
        mContentResolver = LoginRegisterActivity.this.getContentResolver();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean canWrite = canWrite(this);
            if (!canWrite) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, LoginRegisterActivity.CODE_WRITE_SETTINGS_PERMISSION);
                handler.removeCallbacks(checkOverlaySetting);
                handler1.postDelayed(checkOverlaySetting, 1000);
            }

        }
        TextView forgetPassword = (TextView) findViewById(R.id.forget_password);
        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleAnalyticsAction(FORGET);
                forgetMyPasswordDialog();
            }
        });
        mContext = this;

    }



    private void forgetMyPasswordDialog() {
        dialogBuilder = NiftyDialogBuilder.getInstance(this);
        RelativeLayout layout = (RelativeLayout) dialogBuilder.findViewById(R.id.main);
        layout.setPadding(20, 20, 20, 20);
        dialogBuilder
                .withTitle("Enter email")
                .withEffect(Flipv)
                .withDialogColor("#005954")
                .setCustomView(R.layout.forget_password_dialog, this)
                .withMessage("")
                .withButton1Text("OK")
                .withButton2Text("Cancel")
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText setEmail = (EditText) dialogBuilder.findViewById(R.id.forget_password_custom);
                        sendNewPassword(setEmail.getText().toString());
                        dialogBuilder.hide();

                    }
                })
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.hide();
                    }
                })
                .show();

    }

    private void sendNewPassword(String email) {
        UserForgotPasswordTask userForgotPasswordTask = new UserForgotPasswordTask(email);
        userForgotPasswordTask.execute((Void) null);
    }

    //This handler responsible to monitor the user agreement to have access to the setting (control the brightness of the screen) when the user click ok its return to login view
    final private Handler handler1 = new Handler();
    private final Runnable checkOverlaySetting = new Runnable() {
        @Override
        @TargetApi(23)
        public void run() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                return;
            }
            if (Settings.System.canWrite(LoginRegisterActivity.this)) {
                Intent i = new Intent(LoginRegisterActivity.this, LoginRegisterActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                return;
            }
            handler1.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUserLoginRegisterTask = null;
        mUtils = null;
    }

    private void initViews() {
        mEmailView = (EditText) findViewById(email);
        mPasswordView = (EditText) findViewById(password);
    }

    public void attemptLoginRegister(View view) {

        if (mUserLoginRegisterTask != null) {
            return;
        }

        mEmailView.setError(null);
        mPasswordView.setError(null);
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;

        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_password_length));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {

            focusView.requestFocus();
        } else {

            mUserLoginRegisterTask = new UserLoginRegisterTask(email, password, view.getId() == R.id.email_sign_in_button);
            mUserLoginRegisterTask.execute((Void) null);
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 1;
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showProgress(final boolean isShow) {
        findViewById(R.id.login_progress).setVisibility(isShow ? View.VISIBLE : View.GONE);
        findViewById(R.id.login_form).setVisibility(isShow ? View.GONE : View.VISIBLE);
    }

    private void setNewPassword(final LoginRegisterActivity loginRegisterActivity) {

        loginRegisterActivity.runOnUiThread(new Runnable() {
            public void run() {

                dialogBuilder = NiftyDialogBuilder.getInstance(loginRegisterActivity);
                RelativeLayout layout = (RelativeLayout) dialogBuilder.findViewById(R.id.main);
                layout.setPadding(20, 20, 20, 20);
                dialogBuilder
                        .withTitle("Enter new password")
                        .withEffect(Flipv)
                        .withDialogColor("#005954")
                        .setCustomView(R.layout.forget_password_dialog, loginRegisterActivity)
                        .withMessage("")
                        .withButton1Text("OK")
                        .withButton2Text("Cancel")
                        .setButton1Click(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                EditText setPassword = (EditText) dialogBuilder.findViewById(R.id.forget_password_custom);
                                EditText email = (EditText) findViewById(R.id.email);
                                NewPasswordTask newPasswordTask = new NewPasswordTask(email.getText().toString(), setPassword.getText().toString());
                                newPasswordTask.execute((Void) null);
                                dialogBuilder.hide();

                            }
                        })
                        .setButton2Click(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogBuilder.hide();
                            }
                        })
                        .show();
            }
        });
    }

    private class UserLoginRegisterTask extends WebServiceTask {
        private final ContentValues contentValues = new ContentValues();
        private final boolean mIsLogin;

        UserLoginRegisterTask(String email, String password, boolean isLogin) {

            super(LoginRegisterActivity.this);
            long changeTime = mUtils.setTimeChange();
            contentValues.put(Constants.USERNAME, email);
            contentValues.put(Constants.PASSWORD, password);
            contentValues.put(Constants.CHANGE_USER_TIME, changeTime);
            mIsLogin = isLogin;
        }

        @Override
        public void showProgress() {
            LoginRegisterActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            LoginRegisterActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            String massage = "";
            JSONObject obj = WebServiceUtils.requestJSONObject(mIsLogin ? Constants.LOGIN_URL : Constants.SIGNUP_URL,
                    mIsLogin ? WebServiceUtils.METHOD.GET : WebServiceUtils.METHOD.POST,
                    contentValues, mContext);
                Log.d("obj", String.valueOf(obj));
            try {
                if (obj != null) {
                    if (mIsLogin) {
                        googleAnalyticsAction(SIGN_IN);
                    } else {
                        googleAnalyticsAction(REGISTER);
                    }
                    if (!obj.has("passWord")) {
                        massage = String.valueOf(obj.getString("token"));
                    } else {
                        setNewPassword(LoginRegisterActivity.this);
                        return false;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (!hasError(obj)) {
                try {

                    switch (massage) {
                        case "userIsNotSignIn": {
                            mUserLoginRegisterTask = null;
                            Message msg = handler.obtainMessage();
                            msg.arg1 = 1;

                            handler.sendMessage(msg);
                            break;
                        }
                        case "passwordOrUsername": {
                            mUserLoginRegisterTask = null;
                            Message msg = handler.obtainMessage();
                            msg.arg1 = 2;
                            handler.sendMessage(msg);
                            break;
                        }
                        case "UserNameAlreadyExists": {
                            mUserLoginRegisterTask = null;
                            Message msg = handler.obtainMessage();
                            msg.arg1 = 3;
                            handler.sendMessage(msg);
                            break;
                        }
                        default:
                            String value = null;
                            if (obj != null) {
                                value = obj.getString("token");
                            }
                            long changeTime = 0;
                            if (obj != null) {
                                changeTime = obj.getLong("changeUserTime");
                            }
                            token = value;
                            if (value != null) {
                                if (!value.equals("false")) {
                                    ContentValues values = new ContentValues();
                                    values.put(UserContract.UserColumns.USER_NAME, email);
                                    values.put(UserContract.UserColumns.PASSWORD, password);
                                    values.put(UserContract.UserColumns.TOKEN, value);
                                    values.put(UserContract.UserColumns.CHANGE_USER_TIME, changeTime);
                                    mContentResolver.insert(UserContract.URI_TABLE, values);
                                    return true;
                                } else {
                                    mUserLoginRegisterTask = null;
                                }
                            }
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Intent intent = new Intent(LoginRegisterActivity.this, DownLoadProgressActivity.class);
            intent.putExtra("TOKEN", token);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


    private class UserForgotPasswordTask extends WebServiceTask {
        private final ContentValues contentValues = new ContentValues();

        UserForgotPasswordTask(String email) {

            super(LoginRegisterActivity.this);

            contentValues.put(Constants.USERNAME, email);

        }

        @Override
        public void showProgress() {
            LoginRegisterActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            LoginRegisterActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            String massage = "";
            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.FORGOT_PASSWORD_URL,
                    WebServiceUtils.METHOD.POST,
                    contentValues, mContext);

            if (obj != null) {
                try {
                    massage = String.valueOf(obj.getString("token"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (massage.equals("nomail")) {
                    mUserLoginRegisterTask = null;
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 1;

                    handler.sendMessage(msg);
                } else {
                    mUserLoginRegisterTask = null;
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 4;

                    handler.sendMessage(msg);
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Intent intent = new Intent(LoginRegisterActivity.this, DownLoadProgressActivity.class);
            intent.putExtra("TOKEN", token);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private class NewPasswordTask extends WebServiceTask {
        private final ContentValues contentValues = new ContentValues();

        NewPasswordTask(String email, String password) {
            super(LoginRegisterActivity.this);
            contentValues.put(Constants.USERNAME, email);
            contentValues.put(Constants.PASSWORD, password);
        }

        @Override
        public void showProgress() {
            LoginRegisterActivity.this.showProgress(true);
        }

        @Override
        public void hideProgress() {
            LoginRegisterActivity.this.showProgress(false);
        }

        @Override
        public boolean performRequest() {
            String massage = "";
            JSONObject obj = WebServiceUtils.requestJSONObject(Constants.NEW_PASSWORD_URL,
                    WebServiceUtils.METHOD.POST,
                    contentValues, mContext);
            if (obj != null) {
                try {
                    massage = String.valueOf(obj.getString("token"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (massage.equals("nomail")) {
                    mUserLoginRegisterTask = null;
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 1;

                    handler.sendMessage(msg);
                } else {
                    mUserLoginRegisterTask = null;
                    Message msg = handler.obtainMessage();
                    msg.arg1 = 5;

                    handler.sendMessage(msg);
                }
            }
            return false;
        }

        @Override
        public void performSuccessfulOperation() {
            Intent intent = new Intent(LoginRegisterActivity.this, DownLoadProgressActivity.class);
            intent.putExtra("TOKEN", token);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tracker googleAnalytics = ((FeedMyRead) this.getApplication()).getTracker();
        googleAnalytics.setScreenName("Login Screen");
        googleAnalytics.send(new HitBuilders.ScreenViewBuilder().build());
        FeedMyRead.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FeedMyRead.activityPaused();
        mUserLoginRegisterTask = null;

    }

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                Toast.makeText(LoginRegisterActivity.this, "No user under that address is signed up", Toast.LENGTH_LONG).show();
            } else if (msg.arg1 == 2) {
                Toast.makeText(LoginRegisterActivity.this, "Password or Email are incorrect", Toast.LENGTH_LONG).show();
            } else if (msg.arg1 == 3) {
                Toast.makeText(LoginRegisterActivity.this, "User name already exists", Toast.LENGTH_LONG).show();
            } else if (msg.arg1 == 4) {
                Toast.makeText(LoginRegisterActivity.this, "Temporary password sent to your Email", Toast.LENGTH_LONG).show();
            } else if (msg.arg1 == 5) {
                Toast.makeText(LoginRegisterActivity.this, "New password Successfully submitted", Toast.LENGTH_LONG).show();
            }
        }

    };

    private void googleAnalyticsAction(LOGIN_ACTION action) {
        Tracker googleAnalytics = ((FeedMyRead) getApplication()).getTracker();
        switch (action) {
            case SIGN_IN:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("SIGN_IN")
                        .build());
                break;
            case REGISTER:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("REGISTER")
                        .build());
                break;
            case FORGET:
                googleAnalytics.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("FORGET")
                        .build());
                break;

        }
    }
}
