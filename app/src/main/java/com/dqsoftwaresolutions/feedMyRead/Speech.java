package com.dqsoftwaresolutions.feedMyRead;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Speech implements Runnable {
    private final TextToSpeech tts;
    private boolean firstTime = true;
    private HashMap<String, String> hashMap;
    private Bundle mBundle;
    private InputStream is = null;
    private BufferedReader reader = null;
    private final Lock mutex = new ReentrantLock();
    private final Condition textSpoken = mutex.newCondition();
    private  final Lock mutex1 = new ReentrantLock();
    private final Condition pauseCond = mutex1.newCondition();
    private int count = 0;
    private int position = 0;
    private final int LEN = 1;
    private final String[] buffer = new String[LEN];
    private boolean pausing = false;
    private boolean starting = true;
    private boolean resuming = false;

    // Constructor
    public Speech(TextToSpeech tts0, InputStream is0) {
        tts = tts0;
        is = is0;
    }

    @Override
    public void run() {
        try {
            speak();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void speak() throws InterruptedException {
        String str;
        if (firstTime) { // Initialization only once
            String keyText = "Text Spoken ID";
            hashMap = new HashMap();
            mBundle = new Bundle();
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, keyText);
            mBundle.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, keyText);
            MyUtteranceProgressListener listener = new MyUtteranceProgressListener();
            tts.setOnUtteranceProgressListener(listener);
            firstTime = false;
        }
        try {
            if (reader == null) {
                reader = new BufferedReader(new InputStreamReader(is));

            }
             while ((str = reader.readLine()) != null) {
                mutex1.lock();
                while (pausing)
                pauseCond.await(); // Condition wait
                mutex1.unlock();
                mutex.lock();
                count += 1;
                if (starting) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts.speak(str, TextToSpeech.QUEUE_ADD, mBundle, str);
                    } else {
                        tts.speak(str, TextToSpeech.QUEUE_ADD, hashMap);
                    }
                    starting = false; // starts once only
                } else {
                    if (resuming) {

// copy buffer string back
                        int j = position - 1;
                        if (j < 0) j = j + LEN;
                        for (int i = 1; i < count; i++) {
                            if (i == 1) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    String keyText = "Text Spoken ID";
                                    tts.speak(buffer[j], TextToSpeech.QUEUE_FLUSH, mBundle, keyText);
                                } else {
                                    tts.speak(buffer[j], TextToSpeech.QUEUE_FLUSH, hashMap);
                                }
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    String keyText = "Text Spoken ID";
                                    tts.speak(buffer[j], TextToSpeech.QUEUE_FLUSH, mBundle, keyText);
                                } else {
                                    tts.speak(buffer[j], TextToSpeech.QUEUE_FLUSH, hashMap);
                                }
                            }

                            j = (j + 1) % LEN;
                        }
                    } // if ( resuming )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        String keyText = "Text Spoken ID";
                        tts.speak(str, TextToSpeech.QUEUE_ADD, mBundle, keyText);

                    } else {
                        tts.speak(str, TextToSpeech.QUEUE_ADD, hashMap);
                    }
                } // else
                while (count > LEN)
                    textSpoken.await(); // Condition wait
                mutex.unlock();
                buffer[position] = str;
                position++;
                if (position == LEN)
                    position = 0; // Circular buffer
            } // while reader
        } catch (IOException e) {
            Log.e("speak", e.getMessage());
        }
    }

    private void onUtteranceCompleted(String utteranceId) {
        if (utteranceId.equals("Text Spoken ID")) {
            mutex.lock();
            count--;
            textSpoken.signal();
            mutex.unlock();
        }
    }

    // Inner class for UtteranceProgressListener
    private class MyUtteranceProgressListener extends UtteranceProgressListener {
        @Override
        public void onDone(String utteranceId) {

            onUtteranceCompleted(utteranceId);
        }

        @Override
        public void onError(String utteranceId) {
        }

        @Override
        public void onStart(String arg0) {
        }
    }

    public void speechPause() {
        if (pausing) return; // engine already stopped
        mutex1.lock();
        pausing = true;
        mutex1.unlock();
        tts.stop(); // stop engine, clears utterance queue
    }

    public void speechResume() {

        if (!pausing) return; // engine already running
        mutex1.lock();
        pausing = false;
        resuming = true;
        pauseCond.signal(); // Wake up the waiting method
        mutex1.unlock();
    }

}
