package com.elvis.CampoZone.data;

public class UserData {
    public String uid;
    public String name;
    public String email;
    public String password;
    public String img;
    public boolean client;

    public UserData(){}

    public UserData(String uid, String name, String email, String password, String img, boolean client) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.password = password;
        this.img = img;
        this.client = client;
    }

    public String getUid(){return uid;}
    public String getName(){return name;}
    public String getEmail(){return email;}
    public String getPassword(){return password;}
    public String getImg(){return img;}
    public boolean isClient() {
        return client;
    }
}