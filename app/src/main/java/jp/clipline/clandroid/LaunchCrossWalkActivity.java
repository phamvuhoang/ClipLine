package jp.clipline.clandroid;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.xwalk.core.JavascriptInterface;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.api.Branch;
import okhttp3.Response;

public class LaunchCrossWalkActivity extends AppCompatActivity {

    private static final String BASE_URL = String.format("%s://%s/training/#/students", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST);
    private final static String TAG = "clwebwrapperapplication";

    private XWalkView mXWalkView;
    private XWalkCookieManager mCookieManager = new XWalkCookieManager();
    public static int PERMISSION_REQUEST_CODE = 1;
    public static int INPUT_FILE_REQUEST_CODE = 2;
    private ValueCallback<Uri> mUploadMessage;

    ProgressBar mProgressBar;

    private BranchLogoutTask mLogoutTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_cross_walk);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBarLaunchCrossWalk);
        mProgressBar.setVisibility(View.VISIBLE);

        mXWalkView = (XWalkView) findViewById(R.id.xwalkWebView);

        mXWalkView.setVisibility(View.GONE);

        mXWalkView.setResourceClient(new ResourceClient(mXWalkView));
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);

        mXWalkView.getSettings().setAllowFileAccess(true);
        mXWalkView.getSettings().setAllowContentAccess(true);
        // CrossWalkの気分次第で必要な場合があるかもなので、残しています
        // mXWalkView.getSettings().setAcceptLanguages("ja");
        mXWalkView.setUIClient(new MyUIClient(mXWalkView));

//        XWalkCookieManager mCookieManager;
//        mCookieManager = new XWalkCookieManager();
        mCookieManager.setAcceptCookie(true);
        mCookieManager.setAcceptFileSchemeCookies(true);

        String fromLogin = getIntent().getExtras().getString("FROM_SCREEN_LOGIN", null);
        if (fromLogin != null) { // intent from screen login
            mCookieManager.removeAllCookie();
        }


        // @see : https://cliplinedev.slack.com/archives/multiplatform/p1485911439000053
        // String cookie = String.format("X-ClipLine-AppType=android; %s", AndroidUtility.getCookie(getApplicationContext()));
        String cookie = String.format("%s", AndroidUtility.getCookie(getApplicationContext()));
        // String cookie = String.format("%s",AndroidUtility.getCookie(getApplicationContext()));
        mCookieManager.setCookie(String.format("%s://%s", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST), AndroidUtility.getCookie(getApplicationContext()));
        mCookieManager.setCookie(String.format("%s://%s", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST), "X-ClipLine-AppType=android");
        Map<String, String> extraHeaders = new HashMap<String, String>();

        mXWalkView.addJavascriptInterface(new NativeInterface(), "NativeInterface");

        if (fromLogin != null) {
            mXWalkView.load(BASE_URL, null, extraHeaders);
        } else {
            String url = (String) getIntent().getExtras().get("BASE_URL");
            mXWalkView.load(String.format(url, BuildConfig.API_PROTOCOL, BuildConfig.API_HOST), null, extraHeaders);
        }

        activityRequestPermissions(PERMISSION_REQUEST_CODE);
    }

    private boolean activityRequestPermissions(int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = AndroidUtility.getSettingPermissions(this);
            boolean isRequestPermission = false;
            for (String permission : permissions) {
                if (!AndroidUtility.hasSelfPermission(this, permission)) {
                    isRequestPermission = true;
                    break;
                }
            }
            if (isRequestPermission) {
                requestPermissions(permissions.toArray(new String[0]), requestCode);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Log.d(TAG, "@@@ onRequestPermissionsResult : Start @@@");
        if (requestCode == PERMISSION_REQUEST_CODE) {

            // 許可されたパーミッションがあるかを確認する
            boolean isSomethingGranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    isSomethingGranted = true;
                    break;
                }
            }

            if (isSomethingGranted) {
                // 設定を変更してもらえた場合、処理を継続する
            } else {
                // 設定を変更してもらえなかった場合、終了
                Toast.makeText(this, "権限取得エラー", Toast.LENGTH_LONG).show();
                finish();
            }
        }
        Log.d(TAG, "@@@ onRequestPermissionsResult : End @@@");
    }

    private class ResourceClient extends XWalkResourceClient {
        ResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public void onLoadStarted(XWalkView view, String url) {
            //if(url.indexOf(BASE_URL)>=0) {
            // Log.d("CrossWalkActivity", String.format("@@@ onLoadStarted : [%s]", url));
            //}
            super.onLoadFinished(view, url);
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            mProgressBar.setVisibility(View.GONE);
            mXWalkView.setVisibility(View.VISIBLE);
            //if (url.indexOf(BASE_URL) >= 0) {
            // Log.d("CrossWalkActivity", String.format("@@@ onLoadFinished : [%s]", url));
            // NativeInterface実装までの仮置き
            Pattern p = Pattern.compile("^.*\\/training\\/students\\/(\\d+)\\/todos\\/(\\d+)$");
            Matcher m = p.matcher(url);
            if (m.matches()) {
//                String studentId = m.toMatchResult().group(1);
//                String todoId = m.toMatchResult().group(2);
//                // 学習者のログインを反映する為
//                AndroidUtility.setCookie(getApplicationContext(), mCookieManager.getCookie(String.format("%s://%s", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST)));
//
//                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
//                intent.putExtra("studentId", studentId);
//                intent.putExtra("categoryId", "988");
//                intent.putExtra("todoId", todoId);
//                startActivity(intent);
//                overridePendingTransition(0, 0);
            }
            //}
            super.onLoadFinished(view, url);
            // finish();
        }

        @Override
        public WebResourceResponse shouldInterceptLoadRequest(XWalkView view, String url) {
            //if (url.indexOf(BASE_URL) >= 0) {
            // Log.d("CrossWalkActivity", String.format("@@@ onLoadFinished : [%s]", url));
            //}
            return super.shouldInterceptLoadRequest(view, url);
        }

        // Location変更通知
        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String url) {
            //if (url.indexOf(BASE_URL) >= 0) {
            // Log.d("CrossWalkActivity", String.format("@@@ shouldOverrideUrlLoading : [%s]", url));
            //}
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    class MyUIClient extends XWalkUIClient {
        MyUIClient(XWalkView view) {
            super(view);
        }

        @Override
        public void openFileChooser(XWalkView view, ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            Log.d(MainActivity.class.getSimpleName(), "openFileChooser");
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/png");
            startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
            mUploadMessage = uploadFile;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();

        if (mUploadMessage == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (resultCode == RESULT_OK) {
            if (data != null) {
                result = data.getData();
            }
        }

        mUploadMessage.onReceiveValue(result);
        mUploadMessage = null;
    }

    @Override
    public void onBackPressed() {
        Log.d("", "");
    }

    public class NativeInterface {

        public NativeInterface() {
        }

        // ex) NativeInterface.alert("hello world!");
        @JavascriptInterface
        public void alert(String message) {
            //do something
            Toast.makeText(getApplicationContext(), String.format("Called from Browser [%s]", message), Toast.LENGTH_SHORT).show();
        }

        // ex) NativeInterface.studentToDo(92680,988,15532);
        @JavascriptInterface
        public void studentToDo(String studentId, String categoryId, String todoContentId, String type) {
            categoryId = "988";
            AndroidUtility.setBack(LaunchCrossWalkActivity.this, false);
            ((ClWebWrapperApplication) getApplication()).setTodoParameters(studentId, categoryId, todoContentId, type);
            // 学習者のログインを反映する為
//            XWalkCookieManager mCookieManager = new XWalkCookieManager();
            AndroidUtility.setCookie(getApplicationContext(), mCookieManager.getCookie(String.format("%s://%s", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST)));

            Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }

        // ex) NativeInterface.coachToDo(92680,988,15532);
        @JavascriptInterface
        public void coachToDo(String coachId, String categoryId, String todoId, String type) {

            Log.i("coachdToDo", coachId + "\n" + categoryId + "\n" + todoId);
            categoryId = "988";
            ((ClWebWrapperApplication) getApplication()).setTodoParameters(coachId, categoryId, todoId, type);
            Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
            startActivity(intent);

            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }

        @JavascriptInterface
        public void studentLogin(String user_id, String password) {
            Log.i("studentLogin", user_id + "\n" + password);
        }


        @JavascriptInterface
        public void coachLogin(String user_id, String password) {
            Log.i("coachLogin", user_id + "\n" + password);
        }

        @JavascriptInterface
        public void logout() {
            Log.i("logout", "logout");
            mCookieManager.removeAllCookie();
            String cookie = AndroidUtility.getCookie(getApplicationContext());
            mLogoutTask = new BranchLogoutTask(cookie);
            mLogoutTask.execute((Void) null);
        }

        @JavascriptInterface
        public void pdfViewer(String pdfUrl) {
            Log.i("pdfViewer", pdfUrl);
        }

    }

    /**
     * Represents an asynchronous logout task
     */
    public class BranchLogoutTask extends AsyncTask<Void, Void, Boolean> {

        private Response mResponse;
        private String mCookie;

        public BranchLogoutTask(String cookie) {
            mCookie = cookie;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mResponse = (Response) Branch.signOut(mCookie);
                return Boolean.TRUE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            String message = null;
            if (success) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                // TODO what happened?
            }
        }

        @Override
        protected void onCancelled() {
            mLogoutTask = null;
        }
    }

}
