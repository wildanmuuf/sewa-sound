package com.unpam.sewasound;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView;

public class DaftarPesananAdapter  extends RecyclerView.Adapter<DaftarPesananViewHolder> {
    private ArrayList<TransaksiItem> mdataList;
    private Context context;
    AlertDialog alert;
    public DaftarPesananAdapter(Context context, @NonNull ArrayList<TransaksiItem> dataList) {
        mdataList = dataList;
        this.context = context;
    }

    @Override
    public DaftarPesananViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_pesanan_saya, parent, false);
        return new DaftarPesananViewHolder(view);
    }


    @UiThread
    @Override
    public void onBindViewHolder(final DaftarPesananViewHolder parentHolder, final int position) {
        if(position != RecyclerView.NO_POSITION){
            parentHolder.onBind(mdataList, position);
            parentHolder.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelTrx(mdataList.get(position).getIdPenyewaan(), context, position, StatusConstant.CANCEL, "Penyewaan dibatalkan");
                }
            });
            parentHolder.btnFinish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(parentHolder.btnFinish.getText().equals("Terima")){
                        cancelTrx(mdataList.get(position).getIdPenyewaan(), context, position, StatusConstant.RECIEVED, "Penyewaan Diterima");
                    }else if(parentHolder.btnFinish.getText().equals("Selesai")){
                        cancelTrx(mdataList.get(position).getIdPenyewaan(), context, position, StatusConstant.FINISH,"Penyewaan Selesai");
                    }else if(parentHolder.btnFinish.getText().equals("Mulai")){
                        cancelTrx(mdataList.get(position).getIdPenyewaan(), context, position, StatusConstant.ON_GOING, "Penyewaan dimulai");
                    }
                }
            });
            parentHolder.btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String getTelp = mdataList.get(position).getNoTelp();
                    context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", getTelp, null)));
                }
            });
        }

    }

    public void cancelTrx (final String id, final Context context, int position, final int status, String txtStatus){
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Penyewaan");
        dbRef.child(id).child("status").setValue(String.valueOf(status)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(task.isSuccessful()){
                            mdataList.get(position).setStatus(String.valueOf(status));
                            notifyItemChanged(position);
                            Toast.makeText(context, txtStatus, Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(context, "error : "+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 2500);
            }
        });
    }



    @Override
    public int getItemCount() {
        return (mdataList == null) ? 0 : mdataList.size();
    }


    void setFilter(List<TransaksiItem> filterList){
        mdataList = new ArrayList<>();
        mdataList.addAll(filterList);
        notifyDataSetChanged();
    }

}
