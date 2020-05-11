package com.example.mapphotos.bean;

import android.graphics.drawable.Drawable;

public class RowInfoBean {
    private int id; // 相册id
    private Drawable thumb; // 相册图标
    private String title; // 相册标题

    public RowInfoBean(Drawable thumb, String title) {
        this.thumb = thumb;
        this.title = title;
    }

    public RowInfoBean() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Drawable getThumb() {
        return thumb;
    }

    public void setThumb(Drawable thumb) {
        this.thumb = thumb;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
