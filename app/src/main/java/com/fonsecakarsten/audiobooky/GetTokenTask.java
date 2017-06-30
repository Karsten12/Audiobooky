package com.fonsecakarsten.audiobooky;

import android.accounts.Account;
import android.app.Activity;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;


/**
 * Created by Karsten on 6/15/2017.
 */

class GetTokenTask extends AsyncTask<Void, Void, Void> {
    private Activity mActivity;
    private Account mAccount;
    private int mRequestCode;

    GetTokenTask(Activity activity, Account account) {
        this.mActivity = activity;
        this.mAccount = account;
        this.mRequestCode = BookActivity.REQUEST_ACCOUNT_AUTHORIZATION;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            String token = fetchToken();
            if (token != null) {
                ((BookActivity) mActivity).onTokenReceived(token);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Get an authentication token from Google
    private String fetchToken() throws IOException {
        String accessToken;
        try {
            String mScope = "oauth2:https://www.googleapis.com/auth/cloud-platform";
            accessToken = GoogleAuthUtil.getToken(mActivity, mAccount, mScope);
            GoogleAuthUtil.clearToken(mActivity, accessToken); // used to remove stale tokens
            accessToken = GoogleAuthUtil.getToken(mActivity, mAccount, mScope);
            return accessToken;
        } catch (UserRecoverableAuthException userRecoverableException) {
            mActivity.startActivityForResult(userRecoverableException.getIntent(), mRequestCode);
        } catch (GoogleAuthException fatalException) {
            fatalException.printStackTrace();
        }
        return null;
    }
}
