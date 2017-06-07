package jp.clipline.clandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.api.Branch;
import okhttp3.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity_bak extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private BranchLoginTask mAuthTask = null;

    // UI references.
//    private AutoCompleteTextView mServiceIdView;
//    private AutoCompleteTextView mBranchIdView;
    private EditText mServiceId;
    private EditText mBranchId;
    private EditText mPasswordView;
    private Button mLogin;
    private ProgressBar mProgressView;
    private View mLoginFormView;

    private String mCookie = null;
    private Response mResponse = null;
    private TextView mAndroidId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_login);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = (ProgressBar) findViewById(R.id.login_progress);
        mProgressView.getIndeterminateDrawable()
                .setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        mServiceId = (EditText) findViewById(R.id.editTextServiceID);
        mBranchId = (EditText) findViewById(R.id.editTextBranchID);
        mPasswordView = (EditText) findViewById(R.id.editTextPassword);
        mLogin = (Button) findViewById(R.id.buttonLogin);
        mLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Map<String, String> loginSetting = AndroidUtility.getLoginSetting(getApplicationContext());
        if (loginSetting != null) {
            mServiceId.setText(loginSetting.get("serviceId"));
            mBranchId.setText(loginSetting.get("branchId"));
            mPasswordView.setText(loginSetting.get("password"));
        }
        mAndroidId = (TextView) findViewById(R.id.androidId);
        mAndroidId.setText(String.format(getString(R.string.android_id_format), AndroidUtility.getAndroidId(getContentResolver())));
        mAndroidId.setText(String.format("[%s] %s", BuildConfig.ANDROID_ENV, mAndroidId.getText()));
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Store values at the time of the login attempt.
        String serviceId = mServiceId.getText().toString();
        String branchId = mBranchId.getText().toString();
        String password = mPasswordView.getText().toString();

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(serviceId)) {
            mServiceId.setError(getString(R.string.error_field_required));
            mServiceId.requestFocus();
            return;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(branchId)) {
            mBranchId.setError(getString(R.string.error_field_required));
            mBranchId.requestFocus();
            return;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) && isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
            return;
        }

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        mAuthTask = new BranchLoginTask(branchId, serviceId, password);
        AndroidUtility.setLoginSetting(getApplicationContext(), branchId, serviceId, password);
        mAuthTask.execute((Void) null);
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() < 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity_bak.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

//        mBranchIdView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class BranchLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mBranchId;
        private final String mServiceId;
        private final String mPassword;

        BranchLoginTask(String branchId, String serviceId, String password) {
            mBranchId = branchId;
            mServiceId = serviceId;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //try {
                //mResponse = (Response) Branch.signInV2(mBranchId, mServiceId, mPassword, AndroidUtility.getAndroidId(getContentResolver()));
                return Boolean.TRUE;
            //} catch (IOException e) {
            //    return Boolean.FALSE;
            //}
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            String message = null;
            int code = 0;
            if (success) {
                code = mResponse.code();
                try {
                    Gson gson = new Gson();
                    String body = mResponse.body().string();
                    HashMap<String, Object> fields = gson.fromJson(body, HashMap.class);
                    message = (String) fields.get("message");

                    if ((null == message) || ("".equals(message))) {
                        mCookie = mResponse.headers().get("Set-Cookie");
                        AndroidUtility.setCookie(getApplicationContext(), mCookie);

                        Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                        intent.putExtra("FROM_SCREEN_LOGIN", "from_screen_login");
                        startActivity(intent);
                        finish();
                    }

                    showError(code);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                code = 403;
                showError(code);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
        }
    }

    private void showError(int code) {
        String title = "";
        String message = "";
        if (code == 200) {
            title = getResources().getString(R.string.login_title_error_200);;
            message = getResources().getString(R.string.login_message_error_200)
                + "\n" + mAndroidId.getText().toString();
        }
        if (code == 401) {
            title = getResources().getString(R.string.login_title_error_401);
            message = getResources().getString(R.string.login_message_error_401);
        }
        if (code == 403) {
            title = getResources().getString(R.string.login_title_error_403);
            message = getResources().getString(R.string.login_message_error_403);
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(getResources().getText(R.string.close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                showProgress(false);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public class GetAgreementTask extends AsyncTask<Void, Void, Boolean> {

        String message = null;

        GetAgreementTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Map<String, String> loginSetting = AndroidUtility.getLoginSetting(getApplicationContext());
                message = Branch.signInWithIdfv(loginSetting.get("branchId"), loginSetting.get("serviceId"), loginSetting.get("password"), AndroidUtility.getAndroidId(getContentResolver()));
                return Boolean.TRUE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                if (message == null) {
                    Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                    intent.putExtra("FROM_SCREEN_LOGIN", "from_screen_login");
                    startActivity(intent);
                    finish();
                } else {
//                    TextView textView = (TextView) findViewById(R.id.textView);
//                    textView.setText(message);
                }
            } else {
            }
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}

