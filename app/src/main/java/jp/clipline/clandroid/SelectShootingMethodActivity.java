package jp.clipline.clandroid;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yovenny.videocompress.MediaController;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.Utility.CameraUtil;
import jp.clipline.clandroid.Utility.PopUpDlg;
import jp.clipline.clandroid.api.ToDo;
import jp.clipline.clandroid.view.StatusView;

import static jp.clipline.clandroid.R.id.progressBar;

public class SelectShootingMethodActivity extends AppCompatActivity /*implements FileChooser.FileSelectedListener*/ {

    private static int REQUEST_CODE_PICTURE_CAPTURE = 1;
    private static int REQUEST_CODE_VIDEO_CAPTURE = 2;
    private static int REQUEST_CODE_PICTURE_SELECTOR = 3;
    private static int REQUEST_CODE_VIDEO_SELECTOR = 4;
    private static int REQUEST_CODE_SELECT_FILE = 5;

    private Uri uriPicture = null;
    private ProgressBar mProgressBar;
    public final String APP_DIR = "Clipline";
    public final String COMPRESSED_VIDEOS_DIR = "/Compressed Videos/";
    private String mOutPathVideoSelect;
    private StatusView mStatusView;
    private PopUpDlg mConfirDlg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Intent intent;
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            String uriFile = String.valueOf(bundle.get("path_result"));
//            String type = String.valueOf(bundle.getString("type"));
//            if (uriFile != null && type != null) {
//                try {
//                    String path = AndroidUtility.getFilePath(this, Uri.parse(uriFile));
//                    if (type.equals("image/png")) {
//                        intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
//                        ((ClWebWrapperApplication) getApplication()).setTodoContent(path, "image/png");
//                        startActivity(intent);
//                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                        finish();
//                    } else if (type.equals("video/mp4")) {
//                        intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
//                        ((ClWebWrapperApplication) this.getApplication()).setTodoContent(path, "video/mp4");
//                        startActivity(intent);
//                        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                        finish();
//                    }
//                } catch (URISyntaxException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
        if (!((ClWebWrapperApplication) this.getApplication()).isBack()) {
            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
        }
        setContentView(R.layout.activity_select_shooting_method);
        mProgressBar = (ProgressBar) findViewById(progressBar);

        LinearLayout llView;

        llView = (LinearLayout) findViewById(R.id.textViewTakePicture);
        llView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                String file_name = System.currentTimeMillis() + ".png";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, file_name);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                uriPicture = Uri.fromFile(new File(CameraUtil.getPhotoFilePath()));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, String.valueOf(uriPicture));
                startActivity(intent);
//                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                finish();
            }
        });

        llView = (LinearLayout) findViewById(R.id.textViewTakeMovie);
        llView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivity(intent);
//                overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                finish();
            }
        });
/*
        textView = (TextView) findViewById(R.id.textViewSelectPicture);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/png"), REQUEST_CODE_PICTURE_SELECTOR);
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/png");
                startActivityForResult(photoPickerIntent, REQUEST_CODE_PICTURE_SELECTOR);
            }
        });

        textView = (TextView) findViewById(R.id.textViewSelectMovie);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("video/mp4"), REQUEST_CODE_VIDEO_SELECTOR);
//                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
//                photoPickerIntent.setType("video/mp4");
//                startActivityForResult(photoPickerIntent, REQUEST_CODE_VIDEO_SELECTOR);

                startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("video*/
/*"), REQUEST_CODE_VIDEO_SELECTOR);
            }
        });
*/
        TextView textView = (TextView) findViewById(R.id.textViewSelectCameraRoll);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent()
                        .setType("image/png|image/jpg|application/pdf|video/mp4")
                        .setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_SELECT_FILE);
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                //startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
//                FileChooser fileChooser = new FileChooser(SelectShootingMethodActivity.this);
//                fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
//                    @Override
//                    public void fileSelected(File file) {
//                        try {
//                            Log.i("selected file: ", file.getAbsolutePath());
//                            // Get the Uri of the selected file
//                            Uri uri = Uri.fromFile(file);
//                            String uriString = uri.toString();
//                            File myFile = new File(uriString);
//                            String path = myFile.getAbsolutePath();
//                            String displayName = null;
//
//                            if (uriString.startsWith("content://")) {
//                                Cursor cursor = null;
//                                try {
//                                    cursor = getContentResolver().query(uri, null, null, null, null);
//                                    if (cursor != null && cursor.moveToFirst()) {
//                                        displayName = cursor.getString(cursor.getColumnIndex(
//                                                android.provider.OpenableColumns.DISPLAY_NAME));
//                                    }
//                                } finally {
//                                    cursor.close();
//                                }
//                            } else if (uriString.startsWith("file://")) {
//                                displayName = myFile.getName();
//                            }
//
//                            String contentType = "";
//                            String pathfile = AndroidUtility.getFilePath(SelectShootingMethodActivity.this, uri);
//                            if (displayName.toLowerCase().endsWith("pdf")) {
//                                contentType = "application/pdf";
//                            } else if (displayName.toLowerCase().endsWith("mp4")) {
//                                contentType = "video/mp4";
//                                CreateCompressDir();
//                                mOutPathVideoSelect = Environment.getExternalStorageDirectory()
//                                        + File.separator
//                                        + APP_DIR
//                                        + COMPRESSED_VIDEOS_DIR
//                                        + "VIDEO_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4";
//                                new VideoCompressor().execute(pathfile, mOutPathVideoSelect, contentType);
//                                return;
//                            } else if (displayName.toLowerCase().endsWith("png") || displayName.toLowerCase().endsWith("jpg")) {
//                                contentType = "image/png";
//                            } else {
//                                return;
//                            }
//
//                            Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
//                            ((ClWebWrapperApplication) getApplication()).setTodoContent(pathfile/*data.getData()*/, contentType);
//                            startActivity(intent);
//                            overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                            finish();
//
//                        } catch (URISyntaxException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                fileChooser.showDialog();
/*
                FileChooser fileChooser = new FileChooser(getParent());
                fileChooser.setExtension("image/png|image/jpg|application/pdf|video/mp4");
                fileChooser.showDialog();
*/
            }
        });

        mStatusView = (StatusView) findViewById(R.id.statusView);
        mStatusView.setTypeView(StatusView.STATUS_VIEW.SELECT);
        mStatusView.setClickListener(new StatusView.ClickListener() {
            @Override
            public void onListenerView() {
                mConfirDlg = new PopUpDlg(SelectShootingMethodActivity.this, true);
                mConfirDlg.show("", getString(R.string.confirm_retry),
                        getString(R.string.yes),
                        getString(R.string.no),
                        // onOK
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                                String id = todoParameters.get("id");
                                String categoryId = todoParameters.get("categoryId");
                                String todoContentId = todoParameters.get("todoContentId");
                                Boolean isStudent = "student".equals(todoParameters.get("loginType"));

                                String url;

                                if (isStudent) {
                                    url = "%s://%s/training/#/students/" + id
                                            + "/todos/" + todoContentId;
                                } else {
                                    url = "%s://%s/training/#/coachs/" + id
                                            + "/todos/" + todoContentId;
                                }

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


        LinearLayout backScreen = (LinearLayout) findViewById(R.id.imageButtonBack);
        backScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                String id = todoParameters.get("id");
                String categoryId = todoParameters.get("categoryId");
                String todoContentId = todoParameters.get("todoContentId");
                Boolean isStudent = "student".equals(todoParameters.get("loginType"));

                String url;

                if (isStudent) {
                    url = "%s://%s/training/#/students/" + id
                            + "/todos/" + todoContentId;
                } else {
                    url = "%s://%s/training/#/coachs/" + id
                            + "/todos/" + todoContentId;
                }

                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                intent.putExtra("BASE_URL", url);
                startActivity(intent);
                overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                finish();
            }
        });

        Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
        String id = todoParameters.get("id");
        String categoryId = todoParameters.get("categoryId");
        String todoContentId = todoParameters.get("todoContentId");
        Boolean isStudent = "student".equals(todoParameters.get("loginType"));

        new GetTodoInformationTask().execute(AndroidUtility.getCookie(getApplicationContext()), id, categoryId, todoContentId);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            try {
//                if (requestCode == REQUEST_CODE_PICTURE_CAPTURE) {
//                    uriPicture = (Uri) data.getExtras().get("path_result");
//                    String path = AndroidUtility.getFilePath(this, uriPicture);
//                    Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
//                    ((ClWebWrapperApplication) getApplication()).setTodoContent(path, "image/png");
//                    startActivity(intent);
////                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
//                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                    finish();
//
//                    return;
//                }
//
//                if (requestCode == REQUEST_CODE_VIDEO_CAPTURE) {
//                    String pathVideo = AndroidUtility.getFilePath(this, data.getData());
//                    Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
//                    ((ClWebWrapperApplication) this.getApplication()).setTodoContent(pathVideo, "video/mp4");
//                    startActivity(intent);
////                    overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
//                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                    finish();
//                    return;
//                }
                if (requestCode == REQUEST_CODE_SELECT_FILE) {
/*
                Uri selectedMediaUri = data.getData();
                String path = selectedMediaUri.getPath();
                String contentType = "image/png";
                if (path.contains("images")) {
                    contentType = "image/png";
                } else  if (path.contains("video")) {
                    contentType = "video/mp4";
                } else {

                }
*/
                    // Get the Uri of the selected file
                    String pathFile = AndroidUtility.getFilePath(this, data.getData());
                    Uri uri = data.getData();
                    String uriString = uri.toString();
                    File myFile = new File(uriString);
                    String path = myFile.getAbsolutePath();
                    String displayName = null;

                    if (uriString.startsWith("content://")) {
                        Cursor cursor = null;
                        try {
                            cursor = getContentResolver().query(uri, null, null, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                displayName = cursor.getString(cursor.getColumnIndex(
                                        android.provider.OpenableColumns.DISPLAY_NAME));
                            }
                        } finally {
                            cursor.close();
                        }
                    } else if (uriString.startsWith("file://")) {
                        displayName = myFile.getName();
                    }

                    String contentType = "image/png";
                    if (displayName.endsWith("pdf")) {
                        contentType = "application/pdf";
                    } else if (displayName.endsWith("mp4")) {
                        contentType = "video/mp4";
                        CreateCompressDir();
                        mOutPathVideoSelect = Environment.getExternalStorageDirectory()
                                + File.separator
                                + APP_DIR
                                + COMPRESSED_VIDEOS_DIR
                                + "VIDEO_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4";
                        new VideoCompressor().execute(pathFile, mOutPathVideoSelect, String.valueOf(requestCode), contentType);
                        return;
                    } else {

                    }

                    Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                    ((ClWebWrapperApplication) this.getApplication()).setTodoContent(pathFile, contentType);
                    startActivity(intent);
//                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                    overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
                    finish();

                    return;
                }

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
/*
            if (requestCode == REQUEST_CODE_PICTURE_SELECTOR) {
                Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                ((ClWebWrapperApplication) this.getApplication()).setTodoContent(data.getData(), "image/png");
                startActivity(intent);
                finish();
                return;
            }

            if (requestCode == REQUEST_CODE_VIDEO_SELECTOR) {
                Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                ((ClWebWrapperApplication) this.getApplication()).setTodoContent(data.getData(), "video/mp4");
                startActivity(intent);
                finish();
                return;
            }
*/
        }else {
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        Log.d("", "");
    }

    public class GetTodoInformationTask extends AsyncTask<String, Void, Boolean> {

        Map<String, Object> todoContent = null;

        GetTodoInformationTask() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String cookie = params[0];
                String id = params[1];
                String categoryId = params[2];
                String todoContentId = params[3];

                todoContent = ToDo.getTodoContent(cookie, categoryId, todoContentId);
                return Boolean.TRUE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                ((ClWebWrapperApplication) getApplication()).setCurrentTodoContent(todoContent);

                TextView textView = (TextView) findViewById(R.id.textViewToDoTitle);
                if (todoContent != null && todoContent.get("title") != null) {
                    textView.setText((String) todoContent.get("title"));
                } else {
                    textView.setText("");
                }
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                        String id = todoParameters.get("id");
                        String categoryId = todoParameters.get("categoryId");
                        String todoContentId = todoParameters.get("todoContentId");
                        Boolean isStudent = "student".equals(todoParameters.get("loginType"));

                        String url;

                        if (isStudent) {
                            url = "%s://%s/training/#/students/" + id
                                    + "/todos/" + todoContentId;
                        } else {
                            url = "%s://%s/training/#/coachs/" + id
                                    + "/todos/" + todoContentId;
                        }

                        Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                        intent.putExtra("BASE_URL", url);
                        startActivity(intent);
                        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                        finish();
                    }
                });

                if (todoContent != null) {
                    boolean hasPlayAction = false;
                    if (todoContent.get("has_play_action") != null) {
                        hasPlayAction = (boolean) todoContent.get("has_play_action");

                        // Only when has_play_action is true, then all status will be visible
                        if (hasPlayAction) {
                            mStatusView.setVisibility(View.VISIBLE);
//                            mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
//                            mImageViewFooterView.setVisibility(View.VISIBLE);
//                            mTextViewFooterView.setVisibility(View.VISIBLE);

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
//                        mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
//                        mImageViewFooterShoot.setVisibility(View.VISIBLE);
//                        mTextViewFooterShoot.setVisibility(View.VISIBLE);
                    }

                    // check has_my_report_play_action
                    if ((todoContent.get("has_my_report_play_action") != null)
                            && ((boolean) todoContent.get("has_my_report_play_action"))) {

                        // 表示
//                        mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
//                        mImageViewFooterCompare.setVisibility(View.VISIBLE);
//                        mTextViewFooterCompare.setVisibility(View.VISIBLE);
                    }
                }
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    public void CreateCompressDir() {
        File f = new File(Environment.getExternalStorageDirectory(), File.separator + APP_DIR);
        f.mkdirs();
        f = new File(Environment.getExternalStorageDirectory(), File.separator + APP_DIR + COMPRESSED_VIDEOS_DIR);
        f.mkdirs();
    }

    class VideoCompressor extends AsyncTask<String, Void, Boolean> {
        String mContentype;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            mContentype = params[3];
//            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
//            metaRetriever.setDataSource(params[0]);
//            String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
//            String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
//            Log.e("doInBackground", height + "\n" + width);
            return MediaController.getInstance().convertVideo(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Boolean compressed) {
            super.onPostExecute(compressed);
            mProgressBar.setVisibility(View.GONE);
            if (compressed) {
                Log.d(SelectShootingMethodActivity.class.getSimpleName(), "Compression successfully!");
                Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                ((ClWebWrapperApplication) getApplication()).setTodoContent(mOutPathVideoSelect, mContentype);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AndroidUtility.setBack(SelectShootingMethodActivity.this, true);
    }
}
