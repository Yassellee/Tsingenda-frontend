package com.example.calendarfrontend;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "Screenshot";
    private ImageView shot;
    private CustomFileObserver mFileObserver;

    private static final String PATH = Environment.getExternalStorageDirectory() + File.separator
            + Environment.DIRECTORY_PICTURES + File.separator + "Screenshots" + File.separator;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shot = (ImageView)findViewById(R.id.screenshot);
        mFileObserver = new CustomFileObserver(PATH);
        verifyStoragePermissions(MainActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFileObserver.startWatching();
//        Log.d(TAG, PATH);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFileObserver.stopWatching();
    }

    /**
     * 目录监听器
     */
    private class CustomFileObserver extends FileObserver {

        private String mPath;

        public CustomFileObserver(String path) {
            super(path);
            this.mPath = path;
        }

        public CustomFileObserver(String path, int mask) {
            super(path, mask);
            this.mPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            if (path != null){
                UpLoadPicture(PATH + path);
            }
        }
    }
    private void UpLoadPicture(String path) {
        //判断文件夹中是否有文件存在
        File file = new File(path);
        if (!file.exists()){
            System.out.println("nonono");
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "图片不存在", Toast.LENGTH_SHORT).show());
        }
        else {
            System.out.println(path);
            runOnUiThread(() -> shot.setImageURI(Uri.fromFile(file)));
        }
//        OkHttpClient client = new OkHttpClient();
//        String imgPath = path;
//        MultipartBody.Builder builder = new MultipartBody.Builder();
//        builder.addFormDataPart("file",imgPath, RequestBody.create(MediaType.parse("png"),new File(imgPath)));
//        RequestBody requestBody = builder.build();
//        Request.Builder reqBuilder = new Request.Builder();
//        Request request = reqBuilder
//                .url(R.string.url+"/tsingenda/login/")
//                .post(requestBody)
//                .build();
//        Call call = client.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //  aBuilder.dismiss();
//                        Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                final String resp = response.body().toString();
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //  aBuilder.dismiss();
//                        Toast.makeText(getApplicationContext(),resp,Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        });
    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }
}