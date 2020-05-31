package com.example.mapapplication2;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

        //calling the same function for all the documents in the firestore
        //the documents when stored, are called Location1, Location2, etc...
        //so calling the function for location1... location5
        for (int i = 0; i < 6; i++) {
            firebaseFirestore.collection("Map Locations").document("Location" + i).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() { //success listener
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) { //checking if the location exists
                                addMapLocations(documentSnapshot);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    //creating the markers for the map
    public void addMapLocations(DocumentSnapshot documentSnapshot) {
        //getting all the values from the db, and storing them into these variables
        String lat = documentSnapshot.getString("Latitude");
        String lon = documentSnapshot.getString("Longitude");
        String col = documentSnapshot.getString("Marker_Color");
        String hsi = documentSnapshot.getString("Light_Info");
        String mes = documentSnapshot.getString("User_Commend");

        //giving the marker the latitude and longitude
        LatLng latlng = new LatLng(Float.parseFloat(lon), Float.parseFloat(lat));
        //switch case to change the color of the marker
        switch (col) {
            case "HUE_YELLOW":
                mMap.addMarker(new MarkerOptions().position(latlng).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)).
                        title(mes + ", Light:" + hsi));
                break;
            case "HUE_MAGENTA":
                mMap.addMarker(new MarkerOptions().position(latlng).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)).
                        title(mes + ", Light:" + hsi));
                break;
            case "HUE_VIOLET":
                mMap.addMarker(new MarkerOptions().position(latlng).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)).
                        title(mes + ", Light:" + hsi));
                break;
            case "HUE_GREEN":
                mMap.addMarker(new MarkerOptions().position(latlng).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).
                        title(mes + ", Light:" + hsi));
                break;
            case "HUE_ORANGE":
                mMap.addMarker(new MarkerOptions().position(latlng).icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)).
                        title(mes + ", Light:" + hsi));
                break;
            default:
                mMap.addMarker(new MarkerOptions().position(latlng).
                        title(mes + ", Light:" + hsi));
                break;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
    }
}
