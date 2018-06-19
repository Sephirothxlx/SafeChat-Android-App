package com.safechat.safechat.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.safechat.safechat.R;
import com.safechat.safechat.dao.Dao;
import com.safechat.safechat.encryptor.AES;
import com.safechat.safechat.encryptor.RSA;
import com.safechat.safechat.entity.Client;
import com.safechat.safechat.message.AbstractMessage;
import com.safechat.safechat.message.Datagram;
import com.safechat.safechat.message.ForwardMessage;
import com.safechat.safechat.message.FriendListResponse;
import com.safechat.safechat.message.FriendRequest;
import com.safechat.safechat.message.SendSessionKeyRequest;
import com.safechat.safechat.message.UserNotExistResponse;
import com.safechat.safechat.message.UserOfflineResponse;
import com.safechat.safechat.user.Friend;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Sephiroth on 17/5/14.
 */

public class MainActivity extends AppCompatActivity {
    private Button addbutton;
    private ListView listView;
    private ArrayList<Friend> friendList;
    private SimpleAdapter adapter;
    private EditText e;
    private String sender;
    private String receiver;
    private Friend f;
    Client c = (Client) getApplication();
    Listener l;
    Handler h;
    private Key sessionKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addbutton = (Button) findViewById(R.id.main_addbutton);
        e = (EditText) findViewById(R.id.main_friend);
        listView = (ListView) findViewById(R.id.listview);
        listView.setOnItemClickListener(new Listener0());
        addbutton.setOnClickListener(new listener1());
        c = (Client) getApplication();
        c.initDao();
        new Thread(new MyThread0()).start();
        try {
            l = new Listener(c, c.getSocket().getInputStream());
            l.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
                switch (msg.what) {
                    case 0:
                        ArrayList<HashMap<String, String>> ID = new ArrayList();
                        for (int i = 0; i < friendList.size(); i++) {
                            c.getUser().addFriend(friendList.get(i));
                            HashMap<String, String> hm = new HashMap();
                            hm.put("name", friendList.get(i).getID());
                            ID.add(hm);
                        }
                        adapter = new SimpleAdapter(getApplication(), ID, R.layout.friendlist_item, new String[]{"name"}, new int[]{R.id.friendlist_item_ID});
                        listView.setAdapter(adapter);
                        break;
                    case 1:
                        builder.setMessage(sender + "请求加你为好友。");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new MyThread2()).start();
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new MyThread3()).start();
                            }
                        });
                        builder.create().show();
                        break;
                    case 2:
                        builder.setMessage("对方同意了");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new MyThread0()).start();
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    case 3:
                        builder.setMessage("对方拒绝了");
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
                        builder.setMessage(sender + "请求与你建立对话");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new MyThread5()).start();
                                c.getUser().getFriendWithId(sender).setSessionKey(sessionKey);
                                l.interrupt();
                                synchronized (this) {
                                    try {
                                        this.wait(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                Bundle bundle = new Bundle();
                                Friend temp = c.getUser().getFriendWithId(sender);
                                bundle.putSerializable("friend", temp);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new MyThread6()).start();
                            }
                        });
                        builder.create().show();
                        break;
                    case 5:
                        builder.setMessage("对方同意与你建立对话");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                l.interrupt();
                                synchronized (this) {
                                    try {
                                        this.wait(1000);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("friend", f);
                                intent.putExtras(bundle);
                                startActivity(intent);
                            }
                        });
                        builder.create().show();
                        break;
                    case 6:
                        builder.setMessage("对方不同意与你建立对话");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    case 7:
                        builder.setMessage(receiver + "不存在");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    case 8:
                        builder.setMessage(receiver + "离线了");
                        builder.setTitle("提示");
                        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                        break;
                    default:
                        break;
                }
            }

        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        c.close();
        c.closeSocket();
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
    }

    public class Listener0 implements AdapterView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            f = friendList.get(position);
            new Thread(new MyThread4()).start();
        }
    }

    public class listener1 implements View.OnClickListener {
        public void onClick(View v) {
            new Thread(new MyThread1()).start();
        }
    }


    class MyThread0 implements Runnable {
        @Override
        public void run() {
            c.askFriendListRequest();
        }
    }

    class MyThread1 implements Runnable {
        @Override
        public void run() {
            c.friend(e.getText().toString());
        }
    }

    class MyThread2 implements Runnable {
        @Override
        public void run() {
            c.acceptFriend(sender);
            c.askFriendListRequest();
        }
    }

    class MyThread3 implements Runnable {
        @Override
        public void run() {
            c.rejectFriend(sender);
        }
    }

    class MyThread4 implements Runnable {
        @Override
        public void run() {
            c.sendSessionKey(f.getID());
        }
    }

    class MyThread5 implements Runnable {
        @Override
        public void run() {
            c.getUser().getFriendWithId(sender).setSessionKey(sessionKey);
            c.acceptSessionKey(sender);
        }
    }

    class MyThread6 implements Runnable {
        @Override
        public void run() {
            c.rejectSessionKey(sender);
        }
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
                    message = AES.decryptMessage(datagram.getCipherMessageBytes(), c.getKcs());
                    timeStampValid = AES.checkTimeStamp(message.getCipherTimeStamp(), c.getKcs());
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
                case FRIEND_LIST_RESPONSE: {
                    friendList = ((FriendListResponse) message).getFriendList();
                    h.sendEmptyMessage(0);
                    break;
                }

                case FRIEND_REQUEST: {
                    sender = ((FriendRequest) message).getSenderID();
                    h.sendEmptyMessage(1);
                    break;
                }

                case ACCEPT_FRIEND_RESPONSE: {
                    h.sendEmptyMessage(2);
                    break;
                }
                case REJECT_FRIEND_RESPONSE: {
                    h.sendEmptyMessage(3);
                    break;
                }
                case FORWARD_MESSAGE: {
                    sender = ((ForwardMessage) message).getSenderID();
                    Datagram datagram = ((ForwardMessage) message).getDatagram();
                    AbstractMessage subMessage = null;
                    boolean timeStampValid = false;
                    try {
                        if (datagram.getMessageEncryptType() == Datagram.MessageEncryptType.RSA) {
                            subMessage = RSA.decryptMessage(datagram.getCipherMessageBytes(), c.getUser().getPrivateKey());
                            timeStampValid = RSA.checkTimeStamp(subMessage.getCipherTimeStamp(), c.getUser().getFriendWithId(sender).getPublicKey());
                        } else {
                            subMessage = AES.decryptMessage(datagram.getCipherMessageBytes(), Dao.getSessionKeyWithFriendId(c.getUser(), sender));
                            timeStampValid = AES.checkTimeStamp(subMessage.getCipherTimeStamp(), Dao.getSessionKeyWithFriendId(c.getUser(), sender));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (timeStampValid) {
                        switch (subMessage.getType()) {
                            case SEND_SESSION_KEY_REQUEST: {
                                sessionKey = ((SendSessionKeyRequest) subMessage).getSessionKey();
                                h.sendEmptyMessage(4);
                                break;
                            }
                            case ACCEPT_SESSION_KEY_RESPONSE: {
                                h.sendEmptyMessage(5);
                                break;
                            }
                            case REJECT_SESSION_KEY_RESPONSE: {
                                h.sendEmptyMessage(6);
                                break;
                            }
                        }
                    }
                    break;
                }
                case USER_NOT_EXIST_RESPONSE: {
                    receiver = ((UserNotExistResponse) message).getReceiverId();
                    h.sendEmptyMessage(7);
                    break;
                }

                case USER_OFFLINE_RESPONSE: {
                    receiver = ((UserOfflineResponse) message).getReceiverId();
                    h.sendEmptyMessage(8);
                    break;
                }
            }
        }
    }
}
