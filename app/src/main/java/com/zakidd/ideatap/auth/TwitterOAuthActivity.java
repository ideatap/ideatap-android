package com.zakidd.ideatap.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;


public class TwitterOAuthActivity extends Activity {

    private WebView mTwitterView;
    private Twitter mTwitter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwitter = new TwitterFactory(new ConfigurationBuilder()
                .setOAuthConsumerKey("wMtuQIoT7PCA5RSSDmjw3nnBu")
                .setOAuthConsumerSecret("X0Hme8DgJXcWhLF9ccEoKQNf4uJasiB6Jjj114398KpiURVEUt")
                .build()).getInstance();
        mTwitterView = new WebView(this);
        mTwitterView.getSettings().setJavaScriptEnabled(true);
        setContentView(mTwitterView);
        loginToTwitter();
    }

    private void loginToTwitter() {
        new AsyncTask<Void, Void, RequestToken>() {
            @Override
            protected RequestToken doInBackground(Void... params) {
                RequestToken token = null;
                try {
                    token = mTwitter.getOAuthRequestToken("oauth://cb");
                }catch(TwitterException te) {
                    Log.e("TAG", te.toString());
                }

                return token;
            }

            @Override
            protected void onPostExecute(final RequestToken requestToken) {
                mTwitterView.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        if(url.startsWith("oauth://cb")) {
                            getTwitterOAuthLogin(requestToken, Uri.parse(url).getQueryParameter("oauth_verifier"));
                        }
                    }
                });

                mTwitterView.loadUrl(requestToken.getAuthorizationURL());
            }
        }.execute();
    }

    private void getTwitterOAuthLogin(final RequestToken requestToken, final String oauthVerifier) {
        new AsyncTask<Void, Void, AccessToken>() {

            @Override
            protected AccessToken doInBackground(Void... params) {
                AccessToken accessToken = null;

                try {
                    accessToken = mTwitter.getOAuthAccessToken(requestToken, oauthVerifier);
                } catch(TwitterException e) {
                    Log.e("TAG", e.toString());
                }

                return accessToken;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("oauth_token", accessToken.getToken());
                resultIntent.putExtra("oauth_token_secret", accessToken.getTokenSecret());
                resultIntent.putExtra("user_id", accessToken.getUserId() + "");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }.execute();
    }
}
