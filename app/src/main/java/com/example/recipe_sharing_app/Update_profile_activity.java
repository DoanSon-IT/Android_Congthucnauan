package com.example.recipe_sharing_app;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.recipe_sharing_app.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Update_profile_activity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etFullName, etEmail, etPassword, etRole, tvUserId;
    private ImageView ivProfileImage;
    ImageButton btn_closes;
    private Button btnUpdate, btnChooseImage;
    private ProgressBar progressBar;

    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private StorageReference storageRef;

    private Uri imageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ các view từ layout
        etFullName = findViewById(R.id.et_name);

//        etFullName.setEnabled(false);
//        tvUserId.setEnabled(false);
//        etEmail.setEnabled(false);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);

        etRole = findViewById(R.id.et_role);
        ivProfileImage = findViewById(R.id.profile_image);
        btnUpdate = findViewById(R.id.btn_update);
        btnChooseImage = findViewById(R.id.btn_change_image);
        tvUserId = findViewById(R.id.et_cookpad_id);
        progressBar = findViewById(R.id.progress_bar);
        btn_closes = findViewById(R.id.btn_close);
        btn_closes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        auth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference("Users");
        storageRef = FirebaseStorage.getInstance().getReference("profile_images");

        loadUserData(); // Lấy dữ liệu người dùng hiện tại

        btnUpdate.setOnClickListener(v -> {
            // Khi nhấn nút "Update", hiện hộp thoại xác nhận
            new AlertDialog.Builder(Update_profile_activity.this)
                    .setTitle("Xác nhận cập nhật")
                    .setMessage("Bạn có chắc chắn muốn cập nhật thông tin không?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        // Hiển thị biểu tượng tải
                        progressBar.setVisibility(View.VISIBLE);
                        // Khi nhấn "Có", gọi hàm cập nhật thông tin
                        updateUserInfo();
                    })
                    .setNegativeButton("Không", (dialog, which) -> {
                        dialog.dismiss(); // Đóng hộp thoại
                    })
                    .show();
        });

        btnChooseImage.setOnClickListener(v -> {
            // Khi nhấn nút "Choose Image", mở thư viện ảnh
            openImageChooser();
        });
    }

    // Hàm mở thư viện ảnh
    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Hàm xử lý kết quả chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            ivProfileImage.setImageURI(imageUri);
        }
    }

    // Hàm lấy dữ liệu người dùng hiện tại
    private void loadUserData() {
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;

        if (userEmail == null) {
            Toast.makeText(this, "Lỗi: Không có người dùng hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);

                        if (user != null) {
                            // Hiển thị dữ liệu người dùng lên UI
                            etFullName.setText(user.getFullName());
                            etEmail.setText(user.getEmail());
                            etPassword.setText(user.getPassword());
                            etRole.setText(user.getRole());

                            // Hiển thị hình ảnh người dùng
                            Glide.with(Update_profile_activity.this)
                                    .load(user.getImages())
                                    .placeholder(R.drawable.chef_icon_removebg_preview) // Hình ảnh thay thế
                                    .into(ivProfileImage);

                            // Hiển thị ID người dùng
                            tvUserId.setText("@cook_"+ userSnapshot.getKey());
                        }
                    }
                } else {
                    Toast.makeText(Update_profile_activity.this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Update_profile_activity.this, "Lỗi khi tải dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm cập nhật thông tin người dùng
    private void updateUserInfo() {
        String fullName = etFullName.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String role = etRole.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(role)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // Ẩn biểu tượng tải nếu có lỗi
            return;
        }

        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;

        if (userEmail == null) {
            Toast.makeText(this, "Lỗi: Không có người dùng hiện tại", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE); // Ẩn biểu tượng tải nếu có lỗi
            return;
        }

        userRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);

                        if (user != null) {
                            // Cập nhật thông tin
                            userSnapshot.getRef().child("fullName").setValue(fullName);
                            userSnapshot.getRef().child("password").setValue(password);
                            userSnapshot.getRef().child("role").setValue(role);

                            if (imageUri != null) {
                                uploadImage(userSnapshot.getRef().getKey());
                            } else {
                                // Ẩn biểu tượng tải sau khi cập nhật thành công
                                progressBar.postDelayed(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(Update_profile_activity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                                }, 2000); // Hiển thị biểu tượng tải trong 2 giây
                            }
                        }
                    }
                } else {
                    Toast.makeText(Update_profile_activity.this, "Không tìm thấy dữ liệu người dùng", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Ẩn biểu tượng tải nếu có lỗi
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Update_profile_activity.this, "Lỗi khi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE); // Ẩn biểu tượng tải nếu có lỗi
            }
        });
    }

    // Hàm upload ảnh lên Firebase Storage và cập nhật URL vào Realtime Database
    private void uploadImage(String userId) {
        StorageReference fileRef = storageRef.child(userId + ".jpg");

        fileRef.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    fileRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                String imageUrl = task.getResult().toString();
                                userRef.child(userId).child("images").setValue(imageUrl)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Ẩn biểu tượng tải sau khi cập nhật thành công
                                                    progressBar.postDelayed(() -> {
                                                        progressBar.setVisibility(View.GONE);
                                                        Toast.makeText(Update_profile_activity.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                                                    }, 2000); // Hiển thị biểu tượng tải trong 2 giây
                                                } else {
                                                    Toast.makeText(Update_profile_activity.this, "Lỗi khi cập nhật ảnh", Toast.LENGTH_SHORT).show();
                                                    progressBar.setVisibility(View.GONE); // Ẩn biểu tượng tải nếu có lỗi
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(Update_profile_activity.this, "Lỗi khi lấy URL ảnh", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE); // Ẩn biểu tượng tải nếu có lỗi
                            }
                        }
                    });
                } else {
                    Toast.makeText(Update_profile_activity.this, "Lỗi khi upload ảnh", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE); // Ẩn biểu tượng tải nếu có lỗi
                }
            }
        });
    }
}
