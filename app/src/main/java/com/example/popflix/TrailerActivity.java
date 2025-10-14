package com.example.popflix;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.popflix.databinding.ActivityTrailerBinding;

public class TrailerActivity extends AppCompatActivity {

    private ActivityTrailerBinding binding;
    private String videoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrailerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get video URL from intent
        videoUrl = getIntent().getStringExtra("videoUrl");
        if (videoUrl == null || videoUrl.isEmpty()) {
            finish();
            return;
        }

        setupToolbar();
        setupWebView();
        loadVideo();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Trailer");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                binding.progressBar.setVisibility(View.GONE);
                binding.webView.setVisibility(View.VISIBLE);
            }
        });

        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                binding.progressBar.setProgress(newProgress);
            }
        });
    }

    private void loadVideo() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.webView.setVisibility(View.GONE);

        // Convert YouTube URL to embed URL for better mobile experience
        String embedUrl = convertToEmbedUrl(videoUrl);
        binding.webView.loadUrl(embedUrl);
    }

    private String convertToEmbedUrl(String youtubeUrl) {
        if (youtubeUrl.contains("youtube.com/watch?v=")) {
            String videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
            if (videoId.contains("&")) {
                videoId = videoId.substring(0, videoId.indexOf("&"));
            }
            return "https://www.youtube.com/embed/" + videoId + "?autoplay=1&modestbranding=1&rel=0";
        } else if (youtubeUrl.contains("youtu.be/")) {
            String videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
            return "https://www.youtube.com/embed/" + videoId + "?autoplay=1&modestbranding=1&rel=0";
        }
        return youtubeUrl;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binding.webView != null) {
            binding.webView.destroy();
        }
        binding = null;
    }
}
