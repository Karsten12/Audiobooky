package com.fonsecakarsten.audiobooky.Book;

/**
 * Created by kfonseca on 8/28/17.
 */

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.fonsecakarsten.audiobooky.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Karsten on 6/30/2017.
 * An async class devoted to aggregating all relevant details about a given book which is found via
 * barcode scanner
 */

public class BookInfoAsync2 extends AsyncTask<String, Void, Void> {
    private String searchBy;
    private String searchQuery;
    private Context context;
    private Activity activity;

    public BookInfoAsync2(String searchBy, String searchQuery, Context con, Activity acc) {
        this.searchBy = searchBy;
        this.searchQuery = searchQuery;
        this.context = con;
        this.activity = acc;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    // Send a request to GoogleBooks and OpenLib via Http request, and retrieve book information
    @Override
    protected Void doInBackground(String... params) {

        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> subtitles = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> publishers = new ArrayList<>();
        ArrayList<String> publishDates = new ArrayList<>();


        // Retrieve book cover image from openLibrary.org

        // Get book info from Google books
        String jsonStr = getJsonString(String.format("https://www.googleapis.com/books/v1/volumes?q=%s+%s:", searchQuery, searchBy));
        if (jsonStr != null) {
            try {
                JSONArray books = new JSONObject(jsonStr)
                        .getJSONArray("items");

                for (int i = 0; i < books.length(); i++) {
                    JSONObject book = books.getJSONObject(i).getJSONObject("volumeInfo");


                    titles.add(book.getString("title"));
                    if (book.has("subtitle")) {
                        subtitles.add(book.getString("subtitle"));
                    }
                    authors.add(book.getJSONArray("authors").getString(0));
                    publishers.add(book.getString("publisher"));
                    publishDates.add(book.getString("publishedDate"));
                }

            } catch (final JSONException ignored) {
            }
        }
        return null;
    }


    private String getJsonString(String reqUrl) {
        String response = null;
        try {
            URL url = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //super.onPostExecute(aVoid);
    }
}

