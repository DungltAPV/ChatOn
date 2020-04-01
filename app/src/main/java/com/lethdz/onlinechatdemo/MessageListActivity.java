package com.lethdz.onlinechatdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.lethdz.onlinechatdemo.dao.FirebaseDAO;
import com.lethdz.onlinechatdemo.modal.ChatRoom;
import com.lethdz.onlinechatdemo.modal.RoomMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageListActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    public RecyclerView.Adapter adapter;
    private List<RoomMessage> messageList = new ArrayList<>();
    private static ChatRoom chatRoom;
    RecyclerView.LayoutManager layoutManager;
    private FirebaseDAO firebaseDAO = new FirebaseDAO();
    private String documentName;
    public static Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        setupAdapter();
        Intent intent = getIntent();
        this.documentName = intent.getStringExtra("DOCUMENT_NAME");
        getChatRoom(documentName, messageList);
        activity = this;
    }

    private void getChatRoom(String documentName, List<RoomMessage> messages) {
        firebaseDAO.getRoomMessage(documentName, messages, adapter, this);
    }

    public void sendMessage(View view) {
        EditText txtMessage = findViewById(R.id.edittext_chatbox);
        if (!txtMessage.getText().toString().equals("")) {
            firebaseDAO.sendMessage(this.documentName, txtMessage.getText().toString(), messageList);
            txtMessage.getText().clear();
        }
    }

    public void backToHome(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    public static void setChatRoom(ChatRoom chatRoom) {
        MessageListActivity.chatRoom = chatRoom;
    }

    public static ChatRoom getChatRoom() {
        return chatRoom;
    }

    private void setupAdapter() {
        recyclerView = findViewById(R.id.reyclerview_message_list);
        adapter = new MessageListViewAdapter(messageList);
        layoutManager = new LinearLayoutManager(this);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if(adapter.getItemCount() != 0) {
                    layoutManager.smoothScrollToPosition(recyclerView, null, adapter.getItemCount() - 1);
                }
                super.onChanged();
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }
}
