/* begin: Options.java */
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
import android.widget.Toast;

public class Options extends Activity implements OnClickListener {
    private Button btn;
    private PostData data = new PostData();

    private static final String URL = "http://mmayfield.com/ecs152c/updatebid.php";
    private static final String TAG = "Options****";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.options);

	btn = (Button) findViewById(R.id.buttonBID);
	btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
	Log.d(TAG, "Updating BID for " + SystemInfo.email);
	Toast.makeText(Options.this, "Updating My Bluetooth ID on Server",
		Toast.LENGTH_SHORT).show();
	String blue = SystemInfo.GWDhelper.getMyBlue();
	Log.d(TAG, "Found BID: " + blue);

	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	nameValuePairs
		.add(new BasicNameValuePair("key", SystemInfo.SERVER_KEY));
	nameValuePairs.add(new BasicNameValuePair("email", SystemInfo.email));
	nameValuePairs.add(new BasicNameValuePair("password",
		SystemInfo.password));
	nameValuePairs.add(new BasicNameValuePair("bid", blue));

	// Get the post data to fill the friends array
	JSONObject result = data.post(nameValuePairs, URL);

	try {
	    // Create the array of names
	    JSONArray info = result.getJSONArray("response");

	    if (info.getString(0).equals("1")) {
		Toast.makeText(Options.this, "Updated Successfully",
			Toast.LENGTH_SHORT).show();
	    } else {
		Toast.makeText(Options.this, "Update Failed!",
			Toast.LENGTH_SHORT).show();
	    }
	} catch (JSONException ex) {
	}
    }
}
/* end: Options.java */