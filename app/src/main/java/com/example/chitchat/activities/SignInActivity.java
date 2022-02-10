package com.example.chitchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chitchat.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import utilities.Constants;
import utilities.PreferenceManager;

interface MyCallBack{
    void onSuccessCallIntent();
}

public class SignInActivity extends AppCompatActivity{

    private ActivitySignInBinding signInBinding;
    private PreferenceManager preferenceManager;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());

        user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        else
        {
            signInBinding = ActivitySignInBinding.inflate(getLayoutInflater());
            setContentView(signInBinding.getRoot());
            setListeners();
        }

    }

    private void setListeners()
    {
        signInBinding.tvSignInCreateNewAccount.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));

        signInBinding.buttonSignIn.setOnClickListener(v->
        {
            if(isSignInDetailsValid())
                signIn();
        });
    }

    private void signIn() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        loading(true);
        mAuth.signInWithEmailAndPassword(signInBinding.etSignInEmail.getText().toString().trim(),signInBinding.etSignInPassword.getText().toString().trim())
                .addOnSuccessListener(authResult -> {

                    FirebaseFirestore.getInstance().collection(Constants.KEY_COLLECTION_USERS)
                            .document(authResult.getUser().getUid())
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                SignInActivity.this.loading(false);
                                preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                                preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                                MyCallBack myCallBack = () -> {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    SignInActivity.this.startActivity(intent);
                                };
                                myCallBack.onSuccessCallIntent();

                            });

                })
                .addOnFailureListener(e-> {
                    loading(false);
                    showToast("Unable to sign in!");
                });



    }

    private boolean isSignInDetailsValid() {
        if(signInBinding.etSignInEmail.getText().toString().trim().isEmpty())
        {
            signInBinding.etSignInEmail.requestFocus();
            showToast("Enter email");
            return false;
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(signInBinding.etSignInEmail.getText().toString().trim()).matches())
        {
            signInBinding.etSignInEmail.requestFocus();
            showToast("Enter valid email");
            return false;
        }

        else if(signInBinding.etSignInPassword.getText().toString().trim().isEmpty())
        {
            signInBinding.etSignInPassword.requestFocus();
            showToast("Enter password");
            return false;
        }

        return true;
    }

    private void showToast(String message)
    {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void loading(boolean isLoading)
    {
        if(isLoading)
        {
            signInBinding.buttonSignIn.setVisibility(View.INVISIBLE);
            signInBinding.progressBar.setVisibility(View.VISIBLE);
        }
        else
        {
            signInBinding.buttonSignIn.setVisibility(View.VISIBLE);
            signInBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}