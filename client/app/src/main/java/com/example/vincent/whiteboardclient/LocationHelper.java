package com.example.vincent.whiteboardclient;

import android.location.Address;
import android.location.Location;
import android.util.Log;

import java.util.List;

import io.nlopez.smartlocation.*;

/**
 * Created by vincent on 12/9/17.
 */

public class LocationHelper implements OnLocationUpdatedListener, OnReverseGeocodingListener {
    private MainActivity mainActivity;
    private SmartLocation smLocation;

    public LocationHelper(MainActivity activity) {
        mainActivity = activity;
        smLocation = SmartLocation.with(activity.getApplicationContext());
    }

    public void getLocation() {
        smLocation.location().oneFix().start(this);
    }

    @Override
    public void onLocationUpdated(Location location) {
        smLocation.geocoding().reverse(location, this);
    }

    @Override
    public void onAddressResolved(Location original, List<Address> results) {
        if (results.size() == 0)
            return;

        Address address = results.get(0);
        String result = address.getFeatureName() + ", " + address.getLocality() + ", " + address.getAdminArea();
        mainActivity.emitLocation(result);
    }
}
