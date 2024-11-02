package com.example.recipe_sharing_app;



import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.recipe_sharing_app.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Find the WebView by its unique ID
        WebView webView = findViewById(R.id.web);

        // loading https://www.geeksforgeeks.org url in the WebView.
        webView.loadUrl("https://console.firebase.google.com/u/0/project/cooks-d1f2c/overview");

        // this will enable the javascript.
        webView.getSettings().setJavaScriptEnabled(true);
        // WebViewClient allows you to handle
        // onPageFinished and override Url loading.
        webView.setWebViewClient(new WebViewClient());
    }
}
