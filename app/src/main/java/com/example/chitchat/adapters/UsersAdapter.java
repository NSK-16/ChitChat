package adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.databinding.RvUsersListBinding;

import java.util.List;

import models.Users;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    List<Users> allUsers;

    public UsersAdapter(List<Users> users) {
        allUsers = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RvUsersListBinding binding = RvUsersListBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
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

    static class UserViewHolder extends RecyclerView.ViewHolder{

        RvUsersListBinding usersListBinding;
        public UserViewHolder(RvUsersListBinding usersListBinding) {
            super(usersListBinding.getRoot());
            this.usersListBinding = usersListBinding;
        }

        public void setUserData(Users user)
        {
            usersListBinding.tvUserName.setText(user.name);
            usersListBinding.tvEmail.setText(user.email);
            usersListBinding.rivUserPhoto.setImageBitmap(getUserImage(user.image));
        }

        public Bitmap getUserImage(String encodedImage)
        {
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }
    }
}
