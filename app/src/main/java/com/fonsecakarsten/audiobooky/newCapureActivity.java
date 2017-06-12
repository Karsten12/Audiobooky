package com.fonsecakarsten.audiobooky;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
    private String mCurrPhotoPath;
    ArrayList<Data_Model> mImageArray;
    MyAdapter mAdapter;
    public static final int RequestPermissionCode = 1;
    boolean permissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        mImageArray = new ArrayList<>();
        mAdapter = new MyAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        checkPermissions();
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
        mCurrPhotoPath = image.getAbsolutePath();
        return image;


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {

                // Save Image To Gallery
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                File f = new File(mCurrPhotoPath);
//                Uri contentUri = Uri.fromFile(f);
//                mediaScanIntent.setData(contentUri);
//                this.sendBroadcast(mediaScanIntent);
//                String clickpath = mCurrPhotoPath;
                Data_Model data_model = new Data_Model();
                data_model.setImage(mCurrPhotoPath);
                mImageArray.add(data_model);
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

            return new Myviewholder(v);
        }

        @Override
        public void onBindViewHolder(final Myviewholder holder, int position) {
            final Data_Model m = mImageArray.get(position);
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
                            // notifyItemRangeChanged(position, mImageArray.size());
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

//                            mImageArray.remove(position);
//                            notifyItemRemoved(position);
//                            notifyItemRangeChanged(position, mImageArray.size());
                            mImageArray.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            notifyItemRangeChanged(holder.getAdapterPosition(), mImageArray.size());
                        }
                    });

                    imageDialog.create();
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


    public void selectImage(View v) {

        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(newCapureActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    if (permissionGranted)
                        takepicture();

                } else if (items[item].equals("Choose from Library")) {
                    if (permissionGranted)
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
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
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
        mImageArray.add(data_model);
        mAdapter.notifyDataSetChanged();
        cursor.close();

//        if (Intent.ACTION_SEND_MULTIPLE.equals(data.getAction()))
//        && Intent.hasExtra(Intent.EXTRA_STREAM)) {
//            // retrieve a collection of selected images
//            ArrayList<Parcelable> list = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
//            // iterate over these images
//            if( list != null ) {
//                for (Parcelable parcel : list) {
//                    Uri uri = (Uri) parcel;
//                    // TODO handle the images one by one here
//                }
//            }
//        }
    }


    // Check if camera and external storage permission is granted
    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int camPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int writeExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (camPermission == PackageManager.PERMISSION_DENIED || writeExternalPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RequestPermissionCode);

            }
        }
    }

    // If permissions not granted, request it
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean camPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeExternalPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (camPermission && writeExternalPermission) {
                        permissionGranted = true;
                    }
                }
        }
    }



}
