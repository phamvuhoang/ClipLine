package jp.clipline.clandroid;

import android.content.ContentValues;
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
import android.widget.ImageButton;
import android.widget.ImageView;
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
import jp.clipline.clandroid.Utility.FileChooser;
import jp.clipline.clandroid.api.ToDo;

import static jp.clipline.clandroid.R.id.progressBar;

public class SelectShootingMethodActivity extends AppCompatActivity /*implements FileChooser.FileSelectedListener*/ {

    private static int REQUEST_CODE_PICTURE_CAPTURE = 1;
    private static int REQUEST_CODE_VIDEO_CAPTURE = 2;
    private static int REQUEST_CODE_PICTURE_SELECTOR = 3;
    private static int REQUEST_CODE_VIDEO_SELECTOR = 4;
    private static int REQUEST_CODE_SELECT_FILE = 5; ///// 20170506 ADD

    private Uri uriPicture = null;

    ///// 20170506 ADD START
    private LinearLayout mLinearLayoutFooterStatus;
    private ImageView mImageViewFooterView;
    private TextView mTextViewFooterView;
    private ImageView mImageViewFooterShoot;
    private TextView mTextViewFooterShoot;
    private ImageView mImageViewFooterCompare;
    private TextView mTextViewFooterCompare;
    ///// 20170506 ADD END
    ///// 20170509 ADD START
    private ProgressBar mProgressBar;
    public final String APP_DIR = "Clipline";
    public final String COMPRESSED_VIDEOS_DIR = "/Compressed Videos/";
    private String mOutPathVideoSelect;
    ///// 20170509 ADD END

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shooting_method);
        mProgressBar = (ProgressBar) findViewById(progressBar);

        TextView textView;
        ImageButton imageButton;

        textView = (TextView) findViewById(R.id.textViewTakePicture);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                String file_name = System.currentTimeMillis() + ".png";
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, file_name);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                uriPicture = Uri.fromFile(new File(CameraUtil.getPhotoFilePath()));
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriPicture);
                startActivityForResult(intent, REQUEST_CODE_PICTURE_CAPTURE);
                overridePendingTransition(0, 0);
            }
        });

        textView = (TextView) findViewById(R.id.textViewTakeMovie);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE_VIDEO_CAPTURE);
                overridePendingTransition(0, 0);
            }
        });

        ///// 20170506 DELETE START
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
        ///// 20170506 DELETE END

        ///// 20170506 ADD START
        textView = (TextView) findViewById(R.id.textViewSelectCameraRoll);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*
            Intent intent = new Intent()
                    .setType("image/png|image/jpg|application/pdf|video/mp4")
                    .setAction(Intent.ACTION_PICK);

                startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_SELECT_FILE);
*/
                //startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
                FileChooser fileChooser = new FileChooser(SelectShootingMethodActivity.this);
                fileChooser.setFileListener(new FileChooser.FileSelectedListener() {
                    @Override
                    public void fileSelected(File file) {
                        ///// 20170509 MODIFY START
                        try {
                            Log.i("selected file: ", file.getAbsolutePath());
                            // Get the Uri of the selected file
                            Uri uri = Uri.fromFile(file);
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

                            String contentType = "";
                            String pathfile = AndroidUtility.getFilePath(SelectShootingMethodActivity.this, uri);
                            if (displayName.toLowerCase().endsWith("pdf")) {
                                contentType = "application/pdf";
                            } else if (displayName.toLowerCase().endsWith("mp4")) {
                                contentType = "video/mp4";
                                CreateCompressDir();
                                mOutPathVideoSelect = Environment.getExternalStorageDirectory()
                                        + File.separator
                                        + APP_DIR
                                        + COMPRESSED_VIDEOS_DIR
                                        + "VIDEO_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".mp4";
                                new VideoCompressor().execute(pathfile, mOutPathVideoSelect, contentType);
                                return;
                            } else if (displayName.toLowerCase().endsWith("png") || displayName.toLowerCase().endsWith("jpg")) {
                                contentType = "image/png";
                            } else {
                                return;
                            }

                            Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                            ((ClWebWrapperApplication) getApplication()).setTodoContent(pathfile/*data.getData()*/, contentType);
                            startActivity(intent);
                            finish();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        ///// 20170509 MIDIFY END
                    }
                });
                fileChooser.showDialog();
/*
                FileChooser fileChooser = new FileChooser(getParent());
                fileChooser.setExtension("image/png|image/jpg|application/pdf|video/mp4");
                fileChooser.showDialog();
*/
            }
        });

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
        ///// 20170506 ADD END

        imageButton = (ImageButton) findViewById(R.id.imageButtonBack);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ///// 20170506 ADD START
                Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
                String studentId = todoParameters.get("studentId");
                String categoryId = todoParameters.get("categoryId");
                String todoContentId = todoParameters.get("todoContentId");
                String url = "%s://%s/training/#/students/" + studentId
                        + "/todos/" + todoContentId;
                ///// 20170506 ADD END
                Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                ///// 20170506 MODIFY START
                intent.putExtra("BASE_URL", url);
                ///// 20170506 MODIFY END
                startActivity(intent);
                finish();
            }
        });

        ///// 20170507 ADD START
        textView = (TextView) findViewById(R.id.textViewTodoBack);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });
        ///// 20170507 ADD END

        Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
        String studentId = todoParameters.get("studentId");
        String categoryId = todoParameters.get("categoryId");
        String todoContentId = todoParameters.get("todoContentId");
        new GetTodoInformationTask().execute(AndroidUtility.getCookie(getApplicationContext()), studentId, categoryId, todoContentId);
        Log.d("", "");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            try {
                ///// 20170509 MODIFY START
                if (requestCode == REQUEST_CODE_PICTURE_CAPTURE) {
                    String path = AndroidUtility.getFilePath(this, uriPicture);
                    Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                    ((ClWebWrapperApplication) getApplication()).setTodoContent(path, "image/png");
                    startActivity(intent);
                    finish();
                    return;
                }

                if (requestCode == REQUEST_CODE_VIDEO_CAPTURE) {
                    String pathVideo = AndroidUtility.getFilePath(this, data.getData());
                    Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                    ((ClWebWrapperApplication) this.getApplication()).setTodoContent(pathVideo, "video/mp4");
                    startActivity(intent);
                    finish();
                    return;
                }
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
                    finish();
                    return;
                }

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            ///// 20170509 MODIFY END

            ///// 20170506 DELETE START
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
            ///// 20170506 DELETE START


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
                String studentId = params[1];
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

                ///// 20170505 MODIFY START
                TextView textView = (TextView) findViewById(R.id.textViewToDoTitle);
                if (todoContent != null && todoContent.get("title") != null) {
                    textView.setText((String) todoContent.get("title"));
                } else {
                    textView.setText("");
                }
                ///// 20170505 MODIFY END

                ///// 20170506 ADD START
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
                            && ((boolean) todoContent.get("has_report_action"))) {

                        // 表示
                        mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
                        mImageViewFooterShoot.setVisibility(View.VISIBLE);
                        mTextViewFooterShoot.setVisibility(View.VISIBLE);
                    }

                    // check has_my_report_play_action
                    if ((todoContent.get("has_my_report_play_action") != null)
                            && ((boolean) todoContent.get("has_my_report_play_action"))) {

                        // 表示
                        mLinearLayoutFooterStatus.setVisibility(View.VISIBLE);
                        mImageViewFooterCompare.setVisibility(View.VISIBLE);
                        mTextViewFooterCompare.setVisibility(View.VISIBLE);
                    }
                }
                ///// 20170506 ADD END
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

    ///// 20170509 ADD START
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
            mContentype = params[2];
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
    ///// 20170509 ADD END


}
