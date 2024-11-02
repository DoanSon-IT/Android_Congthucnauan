package com.example.recipe_sharing_app;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class IntroMainActivity extends AppCompatActivity {

    private ImageView logoImageView;
    private TextView introTitle, introDescription;
    private ImageButton nextIcon ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.intro_activity);

        // Ánh xạ các view
        logoImageView = findViewById(R.id.logoImageView);
        introTitle = findViewById(R.id.introTitle);
        introDescription = findViewById(R.id.introDescription);
        nextIcon = findViewById(R.id.nextIcons);
        nextIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroMainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        // Gọi hàm để bắt đầu hiệu ứng fade-in
        startIntroAnimation();


    }

    private void startIntroAnimation() {
        // Tạo hiệu ứng fade-in cho logo
        AlphaAnimation fadeInLogo = new AlphaAnimation(0.0f, 1.0f);
        fadeInLogo.setDuration(1500);  // Thời gian chạy hiệu ứng là 1.5 giây
        fadeInLogo.setFillAfter(true); // Giữ trạng thái sau khi chạy xong

        // Tạo hiệu ứng fade-in cho tiêu đề
        AlphaAnimation fadeInTitle = new AlphaAnimation(0.0f, 1.0f);
        fadeInTitle.setDuration(1500);
        fadeInTitle.setFillAfter(true);

        // Tạo hiệu ứng fade-in cho mô tả
        AlphaAnimation fadeInDescription = new AlphaAnimation(0.0f, 1.0f);
        fadeInDescription.setDuration(1500);
        fadeInDescription.setFillAfter(true);

        // Tạo hiệu ứng fade-in cho icon tiếp theo
        AlphaAnimation fadeInNextIcon = new AlphaAnimation(0.0f, 1.0f);
        fadeInNextIcon.setDuration(1500);
        fadeInNextIcon.setFillAfter(true);

        // Bắt đầu hiệu ứng cho logo
        logoImageView.startAnimation(fadeInLogo);

        // Sử dụng Handler để delay việc bắt đầu các hiệu ứng của tiêu đề và mô tả
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                introTitle.startAnimation(fadeInTitle);
            }
        }, 1600); // Delay 1.6 giây để tiêu đề xuất hiện sau logo

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                introDescription.startAnimation(fadeInDescription);
            }
        }, 3200); // Delay 3.2 giây để mô tả xuất hiện sau tiêu đề

        // Hiển thị icon tiếp theo sau khi tất cả các phần khác đã xuất hiện
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                nextIcon.startAnimation(fadeInNextIcon);
                nextIcon.setVisibility(View.VISIBLE); // Hiển thị icon khi animation hoàn tất
            }
        }, 4800); // Delay 4.8 giây để icon xuất hiện cuối cùng
    }
}