package com.example.chitchat.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat.R;
import com.example.chitchat.databinding.ActivityMainBinding;
import com.example.chitchat.databinding.LayoutBottomsheetProfileBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

import utilities.Constants;
import utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener{

    private ActivityMainBinding mainBinding;
    private PreferenceManager preferenceManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    BottomSheetDialog bottomSheetDialog;
    private LayoutBottomsheetProfileBinding bottomSheetProfileBinding;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setProfilePhoto();
        setContentView(mainBinding.getRoot());
        getToken();
        setListeners();
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
}