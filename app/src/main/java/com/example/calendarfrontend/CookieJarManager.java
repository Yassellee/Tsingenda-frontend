package com.example.calendarfrontend;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * OkHttp框架Cookie管理类
 */

public class CookieJarManager implements CookieJar {

    public static  CookieJarManager cookieJar = new CookieJarManager();
    private final String TAG = "CookieJarManager";

    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (null == url || null == cookies || cookies.size() <= 0) {
            return;
        }

        cookieStore.put(url.host(), cookies);

        for (Cookie cookie : cookies) {
            Log.d(TAG, "cookie Name:" + cookie.name());
            Log.d(TAG, "cookie Path:" + cookie.path());
            System.out.println(cookie);
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        if (null != url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        } else {
            return new ArrayList<Cookie>();
        }
    }
}