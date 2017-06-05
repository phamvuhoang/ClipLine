package jp.clipline.clandroid;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import net.ypresto.androidtranscoder.MediaTranscoder;
import net.ypresto.androidtranscoder.format.MediaFormatStrategyPresets;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.Utility.CameraUtil;
import jp.clipline.clandroid.Utility.ConstCameraActivity;
import jp.clipline.clandroid.Utility.IntentParameters;

public class CameraActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ConstCameraActivity, CompoundButton.OnCheckedChangeListener, View.OnTouchListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private IntentParameters mIntentParameters;
    private Camera mCamera;
    private SurfaceView mSurfaceViewPreview;
    private RelativeLayout mRelativeLayoutPreview;
    private boolean mIsRecording;
    private MediaRecorder mMediaRecorder;
    private Button mButtonVideo;
    private Button mButtonVideoRecording;
    private Button mButtonPicture;
    private long mButtonVideoLastClickTime = System.currentTimeMillis();
    private String mNextVideoAbsolutePath;
    private ImageButton mImageButtonBackFront;
    private ImageButton mImageButtonChangeAspectWide;
    private ImageButton mImageButtonChangeAspectStandard;
    private TextView mTextViewVideoRecordingTime;
    private ImageButton mImageButtonChangePicture;
    private ImageButton mImageButtonChangeVideo;
    private Button mButtonFocus;
    //private ImageButton mImageButtonIcSettingsButton;
    //private ImageView mImageViewOpenGallery;
    private TextView mTextViewButtonClose;
    private SeekBar mSeekBarZoom;
    private SeekBar mSeekBarBrightness;
    private Timer mTimerVideoRecordingTimeUpdate;
    private Handler mHandlerVideoRecordingTimeUpdate = new Handler();
    private Timer mTimerHideFocus;
    private Handler mHandlerHideFocus = new Handler();
    private int mLaptime;
    private HandlerThread mHandlerThreadTakePicture;
    private Handler mHandlerTakePicture;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mBeforeScale = 0.0f;
    private Future<Void> mFuture;
    private boolean mIsBackShooting = true;
    private static final int REQUEST_CODE_PICK = 1;
    private ProgressBar mProgressBar;

    // 解像度リスト
    private static List<Camera.Size> mVideoSizeList = new ArrayList<>();
    private static List<Camera.Size> mPictureSizeList = new ArrayList<>();

    // 対応アスペクト比解像度リスト
    private static List<Camera.Size> mStandardAspectVideoSizeList = new ArrayList<>();
    private static List<Camera.Size> mStandardAspectPictureSizeList = new ArrayList<>();
    private static Camera.Size mStandardAspectVideoSize = null;
    private static Camera.Size mStandardAspectPictureSize = null;

    private static List<Camera.Size> mWideAspectVideoSizeList = new ArrayList<>();
    private static List<Camera.Size> mWideAspectPictureSizeList = new ArrayList<>();
    private static Camera.Size mWideAspectVideoSize = null;
    private static Camera.Size mWideAspectPictureSize = null;

    private static Camera.Size mCurrentAspectVideoSize = null;
    private static Camera.Size mCurrentAspectPictureSize = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, String.format("onCreate - Start"));

        Point size = CameraUtil.getRealScreenSize(getWindowManager());
        String message = String.format("DEBUG : %s(%d,%d) : %s端末", android.os.Build.MODEL, size.x, size.y, (CameraUtil.isStandardAspectHardWare(getWindowManager())) ? "スタンダード" : "ワイド");
        Log.v(TAG, message);

        if (savedInstanceState != null) {
            String temp = String.valueOf(savedInstanceState.get("intentParameters_save"));
            if (temp != null) {
                mIntentParameters = new Gson().fromJson(temp, IntentParameters.class);
            }
        } else {
            mIntentParameters = new IntentParameters(getIntent(), CameraUtil.isStandardAspectHardWare(getWindowManager()));
        }

        mIntentParameters.printInfomation();
        Log.d(TAG, String.format("IntentParameter : %s : %s, %s", mIntentParameters.isCallFromIntent() ? "インテント起動" : "", mIntentParameters.isAspectWide() ? "ワイド" : "スタンダード", mIntentParameters.isBackShooting() ? "背面" : "前面"));

//        if (Build.VERSION.SDK_INT >= 23) {
//            if(activityRequestPermissions(PERMISSION_REQUEST_CODE)){
//                Intent intent = new Intent(getApplicationContext(), PermissionsWaitActivity.class);
//                mIntentParameters.apply(intent);
//                startActivity(intent);
//                overridePendingTransition(0, 0);
//                finish();
//            }
//        }

        setContentView(R.layout.activity_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mRelativeLayoutPreview = (RelativeLayout) this.findViewById(R.id.relativeLayoutPreview);
        mRelativeLayoutPreview.addOnLayoutChangeListener(relativeLayoutListener);

        // mSurfaceViewPreview = new SurfaceView(this);
        mSurfaceViewPreview = (SurfaceView) this.findViewById(R.id.surfaceView);
        mSurfaceViewPreview.getHolder().addCallback(this.mSurfaceHolderCallback);
        mSurfaceViewPreview.getHolder().setSizeFromLayout();
        mSurfaceViewPreview.setOnTouchListener(this);

        mScaleGestureDetector = new ScaleGestureDetector(this, mScaleGestureListener);

        mButtonVideo = (Button) findViewById(R.id.buttonVideo);
        mButtonVideo.setOnClickListener(mButtonVideoAndVideoRecording);
        mButtonVideoRecording = (Button) findViewById(R.id.buttonVideoRecording);
        mButtonVideoRecording.setOnClickListener(mButtonVideoAndVideoRecording);
        mButtonPicture = (Button) findViewById(R.id.buttonPicture);
        mButtonPicture.setOnClickListener(mButtonVideoAndVideoRecording);

        mImageButtonBackFront = (ImageButton) findViewById(R.id.imageButtonBackFront);
        mImageButtonBackFront.setOnClickListener(mImageButtonBackFrontListener);
        mIsBackShooting = mIntentParameters.isBackShooting();
        mImageButtonChangeAspectWide = (ImageButton) findViewById(R.id.imageButtonChangeAspectWide);
        mImageButtonChangeAspectWide.setOnClickListener(mImageButtonChangeAspectListener);
        mImageButtonChangeAspectStandard = (ImageButton) findViewById(R.id.imageButtonChangeAspectStandard);
        mImageButtonChangeAspectStandard.setOnClickListener(mImageButtonChangeAspectListener);
        if (mIntentParameters.isAspectWide()) {
            mImageButtonChangeAspectStandard.setVisibility(View.INVISIBLE);
            mImageButtonChangeAspectWide.setVisibility(View.VISIBLE);
        } else {
            mImageButtonChangeAspectStandard.setVisibility(View.VISIBLE);
            mImageButtonChangeAspectWide.setVisibility(View.INVISIBLE);
        }
        mTextViewVideoRecordingTime = (TextView) findViewById(R.id.textViewVideoRecordingTime);
        mImageButtonChangePicture = (ImageButton) findViewById(R.id.imageButtonChangePicture);
        mImageButtonChangePicture.setOnClickListener(mImageButtonChangePictureListener);
        mImageButtonChangeVideo = (ImageButton) findViewById(R.id.imageButtonChangeVideo);
        mImageButtonChangeVideo.setOnClickListener(mImageButtonChangeVideoListener);
        mImageButtonChangeVideo.setVisibility(View.INVISIBLE);
        mButtonFocus = (Button) findViewById(R.id.buttonFocus);
        //mImageViewOpenGallery = (ImageView) findViewById(R.id.imageViewOpenGallery);
        //mImageViewOpenGallery.setOnClickListener(mImageViewOpenGalleryListener);
        //applyThumbnailToGalleryButton();
        mTextViewButtonClose = (TextView) findViewById(R.id.textViewClose);
        mTextViewButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mSeekBarZoom = (SeekBar) findViewById(R.id.seekBarZoom);
        mSeekBarZoom.setOnSeekBarChangeListener(mSeekBarChangeListenerZoom);
        mSeekBarBrightness = (SeekBar) findViewById(R.id.seekBarBrightness);
        mSeekBarBrightness.setOnSeekBarChangeListener(mSeekBarChangeListenerBrightness);

        if (!isVideoMode()) {
            changeToPicture();
        }

        // 上下のレイヤーは操作を透過させない為
        View.OnTouchListener touchIgnoredListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        };
        RelativeLayout relativeLayoutTop = (RelativeLayout) findViewById(R.id.relativeLayoutTop);
        relativeLayoutTop.setOnTouchListener(touchIgnoredListener);
        RelativeLayout relativeLayoutBottom = (RelativeLayout) findViewById(R.id.relativeLayoutBottom);
        relativeLayoutBottom.setOnTouchListener(touchIgnoredListener);
        FrameLayout frameLayoutSeekBarZoom = (FrameLayout) findViewById(R.id.frameLayoutSeekBarZoom);
        frameLayoutSeekBarZoom.setOnTouchListener(touchIgnoredListener);
        FrameLayout frameLayoutSeekBarBrightness = (FrameLayout) findViewById(R.id.frameLayoutSeekBarBrightness);
        frameLayoutSeekBarBrightness.setOnTouchListener(touchIgnoredListener);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBarLarge);
        mProgressBar.setOnTouchListener(touchIgnoredListener);

        // 以下、ハンバーガーメニュー用途
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setVisibility(View.INVISIBLE);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        // getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_info);
        toggle.syncState();

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setVisibility(View.VISIBLE);

//        mImageButtonIcSettingsButton = (ImageButton) findViewById(R.id.icSettings);
//        mImageButtonIcSettingsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//                navigationView.setVisibility(View.VISIBLE);
//
//                DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//                drawerLayout.openDrawer(GravityCompat.START);
//            }
//        });
        Log.v(TAG, String.format("onCreate - End"));
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, String.format("onDestroy - Start"));
        super.onDestroy();
        //mImageViewOpenGallery = null;
        mImageButtonChangeAspectStandard = null;
        mImageButtonChangeAspectWide = null;
        mImageButtonChangePicture = null;
        mImageButtonChangeVideo = null;
        Log.v(TAG, String.format("onDestroy - End"));
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause - Start");
        teardownCamera();
        Log.d(TAG, "onPause - End");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume - Start");
        setupCamera();
        Log.d(TAG, "onResume - End");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged - Start");
        try {
            mCamera.setPreviewDisplay(mSurfaceViewPreview.getHolder());
        } catch (IOException e) {
            Log.e(TAG, String.format("onCheckedChanged : message=%s, stacktrace=%s", e.getMessage(), e.getStackTrace().toString()));
        }
        mCamera.startPreview();
        Log.d(TAG, "onCheckedChanged - End");
    }

    @Override
    public void finish() {
        super.finish();
        Log.d(TAG, "finish - Start");
//        overridePendingTransition(0, 0);
        Log.d(TAG, "finish - End");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScaleGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CHOSE_FILE_CODE && resultCode == RESULT_OK) {
                mProgressBar.setVisibility(View.VISIBLE);
                final File file;
                File outputDir = new File(CameraUtil.getVideoStorageDir("SimpleCamera").toString());
                file = File.createTempFile("IMPORTED_CLIP_", ".mp4", outputDir);
                final ParcelFileDescriptor parcelFileDescriptor;
                ContentResolver resolver = getContentResolver();
                parcelFileDescriptor = resolver.openFileDescriptor(data.getData(), "r");
                final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
                    @Override
                    public void onTranscodeProgress(double progress) {
                        if (progress < 0) {
                            mProgressBar.setIndeterminate(true);
                        } else {
                            mProgressBar.setIndeterminate(false);
                            mProgressBar.setProgress((int) Math.round(progress * mProgressBar.getMax()));
                        }
                    }

                    @Override
                    public void onTranscodeCompleted() {
                        //Log.d(TAG, "transcoding took " + (SystemClock.uptimeMillis() - startTime) + "ms");
                        //onTranscodeFinished(true, "transcoded file placed on " + file, parcelFileDescriptor);

                        // Uri uri = FileProvider.getUriForFile(CameraActivity.this, FILE_PROVIDER_AUTHORITY, file);
                        //startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(uri, "video/mp4").setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CameraActivity.this, String.format(getString(R.string.compress_completed), file.getAbsolutePath()), Toast.LENGTH_SHORT).show();

                        CameraUtil.registVideoAndroidDB(file.getAbsolutePath(), CameraActivity.this.getApplicationContext());
                    }

                    @Override
                    public void onTranscodeCanceled() {
                        // onTranscodeFinished(false, "Transcoder canceled.", parcelFileDescriptor);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CameraActivity.this, getString(R.string.compress_canceled), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onTranscodeFailed(Exception exception) {
                        //onTranscodeFinished(false, "Transcoder error occurred.", parcelFileDescriptor);
                        mProgressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CameraActivity.this, getString(R.string.compress_failed), Toast.LENGTH_SHORT).show();
                    }
                };

                mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, file.getAbsolutePath(),
                        MediaFormatStrategyPresets.createAndroid720pStrategy(8000 * 1000, 128 * 1000, 1), listener);
            }
        } catch (IOException e) {
            Log.e(TAG, String.format("onActivityResult : message=%s, stacktrace=%s", e.getMessage(), e.getStackTrace()[0]));
        }
    }

    // @see : http://qiita.com/negi_magnet/items/e8d1b95a17da5539c261
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // このメソッドは、ビューを押した時、ドラッグした時、離した時など、色々な時に呼ばれる
        // 今回は押した時にだけ処理するように下のif文を付ける
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // event.getX() でタッチしたx座標が分かる
            // event.getY() でタッチしたy座標が分かる
            Camera.Parameters parameters = mCamera.getParameters();
            // 対応しているかチェック
            if (parameters.getMaxNumFocusAreas() > 0) {
                int x, y;
                switch (this.getWindowManager().getDefaultDisplay().getRotation()) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        x = (int) event.getY();
                        y = (mSurfaceViewPreview.getWidth() - (int) event.getX());
                        break;
                    //case Surface.ROTATION_90:
                    //case Surface.ROTATION_270:
                    default:
                        x = (int) event.getX();
                        y = (int) event.getY();
                        break;
                }

                mButtonFocus.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mButtonFocus.getLayoutParams();
                layoutParams.topMargin = ((int) event.getY()) - (mButtonFocus.getHeight() / 2);
                layoutParams.leftMargin = (int) event.getX() - (mButtonFocus.getWidth() / 2);
                mButtonFocus.setLayoutParams(layoutParams);
                mTimerHideFocus = new Timer();
                mTimerHideFocus.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mHandlerHideFocus.post(new Runnable() {
                            public void run() {
                                mButtonFocus.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }, 5000, 5000);

                // タッチした座標を[-1000,1000]の範囲に落としこむ
                int fx = x * 2000 / parameters.getPreviewSize().width - 1000;
                int fy = y * 2000 / parameters.getPreviewSize().width - 1000;

                // 上記の(x,y)を中央とした100x100の矩形領域を設定することにする
                // 矩形の端が[-1000,1000]を出ないように調整
                if (fx < -950) fx = -950;
                if (fx > 950) fx = 950;
                if (fy < -950) fy = -950;
                if (fy > 950) fy = 950;

                // 矩形領域をセット
                List<Camera.Area> area = new ArrayList<Camera.Area>();
                area.add(new Camera.Area(new Rect(fx - 50, fy - 50, fx + 50, fy + 50), 1));
                parameters.setFocusAreas(area);
                mCamera.setParameters(parameters);

                // 最後にオートフォーカス呼び出し
                mCamera.autoFocus(null);
            }
        }

        if (onTouchEvent(event)) {
            return true;
        }
        ;

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // @see : https://www.ipentec.com/document/document.aspx?page=android-rotate-screen-save-value-in-bundle
        super.onSaveInstanceState(outState);
        outState.putString("SEEK_BAR_ZOOM_PROGRESS", Integer.toString(mSeekBarZoom.getProgress()));
        outState.putString("SEEK_BAR_BRIGHTNESS_PROGRESS", Integer.toString(mSeekBarBrightness.getProgress()));
        outState.putString("IS_VIDEO_MODE", Boolean.toString(isVideoMode()));
        Gson gson = new Gson();
        outState.putString("intentParameters_save", gson.toJson(mIntentParameters));
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_contact) {
            Intent intent = new Intent();
            // アクションを指定
            intent.setAction(Intent.ACTION_SENDTO);
            // データを指定
            intent.setData(Uri.parse("mailto:support_activate@clipline.jp"));
            // CCを指定
            // intent.putExtra(Intent.EXTRA_CC, new String[]{"cc@example.com"});
            // BCCを指定
            intent.putExtra(Intent.EXTRA_BCC, new String[]{"bcc@example.com"});
            // 件名を指定
            intent.putExtra(Intent.EXTRA_SUBJECT, "シンプルカメラアプリについての問い合わせ");
            // 本文を指定
            intent.putExtra(Intent.EXTRA_TEXT, collectionTerminalInformation());
            // Intentを発行
            startActivity(intent);
        } else if (id == R.id.nav_usage) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("jp.clipline.clsimplecamera", "jp.clipline.clsimplecamera.DocumentDisplayActivity");
            intent.putExtra("ASSETS_HTML_FILE", "https://clipline.jp/service/privacy-policy");
            startActivity(intent);
        } else if (id == R.id.nav_terms_of_use) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("jp.clipline.clsimplecamera", "jp.clipline.clsimplecamera.DocumentDisplayActivity");
            intent.putExtra("ASSETS_HTML_FILE", "https://clipline.jp/service/terms");
            startActivity(intent);
        } else if (id == R.id.nav_external_file_import) {
            Toast.makeText(CameraActivity.this, getString(R.string.compress_start), Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("video/*"), CHOSE_FILE_CODE);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isOrientationPortrait() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    private void setOperation(boolean bool) {
        mImageButtonChangeVideo.setEnabled(bool);
        mImageButtonChangePicture.setEnabled(bool);

        mImageButtonChangeAspectWide.setEnabled(bool);
        mImageButtonChangeAspectStandard.setEnabled(bool);
        mImageButtonBackFront.setEnabled(bool);
        //mImageButtonIcSettingsButton.setEnabled(bool);
        //mImageViewOpenGallery.setEnabled(bool);
    }

    private void disableOperation() {
        setOperation(false);
    }

    private void enableOperation() {
        setOperation(true);
    }

    private void changeToVideo() {
        mImageButtonChangePicture.setVisibility(View.VISIBLE);
        mImageButtonChangeVideo.setVisibility(View.INVISIBLE);
        mTextViewVideoRecordingTime.setVisibility(View.VISIBLE);
        mButtonPicture.setVisibility(View.INVISIBLE);

        mIntentParameters.setIsVideo(true);
    }

    private void changeToPicture() {
        mImageButtonChangePicture.setVisibility(View.INVISIBLE);
        mImageButtonChangeVideo.setVisibility(View.VISIBLE);
        mTextViewVideoRecordingTime.setVisibility(View.INVISIBLE);
        mButtonPicture.setVisibility(View.VISIBLE);

        mIntentParameters.setIsVideo(false);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CameraActivity.this, SelectShootingMethodActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        finish();
    }

    private boolean isVideoMode() {
        return mIntentParameters.isVideo();
    }

//    private void applyThumbnailToGalleryButton() {
//        SharedPreferences data = getSharedPreferences("settings", MODE_PRIVATE);
//        String filename = data.getString("lastCreatedFile", null);
//        String type = data.getString("lastCreatedFileType", null);
//
//        if (type == null) {
//            return;
//        }
//
//        mImageViewOpenGallery.setImageBitmap(null);
//        mImageViewOpenGallery.setImageURI(null);
//        mImageViewOpenGallery.destroyDrawingCache();
//        if (type.equals("Video")) {
//            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filename, MediaStore.Video.Thumbnails.MINI_KIND);
//            mImageViewOpenGallery.setImageBitmap(bitmap);
//        } else {
//            mImageViewOpenGallery.setImageURI(Uri.parse(filename));
//        }
//    }

    private String collectionTerminalInformation() {

        teardownCamera();

        Point realScreenSize = CameraUtil.getRealScreenSize(getWindowManager());
        String message;
        StringBuilder messages = new StringBuilder();
        message = String.format("%s(%d,%d) : %s端末\n", android.os.Build.MODEL, realScreenSize.x, realScreenSize.y, (CameraUtil.isStandardAspectHardWare(getWindowManager())) ? "スタンダード" : "ワイド");
        messages.append(message);
        message = String.format("APIレベル : %d\n", android.os.Build.VERSION.SDK_INT);
        messages.append(message);
        message = String.format("カメラ個数 : %d\n", Camera.getNumberOfCameras());
        messages.append(message);
        Camera.CameraInfo info = new Camera.CameraInfo();
        // closeCameraInstance();
        // FIXME : 為藤さんに相談
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            Camera camera = Camera.open(i);
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
            for (Camera.Size size : supportedPictureSizes) {
                message = String.format("カメラ%d : 写真解像度 : %dx%d\n", (i + 1), size.width, size.height);
                messages.append(message);
            }
            // http://stackoverflow.com/questions/14263521/android-getsupportedvideosizes-allways-returns-null
            List<Camera.Size> supportedVideoSizes = parameters.getSupportedVideoSizes();
            if (supportedVideoSizes == null) {
                supportedVideoSizes = parameters.getSupportedPreviewSizes();
            }
            for (Camera.Size size : supportedVideoSizes) {
                message = String.format("カメラ%d : 動画解像度 : %dx%d\n", (i + 1), size.width, size.height);
                messages.append(message);
            }
            List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            for (Camera.Size size : supportedPreviewSizes) {
                message = String.format("カメラ%d : プレビュー解像度 : %dx%d\n", (i + 1), size.width, size.height);
                messages.append(message);
            }
            camera.release();
        }

        messages.append(getString(R.string.mail_template));

        setupCamera();

        return messages.toString();
    }

    private void takePicture() {
        // mIsShutter = true ;
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureSize(mCurrentAspectPictureSize.width, mCurrentAspectPictureSize.height);
        Log.d(TAG, String.format("takePicture [%d, %d]", mCurrentAspectPictureSize.width, mCurrentAspectPictureSize.height));
        parameters.setJpegQuality(JPEG_QUALITY);
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        // Autoが選べるもののみAoutoFocusを適用
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
        mCamera.takePicture(mShutterCallback, null, mPictureCallback);
    }

    private void initializeVideoSettings() {
        Log.v(TAG, "initializeVideoSettings - Start");
        // TODO 自動生成されたメソッド・スタブ
        try {
            mMediaRecorder = new MediaRecorder();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 録画の入力ソースを指定
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // ファイルフォーマットを指定
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // ビデオエンコーダを指定
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncodingBitRate(ENCODING_BIT_RATE);
            mMediaRecorder.setVideoFrameRate(FRAME_RATE); // 動画のフレームレートを指定
            if (mIntentParameters.isBackShooting()) {
                mMediaRecorder.setOrientationHint(adjustDisplayOrientation());
            } else {
                switch (adjustDisplayOrientation()) {
//                        case 0:
//                            matrix.setRotate(180);
//                            break;
                    case 90:
                        mMediaRecorder.setOrientationHint(270);
                        break;
                    case 270:
                        mMediaRecorder.setOrientationHint(90);
                        break;
//                        case 180:
//                            matrix.setRotate(0);
//                            break;
                    default:
                        mMediaRecorder.setOrientationHint(adjustDisplayOrientation());
                        break;
                }
            }
            Log.d(TAG, String.format("録画サイズ [%d, %d]", mCurrentAspectVideoSize.width, mCurrentAspectVideoSize.height));
            mMediaRecorder.setVideoSize(mCurrentAspectVideoSize.width, mCurrentAspectVideoSize.height); // 動画のサイズを指定
            mNextVideoAbsolutePath = CameraUtil.getVideoFilePath();
            File file = new File(mNextVideoAbsolutePath);
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            mMediaRecorder.setPreviewDisplay(mSurfaceViewPreview.getHolder().getSurface());//
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            String message = String.format("ファイルを生成しました [%s] size=(%d, %d) frame=%d bit=%d", file.getAbsolutePath(), mCurrentAspectVideoSize.width, mCurrentAspectVideoSize.height, FRAME_RATE, ENCODING_BIT_RATE);
            Log.d(TAG, message);
        } catch (IOException e) {
            Log.e(TAG, String.format("initializeVideoSettings -> IOException : message=%s, stacktrace=%s", e.getMessage(), e.getStackTrace()[0]));
        }
        Log.v(TAG, "initializeVideoSettings - End");
    }

    private void setupCamera() {
        releaseCameraAndPreview();
        int cameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

        if (Camera.getNumberOfCameras() > 1 && !mIsBackShooting) { // カメラが一個以上で、背面以外選択時はフロント
            cameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        if (mCamera == null) {
            try {
                Log.e(TAG, String.format("setupCamera : open camera %s", (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) ? "背面" : "前面"));
                mCamera = Camera.open(cameraFacing);
            } catch (Exception e) {
                Log.e(TAG, String.format("getCameraInstance(Camera is not available) -> Exception : message=%s, stacktrace=%s", e.getMessage(), e.getStackTrace().toString()));
            }
        } else {
            Log.e(TAG, String.format("setupCamera - already opend camera"));
        }

        setVariousAspectSize();

        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPictureSize(mCurrentAspectPictureSize.width, mCurrentAspectPictureSize.height);
        parameters.setPreviewSize(mCurrentAspectPictureSize.width, mCurrentAspectPictureSize.height);
        Log.d(TAG, String.format("@@@ setPictureSize [%d, %d]", mCurrentAspectPictureSize.width, mCurrentAspectPictureSize.height));
        Log.d(TAG, String.format("@@@ setPreviewSize [%d, %d]", mCurrentAspectPictureSize.width, mCurrentAspectPictureSize.height));
        mSeekBarZoom.setMax(parameters.getMaxZoom());
        mSeekBarZoom.setProgress(parameters.getZoom());
        mSeekBarBrightness.setMax(parameters.getMaxExposureCompensation());
        mSeekBarBrightness.setProgress(parameters.getExposureCompensation());
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // FIXME : バグありの関数
        mCamera.setDisplayOrientation(adjustDisplayOrientation());
        // 必ずOnLayoutChangeListenerが呼ばれるように明示的に実行（アスペクト比変更時に呼ばれない場合があった）
        mRelativeLayoutPreview.requestLayout();
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void teardownCamera() {
        if (mCamera != null) {
            Log.d(TAG, "teardownCamera - Release Camera");
            try {
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.release();
            } finally {
                mCamera = null;
            }
        } else {
            Log.d(TAG, "teardownCamera - already released Camera");
        }
    }

    private static boolean isAvailableAspectRatio(int width, int height, int aspectWidth, int aspectHeight) {
        return (width == height * aspectWidth / aspectHeight);
    }

    private static boolean isAvailableAspectRatioStandard(int width, int height) {
        return isAvailableAspectRatio(width, height, 4, 3);
    }

    private static boolean isAvailableAspectRatioWide(int width, int height) {
        return isAvailableAspectRatio(width, height, 16, 9);
    }

    private static void chooseVideoSize(Camera.Size[] choices) {
        // http://wada811.blogspot.com/2013/11/media-recorder-start-failed-in-specific-model.html
        CamcorderProfile profile_high = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        CamcorderProfile profile_low = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        // 縦横比(16:9)とサイズと録画可能サイズで解像度を絞り込んでいてる
        mStandardAspectVideoSizeList.clear();
        mWideAspectVideoSizeList.clear();
        for (Camera.Size size : choices) {
            //
            if (isAvailableAspectRatioStandard(size.width, size.height) &&
                    (size.width >= profile_low.videoFrameWidth && size.height >= profile_low.videoFrameHeight) &&
                    (size.width <= profile_high.videoFrameWidth && size.height <= profile_high.videoFrameHeight)) {
                mStandardAspectVideoSizeList.add(size);
                // Log.d(TAG, String.format("利用可能ビデオサイズ [%d, %d] スタンダード対象アスペクト比", size.width, size.height));
            } else if (isAvailableAspectRatioWide(size.width, size.height) &&
                    (size.width >= profile_low.videoFrameWidth && size.height >= profile_low.videoFrameHeight) &&
                    (size.width <= profile_high.videoFrameWidth && size.height <= profile_high.videoFrameHeight)) {
                mWideAspectVideoSizeList.add(size);
                // Log.d(TAG, String.format("利用可能ビデオサイズ [%d, %d] ワイド対象アスペクト比", size.width, size.height));
            } else {
                // Log.d(TAG, String.format("利用可能ビデオサイズ [%d, %d]", size.width, size.height));
            }
        }
        Collections.sort(mStandardAspectVideoSizeList, CameraUtil.buildSizeComparative());
        Collections.sort(mWideAspectVideoSizeList, CameraUtil.buildSizeComparative());
    }

    private static void choosePictureSize(Camera.Size[] choices) {
        // 縦横比(16:9)とサイズと録画可能サイズで解像度を絞り込んでいてる
        mStandardAspectPictureSizeList.clear();
        mWideAspectPictureSizeList.clear();
        for (Camera.Size size : choices) {
            //
            if (isAvailableAspectRatioStandard(size.width, size.height)) {
                mStandardAspectPictureSizeList.add(size);
                // Log.d(TAG, String.format("利用可能ピクチャーサイズ [%d, %d] スタンダード対象アスペクト比", size.width, size.height));
            } else if (isAvailableAspectRatioWide(size.width, size.height)) {
                mWideAspectPictureSizeList.add(size);
                // Log.d(TAG, String.format("利用可能ピクチャーサイズ [%d, %d] ワイド対象アスペクト比", size.width, size.height));
            } else {
                // Log.d(TAG, String.format("利用可能ピクチャーサイズ [%d, %d]", size.width, size.height));
            }
        }
        Collections.sort(mStandardAspectPictureSizeList, CameraUtil.buildSizeComparative());
        Collections.sort(mWideAspectPictureSizeList, CameraUtil.buildSizeComparative());
    }

    // シャッターが押されたときに呼ばれるコールバック
    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
//            mIsShutter = true ;
            disableOperation();
        }
    };

    private void startPictureTakeThread() {
        // 画像処理を行うためのスレッドを立てる
        mHandlerThreadTakePicture = new HandlerThread("TakePicture");
        mHandlerThreadTakePicture.start();
        mHandlerTakePicture = new Handler(mHandlerThreadTakePicture.getLooper());
    }

    private void stopPictureTakeThread() {
        // スレッドを止める
        mHandlerThreadTakePicture.quitSafely();
        try {
            mHandlerThreadTakePicture.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            mHandlerThreadTakePicture = null;
            mHandlerTakePicture = null;
        }
    }

    private boolean isCameraWorking() {
        return mIsRecording; //|| mIsShutter ;
    }

    //　端末の向きに合わせてカメラの角度を調整する 
    private int _adjustDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mIntentParameters.isBackShooting() ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        // 端末の方向に合わせて、調整する値を決定する
        int degrees = 0;
        switch (getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        // 最終的なカメラの角度を計算する。 
        // 0〜360度に収まるように、360を足した上で、360で割った余りを計算する
        Log.d(TAG, String.format("【FIXME】adjustDisplayOrientation カメラの角度 = %d", (info.orientation - degrees + 360) % 360));
        return (info.orientation - degrees + 360) % 360;
    }

    public int adjustDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(mIntentParameters.isBackShooting() ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    private void _transferGalleryIntent() {
        // ギャラリー表示
        // @see : http://androidkaihatu.blog.fc2.com/blog-entry-21.html
        Intent intent;
        try {
            // for Honycomb
            intent = new Intent();
            intent.setClassName("com.android.gallery3d", "com.android.gallery3d.app.Gallery");
            startActivity(intent);
        } catch (Exception e) {
            try {
                // for Recent device
                intent = new Intent();
                intent.setClassName("com.cooliris.media", "com.cooliris.media.Gallery");
                startActivity(intent);
            } catch (ActivityNotFoundException e1) {
                try {
                    // for Other device except HTC
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("content://media/external/images/media"));
                    startActivity(intent);
                } catch (ActivityNotFoundException e2) {
                    // for HTC
                    intent = new Intent();
                    intent.setClassName("com.htc.album", "com.htc.album.AlbumTabSwitchActivity");
                    startActivity(intent);
                }
            }
        }
    }

    private void transferGalleryIntent() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            SharedPreferences data = getSharedPreferences("settings", MODE_PRIVATE);
            String filename = data.getString("lastCreatedFile", null);
            String type = data.getString("lastCreatedFileType", null);
            if (type == null) {
                intent.setData(Uri.parse("content://media/external/images/media"));
            } else {
                intent.setDataAndType(Uri.fromFile(new File(filename)), (type == "Picture") ? "image/png" : "video/mp4");
            }
            startActivity(intent);
        } catch (ActivityNotFoundException e2) {
            e2.printStackTrace();
        }
    }

    private void setVariousAspectSize() {
        Camera.Parameters parameters = mCamera.getParameters();

        // http://stackoverflow.com/questions/14263521/android-getsupportedvideosizes-allways-returns-null
        mVideoSizeList = parameters.getSupportedVideoSizes();
        if (mVideoSizeList == null) {
            mVideoSizeList = parameters.getSupportedPreviewSizes();
        }
        Collections.sort(mVideoSizeList, CameraUtil.buildSizeComparative());
        chooseVideoSize((Camera.Size[]) mVideoSizeList.toArray(new Camera.Size[0]));

        mPictureSizeList = parameters.getSupportedPictureSizes();
        Collections.sort(mPictureSizeList, CameraUtil.buildSizeComparative());
        choosePictureSize((Camera.Size[]) mPictureSizeList.toArray(new Camera.Size[0]));

        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();

        mStandardAspectVideoSize = mCamera.new Size(STANDARD_VIDEO_WIDTH, STANDARD_VIDEO_HEIGHT);
        if (!mStandardAspectVideoSizeList.contains(mStandardAspectVideoSize)) {
            mStandardAspectVideoSize = mStandardAspectVideoSizeList.get(0);
        }
        Log.d(TAG, String.format("ビデオサイズ＠スタンダード [%d, %d]", mStandardAspectVideoSize.width, mStandardAspectVideoSize.height));

        mWideAspectVideoSize = mCamera.new Size(WIDE_VIDEO_WIDTH, WIDE_VIDEO_HEIGHT);
        if (!mWideAspectVideoSizeList.contains(mWideAspectVideoSize)) {
            mWideAspectVideoSize = mWideAspectVideoSizeList.get(0);
        }
        Log.d(TAG, String.format("ビデオサイズ＠ワイド [%d, %d]", mWideAspectVideoSize.width, mWideAspectVideoSize.height));

        mStandardAspectPictureSize = mStandardAspectPictureSizeList.get(0);
        mWideAspectPictureSize = mWideAspectPictureSizeList.get(0);
        Point sizeRealScreen = CameraUtil.getRealScreenSize(getWindowManager());
        Iterator<Camera.Size> iterator = null;
        // リアルディスプレイ解像度以下で最も大きいものを利用
        iterator = mStandardAspectPictureSizeList.iterator();
        while (iterator.hasNext()) {
            Camera.Size currentSize = iterator.next();
            if ((currentSize.width <= sizeRealScreen.x && currentSize.height <= sizeRealScreen.y)
                    && (previewSizeList.contains(currentSize))) {
                mStandardAspectPictureSize = currentSize;
            }
        }
        Log.d(TAG, String.format("ピクチャーサイズ＠スタンダード [%d, %d]", mStandardAspectPictureSize.width, mStandardAspectPictureSize.height));
        iterator = mWideAspectPictureSizeList.iterator();
        while (iterator.hasNext()) {
            Camera.Size currentSize = iterator.next();
            if ((currentSize.width <= sizeRealScreen.x && currentSize.height <= sizeRealScreen.y)
                    && (previewSizeList.contains(currentSize))) {
                mWideAspectPictureSize = currentSize;
            }
        }
        Log.d(TAG, String.format("ピクチャーサイズ＠ワイド [%d, %d]", mWideAspectPictureSize.width, mWideAspectPictureSize.height));

        mCurrentAspectVideoSize = (mIntentParameters.isAspectWide()) ? mWideAspectVideoSize : mStandardAspectVideoSize;
        mCurrentAspectPictureSize = (mIntentParameters.isAspectWide()) ? mWideAspectPictureSize : mStandardAspectPictureSize;
    }

    private boolean activityRequestPermissions(int requestCode) {
        if (Build.VERSION.SDK_INT >= 23) {
            ArrayList<String> permissions = CameraUtil.getSettingPermissions(this);
            boolean isRequestPermission = false;
            for (String permission : permissions) {
                if (!CameraUtil.hasSelfPermission(this, permission)) {
                    isRequestPermission = true;
                    break;
                }
            }
            if (isRequestPermission) {
                // requestPermissions(permissions.toArray(new String[0]), requestCode);
                return true;
            }
        }
        return false;
    }

    // パーミッション要求の結果を受け取る
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        Log.d(TAG, "@@@ onRequestPermissionsResult : Start @@@");
        if (requestCode == PERMISSION_REQUEST_CODE) {

            // 許可されたパーミッションがあるかを確認する
            boolean isSomethingGranted = false;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    isSomethingGranted = true;
                    break;
                }
            }

            if (isSomethingGranted) {
                // 設定を変更してもらえた場合、処理を継続する
                //onUserLocationAvailable();
            } else {
                // 設定を変更してもらえなかった場合、終了
                Log.d(TAG, "@@@ onRequestPermissionsResult : Call Finish @@@");
                finish();
            }
        }
        Log.d(TAG, "@@@ onRequestPermissionsResult : End @@@");
    }

    private void holdSettingForGalleryButton(String filename, boolean isVideo) {
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putString("lastCreatedFile", filename);
        editor.putString("lastCreatedFileType", isVideo ? "Video" : "Picture");
        editor.apply();
    }

    // ------------------------------------------------------------------------------------------ //
    //  Listener
    // ------------------------------------------------------------------------------------------ //

    /**
     * ピンチイン/アウトのListener
     */
    private ScaleGestureDetector.SimpleOnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if ((mBeforeScale != 0.0f) && ((mBeforeScale - detector.getScaleFactor()) != 0.0f)) {
                float difference = -(mBeforeScale - detector.getScaleFactor());
                Camera.Parameters parameters = mCamera.getParameters();
                if (difference > 0) {
                    if (mSeekBarZoom.getProgress() != mSeekBarZoom.getMax()) {
                        mSeekBarZoom.setProgress(mSeekBarZoom.getProgress() + 1);
                        parameters.setZoom(parameters.getZoom() + 1);
                    }
                } else {
                    if (mSeekBarZoom.getProgress() != 0) {
                        mSeekBarZoom.setProgress(mSeekBarZoom.getProgress() - 1);
                        parameters.setZoom(parameters.getZoom() - 1);
                    }
                }
                mCamera.setParameters(parameters);
            }
            mBeforeScale = detector.getScaleFactor();
            return super.onScale(detector);
        }
    };

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListenerZoom = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            if (mCamera != null) { // 縦横切替時のnullガード
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setZoom(progress);
                mCamera.setParameters(parameters);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListenerBrightness = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
            if (mCamera != null) { // 縦横切替時のnullガード
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setExposureCompensation(progress);
                mCamera.setParameters(parameters);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    // 撮影サイズに応じて描画領域にサイズ調整して貼り付けます
    private View.OnLayoutChangeListener relativeLayoutListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            Log.d(TAG, String.format("@@@ View.OnLayoutChangeListener [%d,%d,%d,%d] [%d,%d,%d,%d]", left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom));
            if ((left == oldLeft) && (top == oldTop) && (right == oldRight) && (bottom == oldBottom)) {
                return;
            }
            double ratioPicture;
            ratioPicture = (double) Math.max(mCurrentAspectPictureSize.width, mCurrentAspectPictureSize.height) / (double) Math.min(mCurrentAspectPictureSize.width, mCurrentAspectPictureSize.height);

            if (isOrientationPortrait()) {
                int w = mRelativeLayoutPreview.getWidth();
                int h = (int) (ratioPicture * (double) w);
                int dw = 0;
                int dh = 0;
                // 画面サイズに収まらない場合の補正処理
                if (h > mRelativeLayoutPreview.getHeight()) {
                    dh = h - mRelativeLayoutPreview.getHeight();
                    dw = (int) ((double) dh / ratioPicture);
                }
                mRelativeLayoutPreview.removeView(mSurfaceViewPreview);
                mRelativeLayoutPreview.addView(mSurfaceViewPreview, w - dw, h - dh);
                Log.d(TAG, String.format("画面サイズ 縦置き %s [%d, %d]", !mIntentParameters.isAspectWide() ? "標準" : "ワイド", w - dw, h - dh));
            } else {
                int h = mRelativeLayoutPreview.getHeight();
                int w = (int) (ratioPicture * (double) h);
                int dw = 0;
                int dh = 0;
                // 画面サイズに収まらない場合の補正処理
                if (w > mRelativeLayoutPreview.getWidth()) {
                    dw = w - mRelativeLayoutPreview.getWidth();
                    dh = (int) ((double) dw / ratioPicture);
                }
                mRelativeLayoutPreview.removeView(mSurfaceViewPreview);
                mRelativeLayoutPreview.addView(mSurfaceViewPreview, w - dw, h - dh);
                Log.d(TAG, String.format("画面サイズ 横置き %s [%d, %d]", !mIntentParameters.isAspectWide() ? "標準" : "ワイド", w - dw, h - dh));
            }
        }
    };

    private View.OnClickListener mImageButtonBackFrontListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIntentParameters.setIsBackShooting(!mIntentParameters.isBackShooting());
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            intent.setAction("CameraSettingChange");
            mIntentParameters.apply(intent);
            startActivity(intent);
            overridePendingTransition(0, 0);

            finish();
        }
    };

    private View.OnClickListener mImageButtonChangeAspectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIntentParameters.setIsAspectWide(!mIntentParameters.isAspectWide());
            Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
            intent.setAction("CameraSettingChange");
            mIntentParameters.apply(intent);
            startActivity(intent);
            overridePendingTransition(0, 0);

            finish();
        }
    };

    private View.OnClickListener mImageButtonChangePictureListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            changeToPicture();
        }
    };

    private View.OnClickListener mImageButtonChangeVideoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            changeToVideo();
        }
    };

    private View.OnClickListener mImageViewOpenGalleryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            transferGalleryIntent();
        }
    };

    private View.OnClickListener mButtonVideoAndVideoRecording = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            long time = System.currentTimeMillis();
            if (time - mButtonVideoLastClickTime < CONTINUOUS_PRESS_INTERVAL) {
                Log.v(TAG, "連続押下破棄");
                Log.v(TAG, "onClick - End");
                return;
            }
            mButtonVideoLastClickTime = time;

            if (isVideoMode()) {
                // 録画中でなければ録画を開始
                if (!isCameraWorking()) {
                    Log.v(TAG, "onClick - RecordStart - Start");
                    initializeVideoSettings(); // MediaRecorderの設定
                    mIsRecording = true; // 録画中のフラグを立てる
                    disableOperation();
                    mButtonVideo.setVisibility(View.GONE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);


                    // 録画中であれば録画を停止

                    mTimerVideoRecordingTimeUpdate = new Timer();
                    mTimerVideoRecordingTimeUpdate.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mHandlerVideoRecordingTimeUpdate.post(new Runnable() {
                                public void run() {
                                    //実行間隔分を加算処理
                                    mLaptime += 1;
                                    //現在のLapTime
                                    int hour = (mLaptime / (3600)) % 60;
                                    int min = (mLaptime / 60) % 60;
                                    int sec = mLaptime % 60;
                                    mTextViewVideoRecordingTime.setText(String.format("%01d:%02d:%02d", hour, min, sec));
                                }
                            });
                        }
                    }, 1000, 1000);
                    Log.v(TAG, "onClick - RecordEnd - End");
                } else {
                    Log.v(TAG, "onClick - RecordStop - Start");
                    mMediaRecorder.stop(); // 録画停止
                    CameraUtil.registVideoAndroidDB(mNextVideoAbsolutePath, CameraActivity.this.getApplicationContext());
                    mMediaRecorder.reset(); // 無いとmediarecorder went away with unhandled
                    // events　が発生
                    mMediaRecorder.release();//
                    mMediaRecorder = null;
                    mIsRecording = false; // 録画中のフラグを外す
                    mButtonVideo.setVisibility(View.VISIBLE);
                    enableOperation();
                    mTimerVideoRecordingTimeUpdate.cancel();
                    mTimerVideoRecordingTimeUpdate = null;
                    mHandlerVideoRecordingTimeUpdate.removeCallbacksAndMessages(null);
                    mLaptime = 0;
                    mTextViewVideoRecordingTime.setText(String.format("%01d:%02d:%02d", 0, 0, 0));

                    holdSettingForGalleryButton(mNextVideoAbsolutePath, true);

                    //applyThumbnailToGalleryButton();

                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    Log.v(TAG, "onClick - RecordStop - Start");
                    if (mIntentParameters.isBrowserCalling()) {
//                    Uri uri = mIntentParameters.getExtraOutput();
//                    if(uri != null) {
//                        try {
//                            // 出力ファイル指定がある場合には、出力
//                            FileInputStream fileInputStream = new FileInputStream(mNextVideoAbsolutePath);
//                            byte[] readBytes = new byte[fileInputStream.available()];
//                            fileInputStream.read(readBytes);
//                            OutputStream outputStream = getContentResolver().openOutputStream(uri);
//                            outputStream.write(readBytes);
//                            outputStream.close();
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }

                        // @FIXME
                        try {
//                            Intent intent = new Intent(CameraActivity.this, SelectShootingMethodActivity.class);
//                        ContentResolver contentResolver = getContentResolver();
                            // intent.setData();
//                        intent.setData(Uri.fromFile(new File(mNextVideoAbsolutePath)));
//                            intent.putExtra("path_result", Uri.fromFile(new File(mNextVideoAbsolutePath)));
//                            intent.putExtra("type", "video/mp4");
//                            startActivity(intent);
//                        setResult(RESULT_OK, intent);
//                    }
                            String path = AndroidUtility.getFilePath(CameraActivity.this, Uri.fromFile(new File(mNextVideoAbsolutePath)));
                            Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                            ((ClWebWrapperApplication) getApplication()).setTodoContent(path, "video/mp4");
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                            finish();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                if (!isCameraWorking()) {
                    takePicture();
                }
            }
        }
    };

    // ------------------------------------------------------------------------------------------ //
    //  Callback
    // ------------------------------------------------------------------------------------------ //

    // サーフェイスのコールバック
    private SurfaceHolder.Callback mSurfaceHolderCallback = new SurfaceHolder.Callback() {

        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "mSurfaceHolderCallback#surfaceCreated");
            try {
                // カメラのセットアップ。
                // CameraViewのコンストラクタ内ですでに初期化は終わっているのですが、
                // 念のため、ここでも処理を行う必要があります。
                // 理由は、ユーザーが別アプリを起動するなどして、
                // CameraView を保持しているアクティビティが画面から見えなくなった際、
                // surfaceDestroyed()メソッドで camera のインスタンスが解放されるためです。
                setupCamera();
            } catch (Exception e) {
                Log.e(TAG, String.format("mSurfaceHolderCallback#surfaceCreated -> Exception : message=%s, stacktrace=%s", e.getMessage(), e.getStackTrace()[0]));
            }

            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.e(TAG, String.format("mSurfaceHolderCallback#surfaceCreated -> IOException : message=%s, stacktrace=%s", e.getMessage(), e.getStackTrace()[0]));
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "mSurfaceHolderCallback#surfaceChanged");
            try {
                mCamera.stopPreview();
                mCamera.startPreview();
            } catch (Exception e) {
                Log.e(TAG, String.format("mSurfaceHolderCallback#surfaceChanged -> Exception : message=%s, stacktrace=%s", e.getMessage(), e.getStackTrace()[0]));
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "mSurfaceHolderCallback#surfaceDestroyed");
            try {
                teardownCamera();
            } catch (Exception e) {
                Log.e(TAG, String.format("mSurfaceHolderCallback#surfaceDestroyed -> Exception : message=%s, stacktrace=%s", e.getMessage(), e.getStackTrace()[0]));
            }
        }
    };

    // JPEGイメージ生成後に呼ばれるコールバック
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        // データ生成完了
        public void onPictureTaken(byte[] originalBytesData, Camera camera) {
            // SDカードにJPEGデータを保存する
            if (originalBytesData != null) {
                startPictureTakeThread();

                // Bitmapの回転（縦横を補完する為）とPngへの変換を行う
                int rotate = 0;
                Matrix matrix = new Matrix();
                if (mIntentParameters.isBackShooting()) {
                    matrix.setRotate(adjustDisplayOrientation());
                    rotate = adjustDisplayOrientation();

                } else {
                    switch (adjustDisplayOrientation()) {
                        case 0:
                            rotate = 0;
                            matrix.setRotate(0);
                            break;
                        case 90:
                            rotate = 270;
                            matrix.setRotate(270);
                            break;
                        case 270:
                            rotate = 90;
                            matrix.setRotate(90);
                            break;
                        case 180:
                            rotate = 180;
                            matrix.setRotate(180);
                            break;
                        default:
                            rotate = adjustDisplayOrientation();
                            matrix.setRotate(adjustDisplayOrientation());

                            break;
                    }
                }
                Bitmap original = BitmapFactory.decodeByteArray(originalBytesData, 0, originalBytesData.length);
                Bitmap rotated = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                rotated.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] bytesData = byteArrayOutputStream.toByteArray();

                String filename = CameraUtil.getPhotoFilePath();
                mHandlerTakePicture.post(new PictureSaver(bytesData, filename, getApplicationContext()));

                holdSettingForGalleryButton(filename, false);

                //mImageViewOpenGallery.setImageBitmap(BitmapFactory.decodeByteArray(bytesData, 0, bytesData.length));

                // プレビューを再開する
                camera.startPreview();
                enableOperation();

                // ブラウザからの起動の場合終了する
                if (mIntentParameters.isBrowserCalling()) {

                    Uri uri = Uri.parse(mIntentParameters.getExtraOutput());

                    if (uri != null) {
                        try {

                            Log.e("ExifInterface", "" + rotate);
                            // 出力ファイル指定がある場合には、出力
                            OutputStream outputStream = getContentResolver().openOutputStream(uri);
                            outputStream.write(originalBytesData);
                            outputStream.close();

                            ExifInterface ef = new ExifInterface(String.valueOf(uri).replace("file:/", ""));
                            ef.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(rotate));
                            ef.saveAttributes();
                            // @FIXME
//                            Intent returnIntent = new Intent(CameraActivity.this, SelectShootingMethodActivity.class);
//                            returnIntent.putExtra("path_result", uri);
//                            returnIntent.putExtra("type", "image/png");
//                            startActivity(returnIntent);
                            String path = AndroidUtility.getFilePath(CameraActivity.this, uri);
                            Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                            ((ClWebWrapperApplication) getApplication()).setTodoContent(path, "image/png");
                            startActivity(intent);
                            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                            finish();
//                            setResult(RESULT_OK, returnIntent);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    finish();
                }
            }
        }
    };

}
