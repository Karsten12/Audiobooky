package com.fonsecakarsten.audiobooky.Database;

import android.provider.BaseColumns;

/**
 * Created by Karsten on 7/13/2017.
 */

public class BookContract {

    private BookContract() {
    }

    public static class bookChapterEntry implements BaseColumns {
        public static final String TABLE_NAME = "bookChapterEntry";
        public static final String COLUMN_NAME_CHAPTER_TITLE = "chapterName";
        public static final String COLUMN_NAME_CHAPTER_DATA = "chapterData";
    }

    public static class bookEntry implements BaseColumns {
        public static final String TABLE_NAME = "book_List";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_SUBTITLE = "subtitle";
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_PUBLISHER = "publisher";
        public static final String COLUMN_NAME_PUBLISH_DATE = "publish_date";
        public static final String COLUMN_NAME_ISBN = "ISBN";
        public static final String COLUMN_NAME_RATING = "rating";
        public static final String COLUMN_NAME_COVER_IMAGE_PATH = "cover_image_path";
        public static final String COLUMN_NAME_ABSOLUTE_PATH = "absolute_image_path";
        public static final String COLUMN_NAME_CONTENT_COLOR = "content_color";
        public static final String COLUMN_NAME_STATUS_COLOR = "status_color";
    }


}
