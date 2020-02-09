package com.unpam.sewasound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.leanback.widget.HorizontalGridView;
import androidx.room.Database;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.synnapps.carouselview.ImageListener;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class UpdateSound extends AppCompatActivity {
    private MaterialBetterSpinner kategoriSpinner;
    private TextView titleTxt;
    private EditText merkTxt;
    private EditText hargaTxt;
    private EditText descTxt;
    private HorizontalGridView gvSoundImage;
    private Button addButtonImages;
    private Button updateButton;

    private TextInputLayout txtMerkInput, txtHargaInput;
    private ArrayList<Bitmap> arrImage;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    private ArrayList<Uri> mArrayUri;
    private ImageSoundAdapter imageSoundAdapter;
    List<String> imagesEncodedList;
    ArrayAdapter<String> arrayAdapter;
    String[] listKategori;
    StorageReference storageRef;
    DatabaseReference dbRef;
    String Kategori="";
    String idSoundBarang;
    int currentCountImage = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_sound);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent i = getIntent();
        idSoundBarang = i.getStringExtra("idSound");

        initView();
        ReadSingleSound(idSoundBarang);
        rupiahCurrency();
        getKategori();
        addImage();
        tambahSound();

    }

    private void ReadSingleSound(String idSoundBarang) {
        dbRef.child("Sound").orderByKey().equalTo(idSoundBarang).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String kategori="";
                for (DataSnapshot val : dataSnapshot.getChildren()) {
                    SoundBarang soundBarang = val.getValue(SoundBarang.class);
                    merkTxt.setText(soundBarang.getMerkSound());
                    hargaTxt.setText(soundBarang.getHargaSound());
                    descTxt.setText(soundBarang.getDeskripsi());
                    setTitle("Update Sound '"+soundBarang.getMerkSound()+"'");
                    kategori = soundBarang.getKategori();
                }
                for(int i=0; i < arrayAdapter.getCount(); i++) {
                    if(kategori.trim().equals(arrayAdapter.getItem(i).toString())){
                        kategoriSpinner.setText(arrayAdapter.getItem(i).toString());
                        break;
                    }
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
                if(obj.length>0){
                    currentCountImage = obj.length;
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

            }
        });
    }

    private void getBitmap(Bitmap bm){
        arrImage.add(bm);
        imageSoundAdapter = new ImageSoundAdapter(UpdateSound.this, arrImage);
        gvSoundImage.setAdapter(imageSoundAdapter);
        gvSoundImage.setVerticalSpacing(gvSoundImage.getHorizontalSpacing());
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) gvSoundImage
                .getLayoutParams();
        mlp.setMargins(0, gvSoundImage.getHorizontalSpacing(), 0, 0);
    }

    private void rupiahCurrency(){
        hargaTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               /* if (hargaTxt.length() < 4) {
                    hargaTxt.setText("Rp. ");
                }*/
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void initView(){
        titleTxt = (TextView) findViewById(R.id.title_sound);
        titleTxt.setText("Perbaharui");
        kategoriSpinner = (MaterialBetterSpinner) findViewById(R.id.kategoriSpin);
        listKategori = getApplicationContext().getResources().getStringArray(R.array.kategori);
        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, listKategori);
        kategoriSpinner.setAdapter(arrayAdapter);
        merkTxt = (EditText) findViewById(R.id.textMerk);
        hargaTxt = (EditText) findViewById(R.id.txtHarga);
        descTxt = (EditText) findViewById(R.id.txtDesc);
        gvSoundImage = (HorizontalGridView) findViewById(R.id.gvSound);
        addButtonImages = (Button) findViewById(R.id.btn_upload_image);
        updateButton = (Button) findViewById(R.id.btn_update_sounds);
        mArrayUri = new ArrayList<Uri>();

        txtMerkInput = (TextInputLayout) findViewById(R.id.textInputMerk);
        txtHargaInput = (TextInputLayout) findViewById(R.id.textInputHarga);

        storageRef = FirebaseStorage.getInstance().getReference("images");
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public String getKategori(){
        kategoriSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                Kategori = parent.getItemAtPosition(position).toString();

            }

        });
        return Kategori;
    }

    private boolean valid(){
        boolean valid = false;
        String kategori = Kategori;
        String Merk = merkTxt.getText().toString().trim();
        String Harga = hargaTxt.getText().toString().trim();
        if(Merk.isEmpty()){
            valid = false;
            txtMerkInput.setError("Silahkan masukkan merk sound!");
        }else{
            valid = true;
        }

        if(Harga.isEmpty()){
            valid = false;
            txtHargaInput.setError("Silahkan masukkan harga!");
        }else{
            valid = true;
        }

        if(kategori.isEmpty()){
            valid = false;
            kategoriSpinner.setError("Silahkan pilih kategori sound");
        }else{
            valid = true;
        }

        return valid;
    }

    public void tambahSound(){
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(UpdateSound.this, R.style.Theme_AppCompat_DayNight_Dialog);
                progressDialog.setMessage("Proses menambahkan...");
                progressDialog.setCancelable(false);
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if (valid()) {
                            SoundBarang.setKey(idSoundBarang);
                            String Merk = merkTxt.getText().toString().trim();
                            String Harga = hargaTxt.getText().toString().trim();
                            String Deskripsi = descTxt.getText().toString().trim();
                            uploadImage();
                            SoundBarang soundBarang = new SoundBarang(User.getKey(), "Rp. "+Merk, Harga, Kategori, Deskripsi);
                            dbRef.child("Sound").child(SoundBarang.getKey()).setValue(soundBarang).addOnCompleteListener(UpdateSound.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Snackbar.make(updateButton, "Sound gagal diupdate. Error : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                    } else {

                                        Snackbar.make(updateButton, "Sound berhasil diupdate", Snackbar.LENGTH_LONG).show();
                                        finish();
                                    }
                                }
                            });
                        }else{
                            Snackbar.make(updateButton, "Sound gagal diupdate", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, 1000);
            }
        });

    }

    public void addImage(){
        addButtonImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGE_MULTIPLE);
            }
        });
    }

    private void uploadImage(){
        if(mArrayUri.size()<0){
            Snackbar.make(updateButton, "Gambar tidak boleh kosong", Snackbar.LENGTH_LONG).show();
        }else {
            Uri[] uri = new Uri[mArrayUri.size()];

            for (int i = 0; i < mArrayUri.size(); i++) {
                uri[i] = Uri.parse(mArrayUri.get(i).toString());
                StorageReference ref = storageRef.child(User.getKey()+"/"+ SoundBarang.getKey()+"/"+SoundBarang.getKey()+"-"+(i+currentCountImage+1));
                ref.putFile(uri[i])
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(getApplicationContext(), "Failed " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                        .getTotalByteCount());
                            }
                        });
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                imagesEncodedList = new ArrayList<String>();

                if(data.getData()!=null){

                    Uri mImageUri=data.getData();
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mImageUri);
                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    mArrayUri.add(mImageUri);
                    arrImage.add(bitmap);
                    imageSoundAdapter = new ImageSoundAdapter( UpdateSound.this,arrImage, mArrayUri, currentCountImage);
                    gvSoundImage.setAdapter(imageSoundAdapter);
                    gvSoundImage.setVerticalSpacing(gvSoundImage.getHorizontalSpacing());
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) gvSoundImage
                            .getLayoutParams();
                    mlp.setMargins(0, gvSoundImage.getHorizontalSpacing(), 0, 0);

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            mArrayUri.add(uri);
                            arrImage.add(bitmap);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            imageSoundAdapter = new ImageSoundAdapter(UpdateSound.this, arrImage, mArrayUri, currentCountImage);
                            gvSoundImage.setAdapter(imageSoundAdapter);
                            gvSoundImage.setVerticalSpacing(gvSoundImage.getHorizontalSpacing());
                            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) gvSoundImage
                                    .getLayoutParams();
                            mlp.setMargins(0, gvSoundImage.getHorizontalSpacing(), 0, 0);

                        }
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
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
