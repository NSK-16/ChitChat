package com.example.chitchat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.chitchat.databinding.ActivityChatBinding;
import com.example.chitchat.models.Users;
import com.example.chitchat.utilities.Constants;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding chatBinding;
    Users receiverUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(chatBinding.getRoot());
        setListeners();
        loadReceiverDetails();
    }

    private void setListeners() {
        chatBinding.ivChatBack.setOnClickListener(view -> onBackPressed());
    }

    private void loadReceiverDetails() {
        receiverUser = (Users)getIntent().getSerializableExtra(Constants.KEY_USER);
        chatBinding.tvReceiverName.setText(receiverUser.name);
    }
}