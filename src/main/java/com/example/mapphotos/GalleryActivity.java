package com.example.mapphotos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mapphotos.util.CommonUtils;

public class GalleryActivity extends Activity {
    private LinearLayout gallery; // 底部显示照片缩略图的长廊
    private ImageView pictureView; // 显示照片的组件
    private DBOpenHelper dbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_view);
        dbOpenHelper = new DBOpenHelper(this);
        // 初始化界面组件
        gallery = (LinearLayout) findViewById(R.id.galleryView);
        pictureView = (ImageView) findViewById(R.id.imageView_picture);
//        pictureView.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//
//                return false;
//            }
//        });
        // 获取 Intent 传递过来的相册 id
        int albumId = getIntent().getIntExtra("album_id", -1);
        getPictureById(albumId);
    }

    private void getPictureById(int albumId) {
        // 检查存储卡是否有效
        if (!(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))) {
            return;
        }
        if (albumId == -1) {
            // 如果相册 id 无效，显示所有相册的照片
            // 从数据库中获取所有照片的照片文件名
            gallery = dbOpenHelper.getAllPicture(gallery, this);
        } else {
            // 显示相册 id 为 albumId 值的所有照片
            gallery = dbOpenHelper.getPictureById(gallery, albumId, this);
        }

    }

    public View getImageView(final String path) {
        int width = dip2px(80);
        int height = dip2px(80);
        // 从照片解码 80x80 在缩略图
        Bitmap bitmap = CommonUtils.decodeBitmapFromFile(path, width, height);
        // 创建 ImageView 组件代表照片缩略图
        ImageView imageView = new ImageView(this);
        // 设定 ImageView 的大小，缩放形式和显示图像
        imageView.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bitmap);
        // 将 ImageView 加入到 LinearLayout 中
        final LinearLayout layout = new LinearLayout(this);
        // 设定 LinearLayout 的大小，重心和右侧空白边距
        layout.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(0, 0, dip2px(5), 0);
        layout.addView(imageView);
        // 单击缩略图则显示对应的照片
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int w = pictureView.getWidth();
                int h = pictureView.getHeight();
                Bitmap picture = CommonUtils.decodeBitmapFromFile(path, w, h);
                pictureView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                pictureView.setImageBitmap(picture);
            }
        });
        return layout;
    }

    // 根据手机的分辨率从 dp 的单位 转成为 px(像素)
    public int dip2px(float dip) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

}
