package com.example.recipe_sharing_app;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.adapter.CommentAdapter;
import com.example.recipe_sharing_app.adapter.IngredientAdapter;
import com.example.recipe_sharing_app.adapter.RatingAdapter;
import com.example.recipe_sharing_app.adapter.StepAdapter;
import com.example.recipe_sharing_app.model.Comment;
import com.example.recipe_sharing_app.model.Ingredient;
import com.example.recipe_sharing_app.model.Rating;
import com.example.recipe_sharing_app.model.Recipe;
import com.example.recipe_sharing_app.model.Step;
import com.example.recipe_sharing_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView imgRecipe, btn_back;
    private TextView txtRecipeName, txtDescription, txtCookTime, txtAuthorName;
    private RecyclerView rvIngredients, rvSteps, rvComments,rc_ratings;
    private IngredientAdapter ingredientAdapter;
    private StepAdapter stepAdapter;
    private CommentAdapter commentAdapter;
    private EditText edtComment;
    private ImageButton btnSubmitComment;
    private RatingBar ratingBar , ratingBarTB;
    private TextView tvRatingValue , diemTB;
    private RatingAdapter ratingAdapter;
    private List<Rating> ratingList;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_recipes);

        // Ánh xạ các view
        imgRecipe = findViewById(R.id.imgRecipe);
        txtRecipeName = findViewById(R.id.txtRecipeName);
        txtDescription = findViewById(R.id.txtDescription);
        txtCookTime = findViewById(R.id.txtCookTime);
        txtAuthorName = findViewById(R.id.ptvAuthor);
        rvIngredients = findViewById(R.id.rvIngredients);
        rvSteps = findViewById(R.id.rvSteps);
        rvComments = findViewById(R.id.recyclerViewComments);
        edtComment = findViewById(R.id.commentInput);
        btnSubmitComment = findViewById(R.id.send);
        btn_back = findViewById(R.id.btnback);
        // Ánh xạ RatingBar và TextView
        ratingBar = findViewById(R.id.ratingBar);
        tvRatingValue = findViewById(R.id.tvRatingValue);
        diemTB = findViewById(R.id.diemTB);
        ratingBarTB = findViewById(R.id.ratingBarTB);

        // Xử lý sự kiện khi nhấn nút quay lại
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // Kết thúc hoạt động hiện tại
            }
        });

        // Thiết lập RecyclerView cho Ingredients, Steps và Comments
        rvIngredients.setLayoutManager(new LinearLayoutManager(this));
        rvSteps.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setLayoutManager(new LinearLayoutManager(this));


        // hien thi danh gia  RecyclerView rating
        rc_ratings = findViewById(R.id.rc_rating);
        rc_ratings.setLayoutManager(new LinearLayoutManager(this));
        ratingList = new ArrayList<>();
        ratingAdapter = new RatingAdapter( ratingList);
        rc_ratings.setAdapter(ratingAdapter);



        // Lấy recipeId từ Intent
        String recipeId = getIntent().getStringExtra("recipeId");
        String authorImageUrl = getIntent().getStringExtra("authorImageUrl");


        // Tải hình ảnh tác giả
        ImageView ivAuthorAvatar = findViewById(R.id.pAuthorImage);
        if (authorImageUrl != null) {
            Glide.with(this).load(authorImageUrl).into(ivAuthorAvatar);
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
//        String authorName = sharedPreferences.getString("authorName", ""); //
        String profileImageUrl = sharedPreferences.getString("profileImageUrl", "");
        ImageView userAvatars = findViewById(R.id.userAvatar);
        Glide.with(this).load(profileImageUrl).into(userAvatars);

        // Tải dữ liệu công thức nấu ăn từ Firebase
        loadRecipeDetails(recipeId);

        // Xử lý sự kiện khi nhấn nút gửi bình luận
        btnSubmitComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                fetchUserInfo(userId, recipeId); // Lấy thông tin người dùng và gửi bình luận
            }
        });
        // Trong phương thức onCreate, khởi tạo các thành phần và thiết lập sự kiện cho RatingBar
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                tvRatingValue.setText(String.valueOf(rating)); // Cập nhật giá trị xếp hạng
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                checkExistingRating(userId, recipeId, rating); // Kiểm tra và lưu đánh giá
            }
        });
    }
// lay tat ca user danh gia trong rating
    private void loadallRatings(String recipeId) {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings").child(recipeId);
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ratingList.clear(); // Xóa danh sách trước khi thêm dữ liệu mới
                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Rating rating = ratingSnapshot.getValue(Rating.class);
                    if (rating != null) {
                        ratingList.add(rating);
                    }
                }
                ratingAdapter.notifyDataSetChanged(); // Cập nhật adapter sau khi tải xong
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipeDetailActivity", "Error loading ratings", error.toException());
            }
        });
    }
    // Hàm kiểm tra xem người dùng đã đánh giá hay chưa
        private void checkExistingRating(String userId, String recipeId, float rating) {
            DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings").child(recipeId).child(userId);
            ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Nếu đánh giá đã tồn tại, cập nhật giá trị
                        ratingsRef.child("rating").setValue(rating);
                    } else {
                        // Nếu chưa có đánh giá, tạo một đánh giá mới
                        Rating ratingObject = new Rating(userId, rating);
                        ratingsRef.setValue(ratingObject);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("RecipeDetailActivity", "Error checking existing rating", error.toException());
                }
            });
        }
//     Tải đánh giá từ Firebase  diem sao tb
    private void loadRatings(String recipeId) {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings").child(recipeId);
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalRating = 0;
                int ratingCount = 0;
                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Rating rating = ratingSnapshot.getValue(Rating.class);
                    if (rating != null) {
                        totalRating += rating.getRating(); // Cộng dồn điểm đánh giá
                        ratingCount++; // Tăng số lượng đánh giá
                    }
                }
                if (ratingCount > 0) {
                    float averageRating = totalRating / ratingCount; // Tính điểm trung bình
                    ratingBarTB.setNumStars(5);      // Đặt số sao tối đa là 5
                    ratingBarTB.setStepSize(0.5f);
                    ratingBarTB.setRating(averageRating); // Cập nhật điểm đánh giá lên RatingBar
                    // Làm tròn đến 1 chữ số thập phân và hiển thị
                    String formattedAverageRating = String.format("%.1f", averageRating);
                    diemTB.setText(formattedAverageRating); // Hiển thị điểm trung bình
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipeDetailActivity", "Error loading ratings", error.toException());
            }
        });
    }
    private void loadUserRating(String recipeId) {
        // Lấy UID của người dùng hiện tại
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Tham chiếu đến bảng Ratings cho recipeId
        DatabaseReference userRatingRef = FirebaseDatabase.getInstance().getReference("Ratings").child(recipeId).child(userId);
        userRatingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy đánh giá của người dùng hiện tại
                    Rating userRating = snapshot.getValue(Rating.class);
                    if (userRating != null) {
                        // Cập nhật điểm đánh giá lên RatingBar
                        ratingBar.setRating(userRating.getRating());
                        tvRatingValue.setText(String.valueOf(userRating.getRating())); // Hiển thị điểm đánh giá cá nhân
                    }
                } else {
                    // Nếu người dùng chưa đánh giá, đặt RatingBar về 0
                    ratingBar.setRating(0);
                    tvRatingValue.setText("Chưa có đánh giá");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi xảy ra (nếu cần)
            }
        });
    }



    // Tải thông tin chi tiết công thức từ Firebase
    private void loadRecipeDetails(String recipeId) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("Recipes").child(recipeId);
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Recipe recipe = snapshot.getValue(Recipe.class);
                if (recipe != null) {
                    // Hiển thị thông tin công thức trên giao diện
                    txtRecipeName.setText(recipe.getName());
                    txtDescription.setText(recipe.getDescription());
                    txtCookTime.setText(recipe.getCookTime());
                    txtAuthorName.setText(recipe.getAuthorName());
                    Picasso.get().load(recipe.getImageUrl()).into(imgRecipe);
                    setupIngredientsRecyclerView(recipe.getIngredients()); // Thiết lập RecyclerView cho nguyên liệu
                    setupStepsRecyclerView(recipe.getSteps()); // Thiết lập RecyclerView cho các bước
                    loadComments(recipeId); // Tải bình luận cho công thức
                    loadUserRating(recipeId);
                    loadallRatings(recipeId);
                    loadRatings(recipeId);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RecipeDetailActivity", "Error loading recipe details", error.toException());
            }
        });
    }

    // Thiết lập RecyclerView cho nguyên liệu
    private void setupIngredientsRecyclerView(List<Ingredient> ingredients) {
        ingredientAdapter = new IngredientAdapter(this, ingredients);
        rvIngredients.setAdapter(ingredientAdapter); // Gán adapter cho RecyclerView
    }

    // Thiết lập RecyclerView cho các bước thực hiện
    private void setupStepsRecyclerView(List<Step> steps) {
        stepAdapter = new StepAdapter(this, steps);
        rvSteps.setAdapter(stepAdapter); // Gán adapter cho RecyclerView
    }

    // Tải bình luận cho công thức từ Firebase
    private void loadComments(String recipeId) {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference("Comments").child(recipeId);
        commentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Comment> commentList = new ArrayList<>();
                for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                    Comment comment = commentSnapshot.getValue(Comment.class);
                    if (comment != null) {
                        commentList.add(comment); // Thêm bình luận vào danh sách
                    }
                }
                // Thiết lập adapter cho RecyclerView hiển thị bình luận
                CommentAdapter commentAdapter = new CommentAdapter(RecipeDetailActivity.this, commentList);
                rvComments.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi khi lấy dữ liệu từ Firebase
            }
        });
    }

    // Lấy thông tin người dùng từ Firebase
    private void fetchUserInfo(String userId, String recipeId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String userName = user.getFullName(); // Lấy tên người dùng
                        String userAvatar = user.getImages(); // Lấy đường dẫn ảnh đại diện
                        submitComment(userId, userName, userAvatar, recipeId); // Gửi bình luận
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("RecipeDetailActivity", "Error fetching user info", databaseError.toException());
            }
        });
    }

    // Gửi bình luận tới Firebase
    private void submitComment(String userId, String userName, String userAvatar, String recipeId ) {
        String commentContent = edtComment.getText().toString(); // Lấy nội dung bình luận
        // Lấy ngày hiện tại với định dạng mong muốn
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        // Tạo đối tượng Comment
        Comment comment = new Comment(userId, recipeId, userName, userAvatar, commentContent, time );
        DatabaseReference commentRef = FirebaseDatabase.getInstance().getReference("Comments").child(recipeId);
        commentRef.push().setValue(comment) // Thêm bình luận vào Firebase
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        edtComment.setText(""); // Xóa ô nhập bình luận
                        loadComments(recipeId); // Tải lại danh sách bình luận
                    } else {
                        Log.e("RecipeDetailActivity", "Error submitting comment", task.getException());
                    }
                });
    }
}
