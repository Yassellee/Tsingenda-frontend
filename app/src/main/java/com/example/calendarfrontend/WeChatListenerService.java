package com.example.calendarfrontend;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.util.XPermission;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.app.Activity;
public class WeChatListenerService extends AccessibilityService {
    private String ChatRecord = "test";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                getWeChatLog(rootNode);
                break;
            default:
                break;
        }
    }

    private void getWeChatLog(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<AccessibilityNodeInfo> listChatRecord = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hp");
            if(listChatRecord.size() == 0){
                return;
            }
            AccessibilityNodeInfo finalNode = listChatRecord.get(listChatRecord.size() - 1);
            List<AccessibilityNodeInfo> imageName = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/i_");
            List<AccessibilityNodeInfo> record = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ib");
            if (imageName.size() != 0) {
                String chatName;
                if (record.size() == 0) {
                    if (!ChatRecord.equals("对方发的是图片或者表情")) {
                        chatName = imageName.get(0).getContentDescription().toString().replace("头像", "");
                        ChatRecord = "对方发的是图片或者表情";
                        Toast.makeText(this, chatName + "：" + ChatRecord, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (!ChatRecord.equals(record.get(0).getText().toString())) {
                        chatName = imageName.get(0).getContentDescription().toString().replace("头像", "");
                        ChatRecord = record.get(0).getText().toString();

                        //ToDo: 发送给后端
                        Toast.makeText(this, chatName + "：" + ChatRecord, Toast.LENGTH_SHORT).show();
                        OkHttpClient client = new OkHttpClient.Builder()
                                .retryOnConnectionFailure(true)
                                .cookieJar(new CookieJarManager())//自动管理Cookie
                                .build();
                        MediaType JSON = MediaType.parse("application/json;charset=utf-8");
                        JSONArray ja = new JSONArray();
                        ja.put(ChatRecord);
                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("data", ja);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        RequestBody requestBody = RequestBody.create(JSON, jo.toString());
                        Request request = new Request.Builder()
                                .url(getString(R.string.neturl)+"/tsingenda/raw_text/")
                                .post(requestBody)
                                .build();
                        Call call = client.newCall(request);
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Toast.makeText(getApplicationContext(),"信息上传失败",Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                System.out.println(response.toString());
                                //TODO:add response to user
                                if(response.isSuccessful())
                                {
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
                                            DbHandler.insertScheme(MainActivity.schemeDB, "schemes", scheme);
                                        } else if(is_agenda){
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                XPopup.requestOverlayPermission(getBaseContext(), new XPermission.SimpleCallback() {
                                                    @Override
                                                    public void onGranted() {
                                                        new XPopup.Builder(getBaseContext())
                                                                .isDarkTheme(true)
                                                                .enableShowWhenAppBackground(true)
                                                                .asCustom(new PopupWindow(getBaseContext(), scheme, raw_text))
                                                                .show();
                                                    }
                                                    @Override
                                                    public void onDenied() {
                                                        Toast.makeText(getBaseContext(), "没有权限无法显示弹窗", Toast.LENGTH_SHORT).show();
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
