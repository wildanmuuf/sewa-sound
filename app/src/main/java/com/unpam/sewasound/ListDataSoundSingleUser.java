package com.unpam.sewasound;


import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.zip.Inflater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class ListDataSoundSingleUser extends Fragment {
    private String idUser;
    private ArrayList<SoundBarangItem> soundBarangList;
    private Spinner kategoriSpinner;
    private ListDataSoundSingleUserAdapter soundAdapter;
    private TextView emptyData;
    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private String namaPelapak;
    private Bitmap soundImage;
    private StorageReference storageReference;
    private DatabaseReference dbRef;
    private String[] listKategori;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_list_data_sound_single_user, container, false);
        setHasOptionsMenu(true);
        idUser = getActivity().getIntent().getStringExtra("userId");
        namaPelapak = getActivity().getIntent().getStringExtra("nama");
        getActivity().setTitle("List Sound "+namaPelapak);
        initView(rootView);
        loadRecycleView();
        RefreshLayout();
        FilterByKategori();
        return rootView;
    }

    public void loadRecycleView(){
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        soundAdapter = new ListDataSoundSingleUserAdapter(getContext(), soundBarangList);
        recyclerView.setAdapter(soundAdapter);
    }

    private void initView(View rootView){
        kategoriSpinner = (Spinner) rootView.findViewById(R.id.filter_by_kategori_cmb);
        listKategori = getActivity().getApplicationContext().getResources().getStringArray(R.array.kategori2);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line, listKategori);
        kategoriSpinner.setAdapter(arrayAdapter);
        soundBarangList = new ArrayList<SoundBarangItem>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_single_user);
        emptyData = (TextView) rootView.findViewById(R.id.emptyText);
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_kelola_sound);
        storageReference = FirebaseStorage.getInstance().getReference("images");
        dbRef = FirebaseDatabase.getInstance().getReference("Sound");
    }

    private void FilterByKategori(){
        kategoriSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String kategoriFilter = "";
                int pos = position;
                if(position == 1){
                    kategoriFilter = "2000 Watt";
                }else if(position == 2){
                    kategoriFilter = "5000 Watt";
                }else if(position== 3){
                    kategoriFilter = "10000 Watt";
                }
                ArrayList<SoundBarangItem> soundBarangs = new ArrayList<>();
                for(SoundBarangItem data : soundBarangList){
                    String kategori = data.getKategori();
                    if(kategori.contains(kategoriFilter)){
                        soundBarangs.add(data);
                    }
                }
                RefreshLayout();
                soundAdapter.setFilter(soundBarangs);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void ReadSound(){
        dbRef.orderByChild("idPelapak").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final long ONE_MEGABYTE = 1024 * 1024;
                if(dataSnapshot.exists()) {
                    emptyData.setVisibility(View.GONE);
                    for (DataSnapshot dsp : dataSnapshot.getChildren()) {

                        final SoundBarang soundBarang = dsp.getValue(SoundBarang.class);
                        final SoundBarangItem item = new SoundBarangItem();
                        String idSound = dsp.getKey();
                        item.setIdSoundBarang(idSound);
                        item.setIdPelapak(soundBarang.getIdPelapak());
                        item.setMerkSound(soundBarang.getMerkSound());
                        item.setHargaSound(soundBarang.getHargaSound());
                        item.setKategori(soundBarang.getKategori());
                        String filename = dsp.getKey() + "-0";
                        storageReference.child(idUser).child(dsp.getKey()).child(filename).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
                    emptyData.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
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
}
