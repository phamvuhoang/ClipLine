package jp.clipline.clandroid;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.Utility.PopUpDlg;
import jp.clipline.clandroid.view.StatusView;

public class SubmissionConfirmationActivity extends BaseActivity implements View.OnClickListener, OnPageChangeListener, OnLoadCompleteListener {

    private StatusView mStatusView;
    private TextView mTextViewCompareToModel;
    private Button mImageButtonCompareOrSubmit;
    private boolean mHasMyReportPlayAction = false;
    private PopUpDlg mConfirDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_confirmation);
        if (savedInstanceState != null) {
            mSubmissionConfirmation = savedInstanceState.getInt("submissionConfirmation");
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mIsPlay = bundle.getBoolean("isPlaying");
            mCurrentTime = bundle.getInt("currentTime");

        }

        findViewByIdVideo();
        setListener();

        mHandler = new MyHandler(this);
        // 戻るボタン
        LinearLayout backScreen = (LinearLayout) findViewById(R.id.imageButtonBack);
        backScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mConfirDlg = new PopUpDlg(SubmissionConfirmationActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
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
        });

        mImageButtonCompareOrSubmit = (Button) findViewById(R.id.buttonCompareOrSubmit);
        mImageButtonCompareOrSubmit.setOnClickListener(this);
        LinearLayout llRetry = (LinearLayout) findViewById(R.id.textViewBack);
        llRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfirDlg = new PopUpDlg(SubmissionConfirmationActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
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
        });

        // Statuses on Footer
        mStatusView = (StatusView) findViewById(R.id.statusView);
        mStatusView.setTypeView(StatusView.STATUS_VIEW.SUBMISS);
        mStatusView.setClickListener(new StatusView.ClickListener() {

            @Override
            public void onListenerView() {
                mConfirDlg = new PopUpDlg(SubmissionConfirmationActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                                String studentId = todoParameters.get("studentId");
                                String categoryId = todoParameters.get("categoryId");
                                String todoContentId = todoParameters.get("todoContentId");
                                String url = "%s://%s/training/#/students/" + studentId
                                        + "/todos/" + todoContentId;
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

            }
        });


        updateStatus();

        // お手本を見比べる
        mTextViewCompareToModel = (TextView) findViewById(R.id.buttonCompareWithModel);
        mTextViewCompareToModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CompareActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                finish();

            }
        });

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


        // 見比べる有／無によりボタン表示／非表示設定
        updateButtonVisible();

        Map<String, Object> currentTodoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();
        TextView textView = (TextView) findViewById(R.id.textViewToDoTitle);

        if (currentTodoContent != null && currentTodoContent.get("title") != null) {
            textView.setText((String) currentTodoContent.get("title"));
        } else {
            textView.setText("");
        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConfirDlg = new PopUpDlg(SubmissionConfirmationActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
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
        });

        showDisplay();

    }

    private void showDisplay() {
        if (mTodoContentType.equals("image/png")) {
            // 画像が撮影or選択された場合
            mImageView.setVisibility(View.VISIBLE);
            mRelativeLayoutContentVideo.setVisibility(View.GONE);
            mPdfView.setVisibility(View.GONE);
            mButtonFullScreen.setVisibility(View.VISIBLE);
            try {
                String path = AndroidUtility.getFilePath(this, mTodoContentData);
                ExifInterface exif = new ExifInterface(path);
                float rotate = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                Log.e("rotate", String.valueOf(rotate));
                mImageView.setImageURI(mTodoContentData);
                mImageView.setRotation(rotate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mTodoContentType.equals("video/mp4")) {
            // 動画が撮影or選択された場合
            mImageView.setVisibility(View.VISIBLE);
            mRelativeLayoutContentVideo.setVisibility(View.VISIBLE);
            mPdfView.setVisibility(View.GONE);
            mButtonFullScreen.setVisibility(View.GONE);
            mRelativeLayoutVideoController.setVisibility(View.VISIBLE);
//            String path = null;
//            try {
//                path = AndroidUtility.getFilePath(this, mTodoContentData);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
//            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(path,
//                    MediaStore.Images.Thumbnails.MINI_KIND);
//            mImageView.setImageBitmap(thumb);
            playVideo(mTodoContentData);
        } else { //content type is pdf
            try {
                mImageView.setVisibility(View.GONE);
                mRelativeLayoutContentVideo.setVisibility(View.GONE);
                mPdfView.setVisibility(View.VISIBLE);
                mButtonFullScreen.setVisibility(View.VISIBLE);
                String path = AndroidUtility.getFilePath(this, mTodoContentData);
                mPdfView.fromFile(new File(path))
                        .defaultPage(0)
                        .onPageChange(this)
                        .enableAnnotationRendering(true)
                        .onLoad(this)
                        .scrollHandle(new DefaultScrollHandle(this))
                        .load();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mButtonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SubmissionConfirmationActivity.this, FullVideoActivity.class);
                startActivity(intent);
            }
        });

        mPlayAndPause.setOnClickListener(this);
        mChangeFullScreen.setOnClickListener(this);
    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

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
//              mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
//              mImageViewFooterShoot.setVisibility(View.VISIBLE);
//              mTextViewFooterShoot.setVisibility(View.VISIBLE);
            }

            // check has_my_report_play_action
            if ((todoContent.get("has_my_report_play_action") != null)
                    && ((boolean) todoContent.get("has_my_report_play_action"))) {

                // 表示
//               mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
//               mImageViewFooterCompare.setVisibility(View.VISIBLE);
//               mTextViewFooterCompare.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateButtonVisible() {
        Map<String, Object> todoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();

//        boolean hasMyReportPlayAction = false;

        if ((todoContent != null)
                && (todoContent.get("has_my_report_play_action") != null)) {
            mHasMyReportPlayAction = ((boolean) todoContent.get("has_my_report_play_action"));
        }
        //TODO HARDCODE TEST
        //mHasMyReportPlayAction = false;

        // TODO 見比べる有???
        if (mHasMyReportPlayAction) {
            mTextViewCompareToModel.setVisibility(View.GONE);
            mImageButtonCompareOrSubmit.setText(getResources().getText(R.string.select_shooting_method_compare));
//            mButtonSummit.setVisibility(View.GONE);
//            mButtonCompare.setVisibility(View.VISIBLE);
        } else {
            mTextViewCompareToModel.setVisibility(View.VISIBLE);
            mImageButtonCompareOrSubmit.setText(getResources().getText(R.string.select_shooting_method_submit));
//            mButtonSummit.setVisibility(View.VISIBLE);
//            mButtonCompare.setVisibility(View.GONE);
        }
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
                    mRelativeLayoutContentVideo.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.GONE);
                    mPlayAndPause.setImageResource(R.drawable.video_stop_style);
                    mVideoView.start();
                    mHandler.sendEmptyMessage(UPDATE_UI);
                }
                break;
            case R.id.change_screen:
                intent = new Intent(this, FullVideoActivity.class);
                intent.putExtra("isPlaying", mVideoView.isPlaying());
                intent.putExtra("currentTime", mVideoView.getCurrentPosition());
                intent.putExtra("isSceenSubmiss", true);
                startActivity(intent);
                finish();
                break;
            case R.id.buttonCompareOrSubmit:
                if (mHasMyReportPlayAction) { // check
                    intent = new Intent(getApplicationContext(), CompareActivity.class);
                    startActivity(intent);
//                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    finish();
                } else { // post
                    // First, get media key
                    handlerUpload();
                }
                break;

            default:
                break;
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

    private void playVideo(Uri uri) {
        mVideoView.setVideoURI(uri);
        mVideoView.requestFocus();
        if (mCurrentTime > 0) {
            mVideoView.seekTo(mCurrentTime);
        }
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (mIsPlay) {
                    mVideoView.start();
                    mPlayAndPause.setImageResource(R.drawable.video_stop_style);
                }
                AndroidUtility.updateTextViewWithTimeFormat(mTotalTimeTv, mVideoView.getDuration());
                mHandler.sendEmptyMessage(UPDATE_UI);
            }
        });

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mHandler.removeMessages(UPDATE_UI);
                mPlayAndPause.setImageResource(R.drawable.video_start_style);
                mPosSeekBar.setProgress(0);
                mCurrentTimeTv.setText("00:00");
            }
        });


    }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("submissionConfirmation", mSubmissionConfirmation);
    }

}
