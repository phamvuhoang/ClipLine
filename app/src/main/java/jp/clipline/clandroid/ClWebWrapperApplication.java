package jp.clipline.clandroid;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jp.clipline.clandroid.Utility.AndroidUtility;
import jp.clipline.clandroid.api.Branch;
import okhttp3.Response;

// ((ClWebWrapperApplication)this.getApplication())
public class ClWebWrapperApplication extends Application {

    // ------------------------------------------------------------------------------------------ //
    private Map<String, Object> currentTodoContent = null;

    private boolean mIsBack = false;

    public void setCurrentTodoContent(Map<String, Object> _currentTodoContent) {
        this.currentTodoContent = _currentTodoContent;
    }

    public Map<String, Object> getCurrentTodoContent() {
        return this.currentTodoContent;
    }

    // ------------------------------------------------------------------------------------------ //
    private Map<String, String> todoParameters = new HashMap<String, String>();

    public void setTodoParameters(String id, String categoryId, String todoContentId, String type, Boolean isStudent) {
        todoParameters.clear();
        todoParameters.put("id", id);
        todoParameters.put("categoryId", categoryId);
        todoParameters.put("todoContentId", todoContentId);
        todoParameters.put("type", type);
        todoParameters.put("loginType", isStudent ? "student" : "coach");
    }

    public Map<String, String> getTodoParameters() {
        return todoParameters;
    }

    // ------------------------------------------------------------------------------------------ //
    private String todoContentDataUri = null;
    private String todoContentDataType = null;

    public void setTodoContent(String data, String type) {
        todoContentDataUri = data;
        todoContentDataType = type;
    }

    public String getTodoContentData() {
        return todoContentDataUri;
    }

    public String getTodoContentType() {
        return todoContentDataType;
    }

    public boolean isBack() {
        return mIsBack;
    }

    public void setBack(boolean isBack) {
        mIsBack = isBack;
    }

    // ------------------------------------------------------------------------------------------ //
    public void onCreate ()
    {
        super.onCreate();

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (thread, e);
            }
        });
    }

    public void handleUncaughtException (Thread thread, Throwable e)
    {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        String cookie = AndroidUtility.getCookie(getApplicationContext());
        BranchLogoutTask logoutTask = new BranchLogoutTask(cookie);
        logoutTask.execute((Void) null);

        //System.exit(1); // kill off the crashed app
    }


    /**
     * Represents an asynchronous logout task
     */
    public class BranchLogoutTask extends AsyncTask<Void, Void, Boolean> {

        private Response mResponse;
        private String mCookie;

        public BranchLogoutTask(String cookie) {
            mCookie = cookie;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mResponse = (Response) Branch.signOut(mCookie);
                return Boolean.TRUE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            String message = null;
            if (success) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            } else {
                // TODO what happened?
            }
        }

        @Override
        protected void onCancelled() {
        }
    }


}
