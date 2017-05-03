package jp.clipline.clwebwrapperapplication;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.clipline.clwebwrapperapplication.Utility.CameraUtil;

/**
 * 非同期的に写真を保存するためのRunnableクラス
 */
class PictureSaver implements Runnable {

    private static final int PNG_COMPRESS_QUALITY = 100;
    private byte[] mBytesData;
    private String mFilename;
    private Context mContext;

    public PictureSaver(byte[] bytesData, String filename, Context context) {
        mFilename = filename;
        mBytesData = bytesData;
        mContext = context;
    }

    @Override
    public void run() {
        // 出力先ファイル
        File file = new File(mFilename);
        FileOutputStream fileOutputStream = null;
        try {
            // ファイルに保存
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(mBytesData);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        } finally {
            if (null != fileOutputStream) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        CameraUtil.registPictureAndroidDB(mFilename, mContext);
    }
}
