package example.api.location.google.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import example.api.location.google.R;
import example.api.location.google.home;
import example.api.location.google.preferenceManager.preferenceManager;
import example.api.location.google.starterActivity;
import example.api.location.google.storeRoom;

public class profileView extends AppCompatActivity {

    ImageView user_dp;
    EditText username;
    FirebaseAuth auth;
    String name = null;
    Uri finalUri = null;
    String finalPicturePath = null;
    Integer RESULT_LOAD_IMG = 109;
    private int STORAGE_PERMISSION_CODE = 23;
    byte[] imgBytes;
    Bitmap bitmap = null;
    boolean taskProfile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        if (home.homeOBJECT != null) {
            home.homeOBJECT.finish();
        }

        getSupportActionBar().setTitle("Profile");

        user_dp = (ImageView) findViewById(R.id.user_dp);
        username = (EditText) findViewById(R.id.username);

        if (!isReadStorageAllowed()) {
            requestStoragePermission();
        }

        user_dp = (ImageView) findViewById(R.id.user_dp);
        user_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskProfile = false;
                if (isReadStorageAllowed()) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMG);
                } else {
                    Toast.makeText(getApplicationContext(), "We dont have permission to look into your Gallery", Toast.LENGTH_LONG).show();
                }
            }
        });


        FirebaseDatabase.getInstance().getReference().child("users").child(preferenceManager.getkey(getApplicationContext())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                name = dataSnapshot.child("name").getValue().toString();
                if (name.charAt(0) != '@') {
                    name = "@" + name;
                }
                username.setText(name);
                String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                if (!profileRef.equalsIgnoreCase("none")) {
                    Glide.with(getApplicationContext()).load(storeRoom.getImagesRootDir(profileRef)).into(user_dp);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void showLogs(View view) {
        Bundle bundle = new Bundle();
        bundle.putString("user", preferenceManager.getkey(getApplicationContext()));
        Intent intent = new Intent(getApplicationContext(), logsViewer.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Displaying a toast
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
            try {
                Uri imageUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(imageUri, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                bitmap = BitmapFactory.decodeFile(picturePath);
                finalUri = imageUri;
                finalPicturePath = picturePath;
                bitmap = storeRoom.resize(bitmap, 1200, 1200);
                imgBytes = storeRoom.getBytes(bitmap);
                user_dp.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } // if ends here
    } // function ends here


    public void uploadProfile() {

        if (name != null) {
            if (!username.getText().toString().trim().equals(name.trim()) && username.getText().toString().trim().length() > 0) {
                FirebaseDatabase.getInstance().getReference().child("users").child(preferenceManager.getkey(getApplicationContext()))
                        .child("name").setValue(username.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Name updated successfully", Toast.LENGTH_SHORT).show();
                        preferenceManager.writeUserName(getApplicationContext(), username.getText().toString().trim());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed to update name\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (taskProfile) {
                Toast.makeText(getApplicationContext(), "Picture already uploaded", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (imgBytes == null || finalUri == null) {
            return;
        }
        if (taskProfile) {
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading profile picture");
        progressDialog.setCancelable(false);
        progressDialog.show();

        StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference().child("display-pictures");
        final String name = System.currentTimeMillis() + "-dp." + storeRoom.getFileType(finalUri, getApplicationContext());

        firebaseStorage.child(name).putBytes(imgBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                FirebaseDatabase.getInstance().getReference()
                        .child("users").child(preferenceManager.getkey(getApplicationContext()))
                        .child("profileAddress").setValue(name);

                if (bitmap != null) {
                    if (!storeRoom.isImageFileExit(name)) {
                        storeRoom.moveImageToLoc(name, bitmap);
                    }
                }// check

                taskProfile = true;
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Picture uploaded", Toast.LENGTH_LONG).show();
            }

        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //displaying percentage in progress dialog
                progressDialog.setMessage("Uploaded " + ((int) progress) + " %");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Error occurred while uploading picture", Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        auth = FirebaseAuth.getInstance();
        if (auth == null) {
            preferenceManager.setKey(getApplicationContext(), "nothing");
            startActivity(new Intent(getApplicationContext(), starterActivity.class));
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.logout) {
            auth.signOut();
            preferenceManager.setKey(getApplicationContext(), "nothing");
            startActivity(new Intent(getApplicationContext(), starterActivity.class));
            finish();
        } else if (id == R.id.cancel) {
            startActivity(new Intent(getApplicationContext(), home.class));
            finish();
        } else if (id == R.id.updateUserData) {
            uploadProfile();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Log.i("123213123", "backpressed");
        startActivity(new Intent(getApplicationContext(), home.class));
        finish();
    }
}
