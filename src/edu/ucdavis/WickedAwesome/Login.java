/* begin: Login.java */
package edu.ucdavis.WickedAwesome;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity implements OnClickListener {

    private Button buttonLogin, buttonSetup;
    private Button btnViewDataTest; // "Test" button to view wireless data
    private EditText emailEditText, passwordEditText;
    private String email, password;
    private Intent i;
    private PostData data = new PostData();

    private static final String URL = "http://mmayfield.com/ecs152c/login.php";

    private static final String TAG = "Login****";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.login);

	buttonLogin = (Button) findViewById(R.id.login_button);
	buttonLogin.setOnClickListener(this);
	buttonSetup = (Button) findViewById(R.id.setup_button);
	buttonSetup.setOnClickListener(this);

	emailEditText = (EditText) findViewById(R.id.txt_username);
	passwordEditText = (EditText) findViewById(R.id.txt_password);

	btnViewDataTest = (Button) findViewById(R.id.btnViewDataTest);
	btnViewDataTest.setOnClickListener(this);
	// Placed here so we don't need them in both Join and Host activities
	SystemInfo.GWDhelper = new GetWirelessData(this);
	SystemInfo.GWDhelper.initAPList();
	SystemInfo.GWDhelper.listenMyGps(); // turn on GPS listener
    }

    @Override
    public void onClick(View v) {
	switch (v.getId()) {
	case R.id.login_button: {
	    Log.d(TAG, "Begin Login");
	    email = emailEditText.getText().toString();
	    password = passwordEditText.getText().toString();
	    postData();
	}
	    break;
	case R.id.setup_button: {
	    i = new Intent(this, Register.class);
	    startActivity(i);
	}
	    break;
	case R.id.btnViewDataTest: {
	    i = new Intent(this, ViewWirelessData.class);
	    startActivity(i);
	}
	    break;
	}
    }

    public void postData() {
	// So you can tell it's doing something
	ProgressDialog dialog = ProgressDialog.show(this, "", "Signing In...",
		true);

	// Add your data
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	nameValuePairs
		.add(new BasicNameValuePair("key", SystemInfo.SERVER_KEY));
	nameValuePairs.add(new BasicNameValuePair("email", email));
	nameValuePairs.add(new BasicNameValuePair("password", password));

	// Get the post data to fill the friends array
	JSONObject result = data.post(nameValuePairs, URL);

	try {
	    // Create the array of names
	    JSONArray res = result.getJSONArray("response");
	    if (res.getString(0).equals("0")) {
		dialog.dismiss();
		Toast.makeText(Login.this, "Error, Try Again!",
			Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Login FAILED: " + email);
	    } else {
		dialog.dismiss();
		Toast.makeText(Login.this, "Success!", Toast.LENGTH_SHORT)
			.show();
		SystemInfo.email = email;
		SystemInfo.password = password;
		Log.d(TAG, "Login: " + email);

		String ingame = res.getString(0);
		if (ingame.equals("1")) { // currently hosting game
		    SystemInfo.inGame = true;
		    SystemInfo.isHost = true;
		    i = new Intent(this, HostList.class);
		    startActivity(i);
		} else if (ingame.equals("2")) { // currently joined to game
		    SystemInfo.inGame = true;
		    SystemInfo.isHost = false;
		    //i = new Intent(this, Play_Join2.class);
		    i = new Intent(this, Play_Join.class);
		    startActivity(i);
		} else { // no game
		    i = new Intent(this, WickedAwesome.class);
		    startActivity(i);
		}
	    }
	} catch (JSONException ex) {
	}
    } // end Post Data
}
/* end: Login.java */