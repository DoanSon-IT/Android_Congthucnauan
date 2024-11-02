package com.example.recipe_sharing_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class All_RecipeAdapter extends RecyclerView.Adapter<All_RecipeAdapter.RecipeViewHolder> {
    private List<Recipe> recipeList;
    private Context context;
    private String currentUserId;

    public All_RecipeAdapter(List<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Lấy ID người dùng hiện tại
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_all, parent, false);
        return new RecipeViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // Thiết lập tên tiêu đề và tên tác giả
        holder.ptvTitle.setText(recipe.getName());
        holder.ptvAuthor.setText(recipe.getAuthorName());

        // Tải hình ảnh của công thức bằng Glide
        Glide.with(context).load(recipe.getImageUrl()).into(holder.pImage);

        // Tải ảnh đại diện của tác giả từ bảng Users trên Firebase
        getAuthorImage(recipe.getAuthorName(), holder.pAuthorImage, authorImageUrl -> {
            // Xử lý sự kiện khi ảnh đại diện đã được tải
            holder.pImage.setOnClickListener(v -> {
                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipe.getRecipeId()); // Truyền ID công thức
                intent.putExtra("authorImageUrl", authorImageUrl); // Truyền URL ảnh đại diện tác giả
                context.startActivity(intent);
            });
        });

        holder.ivEmotion.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            boolean isGuest = sharedPreferences.getBoolean("isGuest", false); // Kiểm tra trạng thái là khách

            if (isGuest) {
                Toast.makeText(context, "Bạn phải đăng nhập để yêu thích công thức này.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra xem công thức đã được thích chưa
            if (isRecipeLiked(recipe.getRecipeId())) {
                Toast.makeText(context, "Bạn đã thích công thức này rồi!", Toast.LENGTH_SHORT).show();
            } else {
                updateFavorites(recipe); // Thêm công thức vào bảng 'Favorites'
                Toast.makeText(context, "Công thức đã được thích!", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnSave.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE);
            boolean isGuest = sharedPreferences.getBoolean("isGuest", false); // Kiểm tra trạng thái là khách

            if (isGuest) {
                Toast.makeText(context, "Bạn phải đăng nhập để lưu công thức này.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra xem công thức đã được lưu chưa
            if (isRecipeSaved(recipe.getRecipeId())) {
                Toast.makeText(context, "Bạn đã lưu công thức này rồi!", Toast.LENGTH_SHORT).show();
            } else {
                updateSaveForLater(recipe); // Thêm công thức vào bảng 'Save for Later'
                Toast.makeText(context, "Công thức đã được lưu!", Toast.LENGTH_SHORT).show();
            }
        });


        // Cập nhật số lượng yêu thích từ Firebase
        updateFavoriteCount(recipe.getRecipeId(), holder.tvEmotionCount);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView ptvTitle, ptvAuthor, tvEmotionCount;
        ImageView pImage, pAuthorImage, ivEmotion, btnSave;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ptvTitle = itemView.findViewById(R.id.ptvTitle);
            ptvAuthor = itemView.findViewById(R.id.ptvAuthor);
            pImage = itemView.findViewById(R.id.pImage);
            pAuthorImage = itemView.findViewById(R.id.pAuthorImage);
            ivEmotion = itemView.findViewById(R.id.ivEmotion); // Nút yêu thích
            btnSave = itemView.findViewById(R.id.btnSave);     // Nút lưu
            tvEmotionCount = itemView.findViewById(R.id.tvEmotionCount); // Đếm số yêu thích
        }
    }
    public void updateList(List<Recipe> newList) {
        recipeList = newList;
        notifyDataSetChanged();
    }

    // Phương thức lấy ảnh đại diện của tác giả từ bảng Users dựa trên tên tác giả
    private void getAuthorImage(String authorName, ImageView pAuthorImage, OnAuthorImageLoadedCallback callback) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        usersRef.orderByChild("fullName").equalTo(authorName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String authorImageUrl = userSnapshot.child("images").getValue(String.class);
                        if (authorImageUrl != null) {
                            Glide.with(context).load(authorImageUrl).into(pAuthorImage); // Tải ảnh đại diện của tác giả
                            callback.onAuthorImageLoaded(authorImageUrl); // Gọi callback với URL ảnh
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
    // Interface callback để truyền URL ảnh đại diện của tác giả
    public interface OnAuthorImageLoadedCallback {
        void onAuthorImageLoaded(String authorImageUrl);
    }
    // Phương thức cập nhật bảng 'Favorites' trong Firebase
    private void updateFavorites(Recipe recipe) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("Favorites")
                .child(recipe.getRecipeId());

        favoritesRef.child(currentUserId).setValue(true); // Thêm công thức vào bảng 'Favorites'
    }

    // Phương thức cập nhật bảng 'Save for Later' trong Firebase
    private void updateSaveForLater(Recipe recipe) {
        DatabaseReference saveForLaterRef = FirebaseDatabase.getInstance()
                .getReference("SaveForLater")
                .child(recipe.getRecipeId());

        saveForLaterRef.child(currentUserId).setValue(true); // Thêm công thức vào bảng 'Save for Later'
    }

    // Kiểm tra xem công thức đã được thích chưa
    private boolean isRecipeLiked(String recipeId) {
        // Thực hiện kiểm tra trạng thái thích công thức (chưa được triển khai)
        // Trả về false để đơn giản hóa
        return false;
    }

    // Kiểm tra xem công thức đã được lưu chưa
    private boolean isRecipeSaved(String recipeId) {
        // Thực hiện kiểm tra trạng thái lưu công thức (chưa được triển khai)
        // Trả về false để đơn giản hóa
        return false;
    }
    // Cập nhật số lượng yêu thích từ Firebase
    private void updateFavoriteCount(String recipeId, TextView tvEmotionCount) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance().getReference("Favorites").child(recipeId);

        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount(); // Đếm số lượng người dùng thích công thức
                tvEmotionCount.setText(String.valueOf(count)); // Cập nhật số lượng yêu thích trên UI
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
}
