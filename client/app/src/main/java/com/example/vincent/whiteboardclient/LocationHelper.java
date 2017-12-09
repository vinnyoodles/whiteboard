package com.example.vincent.whiteboardclient;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PorterDuffXfermode;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by vincent on 12/9/17.
 */

public class LocationHelper {
    private LocationManager manager;
    private MainActivity mainActivity;

    public LocationHelper(MainActivity activity) {
        this.manager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        LocationListener listener = new MyLocationListener();
        mainActivity = activity;
        if (ContextCompat.checkSelfPermission(mainActivity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(mainActivity, permissions, 1);
        }
        this.manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, listener);
    }

    private class MyLocationListener implements LocationListener {
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Geocoder coder = new Geocoder(mainActivity.getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = coder.getFromLocation(latitude, longitude, 1);
                for (Address a : addresses)
                    mainActivity.emitLocation(a.getLocality());
            } catch (IOException e){
                Log.d("IOException", "failed to get location");
            }
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }
}
