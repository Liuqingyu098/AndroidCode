package com.example.mapphotos.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class CommonUtils {
    public static final String PICTURE_PATH; // 照片保存路径
    public static final String THUMB_PATH; // 缩略图保存路径

    /**
     * static 代码块，它在本类首次加载时执行一次
     */
    static {
        // 获取系统外置存储的目录，即 SD 卡的路径
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        PICTURE_PATH = sdPath + "/MapPhotos/Picture/";
        THUMB_PATH = PICTURE_PATH + ".thumb/";
    }

    /**
     * 生成一个以当前时间命名的照片文件名字符串
     * @return
     */

    public static String getPictureNameByNowTime() {
        String filename = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        Date now = new Date();
        filename = simpleDateFormat.format(now) + ".jpg";
        return filename;
    }

    /**
     * 保存照片文件，并返回最终生成的照片文件完整路径
     * @param context
     * @param bitmap
     * @param path
     * @return
     */
    public static String savePicture(Context context, Bitmap bitmap, String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        String filename = getPictureNameByNowTime();
        String completePath = path + filename;
        // 调用 compress() 方法将图像压缩为 JPEG 格式保存到文件
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(completePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 ,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return completePath;
    }

    /**
     * 解码照片文件，返回指定尺寸的 Bitmap 对象
     * @param absolutePath
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeBitmapFromFile(String absolutePath, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        // 获取指定照片的分辨率大小
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absolutePath, options);
        // 计算采样倍率
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // 按照指定倍率对照片进行解码，解码后即得到指定大小的Bitmap 对象
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(absolutePath, options);
        return bitmap;
    }

    /**
     * 计算解码尺寸倍率， 结果是 1 则为原始图像大小，2 则为原始图像的二分之一，依此类推
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 图片原始尺寸
        final float height = options.outHeight;
        final float width = options.outWidth;
        // 假定默认采样比例是1，即原图像大小
        int inSampleSize = 1;
        // 根据宽高比例计算期望的倍率，四舍五入取整
        if (height > reqHeight || width > reqHeight) {
            // 将较小的值，与期望的宽和高比较计算，以保证缩放后的图像有正常的宽高比例
            if (width < height) {
                // Math.round() 是四舍五入处理
                inSampleSize = Math.round(width/reqHeight);
            } else {
                inSampleSize = Math.round(height/reqHeight);
            }
        }
        return inSampleSize;
    }

    /**
     * 将图像文件解码为 128x128 的尺寸的Bitmap 对象。得到的
     * 图像大小不一定正好是 128x128 尺寸，但宽和高均不超过 128
     * @param path
     * @param filename
     * @return
     */
    public static Bitmap getPicture128(String path, String filename) {
        String imageFile = path + filename;
        return decodeBitmapFromFile(imageFile,128,128);
    }
    public static Bitmap getPicture128(String absolutePath) {
        return decodeBitmapFromFile(absolutePath,128,128);
    }

    /**
     * 将图像文件解码为 64x64 的尺寸的Bitmap 对象。得到的
     * 图像大小不一定正好是 64x64 尺寸，但宽和高均不超过 64
     * @param path
     * @param filename
     * @return
     */
    public static Bitmap getPicture64(String path, String filename) {
        String imageFile = path + filename;
        return decodeBitmapFromFile(imageFile,64,64);
    }
    public static Bitmap getPicture64(String absolutePath) {
        return decodeBitmapFromFile(absolutePath,64,64);
    }

    public static boolean pictureIsExists(String pictureName) {
        String absolutePath = PICTURE_PATH + pictureName;
        File file = new File(absolutePath);
        if (!file.exists())
            return false;
        return true;
    }

    public static void deletePicture(String pictureName) {
        String absolutePath = PICTURE_PATH + pictureName;
        File file = new File(absolutePath);
        if (file.exists())
            file.delete();
        // 用于更新系统媒体库，防止有残留
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            new MediaScanner(PreviewActivity.this, path);
//        } else {
//            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
//        }
    }
}
