package br.com.lucasbaiao.minharotina.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.d(getClass().getSimpleName(), "Boot completed detected... Starting app checker service!!!");
            Intent pushIntent = new Intent(context, AppCheckerBackgroundService.class);
            context.startService(pushIntent);
        }
    }
}