package com.fonsecakarsten.audiobooky;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by Karsten on 6/30/2017.
 */

class BookInfoAsync extends AsyncTask<String, Void, AudioBook> {
    private String ISBN;
    private Context context;
    private Activity activity;
    private ProgressDialog progressDialog;

    BookInfoAsync(String isbn, Context con, Activity acc) {
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

        String title = "Hello World";
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

        File f = null;
        if (bitmap != null) {
            // Create a file to store the bitmap
            File directory = context.getDir("CoverImages", Context.MODE_PRIVATE);
            f = new File(directory, title);
            // Store bitmap into file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        book.setCoverImagePath(Uri.fromFile(f).toString());
        book.setTitle(title);
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
