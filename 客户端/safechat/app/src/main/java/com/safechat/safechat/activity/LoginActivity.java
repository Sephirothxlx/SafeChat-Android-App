package com.safechat.safechat.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.safechat.safechat.R;
import com.safechat.safechat.entity.Client;
import com.safechat.safechat.message.AbstractMessage;
import com.safechat.safechat.message.AcceptLoginResponse;

import java.io.File;
import java.net.ServerSocket;

/**
 * Created by Sephiroth on 17/5/13.
 */

public class LoginActivity extends AppCompatActivity {
    // 登陆按钮
    private Button loginbutton;
    private Button registbutton;
    private Button choosebutton;
    // 调试文本，注册文本
    // 显示用户名和密码
    private EditText username, password;
    Client c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 获取控件
        username = (EditText) findViewById(R.id.login_email);
        password = (EditText) findViewById(R.id.login_password);
        password.setEnabled(false);
        loginbutton = (Button) findViewById(R.id.login_loginbutton);
        registbutton = (Button) findViewById(R.id.login_registerbutton);
        choosebutton = (Button) findViewById(R.id.login_choosebutton);
        // 设置按钮监听器
        loginbutton.setOnClickListener(new listener0());
        registbutton.setOnClickListener(new listener1());
        choosebutton.setOnClickListener(new listener2());
        c = (Client) getApplication();
        c.initDao();

    }

    public class listener0 implements View.OnClickListener {
        public void onClick(View v) {
            new Thread(new MyThread()).start();
        }
    }

    public class listener1 implements View.OnClickListener {
        public void onClick(View v) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    }

    public class listener2 implements View.OnClickListener {
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"), 1);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(LoginActivity.this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                String path = getPath(this, uri);
                if (path == null) {
                    Toast.makeText(LoginActivity.this, "请选择文件", Toast.LENGTH_SHORT).show();
                } else {
                    File f = new File(path);
                    password.setText(path);
                }
            }
        }
    }

    public String getPath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null)
            data = uri.getPath();
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    class MyThread implements Runnable {
        @Override
        public void run() {
            Looper.prepare();
            String x = username.getText().toString();
            String y = password.getText().toString();
            AlertDialog.Builder builder = new android.app.AlertDialog.Builder(LoginActivity.this);
            c.initSocket();
            if (x == null || x.equals("") || y == null || y.equals("")) {
                builder.setMessage("请填写登录信息");
                builder.setTitle("提示");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                c.closeSocket();
            } else {
                c.login(x, y);
                AcceptLoginResponse temp = (AcceptLoginResponse) c.getMessage();
                if (temp != null && temp.getType() == AbstractMessage.Type.ACCEPT_LOGIN_RESPONSE) {
                    c.setKcs(temp.getKcs());
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    builder.setMessage("登录失败");
                    builder.setTitle("提示");
                    builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.create().show();
                    c.closeSocket();
                }
            }
            Looper.loop();
        }
    }
}
