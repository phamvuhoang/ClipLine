package jp.clipline.clwebwrapperapplication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.Map;

import jp.clipline.clwebwrapperapplication.Utility.AndroidUtility;
import jp.clipline.clwebwrapperapplication.api.Agreement;
import jp.clipline.clwebwrapperapplication.api.Branch;

public class TerminalFirstUseScreenDisplayedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal_first_use_screen_displayed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        new GetAgreementTask().execute((Void) null);
    }

    public class GetAgreementTask extends AsyncTask<Void, Void, Boolean> {

        String message = null ;

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
                if(message==null) {
/*
                    Intent intent = new Intent(getApplicationContext(), LaunchCrossWalkActivity.class);
                    startActivity(intent);
                    finish();
*/

                    ///// 20170505 TEMPORARY ADD START Bypass LaunchCrossWalkActivity
                    ((ClWebWrapperApplication)getApplication()).setTodoParameters("92817", "988", "15796");

                    Intent intent = new Intent(getApplicationContext(), SelectShootingMethodActivity.class);
                    startActivity(intent);
                    finish();
                    ///// 20170505 TEMPORARY ADD END
                }
                else {
                    TextView textView = (TextView) findViewById(R.id.textView);
                    textView.setText(message);
                }
            }
            else {
            }
        }

        @Override
        protected void onCancelled() {
        }
    }

}
