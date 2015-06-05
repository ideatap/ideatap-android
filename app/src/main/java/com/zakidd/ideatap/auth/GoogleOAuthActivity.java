package com.zakidd.ideatap.auth;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.io.IOException;


public class GoogleOAuthActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private static final int RC_FIX_CONNECTION_ERROR = 1;

    private GoogleApiClient mGoogleApiClient;
    private GoogleApiAvailability mGoogleApiAvailability;

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
                .addScope(new Scope("email"))
                .build();

        mGoogleApiAvailability = GoogleApiAvailability.getInstance();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        final Context context = this.getApplicationContext();
        final String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                String oauthToken = null;

                try {
                    oauthToken = GoogleAuthUtil.getToken(context, accountName,
                            "oauth2:email " + Plus.SCOPE_PLUS_LOGIN);
                } catch (IOException | GoogleAuthException e) {
                    e.printStackTrace();
                }

                if (oauthToken == null) {
                    this.cancel(true);
                }

                return oauthToken;
            }

            @Override
            protected void onPostExecute(String oauthToken) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("oauth_token", oauthToken);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }.execute();
    }

    /**
     * When our GoogleApiClient instance, mGoogleApiClient, fails to connect then we get notified
     * here.  We are passed a ConnectionResult instance so we can attempt to fix the issue
     * by calling the ConnectionResult's intent sender resolution, if it has one.
     *
     * @param result - Result from attempting to connect to Google Play Services.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e("IdeaTap", "COnnection Error: " + result.hasResolution());
        Log.e("IdeaTap", "Connection Error #: " + result.getErrorCode());
        if (result.hasResolution()) {
            if (!mIntentInProgress) {
                try {
                    mIntentInProgress = true;
                    result.startResolutionForResult(this, RC_SIGN_IN);
                } catch (IntentSender.SendIntentException e) {
                    mIntentInProgress = false;
                    mGoogleApiClient.connect();
                }
            }
        }
        else {
            final int error_code = result.getErrorCode();

            switch (error_code) {
                case ConnectionResult.SERVICE_MISSING:
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                case ConnectionResult.SERVICE_DISABLED:
                    mGoogleApiAvailability.getErrorDialog(this, error_code, RC_FIX_CONNECTION_ERROR);
                    break;
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
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
