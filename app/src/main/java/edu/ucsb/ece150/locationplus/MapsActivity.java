package edu.ucsb.ece150.locationplus;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.Manifest;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Geofence mGeofence;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mPendingIntent = null;

    private GnssStatus.Callback mGnssStatusCallback;
    private GoogleMap mMap;
    private LocationManager mLocationManager;

    private Toolbar mToolbar;

    private Marker currentUserLocationMarker;
    private LatLng userLocation;

    private ImageButton toggleSatelliteListButton;
    private ListView satelliteList;
    private TextView viewFixCount;
    private TextView viewSatCount;
    private ArrayList<Satellite> satelliteArrayList;
    private ArrayAdapter<Satellite> adapter;
    boolean test1 = true;
    private static SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MapsActivity", "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //set up satellite listView
        satelliteList = findViewById(R.id.view_satellite_list);
        satelliteArrayList = new ArrayList<Satellite>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, satelliteArrayList);
        satelliteList.setAdapter(adapter);
        satelliteList.setVisibility(ListView.INVISIBLE);

        satelliteArrayList.add(new Satellite(-1, -1, -1, -1));

        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up Geofencing Client
        mGeofencingClient = LocationServices.getGeofencingClient(MapsActivity.this);


        viewFixCount = findViewById(R.id.viewFixCount);
        viewSatCount = findViewById(R.id.viewSatelliteCount);


        viewSatCount.setVisibility(View.INVISIBLE);
        viewFixCount.setVisibility(View.INVISIBLE);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mGnssStatusCallback = new GnssStatus.Callback() {

            @Override
            public void onStarted() {
                super.onStarted();
                Log.i("SATELLITEINFO", "Started Gnss fix ");
            }

            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                Log.i("SATELLITEINFO", "SatChange status: "+status.toString());
                int satelliteCount = 0;
                String info_satellite = null;
                String sat_num = null;
                satelliteCount = status.getSatelliteCount();
                sat_num = " Number of Satellites visible : " + satelliteCount + "\n";
                viewSatCount.setText(sat_num);
                Log.i("SATELLITEINFO", "SatChange Count: "+satelliteCount);
                satelliteArrayList.clear();
                for(int sat = 0; sat<satelliteCount; sat++ ){
                    info_satellite = " Satellite No - " + sat + ":" + "\n"
                            + "  Signal_strength : " + status.getCn0DbHz(sat) + "\n"
                            + "  Elevation_Angle : " + status.getElevationDegrees(sat) + "\n"
                            + "  Angle with True North(AZm number) : " + status.getAzimuthDegrees(sat) + "\n"
                            + "  Identification_number_of_satellite : " + status.getSvid(sat) + "\n"
                            + "  Is usedInFix : " + status.usedInFix(sat)
                            + "\n";

                    int prn = status.getSvid(sat);
                    float azimuthDegrees = status.getAzimuthDegrees(sat);
                    float elevationDegrees = status.getElevationDegrees(sat);
                    float cn0DbHz = status.getCn0DbHz(sat);


                    Log.i("SATELLITEINFO", "\t \t Details: "+info_satellite);
                    Satellite newSatellite = new Satellite(prn, azimuthDegrees, elevationDegrees, cn0DbHz);
                    Log.d("TAG", "onSatelliteStatusChanged: added satellite");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            satelliteArrayList.add(newSatellite);
                            adapter.notifyDataSetChanged();
                            Log.d("GOTEM", "run: ");
                            // Update your UI elements here
                        }
                    });

                    if (info_satellite != null) {
                        Log.i("info_satellite", info_satellite);
                    }
                }

            }
        };

        // [TODO] Additional setup for viewing satellite information (lists, adapters, etc.)
        toggleSatelliteListButton = findViewById(R.id.button_satellite_list_toggle);


        toggleSatelliteListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TEST1", "onClick: 1");
                // Toggle the visibility of the ListView
                if (satelliteList.getVisibility() == ListView.VISIBLE) {
                    Log.d("TEST2", "onClick: 2");
                    satelliteList.setVisibility(ListView.INVISIBLE);
                    viewSatCount.setVisibility(View.INVISIBLE);
                    viewFixCount.setVisibility(View.INVISIBLE);

                } else {

                    satelliteList.setVisibility(ListView.VISIBLE);
                    viewSatCount.setVisibility(View.VISIBLE);
                    viewFixCount.setVisibility(View.VISIBLE);
                    Log.d("TEST3", "onClick: 3");

                }
            }
        });

        // Set up Toolbar
        mToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // [TODO] Implement behavior when Google Maps is ready
        ImageButton btnCenterOnUser = findViewById(R.id.button_center_on_user);
        btnCenterOnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MapsActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                    return;
                }
                Location lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (lastKnownLocation != null) {
                    userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15)); // You can define the zoom level
                }
            }
        });

        // [TODO] In addition, add a listener for long clicks (which is the starting point for
        // creating a Geofence for the destination and listening for transitions that indicate
        // arrival)
    }

    @Override
    public void onLocationChanged(Location location) {
        // [TODO] Implement behavior when a location update is received
        userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // If it's the first location update or if marker doesn't exist
        if (currentUserLocationMarker == null) {
            // Create a marker options object
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(userLocation);
            markerOptions.title("Current Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            // Add marker to the map and save it
            currentUserLocationMarker = mMap.addMarker(markerOptions);
        } else {
            // Update the position of the existing marker
            currentUserLocationMarker.setPosition(userLocation);
        }

        // Move the camera to the user's location and zoom in
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    /*
     * The following three methods onProviderDisabled(), onProviderEnabled(), and onStatusChanged()
     * do not need to be implemented -- they must be here because this Activity implements
     * LocationListener.
     *
     * You may use them if you need to.
     */
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private GeofencingRequest getGeofenceRequest() {
        // [TODO] Set the initial trigger (i.e. what should be triggered if the user is already
        // inside the Geofence when it is created)

        return new GeofencingRequest.Builder()
                //.setInitialTrigger()  <--  Add triggers here
                .addGeofence(mGeofence)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if(mPendingIntent != null)
            return mPendingIntent;

        Intent intent = new Intent(MapsActivity.this, GeofenceBroadcastReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mPendingIntent;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() throws SecurityException {
        super.onStart();
        Log.d("MapsActivity", "onStart: ");
        // [DONE] Ensure that necessary permissions are granted (look in AndroidManifest.xml to
        // see what permissions are needed for this app)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("here1", "onResume: ");
        //loadCurrentLocationMarkerState();
        //satelliteArrayList = getArrayListFromSharedPreferences(this);
        // [TODO] Data recovery
    }


    private void saveCurrentLocationMarkerState(LatLng position) {
        if (position != null) {
            sharedPref = getSharedPreferences("LocationPlusPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat("marker_lat", (float) position.latitude);
            editor.putFloat("marker_lng", (float) position.longitude);
            editor.apply();
        }
    }

    private LatLng loadCurrentLocationMarkerState() {
        sharedPref = getSharedPreferences("LocationPlusPrefs", Context.MODE_PRIVATE);
        float lat = sharedPref.getFloat("marker_lat", -1);
        float lng = sharedPref.getFloat("marker_lng", -1);
        if (lat != -1 && lng != -1) {
            return new LatLng(lat, lng);
        }
        return null;
    }

    public static ArrayList<Satellite> getArrayListFromSharedPreferences(Context context) {
        // Get the SharedPreferences object
        sharedPref = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        // Retrieve the JSON string from SharedPreferences
        String jsonArray = sharedPref.getString("satelliteList", null);

        if (jsonArray != null) {
            // Initialize Gson for JSON conversion
            Gson gson = new Gson();

            // Convert the JSON string back to an ArrayList
            Type type = new TypeToken<ArrayList<Satellite>>() {}.getType();
            return gson.fromJson(jsonArray, type);
        } else {
            // Return an empty ArrayList if the data doesn't exist in SharedPreferences
            return new ArrayList<>();
        }
    }

    public static void saveArrayListToSharedPreferences(Context context, ArrayList<Satellite> satelliteList) {
        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        // Initialize Gson for JSON conversion
        Gson gson = new Gson();

        // Convert the ArrayList to a JSON string
        String jsonArray = gson.toJson(satelliteList);

        // Save the JSON string in SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("satelliteList", jsonArray);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentLocationMarkerState(userLocation);
        saveArrayListToSharedPreferences(this, satelliteArrayList);
        // [TODO] Data saving
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();

        mLocationManager.removeUpdates(this);
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
    }
}
