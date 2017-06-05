package jp.clipline.clandroid.Utility;

import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;

import java.util.Set;

public class IntentParameters {

    private boolean mIsCallFromIntent = false;
    private boolean mIsBackShooting = true;
    private boolean mIsAspectWide;
    private boolean mIsVideo = true;
    private boolean mIsBrowserCalling = false;
    private boolean mIsProfile = false;
    private String mGetAction;
    private String mExtraOutput = null;
    // FIXME : プロフィール画像撮影モード、

    public IntentParameters(Intent intent, boolean isStandardAspectHardWare) {
        mGetAction = intent.getAction();
        Log.d("SimpleCamera", "@@@ getAction = " + mGetAction + " @@@");


        if (intent.getAction().equals(MediaStore.ACTION_IMAGE_CAPTURE)) {
            mIsBrowserCalling = true;
            mIsVideo = false;
            mExtraOutput = (String) intent.getExtras().get(MediaStore.EXTRA_OUTPUT);
            return;
        }

        if (intent.getAction().equals(MediaStore.ACTION_VIDEO_CAPTURE)) {
            mIsBrowserCalling = true;
            mIsVideo = true;
            // この場合は EXTRA_OUTPUT は設定されない
            // mExtraOutput = (Uri)intent.getExtras().get(MediaStore.EXTRA_OUTPUT);
            return;
        }

        if ((intent.getData() != null) && (intent.getData().getScheme() != null)) {
            Log.d("SimpleCamera", "@@@ カスタムURLスキーム起動 @@@");
            mIsBrowserCalling = true;
            // カスタムURLスキーム起動

            // http://blog.tappli.com/article/40839753.html
            Set<String> parameterNames = intent.getData().getQueryParameterNames();
            for (String name : parameterNames) {
                String value = intent.getData().getQueryParameter(name);
                Log.d("SimpleCamera", "@@@ " + name + " => " + value);
                if (name.equals("IsVideo")) {
                    mIsVideo = Boolean.valueOf(value.toUpperCase());
                }
                if (name.equals("IsBackShooting")) {
                    mIsBackShooting = Boolean.valueOf(value.toUpperCase());
                }
                if (name.equals("IsProfile")) {
                    mIsProfile = Boolean.valueOf(value.toUpperCase());
                }
            }

            return;
        }

        if (intent.getBooleanExtra("CallFromIntent", false)) {
            // 自己インテント呼び出し起動
            mIsCallFromIntent = true;
            mIsBackShooting = intent.getBooleanExtra("IsBackShooting", true);
            mIsAspectWide = intent.getBooleanExtra("IsAspectWide", true);
            mIsVideo = intent.getBooleanExtra("IsVideo", true);
            mIsBrowserCalling = intent.getBooleanExtra("mIsBrowserCalling",false);
            mExtraOutput = intent.getStringExtra("mExtraOutput");
            return;
        }

        Log.d("SimpleCamera", "@@@ 通常起動 @@@");
        // 通常の起動
        mIsCallFromIntent = false;
        mIsBackShooting = true;
        mIsAspectWide = !isStandardAspectHardWare;
        mIsVideo = true;
    }

    public boolean isCallFromIntent() {
        return mIsCallFromIntent;
    }

    public boolean isBrowserCalling() {
        return mIsBrowserCalling;
    }

    public boolean isBackShooting() {
        return mIsBackShooting;
    }

    public boolean isAspectWide() {
        return mIsAspectWide;
    }

    public boolean isVideo() {
        return mIsVideo;
    }

    public String getExtraOutput() {
        return mExtraOutput;
    }

    public void setIsBackShooting(boolean isBackShooting) {
        mIsBackShooting = isBackShooting;
    }

    public void setIsAspectWide(boolean isAspectWide) {
        mIsAspectWide = isAspectWide;
    }

    public void setIsVideo(boolean isVideo) {
        mIsVideo = isVideo;
    }

    public void apply(Intent intent) {
        intent.putExtra("CallFromIntent", true);
        intent.putExtra("IsAspectWide", isAspectWide());
        intent.putExtra("IsBackShooting", isBackShooting());
        intent.putExtra("IsVideo", isVideo());
        intent.putExtra("mIsBrowserCalling", isBrowserCalling());
        intent.putExtra("mExtraOutput", getExtraOutput());
    }

    public void printInfomation() {
        Log.d("SimpleCamera", "mIsCallFromIntent = " + mIsCallFromIntent);
        Log.d("SimpleCamera", "mIsBackShooting = " + mIsBackShooting);
        Log.d("SimpleCamera", "mIsAspectWide = " + mIsAspectWide);
        Log.d("SimpleCamera", "mIsVideo = " + mIsVideo);
    }
}
