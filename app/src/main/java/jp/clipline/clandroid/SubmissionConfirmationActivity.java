package jp.clipline.clandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.net.URISyntaxException;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;

public class SubmissionConfirmationActivity extends AppCompatActivity {

    private String mTodoContentType = null;
    private Uri mTodoContentData = null;
    private WebView mWebView;

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
        ///// 20170506 MODIFY START

        // 戻るボタン
        imageButton = (ImageButton) findViewById(R.id.imageButtonBack);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();
            }
        });

        textView = (TextView) findViewById(R.id.textViewBack);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 見比べる : ToCompare

        Button button = (Button) findViewById(R.id.buttonCompare);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CompareActivity.class);
                startActivity(intent);
                finish();
            }
        });

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
}
