package com.example.chitchat.activities;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.chitchat.R;
import com.example.chitchat.databinding.ActivityMainBinding;



import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.HashMap;

import utilities.Constants;
import utilities.PreferenceManager;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding mainBinding;
    private PreferenceManager preferenceManager;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    BottomSheetDialog bottomSheetDialog;
    View bottomSheetView;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(R.layout.activity_main);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        setProfilePhoto();
        getToken();
        setListeners();
    }

    private void setProfilePhoto() {

        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        mainBinding.rivProfilePhoto.setImageBitmap(bitmap);
    }

    private void setListeners() {
        mainBinding.rivProfilePhoto.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(MainActivity.this,R.style.BottomSheetDialogTheme);
            bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_bottomsheet_profile, findViewById(R.id.bottomSheetContainer));
            loadUserDetails();
        });

    }

    private void loadUserDetails() {

        RoundedImageView bottomSheetPhoto = bottomSheetView.findViewById(R.id.bottomSheetPhoto);
        bottomSheetPhoto.setImageBitmap(bitmap);

        TextView bottomSheetEmail = bottomSheetView.findViewById(R.id.bottomSheetUserEmail);
        bottomSheetEmail.setText(mAuth.getCurrentUser().getEmail());

        TextView bottomSheetName = bottomSheetView.findViewById(R.id.bottomSheetUserName);
        bottomSheetName.setText(preferenceManager.getString(Constants.KEY_NAME));

        AppCompatImageView signout = bottomSheetView.findViewById(R.id.ivSignOut);

        signout.setOnClickListener(view->{
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
                        .addOnSuccessListener(unused -> {
                            preferenceManager.clear();
                            mAuth.signOut();
                            startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e-> Log.d("Sign Out",e.getMessage()));

                bottomSheetDialog.dismiss();
            });
            builder.setNegativeButton("No",(dialogInterface,i) -> dialogInterface.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
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

//    @Override
//    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//        if(firebaseAuth.getCurrentUser() == null)
//        {
//            HashMap<String,Object> updates = new HashMap<>();
//            updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
//            db.collection(Constants.KEY_COLLECTION_USERS)
//                    .document(mAuth.getCurrentUser().getUid())
//                    .update(updates)
//                    .addOnSuccessListener(unused -> {
//                        preferenceManager.clear();
//                        startActivity(new Intent(getApplicationContext(),SignInActivity.class));
//                        finish();
//                    })
//                    .addOnFailureListener(e-> Log.d("Sign Out",e.getMessage()));
//
//        }
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        FirebaseAuth.getInstance().addAuthStateListener(this);
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        FirebaseAuth.getInstance().removeAuthStateListener(this);
//    }
}