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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

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
        Log.d("oncreate", "set adapter");
        recyclerView.setAdapter(mAdapter);
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = 200;
        recyclerView.setLayoutParams(params);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    public void takepicture() {
        Log.d("Cameraclick", "takepicture");
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
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(mCurrentPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
                String clickpath = mCurrentPhotoPath;
                Data_Model data_model = new Data_Model();
                data_model.setImage(clickpath);
                imageArray.add(data_model);
                mAdapter.notifyDataSetChanged();


            } else if (requestCode == SELECT_FILE) {
                Log.d("gallery", "checkpointd");
                onSelectFromGalleryResult(data);
            }
        }

    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.Myviewholder> {

        @Override
        public Myviewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.imgedisplay, parent, false);
            Myviewholder myviewholder = new Myviewholder(v);
            Log.d("myactivty ", "oncreateViewHolder");

            return myviewholder;
        }

        @Override
        public void onBindViewHolder(Myviewholder holder, final int position) {
            final Data_Model m = imageArray.get(position);
            Log.d(" myactivty", "onBindviewholder" + position);
            //holder.imageView.setVisibility(View.VISIBLE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();
            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 20;
            final Bitmap bitmap = BitmapFactory.decodeFile(m.getImage(), options);
            holder.imageView.setImageBitmap(bitmap);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ImageDisplay.class);
                    String chkpath = m.getImage();
                    intent.putExtra("path", chkpath);
                    Log.d("intent", "new activity");
                    startActivity(intent);
                }
            });
            holder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    final int pst = position;
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    imageArray.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, imageArray.size());
                                    // Yes button clicked
                                    Toast.makeText(newCapureActivity.this, "Yes Clicked",
                                            Toast.LENGTH_LONG).show();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    // No button clicked
                                    // do nothing
                                    Toast.makeText(newCapureActivity.this, "No Clicked",
                                            Toast.LENGTH_LONG).show();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(newCapureActivity.this);
                    builder.setMessage("Are you sure want to Delete ?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                    return true;
                }
            });


        }

        @Override
        public int getItemCount() {
            return imageArray.size();
        }

        public class Myviewholder extends RecyclerView.ViewHolder {
            public ImageView imageView;

            public Myviewholder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView.findViewById(R.id.image);
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data) {

        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        Data_Model data_model = new Data_Model();
        data_model.setImage(path);
        imageArray.add(data_model);
        mAdapter.notifyDataSetChanged();
        // Toast.makeText(this, "" + path, Toast.LENGTH_LONG).show();
    }

    public void selectImage() {

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
        Log.d("gallery", "checkpointA");
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
        Log.d("gallery", "checkpointB");
    }
}
