package com.example.recipe_sharing_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.recipe_sharing_app.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private TextView tvSignUp;
    private TextView skiplg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo các thành phần giao diện
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvSignUp = findViewById(R.id.tvSignUp);
        skiplg = findViewById(R.id.skiplogin);

        // Khởi tạo FirebaseAuth và tham chiếu tới Firebase Database
        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");

        // Điều hướng tới màn hình đăng ký
        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Khi người dùng nhấn "Skip Login" (Bỏ qua đăng nhập)
        skiplg.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Xử lý sự kiện khi nhấn nút đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Kiểm tra đầu vào hợp lệ
            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email không được để trống");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                etPassword.setError("Mật khẩu không được để trống");
                return;
            }

            // Gọi hàm đăng nhập người dùng
            loginUser(email, password);
        });
    }

    // Hàm xử lý đăng nhập
    private void loginUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE); // Hiển thị progress bar
        btnLogin.setEnabled(false); // Vô hiệu hóa nút đăng nhập

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            checkUserRole(firebaseUser.getUid()); // Kiểm tra role của người dùng
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại. Email hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Kiểm tra role của người dùng và lưu authorName vào SharedPreferences
    private void checkUserRole(String userId) {
        mDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        String role = user.getRole();
                        String authorName = user.getFullName();
                        String profileImageUrl = user.getImages(); // Lấy profileImageUrl từ Firebase
                        String email = user.getEmail(); // Lấy email từ Firebase (nếu có)

                        // Lưu authorName, profileImageUrl và email vào SharedPreferences
                        saveUserDataToSession(authorName, profileImageUrl, email);

                        // Sử dụng Handler để giả lập thời gian chờ
                        new Handler().postDelayed(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);

                            if ("admin".equalsIgnoreCase(role)) {
                                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                startActivity(intent);
                            } else if ("user".equalsIgnoreCase(role)) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                            finish(); // Đóng LoginActivity
                        }, 3000); // 3 giây
                    } else {
                        Toast.makeText(LoginActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối cơ sở dữ liệu: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
            }
        });
    }

    // Hàm lưu authorName, profileImageUrl và email vào SharedPreferences
    private void saveUserDataToSession(String authorName, String profileImageUrl, String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("authorName", authorName); // Lưu authorName
        editor.putString("profileImageUrl", profileImageUrl); // Lưu profileImageUrl
        editor.putString("email", email); // Lưu email
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
