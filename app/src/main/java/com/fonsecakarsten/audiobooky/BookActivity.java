package com.fonsecakarsten.audiobooky;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
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
import com.fonsecakarsten.audiobooky.Database.BookChapterDbHelper;
import com.fonsecakarsten.audiobooky.Database.BookContract;
import com.fonsecakarsten.audiobooky.Database.BookContract.bookChapterEntry;
import com.fonsecakarsten.audiobooky.Database.BookDbHelper;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class BookActivity extends AppCompatActivity {
    static final int REQUEST_CODE_PICK_ACCOUNT = 11;
    static final int REQUEST_ACCOUNT_AUTHORIZATION = 12;
    private static String accessToken;
    Account mAccount;
    private recycleAdapter mAdapter;
    private ArrayList<String> mChapters = new ArrayList<>();
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
        bookTitle = extras.getString("BOOK_TITLE");
        String bookAuthor = extras.getString("BOOK_AUTHOR");
        String bookGraphic = extras.getString("BOOK_GRAPHIC");
        bookGraphicAbsolutePath = extras.getString("BOOK_GRAPHIC_ABSOLUTEPATH");

        // Set imageView to book cover
        ImageView imageView = (ImageView) findViewById(R.id.book_image);
        Glide.with(this).load(Uri.parse(bookGraphic)).into(imageView);


        // If database exists, then Calling activity is MainActivity, so read Database for chapterList
        if (checkDatabaseExist(this, bookTitle)) {
            readDatabase();
        } else {
            // Database doesn't exist. Need to add it to bookDatabase and create a new database for this book
            addBookToDatabase(bookAuthor, bookGraphic);
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
        Palette palette = createPaletteSync();
        //collapsingToolbarLayout.setCollapsedTitleTextColor(palette.getMutedSwatch().getTitleTextColor());
        //collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        //collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark)));
        getWindow().setStatusBarColor(palette.getMutedColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));

        FloatingActionButton playFab = (FloatingActionButton) findViewById(R.id.play_fab);
        FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.add_chapter_fab);
        playFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChapter(lastChapterRead);

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
                .setTitle("Add a new chapter!")
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!title.getText().toString().isEmpty()) {
                            tempChapTitle = title.getText().toString();
                            Intent intent = new Intent(getApplicationContext(), NewCaptureActivity.class);
                            startActivityForResult(intent, 1);
                        } else {
                            // TODO
                            // Keep dialog open if title string is empty
                            Toast.makeText(BookActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create();
        title.requestFocus();
        newChapterDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        newChapterDialog.show();

    }

    // Get images from newCaptureActivity and process them, converting them with CloudVisionAsync
    private void processImages(ArrayList<String> imageArray) {
        ArrayList<String> chapterText = new ArrayList<>();
        for (int i = 0; i < imageArray.size(); i++) {
            // Process each image 1 at a time
            CloudVisionAsync task = new CloudVisionAsync(accessToken, imageArray.get(i));
            try {
                //book.setPageText(task.get().get(0));
                chapterText.add(task.get().get(0));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        // TODO
        // Add chapterText to the dataBase
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


    public void getBookDetails() {
        String Selection = BookContract.bookEntry.COLUMN_NAME_TITLE + "=?";
        String[] rowQuery = {bookTitle};
        BookDbHelper bookDatabase = new BookDbHelper(this);
        Cursor c1 = bookDatabase.getReadableDatabase().query(
                BookContract.bookEntry.TABLE_NAME,      // queries the list of books
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
            int chapterNameColumn = c.getColumnIndex(bookChapterEntry.COLUMN_NAME_TITLE);
            mChapters.add(c.getString(chapterNameColumn));
        }
        c.close();
    }

    // Add the new chapter to the database
    public void writeDatabase(String chapterTitle, ArrayList<String> chapterText) {

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        // Add chapter title
        values.put(bookChapterEntry.COLUMN_NAME_TITLE, chapterTitle);

        // Add arrayList containing the chapterText
        JSONObject json = new JSONObject();
        try {
            json.put("chapterArray", new JSONArray(chapterText));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String arrayList = json.toString();
        values.put(bookChapterEntry.COLUMN_NAME_CHAPTER_DATA, arrayList);

        // Insert the new row, returning the primary key value of the new row
        db.insert(bookChapterEntry.TABLE_NAME, null, values);
    }

    // Add the newBook to the Bookdatabase
    private void addBookToDatabase(String Author, String bookGraphic) {

        AudioBook book = (AudioBook) getIntent().getSerializableExtra("newBook");
        ContentValues values = new ContentValues();

        values.put(BookContract.bookEntry.COLUMN_NAME_TITLE, bookTitle);
        values.put(BookContract.bookEntry.COLUMN_NAME_AUTHOR, Author);
        values.put(BookContract.bookEntry.COLUMN_NAME_COVER_IMAGE_PATH, bookGraphic);
        values.put(BookContract.bookEntry.COLUMN_NAME_ABSOLUTE_PATH, bookGraphicAbsolutePath);

        values.put(BookContract.bookEntry.COLUMN_NAME_SUBTITLE, book.getSubtitle());
        values.put(BookContract.bookEntry.COLUMN_NAME_DESCRIPTION, book.getDescription());
        values.put(BookContract.bookEntry.COLUMN_NAME_PUBLISHER, book.getPublisher());
        values.put(BookContract.bookEntry.COLUMN_NAME_PUBLISH_DATE, book.getPublishDate());
        values.put(BookContract.bookEntry.COLUMN_NAME_ISBN, book.getISBN());
        values.put(BookContract.bookEntry.COLUMN_NAME_RATING, book.getRating());
        values.put(BookContract.bookEntry.COLUMN_NAME_COVER_IMAGE_PATH, bookGraphic);

        SQLiteDatabase bookDatabase = new BookDbHelper(this).getWritableDatabase();
        bookDatabase.insert(BookContract.bookEntry.TABLE_NAME, null, values);

        // Create the database for this book
        BookChapterDbHelper mDbHelper = new BookChapterDbHelper(this, bookTitle);
        db = mDbHelper.getReadableDatabase();
    }

    // Get the chapter
    private void getChapter(int chapter) {
        // TODO
        // Pass text into readActivity
        String selection = bookChapterEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(chapter)};

        // SELECT all_columns FROM table_name WHERE bookChapterEntry._ID == chapter
        Cursor c = db.query(
                bookChapterEntry.TABLE_NAME,    // The table to query
                null,                           // The columns to return
                selection,                      // The columns for the WHERE clause
                selectionArgs,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                null);                          // The sort order
        int chapterNameColumn = c.getColumnIndex(bookChapterEntry.COLUMN_NAME_TITLE);
        int chapterDataColumn = c.getColumnIndex(bookChapterEntry.COLUMN_NAME_CHAPTER_DATA);
        String chapterName = c.getString(chapterNameColumn);
        String idk = c.getString(chapterDataColumn);
        c.close();

        ArrayList<String> chapterText = new ArrayList<>();
        JSONObject json = null;
        JSONArray array = null;
        try {
            json = new JSONObject(idk);
            array = json.getJSONArray("chapterArray");
            for (int i = 0; i < array.length(); i++) {
                chapterText.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Only if the chapter text size is > 0, then go to readActivity
        if (chapterText.size() > 0) {
            Intent intent = new Intent(this, readChapterActivity.class);
            intent.putExtra("CHAPTER_NAME", chapterName);
            intent.putStringArrayListExtra("CHAPTER_TEXT", chapterText);
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //saveBook();
        //readDatabase(null);
    }

    // Generate palette synchronously and return it
    private Palette createPaletteSync() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;
        Bitmap bmp = null;

        try {
            bmp = BitmapFactory.decodeStream(new FileInputStream(new File(bookGraphicAbsolutePath)), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return Palette.from(bmp).generate();
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
        public void onBindViewHolder(final BookActivity.recycleAdapter.viewholder holder, final int position) {
            holder.chapterName.setText(mChapters.get(position));
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getChapter(holder.getAdapterPosition());
                    // TODO
                    // Intent to reading activity
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

            viewholder(View itemView) {
                super(itemView);
                root = (RelativeLayout) itemView.findViewById(R.id.chapter_row_root);
                chapterName = (TextView) itemView.findViewById(R.id.chapter_name);
            }
        }
    }

}
