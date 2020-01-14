package com.elvis.CampoZone.data;

public class VenueData {
    public String uid;
    public String name;
    public String owner;
    public String location;
    public String price;
    public String email;
    public String number;
    public String img;
    public String key;
    public String description;

    public VenueData(){}

    public VenueData(String uid, String name, String owner, String location, String price,
                     String email, String number, String img, String key, String description) {
        this.uid = uid;
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.price = price;
        this.email = email;
        this.number = number;
        this.img = img;
        this.key = key;
        this.description = description;
    }

    public String getUid(){return uid;}
    public String getName(){return name;}
    public String getOwner() {
        return owner;
    }
    public String getLocation() {
        return location;
    }
    public String getPrice() {
        return price;
    }
    public String getEmail(){return email;}
    public String getNumber() {
        return number;
    }
    public String getImg(){return img;}
    public String getKey() {
        return key;
    }
    public String getDescription() {
        return description;
    }
}
