package com.example.chitchat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.databinding.ItemContainerRecentConversationsBinding;
import com.example.chitchat.listeners.ConversationListener;
import com.example.chitchat.models.ChatMessage;
import com.example.chitchat.models.Users;

import java.util.List;

public class RecentConversationsAdapter extends RecyclerView.Adapter<RecentConversationsAdapter.ConversationHolder> {

    List<ChatMessage> chatMessages;
    ConversationListener conversationListener;

    public RecentConversationsAdapter(List<ChatMessage> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversationHolder(ItemContainerRecentConversationsBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversationHolder extends RecyclerView.ViewHolder{

        ItemContainerRecentConversationsBinding recentConversationsBinding;

        public ConversationHolder(ItemContainerRecentConversationsBinding binding) {
            super(binding.getRoot());
            recentConversationsBinding = binding;
        }

        void setData(ChatMessage chatMessage)
        {
            recentConversationsBinding.tvConversationUserName.setText(chatMessage.conversationName);
            recentConversationsBinding.tvLastMessage.setText(chatMessage.message);
            recentConversationsBinding.rivConversationUserPhoto.setImageBitmap(getBitmap(chatMessage.conversationImage));
            recentConversationsBinding.getRoot().setOnClickListener(v->{
                Users user = new Users();
                user.id = chatMessage.conversationId;
                user.name = chatMessage.conversationName;
                user.image = chatMessage.conversationImage;
                conversationListener.onConversationClickListener(user);
            });
        }

        Bitmap getBitmap(String bitmap)
        {
            byte[] bytes = Base64.decode(bitmap,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }
    }
}
