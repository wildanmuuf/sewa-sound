package com.unpam.sewasound;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;


public class SoundParentViewHolder extends RecyclerView.ViewHolder {

    public TextView txtMerk, txtHarga, txtKategori;
    public ImageView imageView;
    public ImageView mArrowExpandImageView;
    public SoundParentViewHolder(final View itemView) {
        super(itemView);
        txtMerk = (TextView) itemView.findViewById(R.id.txt_merk);
        txtHarga = (TextView) itemView.findViewById(R.id.txt_harga);
        txtKategori = (TextView) itemView.findViewById(R.id.txt_kategori);
        imageView = (ImageView) itemView.findViewById(R.id.img);
        mArrowExpandImageView = (ImageView)itemView.findViewById(R.id.expand_icon);

    }

    public void onBind(@NonNull ArrayList<SoundBarangItem> soundBarangItem, int position){
        txtMerk.setText(soundBarangItem.get(position).getMerkSound());
        txtHarga.setText(soundBarangItem.get(position).getHargaSound());
        txtKategori.setText(soundBarangItem.get(position).getKategori());
        imageView.setImageBitmap(soundBarangItem.get(position).getSoundImage());
    }
}