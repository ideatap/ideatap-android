package com.zakidd.ideatap.ui;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by g33kidd on 6/3/15.
 */
public class IdeaTap extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
