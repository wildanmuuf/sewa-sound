package com.unpam.sewasound;

import android.graphics.Bitmap;

public class TransaksiItem {
    private String IdPenyewaan;
    private String Sound;
    private String UserTrx;
    private String TanggalAwal;
    private String TanggalAkhir;
    private String AlamatTarget;
    private String Status;
    private String Total;
    private String Keterangan;
    private Bitmap SoundImage;
    private String NoTelp;
    private String Kategori;

    public String getKategori() {
        return Kategori;
    }

    public void setKategori(String kategori) {
        Kategori = kategori;
    }


    public String getNoTelp() {
        return NoTelp;
    }

    public void setNoTelp(String noTelp) {
        NoTelp = noTelp;
    }

    public Bitmap getSoundImage() {
        return SoundImage;
    }

    public void setSoundImage(Bitmap soundImage) {
        SoundImage = soundImage;
    }

    public String getIdPenyewaan() {
        return IdPenyewaan;
    }

    public void setIdPenyewaan(String idPenyewaan) {
        IdPenyewaan = idPenyewaan;
    }

    public String getSound() {
        return Sound;
    }

    public void setSound(String sound) {
        Sound = sound;
    }

    public String getUserTrx() {
        return UserTrx;
    }

    public void setUserTrx(String userTrx) {
        UserTrx = userTrx;
    }

    public String getTanggalAwal() {
        return TanggalAwal;
    }

    public void setTanggalAwal(String tanggalAwal) {
        TanggalAwal = tanggalAwal;
    }

    public String getTanggalAkhir() {
        return TanggalAkhir;
    }

    public void setTanggalAkhir(String tanggalAkhir) {
        TanggalAkhir = tanggalAkhir;
    }

    public String getAlamatTarget() {
        return AlamatTarget;
    }

    public void setAlamatTarget(String alamatTarget) {
        AlamatTarget = alamatTarget;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getTotal() {
        return Total;
    }

    public void setTotal(String total) {
        Total = total;
    }

    public String getKeterangan() {
        return Keterangan;
    }

    public void setKeterangan(String keterangan) {
        Keterangan = keterangan;
    }


}
