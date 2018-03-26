package com.fonsecakarsten.listen;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.fonsecakarsten.listen.Barcode.BarcodeCaptureActivity;
import com.fonsecakarsten.listen.Book.BookActivity;
import com.fonsecakarsten.listen.Book.BookInfoAsync;
import com.fonsecakarsten.listen.Database.BookContract.bookEntry;
import com.fonsecakarsten.listen.Database.BookDbHelper;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS};
    private ArrayList<String> mBooksTitle = new ArrayList<>();
    private ArrayList<String> mBooksAuthor = new ArrayList<>();
    private ArrayList<String> mBooksGraphic = new ArrayList<>();
    private ArrayList<String> mBooksAbsolutePath = new ArrayList<>();
    private ArrayList<Integer> mBooksContentColor = new ArrayList<>();
    private ArrayList<Integer> mBooksStatusColor = new ArrayList<>();
    private recycleAdapter mAdapter;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private ArrayList<Integer> toDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.app_bar);
        setActionBar(toolbar);

        // Set up recyclerView
        RecyclerView recyclerView = findViewById(R.id.mainRecView);
        mAdapter = new recycleAdapter();
        recyclerView.setAdapter(mAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), mLayoutManager.getOrientation()));


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(MainActivity.this /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();

        // Set up Floating action button
        FloatingActionButton captureBtn = findViewById(R.id.FAB1);
        captureBtn.bringToFront();
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewBook();
//                signIn();
            }
        });
    }

    // Create and/or open the database and get the book list
    private void readDatabase() {
        // instantiate subclass of SQLiteOpenHelper
        BookDbHelper mDbHelper = new BookDbHelper(this);

        // Create and/or open a database to read from it, this allows for read access as well
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] columnsToGet = {bookEntry.COLUMN_NAME_TITLE, bookEntry.COLUMN_NAME_AUTHOR, bookEntry.COLUMN_NAME_COVER_IMAGE_PATH,
                bookEntry.COLUMN_NAME_ABSOLUTE_PATH, bookEntry.COLUMN_NAME_CONTENT_COLOR, bookEntry.COLUMN_NAME_STATUS_COLOR};

        // SELECT columnsToGet FROM table_name
        Cursor c = db.query(
                bookEntry.TABLE_NAME,    // The table to query
                columnsToGet,            // The columns to return
                null,                    // The columns for the WHERE clause
                null,                    // The values for the WHERE clause
                null,                    // don't group the rows
                null,                    // don't filter by row groups
                null);                   // The sort order

        if (c.getCount() != mBooksTitle.size()) {
            // Get all the chapter names and add it to the arraylist
            mBooksTitle.clear();
            mBooksAuthor.clear();
            mBooksGraphic.clear();
            mBooksAbsolutePath.clear();
            mBooksContentColor.clear();
            mBooksStatusColor.clear();
            while (c.moveToNext()) {
                mBooksTitle.add(c.getString(c.getColumnIndex(bookEntry.COLUMN_NAME_TITLE)));
                mBooksAuthor.add(c.getString(c.getColumnIndex(bookEntry.COLUMN_NAME_AUTHOR)));
                mBooksGraphic.add(c.getString(c.getColumnIndex(bookEntry.COLUMN_NAME_COVER_IMAGE_PATH)));
                mBooksAbsolutePath.add(c.getString(c.getColumnIndex(bookEntry.COLUMN_NAME_ABSOLUTE_PATH)));
                mBooksContentColor.add(c.getInt(c.getColumnIndex(bookEntry.COLUMN_NAME_CONTENT_COLOR)));
                mBooksStatusColor.add(c.getInt(c.getColumnIndex(bookEntry.COLUMN_NAME_STATUS_COLOR)));
            }
            mAdapter.notifyDataSetChanged();
        }
        c.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        readDatabase();
    }

    private void addNewBook() {
        String[] list = {"Barcode", "Title", "Author", "ISBN"};
        AlertDialog newDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.newBookString)
                .setCancelable(false)
                .setItems(list, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                                startActivityForResult(intent, 1);
                                break;
                            case 1:
                                // TODO
                                // Add manual information entry
                                // Open new dialog w/ spinner selecting choice from title, author, or ISBN,
                                // make new BookInfoAsync task
                                break;
                            case 2:
                                
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        newDialog.show();
    }

    private void searchBookBy(String value) {
        AlertDialog newDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.newBookString)
                .setCancelable(false)
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        newDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                BookInfoAsync task = new BookInfoAsync(data.getStringExtra("BarCodeString"), getApplicationContext(), MainActivity.this);
                task.execute();
            }
        } else if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }


    private void uploadData() {
        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    }

//    @Override
//    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
//        MenuInflater menuInflater = getMenuInflater();
//        menuInflater.inflate(R.menu.delete_books, menu);
//        toDelete = new ArrayList<>();
//        return true;
//    }
//
//    @Override
//    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
//        switch (menuItem.getItemId()) {
//            case R.id.action_delete:
//                for (Integer item : toDelete) {
//                    mBooksTitle.remove(item);
//                    mBooksAuthor.remove(item);
//                    mBooksGraphic.remove(item);
//                    mBooksAbsolutePath.remove(item);
//                    mBooksContentColor.remove(item);
//                    mBooksStatusColor.remove(item);
//                }
//                actionMode.finish();
//                return true;
//            default:
//                return false;
//        }
//    }
//
//    @Override
//    public void onDestroyActionMode(ActionMode actionMode) {
//        toDelete.clear();
//    }


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
        public void onBindViewHolder(final viewholder holder, int position) {

            Glide.with(MainActivity.this).load(Uri.parse(mBooksGraphic.get(position))).into(holder.imageView);
            holder.bookName.setText(mBooksTitle.get(position));
            holder.bookAuthor.setText(mBooksAuthor.get(position));

            final int pos = holder.getAdapterPosition();

            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go to bookActivity
                    Intent intent = new Intent(getApplicationContext(), BookActivity.class);

                    intent.putExtra("BOOK_TITLE", mBooksTitle.get(pos));
                    intent.putExtra("BOOK_AUTHOR", mBooksAuthor.get(pos));
                    intent.putExtra("BOOK_GRAPHIC", mBooksGraphic.get(pos));
                    intent.putExtra("BOOK_GRAPHIC_ABSOLUTEPATH", mBooksAbsolutePath.get(pos));
                    intent.putExtra("CONTENT_COLOR", mBooksContentColor.get(pos));
                    intent.putExtra("STATUS_COLOR", mBooksStatusColor.get(pos));

                    startActivity(intent);
                    overridePendingTransition(R.anim.enter, R.anim.exit);
                }
            });

            holder.root.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO
                    // Add curr book to toDelete so that the book can be deleted

                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return mBooksTitle.size();
        }

        class viewholder extends RecyclerView.ViewHolder {
            CircleImageView imageView;
            TextView bookName;
            TextView bookAuthor;
            RelativeLayout root;

            viewholder(View itemView) {
                super(itemView);
                root = itemView.findViewById(R.id.book_row_root);
                imageView = itemView.findViewById(R.id.profile_image);
                bookName = itemView.findViewById(R.id.book_name);
                bookAuthor = itemView.findViewById(R.id.book_author);

            }
        }
    }
}
