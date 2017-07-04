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
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Karsten on 6/30/2017.
 * An async class devoted to aggregating all relevant details about a given book which is found via
 * barcode scanner
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
        String imageURL;

        String title = "Hello World";
        String author = null;
        Bitmap bitmap = null;

        // Retrieve book cover image from openLibrary.org
        String[] sizes = {"L", "M", "S"};
        for (String size : sizes) {
            imageURL = String.format("http://covers.openlibrary.org/b/isbn/%s-%s.jpg", ISBN, size);
            try {
                URL url = new URL(imageURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                bitmap = BitmapFactory.decodeStream(connection.getInputStream());

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                break;
            }
        }

        File f = null;
        if (bitmap != null) {
            // Create a file inside of CoverImages folder to store the book image
            f = new File(context.getDir("CoverImages", Context.MODE_PRIVATE), title);
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
        activity.startActivity(intent);

    }
}
