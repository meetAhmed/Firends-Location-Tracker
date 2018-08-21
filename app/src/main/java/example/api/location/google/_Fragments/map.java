package example.api.location.google._Fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import example.api.location.google.InfoWindowData;
import example.api.location.google.Models.linkedPeople;
import example.api.location.google.R;
import example.api.location.google.customInfoAdapter;
import example.api.location.google.home;
import example.api.location.google.infoAdapter;
import example.api.location.google.model;
import example.api.location.google.preferenceManager.preferenceManager;
import example.api.location.google.storeRoom;

public class map extends Fragment implements OnMapReadyCallback {

    FusedLocationProviderClient client;
    LocationRequest locRequest;
    LocationCallback locationCallback;
    final int requestCode = 123;

    View mView;
    private GoogleMap mMap;
    Marker userMarker = null;
    RecyclerView locList;
    infoAdapter ObjlocAdapter;
    ArrayList<linkedPeople> modelArrayList;
    private SupportMapFragment fragment;

    double radiusInMeters = 80.0;
    int strokeColor = 0xffff0000; //red outline
    int shadeColor = 0x44ff0000; //opaque red fill

    String userKey;
    boolean isMapReady = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.activity_user_loc, container, false);
        //SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        // mapFragment.getMapAsync(this);
        userKey = preferenceManager.getkey(getActivity());
        FragmentManager fm = getChildFragmentManager();
        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        if (fragment == null) {
            fragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map, fragment).commit();
            fragment.getMapAsync(this);
        }

        modelArrayList = new ArrayList<>();
        locList = (RecyclerView) mView.findViewById(R.id.recViewer);
        locList.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        locList.setLayoutManager(layoutManager);
        ObjlocAdapter = new infoAdapter(modelArrayList, new infoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, final String name) {
                linkedPeople people = modelArrayList.get(position);
                if (userKey == null) {
                    userKey = preferenceManager.getkey(getActivity());
                }
                String otherKey = null;
                if (userKey.equalsIgnoreCase(people.getSender())) {
                    otherKey = people.getReceiver();
                } else {
                    otherKey = people.getSender();
                }

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("user-Locations");
                Query lastQuery = databaseReference.child(otherKey).orderByKey().limitToLast(1);
                lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            model o = data.getValue(model.class);
                            LatLng loc = new LatLng(o.getLat(), o.getLon());
                            setMarker(loc, name, o.getTime());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Handle possible errors.
                    }
                });

                ObjlocAdapter.selectedPosition = position;
                ObjlocAdapter.notifyDataSetChanged();

            }
        });
        locList.setAdapter(ObjlocAdapter);
        readUsernames();
        return mView;
    }

    public void startUpdate() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
            return;
        }
        client.requestLocationUpdates(locRequest, locationCallback, Looper.myLooper());
    }

    public void stopUpdate() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        client.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopUpdate();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (home.homeOBJECT != null) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
            } else {
                // we have permissions
                client = LocationServices.getFusedLocationProviderClient(getActivity());
                buildLocationRequest();
                buildLocationCallback();
                startUpdate();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(code, permissions, grantResults);
        switch (code) {
            case requestCode:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    }
                }

        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location loc : locationResult.getLocations()) {

                    final Double lat = loc.getLatitude();
                    final Double lon = loc.getLongitude();

                    preferenceManager.writeLocation(getActivity(), String.valueOf(lat), String.valueOf(lon));
                    storeRoom.sentLocToServer(lat, lon, getActivity());

                    String username = preferenceManager.getUserName(getActivity());
                    if (username.charAt(0) != '@') {
                        username = "@" + username;
                    }
                    if (isMapReady) {
                        setMarker(new LatLng(lat, lon), username, System.currentTimeMillis());
                    }
                } // loop ends here
            }
        };
    }// ends here

    private void buildLocationRequest() {
        locRequest = new LocationRequest();
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locRequest.setInterval(80 * 1000);
        locRequest.setFastestInterval(3000);
        locRequest.setSmallestDisplacement(10);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;
        UiSettings settings = mMap.getUiSettings();
        mMap.setTrafficEnabled(true);
        settings.setAllGesturesEnabled(true);
        settings.setCompassEnabled(true);
        settings.setMyLocationButtonEnabled(true);
        settings.setRotateGesturesEnabled(true);
        settings.setScrollGesturesEnabled(true);
        settings.setTiltGesturesEnabled(true);
        settings.setZoomControlsEnabled(true);
        settings.setZoomGesturesEnabled(true);
    }

    public void setMarker(LatLng loc, String username, Long time) {
        String name = storeRoom.getUserLocAddress(loc.latitude, loc.longitude, getActivity());
        if (name == null) {
            name = "You are here ";
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(loc)
                .radius(radiusInMeters)
                .fillColor(shadeColor)
                .strokeColor(strokeColor)
                .strokeWidth(8);
        mMap.addCircle(circleOptions);
        if (userMarker != null) {
            userMarker.remove();
            userMarker = null;
        }

        InfoWindowData info = new InfoWindowData();
        info.setPlace(name);
        info.setTime(storeRoom.getDateForNotification(time));
        info.setUsername(username);

        customInfoAdapter adapter = new customInfoAdapter(getActivity());
        mMap.setInfoWindowAdapter(adapter);

        userMarker = mMap.addMarker(new MarkerOptions().position(loc).title(""));
        userMarker.setTag(info);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    if (userMarker.isInfoWindowShown()) {
                        userMarker.hideInfoWindow();
                    } else {
                        userMarker.showInfoWindow();
                    }
                } catch (NullPointerException e) {

                }
                return false;
            }
        });


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
    }


    public void readUsernames() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("LinkedPeople")
                .child("Friends").child(preferenceManager.getkey(getActivity()));
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                linkedPeople model = dataSnapshot.getValue(linkedPeople.class);
                if (model.getRequestStatus().equalsIgnoreCase("accepted")) {
                    modelArrayList.add(model);
                    ObjlocAdapter.notifyDataSetChanged();
                }
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
