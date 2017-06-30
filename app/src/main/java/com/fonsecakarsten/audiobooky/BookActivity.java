package com.fonsecakarsten.audiobooky;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class BookActivity extends AppCompatActivity {
    static final int REQUEST_CODE_PICK_ACCOUNT = 11;
    static final int REQUEST_ACCOUNT_AUTHORIZATION = 12;
    private recycleAdapter mAdapter;
    private ArrayList<String> mImageArray;
    private static String accessToken;
    private AudioBook book;
    private Intent fromCA;
    Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

        // Set up recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.book_recview);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));

        // Get title
        fromCA = getIntent();
        book = (AudioBook) fromCA.getSerializableExtra("newBook");
        String title = book.getTitle();

        if (checkFileExists(title)) {
            openBook(title);
        } else {
            saveBook(title);
        }

        // Set up collapsing toolbar complex view
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(book.getTitle());
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
//        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
//        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));

    }

    // Get results from newCaptureActivity and process images, converting them with CloudVisionAsync
    private void getResults() {
        accessToken = fromCA.getExtras().getString("token");
        mImageArray = fromCA.getExtras().getStringArrayList("imageArray");
        for (int i = 0; i < mImageArray.size(); i++) {
            CloudVisionAsync task = new CloudVisionAsync(accessToken, mImageArray.get(i));
            try {
                book.setPageText(task.get().get(0));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    // Check if audio book file already exists
    public boolean checkFileExists(String fname) {
        File appDir = getApplicationContext().getDir("books", Context.MODE_PRIVATE);
        File subDirectory = new File(appDir, fname);
        return subDirectory.exists();
    }

    // Open saved audio book
    public void openBook(String title) {
        File mydir = getApplicationContext().getDir("books", Context.MODE_PRIVATE);
        File bookFile = new File(mydir, title);
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            //fis = getApplicationContext().openFileInput(title);
            fis = new FileInputStream(bookFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            is = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            book = (AudioBook) is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create new audio book
    public void saveBook(String title) {
        File mydir = getApplicationContext().getDir("books", Context.MODE_PRIVATE);
        File bookFile = new File(mydir, title);
        FileOutputStream fos = null;
        ObjectOutputStream os = null;

        try {
            //fos = getApplicationContext().openFileOutput(title, Context.MODE_PRIVATE);
            fos = new FileOutputStream(bookFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            os = new ObjectOutputStream(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.writeObject(book);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add a chapter to the current audiobook
    public void addChapter() {
        getAuthToken();
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.newchapter_dialog, (ViewGroup) findViewById(R.id.newchapter_dialog_root), false);
        final EditText title = (EditText) layout.findViewById(R.id.chapter_title);

        AlertDialog newChapterDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setTitle("Add a new chapter!")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // TODO
                        // Check if either textboxes are empty
                        // Should be startActivity for result
                        Intent intent = new Intent(getApplicationContext(), NewCaptureActivity.class);
                        startActivity(intent);
                    }
                })
                .create();
        title.requestFocus();
        newChapterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        newChapterDialog.show();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
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
                Toast.makeText(this, "You need to select an account", Toast.LENGTH_SHORT).show();
                pickUserAccount();
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
            new GetTokenTask(BookActivity.this, mAccount).execute();
        }
    }

    public void onTokenReceived(String token) {
        accessToken = token;
    }

    private class recycleAdapter extends RecyclerView.Adapter<BookActivity.recycleAdapter.viewholder> {

        @Override
        public BookActivity.recycleAdapter.viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.chapter_row, parent, false);
            return new BookActivity.recycleAdapter.viewholder(v);
        }

        @Override
        public void onBindViewHolder(BookActivity.recycleAdapter.viewholder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 20;
        }

        class viewholder extends RecyclerView.ViewHolder {
            TextView chapterName;
            RelativeLayout root;

            viewholder(View itemView) {
                super(itemView);
                root = (RelativeLayout) itemView.findViewById(R.id.chapter_row_root);
                chapterName = (TextView) itemView.findViewById(R.id.chapter_name);
            }
        }
    }

}
