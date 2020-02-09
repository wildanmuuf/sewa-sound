package com.unpam.sewasound;

public class Alamat {
    private String Address;
    private double Lat;
    private double Lot;

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }


    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public double getLot() {
        return Lot;
    }

    public void setLot(double lot) {
        Lot = lot;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }

    private String Distance;


    public Alamat(String address, double lat, double lot){
        Address = address;
        Lat = lat;
        Lot = lot;
    }

    public Alamat(){}

}
