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

import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
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
    Map<String, String> photomap = new HashMap<String, String>();

    private static final String PATH = Environment.getExternalStorageDirectory() + File.separator
            + Environment.DIRECTORY_PICTURES + File.separator + "Screenshots" + File.separator;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashReport.initCrashReport(getApplicationContext(), "32de36c238", true);
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
                try {
                    UpLoadPicture(PATH + path);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private void UpLoadPicture(String path) throws JSONException {
        //判断文件夹中是否有文件存在
        File file = new File(path);
        if (!file.exists()){
            runOnUiThread(() -> Toast.makeText(MainActivity.this, "图片不存在", Toast.LENGTH_SHORT).show());
        }
        else {
            runOnUiThread(() -> shot.setImageURI(Uri.fromFile(file)));
            if(photomap.containsKey(path)){
                System.out.println("return directly");
                return;
            }
            else {
                photomap.put(path,"1");
            }
        }
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONObject photo = new JSONObject();
        try {
            photo.put("data",imageToBase64Str(path));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        OkHttpClient client = new OkHttpClient();
        FormBody.Builder builder = new FormBody.Builder();
        Request.Builder reqBuilder = new Request.Builder();
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(photo));
        Request request = reqBuilder
                .url(getString(R.string.url)+"/tsingenda/image/")
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //  aBuilder.dismiss();
                        Toast.makeText(getApplicationContext(),"上传失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.toString());
                //TODO:add response to user
            }
        });
    }

    public static String imageToBase64Str(String imgFile) {
        InputStream inputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream(imgFile);
            data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 加密
        Base64.Encoder encoder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encoder = Base64.getEncoder();
            return encoder.encodeToString(data);
        }
        return imgFile;
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