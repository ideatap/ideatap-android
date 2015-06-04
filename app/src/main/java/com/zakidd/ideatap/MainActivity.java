package com.zakidd.ideatap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends AppCompatActivity {

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

        twitterLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authWithTwitter();
            }
        });
    }

    private void authWithGoogle() {

    }

    private void authWithTwitter() {
        startActivityForResult(new Intent(this, TwitterOAuthActivity.class), 2);
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
                System.out.println();
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
        if(requestCode == 2) {
            options.put("oauth_token", data.getStringExtra("oauth_token"));
            options.put("oauth_token_secret", data.getStringExtra("oauth_token_secret"));
            options.put("user_id", data.getStringExtra("user_id"));
            firebaseAuth("twitter", options);
            AuthData authData = firebase.getAuth();
            System.out.println(authData);
        }
    }
}