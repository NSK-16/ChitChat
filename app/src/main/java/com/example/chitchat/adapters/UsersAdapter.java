package com.example.chitchat.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.databinding.ItemContainerUsersBinding;

import java.util.List;

import com.example.chitchat.listeners.UserListener;
import com.example.chitchat.models.Users;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<Users> allUsers;
    private UserListener userListener;

    public UsersAdapter(List<Users> users,UserListener userListener) {
        allUsers = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUsersBinding binding = ItemContainerUsersBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new UserViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return allUsers.size();
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(allUsers.get(position));
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ItemContainerUsersBinding usersBinding;

        public UserViewHolder(ItemContainerUsersBinding usersBinding) {
            super(usersBinding.getRoot());
            this.usersBinding = usersBinding;
        }

        public void setUserData(Users user)
        {
            usersBinding.tvUserName.setText(user.name);
            usersBinding.tvEmail.setText(user.email);
            usersBinding.rivUserPhoto.setImageBitmap(getUserImage(user.image));
            usersBinding.getRoot().setOnClickListener(view -> userListener.onUserClickListener(user));
        }

        public Bitmap getUserImage(String encodedImage)
        {
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }
    }
}
