package jp.clipline.clandroid.api;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jp.clipline.clandroid.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ToDo {

    private final static String TAG = "clwebwrapperapplication";

    //private final static String STUDENT_TODO_CATEGORIES_URL = "%s://%s//v2/api/v2/training/student_todo_categories/%s/sub_categories";

    private final static String STUDENT_TODO_CONTENTS_URL = "%s://%s//v2/api/v2/training/student_todo_contents/%s";
    private final static String COACH_TODO_CONTENTS_URL = "%s://%s//v2/api/v2/training/coach_todo_contents/%s";

    public static Map<String, Object> getTodoContent(String cookie, String categoryId, String todoContentId, String loginType) throws IOException {
        //TEST
//        todoContentId = String.valueOf(15892);

        Gson gson = new Gson();
        Map<String, Object> current_todo_content = null;

        String language = Locale.getDefault().toString();
        String url = "student".equals(loginType) ? STUDENT_TODO_CONTENTS_URL : COACH_TODO_CONTENTS_URL;

        Request request = new Request.Builder()
                .url(String.format(url, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST, todoContentId))
                .addHeader("Cookie", cookie)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept-Language", language)
                .get()
                .build();

        Response response = new OkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            String body = response.body().string();

            Map<String, Object> responseBody = new HashMap<String, Object>();
            responseBody = gson.fromJson(body, responseBody.getClass());

            Map<String, Object> todo_contents = (LinkedTreeMap<String, Object>) responseBody.get("todo_contents");
            //Map<String, Object> todo_contents = (HashMap<String, Object>) responseBody.get("todo_contents");
            if (Integer.parseInt(todoContentId) == Double.valueOf((double) todo_contents.get("id")).intValue()) {
                current_todo_content = todo_contents;
            }

        } else {
            throw new IOException(String.format("Todo#getCategories : failed [%d]", response.code()));
        }

        response.close();

        return current_todo_content;
    }

}
