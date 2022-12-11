package com.example.calendarfrontend;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lxj.xpopup.core.CenterPopupView;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.FormBody;
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
    private EditText mTitle, mDate, mLocation, mStartTime, mEndTime;
    private OkHttpClient client;

    public PopupWindow(Context context, Scheme scheme) {
        super(context);
        mScheme = scheme;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.popup;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate() {
        super.onCreate();
        mTitle = findViewById(R.id.tv_title);
        mDate = findViewById(R.id.tv_date);
        mLocation = findViewById(R.id.tv_location);
        mStartTime = findViewById(R.id.tv_startTime);
        mEndTime = findViewById(R.id.tv_endTime);
        mTitle.setText(mScheme.getTitle());
        mDate.setText(mScheme.getYear() + "-" + mScheme.getMonth() + "-" + mScheme.getDay());
        mLocation.setText(mScheme.getLocation());
        mStartTime.setText(mScheme.getStartTime());
        mEndTime.setText(mScheme.getEndTime());
        client = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .cookieJar(new CookieJarManager())//自动管理Cookie
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
            FormBody.Builder builder = new FormBody.Builder();
            RequestBody body = new FormBody.Builder()
                    .add("id", String.valueOf(mScheme.getId()))
                    .add("text", mScheme.getTitle())
                    .add("is_agenda", String.valueOf(true))
                    .add("confidence_high", String.valueOf(true))
                    .build();
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
            FormBody.Builder builder = new FormBody.Builder();
            RequestBody body = new FormBody.Builder()
                    .add("id", String.valueOf(mScheme.getId()))
                    .add("text", mScheme.getTitle())
                    .add("is_agenda", String.valueOf(true))
                    .add("confidence_high", String.valueOf(false))
                    .build();
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
            FormBody.Builder builder = new FormBody.Builder();
            RequestBody body = new FormBody.Builder()
                    .add("id", String.valueOf(mScheme.getId()))
                    .add("text", mScheme.getTitle())
                    .add("is_agenda", String.valueOf(false))
                    .add("confidence_high", String.valueOf(false))
                    .build();
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
