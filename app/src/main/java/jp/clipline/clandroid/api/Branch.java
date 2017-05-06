package jp.clipline.clandroid.api;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import jp.clipline.clandroid.BuildConfig;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Branch {

    private final static String TAG = "clwebwrapperapplication";

    private final static String SIGN_IN_URL = "%s://%s/v2/api/v1/branches/sign_in";
    private final static String SIGN_OUT_URL = "%s://%s/v2/api/v1/branches/sign_out";
    private final static String SIGN_IN_WITH_IDFV_URL = "%s://%s/v2/api/v1/branches/sign_in_with_idfv";

    public static String signIn(String branchId, String serviceId, String password) throws IOException {

        String cookie = null;

        RequestBody requestBody = new FormBody.Builder()
                .add("branch_id", branchId)
                .add("service_id", serviceId)
                .add("password", password)
                .build();

        ///// 20170505 ADD START
        String language = Locale.getDefault().toString();
        ///// 20170505 ADD END

        ///// 20170505 MODIFY START
        Request request = new Request.Builder()
                .url(String.format(SIGN_IN_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST))
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Language", language)
                .build();
        ///// 20170505 MODIFY END

        Response response = new OkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            Log.d(TAG, String.format("Sign In : Cookie = %s", response.headers().get("Set-Cookie")));
            cookie = response.headers().get("Set-Cookie");
        } else {
            Log.d(TAG, String.format("Sign In : failed"));
            throw new IOException("Sign In : failed");
        }
        response.close();

        return cookie;
    }

    public static void signOut(String cookie) throws IOException {
        Request request = new Request.Builder()
                .url((String.format(SIGN_OUT_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST)))
                .addHeader("Cookie", cookie)
                .delete()
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        response.close();
    }

    public static String signInWithIdfv(String branchId, String serviceId, String password, String deviceId) throws IOException {

        String message = null;

        // @FIXME : AndroidIDは、フォーマットとして通過できない為暫定処理
        deviceId = "b8925c97db4db2f8";
        deviceId = "156A3A67-D0FB-41A7-B1C5-1BDFE743F595";

        RequestBody requestBody = new FormBody.Builder()
                .add("branch_id", branchId)
                .add("service_id", serviceId)
                .add("password", password)
                .add("device_id", deviceId)
                .add("device_type", "android")
                .build();

        Request request = new Request.Builder()
                .url(String.format(SIGN_IN_WITH_IDFV_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST))
                .post(requestBody)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            Gson gson = new Gson();
            String body = response.body().string();
            HashMap<String, Object> fields = gson.fromJson(body, HashMap.class);
            if (((Boolean) fields.get("success")).booleanValue()) {
                response.close();
                return null;
            } else {
                message = (String) fields.get("message");
                response.close();
                return message;
            }
        } else {
            throw new IOException("Sign In : failed");
        }
    }

}