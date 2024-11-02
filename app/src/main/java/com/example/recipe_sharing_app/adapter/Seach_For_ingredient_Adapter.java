package com.example.recipe_sharing_app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_sharing_app.R;
import com.example.recipe_sharing_app.model.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class Seach_For_ingredient_Adapter extends RecyclerView.Adapter<Seach_For_ingredient_Adapter.IngredientViewHolder> {

    private List<Ingredient> ingredientList;
    private Context context;
    private List<Ingredient> selectedIngredients = new ArrayList<>();

    // Constructor
    public Seach_For_ingredient_Adapter(List<Ingredient> ingredientList, Context context) {
        this.ingredientList = ingredientList;
        this.context = context;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.seach_for_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredientList.get(position);
        holder.radioButton.setText(ingredient.getName());

        // Set checked state based on selection
        holder.radioButton.setChecked(selectedIngredients.contains(ingredient));

        holder.radioButton.setOnClickListener(v -> {
            boolean isChecked = holder.radioButton.isChecked();

            if (isChecked) {
                // Check if already selected two ingredients
                if (selectedIngredients.size() < 2) {
                    selectedIngredients.add(ingredient);
                    Toast.makeText(context, "Nguyên liệu đã chọn: " + ingredient.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    // Max selection reached, uncheck the RadioButton
                    holder.radioButton.setChecked(false);
                    Toast.makeText(context, "Chỉ được chọn 2 nguyên liệu", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Remove ingredient if unchecked
                selectedIngredients.remove(ingredient);
                Toast.makeText(context, "Nguyên liệu đã bỏ chọn: " + ingredient.getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredientList.size();
    }

    // ViewHolder class
    public static class IngredientViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioButton;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            radioButton = itemView.findViewById(R.id.checkBoxIngredient);
        }
    }
}
