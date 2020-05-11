package com.example.mapphotos;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

import com.example.mapphotos.bean.Picture;
import com.example.mapphotos.bean.RowInfoBean;
import com.example.mapphotos.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class DBOpenHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;
    private MainActivity mainActivity;
    public DBOpenHelper(Context context) {
        super(context, "mapphotos.db", null, 1);
        db = getReadableDatabase();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists t_album(id integer primary key autoincrement, title text, thumb text)");
        db.execSQL("create table if not exists t_album_picture(id integer primary key autoincrement, latitude real, longitude real, picture text, thumb text, album_id integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    public void addPicture(double latitude, double longitude, String picture, String thumb, int albumId) {
        db.execSQL("insert into t_album_picture(latitude, longitude, picture, thumb, album_id) values(?, ?, ?, ?, ?)", new Object[]{latitude, longitude, picture, thumb, albumId});
    }

    public void deletePictureById(int id) {
        db.execSQL("delete from t_album_picture where id = ?", new Object[]{id});
    }

    public void deletePictureByAlbumId(int albumId) {
        db.execSQL("delete from t_album_picture where album_id = ?", new Object[]{albumId});
    }

    public void addAlbum(String title) {
        db.execSQL("insert into t_album(title, thumb) values(?, ?)", new Object[]{title, null});
    }

    public void updateAlbumThumb(int id, String thumb) {
        db.execSQL("update t_album set thumb = ? where id = ?", new Object[]{thumb,id});
    }

    public void updateAlbumTitle(int id, String title) {
        db.execSQL("update t_album set title = ? where id = ?", new Object[]{title,id});
    }

    public LinearLayout getAllPicture(LinearLayout gallery, GalleryActivity galleryActivity) {
        Cursor cursor = db.rawQuery("select * from t_album_picture order by id desc", null);
        while (cursor.moveToNext()) {
            String picture = cursor.getString(cursor.getColumnIndex("picture"));
            String path = CommonUtils.PICTURE_PATH + picture;
            // 根据照片图像创建缩略图 view，加入到 gallery 布局
            View view = galleryActivity.getImageView(path);
            gallery.addView(view);
        }
        return gallery;
    }

    public LinearLayout getPictureById(LinearLayout gallery, int albumId, GalleryActivity galleryActivity) {
        Cursor cursor = db.rawQuery("select * from t_album_picture " + "where album_id = ? order by id desc", new String[]{String.valueOf(albumId)});
        while (cursor.moveToNext()) {
            String picture = cursor.getString(cursor.getColumnIndex("picture"));
            String path = CommonUtils.PICTURE_PATH + picture;
            // 根据照片图像创建缩略图 view，加入到 gallery 布局
            View view = galleryActivity.getImageView(path);
            gallery.addView(view);
        }
        return gallery;
    }

    public Picture getPictureById(int id) {
        Picture picture = new Picture();
        Cursor cursor = db.rawQuery("select id, picture from t_album_picture" + " where id = ?", new String[]{String.valueOf(id)});
        while (cursor.moveToNext()) {
            picture.setPictureName(cursor.getString(cursor.getColumnIndex("picture")));
            picture.setId(cursor.getInt(cursor.getColumnIndex("id")));
        }
        return picture;
    }

    public ArrayList<Picture> getPictureByAlbumId(int albumId) {
        ArrayList<Picture> pictureInfo = new ArrayList<Picture>();
        Cursor cursor = db.rawQuery("select id, picture from t_album_picture" + " where album_id = ?", new String[]{String.valueOf(albumId)});
        while (cursor.moveToNext()) {
            String picture = cursor.getString(cursor.getColumnIndex("picture"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            pictureInfo.add(new Picture(id, picture));
        }
        return pictureInfo;
    }

    public ArrayList<Picture> getMaxIdPictureByAlbumId(int albumId) {
        ArrayList<Picture> pictureInfo = new ArrayList<Picture>();
        Cursor cursor = db.rawQuery("select max(id), latitude, longitude, picture from t_album_picture" + " where album_id = ? group by latitude, longitude", new String[]{String.valueOf(albumId)});
        while (cursor.moveToNext()) {
            String picture = cursor.getString(cursor.getColumnIndex("picture"));
            double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
            double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));
            pictureInfo.add(new Picture(picture, latitude, longitude));
        }
        return pictureInfo;
    }

    public ArrayList<Picture> getAllPicture() {
        ArrayList<Picture> pictures = new ArrayList<>();
        Cursor cursor = db.rawQuery("select id,picture from t_album_picture", null);
        while (cursor.moveToNext()) {
            String picture = cursor.getString(cursor.getColumnIndex("picture"));
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            pictures.add(new Picture(id, picture));
        }
        return pictures;
    }
    public void deleteAlbum(int albumId) {
        db.execSQL("delete from t_album where id = ?", new Object[]{albumId});
    }
}
