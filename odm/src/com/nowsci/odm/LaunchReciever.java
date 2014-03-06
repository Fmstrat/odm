package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.Logd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LaunchReciever extends BroadcastReceiver {
	private static final String TAG= "ODMLaunchReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String code = getResultData();
		if (code == null)
			code = intent.getStringExtra("android.intent.extra.PHONE_NUMBER");
		if (code.equals("###333###")) {
			Logd(TAG, "Launching.");
			setResultData(null);
			Intent i = new Intent(context, StartupActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
		}
	}

}
