package com.fonsecakarsten.audiobooky;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Karsten on 7/23/2017.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.Myviewholder> {

    private File imagesFile;
    private Context context;

    public ImageAdapter(File folderFile, Context con) {
        imagesFile = folderFile;
        context = con;
    }

    @Override
    public Myviewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_row, parent, false);

        return new Myviewholder(v);
    }

    @Override
    public void onBindViewHolder(final Myviewholder holder, int position) {
        final String imageFilePath = imagesFile.listFiles()[position].getAbsolutePath();
        //Glide.with(context).load(imageFilePath).into(holder.imageView);

        // Display image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 16;
        final Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath, options);
        holder.imageView.setImageBitmap(bitmap);

//        holder.imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
//                View layout = inflater.inflate(R.layout.image_popup, (ViewGroup) findViewById(R.id.image_popup_root), false);
//
//                // Display the image
//                ImageView image = (ImageView) layout.findViewById(R.id.image_popup_image);
//                Glide.with(context).load(imageFilePath).into(image);
//
//                AlertDialog imageDialog = new AlertDialog.Builder(context)
//                        .setView(layout)
//                        .setPositiveButton("RETAKE", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                // Todo
//                                // Retake image
//                                // Remove old image
//                                // Add new image
//                                // notifyItemRemoved(position);
//                                // notifyItemRangeChanged(position, mImageArray.size());
//                            }
//                        })
//                        .setNeutralButton(R.string.Ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .setNegativeButton("REMOVE", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                //mImageArray.remove(holder.getAdapterPosition());
//                                notifyItemRemoved(holder.getAdapterPosition());
//                                //notifyItemRangeChanged(holder.getAdapterPosition(), mImageArray.size());
//                            }
//                        }).create();
//                imageDialog.show();
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return imagesFile.listFiles().length;
    }

    class Myviewholder extends RecyclerView.ViewHolder {
        ImageView imageView;

        Myviewholder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
        }
    }
}
