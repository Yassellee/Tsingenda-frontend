package com.example.calendarfrontend;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class LoginActivity extends AppCompatActivity {
    private EditText myemail;
    private EditText mypwd;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private OkHttpClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        myemail = findViewById(R.id.email);
        mypwd = findViewById(R.id.password);
        Button buttonreg = findViewById(R.id.regist);
        buttonreg.setOnClickListener(mListener);
        Button buttonlog = findViewById(R.id.login);
        buttonlog.setOnClickListener(mListener);
        Button buttonfog = findViewById(R.id.forget);
        buttonfog.setOnClickListener(mListener);
        sharedPref = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        client = new OkHttpClient();
    }
    OnClickListener mListener = new OnClickListener() {
        public void onClick(View v){
            switch (v.getId()){
                case R.id.login:
                    login();
                    break;
                case R.id.regist:
                    Intent LoginToRegister=new Intent(LoginActivity.this,RegisterActivity.class);
                    startActivity(LoginToRegister);
                    break;
                case R.id.forget:
                    Intent LoginToForget=new Intent(LoginActivity.this,ChangepwdActivity.class);
                    startActivity(LoginToForget);
                    break;
            }
        }
    };
    public void login(){
        String userpwd = mypwd.getText().toString().trim();
        String username = myemail.getText().toString().trim();

        FormBody.Builder builder = new FormBody.Builder();
        RequestBody body = new FormBody.Builder()
                .add("action","login")
                .add("username", username)
                .add("password", userpwd)
                .build();
        Request request = new Request.Builder()
                .url(getString(R.string.url)+"/tsingenda/login/")
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
                if(response.isSuccessful())
                {
                    HashMap<String, String> content = JSON.parseObject(response.body().string(), new TypeReference<HashMap<String, String>>(){});
                    HashMap<String, String> data = JSON.parseObject(content.get("data"), new TypeReference<HashMap<String, String>>(){});
                    if(data.get("status")!=null){
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show());
                    }
                    else{
                        editor.putString("name", content.get("name"));
                        editor.putString("avatar", content.get("avatar"));
                        editor.commit();
                        runOnUiThread(() -> {
                            Intent LoginToMain = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(LoginToMain);
                            finish();
                        });
                    }
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}