package com.fonsecakarsten.audiobooky.Book;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fonsecakarsten.audiobooky.AddBookActivity;
import com.fonsecakarsten.audiobooky.CloudVisionAsync;
import com.fonsecakarsten.audiobooky.Database.BookChapterDbHelper;
import com.fonsecakarsten.audiobooky.Database.BookContract.bookChapterEntry;
import com.fonsecakarsten.audiobooky.Database.BookContract.bookEntry;
import com.fonsecakarsten.audiobooky.Database.BookDbHelper;
import com.fonsecakarsten.audiobooky.GetTokenTask;
import com.fonsecakarsten.audiobooky.R;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.io.File;
import java.util.ArrayList;


public class BookActivity extends AppCompatActivity {
    public static final int REQUEST_ACCOUNT_AUTHORIZATION = 12;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 11;
    private static String accessToken;
    private Account mAccount;
    private recycleAdapter mAdapter;
    private ArrayList<String> mChapters = new ArrayList<>();
    private ArrayList<Boolean> mChaptersReady = new ArrayList<>();
    private String bookTitle;
    private String bookGraphicAbsolutePath;
    private String tempChapTitle = null;
    private SQLiteDatabase db;
    private int lastChapterRead;

    private boolean checkDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        bookTitle = extras.getString(getString(R.string.book_title));
        String bookAuthor = extras.getString(getString(R.string.book_author));
        String bookGraphic = extras.getString(getString(R.string.book_graphic));
        bookGraphicAbsolutePath = extras.getString(getString(R.string.book_graphic_absolutePath));
        int content_color = extras.getInt("CONTENT_COLOR");
        int status_color = extras.getInt("STATUS_COLOR");

        // Set imageView to book cover
        ImageView imageView = (ImageView) findViewById(R.id.book_image);
        Glide.with(this).load(Uri.parse(bookGraphic)).into(imageView);


        // If database exists, then Calling activity is MainActivity, so read Database for chapterList
        if (checkDatabaseExist(this, bookTitle)) {
            readDatabase();
        } else {
            // Database doesn't exist. Need to add it to bookDatabase and create a new database for this book
            addBookToDatabase(bookAuthor, bookGraphic, content_color, status_color);
        }

        // Set up recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.book_recview);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));

        // Set up collapsing toolbar complex view
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(bookTitle);
        //collapsingToolbarLayout.setCollapsedTitleTextColor(palette.getMutedSwatch().getTitleTextColor());
        //collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        collapsingToolbarLayout.setContentScrimColor(content_color);
        //collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)));
        getWindow().setStatusBarColor(status_color);

        FloatingActionButton playFab = (FloatingActionButton) findViewById(R.id.play_fab);
        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.add_chapter_fab);
        playFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readChapter(lastChapterRead);

            }
        });
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addChapter();
            }
        });
    }

    // Add a chapter to the current audioBook
    private void addChapter() {
        getAuthToken();
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.newchapter_dialog, (ViewGroup) findViewById(R.id.newchapter_dialog_root), false);
        final EditText title = (EditText) layout.findViewById(R.id.chapter_title);

        AlertDialog newChapterDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setTitle(R.string.newChapterString)
                .setCancelable(false)
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!title.getText().toString().isEmpty()) {
                            tempChapTitle = title.getText().toString();
                            Intent intent = new Intent(getApplicationContext(), AddBookActivity.class);
                            startActivityForResult(intent, 1);
                        } else {
                            // TODO
                            // Keep dialog open if title string is empty
                            Toast.makeText(BookActivity.this, R.string.TitleEmptyWarning, Toast.LENGTH_SHORT).show();
                        }
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

        if (requestCode == 1) {
            // Process images for a new chapter
            if (resultCode == RESULT_OK) {
                writeDatabase(data.getExtras().getStringArrayList("imageArray"));
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
                Toast.makeText(this, R.string.select_account, Toast.LENGTH_SHORT).show();
                pickUserAccount();
            }
        } else if (requestCode == REQUEST_ACCOUNT_AUTHORIZATION) {
            if (resultCode == RESULT_OK) {
                Bundle extra = data.getExtras();
                onTokenReceived(extra.getString("authtoken"));
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.auth_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void getBookDetails() {
        String Selection = bookEntry.COLUMN_NAME_TITLE + "=?";
        String[] rowQuery = {bookTitle};
        BookDbHelper bookDatabase = new BookDbHelper(this);
        Cursor c1 = bookDatabase.getReadableDatabase().query(
                bookEntry.TABLE_NAME,      // queries the list of books
                null,                                   // queries all columns
                Selection,                              // return the row (basically the book) where the id
                rowQuery,                               //  == tablePosition
                null,                                   // don't group the rows
                null,                                   // don't filter by row groups
                null);                                  // The sort order

        c1.close();
    }

    // Create and/or open the database and get the chapter list
    private void readDatabase() {
        // instantiate subclass of SQLiteOpenHelper
        BookChapterDbHelper mDbHelper = new BookChapterDbHelper(this, bookTitle);

        // Create and/or open a database to read from it, this allows for read access as well
        db = mDbHelper.getReadableDatabase();

        // SELECT all_columns FROM table_name
        Cursor c = db.query(
                bookChapterEntry.TABLE_NAME,    // The table to query
                null,                           // The columns to return
                null,                      // The columns for the WHERE clause
                null,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                null);                          // The sort order

        // Get all the chapter names and add it to the arraylist
        while (c.moveToNext()) {
            int chapterNameColumn = c.getColumnIndex(bookChapterEntry.COLUMN_NAME_CHAPTER_TITLE);
            mChapters.add(c.getString(chapterNameColumn));
            mChaptersReady.add(true);
        }
        c.close();
    }

    // Get images from newCaptureActivity and process them, converting them with CloudVisionAsync
    // Then adds the new chapter to the database
    private void writeDatabase(ArrayList<String> imageArray) {
        // Add book to chapterList to list
        mChapters.add(tempChapTitle);
        mChaptersReady.add(false);
        mAdapter.notifyDataSetChanged();
        int position = mChapters.indexOf(tempChapTitle);
        // chapterText is being processed, show indeterminate progressCircle
        CloudVisionAsync task = new CloudVisionAsync(accessToken, tempChapTitle, imageArray, db, mAdapter, mChaptersReady, position);
        task.execute();
    }

    // Add the newBook to the bookDatabase
    private void addBookToDatabase(String Author, String bookGraphic, int con_color, int stat_color) {

        AudioBook book = (AudioBook) getIntent().getSerializableExtra("newBook");
        ContentValues values = new ContentValues();

        values.put(bookEntry.COLUMN_NAME_TITLE, bookTitle);
        values.put(bookEntry.COLUMN_NAME_AUTHOR, Author);
        values.put(bookEntry.COLUMN_NAME_COVER_IMAGE_PATH, bookGraphic);
        values.put(bookEntry.COLUMN_NAME_ABSOLUTE_PATH, bookGraphicAbsolutePath);

        values.put(bookEntry.COLUMN_NAME_SUBTITLE, book.getSubtitle());
        values.put(bookEntry.COLUMN_NAME_DESCRIPTION, book.getDescription());
        values.put(bookEntry.COLUMN_NAME_PUBLISHER, book.getPublisher());
        values.put(bookEntry.COLUMN_NAME_PUBLISH_DATE, book.getPublishDate());
        values.put(bookEntry.COLUMN_NAME_ISBN, book.getISBN());
        values.put(bookEntry.COLUMN_NAME_RATING, book.getRating());
        values.put(bookEntry.COLUMN_NAME_COVER_IMAGE_PATH, bookGraphic);
        values.put(bookEntry.COLUMN_NAME_CONTENT_COLOR, con_color);
        values.put(bookEntry.COLUMN_NAME_STATUS_COLOR, stat_color);

        SQLiteDatabase bookDatabase = new BookDbHelper(this).getWritableDatabase();
        bookDatabase.insert(bookEntry.TABLE_NAME, null, values);

        // Create the database for this book
        BookChapterDbHelper mDbHelper = new BookChapterDbHelper(this, bookTitle);
        db = mDbHelper.getReadableDatabase();
    }

    // Go to ReadActivity
    private void readChapter(int chapter) {
        Intent intent = new Intent(this, ReadChapterActivity.class);
        intent.putExtra("BOOK_TITLE", bookTitle);
        intent.putExtra("CHAPTER_NAME", mChapters.get(chapter));
        startActivity(intent);
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

    public class recycleAdapter extends RecyclerView.Adapter<BookActivity.recycleAdapter.viewholder> {

        @Override
        public BookActivity.recycleAdapter.viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.chapter_row, parent, false);
            return new BookActivity.recycleAdapter.viewholder(v);
        }

        @Override
        public void onBindViewHolder(final BookActivity.recycleAdapter.viewholder holder, int position) {
            holder.chapterName.setText(mChapters.get(position));
            if (mChaptersReady.get(position)) {
                holder.progressBar.setVisibility(View.GONE);
            } else {
                holder.progressBar.setVisibility(View.VISIBLE);
            }
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    readChapter(holder.getAdapterPosition());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mChapters.size();
        }

        class viewholder extends RecyclerView.ViewHolder {
            TextView chapterName;
            RelativeLayout root;
            ProgressBar progressBar;

            viewholder(View itemView) {
                super(itemView);
                root = (RelativeLayout) itemView.findViewById(R.id.chapter_row_root);
                chapterName = (TextView) itemView.findViewById(R.id.chapter_name);
                progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar2);
            }
        }
    }

}
