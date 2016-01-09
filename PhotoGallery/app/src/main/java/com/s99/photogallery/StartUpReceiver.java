package com.s99.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartUpReceiver extends BroadcastReceiver {
    private final String TAG = "StartUpReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received Broadcast Intent: " + intent.getAction());

        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }
}
