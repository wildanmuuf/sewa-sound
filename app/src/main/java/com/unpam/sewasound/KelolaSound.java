package com.unpam.sewasound;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;

public class KelolaSound extends Fragment implements MainActivity.OnBackPressedListner{

    private ArrayList<SoundBarangItem> soundBarangList;
    private SoundAdapter soundAdapter;
    private TextView emptyData;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private Bitmap soundImage;
    private StorageReference storageReference;
    private DatabaseReference dbRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_kelola_sound, container, false);
        setHasOptionsMenu(true);
        initView(rootView);

        loadRecycleView();
        RefreshLayout();
        getActivity().setTitle("Kelola Sound");

        return rootView;
    }

    public void loadRecycleView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        soundAdapter = new SoundAdapter(getContext(), soundBarangList);
        recyclerView.setAdapter(soundAdapter);
    }

    private void initView(View rootView){
        soundBarangList = new ArrayList<SoundBarangItem>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        emptyData = (TextView) rootView.findViewById(R.id.emptyText);
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_kelola_sound);
        storageReference = FirebaseStorage.getInstance().getReference("images");
        dbRef = FirebaseDatabase.getInstance().getReference("Sound");
    }

    public void RefreshLayout(){
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                soundBarangList.clear();
                if(soundBarangList.size() == 0){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReadSound();
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
        soundBarangList.clear();
    }

    @Override
    public void onResume(){
        super.onResume();
        refreshLayout.setRefreshing(true);
        loadRecycleView();
        if(soundBarangList.size() == 0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReadSound();
                    loadRecycleView();
                    refreshLayout.setRefreshing(false);

                }
            }, 2500);
        }else{
            soundBarangList.clear();
        }
    }

    public void ReadSound(){
        String idPelapak = User.getKey();
        dbRef.orderByChild("idPelapak").equalTo(idPelapak).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final long ONE_MEGABYTE = 1024 * 1024;
                if(dataSnapshot.exists()) {
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                        final SoundBarang soundBarang = dsp.getValue(SoundBarang.class);
                        final SoundBarangItem item = new SoundBarangItem();
                        String idSound = dsp.getKey();
                        item.setIdSoundBarang(idSound);
                        item.setIdPelapak(soundBarang.getIdPelapak());
                        item.setMerkSound(soundBarang.getMerkSound());
                        item.setHargaSound(soundBarang.getHargaSound());
                        String filename = dsp.getKey() + "-0";
                        storageReference.child(User.getKey()).child(dsp.getKey()).child(filename).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {

                                soundImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                Bitmap rectangular = Bitmap.createScaledBitmap(soundImage, 1000, 500, true);
                                soundBarang.setSoundImage(rectangular);
                                item.setSoundImage(rectangular);
                                soundAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                soundAdapter.notifyDataSetChanged();
                            }
                        });
                        soundBarangList.add(item);
                        soundAdapter.notifyDataSetChanged();
                    }

                }else{

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        inflater.inflate(R.menu.kelola_sound, menu);
        super.onCreateOptionsMenu(menu, inflater);
        searchView(menu);
    }

    private void searchView(Menu menu){
        MenuItem searchMenu = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchMenu.getActionView();
        searchView.setQueryHint("Cari dengan merk");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String nextText) {
                //Data akan berubah saat user menginputkan text/kata kunci pada SearchView
                nextText = nextText.toLowerCase();
                ArrayList<SoundBarangItem> soundBarangs = new ArrayList<>();
                for(SoundBarangItem data : soundBarangList){
                    String merk = data.getMerkSound().toLowerCase();
                    if(merk.contains(nextText)){
                        soundBarangs.add(data);
                    }
                }
                soundAdapter.setFilter(soundBarangs);
                return true;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

       if(id==R.id.add){
           startActivity(new Intent(getActivity().getApplicationContext(), TambahSound.class));
       }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        getActivity().getSupportFragmentManager().popBackStack();
        return false;
    }
}
