package com.example.mapphotos;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private final SurfaceHolder surfaceHolder;
    private Camera camera;

    public CameraPreview(Context context) {
        super(context);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //surface第一次创建时回调
        //打开相机
//        camera = Camera.open();
//        try {
//            camera.setPreviewDisplay(holder);
//            camera.startPreview();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            //设置预览方向
            camera.setDisplayOrientation(90);
            //把这个预览效果展示在SurfaceView上面
            camera.setPreviewDisplay(holder);
            //开启预览效果
            camera.startPreview();
        } catch (IOException e) {
//            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // surface销毁的时候回调
        surfaceHolder.removeCallback(this);
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}
