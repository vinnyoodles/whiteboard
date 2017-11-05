package com.example.vincent.whiteboardclient;

import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements FragmentCallback {
    private CanvasFragment canvasFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupFragments();
    }

    private void setupFragments() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        canvasFragment = new CanvasFragment();
        canvasFragment.setCallback(this);
        ft.add(R.id.frame, canvasFragment);
        ft.commit();
    }
}
