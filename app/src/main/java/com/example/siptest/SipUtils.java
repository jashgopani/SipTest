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
    private static String intentAction = "INCOMING_SIP_CALL";
    private static SipManager sipManager = null;

    public static String getIntentAction() {
        return intentAction;
    }

    public static void setIntentAction(String intentAction) {
        SipUtils.intentAction = intentAction;
    }

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
            builder.setProtocol("TCP");
            SipProfile profile = builder.build();

            Log.d(TAG, "buildLocalProfile: URI " + profile.getUriString());
            return profile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void openSipProfile(Context context, SipProfile profile, SipRegistrationListener listener) {
        if (profile != null) {
            //Open profile for calls
            Intent intent = new Intent();
            intent.setAction(getIntentAction());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, Intent.FILL_IN_DATA);
            try {
                Log.d(TAG, "openSipProfile: inside try-catch");
                sipManager.open(profile, pendingIntent, null);
                sipManager.setRegistrationListener(profile.getUriString(), listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean closeSipProfile(SipProfile profile) {
        if (sipManager == null) {
            return false;
        }
        try {
            if (profile != null)
                sipManager.close(profile.getUriString());
            return true;
        } catch (Exception ee) {
            Log.d(TAG, "closeSipProfile: Failed to close local profile.", ee);
            return false;
        }

    }

    public static void makeAudioCall(SipProfile myProfile, String peerUriString, SipAudioCall.Listener listener, int timeoutInSeconds) {
        try {
            sipManager.makeAudioCall(myProfile.getUriString(), peerUriString, listener, timeoutInSeconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void takeAudioCall(Intent incomingCallIntent, SipAudioCall.Listener listener) {

        try {
            SipAudioCall sipAudioCall = sipManager.takeAudioCall(incomingCallIntent, listener);
            listener.onCallEstablished(sipAudioCall);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void endOngoingCall(SipAudioCall ongoingCall){
        try{
            ongoingCall.endCall();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public static SipManager getSipManager() {
        return sipManager;
    }
}
