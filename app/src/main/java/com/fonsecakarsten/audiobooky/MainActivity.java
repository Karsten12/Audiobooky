package com.fonsecakarsten.audiobooky;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fonsecakarsten.audiobooky.R.mipmap.ic_launcher_round;

public class MainActivity extends Activity {

    ArrayList<String> mImageArray;
    recycleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

    }

    public void onFABClick(View v) {
        Intent intent = new Intent(this, newCapureActivity.class);
        startActivityForResult(intent, 1);
    }


    // Convert each image's text using OCR
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                mImageArray = data.getExtras().getStringArrayList("imageArray");
                for (int i = 0; i < mImageArray.size(); i++) {
                    OCR_async task = new OCR_async(mImageArray.get(i), getApplicationContext());
                    task.execute();
                }
            }
        }
    }


    private class recycleAdapter extends RecyclerView.Adapter<recycleAdapter.viewholder> {

        @Override
        public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.book_row, parent, false);

            return new viewholder(v);
        }

        @Override
        public void onBindViewHolder(viewholder holder, int position) {
            holder.imageView.setImageResource(ic_launcher_round);
            holder.bookName.setText("The Hardy Boys");
            holder.bookDescrp.setText("The Disappearing Floor");


        }

        @Override
        public int getItemCount() {
            return 10;
        }

        class viewholder extends RecyclerView.ViewHolder {
            CircleImageView imageView;
            TextView bookName;
            TextView bookDescrp;

            viewholder(View itemView) {
                super(itemView);
                imageView = (CircleImageView) itemView.findViewById(R.id.profile_image);
                bookName = (TextView) itemView.findViewById(R.id.book_name);
                bookDescrp = (TextView) itemView.findViewById(R.id.book_description);

            }
        }
    }

}
