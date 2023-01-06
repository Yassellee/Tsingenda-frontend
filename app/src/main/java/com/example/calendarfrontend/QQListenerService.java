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
            if (listChatRecord == null || listChatRecord.size() == 0) {
                Toast.makeText(this, "未检测到聊天框", Toast.LENGTH_SHORT).show();
                return;
            }
            AccessibilityNodeInfo lastChatRecord = listChatRecord.get(listChatRecord.size() - 1);
            if(lastChatRecord != null) {
                List<AccessibilityNodeInfo> listChatContent = lastChatRecord.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/chat_item_content_layout");
                if (listChatContent != null && listChatContent.size() != 0 && !this.ChatRecord.equals(listChatContent.get(0).getText().toString())) {
                    this.ChatRecord = listChatContent.get(0).getText().toString();
                    Toast.makeText(this, "ChatRecord：" + this.ChatRecord, Toast.LENGTH_SHORT).show();
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
