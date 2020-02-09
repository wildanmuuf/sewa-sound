package com.unpam.sewasound;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.Map;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class ItemSoundSingle extends AppCompatActivity implements OnMapReadyCallback {
    private MapView mapView;
    private MaterialFavoriteButton favoriteButton;
    private TextView  merkSound, hargaSound, kategoriSound, descSound;
    private EditText alamatText;
    private Button buttonSewa, buttonOpenGMaps;
    private CarouselView detailCarousel;
    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private MapboxMap mapboxMap;
    private Marker marker;

    private ArrayList<Bitmap> arrImage;
    private String idSound;
    private String idPelapak;
    private String idPenyewa;
    private String idFavorite ="";
    private String isFavorite;
    //constant
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private GeoJsonSource geoJsonSource;
    private String hakAkses;

    private double UserLongitude, UserLatitude;
    public LatLng UserLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this, getString(R.string.API_KEY));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_sound_single);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        idPelapak = getIntent().getStringExtra("idPelapak");
        idSound = getIntent().getStringExtra("idSound");
        SharedPreferences sharedPreferences = getSharedPreferences("email-password", MODE_PRIVATE);
        idPenyewa = sharedPreferences.getString("idUser",null);
        initView();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        ReadData();
        ReadAlamat();
        openGMaps();
        Favorite();
        SewaSound();
        HideButtonSewa(hakAkses);
    }
    private void openGMaps(){
        buttonOpenGMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q="+LocationService.getSaveLatLng().getLatitude()+","+LocationService.getSaveLatLng().getLongitude());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }

    private void initView(){
        mapView = findViewById(R.id.small_map);
        merkSound = (TextView) findViewById(R.id.item_merk);
        hargaSound = (TextView) findViewById(R.id.item_harga);
        kategoriSound = (TextView) findViewById(R.id.item_kategori);
        descSound = (TextView) findViewById(R.id.item_deskripsi);
        detailCarousel = (CarouselView) findViewById(R.id.carosel_item_sound);
        alamatText = (EditText) findViewById(R.id.item_alamat);
        favoriteButton = (MaterialFavoriteButton) findViewById(R.id.favorite);

        SharedPreferences getSharedHakAkses = getSharedPreferences("hak-akses", Context.MODE_PRIVATE);
        hakAkses = getSharedHakAkses.getString("hakAkses",null);

        buttonOpenGMaps = (Button) findViewById(R.id.btn_open_maps);
        buttonSewa = (Button) findViewById(R.id.btn_sewa);
        storageRef = FirebaseStorage.getInstance().getReference("images");
    }

    private void HideButtonSewa(String hakAkses){
        if(hakAkses.equals("Pelapak")){
            buttonSewa.setVisibility(View.GONE);
        }else{
            buttonSewa.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        geoJsonSource = new GeoJsonSource(geojsonSourceLayerId);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                moveAndaddCamera();
            }
        });
    }

    private void SewaSound(){
        buttonSewa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ItemSoundSingle.this, PenyewaanActivity.class);
                i.putExtra("idSound", idSound);
                i.putExtra("idPelapak", idPelapak);
                i.putExtra("idPenyewa", idPenyewa);
                startActivity(i);
            }
        });
    }

    private void Favorite(){
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

        favoriteButton.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
            @Override
            public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                if(favorite){
                    isFavorite = "true";
                }else{
                    isFavorite = "false";
                }
            }
        });
        favoriteButton.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
            @Override
            public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                dbRef = FirebaseDatabase.getInstance().getReference();
                Favorite Favorite = new Favorite(idSound, idPenyewa, idPelapak, isFavorite, combineId);
                if(idFavorite.equals("")){
                    idFavorite = dbRef.push().getKey();
                }
                dbRef.child("Favorite").child(idFavorite).setValue(Favorite);
            }
        });
    }

    private void ReadData(){
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("Sound").orderByKey().equalTo(idSound).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot val : dataSnapshot.getChildren()) {
                    SoundBarang soundBarang = val.getValue(SoundBarang.class);
                    merkSound.setText(soundBarang.getMerkSound());
                    setTitle("Detail "+soundBarang.getMerkSound());
                    hargaSound.setText("Harga : "+soundBarang.getHargaSound());
                    kategoriSound.setText("Kategori : " + soundBarang.getKategori());
                    descSound.setText("Deskripsi : \n" +soundBarang.getDeskripsi());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final long ONE_MEGABYTE = 1024 * 1024;
        int id = 0;

        arrImage = new ArrayList<Bitmap>();

        storageRef.child(idPelapak).child(idSound).listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                Object[] obj = new Object[task.getResult().getItems().size()];
                task.getResult().getItems().toArray(obj);
                for(int i = 0; i <obj.length; i++){
                    StorageReference storageReference = (StorageReference)obj[i];
                    storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap source = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            getBitmap(source);
                        }
                    });
                }
            }
        });
    }

    public void ReadAlamat(){
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("Alamat").orderByKey().equalTo(idPelapak).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot alamatDs : dataSnapshot.getChildren()){
                    Alamat alamat = alamatDs.getValue(Alamat.class);
                    alamatText.setText(alamat.getAddress());
                    UserLongitude = alamat.getLot();
                    UserLatitude = alamat.getLat();

                    LocationService.setSaveLatLng(new LatLng(UserLatitude,UserLongitude));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void moveAndaddCamera(){
        addMarker(LocationService.getSaveLatLng());
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(LocationService.getSaveLatLng())
                        .zoom(18)
                        .build()), 4000);
        mapboxMap.getUiSettings().setAllGesturesEnabled(false);
    }

    private void getBitmap(Bitmap bm){
        arrImage.add(bm);
        detailCarousel.setImageListener(new ImageListener() {
            @Override
            public void setImageForPosition(int position, ImageView imageView) {
                imageView.setImageBitmap(arrImage.get(position));
            }
        });
        detailCarousel.setPageCount(arrImage.size());

    }

    private void addMarker(LatLng latLng){
        Icon icon = drawableToIcon(getApplicationContext(), R.drawable.map_default_map_marker, 150);
        if(marker != null){
            marker.remove();
        }
        if(latLng != null){
            marker = mapboxMap.addMarker(new MarkerOptions().position(latLng).icon(icon));
        }
    }
    public Icon drawableToIcon(@NonNull Context context, @DrawableRes int id, int size) {

        Drawable vectorDrawable = AppCompatResources.getDrawable(context, id);
        if (vectorDrawable == null)
            return null;
        Bitmap bitmap = Bitmap.createBitmap((size/10)*6, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return IconFactory.getInstance(context).fromBitmap(bitmap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                finish();
                break;

        }
        return true;
    }

}
