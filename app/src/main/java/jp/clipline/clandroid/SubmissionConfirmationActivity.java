package jp.clipline.clandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.api.MediaKey;
import jp.clipline.clandroid.api.Report;
import jp.clipline.clandroid.view.FullVideo;
import jp.clipline.clandroid.view.StatusView;

public class SubmissionConfirmationActivity extends AppCompatActivity implements View.OnClickListener {

    private String mTodoContentType = null;
    private Uri mTodoContentData = null;
    private WebView mWebView;

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

    //VIDEO
    private FullVideo mVideoView;
    private TextView mCurrentTimeTv;
    private TextView mTotalTimeTv;
    private SeekBar mPosSeekBar;
    private SeekBar mVolumeSeekBar;
    private ImageView mPlayAndPause;
    private ImageView mChangeFullScreen;
    private AudioManager mAudioManager;
    private int currentVolume;
    private int maxVolume;
    private final int SEEKTOTIME = 1111;
    private final int UPDATE_UI = 1;
    private final MyHandler mHandler = new MyHandler(this);

    private Button mButtonCompareToModel;
    //    private Button mButtonCompare;
//    private Button mButtonSummit;
    private Button mImageButtonCompareOrSubmit;
    private boolean mHasMyReportPlayAction = false;
    private RelativeLayout mRelativeLayoutContentVideo;
    private ImageView mImageViewSubmit;
    private TextView mTextViewUpload;
    private Button mButtonReportSentComment;
    private Button mButtonReportSentClose;
    private Button mButtonReportSentRetry;
    private TextView mTextViewError;
    private ProgressBar mProgressBar;
    private View mViewProgressBar;

    private RelativeLayout mRelativeLayoutOverlay;
    ///// 20170521 ADD START
    private final int UPLOAD_NONE = 0;
    private final int UPLOAD_SUCCESSFULL = 1;
    private final int UPLOAD_FAILE = 2;
    private int mSubmissionConfirmation = 0;
    ///// 20170521 ADD END

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_confirmation);
        ///// 20170521 ADD START
        if (savedInstanceState != null) {
            mSubmissionConfirmation = savedInstanceState.getInt("submissionConfirmation");
        }
        ///// 20170521 ADD START
        mTodoContentType = ((ClWebWrapperApplication) this.getApplication()).getTodoContentType();
        File file = new File(((ClWebWrapperApplication) this.getApplication()).getTodoContentData());
        Uri uriFile = Uri.fromFile(file);
        mTodoContentData = uriFile;
//        ImageButton imageButton;
        ImageView imageView;
        TextView textView;

        // レポート完成画面
        mRelativeLayoutOverlay = (RelativeLayout) findViewById(R.id.relativeLayoutOverlay);
        mRelativeLayoutOverlay.setVisibility(View.GONE);
        ///// 20170521 DELETE START
//        imageButton = (ImageButton) findViewById(R.id.imageButtonReportSentClose);
//        imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mRelativeLayoutOverlay.setVisibility(View.GONE);
//            }
//        });
        ///// 20170521 DELETE END
        ///// 20170520 ADD START
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
        ///// 20170520 ADD END

        ///// 20170521 MODIFY START
        // レポート完成：もどるボタン
        mButtonReportSentClose = (Button) findViewById(R.id.buttonReportSentClose);
        mButtonReportSentClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            }
        });
///// 20170521 MODIFY END
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
                ///// 20170523 ADD START
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                ///// 20170523 ADD END
                finish();
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);

        findViewById();
//        VideoView videoView = (VideoView) findViewById(R.id.videoView);
        mWebView = (WebView) findViewById(R.id.webView);
        if (mTodoContentType.equals("image/png")) {
            // 画像が撮影or選択された場合
            imageView.setImageURI(mTodoContentData);
//            videoView.setVisibility(View.GONE);
            mWebView.setVisibility(View.GONE);
            mRelativeLayoutContentVideo.setVisibility(View.GONE);
        } else if (mTodoContentType.equals("video/mp4")) {
            // 動画が撮影or選択された場合
            playVideo(mTodoContentData);
//            videoView.setVideoURI(mTodoContentData);
//            videoView.start();
            imageView.setVisibility(View.GONE);
            mWebView.setVisibility(View.GONE);
        } else {
            try {
                String path = "file:///" + AndroidUtility.getFilePath(this, mTodoContentData);
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.loadUrl(path);
                mWebView.setVisibility(View.VISIBLE);
//                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }

        ///// 20170523 MODIFY START
        // 戻るボタン
        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButtonBack);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
///*
                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                finish();
//*/
//                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
//                String studentId = todoParameters.get("studentId");
//                String categoryId = todoParameters.get("categoryId");
//                String todoContentId = todoParameters.get("todoContentId");
//                String url = "%s://%s/training/#/students/" + studentId
//                        + "/todos/" + todoContentId;
//
//                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
//                intent.putExtra("BASE_URL", url);
//                startActivity(intent);
//                finish();
//
            }
        });
        ///// 20170523 MODIFY END

//        textView = (TextView) findViewById(R.id.textViewTodoBack);
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
///*
//                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
//                startActivity(intent);
//                finish();
//*/
//                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
//                String studentId = todoParameters.get("studentId");
//                String categoryId = todoParameters.get("categoryId");
//                String todoContentId = todoParameters.get("todoContentId");
//                String url = "%s://%s/training/#/students/" + studentId
//                        + "/todos/" + todoContentId;
//
//                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
//                intent.putExtra("BASE_URL", url);
//                startActivity(intent);
//                finish();
//            }
//        });

        mImageButtonCompareOrSubmit = (Button) findViewById(R.id.buttonCompareOrSubmit);
        mImageButtonCompareOrSubmit.setOnClickListener(this);
        ///// 20170520 DELETE START
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
        // 一覧へ戻る
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


//            }
//        });
        ///// 20170520 DELETE END

        // やり直す: back to SelectShooting
        textView = (TextView) findViewById(R.id.textViewBack);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                startActivity(intent);
                ///// 20170523 MODIFY START
//                overridePendingTransition(R.anim.slide_out, R.anim.slide_in);
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                ///// 20170523 MODIFY END
                finish();
            }
        });

        // Statuses on Footer
        mStatusView = (StatusView) findViewById(R.id.statusView);
        mStatusViewResport = (StatusView) findViewById(R.id.statusResport);
        mStatusViewCheck = (StatusView) findViewById(R.id.statusCheck);

        mStatusView.setTypeView(StatusView.STATUS_VIEW.VIEW, true);
        mStatusViewResport.setTypeView(StatusView.STATUS_VIEW.REPORT, true);
        mStatusViewCheck.setTypeView(StatusView.STATUS_VIEW.CHECK, true);

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

        // お手本を見比べる
        mButtonCompareToModel = (Button) findViewById(R.id.buttonCompareWithModel);
        mButtonCompareToModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CompareActivity.class);
                startActivity(intent);
    ///// 20170523 MODIFY START
//                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                ///// 20170523 MODIFY END
                finish();

            }
        });

        ///// 20170520 DELETE START
        // 見比べる : ToCompare
//        mButtonCompare = (Button) findViewById(R.id.buttonCompare);/
//        mButtonCompare.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplicationContext(), CompareActivity.class);
//                startActivity(intent);
//
//                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
//
//                finish();
//            }
//        });

        // この内容で提出
//        mButtonSummit = (Button) findViewById(R.id.buttonSubmit);
//        mButtonSummit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // First, get media key
//                // On post execute, upload file to S3 and call report submit api
//                new GetMediaKeyTask().execute(AndroidUtility.getCookie(getApplicationContext()));
//            }
//        });

        ///// 20170520 DELETE END

        // 見比べる有／無によりボタン表示／非表示設定
        updateButtonVisible();

        Map<String, Object> currentTodoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();
        textView = (TextView) findViewById(R.id.textViewToDoTitle);

        if (currentTodoContent != null && currentTodoContent.get("title") != null) {
            textView.setText((String) currentTodoContent.get("title"));
        } else {
            textView.setText("");
        }
        ///// 20170520 MODIFY START
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                startActivity(intent);
                ///// 20170523 MODIFY START
//                overridePendingTransition(R.anim.slide_out, R.anim.slide_in);
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                ///// 20170523 MODIFY END
                finish();
            }
        });
        ///// 20170520 MODIFY END
    }

    private void findViewById() {
        mVideoView = (FullVideo) findViewById(R.id.video_view);
        mCurrentTimeTv = (TextView) findViewById(R.id.current_time_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.total_time_tv);
        mPosSeekBar = (SeekBar) findViewById(R.id.pos_seekBar);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.volume_seek);
        mPlayAndPause = (ImageView) findViewById(R.id.pause_img);
        mChangeFullScreen = (ImageView) findViewById(R.id.change_screen);
        mRelativeLayoutContentVideo = (RelativeLayout) findViewById(R.id.relativeLayoutContentVideo);
        mImageViewSubmit = (ImageView) findViewById(R.id.imageViewStatusSubmit);
        mTextViewUpload = (TextView) findViewById(R.id.textViewUpload);
        setListener();
        init();
    }

    private void setListener() {
        mPlayAndPause.setOnClickListener(this);
        mChangeFullScreen.setOnClickListener(this);

        mPosSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeTv, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeMessages(UPDATE_UI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                mVideoView.seekTo(progress);
                mHandler.sendEmptyMessage(UPDATE_UI);
            }
        });

        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void init() {
        ViewTreeObserver viewObserver = mVideoView.getViewTreeObserver();
        viewObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mVideoView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVolumeSeekBar.setMax(maxVolume);
        mVolumeSeekBar.setProgress(currentVolume);


    }

    private void playVideo(Uri uri) {
//        mVideoView.setVideoPath(path);
//        mVideoView.setVideoURI(Uri.parse(path));
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                mVideoView.start();
                AndroidUtility.updateTextViewWithTimeFormat(mTotalTimeTv, mVideoView.getDuration());
                mHandler.sendEmptyMessage(UPDATE_UI);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mHandler.removeMessages(UPDATE_UI);
                mVideoView.pause();
                mPlayAndPause.setImageResource(R.drawable.video_start_style);
                mPosSeekBar.setProgress(0);
                mCurrentTimeTv.setText("00:00");
            }
        });

    }

    public class MyHandler extends Handler {
        WeakReference<Activity> mActivityReference;

        public MyHandler(Activity activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final Activity activity = mActivityReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case UPDATE_UI:
                        int currentPosition = mVideoView.getCurrentPosition();
                        int totalPosition = mVideoView.getDuration();
                        AndroidUtility.updateTextViewWithTimeFormat(mCurrentTimeTv, currentPosition);
                        mPosSeekBar.setMax(totalPosition);
                        mPosSeekBar.setProgress(currentPosition);
                        mHandler.sendEmptyMessageDelayed(UPDATE_UI, 500);
                        break;
                    default:
                        break;
                }
            }
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

    private void updateButtonVisible() {
        ///// 20170520 MODIFY START
        Map<String, Object> todoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();

//        boolean hasMyReportPlayAction = false;

        if ((todoContent != null)
                && (todoContent.get("has_my_report_play_action") != null)) {
            mHasMyReportPlayAction = ((boolean) todoContent.get("has_my_report_play_action"));
        }
        //TODO HARDCODE TEST
        mHasMyReportPlayAction = false;

        // TODO 見比べる有???
        if (mHasMyReportPlayAction) {
            mButtonCompareToModel.setVisibility(View.GONE);
            mImageButtonCompareOrSubmit.setText(getResources().getText(R.string.select_shooting_method_compare));
//            mButtonSummit.setVisibility(View.GONE);
//            mButtonCompare.setVisibility(View.VISIBLE);
        } else {
            mButtonCompareToModel.setVisibility(View.VISIBLE);
            mImageButtonCompareOrSubmit.setText(getResources().getText(R.string.select_shooting_method_submit));
//            mButtonSummit.setVisibility(View.VISIBLE);
//            mButtonCompare.setVisibility(View.GONE);
        }
        ///// 20170520 MODIFY END
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.pause_img:
                if (mVideoView.isPlaying()) {
                    mPlayAndPause.setImageResource(R.drawable.video_start_style);
                    mVideoView.pause();
                    mHandler.removeMessages(UPDATE_UI);
                } else {
                    mPlayAndPause.setImageResource(R.drawable.video_stop_style);
                    mVideoView.start();
                    mHandler.sendEmptyMessage(UPDATE_UI);
                }
                break;
            case R.id.change_screen:
                intent = new Intent(this, FullVideoActivity.class);
                intent.putExtra("seekto", mVideoView.getDuration());
                startActivityForResult(intent, SEEKTOTIME);

                break;
            ///// 20170520 MODIFY START
            case R.id.buttonCompareOrSubmit:
                if (mHasMyReportPlayAction) { // check
                    intent = new Intent(getApplicationContext(), CompareActivity.class);
                    startActivity(intent);
                    ///// 20170523 MODIFY START
//                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    ///// 20170523 MODIFY END
                    finish();
                } else { // post
                    // First, get media key
                    // On post execute, upload file to S3 and call report submit api
                    ///// 20170523 MODIFY START
                    mRelativeLayoutOverlay.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    mTextViewUpload.setText(getResources().getText(R.string.report_sent));
                    mImageViewSubmit.setBackground(null);
                    mTextViewError.setVisibility(View.GONE);
                    mViewProgressBar.setVisibility(View.GONE);
                    mButtonReportSentComment.setVisibility(View.GONE);
                    mButtonReportSentRetry.setVisibility(View.GONE);
                    mButtonReportSentClose.setVisibility(View.GONE);
                    new GetMediaKeyTask().execute(AndroidUtility.getCookie(getApplicationContext()));
                    ///// 20170523 MODIFY END
                }
                break;
            ///// 20170520 MODIFY END
            default:
                break;
        }
    }

    ///// 20170521 ADD START
    @Override
    protected void onResume() {
        super.onResume();
        if (mSubmissionConfirmation == UPLOAD_SUCCESSFULL) {
            mRelativeLayoutOverlay.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mViewProgressBar.setBackground(ContextCompat.getDrawable(SubmissionConfirmationActivity.this, R.color.green));
            mViewProgressBar.setVisibility(View.VISIBLE);
            mImageViewSubmit.setBackground(ContextCompat.getDrawable(SubmissionConfirmationActivity.this, R.drawable.icon_status_complete));
            mTextViewUpload.setText(getResources().getText(R.string.report_sent_successful));
            mButtonReportSentComment.setVisibility(View.VISIBLE);
            mButtonReportSentClose.setVisibility(View.VISIBLE);
            mButtonReportSentRetry.setVisibility(View.GONE);
        } else if (mSubmissionConfirmation == UPLOAD_FAILE) {
            mRelativeLayoutOverlay.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            mViewProgressBar.setBackground(ContextCompat.getDrawable(SubmissionConfirmationActivity.this, R.color.colorRed));
            mViewProgressBar.setVisibility(View.VISIBLE);
            mImageViewSubmit.setBackground(ContextCompat.getDrawable(SubmissionConfirmationActivity.this, R.drawable.icon_error));
            mTextViewUpload.setText(getResources().getText(R.string.report_sent_failed));
            mButtonReportSentComment.setVisibility(View.GONE);
            mButtonReportSentClose.setVisibility(View.VISIBLE);
            mButtonReportSentRetry.setVisibility(View.VISIBLE);
            mTextViewError.setVisibility(View.VISIBLE);
        } else {
            mRelativeLayoutOverlay.setVisibility(View.GONE);
        }
    }

    ///// 20170521 ADD END
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SEEKTOTIME) {
            if (resultCode == Activity.RESULT_OK) {
                mPlayAndPause.setImageResource(R.drawable.video_start_style);
                int seek = data.getIntExtra("seekto", 0);
                mVideoView.seekTo(seek);
            }
        }
    }

    ///// 20170521 ADD START
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("submissionConfirmation", mSubmissionConfirmation);
    }
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
                mViewProgressBar.setBackground(ContextCompat.getDrawable(SubmissionConfirmationActivity.this, R.color.green));
                mViewProgressBar.setVisibility(View.VISIBLE);
                mImageViewSubmit.setBackground(ContextCompat.getDrawable(SubmissionConfirmationActivity.this, R.drawable.icon_status_complete));
                mTextViewUpload.setText(getResources().getText(R.string.report_sent_successful));
                mButtonReportSentComment.setVisibility(View.VISIBLE);
                mButtonReportSentClose.setVisibility(View.VISIBLE);
                mButtonReportSentRetry.setVisibility(View.GONE);
                mSubmissionConfirmation = UPLOAD_SUCCESSFULL;

            } else {
                mProgressBar.setVisibility(View.GONE);
                mViewProgressBar.setBackground(ContextCompat.getDrawable(SubmissionConfirmationActivity.this, R.color.colorRed));
                mViewProgressBar.setVisibility(View.VISIBLE);
                mImageViewSubmit.setBackground(ContextCompat.getDrawable(SubmissionConfirmationActivity.this, R.drawable.icon_error));
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

}
