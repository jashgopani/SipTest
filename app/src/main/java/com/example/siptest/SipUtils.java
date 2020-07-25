package com.example.siptest;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.util.Log;

public class SipUtils {
    private static final String TAG = "SipUtils";
    private static SipManager sipManager = null;

    public static SipManager getSipManager(Context context) {
        if (sipManager == null)
            sipManager = SipManager.newInstance(context);

        return sipManager;
    }

    public static SipProfile buildLocalProfile(String username, String password, String domain) {
        try {

            SipProfile.Builder builder = new SipProfile.Builder(username, domain);
            builder.setAuthUserName(username);
            builder.setPassword(password);
            builder.setAutoRegistration(true);
            builder.setSendKeepAlive(true);
            SipProfile profile = builder.build();

            Log.d(TAG, "buildLocalProfile: URI " + profile.getUriString());
            return profile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void openSipProfile(Context context, SipProfile profile, SipRegistrationListener listener, String intentAction) {
        if (profile != null) {
            //Open profile for calls
            Intent intent = new Intent();
            intent.setAction(intentAction);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, Intent.FILL_IN_DATA);
            try {
                sipManager.open(profile, pendingIntent, null);
                sipManager.setRegistrationListener(profile.getUriString(), listener);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void closeSipProfile(SipProfile profile) {
        if (sipManager == null) {
            return;
        }
        try {
            if (profile != null) {
                sipManager.close(profile.getUriString());
            }
        } catch (Exception ee) {
            Log.d(TAG, "closeSipProfile: Failed to close local profile.", ee);
        }
    }

    public static SipAudioCall makeAudioCall(SipProfile myProfile, SipProfile peerProfile, SipAudioCall.Listener listener, int timeoutInSeconds) {
        try {
            return sipManager.makeAudioCall(myProfile.getUriString(), peerProfile.getUriString(), listener, timeoutInSeconds);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SipAudioCall takeAudioCall(Intent incomingCallIntent, SipAudioCall.Listener listener){

        try{
            return sipManager.takeAudioCall(incomingCallIntent,listener);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
