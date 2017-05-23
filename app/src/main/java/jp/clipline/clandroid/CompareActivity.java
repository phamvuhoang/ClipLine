package jp.clipline.clandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.api.MediaKey;
import jp.clipline.clandroid.api.Report;
import jp.clipline.clandroid.view.StatusView;


public class CompareActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String TAG = "clwebwrapperapplication";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 99999;

    private String mTodoContentType = null;
    private Uri mTodoContentData = null;
    private WebView mWebViewContent;
    private WebView mWebViewMine;

//    private Button mButtonStartAllVideo;
//    private Button mButtonSwitch;

    private TextView mBackScreen;
    private ImageButton mButtonClose;
    private LinearLayout mButtonBack;

    private VideoView mVideoViewContent;
    private VideoView mVideoViewMine;
    private String mPath;
    private Map<String, Object> mCurrentTodoContent;
    private boolean mIsCheckSwitch = true;

    private StatusView mStatusView;
    private StatusView mStatusViewResport;
    private StatusView mStatusViewCheck;
    //    private LinearLayout mLinearLayoutFooterStatus;
//    private ImageView mImageViewFooterView;
//    private TextView mTextViewFooterView;
//    private ImageView mImageViewFooterShoot;
//    private TextView mTextViewFooterShoot;
//    private ImageView mImageViewFooterCompare;
//    private TextView mTextViewFooterCompare;

    //Video
    private TextView mCurrentTimeContent;
    private TextView mCurrentTimeMine;
    private TextView mTotalTimeContent;
    private TextView mTotalTimeMine;
    private SeekBar mPosSeekBarContent;
    private SeekBar mPosSeekBarMine;
    private ImageView mPlayAndPauseContent;
    private ImageView mPlayAndPauseMine;
    private ImageView mChangeFullScreen;
    private final int SEEKTOTIME = 1111;
    private final int UPDATE_UI = 1;
    private final MyHandlerContent mHandlerContent = new MyHandlerContent(this);
    private final MyHandlerMine mHandlerMine = new MyHandlerMine(this);
    private ImageView mImageViewSwitch;
    private RelativeLayout mRelativeLayoutPreviewLeft;
    private RelativeLayout mRelativeLayoutPreviewRight;
    private ImageView mImageViewSubmit;
    private TextView mTextViewUpload;
    private Button mButtonReportSentComment;
    private Button mButtonReportSentClose;
    private Button mButtonReportSentRetry;
    private TextView mTextViewError;
    private ProgressBar mProgressBar;

    private Button mButtonSummit;

    private RelativeLayout mRelativeLayoutOverlay;
    ///// 20170521 ADD START
    private final int UPLOAD_NONE = 0;
    private final int UPLOAD_SUCCESSFULL = 1;
    private final int UPLOAD_FAILE = 2;

    private int mSubmissionConfirmation = 0;
    private View mViewProgressBar;
    ///// 20170521 ADD END


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        ///// 20170521 ADD START
        if (savedInstanceState != null) {
            mSubmissionConfirmation = savedInstanceState.getInt("compareActivity");
        }
        ///// 20170521 ADD START
        // レポート完成画面
        mRelativeLayoutOverlay = (RelativeLayout) findViewById(R.id.relativeLayoutOverlay);
        mRelativeLayoutOverlay.setVisibility(View.GONE);
        ///// 20170521 DELETE START
//        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonReportSentClose);
//        imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mRelativeLayoutOverlay.setVisibility(View.GONE);
//            }
//        });
        ///// 20170521 DELETE END
        // レポート完成：もどるボタン
        mTextViewError = (TextView) findViewById(R.id.textViewError);
        mButtonReportSentRetry = (Button) findViewById(R.id.buttonReportSentRetry);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mViewProgressBar = findViewById(R.id.viewProgressBar);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
        mButtonReportSentRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                mTextViewUpload.setText(getResources().getText(R.string.report_sent));
                mImageViewSubmit.setBackground(null);
                mTextViewError.setVisibility(View.GONE);
                mViewProgressBar.setVisibility(View.GONE);
                new GetMediaKeyTask().execute(AndroidUtility.getCookie(getApplicationContext()));
            }
        });
        mButtonReportSentClose = (Button) findViewById(R.id.buttonReportSentClose);

        mButtonReportSentClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///// 20170521 MODIFY START
                mRelativeLayoutOverlay.setVisibility(View.GONE);
                mSubmissionConfirmation = UPLOAD_NONE;
//                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
//                String studentId = todoParameters.get("studentId");
//                String categoryId = todoParameters.get("categoryId");
//                String todoContentId = todoParameters.get("todoContentId");
//                String url = "%s://%s/training/#/students/" + studentId
//                        + "/todos?type=updates"; // TODO type=updates/repeat???
//                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
//                intent.putExtra("BASE_URL", url);
//                startActivity(intent);
//                finish();
                ///// 20170521 MODIFY END
            }
        });

        // レポート完成：コメントを入れるボタン
        mButtonReportSentComment = (Button) findViewById(R.id.buttonReportSentInputComment);

        mButtonReportSentComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                String studentId = todoParameters.get("studentId");
                String categoryId = todoParameters.get("categoryId");
                String todoContentId = todoParameters.get("todoContentId");
                String url = "%s://%s/training/#/students/" + studentId
                        + "/todos/" + todoContentId;

                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                intent.putExtra("BASE_URL", url);
                startActivity(intent);
                finish();
            }
        });

        mWebViewContent = (WebView) findViewById(R.id.webViewContent);
        mWebViewMine = (WebView) findViewById(R.id.webViewMine);

        mVideoViewContent = (VideoView) findViewById(R.id.videoViewContent);
        mVideoViewMine = (VideoView) findViewById(R.id.videoViewMine);
        mBackScreen = (TextView) findViewById(R.id.backScreen);

//        mButtonStartAllVideo = (Button) findViewById(R.id.buttonStartAllVideo);
//        mButtonSwitch = (Button) findViewById(R.id.buttonSwitch);

//        mButtonClose = (ImageButton) findViewById(R.id.imageButton);
        mButtonBack = (LinearLayout) findViewById(R.id.imageButtonBack);

        mBackScreen.setOnClickListener(this);

//        mButtonStartAllVideo.setOnClickListener(this);
//        mButtonSwitch.setOnClickListener(this);

//        mButtonClose.setOnClickListener(this);
        mButtonBack.setOnClickListener(this);

        mWebViewContent.getSettings().setJavaScriptEnabled(true);
        mWebViewMine.getSettings().setJavaScriptEnabled(true);

        mTodoContentType = ((ClWebWrapperApplication) this.getApplication()).getTodoContentType();
        File file = new File(((ClWebWrapperApplication) this.getApplication()).getTodoContentData());
        Uri uriFile = Uri.fromFile(file);
        mTodoContentData = uriFile;


        mCurrentTodoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();
        TextView textView = (TextView) findViewById(R.id.textViewToDoTitle);
        ///// 20170521 ADD START
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///// 20170521 DELETE START
//                Intent intent;
//                Map<String, String> todoParameters;
//                String studentId;
//                String categoryId;
//                String todoContentId;
//                String url;
//
//                todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
//                studentId = todoParameters.get("studentId");
//                categoryId = todoParameters.get("categoryId");
//                todoContentId = todoParameters.get("todoContentId");
/*
                String url = "%s://%s/training/#/students/" + studentId
                        + "/todos?type=caetgory&category_id=" + categoryId;
*/
//                url = "%s://%s/training/#/students/" + studentId
//                        + "/todos/" + todoContentId;
//                intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
//                intent.putExtra("BASE_URL", url);
//                startActivity(intent);
//                finish();
                ///// 20170521 DELETE END
                Intent intent = new Intent(CompareActivity.this, SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ///// 20170521 ADD END

        mIsCheckSwitch = true;
        if (mCurrentTodoContent != null && mCurrentTodoContent.get("title") != null) {
            textView.setText((String) mCurrentTodoContent.get("title"));
        } else {
            textView.setText("");
        }
        try {
            mPath = "file://" + AndroidUtility.getFilePath(this, mTodoContentData);
            if (mTodoContentType.equals("image/png")) {
                mWebViewMine.removeAllViews();
                ImageView imageView = new ImageView(this);
                Picasso.with(this).load(mPath).into(imageView);
                mWebViewMine.addView(imageView);
                mWebViewMine.setVisibility(View.VISIBLE);
            } else if (mTodoContentType.equals("video/mp4")) {
                mVideoViewMine.setVideoPath(mPath);
                mVideoViewMine.setVisibility(View.VISIBLE);
            }

            if (mCurrentTodoContent != null) {
                boolean isVideo = (boolean) mCurrentTodoContent.get("is_video");
                boolean isImage = (boolean) mCurrentTodoContent.get("is_image");
                boolean isPdf = (boolean) mCurrentTodoContent.get("is_pdf");

                if (isVideo) {
                    mVideoViewContent.setVideoPath((String) mCurrentTodoContent.get("pre_signed_standard_mp4_url"));

//                    mVideoViewContent.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
//                            mp.seekTo(100);
//                        }
//                    });

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

        // Statuses on Footer

        mStatusView = (StatusView) findViewById(R.id.statusView);
        mStatusViewResport = (StatusView) findViewById(R.id.statusResport);
        mStatusViewCheck = (StatusView) findViewById(R.id.statusCheck);

        mStatusView.setTypeView(StatusView.STATUS_VIEW.VIEW, false);
        mStatusViewResport.setTypeView(StatusView.STATUS_VIEW.REPORT, false);
        mStatusViewCheck.setTypeView(StatusView.STATUS_VIEW.CHECK, false);

//        mLinearLayoutFooterStatus = (LinearLayout) findViewById(R.id.linearLayoutFooterStatus);
//        mImageViewFooterView = (ImageView) findViewById(R.id.imageViewFooterView);
//        mTextViewFooterView = (TextView) findViewById(R.id.textViewFooterView);
//        mImageViewFooterShoot = (ImageView) findViewById(R.id.imageViewFooterShoot);
//        mTextViewFooterShoot = (TextView) findViewById(R.id.textViewFooterShoot);
//        mImageViewFooterCompare = (ImageView) findViewById(R.id.imageViewFooterCompare);
//        mTextViewFooterCompare = (TextView) findViewById(R.id.textViewFooterCompare);

        // Firstly, hide the status, after getting result from api, then depend on the flags to process the view/hide

//        mStatusView.setVisibility(View.GONE);
//        mStatusViewResport.setVisibility(View.GONE);
//        mStatusViewCheck.setVisibility(View.GONE);

//        mLinearLayoutFooterStatus.setVisibility(View.GONE);
//        mImageViewFooterView.setVisibility(View.GONE);
//        mTextViewFooterView.setVisibility(View.GONE);
//        mImageViewFooterShoot.setVisibility(View.GONE);
//        mTextViewFooterShoot.setVisibility(View.GONE);
//        mImageViewFooterCompare.setVisibility(View.GONE);
//        mTextViewFooterCompare.setVisibility(View.GONE);

        updateStatus();

        // この内容で提出
        mButtonSummit = (Button) findViewById(R.id.buttonSubmit);
        mButtonSummit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///// 20170523 MODIFY START
                // First, get media key
                // On post execute, upload file to S3 and call report submit api
                mRelativeLayoutOverlay.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                mTextViewUpload.setText(getResources().getText(R.string.report_sent));
                mImageViewSubmit.setBackground(null);
                mTextViewError.setVisibility(View.GONE);
                mViewProgressBar.setVisibility(View.GONE);
                mButtonReportSentComment.setVisibility(View.GONE);
                mButtonReportSentRetry.setVisibility(View.GONE);
                mButtonReportSentClose.setVisibility(View.GONE);
                ///// 20170523 MODIFY END
                new GetMediaKeyTask().execute(AndroidUtility.getCookie(getApplicationContext()));
            }
        });

        findViewById();
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        Map<String, String> todoParameters;
        String studentId;
        String categoryId;
        String todoContentId;
        String url;
        boolean isPortrait;
        switch (v.getId()) {
//            case R.id.buttonStartAllVideo:
//                mVideoViewContent.setMediaController(new MediaController(this));
//                mVideoViewContent.start();
//                mVideoViewMine.start();
//                break;
            case R.id.backScreen:
                intent = new Intent(CompareActivity.this, SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();

                break;
//            case R.id.buttonSwitch:
//                if (mIsCheckSwitch) {
//                    mVideoViewContent.setVideoPath(mPath);
//                    mVideoViewMine.setVideoPath(String.valueOf(mCurrentTodoContent.get("pre_signed_standard_mp4_url")));
//                    mIsCheckSwitch = false;
//                } else {
//                    mVideoViewContent.setVideoPath(String.valueOf(mCurrentTodoContent.get("pre_signed_standard_mp4_url")));
//                    mVideoViewMine.setVideoPath(mPath);
//                    mIsCheckSwitch = true;
//                }
//                mVideoViewContent.start();
//                mVideoViewMine.start();
//
//                break;
            ///// 201705021 DELETE START
//            case R.id.imageButton:
//                todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
//                studentId = todoParameters.get("studentId");
//                categoryId = todoParameters.get("categoryId");
//                todoContentId = todoParameters.get("todoContentId");
//                url = "%s://%s/training/#/students/" + studentId
//                        + "/todos?type=updates"; // TODO type=updates/repeat???
//                intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
//                intent.putExtra("BASE_URL", url);
//                startActivity(intent);
//                finish();
//                break;
            ///// 201705021 DELETE END
            case R.id.imageButtonBack:
                ///// 20170521 DELETE START
//                todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
//                studentId = todoParameters.get("studentId");
//                categoryId = todoParameters.get("categoryId");
//                todoContentId = todoParameters.get("todoContentId");
/*
                String url = "%s://%s/training/#/students/" + studentId
                        + "/todos?type=caetgory&category_id=" + categoryId;
*/
//                url = "%s://%s/training/#/students/" + studentId
//                        + "/todos/" + todoContentId;
//                intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
//                intent.putExtra("BASE_URL", url);
//                startActivity(intent);
                ///// 20170521 DELETE END
                intent = new Intent(CompareActivity.this, SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.pause_img_content:
            case R.id.pause_img_mine:
                if (mVideoViewContent.isPlaying() && mVideoViewMine.isPlaying()) {
                    mPlayAndPauseContent.setImageResource(R.drawable.video_start_style);
                    mPlayAndPauseMine.setImageResource(R.drawable.video_start_style);
                    mVideoViewContent.pause();
                    mVideoViewMine.pause();
                    mHandlerContent.removeMessages(UPDATE_UI);
                    mHandlerMine.removeMessages(UPDATE_UI);
                } else {
                    mPlayAndPauseContent.setImageResource(R.drawable.video_stop_style);
                    mPlayAndPauseMine.setImageResource(R.drawable.video_stop_style);
                    mVideoViewContent.start();
                    mVideoViewMine.start();
                    mHandlerContent.sendEmptyMessage(UPDATE_UI);
                    mHandlerMine.sendEmptyMessage(UPDATE_UI);
                }
                break;
            case R.id.change_screen:

                intent = new Intent(this, FullVideoActivity.class);
                if (mIsCheckSwitch) {
                    intent.putExtra("path_uri", mPath);
                } else {
                    intent.putExtra("path_uri", String.valueOf(mCurrentTodoContent.get("media_thumb_pre_signed_url")));
                }
                startActivity(intent);
                break;

            case R.id.imageViewSwitch:
                isPortrait = getRotation();
                if (isPortrait) {  // screen portrait
                    if (mIsCheckSwitch) {
                        mIsCheckSwitch = false;
                        mVideoViewContent.setVideoPath(mPath);
                        mVideoViewMine.setVideoPath(String.valueOf(mCurrentTodoContent.get("pre_signed_standard_mp4_url")));
                    } else {
                        mIsCheckSwitch = true;
                        mVideoViewContent.setVideoPath(String.valueOf(mCurrentTodoContent.get("pre_signed_standard_mp4_url")));
                        mVideoViewMine.setVideoPath(mPath);
                    }
                } else { // screen landscape
                    if (mIsCheckSwitch) {
                        mIsCheckSwitch = false;
                        mRelativeLayoutPreviewLeft.setBackground(ContextCompat.getDrawable(this, R.drawable.border_color_green_select));
                        mRelativeLayoutPreviewRight.setBackground(ContextCompat.getDrawable(this, R.drawable.border_color_green));
                        mCurrentTimeContent.setVisibility(View.VISIBLE);
                        mTotalTimeContent.setVisibility(View.VISIBLE);
                        mPosSeekBarContent.setVisibility(View.VISIBLE);
                        mCurrentTimeMine.setVisibility(View.GONE);
                        mTotalTimeMine.setVisibility(View.GONE);
                        mPosSeekBarMine.setVisibility(View.GONE);
                    } else {
                        mIsCheckSwitch = true;
                        mRelativeLayoutPreviewLeft.setBackground(ContextCompat.getDrawable(this, R.drawable.border_color_green));
                        mRelativeLayoutPreviewRight.setBackground(ContextCompat.getDrawable(this, R.drawable.border_color_green_select));
                        mCurrentTimeContent.setVisibility(View.GONE);
                        mTotalTimeContent.setVisibility(View.GONE);
                        mPosSeekBarContent.setVisibility(View.GONE);
                        mCurrentTimeMine.setVisibility(View.VISIBLE);
                        mTotalTimeMine.setVisibility(View.VISIBLE);
                        mPosSeekBarMine.setVisibility(View.VISIBLE);
                    }
                }

                break;
            default:
                break;
        }
    }

    /**
     * Update footer icons based on return from server
     */
    private void updateStatus() {
        Map<String, Object> todoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();

        if (todoContent != null) {
            boolean hasPlayAction = false;
            if (todoContent.get("has_play_action") != null) {
                hasPlayAction = (boolean) todoContent.get("has_play_action");

                // Only when has_play_action is true, then all status will be visible
                if (hasPlayAction) {
                    mStatusView.setVisibility(View.VISIBLE);
//                  mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
//                  mImageViewFooterView.setVisibility(View.VISIBLE);
//                  mTextViewFooterView.setVisibility(View.VISIBLE);
                    // TODO 点灯
                    // TODO 点灯
                    if ((todoContent.get("is_play_action_cleared") != null)
                            && ((boolean) todoContent.get("is_play_action_cleared"))) {

                    }
                }
            }

            // Check has_report_action
            if ((todoContent.get("has_report_action") != null)
                    && ((boolean) todoContent.get("has_report_action"))) {

                // 表示
                mStatusViewResport.setVisibility(View.VISIBLE);

//              mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
//              mImageViewFooterShoot.setVisibility(View.VISIBLE);
//              mTextViewFooterShoot.setVisibility(View.VISIBLE);
            }

            // check has_my_report_play_action
            if ((todoContent.get("has_my_report_play_action") != null)
                    && ((boolean) todoContent.get("has_my_report_play_action"))) {

                // 表示
                mStatusViewCheck.setVisibility(View.VISIBLE);

//               mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
//               mImageViewFooterCompare.setVisibility(View.VISIBLE);
//               mTextViewFooterCompare.setVisibility(View.VISIBLE);
            }
        }
    }

    ///// 20170521 ADD START
    @Override
    protected void onResume() {
        super.onResume();
        if (mSubmissionConfirmation == UPLOAD_SUCCESSFULL) {
            mRelativeLayoutOverlay.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mViewProgressBar.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.color.green));
            mViewProgressBar.setVisibility(View.VISIBLE);
            mImageViewSubmit.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.icon_status_complete));
            mTextViewUpload.setText(getResources().getText(R.string.report_sent_successful));
            mButtonReportSentComment.setVisibility(View.VISIBLE);
            mButtonReportSentClose.setVisibility(View.VISIBLE);
            mButtonReportSentRetry.setVisibility(View.GONE);
        } else if (mSubmissionConfirmation == UPLOAD_FAILE) {
            mRelativeLayoutOverlay.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mViewProgressBar.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.color.colorRed));
            mViewProgressBar.setVisibility(View.VISIBLE);
            mImageViewSubmit.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.icon_error));
            mTextViewUpload.setText(getResources().getText(R.string.report_sent_failed));
            mButtonReportSentComment.setVisibility(View.GONE);
            mButtonReportSentClose.setVisibility(View.VISIBLE);
            mButtonReportSentRetry.setVisibility(View.VISIBLE);
            mTextViewError.setVisibility(View.VISIBLE);
        } else {
            mRelativeLayoutOverlay.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("compareActivity", mSubmissionConfirmation);
    }
    ///// 20170523 ADD START
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }
    ///// 20170523 ADD END

    ///// 20170521 ADD END

    /**
     * Get media key to send file to S3 and send report
     */
    public class GetMediaKeyTask extends AsyncTask<String, Void, Boolean> {

        Map<String, Object> mediaKey = null;

        GetMediaKeyTask() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String cookie = params[0];

                mediaKey = MediaKey.getMediaKeyContent(cookie);
                return Boolean.TRUE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                // TODO send file to S3???

                if ((mediaKey != null)
                        && (mediaKey.get("object_key") != null)) {
                    Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                    String todoContentId = todoParameters.get("todoContentId");

                    String objectKey = (String) mediaKey.get("object_key");

                    // TODO change to right value
                    String contentType = "video/mp4";
                    String mediaURLInDevice = "//content:/....";
                    String mediaDuration = "10:00";
                    String takenAt = "2016-05-07 10:11:12";

                    // Call report API
                    new SendReportTask().execute(AndroidUtility.getCookie(getApplicationContext()),
                            objectKey, contentType, mediaURLInDevice, mediaDuration, takenAt, todoContentId);

                }
            }
        }

        @Override
        protected void onCancelled() {
        }
    }


    public class SendReportTask extends AsyncTask<String, Void, Boolean> {

        Map<String, Object> reponseData = null;

        SendReportTask() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String cookie = params[0];
                String mediaKey = params[1];
                String contentType = params[2];
                String mediaURLInDevice = params[3];
                String mediaDuration = params[4];
                String takenAt = params[5];
                String todoContentId = params[6];

                reponseData = Report.sendStudentReport(cookie, mediaKey, contentType, mediaURLInDevice, mediaDuration, takenAt, todoContentId);

                return Boolean.TRUE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            //TODO TEST
            ///// 20170523 ADD START
            success = true;
            ///// 20170523 ADD END

            ///// 20170521 MODIFY START
            if (success) {
                mProgressBar.setVisibility(View.GONE);
                mViewProgressBar.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.color.green));
                mViewProgressBar.setVisibility(View.VISIBLE);
                mImageViewSubmit.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.icon_status_complete));
                mTextViewUpload.setText(getResources().getText(R.string.report_sent_successful));
                mButtonReportSentComment.setVisibility(View.VISIBLE);
                mButtonReportSentClose.setVisibility(View.VISIBLE);
                mButtonReportSentRetry.setVisibility(View.GONE);
                mSubmissionConfirmation = UPLOAD_SUCCESSFULL;

            } else {
                mProgressBar.setVisibility(View.GONE);
                mViewProgressBar.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.color.colorRed));
                mViewProgressBar.setVisibility(View.VISIBLE);
                mImageViewSubmit.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.icon_error));
                mTextViewUpload.setText(getResources().getText(R.string.report_sent_failed));
                mButtonReportSentComment.setVisibility(View.GONE);
                mButtonReportSentClose.setVisibility(View.VISIBLE);
                mButtonReportSentRetry.setVisibility(View.VISIBLE);
                mTextViewError.setVisibility(View.VISIBLE);
                mSubmissionConfirmation = UPLOAD_FAILE;
            }
            ///// 20170521 MODIFY END

            // TODO Just show complete screen for now, need to modify later
            mRelativeLayoutOverlay.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onCancelled() {
        }
    }

    private void findViewById() {
        mCurrentTimeContent = (TextView) findViewById(R.id.current_time_tv_content);
        mCurrentTimeMine = (TextView) findViewById(R.id.current_time_tv_mine);
        mTotalTimeContent = (TextView) findViewById(R.id.total_time_tv_content);
        mTotalTimeMine = (TextView) findViewById(R.id.total_time_tv_mine);
        mPosSeekBarContent = (SeekBar) findViewById(R.id.pos_seekBar_content);
        mPosSeekBarMine = (SeekBar) findViewById(R.id.pos_seekBar_mine);
        mPlayAndPauseContent = (ImageView) findViewById(R.id.pause_img_content);
        mPlayAndPauseMine = (ImageView) findViewById(R.id.pause_img_mine);
        mChangeFullScreen = (ImageView) findViewById(R.id.change_screen);
        mImageViewSwitch = (ImageView) findViewById(R.id.imageViewSwitch);
        mRelativeLayoutPreviewLeft = (RelativeLayout) findViewById(R.id.relativeLayoutPreviewLeft);
        mRelativeLayoutPreviewRight = (RelativeLayout) findViewById(R.id.relativeLayoutPreviewRight);
        mImageViewSubmit = (ImageView) findViewById(R.id.imageViewStatusSubmit);
        mTextViewUpload = (TextView) findViewById(R.id.textViewUpload);
        setListener();
        init();

        //Default
        mCurrentTimeMine.setVisibility(View.GONE);
        mTotalTimeMine.setVisibility(View.GONE);
        mPosSeekBarMine.setVisibility(View.GONE);
        mPlayAndPauseMine.setVisibility(View.GONE);

    }

    private void setListener() {
        mPlayAndPauseContent.setOnClickListener(this);
        mPlayAndPauseMine.setOnClickListener(this);
        mChangeFullScreen.setOnClickListener(this);
        mImageViewSwitch.setOnClickListener(this);

        mPosSeekBarContent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeContent, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandlerContent.removeMessages(UPDATE_UI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mVideoViewContent.seekTo(progress);
                mHandlerContent.sendEmptyMessage(UPDATE_UI);
            }
        });
        mPosSeekBarMine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeMine, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandlerMine.removeMessages(UPDATE_UI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mVideoViewMine.seekTo(progress);
                mHandlerMine.sendEmptyMessage(UPDATE_UI);
            }
        });

    }

    private void init() {
        ViewTreeObserver viewObserverConten = mVideoViewContent.getViewTreeObserver();
        viewObserverConten.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoViewContent.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        mVideoViewContent.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(1);
                AndroidUtility.updateTextViewWithTimeFormat(mTotalTimeContent, mVideoViewContent.getDuration());
                mHandlerContent.sendEmptyMessage(UPDATE_UI);
            }
        });

        mVideoViewContent.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mHandlerContent.removeMessages(UPDATE_UI);
                mVideoViewContent.pause();
                mPlayAndPauseContent.setImageResource(R.drawable.video_start_style);
                mCurrentTimeContent.setText("00:00");
            }
        });

        ViewTreeObserver viewObserverMine = mVideoViewMine.getViewTreeObserver();
        viewObserverMine.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoViewMine.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        mVideoViewMine.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(1);
                AndroidUtility.updateTextViewWithTimeFormat(mTotalTimeMine, mVideoViewMine.getDuration());
                mHandlerMine.sendEmptyMessage(UPDATE_UI);
            }
        });

        mVideoViewMine.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mHandlerMine.removeMessages(UPDATE_UI);
                mVideoViewMine.pause();
                mPlayAndPauseMine.setImageResource(R.drawable.video_start_style);
                mCurrentTimeMine.setText("00:00");
            }
        });
    }

    public class MyHandlerContent extends Handler {
        WeakReference<Activity> mActivityReference;

        public MyHandlerContent(Activity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final Activity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case UPDATE_UI:
                        int currentPosition = mVideoViewContent.getCurrentPosition();
                        int totalPosition = mVideoViewContent.getDuration();
                        AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeContent, currentPosition);
                        mPosSeekBarContent.setMax(totalPosition);
                        mPosSeekBarContent.setProgress(currentPosition);
                        mHandlerContent.sendEmptyMessageDelayed(UPDATE_UI, 500);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public class MyHandlerMine extends Handler {
        WeakReference<Activity> mActivityReference;

        public MyHandlerMine(Activity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final Activity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case UPDATE_UI:
                        int currentPosition = mVideoViewMine.getCurrentPosition();
                        int totalPosition = mVideoViewMine.getDuration();
                        AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeMine, currentPosition);
                        mPosSeekBarMine.setMax(totalPosition);
                        mPosSeekBarMine.setProgress(currentPosition);
                        mHandlerMine.sendEmptyMessageDelayed(UPDATE_UI, 500);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public boolean getRotation() {
        int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getOrientation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return true;//"portrait"
            case Surface.ROTATION_90:
                return false;//"landscape";
            case Surface.ROTATION_180:
                return true;//"reverse portrait";
            default:
                return false;//"reverse landscape";
        }
    }
}
