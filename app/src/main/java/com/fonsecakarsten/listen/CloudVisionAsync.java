package com.fonsecakarsten.listen;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.BreakIterator;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.widget.Toast;

import com.fonsecakarsten.listen.Book.BookActivity;
import com.fonsecakarsten.listen.Database.BookContract;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.Image;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Karsten on 6/15/2017.
 * An async class that sends and processes all book pictures into text via OCR software
 */

public class CloudVisionAsync extends AsyncTask<Void, Void, Void> {

    private String accessToken;
    private String chapterTitle;
    private ArrayList<String> URI;
    private SQLiteDatabase db;
    private BookActivity.recycleAdapter mAdapter;
    private ArrayList<Boolean> idk;
    private int position;
    private Context con;

    public CloudVisionAsync(String token, String chptrTitle, ArrayList<String> imageURIS, SQLiteDatabase database,
                            BookActivity.recycleAdapter adapter, ArrayList<Boolean> something, int pos, Context con1) {
        this.accessToken = token;
        this.chapterTitle = chptrTitle;
        this.URI = imageURIS;
        this.db = database;
        this.idk = something;
        this.position = pos;
        this.mAdapter = adapter;
        this.con = con1;
    }

    @Override
    protected Void doInBackground(Void... params) {
//        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
//        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
//        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();
//
//        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, credential);
//        Vision vision = builder.build();
//
//        // The features that I want from each image
//        List<Feature> featureList = new ArrayList<>();
//        Feature textDetection = new Feature();
//        textDetection.setType("TEXT_DETECTION");
//        textDetection.setMaxResults(10);
//        featureList.add(textDetection);
//
//        // The list of images to be processed
//        List<AnnotateImageRequest> imageList = new ArrayList<>();
//
//
//        for (String x : URI) {
//            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();
//            Image base64EncodedImage = resizeBitmap(x);
//            annotateImageRequest.setImage(base64EncodedImage);
//            annotateImageRequest.setFeatures(featureList);
//            imageList.add(annotateImageRequest);
//        }
//
//        BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
//        batchAnnotateImagesRequest.setRequests(imageList);
//
//        BatchAnnotateImagesResponse response = null;
//        try {
//            Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
//            annotateRequest.setDisableGZipContent(true);
//            response = annotateRequest.execute();
//        } catch (IOException e) {
//            // Request failed
//        }


        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        // Add chapter title
        values.put(BookContract.bookChapterEntry.COLUMN_NAME_CHAPTER_TITLE, chapterTitle);

        // Add arrayList containing the chapterText
        JSONObject json = new JSONObject();
        try {
//            json.put("chapterArray", new JSONArray(convertResponseToString(response)));
            json.put("chapterArray", new JSONArray(getMobileVisionText(URI.get(0))));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String arrayList = json.toString();
        values.put(BookContract.bookChapterEntry.COLUMN_NAME_CHAPTER_DATA, arrayList);

        // Insert the new row, returning the primary key value of the new row
        db.insert(BookContract.bookChapterEntry.TABLE_NAME, null, values);

        idk.set(position, true);
        return null;
    }

    // Occurs on the UI/main thread
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mAdapter.notifyItemChanged(position);
    }

    @SuppressLint("NewApi")
    private ArrayList<String> convertResponseToString(BatchAnnotateImagesResponse response) {

        int pages = response.getResponses().size();
        ArrayList<String> pageTexts = new ArrayList<>();
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        for (int i = 0; i < pages; i++) {
            String pageText = response.getResponses().get(i).getTextAnnotations().get(0).getDescription(); // Get the text on the ith page
            // Remove all newLines and carrageReturns
            pageText = pageText.replace("\n", "").replace("\r", "");
            iterator.setText(pageText);
            int start = iterator.first();
            StringBuilder builder = new StringBuilder();

            for (int end = iterator.next(); end != BreakIterator.DONE;
                 start = end, end = iterator.next()) {
                builder.append(pageText.substring(start, end));
            }
            pageTexts.add(builder.toString());
        }

        return pageTexts;
    }

    private Image resizeBitmap(String uri) {
        File f = new File(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
//        BitmapFactory.decodeFile(f.getName(), options);
//        options.inJustDecodeBounds = true;
//        long length = f.length();
//        length = length / 1024;
//
//        int maxDimension = 1024;
//        int originalWidth = options.outWidth;
//        int originalHeight = options.outHeight;
//        int resizedWidth = maxDimension;
//        int resizedHeight = maxDimension;
//
//        if (originalHeight > originalWidth) {
//            resizedHeight = maxDimension;
//            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
//        } else if (originalWidth > originalHeight) {
//            resizedWidth = maxDimension;
//            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
//        } else if (originalHeight == originalWidth) {
//            resizedHeight = maxDimension;
//            resizedWidth = maxDimension;
//        }
        Bitmap bitmap = null;
//        bitmap =  Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
        options.inSampleSize = 2;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Image image = new Image();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        image.encodeContent(imageBytes);
        boolean deleted = f.delete();
        return image;
    }

    private ArrayList<String> getMobileVisionText(String uri) {
        ArrayList<String> pageTexts = new ArrayList<>();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeFile(uri, options);

        if (bitmap != null) {

            TextRecognizer textRecognizer = new TextRecognizer.Builder(con).build();

            if (!textRecognizer.isOperational()) {

                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = con.registerReceiver(null, lowstorageFilter) != null;

                if (hasLowStorage) {
                    Toast.makeText(con, "Low Storage", Toast.LENGTH_LONG).show();
                    //Log.w(LOG_TAG, "Low Storage");
                }
            }


            Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();

            SparseArray<TextBlock> text = textRecognizer.detect(imageFrame);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < text.size(); i++) {
                TextBlock textBlock = text.valueAt(i);
                if (textBlock != null && textBlock.getValue() != null) {
                    builder.append(textBlock.getValue().toString());
                }
            }
            pageTexts.add(builder.toString());
            return pageTexts;
        }
        return null;
    }
}
