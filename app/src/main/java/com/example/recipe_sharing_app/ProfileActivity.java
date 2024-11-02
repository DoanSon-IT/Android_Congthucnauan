package com.example.recipe_sharing_app;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    ImageButton navAdds,navProfile ,imagesprofile ,navHome;
    Button btnsettings_icon , log_out;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String authorName = sharedPreferences.getString("authorName", ""); //
        String profileImageUrl = sharedPreferences.getString("profileImageUrl", "");
        String email = sharedPreferences.getString("email", "");

        TextView tvAuthorName = findViewById(R.id.username);
        tvAuthorName.setText("" + authorName);
        TextView emails = findViewById(R.id.account_tag);
        emails.setText("" + email);


        navAdds = findViewById(R.id.navAdd);
        imagesprofile = findViewById(R.id.profile_image);
        Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.chef_icon_removebg_preview) // Ảnh mặc định khi chờ tải
                .error(R.drawable.ic_launcher_foreground) // Ảnh hiển thị khi tải thất bại
                .into(imagesprofile);
        navProfile = findViewById(R.id.navProfile);

        Glide.with(this)
                .load(profileImageUrl)
                .placeholder(R.drawable.chef_icon_removebg_preview) // Ảnh mặc định khi chờ tải
                .error(R.drawable.ic_launcher_foreground) // Ảnh hiển thị khi tải thất bại
                .into(navProfile);

        navAdds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this ,AddRecipeActivity.class);
                startActivity(intent);
            }
        });
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this , ProfileActivity.class);
                startActivity(intent);
            }
        });
        log_out =findViewById(R.id.btnlog_out);
        log_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLogoutConfirmationDialog();
            }
        });
        navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
        btnsettings_icon = findViewById(R.id.settings_icon);
        btnsettings_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this,Update_profile_activity.class);
                startActivity(intent);
            }
        });

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Setup TabLayout with ViewPager2
        tabLayout.addTab(tabLayout.newTab().setText("Món Đã Lưu"));
        tabLayout.addTab(tabLayout.newTab().setText("Món Yêu Thích"));
        // Tab "Save For Later"
        tabLayout.addTab(tabLayout.newTab().setText("Món Của Tôi"));
        // Tab "My Recipes"
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Do nothing
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Do nothingbtnsettings_icon
            }
        });
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }
    private void logout() {
//        mAuth.signOut();
        // Xóa toàn bộ session
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply(); // Áp dụng thay đổi

        // Khôi phục trạng thái khách (nếu cần)

        // Chuyển đến màn hình đăng nhập
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Kết thúc MainActivity
    }

    private void showLogoutConfirmationDialog() {
        // Tạo AlertDialog để hỏi người dùng có muốn đăng xuất không
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Đăng xuất");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");

        // Nút "Đồng ý" - thực hiện đăng xuất
        builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout(); // Gọi phương thức đăng xuất
            }
        });

        // Nút "Hủy" - đóng hộp thoại mà không làm gì
        builder.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Đóng hộp thoại
            }
        });

        // Hiển thị hộp thoại
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
