package com.example.mapphotos.bean;

public class Picture {
    private int id;
    private double latitude;
    private double longitude;
    private String pictureName;
    private String thumbName;
    private int albumId;

    public Picture(String pictureName, double latitude, double longitude) {
        this.pictureName = pictureName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Picture(int id, String picture) {
        this.pictureName = picture;
        this.id = id;
    }

    public Picture() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPictureName() {
        return pictureName;
    }

    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    public String getThumbName() {
        return thumbName;
    }

    public void setThumbName(String thumbName) {
        this.thumbName = thumbName;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }
}
