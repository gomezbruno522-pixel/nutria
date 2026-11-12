package com.nutria.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;
    private static final int PERMISSION_REQUEST = 44;
    private static final int NOTIFICATION_REQUEST = 45;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        webView = new WebView(this);
        webView.setBackgroundColor(0xFF111213);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new StepBridge(this), "AndroidSteps");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
        requestPermissionsIfNeeded();
    }

    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= 29 &&
            checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERMISSION_REQUEST);
        }
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        // El servicio se inicia cuando el usuario toca Iniciar dentro de NUTRIA.
    }

    private boolean hasActivityPermission() {
        return Build.VERSION.SDK_INT < 29 ||
            checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasStepSensor() {
        SensorManager manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        return manager != null && manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null;
    }

    private void startCounter() {
        Intent i = new Intent(this, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i); else startService(i);
    }

    public class StepBridge {
        private final Activity activity;
        StepBridge(Activity a) { activity = a; }

        @JavascriptInterface public boolean startTracking() {
            if (!hasActivityPermission() || !hasStepSensor()) return false;
            activity.getSharedPreferences(StepCounterService.PREFS, MODE_PRIVATE)
                .edit().putBoolean("tracking_enabled", true).apply();
            activity.runOnUiThread(() -> startCounter());
            return true;
        }

        @JavascriptInterface public void pauseTracking() {
            activity.getSharedPreferences(StepCounterService.PREFS, MODE_PRIVATE)
                .edit().putBoolean("tracking_enabled", false).apply();
            activity.runOnUiThread(() -> activity.stopService(new Intent(activity, StepCounterService.class)));
        }

        @JavascriptInterface public int getTodaySteps() {
            return activity.getSharedPreferences(StepCounterService.PREFS, MODE_PRIVATE)
                .getInt("steps_" + StepCounterService.todayKey(), 0);
        }
    }

    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
