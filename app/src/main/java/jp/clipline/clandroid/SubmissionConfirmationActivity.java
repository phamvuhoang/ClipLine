package jp.clipline.clandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.api.MediaKey;

public class SubmissionConfirmationActivity extends AppCompatActivity {

    private String mTodoContentType = null;
    private Uri mTodoContentData = null;
    private WebView mWebView;

    ///// 20170507 ADD START
    private LinearLayout mLinearLayoutFooterStatus;
    private ImageView mImageViewFooterView;
    private TextView mTextViewFooterView;
    private ImageView mImageViewFooterShoot;
    private TextView mTextViewFooterShoot;
    private ImageView mImageViewFooterCompare;
    private TextView mTextViewFooterCompare;

    private Button mButtonCompareToModel;
    private Button mButtonCompare;
    private Button mButtonSummit;
    ///// 20170507 ADD END

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_confirmation);

        mTodoContentType = ((ClWebWrapperApplication) this.getApplication()).getTodoContentType();
        mTodoContentData = ((ClWebWrapperApplication) this.getApplication()).getTodoContentData();

        ImageButton imageButton;
        ImageView imageView;
        TextView textView;

        imageView = (ImageView) findViewById(R.id.imageView);
        VideoView videoView = (VideoView) findViewById(R.id.videoView);
        ///// 20170506 MODIFY START
        mWebView = (WebView) findViewById(R.id.webView);
        if (mTodoContentType.equals("image/png")) {
            // 画像が撮影or選択された場合
            imageView.setImageURI(mTodoContentData);
            videoView.setVisibility(View.GONE);
            mWebView.setVisibility(View.GONE);
        } else if (mTodoContentType.equals("video/mp4")) {
            // 動画が撮影or選択された場合
            videoView.setVideoURI(mTodoContentData);
            videoView.start();
            imageView.setVisibility(View.GONE);
            mWebView.setVisibility(View.GONE);
        } else {
            try {
                String path = "file:///" + AndroidUtility.getFilePath(this, mTodoContentData);
                mWebView.getSettings().setJavaScriptEnabled(true);
                mWebView.loadUrl(path);
                mWebView.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.GONE);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

        }
        ///// 20170506 MODIFY END

        // 戻るボタン
        imageButton = (ImageButton) findViewById(R.id.imageButtonBack);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ///// 20170507 DELETE START
/*
                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();
*/
                ///// 20170507 DELETE END

                ///// 20170507 ADD START
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
                ///// 20170507 ADD END

            }
        });

        textView = (TextView) findViewById(R.id.textViewTodoBack);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ///// 20170507 DELETE START
/*
                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();
*/
                ///// 20170507 DELETE END

                ///// 20170507 ADD START
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
                ///// 20170507 ADD END
            }
        });

        ///// 20170507 ADD START
        imageButton = (ImageButton) findViewById(R.id.imageButtonTodoClose);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 一覧へ戻る
                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                String studentId = todoParameters.get("studentId");
                String categoryId = todoParameters.get("categoryId");
                String todoContentId = todoParameters.get("todoContentId");
                String url = "%s://%s/training/#/students/" + studentId
                        + "/todos?type=updates"; // TODO type=updates/repeat???
                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                intent.putExtra("BASE_URL", url);
                startActivity(intent);
                finish();
            }
        });

        // やり直す: back to SelectShooting
        textView = (TextView) findViewById(R.id.textViewBack);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Statuses on Footer
        mLinearLayoutFooterStatus = (LinearLayout) findViewById(R.id.linearLayoutFooterStatus);
        mImageViewFooterView = (ImageView) findViewById(R.id.imageViewFooterView);
        mTextViewFooterView = (TextView) findViewById(R.id.textViewFooterView);
        mImageViewFooterShoot = (ImageView) findViewById(R.id.imageViewFooterShoot);
        mTextViewFooterShoot = (TextView) findViewById(R.id.textViewFooterShoot);
        mImageViewFooterCompare = (ImageView) findViewById(R.id.imageViewFooterCompare);
        mTextViewFooterCompare = (TextView) findViewById(R.id.textViewFooterCompare);

        // Firstly, hide the status, after getting result from api, then depend on the flags to process the view/hide
        mLinearLayoutFooterStatus.setVisibility(View.GONE);
        mImageViewFooterView.setVisibility(View.GONE);
        mTextViewFooterView.setVisibility(View.GONE);
        mImageViewFooterShoot.setVisibility(View.GONE);
        mTextViewFooterShoot.setVisibility(View.GONE);
        mImageViewFooterCompare.setVisibility(View.GONE);
        mTextViewFooterCompare.setVisibility(View.GONE);

        updateStatus();

        // お手本を見比べる
        mButtonCompareToModel = (Button) findViewById(R.id.buttonCompareWithModel);
        mButtonCompareToModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CompareActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ///// 20170507 ADD END

        // 見比べる : ToCompare
        mButtonCompare = (Button) findViewById(R.id.buttonCompare);
        mButtonCompare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CompareActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // この内容で提出
        mButtonSummit = (Button) findViewById(R.id.buttonSubmit);
        mButtonSummit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // First, get media key
                // On post execute, upload file to S3 and call report submit api
                new GetMediaKeyTask().execute(AndroidUtility.getCookie(getApplicationContext()));
            }
        });

        // 見比べる有／無によりボタン表示／非表示設定
        updateButtonVisible();

        Map<String, Object> currentTodoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();
        textView = (TextView) findViewById(R.id.textViewToDoTitle);

        ///// 20170505 MODIFY START
        if (currentTodoContent != null && currentTodoContent.get("title") != null) {
            textView.setText((String) currentTodoContent.get("title"));
        } else {
            textView.setText("");
        }
        ///// 20170505 MODIFY END
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
                    mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
                    mImageViewFooterView.setVisibility(View.VISIBLE);
                    mTextViewFooterView.setVisibility(View.VISIBLE);
                    // TODO 点灯
                    if ((todoContent.get("is_play_action_cleared") != null)
                            && ((boolean) todoContent.get("is_play_action_cleared"))) {

                    }
                }
            }

            // Check has_report_action
            if ((todoContent.get("has_report_action") != null)
                    && ((boolean)todoContent.get("has_report_action"))) {

                // 表示
                mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
                mImageViewFooterShoot.setVisibility(View.VISIBLE);
                mTextViewFooterShoot.setVisibility(View.VISIBLE);
            }

            // check has_my_report_play_action
            if ((todoContent.get("has_my_report_play_action") != null)
                    && ((boolean)todoContent.get("has_my_report_play_action"))) {

                // 表示
                mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
                mImageViewFooterCompare.setVisibility(View.VISIBLE);
                mTextViewFooterCompare.setVisibility(View.VISIBLE);
            }
        }
    }

    private void updateButtonVisible() {
        Map<String, Object> todoContent = ((ClWebWrapperApplication) getApplication()).getCurrentTodoContent();

        boolean hasMyReportPlayAction = false;

        if ((todoContent != null)
                && (todoContent.get("has_my_report_play_action") != null)) {
            hasMyReportPlayAction = ((boolean)todoContent.get("has_my_report_play_action"));
        }

        ///// 20170507 TEMPORARY ADD to test submit button
        hasMyReportPlayAction = false;

        // TODO 見比べる有???
        if (hasMyReportPlayAction) {
            mButtonCompareToModel.setVisibility(View.GONE);
            mButtonSummit.setVisibility(View.GONE);

            mButtonCompare.setVisibility(View.VISIBLE);
        } else {
            mButtonCompareToModel.setVisibility(View.VISIBLE);
            mButtonSummit.setVisibility(View.VISIBLE);

            mButtonCompare.setVisibility(View.GONE);
        }
    }



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
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

}
