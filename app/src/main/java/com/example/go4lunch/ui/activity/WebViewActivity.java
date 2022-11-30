package com.example.go4lunch.ui.activity;

import android.os.Bundle;
import android.webkit.WebViewClient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.go4lunch.R;
import com.example.go4lunch.databinding.ActivityWebViewBinding;
import com.example.go4lunch.databinding.ToolbarBinding;

public class WebViewActivity extends AppCompatActivity {

    private ActivityWebViewBinding binding;
    private ToolbarBinding toolbarBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        
        
        configureToolbar();
        configureSwipeRefreshLayout();
        displayWebView();
    }

    private void configureSwipeRefreshLayout() {
        binding.webViewSwipeRefresh.setOnRefreshListener(this::displayWebView);
    }

    private void displayWebView() {
        String url = getIntent().getStringExtra("Website");
        if (url != null){
            binding.webView.getSettings().setJavaScriptEnabled(true);
            binding.webView.getSettings().setLoadsImagesAutomatically(true);
            binding.webView.loadUrl(url);
            binding.webView.setWebViewClient(new WebViewClient());
            binding.webViewSwipeRefresh.setRefreshing(false);
        }else{
            binding.webViewSwipeRefresh.setRefreshing(false);

        }

    }

    private void configureToolbar() {
        setSupportActionBar(toolbarBinding.simpleToolbar);
        ActionBar actionBar = getSupportActionBar();
    }
}
