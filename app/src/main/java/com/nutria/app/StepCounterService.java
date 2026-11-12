package com.nutria.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {
    public static final String PREFS = "nutria_steps";
    private static final String CHANNEL = "nutria_activity";
    private SensorManager sensorManager;
    private Sensor stepSensor;

    public static String todayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        Notification.Builder builder = Build.VERSION.SDK_INT >= 26
            ? new Notification.Builder(this, CHANNEL)
            : new Notification.Builder(this);
        Notification n = builder
            .setContentTitle("NUTRIA está contando tus pasos")
            .setContentText("Actividad diaria registrada en segundo plano")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build();
        startForeground(21, n);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        stepSensor = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (stepSensor == null || !sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)) {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean("tracking_enabled", false).apply();
            stopSelf();
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CHANNEL, "Actividad de NUTRIA", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
    }

    @Override public int onStartCommand(android.content.Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_STEP_COUNTER || event.values.length == 0) return;
        float current = event.values[0];
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String today = todayKey();
        String storedDay = prefs.getString("day", "");
        int todaySteps = prefs.getInt("steps_" + today, 0);
        float previousSensor = prefs.getFloat("last_sensor", -1f);
        if (!today.equals(storedDay)) todaySteps = 0;
        int delta = (previousSensor < 0 || current < previousSensor)
            ? 0 : Math.max(0, Math.round(current - previousSensor));
        todaySteps += delta;
        prefs.edit().putFloat("last_sensor", current)
            .putInt("steps_" + today, todaySteps).putString("day", today).apply();
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override public void onDestroy() {
        if (sensorManager != null) sensorManager.unregisterListener(this);
        super.onDestroy();
    }

    @Override public IBinder onBind(android.content.Intent intent) { return null; }
}
