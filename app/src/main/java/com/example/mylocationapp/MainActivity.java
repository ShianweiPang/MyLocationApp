package com.example.mylocationapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.ActivityIdentificationService;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.HWLocation;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.utils.JsonUtil;
//import com.huawei.hms.support.api.location.common.HMSLocationLog;

import org.w3c.dom.ls.LSOutput;

import java.util.Iterator;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Step 1 : Create the Location Service Client
    // Initialise LocationRequest, FusedLocationProviderClient, and SettingsClient
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingClient;
    TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dynamicPermission();

        findViewById(R.id.requestlocationupdate).setOnClickListener(this);
        findViewById(R.id.removerequestlocationupdate).setOnClickListener(this);
        txtResult = findViewById(R.id.txtResult);

        // the "this" refers to this MainActivity or the context of the activity
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();
        // Set the interval for location update(unit:milliseconds)
        mLocationRequest.setInterval(10000);
        // Set the priority of the location request as high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNeedAddress(true);
        mLocationRequest.setLanguage("en");
        mLocationRequest.setCountryCode("EN");

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    List<Location> locations = locationResult.getLocations();
                    List<HWLocation> hwLocations = locationResult.getHWLocationList();

//                    Iterator locationsIterator=locations.iterator();
//                    Iterator HWlocationsIterator=hwLocations.iterator();
//                    while (locationsIterator.hasNext()&&HWlocationsIterator.hasNext()){
//                        Location location = (Location) locationsIterator.next();
//                        HWLocation hwlocation = (HWLocation) HWlocationsIterator.next();
//                        txtResult.setText("Location [Longitude,Latitude,Accuracy]: "
//                                    + "\n" + location.getLongitude()
//                                    + "\n" + location.getLatitude()
//                                    + "\n" + hwlocation.getCity()+","+ hwlocation.getCountryCode()+ ","+ hwlocation.getCountryName() +","+hwlocation.getLatitude()+ ","+hwlocation.getLongitude()
//                                    + "\n" + location.getAccuracy());
//                    }
                    if (!hwLocations.isEmpty()) {
                        for (HWLocation location : hwLocations) {
                            Log.i("MainActivity", "onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                                            + "," + location.getLatitude() + "," + location.getAccuracy());

                            txtResult.setText("Location [Longitude,Latitude,Accuracy]: "
                                    + "\n" + location.getLongitude()
                                    + "\n" + location.getLatitude()
                                    + "\n" + location.getCountryCode()
                                    + "\n" + location.getCountryName()
                                    + "\n" + location.getCity()
                                    + "\n" + location.getState()
                                    + "\n" + location.getCounty()
                                    + "\n" + location.getTime()
                                    + "\n" + location.getAccuracy());
                        }
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (locationAvailability != null) {
                    boolean flag = locationAvailability.isLocationAvailable();
                    Log.i("MainActivity", "onLocationAvailability isLocationAvailable:" + flag);
                }
            }
        };
    }

    private void dynamicPermission() {
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
    }

    private void removeLocationUpdatesWithIntent() {
        // Note: When requesting location updates is stopped, mLocationCallback must be the same object as LocationCallback in the requestLocationUpdates method.
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
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
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();
            // Check the device location settings.
            Task<LocationSettingsResponse> locationSettingsResponseTask = mSettingClient.checkLocationSettings(locationSettingsRequest);
            locationSettingsResponseTask
                    // Define callback for success in checking the device location settings.
                    .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i("MainActivity", "check location settings success");
                            // Initiate location requests when the location settings meet the requirements.
                            mFusedLocationProviderClient
                                    .requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
                                    // Define callback for success in requesting location updates.
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        // If success perform certain actions
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i("MainActivity", "requestLocationUpdatesWithCallback onSuccess");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        // If failed perform certain actions
                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e("MainActivity", "requestLocationUpdatesWithCallback onFailure:" + e.getMessage());
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
                                        Toast.makeText(MainActivity.this, "Location Setting Incorrect", LENGTH_SHORT).show();
                                        // Call startResolutionForResult to display a pop-up asking the user to enable related permission.
                                        rae.startResolutionForResult(MainActivity.this, 0);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Toast.makeText(MainActivity.this, (CharSequence) sie, LENGTH_SHORT).show();
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
        }catch (Exception e){
            Log.e("MainActivity","requestLocationUpdates Exception"+ e.getMessage());
        }
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