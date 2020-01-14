package com.elvis.CampoZone.data;

public class BookingData {
    public String uid;
    public String name;
    public String email;
    public String date;
    public String time;
    public String key;
    public String description;
    public String status;

    public BookingData(){}

    public BookingData(String uid, String name, String email, String date, String time, String key,
                       String description, String status) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.date = date;
        this.time = time;
        this.key = key;
        this.description = description;
        this.status = status;
    }

    public String getUid(){return uid;}
    public String getName(){return name;}
    public String getEmail(){return email;}
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getKey() {
        return key;
    }
    public String getDescription() {
        return description;
    }
    public String getStatus() {
        return status;
    }

}
