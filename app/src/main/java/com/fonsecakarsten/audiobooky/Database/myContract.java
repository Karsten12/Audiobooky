package com.fonsecakarsten.audiobooky.Database;

import android.provider.BaseColumns;

/**
 * Created by Karsten on 7/13/2017.
 */

public class myContract {

    private myContract() {
    }

    public static class bookEntry implements BaseColumns {
        public static final String TABLE_NAME = "bookEntry";
        public static final String COLUMN_NAME_TITLE = "chapterName";
        public static final String COLUMN_NAME_CHAPTER_DATA = "chapterData";
    }
}
