package com.zakidd.ideatap;

import android.app.Application;

import com.firebase.client.Firebase;


public class IdeaTap extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
