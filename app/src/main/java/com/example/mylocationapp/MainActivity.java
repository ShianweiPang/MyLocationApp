package com.example.mylocationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
//import com.huawei.hms.support.api.location.common.HMSLocationLog;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Step 1 : Create the Location Service Client
    // Initialise LocationRequest, FusedLocationProviderClient, and SettingsClient
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Dynamically apply for required permissions if the API level is 28 or smaller.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i("MainActivity", "android sdk <= 28 Q");
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, strings, 1);
            }
        } else {
            // Dynamically apply for required permissions if the API level is greater than 28. The android.permission.ACCESS_BACKGROUND_LOCATION permission is required.
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        "android.permission.ACCESS_BACKGROUND_LOCATION"};
                ActivityCompat.requestPermissions(this, strings, 2);
            }
        }

        findViewById(R.id.requestlocationupdate).setOnClickListener(this);
        findViewById(R.id.removerequestlocationupdate).setOnClickListener(this);

        // the "this" refers to this MainActivity or the context of the activity
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();
        // Set the interval for location update(unit:milliseconds)
        mLocationRequest.setInterval(10000);
        // Set the priority of the location request as high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void removeLocationUpdatesWithIntent() {
        // Note: When requesting location updates is stopped, mLocationCallback must be the same object as LocationCallback in the requestLocationUpdates method.
        mFusedLocationProviderClient.removeLocationUpdates(getPendingIntent())
                // Define callback for success in stopping requesting location updates.
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Location Request Successfully Stopped",LENGTH_SHORT).show();
                    }
                })
                // Define callback for failure in stopping requesting location updates.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(MainActivity.this, "Location Request Failed to Stop",LENGTH_SHORT).show();
                    }
                });
    }

    private void requestLocationUpdatesWithIntent() {
        // Check whether the app has allowed location setting
        // Put the parameters in builder
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        // return build value
        LocationSettingsRequest locationSettingsRequest = builder.build();
        // Check the device location settings.
        mSettingClient.checkLocationSettings(locationSettingsRequest)
                // Define callback for success in checking the device location settings.
                .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        // Initiate location requests when the location settings meet the requirements.
                        mFusedLocationProviderClient
                                .requestLocationUpdates(mLocationRequest, getPendingIntent())
                                // Define callback for success in requesting location updates.
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    // If success perform certain actions
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(MainActivity.this,"Location Setting Correct", LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                // Define callback for failure in checking the device location settings.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Device location settings do not meet the requirements.
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    Toast.makeText(MainActivity.this,"Location Setting Incorrect", LENGTH_SHORT).show();
                                    // Call startResolutionForResult to display a pop-up asking the user to enable related permission.
                                    rae.startResolutionForResult(MainActivity.this, 0);
                                } catch (IntentSender.SendIntentException sie) {
                                    Toast.makeText(MainActivity.this, (CharSequence) sie, LENGTH_SHORT).show();
                                }
                                break;
                        }
                    }
                });
    }

    private PendingIntent getPendingIntent() {
        //The LocationBroadcastReceiver class is a custom class. For detailed implementation methods, please refer to the sample code.
        Intent intent = new Intent(this, LocationBroadcastReceiver.class);
        intent.setAction(LocationBroadcastReceiver.ACTION_PROCESS_LOCATION);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void startIntent(Class<?> clazz) {
        Intent intent = new Intent(this,clazz);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        try{
            switch (v.getId()){
                case R.id.requestlocationupdate:
                    requestLocationUpdatesWithIntent();
                    break;
                case R.id.removerequestlocationupdate:
                    removeLocationUpdatesWithIntent();
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            Log.e("MainActivity", "Request Location Updates Wtih Intent Activity: "+e);
        }

    }
}