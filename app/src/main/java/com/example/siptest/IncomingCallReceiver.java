package com.example.siptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.util.Log;

/**
 * Listens for incoming SIP calls, intercepts and hands them off to MainActivity.
 * android.siptest.INCOMING_CALL
 */
public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Intent received "+intent);
        if(intent!=null)
        Log.d(TAG, "onReceive: Action is "+intent.getAction());
    }
}