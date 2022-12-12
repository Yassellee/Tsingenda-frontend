package com.example.calendarfrontend;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        StringBuilder content = new StringBuilder();//用于存储短信内容
        String sender = null;//存储短信发送方手机号
        Bundle bundle = intent.getExtras();//通过getExtras()方法获取短信内容
        String format = intent.getStringExtra("format");
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");//根据pdus关键字获取短信字节数组，数组内的每个元素都是一条短信
            for (Object object : pdus) {
                SmsMessage message= null;//将字节数组转化为Message对象
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    message = SmsMessage.createFromPdu((byte[])object,format);
                }
                sender = message.getOriginatingAddress();//获取短信手机号
                content.append(message.getMessageBody());//获取短信内容
                MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                JSONArray ja = new JSONArray();
                ja.put(content);
                JSONObject texttext = new JSONObject();
                try {
                    texttext.put("data",ja);
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
                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(texttext));
                Request request = reqBuilder
                        .url("http://123.125.240.150:37511/tsingenda/raw_text/")
                        .post(requestBody)
                        .build();
                Call call = client.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        System.out.println(response.toString());
                        //TODO:add response to user
                        if(response.isSuccessful())
                        {

                        }
                    }
                });
            }
        }
    }
}