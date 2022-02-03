package com.example.chitchat.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.example.chitchat.R;
import com.example.chitchat.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

import adapters.ImagesAdapter;
import utilities.Constants;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding signUpBinding;
    private String encodedImage;
    private Dialog pickImageDialog;
    private static SignUpActivity INSTANCE;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        INSTANCE = this;
        signUpBinding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(signUpBinding.getRoot());
        setListeners();
    }

    public static SignUpActivity getInstance()
    {
        return INSTANCE;
    }

    private void setListeners()
    {
        signUpBinding.tvSignIn.setOnClickListener(v->onBackPressed());
        signUpBinding.buttonSignUp.setOnClickListener(v->{
            if(isSignUpDetailsValid())
                signUp();
        });
        signUpBinding.ivProfilePicture.setOnClickListener(v->{
            showImageDialog();
        });
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void signUp()
    {
        loading(true);
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(signUpBinding.etSignUpEmail.getText().toString().trim(),signUpBinding.etSignUpPassword.getText().toString().trim())
        .addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                HashMap<String,Object> user = new HashMap<>();
                user.put(Constants.KEY_NAME,signUpBinding.etSignUpName.getText().toString().trim());
                user.put(Constants.KEY_EMAIL,signUpBinding.etSignUpEmail.getText().toString().trim());
                user.put(Constants.KEY_IMAGE,encodedImage);

                db.collection(Constants.KEY_COLLECTION_USERS)
                        .document(mAuth.getCurrentUser().getUid())
                        .set(user)
                        .addOnSuccessListener(documentReference -> {
                            loading(false);
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .addOnFailureListener(exception ->{
                            loading(false);
                            showToast(exception.getMessage());
                        });
            }
            else
            {
                loading(false);
                showToast("Failed to sign up!");
            }

        });

    }

    private String encodeImage(Bitmap bitmap)
    {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    public void showImageDialog()
    {
        pickImageDialog  = new Dialog(SignUpActivity.this);
        pickImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pickImageDialog.setCanceledOnTouchOutside(false);
        pickImageDialog.setContentView(R.layout.dialog_pick_image);


        RecyclerView recyclerView = pickImageDialog.findViewById(R.id.rvImages);
        ImagesAdapter imagesAdapter = new ImagesAdapter();
        recyclerView.setAdapter(imagesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));

        pickImageDialog.show();
    }

    public void pickImage(int imageId)
    {

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),imageId);
        signUpBinding.ivProfilePicture.setImageBitmap(bitmap);
        encodedImage = encodeImage(bitmap);
        signUpBinding.tvAddImage.setVisibility(View.GONE);
        signUpBinding.ivProfilePicture.setBackgroundColor(Color.parseColor("#0096FF"));
        pickImageDialog.dismiss();
    }

    private void loading(boolean isLoading)
    {
        if(isLoading)
        {
            signUpBinding.buttonSignUp.setVisibility(View.INVISIBLE);
            signUpBinding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            signUpBinding.buttonSignUp.setVisibility(View.VISIBLE);
            signUpBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }


    private boolean isSignUpDetailsValid()
    {
        if(encodedImage == null){
            showToast("Select an avatar");
            return false;
        }

        else if(signUpBinding.etSignUpName.getText().toString().trim().isEmpty())
        {
            signUpBinding.etSignUpName.requestFocus();
            showToast("Enter name");
            return false;
        }

        else if(signUpBinding.etSignUpEmail.getText().toString().trim().isEmpty())
        {
            signUpBinding.etSignUpEmail.requestFocus();
            showToast("Enter email");
            return false;
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(signUpBinding.etSignUpEmail.getText().toString().trim()).matches())
        {
            signUpBinding.etSignUpEmail.requestFocus();
            showToast("Enter valid email");
            return false;
        }

        else if(signUpBinding.etSignUpPassword.getText().toString().trim().isEmpty())
        {
            signUpBinding.etSignUpPassword.requestFocus();
            showToast("Enter password");
            return false;
        }

        else if(signUpBinding.etSignUpConfirmPassword.getText().toString().trim().isEmpty())
        {
            signUpBinding.etSignUpConfirmPassword.requestFocus();
            showToast("Confirm your password");
            return false;
        }

        else if(!(signUpBinding.etSignUpPassword.getText().toString().trim().equals(signUpBinding.etSignUpConfirmPassword.getText().toString().trim())))
        {
            signUpBinding.etSignUpConfirmPassword.requestFocus();
            showToast("Password must match confirm password");
            return false;
        }

        else
            return true;
    }
}