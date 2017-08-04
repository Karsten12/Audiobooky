package com.fonsecakarsten.audiobooky.Book;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

/**
 * Created by Karsten on 6/13/2017.
 */

class MobileVisionAsync extends AsyncTask<String, Void, String> {

    private String URI;
    private Context context;

    MobileVisionAsync(String imageURI, Context con) {
        this.URI = imageURI;
        this.context = con;
    }


    @Override
    protected String doInBackground(String... params) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        Bitmap bitmap = BitmapFactory.decodeFile(URI, options);

        if (bitmap != null) {

            TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

            if (!textRecognizer.isOperational()) {

                IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                boolean hasLowStorage = context.registerReceiver(null, lowstorageFilter) != null;

                if (hasLowStorage) {
                    Toast.makeText(context, "Low Storage", Toast.LENGTH_LONG).show();
                    //Log.w(LOG_TAG, "Low Storage");
                }
            }


            Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();

            SparseArray<TextBlock> text = textRecognizer.detect(imageFrame);
            for (int i = 0; i < text.size(); i++) {
                TextBlock textBlock = text.valueAt(i);
                if (textBlock != null && textBlock.getValue() != null) {
                    System.out.println(textBlock.getValue());
                }
            }
        }
        return null;
    }
}
