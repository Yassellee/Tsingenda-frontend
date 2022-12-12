package com.example.calendarfrontend;

import static java.lang.Math.min;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.lxj.xpopup.util.XPermission;
import com.lxj.xpopupext.listener.TimePickerListener;
import com.lxj.xpopupext.popup.TimePickerPopup;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

public class MainActivity extends AppCompatActivity implements
        com.haibin.calendarview.CalendarView.OnCalendarSelectListener,
        CalendarView.OnYearChangeListener,
        View.OnClickListener {

    TextView mCurrentMonth, mTextCurrentDay;
    ImageView mSearch, mSetting;
    ListView mlistView;
    //    CalendarLayout mCalendarLayout;
    FloatingActionButton mAdd, mCurrent;
    FrameLayout mFrameLayout;
    public static List<Scheme> schemeList = new ArrayList<>();
    public static SchemeAdapter adapter;
    public static SQLiteDatabase schemeDB;
    public static CalendarView mCalendarView;

    private final String TAG = "Screenshot";
//    private ImageView shot;
    private CustomFileObserver mFileObserver;
    Map<String, String> photomap = new HashMap<String, String>();

    private static final String PATH = Environment.getExternalStorageDirectory() + File.separator
            + Environment.DIRECTORY_PICTURES + File.separator + "Screenshots" + File.separator;

//    private  static  final String PATH = "storage/Pictures/Screenshots/";
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashReport.initCrashReport(getApplicationContext(), "32de36c238", true);
        setContentView(R.layout.activity_main);
//        shot = (ImageView)findViewById(R.id.screenshot);
        mFileObserver = new CustomFileObserver(PATH);
        verifyStoragePermissions(MainActivity.this);
        if(isExternalStorageReadable() && isExternalStorageWritable()){
            Toast.makeText(MainActivity.this, "yes you can read and write", Toast.LENGTH_SHORT).show();
        }
        else if(isExternalStorageReadable()){
            Toast.makeText(MainActivity.this, "yes you can read", Toast.LENGTH_SHORT).show();
        }
        else if(isExternalStorageWritable()){
            Toast.makeText(MainActivity.this, "yes you can write", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this, "you can't do anything", Toast.LENGTH_SHORT).show();
        }
        mCurrentMonth = findViewById(R.id.currentMonth);
        mSearch = findViewById(R.id.search);
        mSetting = findViewById(R.id.setting);
        mCalendarView = findViewById(R.id.calendarView);
        mAdd = findViewById(R.id.fab_add);
        mCurrent = findViewById(R.id.fab_current);
        mFrameLayout = findViewById(R.id.currentDay);
        mTextCurrentDay = findViewById(R.id.text_current);
        mlistView = findViewById(R.id.SchemeList);
        mCalendarView.setOnCalendarSelectListener(this);
        mCalendarView.setOnYearChangeListener(this);
        mCurrentMonth.setText(String.format("%d年%d月", mCalendarView.getCurYear(), mCalendarView.getCurMonth()));
        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));

        schemeDB = this.openOrCreateDatabase("SchemeDB", Context.MODE_PRIVATE, null);
        DbHandler.createTable(schemeDB, "schemes");

        Toast.makeText(MainActivity.this, PATH, Toast.LENGTH_SHORT).show();

        schemeList = DbHandler.fetchScheme(schemeDB, "schemes", mCalendarView.getCurYear(), mCalendarView.getCurMonth(), mCalendarView.getCurDay());
        adapter = new SchemeAdapter(this, R.layout.item, schemeList);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener((parent, view, position, id) -> {
            new XPopup.Builder(this)
                    .isDarkTheme(true)
                    .asCustom(new ModifySchemePopup(this, position))
                    .show();
        });
        mlistView.setOnItemLongClickListener((parent, view, position, id) -> {
            new XPopup.Builder(this)
                    .isDarkTheme(true)
                    .maxWidth(800)
                    .asConfirm("确定删除该日程吗？", "", () -> {
                        // Todo: 发送给服务器
                        Scheme mScheme = schemeList.get(position);
                        if(mScheme.getId() != -1) {
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .retryOnConnectionFailure(true)
                                    .cookieJar(CookieJarManager.cookieJar)//自动管理Cookie
                                    .build();
                            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                            JSONObject jo = new JSONObject();
                            try {
                                jo.put("id", mScheme.getId());
                                jo.put("text", mScheme.getTitle());
                                jo.put("is_agenda", false);
                                jo.put("confidence_high", false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            JSONArray ja = new JSONArray();
                            ja.put(jo);
                            RequestBody body = RequestBody.create(JSON, ja.toString());
                            Request request = new Request.Builder()
                                    .url(R.string.neturl+"/tsingenda/feedback/")
                                    .post(body)
                                    .build();
                            Call call = client.newCall(request);
                            call.enqueue(new Callback()
                            {
                                @Override
                                public void onFailure(Call call, IOException e)
                                {
                                    e.printStackTrace();
                                }
                                @Override
                                public void onResponse(Call call, Response response) throws IOException
                                {
                                    //此方法运行在子线程中，不能在此方法中进行UI操作。
                                    if(!response.isSuccessful())
                                    {
                                        throw new IOException("Unexpected code " + response);
                                    }
                                }
                            });
                        }
                        DbHandler.deleteScheme(schemeDB, "schemes", schemeList.get(position));
                        schemeList.remove(position);
                        adapter.notifyDataSetChanged();
                    })
                    .show();
            return true;
        });

        TimePickerPopup popup = new TimePickerPopup(MainActivity.this)
                .setTimePickerListener(new TimePickerListener() {
                    @Override
                    public void onTimeChanged(Date date) {
                    }
                    @Override
                    public void onTimeConfirm(Date date, View view) {
                        mCalendarView.scrollToCalendar(date.getYear()+1900, date.getMonth()+1, date.getDate());
                    }
                    @Override
                    public void onCancel() {

                    }
                });


        mCurrentMonth.setOnClickListener((v) -> {
//            if (!mCalendarLayout.isExpand()) {
//                mCalendarLayout.expand();
//                return;
//            }
            if(mCalendarView.isYearSelectLayoutVisible()) {
                mCalendarView.closeYearSelectLayout();
                mCurrentMonth.setText(String.format("%d年%d月", mCalendarView.getCurYear(), mCalendarView.getCurMonth()));
            } else {
                mCalendarView.showYearSelectLayout(mCalendarView.getCurYear());
                mCurrentMonth.setText(String.valueOf(mCalendarView.getCurYear()));
            }
        });
        mCurrent.setOnClickListener((v) -> {
            mCalendarView.scrollToCurrent();
        });
        mAdd.setOnClickListener((v) -> {
            new XPopup.Builder(this)
                    .isDarkTheme(true)
                    .asCustom(new AddSchemePopup(this))
                    .show();
        });
        mSearch.setOnClickListener((v) -> {
            new XPopup.Builder(this)
                    .isDarkTheme(true)
                    .maxWidth(800)
                    .asInputConfirm("请输入日程标题", "",
                            new OnInputConfirmListener() {
                                @Override
                                public void onConfirm(String text) {
                                    Scheme tmp = DbHandler.queryScheme(schemeDB, "schemes", text);
                                    if(tmp.getTitle() != null) {
                                        mCalendarView.scrollToCalendar(tmp.getYear(), tmp.getMonth(), tmp.getDay());
                                        Toast.makeText(MainActivity.this, "已跳转到日程所在日期", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "未找到该日程", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
        });
        mSetting.setOnClickListener((v) -> {
            new XPopup.Builder(this)
                    .isDarkTheme(true)
                    .atView(mSetting)
                    .asAttachList(new String[]{"跳转到指定日期", "同步到系统日历", "退出当前账号"}, null, (position, text) -> {
                        switch(position){
                            case 0:
                                new XPopup.Builder(MainActivity.this)
                                        .isDarkTheme(true)
                                        .asCustom(popup)
                                        .show();
                                break;
                            case 1:
                                break;
                            case 2:
                                Intent MaintoLogin=new Intent(MainActivity.this,LoginActivity.class);
                                startActivity(MaintoLogin);
                                break;
                        }
                    })
                    .show();
        });
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
//            runOnUiThread(() -> shot.setImageURI(Uri.fromFile(file)));
            if(photomap.containsKey(path)){
                System.out.println("return directly");
                return;
            }
            else {
                photomap.put(path,"1");
            }
        }
        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
        JSONArray ja = new JSONArray();
        System.out.println(imageToBase64Str(path));
        ja.put(imageToBase64Str(path));
        JSONObject photo = new JSONObject();
        try {
            photo.put("data",ja);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(ja);
        OkHttpClient client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .cookieJar(CookieJarManager.cookieJar)//自动管理Cookie
                .build();
        Request.Builder reqBuilder = new Request.Builder();
        RequestBody requestBody = RequestBody.create(JSON, String.valueOf(photo));
        Request request = reqBuilder
                .url(getString(R.string.neturl)+"/tsingenda/image/")
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
                if(response.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT).show();
                    try {
                        JSONObject jo = new JSONObject(response.body().string());
                        JSONArray ja = jo.getJSONArray("data");
                        JSONArray ja1 = ja.getJSONArray(0);
                        JSONObject jo1 = ja1.getJSONObject(0);
                        String raw_text = jo1.getString("raw_text");
                        Scheme scheme = new Scheme();
                        scheme.setId(jo1.getInt("id"));
                        scheme.setConf_id(jo1.getInt("conf_id"));
                        scheme.setTitle(jo1.getString("title"));
                        JSONArray ja2 = jo1.getJSONArray("dates");
                        String date = ja2.getString(1);
                        String[] date1 = date.split("-");
                        scheme.setYear(Integer.parseInt(date1[0]));
                        scheme.setMonth(Integer.parseInt(date1[1]));
                        scheme.setDay(Integer.parseInt(date1[2]));
                        JSONArray ja3 = jo1.getJSONArray("times");
                        String time = ja3.getString(1);
                        String[] time1 = time.split(":");
                        scheme.setStartTime(time1[0] + ":" + time1[1]);
                        scheme.setEndTime(String.valueOf(Integer.parseInt(time1[0]) + 1) + ":" + time1[1]);
                        scheme.setLocation(jo1.getString("location"));
                        boolean is_agenda = jo1.getBoolean("is_agenda");
                        boolean confidence_high = jo1.getBoolean("confidence_high");
                        if(confidence_high)
                        {
                            Toast.makeText(getApplicationContext(),"插入日程",Toast.LENGTH_SHORT).show();
                            DbHandler.insertScheme(schemeDB, "schemes", scheme);
                        } else if(is_agenda){
                            Toast.makeText(getApplicationContext(),"展示弹窗",Toast.LENGTH_SHORT).show();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                XPopup.requestOverlayPermission(MainActivity.this, new XPermission.SimpleCallback() {
                                    @Override
                                    public void onGranted() {
                                       new XPopup.Builder(MainActivity.this)
                                                .isDarkTheme(true)
                                                .enableShowWhenAppBackground(true)
                                                .asCustom(new PopupWindow(MainActivity.this, scheme, raw_text))
                                                .show();
                                    }
                                    @Override
                                    public void onDenied() {
                                        Toast.makeText(MainActivity.this, "没有权限无法显示弹窗", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
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
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE};

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read permission
        int readpermission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int managepermission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        int writepermission = ActivityCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (writepermission != PackageManager.PERMISSION_GRANTED || readpermission != PackageManager.PERMISSION_GRANTED || managepermission!=PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
            System.out.println("you don't have all the permissions");
        }
        else {
            System.out.println("you have all the permissions");
        }
    }

    // Checks if a volume containing external storage is available
// for read and write.
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // Checks if a volume containing external storage is available to at least read.
    private boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    @Override
    public void onCalendarOutOfRange(Calendar calendar) {

    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onCalendarSelect(Calendar calendar, boolean isClick) {
        mCurrentMonth.setText(String.format("%d年%d月", calendar.getYear(), calendar.getMonth()));
        if(calendar.isCurrentDay()) {
            mFrameLayout.setVisibility(View.GONE);
        } else {
            mFrameLayout.setVisibility(View.VISIBLE);
        }
        schemeList.clear();
        schemeList.addAll(DbHandler.fetchScheme(schemeDB, "schemes", calendar.getYear(), calendar.getMonth(), calendar.getDay()));
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onYearChange(int year) {
        mCurrentMonth.setText(String.valueOf(year));
    }

    @Override
    public void onClick(View v) {

    }
}