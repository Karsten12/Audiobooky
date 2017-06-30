package com.fonsecakarsten.audiobooky;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.fonsecakarsten.audiobooky.R.mipmap.ic_launcher;


public class MainActivity extends AppCompatActivity {

    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS};
    private ArrayList<AudioBook> mBooks = new ArrayList<>();
    private recycleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setActionBar(toolbar);

        // Set up recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mainRecView);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));

        // Set up Floating action button
        FloatingActionButton captureBtn = (FloatingActionButton) findViewById(R.id.FAB1);
        captureBtn.bringToFront();
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewBook();
            }
        });

        openAddAllBooks();
    }

    // Get list of audioBooks from internal app subdirectory "books"
    public void getBookList() {
        File[] files = getFilesDir().listFiles();
        System.out.println(files.length);
    }

    //  Get a list of all audiobooks from internal app subdirectory "books" and add them to mbooks
    public void openAddAllBooks() {
        File appDir = getApplicationContext().getDir("books", Context.MODE_PRIVATE);
        File subDirectory = appDir.getAbsoluteFile();
        FileInputStream fis;
        ObjectInputStream is;
        AudioBook audioBook;

        for (String list : subDirectory.list()) {
            fis = null;
            is = null;
            audioBook = null;
            try {
                fis = new FileInputStream(new File(appDir, list));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                is = new ObjectInputStream(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                audioBook = (AudioBook) is.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBooks.add(audioBook);
        }
    }

    public void addNewBook() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.newbook_dialog, (ViewGroup) findViewById(R.id.newbook_dialog_root), false);
        final EditText title = (EditText) layout.findViewById(R.id.dialog_title);
        final EditText author = (EditText) layout.findViewById(R.id.dialog_author);

        AlertDialog newBookDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setTitle("Add a new audiobook!")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AudioBook newBook = new AudioBook();
                        newBook.setTitle(title.getText().toString());
                        newBook.setAuthor(author.getText().toString());

                        // TODO
                        // Check if either textboxes are empty
                        // ISBN mobile vision activity
                        Intent intent = new Intent(getApplicationContext(), NewCaptureActivity.class);
                        intent.putExtra("newBook", newBook);
                        startActivity(intent);
                    }
                })
                .create();
        title.requestFocus();
        newBookDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        newBookDialog.show();
    }

//    // Check if permissions granted
//    public static boolean hasPermissions(Context context, String... permissions)  {
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
//            for (String permission : permissions) {
//                int idk = ActivityCompat.checkSelfPermission(context, permission);
//                System.out.println(idk);
//                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
//                    return false;
//                }
//            }
//        }
//        return true;
//    }

    // If permissions not granted, request it
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        int granted = PackageManager.PERMISSION_GRANTED;
//        switch (requestCode) {
//            case RequestPermissionCode:
//                //System.out.println("HELLO");
//                if (grantResults.length > 0 &&
//                        grantResults[0] == granted &&
//                        grantResults[1] == granted &&
//                        grantResults[2] == granted) {
//                    // Permissions all granted
//                    break;
//                } else {
//                    // One or more permissions denied, re-request permissions
//                    ActivityCompat.requestPermissions(this, PERMISSIONS, RequestPermissionCode);
//                }
//        }
//    }

    private class recycleAdapter extends RecyclerView.Adapter<recycleAdapter.viewholder> {

        @Override
        public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = getLayoutInflater().inflate(R.layout.book_row, parent, false);

            return new viewholder(v);
        }

        @Override
        public void onBindViewHolder(viewholder holder, final int position) {
//            holder.imageView.setImageBitmap(mBooks.get(position).getCoverImage());
//            holder.bookName.setText(mBooks.get(position).getTitle());
//            holder.bookAuthor.setText(mBooks.get(position).getAuthor());
            holder.imageView.setImageResource(ic_launcher);
            holder.bookName.setText("The Hardy Boys: The Disappearing Floor");
            holder.bookAuthor.setText("Franklin W. Dixon");

//            holder.root.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // Go to listenAudio activity
//                    AudioBook book = mBooks.get(position);
//                    Intent intent = new Intent(getApplicationContext(), BookActivity.class);
//                    intent.putExtra("newBook", book);
//                    startActivity(intent);
//                }
//            });
        }

        @Override
        public int getItemCount() {
            //return mBooks.size();
            return 10;
        }

        class viewholder extends RecyclerView.ViewHolder {
            CircleImageView imageView;
            TextView bookName;
            TextView bookAuthor;
            RelativeLayout root;

            viewholder(View itemView) {
                super(itemView);
                root = (RelativeLayout) itemView.findViewById(R.id.book_row_root);
                imageView = (CircleImageView) itemView.findViewById(R.id.profile_image);
                bookName = (TextView) itemView.findViewById(R.id.book_name);
                bookAuthor = (TextView) itemView.findViewById(R.id.book_author);

            }
        }
    }
}
