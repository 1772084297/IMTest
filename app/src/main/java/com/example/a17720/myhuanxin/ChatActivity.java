package com.example.a17720.myhuanxin;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener, EMMessageListener {


    EditText content_ed;

    Button sendMessage_btn;
    ListView msgListView;
    List<Msg> msgList = new ArrayList<>();
    MsgAdapter msgAdapter;



    String toChatUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        content_ed = findViewById(R.id.content);
        sendMessage_btn = findViewById(R.id.send_message);

        new Thread(){
            @Override
            public void run() {
                Message message = new Message();
                message.obj = msgList;
                message.what = 2;
                handler.sendMessage(message);
            }
        }.run();


        msgAdapter= new MsgAdapter(ChatActivity.this, R.layout.msg_item, msgList);
        msgListView = findViewById(R.id.msg_list_view);
        msgListView.setAdapter(msgAdapter);
        sendMessage_btn.setOnClickListener(this);

        toChatUsername  = getIntent().getStringExtra("username");
        if (toChatUsername!=null){
            setTitle(toChatUsername);
        }

        //设置接收消息监听器
        EMClient.getInstance().chatManager().addMessageListener(this);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.send_message:
                sendMessage();
                break;
        }
    }

    public void sendMessage(){
        String content = content_ed.getText().toString();
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        EMClient.getInstance().chatManager().sendMessage(message);

        Msg msg = new Msg(content,Msg.TYPE_SENT);
        msgList.add(msg);
        msgAdapter.notifyDataSetChanged(); // 当有新消息时，刷新 ListView 中的显示
        msgListView.setSelection(msgList.size()); // 将 ListView 定位到最后一行
        content_ed.setText("");//清空输入框

    }


    //接收消息
    @Override
    public void onMessageReceived(List<EMMessage> list) {
        for (EMMessage message:list){
            Msg msg = new Msg(parseMessage(message),Msg.TYPE_RECEIVED);
            msgList.add(msg);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                msgAdapter.notifyDataSetChanged(); // 当有新消息时，刷新 ListView 中的显示
                msgListView.setSelection(msgList.size()); // 将 ListView 定位到最后一行
                content_ed.setText("");
            }
        });
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 2:
                    try{
                        //java.lang.NullPointerException: Attempt to invoke virtual method 'java.util.List com.hyphenate.chat.EMConversation.getAllMessages()' on a null object reference
                        EMConversation conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername);
                        List<EMMessage> messages = conversation.getAllMessages();
                        for (EMMessage message:messages){
                            Msg msg1 = new Msg(parseMessage(message),message.getFrom().equals(toChatUsername)?Msg.TYPE_RECEIVED:Msg.TYPE_SENT);
                            msgList.add(msg1);
                        }
                    }catch (Exception e){
                        Log.d("TAG",e.toString());
                    }
                    break;
            }
        }
    };

    @Override
    public void onCmdMessageReceived(List<EMMessage> list) {

    }

    @Override
    public void onMessageRead(List<EMMessage> list) {

    }

    @Override
    public void onMessageDelivered(List<EMMessage> list) {

    }

    @Override
    public void onMessageRecalled(List<EMMessage> list) {

    }

    @Override
    public void onMessageChanged(EMMessage emMessage, Object o) {

    }

    public String parseMessage(EMMessage message){
        String str = message.getBody().toString();
        String[] strings = str.split("\"");
        return strings[1];
    }

}
