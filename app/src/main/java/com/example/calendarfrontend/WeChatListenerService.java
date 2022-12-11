package com.example.calendarfrontend;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WeChatListenerService extends AccessibilityService {
    private String ChatRecord = "test";
    private OkHttpClient client;

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
            List<AccessibilityNodeInfo> listChatRecord = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/o");
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

                        // ToDo: 发送给后端
                        Toast.makeText(this, chatName + "：" + ChatRecord, Toast.LENGTH_SHORT).show();
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
