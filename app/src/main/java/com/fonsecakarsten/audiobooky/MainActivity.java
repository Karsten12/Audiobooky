package com.fonsecakarsten.audiobooky;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fonsecakarsten.audiobooky.NewCapureActivity.RequestPermissionCode;
import static com.fonsecakarsten.audiobooky.R.mipmap.ic_launcher_round;

public class MainActivity extends Activity {
    private ArrayList<AudioBook> mBooks = new ArrayList<>();
    private ArrayList<String> mImageArray;
    private recycleAdapter mAdapter;
    private AudioBook newBook;

    private static String accessToken;
    static final int REQUEST_CODE_PICK_ACCOUNT = 11;
    static final int REQUEST_ACCOUNT_AUTHORIZATION = 12;
    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mainRecView);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);

        FloatingActionButton captureBtn = (FloatingActionButton) findViewById(R.id.FAB1);
        captureBtn.bringToFront();
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewBook();
            }
        });


        if (!hasPermission()) {
            ActivityCompat.requestPermissions(this, getResources().getStringArray(R.array.permissions), RequestPermissionCode);
        }
        //getAuthToken();
    }

    // Convert each image's text using OCR
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mImageArray = data.getExtras().getStringArrayList("imageArray");
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
        if (mAccount == null) {
            pickUserAccount();
        } else {
            new GetTokenTask(MainActivity.this, mAccount).execute();
        }
    }

    public void onTokenReceived(String token) {
        accessToken = token;
    }

    public void createNewBook() {
        AlertDialog.Builder newBookDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.newbook_dialog, (ViewGroup) findViewById(R.id.dialog_root), false);
        newBookDialog.setView(layout);

        final EditText title = (EditText) layout.findViewById(R.id.dialog_title);
        final EditText author = (EditText) layout.findViewById(R.id.dialog_author);

        newBookDialog.setTitle("Create a new audiobook!");
        newBookDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        newBookDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                newBook = new AudioBook();
                newBook.setTitle(title.getText().toString());
                newBook.setAuthor(author.getText().toString());

                Intent intent = new Intent(getApplicationContext(), NewCapureActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        newBookDialog.create();
        newBookDialog.show();
    }

    // Check if permissions granted
    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permArray = getResources().getStringArray(R.array.permissions);
            for (String perm1 : permArray) {
                int idk = ActivityCompat.checkSelfPermission(this, perm1);
            }
        }
        return true;
    }

    // If permissions not granted, request it
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int granted = PackageManager.PERMISSION_GRANTED;
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0 &&
                        grantResults[0] == granted &&
                        grantResults[1] == granted &&
                        grantResults[2] == granted) {
                    // Permissions all granted
                    break;
                } else {
                    // One or more permissions denied, re-request permissions
                    ActivityCompat.requestPermissions(this, getResources().getStringArray(R.array.permissions), RequestPermissionCode);
                }
        }
    }

    private class recycleAdapter extends RecyclerView.Adapter<recycleAdapter.viewholder> {

        @Override
        public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.book_row, parent, false);

            return new viewholder(v);
        }

        @Override
        public void onBindViewHolder(viewholder holder, int position) {
//            holder.imageView.setImageBitmap(mBooks.get(position).getCoverImage());
//            holder.bookName.setText(mBooks.get(position).getTitle());
//            holder.bookAuthor.setText(mBooks.get(position).getAuthor());
            holder.imageView.setImageResource(ic_launcher_round);
            holder.bookName.setText("The Hardy Boys: The Disappearing Floor");
            holder.bookAuthor.setText("Franklin W. Dixon");


        }

        @Override
        public int getItemCount() {
            //return mBooks.size();
            return 10;
        }

        class viewholder extends RecyclerView.ViewHolder {
            CircleImageView imageView;
            TextView bookName;
            TextView bookAuthor;

            viewholder(View itemView) {
                super(itemView);
                imageView = (CircleImageView) itemView.findViewById(R.id.profile_image);
                bookName = (TextView) itemView.findViewById(R.id.book_name);
                bookAuthor = (TextView) itemView.findViewById(R.id.book_author);

            }
        }
    }

}
