package com.fonsecakarsten.audiobooky.Book;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;

import com.fonsecakarsten.audiobooky.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Karsten on 6/30/2017.
 * An async class devoted to aggregating all relevant details about a given book which is found via
 * barcode scanner
 */

public class BookInfoAsync extends AsyncTask<String, Void, AudioBook> {
    private String ISBN;
    private Context context;
    private Activity activity;
    private ProgressDialog progressDialog;
    private Intent intent;

    public BookInfoAsync(String isbn, Context con, Activity acc) {
        this.ISBN = isbn;
        this.context = con;
        this.activity = acc;
        this.intent = new Intent(context, BookActivity.class);
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

        String title = null;
        String subtitle = null;
        String author = null;
        Bitmap bitmap = null;
        String publisher = null;
        String publishDate = null;
        String description = null;
        int rating = 0;
        int content_color = 0;
        int status_color = 0;


        // Retrieve book cover image from openLibrary.org
        String imageURL;
        String[] sizes = {"L", "M", "S"};
        for (String size : sizes) {
            imageURL = String.format("http://covers.openlibrary.org/b/isbn/%s-%s.jpg", ISBN, size);
            bitmap = getImageBitmap(imageURL);

            if (bitmap != null) {
                break;
            }
        }

        // Get book info from Google books
        String jsonStr = getJsonString(String.format("https://www.googleapis.com/books/v1/volumes?q=isbn:%s", ISBN));
        if (jsonStr != null) {
            try {
                JSONObject bookInfo = new JSONObject(jsonStr)
                        .getJSONArray("items")
                        .getJSONObject(0)
                        .getJSONObject("volumeInfo");

                title = bookInfo.getString("title");
                if (bookInfo.has("subtitle")) {
                    subtitle = bookInfo.getString("subtitle");
                }
                author = bookInfo.getJSONArray("authors").getString(0);
                publisher = bookInfo.getString("publisher");
                publishDate = bookInfo.getString("publishedDate");
                description = bookInfo.getString("description");
                rating = bookInfo.getInt("averageRating");
                if (bitmap == null) {
                    bitmap = getImageBitmap(bookInfo.getJSONArray("imageLinks").getString(1));
                }

            } catch (final JSONException e) {
                System.out.println("Error");
            }
        }


        File f = null;
        if (bitmap != null && title != null) {

            // Get palette colors
            Palette palette = Palette.from(bitmap).generate();
            content_color = palette.getMutedColor(ContextCompat.getColor(context, R.color.colorPrimary));
            status_color = palette.getMutedColor(ContextCompat.getColor(context, R.color.colorPrimary));

            // Create a file inside of CoverImages folder to store the book image
            f = new File(context.getDir("CoverImages", Context.MODE_PRIVATE), title);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if (subtitle != null) {
            book.setSubtitle(subtitle);
            intent.putExtra("BOOK_SUBTITLE", subtitle);
        }

        book.setPublisher(publisher);
        book.setPublishDate(publishDate);
        book.setDescription(description);
        book.setRating(rating);
        book.setISBN(ISBN);

        intent.putExtra("BOOK_TITLE", title);
        intent.putExtra("BOOK_SUBTITLE", subtitle);
        intent.putExtra("BOOK_AUTHOR", author);
        intent.putExtra("BOOK_GRAPHIC", Uri.fromFile(f).toString());
        intent.putExtra("BOOK_GRAPHIC_ABSOLUTEPATH", f.getAbsolutePath());
        intent.putExtra("CONTENT_COLOR", content_color);
        intent.putExtra("STATUS_COLOR", status_color);

        return book;
    }


    private Bitmap getImageBitmap(String imageURL) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(imageURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setDoInput(true);
            con.connect();
            bitmap = BitmapFactory.decodeStream(con.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private String getJsonString(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(context.getString(R.string.get));
            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();

            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            } catch (IOException e) {
                // Error occurred
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    // Error occurred
                }
            }
            response = sb.toString();
        } catch (Exception e) {
            // Error occurred
        }
        return response;
    }

    @Override
    protected void onPostExecute(AudioBook book) {
        super.onPostExecute(book);
        intent.putExtra("newBook", book);
        activity.startActivity(intent);
        progressDialog.dismiss();
    }
}
