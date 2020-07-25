package com.example.siptest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.os.Bundle;
import android.util.Log;

/**
 * Listens for incoming SIP calls, intercepts and hands them off to MainActivity.
 * android.siptest.INCOMING_CALL
 */
public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = "IncomingCallReceiver";
    SipAudioCall.Listener listener = null;
    IncomingCallDebug debug;

    public IncomingCallReceiver() {
    }

    public IncomingCallReceiver(SipAudioCall.Listener listener, IncomingCallDebug debug) {
        this.listener = listener;
        this.debug = debug;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: Intent received " + intent);
        try {
            debug.logThis(intent.getAction());
            SipUtils.takeAudioCall(intent, listener);
            debug.logThis(intent.toString());
            Bundle bundle = intent.getExtras();
            for (String k : bundle.keySet()) {
                String s = bundle.get(k).toString();
                debug.logThis("Key : "+k);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}