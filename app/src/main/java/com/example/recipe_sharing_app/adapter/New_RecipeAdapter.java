package com.example.recipe_sharing_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.RecipeDetailActivity;
import com.example.recipe_sharing_app.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class New_RecipeAdapter extends RecyclerView.Adapter<New_RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private Context context;

    public New_RecipeAdapter(List<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
    }
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_new, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // Set data cho món ăn
        holder.tvRecipeName.setText(recipe.getName());
        holder.tvRecipeAuthor.setText(recipe.getAuthorName());

        // Tải hình ảnh món ăn bằng Glide
        Glide.with(context).load(recipe.getImageUrl()).into(holder.imgRecipeImage);

        // Lấy ảnh đại diện của tác giả từ Firebase và truyền vào intent
        getAuthorImage(recipe.getAuthorName(), holder.ivAuthorAvatar, authorImageUrl -> {
            holder.imgRecipeImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipe.getRecipeId());
                intent.putExtra("authorImageUrl", authorImageUrl);
                context.startActivity(intent);
            });
        });

        // Xử lý nút lưu
        // Xử lý nút lưu
        holder.btnSave.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            boolean isGuest = sharedPreferences.getBoolean("isGuest", false); // Kiểm tra trạng thái là khách

            if (isGuest) {
                Toast.makeText(context, "Bạn phải đăng nhập để lưu công thức này.", Toast.LENGTH_SHORT).show();
                return;
            }

            isRecipeSavedForLater(recipe.getRecipeId(), isSaved -> {
                if (isSaved) {
                    Toast.makeText(context, "Bạn đã lưu công thức này rồi!", Toast.LENGTH_SHORT).show();
                } else {
                    updateSaveForLater(recipe);
                    Toast.makeText(context, "Công thức đã được lưu!", Toast.LENGTH_SHORT).show();
                }
            });
        });

// Xử lý nút yêu thích
        holder.ivEmotion.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            boolean isGuest = sharedPreferences.getBoolean("isGuest", false); // Kiểm tra trạng thái là khách

            if (isGuest) {
                Toast.makeText(context, "Bạn phải đăng nhập để yêu thích công thức này.", Toast.LENGTH_SHORT).show();
                return;
            }

            isRecipeLiked(recipe.getRecipeId(), isLiked -> {
                if (isLiked) {
                    Toast.makeText(context, "Bạn đã thích công thức này rồi!", Toast.LENGTH_SHORT).show();
                } else {
                    updateFavorites(recipe);
                    Toast.makeText(context, "Công thức đã được thích!", Toast.LENGTH_SHORT).show();
                }
            });
        });


        // Cập nhật số lượng yêu thích
        updateFavoriteCount(recipe.getRecipeId(), holder.tvEmotionCount);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipeName, tvRecipeAuthor, tvEmotionCount;
        ImageView imgRecipeImage, ivAuthorAvatar;
        ImageButton btnSave;
        ImageView ivEmotion;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeAuthor = itemView.findViewById(R.id.tvRecipeAuthor);
            imgRecipeImage = itemView.findViewById(R.id.imgRecipeImage);
            ivAuthorAvatar = itemView.findViewById(R.id.ivAuthorAvatar);
            btnSave = itemView.findViewById(R.id.btnSave);
            ivEmotion = itemView.findViewById(R.id.ivEmotion);
            tvEmotionCount = itemView.findViewById(R.id.tvEmotionCount);
        }
    }

    // Phương thức lấy ID người dùng hiện tại
    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    // Phương thức cập nhật 'Favorites' trong Firebase
    private void updateFavorites(Recipe recipe) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("Favorites")
                .child(recipe.getRecipeId());

        String userId = getCurrentUserId();
        favoritesRef.child(userId).setValue(true);
    }

    // Phương thức cập nhật 'Save for Later' trong Firebase
    private void updateSaveForLater(Recipe recipe) {
        DatabaseReference saveForLaterRef = FirebaseDatabase.getInstance()
                .getReference("SaveForLater")
                .child(recipe.getRecipeId());

        String userId = getCurrentUserId();
        saveForLaterRef.child(userId).setValue(true);
    }

    // Phương thức kiểm tra công thức đã được lưu chưa
    private void isRecipeSavedForLater(String recipeId, OnCheckCallback callback) {
        DatabaseReference saveForLaterRef = FirebaseDatabase.getInstance()
                .getReference("SaveForLater")
                .child(recipeId);

        String userId = getCurrentUserId();
        saveForLaterRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onCheck(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    // Phương thức kiểm tra công thức đã được yêu thích chưa
    private void isRecipeLiked(String recipeId, OnCheckCallback callback) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("Favorites")
                .child(recipeId);

        String userId = getCurrentUserId();
        favoritesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onCheck(dataSnapshot.exists());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    // Phương thức cập nhật số lượng yêu thích
    private void updateFavoriteCount(String recipeId, TextView tvEmotionCount) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("Favorites")
                .child(recipeId);

        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tvEmotionCount.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    // Callback interface để kiểm tra trạng thái
    public interface OnCheckCallback {
        void onCheck(boolean exists);
    }

    // Phương thức lấy ảnh đại diện của tác giả từ bảng Users dựa trên authorName và trả về thông qua callback
    private void getAuthorImage(String authorName, ImageView pAuthorImage, OnAuthorImageLoadedCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("fullName").equalTo(authorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String authorImageUrl = userSnapshot.child("images").getValue(String.class);
                        if (authorImageUrl != null) {
                            Glide.with(context).load(authorImageUrl).into(pAuthorImage);
                            callback.onAuthorImageLoaded(authorImageUrl);
                        }
                    }
                }
            }
            @Override
            public  void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
    // Callback interface để truyền URL ảnh
    public interface OnAuthorImageLoadedCallback {
        void onAuthorImageLoaded(String authorImageUrl);
    }
}
