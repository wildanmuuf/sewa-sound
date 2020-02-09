package com.unpam.sewasound;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class PesananSaya extends Fragment {
    private ArrayList<TransaksiItem> transaksiItems;
    private PesananSayaAdapter pesananSayaAdapter;
    private TextView emptyData;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private Bitmap soundImage;
    private StorageReference storageReference;
    private DatabaseReference dbRef;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_pesanan_saya, container, false);
        setHasOptionsMenu(true);
        initView(rootView);

        loadRecycleView();
        RefreshLayout();
        getActivity().setTitle("Pesanan Saya");

        return rootView;
    }

    public void loadRecycleView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        pesananSayaAdapter = new PesananSayaAdapter(getContext(), transaksiItems);
        recyclerView.setAdapter(pesananSayaAdapter);
    }

    private void initView(View rootView){
        transaksiItems = new ArrayList<TransaksiItem>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_pesanan_view);
        emptyData = (TextView) rootView.findViewById(R.id.emptyText);
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.pesanan_view_swipe_refresh);
        storageReference = FirebaseStorage.getInstance().getReference("images");
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void RefreshLayout(){
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(transaksiItems.size()!=0){
                    transaksiItems.clear();
                }
                if(transaksiItems.isEmpty()){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReadTransaksi();
                            loadRecycleView();
                            refreshLayout.setRefreshing(false);
                        }
                    }, 2500);

                }
            }
        });
    }


    @Override
    public void onPause(){
        super.onPause();
        transaksiItems.clear();
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout.setRefreshing(true);
        loadRecycleView();
        if(transaksiItems.size() == 0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReadTransaksi();
                    loadRecycleView();
                    refreshLayout.setRefreshing(false);

                }
            }, 2500);
        }
    }

    public void ReadTransaksi(){
        String idPenyewa = User.getKey();

        dbRef.child("Penyewaan").orderByChild("idPenyewa").equalTo(idPenyewa).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final long ONE_MEGABYTE = 1024 * 1024;

                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                        final Penyewaan penyewaan = dsp.getValue(Penyewaan.class);
                        String idSound = penyewaan.getIdSound();
                        String idPelapak = penyewaan.getIdPelapak();
                        final TransaksiItem item = new TransaksiItem();
                        String idPenyewaan = dsp.getKey();
                        item.setIdPenyewaan(idPenyewaan);
                        item.setTanggalAwal(penyewaan.getTanggalAwal());
                        item.setTanggalAkhir(penyewaan.getTanggalAkhir());
                        item.setAlamatTarget(penyewaan.getAlamatTarget());
                        item.setStatus(penyewaan.getStatus());
                        item.setTotal(penyewaan.getTotal());

                        String filename =idSound + "-0";

                        dbRef = FirebaseDatabase.getInstance().getReference("Sound");
                        dbRef.orderByKey().equalTo(idSound).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot DsSound : dataSnapshot.getChildren()){
                                    String kategori = DsSound.child("kategori").getValue(String.class);
                                    String sound  = DsSound.child("merkSound").getValue(String.class);
                                    item.setSound(sound);
                                    item.setKategori(kategori);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        dbRef = FirebaseDatabase.getInstance().getReference("Users");
                        dbRef.orderByKey().equalTo(idPelapak).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot dsUser : dataSnapshot.getChildren()){
                                    String nama  = dsUser.child("name").getValue(String.class);
                                    String noTelp = dsUser.child("noTelp").getValue(String.class);
                                    item.setUserTrx(nama);
                                    item.setNoTelp(noTelp);
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        storageReference.child(idPelapak).child(idSound).child(filename).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {

                                soundImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Bitmap rectangular = Bitmap.createScaledBitmap(soundImage, 1000, 500, true);
                                item.setSoundImage(rectangular);
                                pesananSayaAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pesananSayaAdapter.notifyDataSetChanged();
                            }
                        });
                        transaksiItems.add(item);
                        pesananSayaAdapter.notifyDataSetChanged();
                    }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
