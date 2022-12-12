package com.example.calendarfrontend;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.util.XPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class QQListenerService extends AccessibilityService {
    private String ChatRecord = "test";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                getQQChatRecord(rootNode);
                break;
            default:
                break;
        }
    }

    private void getQQChatRecord(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<AccessibilityNodeInfo> listChatRecord = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/a6b");
            Toast.makeText(this, listChatRecord.size(), Toast.LENGTH_SHORT).show();
            if(listChatRecord.size() == 0){
                return;
            }
            AccessibilityNodeInfo finalNode = listChatRecord.get(listChatRecord.size() - 1);
            List<AccessibilityNodeInfo> record = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_content_layout");
            if(record.size() == 0){
                return;
            }
            Toast.makeText(this, record.size(), Toast.LENGTH_SHORT).show();
            if (!ChatRecord.equals(record.get(0).getText().toString())) {
                ChatRecord = record.get(0).getText().toString();
                //ToDo: 发送给后端
                Toast.makeText(this, "ChatRecord：" + ChatRecord, Toast.LENGTH_SHORT).show();
//                MediaType JSON = MediaType.parse("application/json;charset=utf-8");
//                JSONArray ja = new JSONArray();
//                ja.put(ChatRecord);
//                JSONObject texttext = new JSONObject();
//                try {
//                    texttext.put("data",ja);
//                }
//                catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                System.out.println(ja);
//                OkHttpClient client = new OkHttpClient.Builder()
//                        .retryOnConnectionFailure(true)
//                        .cookieJar(CookieJarManager.cookieJar)//自动管理Cookie
//                        .build();
//                Request.Builder reqBuilder = new Request.Builder();
//                RequestBody requestBody = RequestBody.create(JSON, String.valueOf(texttext));
//                Request request = reqBuilder
//                        .url("http://123.125.240.150:37511/tsingenda/raw_text/")
//                        .post(requestBody)
//                        .build();
//                Call call = client.newCall(request);
//                call.enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//                    }
//
//                    @Override
//                    public void onResponse(Call call, Response response) throws IOException {
//                        System.out.println(response.toString());
//                        //TODO:add response to user
//                        if(response.isSuccessful())
//                        {
//                            try {
//                                JSONObject jo = new JSONObject(response.body().string());
//                                JSONArray dataArray = jo.getJSONArray("data");
//                                JSONObject data = dataArray.getJSONObject(0);
//
//                                String raw_text = data.getString("raw_text");
//                                Scheme scheme = new Scheme();
//                                scheme.setId(data.getInt("id"));
//                                scheme.setConf_id(data.getInt("conf_id"));
//                                scheme.setTitle(data.getString("title"));
//
//                                JSONArray dates = data.getJSONArray("dates");
//                                if(dates.length() == 0){
//                                    scheme.setYear(2022);
//                                    scheme.setMonth(12);
//                                    scheme.setDay(13);
//                                } else {
//                                    JSONArray date = dates.getJSONArray(0);
//                                    if(date.length() == 0){
//                                        scheme.setYear(2022);
//                                        scheme.setMonth(12);
//                                        scheme.setDay(13);
//                                    } else {
//                                        String Nums = date.getString(1);
//                                        String[] Num = Nums.split("-");
//                                        scheme.setYear(Integer.parseInt(Num[0]));
//                                        scheme.setMonth(Integer.parseInt(Num[1]));
//                                        scheme.setDay(Integer.parseInt(Num[2]));
//                                    }
//                                }
//                                JSONArray times = data.getJSONArray("times");
//                                if(times.length() == 0){
//                                    scheme.setStartTime("00:00");
//                                    scheme.setEndTime("23:59");
//                                } else {
//                                    JSONArray time = times.getJSONArray(0);
//                                    if(time.length() == 0){
//                                        scheme.setStartTime("00:00");
//                                        scheme.setEndTime("23:59");
//                                    } else {
//                                        String Nums = time.getString(1);
//                                        String[] Num = Nums.split(":");
//                                        scheme.setStartTime(Nums);
//                                        if(Integer.parseInt(Num[0]) < 23){
//                                            scheme.setEndTime(String.valueOf(Integer.parseInt(Num[0])+1) + ":" + Num[1]);
//                                        } else {
//                                            scheme.setEndTime("23:59");
//                                        }
//                                    }
//                                }
//                                JSONArray locations = data.getJSONArray("locations");
//                                if(locations.length() == 0){
//                                    scheme.setLocation("未知");
//                                } else {
//                                    String location = locations.getString(0);
//                                    scheme.setLocation(location);
//                                }
//                                boolean is_agenda = data.getBoolean("is_agenda");
//                                boolean confidence_high = data.getBoolean("confidence_high");
//
//                                if(confidence_high)
//                                {
//                                    DbHandler.insertScheme(MainActivity.schemeDB, "schemes", scheme);
//                                    if(scheme.getYear() == MainActivity.selectedYear && scheme.getMonth() == MainActivity.selectedMonth && scheme.getDay() == MainActivity.selectedDay){
//                                        MainActivity.schemeList.add(scheme);
//                                        MainActivity.adapter.notifyDataSetChanged();
//                                    }
//                                } else if(is_agenda){
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                        XPopup.requestOverlayPermission(getBaseContext(), new XPermission.SimpleCallback() {
//                                            @Override
//                                            public void onGranted() {
//                                                new XPopup.Builder(getBaseContext())
//                                                        .isDarkTheme(true)
//                                                        .enableShowWhenAppBackground(true)
//                                                        .asCustom(new PopupWindow(getBaseContext(), scheme, raw_text))
//                                                        .show();
//                                            }
//                                            @Override
//                                            public void onDenied() {
//
//                                            }
//                                        });
//                                    }
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
            }
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "Interrupt", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        Toast.makeText(this, "服务已开启", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this, "服务已关闭", Toast.LENGTH_SHORT).show();
        return super.onUnbind(intent);
    }

}
