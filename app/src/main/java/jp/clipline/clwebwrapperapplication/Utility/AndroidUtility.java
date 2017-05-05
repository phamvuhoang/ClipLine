package jp.clipline.clwebwrapperapplication.Utility;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

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
}
