package com.dqsoftwaresolutions.feedMyRead.data;


public class MainListPosition {
    private int mPosition;
    private static int instantiationCounter = 0;
    private static volatile MainListPosition instance;


    private MainListPosition() {

    }
    public static MainListPosition getInstance() {
        if (instance == null ) {
            instance = new MainListPosition();
        }

        return instance;
    }
    public int getInstantiationCounter(){
        return instantiationCounter;
    }
    public MainListPosition(int position) {

        mPosition = position;
    }

    public void setPosition(int position) {

        instantiationCounter=position;
        mPosition = position;
    }

}

