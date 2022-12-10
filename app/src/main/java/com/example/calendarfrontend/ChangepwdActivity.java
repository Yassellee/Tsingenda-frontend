package com.example.calendarfrontend;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChangepwdActivity extends AppCompatActivity {
    private EditText myemail;
    private EditText mypsw;
    private EditText mypswcheck;
    private Button r;
    private Button tologin;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashReport.initCrashReport(getApplicationContext(), "32de36c238", true);
        setContentView(R.layout.activity_register);
        myemail=(EditText)findViewById(R.id.remail);
        mypsw=(EditText)findViewById(R.id.rpassword);
        mypswcheck=(EditText)findViewById(R.id.rerpassword);
        tologin = (Button)findViewById(R.id.tologin);
        r=(Button)findViewById(R.id.regist);
        r.setOnClickListener(reglistener);
        tologin.setOnClickListener(reglistener);
        sharedPref = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }
    View.OnClickListener reglistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.regist:
                    registerCheck();
                    break;
                case R.id.tologin:
                    backtoLogin();
            }
        }
    };

    public void backtoLogin(){
        Intent registerToLogin = new Intent(ChangepwdActivity.this, LoginActivity.class);
        startActivity(registerToLogin);
    }
    public void registerCheck(){
        if(isUserNameAndPwdValid()){
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("action","change_password");
            builder.add("username", myemail.getText().toString().trim());
            builder.add("new_password", mypsw.getText().toString().trim());
            FormBody formBody = builder.build();
            Request request = new Request.Builder()
                    .url(getString(R.string.url) + "/tsingenda/login/")
                    .post(formBody)
                    .build();
            OkHttpClient client = new OkHttpClient();
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
                    if(response.isSuccessful())
                    {
                        HashMap<String, String> content = JSON.parseObject(response.body().string(), new TypeReference<HashMap<String, String>>(){});
                        HashMap<String, String> data = JSON.parseObject(content.get("data"), new TypeReference<HashMap<String, String>>(){});
                        if(data.get("status")!=null){
                            runOnUiThread(() -> Toast.makeText(ChangepwdActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show());
                        }
                        else{
                            editor.putString("name", content.get("name"));
                            editor.putString("avatar", content.get("avatar"));
                            editor.commit();
                            runOnUiThread(() -> {
                                Intent LoginToMain = new Intent(ChangepwdActivity.this, MainActivity.class);
                                startActivity(LoginToMain);
                                finish();
                            });
                        }
                    }
                }
            });

        }

    }
    public boolean isUserNameAndPwdValid() {
        String a="手机号不能为空";
        String b="密码不能为空";
        String c="确认密码不能为空";
        String d="密码输入不一致";
        if (myemail.getText().toString().trim().equals("")) {
            Toast.makeText(this,a,Toast.LENGTH_SHORT).show();
            return false;
        } else if (mypsw.getText().toString().trim().equals("")) {
            Toast.makeText(this,b,
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(mypswcheck.getText().toString().trim().equals("")) {
            Toast.makeText(this, c,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!mypsw.getText().toString().trim().equals(mypswcheck.getText().toString().trim())){
            Toast.makeText(this, d, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}