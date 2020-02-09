package com.unpam.sewasound;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.WindowManager;

public class NetworkService {
    public ProgressDialog progressDialog;
    AlertDialog alert;
    private boolean checkConnectionAvailable(Context context){
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        else
            connected = false;
        return connected;
    }

    public void CheckConnection(final Activity context){
        progressDialog = new ProgressDialog(context, R.style.Theme_AppCompat_DayNight_Dialog);
        progressDialog.setMessage("Mengunduh data...");
        progressDialog.show();
        context.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,  WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        if(progressDialog.isShowing()){
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            if(!checkConnectionAvailable(context)){
                progressDialog.dismiss();
                alert = new AlertDialog.Builder(context).
                        setTitle("Koneksi internet dibutuhkan").
                        setMessage("Hidupkan koneksi internet terlebih dahulu.").setCancelable(false).
                        setPositiveButton("COBA LAGI?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CheckConnection(context);
                            }
                        }).setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.finish();
                    }
                }).create();
                alert.show();
            }
        }
    }

}
