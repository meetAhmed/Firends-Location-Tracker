package example.api.location.google;

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
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import example.api.location.google.preferenceManager.preferenceManager;

public class setProfilePicture extends AppCompatActivity {

    ImageView user_dp;
    Uri finalUri = null;
    String finalPicturePath = null;
    Integer RESULT_LOAD_IMG = 109;
    private int STORAGE_PERMISSION_CODE = 23;
    byte[] imgBytes;
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_picture);

        if (!isReadStorageAllowed()) {
            requestStoragePermission();
        }

        user_dp = (ImageView) findViewById(R.id.user_dp);
        user_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isReadStorageAllowed()) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMG);
                } else {
                    Toast.makeText(getApplicationContext(), "We dont have permission to look into your Gallery", Toast.LENGTH_LONG).show();
                }
            }
        });
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

    public void skipIT(View view) {
        startActivity(new Intent(getApplicationContext(), home.class));
        finish();
    }

    public void uploadNow(View view) {
        if (imgBytes == null || finalUri == null) {
            Toast.makeText(getApplicationContext(), "Select image", Toast.LENGTH_LONG).show();
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
                        if (storeRoom.moveImageToLoc(name, bitmap)) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Picture uploaded", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), home.class));
                            finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Picture uploaded", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), home.class));
                            finish();
                            // error occured while moving the image
                        }
                    }
                }
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
                Toast.makeText(getApplicationContext(), "Error occured while uploading picture", Toast.LENGTH_LONG).show();
            }
        });

    }

}
