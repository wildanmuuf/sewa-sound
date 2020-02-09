package com.unpam.sewasound;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import static android.content.Context.MODE_PRIVATE;


public class SoundBarangItem {

    private String IdSoundBarang;
    private String IdPelapak;
    private String HargaSound;
    private String MerkSound;
    private String Kategori;
    private Bitmap SoundImage;


    public String getIdPenyewa(Context ctx) {
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("email-password", MODE_PRIVATE);
        String idPenyewa = sharedPreferences.getString("idUser",null);
        return idPenyewa;
    }



    public SoundBarangItem(){}

    public String getIdSoundBarang() {
        return IdSoundBarang;
    }

    public void setIdSoundBarang(String idSoundBarang) {
        IdSoundBarang = idSoundBarang;
    }

    public String getHargaSound() {
        return HargaSound;
    }

    public void setHargaSound(String hargaSound) {
        this.HargaSound = hargaSound;
    }

    public String getMerkSound() {
        return MerkSound;
    }

    public void setMerkSound(String merkSound) {
        this.MerkSound = merkSound;
    }

    public String getKategori() {
        return Kategori;
    }

    public void setKategori(String kategori) {
        this.Kategori = kategori;
    }

    public String getIdPelapak() {
        return IdPelapak;
    }

    public void setIdPelapak(String idPelapak) {
        IdPelapak = idPelapak;
    }

    public Bitmap getSoundImage() {
        return SoundImage;
    }

    public void setSoundImage(Bitmap soundImage) {
        SoundImage = soundImage;
    }


}
