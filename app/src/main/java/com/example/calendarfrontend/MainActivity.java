package com.example.calendarfrontend;

import static java.lang.Math.min;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.view.MotionEvent;
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
import com.lxj.xpopup.interfaces.OnConfirmListener;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.lxj.xpopup.util.XPermission;
import com.lxj.xpopupext.listener.TimePickerListener;
import com.lxj.xpopupext.popup.TimePickerPopup;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.lzf.easyfloat.permission.PermissionUtils;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;
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
    private CalendarView mCalendarView;
    public static int selectedYear;
    public static int selectedMonth;
    public static int selectedDay;
    private IntentFilter filter;
    private SmsReceiver receiver;

    private static final int REQUEST_CODE_FOR_OVERLAY_PERMISSION = 0x201;
    private final String TAG = "Screenshot";
//    private ImageView shot;
    private CustomFileObserver mFileObserver;
    Map<String, String> photomap = new HashMap<String, String>();

    private static final String PATH = Environment.getExternalStorageDirectory() + File.separator
            + Environment.DIRECTORY_PICTURES + File.separator + "Screenshots" + File.separator;

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getReadPermissions();
        EasyFloat.with(this)
        // 设置浮窗显示类型，默认只在当前Activity显示，可选一直显示、仅前台显示
        .setShowPattern(ShowPattern.ALL_TIME).setLayout(R.layout.floattab)
                // 设置浮窗是否可拖拽
                .setDragEnable(true)
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void createdResult(boolean isCreated, @Nullable String msg, @Nullable View view) { }

                    @Override
                    public void show(@NotNull View view) {
                    }

                    @Override
                    public void hide(@NotNull View view) { }

                    @Override
                    public void dismiss() { }

                    @Override
                    public void touchEvent(@NotNull View view, @NotNull MotionEvent event) { }

                    @Override
                    public void drag(@NotNull View view, @NotNull MotionEvent event) { }

                    @Override
                    public void dragEnd(@NotNull View view) { }
                })
                .show();
        CrashReport.initCrashReport(getApplicationContext(), "32de36c238", true);
        setContentView(R.layout.activity_main);
//        Intent it=new Intent(this, CheckPicService.class);
//        startService(it);
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
        selectedYear = mCalendarView.getCurYear();
        selectedMonth = mCalendarView.getCurMonth();
        selectedDay = mCalendarView.getCurDay();

        schemeDB = this.openOrCreateDatabase("SchemeDB", Context.MODE_PRIVATE, null);
        DbHandler.createTable(schemeDB, "schemes");

        Toast.makeText(MainActivity.this, PATH, Toast.LENGTH_SHORT).show();

        schemeList = DbHandler.fetchScheme(schemeDB, "schemes", selectedYear, selectedMonth, selectedDay);
        adapter = new SchemeAdapter(this, R.layout.item, schemeList);
        mlistView.setAdapter(adapter);
        mlistView.setOnItemClickListener((parent, view, position, id) -> {
            new XPopup.Builder(this)
                    .isDarkTheme(true)
                    .asCustom(new ModifySchemePopup(this, position))
                    .show();
        });
        mlistView.setOnItemLongClickListener((parent, view, position, id) -> {
            new XPopup.Builder(MainActivity.this)
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
                                jo.put("conf_id", mScheme.getConf_id());
                                jo.put("is_agenda", false);
                                jo.put("confidence_high", false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            JSONArray ja = new JSONArray();
                            ja.put(jo);
                            JSONObject feedback = new JSONObject();
                            try {
                                feedback.put("data",ja);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                            RequestBody body = RequestBody.create(JSON, feedback.toString());
                            Request request = new Request.Builder()
                                    .url(getString(R.string.neturl)+"/tsingenda/feedback/")
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
//            runOnUiThread(() -> Toast.makeText(MainActivity.this, "图片不存在", Toast.LENGTH_SHORT).show());
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
                    try {
                        JSONObject jo = new JSONObject(response.body().string());
                        JSONArray dataArray = jo.getJSONArray("data");
                        JSONObject data = dataArray.getJSONObject(0);

                        String raw_text = data.getString("raw_text");
                        Scheme scheme = new Scheme();
                        scheme.setId(data.getInt("id"));
                        scheme.setConf_id(data.getInt("conf_id"));
                        scheme.setTitle(data.getString("title"));

                        JSONArray dates = data.getJSONArray("dates");
                        if(dates.length() == 0){
                            scheme.setYear(2022);
                            scheme.setMonth(12);
                            scheme.setDay(13);
                        } else {
                            JSONArray date = dates.getJSONArray(0);
                            if(date.length() <= 1){
                                scheme.setYear(2022);
                                scheme.setMonth(12);
                                scheme.setDay(13);
                            } else {
                                String Nums = date.getString(1);
                                String[] Num = Nums.split("-");
                                scheme.setYear(Integer.parseInt(Num[0]));
                                scheme.setMonth(Integer.parseInt(Num[1]));
                                scheme.setDay(Integer.parseInt(Num[2]));
                            }
                        }
                        JSONArray times = data.getJSONArray("times");
                        if(times.length() == 0){
                            scheme.setStartTime("00:00");
                            scheme.setEndTime("23:59");
                        } else {
                            JSONArray time = times.getJSONArray(0);
                            if(time.length() == 0){
                                scheme.setStartTime("00:00");
                                scheme.setEndTime("23:59");
                            } else {
                                String Nums = time.getString(1);
                                String[] Num = Nums.split(":");
                                scheme.setStartTime(Nums);
                                if(Integer.parseInt(Num[0]) < 23){
                                    scheme.setEndTime(String.valueOf(Integer.parseInt(Num[0])+1) + ":" + Num[1]);
                                } else {
                                    scheme.setEndTime("23:59");
                                }
                            }
                        }
                        JSONArray locations = data.getJSONArray("locations");
                        if(locations.length() == 0){
                            scheme.setLocation("未知");
                        } else {
                            String location = locations.getString(0);
                            scheme.setLocation(location);
                        }
                        boolean is_agenda = data.getBoolean("is_agenda");
                        boolean confidence_high = data.getBoolean("confidence_high");
                        if(confidence_high)
                        {
                            DbHandler.insertScheme(schemeDB, "schemes", scheme);
                            if(scheme.getYear() ==selectedYear && scheme.getMonth() == selectedMonth && scheme.getDay() == selectedDay){
                                schemeList.add(scheme);
                                adapter.notifyDataSetChanged();
                            }
                        } else if(is_agenda){
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
    private void getReadPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                    | ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS) | ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//是否请求过该权限
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, 10001);
                } else {//没有则请求获取权限，示例权限是：存储权限和短信权限，需要其他权限请更改或者替换
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10001);
                }
            } else {//如果已经获取到了权限则直接进行下一步操作
                filter = new IntentFilter();
                filter.addAction("android.provider.Telephony.SMS_RECEIVED" );
                receiver=new SmsReceiver();
                registerReceiver(receiver,filter);//注册广播接收器
            }
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
        selectedYear = calendar.getYear();
        selectedMonth = calendar.getMonth();
        selectedDay = calendar.getDay();
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


