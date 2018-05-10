package com.example.affine.chatapp2;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity implements MqttBrokerManager.MqttBrokerCallback {
    RecyclerView messageList;
    MessageListAdapter messageListAdapter;
    MqttBrokerManager mqttBrokerManager;
    List<DataModel> msgList = Lists.newArrayList();
    EditText chatbox;
    ImageView onlineStatus;
    TextView isTypingStatus;
    FloatingActionButton sendButton;
    Gson gson;
    PreferenceManager preferenceManager;
    String channelId;
    String onlinechannelId;
    String istypingchannelId;
    String listenonlinechannelId;
    String listenistypingchannelId;
    DataManager dataManager;
    Handler isTypingHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = getIntent().getBundleExtra("data");
        final UserInfo userInfo = (UserInfo) extras.getSerializable("user");
        Log.i("userinfo ", userInfo.getName() + userInfo.getId());
        gson = new Gson();
        dataManager = new DataManager(new ApiService.Factory().createService());
        preferenceManager = new PreferenceManager(this);
        final int id = preferenceManager.getId();
        String name = preferenceManager.getName();

        chatbox = findViewById(R.id.chatMessage);
        sendButton = findViewById(R.id.chatSubmit);
        messageList = findViewById(R.id.chatList);
        onlineStatus = findViewById(R.id.onlineStatus);
        isTypingStatus = findViewById(R.id.isTyping);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        messageList.setLayoutManager(linearLayoutManager);
        messageListAdapter = new MessageListAdapter();
        messageListAdapter.setUserId(id);
        messageList.setAdapter(messageListAdapter);
        mqttBrokerManager = new MqttBrokerManager(this, this);
        mqttBrokerManager.connect();

        channelId = createChannelId(id, userInfo.getId());
        istypingchannelId = createistypingchannelId(id, userInfo.getId());
        listenonlinechannelId = createlistenonlinechannelId(userInfo.getId());
        listenistypingchannelId = createlistenistypingchannelId(id, userInfo.getId());
        getSupportActionBar().setTitle(userInfo.getName());

        dataManager.getHistory(channelId).subscribeWith(new HistoryObserver());
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chatbox.getText().toString();
                DataModel dataModel = new DataModel();
                dataModel.setMesgType("message");
                dataModel.setMessage(message);
                dataModel.setMsgId(System.currentTimeMillis());
                dataModel.setUserId(id);
                mqttBrokerManager.publish(channelId, gson.toJson(dataModel));
                chatbox.getText().clear();
            }
        });

        chatbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s)) {
                    //send message of istyping
                    final int id2 = preferenceManager.getId();
                    DataModel dataModel2 = new DataModel();
                    dataModel2.setMesgType("istypingStatus");
                    dataModel2.setMessage("istyping");
                    dataModel2.setMsgId(System.currentTimeMillis());
                    dataModel2.setUserId(id2);
                    mqttBrokerManager.publish(istypingchannelId, gson.toJson(dataModel2));
                    if (updateisTypingStatus != null) {
                        isTypingHandler.removeCallbacks(updateisTypingStatus);
                        isTypingHandler.postDelayed(updateisTypingStatus, 2000);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        android.os.Handler customHandler = new android.os.Handler();
        customHandler.postDelayed(updateTimeThread, 1000);

    }

    private Runnable updateTimeThread = new Runnable() {
        android.os.Handler customHandler = new android.os.Handler();

        @Override
        public void run() {
            postOnlineStatus();
            customHandler.postDelayed(this, 3000);
        }
    };

    private Runnable updateisTypingStatus = new Runnable() {

        @Override
        public void run() {
            postisTypingStatus();
        }
    };

    public void postisTypingStatus() {
        final int id2 = preferenceManager.getId();
        DataModel dataModel2 = new DataModel();
        dataModel2.setMesgType("istypingStatus");
        dataModel2.setMessage("isnottyping");
        dataModel2.setMsgId(System.currentTimeMillis());
        dataModel2.setUserId(id2);
        mqttBrokerManager.publish(istypingchannelId, gson.toJson(dataModel2));
    }

    public void postOnlineStatus() {
        final int id1 = preferenceManager.getId();
        onlinechannelId = createonlineChannelId(id1);
        DataModel dataModel1 = new DataModel();
        dataModel1.setMesgType("onlineStatus");
        dataModel1.setMessage("online");
        dataModel1.setMsgId(System.currentTimeMillis());
        dataModel1.setUserId(id1);
        mqttBrokerManager.publish(onlinechannelId, gson.toJson(dataModel1));

    }

    @Override
    public void onConnectionEstablished(MqttAndroidClient MqttAndroidClient) {
        Log.i("main", "connected " + channelId);
        mqttBrokerManager.receiveMessages();
        mqttBrokerManager.subscribe(channelId);
        mqttBrokerManager.subscribe(listenonlinechannelId);
        mqttBrokerManager.subscribe(listenistypingchannelId);
    }

    @Override
    public void onDisconnected() {
        Log.i("main", "disconnected");

    }

    @Override
    public void onSubscription() {
        Log.i("main", "subscription");

    }

    @Override
    public void onUnSubscription() {
        Log.i("main", "unsubscribe");

    }

    @Override
    public void onMessageReceived(String topic, String message) {
        Log.i("main", "message " + topic + " " + message);
        DataModel dataModel = gson.fromJson(message, DataModel.class);
        if (Strings.areEqual(dataModel.getMesgType(), "message")) {
            msgList.add(dataModel);
            messageListAdapter.setmDataset(msgList);
            messageList.scrollToPosition(msgList.size() - 1);
        }
        if (Strings.areEqual(dataModel.getMesgType(), "onlineStatus")) {
            if (Strings.areEqual(dataModel.getMessage(), "online")) {
                onlineStatus.setBackgroundResource(R.drawable.onlineindicator);
            }
            if (Strings.areEqual(dataModel.getMessage(), "offline")) {
                onlineStatus.setBackgroundResource(R.drawable.circle);
            }
        }
        if (Strings.areEqual(dataModel.getMesgType(), "istypingStatus")) {
            if (Strings.areEqual(dataModel.getMessage(), "istyping")) {
                isTypingStatus.setVisibility(TextView.VISIBLE);
            } else {
                isTypingStatus.setVisibility(TextView.INVISIBLE);
            }
        }
    }

    @Override
    public void onConnectionLost() {
        Log.i("main", "connectedlost");

    }

    @Override
    public void onError(String section, String error) {
        Log.i("main", "error");

    }


    private String createChannelId(int n1, int n2) {
        if (n1 > n2) {
            return "PQ/" + n2 + "" + n1;
        }
        return "PQ/" + n1 + "" + n2;
    }

    private String createistypingchannelId(int n1, int n2) {
        if (n1 > n2) {
            return "PQ/" + n2 + "" + n1 + "istyping" + n1;
        }
        return "PQ/" + n1 + "" + n2 + "istyping" + n1;
    }

    private String createlistenistypingchannelId(int n1, int n2) {
        if (n1 > n2) {
            return "PQ/" + n2 + "" + n1 + "istyping" + n2;
        }
        return "PQ/" + n1 + "" + n2 + "istyping" + n2;
    }

    private String createlistenonlinechannelId(int n1) {
        return "PQ/" + n1 + "onlinestatus";
    }

    private String createonlineChannelId(int n1) {
        return "PQ/" + n1 + "onlinestatus";
    }


    class HistoryObserver implements SingleObserver<Response<List<HistoryResponseItem>>> {

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onSuccess(Response<List<HistoryResponseItem>> listResponse) {
            List<HistoryResponseItem> data = listResponse.getData();
            for (HistoryResponseItem item : data) {
                String json = item.getMessage();
//                Log.i("data ", json);
                try {
                    DataModel dataModel = gson.fromJson(json, DataModel.class);
//                Log.i("data ", dataModel.getMesgType());
//                Log.i("data ", dataModel.getMessage());
//                Log.i("data ", "" + dataModel.getUserId());
                    msgList.add(dataModel);
//                    Collections.reverse(msgList);
                } catch (Throwable throwable) {

                }
            }
            messageListAdapter.setmDataset(msgList);
            messageList.scrollToPosition(msgList.size() - 1);
        }

        @Override
        public void onError(Throwable e) {

        }
    }
}

