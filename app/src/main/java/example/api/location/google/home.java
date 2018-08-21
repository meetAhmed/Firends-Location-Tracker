package example.api.location.google;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import example.api.location.google.Activities.profileView;
import example.api.location.google._Fragments.friends;
import example.api.location.google._Fragments.map;
import example.api.location.google._Fragments.notifications;
import example.api.location.google._Fragments.people;
import example.api.location.google.preferenceManager.preferenceManager;

public class home extends AppCompatActivity {

    private PlaceAutocompleteFragment placeAutocompleteFragment;
    ImageView user_dp;
    ArrayList<AlertDialog> dialogArrayList;

    public static home homeOBJECT;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.friends:
                    fragment = new friends();
                    break;
                case R.id.people:
                    fragment = new people();
                    break;
                case R.id.map:
                    fragment = new map();
                    break;
                case R.id.navigation_notifications:
                    fragment = new notifications();
                    break;
            }
            return loadFragment(fragment);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (home.homeOBJECT != null) {
            home.homeOBJECT.finish();
        }
        homeOBJECT = this;

        dialogArrayList = new ArrayList<>();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        getSupportActionBar().hide();
        loadFragment(new map());
        navigation.setSelectedItemId(R.id.map);

        user_dp = (ImageView) findViewById(R.id.user_dp);
        FirebaseDatabase.getInstance().getReference().child("users").child(preferenceManager.getkey(getApplicationContext())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                preferenceManager.writeUserName(getApplicationContext(), name);
                String profileRef = dataSnapshot.child("profileAddress").getValue().toString();
                if (!profileRef.equalsIgnoreCase("none")) {
                    Glide.with(getApplicationContext()).load(storeRoom.getImagesRootDir(profileRef)).into(user_dp);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        user_dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), profileView.class));
            }
        });

        placeAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentByTag("searchLocation");

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkGPSStatus()) {
            showMessage();
        }

        registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                final LatLng latLngLoc = place.getLatLng();
                Bundle bundle = new Bundle();
                bundle.putDouble("lon", latLngLoc.longitude);
                bundle.putDouble("lat", latLngLoc.latitude);
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onError(Status status) {
                Log.i("1231232", status.getStatusMessage() + "\n" + status.toString());
                Toast.makeText(getApplicationContext(), "" + status.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gpsReceiver);
    }

    private BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches(LocationManager.PROVIDERS_CHANGED_ACTION)) {
                if (!checkGPSStatus()) {
                    showMessage();
                } else {
                    if (dialogArrayList != null) {
                        for (AlertDialog dialog : dialogArrayList) {
                            dialog.dismiss();
                        }
                    }
                }
            }
        }
    };

    public void showMessage() {
        LayoutInflater layoutInflater = LayoutInflater.from(home.this);
        final View customView = layoutInflater.inflate(R.layout.location_disable_custom, null);
        AlertDialog.Builder myBox = new AlertDialog.Builder(home.this);
        myBox.setView(customView);
        AlertDialog dialog = myBox.create();
        dialogArrayList.add(dialog);
        dialog.setCancelable(false);

        final LinearLayout openSetting = (LinearLayout) customView.findViewById(R.id.requestbutton);
        final LinearLayout closeApp = (LinearLayout) customView.findViewById(R.id.cancel);
        final TextView content = (TextView) customView.findViewById(R.id.content);


        content.setText("Your location is off.\nFor the proper working of app please enable both GPS and Network location to estimate your location.");

        openSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        closeApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (home.homeOBJECT != null) {
                    home.homeOBJECT.finish();
                }
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private Boolean checkGPSStatus() {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
        if (gps_enabled && network_enabled) {
            return true;
        } else {
            return false;
        }
    }


    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            return true;
        }
        return false;
    }
}
