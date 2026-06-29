package com.nasremote;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private SharedPreferences prefs;
    private String baseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("nasremote", MODE_PRIVATE);
        baseUrl = prefs.getString("url", "http://192.168.68.53:7070");
        setContentView(R.layout.activity_main);
        webView = findViewById(R.id.webview);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(baseUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, "Changer IP");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 1) { showIpDialog(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void showIpDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 10);
        TextView label = new TextView(this);
        label.setText("URL du NAS Remote :");
        layout.addView(label);
        EditText input = new EditText(this);
        input.setText(baseUrl);
        layout.addView(input);
        new AlertDialog.Builder(this)
            .setTitle("Configuration")
            .setView(layout)
            .setPositiveButton("Sauvegarder", (d, w) -> {
                String newUrl = input.getText().toString().trim();
                if (!newUrl.startsWith("http")) newUrl = "http://" + newUrl;
                baseUrl = newUrl;
                prefs.edit().putString("url", baseUrl).apply();
                webView.loadUrl(baseUrl);
            })
            .setNegativeButton("Annuler", null)
            .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String key = null;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) key = "vol_up";
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) key = "vol_down";
        if (key != null) {
            final String k = key;
            new Thread(() -> {
                try {
                    java.net.URL u = new java.net.URL(baseUrl + "/key?k=" + k);
                    u.openConnection().getInputStream().close();
                } catch (Exception e) {}
            }).start();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
    }
}
