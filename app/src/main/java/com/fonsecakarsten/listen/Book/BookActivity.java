package com.fonsecakarsten.listen.Book;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fonsecakarsten.listen.CloudVisionAsync;
import com.fonsecakarsten.listen.Database.BookChapterDbHelper;
import com.fonsecakarsten.listen.Database.BookContract.bookChapterEntry;
import com.fonsecakarsten.listen.Database.BookContract.bookEntry;
import com.fonsecakarsten.listen.Database.BookDbHelper;
import com.fonsecakarsten.listen.GetTokenTask;
import com.fonsecakarsten.listen.R;
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
    private String[] bookInfo;
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
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        bookTitle = extras.getString(getString(R.string.book_title));
        String bookAuthor = extras.getString(getString(R.string.book_author));
        String bookGraphic = extras.getString(getString(R.string.book_graphic));
        bookGraphicAbsolutePath = extras.getString(getString(R.string.book_graphic_absolutePath));
        int content_color = extras.getInt(getString(R.string.cont_color));
        int status_color = extras.getInt(getString(R.string.stat_color));

        // Set imageView to book cover
        ImageView imageView = findViewById(R.id.book_image);
        Glide.with(this).load(Uri.parse(bookGraphic)).into(imageView);


        // If database exists, then Calling activity is MainActivity, so read Database for chapterList
        if (checkDatabaseExist(this, bookTitle)) {
            readDatabase();
        } else {
            // Database doesn't exist. Need to add it to bookDatabase and create a new database for this book
            addBookToDatabase(bookAuthor, bookGraphic, content_color, status_color);
        }

        // Set up recyclerView
        RecyclerView recyclerView = findViewById(R.id.book_recview);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));

        // Set up collapsing toolbar complex view
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(bookTitle);
        //collapsingToolbarLayout.setCollapsedTitleTextColor(palette.getMutedSwatch().getTitleTextColor());
        //collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        collapsingToolbarLayout.setContentScrimColor(content_color);
        //collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)));
        getWindow().setStatusBarColor(status_color);

        FloatingActionButton playFab = findViewById(R.id.play_fab);
        FloatingActionButton addFab = findViewById(R.id.add_chapter_fab);
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
        final EditText title = layout.findViewById(R.id.chapter_title);

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
                            Intent intent = new Intent(getApplicationContext(), AddChapterActivity.class);
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


    private void getBookDetails() {

        if (bookInfo == null) {
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

            c1.moveToFirst();
            bookInfo = new String[6];
            bookInfo[0] = c1.getString(c1.getColumnIndex(bookEntry.COLUMN_NAME_SUBTITLE));
            bookInfo[1] = c1.getString(c1.getColumnIndex(bookEntry.COLUMN_NAME_AUTHOR));
            bookInfo[2] = String.valueOf(c1.getInt(c1.getColumnIndex(bookEntry.COLUMN_NAME_RATING)));
            bookInfo[3] = c1.getString(c1.getColumnIndex(bookEntry.COLUMN_NAME_DESCRIPTION));
            bookInfo[4] = c1.getString(c1.getColumnIndex(bookEntry.COLUMN_NAME_PUBLISH_DATE));
            bookInfo[5] = c1.getString(c1.getColumnIndex(bookEntry.COLUMN_NAME_ISBN));
            c1.close();
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.book_info, (ViewGroup) findViewById(R.id.book_info_root), false);

        TextView subTitle = layout.findViewById(R.id.sub_title);
        TextView bookAuthor = layout.findViewById(R.id.book_author);
        RatingBar ratingBar = layout.findViewById(R.id.ratingBar);
        TextView description = layout.findViewById(R.id.description);
        TextView publishDate = layout.findViewById(R.id.publish_date);
        TextView ISBN = layout.findViewById(R.id.ISBN);

        subTitle.setText(bookInfo[0]);
        bookAuthor.setText(bookInfo[1]);
        ratingBar.setRating(Float.parseFloat(bookInfo[2]));
        description.setText(bookInfo[3]);
        publishDate.setText(bookInfo[4]);
        ISBN.setText(bookInfo[5]);


        AlertDialog newChapterDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setTitle(bookTitle)
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        newChapterDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                finish();
                overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
                return true;
            case R.id.button_info:
                getBookDetails();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.left_to_right, R.anim.right_to_left);
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

        // Get all the chapter names and add it to the arrayList
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
//        CloudVisionAsync task = new CloudVisionAsync(accessToken, tempChapTitle, imageArray, db, mAdapter, mChaptersReady, position);
        CloudVisionAsync task = new CloudVisionAsync(accessToken, tempChapTitle, imageArray, db, mAdapter, mChaptersReady, position, this);
        task.execute();
//        MobileVisionAsync task = new MobileVisionAsync(imageArray.get(0), this);
//        task.execute();
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

    public class recycleAdapter extends RecyclerView.Adapter<recycleAdapter.viewHolder> {

        @Override
        public viewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.chapter_row, parent, false);
            return new viewHolder(v);
        }

        @Override
        public void onBindViewHolder(final viewHolder holder, int position) {
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

        class viewHolder extends RecyclerView.ViewHolder {
            TextView chapterName;
            RelativeLayout root;
            ProgressBar progressBar;

            viewHolder(View itemView) {
                super(itemView);
                root = itemView.findViewById(R.id.chapter_row_root);
                chapterName = itemView.findViewById(R.id.chapter_name);
                progressBar = itemView.findViewById(R.id.progressBar2);
            }
        }
    }

}
