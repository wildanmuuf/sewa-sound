package com.unpam.sewasound;

import android.graphics.Bitmap;

public class User {

    private String IdUser;
    private String Email;
    private String Password;
    private String Name;
    private String Username;
    private String HakAkses;
    private String NoTelp;
    private String Address;
    private String Distance;
    private Bitmap PhotoProfile;

    public String getIdUser() {
        return IdUser;
    }

    public void setIdUser(String idUser) {
        IdUser = idUser;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getDistance() {
        return Distance;
    }

    public void setDistance(String distance) {
        Distance = distance;
    }


    public User(){}
    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getHakAkses() {
        return HakAkses;
    }

    public void setHakAkses(String hakAkses) {
        HakAkses = hakAkses;
    }

    public String getNoTelp() {
        return NoTelp;
    }

    public void setNoTelp(String noTelp) {
        NoTelp = noTelp;
    }



    public Bitmap getPhotoProfile() {
        return PhotoProfile;
    }

    public void setPhotoProfile(Bitmap photoProfile) {
        PhotoProfile = photoProfile;
    }

    public static String Key;


    public User(String email, String password, String name, String username, String hakAkses) {
        Email = email;
        Password = password;
        Name = name;
        Username = username;
        HakAkses = hakAkses;
    }
    //without no telp. this only for update
    public User(String email, String password, String name, String username, String hakAkses, String noTelp) {
        Email = email;
        Password = password;
        Name = name;
        Username = username;
        HakAkses = hakAkses;
        NoTelp = noTelp;

    }

    public static void setKey(String key){
        Key = key;
    }
    public static String getKey(){
        return Key;
    }
}
