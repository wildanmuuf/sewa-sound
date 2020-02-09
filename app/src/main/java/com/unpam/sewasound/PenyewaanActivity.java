package com.unpam.sewasound;

import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PenyewaanActivity extends AppCompatActivity {

    private DatePickerDialog datePickerDialog;
    private EditText editTextTglAwal;
    private EditText editTextTglAkhir;
    private TextInputLayout inputTglAwal, inputTglAkhir, inputAlamat;
    private EditText editTextMerk, editTextHarga, editTextKategori, editTextKeterangan,editTextAlamat, editTextTotal;
    private String idSound;
    private String idPelapak;
    private String idPenyewa;
    private String alamat;
    private List<Date> getAllDate;
    private String hakAkses;
    ProgressDialog progressDialog;
    private Button cariLokasi, useUserLocation, sewaSound;
    SimpleDateFormat formatter = null;
    private DatabaseReference dbRef;
    private NotificationUtils notificationUtils;
    final String pola = "EEEE, dd MMMM yyyy";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_penyewaan);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initView();
        ReadData();
        showDateDialog(editTextTglAwal);
        showDateDialog(editTextTglAkhir);
        cariLokasi();
        SewaSound();
        ReadRecentPenyewaan();
        UseUserLocation();
        setTitle("Data Penyewaan Sound");
    }

    private void initView(){
        idSound = getIntent().getStringExtra("idSound");
        idPelapak = getIntent().getStringExtra("idPelapak");
        idPenyewa = getIntent().getStringExtra("idPenyewa");

        editTextTglAwal = findViewById(R.id.txt_tgl_awal);
        editTextTglAkhir = findViewById(R.id.txt_tgl_akhir);
        editTextMerk = findViewById(R.id.textMerk);
        editTextHarga = findViewById(R.id.textHarga);
        editTextKategori = findViewById(R.id.textKategori);
        editTextKeterangan = findViewById(R.id.textKeterangan);
        editTextTotal = findViewById(R.id.total_sewa);
        editTextAlamat = findViewById(R.id.sewa_alamat);

        inputTglAwal = findViewById(R.id.textInputTglAwal);
        inputTglAkhir = findViewById(R.id.textInputTglAkhir);
        inputAlamat = findViewById(R.id.textInputAlamat);

        cariLokasi = findViewById(R.id.btn_cari_lokasi);
        useUserLocation = findViewById(R.id.alamat_user);
        sewaSound = findViewById(R.id.btn_sewa_sound);
        dbRef = FirebaseDatabase.getInstance().getReference();
        formatter = new SimpleDateFormat(pola, new Locale("id"));
        getAllDate = new ArrayList<>();
        notificationUtils = new NotificationUtils(this);

    }

    private boolean valid(){
        boolean valid = false;
        String TanggalAwal = editTextTglAwal.getText().toString();
        String TanggalAkhir = editTextTglAkhir.getText().toString();
        String Alamat = editTextAlamat.getText().toString();

        if(TanggalAwal.isEmpty() || TanggalAwal.equals("")){
            inputTglAwal.setError("Pilih tanggal awal penyewaan");
            valid = false;
        }else{
            inputTglAwal.setError(null);
            valid = true;
        }

        if(TanggalAkhir.isEmpty() || TanggalAkhir.equals("")){
            inputTglAkhir.setError("Pilih tanggal awal penyewaan");
            valid = false;
        }else{
            inputTglAkhir.setError(null);
            valid = true;
        }

        if(Alamat.isEmpty() || Alamat.equals("")){
            inputAlamat.setError("Pilih tanggal awal penyewaan");
            valid = false;
        }else{
            inputAlamat.setError(null);
            valid = true;
        }

        return valid;
    }

    private void UseUserLocation(){
        useUserLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbRef = FirebaseDatabase.getInstance().getReference();
                dbRef.child("Alamat").orderByKey().equalTo(idPenyewa).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            Alamat alamat = ds.getValue(Alamat.class);
                            editTextAlamat.setText(alamat.getAddress());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void ReadData(){
        dbRef.child("Sound").orderByKey().equalTo(idSound).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    SoundBarang soundBarang = ds.getValue(SoundBarang.class);
                    editTextMerk.setText(soundBarang.getMerkSound());
                    editTextHarga.setText(soundBarang.getHargaSound());
                    editTextKategori.setText(soundBarang.getKategori());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SewaSound(){
        sewaSound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog = new ProgressDialog(PenyewaanActivity.this, R.style.Theme_AppCompat_DayNight_Dialog);
                progressDialog.setMessage("Mohon tunggu...");
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();

                        if(valid()){
                            final String TanggalAwal = editTextTglAwal.getText().toString();
                            final String TanggalAkhir = editTextTglAkhir.getText().toString();
                            final String Alamat = editTextAlamat.getText().toString();
                            final String Keterangan = editTextKeterangan.getText().toString();
                            final String Total = editTextTotal.getText().toString();
                            Date today = new Date();
                            final String TanggalSewa = tampilkanTanggalDanWaktu(today, pola, new Locale("id"));
                            Penyewaan penyewaan = new Penyewaan(idSound,
                                    idPenyewa,
                                    idPelapak,
                                    TanggalSewa,
                                    TanggalAwal,
                                    TanggalAkhir, Alamat, String.valueOf(StatusConstant.PROCESS), Keterangan, Total);
                            String key = dbRef.push().getKey();
                            dbRef.child("Penyewaan").child(key).setValue(penyewaan).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isComplete()){
                                        Snackbar.make(sewaSound, "Penyewaan akan diproses", Snackbar.LENGTH_LONG).show();
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                                    Notification.Builder builder =
                                                            notificationUtils.getAndroidChannelNotification("Penyewaan Sound "+
                                                                    editTextMerk.getText()+" akan diproses!", "Silahkan telepon pelapak");
                                                    notificationUtils.getManager().notify(101, builder.build());
                                                }
                                                Intent i = new Intent(PenyewaanActivity.this, MainActivity.class);
                                                startActivity(i);
                                            }
                                        },2000);
                                    }else{
                                        Snackbar.make(sewaSound, "Penyewaan gagal", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                }, 1000);

            }
        });
    }

    private void cariLokasi(){
        cariLokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(PenyewaanActivity.this, MapsActivity.class);
                i.putExtra("status", "penyewaan");
                startActivityForResult(i, 2);
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2) {
            if(resultCode == 2) {
                alamat = data.getStringExtra("alamat");
                editTextAlamat.setText(alamat);
            }
        }
    }

    private void CalculateDate(){
        editTextTglAkhir.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!editTextTglAwal.getText().toString().equals("") &&
                        !editTextTglAkhir.getText().toString().equals("")){
                    try{
                        Date tgl_awal = formatter.parse(editTextTglAwal.getText().toString());
                        Date tgl_akhir = formatter.parse(editTextTglAkhir.getText().toString());

                        long diff = tgl_akhir.getTime() - tgl_awal.getTime();
                        int days =(int) diff/(24 * 60 * 60 * 1000);
                        if(days ==0){
                            days =1;
                        }
                        String harga = editTextHarga.getText().toString().substring(4);
                        int total = Integer.parseInt(harga)*days;
                        editTextTotal.setText("Rp. "+String.valueOf(total));

                    }catch (Exception err){
                        Toast.makeText(PenyewaanActivity.this, err.getMessage(), Toast.LENGTH_LONG);
                    }
                }
            }
        });

    }

    private void ReadRecentPenyewaan(){
        dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("Penyewaan").orderByChild("idSound").equalTo(idSound ).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Penyewaan penyewaan = ds.getValue(Penyewaan.class);
                    try {
                        Date startDate = formatter.parse(penyewaan.getTanggalAwal());
                        Date endDate = formatter.parse(penyewaan.getTanggalAkhir());
                        getAllDate = getDatesBetween(startDate, endDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showDateDialog(final EditText editText){
        Locale.setDefault(new Locale("id"));
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar newCalendar = Calendar.getInstance();
                datePickerDialog = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                                Calendar newDate = Calendar.getInstance();
                                newDate.set(year, monthOfYear, dayOfMonth);
                                String hasil = tampilkanTanggalDanWaktu(newDate.getTime(), pola,new Locale("id"));
                                editText.setText(hasil);
                                CalculateDate();
                            }
                        },
                        newCalendar.get(Calendar.YEAR),
                        newCalendar.get(Calendar.MONTH),
                        newCalendar.get(Calendar.DAY_OF_MONTH)
                );
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, 3);

                datePickerDialog.setMinDate(calendar);
                Calendar[] days = new Calendar[getAllDate.size()];
                for(int i = 0; i < getAllDate.size(); i++){
                    Calendar day = Calendar.getInstance();
                    day.setTime(getAllDate.get(i));
                    days[i] = day;
                }
                datePickerDialog.setOkColor(Color.WHITE);
                datePickerDialog.setCancelColor(Color.WHITE);
                datePickerDialog.setDisabledDays(days);
//                Log.e("day", days[1].get(Calendar.DATE)+"");
                datePickerDialog.show(getSupportFragmentManager(), "DatePicker");

            }
        });
    }

    private List<Date> getDatesBetween(Date startDate, Date endDate){
        List<Date> datesInRange = new ArrayList<>();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        while(startCalendar.before(endCalendar)){
            Date result = startCalendar.getTime();

            datesInRange.add(result);
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        Date lastDate = endCalendar.getTime();
        datesInRange.add(lastDate);
        return datesInRange;
    }


    private String tampilkanTanggalDanWaktu(Date tanggalDanWaktu,
                                                     String pola, Locale lokal) {
        String tanggalStr = null;

        if (lokal == null) {
            formatter = new SimpleDateFormat(pola);
        } else {
            formatter = new SimpleDateFormat(pola, lokal);
        }

        tanggalStr = formatter.format(tanggalDanWaktu);
        return tanggalStr;
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
