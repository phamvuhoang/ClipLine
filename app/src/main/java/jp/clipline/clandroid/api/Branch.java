package jp.clipline.clandroid.api;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import jp.clipline.clandroid.BuildConfig;
import jp.clipline.clandroid.Utility.AndroidUtility;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Branch {

    private final static String TAG = "clwebwrapperapplication";

    private final static String SIGN_IN_URL = "%s://%s/v2/api/v2/branches/sign_in";
    private final static String SIGN_OUT_URL = "%s://%s/v2/api/v1/branches/sign_out";
    private final static String SIGN_IN_WITH_IDFV_URL = "%s://%s/v2/api/v1/branches/sign_in_with_idfv";

    public static Object signInV2(String branchId, String serviceId, String password, String deviceId) throws IOException {
        String message = null;

        //deviceId = "156A3A67-D0FB-41A7-B1C5-1BDFE743F595";
        deviceId = AndroidUtility.formatDeviceID(deviceId);
        Log.d("deviceId", deviceId);
        RequestBody requestBody = new FormBody.Builder()
                .add("branch_id", branchId)
                .add("service_id", serviceId)
                .add("password", password)
                .add("device_id", deviceId)
                .add("device_type", "android")
                .add("request_access", "1")
                .build();

        Request request = new Request.Builder()
                .url(String.format(SIGN_IN_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST))
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response;
        } else {
            throw new IOException("Sign In : failed");
        }

    }

    public static Object signOut(String cookie) throws IOException {
        Request request = new Request.Builder()
                .url((String.format(SIGN_OUT_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST)))
                .addHeader("Cookie", cookie)
                .delete()
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        Object res = response;
        response.close();

        return res;
    }

    public static String signInWithIdfv(String branchId, String serviceId, String password, String deviceId) throws IOException {

        String message = null;

        // @FIXME : AndroidIDは、フォーマットとして通過できない為暫定処理
//        deviceId = "b8925c97db4db2f8";
        //deviceId = "156A3A67-D0FB-41A7-B1C5-1BDFE743F595";
        deviceId = AndroidUtility.formatDeviceID(deviceId);
        Log.d("deviceId", deviceId);
        RequestBody requestBody = new FormBody.Builder()
                .add("branch_id", branchId)
                .add("service_id", serviceId)
                .add("password", password)
                .add("device_id", deviceId)
                .add("device_type", "android")
                .add("request_access", "1")
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
            }
        } else {
            throw new IOException("Sign In : failed");
        }

        return null;
    }

}