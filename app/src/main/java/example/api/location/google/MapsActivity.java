package example.api.location.google;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import example.api.location.google.preferenceManager.preferenceManager;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    String text;
    ArrayList<LatLng> coordList;
    private List<Polyline> polylines;
    ProgressDialog dialog;
    Bundle bundle = null;
    LatLng star, end;
    TextView distance, time;
    RadioGroup routeMode;
    double radiusInMeters = 80.0;
    int strokeColor = 0xffff0000; //red outline
    int shadeColor = 0x44ff0000; //opaque red fill
    Marker userMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_act_parent);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dialog = new ProgressDialog(this);
        distance = (TextView) findViewById(R.id.distance);
        time = (TextView) findViewById(R.id.time);
        routeMode = (RadioGroup) findViewById(R.id.routeMode);

        bundle = getIntent().getExtras();
        end = new LatLng(bundle.getDouble("lat"), bundle.getDouble("lon"));

        routeMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.drivingMode:
                        getRouteToMarker(end, AbstractRouting.TravelMode.DRIVING);
                        break;
                    case R.id.walkingMode:
                        getRouteToMarker(end, AbstractRouting.TravelMode.WALKING);
                        break;
                }
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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

        if (bundle != null) {
            getRouteToMarker(end, AbstractRouting.TravelMode.WALKING);
        }

    }

    public LatLng getSuspectLoc() {
        if (bundle != null) {
            return new LatLng(bundle.getDouble("lat"), bundle.getDouble("lon"));
        }
        return null;
    }

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.marker_layout, null);

        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
        //   TextView txt_name = (TextView) marker.findViewById(R.id.name);
        // txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);
        return bitmap;
    }

    public void setMarker(Double lat, Double lon) {
        String name = storeRoom.getUserLocAddress(lat, lon, getApplicationContext());
        if (name == null) {
            name = "Location";
        }
        LatLng loc = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(loc).title(name));
    }


    public void setMarker(LatLng loc) {
        String name = storeRoom.getUserLocAddress(loc.latitude, loc.longitude, MapsActivity.this);
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

        InfoWindowData info = new InfoWindowData();
        info.setPlace(name);
        info.setTime("");
        info.setUsername("");

        customInfoAdapter adapter = new customInfoAdapter(MapsActivity.this);
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


    public void showMultiplePoints() {
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.addAll(storeRoom.reverseList(coordList));
        polylineOptions.width(5).color(Color.RED).geodesic(true);
        mMap.addPolyline(polylineOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordList.get(0), 15));
        // setMarker(coordList.get(0));
    }


    private void getRouteToMarker(LatLng end, AbstractRouting.TravelMode mode) {

        dialog.setMessage("Loading data");
        dialog.show();
        dialog.setCancelable(false);
        Routing routing = new Routing.Builder()
                .travelMode(mode)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(preferenceManager.getUserLocation(getApplicationContext()), end)
                .build();
        routing.execute();
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        dialog.dismiss();
        if (e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        dialog.dismiss();
        erasePolylines();
        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(R.color.dark_blue));
            polyOptions.width(25);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            setMarker(end);
            setMarker(preferenceManager.getUserLocation(getApplicationContext()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(end, 15));
            polylines.add(polyline);

            time.setText(route.get(i).getDurationText());
            distance.setText(route.get(i).getDistanceText());

            String fro = "Distance text " + route.get(i).getDistanceText() + "\n";
            fro += "Distance value " + route.get(i).getDistanceValue() + "\n";
            fro += "Distance value " + route.get(i).getDistanceValue() + "\n";
            fro += "Distance value/1000 " + (route.get(i).getDistanceValue() / 1000) + "\n";
            fro += "Time text " + route.get(i).getDurationText() + "\n";
            fro += "Time value " + route.get(i).getDurationValue() + "\n";
            fro += "Time value/1000 " + route.get(i).getDurationValue() + "\n";
            Log.i("1231232", fro);


            // Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onRoutingCancelled() {
        dialog.dismiss();
    }

    private void erasePolylines() {
        if (polylines == null) {
            return;
        }
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }
}
