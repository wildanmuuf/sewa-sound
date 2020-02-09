package com.unpam.sewasound;

public class    Penyewaan {
    private String IdSound;
    private String IdPenyewa;
    private String IdPelapak;
    private String TanggalSewa;
    private String TanggalAwal;
    private String TanggalAkhir;
    private String AlamatTarget;
    private String Status;
    private String Total;
    private String Keterangan;

    public Penyewaan(){}

    public Penyewaan(String idSound,
                     String idPenyewa,
                     String idPelapak,
                     String tanggalSewa,
                     String tanggalAwal,
                     String tanggalAkhir,
                     String alamatTarget,
                     String status, String keterangan, String total) {
        IdSound = idSound;
        IdPenyewa = idPenyewa;
        IdPelapak = idPelapak;
        TanggalSewa = tanggalSewa;
        TanggalAwal = tanggalAwal;
        TanggalAkhir = tanggalAkhir;
        AlamatTarget = alamatTarget;
        Status = status;
        Keterangan = keterangan;
        Total = total;
    }

    public String getIdSound() {
        return IdSound;
    }

    public void setIdSound(String idSound) {
        IdSound = idSound;
    }

    public String getIdPenyewa() {
        return IdPenyewa;
    }

    public void setIdPenyewa(String idPenyewa) {
        IdPenyewa = idPenyewa;
    }

    public String getIdPelapak() {
        return IdPelapak;
    }

    public void setIdPelapak(String idPelapak) {
        IdPelapak = idPelapak;
    }

    public String getTanggalSewa() {
        return TanggalSewa;
    }

    public void setTanggalSewa(String tanggalSewa) {
        TanggalSewa = tanggalSewa;
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
