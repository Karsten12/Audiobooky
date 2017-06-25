package com.fonsecakarsten.audiobooky;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BookActivity extends AppCompatActivity {
    private recycleAdapter mAdapter;
    private ArrayList<String> mImageArray;
    private static String accessToken;
    private AudioBook book;
    private Intent fromCA;
    private File f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

//        // Set up toolbar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
//        setActionBar(toolbar);

        // Set up recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.book_recview);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));

        // Get title
        fromCA = getIntent();
        book = (AudioBook) fromCA.getSerializableExtra("newBook");
        String title = book.getTitle();

        // Set up collapsing toolbar complex view
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(book.getTitle());
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
//        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
//        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));


        // Create file in private app directory to store all of this book's content
        // Book's title will be the the fileName
        String directory = "data/data/com.fonsecakarsten.audiobooky/";
        f = new File(directory + title + ".txt");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getResults();
    }

    // Get results from newCaptureActiavity and process images, converting them with CloudVisionAsync
    private void getResults() {
        accessToken = fromCA.getExtras().getString("token");
        mImageArray = fromCA.getExtras().getStringArrayList("imageArray");
        for (int i = 0; i < mImageArray.size(); i++) {
            CloudVisionAsync task = new CloudVisionAsync(accessToken, mImageArray.get(i));
            try {
                book.setPageText(task.get().get(0));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private class recycleAdapter extends RecyclerView.Adapter<BookActivity.recycleAdapter.viewholder> {

        @Override
        public BookActivity.recycleAdapter.viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.chapter_row, parent, false);

            return new BookActivity.recycleAdapter.viewholder(v);
        }

        @Override
        public void onBindViewHolder(BookActivity.recycleAdapter.viewholder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 20;
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
