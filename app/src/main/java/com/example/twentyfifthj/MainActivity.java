package com.example.twentyfifthj;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseReference databaseReference;
    private HashMap<String, Marker> markers = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        Button btnNormal = findViewById(R.id.btnNormal);
        Button btnHybrid = findViewById(R.id.btnHybrid);
        Button btnSatellite = findViewById(R.id.btnSatellite);
        Button btnTerrain = findViewById(R.id.btnTerrain);

        btnNormal.setOnClickListener(v -> mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL));
        btnHybrid.setOnClickListener(v -> mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID));
        btnSatellite.setOnClickListener(v -> mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE));
        btnTerrain.setOnClickListener(v -> mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN));

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Replace "your-firebase-database-url" with your Firebase database URL
        databaseReference = FirebaseDatabase.getInstance().getReference("Players");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot playerSnapshot : dataSnapshot.getChildren()) {
                    String playerId = playerSnapshot.getKey();
                    double lat = playerSnapshot.child("Lat").getValue(Double.class);
                    double lng = playerSnapshot.child("Long").getValue(Double.class);
                    updateMarker(playerId, new LatLng(lat, lng));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("Firebase", "Error: " + databaseError.getMessage());
            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    private void updateMarker(String playerId, LatLng latLng) {
        if (markers.containsKey(playerId)) {
            markers.get(playerId).setPosition(latLng);
        } else {
            databaseReference.child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String playerName = dataSnapshot.child("Name").getValue(String.class);
                    float hue = new Random().nextFloat() * 360;
                    Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(playerName).icon(BitmapDescriptorFactory.defaultMarker(hue)));
                    markers.put(playerId, marker);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("Firebase", "Error: " + databaseError.getMessage());
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng defaultLocation = new LatLng(19.076090, 72.877426); // Default location if Firebase data is not available
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}