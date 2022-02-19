package com.example.chitchat.activities;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;

import com.example.chitchat.adapters.ChatsAdapter;
import com.example.chitchat.databinding.ActivityChatBinding;
import com.example.chitchat.models.ChatMessage;
import com.example.chitchat.models.Users;
import com.example.chitchat.utilities.Constants;
import com.example.chitchat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding chatBinding;
    Users receiverUser;
    String senderId;
    List<ChatMessage> allChatMessages;
    ChatsAdapter chatsAdapter;
    Bitmap bitmap;
    PreferenceManager preferenceManager;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatBinding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(chatBinding.getRoot());
        senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        setListeners();
        loadReceiverDetails();
        setBitmap();

        allChatMessages = new ArrayList<>();
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatsAdapter = new ChatsAdapter(allChatMessages,bitmap,senderId);
        chatBinding.rvChat.setAdapter(chatsAdapter);
        db = FirebaseFirestore.getInstance();

        listenMessages();
    }

    private void listenMessages()
    {
        db.collection(Constants.KEY_COLLECTION_CHATS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .addSnapshotListener(eventListener);

        db.collection(Constants.KEY_COLLECTION_CHATS)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,senderId)
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value,error) ->{
        if(error!=null)
            return;

        if(value!=null)
        {
            int count = allChatMessages.size();
            for(DocumentChange documentChange: value.getDocumentChanges())
            {
                if(documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getFormattedDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.date = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    allChatMessages.add(chatMessage);
                }
            }

            Collections.sort(allChatMessages,(obj1, obj2) -> obj1.date.compareTo(obj2.date));

            if(count == 0)
            {
                chatsAdapter.notifyDataSetChanged();
            }

            else
            {
                chatsAdapter.notifyItemRangeInserted(allChatMessages.size(),allChatMessages.size());
                chatBinding.rvChat.smoothScrollToPosition(allChatMessages.size() - 1);
            }
            chatBinding.rvChat.setVisibility(View.VISIBLE);
        }
        chatBinding.chatProgressBar.setVisibility(View.GONE);
    };
    private void setBitmap()
    {
        byte[] bytes = Base64.decode(receiverUser.image, Base64.DEFAULT);
        bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    private void setListeners() {
        chatBinding.ivChatBack.setOnClickListener(view -> onBackPressed());
        chatBinding.layoutSend.setOnClickListener(view-> sendMessage());
    }

    private void loadReceiverDetails() {
        receiverUser = (Users)getIntent().getSerializableExtra(Constants.KEY_USER);
        chatBinding.tvReceiverName.setText(receiverUser.name);
    }

    private void sendMessage()
    {
        if(!chatBinding.etMessage.getText().toString().equals("") && chatBinding.etMessage.getText()!=null)
        {
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, senderId);
            message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            message.put(Constants.KEY_TIMESTAMP,new Date());
            message.put(Constants.KEY_MESSAGE,chatBinding.etMessage.getText().toString());
            db.collection(Constants.KEY_COLLECTION_CHATS).add(message);
            chatBinding.etMessage.setText(null);
        }

    }

    private String getFormattedDateTime(Date date)
    {
        return new SimpleDateFormat("dd MMM yyyy -hh:mm a", Locale.getDefault()).format(date);
    }
}