package br.com.lucasbaiao.minharotina.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
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

    private static final int MINUTES = 1;
    private static final int SECONDS = 60;
    private static final int MILLI = 1000;
    private static final int APP_CHECK_INTERVAL = MINUTES * SECONDS * MILLI;

    private int getInterval() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getInt(SettingsActivity.KEY_PREF_SYNC_APP_INTERVAL, APP_CHECK_INTERVAL);
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
            Log.d(getClass().getSimpleName(), process == null ? "" : process);
            ForegroundApp foregroundApp = new ForegroundApp(Calendar.getInstance().getTimeInMillis(), process);
            AppDatabaseHelper.createNewAppForegroundRow(context, foregroundApp);
        }
    }
}
