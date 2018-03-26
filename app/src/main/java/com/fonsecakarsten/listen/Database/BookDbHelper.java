package com.fonsecakarsten.listen.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fonsecakarsten.listen.Database.BookContract.bookEntry;


/**
 * Created by Karsten on 7/14/2017.
 */

public class BookDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "AudioBookList.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + bookEntry.TABLE_NAME + " (" +
                    bookEntry._ID + " INTEGER PRIMARY KEY," +
                    bookEntry.COLUMN_NAME_TITLE + " TEXT," +
                    bookEntry.COLUMN_NAME_SUBTITLE + " TEXT," +
                    bookEntry.COLUMN_NAME_AUTHOR + " TEXT," +
                    bookEntry.COLUMN_NAME_DESCRIPTION + " TEXT," +
                    bookEntry.COLUMN_NAME_PUBLISHER + " TEXT," +
                    bookEntry.COLUMN_NAME_PUBLISH_DATE + " TEXT," +
                    bookEntry.COLUMN_NAME_ISBN + " TEXT," +
                    bookEntry.COLUMN_NAME_RATING + " INTEGER," +
                    bookEntry.COLUMN_NAME_COVER_IMAGE_PATH + " TEXT," +
                    bookEntry.COLUMN_NAME_ABSOLUTE_PATH + " TEXT," +
                    bookEntry.COLUMN_NAME_CONTENT_COLOR + " INTEGER," +
                    bookEntry.COLUMN_NAME_STATUS_COLOR + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + bookEntry.TABLE_NAME;

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // CREATE TABLE pets (_id INTEGER, chapterName TEXT, chapterData TEXT (JSON));
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
