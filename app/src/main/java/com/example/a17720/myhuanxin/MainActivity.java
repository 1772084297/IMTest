package com.example.a17720.myhuanxin;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends MyApplication implements View.OnClickListener, EMCallBack,
        EMContactListener, EMMessageListener, ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {

    EditText editText;
    Button addFriend_btn;
    Button logout_btn;

    EditText content_ed;
    ExpandableListView expandableListView;


    Map<String,List<String>> dataset = new HashMap<>();
    private String[] parentList = {"好友列表"};
    List<String> usernames = new ArrayList<>();


    String TAG = "123Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.editText);
        addFriend_btn = findViewById(R.id.add_friend);
        logout_btn = findViewById(R.id.logout);
        content_ed = findViewById(R.id.content);

        expandableListView = findViewById(R.id.expandableListView);

        addFriend_btn.setOnClickListener(this);
        logout_btn.setOnClickListener(this);


//        for (int i=0;i<5;i++){
//            usernames.add("好友"+i);
//        }
//        dataset.put(parentList[0],usernames);
////       设置适配器
//        expandableListView.setAdapter(new MyExpandableListAdapter(MainActivity.this,dataset));




        new Thread(){
            @Override
            public void run() {
                try {
                    Message message = new Message();
                    usernames = EMClient.getInstance().contactManager().getAllContactsFromServer();
                    message.obj = usernames;
                    message.what = 1;
                    handler.sendMessage(message);

                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }.start();

//        设置分组项的点击监听事件
        expandableListView.setOnGroupClickListener(this);
//        设置子选项点击监听事件
        expandableListView.setOnChildClickListener(this);

    }



    private  Handler handler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    usernames = (List<String>) msg.obj;
                    dataset.put(parentList[0],usernames);
//       设置适配器
                    expandableListView.setAdapter(new MyExpandableListAdapter(MainActivity.this,dataset));
                    break;

            }
        }
    };


    //注册监听器
    @Override
    protected void onResume() {
        super.onResume();
        EMClient.getInstance().chatManager().addMessageListener(this);
        EMClient.getInstance().contactManager().setContactListener(this);
    }

    //取消消息接收监听器
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_friend:
                addFriend();
                break;
            case R.id.logout:
                logout();
                break;
        }
    }

    public void addFriend(){
        String toAddUsername  = editText.getText().toString();
        String reason = null;    //添加好友的理由   可以不用实现吧
        try {
            EMClient.getInstance().contactManager().addContact(toAddUsername, reason);
            Log.d("TAG","好友申请已发送");
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    public void logout(){
        EMClient.getInstance().logout(true,this);
    }

    @Override
    public void onSuccess() {
        Log.d(TAG,"退出登陆成功");
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }

    @Override
    public void onError(int i, String s) {
        Log.d(TAG,"退出登陆失败"+i+":"+s);
    }

    @Override
    public void onProgress(int i, String s) {

    }


    public void sendMessage(){
        String toChatUsername = editText.getText().toString();
        String content = content_ed.getText().toString();
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        message.setChatType(EMMessage.ChatType.Chat);
        //发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        Log.d(TAG,"发送消息"+message.getBody().toString());
    }

    // ContactListener的方法 添加好友
    @Override
    public void onContactAdded(String s) {
        Log.d("TAG",s+"好友申请被同意");
    }

    @Override
    public void onContactDeleted(String s) {
        Log.d("TAG",s+"好友请求被拒绝");
    }

    @Override
    public void onContactInvited(String s, String reason) {
        Log.d("TAG",s+"收到好友邀请"+reason);
    }

    @Override
    public void onFriendRequestAccepted(String s) {
        Log.d("TAG",s+"被删除时回调此方法");
    }

    @Override
    public void onFriendRequestDeclined(String s) {
        Log.d("TAG",s+"增加了联系人时回调此方法");
    }


    @Override
    public void onMessageReceived(List<EMMessage> list) {
        String str = " ";
        for (EMMessage message:list){
            str+=message.getBody().toString()+"  ";
        }
        Log.d(TAG,str);
    }

    @Override
    public void onCmdMessageReceived(List<EMMessage> list) {
        //收到透传消息
    }

    @Override
    public void onMessageRead(List<EMMessage> list) {
        //收到已读回执
    }

    @Override
    public void onMessageDelivered(List<EMMessage> list) {
        //收到已送达回执
    }

    @Override
    public void onMessageRecalled(List<EMMessage> list) {
        //消息被撤回
    }

    @Override
    public void onMessageChanged(EMMessage emMessage, Object o) {
        //消息状态变动
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        Toast.makeText(getApplicationContext(), parentList[i], Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        Toast.makeText(getApplicationContext(),dataset.get(parentList[i]).get(i1),Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra("username",dataset.get(parentList[i]).get(i1));
        startActivity(intent);
        return true;
    }


}