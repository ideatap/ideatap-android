package com.zakidd.ideatap.auth;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;


public class GoogleOAuthActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;

    /** Flag used to tell if we have an intent in progress.  Used onConnectionFailed */
    private boolean mIntentInProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Plus.API)
            .addScope(Plus.SCOPE_PLUS_LOGIN)
            .build();

        connectIfNotConnecting();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.e("IdeaTap: ", "CONNECTED");
    }

    /**
     * When our GoogleApiClient instance, mGoogleApiClient, fails to connect then we get notified
     * here.  We are passed a ConnectionResult instance so we can attempt to fix the issue
     * by calling the ConnectionResult's intent sender resolution, if it has one.
     *
     * @param result
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(), RC_SIGN_IN,
                        null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectIfNotConnecting();
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent data) {
        // If we created the Intent with the RC_SIGN_IN value then we know that we were attempting
        // to fix a sign in issue and that hopefully it has been solved so we will attempt to sign
        // in now.
        if (requestCode == RC_SIGN_IN) {
            mIntentInProgress = false;
            connectIfNotConnecting();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectIfNotConnecting();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnectIfConnected();
    }

    private void connectIfNotConnecting() {
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    private void disconnectIfConnected() {
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }
    }
}
