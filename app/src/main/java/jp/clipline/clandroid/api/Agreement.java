package jp.clipline.clandroid.api;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import jp.clipline.clandroid.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Agreement {
    private final static String TAG = "clwebwrapperapplication";

    private final static String SHOW_URL = "%s://%s/v2/api/v1/agreement.json";

    public static String get() throws IOException {
        String contentBody = null;

        String language = Locale.getDefault().toString();

        Request request = new Request.Builder()
                .url(String.format(SHOW_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Language", language)
                .get()
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            System.out.println("postJSONRequest response.body : " + response.body().string());
            Gson gson = new Gson();
            HashMap<String, Object> fields = gson.fromJson(response.body().string(), HashMap.class);
            contentBody = (String) fields.get("body");
        }
        response.close();

        return contentBody;
    }

}