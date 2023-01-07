package com.example.calendarfrontend;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
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
            if (pdus.length > 0) {
                Object obj = pdus[0];
                SmsMessage message = null;//将字节数组转化为Message对象
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    message = SmsMessage.createFromPdu((byte[]) obj, format);
                }
                sender = message.getOriginatingAddress();//获取短信手机号
                content.append(message.getMessageBody());//获取短信内容
                if (!MainActivity.lastMessage.toString().equals(content.toString())) {
                    MainActivity.lastMessage = content;
                    MediaType parse = MediaType.parse("application/json;charset=utf-8");
                    JSONArray jSONArray = new JSONArray();
                    jSONArray.put(content);
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
                                            scheme.setLocation("未知");
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
        }
    }
}