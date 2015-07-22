package com.jeremy.cameragmail;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

public class NetworkReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isNetworkDown = intent.getBooleanExtra(
                ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        Log.d("NetworkReceiver", "action: "
                + intent.getAction() + ", isDown: " + isNetworkDown);
        if(!isNetworkDown)
            context.startService(new Intent(context, SenderIntentService.class));
    }

}
