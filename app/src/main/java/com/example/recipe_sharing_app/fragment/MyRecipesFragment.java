package com.example.recipe_sharing_app.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.adapter.My_RecipeAdapter;
import com.example.recipe_sharing_app.model.Recipe;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class MyRecipesFragment extends Fragment implements My_RecipeAdapter.OnRecipeLikeListener {

    private RecyclerView recyclerView;
    private My_RecipeAdapter myRecipeAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private SearchView searchView;
    private String authorName;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_recipes, container, false);

        searchView = view.findViewById(R.id.search_view);
        recyclerView = view.findViewById(R.id.recycler_view_recipes);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        myRecipeAdapter = new My_RecipeAdapter(recipeList,this, getContext());
        recyclerView.setAdapter(myRecipeAdapter);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        authorName = sharedPreferences.getString("authorName", "Guest");
        // Lấy userId từ FirebaseAuth
        userId = getCurrentUserId();
        loadRecipes();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterRecipes(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterRecipes(newText);
                return true;
            }
        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadRecipes() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("Recipes");

        recipeRef.orderByChild("authorName").equalTo(authorName)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        recipeList.clear();
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Recipe recipe = snapshot.getValue(Recipe.class);
                                recipeList.add(recipe);
                            }
                            myRecipeAdapter.updateList(recipeList);
                        } else {
                            // Handle no data found
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors
                    }
                });
    }

    private void filterRecipes(String query) {
        List<Recipe> filteredList = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            if (recipe.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(recipe);
            }
        }
        myRecipeAdapter.updateList(filteredList);
    }
    @Override
    public void onRecipeLike(Recipe recipe) {
        String userId = getCurrentUserId();
        DatabaseReference favoritesRef = FirebaseDatabase.getInstance()
                .getReference("Favorites")
                .child(recipe.getRecipeId())
                .child(userId);

        favoritesRef.setValue(true).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Công thức đã được thích!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Lỗi khi thích công thức!", Toast.LENGTH_SHORT).show();
        });
    }
    private String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid(); // Trả về userId từ Firebase Authentication
        } else {
            return "guestUserId"; // Nếu không có người dùng nào đăng nhập, trả về id mặc định
        }
    }


}
