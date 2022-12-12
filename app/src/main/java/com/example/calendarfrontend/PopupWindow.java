package com.example.calendarfrontend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lxj.xpopup.core.CenterPopupView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@SuppressLint("ViewConstructor")
public class PopupWindow extends CenterPopupView {
    private final Scheme mScheme;
    private final String raw_text;
    private TextView mSource;
    private EditText mTitle, mDate, mLocation, mStartTime, mEndTime;
    private OkHttpClient client;

    public PopupWindow(Context context, Scheme scheme, String raw_text) {
        super(context);
        mScheme = scheme;
        this.raw_text = raw_text;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate() {
        super.onCreate();
        mSource = findViewById(R.id.tv_source);
        mTitle = findViewById(R.id.tv_title);
        mDate = findViewById(R.id.tv_date);
        mLocation = findViewById(R.id.tv_location);
        mStartTime = findViewById(R.id.tv_startTime);
        mEndTime = findViewById(R.id.tv_endTime);

        mSource.setText(raw_text);
        mTitle.setText(mScheme.getTitle());
        mDate.setText(mScheme.getYear() + "-" + mScheme.getMonth() + "-" + mScheme.getDay());
        mLocation.setText(mScheme.getLocation());
        mStartTime.setText(mScheme.getStartTime());
        mEndTime.setText(mScheme.getEndTime());
        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .cookieJar(CookieJarManager.cookieJar)//自动管理Cookie
                .build();

        findViewById(R.id.btn_add).setOnClickListener(v -> {
            // 加入日程
            mScheme.setTitle(mTitle.getText().toString());
            mScheme.setLocation(mLocation.getText().toString());
            mScheme.setStartTime(mStartTime.getText().toString());
            mScheme.setEndTime(mEndTime.getText().toString());
            String[] date = mDate.getText().toString().split("-");
            if(mScheme.getTitle().equals("")) {
//                Toast.makeText(getContext(), "标题不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if(date.length != 3){
//                Toast.makeText(getContext(), "日期格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
            int year = Integer.parseInt(date[0]);
            int month = Integer.parseInt(date[1]);
            int day = Integer.parseInt(date[2]);
            if(!checkdate(year, month, day)){
//                Toast.makeText(getContext(), "日期格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!checktime(mScheme.getStartTime(), mScheme.getEndTime())){
//                Toast.makeText(getContext(), "时间格式错误", Toast.LENGTH_SHORT).show();
                return;
            }
            DbHandler.insertScheme(MainActivity.schemeDB, "schemes", mScheme);
            // TODO: 反馈给后端
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            JSONObject jo = new JSONObject();
            try {
                jo.put("id", mScheme.getId());
                jo.put("conf_id", mScheme.getConf_id());
                jo.put("is_agenda", true);
                jo.put("confidence_high", true);
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
                    .url("http://123.125.240.150:37511/tsingenda/feedback/")
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
            dismiss();
        });
        findViewById(R.id.btn_ignore).setOnClickListener(v -> {
            // 忽略日程
            // TODO: 反馈给后端
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            JSONObject jo = new JSONObject();
            try {
                jo.put("id", mScheme.getId());
                jo.put("conf_id", mScheme.getConf_id());
                jo.put("is_agenda", true);
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
                    .url("http://123.125.240.150:37511/tsingenda/feedback/")
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
            dismiss();
        });
        findViewById(R.id.btn_ignore).setOnClickListener(v -> {
            // 不是日程
            // TODO: 反馈给后端
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
                    .url("http://123.125.240.150:37511/tsingenda/feedback/")
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
            dismiss();
        });
    }

    private boolean checkdate(int year, int month, int day) {
        int[] maxdays = {31,29,31,30,31,30,31,31,30,31,30,31};
        if(year < 0 || month < 0 || month > 12 || day < 0 || day > maxdays[month - 1]) {
            return false;
        }
        if(month == 2 && day == 29) {
            return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
        }
        return true;
    }
    private boolean checktime(String time1, String time2) {
        String[] time1s = time1.split(":");
        String[] time2s = time2.split(":");
        if(time1s.length != 2 || time2s.length != 2) {
            return false;
        }
        int hour1 = Integer.parseInt(time1s[0]);
        int minute1 = Integer.parseInt(time1s[1]);
        int hour2 = Integer.parseInt(time2s[0]);
        int minute2 = Integer.parseInt(time2s[1]);
        if(hour1 < 0 || hour1 > 23 || hour2 < 0 || hour2 > 23 || minute1 < 0 || minute1 > 59 || minute2 < 0 || minute2 > 59) {
            return false;
        }
        if(hour1 > hour2 || (hour1 == hour2 && minute1 > minute2)) {
            return false;
        }
        return true;
    }
}
