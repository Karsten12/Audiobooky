package com.fonsecakarsten.audiobooky; /**
 * Created by Karsten on 6/5/2017.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class ImageDisplay extends AppCompatActivity {
    ImageView imageView;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imgedisplay);
        imageView = (ImageView) findViewById(R.id.imageviewnew);
        //setupActionBar();

        // Get Image Path
        path = getIntent().getExtras().getString("path");

        // Get Image
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 4;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);

        // Display Image
        imageView.setImageBitmap(bitmap);
    }
}
