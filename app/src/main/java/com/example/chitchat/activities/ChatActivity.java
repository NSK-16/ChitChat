package com.example.chitchat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;


import androidx.annotation.NonNull;
import com.example.chitchat.adapters.ChatsAdapter;
import com.example.chitchat.apiUtilities.ApiService;
import com.example.chitchat.apiUtilities.RetrofitClient;
import com.example.chitchat.databinding.ActivityChatBinding;
import com.example.chitchat.models.ChatMessage;
import com.example.chitchat.models.Users;
import com.example.chitchat.utilities.Constants;
import com.example.chitchat.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AvailabilityActivity {

    ActivityChatBinding chatBinding;
    Users receiverUser;
    String senderId;
    List<ChatMessage> allChatMessages;
    ChatsAdapter chatsAdapter;
    Bitmap bitmap;
    PreferenceManager preferenceManager;
    FirebaseFirestore db;
    String conversationId;
    boolean isReceiverAvailable = false;

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
        if(conversationId == null)
            checkForConversation();
    };

    private void listenForReceiverAvailability()
    {
        db.collection(Constants.KEY_COLLECTION_USERS)
                .document(receiverUser.id)
                .addSnapshotListener(ChatActivity.this, (value, error) -> {
                    if(error!=null)
                        return;

                    if(value!=null)
                    {
                        if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                            int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)).intValue();
                            isReceiverAvailable = availability == 1;
                        }
                        receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);

                        if(receiverUser.image == null)
                        {
                            receiverUser.image = value.getString(Constants.KEY_IMAGE);
                            setBitmap();
                            chatsAdapter.setReceiverImage(bitmap);
                            chatsAdapter.notifyDataSetChanged();
                        }
                    }
                    if(isReceiverAvailable)
                    {
                        chatBinding.onlineStatus.setVisibility(View.VISIBLE);
                    }
                    else {
                        chatBinding.onlineStatus.setVisibility(View.GONE);
                    }

                });

    }

    private void setBitmap()
    {
        if(receiverUser.image!=null)
        {
            byte[] bytes = Base64.decode(receiverUser.image, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }
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
        if(!chatBinding.etMessage.getText().toString().trim().equals("") && chatBinding.etMessage.getText()!=null)
        {
            HashMap<String, Object> message = new HashMap<>();
            message.put(Constants.KEY_SENDER_ID, senderId);
            message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            message.put(Constants.KEY_TIMESTAMP,new Date());
            message.put(Constants.KEY_MESSAGE,chatBinding.etMessage.getText().toString().trim());
            db.collection(Constants.KEY_COLLECTION_CHATS).add(message);

            if(conversationId != null)
                updateConversation(chatBinding.etMessage.getText().toString().trim());
            else
            {
                HashMap<String,Object> conversation = new HashMap<>();
                conversation.put(Constants.KEY_SENDER_ID,senderId);
                conversation.put(Constants.KEY_SENDER_NAME,preferenceManager.getString(Constants.KEY_NAME));
                conversation.put(Constants.KEY_SENDER_IMAGE,preferenceManager.getString(Constants.KEY_IMAGE));
                conversation.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
                conversation.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
                conversation.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
                conversation.put(Constants.KEY_TIMESTAMP,new Date());
                conversation.put(Constants.KEY_LAST_MESSAGE,chatBinding.etMessage.getText().toString().trim());
                addConversation(conversation);
            }

            if(!isReceiverAvailable)
            {
                try {
                    JSONArray tokens = new JSONArray();
                    tokens.put(receiverUser.token);

                    JSONObject data = new JSONObject();
                    data.put("userId",FirebaseAuth.getInstance().getCurrentUser().getUid());
                    data.put(Constants.KEY_NAME,preferenceManager.getString(Constants.KEY_NAME));
                    data.put(Constants.KEY_FCM_TOKEN,preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                    data.put(Constants.KEY_MESSAGE,chatBinding.etMessage.getText().toString().trim());

                    JSONObject body = new JSONObject();
                    body.put(Constants.REMOTE_MSG_DATA,data);
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                    sendNotification(body.toString());
                }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }
            chatBinding.etMessage.setText(null);
        }

    }

    private String getFormattedDateTime(Date date)
    {
        return new SimpleDateFormat("dd MMM yyyy -hh:mm a", Locale.getDefault()).format(date);
    }

    private void checkForConversation()
    {
        if(!allChatMessages.isEmpty())
        {
            fetchConversations(senderId,receiverUser.id);
            fetchConversations(receiverUser.id, senderId);
        }
    }

    private void fetchConversations(String senderId, String receiverId)
    {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverId)
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()  && task.getResult()!=null && task.getResult().getDocuments().size() > 0)
                    {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        conversationId = documentSnapshot.getId();
                    }
                });
    }

    private void addConversation(HashMap<String,Object> conversation)
    {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message)
    {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .document(conversationId)
                .update(Constants.KEY_LAST_MESSAGE,message,Constants.KEY_TIMESTAMP,new Date());
    }

    private void sendNotification(String messageBody)
    {
        RetrofitClient.getInstance().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful())
                {
                    if (response.body()!=null)
                    {
                        try
                        {   JSONObject responseJson = new JSONObject();
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure") == 1 )
                            {
                                JSONObject error = new JSONObject(response.body());
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenForReceiverAvailability();
    }
}