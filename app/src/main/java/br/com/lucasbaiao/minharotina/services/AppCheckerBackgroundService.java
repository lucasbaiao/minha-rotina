package br.com.lucasbaiao.minharotina.services;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rvalerio.fgchecker.AppChecker;

import java.util.Calendar;

import br.com.lucasbaiao.minharotina.persistence.AppDatabaseHelper;
import br.com.lucasbaiao.minharotina.persistence.model.ForegroundApp;
import br.com.lucasbaiao.minharotina.view.SettingsActivity;

public class AppCheckerBackgroundService extends IntentService {

    private static final int DEFAULT_INTERVAL = 1;
    private static final int SECONDS = 60;
    private static final int MILLI = SECONDS * 1000;
    private static ScreenStatus mScreenState = ScreenStatus.ON;
    private ScreenReceiver mScreenReceiver;
    private final ScreenListener mListener = new ScreenListener() {

        @Override
        public void onStateChange(ScreenStatus status) {
            AppCheckerBackgroundService.mScreenState = status;
        }
    };

    private int getInterval() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String interval = sharedPref.getString(SettingsActivity.KEY_PREF_SYNC_APP_INTERVAL, "" + DEFAULT_INTERVAL);
        Log.d(getClass().getSimpleName(), "App checker interval: " + interval);

        try {
            return Integer.parseInt(interval) * MILLI;
        }catch (Exception ex) {
            return DEFAULT_INTERVAL * MILLI;
        }
    }

    public AppCheckerBackgroundService() {
        super("AppCheckerBackgroundService");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.startChecker();
        this.registerScreenReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.mScreenReceiver != null) {
            unregisterReceiver(this.mScreenReceiver);
        }
    }

    private void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mScreenReceiver = new ScreenReceiver(mListener);
        registerReceiver(mScreenReceiver, filter);
    }

    private void startChecker() {
        Log.d(getClass().getSimpleName(), "Starting process app checker monitor");
        AppChecker checker = new AppChecker();
        checker.timeout(getInterval())
                .other(new AppCheckerListener(getApplicationContext()))
                .start(getApplicationContext());
    }

    private static class AppCheckerListener implements AppChecker.Listener {
        private final Context context;

        private AppCheckerListener(Context context) {
            this.context = context;
        }

        @Override
        public void onForeground(String process) {
            if (AppCheckerBackgroundService.mScreenState == ScreenStatus.ON) {
                Log.d(getClass().getSimpleName(), process == null ? "" : process);
                ForegroundApp foregroundApp = new ForegroundApp(Calendar.getInstance().getTimeInMillis(), process);
                AppDatabaseHelper.createNewAppForegroundRow(context, foregroundApp);
            } else {
                Log.d(getClass().getSimpleName(), "Screen is off. Do anything");
            }
        }
    }

    private static class ScreenReceiver extends BroadcastReceiver {

        final ScreenListener listener;

        private ScreenReceiver(ScreenListener listener) {
            this.listener = listener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getClass().getSimpleName(), "Screen receiver, receive status " + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                this.listener.onStateChange(ScreenStatus.OFF);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                this.listener.onStateChange(ScreenStatus.ON);
            }
        }
    }

    private interface ScreenListener {
        void onStateChange(ScreenStatus status);
    }
    private enum ScreenStatus {
        ON, OFF
    }
}
