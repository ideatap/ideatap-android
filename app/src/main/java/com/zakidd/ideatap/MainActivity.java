package com.zakidd.ideatap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.api.GoogleApiClient;
import com.zakidd.ideatap.auth.TwitterOAuthActivity;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    final protected int TWITTER_SIGN_IN = 2;
    final protected int GOOGLE_SIGN_IN = 3;

    private GoogleApiClient googleApiClient;
    private Firebase firebase;

    @InjectView(R.id.googleLoginButton) Button googleLoginButton;
    @InjectView(R.id.twitterLoginButton) Button twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        firebase = new Firebase("https://idea-tap.firebaseio.com");
    }

    @OnClick(R.id.googleLoginButton)
    private void authWithGoogle() {

    }

    @OnClick(R.id.twitterLoginButton)
    public void authWithTwitter() {
        startActivityForResult(new Intent(this, TwitterOAuthActivity.class), TWITTER_SIGN_IN);
    }

    private void firebaseAuth(final String provider, Map<String, String> options) {
        firebase.authWithOAuthToken(provider, options, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Object profile = authData.getProviderData().get("cachedUserProfile");
                Map<String, String> newUser = new HashMap<String, String>();
                newUser.put("provider", authData.getProvider());
                newUser.put("provider_id", authData.getProviderData().get("id").toString());
                newUser.put("username", authData.getProviderData().get("username").toString());
                newUser.put("name", authData.getProviderData().get("displayName").toString());
//                newUser.put("image", );

                firebase.child("users").child(authData.getUid()).setValue(newUser);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Map<String, String> options = new HashMap<String, String>();

        if (requestCode == TWITTER_SIGN_IN) {
            options.put("oauth_token", data.getStringExtra("oauth_token"));
            options.put("oauth_token_secret", data.getStringExtra("oauth_token_secret"));
            options.put("user_id", data.getStringExtra("user_id"));
            firebaseAuth("twitter", options);
            AuthData authData = firebase.getAuth();
            System.out.println(authData);
        }
    }
}