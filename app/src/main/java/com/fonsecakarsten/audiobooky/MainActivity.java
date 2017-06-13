package com.fonsecakarsten.audiobooky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

import java.util.ArrayList;

public class MainActivity extends Activity {

    ArrayList<String> mImageArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

    }

    public void onFABClick(View v) {
        Intent intent = new Intent(this, newCapureActivity.class);
        startActivityForResult(intent, 1);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mImageArray = data.getExtras().getStringArrayList("imageArray");
            }
        }
    }

}
