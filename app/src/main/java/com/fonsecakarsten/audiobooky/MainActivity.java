package com.fonsecakarsten.audiobooky;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.fonsecakarsten.audiobooky.Barcode.BarcodeCaptureActivity;
import com.fonsecakarsten.audiobooky.Database.BookContract.bookEntry;
import com.fonsecakarsten.audiobooky.Database.BookDbHelper;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS};
    private ArrayList<AudioBook> mBooks = new ArrayList<>();
    private ArrayList<String> mBooksTitle = new ArrayList<>();
    private ArrayList<String> mBooksAuthor = new ArrayList<>();
    private ArrayList<String> mBooksGraphic = new ArrayList<>();
    private ArrayList<String> mBooksAbsolutePath = new ArrayList<>();
    private recycleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

        // Set up recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mainRecView);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));

        // Set up Floating action button
        FloatingActionButton captureBtn = (FloatingActionButton) findViewById(R.id.FAB1);
        captureBtn.bringToFront();
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewBook();
            }
        });

        readDatabase();
    }

    // Create and/or open the database and get the book list
    private void readDatabase() {
        // instantiate subclass of SQLiteOpenHelper
        BookDbHelper mDbHelper = new BookDbHelper(this);

        // Create and/or open a database to read from it, this allows for read access as well
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] columnsToGet = {bookEntry.COLUMN_NAME_TITLE, bookEntry.COLUMN_NAME_AUTHOR, bookEntry.COLUMN_NAME_COVER_IMAGE_PATH, bookEntry.COLUMN_NAME_ABSOLUTE_PATH};

        // SELECT columnsToGet FROM table_name
        Cursor c = db.query(
                bookEntry.TABLE_NAME,    // The table to query
                columnsToGet,            // The columns to return
                null,                    // The columns for the WHERE clause
                null,                    // The values for the WHERE clause
                null,                    // don't group the rows
                null,                    // don't filter by row groups
                null);                   // The sort order

        if (c.getCount() > mBooksTitle.size()) {
            // Get all the chapter names and add it to the arraylist
            mBooksTitle.clear();
            mBooksAuthor.clear();
            mBooksGraphic.clear();
            mBooksAbsolutePath.clear();
            while (c.moveToNext()) {
                int bookNameColumn = c.getColumnIndex(bookEntry.COLUMN_NAME_TITLE);
                int bookAuthorColumn = c.getColumnIndex(bookEntry.COLUMN_NAME_AUTHOR);
                int bookGraphic = c.getColumnIndex(bookEntry.COLUMN_NAME_COVER_IMAGE_PATH);
                int bookGraphicAbsolutePath = c.getColumnIndex(bookEntry.COLUMN_NAME_ABSOLUTE_PATH);
                mBooksTitle.add(c.getString(bookNameColumn));
                mBooksAuthor.add(c.getString(bookAuthorColumn));
                mBooksGraphic.add(c.getString(bookGraphic));
                mBooksAbsolutePath.add(c.getString(bookGraphicAbsolutePath));
            }
            mAdapter.notifyDataSetChanged();
        }
        c.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        readDatabase();
    }

    private void addNewBook() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.newbook_dialog, (ViewGroup) findViewById(R.id.newbook_dialog_root), false);
        final EditText title = (EditText) layout.findViewById(R.id.dialog_title);
        final EditText author = (EditText) layout.findViewById(R.id.dialog_author);

        AlertDialog newBookDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setTitle("Add a new audiobook!")
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
//
//                      // TODO
//                      // Check if either textboxes are empty
//                      // ISBN mobile vision activity
                        Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                        intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                        intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                        startActivityForResult(intent, 1);

                    }
                })
                .create();
        title.requestFocus();
        newBookDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        newBookDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                BookInfoAsync task = new BookInfoAsync(data.getStringExtra("BarCodeString"),
                        getApplicationContext(), MainActivity.this);
                task.execute();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //    // Check if permissions granted
//    public static boolean hasPermissions(Context context, String... permissions)  {
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
//            for (String permission : permissions) {
//                int idk = ActivityCompat.checkSelfPermission(context, permission);
//                System.out.println(idk);
//                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    // If permissions not granted, request it
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        int granted = PackageManager.PERMISSION_GRANTED;
//        switch (requestCode) {
//            case RequestPermissionCode:
//                //System.out.println("HELLO");
//                if (grantResults.length > 0 &&
//                        grantResults[0] == granted &&
//                        grantResults[1] == granted &&
//                        grantResults[2] == granted) {
//                    // Permissions all granted
//                    break;
//                } else {
//                    // One or more permissions denied, re-request permissions
//                    ActivityCompat.requestPermissions(this, PERMISSIONS, RequestPermissionCode);
//                }
//        }
//    }

    private class recycleAdapter extends RecyclerView.Adapter<recycleAdapter.viewholder> {

        @Override
        public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.book_row, parent, false);

            return new viewholder(v);
        }

        @Override
        public void onBindViewHolder(final viewholder holder, int position) {

            Glide.with(MainActivity.this).load(Uri.parse(mBooksGraphic.get(position))).into(holder.imageView);
            holder.bookName.setText(mBooksTitle.get(position));
            holder.bookAuthor.setText(mBooksAuthor.get(position));

            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go to bookActivity
                    Intent intent = new Intent(getApplicationContext(), BookActivity.class);

                    intent.putExtra("BOOK_TITLE", mBooksTitle.get(holder.getAdapterPosition()));
                    intent.putExtra("BOOK_AUTHOR", mBooksAuthor.get(holder.getAdapterPosition()));
                    intent.putExtra("BOOK_GRAPHIC", mBooksGraphic.get(holder.getAdapterPosition()));
                    intent.putExtra("BOOK_GRAPHIC_ABSOLUTEPATH", mBooksAbsolutePath.get(holder.getAdapterPosition()));

                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mBooksTitle.size();
        }

        class viewholder extends RecyclerView.ViewHolder {
            CircleImageView imageView;
            TextView bookName;
            TextView bookAuthor;
            RelativeLayout root;

            viewholder(View itemView) {
                super(itemView);
                root = (RelativeLayout) itemView.findViewById(R.id.book_row_root);
                imageView = (CircleImageView) itemView.findViewById(R.id.profile_image);
                bookName = (TextView) itemView.findViewById(R.id.book_name);
                bookAuthor = (TextView) itemView.findViewById(R.id.book_author);

            }
        }
    }
}
