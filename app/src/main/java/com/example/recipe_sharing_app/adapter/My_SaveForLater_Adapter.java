package com.example.recipe_sharing_app.adapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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

public class My_SaveForLater_Adapter extends RecyclerView.Adapter<My_SaveForLater_Adapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private Context context;

    public My_SaveForLater_Adapter(List<Recipe> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context=context;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_save_for_later, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);

        // Set author name
        holder.authorNameTextView.setText(recipe.getAuthorName());

        // Set recipe title
        holder.recipeTitleTextView.setText(recipe.getName());
        // Tải ảnh đại diện của tác giả từ bảng Users trên Firebase
        getAuthorImage(recipe.getAuthorName(), holder.authorImageView, authorImageUrl -> {
            // Xử lý sự kiện khi ảnh đại diện đã được tải
            holder.recipeImageView.setOnClickListener(v -> {
                Intent intent = new Intent(context, RecipeDetailActivity.class);
                intent.putExtra("recipeId", recipe.getRecipeId()); // Truyền ID công thức
                intent.putExtra("authorImageUrl", authorImageUrl); // Truyền URL ảnh đại diện tác giả
                context.startActivity(intent);
            });
        });
        // Load recipe image using Glide
        Glide.with(holder.itemView.getContext())
                .load(recipe.getImageUrl())  // Set this as the image URL for the recipe
                .placeholder(R.drawable.chef_icon_removebg_preview) // Fallback image
                .into(holder.recipeImageView);

        // Optionally, set an author image (if available)
        Glide.with(holder.itemView.getContext())
                .load("")  // Set the author image URL if applicable
                .placeholder(R.drawable.avata2) // Fallback image for author
                .into(holder.authorImageView);

        // Handle save icon
        // Handle save icon
        holder.saveIcon.setImageResource(R.drawable.save_instagram); // Set your save icon here

// Add long click listener for 3 seconds to remove from "Save for Later"
        holder.saveIcon.setOnLongClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "Giữ 2 giây để xóa...", Toast.LENGTH_SHORT).show();

            // Delay for 3 seconds (3000 ms) before showing the confirmation dialog
            new Handler().postDelayed(() -> {
                // Show a confirmation dialog before removing
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Xác nhận")
                        .setMessage("Bạn có chắc chắn muốn xóa mục này khỏi 'Lưu xem sau'?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            // User confirmed to remove the item
                            removeFromSaveForLater(recipe.getRecipeId(), holder);
                            Toast.makeText(holder.itemView.getContext(), "Đã xóa khỏi 'Lưu xem sau'", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", (dialog, which) -> {
                            // User canceled the dialog, do nothing
                            dialog.dismiss();
                        })
                        .show();
            }, 1000); // 3-second delay
            return true;
        });

    }

    private void removeFromSaveForLater(String recipeId, RecipeViewHolder holder) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference saveForLaterRef = FirebaseDatabase.getInstance().getReference("SaveForLater");

        // Remove recipe from "Save for Later" in Firebase ( delete)
        saveForLaterRef.child(recipeId).child(userId).removeValue().addOnSuccessListener(aVoid -> {
            // Notify user that the recipe is removed
            Toast.makeText(holder.itemView.getContext(), "Đã xóa khỏi danh sách xem sau!", Toast.LENGTH_SHORT).show();

            // Optionally, remove recipe from list and notify adapter
            recipeList.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());

        }).addOnFailureListener(e -> {
            Toast.makeText(holder.itemView.getContext(), "Lỗi khi xóa công thức khỏi danh sách xem sau!", Toast.LENGTH_SHORT).show();
        });
    }

    // Phương thức lấy ảnh đại diện của tác giả từ bảng Users dựa trên tên tác giả
    private void getAuthorImage(String authorName, ImageView pAuthorImage, All_RecipeAdapter.OnAuthorImageLoadedCallback callback) {
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

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        ImageView authorImageView, recipeImageView, saveIcon;
        TextView authorNameTextView, recipeTitleTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            authorImageView = itemView.findViewById(R.id.authorImage);
            recipeImageView = itemView.findViewById(R.id.recipeImage);
            saveIcon = itemView.findViewById(R.id.saveIcon);
            authorNameTextView = itemView.findViewById(R.id.authorName);
            recipeTitleTextView = itemView.findViewById(R.id.recipeTitle);
        }
    }
}
