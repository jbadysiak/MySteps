package com.jakubbadysiak.mysteps;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.jakubbadysiak.mysteps.Accelerometer.StepDetector;
import com.jakubbadysiak.mysteps.Accelerometer.StepListener;
import com.jakubbadysiak.mysteps.GSM.MyPhoneListener;
import com.jakubbadysiak.mysteps.Service.LocationTrackService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements SensorEventListener, StepListener {

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private LocationTrackService locationTrackService;

    private Intent intentMessage;

    private Intent intentMap;
    private StepDetector stepDetector;
    private SensorManager sensorManager;
    private BatteryManager batteryManager;
    private int batLevel;
    private Sensor accel;
    private static final String TEXT_NUM_STEPS = "Number of steps: ";
    private int numSteps;
    private Button btnStart, btnStop;
    private TextView tvSteps;
    private String phoneDetails;
    private float x, y, z;
    private TelephonyManager telephonyManager;
    private MyPhoneListener myPhoneListener;
    private double longitude, latitude, altitude;
    private ArrayList<LatLng> latLngList;
    private LatLng latLng;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        context = getBaseContext();
        latLngList = new ArrayList<>();
        batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stepDetector = new StepDetector();
        stepDetector.registerListener(this);
        myPhoneListener = new MyPhoneListener();
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(myPhoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        intentMap = new Intent(getBaseContext(), MapsActivity.class);

        intentMessage = new Intent(getBaseContext(), SendActivity.class);

        tvSteps = (TextView) findViewById(R.id.tvSteps);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setEnabled(false);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                locationTrackService = new LocationTrackService(MainActivity.this);

                if (locationTrackService.canGetLocation()) {
                    numSteps = 0;
                    sensorManager.registerListener(MainActivity.this, accel, SensorManager.SENSOR_DELAY_FASTEST);
                    btnStop.setEnabled(true);
                } else {
                    locationTrackService.showSettingsAlert();
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStopAlert();
            }
        });


    }

    public void showStopAlert() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Stop following me.");
        alertDialog.setMessage("Do you want to stop following?");

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                longitude = locationTrackService.getLongitude();
                latitude = locationTrackService.getLatitude();
                altitude = locationTrackService.getAltitude();

                latLng = new LatLng(latitude, longitude);
                latLngList.add(latLng);

                sensorManager.unregisterListener(MainActivity.this);
                locationTrackService.stopListener();

                batLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                phoneDetails = "Map data: \nLatitude=" + latitude + "\nLongitude=" + longitude + "\nAltitude=" + altitude + "\n" + TEXT_NUM_STEPS + numSteps + "\nAccelerator: \nX = " + x + "  Y = " + y + "  Z = " + z + "\nBattery = " + batLevel + "%\nSignal GSM = " + myPhoneListener.signalStrengthValue + "dB";

                numSteps = 0;

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    String currentDateAndTime = sdf.format(new Date());
                    File root = new File(Environment.getExternalStorageDirectory(), "Notes");
                    if (!root.exists()) {
                        root.mkdirs();
                    }
                    File file = new File(root, currentDateAndTime + ".txt");
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.append(phoneDetails.toString());
                    fileWriter.flush();
                    fileWriter.close();
                    Toast.makeText(getApplicationContext(), "Phone details were saved on your phone.", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String message = phoneDetails.toString();

                intentMap.putExtra("LatLngs", latLngList);
                intentMap.putExtra("phoneDetails", message);

                startActivity(intentMap);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();

        for (Object perm : wanted) {
            if (!hasPermission((String) perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(String.valueOf(permissionsRejected.get(0)))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrackService.stopListener();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            stepDetector.updateAccel(
                    sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]
            );
            x = sensorEvent.values[0];
            y = sensorEvent.values[1];
            z = sensorEvent.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(long timeNS) {
        numSteps++;
        tvSteps.setText(TEXT_NUM_STEPS + numSteps);

        locationTrackService = new LocationTrackService(MainActivity.this);

        if (numSteps == 1){
            if (locationTrackService.canGetLocation()) {
                longitude = locationTrackService.getLongitude();
                latitude = locationTrackService.getLatitude();

                LatLng latLng = new LatLng(latitude, longitude);
                latLngList.add(latLng);
                Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();

            } else {
                locationTrackService.showSettingsAlert();
            }
        }

        if (numSteps % 5 == 0) {


            if (locationTrackService.canGetLocation()) {
                longitude = locationTrackService.getLongitude();
                latitude = locationTrackService.getLatitude();

                LatLng latLng = new LatLng(latitude, longitude);
                latLngList.add(latLng);
                Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();

            } else {
                locationTrackService.showSettingsAlert();
            }
        }
    }
}
