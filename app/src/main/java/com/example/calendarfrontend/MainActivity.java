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
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.os.Message;
import android.provider.Settings;
import android.telephony.SmsMessage;
import android.text.TextUtils;
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
import java.util.Objects;

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
    public static Handler mhandler;
    public String lastClipboard = "";
    public static StringBuilder lastMessage = new StringBuilder();

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
        // ?????????????????????????????????????????????Activity?????????????????????????????????????????????
        .setShowPattern(ShowPattern.ALL_TIME).setLayout(R.layout.floattab)
                // ???????????????????????????
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
        mCurrentMonth.setText(String.format("%d???%d???", mCalendarView.getCurYear(), mCalendarView.getCurMonth()));
        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));
        selectedYear = mCalendarView.getCurYear();
        selectedMonth = mCalendarView.getCurMonth();
        selectedDay = mCalendarView.getCurDay();
        mhandler = new Handler(new Handler.Callback() {
            public boolean handleMessage(Message message) {
                if (message.what == 1) {
                    new XPopup.Builder(MainActivity.this).isDarkTheme(true).enableShowWhenAppBackground(true).asCustom(new PopupWindow(MainActivity.this, (Scheme) message.obj)).show();
                    return false;
                } else if (message.what != 2) {
                    return false;
                } else {
                    MainActivity.schemeList.add((Scheme) message.obj);
                    MainActivity.adapter.notifyDataSetChanged();
                    return false;
                }
            }
        });

        schemeDB = this.openOrCreateDatabase("SchemeDB", Context.MODE_PRIVATE, null);
        DbHandler.createTable(schemeDB, "schemes");

//        Toast.makeText(MainActivity.this, PATH, Toast.LENGTH_SHORT).show();

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
                    .asConfirm("???????????????????????????", "", () -> {
                        Scheme mScheme = schemeList.get(position);
                        if(mScheme.getId() != -1) {
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .retryOnConnectionFailure(true)
                                    .cookieJar(CookieJarManager.cookieJar)//????????????Cookie
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
                                    //????????????????????????????????????????????????????????????UI?????????
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
                mCurrentMonth.setText(String.format("%d???%d???", mCalendarView.getCurYear(), mCalendarView.getCurMonth()));
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
                    .asInputConfirm("?????????????????????", "",
                            new OnInputConfirmListener() {
                                @Override
                                public void onConfirm(String text) {
                                    Scheme tmp = DbHandler.queryScheme(schemeDB, "schemes", text);
                                    if(tmp.getTitle() != null) {
                                        mCalendarView.scrollToCalendar(tmp.getYear(), tmp.getMonth(), tmp.getDay());
                                        Toast.makeText(MainActivity.this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "??????????????????", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).show();
        });
        mSetting.setOnClickListener((v) -> {
            new XPopup.Builder(this)
                    .isDarkTheme(true)
                    .atView(mSetting)
                    .asAttachList(new String[]{"?????????????????????", "??????????????????"}, null, (position, text) -> {
                        switch(position){
                            case 0:
                                new XPopup.Builder(MainActivity.this)
                                        .isDarkTheme(true)
                                        .asCustom(popup)
                                        .show();
                                break;
                            case 1:
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
        getClipboardData();
//        Log.d(TAG, PATH);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    /**
     * ???????????????
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
        //???????????????????????????????????????
        File file = new File(path);
        if (!file.exists()){
//            runOnUiThread(() -> Toast.makeText(MainActivity.this, "???????????????", Toast.LENGTH_SHORT).show());
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
        MediaType parse = MediaType.parse("application/json;charset=utf-8");
        JSONArray jSONArray = new JSONArray();
        System.out.println(imageToBase64Str(path));
        jSONArray.put(imageToBase64Str(path));
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("data", jSONArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(jSONArray);
        OkHttpClient build = new OkHttpClient.Builder().retryOnConnectionFailure(true).cookieJar(CookieJarManager.cookieJar).build();
        Request.Builder builder = new Request.Builder();
        build.newCall(builder.url("http://123.125.240.150:37511/tsingenda/image/").post(RequestBody.create(parse, String.valueOf(jSONObject))).build()).enqueue(new Callback() {
            public void onFailure(Call call, final IOException iOException) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Context applicationContext = MainActivity.this.getApplicationContext();
                        Toast.makeText(applicationContext, "????????????:" + iOException.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.toString());
                if (response.isSuccessful()) {
                    try {
                        JSONArray jSONArray = new JSONObject(response.body().string()).getJSONArray("data");
                        if (jSONArray.length() != 0) {
                            JSONObject jSONObject = jSONArray.getJSONObject(0);
                            final Scheme scheme = new Scheme();
                            scheme.setId(jSONObject.getInt("id"));
                            scheme.setConf_id(jSONObject.getInt("conf_id"));
                            scheme.setTitle(jSONObject.getString("title"));
                            scheme.setRaw_text(jSONObject.getString("raw_text"));
                            JSONArray jSONArray2 = jSONObject.getJSONArray("dates");
                            if (jSONArray2.length() == 0) {
                                scheme.setYear(2022);
                                scheme.setMonth(12);
                                scheme.setDay(13);
                            } else {
                                JSONArray jSONArray3 = jSONArray2.getJSONArray(0);
                                if (jSONArray3.length() <= 1) {
                                    scheme.setYear(2022);
                                    scheme.setMonth(12);
                                    scheme.setDay(13);
                                } else {
                                    String[] split = jSONArray3.getString(1).split("-");
                                    scheme.setYear(Integer.parseInt(split[0]));
                                    scheme.setMonth(Integer.parseInt(split[1]));
                                    scheme.setDay(Integer.parseInt(split[2]));
                                }
                            }
                            JSONArray jSONArray4 = jSONObject.getJSONArray("times");
                            if (jSONArray4.length() == 0) {
                                scheme.setStartTime("00:00");
                                scheme.setEndTime("23:59");
                            } else {
                                JSONArray jSONArray5 = jSONArray4.getJSONArray(0);
                                if (jSONArray5.length() == 0) {
                                    scheme.setStartTime("00:00");
                                    scheme.setEndTime("23:59");
                                } else {
                                    String[] split2 = jSONArray5.getString(1).split(":");
                                    scheme.setStartTime(split2[0] + ":" + split2[1]);
                                    if (Integer.parseInt(split2[0]) < 23) {
                                        scheme.setEndTime(String.valueOf(Integer.parseInt(split2[0]) + 1) + ":" + split2[1]);
                                    } else {
                                        scheme.setEndTime("23:59");
                                    }
                                }
                            }
                            JSONArray jSONArray6 = jSONObject.getJSONArray("locations");
                            if (jSONArray6.length() == 0) {
                                scheme.setLocation("??????");
                            } else {
                                scheme.setLocation(jSONArray6.getString(0));
                            }
                            boolean z = jSONObject.getBoolean("is_agenda");
                            boolean z2 = jSONObject.getBoolean("confidence_high");
                            if (!z) {
                                return;
                            }
                            if (z2) {
                                DbHandler.insertScheme(MainActivity.schemeDB, "schemes", scheme);
                                if (scheme.getYear() == MainActivity.selectedYear && scheme.getMonth() == MainActivity.selectedMonth && scheme.getDay() == MainActivity.selectedDay) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        public void run() {
                                            MainActivity.schemeList.add(scheme);
                                            MainActivity.adapter.notifyDataSetChanged();
                                        }
                                    });
                                    return;
                                }
                                return;
                            }
                            MainActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    new XPopup.Builder(MainActivity.this).isDarkTheme(true).enableShowWhenAppBackground(true).asCustom(new PopupWindow(MainActivity.this, scheme)).show();
                                }
                            });
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
        // ??????
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
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS) | ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//????????????????????????
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE}, 10001);
                } else {//?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.READ_SMS,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10001);
                }
            } else {//????????????????????????????????????????????????????????????
                filter = new IntentFilter();
                filter.addAction("android.provider.Telephony.SMS_RECEIVED" );
                receiver=new SmsReceiver();
                registerReceiver(receiver,filter);//?????????????????????
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
        mCurrentMonth.setText(String.format("%d???%d???", calendar.getYear(), calendar.getMonth()));
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

    private void getClipboardData() {
        getWindow().getDecorView().post(new Runnable() {
            public void run() {
                if (MainActivity.this.fetchClipboard() != null && !Objects.equals(MainActivity.this.fetchClipboard(), MainActivity.this.lastClipboard)) {
                    MainActivity mainActivity = MainActivity.this;
                    String unused = mainActivity.lastClipboard = mainActivity.fetchClipboard();
                    MediaType parse = MediaType.parse("application/json;charset=utf-8");
                    JSONArray jSONArray = new JSONArray();
                    jSONArray.put(MainActivity.this.lastClipboard);
                    JSONObject jSONObject = new JSONObject();
                    try {
                        jSONObject.put("data", jSONArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println(jSONArray);
                    OkHttpClient build = new OkHttpClient.Builder().retryOnConnectionFailure(true).cookieJar(CookieJarManager.cookieJar).build();
                    Request.Builder builder = new Request.Builder();
                    build.newCall(builder.url("http://123.125.240.150:37511/tsingenda/raw_text/").post(RequestBody.create(parse, String.valueOf(jSONObject))).build()).enqueue(new Callback() {
                        public void onFailure(Call call, IOException iOException) {
                        }

                        public void onResponse(Call call, Response response) throws IOException {
                            System.out.println(response.toString());
                            if (response.isSuccessful()) {
                                try {
                                    JSONArray jSONArray = new JSONObject(response.body().string()).getJSONArray("data");
                                    if (jSONArray.length() != 0) {
                                        JSONObject jSONObject = jSONArray.getJSONObject(0);
                                        Scheme scheme = new Scheme();
                                        scheme.setId(jSONObject.getInt("id"));
                                        scheme.setConf_id(jSONObject.getInt("conf_id"));
                                        scheme.setTitle(jSONObject.getString("title"));
                                        scheme.setRaw_text(jSONObject.getString("raw_text"));
                                        JSONArray jSONArray2 = jSONObject.getJSONArray("dates");
                                        if (jSONArray2.length() == 0) {
                                            scheme.setYear(2022);
                                            scheme.setMonth(12);
                                            scheme.setDay(13);
                                        } else {
                                            JSONArray jSONArray3 = jSONArray2.getJSONArray(0);
                                            if (jSONArray3.length() == 0) {
                                                scheme.setYear(2022);
                                                scheme.setMonth(12);
                                                scheme.setDay(13);
                                            } else {
                                                String[] split = jSONArray3.getString(1).split("-");
                                                scheme.setYear(Integer.parseInt(split[0]));
                                                scheme.setMonth(Integer.parseInt(split[1]));
                                                scheme.setDay(Integer.parseInt(split[2]));
                                            }
                                        }
                                        JSONArray jSONArray4 = jSONObject.getJSONArray("times");
                                        if (jSONArray4.length() == 0) {
                                            scheme.setStartTime("00:00");
                                            scheme.setEndTime("23:59");
                                        } else {
                                            JSONArray jSONArray5 = jSONArray4.getJSONArray(0);
                                            if (jSONArray5.length() == 0) {
                                                scheme.setStartTime("00:00");
                                                scheme.setEndTime("23:59");
                                            } else {
                                                String[] split2 = jSONArray5.getString(1).split(":");
                                                scheme.setStartTime(split2[0] + ":" + split2[1]);
                                                if (Integer.parseInt(split2[0]) < 23) {
                                                    scheme.setEndTime(String.valueOf(Integer.parseInt(split2[0]) + 1) + ":" + split2[1]);
                                                } else {
                                                    scheme.setEndTime("23:59");
                                                }
                                            }
                                        }
                                        JSONArray jSONArray6 = jSONObject.getJSONArray("locations");
                                        if (jSONArray6.length() == 0) {
                                            scheme.setLocation("??????");
                                        } else {
                                            scheme.setLocation(jSONArray6.getString(0));
                                        }
                                        boolean z = jSONObject.getBoolean("is_agenda");
                                        boolean z2 = jSONObject.getBoolean("confidence_high");
                                        if (!z) {
                                            return;
                                        }
                                        if (z2) {
                                            DbHandler.insertScheme(MainActivity.schemeDB, "schemes", scheme);
                                            if (scheme.getYear() == MainActivity.selectedYear && scheme.getMonth() == MainActivity.selectedMonth && scheme.getDay() == MainActivity.selectedDay) {
                                                Message message = new Message();
                                                message.what = 2;
                                                message.obj = scheme;
                                                MainActivity.mhandler.sendMessage(message);
                                                return;
                                            }
                                            return;
                                        }
                                        Message message2 = new Message();
                                        message2.what = 1;
                                        message2.obj = scheme;
                                        MainActivity.mhandler.sendMessage(message2);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public String fetchClipboard() {
        ClipData primaryClip;
        ClipData.Item itemAt;
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip() || (primaryClip = clipboardManager.getPrimaryClip()) == null || primaryClip.getItemCount() <= 0 || (itemAt = primaryClip.getItemAt(0)) == null) {
            return null;
        }
        CharSequence text = itemAt.getText();
        if (!TextUtils.isEmpty(text)) {
            return text.toString();
        }
        return null;
    }
}


