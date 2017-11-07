package com.example.vincent.whiteboardclient;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements FragmentCallback {
    private CanvasFragment canvasFragment;
    private Socket socketInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        canvasFragment.setCallback(this);
        ft.add(R.id.frame, canvasFragment);
        ft.commit();
    }

}
