package jp.clipline.clwebwrapperapplication;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Map;

public class SubmissionConfirmationActivity extends AppCompatActivity {

    private String mTodoContentType = null;
    private Uri mTodoContentData = null;

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

        if (mTodoContentType.equals("image/png")) {
            // 画像が撮影or選択された場合
            imageView.setImageURI(mTodoContentData);
            videoView.setVisibility(View.INVISIBLE);
        } else if (mTodoContentType.equals("video/mp4")) {
            // 動画が撮影or選択された場合
            videoView.setVideoURI(mTodoContentData);
            videoView.start();
            imageView.setVisibility(View.INVISIBLE);
        } else {
            Log.d("", "");
        }

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

        ///// 20170504 MODIFY START

        if (currentTodoContent != null && currentTodoContent.get("title") != null) {
            textView.setText((String) currentTodoContent.get("title"));
        } else {
            //TODO NVTu contact a Hoang
            textView.setText("");
        }
        ///// 20170504 MIDIFY END
    }
}
