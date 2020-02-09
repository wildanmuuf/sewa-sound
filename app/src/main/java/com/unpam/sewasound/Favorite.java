package com.unpam.sewasound;

public class Favorite {
    private String IdSound;
    private String IdPenyewa;
    private String IdPelapak;
    private String IsFavorite;


    private String CombineId;
    public Favorite(){ }

    public Favorite(String idSound, String idPenyewa, String idPelapak, String isFavorite, String combineId) {
        IdSound = idSound;
        IdPenyewa = idPenyewa;
        IdPelapak = idPelapak;
        IsFavorite = isFavorite;
        CombineId = combineId;
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

    public String getIsFavorite() {
        return IsFavorite;
    }

    public void setIsFavorite(String isFavorite) {
        IsFavorite = isFavorite;
    }

    public String getCombineId() {
        return CombineId;
    }

    public void setCombineId(String combineId) {
        CombineId = combineId;
    }



}
