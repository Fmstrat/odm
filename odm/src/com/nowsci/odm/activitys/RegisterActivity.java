package com.nowsci.odm.activitys;

import static com.nowsci.odm.misc.CommonUtilities.getVAR;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.nowsci.odm.AlertDialogManager;
import com.nowsci.odm.ConnectionDetector;
import com.nowsci.odm.misc.CommonUtilities;

import com.nowsci.odm.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ToggleButton;

public class RegisterActivity extends Activity {

	private static final String TAG = "RegisterActivity";

	// alert dialog manager
	AlertDialogManager alert = new AlertDialogManager();
	// Internet detector
	ConnectionDetector cd;
	// UI elements
	EditText txtName;
	EditText txtServerUrl;
	EditText txtUsername;
	EditText txtEncKey;
	EditText txtPin;
	ToggleButton tglValidSSL;
	ToggleButton tglDebug;
	ToggleButton tglVersion;
	Button btnRegister;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_register);
		cd = new ConnectionDetector(getApplicationContext());
		// Check if Internet present
		if (!cd.isConnectingToInternet()) {
			// Internet Connection is not present
			alert.showAlertDialog(RegisterActivity.this, getApplicationContext().getString(R.string.main_internet_required_head), getApplicationContext().getString(R.string.main_internet_required), false);
			// stop executing code by return
			return;
		}
		txtName = (EditText) findViewById(R.id.txtName);
		txtName.setText(android.os.Build.MODEL); // default text!
		txtName.setHint(android.os.Build.MODEL);
		txtServerUrl = (EditText) findViewById(R.id.txtServerUrl);
		txtUsername = (EditText) findViewById(R.id.txtUsername);
		txtEncKey = (EditText) findViewById(R.id.txtEncKey);
		txtPin = (EditText) findViewById(R.id.txtPin);
		tglValidSSL = (ToggleButton) findViewById(R.id.tglValidSSL);
		tglDebug = (ToggleButton) findViewById(R.id.tglDebug);
		tglVersion = (ToggleButton) findViewById(R.id.tglVersion);
		btnRegister = (Button) findViewById(R.id.btnRegister);
		
		// Enable hidden fields
		if(CommonUtilities.APP_ALL_OPTIONS) {
			findViewById(R.id.txtServerUrlLbl).setVisibility(View.VISIBLE);
			txtServerUrl.setVisibility(View.VISIBLE);			
			findViewById(R.id.layout_checkssl).setVisibility(View.VISIBLE);
			findViewById(R.id.layout_checkversion).setVisibility(View.VISIBLE);
			findViewById(R.id.layout_debugadb).setVisibility(View.VISIBLE);
		}
		
		if(! "".equals(getVAR("NAME")))
			txtName.setText(getVAR("NAME"));
		String su = getVAR("SERVER_URL");
		String s;
		if (su.length() > 5)
			s = su.substring(0, (su.length() - 4));
		else
			s = su;
		if(! "".equals(s))
			txtServerUrl.setText(s);
		txtUsername.setText(getVAR("USERNAME"));
		txtEncKey.setText(getVAR("ENC_KEY"));
		txtPin.setText(getVAR("PIN"));
		if (!getVAR("VALID_SSL").equals("false"))
			tglValidSSL.setChecked(true);
		else
			tglValidSSL.setChecked(false);
		if (getVAR("DEBUG").equals("true"))
			tglDebug.setChecked(true);
		else
			tglDebug.setChecked(false);
		if (!getVAR("VERSION").equals("false"))
			tglVersion.setChecked(true);
		else
			tglVersion.setChecked(false);

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
			alert.showAlertDialog(RegisterActivity.this, getApplicationContext().getString(R.string.reg_config_error_head), getApplicationContext().getString(R.string.reg_config_error), false);
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
				String pin = txtPin.getText().toString();
				String validssl = "";
				String debug = "";
				String version = "";
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
					if (name.trim().length() > 0 && serverurl.trim().length() > 0 && username.trim().length() > 0 && enckey.trim().length() > 0) {
						// Launch Main Activity to register user on server and send registration details
						String SERVER_URL = "";
						if (serverurl.endsWith("/")) {
							SERVER_URL = serverurl + "api/";
						} else {
							SERVER_URL = serverurl + "/api/";
						}
						CommonUtilities.setVAR("SERVER_URL", SERVER_URL);
						CommonUtilities.setVAR("USERNAME", username);
						CommonUtilities.setVAR("VALID_SSL", validssl);
						CommonUtilities.setVAR("DEBUG", debug);
						CommonUtilities.setVAR("ENC_KEY", enckey);
						CommonUtilities.setVAR("NAME", name);
						CommonUtilities.setVAR("VERSION", version);
						CommonUtilities.setVAR("PIN", pin);
						Intent intent = new Intent(getApplicationContext(), StartupActivity.class);
						intent.putExtra("VERSION_CHECK", false);
						startActivity(intent);
						finish();
					} else {
						// User hasn't filled in data
						alert.showAlertDialog(RegisterActivity.this, getApplicationContext().getString(R.string.reg_registration_error_head), getApplicationContext().getString(R.string.reg_registration_error_enter_details), false);
					}
				} else {
					alert.showAlertDialog(RegisterActivity.this, getApplicationContext().getString(R.string.reg_registration_error_head), getApplicationContext().getString(R.string.reg_registration_error_wrong_url), false);				
				}
			}
		});
	}

}
