package com.unpam.sewasound;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class SoundAdapter  extends RecyclerView.Adapter<SoundParentViewHolder> {
    private ArrayList<SoundBarangItem> mdataList;
    private Context context;
    AlertDialog alert;
    public SoundAdapter(Context context, @NonNull ArrayList<SoundBarangItem> dataList) {
        mdataList = dataList;
        this.context = context;
    }

    @Override
    public SoundParentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_sound, parent, false);
        return new SoundParentViewHolder(view);
    }


    @UiThread
    @Override
    public void onBindViewHolder(final SoundParentViewHolder parentHolder, final int position) {
        if(position != RecyclerView.NO_POSITION){
            parentHolder.onBind(mdataList, position);
            parentHolder.mArrowExpandImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    parentHolder.mArrowExpandImageView.animate().rotation(180).start();

                    PopupMenu popup = new PopupMenu(parentHolder.mArrowExpandImageView.getContext(), v);

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.detail_item:
                                    Intent i = new Intent(context, DetailSound.class);
                                    i.putExtra("idSound", mdataList.get(position).getIdSoundBarang());
                                    context.startActivity(i);
                                    return true;
                                case R.id.delete_item:
                                    alert = new AlertDialog.Builder(context).
                                            setTitle("Apakah anda yakin ingin menghapus ini ?").
                                            setMessage("Hapus sound ini akan menyebabkan data sound anda akan hilang.").setCancelable(false).
                                            setPositiveButton("HAPUS", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    removeSound(mdataList.get(position).getIdSoundBarang(), context, mdataList.get(position).getMerkSound());
                                                    mdataList.remove(position);
                                                    notifyDataSetChanged();
                                                }
                                            }).setNegativeButton("BATAL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            alert.dismiss();
                                        }
                                    }).create();
                                    alert.show();

                                    return true;
                                case R.id.update_item:
                                    Intent update = new Intent(context, UpdateSound.class);
                                    update.putExtra("idSound", mdataList.get(position).getIdSoundBarang());
                                    context.startActivity(update);
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    // here you can inflate your menu
                    popup.inflate(R.menu.kelola_sound_menu);
                    popup.setGravity(Gravity.RIGHT);

                    // if you want icon with menu items then write this try-catch block.
                    popup.show();
                    popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
                        @Override
                        public void onDismiss(PopupMenu menu) {
                            parentHolder.mArrowExpandImageView.animate().rotation(0).start();
                        }
                    });
                }
            });
        }

    }

    public void removeSound(final String id, final Context context, final String merk){

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Sound");
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference("images");
        try{
            dbRef.child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    storageReference.child(User.getKey()).child(id).listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ListResult> task) {
                            Object[] obj = new Object[task.getResult().getItems().size()];
                            task.getResult().getItems().toArray(obj);
                            for(int i = 0; i < obj.length; i++) {
                                StorageReference deleteStorage = (StorageReference)obj[i];
                                deleteStorage.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(context, "Sound Barang dengan merk: " + merk, Toast.LENGTH_LONG);
                                    }
                                });
                            }
                        }
                    });

                }
            });
        } catch (DatabaseException err){
            err.printStackTrace();
        }

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

}
