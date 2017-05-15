package jp.clipline.clandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.lang.ref.WeakReference;

import jp.clipline.clandroid.view.FullVideo;

/**
 * Created by nguyentu on 5/16/17.
 */

public class FullVideoActivity extends AppCompatActivity implements View.OnClickListener {

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
    private Uri mTodoContentData = null;
    private String mTodoContentType = null;

    private static final int UPDATE_UI = 1;
    private final MyHandler mHandler = new MyHandler(this);
    private int mSeekTo = 0;
    private final String SAVE_SEEKTO = "save_seekto";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSeekTo = savedInstanceState.getInt(SAVE_SEEKTO);
        } else {
            Intent intent = getIntent();
            mSeekTo = intent.getIntExtra("seekto", 0);
        }
        setContentView(R.layout.layout_video_view);
        findViewById();
    }

    private void findViewById() {
        mVideoView = (FullVideo) findViewById(R.id.video_view);
        mCurrentTimeTv = (TextView) findViewById(R.id.current_time_tv);
        mTotalTimeTv = (TextView) findViewById(R.id.total_time_tv);
        mPosSeekBar = (SeekBar) findViewById(R.id.pos_seekBar);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.volume_seek);
        mPlayAndPause = (ImageView) findViewById(R.id.pause_img);
        mChangeFullScreen = (ImageView) findViewById(R.id.change_screen);
        setListener();
        init();
        mTodoContentType = ((ClWebWrapperApplication) this.getApplication()).getTodoContentType();
        File file = new File(((ClWebWrapperApplication) this.getApplication()).getTodoContentData());
        mTodoContentData = Uri.fromFile(file);
        playVideo(mTodoContentData);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_SEEKTO, mVideoView.getDuration());
        super.onSaveInstanceState(outState);
    }

    private void setListener() {
        mPlayAndPause.setOnClickListener(this);
        mChangeFullScreen.setOnClickListener(this);

        mPosSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                updateTextViewWithTimeFormat(mCurrentTimeTv, progress);
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
                mVideoView.start();
                updateTextViewWithTimeFormat(mTotalTimeTv, mVideoView.getDuration());
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
        mVideoView.seekTo(mSeekTo);

    }


    private void updateTextViewWithTimeFormat(TextView tv, int milliSecond) {
        int second = milliSecond / 1000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;

        String timeStr = null;
        if (hh != 0) {
            timeStr = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            timeStr = String.format("%02d:%02d", mm, ss);
        }
        tv.setText(timeStr);
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
                Intent intent = new Intent();
                intent.putExtra("seekto", mVideoView.getDuration());
                setResult(Activity.RESULT_OK, intent);
                finish();

                break;
        }
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
