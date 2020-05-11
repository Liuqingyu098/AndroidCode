//package com.example.mapphotos;
//
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.load.engine.DiskCacheStrategy;
//import com.bumptech.glide.request.RequestOptions;
//import com.example.mapphotos.bean.Picture;
//import com.example.mapphotos.util.CommonUtils;
//import com.ibbhub.album.AlbumFragment;
//import com.ibbhub.album.ITaDecoration;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//public class MyAlbumFragment extends AlbumFragment {
//
//    private DBOpenHelper dbOpenHelper;
//    private List<Picture> pictures;
//    private static int albumId;
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_album,container,false);
//        dbOpenHelper = new DBOpenHelper(getActivity());
//        albumId = getArguments().getInt("album_id",-1);
//        return view;
//    }
//
//    @Override
//    public List<File> buildAlbumSrc() {
//        List<File> fileList = new ArrayList<>();
////        if (albumId == -1){
////            pictures = dbOpenHelper.getAllPicture();
////        } else {
////            pictures = dbOpenHelper.getPictureByAlbumId(albumId);
////        }
////        for (int i = 0; i < pictures.size(); i++) {
////            String path = CommonUtils.PICTURE_PATH + pictures.get(i).getPictureName();
////            fileList.add(new File(path));
////        }
////        String path = Environment.getExternalStorageDirectory().getAbsolutePath()
////                + "/DCIM/Camera";
//        String path = CommonUtils.PICTURE_PATH;
//        fileList.add(new File(path));
//        return fileList;
//    }
//
//    @Override
//    public ITaDecoration buildDecoration() {
//        return null;
//    }
//
//    @Override
//    public String fileProviderName() {
//        return null;
//    }
//
//    @Override
//    public void loadOverrideImage(String path, ImageView iv) {
//        Glide.with(iv)
//                .load(path)
//                .thumbnail(0.1f)
//                .apply(buildOptions())
//                .into(iv);
//    }
//
//    @Override
//    public void loadImage(String path, ImageView iv) {
//        Glide.with(iv)
//                .load(path)
//                .thumbnail(0.1f)
//                .into(iv);
//    }
//
//    @Override
//    public void onChooseModeChange(boolean isChoose) {
//        onChooseModeChange(isChoose);
//    }
//
//    public static RequestOptions buildOptions() {
//        RequestOptions requestOptions = new RequestOptions();
//        requestOptions.override(100, 100);
//        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL);
//        return requestOptions;
//    }
//}
