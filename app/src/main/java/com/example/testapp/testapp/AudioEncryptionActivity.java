package com.example.testapp.testapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapp.aes.AESUtils;

import static android.support.v4.content.FileProvider.getUriForFile;

/**
 * Created by 93367 on 2017/5/22.
 */

public class AudioEncryptionActivity extends Activity implements
        OnClickListener{
    private static final String TAG = "AudioEncryptionActivity";
    private MediaPlayer mPlayer;
    private Button mPlayButton;
    private Button mEncryptionButton;
    private Button mDecryptionButton;
    private TextView mFileName = null;
    private File sdCard = Environment.getExternalStorageDirectory();
    private File oldFile = new File(sdCard, "test.mp3");
    // 音频文件的路径，在res\raw\recording_old.3gpp中找到音频文件，再放到外部存储的根目录下。用于测试
    private FileInputStream fis = null;
    private FileOutputStream fos = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.audio_encrypt_activity);
        mPlayButton = (Button) findViewById(R.id.playButton);
        mPlayButton.setOnClickListener(this);
        mEncryptionButton = (Button) findViewById(R.id.encryptionButton);
        mEncryptionButton.setOnClickListener(this);
        mDecryptionButton = (Button) findViewById(R.id.decryptionButton);
        mDecryptionButton.setOnClickListener(this);
        mFileName = (TextView) findViewById( R.id.filename );
        //Uri contentUri = getUriForFile(this, "com.example.testapp.testapp.fileprovider", oldFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            ;//finish();
        } else if (requestCode == 6) {
            //mFilePath = Uri.decode( data.getDataString() );
            Uri uri = data.getData();
            String mFilePath = getPath(this, uri);
            oldFile = new File(mFilePath);
            mFileName.setText(URLUtil.guessFileName(mFilePath, null, null));
        }
    }

    private void openAssignFolder(File file){
        if(null==file || !file.exists()){
            return;
        }
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("file/*");
        try {
            startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"),
                    6);
            //startActivity(Intent.createChooser(intent,"选择浏览工具"));
            //startActivityForResult(intent, 6);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chooseFile:
                openAssignFolder(sdCard);
                break;
            case R.id.playButton:
                if (mPlayer != null) {
                    mPlayer.release();
                    mPlayer = null;
                }
                // mPlayer = MediaPlayer.create(this, R.raw.recording_old);
                boolean isSuccess = true;
                try {
                    fis = new FileInputStream(oldFile);
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(fis.getFD());
                    mPlayer.prepare(); // 去掉会出错
                    mPlayer.start();
                } catch (FileNotFoundException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (!isSuccess)
                    Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
                break;

            case R.id.encryptionButton:
                // 加密保存
                isSuccess = true;
                try {
                    fis = new FileInputStream(oldFile);
                    byte[] oldByte = new byte[(int) oldFile.length()];
                    fis.read(oldByte); // 读取
                    byte[] newByte = AESUtils.encryptVoice(oldByte);
                    // 加密
                    fos = new FileOutputStream(oldFile);
                    fos.write(newByte);

                } catch (FileNotFoundException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (Exception e) {
                    isSuccess = false;
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (isSuccess)
                    Toast.makeText(this, "加密成功", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "加密失败", Toast.LENGTH_SHORT).show();

                Log.i(TAG, "保存成功");
                break;

            case R.id.decryptionButton:
                // 解密保存
                isSuccess = true;
                byte[] oldByte = new byte[(int) oldFile.length()];
                try {
                    fis = new FileInputStream(oldFile);
                    fis.read(oldByte);
                    byte[] newByte = AESUtils.decryptVoice(oldByte);
                    // 解密
                    fos = new FileOutputStream(oldFile);
                    fos.write(newByte);

                } catch (FileNotFoundException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (Exception e) {
                    isSuccess = false;
                    e.printStackTrace();
                }
                try {
                    fis.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isSuccess)
                    Toast.makeText(this, "解密成功", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "解密失败", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }

    }
}
