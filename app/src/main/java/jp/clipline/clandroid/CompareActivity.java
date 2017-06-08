package jp.clipline.clandroid;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.Utility.PopUpDlg;
import jp.clipline.clandroid.view.StatusView;


public class CompareActivity extends BaseActivity implements View.OnClickListener, OnPageChangeListener, OnLoadCompleteListener {

    private final static String TAG = "clwebwrapperapplication";
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 99999;

    private String mTodoContentType = null;
    private Uri mTodoContentData = null;
    private LinearLayout mBackScreen;
    private ImageButton mButtonClose;
    private LinearLayout mButtonBack;

    private VideoView mVideoViewContent;
    private VideoView mVideoViewMine;
    private String mPath;
    private Map<String, Object> mCurrentTodoContent;
    private boolean mIsCheckSwitch = true; // default right

    private StatusView mStatusView;
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
    private final int UPDATE_UI_CONTENT = 1;
    private final int UPDATE_UI_MINE = 2;
    private final MyHandlerContent mHandlerContent = new MyHandlerContent(this);
    private final MyHandlerMine mHandlerMine = new MyHandlerMine(this);
    private ImageView mImageViewSwitch;
    private RelativeLayout mRelativeLayoutPreviewLeft;
    private RelativeLayout mRelativeLayoutPreviewRight;
    private Button mButtonSummit;
    private PopUpDlg mConfirDlg;

    private ImageView mImageViewContent;
    private ImageView mImageViewMine;
    private PDFView mPdfViewContent;
    private PDFView mPdfViewMine;

    private Bitmap mThumbnailContent;
    private Bitmap mThumbnailMine;

    private MediaPlayer mMediaPlayerConent;
    private MediaPlayer mMediaPlayerMine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);
        if (savedInstanceState != null) {
            mSubmissionConfirmation = savedInstanceState.getInt("compareActivity");
        }

//        mWebViewContent = (WebView) findViewById(R.id.webViewContent);
//        mWebViewMine = (WebView) findViewById(R.id.webViewMine);
        mVideoViewContent = (VideoView) findViewById(R.id.videoViewContent);
        mVideoViewMine = (VideoView) findViewById(R.id.videoViewMine);
//        mRelativeLayoutVideoController = (LinearLayout) findViewById(R.id.bottom_layout);
        mButtonFullScreen = (Button) findViewById(R.id.buttonFullScreen);
        mButtonFullScreen.setOnClickListener(this);
        mBackScreen = (LinearLayout) findViewById(R.id.backScreen);
        mButtonBack = (LinearLayout) findViewById(R.id.imageButtonBack);
        mBackScreen.setOnClickListener(this);
        mButtonBack.setOnClickListener(this);

        mImageViewContent = (ImageView) findViewById(R.id.imageViewContent);
        mImageViewMine = (ImageView) findViewById(R.id.imageViewMine);

        mPdfViewContent = (PDFView) findViewById(R.id.pdfViewContent);
        mPdfViewMine = (PDFView) findViewById(R.id.pdfViewMine);

//        mWebViewContent.getSettings().setJavaScriptEnabled(true);
//        mWebViewMine.getSettings().setJavaScriptEnabled(true);

        mTodoContentType = ((ClWebWrapperApplication) this.getApplication()).getTodoContentType();
        File file = new File(((ClWebWrapperApplication) this.getApplication()).getTodoContentData());
        Uri uriFile = Uri.fromFile(file);
        mTodoContentData = uriFile;


        mCurrentTodoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();
        TextView textView = (TextView) findViewById(R.id.textViewToDoTitle);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfirDlg = new PopUpDlg(CompareActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(CompareActivity.this, SelectShootingMethodActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        },
                        // onCancel
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });

            }
        });

        mIsCheckSwitch = true;
        if (mCurrentTodoContent != null && mCurrentTodoContent.get("title") != null) {
            textView.setText((String) mCurrentTodoContent.get("title"));
        } else {
            textView.setText("");
        }
        try {
            String path = AndroidUtility.getFilePath(this, mTodoContentData);
            mPath = "file://" + path;
            if (mTodoContentType.equals("image/png")) {
                mImageViewMine.setVisibility(View.VISIBLE);
                mPdfViewMine.setVisibility(View.GONE);
                mVideoViewMine.setVisibility(View.GONE);
                mButtonFullScreen.setVisibility(View.INVISIBLE);

                ExifInterface exif = null;
                try {
                    exif = new ExifInterface(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                float rotate = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                Picasso.with(this)
                        .load(mPath)
                        .rotate(rotate)
                        .into(mImageViewMine);

            } else if (mTodoContentType.equals("video/mp4")) {
                mImageViewMine.setVisibility(View.VISIBLE);
                mPdfViewMine.setVisibility(View.GONE);
                mVideoViewMine.setVisibility(View.GONE);
                mButtonFullScreen.setVisibility(View.GONE);
                mVideoViewMine.setVideoPath(mPath);
                mThumbnailMine = ThumbnailUtils.createVideoThumbnail(path,
                        MediaStore.Images.Thumbnails.MINI_KIND);
                mImageViewMine.setImageBitmap(mThumbnailMine);
                if (!getRotation()) {
                    mVideoViewMine.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            mIsCheckSwitch = true;
                            mRelativeLayoutPreviewLeft.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.border_color_green));
                            mRelativeLayoutPreviewRight.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.border_color_green_select));
                            return false;
                        }
                    });
                    mImageViewMine.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mIsCheckSwitch = true;
                            mRelativeLayoutPreviewLeft.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.border_color_green));
                            mRelativeLayoutPreviewRight.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.border_color_green_select));
                        }
                    });
                }

            } else {
                mImageViewMine.setVisibility(View.GONE);
                mPdfViewMine.setVisibility(View.VISIBLE);
                mVideoViewMine.setVisibility(View.GONE);
                mButtonFullScreen.setVisibility(View.INVISIBLE);
                mPdfViewMine.fromUri(Uri.parse(mPath))
                        .defaultPage(0)
                        .onPageChange(this)
                        .enableAnnotationRendering(true)
                        .onLoad(this)
                        .scrollHandle(new DefaultScrollHandle(this))
                        .load();
            }

            if (mCurrentTodoContent != null) {
                final boolean isVideo = (boolean) mCurrentTodoContent.get("is_video");
                final boolean isImage = (boolean) mCurrentTodoContent.get("is_image");
                boolean isPdf = (boolean) mCurrentTodoContent.get("is_pdf");

                if (isVideo) {
                    mImageViewContent.setVisibility(View.VISIBLE);
                    mPdfViewContent.setVisibility(View.GONE);
                    mVideoViewContent.setVisibility(View.INVISIBLE);
                    mButtonFullScreen.setVisibility(View.GONE);
                    //thumbnail
                    Picasso.with(this)
                            .load(String.valueOf(mCurrentTodoContent.get("media_thumb_pre_signed_url")))
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    mThumbnailContent = bitmap;
                                    mImageViewContent.setImageBitmap(bitmap);
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });
                    mVideoViewContent.setVideoPath((String) mCurrentTodoContent.get("pre_signed_standard_mp4_url"));
                    if (!getRotation()) {
                        mVideoViewContent.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                mIsCheckSwitch = false;
                                mRelativeLayoutPreviewLeft.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.border_color_green_select));
                                mRelativeLayoutPreviewRight.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.border_color_green));
                                return false;
                            }
                        });
                        mImageViewContent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mIsCheckSwitch = false;
                                mRelativeLayoutPreviewLeft.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.border_color_green_select));
                                mRelativeLayoutPreviewRight.setBackground(ContextCompat.getDrawable(CompareActivity.this, R.drawable.border_color_green));
                            }
                        });
                    }

                } else if (isImage) { //TODO contact (media_thumb_pre_signed_url)
                    mImageViewContent.setVisibility(View.VISIBLE);
                    mPdfViewContent.setVisibility(View.GONE);
                    mVideoViewContent.setVisibility(View.GONE);
//                    mRelativeLayoutVideoController.setVisibility(View.GONE);
                    mButtonFullScreen.setVisibility(View.INVISIBLE);

                    Picasso.with(this)
                            .load(String.valueOf(mCurrentTodoContent.get("media_thumb_pre_signed_url")))
                            .into(mImageViewContent);
                } else if (isPdf) {
                    mImageViewContent.setVisibility(View.GONE);
                    mPdfViewContent.setVisibility(View.VISIBLE);
                    mVideoViewContent.setVisibility(View.GONE);
//                    mRelativeLayoutVideoController.setVisibility(View.GONE);
                    mButtonFullScreen.setVisibility(View.INVISIBLE);
                    mPdfViewContent.fromUri(Uri.parse(String.valueOf(mCurrentTodoContent.get("media_thumb_pre_signed_url"))))
                            .defaultPage(0)
                            .onPageChange(this)
                            .enableAnnotationRendering(true)
                            .onLoad(this)
                            .scrollHandle(new DefaultScrollHandle(this))
                            .load();
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

        mStatusView.setTypeView(StatusView.STATUS_VIEW.COMPARE);
        mStatusView.setClickListener(new StatusView.ClickListener() {
            @Override
            public void onListenerView() {
                mConfirDlg = new PopUpDlg(CompareActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                                String id = todoParameters.get("id");
                                String categoryId = todoParameters.get("categoryId");
                                String todoContentId = todoParameters.get("todoContentId");
                                Boolean isStudent = "student".equals(todoParameters.get("loginType"));

                                String url;

                                if (isStudent) {
                                    url = "%s://%s/training/#/students/" + id
                                            + "/todos/" + todoContentId;
                                } else {
                                    url = "%s://%s/training/#/coachs/" + id
                                            + "/todos/" + todoContentId;
                                }

                                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                                intent.putExtra("BASE_URL", url);
                                startActivity(intent);
                                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                                finish();
                            }
                        },
                        // onCancel
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });
            }

            @Override
            public void onListenerReport() {
                mConfirDlg = new PopUpDlg(CompareActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(CompareActivity.this, SubmissionConfirmationActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        },
                        // onCancel
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });
            }
        });

        updateStatus();

        // この内容で提出
        mButtonSummit = (Button) findViewById(R.id.buttonSubmit);
        mButtonSummit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handlerUpload();

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
        boolean isPortrait = getRotation();
        switch (v.getId()) {
            case R.id.backScreen:
            case R.id.imageButtonBack:
                mConfirDlg = new PopUpDlg(CompareActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(CompareActivity.this, SelectShootingMethodActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        },
                        // onCancel
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null) {
                                    dialog.dismiss();
                                }
                            }
                        });
                break;
            case R.id.pause_img_content:
            case R.id.pause_img_mine:
                if (!mTodoContentType.equals("video/mp4")) {
                    return;
                }
                mImageViewContent.setVisibility(View.GONE);
                mVideoViewContent.setVisibility(View.VISIBLE);

                mImageViewMine.setVisibility(View.GONE);
                mVideoViewMine.setVisibility(View.VISIBLE);
                if (mVideoViewContent.isPlaying() || mVideoViewMine.isPlaying()) {
                    mPlayAndPauseContent.setImageResource(R.drawable.video_start_style);
                    mPlayAndPauseMine.setImageResource(R.drawable.video_start_style);
                    mVideoViewContent.pause();
                    mVideoViewMine.pause();
                    mHandlerContent.removeMessages(UPDATE_UI_CONTENT);
                    mHandlerMine.removeMessages(UPDATE_UI_MINE);

                } else {
                    mPlayAndPauseContent.setImageResource(R.drawable.video_stop_style);
                    mPlayAndPauseMine.setImageResource(R.drawable.video_stop_style);
                    mVideoViewContent.start();
                    mVideoViewMine.start();
                    mHandlerContent.sendEmptyMessage(UPDATE_UI_CONTENT);
                    mHandlerMine.sendEmptyMessage(UPDATE_UI_MINE);
                }
                break;
            case R.id.buttonFullScreen:
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
                if (!mTodoContentType.equals("video/mp4")) {
                    return;
                }


                if (mIsCheckSwitch) {
                    mIsCheckSwitch = false;
                    if (!isPortrait) {
                        mRelativeLayoutPreviewLeft.setBackground(ContextCompat.getDrawable(this, R.drawable.border_color_green_select));
                        mRelativeLayoutPreviewRight.setBackground(ContextCompat.getDrawable(this, R.drawable.border_color_green));
//                        mMediaPlayerConent.setVolume(100f, 100f);
//                        mMediaPlayerMine.setVolume(0f, 0f);
                        return;
                    }
                    mVideoViewContent.stopPlayback();
                    mVideoViewMine.stopPlayback();
                    mPlayAndPauseContent.setImageResource(R.drawable.video_start_style);
                    mPlayAndPauseMine.setImageResource(R.drawable.video_start_style);

                    mImageViewContent.setImageBitmap(mThumbnailMine);
                    mImageViewMine.setImageBitmap(mThumbnailContent);

                    mImageViewContent.setVisibility(View.VISIBLE);
                    mImageViewMine.setVisibility(View.VISIBLE);
                    mVideoViewContent.setVisibility(View.GONE);
                    mVideoViewMine.setVisibility(View.GONE);

                    mVideoViewContent.setVideoPath(mPath);
                    mVideoViewMine.setVideoPath(String.valueOf(mCurrentTodoContent.get("pre_signed_standard_mp4_url")));

                    mCurrentTimeContent.setVisibility(View.VISIBLE);
                    mTotalTimeContent.setVisibility(View.VISIBLE);
                    mPosSeekBarContent.setVisibility(View.VISIBLE);
                    mCurrentTimeMine.setVisibility(View.GONE);
                    mTotalTimeMine.setVisibility(View.GONE);
                    mPosSeekBarMine.setVisibility(View.GONE);

                    mCurrentTimeContent.setText("00:00");
                    mTotalTimeContent.setText("00:00");
                    mCurrentTimeMine.setText("00:00");
                    mTotalTimeMine.setText("00:00");


                } else {
                    mIsCheckSwitch = true;
                    if (!isPortrait) {
                        mRelativeLayoutPreviewLeft.setBackground(ContextCompat.getDrawable(this, R.drawable.border_color_green));
                        mRelativeLayoutPreviewRight.setBackground(ContextCompat.getDrawable(this, R.drawable.border_color_green_select));
//                        mMediaPlayerConent.setVolume(0f, 0f);
//                        mMediaPlayerMine.setVolume(100f, 100f);
                        return;
                    }
                    mVideoViewContent.stopPlayback();
                    mVideoViewMine.stopPlayback();
                    mPlayAndPauseContent.setImageResource(R.drawable.video_start_style);
                    mPlayAndPauseMine.setImageResource(R.drawable.video_start_style);

                    mImageViewContent.setImageBitmap(mThumbnailContent);
                    mImageViewMine.setImageBitmap(mThumbnailMine);

                    mImageViewContent.setVisibility(View.VISIBLE);
                    mImageViewMine.setVisibility(View.VISIBLE);
                    mVideoViewContent.setVisibility(View.GONE);
                    mVideoViewMine.setVisibility(View.GONE);

                    mVideoViewContent.setVideoPath(String.valueOf(mCurrentTodoContent.get("pre_signed_standard_mp4_url")));
                    mVideoViewMine.setVideoPath(mPath);

                    mCurrentTimeContent.setVisibility(View.GONE);
                    mTotalTimeContent.setVisibility(View.GONE);
                    mPosSeekBarContent.setVisibility(View.GONE);
                    mCurrentTimeMine.setVisibility(View.VISIBLE);
                    mTotalTimeMine.setVisibility(View.VISIBLE);
                    mPosSeekBarMine.setVisibility(View.VISIBLE);

                    mCurrentTimeContent.setText("00:00");
                    mTotalTimeContent.setText("00:00");
                    mCurrentTimeMine.setText("00:00");
                    mTotalTimeMine.setText("00:00");
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
            }

            // check has_my_report_play_action
            if ((todoContent.get("has_my_report_play_action") != null)
                    && ((boolean) todoContent.get("has_my_report_play_action"))) {

                // 表示
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSubmissionConfirmation == UPLOAD_SUCCESSFULL) {
            UploadSuccessfull();
        } else if (mSubmissionConfirmation == UPLOAD_FAILE) {
            UploadFaile();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("compareActivity", mSubmissionConfirmation);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
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
//        setListener();
        init();

        //Default
        mCurrentTimeContent.setVisibility(View.GONE);
        mTotalTimeContent.setVisibility(View.GONE);
        mPosSeekBarContent.setVisibility(View.GONE);
        mPlayAndPauseContent.setVisibility(View.GONE);

        mCurrentTimeMine.setVisibility(View.VISIBLE);
        mTotalTimeMine.setVisibility(View.VISIBLE);
        mPosSeekBarMine.setVisibility(View.VISIBLE);
        mPlayAndPauseMine.setVisibility(View.VISIBLE);

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
                mHandlerContent.sendEmptyMessage(UPDATE_UI_CONTENT);
            }
        });

        mVideoViewContent.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mMediaPlayerConent = mediaPlayer;
                mHandlerContent.removeMessages(UPDATE_UI_CONTENT);
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
                mHandlerMine.sendEmptyMessage(UPDATE_UI_MINE);
            }
        });

        mVideoViewMine.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mMediaPlayerMine = mediaPlayer;
                mHandlerMine.removeMessages(UPDATE_UI_MINE);
                mPlayAndPauseMine.setImageResource(R.drawable.video_start_style);
                mCurrentTimeMine.setText("00:00");
            }
        });

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
                mHandlerContent.removeMessages(UPDATE_UI_CONTENT);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mVideoViewContent.seekTo(progress);
                mHandlerContent.sendEmptyMessage(UPDATE_UI_CONTENT);
            }
        });
        mPosSeekBarMine.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeMine, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandlerMine.removeMessages(UPDATE_UI_MINE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mVideoViewMine.seekTo(progress);
                mHandlerMine.sendEmptyMessage(UPDATE_UI_MINE);
            }
        });
    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

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
                    case UPDATE_UI_CONTENT:
                        int currentPosition = mVideoViewContent.getCurrentPosition();
                        int totalPosition = mVideoViewContent.getDuration();
                        AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeContent, currentPosition);
                        mPosSeekBarContent.setMax(totalPosition);
                        mPosSeekBarContent.setProgress(currentPosition);
                        mHandlerContent.sendEmptyMessageDelayed(UPDATE_UI_CONTENT, 500);
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
                    case UPDATE_UI_MINE:
                        int currentPosition = mVideoViewMine.getCurrentPosition();
                        int totalPosition = mVideoViewMine.getDuration();
                        AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeMine, currentPosition);
                        mPosSeekBarMine.setMax(totalPosition);
                        mPosSeekBarMine.setProgress(currentPosition);
                        mHandlerMine.sendEmptyMessageDelayed(UPDATE_UI_MINE, 500);
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
