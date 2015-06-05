package com.zakidd.ideatap.auth;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.SignInButton;
import com.zakidd.ideatap.R;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class LoginActivity extends AppCompatActivity {

    /** Request Codes **/
    final protected int RC_TWITTER_SIGN_IN = 2;
    final protected int RC_GOOGLE_SIGN_IN = 3;

    final public String GOOGLE_FIREBASE_PROVIDER = "google";
    final public String TWITTER_FIREBASE_PROVIDER = "twitter";

    private Firebase firebase;

    @InjectView(R.id.googleLoginButton) SignInButton googleLoginButton;
    @InjectView(R.id.twitterLoginButton) Button twitterLoginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.inject(this);
        firebase = new Firebase("https://idea-tap.firebaseio.com");
    }

    @OnClick(R.id.googleLoginButton)
    public void authWithGoogle() {
        startActivityForResult(new Intent(this, GoogleOAuthActivity.class), RC_GOOGLE_SIGN_IN);
    }

    @OnClick(R.id.twitterLoginButton)
    public void authWithTwitter() {
        startActivityForResult(new Intent(this, TwitterOAuthActivity.class), RC_TWITTER_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Map<String, String> options = new HashMap<>();

        if (resultCode == Activity.RESULT_OK) {
            switch(requestCode) {
                case RC_TWITTER_SIGN_IN:
                    twitterAuthentication(data);
                    break;

                case RC_GOOGLE_SIGN_IN:
                    googleAuthentication(data);
                    break;
            }
        }
    }

    protected void twitterAuthentication(Intent data) {
        Map<String, String> options = new HashMap<>();

        try {
            options.put("oauth_token", data.getStringExtra("oauth_token"));
            options.put("oauth_token_secret", data.getStringExtra("oauth_token_secret"));
            options.put("user_id", data.getStringExtra("user_id"));

            firebase.authWithOAuthToken(TWITTER_FIREBASE_PROVIDER, options, new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    Object profile = authData.getProviderData().get("cachedUserProfile");

                    Map<String, String> newUser = new HashMap<>();
                    newUser.put("provider", authData.getProvider());
                    newUser.put("provider_id", authData.getProviderData().get("id").toString());
                    newUser.put("username", authData.getProviderData().get("username").toString());
                    newUser.put("name", authData.getProviderData().get("displayName").toString());
                    // newUser.put("image", );

                    firebase.child("users").child(authData.getUid()).setValue(newUser);
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {

                }
            });

            AuthData authData = firebase.getAuth();
        } catch(NullPointerException e) {
            // TODO: Abstract getResources().getString(R.string.log_label to a separate Util
            // library for logging.
            Log.i(getResources().getString(R.string.log_label), "User backed out of Twitter Login");
        }
    }

    @SuppressWarnings("unchecked")
    protected void googleAuthentication(Intent data) {
        String oauth_token = data.getStringExtra("oauth_token");

        firebase.authWithOAuthToken(GOOGLE_FIREBASE_PROVIDER, oauth_token, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Map<String, String> newUser = new HashMap<>();
                Map<String, Object> providerData = authData.getProviderData();
                Map<String, String> userProfile = (Map<String, String>) providerData.get("cachedUserProfile");

                Log.e("IdeaTap", providerData.toString());

                newUser.put("provider", authData.getProvider());
                newUser.put("provider_id", providerData.get("id").toString());
                newUser.put("email", providerData.get("email").toString());
                newUser.put("username", providerData.get("displayName").toString());
                newUser.put("first_name", userProfile.get("given_name"));
                newUser.put("last_name", userProfile.get("last_name"));
                newUser.put("locale", userProfile.get("locale"));
                newUser.put("image", userProfile.get("picture"));

                firebase.child("users").child(authData.getUid()).setValue(newUser);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
    }
}

