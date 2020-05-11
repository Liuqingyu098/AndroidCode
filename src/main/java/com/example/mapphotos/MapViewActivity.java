package com.example.mapphotos;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.mapphotos.bean.Picture;
import com.example.mapphotos.bean.RowInfoBean;
import com.example.mapphotos.util.CommonUtils;

import java.util.ArrayList;
import java.util.List;


public class MapViewActivity extends Activity {
    public LocationClient mLocationClient = null;
    public MyLocationListener myListener = new MyLocationListener();
    private MapView mMapView=null;
    private Button cameraButton;
    private List<String> permissionList = new ArrayList<>();
    public BaiduMap bMap;
    private DBOpenHelper dbOpenHelper;
    private ArrayList<Picture> picture;
    private static Picture pictureInfo;
    private MyLocationData data;
    public boolean firstOpen=true;
    private static RowInfoBean bean;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
        // 获取从 MainActivity 传递过来的相册信息
        bean = new RowInfoBean();
        pictureInfo = new Picture();
        bean.setId(getIntent().getIntExtra("album_id", -1));
        bean.setTitle(getIntent().getStringExtra("album_title"));
        dbOpenHelper = new DBOpenHelper(this);
        picture = dbOpenHelper.getMaxIdPictureByAlbumId(bean.getId());
        mMapView = (MapView)findViewById(R.id.bmapView);
        // 删除百度地图 Logo
        mMapView.removeViewAt(1);
        bMap = mMapView.getMap();
        showPictureInMap(picture);
        cameraButton = findViewById(R.id.camera);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkCamera()) {
                    Toast.makeText(getApplicationContext(), "相机无法打开", Toast.LENGTH_SHORT).show();
                } else {
                    if (ContextCompat.checkSelfPermission(MapViewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        permissionList.add(Manifest.permission.CAMERA);
                    }
                    Intent intent = new Intent(getApplicationContext(), JCameraViewActivity.class);
                    intent.putExtra("latitude", pictureInfo.getLatitude());
                    intent.putExtra("longitude", pictureInfo.getLongitude());
                    intent.putExtra("album_id", bean.getId());
                    startActivity(intent);
                }
            }
        });
        // 定位开启
        bMap.setMyLocationEnabled(true);
        // 定位初始化

        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);
        // 注册监听函数
        // 动态申请权限
        if(ContextCompat.checkSelfPermission(MapViewActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MapViewActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MapViewActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String[] permissions =permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MapViewActivity.this, permissions, 1);
        }
        else{
            initLocationOption();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(new Intent(getApplicationContext(), Service.class));
        } else {
            getApplicationContext().startService(new Intent(getApplicationContext(), Service.class));
        }
        // 调用LocationClient的start()方法，便可发起定位请求
        mLocationClient.start();


    // mLocationClient为第二步初始化过的LocationClient对象

    }

    private void initLocationOption() {
        // 定位服务的客户端。宿主程序在客户端声明此类，并调用，目前只支持在主线程中启动
        LocationClient locationClient = new LocationClient(getApplicationContext());
        // 声明LocationClient类实例并配置定位参数
        LocationClientOption locationOption = new LocationClientOption();
        MyLocationListener myLocationListener = new MyLocationListener();
        // 注册监听函数
        locationClient.registerLocationListener(myLocationListener);
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        locationOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
        locationOption.setCoorType("bd09ll");
        // 可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        locationOption.setScanSpan(1000);
        // 可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
        // 可选，设置是否需要地址描述
        locationOption.setIsNeedLocationDescribe(true);
        // 可选，设置是否需要设备方向结果
        locationOption.setNeedDeviceDirect(false);
        // 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        locationOption.setLocationNotify(true);
        // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        locationOption.setIgnoreKillProcess(true);
        // 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        locationOption.setIsNeedLocationDescribe(true);
        // 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        locationOption.setIsNeedLocationPoiList(true);
        // 可选，默认false，设置是否收集CRASH信息，默认收集
        locationOption.SetIgnoreCacheException(false);
        // 可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
        // 可选，默认false，设置定位时是否需要海拔信息，默认不需要，除基础定位版本都可用
        locationOption.setIsNeedAltitude(false);
        // 设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者，该模式下开发者无需再关心定位间隔是多少，定位SDK本身发现位置变化就会及时回调给开发者
        locationOption.setOpenAutoNotifyMode();
        // 设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
        locationOption.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        locationOption.setEnableSimulateGps(false);
        // 可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        // 需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        locationClient.setLocOption(locationOption);

        // 开始定位
        // 配置定位地图现实的方式 现在是罗盘形式展示
//        bMap.setMyLocationConfiguration(new MyLocationConfiguration(com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.COMPASS,true,null));
        if (!locationClient.isStarted())
            locationClient.start();
    }



    class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // 此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果
            // 以下只列举部分获取经纬度相关（常用）的结果信息
            // 更多结果信息获取说明，请参照类参考中BDLocation类中的说明

            if (mMapView == null || location == null) {
                return;
            }

            // 定位数据
            data = new MyLocationData.Builder().accuracy(location.getRadius())
                    .direction(50)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            // 把定位的数据加到百度地图上
            pictureInfo.setLongitude(location.getLongitude());
            pictureInfo.setLatitude(location.getLatitude());
            bMap.setMyLocationData(data);
            if (firstOpen) {
                firstOpen = false;
                // 地图的状态
                MapStatus.Builder status = new MapStatus.Builder();
                LatLng ll;
                if (picture.size() != 0) {
                    ll = new LatLng(picture.get(0).getLatitude(),
                            picture.get(0).getLongitude());
                } else {
                    ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                }
                // 设置缩放的等级和中心点(中心点为第一行数据的位置）
                status.zoom(17.0f).target(ll);
                // 地图改变的是的状态的动画
                bMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(status.build()));

            }


        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    private boolean checkCamera() {
        return this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    // 在地图上显示照片缩略图地标
    public void markInMap(double latitude, double longitude, Bitmap thumb64) {
        //定义Maker坐标点
        LatLng point = new LatLng(latitude, longitude);
        // 构建Marker图标
        final BitmapDescriptor bitmap = BitmapDescriptorFactory.fromBitmap(thumb64);
        if (bitmap != null) {
            // 构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap);
            // 在地图上添加Marker，并显示
            bMap.addOverlay(option);
            bMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Intent intent = new Intent(getApplicationContext(), AlbumActivity.class);
                    intent.putExtra("album_id", bean.getId());
                    intent.putExtra("album_title", bean.getTitle());
                    startActivity(intent);
                    return false;
                }
            });
        }
    }

    public void showPictureInMap(ArrayList<Picture> picture) {
        if (picture.size() != 0) {
            // 将选择相册中的图片显示到地图上
//            for (int i = 0; i<picture.size(); i++) {
//                Bitmap thumb64 = CommonUtils.getPicture64(CommonUtils.PICTURE_PATH + picture.get(i).getPictureName());
//                markInMap(picture.get(i).getLatitude(), picture.get(i).getLongitude(), thumb64);
//            }
            Bitmap thumb64 = CommonUtils.getPicture64(CommonUtils.PICTURE_PATH + picture.get(0).getPictureName());
            markInMap(picture.get(0).getLatitude(), picture.get(0).getLongitude(), thumb64);
        }
    }

    @Override
    public void onBackPressed() {
        // 按<back>键时返回给前一个 Activity 的结果码
        setResult(MainActivity.BACK);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case MainActivity.BACK:
                // 返回此界面时重新加载一次数据
                picture.clear();
                showPictureInMap(picture);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}