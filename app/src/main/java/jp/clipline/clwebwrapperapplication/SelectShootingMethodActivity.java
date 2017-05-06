package jp.clipline.clwebwrapperapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import jp.clipline.clwebwrapperapplication.Utility.AndroidUtility;
import jp.clipline.clwebwrapperapplication.Utility.CameraUtil;
import jp.clipline.clwebwrapperapplication.api.ToDo;

public class SelectShootingMethodActivity extends AppCompatActivity {

    private static int REQUEST_CODE_PICTURE_CAPTURE = 1;
    private static int REQUEST_CODE_VIDEO_CAPTURE = 2;
    private static int REQUEST_CODE_PICTURE_SELECTOR = 3;
    private static int REQUEST_CODE_VIDEO_SELECTOR = 4;
    private static int REQUEST_CODE_SELECT_FILE = 5; ///// 20170506 ADD

    private Uri uriPicture = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_shooting_method);

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
            Intent intent = new Intent()
                    .setType("image/png")
                    .setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(intent, "Select a file"), REQUEST_CODE_SELECT_FILE);
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
/*
                String url = "%s://%s/training/#/students/" + studentId
                        + "/todos?type=caetgory&category_id=" + categoryId;
*/
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

        Map<String, String> todoParameters = ((ClWebWrapperApplication) getApplication()).getTodoParameters();
        String studentId = todoParameters.get("studentId");
        String categoryId = todoParameters.get("categoryId");
        String todoContentId = todoParameters.get("todoContentId");
        new GetTodoInformationTask().execute(AndroidUtility.getCookie(getApplicationContext()), studentId, categoryId, todoContentId);
        Log.d("", "");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICTURE_CAPTURE) {
                Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                ((ClWebWrapperApplication) this.getApplication()).setTodoContent(uriPicture, "image/png");
                startActivity(intent);
                finish();
                return;
            }

            if (requestCode == REQUEST_CODE_VIDEO_CAPTURE) {
                Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                ((ClWebWrapperApplication) this.getApplication()).setTodoContent(data.getData(), "video/mp4");
                startActivity(intent);
                finish();
                return;
            }

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

            ///// 20170506 ADD START
            if (requestCode == REQUEST_CODE_SELECT_FILE) {
                Uri selectedMediaUri = data.getData();
                String contentType = "image/png";
                if (selectedMediaUri.toString().contains("images")) {
                    contentType = "image/png";
                } else  if (selectedMediaUri.toString().contains("video")) {
                    contentType = "video/mp4";
                } else {

                }

                Intent intent = new Intent(getApplicationContext(), SubmissionConfirmationActivity.class);
                ((ClWebWrapperApplication) this.getApplication()).setTodoContent(data.getData(), contentType);
                startActivity(intent);
                finish();
                return;
            }
            ///// 20170506 ADD END


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


            }
        }

        @Override
        protected void onCancelled() {
        }
    }

}
