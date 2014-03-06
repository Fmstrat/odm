package com.nowsci.odm;

import static com.nowsci.odm.CommonUtilities.getVAR;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class RegisterActivity extends Activity {

	private static final String TAG= "ODMRegisterActivity";

	// alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();
	// Internet detector
	ConnectionDetector cd;
	// UI elements
	EditText txtName;
	EditText txtServerUrl;
	EditText txtUsername;
	EditText txtEncKey;
	ToggleButton tglHideIcon;
	ToggleButton tglValidSSL;
	ToggleButton tglDebug;
	ToggleButton tglVersion;
	ToggleButton tglNetworkOnly;
	Button btnRegister;
	EditText txtInterval;

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
		tglHideIcon = (ToggleButton) findViewById(R.id.tglHideIcon);
		tglValidSSL = (ToggleButton) findViewById(R.id.tglValidSSL);
		tglDebug = (ToggleButton) findViewById(R.id.tglDebug);
		tglVersion = (ToggleButton) findViewById(R.id.tglVersion);
		tglNetworkOnly = (ToggleButton) findViewById(R.id.tglNetworkOnly);
		txtInterval = (EditText) findViewById(R.id.txtInterval);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		txtName.setText(getVAR("NAME"));
		String su = getVAR("SERVER_URL");
		String s;
		if (su.length() > 5)
			s = su.substring(0, (su.length() - 4));
		else
			s = su;
		txtServerUrl.setText(s);
		txtUsername.setText(getVAR("USERNAME"));
		txtEncKey.setText(getVAR("ENC_KEY"));
		if (getVAR("HIDE_ICON").equals("true"))
			tglHideIcon.setChecked(true);
		else
			tglHideIcon.setChecked(false);
		if (!getVAR("VALID_SSL").equals("false"))
			tglValidSSL.setChecked(true);
		else
			tglValidSSL.setChecked(false);
		if (getVAR("DEBUG").equals("true"))
			tglDebug.setChecked(true);
		else
			tglDebug.setChecked(false);
		if (getVAR("VERSION").equals("true"))
			tglVersion.setChecked(true);
		else
			tglVersion.setChecked(false);
		if (getVAR("NETWORK_ONLY").equals("true"))
			tglNetworkOnly.setChecked(true);
		else
			tglNetworkOnly.setChecked(false);
		txtInterval.setText(getVAR("INTERVAL"));
		if (getVAR("INTERVAL").equals("0"))
			tglNetworkOnly.setEnabled(false);
		txtInterval.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (s.toString().equals("0") || s.toString().equals("")) {
					tglNetworkOnly.setEnabled(false);
				} else {
					tglNetworkOnly.setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});
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
				String version = "";
				String hideicon = "";
				String networkonly = "";
				if (tglHideIcon.isChecked())
					hideicon = "true";
				else
					hideicon = "false";
				if (tglValidSSL.isChecked())
					validssl = "true";
				else
					validssl = "false";
				if (tglDebug.isChecked())
					debug = "true";
				else
					debug = "false";
				if (tglVersion.isChecked())
					version = "true";
				else
					version = "false";
				if (tglNetworkOnly.isChecked())
					networkonly = "true";
				else
					networkonly = "false";
				String interval = txtInterval.getText().toString();
				if (interval.equals("")) {
					interval = "0";
				}
				Boolean cont = false;
				try {
					URL u = new URL(serverurl);
					u.toURI();
					cont = true;
				} catch (MalformedURLException e) {
					Log.d(TAG, e.getMessage());
				} catch (URISyntaxException e) {
					Log.d(TAG, e.getMessage());
				}
				if (cont) {
					// Check if user filled the form
					if (interval.trim().length() > 0 && name.trim().length() > 0 && serverurl.trim().length() > 0 && username.trim().length() > 0 && enckey.trim().length() > 0) {
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
						mEditor.putString("HIDE_ICON", hideicon).commit();
						mEditor.putString("NAME", name).commit();
						mEditor.putString("VERSION", version).commit();
						mEditor.putString("INTERVAL", interval).commit();
						mEditor.putString("NETWORK_ONLY", networkonly).commit();
						Intent intent = new Intent(getApplicationContext(), StartupActivity.class);
						intent.putExtra("VERSION_CHECK", false);
						startActivity(intent);
						finish();
					} else {
						// User hasn't filled in data
						alert.showAlertDialog(RegisterActivity.this, "Registration Error!", "Please enter your details", false);
					}
				} else {
					alert.showAlertDialog(RegisterActivity.this, "Registration Error!", "Please enter a valid URL in the form http(s)://host.ext/dir", false);
				}
			}
		});
	}

}
