package com.example.recipe_sharing_app.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.adapter.Seach_For_ingredient_Adapter;
import com.example.recipe_sharing_app.adapter.New_RecipeAdapter;
import com.example.recipe_sharing_app.adapter.All_RecipeAdapter;
import com.example.recipe_sharing_app.model.Ingredient;
import com.example.recipe_sharing_app.model.Recipe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

public class KhoCamHungFragment extends Fragment {

    private RecyclerView rvRecipes, rvOtherItems, rvIngredients;
    private New_RecipeAdapter newRecipeAdapter;
    private All_RecipeAdapter allRecipeAdapter;
    private Seach_For_ingredient_Adapter seachForIngredientAdapter;
    private List<Recipe> recipeList = new ArrayList<>();
    private List<Recipe> otherItemList = new ArrayList<>();
    private List<Ingredient> ingredientList = new ArrayList<>();

    private DatabaseReference recipeDatabaseReference;
    private DatabaseReference ingredientDatabaseReference;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kho_cam_hung, container, false);

        rvRecipes = view.findViewById(R.id.rvRecipes);
        rvOtherItems = view.findViewById(R.id.rvOtherItems);
        rvIngredients = view.findViewById(R.id.rcPopulerNL);

        // Set up rvRecipes (Grid layout)----------recipe
        rvRecipes.setLayoutManager(new GridLayoutManager(getContext(), 2));
        newRecipeAdapter = new New_RecipeAdapter(recipeList, getContext());
        rvRecipes.setAdapter(newRecipeAdapter);

        // Set up rvOtherItems (Horizontal layout)-------------------(product)
        rvOtherItems.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        allRecipeAdapter = new All_RecipeAdapter(otherItemList, getContext());
        rvOtherItems.setAdapter(allRecipeAdapter);
//
//        // Set up rvIngredients (Linear layout) for ingredients
//        rvIngredients.setLayoutManager(new GridLayoutManager(getContext(), 4));
//        seachForIngredientAdapter = new Seach_For_ingredient_Adapter(ingredientList, getContext());
//        rvIngredients.setAdapter(seachForIngredientAdapter);
        // Set up rvIngredients (Linear layout) for ingredients


        // Set up rvIngredients (Grid layout with horizontal scrolling) for ingredients
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false);
        rvIngredients.setLayoutManager(gridLayoutManager);

        seachForIngredientAdapter = new Seach_For_ingredient_Adapter(ingredientList, getContext());
        rvIngredients.setAdapter(seachForIngredientAdapter);





        recipeDatabaseReference = FirebaseDatabase.getInstance().getReference("Recipes");
        ingredientDatabaseReference = FirebaseDatabase.getInstance().getReference("Ingredients");

        // Fetch data for rvRecipes
        fetchRecipesForRvRecipes();

        // Fetch data for rvOtherItems
        fetchOtherItemsForRvOtherItems();

        // Fetch data for rvIngredients
        fetchIngredientsForRvIngredients();

        return view;
    }

    private void fetchRecipesForRvRecipes() {
        recipeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipeList.add(recipe);
                    }
                }
                newRecipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if necessary
            }
        });
    }

    private void fetchOtherItemsForRvOtherItems() {
        recipeDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                otherItemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        otherItemList.add(recipe);
                    }
                }
                allRecipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if necessary
            }
        });
    }

    private void fetchIngredientsForRvIngredients() {
        ingredientDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ingredientList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ingredient ingredient = snapshot.getValue(Ingredient.class);
                    if (ingredient != null) {
                        ingredientList.add(ingredient);
                    }
                }
                seachForIngredientAdapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error if necessary
            }
        });
    }

}
