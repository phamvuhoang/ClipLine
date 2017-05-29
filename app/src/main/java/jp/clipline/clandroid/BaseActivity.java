package jp.clipline.clandroid;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.api.MediaKey;
import jp.clipline.clandroid.api.Report;


public class BaseActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                ///// 20170523 ADD START
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                ///// 20170523 ADD END
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
}
