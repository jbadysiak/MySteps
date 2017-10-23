package com.jakubbadysiak.mysteps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ArrayList<LatLng> latLngs;
    private GoogleMap mMap;
    private MarkerOptions markerStart;
    private MarkerOptions markerStop;
    private Polyline polyline;
    private MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mainActivity = new MainActivity();
        latLngs = getIntent().getParcelableArrayListExtra("LatLngs");



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }

        markerStart = new MarkerOptions().title("Start point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerStop = new MarkerOptions().title("Stop point").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        drawPolylines();

    }

    private void drawPolylines() {

        PolylineOptions polylineOptions = new PolylineOptions().color(Color.BLUE).width(8).geodesic(true);
        for (int i = 0; i < latLngs.size(); i++) {
            if (i == 0) {
                markerStart.position(latLngs.get(i));
                mMap.addMarker(markerStart);
            }
            if (i == (latLngs.size() - 1)) {
                markerStop.position(latLngs.get(i));
                mMap.addMarker(markerStop);
            }
            LatLng point = latLngs.get(i);
            polylineOptions.add(point);
        }

        polyline = mMap.addPolyline(polylineOptions);
    }
}
