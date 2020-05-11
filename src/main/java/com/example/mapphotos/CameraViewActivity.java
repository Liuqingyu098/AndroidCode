package com.example.mapphotos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.location.Location;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.mapphotos.util.CommonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraViewActivity extends Activity implements SurfaceHolder.Callback{
    private Button backButton;
    private Button reverseButton;
    private Button takePictureButton;
    private TextView blankTextView;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private SurfaceHolder surfaceHolder;
    private Bitmap picture;
    private DBOpenHelper dbOpenHelper;

    private static double myLatitude;
    private static double myLongitude;
    private static int albumId;

    private final int CAMERA_FRONT = 0;// 前置摄像头标记
    private final int CAMERA_BACK = 1;// 后置摄像头标记
    private int currentCameraType = -1;// 当前打开的摄像头标记

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        dbOpenHelper = new DBOpenHelper(this);
        try {
            camera = openCamera(CAMERA_BACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = getIntent();
        myLatitude = intent.getDoubleExtra("latitude",-1);
        myLongitude = intent.getDoubleExtra("longitude",-1);
        albumId = intent.getIntExtra("album_id",-1);
        backButton = (Button) findViewById(R.id.buttonBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        reverseButton = (Button) findViewById(R.id.buttonReverse);
        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    reverseCamera();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        takePictureButton = (Button) findViewById(R.id.buttonTakePicture);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (!success) {
                            Toast.makeText(getApplicationContext(), "警告，相机无法聚焦!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // 自动聚焦成功则执行拍照处理
                        camera.takePicture(null, null, new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] data, Camera camera) {
                                if (picture != null && !picture.isRecycled()) {
                                    // 释放图片内存
                                    picture.recycle();
                                }
                                // 暂停相机预览
                                camera.stopPreview();
                                // 将相机取景预览画面解码为图像数据
                                picture = BitmapFactory.decodeByteArray(data, 0, data.length);
                                // 竖屏预览时旋转了90 度，故照片需往回旋转90 度
                                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                                    // 构造旋转矩阵，主要用在图像处理中
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(90);// 设置矩阵旋转90 度
                                    int w = picture.getWidth();
                                    int h = picture.getHeight();
                                    // 将照片旋转90 度
                                    try {
                                        Bitmap bitmap = Bitmap.createBitmap(picture, 0, 0, w, h, matrix, true);
                                        picture.recycle();
                                        picture = bitmap;
                                    } catch (OutOfMemoryError o) {
                                        // 旋转照片失败
                                    }
                                } else {
                                    // 相机拍照默认是横屏，不做任何处理
                                }
                                if (picture != null) {
                                    // 处理拍照内容，比如保存为照片文件

                                    // 保存照片文件到 SD 卡
                                    String picturePath = CommonUtils.savePicture(getApplicationContext(), picture, CommonUtils.PICTURE_PATH);
                                    Bitmap thumb64 = CommonUtils.getPicture64(picturePath);
                                    // 保存缩略图文件到 SD 卡
                                    String thumb64Path = CommonUtils.savePicture(getApplicationContext(), thumb64, CommonUtils.THUMB_PATH);
                                    // 获得照片，缩略图的文件名（不含所在的目录名）
                                    String pictureName = new File(picturePath).getName();
                                    String thumb64Name = new File(thumb64Path).getName();
                                    // 保存照片数据到数据库
                                    dbOpenHelper.addPicture(myLatitude, myLongitude, pictureName, thumb64Name, albumId);
                                    // 修改相册条目的缩略图为最近一次拍照的缩略图
                                    dbOpenHelper.updateAlbumThumb(albumId, thumb64Name);
                                    // 释放照片内存
                                    picture.recycle();
                                    picture = null;
                                    Toast.makeText(getApplicationContext(), "[ 已拍照]", Toast.LENGTH_SHORT).show();
                                }
                                // 拍照结束继续预览
                                camera.startPreview();
                            }
                        });
                    }
                });
            }
        });
        blankTextView = (TextView)findViewById(R.id.textViewBlankInTop);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        // 创建 SurfaceHolder 对象
        surfaceHolder = surfaceView.getHolder();
        // 注册回调监听器
        surfaceHolder.addCallback(this);
        // 设置 SurfaceHolder 的类型
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        try {
            //设置预览方向
            camera.setDisplayOrientation(90);
            //把这个预览效果展示在SurfaceView上面
            camera.setPreviewDisplay(holder);
//            setPreviewSize(camera, 0 ,0);
            //开启预览效果
            camera.startPreview();
        } catch (IOException e) {
//            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() == null) {
            return;
        }
        //停止预览效果
        camera.stopPreview();
        surfaceHolder = holder;
        // 指定相机参数：图片分辨率、横竖屏切换、自动聚焦
        Camera.Parameters parameters = camera.getParameters();
        final List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                // 到排序，确保大的分辨率在前
                return o2.width-o1.width;
            }
        });
        for (Camera.Size size : sizes) {
            // 拍照分辨率不能过大，否则容易造成 OutOfMemoryError
            if (size.width <= 1600) {
                parameters.setPictureSize(size.width, size.height);
                break;
            }
        }
        // 横竖屏镜头自动调整
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
            // 设置为竖屏方向
            parameters.set("orientation", "portrait");
            camera.setDisplayOrientation(90);
        } else {
            // 设置为横屏方向
            parameters.set("orientation", "landscape");
            camera.setDisplayOrientation(0);
        }
        // 设置相机为自动聚焦模式
        List<String> foucsModes = parameters.getSupportedFlashModes();
        if (foucsModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        // 使设置参数生效
        camera.setParameters(parameters);
        // 设置相机取景预览的缓冲区内存，取决于预览画面的宽，高和每像素占用的字节数
        int imgformat = parameters.getPreviewFormat();
        // !!!!!!!!!!!!!!!!ImageFormat可能是错误的!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1
        int bitsperpixel = ImageFormat.getBitsPerPixel(imgformat);
        Camera.Size camerasize = parameters.getPreviewSize();
        int frame_size = ((camerasize.width * camerasize.height) * bitsperpixel) /8;
        byte[] frame = new byte[frame_size];
        // 设置取景预览时，将取景画面的图像数据保存到frame数据缓冲区中
        camera.addCallbackBuffer(frame);
        // 启动相机取景预览
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // surface销毁的时候回调
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            // 准备将下一帧预览画面图像保存到data缓冲区中，也即前面的frame字节数组
            camera.addCallbackBuffer(data);
        }
    };

    @SuppressLint("NewApi")
    private Camera openCamera(int type){
        int frontIndex =-1;
        int backIndex = -1;
        int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for(int cameraIndex = 0; cameraIndex<cameraCount; cameraIndex++){
            Camera.getCameraInfo(cameraIndex, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                frontIndex = cameraIndex;
            }else if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK){
                backIndex = cameraIndex;
            }
        }

        currentCameraType = type;
        if(type == CAMERA_FRONT && frontIndex != -1){
            return Camera.open(frontIndex);
        }else if(type == CAMERA_BACK && backIndex != -1){
            return Camera.open(backIndex);
        }
        return null;
    }

    private void reverseCamera() throws IOException{
        camera.stopPreview();
        camera.release();
        if (currentCameraType == CAMERA_FRONT) {
            camera = openCamera(CAMERA_BACK);
        } else {
            camera = openCamera(CAMERA_FRONT);
        }
        camera.setPreviewDisplay(surfaceView.getHolder());
        camera.startPreview();
    }

//    private void setPictureSize(Camera camera ,int expectWidth,int expectHeight){
//        Camera.Parameters parameters = camera.getParameters();
//        Point point = new Point(expectWidth, expectHeight);
//        Camera.Size size = findProperSize(point,parameters.getSupportedPreviewSizes());
//        parameters.setPictureSize(size.width, size.height);
//        camera.setParameters(parameters);
//    }

    private Camera.Size setPreviewSize(Camera camera, int expectWidth, int expectHeight) {
        Camera.Parameters parameters = camera.getParameters();
        Point point = new Point(expectWidth, expectHeight);
        Camera.Size size = findProperSize(point,parameters.getSupportedPictureSizes());
        parameters.setPictureSize(size.width, size.height);
        camera.setParameters(parameters);
        return size;
    }

    /**
     * 找出最合适的尺寸，规则如下：
     * 1.将尺寸按比例分组，找出比例最接近屏幕比例的尺寸组
     * 2.在比例最接近的尺寸组中找出最接近屏幕尺寸且大于屏幕尺寸的尺寸
     * 3.如果没有找到，则忽略2中第二个条件再找一遍，应该是最合适的尺寸了
     */
    private static Camera.Size findProperSize(Point surfaceSize, List<Camera.Size> sizeList) {
        if (surfaceSize.x <= 0 || surfaceSize.y <= 0 || sizeList == null) {
            return null;
        }

        int surfaceWidth = surfaceSize.x;
        int surfaceHeight = surfaceSize.y;

        List<List<Camera.Size>> ratioListList = new ArrayList<>();
        for (Camera.Size size : sizeList) {
            addRatioList(ratioListList, size);
        }

        final float surfaceRatio = (float) surfaceWidth / surfaceHeight;
        List<Camera.Size> bestRatioList = null;
        float ratioDiff = Float.MAX_VALUE;
        for (List<Camera.Size> ratioList : ratioListList) {
            float ratio = (float) ratioList.get(0).width / ratioList.get(0).height;
            float newRatioDiff = Math.abs(ratio - surfaceRatio);
            if (newRatioDiff < ratioDiff) {
                bestRatioList = ratioList;
                ratioDiff = newRatioDiff;
            }
        }

        Camera.Size bestSize = null;
        int diff = Integer.MAX_VALUE;
        assert bestRatioList != null;
        for (Camera.Size size : bestRatioList) {
            int newDiff = Math.abs(size.width - surfaceWidth) + Math.abs(size.height - surfaceHeight);
            if (size.height >= surfaceHeight && newDiff < diff) {
                bestSize = size;
                diff = newDiff;
            }
        }

        if (bestSize != null) {
            return bestSize;
        }

        diff = Integer.MAX_VALUE;
        for (Camera.Size size : bestRatioList) {
            int newDiff = Math.abs(size.width - surfaceWidth) + Math.abs(size.height - surfaceHeight);
            if (newDiff < diff) {
                bestSize = size;
                diff = newDiff;
            }
        }

        return bestSize;
    }

    private static void addRatioList(List<List<Camera.Size>> ratioListList, Camera.Size size) {
        float ratio = (float) size.width / size.height;
        for (List<Camera.Size> ratioList : ratioListList) {
            float mine = (float) ratioList.get(0).width / ratioList.get(0).height;
            if (ratio == mine) {
                ratioList.add(size);
                return;
            }
        }

        List<Camera.Size> ratioList = new ArrayList<>();
        ratioList.add(size);
        ratioListList.add(ratioList);
    }
}
//    public void requestPower() {
////判断是否已经赋予权限
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.上表权限字符)
//                != PackageManager.PERMISSION_GRANTED) {
//            //如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.上表权限字符)) {//这里可以写个对话框之类的项向用户解释为什么要申请权限，并在对话框的确认键后续再次申请权限.它在用户选择"不再询问"的情况下返回false
//            } else {
//                //申请权限，字符串数组内是一个或多个要申请的权限，1是申请权限结果的返回参数，在onRequestPermissionsResult可以得知申请结果
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.上表权限字符,}, 1);
//            }
//        }
//    }

//解决预览显示问题
//这种做法跟系统相机类似，首先先确定预览分辨率是多少，代码方法1有写，比如这里选择了第一个19201080的分辨率，屏幕分辨率是22801080，设surfaceview的宽为match_parent及1080，那么为了使其不变形，高度必须为1920.
//        高度的计算公式为
//        surfacview高 = 预览宽*屏幕高/屏幕宽
//        代码设置surfaceview的高
//        RelativeLayout.LayoutParams lp = new
//        RelativeLayout.LayoutParams(surfaceView.getLayoutParams());
//        lp.height = surfaceview高
//        surfaceView.setLayoutParams(lp);


