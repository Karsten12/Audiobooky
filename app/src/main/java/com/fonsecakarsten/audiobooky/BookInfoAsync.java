package com.fonsecakarsten.audiobooky;

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
    private AudioBook book;

    BookInfoAsync(String isbn, AudioBook bk) {
        this.ISBN = isbn;
        this.book = bk;
    }


    // Send a request to GoogleBooks and OpenLib via Http request, and retrieve book information
    @Override
    protected AudioBook doInBackground(String... params) {
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
        book.setTitle(title);
        book.setAuthor(author);
        return book;
    }
}
