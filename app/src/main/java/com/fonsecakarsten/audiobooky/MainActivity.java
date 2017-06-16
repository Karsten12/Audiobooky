package com.fonsecakarsten.audiobooky;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fonsecakarsten.audiobooky.R.mipmap.ic_launcher_round;

public class MainActivity extends Activity {

    ArrayList<String> mImageArray;
    recycleAdapter mAdapter;


    private static String accessToken;
    static final int REQUEST_GALLERY_IMAGE = 10;
    static final int REQUEST_CODE_PICK_ACCOUNT = 11;
    static final int REQUEST_ACCOUNT_AUTHORIZATION = 12;
    static final int REQUEST_PERMISSIONS = 13;
    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        getAuthToken();
    }

    public void onFABClick(View v) {
        Intent intent = new Intent(this, NewCapureActivity.class);
        startActivityForResult(intent, 1);
    }


    // Convert each image's text using OCR
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mImageArray = data.getExtras().getStringArrayList("imageArray");
//                for (int i = 0; i < mImageArray.size(); i++) {
//                    MobileVisionAsync task = new MobileVisionAsync(mImageArray.get(i), getApplicationContext());
//                    task.execute();
//                }
                if (mImageArray.size() > 0) {
                    CloudVisionAsync task = new CloudVisionAsync(accessToken, mImageArray.get(0));
                    task.execute();
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                AccountManager am = AccountManager.get(this);
                Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                for (Account account : accounts) {
                    if (account.name.equals(email)) {
                        mAccount = account;
                        break;
                    }
                }
                getAuthToken();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "No Account Selected", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_ACCOUNT_AUTHORIZATION) {
            if (resultCode == RESULT_OK) {
                Bundle extra = data.getExtras();
                onTokenReceived(extra.getString("authtoken"));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Authorization Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void pickUserAccount() {
        String[] accountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    private void getAuthToken() {
        String SCOPE = "oauth2:https://www.googleapis.com/auth/cloud-platform";
        if (mAccount == null) {
            pickUserAccount();
        } else {
            new GetTokenTask(MainActivity.this, mAccount, SCOPE, REQUEST_ACCOUNT_AUTHORIZATION).execute();
        }
    }

    public void onTokenReceived(String token) {
        accessToken = token;
    }


    private class recycleAdapter extends RecyclerView.Adapter<recycleAdapter.viewholder> {

        @Override
        public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.book_row, parent, false);

            return new viewholder(v);
        }

        @Override
        public void onBindViewHolder(viewholder holder, int position) {
            holder.imageView.setImageResource(ic_launcher_round);
            holder.bookName.setText("The Hardy Boys");
            holder.bookDescrp.setText("The Disappearing Floor");


        }

        @Override
        public int getItemCount() {
            return 10;
        }

        class viewholder extends RecyclerView.ViewHolder {
            CircleImageView imageView;
            TextView bookName;
            TextView bookDescrp;

            viewholder(View itemView) {
                super(itemView);
                imageView = (CircleImageView) itemView.findViewById(R.id.profile_image);
                bookName = (TextView) itemView.findViewById(R.id.book_name);
                bookDescrp = (TextView) itemView.findViewById(R.id.book_description);

            }
        }
    }

}
