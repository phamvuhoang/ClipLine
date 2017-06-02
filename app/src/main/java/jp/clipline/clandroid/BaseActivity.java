package jp.clipline.clandroid;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.api.MediaKey;
import jp.clipline.clandroid.api.Report;

public class BaseActivity extends AppCompatActivity {

    //DIALOG CONFORM
    private ImageView mImageViewSubmit;
    private TextView mTextViewUpload;
    private Button mButtonReportSentComment;
    private Button mButtonReportSentClose;
    private Button mButtonReportSentRetry;
    private TextView mTextViewError;
    private ProgressBar mProgressBar;
    private View mViewProgressBar;
    protected AlertDialog mAlertDialog;
    protected final int UPLOAD_NONE = 0;
    protected final int UPLOAD_SUCCESSFULL = 1;
    protected final int UPLOAD_FAILE = 2;
    protected int mSubmissionConfirmation = 0;

    //VIDEO
    protected VideoView mVideoView;
    protected TextView mCurrentTimeTv;
    protected TextView mTotalTimeTv;
    protected TextView mTextLine;
    protected SeekBar mPosSeekBar;
    protected ImageView mPlayAndPause;
    protected ImageView mChangeFullScreen;
    protected AudioManager mAudioManager;
    protected int currentVolume;
    protected int maxVolume;
    protected final int SEEKTOTIME = 1111;
    protected final int UPDATE_UI = 1;
    protected RelativeLayout mRelativeLayoutContentVideo;
    protected LinearLayout mRelativeLayoutVideoController;

    //PDF
    protected PDFView mPdfView;

    //Image
    protected ImageView mImageView;

    //data
    protected String mTodoContentType = null;
    protected Uri mTodoContentData = null;

    protected SubmissionConfirmationActivity.MyHandler mHandler;
    protected Button mButtonFullScreen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDialog();
        mTodoContentType = ((ClWebWrapperApplication) this.getApplication()).getTodoContentType();
        File file = new File(((ClWebWrapperApplication) this.getApplication()).getTodoContentData());
        mTodoContentData = Uri.fromFile(file);

    }

    /**
     * -------------------------DIALOG-------------------------
     */

    public void showReport() {
        mAlertDialog.show();
    }

    public void hideReport() {
        mAlertDialog.dismiss();
    }

    public void UploadSuccessfull() {
        mAlertDialog.show();
        mProgressBar.setVisibility(View.GONE);
        mViewProgressBar.setBackground(ContextCompat.getDrawable(BaseActivity.this, R.color.green));
        mViewProgressBar.setVisibility(View.VISIBLE);
        mImageViewSubmit.setBackground(ContextCompat.getDrawable(BaseActivity.this, R.drawable.icon_status_complete));
        mTextViewUpload.setText(getResources().getText(R.string.report_sent_successful));
        mButtonReportSentComment.setVisibility(View.VISIBLE);
        mButtonReportSentClose.setVisibility(View.VISIBLE);
        mButtonReportSentRetry.setVisibility(View.GONE);
    }

    public void UploadFaile() {
        mAlertDialog.show();
        mProgressBar.setVisibility(View.GONE);
        mViewProgressBar.setBackground(ContextCompat.getDrawable(BaseActivity.this, R.color.colorRed));
        mViewProgressBar.setVisibility(View.VISIBLE);
        mImageViewSubmit.setBackground(ContextCompat.getDrawable(BaseActivity.this, R.drawable.icon_error));
        mTextViewUpload.setText(getResources().getText(R.string.report_sent_failed));
        mButtonReportSentComment.setVisibility(View.GONE);
        mButtonReportSentClose.setVisibility(View.VISIBLE);
        mButtonReportSentRetry.setVisibility(View.VISIBLE);
        mTextViewError.setVisibility(View.VISIBLE);
    }

    public void handlerUpload() {
        mAlertDialog.show();
        mProgressBar.setVisibility(View.VISIBLE);
        mTextViewUpload.setText(getResources().getText(R.string.report_sent));
        mImageViewSubmit.setBackground(null);
        mTextViewError.setVisibility(View.GONE);
        mViewProgressBar.setVisibility(View.GONE);
        mButtonReportSentComment.setVisibility(View.GONE);
        mButtonReportSentRetry.setVisibility(View.GONE);
        mButtonReportSentClose.setVisibility(View.GONE);
        new GetMediaKeyTask().execute(AndroidUtility.getCookie(getApplicationContext()));
    }

    private void initDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_report, null);
        dialogBuilder.setView(dialogView);
        mAlertDialog = dialogBuilder.create();
        mTextViewError = (TextView) dialogView.findViewById(R.id.textViewError);
        mButtonReportSentComment = (Button) dialogView.findViewById(R.id.buttonReportSentInputComment);
        mButtonReportSentRetry = (Button) dialogView.findViewById(R.id.buttonReportSentRetry);
        mButtonReportSentClose = (Button) dialogView.findViewById(R.id.buttonReportSentClose);
        mProgressBar = (ProgressBar) dialogView.findViewById(R.id.progressBar);
        mViewProgressBar = dialogView.findViewById(R.id.viewProgressBar);
        mImageViewSubmit = (ImageView) dialogView.findViewById(R.id.imageViewStatusSubmit);
        mTextViewUpload = (TextView) dialogView.findViewById(R.id.textViewUpload);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
        mAlertDialog.setCanceledOnTouchOutside(false);

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
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                finish();
            }
        });
        mButtonReportSentRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                mTextViewUpload.setText(getResources().getText(R.string.report_sent));
                mImageViewSubmit.setBackground(null);
                mTextViewError.setVisibility(View.GONE);
                mViewProgressBar.setVisibility(View.GONE);

                mButtonReportSentComment.setVisibility(View.GONE);
                mButtonReportSentRetry.setVisibility(View.GONE);
                mButtonReportSentClose.setVisibility(View.GONE);
                new GetMediaKeyTask().execute(AndroidUtility.getCookie(getApplicationContext()));
            }
        });
        mButtonReportSentClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.dismiss();
                mSubmissionConfirmation = UPLOAD_NONE;

                // 一覧へ戻る
                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                // TODO add check student or coach and add param properly
                String studentId = todoParameters.get("studentId");
                String categoryId = todoParameters.get("categoryId");
                String todoContentId = todoParameters.get("todoContentId");
                String type = todoParameters.get("type");
                String url = "%s://%s/training/#/students/" + studentId
                        + "/todos?type=" + type;
                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                intent.putExtra("BASE_URL", url);
                startActivity(intent);
                finish();
            }
        });
    }


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
            success = true;

            if (success) {
                mProgressBar.setVisibility(View.GONE);
                mViewProgressBar.setBackground(ContextCompat.getDrawable(BaseActivity.this, R.color.green));
                mViewProgressBar.setVisibility(View.VISIBLE);
                mImageViewSubmit.setBackground(ContextCompat.getDrawable(BaseActivity.this, R.drawable.icon_status_complete));
                mTextViewUpload.setText(getResources().getText(R.string.report_sent_successful));
                mButtonReportSentComment.setVisibility(View.VISIBLE);
                mButtonReportSentClose.setVisibility(View.VISIBLE);
                mButtonReportSentRetry.setVisibility(View.GONE);
                mSubmissionConfirmation = UPLOAD_SUCCESSFULL;

            } else {
                mProgressBar.setVisibility(View.GONE);
                mViewProgressBar.setBackground(ContextCompat.getDrawable(BaseActivity.this, R.color.colorRed));
                mViewProgressBar.setVisibility(View.VISIBLE);
                mImageViewSubmit.setBackground(ContextCompat.getDrawable(BaseActivity.this, R.drawable.icon_error));
                mTextViewUpload.setText(getResources().getText(R.string.report_sent_failed));
                mButtonReportSentComment.setVisibility(View.GONE);
                //mButtonReportSentClose.setVisibility(View.VISIBLE);
                mButtonReportSentRetry.setVisibility(View.VISIBLE);
                mTextViewError.setVisibility(View.VISIBLE);
                mSubmissionConfirmation = UPLOAD_FAILE;
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    /**
     * -------------------------VIDEO and PDF and Image-------------------------
     */
    protected void findViewByIdVideo() {
        mVideoView = (VideoView) findViewById(R.id.video_view);
        mCurrentTimeTv = (TextView) findViewById(R.id.current_time_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.total_time_tv);
        mTextLine = (TextView) findViewById(R.id.textLine);
        mPosSeekBar = (SeekBar) findViewById(R.id.pos_seekBar);
        mPlayAndPause = (ImageView) findViewById(R.id.pause_img);
        mChangeFullScreen = (ImageView) findViewById(R.id.change_screen);
        mRelativeLayoutContentVideo = (RelativeLayout) findViewById(R.id.relativeLayoutContentVideo);
        mRelativeLayoutVideoController = (LinearLayout) findViewById(R.id.bottom_layout);
        mPdfView = (PDFView) findViewById(R.id.pdfView);
        mImageView = (ImageView) findViewById(R.id.imageViewSelect);
        mButtonFullScreen = (Button) findViewById(R.id.buttonFullScreen);
    }



    protected void setListener() {


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
    }

}
