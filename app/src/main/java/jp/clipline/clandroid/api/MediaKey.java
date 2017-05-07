package jp.clipline.clandroid.api;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.clipline.clandroid.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MediaKey {


    private final static String TAG = "clwebwrapperapplication";

    private final static String MEDIA_KEY_URL = "%s://%s//v2/api/v1/media_objects/generate_media_key?object_type=student_report";

    public static Map<String, Object> getMediaKeyContent(String cookie) throws IOException {

        Gson gson = new Gson();
        Map<String, Object> mediaKeyContent = null;

        String language = Locale.getDefault().toString();

        Request request = new Request.Builder()
                .url(String.format(MEDIA_KEY_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST))
                .addHeader("Cookie", cookie)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Language", language)
                .get()
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            String body = response.body().string();

            List<Map<String, Object>> subCategories = new ArrayList<>();
            subCategories = gson.fromJson(body, subCategories.getClass());
/*
            for (Map<String, Object> subCategory : subCategories) {
                ArrayList<Map<String, Object>> todos = (ArrayList<Map<String, Object>>) subCategory.get("todos");
                for (Map<String, Object> todo : todos) {
                    ArrayList<Map<String, Object>> todo_contents = (ArrayList<Map<String, Object>>) todo.get("todo_contents");
                    for (Map<String, Object> todo_content : todo_contents) {
                        if (Integer.parseInt(todoContentId) == Double.valueOf((double) todo_content.get("id")).intValue()) {
                            current_todo_content = todo_content;
                        }
                    }
                }
            }
*/
        } else {
            throw new IOException(String.format("MediaKey#getMediaKey : failed [%d]", response.code()));
        }

        response.close();

        return mediaKeyContent;
    }
}
