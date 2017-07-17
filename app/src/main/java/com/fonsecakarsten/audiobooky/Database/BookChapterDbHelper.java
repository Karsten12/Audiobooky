package com.fonsecakarsten.audiobooky.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fonsecakarsten.audiobooky.Database.BookContract.bookChapterEntry;

/**
 * Created by Karsten on 7/13/2017.
 */

public class BookChapterDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + bookChapterEntry.TABLE_NAME + " (" +
                    bookChapterEntry._ID + " INTEGER PRIMARY KEY," +
                    bookChapterEntry.COLUMN_NAME_TITLE + " TEXT," +
                    bookChapterEntry.COLUMN_NAME_CHAPTER_DATA + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + bookChapterEntry.TABLE_NAME;

    public BookChapterDbHelper(Context context, String dbName) {
        super(context, dbName, null, DATABASE_VERSION);
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
