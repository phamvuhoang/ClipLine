package jp.clipline.clwebwrapperapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.util.ArrayList;

import jp.clipline.clwebwrapperapplication.Utility.AndroidUtility;

public class LaunchWebViewActivity extends AppCompatActivity {

    private static final String BASE_URL = String.format("%s://%s/training/students", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST);
    private final static String TAG = "clwebwrapperapplication";
    public static int PERMISSION_REQUEST_CODE = 1;
    public static int INPUT_FILE_REQUEST_CODE = 2;

    private ValueCallback<Uri[]> mFilePathCallback;
    private ValueCallback<Uri> mUploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_web_view);

        WebView mWebView = (WebView) findViewById(R.id.web_view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        CookieSyncManager.createInstance(this);
        CookieSyncManager.getInstance().startSync();
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(String.format("%s://%s", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST), AndroidUtility.getCookie(getApplicationContext()));
        CookieSyncManager.getInstance().sync();
        mWebView.setWebViewClient(new CustomWebViewClient());
        mWebView.setWebChromeClient(new CustomWebChromeClient());
        // ChromeでのRemoteDebugを有効にする
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView.loadUrl(BASE_URL);
        // mWebView.evaluateJavascript();

        activityRequestPermissions(PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;

            // Check that the response is a good one
            if (resultCode == RESULT_OK) {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[]{Uri.parse(dataString)};
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        } else {
            if (mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }

            Uri result = null;

            if (resultCode == RESULT_OK) {
                if (data != null) {
                    result = data.getData();
                }
            }

            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }

    private class CustomWebViewClient extends WebViewClient {
        @Override
        public void onLoadResource(WebView view, String url) {
            Log.d("WebViewActivity", String.format("@@@ onLoadResource : [%s]", url));

            super.onLoadResource(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("WebViewActivity", String.format("@@@ onPageFinished : [%s]", url));

            super.onPageFinished(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("WebViewActivity", String.format("@@@ onPageStarted : [%s]", url));

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.d("WebViewActivity", String.format("@@@ shouldInterceptRequest : [%s]", url));

            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Toast.makeText(getApplicationContext(), "shouldOverrideUrlLoading", Toast.LENGTH_SHORT).show();
            Log.d("WebViewActivity", String.format("@@@ shouldOverrideUrlLoading : [%s]", url));

            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private class CustomWebChromeClient extends WebChromeClient {
        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> uploadFile) {
            openFileChooser(uploadFile, "");
            Toast.makeText(getApplicationContext(), "onShowFileChooser : For Android < 3.0", Toast.LENGTH_SHORT).show();
        }

        // For 3.0 <= Android < 4.1
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType) {
            openFileChooser(uploadFile, acceptType, "");
            Toast.makeText(getApplicationContext(), "onShowFileChooser : For 3.0 <= Android < 4.1", Toast.LENGTH_SHORT).show();
        }

        // For 4.1 <= Android < 5.0
        public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
            if (mUploadMessage != null) {
                mUploadMessage.onReceiveValue(null);
            }
            mUploadMessage = uploadFile;

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/png");

            startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
        }

        // For Android 5.0+
        @Override
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;

            // https://mp-dev.clipline.jp/training/students
//            if(webView.getUrl().equals("https://mp-dev.clipline.jp/training/students")) {
//                String file_name = System.currentTimeMillis() + ".jpg";
//                ContentValues values = new ContentValues();
//                values.put(MediaStore.Images.Media.TITLE, file_name);
//                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//                Uri uri_picture = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri_picture);
//                startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
//            }
//            else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/png");
            startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
//            }

            return true;
        }
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
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

}
