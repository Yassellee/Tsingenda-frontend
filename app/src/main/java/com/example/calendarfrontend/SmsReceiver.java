package com.example.calendarfrontend;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.util.XPermission;

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
                                    if(date.length() == 0){
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
                                    DbHandler.insertScheme(MainActivity.schemeDB, "schemes", scheme);
                                    if(scheme.getYear() == MainActivity.selectedYear && scheme.getMonth() == MainActivity.selectedMonth && scheme.getDay() == MainActivity.selectedDay){
                                        MainActivity.schemeList.add(scheme);
                                        MainActivity.adapter.notifyDataSetChanged();
                                    }
                                } else if(is_agenda){
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        XPopup.requestOverlayPermission(context, new XPermission.SimpleCallback() {
                                            @Override
                                            public void onGranted() {
                                                new XPopup.Builder(context)
                                                        .isDarkTheme(true)
                                                        .enableShowWhenAppBackground(true)
                                                        .asCustom(new PopupWindow(context, scheme, raw_text))
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
        }
    }
}