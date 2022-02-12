package com.example.chitchat.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.databinding.ItemContainerReceivedMessageBinding;
import com.example.chitchat.databinding.ItemContainerSentMessageBinding;
import com.example.chitchat.models.ChatMessage;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverImage;
    private final String senderId;

    private static final int VIEW_TYPE_SEND = 1;
    private static final int VIEW_TYPE_RECEIVER = 2;

    public ChatsAdapter(List<ChatMessage> chatMessages, Bitmap receiverImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverImage = receiverImage;
        this.senderId = senderId;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SEND)
            return new SenderViewHolder(ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        else
            return new ReceiverViewHolder(ItemContainerReceivedMessageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SEND)
            ((SenderViewHolder) holder).setData(chatMessages.get(position));
        else
            ((ReceiverViewHolder) holder).setData(chatMessages.get(position),receiverImage);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId))
            return VIEW_TYPE_SEND;
        else
            return VIEW_TYPE_RECEIVER;
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder{

        private ItemContainerSentMessageBinding sentMessageBinding;

        public SenderViewHolder(ItemContainerSentMessageBinding sentMessageBinding) {
            super(sentMessageBinding.getRoot());
            this.sentMessageBinding = sentMessageBinding;
        }

        private void setData(ChatMessage chatMessage)
        {
            sentMessageBinding.tvSentMessage.setText(chatMessage.message);
            sentMessageBinding.tvDateTime.setText(chatMessage.dateTime);
        }
    }

   static class ReceiverViewHolder extends RecyclerView.ViewHolder{

        ItemContainerReceivedMessageBinding receivedMessageBinding;
        public ReceiverViewHolder(ItemContainerReceivedMessageBinding receivedMessageBinding){
            super(receivedMessageBinding.getRoot());
            this.receivedMessageBinding = receivedMessageBinding;
        }

        private void setData(ChatMessage chatMessage, Bitmap bitmap)
        {
            receivedMessageBinding.tvReceivedMessage.setText(chatMessage.message);
            receivedMessageBinding.tvDateTime.setText(chatMessage.dateTime);
            receivedMessageBinding.rivReceiverImage.setImageBitmap(bitmap);
        }
    }
}
