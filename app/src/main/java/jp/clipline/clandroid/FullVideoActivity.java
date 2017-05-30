package jp.clipline.clandroid;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.lang.ref.WeakReference;

import jp.clipline.clandroid.Utility.AndroidUtility;
import uk.co.senab.photoview.PhotoView;

import static jp.clipline.clandroid.Utility.AndroidUtility.updateTextViewWithTimeFormat;

public class FullVideoActivity extends BaseActivity implements View.OnClickListener, OnPageChangeListener, OnLoadCompleteListener {

    private static final int UPDATE_UI = 1;
    private final MyHandler mHandler = new MyHandler(this);
    private PhotoView mPhotoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_display);
        mPhotoView = (PhotoView) findViewById(R.id.photoView);
        findViewByIdVideo();
        setListener();
        showDisplay();

    }


    private void showDisplay() {
        if (mTodoContentType.equals("image/png")) {
            // 画像が撮影or選択された場合
            mImageView.setVisibility(View.VISIBLE);
            mRelativeLayoutContentVideo.setVisibility(View.GONE);
            mPdfView.setVisibility(View.GONE);
            mButtonFullScreen.setVisibility(View.VISIBLE);

            mPhotoView.setImageURI(mTodoContentData);
        } else if (mTodoContentType.equals("video/mp4")) {
            // 動画が撮影or選択された場合
            mPhotoView.setVisibility(View.GONE);
            mRelativeLayoutContentVideo.setVisibility(View.INVISIBLE);
            mPdfView.setVisibility(View.GONE);
            mButtonFullScreen.setVisibility(View.GONE);

            playVideo(mTodoContentData);
        } else { //content type is pdf
            try {
                mPhotoView.setVisibility(View.GONE);
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
                finish();
            }
        });

        mPlayAndPause.setOnClickListener(this);
        mChangeFullScreen.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
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
                finish();
                break;
        }
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
                        updateTextViewWithTimeFormat(mCurrentTimeTv, currentPosition);
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

}
