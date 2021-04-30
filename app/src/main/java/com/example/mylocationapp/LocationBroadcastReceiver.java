package com.example.mylocationapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.huawei.hms.location.ActivityIdentificationData;
import com.huawei.hms.location.ActivityIdentificationResponse;

import java.util.List;

public class LocationBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_LOCATION.equals(action)) {
                ActivityIdentificationResponse activityIdentificationResponse = ActivityIdentificationResponse.getDataFromIntent(intent);
                List<ActivityIdentificationData> list = activityIdentificationResponse .getActivityIdentificationDatas();
            }
        }
    }
}