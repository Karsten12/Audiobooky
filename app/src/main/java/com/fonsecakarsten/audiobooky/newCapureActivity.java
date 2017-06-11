package com.fonsecakarsten.audiobooky;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Karsten on 6/5/2017.
 */

public class newCapureActivity extends Activity {

    static final int REQUEST_TAKE_PHOTO = 1;
    private int SELECT_FILE = 2;
    private String mCurrentPhotoPath;
    ArrayList<Data_Model> imageArray;
    MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        imageArray = new ArrayList<>();
        mAdapter = new MyAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    public void takepicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                try {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", createImageFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {

                // Save Image To Gallery
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                File f = new File(mCurrentPhotoPath);
//                Uri contentUri = Uri.fromFile(f);
//                mediaScanIntent.setData(contentUri);
//                this.sendBroadcast(mediaScanIntent);
//                String clickpath = mCurrentPhotoPath;
                Data_Model data_model = new Data_Model();
                data_model.setImage(mCurrentPhotoPath);
                imageArray.add(data_model);
                mAdapter.notifyDataSetChanged();


            } else if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            }
        }

    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.Myviewholder> {

        @Override
        public Myviewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.image_popup, parent, false);
            Myviewholder myviewholder = new Myviewholder(v);

            return myviewholder;
        }

        @Override
        public void onBindViewHolder(Myviewholder holder, final int position) {
            final Data_Model m = imageArray.get(position);
            //holder.imageView.setVisibility(View.VISIBLE);
            final BitmapFactory.Options options = new BitmapFactory.Options();

            // Downsize image, as it throws OutOfMemory Exception for larger images
            options.inSampleSize = 20;
            final Bitmap bitmap = BitmapFactory.decodeFile(m.getImage(), options);
            holder.imageView.setImageBitmap(bitmap);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder imageDialog = new AlertDialog.Builder(newCapureActivity.this);
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                    View layout = inflater.inflate(R.layout.image_popup, (ViewGroup) findViewById(R.id.image_popup_root), false);
                    final ImageView image = (ImageView) layout.findViewById(R.id.image_view);

                    // Get Image
//                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//                    bmOptions.inJustDecodeBounds = false;
//                    bmOptions.inSampleSize = 4;
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = 4;
                    Bitmap bitmap = BitmapFactory.decodeFile(m.getImage(), options);

                    // Display the image
                    image.setImageBitmap(bitmap);
                    imageDialog.setView(layout);

                    // Retake image
                    imageDialog.setPositiveButton("RETAKE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Todo
                            // Retake image
                            // Remove old image
                            // Add new image
                            // notifyItemRemoved(position);
                            // notifyItemRangeChanged(position, imageArray.size());
                        }
                    });
                    // Do nothing, close dialog
                    imageDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    // Remove image from list
                    imageDialog.setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            imageArray.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, imageArray.size());
                        }
                    });

                    imageDialog.create();
                    imageDialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return imageArray.size();
        }

        class Myviewholder extends RecyclerView.ViewHolder {
            ImageView imageView;

            Myviewholder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image_view);
            }
        }
    }


    public void selectImage(View v) {

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(newCapureActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = true;

                if (items[item].equals("Take Photo")) {
                    if (result)
                        takepicture();

                } else if (items[item].equals("Choose from Library")) {
                    if (result)
                        galleryIntent();


                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    public void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null, null);
        assert cursor != null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        Data_Model data_model = new Data_Model();
        data_model.setImage(path);
        imageArray.add(data_model);
        mAdapter.notifyDataSetChanged();
        cursor.close();
        // Toast.makeText(this, "" + path, Toast.LENGTH_LONG).show();
    }
}
