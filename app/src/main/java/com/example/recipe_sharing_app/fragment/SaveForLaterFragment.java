package com.example.recipe_sharing_app.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.adapter.My_SaveForLater_Adapter;
import com.example.recipe_sharing_app.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SaveForLaterFragment extends Fragment {

    private RecyclerView recyclerView;
    private My_SaveForLater_Adapter mySaveForLaterAdapter;
    private List<Recipe> savedRecipes;
    private DatabaseReference savedForLaterRef;
    private DatabaseReference recipesRef;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved_recipes, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the recipe list and adapter
        savedRecipes = new ArrayList<>();
        mySaveForLaterAdapter = new My_SaveForLater_Adapter(savedRecipes,getContext());
        recyclerView.setAdapter(mySaveForLaterAdapter);

        // Load saved recipes
        loadSavedRecipes();

        return view;
    }

    private void loadSavedRecipes() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem dữ liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        savedForLaterRef = FirebaseDatabase.getInstance().getReference("SaveForLater");
        recipesRef = FirebaseDatabase.getInstance().getReference("Recipes");

        // Lắng nghe thay đổi trong bảng SaveForLater
        savedForLaterRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                savedRecipes.clear();
                if (dataSnapshot.exists()) {
                    // Duyệt qua tất cả các recipeId trong SaveForLater
                    for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                        String recipeId = recipeSnapshot.getKey();

                        // Kiểm tra xem userId có trong danh sách lưu của recipe này không
                        if (recipeSnapshot.hasChild(userId)) {
                            Boolean isSaved = recipeSnapshot.child(userId).getValue(Boolean.class);
                            if (isSaved != null && isSaved) {
                                // Nếu user đã lưu recipe này, lấy thông tin từ bảng Recipes
                                loadRecipeDetails(recipeId);
                            }
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Không có công thức nào đã lưu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadRecipeDetails(String recipeId) {
        // Tải thông tin công thức từ bảng Recipes theo recipeId
        recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Recipe recipe = dataSnapshot.getValue(Recipe.class);
                if (recipe != null) {
                    savedRecipes.add(recipe);
                    mySaveForLaterAdapter.notifyDataSetChanged();
                } else {
                    Log.d("FirebaseData", "Recipe not found for ID: " + recipeId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Lỗi khi tải công thức: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}