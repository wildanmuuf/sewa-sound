package com.unpam.sewasound;

import android.graphics.Bitmap;

public class SoundBarang {

    private String IdPelapak;
    private String HargaSound;
    private String MerkSound;
    private String Kategori;
    private Bitmap SoundImage;
    private String Deskripsi;
    public String getDeskripsi() {
        return Deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        Deskripsi = deskripsi;
    }


    public SoundBarang(){}

    public SoundBarang(String idPelapak, String merkSound, String hargaSound,  String kategori, String deskripsi) {
        this.IdPelapak = idPelapak;
        this.HargaSound = hargaSound;
        this.MerkSound = merkSound;
        this.Kategori = kategori;
        this.Deskripsi = deskripsi;
    }

    public SoundBarang(String merkSound, String hargaSound,  String kategori, Bitmap soundImage) {
        this.HargaSound = hargaSound;
        this.MerkSound = merkSound;
        this.Kategori = kategori;
        this.SoundImage = soundImage;
    }

    public SoundBarang(String merkSound, String hargaSound,  String kategori) {
        this.HargaSound = hargaSound;
        this.MerkSound = merkSound;
        this.Kategori = kategori;
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


    private static String Key;
    public static void setKey(String key){
        Key = key;
    }
    public static String getKey(){
        return Key;
    }
}
