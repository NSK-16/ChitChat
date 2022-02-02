package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chitchat.R;
import com.example.chitchat.activities.SignUpActivity;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImagesViewHolder>{

    private final ArrayList<Integer> allImages = new ArrayList<>();

    //Adding all default avatars
    public ImagesAdapter() {

        allImages.add(R.drawable.avatar1);
        allImages.add(R.drawable.avatar2);
        allImages.add(R.drawable.avatar3);
        allImages.add(R.drawable.avatar4);
        allImages.add(R.drawable.avatar5);
        allImages.add(R.drawable.avatar6);
        allImages.add(R.drawable.avatar7);
        allImages.add(R.drawable.avatar8);
        allImages.add(R.drawable.avatar9);
        allImages.add(R.drawable.avatar10);
    }

    @NonNull
    @Override
    public ImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View imageItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_images,parent,false);
        return new ImagesViewHolder(imageItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ImagesViewHolder holder, int position) {
        holder.roundedImageView.setImageResource(allImages.get(position));
    }

    @Override
    public int getItemCount() {
        return allImages.size();
    }

    class ImagesViewHolder extends RecyclerView.ViewHolder{
        RoundedImageView roundedImageView;

        public ImagesViewHolder(@NonNull View itemView) {
            super(itemView);
            roundedImageView =  itemView.findViewById(R.id.rivAvatar);

            itemView.setOnClickListener(view -> {
                SignUpActivity.getInstance().pickImage(allImages.get(getAdapterPosition()));
            });
        }
    }
}