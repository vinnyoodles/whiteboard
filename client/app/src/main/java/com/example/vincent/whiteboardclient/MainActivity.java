package com.example.vincent.whiteboardclient;

import android.app.FragmentManager;
import android.graphics.Point;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements FragmentCallback {
    private CanvasFragment canvasFragment;
    private Socket socketInstance;
    private boolean isConnected;

    // Screen dimensions
    private double width;
    private double height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = (double) size.x;
        height = (double) size.y;

        setupFragments();
        if (!isConnected) {
            getSocketInstance().connect();
            canvasFragment.addListeners();
            isConnected = true;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnected) {
            getSocketInstance().disconnect();
            canvasFragment.removeListeners();
            isConnected = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isConnected) {
            getSocketInstance().connect();
            canvasFragment.addListeners();
            isConnected = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isConnected) {
            getSocketInstance().disconnect();
            canvasFragment.removeListeners();
            isConnected = false;
        }
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

    private void setupFragments() {
        FragmentManager fm = getFragmentManager();
        canvasFragment = (CanvasFragment) fm.findFragmentByTag(Constants.RETAINED_FRAGMENT);

        if (canvasFragment == null) {
            Log.d("debug", "show fragment");
            canvasFragment = new CanvasFragment();
            Bundle bundle = new Bundle();
            bundle.putDouble(Constants.WIDTH, width);
            bundle.putDouble(Constants.HEIGHT, height);
            canvasFragment.setArguments(bundle);
            canvasFragment.setCallback(this);
            fm.beginTransaction().add(R.id.frame, canvasFragment, Constants.RETAINED_FRAGMENT).commit();
        }
    }

}
