package com.nowsci.odm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class IconActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = new Intent(getApplicationContext(), StartupActivity.class);
		this.startActivity(intent);
		finish();
	}
		
}
