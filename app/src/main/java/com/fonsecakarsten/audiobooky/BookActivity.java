package com.fonsecakarsten.audiobooky;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class BookActivity extends AppCompatActivity {
    static final int REQUEST_CODE_PICK_ACCOUNT = 11;
    static final int REQUEST_ACCOUNT_AUTHORIZATION = 12;
    private static String accessToken;
    Account mAccount;
    private recycleAdapter mAdapter;
    private AudioBook book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        ImageView imageView = (ImageView) findViewById(R.id.book_image);

        // Get book from calling Activity
        book = (AudioBook) getIntent().getSerializableExtra("newBook");

        Glide.with(this).load(Uri.parse(book.getCoverImagePath())).into(imageView);

        // Set up recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.book_recview);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));

        // Set up collapsing toolbar complex view
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(book.getTitle());
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
//        Palette palette = createPaletteSync();
//        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
//        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)));

        FloatingActionButton playFab = (FloatingActionButton) findViewById(R.id.play_fab);
        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.add_chapter_fab);
        playFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChapter();
            }
        });

    }
    // Create new audio book
    public void saveBook() {
        File mydir = getApplicationContext().getDir("books", Context.MODE_PRIVATE);
        File bookFile = new File(mydir, book.getTitle());
        FileOutputStream fos = null;
        ObjectOutputStream os = null;

        try {
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
                        // Check if title textbox is empty
                        Intent intent = new Intent(getApplicationContext(), NewCaptureActivity.class);
                        startActivityForResult(intent, 1);
                    }
                })
                .create();
        title.requestFocus();
        newChapterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        newChapterDialog.show();

    }

    // Get images from newCaptureActivity and process them, converting them with CloudVisionAsync
    private void processImages(ArrayList<String> imageArray) {
        for (int i = 0; i < imageArray.size(); i++) {
            CloudVisionAsync task = new CloudVisionAsync(accessToken, imageArray.get(i));
            try {
                book.setPageText(task.get().get(0));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            // Process images for a new chapter
            if (resultCode == RESULT_OK) {
                processImages(data.getExtras().getStringArrayList("imageArray"));
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

    @Override
    protected void onPause() {
        super.onPause();
        saveBook();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveBook();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveBook();
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

    // Generate palette synchronously and return it
//    public Palette createPaletteSync() {
//        Palette p = Palette.from(book.getCoverImage()).generate();
//        return p;
//    }

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
