package com.unpam.sewasound;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailSound extends AppCompatActivity {
    private String idSoundBarang;
    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private TextView merkSound, hargaSound, kategoriSound, descSound;
    private ArrayList<Bitmap> arrImage;
    private CarouselView detailCarousel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_sound);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        idSoundBarang = i.getStringExtra("idSound");
        initView();
        ReadSingleSound(idSoundBarang);
    }
    private void ReadSingleSound(String idSoundBarang) {
        dbRef.orderByKey().equalTo(idSoundBarang).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot val : dataSnapshot.getChildren()) {
                    SoundBarang soundBarang = val.getValue(SoundBarang.class);
                    merkSound.setText(soundBarang.getMerkSound());
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

        storageRef.child(User.getKey()).child(idSoundBarang).listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
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

    private void initView(){
        merkSound = (TextView) findViewById(R.id.detail_merk);
        hargaSound = (TextView) findViewById(R.id.detail_harga);
        kategoriSound = (TextView) findViewById(R.id.detail_kategori);
        descSound = (TextView) findViewById(R.id.detail_deskripsi);
        detailCarousel = (CarouselView) findViewById(R.id.carosel_detail_sound);
        dbRef = FirebaseDatabase.getInstance().getReference("Sound");
        storageRef = FirebaseStorage.getInstance().getReference("images");
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
