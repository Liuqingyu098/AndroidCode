package com.example.mapphotos;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.listener.ClickListener;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;
import com.cjt2325.cameralibrary.CaptureLayout;
import com.cjt2325.cameralibrary.listener.TypeListener;
import com.cjt2325.cameralibrary.state.CameraMachine;
import com.example.mapphotos.bean.Picture;
import com.example.mapphotos.util.CommonUtils;

import java.io.File;

public class JCameraViewActivity extends Activity {
    private JCameraView jCameraView;
    private DBOpenHelper dbOpenHelper;
    private Picture picture;
    private CaptureLayout captureLayout;
    private Context context;
    private TextView txt_tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jcamera_view);
        dbOpenHelper = new DBOpenHelper(this);
        picture = new Picture();
        picture.setLatitude(getIntent().getDoubleExtra("latitude",-1));
        picture.setLongitude(getIntent().getDoubleExtra("longitude",-1));
        picture.setAlbumId(getIntent().getIntExtra("album_id",-1));
        jCameraView = (JCameraView) findViewById(R.id.jCameraView);
        captureLayout = (CaptureLayout) findViewById(R.id.capture_layout);
//        txt_tip = new TextView(this);
//        FrameLayout.LayoutParams txt_param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//        txt_param.gravity = Gravity.CENTER_HORIZONTAL;
//        txt_param.setMargins(0, 0, 0, 0);
//        txt_tip.setText("轻触拍照");
//        txt_tip.setTextColor(0xFFFFFFFF);
//        txt_tip.setGravity(Gravity.CENTER);
//        txt_tip.setLayoutParams(txt_param);
//        captureLayout.addView(txt_tip);
        // 设置视频保存路径
        // jCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");

        // 设置只能录像或只能拍照或两种都可以（默认只能拍照）
        jCameraView.setFeatures(JCameraView.BUTTON_STATE_ONLY_CAPTURE);
        // 设置视频质量
        // jCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);

        // JCameraView监听
        jCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //打开Camera失败回调
                Log.i("CJT", "open camera error");
            }

            @Override
            public void AudioPermissionError() {

            }
        });

        jCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                // 获取图片 bitmap 并保存
                String absolutePath = CommonUtils.savePicture(getApplicationContext(), bitmap, CommonUtils.PICTURE_PATH);
                Bitmap thumb64 = CommonUtils.getPicture64(absolutePath);
                // 保存缩略图文件到 SD 卡
                String thumb64Path = CommonUtils.savePicture(getApplicationContext(), thumb64, CommonUtils.THUMB_PATH);
                // 获得照片，缩略图的文件名（不含所在的目录名）
                String pictureName = new File(absolutePath).getName();
                String thumb64Name = new File(thumb64Path).getName();
                // 保存照片数据到数据库
                dbOpenHelper.addPicture(picture.getLatitude(), picture.getLongitude(), pictureName, thumb64Name, picture.getAlbumId());
                // 修改相册条目的缩略图为最近一次拍照的缩略图
                dbOpenHelper.updateAlbumThumb(picture.getAlbumId(), thumb64Name);
                Toast.makeText(JCameraViewActivity.this,"已保存至MapPhotos/Pictures/"+pictureName, Toast.LENGTH_SHORT).show();
                onResume();
            }
            @Override
            public void recordSuccess(String url,Bitmap firstFrame) {
                // 获取视频路径
//                Log.i("CJT", "url = " + url);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        jCameraView.onResume();
    }

    @Override
    protected void onPause() {
        jCameraView.onPause();
        super.onPause();
    }

//    @Override
//    public void onBackPressed() {
//        // 按<back>键时返回给前一个 Activity 的结果码
//        setResult(MainActivity.MAPVIEW_BACK);
//        finish();
//    }
}
