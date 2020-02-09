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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class TambahSound extends AppCompatActivity {
    private MaterialBetterSpinner kategoriSpinner;
    private EditText merkTxt;
    private EditText descTxt;
    private EditText hargaTxt;
    private HorizontalGridView gvSoundImage;
    private Button addButtonImages;
    private Button addButton;
    private ArrayList<Bitmap> arrImage;
    private TextInputLayout txtMerkInput, txtHargaInput;
    int PICK_IMAGE_MULTIPLE = 1;
    String imageEncoded;
    private ArrayList<Uri> mArrayUri;
    private ImageSoundAdapter imageSoundAdapter;
    List<String> imagesEncodedList;
    String[] listKategori;
    StorageReference storageRef;
    DatabaseReference dbRef;
    String Kategori="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tambah_sound);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        rupiahCurrency();
        getKategori();
        addImage();
        tambahSound();
        setTitle("Tambah Sound Barang");
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

    private void rupiahCurrency(){
        hargaTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (hargaTxt.length() <= 4) {
//                    hargaTxt.setText("Rp. ");
//                    if(hargaTxt.getSelectionEnd() != hargaTxt.getText().length()){
//                        hargaTxt.setSelection(hargaTxt.getText().length());
//                    }
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
    private void initView(){
        kategoriSpinner = (MaterialBetterSpinner) findViewById(R.id.kategoriSpin);
        listKategori = getApplicationContext().getResources().getStringArray(R.array.kategori);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, listKategori);
        kategoriSpinner.setAdapter(arrayAdapter);
        merkTxt = (EditText) findViewById(R.id.textMerk) ;
        hargaTxt = (EditText) findViewById(R.id.txtHarga);
        descTxt = (EditText) findViewById(R.id.txt_desc);
        gvSoundImage = (HorizontalGridView) findViewById(R.id.gvSound);
        addButtonImages = (Button) findViewById(R.id.btn_upload_image);
        addButton = (Button) findViewById(R.id.btn_tambah_sound);
        mArrayUri = new ArrayList<Uri>();
        arrImage = new ArrayList<Bitmap>();
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
            txtMerkInput.setError(null);
        }

        if(Harga.isEmpty()){
            valid = false;
            txtHargaInput.setError("Silahkan masukkan harga!");
        }else{
            valid = true;
            txtHargaInput.setError(null);

        }

        if(kategori.equals("")){
            valid = false;
            kategoriSpinner.setError("Silahkan pilih kategori sound");
        }else{
            valid = true;
            kategoriSpinner.setError(null);

        }

        return valid;
    }

    public void tambahSound(){
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(TambahSound.this, R.style.Theme_AppCompat_DayNight_Dialog);
                progressDialog.setMessage("Proses menambahkan...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        if(valid()) {
                            SoundBarang.setKey(dbRef.push().getKey());
                            String Merk = merkTxt.getText().toString().trim();
                            String Harga = hargaTxt.getText().toString().trim();
                            String Deskripsi = descTxt.getText().toString().trim();
                            uploadImage();
                            SoundBarang soundBarang = new SoundBarang(User.getKey(), Merk, "Rp. "+Harga, Kategori, Deskripsi);
                            dbRef.child("Sound").child(SoundBarang.getKey()).setValue(soundBarang).addOnCompleteListener(TambahSound.this, new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Snackbar.make(addButton, "Sound gagal dimasukan. Error : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                    } else {

                                        Snackbar.make(addButton, "Sound berhasil dimasukkan", Snackbar.LENGTH_LONG).show();
                                        finish();
                                    }
                                }
                            });
                        }else{
                            Snackbar.make(addButton, "Sound Gagal ditambahkan", Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, 2500);

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
            Snackbar.make(addButton, "Gambar tidak boleh kosong", Snackbar.LENGTH_LONG).show();
        }else {
            Uri[] uri = new Uri[mArrayUri.size()];

            for (int i = 0; i < mArrayUri.size(); i++) {
                uri[i] = Uri.parse(mArrayUri.get(i).toString());
                StorageReference ref = storageRef.child(User.getKey()+"/"+ SoundBarang.getKey()+"/"+SoundBarang.getKey()+"-"+i);
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
                    imageSoundAdapter = new ImageSoundAdapter( getApplicationContext(),arrImage);
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

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded  = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                            imageSoundAdapter = new ImageSoundAdapter(getApplicationContext(),arrImage);
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
}
