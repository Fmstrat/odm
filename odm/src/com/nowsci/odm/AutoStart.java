package com.nowsci.odm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStart extends BroadcastReceiver
{   
    UpdateAlarm updateAlarm = new UpdateAlarm();
    @Override
    public void onReceive(Context context, Intent intent)
    {   
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            updateAlarm.SetAlarm(context);
        }
    }
}
