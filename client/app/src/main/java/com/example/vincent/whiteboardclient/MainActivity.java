package com.example.vincent.whiteboardclient;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements FragmentCallback {
    private CanvasFragment canvasFragment;
    private RoomFragment roomFragment;
    private Socket socketInstance;
    private String userName;
    private NetworkReceiver networkReceiver;
    private boolean receiverRegistered = false;
    private AudioHelper audioHelper;
    private BackgroundServices bServices;

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
        bServices = new BackgroundServices(this);
        new LocationHelper(this).getLocation();
        audioHelper = new AudioHelper();
        if (savedInstanceState != null) {
            userName = savedInstanceState.getString(Constants.USER_NAME_KEY);
        }

        // Show the room fragment to request a room name.
        if (userName == null)
            setupRoomFragment();
        else
            setupCanvasFragment();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (canvasFragment != null)
            canvasFragment.saveBitmap();
        unregister();
        audioHelper.stopStream();
        getSocketInstance().disconnect();
        bServices.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (canvasFragment != null)
            canvasFragment.saveBitmap();
        unregister();
        audioHelper.stopStream();
        bServices.onStop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        bServices.onStop();
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
        getSocketInstance().emit(Constants.LOCATION_EVENT, location);
    }

    public void enterRoom(String user) {
        userName = user;
        FragmentManager manager = getFragmentManager();
        Fragment room = manager.findFragmentByTag(Constants.RETAINED_ROOM_FRAGMENT);
        if (room != null) {
            manager.beginTransaction().remove(room).commit();
        }
        setupCanvasFragment();

        getSocketInstance().emit(Constants.JOIN_ROOM_EVENT, user);
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
        canvasFragment = (CanvasFragment) fm.findFragmentByTag(Constants.RETAINED_CANVAS_FRAGMENT);

        if (canvasFragment == null) {
            canvasFragment = new CanvasFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble(Constants.WIDTH, width);
            bundle.putDouble(Constants.HEIGHT, height);
            canvasFragment.setArguments(bundle);
            fm.beginTransaction().add(R.id.frame, canvasFragment, Constants.RETAINED_CANVAS_FRAGMENT).commit();
        }
        canvasFragment.setCallback(this);
        audioHelper.startStream(getSocketInstance());
        bServices.startLocationFetch();
    }

    private void setupRoomFragment() {
        FragmentManager fm = getFragmentManager();
        roomFragment = (RoomFragment) fm.findFragmentByTag(Constants.RETAINED_ROOM_FRAGMENT);

        if (roomFragment == null) {
            roomFragment = new RoomFragment();
            fm.beginTransaction().add(R.id.frame, roomFragment, Constants.RETAINED_ROOM_FRAGMENT).commit();
        }
        roomFragment.setCallback(this);
    }
}
