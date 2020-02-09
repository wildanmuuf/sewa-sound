package com.unpam.sewasound;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import static android.content.Context.LOCATION_SERVICE;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;


public class Root extends Fragment implements LocationListener{

    public Root() {
    }

    View rootView;
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton btnSortTerdekat;
    private ArrayList<User> userList;
    private ArrayList<Alamat> alamatList;
    private SoundHomeAdapter homeAdapter;
    private TextView emptyData;
    private LatLng myLocation;
    private ViewFlipper homeFlipper;
    MaterialBetterSpinner kateogriWatt;
    private String Merk, Harga, Kategori;
    private Bitmap userImage;
    private StorageReference storageReference;
    private DatabaseReference dbRef;
    private ArrayList<Bitmap> bms;
    protected LocationManager locationManager;
    long LOCATION_REFRESH_TIME = 1000;
    float LOCATION_REFRESH_DISTANCE = 0;
    private SwipeRefreshLayout refreshLayout;
    LocationListener mLocationListener = null;
    SharedPreferences.Editor editor;
    double myLongituded = 0;
    double myLatitude = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        locationManager();
        rootView = inflater.inflate(R.layout.list_view_root, container, false);
        Mapbox.getInstance(getContext(), getString(R.string.API_KEY));
        SharedPreferences locationPref = getActivity().getSharedPreferences("location", Context.MODE_PRIVATE);
        editor = locationPref.edit();
        setHasOptionsMenu(true);
        initView(rootView);
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        //locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        RefreshLayout();
        loadDataToRecycler();
        Sort();
        getActivity().setTitle("Penyewaan Sound");
        return rootView;
    }


    private void locationManager() {
        try {
            if(getContext() !=null) {
                locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
                if (myLocation == null) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
                }
            }
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onLocationChanged(Location location) {
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        double latitude = myLocation.getLatitude();
        double longitude = myLocation.getLongitude();
        editor.putString("locLatitude", String.valueOf(latitude));
        editor.putString("locLongitude", String.valueOf(longitude));
        editor.apply();
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    public void loadDataToRecycler(){

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        homeAdapter = new SoundHomeAdapter(getContext(),userList);
        recyclerView.setAdapter(homeAdapter);

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        searchView(menu);
    }


    private void initView(View rootView){
        userList = new ArrayList<User>();
        alamatList = new ArrayList<Alamat>();
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_root);
        btnSortTerdekat = (ExtendedFloatingActionButton) rootView.findViewById(R.id.sort);
        emptyData = (TextView) rootView.findViewById(R.id.emptyText);
        refreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.root_swipe_refresh);
        storageReference = FirebaseStorage.getInstance().getReference("photo");
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public void ReadData(){
        if(LocationService.CheckLocationService(getActivity())){
            SharedPreferences getSharedLocation = getContext().getSharedPreferences("location", Context.MODE_PRIVATE);
            String strLongitude = getSharedLocation.getString("locLongitude", null);
            String strLatitude = getSharedLocation.getString("locLatitude", null);
            double longitude = Double.parseDouble(strLongitude);
            double latitude = Double.parseDouble(strLatitude);
            myLocation = new LatLng(latitude, longitude);

            dbRef.child("Users").orderByChild("hakAkses").equalTo("Pelapak").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    final long ONE_MEGABYTE = 1024 * 1024;
                    if(dataSnapshot.exists()) {
                        for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                            final User users = dsp.getValue(User.class);
                            String id = dsp.getKey();
                            users.setIdUser(id);
                            dbRef.child("Alamat").orderByKey().equalTo(dsp.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot alamatData) {
                                    if(alamatData.exists()) {
                                        for (DataSnapshot var : alamatData.getChildren()) {
                                            final Alamat alamat = var.getValue(Alamat.class);
                                            String myAlamat = alamat.getAddress();
                                            users.setAddress(myAlamat);
                                            double lat = alamat.getLat();
                                            double lot = alamat.getLot();
                                            LatLng latLng = new LatLng(lat, lot);
                                            locationManager();
                                            double distance = myLocation.distanceTo(latLng);
                                            DecimalFormat newFormat = new DecimalFormat("##.00");
                                            String satuan = " M";
                                            if (distance >= 1000) {
                                                distance = distance / 1000;
                                                satuan = " KM";
                                            }
                                            //double newDistance =Double.parseDouble(newFormat.format(distance));
                                            users.setDistance(newFormat.format(distance) + satuan);

                                            homeAdapter.notifyDataSetChanged();
                                        }
                                    }else{
                                        users.setAddress("Pelapak belum memperbaharui alamat");
                                        users.setDistance("0");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            storageReference.child(id).getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {

                                    userImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    Bitmap rectangular = Bitmap.createScaledBitmap(userImage, 1000, 500, true);
                                    users.setPhotoProfile(rectangular);
                                    homeAdapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    homeAdapter.notifyDataSetChanged();
                                }
                            });
                            userList.add(users);
                            homeAdapter.notifyDataSetChanged();


                        }



                    }else{

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void Sort() {
        btnSortTerdekat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Collections.sort(userList, new Comparator<User>() {
                    @Override
                    public int compare(User o1, User o2) {
                        double distance1 = Double.parseDouble(removeLastCharacter(o1.getDistance()));
                        double distance2 = Double.parseDouble(removeLastCharacter(o2.getDistance()));
                        if(!o1.getDistance().contains("KM") ){
                            distance1 = distance1/1000;
                        }
                        if(!o2.getDistance().contains("KM")){
                            distance2 = distance2/1000;
                        }
                        return distance1 < distance2 ? -1 : (distance1 > distance2) ? 1 : 0;
                    }
                });
                homeAdapter = new SoundHomeAdapter(getContext(), userList);
                recyclerView.setAdapter(homeAdapter);
            }
        });
    }

    private String removeLastCharacter(String str) {
        String result = "0";
        if ((!str.equals("0")) && (str.length() > 0)) {
            result = str.substring(0, str.length() - 3);
        }
        if(result.contains(",")){
            result = result.replace(",",".");
        }
        return result;
    }

    private void searchView(Menu menu){
        MenuItem searchMenu = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchMenu.getActionView();
        searchView.setQueryHint("Cari dengan Nama Pelapak");
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
                ArrayList<User> user = new ArrayList<>();
                for(User data : userList){
                    String nama = data.getName().toLowerCase();
                    if(nama.contains(nextText)){
                        user.add(data);
                    }
                }
                homeAdapter.setFilter(user);
                return true;
            }
        });

    }

    public void RefreshLayout(){
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                btnSortTerdekat.setVisibility(View.INVISIBLE);
                userList.clear();
                if(userList.size() == 0){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ReadData();
                            loadDataToRecycler();
                            btnSortTerdekat.setVisibility(View.VISIBLE);
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
        //myLocation = new LatLng(0,0);
        locationManager();
        userList.clear();
    }
    @Override
    public void onResume(){
        super.onResume();
        //myLocation = new LatLng(0,0);
        locationManager();
        refreshLayout.setRefreshing(true);
        btnSortTerdekat.setVisibility(View.INVISIBLE);
        loadDataToRecycler();
        if(userList.size() == 0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //checkPermission();
                    refreshLayout.setRefreshing(false);
                    btnSortTerdekat.setVisibility(View.VISIBLE);
                    loadDataToRecycler();
                    ReadData();
                }
            }, 2500);
        }else{
            userList.clear();
        }
    }


}