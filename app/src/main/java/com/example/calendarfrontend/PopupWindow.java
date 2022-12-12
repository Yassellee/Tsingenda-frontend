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
            mScheme.setYear(Integer.parseInt(date[0]));
            mScheme.setMonth(Integer.parseInt(date[1]));
            mScheme.setDay(Integer.parseInt(date[2]));
            DbHandler.insertScheme(MainActivity.schemeDB, "schemes", mScheme);
            // TODO: 反馈给后端
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            JSONObject jo = new JSONObject();
            try {
                jo.put("id", mScheme.getId());
                jo.put("text", mScheme.getTitle());
                jo.put("is_agenda", true);
                jo.put("confidence_high", true);
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
            dismiss();
        });
        findViewById(R.id.btn_ignore).setOnClickListener(v -> {
            // 忽略日程
            // TODO: 反馈给后端
            MediaType JSON = MediaType.parse("application/json;charset=utf-8");
            JSONObject jo = new JSONObject();
            try {
                jo.put("id", mScheme.getId());
                jo.put("text", mScheme.getTitle());
                jo.put("is_agenda", true);
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
            dismiss();
        });
        findViewById(R.id.btn_ignore).setOnClickListener(v -> {
            // 不是日程
            // TODO: 反馈给后端
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
            JSONObject jo2 = new JSONObject();
            try {
                jo2.put("data", ja);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(JSON, jo2.toString());
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
            dismiss();
        });
    }
}
