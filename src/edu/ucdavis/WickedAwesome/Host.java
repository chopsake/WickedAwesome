/* begin: Host.java */
package edu.ucdavis.WickedAwesome;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

public class Host extends Activity implements OnClickListener {
    private Button buttonHost;
    private EditText title;
    private PostData data = new PostData();

    private static final String CREATE_URL = "http://mmayfield.com/ecs152c/create.php";
    private static final String QUIT_URL = "http://mmayfield.com/ecs152c/quit.php";
    private static final String INSERT_WIFI_URL = "http://mmayfield.com/ecs152c/insertwifi.php";

    private static final String TAG = "Host****";

    private static final String START_GAME = "Start Game";
    private static final String END_GAME = "End Game";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.host);

	title = (EditText) findViewById(R.id.HostTitle);
	buttonHost = (Button) findViewById(R.id.buttonHost);
	buttonHost.setOnClickListener(this);

	setButton(); // sets button to appropriate text
    }

    @Override
    protected void onResume() {
	super.onResume();
	setButton();
    }

    @Override
    public void onClick(View v) {
	// create new game if not in game and entered Title
	if (!SystemInfo.inGame) {
	    if (title.getText().toString().equals("")) {
		Toast.makeText(Host.this, "Enter Game Title", Toast.LENGTH_LONG)
			.show();
		Log.d(TAG, "No Game Title");
	    } else { // have game title
		Log.d(TAG, "Creating Game");
		// get lat and long of host
		SystemInfo.GWDhelper.listenMyGps();
		String lat = Double.toString(SystemInfo.GWDhelper.getGPSlat());
		String lon = Double.toString(SystemInfo.GWDhelper.getGPSlong());
		SystemInfo.gameTitle = title.getText().toString(); // save title

		if (lat.equals("0.0") && lon.equals("0.0")) { // no GPS
		    Toast.makeText(Host.this, "Unable to get GPS location!",
			    Toast.LENGTH_LONG).show();
		    lat = null;
		    lon = null;
		} else {
		    Toast.makeText(Host.this,
			    "My location: " + lat + ", " + lon,
			    Toast.LENGTH_SHORT).show();
		}

		// post to server
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair("key",
			SystemInfo.SERVER_KEY));
		nameValuePairs.add(new BasicNameValuePair("email",
			SystemInfo.email));
		nameValuePairs.add(new BasicNameValuePair("password",
			SystemInfo.password));
		nameValuePairs.add(new BasicNameValuePair("lat", lat));
		nameValuePairs.add(new BasicNameValuePair("lon", lon));
		nameValuePairs.add(new BasicNameValuePair("pref",
			SystemInfo.gameTitle));

		JSONObject result = data.post(nameValuePairs, CREATE_URL);

		// parse server response
		try {
		    JSONArray info = result.getJSONArray("response");

		    if (info.getString(0).equals("3")) {
			Toast.makeText(Host.this, "Game Created",
				Toast.LENGTH_SHORT).show();
			SystemInfo.inGame = true;
			SystemInfo.isHost = true;
			setButton();
		    } else {
			Toast.makeText(Host.this, "Unable to Start New Game",
				Toast.LENGTH_SHORT).show();
		    }
		} catch (JSONException ex) {
		}

		// populate wifi table
		Toast.makeText(Host.this, "Sending Wifi Locations",
			Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Uploading WifiList to Server");

		SystemInfo.GWDhelper.initAPList();
		ArrayList<WifiList> wifilist = SystemInfo.GWDhelper
			.getWifiList();
		for (int i = 0; i < wifilist.size(); i++) {
		    List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		    nvp.add(new BasicNameValuePair("key", SystemInfo.SERVER_KEY));
		    nvp.add(new BasicNameValuePair("email", SystemInfo.email));
		    nvp.add(new BasicNameValuePair("password",
			    SystemInfo.password));
		    nvp.add(new BasicNameValuePair("macid", wifilist.get(i)
			    .getMac()));
		    nvp.add(new BasicNameValuePair("snr", Integer.toString(wifilist.get(i)
			    .getSignal())));
		    data.post(nvp, INSERT_WIFI_URL);
		}

		Intent i = new Intent(this, HostList.class);
		startActivity(i);
	    }
	} else { // end game
	    Log.d(TAG, "Ending Game");
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(new BasicNameValuePair("key",
		    SystemInfo.SERVER_KEY));
	    nameValuePairs
		    .add(new BasicNameValuePair("email", SystemInfo.email));
	    nameValuePairs.add(new BasicNameValuePair("password",
		    SystemInfo.password));

	    // remove all games and wifi list
	    data.post(nameValuePairs, QUIT_URL);

	    Toast.makeText(Host.this, "Game Ended", Toast.LENGTH_SHORT).show();
	    SystemInfo.inGame = false;
	    SystemInfo.isHost = false;
	    setButton();
	    // finish(); // log back in to be directed to correct page
	}
    }

    // sets button to appropriate text
    private void setButton() {
	if (SystemInfo.inGame) {
	    buttonHost.setText(END_GAME);
	    title.setText(SystemInfo.gameTitle);
	    title.setEnabled(false);
	} else { // not currently in game
	    buttonHost.setText(START_GAME);
	    title.setText(SystemInfo.gameTitle); // default to last title
	    title.setEnabled(true);
	}
    }
}
/* end: Host.java */