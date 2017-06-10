package jp.clipline.clandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;

import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.clipline.clandroid.Utility.AndroidUtility;
import uk.co.senab.photoview.PhotoView;

import static jp.clipline.clandroid.Utility.AndroidUtility.updateTextViewWithTimeFormat;

public class FullVideoActivity extends BaseActivity implements View.OnClickListener, OnPageChangeListener, OnLoadCompleteListener {

    private static final int UPDATE_UI = 1;
    private final MyHandler mHandler = new MyHandler(this);
    private PhotoView mPhotoView;
    private ProgressBar mProgressBar;
    private String mPathPdfViewer;
    private boolean checkStartVideo = true;
    private String mPathVideoFromCompare = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_display);
        mPhotoView = (PhotoView) findViewById(R.id.photoView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBarPdf);
        mProgressBar.setVisibility(View.GONE);
        findViewByIdVideo();
        setListener();
        if (savedInstanceState != null) {
            mPathPdfViewer = String.valueOf(savedInstanceState.get("download_pdf_is_successful"));
            mPhotoView.setVisibility(View.GONE);
            mRelativeLayoutContentVideo.setVisibility(View.GONE);
            mPdfView.setVisibility(View.VISIBLE);
            mButtonFullScreen.setVisibility(View.GONE);
            mPdfView.fromFile(new File(mPathPdfViewer))
                    .defaultPage(0)
                    .onPageChange(FullVideoActivity.this)
                    .enableAnnotationRendering(true)
                    .onLoad(FullVideoActivity.this)
                    .scrollHandle(new DefaultScrollHandle(FullVideoActivity.this))
                    .load();
            return;
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String path = bundle.getString("pdfViewer");
            mIsPlay = bundle.getBoolean("isPlaying");
            mCurrentTime = bundle.getInt("currentTime");
            mIsSceenSub = bundle.getBoolean("isSceenSubmiss");
            if (!mIsSceenSub) {
                mPathVideoFromCompare = bundle.getString("path_uri");
            }
            if (!TextUtils.isEmpty(path)) {
                mPhotoView.setVisibility(View.GONE);
                mRelativeLayoutContentVideo.setVisibility(View.GONE);
                mPdfView.setVisibility(View.GONE);
                mButtonFullScreen.setVisibility(View.GONE);
                new DownloadPdfAsync().execute(path);
                return;
            }
            showDisplay();
        } else {
            showDisplay();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(mPathPdfViewer)) {
            outState.putString("download_pdf_is_successful", mPathPdfViewer);
        }
    }

    private void showDisplay() {
        if (mTodoContentData == null || mTodoContentType == null) {
            return;
        }
        if (mTodoContentType.equals("image/png")) {
            // 画像が撮影or選択された場合
            mPhotoView.setVisibility(View.VISIBLE);
            mRelativeLayoutContentVideo.setVisibility(View.GONE);
            mPdfView.setVisibility(View.GONE);
            mButtonFullScreen.setVisibility(View.VISIBLE);
            try {
                String path = AndroidUtility.getFilePath(this, mTodoContentData);
                ExifInterface exif = new ExifInterface(path);
                float rotate = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                mPhotoView.setImageURI(mTodoContentData);
                mPhotoView.setRotation(rotate);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (mTodoContentType.equals("video/mp4")) {
            // 動画が撮影or選択された場合
            mPhotoView.setVisibility(View.GONE);
            mRelativeLayoutContentVideo.setVisibility(View.VISIBLE);
            mPdfView.setVisibility(View.GONE);
            mButtonFullScreen.setVisibility(View.GONE);
            if (TextUtils.isEmpty(mPathVideoFromCompare)) {
                playVideo(mTodoContentData);
            } else {
                playVideo(Uri.parse(mPathVideoFromCompare));
            }

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
                if (mIsSceenSub) {
                    Intent intent = new Intent(FullVideoActivity.this, SubmissionConfirmationActivity.class);
                    intent.putExtra("isPlaying", mVideoView.isPlaying());
                    intent.putExtra("currentTime", mVideoView.getCurrentPosition());
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(FullVideoActivity.this, CompareActivity.class);
                    intent.putExtra("isPlaying", mVideoView.isPlaying());
                    intent.putExtra("currentTime", mVideoView.getCurrentPosition());
                    startActivity(intent);
                    finish();
                }
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
                        int currentPosition;
                        if (mCurrentTime > 0 && checkStartVideo) {
                            currentPosition = mCurrentTime;
                            checkStartVideo = false;
                        } else {
                            currentPosition = mVideoView.getCurrentPosition();
                        }
                        int totalPosition = mVideoView.getDuration();
                        updateTextViewWithTimeFormat(mCurrentTimeTv, currentPosition);
//                        mPosSeekBar.setMax(totalPosition);
//                        mPosSeekBar.setProgress(currentPosition);
                        mHandler.sendEmptyMessageDelayed(UPDATE_UI, 500);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void playVideo(Uri uri) {
        mVideoView.setVideoPath(String.valueOf(uri));
        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mVideoView.seekTo(mCurrentTime);
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
//                mPosSeekBar.setProgress(0);
                mCurrentTimeTv.setText("00:00");
            }
        });


    }

    private void showProgressBar(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mProgressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    class DownloadPdfAsync extends AsyncTask<String, String, String> {

        public DownloadPdfAsync() {
            showProgressBar(true);
        }

        @Override
        protected String doInBackground(final String... aurl) {
            File directory = null;
            try {
                String extStorageDirectory = Environment.getExternalStorageDirectory()
                        + File.separator
                        + "Clipline";

                File dir = new File(extStorageDirectory, "PDF");
                if (dir.exists() == false) {
                    dir.mkdirs();
                }
                directory = new File(dir, new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(new Date()) + ".pdf");
                try {
                    if (!directory.exists())
                        directory.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                int lenghtOfFile = conexion.getContentLength();
                conexion.connect();
                conexion.setReadTimeout(10000);
                conexion.setConnectTimeout(15000); // millis

                FileOutputStream f = new FileOutputStream(directory);

                InputStream in = conexion.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = in.read(buffer)) > 0) {
                    f.write(buffer, 0, len1);
                }
                f.flush();
                f.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return directory.getAbsolutePath();

        }

        @Override
        protected void onPostExecute(String path) {
            mPathPdfViewer = path;
            showProgressBar(false);
            mPdfView.setVisibility(View.VISIBLE);
            mPdfView.fromFile(new File(mPathPdfViewer))
                    .defaultPage(0)
                    .onPageChange(FullVideoActivity.this)
                    .enableAnnotationRendering(true)
                    .onLoad(FullVideoActivity.this)
                    .scrollHandle(new DefaultScrollHandle(FullVideoActivity.this))
                    .load();
        }
    }
}
