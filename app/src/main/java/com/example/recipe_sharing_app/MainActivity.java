package com.example.recipe_sharing_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.adapter.All_RecipeAdapter;
import com.example.recipe_sharing_app.adapter.PageAdapter;
import com.example.recipe_sharing_app.model.Recipe;
import com.example.recipe_sharing_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvOtherItems;
    private List<Recipe> otherItemList = new ArrayList<>();
    private All_RecipeAdapter allRecipeAdapter;
    private DatabaseReference recipeDatabaseReference;

    private ViewPager2 viewPager;
    private TextView tabCacBanBep, tabKhoCamHung;
    private View tabIndicator;
    private ImageButton navAdds, navProfile, navHome;
    private SearchView searchView;
    private FirebaseAuth auth;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup RecyclerView cho các công thức tìm kiếm
        rvOtherItems = findViewById(R.id.rcSearch);
        rvOtherItems.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        allRecipeAdapter = new All_RecipeAdapter(otherItemList, this);
        rvOtherItems.setAdapter(allRecipeAdapter);

        // Setup SearchView
        searchView = findViewById(R.id.etSearch);
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

        // Setup ViewPager cho tabs
        viewPager = findViewById(R.id.viewPager);
        tabCacBanBep = findViewById(R.id.tabCacBanBep);
        tabKhoCamHung = findViewById(R.id.tabKhoCamHung);
        tabIndicator = findViewById(R.id.tabIndicator);
        navAdds = findViewById(R.id.navAdd);
        navHome = findViewById(R.id.navHome);
        navProfile = findViewById(R.id.navProfile);

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        recipeDatabaseReference = FirebaseDatabase.getInstance().getReference("Recipes");

        loadUserData();

        navAdds.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            boolean isGuest = sharedPreferences.getBoolean("isGuest", false);

            if (auth.getCurrentUser() != null && !isGuest) {
                // Người dùng đã đăng nhập và không phải khách
                Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            } else {
                // Hiển thị AlertDialog
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Thông báo")
                        .setMessage("Bạn phải đăng nhập mới có thể thêm công thức món ăn.")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("Ở lại", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


        navProfile.setOnClickListener(view -> {
            // Kiểm tra xem người dùng có đăng nhập không
            SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
            boolean isGuest = sharedPreferences.getBoolean("isGuest", false);
            if (auth.getCurrentUser() != null && !isGuest)  {
                // Nếu đã đăng nhập, chuyển đến Activity hồ sơ
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            } else {
                // Nếu chưa đăng nhập, hiển thị AlertDialog thông báo
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Thông báo")
                        .setMessage("Bạn phải đăng nhập để xem hồ sơ.")
                        .setPositiveButton("Đồng ý", (dialog, which) -> {
                            // Chuyển đến màn hình đăng nhập
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("Ở lại", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });


        setupViewPager();
        tabCacBanBep.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabKhoCamHung.setOnClickListener(v -> viewPager.setCurrentItem(1));

        setupTabIndicator();
    }

    private void setupViewPager() {
        PageAdapter pageAdapter = new PageAdapter(this);
        viewPager.setAdapter(pageAdapter);
    }

    private void setupTabIndicator() {
        updateTabIndicator(0);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabIndicator(position);
            }
        });
    }

    private void updateTabIndicator(int position) {
        int tabWidth = tabCacBanBep.getWidth();
        int offset = position * tabWidth;
        tabIndicator.animate().translationX(offset).setDuration(300).start();
    }

    private void filterRecipes(String query) {
        if (query.isEmpty()) {
            otherItemList.clear();
            allRecipeAdapter.updateList(otherItemList);
            return;
        }

        recipeDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Recipe> filteredList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipe recipe = snapshot.getValue(Recipe.class);
                    if (recipe != null && recipe.getName().toLowerCase().contains(query.toLowerCase())) {
                        filteredList.add(recipe);
                    }
                }
                allRecipeAdapter.updateList(filteredList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", "");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Kiểm tra xem email có rỗng không
        if (email.isEmpty()) {
            // Nếu email rỗng, sử dụng ảnh mặc định và đánh dấu là khách
            Glide.with(MainActivity.this)
                    .load(R.drawable.ic_launcher_foreground) // Hình ảnh mặc định cho khách
                    .into(navProfile);
            Toast.makeText(this, "Không có người dùng hiện tại. Đang sử dụng chế độ khách.", Toast.LENGTH_SHORT).show();

            // Lưu trạng thái là khách
            editor.putBoolean("isGuest", true);
            editor.apply();
            return; // Ngưng thực hiện hàm nếu email rỗng
        }

        userRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            Glide.with(MainActivity.this)
                                    .load(user.getImages())
                                    .placeholder(R.drawable.recipe)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .into(navProfile);
                        } else {
                            // Xử lý khi user null
                            Glide.with(MainActivity.this)
                                    .load(R.drawable.ic_launcher_foreground)
                                    .into(navProfile);
                        }
                    }
                    editor.putBoolean("isGuest", false); // Đánh dấu không phải khách
                } else {
                    Glide.with(MainActivity.this)
                            .load(R.drawable.ic_launcher_foreground)
                            .into(navProfile);
                    Toast.makeText(MainActivity.this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    editor.putBoolean("isGuest", true); // Đánh dấu là khách
                }
                editor.apply();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

}