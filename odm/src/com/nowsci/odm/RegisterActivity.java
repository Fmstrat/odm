package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.getVAR;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class RegisterActivity extends Activity {
	// alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();
	// Internet detector
	ConnectionDetector cd;
	// UI elements
	EditText txtName;
	EditText txtServerUrl;
	EditText txtUsername;
	EditText txtEncKey;
	ToggleButton tglValidSSL;
	ToggleButton tglDebug;
	Button btnRegister;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		cd = new ConnectionDetector(getApplicationContext());
		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(RegisterActivity.this, "Internet Connection Error", "Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}
		txtName = (EditText) findViewById(R.id.txtName);
		txtServerUrl = (EditText) findViewById(R.id.txtServerUrl);
		txtUsername = (EditText) findViewById(R.id.txtUsername);
		txtEncKey = (EditText) findViewById(R.id.txtEncKey);
		tglValidSSL = (ToggleButton) findViewById(R.id.tglValidSSL);
		tglDebug = (ToggleButton) findViewById(R.id.tglDebug);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		txtName.setText(getVAR("NAME"));
		String su = getVAR("SERVER_URL");
		String s = su.substring(0, (su.length() - 4));
		txtServerUrl.setText(s);
		txtUsername.setText(getVAR("USERNAME"));
		txtEncKey.setText(getVAR("ENC_KEY"));
		if (!getVAR("VALID_SSL").equals("false"))
			tglValidSSL.setChecked(true);
		else
			tglValidSSL.setChecked(false);
		if (getVAR("DEBUG").equals("true"))
			tglDebug.setChecked(true);
		else
			tglDebug.setChecked(false);

		// These lines are for debugging only.
		// NOTE: FORDEVEL
		/*
		txtName.setText("Test Device");
		txtServerUrl.setText("https://HOST/odm");
		txtUsername.setText("testuser");
		txtEncKey.setText("password");
		*/

		// Check if GCM configuration is set
		String SENDER_ID = getVAR("SENDER_ID");
		if (SENDER_ID == null || SENDER_ID.length() == 0) {
			// GCM sender id is missing
			alert.showAlertDialog(RegisterActivity.this, "Configuration Error!", "Please set your GCM Sender ID", false);
			// stop executing code by return
			return;
		}

		// Click event on Register button
		btnRegister.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String name = txtName.getText().toString();
				String serverurl = txtServerUrl.getText().toString();
				String username = txtUsername.getText().toString();
				String enckey = txtEncKey.getText().toString();
				String validssl = "";
				String debug = "";
				if (tglValidSSL.isChecked())
					validssl = "true";
				else
					validssl = "false";
				if (tglDebug.isChecked())
					debug = "true";
				else
					debug = "false";
				// Check if user filled the form
				if (name.trim().length() > 0 && serverurl.trim().length() > 0 && username.trim().length() > 0 && enckey.trim().length() > 0) {
					// Launch Main Activity to register user on server and send registration details
					SharedPreferences mPrefs = getSharedPreferences("usersettings", 0);
					SharedPreferences.Editor mEditor = mPrefs.edit();
					String SERVER_URL = "";
					if (serverurl.endsWith("/")) {
						SERVER_URL = serverurl + "api/";
					} else {
						SERVER_URL = serverurl + "/api/";
					}
					mEditor.putString("SERVER_URL", SERVER_URL).commit();
					mEditor.putString("USERNAME", username).commit();
					mEditor.putString("VALID_SSL", validssl).commit();
					mEditor.putString("DEBUG", debug).commit();
					mEditor.putString("ENC_KEY", enckey).commit();
					mEditor.putString("NAME", name).commit();
					Intent intent = new Intent(getApplicationContext(), StartupActivity.class);
					startActivity(intent);
					finish();
				} else {
					// User hasn't filled in data
					alert.showAlertDialog(RegisterActivity.this, "Registration Error!", "Please enter your details", false);
				}
			}
		});
	}

}
