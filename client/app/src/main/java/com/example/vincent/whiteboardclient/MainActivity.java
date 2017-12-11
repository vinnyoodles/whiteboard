package com.example.vincent.whiteboardclient;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements FragmentCallback {
    private CanvasFragment canvasFragment;
    private Socket socketInstance;
    private String userName;
    private NetworkReceiver networkReceiver;
    private boolean receiverRegistered = false;
    private LocationHelper locationHelper;
    private AudioHelper audioHelper;

    // Screen dimensions
    private double width;
    private double height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{android.Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = (double) size.x;
        height = (double) size.y;
        register();
        locationHelper = new LocationHelper(this);
        locationHelper.getLocation();
        audioHelper = new AudioHelper();
        if (savedInstanceState != null) {
            userName = savedInstanceState.getString(Constants.USER_NAME_KEY);
        }

        // Show the room fragment to request a room name.
        if (userName == null) {
            FragmentManager fm = getFragmentManager();
            RoomFragment roomFragment = new RoomFragment();
            roomFragment.setCallback(this);
            fm.beginTransaction().add(R.id.frame, roomFragment).commit();
            return;
        }

        setupCanvasFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (canvasFragment != null)
            canvasFragment.saveBitmap();
        unregister();
        audioHelper.stopStream();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (canvasFragment != null)
            canvasFragment.saveBitmap();
        unregister();
        audioHelper.stopStream();
    }

    @Override
    protected void onResume() {
        super.onResume();
        register();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (userName != null)
            outState.putString(Constants.USER_NAME_KEY, userName);
    }

    public void emitLocation(String location) {
        if (location == null)
            return;

        JSONObject json = new JSONObject();
        try {
            json.put(Constants.LOCATION_KEY, location);

        } catch (org.json.JSONException e) {
            Log.e("json", e.getLocalizedMessage());
        }
        getSocketInstance().emit(Constants.LOCATION_EVENT, json);
    }

    public void enterRoom(String user) {
        userName = user;
        setupCanvasFragment();

        JSONObject json = new JSONObject();
        try {
            json.put(Constants.USER_NAME_KEY, userName);

        } catch (org.json.JSONException e) {
            Log.e("json", e.getLocalizedMessage());
        }
        getSocketInstance().emit(Constants.JOIN_ROOM_EVENT, json);
    }

    public void connected() {
        getSocketInstance().connect();
        if (canvasFragment != null)
            canvasFragment.addListeners();
    }

    public void disconnected() {
        getSocketInstance().disconnect();
        if (canvasFragment != null)
            canvasFragment.removeListeners();
    }

    public Socket getSocketInstance() {
        if (socketInstance == null) {
            try {
                socketInstance = IO.socket(Constants.SERVER_URL);
            } catch (URISyntaxException e) {
                Log.e("socket", e.getLocalizedMessage());
            }
        }
        return socketInstance;
    }

    public void onFragmentViewCreated() {
        canvasFragment.setRotation(getResources().getConfiguration().orientation);
    }

    private void register() {
        if (networkReceiver == null)
            networkReceiver = new NetworkReceiver(this);

        if (!receiverRegistered) {
            registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
            receiverRegistered = true;
        }
    }

    private void unregister() {
        if (networkReceiver != null && receiverRegistered) {
            unregisterReceiver(networkReceiver);
            receiverRegistered = false;
        }
    }

    private void setupCanvasFragment() {
        FragmentManager fm = getFragmentManager();
        canvasFragment = (CanvasFragment) fm.findFragmentByTag(Constants.RETAINED_FRAGMENT);

        if (canvasFragment == null) {
            canvasFragment = new CanvasFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble(Constants.WIDTH, width);
            bundle.putDouble(Constants.HEIGHT, height);
            canvasFragment.setArguments(bundle);
            canvasFragment.setCallback(this);
            fm.beginTransaction().add(R.id.frame, canvasFragment, Constants.RETAINED_FRAGMENT).commit();
        }

        audioHelper.startStream(getSocketInstance());
    }
}
