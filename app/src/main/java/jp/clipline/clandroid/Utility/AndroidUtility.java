package jp.clipline.clandroid.Utility;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AndroidUtility {

    public static String getAndroidId(ContentResolver contentResolver) {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    }

    public static Map<String, String> getLoginSetting(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginSetting", context.MODE_PRIVATE);
        if (null == sharedPreferences.getString("serviceId", null)) {
            return null;
        }
        Map<String, String> loginSetting = new HashMap<String, String>();
        loginSetting.put("serviceId", sharedPreferences.getString("serviceId", null));
        loginSetting.put("branchId", sharedPreferences.getString("branchId", null));
        loginSetting.put("password", sharedPreferences.getString("password", null));
        return loginSetting;
    }

    public static void setLoginSetting(Context context, String branchId, String serviceId, String password) {
        SharedPreferences.Editor editor = context.getSharedPreferences("LoginSetting", context.MODE_PRIVATE).edit();
        editor.putString("serviceId", serviceId);
        editor.putString("branchId", branchId);
        editor.putString("password", password);
        editor.apply();
    }

    public static boolean isTerminalFirstUseScreenDisplayed(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("Device", context.MODE_PRIVATE);
        return !sharedPreferences.getBoolean("terminalFirstUseScreenDisplayed", false);
    }

    public static void setTerminalFirstUseScreenDisplayed(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences("Device", context.MODE_PRIVATE).edit();
        editor.putBoolean("terminalFirstUseScreenDisplayed", true);
        editor.apply();
    }

    public static String getCookie(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("LoginSetting", context.MODE_PRIVATE);
        return sharedPreferences.getString("cookie", null);
    }

    public static void setCookie(Context context, String cookie) {
        SharedPreferences.Editor editor = context.getSharedPreferences("LoginSetting", context.MODE_PRIVATE).edit();
        editor.putString("cookie", cookie);
        editor.apply();
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

    @SuppressLint("NewApi")
    public static String getFilePath(Context context, Uri uri) throws URISyntaxException {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    ///// 20170514 ADD START
    /**
     * check string is null or blank
     *
     * @param value
     * @return
     */
    public static boolean isNullOrBlank(String value) {
        return (value == null) || (value.equals("null")) || (value.equals(""));
    }
    ///// 20170514 ADD END
}
