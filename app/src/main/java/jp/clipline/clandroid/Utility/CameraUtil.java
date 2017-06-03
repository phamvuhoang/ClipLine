package jp.clipline.clandroid.Utility;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;

public class CameraUtil {

    public static final String TAG = "SimpleCamera";

    // これを行わないと、ギャラリーに反映されない
    public static void registVideoAndroidDB(String filename, Context context) {
        Log.v(TAG, "registVideoAndroidDB - Start");
        // Save the name and description of a video in a ContentValues map.
        final ContentValues values = new ContentValues(2);
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.DATA, filename);
        // Add a new record (identified by uri)
        // final Uri uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filename)));
        Log.v(TAG, "registVideoAndroidDB - End");
    }

    //メディアに登録作業が必要らしい。
    public static void registPictureAndroidDB(String filename, Context context) {
        Log.v(TAG, "registPictureAndroidDB - Start");

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filename);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);

        Log.v(TAG, "registPictureAndroidDB - End");
    }

    public static Uri addImageToGallery(ContentResolver cr, String imgType, File filepath) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "player");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "player");
        values.put(MediaStore.Images.Media.DESCRIPTION, "");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/" + imgType);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATA, filepath.toString());

        return cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static File getVideoStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), albumName);
        if (!file.mkdirs()) {
            Log.w(TAG, "Directory not created");
        }
        return file;
    }

    public static File getPhotoStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);
        if (!file.mkdirs()) {
            Log.w(TAG, "Directory not created");
        }
        return file;
    }

    public static String getVideoFilePath() {
        Log.v(TAG, "getVideoFilePath");
        return CameraUtil.getVideoStorageDir("SimpleCamera").toString() + "/CLIP_" + System.currentTimeMillis() + ".mp4";
    }

    public static String getPhotoFilePath() {
        Log.v(TAG, "getPhotoFilePath");
        return CameraUtil.getPhotoStorageDir("SimpleCamera").toString() + "/CLIP_" + System.currentTimeMillis() + ".JPEG";
    }

    // リアルな画面解像度を取得します
    // @see : http://stackoverflow.com/questions/27797549/how-to-get-nexus-7flo-real-screen-resolution-programmatically
    public static Point getRealScreenSize(Activity activity) {
        int width = 0;
        int height = 0;
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = activity.getWindowManager().getDefaultDisplay();
        Method mGetRawH = null, mGetRawW = null;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                width = metrics.widthPixels;
                height = metrics.heightPixels;
                Log.i("ZYStudio", "with and height:" + width + "|" + height);
            } else {
                mGetRawH = Display.class.getMethod("getRawHeight");
                mGetRawW = Display.class.getMethod("getRawWidth");
                Log.i("ZYStudio", "rawW and rawH:" + mGetRawW + "|" + mGetRawH);
                try {
                    width = (Integer) mGetRawW.invoke(display);
                    height = (Integer) mGetRawH.invoke(display);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        }

        return new Point(width, height);
    }

    public static ArrayList<String> getSettingPermissions(Context context) {
        ArrayList<String> list = new ArrayList<String>();
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null || packageInfo.requestedPermissions == null) return list;

        for (String permission : packageInfo.requestedPermissions) {
            list.add(permission);
        }
        return list;
    }

    public static boolean hasSelfPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT < 23) return true;
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static Comparator<Camera.Size> buildSizeComparative() {
        return new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size source, Camera.Size destination) {
                if (source.width == destination.width) {
                    return (source.height - destination.height);
                } else {
                    return (source.width - destination.width);
                }
            }
        };
    }

    // リアルな画面解像度を取得します
    // @see : http://stackoverflow.com/questions/27797549/how-to-get-nexus-7flo-real-screen-resolution-programmatically
    public static Point getRealScreenSize(WindowManager windowManager) {
        int width = 0;
        int height = 0;
        final DisplayMetrics metrics = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        Method mGetRawH = null, mGetRawW = null;

        try {
            // For JellyBean 4.2 (API 17) and onward
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics);

                width = metrics.widthPixels;
                height = metrics.heightPixels;
                Log.i("ZYStudio", "with and height:" + width + "|" + height);
            } else {
                mGetRawH = Display.class.getMethod("getRawHeight");
                mGetRawW = Display.class.getMethod("getRawWidth");
                Log.i("ZYStudio", "rawW and rawH:" + mGetRawW + "|" + mGetRawH);
                try {
                    width = (Integer) mGetRawW.invoke(display);
                    height = (Integer) mGetRawH.invoke(display);
                } catch (IllegalArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e3) {
            e3.printStackTrace();
        }

        return new Point(width, height);
    }

    // スタンダード（4:3）比率端末判定
    public static boolean isStandardAspectHardWare(WindowManager windowManager) {
        Point size = CameraUtil.getRealScreenSize(windowManager);
        BigDecimal width = new BigDecimal((size.x >= size.y) ? size.x : size.y);
        BigDecimal height = new BigDecimal((size.x >= size.y) ? size.y : size.x);
        BigDecimal standard_width = new BigDecimal(4);
        BigDecimal standard_height = new BigDecimal(3);

        return (width.divide(height, 2, BigDecimal.ROUND_HALF_UP).compareTo(standard_width.divide(standard_height, 2, BigDecimal.ROUND_HALF_UP)) == 0) ? true : false;
    }
}
