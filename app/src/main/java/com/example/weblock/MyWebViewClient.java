package com.example.weblock;

import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // ⚠️ Akceptujemy wszystkie certyfikaty SSL (uwaga: niebezpieczne w produkcji!)
        handler.proceed();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true; // oznacza, że my obsłużyliśmy załadowanie
    }
}
