package com.fonsecakarsten.audiobooky;

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

import java.util.ArrayList;

/**
 * Created by Karsten on 7/16/2017.
 */

public class readChapterActivity extends AppCompatActivity {

    FragmentAdapter adapter;
    ViewPager mViewPager;
    ArrayList<String> chapterText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the chapter data
        Bundle extras = getIntent().getExtras();

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle(extras.getString("CHAPTER_TITLE"));
        setActionBar(toolbar);

        chapterText = extras.getStringArrayList("CHAPTER_TEXT");

        adapter = new FragmentAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);

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
