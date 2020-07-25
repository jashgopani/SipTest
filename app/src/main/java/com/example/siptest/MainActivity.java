package com.example.siptest;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipErrorCode;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    SipAudioCall call;
    SipManager sipManager;
    Context context = MainActivity.this;
    EditText myusername, mypassword, targetUsername;
    Button openProfileBtn, callBtn, isRegisteredBtn, closeProfileBtn, endCallBtn;
    TextView logtv;
    String username, password;
    SipProfile currentuser;
    String domain = "sip.allincall.in";
    String peerUsername = "";

    SipRegistrationListener sipRegistrationListener = new SipRegistrationListener() {
        @Override
        public void onRegistering(String s) {
            Log.d(TAG, "onRegistering: SipRegisterationListener " + s);
            pushToLog("onRegistering: SipRegisterationListener " + s);
        }

        @Override
        public void onRegistrationDone(String s, long l) {
            Log.d(TAG, "onRegistrationDone: SipRegisterationListener " + s + " | " + l);
            pushToLog("onRegistrationDone: SipRegisterationListener " + s + " | " + l);
        }

        @Override
        public void onRegistrationFailed(String s, int i, String s1) {
            Log.d(TAG, "onRegistrationFailed: SipRegisterationListener " + SipErrorCode.toString(i));
            pushToLog("onRegistrationFailed: SipRegisterationListener " + SipErrorCode.toString(i));
        }
    };

    SipAudioCall.Listener audioCallListener = new SipAudioCall.Listener() {
        private static final String TAG = "SipAudioCall.Listener";

        @Override
        public void onReadyToCall(SipAudioCall call) {
            super.onReadyToCall(call);
            Log.d(TAG, "onReadyToCall: AudioCallListener");
            pushToLog("onReadyToCall: AudioCallListener");

        }

        @Override
        public void onCalling(SipAudioCall call) {
            super.onCalling(call);
            Log.d(TAG, "onCalling: ");

        }

        @Override
        public void onRinging(SipAudioCall call, SipProfile caller) {
            super.onRinging(call, caller);
            Log.d(TAG, "onRinging: ");
        }

        @Override
        public void onRingingBack(SipAudioCall call) {
            super.onRingingBack(call);
            Log.d(TAG, "onRingingBack: ");
        }

        @Override
        public void onCallEstablished(SipAudioCall call) {
            super.onCallEstablished(call);
            Log.d(TAG, "onCallEstablished: ");
            call.startAudio();
            call.setSpeakerMode(true);
            call.toggleMute();
        }

        @Override
        public void onCallEnded(SipAudioCall call) {
            super.onCallEnded(call);
        }

        @Override
        public void onCallBusy(SipAudioCall call) {
            super.onCallBusy(call);
            Log.d(TAG, "onCallBusy: ");
        }

        @Override
        public void onCallHeld(SipAudioCall call) {
            super.onCallHeld(call);
            Log.d(TAG, "onCallHeld: ");
        }

        @Override
        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
            super.onError(call, errorCode, errorMessage);
            Log.d(TAG, "onError: ");
        }


        @Override
        public void onChanged(SipAudioCall call) {
            super.onChanged(call);
            Log.d(TAG, "onChanged: ");
        }
    };
    private Runnable isRegisteredRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "run: isRegistered : " + sipManager.isRegistered(currentuser.getUriString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermissions();

        myusername = findViewById(R.id.sip_username);
        mypassword = findViewById(R.id.sip_password);
        targetUsername = findViewById(R.id.sip_target);
        logtv = findViewById(R.id.logtv);
        openProfileBtn = findViewById(R.id.register_btn);
        closeProfileBtn = findViewById(R.id.unregister_btn);
        isRegisteredBtn = findViewById(R.id.isRegistered_btn);
        callBtn = findViewById(R.id.call_btn);
        endCallBtn = findViewById(R.id.end_call_btn);

        sipManager = SipManager.newInstance(context);
        if (sipManager == null) {
            Toast.makeText(context, "Device does not support SIP Calling", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: Sip Manager null");
        } else {
            Toast.makeText(context, "Device Supports SIP Calling", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: Sip Manager Instantiated");
        }

        openProfileBtn.setOnClickListener(view -> {
            Log.d(TAG, "onClick: Register Button Clicked");
            registerProfile();
        });

        closeProfileBtn.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: Unregister Btn clicked");
            unregisterProfile();
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Call button Clicked");
                callTarget();
            }
        });

        isRegisteredBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().post(isRegisteredRunnable);
            }
        });

    }


    private void getPermissions() {
        String[] permissions = {
                Manifest.permission.USE_SIP,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
        };
        try {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setTarget() {
        String target = this.targetUsername.getText().toString();
        if (target == null || target.trim().length() == 0) {
            if (username.startsWith("6")) target = "7001";
        }
        targetUsername.setText(target);
        peerUsername = target;
    }

    private void callTarget() {
        Log.d(TAG, "callTarget: Inside Call Target");
        setTarget();
        SipProfile peer = buildLocalProfile(peerUsername, peerUsername, domain);
        try {
            call = sipManager.makeAudioCall(currentuser.getUriString(), peer.getUriString(), null, 30);
            call.setListener(audioCallListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerProfile() {
        username = myusername.getText().toString();
        password = mypassword.getText().toString();
        Log.d(TAG, "registerProfile: Trying to register " + username + ", " + password);
        setTarget();

        //Build local profile
        currentuser = buildLocalProfile(username, password, domain);

        if (currentuser != null) {
            Log.d(TAG, "registerProfile: Current User Profile Created ");
            Toast.makeText(context, "Your Local Profile is created", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "registerProfile: Current User Profile Not Created");
            Toast.makeText(context, "Your Local Profile is Not created", Toast.LENGTH_SHORT).show();
        }

        //Open profile for calls
        Intent intent = new Intent();
        intent.setAction("android.siptest.INCOMING_CALL");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, Intent.FILL_IN_DATA);
        try {
            sipManager.open(currentuser, pendingIntent, null);
            sipManager.setRegistrationListener(currentuser.getUriString(), sipRegistrationListener);
            new Handler().postDelayed(isRegisteredRunnable, 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void unregisterProfile() {

    }
    private SipProfile buildLocalProfile(String username, String password, String domain) {
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

    public void pushToLog(final String msg) {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                String s = logtv.getText().toString();
                logtv.setText(msg + "\n*******\n" + s);
            }
        });
    }

    public void closeLocalProfile() {
        if (sipManager == null) {
            return;
        }
        try {
            if (currentuser != null) {
                sipManager.close(currentuser.getUriString());
            }
        } catch (Exception ee) {
            Log.d(TAG, "closeLocalProfile: Failed to close local profile.", ee);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeLocalProfile();
    }
}