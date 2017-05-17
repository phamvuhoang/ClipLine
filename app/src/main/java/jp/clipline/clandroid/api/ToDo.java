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

public class ToDo {

    private final static String TAG = "clwebwrapperapplication";

    private final static String STUDENT_TODO_CATEGORIES_URL = "%s://%s//v2/api/v2/training/student_todo_categories/%s/sub_categories";

    public static Map<String, Object> getTodoContent(String cookie, String categoryId, String todoContentId) throws IOException {
        ///// 20170514 MODIFY START
        //TEST
        todoContentId = String.valueOf(15892);
        ///// 20170514 MODIFY END

//        CookieJar cookieJar = new CookieJar() {
//            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
//
//            @Override
//            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//                cookieStore.put(url.host(), cookies);
//            }
//
//            @Override
//            public List<Cookie> loadForRequest(HttpUrl url) {
//                List<Cookie> cookies = cookieStore.get(url.host());
//                return cookies != null ? cookies : new ArrayList<Cookie>();
//            }
//        };

//        cookieJar.saveFromResponse(String.format("%s://%s/", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST), "");
//
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .cookieJar(cookieJar)
//                .build();

        Gson gson = new Gson();
        Map<String, Object> current_todo_content = null;

        String language = Locale.getDefault().toString();

        Request request = new Request.Builder()
                .url(String.format(STUDENT_TODO_CATEGORIES_URL, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST, categoryId))
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
        } else {
            throw new IOException(String.format("Todo#getCategories : failed [%d]", response.code()));
        }

        response.close();

        return current_todo_content;
    }

//    private static CookieJar cookieJar = new CookieJar() {
//        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
//
//        @Override
//        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
//            cookieStore.put(url.host(), cookies);
//        }
//
//        @Override
//        public List<Cookie> loadForRequest(HttpUrl url) {
//            List<Cookie> cookies = cookieStore.get(url.host());
//            return cookies != null ? cookies : new ArrayList<Cookie>();
//        }
//    };

}
