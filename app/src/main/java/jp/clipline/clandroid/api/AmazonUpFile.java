package jp.clipline.clandroid.api;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.chromium.base.ContextUtils.getApplicationContext;

/**
 * Created by nguyentu on 6/8/17.
 */

public class AmazonUpFile {

    private final static String TAG = "clwebwrapperapplication";

    private final static String AMAZON_URL = "http://docs.aws.amazon.com/mobile/sdkforandroid/developerguide/s3transferutility.html#upload-an-object-to-s3-with-metadata";

    public static Map<String, Object> sendAmazonReport(Map<String, Object> mediaKey, String path) throws IOException, JSONException {

        Gson gson = new Gson();
        Map<String, Object> responseData = null;
        JSONObject param = new JSONObject();
        param.put("Bucket", String.valueOf(mediaKey.get("regbucketion")));
        Map<String, Object> credentials = gson.fromJson(String.valueOf(mediaKey.get("credentials")), Map.class);
        String language = Locale.getDefault().toString();
        String cookie = AndroidUtility.getCookie(getApplicationContext());
        RequestBody requestBody = new FormBody.Builder()
                .add("region", String.valueOf(mediaKey.get("region")))
                .add("accessKeyId", String.valueOf(credentials.get("access_key_id")))
                .add("sessionToken", String.valueOf(credentials.get("session_token")))
                .add("media_duration", String.valueOf(mediaKey.get("region")))
                .add("region", String.valueOf(mediaKey.get("region")))
                .add("param", param.toString())
                .build();

        RequestBody multipartBody = new MultipartBody.Builder()
                .addPart(requestBody)
                .addFormDataPart("my_file",path)
                .build();


        Request request = new Request.Builder()
                .url(AMAZON_URL)
                .post(multipartBody)
                .addHeader("Cookie", cookie)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Language", language)
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            String body = response.body().string();

            responseData = gson.fromJson(body, Map.class);
        } else {
            throw new IOException(String.format("Report#sendStudenReport : failed [%d]", response.code()));
        }

        response.close();

        return responseData;
    }
}
