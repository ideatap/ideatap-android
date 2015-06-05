package com.zakidd.ideatap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zakidd.ideatap.auth.LoginActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do if login logic here:
        // if (something.logged_in) {
        //     Redirect To "Home"?
        // } else {
        Intent loginIntent = new Intent(this, LoginActivity.class);
        this.startActivity(loginIntent);
        // }
    }
}