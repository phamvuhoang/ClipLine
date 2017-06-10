package jp.clipline.clandroid.api;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import jp.clipline.clandroid.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MediaKey {

    private final static String TAG = "clwebwrapperapplication";

    private final static String STUDENT_MEDIA_KEY_URL = "%s://%s//v2/api/v1/media_objects/generate_media_key?object_type=student_report";
    private final static String COACH_MEDIA_KEY_URL = "%s://%s//v2/api/v1/media_objects/generate_media_key?object_type=coach_report";

    public static Map<String, Object> getMediaKeyContent(String cookie, String loginType) throws IOException {

        Gson gson = new Gson();
        Map<String, Object> mediaKeyContent = null;

        String language = Locale.getDefault().toString();
        String url = "student".equals(loginType) ? STUDENT_MEDIA_KEY_URL : COACH_MEDIA_KEY_URL;

        Request request = new Request.Builder()
                .url(String.format(url, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST))
                .addHeader("Cookie", cookie)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Language", language)
                .get()
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            String body = response.body().string();

            mediaKeyContent = gson.fromJson(body, Map.class);
        } else {
            throw new IOException(String.format("MediaKey#getMediaKey : failed [%d]", response.code()));
        }

        response.close();

        return mediaKeyContent;
    }
}
