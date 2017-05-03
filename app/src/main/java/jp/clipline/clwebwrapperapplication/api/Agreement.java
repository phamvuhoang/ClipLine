package jp.clipline.clwebwrapperapplication.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;

import jp.clipline.clwebwrapperapplication.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Agreement {
    private final static String TAG = "clwebwrapperapplication";

    private final static String SHOW_URL = "%s://%s/v2/api/v1/agreement.json";

    public static String get() throws IOException {
        String contentBody = null;
        Request request = new Request.Builder()
                .url(String.format(SHOW_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST))
                .get()
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            System.out.println("postJSONRequest response.body : " + response.body().string());
            Gson gson = new Gson();
            HashMap<String, Object> fields = gson.fromJson(response.body().string(), HashMap.class);
            contentBody = (String)fields.get("body") ;
        }
        response.close();

        return contentBody;
    }

}