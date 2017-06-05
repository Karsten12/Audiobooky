package com.fonsecakarsten.audiobooky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

    }

    public void onFABClick(View v) {
        Intent intent = new Intent(this, Capture_Activity.class);
        startActivity(intent);
    }

}
