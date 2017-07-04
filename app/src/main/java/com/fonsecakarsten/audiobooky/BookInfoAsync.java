package com.fonsecakarsten.audiobooky;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by Karsten on 6/30/2017.
 */

public class BookInfoAsync extends AsyncTask<String, Void, AudioBook> {
    private String ISBN;
    private Context context;
    private Activity activity;
    private ProgressDialog progressDialog;

    public BookInfoAsync(String isbn, Context con, Activity acc) {
        this.ISBN = isbn;
        this.context = con;
        this.activity = acc;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Loading");
        progressDialog.show();
    }

    // Send a request to GoogleBooks and OpenLib via Http request, and retrieve book information
    @Override
    protected AudioBook doInBackground(String... params) {
        AudioBook book = new AudioBook();
        String googleBookURL = String.format("https://www.googleapis.com/books/v1/volumes?q=isbn:%s", ISBN);
        String imageURL = String.format("http://covers.openlibrary.org/b/isbn/%s-L.jpg", ISBN);

        String title = null;
        String author = null;
        Bitmap bitmap = null;


        // Get book cover image
        try {
            java.net.URL url = new java.net.URL(imageURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            e.printStackTrace();
        }

        book.setCoverImage(bitmap);
        book.setTitle("Hello World");
        book.setAuthor("Karsten Fonseca");
        return book;
    }

    @Override
    protected void onPostExecute(AudioBook book) {
        super.onPostExecute(book);

        Intent intent = new Intent(context, BookActivity.class);
        intent.putExtra("newBook", book);
        progressDialog.dismiss();
        activity.finish();
        activity.startActivity(intent);

    }
}
