/* begin: Register.java */
package edu.ucdavis.WickedAwesome;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Register extends Activity implements OnClickListener {
    private EditText email, fname, lname, password;
    private Button submit;
    private String bid;
    private PostData data = new PostData();

    private static final String URL = "http://mmayfield.com/ecs152c/register.php";

    private static final String TAG = "Register****";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.register);

	submit = (Button) findViewById(R.id.setup_button);
	submit.setOnClickListener(this);

	email = (EditText) findViewById(R.id.txt_username);
	fname = (EditText) findViewById(R.id.txt_fname);
	lname = (EditText) findViewById(R.id.txt_lname);
	password = (EditText) findViewById(R.id.txt_password);

	bid = SystemInfo.GWDhelper.getMyBlue(); // bluetooth ID
    }

    @Override
    public void onClick(View v) {
	register();
    }

    public void register() {
	Log.d(TAG, "Begin register new user");
	// Checks if everything is filled in
	if (email.getText().toString().equals("")
		|| password.getText().toString().equals("")
		|| lname.getText().toString().equals("")
		|| fname.getText().toString().equals("")) {
	    Toast.makeText(Register.this, "Missing information!",
		    Toast.LENGTH_SHORT).show();
	} else {
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(new BasicNameValuePair("key",
		    SystemInfo.SERVER_KEY));
	    nameValuePairs.add(new BasicNameValuePair("email", email.getText()
		    .toString()));
	    nameValuePairs.add(new BasicNameValuePair("fname", fname.getText()
		    .toString()));
	    nameValuePairs.add(new BasicNameValuePair("lname", lname.getText()
		    .toString()));
	    nameValuePairs.add(new BasicNameValuePair("bid", bid));
	    nameValuePairs.add(new BasicNameValuePair("password", password
		    .getText().toString()));

	    Log.d(TAG, "Contacting server");

	    // Get the post data to fill the friends array
	    JSONObject result = data.post(nameValuePairs, URL);

	    try {
		// Create the array of names
		JSONArray info = result.getJSONArray("response");

		if (info.getString(0).equals("1")) // Register is a success
		{
		    Toast.makeText(Register.this, "Registration Successful",
			    Toast.LENGTH_SHORT).show();
		    Log.d(TAG, "Registered: " + email.getText().toString());
		    finish(); // return back to login
		} else if (info.getString(0).equals("0")) {
		    Toast.makeText(Register.this,
			    "Email Address Already In Use", Toast.LENGTH_SHORT)
			    .show();
		} else {
		    Toast.makeText(Register.this,
			    "Registration Error, Please Try Again!",
			    Toast.LENGTH_SHORT).show();
		}
	    } catch (JSONException ex) {
	    }
	}
    } // end register()
}
/* end: Register.java */