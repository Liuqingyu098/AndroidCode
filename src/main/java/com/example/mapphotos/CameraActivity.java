package com.example.mapphotos;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.cjt2325.cameralibrary.listener.CaptureListener;
import com.cjt2325.cameralibrary.listener.ClickListener;
//import com.example.mapphotos.CaptureLayout;
import com.cjt2325.cameralibrary.CaptureLayout;
import com.cjt2325.cameralibrary.listener.TypeListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraView;

public class CameraActivity extends Activity {
    private CameraView cameraView;
//    private ImageView reverse;
    private ImageView previewImage;
    private RelativeLayout previewCamera;
    private CaptureLayout captureLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        cameraView = findViewById(R.id.camera);
        previewImage = (ImageView) findViewById(R.id.preview_image);
//        previewCamera = (RelativeLayout) findViewById(R.id.bottom_layout_camera);
//        reverse = (ImageView) findViewById(R.id.reverseCamera);
        captureLayout = findViewById(R.id.capture_layout);
//        reverse.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                cameraView.toggleFacing();
//            }
//        });

        captureLayout.setLeftClickListener(new ClickListener() {//返回事件回调
            @Override
            public void onClick() {
                finish();
            }
        });

        captureLayout.setRightClickListener(new ClickListener() {//确认事件回调
            @Override
            public void onClick() {
                Toast.makeText(CameraActivity.this,"已保存", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        captureLayout.setCaptureLisenter(new CaptureListener() {
            @Override
            public void takePictures() {
                Toast.makeText(CameraActivity.this,"已拍摄", Toast.LENGTH_SHORT).show();
                captureLayout.setIconSrc(R.drawable.preview_cancel, R.drawable.preview_save);
                captureLayout.startTypeBtnAnimator();
                captureLayout.startAlphaAnimation();
            }

            @Override
            public void recordShort(long time) {

            }

            @Override
            public void recordStart() {

            }

            @Override
            public void recordEnd(long time) {

            }

            @Override
            public void recordZoom(float zoom) {

            }

            @Override
            public void recordError() {

            }
        });

        captureLayout.setTypeLisenter(new TypeListener() {
            @Override
            public void cancel() {

            }

            @Override
            public void confirm() {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraView.stop();
    }

//    @Override
//    public void takePictures() {
//        cameraView.captureImage();
//    }

//    @Override
//    public void cancel() {
//        cameraView.start();
//        reverse.setVisibility(View.VISIBLE);
//        captureLayout.resetCaptureLayout();
//        previewImage.setVisibility(View.INVISIBLE);
//    }
//
//    @Override
//    public void confirm() {
////        DO_FLASH.setVisibility(View.VISIBLE);
////        DO_REVARSE.setVisibility(View.VISIBLE);
////        preview.setVisibility(View.GONE);
////        capture.resetCaptureLayout();
////        img.setVisibility(View.INVISIBLE);
////        video.setVisibility(View.INVISIBLE);
////        if(video!=null && video.isPlaying()){
////            video.pause();
////        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.N)
//    @Override
//    public void onImage(CameraKitImage cameraKitImage) {
//        previewImage.setVisibility(View.VISIBLE);
//        reverse.setVisibility(View.GONE);
////        if (cameraView.isFacingFront()) {
////            previewImage.setImageBitmap(loadBitmap(cameraKitImage.getJpeg());
////        } else {
////            previewImage.setImageBitmap(cameraKitImage.getBitmap());
////        }
//        previewImage.setImageBitmap(cameraKitImage.getBitmap());
//        cameraView.stop();
//    }


}
