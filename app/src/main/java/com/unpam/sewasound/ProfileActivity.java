package com.unpam.sewasound;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.autofill.AutofillId;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.INPUT_METHOD_SERVICE;


public class ProfileActivity extends Fragment {

    EditText editTextName, editTextUsername, editTextPassword, editTextEmail, editTextAlamat, editTextNoTelp;
    User user;
    Alamat alamat;

    //firebase
    FirebaseAuth auth;
    DatabaseReference dbRef;
    StorageReference storageReference;

    //editText and Variable
    private TextInputLayout textInputLayoutName;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutUserName;
    private TextInputLayout textInputLayoutPassword;
    private TextInputLayout textInputLayoutPhone;
    private TextInputLayout textInputLayoutAddress;
    ImageButton buttonPhoto;
    ImageView removeImage;
    Button buttonUpdate, buttonVerify;
    Button buttonPilihLokasi;
    String title;
    double latitude, longitude;
    String hakAkses;
    public String key;

    //cons
    private static final int SELECT_PICTURE = 1234;
    private static final int PICK_FROM_GALLERY = 1;
    private static final int SELECT_LOCATION = 2;
    BitmapFactory.Options options;
    Bitmap photo;
    Uri selectedImage;
    private float pxFromDp(float dp)
    {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float dpFromPx(float px)
    {
        return px / getResources().getDisplayMetrics().density;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_profile, container, false);

        initView(rootView);
        ReadUser();
        verifyEmail();
        Update();
        UploadPhoto();
        RemoveImage();
        buttonPilihLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("status", "profile");
                startActivityForResult(intent, SELECT_LOCATION);
            }
        });
        getActivity().setTitle("My Profile");
        return rootView;
    }


    private void RemoveImage(){
        removeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_register);
                buttonPhoto.setImageBitmap(bm);
                storageReference.child("photo/"+User.getKey()).delete();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode) {
            case PICK_FROM_GALLERY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                } else {
                    //do something like displaying a message that he didn`t allow the app to access gallery and you wont be able to let him select from gallery
                }
                break;
        }
    }

    public void UploadPhoto(){
        buttonPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_FROM_GALLERY);
                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, PICK_FROM_GALLERY);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FROM_GALLERY) {
                selectedImage =  data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (selectedImage != null) {
                    Cursor cursor = getActivity().getContentResolver().query(selectedImage,
                            filePathColumn, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        options = new BitmapFactory.Options();
                        options.inSampleSize = 2;
                        photo =BitmapFactory.decodeFile(picturePath, options);
                        Bitmap size = Bitmap.createScaledBitmap(photo, 312,312, false);
                        Bitmap center;
                        if(photo.getWidth() >= photo.getHeight()){
                            center = Bitmap.createBitmap(size, size.getWidth()/2 - size.getHeight()/2, 0, size.getHeight(),size.getHeight());
                        }else{
                            center = Bitmap.createBitmap(size, 0, size.getHeight()/2 - size.getWidth()/2, size.getHeight(),size.getHeight());
                        }
                        Bitmap roundedPhoto = PhotoOptions.getCircularBitmap(center);
                        buttonPhoto.setImageBitmap(roundedPhoto);
                        cursor.close();

                    }
                }

            }
        }else if(resultCode ==SELECT_LOCATION){
            if(requestCode == SELECT_LOCATION){
                title = data.getStringExtra("alamat");
                latitude = data.getDoubleExtra("latitude", 0);
                longitude = data.getDoubleExtra("longitude", 0);
                editTextAlamat.setText(title);

            }
        }
    }

    public void ReadUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String email = auth.getCurrentUser().getEmail();
        dbRef.child("Users").orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot val : dataSnapshot.getChildren()) {
                    User.setKey(val.getKey());
                    key = val.getKey();
                    user = val.getValue(User.class);
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference("photo/" + User.getKey());
                    PhotoOptions photoOptions = new PhotoOptions();
                    photoOptions.downloadImage(getContext(), storageReference, buttonPhoto, "Profile");
                    editTextUsername.setText(user.getUsername());
                    editTextName.setText(user.getName());
                    editTextEmail.setText(user.getEmail());
                    editTextPassword.setText(user.getPassword());
                    editTextNoTelp.setText(user.getNoTelp());
                    hakAkses = user.getHakAkses();
                    dbRef.child("Alamat").child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot alamatData) {
                            if(alamatData.exists()) {
                                Alamat alamat = alamatData.getValue(Alamat.class);
                                title = alamat.getAddress();
                                longitude = alamat.getLot();
                                latitude = alamat.getLat();
                                editTextAlamat.setText(title);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void verifyEmail(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(!user.isEmailVerified()){
            buttonVerify.setEnabled(true);
            buttonVerify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                buttonVerify.setBackgroundColor(Color.parseColor("#87CEFA"));
                                buttonVerify.setEnabled(false);
                            }
                        }
                    });
                }
            });
        }else{
            buttonVerify.setBackgroundColor(Color.parseColor("#87CEFA"));
            buttonVerify.setEnabled(false);
        }

    }
    public boolean valid(){
        boolean valid;
        String Name = editTextName.getText().toString();
        String UserName = editTextUsername.getText().toString();
        String Email = editTextEmail.getText().toString();
        String Password = editTextPassword.getText().toString();
        String Phone = editTextNoTelp.getText().toString();
        String Alamat = editTextAlamat.getText().toString();
        if(Name.isEmpty() && UserName.isEmpty() && Email.isEmpty() && Password.isEmpty() && Phone.isEmpty() && Alamat.isEmpty()){
            valid = false;
            textInputLayoutName.setError("Silahkan masukkan nama!");
            textInputLayoutEmail.setError("Masukkan format email dengan benar!");
            textInputLayoutPhone.setError("Silahkan masukkan Nomor Telepon Anda!");
            textInputLayoutPassword.setError("Silahkan masukan password!");


        }else {
            if (Name.isEmpty()) {
                valid = false;
                textInputLayoutName.setError("Silahkan masukkan nama!");
            }else{
                valid = true;
                textInputLayoutName.setError(null);

            }
            //Handling validation for UserName field
            if (UserName.isEmpty()) {
                valid = false;
                textInputLayoutUserName.setError("Silahkan masukkan username!");

            } else if (UserName.length() < 5) {
                valid = false;
                textInputLayoutUserName.setError("Username terlalu pendek!");

            }else{
                valid = true;
                textInputLayoutUserName.setError(null);
            }

            //Handling validation for Email field
            if (Email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
                valid = false;
                textInputLayoutEmail.setError("Masukkan format email dengan benar!");
            }else{
                valid = true;
                textInputLayoutEmail.setError(null);

            }

            if (Phone.isEmpty() || !Patterns.PHONE.matcher(Phone).matches()) {
                valid = false;
                textInputLayoutPhone.setError("Silahkan masukkan Nomor Telepon Anda!");
            }else{
                valid = true;
                textInputLayoutPhone.setError(null);
            }

            //Handling validation for Password field
            if (Password.isEmpty()) {
                valid = false;
                textInputLayoutPassword.setError("Silahkan masukan password!");

            }else if (Password.length() < 6) {
                valid = false;
                textInputLayoutPassword.setError("Password terlalu pendek!");
            }else{
                valid = true;
                textInputLayoutPassword.setError(null);
            }
        }

        return valid;
    }

    private void Update(){

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog = new ProgressDialog(getActivity(), R.style.Theme_AppCompat_DayNight_Dialog);
                progressDialog.setMessage("Proses update...");
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(valid()){
                            final String Username = editTextUsername.getText().toString().trim();
                            final String Name = editTextName.getText().toString().trim();
                            final String Email = editTextEmail.getText().toString().trim();
                            final String Password = editTextPassword.getText().toString().trim();
                            final String NoTelp = editTextNoTelp.getText().toString().trim();

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            SharedPreferences getSharedEmailPassword = getContext().getSharedPreferences("email-password", Context.MODE_PRIVATE);
                            String currentEmail = getSharedEmailPassword.getString("email",null);
                            String currentPassword = getSharedEmailPassword.getString("password", null);
                            AuthCredential credential = EmailAuthProvider
                                    .getCredential(currentEmail, currentPassword);

                            currentUser.reauthenticate(credential)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                                firebaseUser.updateEmail(Email)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d("EMAIL", "User email address updated.");
                                                                    AuthCredential newCredential = EmailAuthProvider
                                                                            .getCredential(Email, Password);
                                                                    firebaseUser.reauthenticate(newCredential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                getActivity().finish();
                                                                                if(getActivity().isFinishing()){
                                                                                    startActivity(new Intent(getContext(), MainActivity.class));
                                                                                    Fragment root = new Root();
                                                                                    FragmentManager fmManager = getActivity().getSupportFragmentManager();
                                                                                    fmManager.beginTransaction()
                                                                                            .replace(R.id.frame_container, root)
                                                                                            .commit();
                                                                                }
                                                                            }

                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                firebaseUser.updatePassword(Password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d("PASSWORD", "User password updated.");
                                                        }
                                                    }
                                                });
                                                user = new User(Email, Password, Name, Username,hakAkses, NoTelp);
                                                dbRef.child("Users").child(User.getKey()).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        progressDialog.dismiss();
                                                        if(!task.isSuccessful()){

                                                            Snackbar.make(buttonUpdate, "Update gagal : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                        }else{

                                                            if(selectedImage != null){
                                                                PhotoOptions.uploadImageToStorage(getContext(), selectedImage, storageReference, key);
                                                            }
                                                            Snackbar.make(buttonUpdate,"Update berhasil", Snackbar.LENGTH_LONG).show();

                                                        }

                                                    }
                                                });
                                                Alamat alamat = new Alamat(title, latitude, longitude);
                                                dbRef.child("Alamat").child(User.getKey()).setValue(alamat).addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!task.isSuccessful()) {
                                                            Snackbar.make(buttonPilihLokasi, "Update gagal. Pesan : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }else{
                                                Snackbar.make(buttonUpdate, "Update gagal : " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                            }

                                        }
                                    });


                        }else{
                            progressDialog.dismiss();
                            Snackbar.make(buttonUpdate, "Update gagal!", Snackbar.LENGTH_LONG).show();

                        }
                    }
                }, 1000);

            }
        });
    }

    public void initView(View view){
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        editTextName = (EditText) view.findViewById(R.id.namaProfile);
        editTextEmail = (EditText) view.findViewById(R.id.emailProfile);
        editTextPassword = (EditText) view.findViewById(R.id.passwordProfile);
        editTextUsername = (EditText) view.findViewById(R.id.usernameProfile);
        editTextNoTelp = (EditText) view.findViewById(R.id.phoneProfile);
        editTextAlamat = (EditText) view.findViewById(R.id.alamatProfile);
        textInputLayoutName = (TextInputLayout) view.findViewById(R.id.textInputNama);
        textInputLayoutEmail = (TextInputLayout) view.findViewById(R.id.textInputEmail);
        textInputLayoutPassword = (TextInputLayout) view.findViewById(R.id.textInputPassword);
        textInputLayoutUserName = (TextInputLayout) view.findViewById(R.id.textInputUsername);
        textInputLayoutPhone = (TextInputLayout) view.findViewById(R.id.textInputPhone);
        textInputLayoutAddress = (TextInputLayout) view.findViewById(R.id.textInputAlamat);
        buttonUpdate = (Button) view.findViewById(R.id.btn_update);
        buttonPilihLokasi = (Button) view.findViewById(R.id.btn_maps);
        buttonVerify = (Button) view.findViewById(R.id.btn_verify);
        buttonPhoto = (ImageButton) view.findViewById(R.id.photoButton);
        removeImage = (ImageView) view.findViewById(R.id.minus);
    }
}
