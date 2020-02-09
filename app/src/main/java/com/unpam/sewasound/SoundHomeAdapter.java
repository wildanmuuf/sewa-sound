package com.unpam.sewasound;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class SoundHomeAdapter  extends RecyclerView.Adapter<SoundHomeAdapter.SoundHomeViewHolder>{
    private ArrayList<User> dataList;
    private Context context;
    public SoundHomeAdapter(Context context, ArrayList<User> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public SoundHomeAdapter.SoundHomeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_user, parent, false);
        return new SoundHomeAdapter.SoundHomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SoundHomeAdapter.SoundHomeViewHolder holder, final int position) {
        holder.txtNama.setText(dataList.get(position).getName());
        holder.txtAlamat.setText(dataList.get(position).getAddress());
        holder.txtDistance.setText(dataList.get(position).getDistance());
        holder.imageView.setImageBitmap(dataList.get(position).getPhotoProfile());
        holder.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = context.getSharedPreferences("email-password", MODE_PRIVATE);
                String idPenyewa = sharedPreferences.getString("idUser",null);
                SharedPreferences sharedHakAkses = context.getSharedPreferences("hak-akses", MODE_PRIVATE);
                String hakAkses = sharedHakAkses.getString("hak-akses",null);
                if(idPenyewa == null){
                    Snackbar.make(v,"Silahkan masuk terlebih dahulu", Snackbar.LENGTH_LONG );
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Fragment login = new LoginActivity();
                            FragmentManager fmManager = ((MainActivity)context).getSupportFragmentManager();
                            fmManager.beginTransaction()
                                    .replace(R.id.frame_container, login)
                                    .commit();
                        }
                    }, 2500);

                }else{
                    String userId = dataList.get(position).getIdUser();
                    String nama = dataList.get(position).getName();
                    Intent i = new Intent(context, ListTabSingleUser.class);
                    i.putExtra("nama",nama);
                    i.putExtra("userId", userId);
                    context.startActivity(i);
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return (dataList == null) ? 0 : dataList.size();
    }

    public class SoundHomeViewHolder extends RecyclerView.ViewHolder{
        private TextView txtNama, txtAlamat, txtDistance;
        private ImageView imageView;
        private CardView item;
        public SoundHomeViewHolder(View itemView) {
            super(itemView);
            item = (CardView) itemView.findViewById(R.id.item_user);
            txtNama = (TextView) itemView.findViewById(R.id.txt_user);
            txtAlamat = (TextView) itemView.findViewById(R.id.txt_alamat);
            txtDistance = (TextView) itemView.findViewById(R.id.txt_distance);
            imageView = (ImageView) itemView.findViewById(R.id.img);
        }
    }

    void setFilter(ArrayList<User> filterList){
        dataList = new ArrayList<>();
        dataList.addAll(filterList);
        notifyDataSetChanged();
    }

    void setSorting(){
        notifyDataSetChanged();
    }
}
