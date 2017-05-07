package jp.clipline.clandroid;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.net.URISyntaxException;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;


public class CompareActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "clwebwrapperapplication";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 99999;

    private String mTodoContentType = null;
    private Uri mTodoContentData = null;
    private WebView mWebViewContent;
    private WebView mWebViewMine;

    private Button mButtonStartAllVideo;
    private Button mButtonSwitch;
    private TextView mBackScreen;
    private ImageButton mButtonClose;
    private ImageButton mButtonBack;

    private VideoView mVideoViewContent;
    private VideoView mVideoViewMine;
    private String mPath;
    private Map<String, Object> mCurrentTodoContent;
    private boolean mIsCheckSwitch = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        ///// 20170507 ADD START

        mWebViewContent = (WebView) findViewById(R.id.webViewContent);
        mWebViewMine = (WebView) findViewById(R.id.webViewMine);

        mVideoViewContent = (VideoView) findViewById(R.id.videoViewContent);
        mVideoViewMine = (VideoView) findViewById(R.id.videoViewMine);
        mBackScreen = (TextView) findViewById(R.id.backScreen);
        mButtonStartAllVideo = (Button) findViewById(R.id.buttonStartAllVideo);
        mButtonSwitch = (Button) findViewById(R.id.buttonSwitch);
        mButtonClose = (ImageButton) findViewById(R.id.imageButton);
        mButtonBack = (ImageButton) findViewById(R.id.imageButtonBack);

        mBackScreen.setOnClickListener(this);
        mButtonStartAllVideo.setOnClickListener(this);
        mButtonSwitch.setOnClickListener(this);
        mButtonClose.setOnClickListener(this);
        mButtonBack.setOnClickListener(this);

        mWebViewContent.getSettings().setJavaScriptEnabled(true);
        mWebViewMine.getSettings().setJavaScriptEnabled(true);

        mTodoContentType = ((ClWebWrapperApplication) this.getApplication()).getTodoContentType();
        mTodoContentData = ((ClWebWrapperApplication) this.getApplication()).getTodoContentData();

        mCurrentTodoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();
        TextView textView = (TextView) findViewById(R.id.textViewToDoTitle);

        mIsCheckSwitch = true;
        if (mCurrentTodoContent != null && mCurrentTodoContent.get("title") != null) {
            textView.setText((String) mCurrentTodoContent.get("title"));
        } else {
            textView.setText("");
        }
        try {
            mPath = "file:///" + AndroidUtility.getFilePath(this, mTodoContentData);
            if (mTodoContentType.equals("image/png")) {
                ImageView imageView = new ImageView(this);
                Picasso.with(this).load(mPath).into(imageView);
                mWebViewMine.addView(imageView);
                mWebViewMine.setVisibility(View.VISIBLE);
            } else if (mTodoContentType.equals("video/mp4")) {
                mVideoViewMine.setVideoPath(mPath);
                mVideoViewMine.setMediaController(new MediaController(this));
                mVideoViewMine.setVisibility(View.VISIBLE);
                mVideoViewMine.seekTo(100);
            }

            if (mCurrentTodoContent != null) {
                boolean isVideo = (boolean) mCurrentTodoContent.get("is_video");
                boolean isImage = (boolean) mCurrentTodoContent.get("is_image");
                boolean isPdf = (boolean) mCurrentTodoContent.get("is_pdf");

                if (isVideo) {
                    mVideoViewContent.setVideoPath((String) mCurrentTodoContent.get("pre_signed_standard_mp4_url"));
                    mVideoViewContent.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mp.seekTo(100);
                        }
                    });
                    mVideoViewContent.setVisibility(View.VISIBLE);
                } else if (isImage) { //TODO contact (media_thumb_pre_signed_url)
                    mWebViewContent.loadUrl(String.valueOf(mCurrentTodoContent.get("media_thumb_pre_signed_url")));
                    mWebViewContent.setVisibility(View.VISIBLE);
                } else if (isPdf) {
                    mWebViewContent.loadUrl(String.valueOf(mCurrentTodoContent.get("media_thumb_pre_signed_url")));
                    mWebViewContent.setVisibility(View.VISIBLE);
                }
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        ///// 20170507 ADD END


/*
        Log.d(TAG,String.format("@@@ media_pre_signed_url => %s",(String)currentTodoContent.get("media_pre_signed_url")));
        Log.d(TAG,String.format("@@@ media_thumb_pre_signed_url => %s",(String)currentTodoContent.get("media_thumb_pre_signed_url")));
        Map<String, String> headers = new HashMap<String, String>();
        // header.put("_clip-line2_session","QitjRlpIVEhGcFBuZWtPbDY2RnY4TXMwZWIwWnR2OE5MaS9lQ2d5YmdoRzA3OEdJKzZ5ajhSaFFVc0NWaGtWUFFVYVZRUXQ2ZnlPNnZzOEtJRzZkeGpBbXd0Q0lSMTBPOGRsQVlBZ3dkWVA3MVdVM3VIckJDYWd2NDFKNit6TXVIL2RvV0JvN2x3OGxhQWFXYjBrZ0MxOTlXTmgvUUVwUWJ5VlpiTmNGYnNtbkQzMXI1eEpoNXYrU3BENndQZFhKaldGVmNDaFhFSC9ZcUpVMWc2bHVZM1dEdzBzcnpkVkdmL3hoV3RaRWg5eW1UcVlVYS81YTBNek5Cbitlc1ZGeU0yd1B1L1ZqSmRYcFpsaGx2ekcxNUJWclJRN1dRMVdvK092V01GUkVMQVFROEFISUF2MnJBcmMxTFQ2N0pxak1kVEUvcWE5RGlxS1A5bXI2dDBacEFZY0U4RWJ3VmdjaStFS3Fybk5ldVkvT21MR1VsSGNrMi9vTmx6NnBhMHN5K2xOZEt0SlpVMm54SWNzNHJGdjVjUksrMFhvd1BxNkZpT3lrL1NrTEVrTT0tLXB0aXY1TUxRUEpTNDI1c1dyV2JmcWc9PQ%3D%3D--26d0f942e2db23c17291ce7ba3fc5d60f01778a3");

//        MediaController mc = new MediaController(this);
//        videoView.setMediaController(mc);

//        CookieSyncManager.createInstance(videoView.getContext());
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        // cookieManager.setCookie("", cookie_value);
//        // String cookie = AndroidUtility.getCookie(getApplicationContext());
//        // String cookie = String.format("%s",AndroidUtility.getCookie(getApplicationContext()));
//        // String cookie_ = "_clip-line2_session=OUJwOTJucXJIa1A0NjlQVXVtQWdPUEdpMVU5TWhuRmoyL0JYY2dGd0E4WXlGZlkzeFUwQlVXT1pIcjlUSmlSb250Sjkxb3Z2M3p6cmJUT3J4S213bFZPVDI2OTJtaTN1cm9RbTJheXlJK1NXUmR3K0IvVXVWb2NyYnlKQ0M4YVF0eVpYdTkrVUlPeWM3V2xoT3Y1Y2gveHlwWlIvV3FiY2RJcVNrSnhCMXo4SEpGZEVYN3grRWkrcHpXRW9wUDhzSXpuSm51WDZNdkxCTUpKUVVBSVFEUmlzRnhPNXlLWU5jaHNNMkVNRUk3MStyc0ppdEwvNjdSbWRzNmNmSEF4R044VjE2SUE1WXZYcHlITm1DWDhoR0x6ZEQ5OVFCN2FuMlVVZllNbXhkZ0FFSVlUWTR5T2JmR0N0UmFsOTNCL2p5bENJdVVmMC8yMDFXcUhxa3lmdWhydyt6WVMrRWdCdlRrL09xLzVhZUtGdlNCRlVZZG9tSFhsUTJRekh0aW9NRWxnbWRmK3VIMm9hU2VMNFZDVFZFNEMxaTlML0d2QmozbjhqT1hUNHczbnJDMFQ5bU9JK1dnUUtJTlBySktsNEh0VTlwSzNwTEpGS3pyU2FaRlE4bkZWdDZVVXlGcXN6TUNzYm5oOE1ybWFMTWplaTRuSk5hbDlBM3B1Mm9SWTBBMVFtVlZ0TDRBVHdZVHh3b3g2OWpSWWIxcEMyRXlWN0xYeER1cjEyaWtZVjZ5L1pmZ25EUFU3L2k4akY1T3Rudy9kNVVnOTUzRlFWV0I3U3NFbEpVYVJycmJxRmNtM1hYSitCdGNDQ2d6RlE3cDkvQ1VwT0RXZWxIY0Qxek5hWDZkN2NZc3dSUGVyMkZXTVFUd3o2OEVPaFdMZmNLV25PNU1CQjQwaEZnbnZmWkZkclpQSFJjYmFOZjFwL1NvdWstLVYwa01Vb3gvNFFueDRsMGx5QjY0T0E9PQ%3D%3D--0ccd4013d9f5e6a296d8378f42e47bd74f5b9e4c" ;
//        cookieManager.setCookie(String.format("%s://%s", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST), AndroidUtility.getCookie(getApplicationContext()));
//        // String aaa = cookieManager.getCookie(String.format("%s://%s", BuildConfig.API_PROTOCOL, BuildConfig.API_HOST));
//        CookieSyncManager.getInstance().sync();

//        MediaController mediaController = new MediaController(CompareActivity.this);
//        MediaController.MediaPlayerControl mediaPlayerControl;
//        mediaController.setMediaPlayer(mediaPlayerControl);
//        videoView.setMediaController(mediaController);

        String media_pre_signed_url = (String) currentTodoContent.get("media_pre_signed_url");
        Uri uri = Uri.parse(media_pre_signed_url);

//        MediaPlayer mMediaPlayer = new MediaPlayer();
//        Map<String, String> extraHeaders = new HashMap<String, String>();
//
//        // extraHeaders.put("Cookie", AndroidUtility.getCookie(getApplicationContext()));
//        extraHeaders.put("Cookie", "_clip-line2_session=cU1kMk15OU5mYmo0d08yZFVsamtDaEVaVXhGbVZYWVpOeUtnYkJBVEU1emUwR0tzaG9HMnJpSEdTZHpEN0ZxbStocGJ6YXhraGxpQTFxRndRWEFQdkxKYllJUjR4eTFKYkdLYkpDTGJCZG5DcXAvVmhyVll2Z0NnNTNUTEcrUnFYTXJTeEdPQld2S1NtUHNhNkw5czdLcSt1Z1pFQ1daLys4NUhQUXdBL0lEWGc3K2ZvMXlHKzhRV0RYcVEzZE50bVI0UXluUEdIT08rSHU2cGU4OU5XcnlPbTREamUwcTVwMzdZaFN2WEVScXNXWkh2M3h5VXRncFhKUmhTNkVaVUFjWXNFeC9nY2RQa25TdXdmNUdMaE5TSTNYVzc3RldYUGZIOWlqc2VueURSdHB4c0hGRGo4UGMyMSs0T1kwQTB1V1JvWkthS0VpOVNpdi94V3BaOVRwNEcrT0lqUTlUU213ajYvMkhseVVkeVFYUFhVL0Q0Zm1SNjhLOEhOK3JoNndCT3kraGZ1V0paSzNlNTZxcW81b3RRTGpaNHF0cURKYlVFSjNJLzVHd2JJSXg1cDQrem1aYUQzR3hEdEQ3eUVrblRHNGVaMk9KR3h0NUNSbExmcEZBQUk0R0NadHpHNTgxZ01vTnZoS0tETnBNLzBGT295d2NEVUJscFM0dlRrUjlpRDBVQk0xclV0cVZsR3FTR2RIVElUNE1nWGpJOVRmZDJUQ0pHM2FORnRlSkd5ZSt1bkpzdlFJMDBHNURCRGlVVy9jcXV0aW03YVdtUGtUY3hvVDBWUjBwSTQrdW5sSUhXcnpuK1BLU2NPUzQxM1NTMGRFc1kzYlIwcVgvcnNVaDRVOTFJU050QzJOcHQ5ZlJDNmtnYTd2TXBqZFZZVW5OcnkrR1I5ZnhyYnFPSzZTOW5Rbk1qUXovdnZFSnQtLUJxcE8ydkd6VFJGbC9oN2ZwUDlUMmc9PQ%3D%3D--33ec998417a56b72ab2932c6367455949dba94c9");
//
//        try {
//            // mMediaPlayer.setDataSource(getApplicationContext(), uri, extraHeaders);
//            mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8")) ; // , extraHeaders);
//            mMediaPlayer.setDisplay(videoView.getHolder());
//            mMediaPlayer.prepare();
//            mMediaPlayer.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        // videoView.setVideoURI(uri);
        // videoView.setVideoURI(Uri.parse("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8"));
*/
//        VideoView videoView = (VideoView) findViewById(R.id.videoViewExample);
//        videoView.setVideoURI(Uri.parse("http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8"));
//        videoView.start();
//
//        Log.e("CompareActivity", mTodoContentType);
//        videoView = (VideoView) findViewById(R.id.videoViewMine);
//        videoView.setVideoURI(mTodoContentData);
//        videoView.start();
    }

    ///// 20170507 ADD START
    @Override
    public void onClick(View v) {
        Intent intent;
        Map<String, String> todoParameters;
        String studentId;
        String categoryId;
        String todoContentId;
        String url;
        switch (v.getId()) {
            case R.id.buttonStartAllVideo:
                mVideoViewContent.setMediaController(new MediaController(this));
                mVideoViewContent.start();
                mVideoViewMine.start();
                break;
            case R.id.backScreen:
                intent = new Intent(CompareActivity.this, SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();

                break;
            case R.id.buttonSwitch:
                if (mIsCheckSwitch) {
                    mVideoViewContent.setVideoPath(mPath);
                    mVideoViewMine.setVideoPath(String.valueOf(mCurrentTodoContent.get("pre_signed_standard_mp4_url")));
                    mIsCheckSwitch = false;
                } else {
                    mVideoViewContent.setVideoPath(String.valueOf(mCurrentTodoContent.get("pre_signed_standard_mp4_url")));
                    mVideoViewMine.setVideoPath(mPath);
                    mIsCheckSwitch = true;
                }
                mVideoViewContent.start();
                mVideoViewMine.start();

                break;
            case R.id.imageButton:
                todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                studentId = todoParameters.get("studentId");
                categoryId = todoParameters.get("categoryId");
                todoContentId = todoParameters.get("todoContentId");
                url = "%s://%s/training/#/students/" + studentId
                        + "/todos?type=updates"; // TODO type=updates/repeat???
                intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                intent.putExtra("BASE_URL", url);
                startActivity(intent);
                finish();
                break;
            case R.id.imageButtonBack:

                todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                studentId = todoParameters.get("studentId");
                categoryId = todoParameters.get("categoryId");
                todoContentId = todoParameters.get("todoContentId");
/*
                String url = "%s://%s/training/#/students/" + studentId
                        + "/todos?type=caetgory&category_id=" + categoryId;
*/
                url = "%s://%s/training/#/students/" + studentId
                        + "/todos/" + todoContentId;
                intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                intent.putExtra("BASE_URL", url);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }
    }
    ///// 20170507 ADD END
}
