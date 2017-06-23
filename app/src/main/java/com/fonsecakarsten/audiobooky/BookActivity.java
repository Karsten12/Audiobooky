package com.fonsecakarsten.audiobooky;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class BookActivity extends AppCompatActivity {
    private ArrayList<String> mImageArray;
    private static String accessToken;
    private AudioBook book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);
    }

    // Get results from newCaptureActiavity and process images, converting them with CloudVisionAsync
    private void getResults() {
        Intent fromCA = getIntent();
        book = (AudioBook) fromCA.getSerializableExtra("newBook");
        accessToken = fromCA.getExtras().getString("token");
        mImageArray = fromCA.getExtras().getStringArrayList("imageArray");
//        if (mImageArray.size() > 0) {
//            CloudVisionAsync task = new CloudVisionAsync(accessToken, mImageArray.get(0));
//            task.execute();
//        }
    }
}
