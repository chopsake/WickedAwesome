/* begin: Play_Join2.java */
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Play_Join2 extends Activity implements OnClickListener {
    private Button buttonQuit;
    private Button buttonTest;
    private PostData data = new PostData();

    private static final String QUIT_URL = "http://mmayfield.com/ecs152c/quit.php";
    private static final String URL = "http://mmayfield.com/ecs152c/getgamelist.php";

    private static final String TAG = "Play_Join2****";

    /* Needed for case R.id.joinTest */
    private static GameLocation GLocHelper; // object for game location

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.play_joined);

	buttonQuit = (Button) findViewById(R.id.joinEndGame);
	buttonQuit.setOnClickListener(this);
	buttonTest = (Button) findViewById(R.id.joinTest);
	buttonTest.setOnClickListener(this);

	SystemInfo.inGame = true; // in a game if this class called

	collectList();
	TextView tv = (TextView) findViewById(R.id.joinGameTitle);
	tv.setText(SystemInfo.gameTitle);
    }

    private void collectList() {
	Log.d(TAG, "Starting collectList()");
	GameData.hostMacList = new ArrayList<WifiList>();
	GameData.prevMacList = new ArrayList<WifiList>();
	GLocHelper = new GameLocation(this);

	// Set Post data
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	nameValuePairs
		.add(new BasicNameValuePair("key", SystemInfo.SERVER_KEY));
	nameValuePairs.add(new BasicNameValuePair("email", SystemInfo.email));
	nameValuePairs.add(new BasicNameValuePair("password",
		SystemInfo.password));

	// Get the post data to fill the array
	JSONObject result = data.post(nameValuePairs, URL);

	try {
	    // Create the array of names
	    JSONArray response = result.getJSONArray("response");
	    JSONArray pref = result.getJSONArray("pref");
	    JSONArray bid = result.getJSONArray("bid");
	    JSONArray lat = result.getJSONArray("lat");
	    JSONArray lon = result.getJSONArray("lon");
	    JSONArray macid = result.getJSONArray("macid");
	    JSONArray snr = result.getJSONArray("snr");

	    if (response.getInt(0) == 1) {
		// insert data into the game list
		SystemInfo.gameTitle = pref.getString(0);
		GameData.hostBid = bid.getString(0);
		GameData.lat = lat.getDouble(0);
		GameData.lon = lon.getDouble(0);
		// Fill the wifi list with data
		for (int i = 0; i < macid.length(); i++) {
		    GameData.hostMacList.add(new WifiList(macid.getString(i),
			    snr.getInt(i)));
		}
		// generate current list for user
		int count = 0;
		int sum = 0;
		GameData.prevMacList = SystemInfo.GWDhelper.getWifiList();
		for (int i = 0; i < GameData.prevMacList.size(); i++) {
		    for (int j = 0; j < GameData.hostMacList.size(); j++) {
			if (GameData.prevMacList.get(i).getMac()
				.equals(GameData.hostMacList.get(j).getMac())) {
			    count++;
			    sum += Math.abs(GameData.hostMacList.get(j)
				    .getSignal()
				    - GameData.prevMacList.get(i).getSignal());
			    break;
			}
		    }
		}
		if (count > 0) {
		    GameData.avgstr = sum / count;
		}
	    }
	} catch (JSONException ex) {
	}
    }

    @Override
    public void onClick(View v) {
	boolean checked = false;
	switch (v.getId()) {
	case R.id.joinEndGame:
	    Toast.makeText(Play_Join2.this, "End Game", Toast.LENGTH_SHORT)
		    .show();
	    endGame();
	    break;
	case R.id.joinTest:
	    /*
	     * Location Comparison Process: 1) Check Bluetooth, use if good or
	     * continue 2) Check WiFi, use if good or continue 3) Check GPS, use
	     * if good or quit game
	     */

	    // check bluetooth
	    if (GLocHelper.BlueScan(GameData.hostBid) == 1) {
		checked = true;
		wonGame(); // bluetooth in range - game over
	    }

	    // check Wifi
	    if (GameData.hostMacList.size() > 0 && !checked
		    && SystemInfo.inGame) {

		// generate current list
		SystemInfo.GWDhelper.initAPList();
		// generate current list for user
		int count = 0;
		int sum = 0;
		double avg = 0.0;
		GameData.currentList = SystemInfo.GWDhelper.getWifiList();
		// compare player with host's AP list
		for (int i = 0; i < GameData.currentList.size(); i++) {
		    for (int j = 0; j < GameData.hostMacList.size(); j++) {
			if (GameData.currentList.get(i).getMac()
				.equals(GameData.hostMacList.get(j).getMac())) {
			    count++; // count matches
			    // sum the signal strenths
			    sum += Math.abs(GameData.hostMacList.get(j)
				    .getSignal()
				    - GameData.currentList.get(i).getSignal());
			    break;
			}
		    }
		}
		// if we have a match
		if (count > 0) {
		    checked = true;
		    avg = sum / count; // average signal str
		    // if average signal strength stronger
		    if (avg <= GameData.avgstr) {
			Toast.makeText(
				Play_Join2.this,
				"avgstr: " + Double.toString(GameData.avgstr)
					+ "\navg: " + Double.toString(avg),
				Toast.LENGTH_LONG).show();
			GameData.avgstr = avg;
			GameData.prevMacList = GameData.currentList;
			Intent intent = new Intent(this, Hot.class);
			startActivity(intent);
		    } else { // avg signal lower
			Toast.makeText(
				Play_Join2.this,
				"avgstr: " + Double.toString(GameData.avgstr)
					+ "\navg: " + Double.toString(avg),
				Toast.LENGTH_LONG).show();
			GameData.avgstr = avg;
			GameData.prevMacList = GameData.currentList;
			Intent intent = new Intent(this, Cold.class);
			startActivity(intent);
		    }
		}
	    } // end check Wifi

	    // check GPS
	    SystemInfo.GWDhelper.listenMyGps();
	    // if have valid lat & currently in game
	    if (GameData.lat > -999 && !checked && SystemInfo.inGame) {
		double lat = SystemInfo.GWDhelper.getGPSlat();
		double lon = SystemInfo.GWDhelper.getGPSlong();
		if (lat > -999) { // have valid coordinate
		    checked = true;
		    float dist = SystemInfo.GWDhelper.getGPSdiff(lat, lon,
			    GameData.lat, GameData.lon);
		    if (dist < GameData.prevDist) {
			GameData.prevDist = dist;
			Intent intent = new Intent(this, Hot.class);
			startActivity(intent);
		    } else {
			GameData.prevDist = dist;
			Intent intent = new Intent(this, Cold.class);
			startActivity(intent);
		    }
		}
	    }

	    if (!checked && SystemInfo.inGame) {
		Toast.makeText(Play_Join2.this,
			"Lost Game Connection!\nEnding Game.",
			Toast.LENGTH_LONG).show();
		endGame();
	    }
	    break; // end location comparisons
	}// end switch (v.getId()
    }// end onClick(View v)

    void endGame() {
	// end Joined game
	Log.d(TAG, "Quiting joined game");
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	nameValuePairs
		.add(new BasicNameValuePair("key", SystemInfo.SERVER_KEY));
	nameValuePairs.add(new BasicNameValuePair("email", SystemInfo.email));
	nameValuePairs.add(new BasicNameValuePair("password",
		SystemInfo.password));

	data.post(nameValuePairs, QUIT_URL);
	GameData.gameComplete = false;
	SystemInfo.inGame = false;
	SystemInfo.gameTitle = null;
	finish();
    }

    // disable back button
    @Override
    public void onBackPressed() {
	return;
    }

    @Override
    protected void onResume() {
	super.onResume();
	// on resume, see if game finished
	if (GameData.gameComplete) {
	    endGame();
	}
    }

    public void wonGame() {
	GameData.gameComplete = true;
	Intent intent = new Intent(this, Won.class);
	startActivity(intent);
	endGame();
	finish();
    }
}
/* end: Play_Join2.java */