package com.example.recipe_sharing_app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipe_sharing_app.adapter.IngredientAdapter;
import com.example.recipe_sharing_app.adapter.StepAdapter;
import com.example.recipe_sharing_app.model.Ingredient;
import com.example.recipe_sharing_app.model.Recipe;
import com.example.recipe_sharing_app.model.Step;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_IMAGE_REQUEST_STEP = 2;

    private EditText etRecipeName, etRecipeDescription, etRecipeAuthor, etRecipeServings, etRecipeCookTime;
    private ImageView imgRecipeImage ;
    private Uri imgUri;
    private Uri stepImageUri;

    private Button btnAddRecipe, btnAddIngredient, btnAddStep;
    private RecyclerView rvIngredients, rvSteps;
    private IngredientAdapter ingredientAdapter;
    private StepAdapter stepAdapter;
    private List<Ingredient> ingredientList = new ArrayList<>();
    private List<Step> stepList = new ArrayList<>();
    private ImageButton btnBack;
    private DatabaseReference recipeDbRef;
    private StorageReference storageRef;
    private ProgressDialog progressDialog;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        etRecipeName = findViewById(R.id.etRecipeName);
        etRecipeDescription = findViewById(R.id.etRecipeDescription);

        // Lấy tên tác giả từ SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String authorName = sharedPreferences.getString("authorName", "Guest");

        // Hiển thị authorName
        etRecipeAuthor = findViewById(R.id.etRecipeAuthor);
        etRecipeAuthor.setText(authorName);

        etRecipeServings = findViewById(R.id.etRecipeServings);
        etRecipeCookTime = findViewById(R.id.etRecipeCookTime);
        imgRecipeImage = findViewById(R.id.imgRecipeImage);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        rvIngredients = findViewById(R.id.rvIngredients);
        rvSteps = findViewById(R.id.rvSteps);
        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnAddStep = findViewById(R.id.btnAddStep);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }

            });



        // Initialize Firebase
        recipeDbRef = FirebaseDatabase.getInstance().getReference("Recipes");
        storageRef = FirebaseStorage.getInstance().getReference("RecipeImages");

        progressDialog = new ProgressDialog(this);

        // RecyclerView setup
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        ingredientAdapter = new IngredientAdapter(this, ingredientList);
        rvIngredients.setAdapter(ingredientAdapter);

        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        stepAdapter = new StepAdapter(this, stepList);
        rvSteps.setAdapter(stepAdapter);

        // Button to upload an image for the recipe
        imgRecipeImage.setOnClickListener(v -> openFileChooser());

        // Button to add new recipe
        btnAddRecipe.setOnClickListener(v -> uploadRecipe());

        // Add ingredient to list
        btnAddIngredient.setOnClickListener(v -> showAddIngredientDialog());

        // Add step to list
        btnAddStep.setOnClickListener(v -> showAddStepDialog());
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openImageChooserForStep(ImageView imgStepImage) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST_STEP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();
            if (requestCode == PICK_IMAGE_REQUEST) {
                imgUri = selectedImageUri;
                imgRecipeImage.setImageURI(imgUri);
            } else if (requestCode == PICK_IMAGE_REQUEST_STEP) {
                stepImageUri = selectedImageUri;
            }
        }
    }

    private void showAddIngredientDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.dialog_add_ingredient, null);
        EditText inputName = layout.findViewById(R.id.inputName);

        new AlertDialog.Builder(this)
                .setTitle("Thêm nguyên liệu")
                .setView(layout)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = inputName.getText().toString().trim();

                    if (!TextUtils.isEmpty(name)) {
                        Ingredient ingredient = new Ingredient(name);
                        ingredientList.add(ingredient);
                        ingredientAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void showAddStepDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.dialog_add_step, null);
        EditText inputDescription = layout.findViewById(R.id.inputDescription);
        ImageView imgStepImage = layout.findViewById(R.id.stepImageView);
        Button btnSelectImage = layout.findViewById(R.id.btnSelectImage); // Button to select image

        imgStepImage.setImageResource(R.drawable.camera_bg); // Placeholder image
        new AlertDialog.Builder(this)
                .setTitle("Thêm bước")
                .setView(layout)
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String description = inputDescription.getText().toString().trim();

                    if (!TextUtils.isEmpty(description)) {
                        if (stepImageUri != null) {
                            uploadStepImage(description);
                        } else {
                            Step step = new Step(description, null);
                            stepList.add(step);
                            stepAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Vui lòng nhập mô tả bước!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();

        // Button to choose image for the step
        btnSelectImage.setOnClickListener(v -> openImageChooserForStep(imgStepImage));
    }

    private void uploadStepImage(String description) {
        progressDialog.setMessage("Đang tải ảnh bước...");
        progressDialog.show();

        StorageReference stepImageRef = storageRef.child("Steps/" + System.currentTimeMillis() + ".jpg");
        stepImageRef.putFile(stepImageUri)
                .addOnSuccessListener(taskSnapshot -> stepImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Step step = new Step(description, imageUrl);
                    stepList.add(step);
                    stepAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi tải ảnh bước: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadRecipe() {

        final String recipeName = etRecipeName.getText().toString().trim();
        final String recipeDescription = etRecipeDescription.getText().toString().trim();
        final String recipeAuthor = etRecipeAuthor.getText().toString().trim();
        final String servings = etRecipeServings.getText().toString().trim();
        final String cookTime = etRecipeCookTime.getText().toString().trim();

        if (TextUtils.isEmpty(recipeName) || TextUtils.isEmpty(recipeDescription) || TextUtils.isEmpty(recipeAuthor)
                || TextUtils.isEmpty(servings) || TextUtils.isEmpty(cookTime) || imgUri == null) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Đang thêm công thức...");
        progressDialog.show();

        // Kiểm tra xem người dùng đã đăng nhập chưa
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Bạn cần đăng nhập để thực hiện thao tác này!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tải ảnh công thức lên Firebase Storage
        final StorageReference fileReference = storageRef.child(System.currentTimeMillis() + ".jpg");
        fileReference.putFile(imgUri)
                .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imgUrl = uri.toString();

                    // Lấy recipeId từ Firebase Database
                    String recipeId = recipeDbRef.push().getKey();

                    // Tạo đối tượng Recipe với recipeId
                    Recipe recipe = new Recipe(recipeId, recipeName, recipeDescription, recipeAuthor, servings, cookTime, imgUrl, ingredientList, stepList);

                    // Lưu recipe vào Firebase Database
                    recipeDbRef.child(recipeId).setValue(recipe)
                            .addOnCompleteListener(task -> {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Thêm công thức thành công!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(this, "Thêm công thức thất bại!", Toast.LENGTH_SHORT).show();
                                }
                            });
                }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
