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
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.safechat.safechat.R;
import com.safechat.safechat.dao.Dao;
import com.safechat.safechat.encryptor.AES;
import com.safechat.safechat.encryptor.FileUtil;
import com.safechat.safechat.encryptor.RSA;
import com.safechat.safechat.entity.Client;
import com.safechat.safechat.message.AbstractMessage;
import com.safechat.safechat.message.AcceptEstablishChatResponse;
import com.safechat.safechat.message.ChatMessage;
import com.safechat.safechat.message.Datagram;
import com.safechat.safechat.message.FileMessage;
import com.safechat.safechat.message.ForwardMessage;
import com.safechat.safechat.user.Friend;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sephiroth on 17/5/16.
 */

public class ChatActivity extends AppCompatActivity {
    private ListView chatlist;
    private TextView name;
    private EditText chatinput;
    private Button sendmessage;
    private Button sendfile;
    private Friend f;
    private String FILE_SAVE_PATH = "/downloadfile/";
    private ArrayList<HashMap<String, String>> al = new ArrayList<>();
    private File file;
    private SimpleAdapter adapter;
    private String senderID;
    private String filePath;
    private byte[] fileByte;
    Key kcs;
    Socket s;
    Client c;
    Listener l;
    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatlist = (ListView) findViewById(R.id.chatlist);
        name = (TextView) findViewById(R.id.chatname);
        chatinput = (EditText) findViewById(R.id.chatinput);
        sendmessage = (Button) findViewById(R.id.sendmessage);
        sendfile = (Button) findViewById(R.id.sendfile);
        sendmessage.setEnabled(false);
        sendfile.setEnabled(false);

        Intent intent = getIntent();
        f = (Friend) intent.getSerializableExtra("friend");
        name.setText(f.getID());
        name.setEnabled(false);
        sendmessage.setOnClickListener(new listener0());
        sendfile.setOnClickListener(new listener1());
        adapter = new SimpleAdapter(getApplication(), al, R.layout.sender_message, new String[]{"message"}, new int[]{R.id.To_Content});
        chatlist.setAdapter(adapter);

        c = (Client) getApplication();
        c.initDao();

        new Thread(new EstablishChatSocket()).start();

        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ChatActivity.this);
                switch (msg.what) {
                    case 0:
                        adapter.notifyDataSetChanged();
                        break;
                    case 1:
                        HashMap<String, String> hm = new HashMap<>();
                        hm.put("message", senderID + "发来文件:" + filePath);
                        al.add(hm);
                        adapter.notifyDataSetChanged();
                        builder.setMessage("文件完好");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                FileUtil.byte2File(fileByte, filePath);
                            }
                        });
                        builder.create().show();
                        break;
                    case 2:
                        builder.setMessage("文件可能被恶意篡改，不予接收");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    case 3:
                        sendmessage.setEnabled(false);
                        sendfile.setEnabled(false);
                        builder.setMessage("对方连接断开");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    case 4:
                        Toast.makeText(ChatActivity.this, "连接已建立", Toast.LENGTH_SHORT).show();
                        sendmessage.setEnabled(true);
                        sendfile.setEnabled(true);
                        break;
                    case 5:
                        Toast.makeText(ChatActivity.this, "连接未建立", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    public class listener0 implements View.OnClickListener {
        public void onClick(View v) {
            HashMap<String, String> hm = new HashMap<>();
            hm.put("message", c.getUser().getID() + ":" + chatinput.getText().toString());
            al.add(hm);
            adapter.notifyDataSetChanged();
            chatlist.setSelection(al.size() - 1);
            new Thread(new MyThread0()).start();
        }
    }

    public class listener1 implements View.OnClickListener {
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"), 1);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(ChatActivity.this, "请安装文件管理器", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class EstablishChatSocket implements Runnable {
        public void run() {
            try {
                s = new Socket(c.HOST, c.PORT);
                c.establishChatSocket(s);
                synchronized (this) {
                    try {
                        this.wait(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                l = new Listener(c, s.getInputStream());
                l.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class MyThread0 implements Runnable {
        public void run() {
            c.chat(s, kcs, f.getID(), chatinput.getText().toString());
        }
    }

    class MyThread1 implements Runnable {
        public void run() {
            try {
                c.sendFile(s, kcs, f.getID(), file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                System.out.println(uri);
                String path = getPath(this, uri);
                if (path == null) {
                    Toast.makeText(ChatActivity.this, "请选择文件", Toast.LENGTH_SHORT).show();
                } else {
                    file = new File(path);
                }
            }
            new Thread(new MyThread1()).start();
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

    class Listener extends Thread {
        private InputStream is;
        private Client c;
        private AbstractMessage message;

        public Listener(Client client, InputStream fromServer) {
            this.is = fromServer;
            this.c = client;
        }

        public AbstractMessage getMessage() {
            return message;
        }

        public AbstractMessage receiveMessage() {
            AbstractMessage message = null;
            boolean timeStampValid = false;
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(is);
                Datagram datagram = (Datagram) objectInputStream.readObject();
                if (datagram.getMessageEncryptType() == Datagram.MessageEncryptType.RSA) {
                    message = RSA.decryptMessage(datagram.getCipherMessageBytes(), c.getUser().getPrivateKey());
                    timeStampValid = RSA.checkTimeStamp(message.getCipherTimeStamp(), Dao.getServerPublicKey());
                } else {
                    message = AES.decryptMessage(datagram.getCipherMessageBytes(), kcs);
                    timeStampValid = AES.checkTimeStamp(message.getCipherTimeStamp(), kcs);
                }
            } catch (Exception e) {
                return null;
            }
            if (timeStampValid) {
                return message;
            }
            return null;
        }

        public void run() {
            while (!isInterrupted()) {
                try {
                    message = receiveMessage();
                    if (message != null)
                        HandleDatagram(message);
                } catch (Exception e) {
                    continue;
                }
            }
        }

        public void HandleDatagram(AbstractMessage message) {
            switch (message.getType()) {
                case USER_OFFLINE_RESPONSE:
                    h.sendEmptyMessage(3);
                    break;
                case ACCEPT_ESTABLISH_CHAT_RESPONSE:
                    kcs = ((AcceptEstablishChatResponse) message).getKcs();
                    h.sendEmptyMessage(4);
                    break;
                case REJECT_ESTABLISH_CHAT_RESPONSE:
                    h.sendEmptyMessage(5);
                    break;
                case FORWARD_MESSAGE: {
                    senderID = ((ForwardMessage) message).getSenderID();
                    Datagram datagram = ((ForwardMessage) message).getDatagram();
                    AbstractMessage subMessage = null;
                    boolean timeStampValid = false;
                    try {

                        subMessage = AES.decryptMessage(datagram.getCipherMessageBytes(), Dao.getSessionKeyWithFriendId(c.getUser(), senderID));
                        timeStampValid = AES.checkTimeStamp(subMessage.getCipherTimeStamp(), Dao.getSessionKeyWithFriendId(c.getUser(), senderID));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (timeStampValid) {
                        switch (subMessage.getType()) {
                            case CHAT_MESSAGE: {
                                String m = ((ChatMessage) subMessage).getContent();
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("message", senderID + ":" + m);
                                al.add(hm);
                                h.sendEmptyMessage(0);
                                break;
                            }
                            case FILE_MESSAGE: {
                                fileByte = ((FileMessage) subMessage).getFile();
                                byte[] macCode1 = ((FileMessage) subMessage).getMac();
                                String fileType = ((FileMessage) subMessage).getFileType();
                                filePath = getApplicationContext().getExternalFilesDir(null) + FILE_SAVE_PATH + senderID + FileUtil.generateString(10) + fileType;
                                File dir = new File(getApplicationContext().getExternalFilesDir(null) + FILE_SAVE_PATH);
                                if (!dir.exists())
                                    dir.mkdir();
                                boolean flag = FileUtil.checkMac(Dao.getSessionKeyWithFriendId(c.getUser(), senderID), fileByte, macCode1);
                                if (flag) {
                                    h.sendEmptyMessage(1);
                                } else {
                                    h.sendEmptyMessage(2);
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
}
