package com.fonsecakarsten.audiobooky.Book;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.flurgle.camerakit.CameraListener;
import com.flurgle.camerakit.CameraView;
import com.fonsecakarsten.audiobooky.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddChapterActivity extends Activity {
    private imageAdapter mAdapter;
    private CameraView mCameraView;
    private ArrayList<String> mImageArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_chapter_activity);


        mCameraView = (CameraView) findViewById(R.id.camera);
        Button button = (Button) findViewById(R.id.picture);

        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new imageAdapter();

        mRecyclerView.setAdapter(mAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.captureImage();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.FAB2);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                done();
            }
        });

        mCameraView.setCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(final byte[] picture) {
                super.onPictureTaken(picture);

                new AsyncTask<byte[], Void, Void>() {
                    @Override
                    protected Void doInBackground(byte[]... params) {
                        File f = createImageFile();
                        FileOutputStream fos;
                        try {
                            fos = new FileOutputStream(f.getAbsoluteFile());
                            fos.write(picture);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        mAdapter.notifyItemInserted(mImageArray.size());
                    }
                }.execute(picture);
            }
        });
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".png",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save a file: path for use with ACTION_VIEW intents
        mImageArray.add(image != null ? image.getAbsolutePath() : null);
        return image;
    }


    // Pass the images back to be processed
    private void done() {
        Intent intent = new Intent();
        intent.putExtra("imageArray", mImageArray);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    private class imageAdapter extends RecyclerView.Adapter<imageAdapter.Myviewholder> {

        @Override
        public imageAdapter.Myviewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.image_row, parent, false);

            return new Myviewholder(v);
        }

        @Override
        public void onBindViewHolder(final imageAdapter.Myviewholder holder, int position) {
            // Display image
            final String imagePath = mImageArray.get(position);
            Glide.with(AddChapterActivity.this).load(imagePath).into(holder.imageView);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.image_popup, (ViewGroup) findViewById(R.id.image_popup_root), false);

                    // Display the image
                    ImageView image = (ImageView) layout.findViewById(R.id.image_popup_image);
                    Glide.with(AddChapterActivity.this).load(imagePath).into(image);

                    final int pos = holder.getAdapterPosition();

                    AlertDialog imageDialog = new AlertDialog.Builder(AddChapterActivity.this)
                            .setView(layout)
                            .setPositiveButton("RETAKE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // TODO
                                    // Retake image
                                    // Remove old image
                                    // Add new image
                                    dialog.dismiss();
                                    File f = new File(imagePath);
                                    f.delete();
                                    mImageArray.remove(pos);
                                    mAdapter.notifyItemRemoved(pos);
                                }
                            })
                            .setNeutralButton(R.string.Ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mImageArray.remove(pos);
                                    mAdapter.notifyItemRemoved(pos);
                                    File f = new File(imagePath);
                                    f.delete();
                                }
                            }).create();
                    imageDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mImageArray.size();
        }

        class Myviewholder extends RecyclerView.ViewHolder {
            ImageView imageView;

            Myviewholder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image_view);
            }
        }
    }


}
