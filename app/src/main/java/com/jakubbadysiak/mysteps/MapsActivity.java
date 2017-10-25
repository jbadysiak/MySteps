package com.jakubbadysiak.mysteps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.bitmap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private ArrayList<LatLng> latLngs;
    private GoogleMap mMap;
    private MarkerOptions markerStart;
    private MarkerOptions markerStop;
    private Intent intentMesseage;
    private Button btnSendActivity;
    private Polyline polyline;
    private String phoneDetails;
    private LatLngBounds.Builder builder;
//    public abstract void onSnapshotReady(Bitmap snapshot);
//
//    public final void snapshot(GoogleMap.SnapshotReadyCallback callback) {
//
//    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnSendActivity = (Button) findViewById(R.id.btnSendActiv);
        latLngs = getIntent().getParcelableArrayListExtra("LatLngs");
        phoneDetails = getIntent().getStringExtra("phoneDetails");

        intentMesseage = new Intent(getBaseContext(), SendActivity.class);

        btnSendActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentMesseage.setType("text/plain");
                intentMesseage.putExtra("phone",phoneDetails);

                startActivity(intentMesseage);
            }
        });

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
        builder = new LatLngBounds.Builder();

        drawPolylines();
        zoomRoad();
        //mapScreen();

    }

    private void mapScreen(){

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                            String currentDateAndTime = sdf.format(new Date());
                            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
                            if (!root.exists()) {
                                root.mkdirs();
                            }
                            File file = new File(root, currentDateAndTime + ".jpg");
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            //FileWriter fileWriter = new FileWriter(file);
                            //fileWriter.close();
                            //Toast.makeText(getApplicationContext(), "Phone details were saved on your phone.", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void zoomRoad() {
        for (int i = 0; i < latLngs.size(); i++){
            builder.include(latLngs.get(i));
        }

        int padding = 50;

        LatLngBounds bounds = builder.build();

        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback(){
            @Override
            public void onMapLoaded() {
                mMap.animateCamera(cu);
            }
        });
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
