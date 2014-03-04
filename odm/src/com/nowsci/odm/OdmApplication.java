package com.nowsci.odm;

import android.app.Application;
import android.content.Context;

public class OdmApplication extends Application{
	 private static Context context;

    public void onCreate(){
        super.onCreate();
        OdmApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return OdmApplication.context;
    }
}
