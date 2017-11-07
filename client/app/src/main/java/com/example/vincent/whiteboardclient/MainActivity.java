package com.example.vincent.whiteboardclient;

import android.graphics.Point;
import android.support.v4.app.FragmentTransaction;
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
        getSocketInstance().connect();
        canvasFragment.addListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSocketInstance().disconnect();
        canvasFragment.removeListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSocketInstance().connect();
        canvasFragment.addListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getSocketInstance().disconnect();
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

    private void setupFragments() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        canvasFragment = new CanvasFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble(Constants.WIDTH, width);
        bundle.putDouble(Constants.HEIGHT, height);
        canvasFragment.setArguments(bundle);
        canvasFragment.setCallback(this);
        ft.add(R.id.frame, canvasFragment);
        ft.commit();
    }

}
