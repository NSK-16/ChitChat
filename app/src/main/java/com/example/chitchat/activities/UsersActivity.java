package com.example.chitchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.chitchat.adapters.UsersAdapter;
import com.example.chitchat.databinding.ActivityUsersBinding;
import com.example.chitchat.listeners.UserListener;
import com.example.chitchat.models.Users;
import com.example.chitchat.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AvailabilityActivity implements UserListener {

    private ActivityUsersBinding usersBinding;
    List<Users> users;
    UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usersBinding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(usersBinding.getRoot());
        setListeners();
        getUsers();
    }

    private void setListeners() {
        usersBinding.ivSelectUserBackButton.setOnClickListener(view-> onBackPressed());
    }


    private void getUsers()
    {
        loading(true);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnSuccessListener(documentSnapshots -> {
                    loading(false);
                    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    users = new ArrayList<>();
                    for(QueryDocumentSnapshot queryDocumentSnapshot: documentSnapshots)
                    {
                        if(queryDocumentSnapshot.getId().equals(currentUserId))
                            continue;

                        Users user = new Users();
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                        user.id = queryDocumentSnapshot.getId();
                        users.add(user);
                    }

                    if(users.isEmpty())
                        showErrorMessage();
                    else
                    {
                        usersAdapter = new UsersAdapter(users, this);
                        usersBinding.rvAllUsers.setLayoutManager(new LinearLayoutManager(this));
                        usersBinding.rvAllUsers.setAdapter(usersAdapter);
                        usersBinding.rvAllUsers.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> showErrorMessage());
    }

    private void showErrorMessage()
    {
        usersBinding.tvErrorMessage.setText(String.format("%s","No users available"));
        usersBinding.tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(boolean isLoading)
    {
        if(isLoading)
            usersBinding.progressBarUsers.setVisibility(View.VISIBLE);
        else
            usersBinding.progressBarUsers.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUserClickListener(Users user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}