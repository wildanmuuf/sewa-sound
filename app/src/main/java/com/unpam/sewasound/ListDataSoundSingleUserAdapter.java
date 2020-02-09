package com.unpam.sewasound;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class ListDataSoundSingleUserAdapter  extends RecyclerView.Adapter<SoundSingleUserHolder>{
    private ArrayList<SoundBarangItem> mdataList;
    private Context context;
    private DatabaseReference dbRef;
    private String isFavorite;
    private String idFavorite ="";
    AlertDialog alert;
    public ListDataSoundSingleUserAdapter(Context context, @NonNull ArrayList<SoundBarangItem> dataList) {
        mdataList = dataList;
        this.context = context;
    }
    @Override
    public SoundSingleUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_sound_single_user, parent, false);
        return new SoundSingleUserHolder(view);
    }

    @UiThread
    @Override
    public void onBindViewHolder(SoundSingleUserHolder parentHolder, final int position) {
        if(position != RecyclerView.NO_POSITION){
            parentHolder.onBind(mdataList, position, context);

        }
        parentHolder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idPelapak = mdataList.get(position).getIdPelapak();
                String idSound = mdataList.get(position).getIdSoundBarang();
                Intent i = new Intent(context, ItemSoundSingle.class);
                i.putExtra("idPelapak", idPelapak);
                i.putExtra("idSound", idSound);
                context.startActivity(i);
            }
        });
        parentHolder.favoriteButton.setId(position);
        Favorite(position, parentHolder.favoriteButton, mdataList.get(position).getIdSoundBarang(), mdataList.get(position).getIdPelapak(), mdataList.get(position).getIdPenyewa(context));

    }

    @Override
    public int getItemCount() {
        return (mdataList == null) ? 0 : mdataList.size();
    }


    void setFilter(List<SoundBarangItem> filterList){
        mdataList = new ArrayList<>();
        mdataList.addAll(filterList);
        notifyDataSetChanged();
    }
    private void Favorite( int position, final MaterialFavoriteButton favoriteButton, String idSound, String idPelapak, String idPenyewa){

        final String combineId = idSound+"&&"+idPenyewa;
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("Favorite").orderByChild("combineId").equalTo(combineId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    idFavorite = ds.getKey();
                    Favorite favorite = ds.getValue(Favorite.class);
                    favoriteButton.setFavorite(Boolean.valueOf(favorite.getIsFavorite()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(favoriteButton.getId() == position){
            favoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                @Override
                public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                    if(favorite){
                        isFavorite = "true";
                    }else{
                        isFavorite = "false";
                    }
                    dbRef = FirebaseDatabase.getInstance().getReference();
                    Favorite Favorite = new Favorite(idSound, idPenyewa, idPelapak, isFavorite, combineId);

                    dbRef.child("Favorite").child(combineId).setValue(Favorite);
                    idFavorite = "";
                }
            });
        }


    }
}
