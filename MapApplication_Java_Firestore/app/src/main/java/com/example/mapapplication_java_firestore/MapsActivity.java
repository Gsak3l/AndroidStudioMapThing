package com.example.mapapplication_java_firestore;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    public int counter = 0; //this counter counts how many marks we've added

    private FirebaseFirestore firebaseFirestore; //firebase
    private LocationListener locationListener; //location listener
    private LocationManager locationManager; //location manager
    //these two variables are for the sensitivity of our location
    private final long MIN_TIME = 1000;
    private final long MIN_DIST = 5;
    //the xml elements
    private EditText latitude;
    private EditText longitude;
    private GoogleMap mMap;
    private Spinner colorSpinner;
    private Button updateButton;
    private EditText userInput;
    //spinner arrayList
    private ArrayList<String> spinnerValues = new ArrayList<>();
    //sensor
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //finding by id both latitude and longitude editTexts
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        //finding the userInput by id
        userInput = findViewById(R.id.userInput);
        //finding the update button by id
        updateButton = findViewById(R.id.updateButton);
        //finding the spinner by id
        colorSpinner = findViewById(R.id.colorSpinner);

        //giving the spinner the available values;
        spinnerValues.add("Yellow");
        spinnerValues.add("Magenta");
        spinnerValues.add("Green");
        spinnerValues.add("Violet");
        spinnerValues.add("Orange");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, spinnerValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(dataAdapter);


        mapFragment.getMapAsync(this);
        //asking the user for his permission to use the location
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);

        //getting the firestore instance
        firebaseFirestore = FirebaseFirestore.getInstance();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    //updating the textBoxes for the longitude and latitude
                    longitude.setText(Double.toString(location.getLongitude()));
                    latitude.setText(Double.toString(location.getLatitude()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            //using both data, and gps to track the location
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UpdateButton(View view) {
        Map<String, Object> location = new HashMap<>();
        //Strings for all the values
        final String lat = latitude.getText().toString();
        final String lon = longitude.getText().toString();
        final String col = "HUE_" + colorSpinner.getSelectedItem().toString().toUpperCase();
        final String mes = userInput.getText().toString();

        //getting the humidity sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> humidity = sensorManager.getSensorList(Sensor.TYPE_RELATIVE_HUMIDITY);
        String hsi = "Sensor Name: " + humidity.get(0).getName() + ", Sensor Type: "
                + humidity.get(0).getType();

        //adding the right values to the location
        location.put("Latitude", lat);
        location.put("Longitude", lon);
        location.put("Marker_Color", col);
        location.put("User_Commend", mes);
        location.put("Sensor_Info", hsi);


        //adding the right color marker to the map basically
        counter++;
        if (counter < 6) {
            firebaseFirestore.collection("Map Locations").document("Location" + counter).set(location)
                    .addOnSuccessListener(new OnSuccessListener<Void>() { //if it succeeds
                        @Override
                        public void onSuccess(Void aVoid) {
                            //giving the latitude and longitude to the marker
                            LatLng latlng = new LatLng(Float.parseFloat(lon), Float.parseFloat(lat));
                            //switch case to change the color of the marker
                            switch (col) {
                                case "HUE_YELLOW":
                                    mMap.addMarker(new MarkerOptions().position(latlng).icon(
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                    break;
                                case "HUE_MAGENTA":
                                    mMap.addMarker(new MarkerOptions().position(latlng).icon(
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                                    break;
                                case "HUE_VIOLET":
                                    mMap.addMarker(new MarkerOptions().position(latlng).icon(
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
                                    break;
                                case "HUE_GREEN":
                                    mMap.addMarker(new MarkerOptions().position(latlng).icon(
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    break;
                                case "HUE_ORANGE":
                                    mMap.addMarker(new MarkerOptions().position(latlng).icon(
                                            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                    break;
                                default:
                                    mMap.addMarker(new MarkerOptions().position(latlng));
                                    break;
                            }
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                            Toast.makeText(MapsActivity.this, "Location Added Successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() { //if it fails
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapsActivity.this, "We couldn't add the location", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, e.toString());
                        }
                    });
        } else {
            counter = 0;
        }
    }
}
