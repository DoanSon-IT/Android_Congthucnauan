package com.example.recipe_sharing_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ChefAdapter extends RecyclerView.Adapter<ChefAdapter.ChefViewHolder> {

    private Context context;
    private List<User> chefList;

    public ChefAdapter(Context context, List<User> chefList) {
        this.context = context;
        this.chefList = chefList;
    }

    @NonNull
    @Override
    public ChefViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cacbanbep, parent, false);
        return new ChefViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChefViewHolder holder, int position) {
        User chef = chefList.get(position);

        // Hiển thị tên bếp
        holder.tvChefName.setText(chef.getFullName());

        // Hiển thị ảnh bếp
        Glide.with(context)
                .load(chef.getImages())
                .placeholder(R.drawable.chef_icon_removebg_preview)  // Placeholder nếu không có ảnh
                .into(holder.imgChef);

        // Lấy số công thức mà mỗi bếp đã chia sẻ dựa trên fullName từ bảng công thức nấu ăn
        getRecipeCountByChef(chef.getFullName(), holder.tvRecipeCount);
    }

    @Override
    public int getItemCount() {
        return chefList.size();
    }

    public static class ChefViewHolder extends RecyclerView.ViewHolder {
        ImageView imgChef;
        TextView tvChefName, tvRecipeCount;

        public ChefViewHolder(@NonNull View itemView) {
            super(itemView);
            imgChef = itemView.findViewById(R.id.imgChef);
            tvChefName = itemView.findViewById(R.id.tvChefName);
            tvRecipeCount = itemView.findViewById(R.id.tvRecipeCount);
        }
    }

    // Hàm để lấy số lượng công thức mà bếp đã chia sẻ dựa trên fullName
    private void getRecipeCountByChef(String chefName, final TextView tvRecipeCount) {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("Recipes");
        recipesRef.orderByChild("authorName").equalTo(chefName)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int recipeCount = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            recipeCount++; // Đếm số công thức của bếp này
                        }
                        tvRecipeCount.setText(String.valueOf(recipeCount)); // Hiển thị số công thức
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Xử lý lỗi nếu có
                    }
                });
    }
}
