package com.nutria.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        if (Build.VERSION.SDK_INT >= 29 &&
            context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) return;
        boolean enabled = context.getSharedPreferences(StepCounterService.PREFS, Context.MODE_PRIVATE)
            .getBoolean("tracking_enabled", false);
        if (!enabled) return;
        Intent service = new Intent(context, StepCounterService.class);
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(service);
        else context.startService(service);
    }
}
