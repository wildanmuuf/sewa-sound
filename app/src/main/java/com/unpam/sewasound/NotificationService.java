package com.unpam.sewasound;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NotificationService extends Service {
    private BroadcastReceiver broadcastReceiver;
    private Penyewaan penyewaan;
    DatabaseReference dbRef;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase("penyewaan")) {
                    String id_user = intent.getStringExtra("idUser");
                    dbRef = FirebaseDatabase.getInstance().getReference("Penyewaan");
                    dbRef.orderByChild("idPelapak").equalTo(id_user).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot itemSnapshot) {
                            for (DataSnapshot item : itemSnapshot.getChildren()) {
                                penyewaan = item.getValue(Penyewaan.class);
                                showNotification("Anda mendapatkan pesanan baru!", "Pesanan untuk tanggal : " + penyewaan.getTanggalAwal() + " s/d " + penyewaan.getTanggalAkhir());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        // set the custom action
        intentFilter.addAction("penyewaan"); //Action is just a string used to identify the receiver as there can be many in your app so it helps deciding which receiver should receive the intent.
        // register the receiver
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void showNotification(String title, String body){
        NotificationUtils notificationUtils = new NotificationUtils(this);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Notification.Builder builder =
                    notificationUtils.getAndroidChannelNotification(title, body);
            notificationUtils.getManager().notify(101, builder.build());
        }
    }
}
