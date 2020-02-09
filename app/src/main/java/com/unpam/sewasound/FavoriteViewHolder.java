package com.unpam.sewasound;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


public class FavoriteViewHolder extends RecyclerView.ViewHolder {
    private TextView txtMerk, txtHarga, txtKategori;
    public MaterialFavoriteButton favoriteButton;
    private ImageView imageView;
    public CardView item;


    public FavoriteViewHolder (View itemView){
        super(itemView);
        item = (CardView) itemView.findViewById(R.id.item_sound_single);
        txtMerk = (TextView) itemView.findViewById(R.id.txt_merk);
        txtHarga = (TextView) itemView.findViewById(R.id.txt_harga);
        txtKategori = (TextView) itemView.findViewById(R.id.txt_kategori);
        imageView = (ImageView) itemView.findViewById(R.id.img);
        favoriteButton = (MaterialFavoriteButton) itemView.findViewById(R.id.favorite);
    }

    public void onBind(@NonNull ArrayList<SoundBarangItem> soundBarangItem, int position, Context ctx){

        txtMerk.setText(soundBarangItem.get(position).getMerkSound());
        txtHarga.setText(soundBarangItem.get(position).getHargaSound());
        txtKategori.setText(soundBarangItem.get(position).getKategori());
        imageView.setImageBitmap(soundBarangItem.get(position).getSoundImage());
    }

}
