package com.example.siptest;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.sip.SipAudioCall;
import android.net.sip.SipErrorCode;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.net.sip.SipSession;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity implements IncomingCallDebug {
    private static final String TAG = "MainActivity";
    Handler mainHandler;
    SipManager sipManager;
    Context context = MainActivity.this;
    EditText myusername, mypassword, targetUsername;
    Button openProfileBtn, isRegisteredBtn, closeProfileBtn, clearLogBtn;
    ToggleButton callBtn;
    TextView logtv;
    String username, password;
    SipProfile currentuser;
    String domain = "sip.allincall.in";
    String intentAction = "android.siptest.INCOMING_CALL";
    String peerUsername = "";
    boolean canCall = false;
    SipAudioCall incomingCall;

    //for incall layout
    ScrollView logScrollView;
    LinearLayout inCallLayout;
    TextView peerNameTv, callStatusTv;
    ToggleButton toggleSpeaker, toggleMute;

    SipRegistrationListener sipRegistrationListener = new SipRegistrationListener() {
        @Override
        public void onRegistering(String s) {
            Log.d(TAG, "onRegistering: SipRegisterationListener " + s);
            pushToLog("onRegistering: SipRegisterationListener " + s);
            canCall = false;
        }

        @Override
        public void onRegistrationDone(String s, long l) {
            Log.d(TAG, "onRegistrationDone: SipRegisterationListener " + s + " | " + l);
            pushToLog("onRegistrationDone: SipRegisterationListener " + s + " | " + l);
            canCall = true;
        }

        @Override
        public void onRegistrationFailed(String s, int i, String s1) {
            Log.d(TAG, "onRegistrationFailed: SipRegisterationListener " + SipErrorCode.toString(i));
            pushToLog("onRegistrationFailed: SipRegisterationListener " + SipErrorCode.toString(i));
            canCall = false;
        }
    };

    SipAudioCall.Listener audioCallListener = new SipAudioCall.Listener() {
        private static final String TAG = "SipAudioCall.Listener";

        @Override
        public void onReadyToCall(SipAudioCall call) {
            super.onReadyToCall(call);
            Log.d(TAG, "onReadyToCall: AudioCallListener");
            pushToLog("onReadyToCall: AudioCallListener");
            toggleCallButtonState(call);
            updateinCallLayout(call);
        }

        @Override
        public void onCalling(SipAudioCall call) {
            super.onCalling(call);
            Log.d(TAG, "onCalling: ");
            toggleCallButtonState(call);
            updateinCallLayout(call);
        }

        @Override
        public void onRinging(SipAudioCall call, SipProfile caller) {
            super.onRinging(call, caller);
            Log.d(TAG, "onRinging: ");
            toggleCallButtonState(call);
            updateinCallLayout(call);
        }

        @Override
        public void onRingingBack(SipAudioCall call) {
            super.onRingingBack(call);
            Log.d(TAG, "onRingingBack: ");
            toggleCallButtonState(call);
            updateinCallLayout(call);
        }

        @Override
        public void onCallEstablished(SipAudioCall call) {
            super.onCallEstablished(call);
            Log.d(TAG, "onCallEstablished: SipAudioCall with " + call.getPeerProfile().getAuthUserName());
            toggleCallButtonState(call);
            updateinCallLayout(call);
            call.startAudio();
            call.toggleMute();
        }

        @Override
        public void onCallEnded(SipAudioCall call) {
            super.onCallEnded(call);
            toggleCallButtonState(null);
            updateinCallLayout(null);
            Log.d(TAG, "onCallEnded: Call ended ");
            pushToLog("onCallEnded: Call ended ");
        }

        @Override
        public void onCallBusy(SipAudioCall call) {
            super.onCallBusy(call);
            Log.d(TAG, "onCallBusy: ");
            toggleCallButtonState(call);

        }

        @Override
        public void onCallHeld(SipAudioCall call) {
            super.onCallHeld(call);
            Log.d(TAG, "onCallHeld: ");
            toggleCallButtonState(call);

        }

        @Override
        public void onError(SipAudioCall call, int errorCode, String errorMessage) {
            super.onError(call, errorCode, errorMessage);
            Log.d(TAG, "onError: Call Error " + SipErrorCode.toString(errorCode));
            Log.d(TAG, "onError: " + errorMessage);
            pushToLog(errorMessage);
            SipUtils.endOngoingCall(call);
            toggleCallButtonState(null);
        }


        @Override
        public void onChanged(SipAudioCall call) {
            super.onChanged(call);
            Log.d(TAG, "onChanged: ");
            toggleCallButtonState(call);
        }
    };


    private IncomingCallReceiver callReceiver;
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
        clearLogBtn = findViewById(R.id.clear_log_btn);

        //incall layout elements
        logScrollView = findViewById(R.id.scroll_view);
        inCallLayout = findViewById(R.id.ongoing_call_layout);
        peerNameTv = findViewById(R.id.peer_name_tv);
        callStatusTv = findViewById(R.id.call_status_tv);
        toggleMute = findViewById(R.id.mute_toggle_button);
        toggleSpeaker = findViewById(R.id.speaker_toggle_btn);

        mainHandler = new Handler();
        toggleCallButtonState(null);

        IntentFilter filter = new IntentFilter();
        SipUtils.setIntentAction(intentAction);
        filter.addAction(intentAction);
        callReceiver = new IncomingCallReceiver(audioCallListener, this);
        this.registerReceiver(callReceiver, filter);

        sipManager = SipUtils.getSipManager(context);
        if (sipManager == null) {
            Toast.makeText(context, "Device does not support SIP Calling", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: Sip Manager null");
        } else {
            Toast.makeText(context, "Device Supports SIP Calling", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: Sip Manager Instantiated");
        }

        openProfileBtn.setOnClickListener(view -> {
            Log.d(TAG, "onClick: Register Button Clicked");
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    openProfile();
                }
            });
        });

        closeProfileBtn.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: Unregister Btn clicked");
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    closeProfile();
                }
            });
        });

        clearLogBtn.setOnClickListener(view -> {
            logtv.setText("");
        });

        toggleMute.setOnClickListener(view -> {
            if (incomingCall != null) {
                incomingCall.toggleMute();
            }
        });

        toggleSpeaker.setOnClickListener(view -> {
            if (incomingCall != null) {
                incomingCall.setSpeakerMode(toggleSpeaker.isChecked());
            }
        });
        callBtn.setOnClickListener(view -> {
            if (incomingCall != null) {
                endCall();
            } else {
                Log.d(TAG, "onClick: Calling " + peerUsername);
                pushToLog("Calling " + peerUsername);
                callTarget();
            }
            Log.d(TAG, "onClick: Call button state " + callBtn.isChecked());
        });

        isRegisteredBtn.setOnClickListener(view -> new Handler(Looper.getMainLooper()).post(isRegisteredRunnable));

    }


    private void getPermissions() {
        String[] permissions = {
                Manifest.permission.USE_SIP,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WAKE_LOCK
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
            else if (username.startsWith("6")) target = "6001";
        }
        targetUsername.setText(target);
        peerUsername = target;
    }

    private void callTarget() {

        if (incomingCall != null && incomingCall.isInCall()) return;

        Log.d(TAG, "callTarget: Inside Call Target");
        setTarget();
        SipUtils.makeAudioCall(currentuser, ("sip:" + peerUsername + "@" + domain), audioCallListener, 30);
        callBtn.setChecked(true);
        inCallLayout.setVisibility(View.VISIBLE);

    }

    private void endCall() {
        SipUtils.endOngoingCall(incomingCall);
        callBtn.setChecked(false);
    }

    private void openProfile() {
        username = myusername.getText().toString();
        password = mypassword.getText().toString();
        Log.d(TAG, "registerProfile: Trying to register " + username + ", " + password);
        setTarget();

        //Build local profile
        currentuser = SipUtils.buildLocalProfile(username, password, domain);

        if (currentuser != null) {
            Log.d(TAG, "registerProfile: Current User Profile Created ");
            Toast.makeText(context, "Your Local Profile is created", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(TAG, "registerProfile: Current User Profile Not Created");
            Toast.makeText(context, "Your Local Profile is Not created", Toast.LENGTH_SHORT).show();
        }

        //Open profile for calls
        SipUtils.openSipProfile(context, currentuser, sipRegistrationListener);

    }

    private void closeProfile() {
        boolean closed = SipUtils.closeSipProfile(currentuser);
        Log.d(TAG, "unregisterProfile: " + (closed ? "Sip Profile Closed Successfully" : "Sip Profile Failed to close"));
        pushToLog("unregisterProfile: " + (closed ? "Sip Profile Closed Successfully" : "Sip Profile Failed to close"));
    }

    private void toggleCallButtonState(SipAudioCall call) {
        Log.d(TAG, "toggleCallButtonState: Called " + call);
        runOnUiThread(() -> {
            incomingCall = call;
            if (incomingCall == null) callBtn.setChecked(false);
            else callBtn.setChecked(true);

            boolean flag = callBtn.isChecked();
            callBtn.setBackgroundColor(flag ? Color.RED : Color.GREEN);
            inCallLayout.setVisibility(flag ? View.VISIBLE : View.GONE);
            logScrollView.setVisibility(flag ? View.GONE : View.VISIBLE);
        });
    }

    private void updateinCallLayout(SipAudioCall call) {
        Log.d(TAG, "updateinCallLayout: ");
        if (call != null) {
            Log.d(TAG, "updateinCallLayout: Call is ongoing ");
            SipProfile peerProfile = call.getPeerProfile();
            if (peerProfile != null) {
                String name = peerProfile.getDisplayName();
                if (name == null || name.trim().length() == 0) name = peerProfile.getUriString();
                if (name == null || name.trim().length() == 0) name = "UNKNOWN";
                String finalName = name;
                runOnUiThread(() -> {
                    try {
                        peerNameTv.setText(finalName);
                        callStatusTv.setText(SipSession.State.toString(call.getState()));
                        Log.d(TAG, "updateinCallLayout: Mute Button " + toggleMute.isChecked());
                        Log.d(TAG, "updateinCallLayout: Mute Status " + call.isMuted());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } else {
            Log.d(TAG, "updateinCallLayout: No Call is in progress");
            runOnUiThread(() -> {
                try {
                    peerNameTv.setText("");
                    callStatusTv.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
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
        SipUtils.closeSipProfile(currentuser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeLocalProfile();
    }

    @Override
    public void logThis(String msg) {
        pushToLog(msg);
        Log.d("IncomingCallReceiver", "logThis: " + msg);
    }
}