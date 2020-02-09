package com.unpam.sewasound;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

import com.mapbox.mapboxsdk.geometry.LatLng;

import androidx.appcompat.view.ContextThemeWrapper;

public class LocationService {
    static LatLng currentLatlng;

    public static LatLng getSaveLatLng() {
        return saveLatLng;
    }

    public static void setSaveLatLng(LatLng saveLatLng) {
        LocationService.saveLatLng = saveLatLng;
    }

    static LatLng saveLatLng;
    static AlertDialog alert;
    public static boolean CheckLocationService(final Activity map){
        boolean enabled = false;
        if(map!=null) {
            LocationManager service = (LocationManager) map.getSystemService(map.LOCATION_SERVICE);
            enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!enabled) {
                alert = new AlertDialog.Builder( new ContextThemeWrapper(map, android.R.style.Theme_DeviceDefault_Dialog_Alert)).
                        setTitle("Lokasi GPS Dibutuhkan").
                        setMessage("Hidupkan Lokasi GPS Terlebih dahulu.").setCancelable(false).
                        setPositiveButton("BUKA PENGATURAN", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent openGpsSetting = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                map.startActivity(openGpsSetting);
                            }
                        }).setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        map.finish();
                    }
                }).create();
                alert.show();
                setCurrentLocation(new LatLng(0, 0));
            }
        }
        return enabled;
    }

    public static void setCurrentLocation(LatLng latLng){
        currentLatlng = new LatLng(latLng.getLatitude(), latLng.getLongitude());

    }
    public static LatLng getCurrentLocation(){
        return currentLatlng;
    }
}
