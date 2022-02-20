package com.example.chitchat.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.chitchat.R;
import com.example.chitchat.adapters.RecentConversationsAdapter;
import com.example.chitchat.databinding.ActivityMainBinding;
import com.example.chitchat.databinding.LayoutBottomsheetProfileBinding;
import com.example.chitchat.listeners.ConversationListener;
import com.example.chitchat.models.ChatMessage;
import com.example.chitchat.models.Users;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.example.chitchat.utilities.Constants;
import com.example.chitchat.utilities.PreferenceManager;

public class MainActivity extends AvailabilityActivity implements FirebaseAuth.AuthStateListener, ConversationListener {

    private ActivityMainBinding mainBinding;
    private PreferenceManager preferenceManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    BottomSheetDialog bottomSheetDialog;
    private LayoutBottomsheetProfileBinding bottomSheetProfileBinding;
    Bitmap bitmap;
    List<ChatMessage> conversations;
    RecentConversationsAdapter recentConversationsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        conversations = new ArrayList<>();
        recentConversationsAdapter = new RecentConversationsAdapter(conversations,this);
        mainBinding.rvRecentConversations.setAdapter(recentConversationsAdapter);

        setProfilePhoto();
        setContentView(mainBinding.getRoot());
        getToken();
        setListeners();
        listenConversations();
    }

    private void setProfilePhoto() {

        try{
            byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            mainBinding.rivProfilePhoto.setImageBitmap(bitmap);
        }
        catch(NullPointerException e)
        {
            mAuth.getCurrentUser().getIdToken(true)
                    .addOnSuccessListener(getTokenResult -> Log.d("NEW TOKEN:" ,getTokenResult.getToken()));
        }

    }

    private void setListeners() {
        mainBinding.rivProfilePhoto.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(MainActivity.this,R.style.BottomSheetDialogTheme);
            bottomSheetProfileBinding = LayoutBottomsheetProfileBinding.inflate(getLayoutInflater());
            bottomSheetDialog.setContentView(bottomSheetProfileBinding.getRoot());
            loadUserDetails();
        });

        mainBinding.fabNewChat.setOnClickListener(view-> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));

    }

    private void showErrorMessage()
    {
        mainBinding.tvNoChatsMessage.setText(String.format("%s","No chats available"));
        mainBinding.tvNoChatsMessage.setVisibility(View.VISIBLE);
    }

    private void loadUserDetails() {

        bottomSheetProfileBinding.bottomSheetPhoto.setImageBitmap(bitmap);
        bottomSheetProfileBinding.bottomSheetUserEmail.setText(mAuth.getCurrentUser().getEmail());
        bottomSheetProfileBinding.bottomSheetUserName.setText(preferenceManager.getString(Constants.KEY_NAME));

        bottomSheetProfileBinding.ivSignOut.setOnClickListener(view->{
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.Theme_Material3_Dark_Dialog_Alert);
            builder.setTitle("Sign Out");
            builder.setMessage("Are you sure you want to sign out?");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", (dialogInterface, i) ->{

                HashMap<String,Object> updates = new HashMap<>();
                updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                db.collection(Constants.KEY_COLLECTION_USERS)
                        .document(mAuth.getCurrentUser().getUid())
                        .update(updates)
                        .addOnSuccessListener(unused -> mAuth.signOut())
                        .addOnFailureListener(e-> Log.d("Sign Out",e.getMessage()));

                bottomSheetDialog.dismiss();
            });
            builder.setNegativeButton("No",(dialogInterface,i) -> dialogInterface.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        });

        bottomSheetDialog.show();
    }

    private void listenConversations()
    {
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID,mAuth.getCurrentUser().getUid())
            .addSnapshotListener(eventListener);

        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,mAuth.getCurrentUser().getUid())
                .addSnapshotListener(eventListener);
    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        if(error!=null)
            return;

        if(value!=null)
        {
            for(DocumentChange documentChange: value.getDocumentChanges())
            {
                if(documentChange.getType() == DocumentChange.Type.ADDED)
                {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if(mAuth.getCurrentUser().getUid().equals(senderId))
                    {
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                    }
                    else
                    {
                        chatMessage.conversationId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        chatMessage.conversationName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversationImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.date = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                }

                else if(documentChange.getType() == DocumentChange.Type.MODIFIED)
                {
                    for(int i=0;i<conversations.size();i++)
                    {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);

                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId))
                        {
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).date = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            if(conversations.isEmpty())
                showErrorMessage();
            else
            {
                Collections.sort(conversations,(obj1,obj2) -> obj2.date.compareTo(obj1.date));
                recentConversationsAdapter.notifyDataSetChanged();
                mainBinding.rvRecentConversations.smoothScrollToPosition(0);
                mainBinding.rvRecentConversations.setVisibility(View.VISIBLE);
                mainBinding.tvNoChatsMessage.setVisibility(View.GONE);
            }

            mainBinding.conversationsProgressBar.setVisibility(View.GONE);
        }
    };

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token)
    {
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS)
                .document(mAuth.getCurrentUser().getUid());
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnSuccessListener(unused -> {

                })
                .addOnFailureListener(e -> Log.d("FCM Token", e.getMessage()));
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        if(firebaseAuth.getCurrentUser() == null)
        {
            preferenceManager.clear();
            startActivity(new Intent(getApplicationContext(),SignInActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onConversationClickListener(Users user) {
        Intent intent = new Intent(MainActivity.this,ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
    }
}