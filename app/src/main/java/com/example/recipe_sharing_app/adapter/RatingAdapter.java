package com.example.recipe_sharing_app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.model.Rating;
import com.example.recipe_sharing_app.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RatingAdapter extends RecyclerView.Adapter<RatingAdapter.RatingViewHolder> {
    private List<Rating> ratingList;

    public RatingAdapter(List<Rating> ratingList) {
        this.ratingList = ratingList;
    }

    @NonNull
    @Override
    public RatingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rating, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RatingViewHolder holder, int position) {
        Rating rating = ratingList.get(position);

        // Lấy thông tin người dùng dựa trên userId
        fetchUserInfo(rating.getUserId(), holder);
        holder.viewRatingBar.setRating(rating.getRating()); // Cập nhật điểm đánh giá
    }

    @Override
    public int getItemCount() {
        return ratingList.size();
    }

    static class RatingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserAvatar;
        TextView txtUserName;
        RatingBar viewRatingBar;

        public RatingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            viewRatingBar = itemView.findViewById(R.id.viewRatingBar);
        }
    }

    private void fetchUserInfo(String userId, RatingViewHolder holder) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        holder.txtUserName.setText(user.getFullName());
                        // Tải ảnh người dùng
                        Glide.with(holder.itemView.getContext())
                                .load(user.getImages())
                                .into(holder.imgUserAvatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RatingAdapter", "Error fetching user info", error.toException());
            }
        });
    }
}
