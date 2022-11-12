package com.example.calendarfrontend;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import java.io.IOException;

//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.FormBody;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;

public class ChangepwdActivity extends AppCompatActivity {
    private EditText newemail;
    private EditText vertifycode;
    private EditText newpwd;
    private EditText newpwdcheck;
    private Button changebotton;
    private Button verbutton;
    private ToggleButton sendbutton;
    private boolean mimasuc;
    private Group yz;
    private Group mm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepwd);
        yz.setOnClickListener(changelistener);
        mm=(Group)findViewById(R.id.mimagroup);
        mm.setOnClickListener(changelistener);
        newemail = (EditText) findViewById(R.id.remail);
        newpwd = (EditText) findViewById(R.id.rpassword);
        newpwdcheck = (EditText) findViewById(R.id.rerpassword);
        changebotton = (Button) findViewById(R.id.regist);
        changebotton.setOnClickListener(changelistener);
        sendbutton.setOnClickListener(changelistener);
    }

    View.OnClickListener changelistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.regist:
                    pwdCheck();
                    break;
            }
        }
    };

    public void pwdCheck() {
        if(isUserNameAndPwdValid()){
//            FormBody.Builder builder = new FormBody.Builder();
//            builder.add("email", newemail.getText().toString().trim());
//            builder.add("password", newpwd.getText().toString().trim());
//            builder.add("code", vertifycode.getText().toString().trim());
//            FormBody formBody = builder.build();
//            Request request = new Request.Builder()
//                    .url(getString(R.string.url) + "/changepwd")
//                    .post(formBody)
//                    .build();
//            OkHttpClient client = new OkHttpClient();
//            Call call = client.newCall(request);
//            call.enqueue(new Callback()
//            {
//                @Override
//                public void onFailure(Call call, IOException e)
//                {
//                    e.printStackTrace();
//                }
//                @Override
//                public void onResponse(Call call, Response response)
//                {
//                    //此方法运行在子线程中，不能在此方法中进行UI操作。
//                    if(response.isSuccessful())
//                    {
//                        runOnUiThread(() -> {
//                            Intent changeToMain=new Intent(ChangepwdActivity.this, LoginActivity.class);
//                            startActivity(changeToMain);
//                            finish();
//                        });
//                    }
//                    else
//                    {
//                        runOnUiThread(() -> Toast.makeText(ChangepwdActivity.this, "验证码错误！", Toast.LENGTH_SHORT).show());
//                    }
//                }
//            });
        }
    }

    public boolean isUserNameAndPwdValid() {
        String a="手机号不能为空";
        String b="密码不能为空";
        String c="确认密码不能为空";
        String d="密码输入不一致";
        if (newemail.getText().toString().trim().equals("")) {
            Toast.makeText(this,a,Toast.LENGTH_SHORT).show();
            return false;
        } else if (newpwd.getText().toString().trim().equals("")) {
            Toast.makeText(this,b,
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(newpwdcheck.getText().toString().trim().equals("")) {
            Toast.makeText(this, c,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(!newpwd.getText().toString().trim().equals(newpwdcheck.getText().toString().trim())){
            Toast.makeText(this, d,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    private void sendVertify() {
//        FormBody.Builder builder = new FormBody.Builder();
//        builder.add("email", newemail.getText().toString().trim());
//        FormBody formBody = builder.build();
//        Request request = new Request.Builder()
//                .url(getString(R.string.url) + "/sendcode")
//                .post(formBody)
//                .build();
//        OkHttpClient client = new OkHttpClient();
//        Call call = client.newCall(request);
//        call.enqueue(new Callback()
//        {
//            @Override
//            public void onFailure(Call call, IOException e)
//            {
//                runOnUiThread(() -> Toast.makeText(ChangepwdActivity.this, "发送失败！", Toast.LENGTH_SHORT).show());
//                e.printStackTrace();
//            }
//            @Override
//            public void onResponse(Call call, Response response) throws IOException
//            {
//                //此方法运行在子线程中，不能在此方法中进行UI操作。
//                if(response.isSuccessful())
//                {
//                    runOnUiThread(() -> {
//
//                        yz.setVisibility(View.INVISIBLE);
//                        mm.setVisibility(View.VISIBLE);
//                        sendbutton.setClickable(false);
//                    });
//                }
//                else
//                {
//                    System.out.println(response.body());
//                    runOnUiThread(() -> Toast.makeText(ChangepwdActivity.this, "发送失败！", Toast.LENGTH_SHORT).show());
//                }
//            }
//        });
    }
}