package com.example.recipe_sharing_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.RecipeDetailActivity;
import com.example.recipe_sharing_app.model.Recipe;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.squareup.picasso.Picasso;

import java.util.List;

public class My_RecipeAdapter extends RecyclerView.Adapter<My_RecipeAdapter.ViewHolder> {

    private List<Recipe> recipeList;
    private OnRecipeLikeListener onRecipeLikeListener;
    private Context context;
    public My_RecipeAdapter(List<Recipe> recipeList, OnRecipeLikeListener onRecipeLikeListener , Context context) {
        this.recipeList = recipeList;
        this.onRecipeLikeListener = onRecipeLikeListener;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.textTitle.setText(recipe.getName());

        // Lấy ảnh đại diện của tác giả từ Firebase và truyền vào intent
        getAuthorImage(recipe.getAuthorName(), holder.image, authorImageUrl -> {
            holder.imageRecipe.setOnClickListener(v -> {
                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipe.getRecipeId());
                intent.putExtra("authorImageUrl", authorImageUrl);
                context.startActivity(intent);
            });
        });
        Picasso.get().load(recipe.getImageUrl()).into(holder.imageRecipe);

        // Xử lý nút "thích"
        holder.ivEmotion.setOnClickListener(v -> {
            if (onRecipeLikeListener != null) {
                onRecipeLikeListener.onRecipeLike(recipe);
            }
        });

        // Cập nhật số lượng yêu thích
        updateFavoriteCount(holder.tvEmotionCount, recipe.getRecipeId());


        // Xử lý sự kiện khi nhấn nút xóa công thức với AlertDialog để xác nhận
        holder.btnDelete_recipes.setOnClickListener(v -> {
            // Hiển thị AlertDialog để xác nhận
            new android.app.AlertDialog.Builder(context)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa công thức này không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Nếu người dùng chọn "Có", tiến hành xóa công thức
                        deleteRecipe(recipe.getRecipeId(), position);
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        // Người dùng chọn "Không", đóng dialog
                        dialog.dismiss();
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateList(List<Recipe> newList) {
        recipeList = newList;
        notifyDataSetChanged();
    }

    private void updateFavoriteCount(TextView tvEmotionCount, String recipeId) {
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("Favorites")
                .child(recipeId);

        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                tvEmotionCount.setText(String.valueOf(count));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }

    public interface OnRecipeLikeListener {
        void onRecipeLike(Recipe recipe);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRecipe;
        TextView textTitle;
        ImageView ivEmotion,image;
        TextView tvEmotionCount;
        ImageButton btnDelete_recipes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.image_recipe);
            textTitle = itemView.findViewById(R.id.text_title);
            ivEmotion = itemView.findViewById(R.id.ivEmotion);
            tvEmotionCount = itemView.findViewById(R.id.tvEmotionCount);
            image = itemView.findViewById(R.id.image_recipes);
            btnDelete_recipes = itemView.findViewById(R.id.btnDelete_recipe);
        }
    }
    private void deleteRecipe(String recipeId, int position) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("Recipes").child(recipeId);

        // Xóa công thức nấu ăn từ Firebase
        recipeRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Xóa thành công, cập nhật lại danh sách và thông báo cho adapter
                recipeList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, recipeList.size());
            } else {
                // Xử lý khi xóa thất bại, hiển thị thông báo lỗi
            }
        });
    }

    // Phương thức lấy ảnh đại diện của tác giả từ bảng Users dựa trên authorName và trả về thông qua callback
    private void getAuthorImage(String authorName, ImageView pAuthorImage, New_RecipeAdapter.OnAuthorImageLoadedCallback callback) {
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
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }
    // Callback interface để truyền URL ảnh
    public interface OnAuthorImageLoadedCallback {
        void onAuthorImageLoaded(String authorImageUrl);
    }

}
