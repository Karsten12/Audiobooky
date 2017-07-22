package com.fonsecakarsten.audiobooky;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.fonsecakarsten.audiobooky.Database.BookChapterDbHelper;
import com.fonsecakarsten.audiobooky.Database.BookContract.bookChapterEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Karsten on 7/16/2017.
 */

public class ReadChapterActivity extends AppCompatActivity {

    FragmentAdapter adapter;
    ViewPager mViewPager;
    String bookTitle;
    ArrayList<String> chapterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the chapter data
        Bundle extras = getIntent().getExtras();

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        bookTitle = extras.getString("CHAPTER_TITLE");
        toolbar.setTitle(bookTitle);
        setActionBar(toolbar);

        adapter = new FragmentAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);

    }

    private void getChapter() {
        BookChapterDbHelper mDbHelper = new BookChapterDbHelper(this, bookTitle);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String selection = bookChapterEntry.COLUMN_NAME_TITLE + "=?";
        String[] selectionArgs = {bookTitle};
        String[] columnsToReturn = {bookChapterEntry.COLUMN_NAME_CHAPTER_DATA};

        // SELECT all_columns FROM table_name WHERE bookChapterEntry._ID == chapter
        Cursor c = db.query(
                bookChapterEntry.TABLE_NAME,    // The table to query
                columnsToReturn,                // The columns to return
                selection,                      // The columns for the WHERE clause
                selectionArgs,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                null);                          // The sort order

        int chapterDataColumn = c.getColumnIndex(bookChapterEntry.COLUMN_NAME_CHAPTER_DATA);
        final String idk = c.getString(chapterDataColumn);
        c.close();

        // CONVERT JSON INTO ARRAYLIST IN BACKGROUND
        new AsyncTask<String, Void, ArrayList>() {
            @Override
            protected ArrayList doInBackground(String... params) {
                ArrayList<String> text = new ArrayList<>();
                try {
                    JSONObject json = new JSONObject(idk);
                    JSONArray array = json.getJSONArray("chapterArray");
                    for (int i = 0; i < array.length(); i++) {
                        text.add(array.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return text;
            }

            @Override
            protected void onPostExecute(ArrayList arrayList) {
                super.onPostExecute(arrayList);
                if (arrayList.size() > 0) {
                    chapterText = arrayList;
                }
            }
        }.execute(idk);

    }

    // Instances of this class are fragments representing a single object in our collection.
    public static class DemoObjectFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            // last two arguments ensure LayoutParams are inflated properly.
            View rootView = inflater.inflate(R.layout.read_chapter_activity, container, false);
            return rootView;
        }
    }

    private class FragmentAdapter extends FragmentStatePagerAdapter {
        FragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new DemoObjectFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return chapterText.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }


}
