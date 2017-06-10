package jp.clipline.clandroid.api;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import jp.clipline.clandroid.BuildConfig;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Report {

    private final static String TAG = "clwebwrapperapplication";

    private final static String STUDENT_REPORT_URL = "%s://%s//v2/api/v2/training/student_reports/";
    private final static String COACH_REPORT_URL = "%s://%s//v2/api/v2/training/coach_reports/";

    public static Map<String, Object> sendStudentReport(String cookie
            , String mediaKey, String contentType
            , String mediaURLInDevice, String mediaDuration, String takenAt
            , String todoContentId
            , String loginType) throws IOException {

        Gson gson = new Gson();
        Map<String, Object> responseData = null;

        RequestBody requestBody = new FormBody.Builder()
                .add("media_key", mediaKey)
                .add("content_type", contentType)
                .add("media_url_in_device", mediaURLInDevice)
                .add("media_duration", mediaDuration)
                .add("taken_at", takenAt)
                .add("todo_content_id", todoContentId)
                .add("source_type", "training_android")
                .build();

        String language = Locale.getDefault().toString();
        String url = "student".equals(loginType) ? STUDENT_REPORT_URL : COACH_REPORT_URL;

        Request request = new Request.Builder()
                .url(String.format(url, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST))
                .post(requestBody)
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
