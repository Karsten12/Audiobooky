package com.fonsecakarsten.audiobooky;

import android.content.Context;
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
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BookActivity extends AppCompatActivity {
    private recycleAdapter mAdapter;
    private ArrayList<String> mImageArray;
    private static String accessToken;
    private AudioBook book;
    private Intent fromCA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

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

        if (checkFileExists(title)) {
            openBook(title);
        } else {
            saveBook(title);
        }

        // Set up collapsing toolbar complex view
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(book.getTitle());
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
//        collapsingToolbarLayout.setContentScrimColor(palette.getMutedColor(primary));
//        collapsingToolbarLayout.setStatusBarScrimColor(palette.getDarkMutedColor(primaryDark));

    }

    // Get results from newCaptureActivity and process images, converting them with CloudVisionAsync
    private void getResults() {
        accessToken = fromCA.getExtras().getString("token");
        mImageArray = fromCA.getExtras().getStringArrayList("imageArray");
        for (int i = 0; i < mImageArray.size(); i++) {
            CloudVisionAsync task = new CloudVisionAsync(accessToken, mImageArray.get(i));
            try {
                book.setPageText(task.get().get(0));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    // Check if audio book file already exists
    public boolean checkFileExists(String fname) {
        File appDir = getApplicationContext().getDir("books", Context.MODE_PRIVATE);
        File subDirectory = new File(appDir, fname);
        return subDirectory.exists();
    }

    // Open saved audio book
    public void openBook(String title) {
        File mydir = getApplicationContext().getDir("books", Context.MODE_PRIVATE);
        File bookFile = new File(mydir, title);
        FileInputStream fis = null;
        ObjectInputStream is = null;

        try {
            //fis = getApplicationContext().openFileInput(title);
            fis = new FileInputStream(bookFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            is = new ObjectInputStream(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            book = (AudioBook) is.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create new audio book
    public void saveBook(String title) {
        File mydir = getApplicationContext().getDir("books", Context.MODE_PRIVATE);
        File bookFile = new File(mydir, title);
        FileOutputStream fos = null;
        ObjectOutputStream os = null;

        try {
            //fos = getApplicationContext().openFileOutput(title, Context.MODE_PRIVATE);
            fos = new FileOutputStream(bookFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            os = new ObjectOutputStream(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.writeObject(book);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
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
