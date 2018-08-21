package example.api.location.google;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import example.api.location.google.Models.user_model;
import example.api.location.google.preferenceManager.preferenceManager;

public class locationGetter extends Service {

    String tag = "123DD123";

    FusedLocationProviderClient client;
    LocationRequest locRequest;
    LocationCallback locationCallback;


    private ChildEventListener profileHandler;
    final long MEGA_BYTE = 1024 * 1024 * 50;

    public locationGetter() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(tag ,"Service Started");

        client = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        buildLocationRequest();
        buildLocationCallback();
        startUpdate();

        profileHandler = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final user_model userObj = dataSnapshot.getValue(user_model.class);
                if (!userObj.getProfileAddress().equalsIgnoreCase("none")) {
                    Log.i(tag ,"Found "+userObj.getProfileAddress());
                    if (!storeRoom.isImageFileExit(userObj.getProfileAddress())) {
                        Log.i(tag ,"Not Present "+userObj.getProfileAddress());
                        StorageReference firebaseStorage = FirebaseStorage.getInstance().getReference().child("display-pictures");
                        firebaseStorage.child(userObj.getProfileAddress()).getBytes(MEGA_BYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                storeRoom.createImageFile(bytes, userObj.getProfileAddress());
                                Log.i(tag ,"Secured "+userObj.getProfileAddress());
                            }
                        });
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        FirebaseDatabase.getInstance().getReference().child("users").addChildEventListener(profileHandler);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location loc : locationResult.getLocations()) {

                    final Double lat = loc.getLatitude();
                    final Double lon = loc.getLongitude();
                    storeRoom.sentLocToServer(lat, lon , getApplicationContext());
                } // loop ends here
            }
        };
    }// ends here

    public void startUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.requestLocationUpdates(locRequest, locationCallback, Looper.myLooper());
    }

    public void stopUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.removeLocationUpdates(locationCallback);
    }

    private void buildLocationRequest() {
        locRequest = new LocationRequest();
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locRequest.setInterval(80 * 1000);
        locRequest.setFastestInterval(3000);
        locRequest.setSmallestDisplacement(10);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdate();
    }
}
