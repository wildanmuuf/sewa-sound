package com.unpam.sewasound;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import org.w3c.dom.Text;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class DaftarPesananViewHolder extends RecyclerView.ViewHolder {

    public TextView txtSound, txtTglPenyewaan, txtTotal, txtUserTrx, txtStatus, txtKategori;
    public ImageView imageView;
    public Button btnCancel, btnFinish, btnCall;
    public DaftarPesananViewHolder(final View itemView) {
        super(itemView);
        txtSound = (TextView) itemView.findViewById(R.id.txt_sound);
        txtKategori = (TextView) itemView.findViewById(R.id.txt_kategori);
        txtTotal = (TextView) itemView.findViewById(R.id.txt_harga);
        txtTglPenyewaan = (TextView) itemView.findViewById(R.id.txt_tgl_penyewaan);
        txtUserTrx = (TextView) itemView.findViewById(R.id.txt_pelapak);
        imageView = (ImageView) itemView.findViewById(R.id.img);
        btnCancel = (Button) itemView.findViewById(R.id.btn_cancel);
        btnCall = (Button) itemView.findViewById(R.id.btn_call);
        btnFinish = (Button) itemView.findViewById(R.id.btn_finish);
        txtStatus = (TextView) itemView.findViewById(R.id.txt_status);
    }

    public void onBind(@NonNull ArrayList<TransaksiItem> transaksiItems, int position){
        checkStatus(Integer.parseInt(transaksiItems.get(position).getStatus()));
        txtSound.setText(transaksiItems.get(position).getSound());
        txtKategori.setText(transaksiItems.get(position).getKategori());
        txtTotal.setText(transaksiItems.get(position).getTotal());
        txtTglPenyewaan.setText(transaksiItems.get(position).getTanggalAwal()+" s/d \n" +transaksiItems.get(position).getTanggalAkhir());
        txtUserTrx.setText(transaksiItems.get(position).getUserTrx());
        imageView.setImageBitmap(transaksiItems.get(position).getSoundImage());
    }

    private void checkStatus(int status){
        if(status == StatusConstant.PROCESS){
            txtStatus.setText("Sedang Proses");
            btnFinish.setText("Terima");
            btnFinish.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#428bca")));
        }else if(status == StatusConstant.CANCEL){
            txtStatus.setText("Dibatalkan");
            btnFinish.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
            btnFinish.setEnabled(false);
            btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
            btnCancel.setEnabled(false);
        }else if(status == StatusConstant.FINISH){
            txtStatus.setText("Transaksi Selesai");
            btnFinish.setText("Selesai");
            btnFinish.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
            btnFinish.setEnabled(false);
            btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
            btnCancel.setEnabled(false);
        }else if(status == StatusConstant.ON_GOING){
            txtStatus.setText("Sedang disewa");
            btnFinish.setText("Selesai");
            btnFinish.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#428bca")));
            btnFinish.setEnabled(true);
            btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
            btnCancel.setEnabled(false);
        }else if(status == StatusConstant.RECIEVED){
            txtStatus.setText("Pesanan Diterima");
            btnFinish.setText("Mulai");
            btnFinish.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#428bca")));
            btnFinish.setEnabled(true);
            btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
            btnCancel.setEnabled(false);
        }
    }
}