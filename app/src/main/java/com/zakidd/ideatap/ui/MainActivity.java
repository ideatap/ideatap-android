package com.zakidd.ideatap.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.zakidd.ideatap.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;


public class MainActivity extends AppCompatActivity {

    Firebase fireRef;

    @InjectView(R.id.googleLoginButton) Button loginButton;
    @InjectView(R.id.testData) EditText testData;
    @InjectView(R.id.testDataView) TextView testDataView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);

        fireRef = new Firebase("https://idea-tap.firebaseio.com/");

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnItemClick(R.id.googleLoginButton)
    public void loginWithGoogle(int positionOfClick) {
        fireRef.authWithOAuthToken("google", "", new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {

            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {

            }
        });
    }

    @OnTextChanged(R.id.testData)
    public void testDataChanged(CharSequence changedText) {
        fireRef.child("test_data").setValue(changedText);
    }

}