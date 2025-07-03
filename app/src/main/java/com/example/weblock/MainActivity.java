package com.example.weblock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;

public class MainActivity extends Activity {

    private static final String PREFS_NAME = "WebBlockPrefs";
    private static final String KEY_URL = "saved_url";
    private static final String PASSWORD = "12345";

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private GestureDetector gestureDetector;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        }

        // Ukryj paski systemowe
        hideSystemUI();

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        webView = findViewById(R.id.webview);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        webView.setWebViewClient(new MyWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Pobierz zapisany URL z SharedPreferences albo ustaw domyślny
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUrl = prefs.getString(KEY_URL,
                "https://aplikuj.hrappka.pl/work-time-register?widget_hash=dba2651f5087cc485c15bc9bbdce39cc");

        webView.loadUrl(savedUrl);

        swipeRefreshLayout.setOnRefreshListener(webView::reload);

        swipeRefreshLayout.setRefreshing(true); // pokaż loader przy starcie

        // Inicjalizacja GestureDetector z wykrywaniem długiego przytrzymania
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                showPasswordDialog();
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true; // musi zwracać true, żeby długie przytrzymanie działało
            }
        });

        // Przekazujemy eventy dotyku do gestureDetector
        webView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return false; // pozwól WebView normalnie obsłużyć dotyk
        });
    }

    private void showPasswordDialog() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Podaj hasło");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String enteredPassword = input.getText().toString();
                if (PASSWORD.equals(enteredPassword)) {
                    showUrlInputDialog();
                } else {
                    Toast.makeText(MainActivity.this, "Niepoprawne hasło", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    private void showUrlInputDialog() {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Wpisz nowy URL");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
            builder.setView(input);

            builder.setPositiveButton("Załaduj", (dialog, which) -> {
                String newUrl = input.getText().toString().trim();
                if (!newUrl.isEmpty()) {
                    // Zapisz do SharedPreferences
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    prefs.edit().putString(KEY_URL, newUrl).apply();

                    webView.loadUrl(newUrl);
                }
            });
            builder.setNegativeButton("Anuluj", (dialog, which) -> dialog.cancel());

            builder.show();
        });
    }

    // Tryb immersyjny (pełny ekran)
    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }
}
