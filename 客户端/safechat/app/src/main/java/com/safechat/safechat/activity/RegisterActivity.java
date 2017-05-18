package com.safechat.safechat.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.safechat.safechat.R;
import com.safechat.safechat.dao.Dao;
import com.safechat.safechat.entity.Client;
import com.safechat.safechat.message.AbstractMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.safechat.safechat.message.AbstractMessage.Type.ACCEPT_REGISTER_RESPONSE;

/**
 * Created by Sephiroth on 17/5/13.
 */

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    // 登陆按钮
    private Button registerbutton;
    // 调试文本，注册文本
    EditText username;
    // 创建等待框
    private ProgressDialog dialog;

    Client c;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // 获取控件
        username = (EditText) findViewById(R.id.register_email);
        registerbutton = (Button) findViewById(R.id.register_registerbutton);
        // 设置按钮监听器
        registerbutton.setOnClickListener(this);
        c=(Client)getApplication();
    }

    @Override
    public void onClick(View v) {
        new Thread(new MyThread()).start();
    }

    class MyThread implements Runnable {
        @Override
        public void run() {
            Looper.prepare();
            String email = username.getText().toString();
            AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RegisterActivity.this);
            if (checkEmail(email)) {
                c.initSocket();
                c.register(email);
                AbstractMessage temp = c.getMessage();
                if (temp != null && temp.getType() == ACCEPT_REGISTER_RESPONSE) {
                    builder.setMessage("注册成功");
                    builder.setTitle("提示");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                        }
                    });
                    builder.create().show();
                    Dao.storeUserPrivateKey(getApplicationContext(),c.getUser());
                    c.closeSocket();
                } else {
                    builder.setMessage("注册失败");
                    builder.setTitle("提示");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                }
            } else {
                builder.setMessage("邮箱格式错误");
                builder.setTitle("提示");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
            Looper.loop();
        }
    }

    public boolean checkEmail(String email) {
        String str = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(email);
        return m.matches();
    }
}
