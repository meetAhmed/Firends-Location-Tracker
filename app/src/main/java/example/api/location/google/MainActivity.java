package example.api.location.google;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RecyclerView locList;
    locAdapter ObjlocAdapter;
    ArrayList<model> modelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modelArrayList = new ArrayList<>();
        locList = (RecyclerView) findViewById(R.id.recViewer);
        locList.setItemAnimator(new DefaultItemAnimator());
        locList.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
        ObjlocAdapter = new locAdapter(modelArrayList);
        locList.setAdapter(ObjlocAdapter);

        readLocations();

       /* Bundle bundle = new Bundle();
        bundle.putString("suspect", "MULTP");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);

        */

    }

    public void readLocations() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user-Locations").child("MG");
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                model ob = dataSnapshot.getValue(model.class);
                modelArrayList.add(ob);
                ObjlocAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
