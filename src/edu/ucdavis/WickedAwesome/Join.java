/* begin: Join.java */
package edu.ucdavis.WickedAwesome;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Join extends ListActivity {
    private Intent i;
    // web site info
    private static final String URL = "http://mmayfield.com/ecs152c/join.php";

    private PostData data = new PostData();
    protected ArrayList<Game> game = new ArrayList<Game>();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	generateList();
	setListAdapter(new ArrayAdapter<Game>(this, R.layout.list, game));

	ListView lv = getListView();
	lv.setTextFilterEnabled(true);

	lv.setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view,
		    int position, long id) {
		onClick(position);
	    }
	});
    }

    protected void onClick(int position) {
	// If there is a game selected start Play_Join activity
	if (game.get(position).getHid() != -1) {
	    SystemInfo.gameTitle = game.get(position).getGameName();
	    // Set Post data
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(new BasicNameValuePair("key",
		    SystemInfo.SERVER_KEY));
	    nameValuePairs
		    .add(new BasicNameValuePair("email", SystemInfo.email));
	    nameValuePairs.add(new BasicNameValuePair("password",
		    SystemInfo.password));
	    nameValuePairs.add(new BasicNameValuePair("cond", "2"));
	    nameValuePairs.add(new BasicNameValuePair("hid", Integer
		    .toString(game.get(position).getHid())));

	    // Get the post data to fill the friends array
	    JSONObject result = data.post(nameValuePairs, URL);

	    try {
		// Create the array of names
		JSONArray response = result.getJSONArray("response");
		if (response.getInt(0) == 1) {
		    //i = new Intent(this, Play_Join2.class);
		    i = new Intent(this, Play_Join.class);
		    startActivity(i);
		} else
		    Toast.makeText(Join.this, "Problem with Join",
			    Toast.LENGTH_SHORT).show();
	    } catch (JSONException ex) {
		Toast.makeText(Join.this, "Error with Join", Toast.LENGTH_SHORT)
			.show();
	    }
	}
    }

    @Override
    protected void onResume() {
	super.onResume();
	generateList();
    }

    private void generateList() {
	game = new ArrayList<Game>();
	// var's needed to process
	Integer numMacId = 1;
	SystemInfo.GWDhelper.initAPList();
	numMacId = SystemInfo.GWDhelper.getWifiList().size();
	ArrayList<WifiList> macId = SystemInfo.GWDhelper.getWifiList();

	// Set Post data
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	nameValuePairs
		.add(new BasicNameValuePair("key", SystemInfo.SERVER_KEY));
	nameValuePairs.add(new BasicNameValuePair("email", SystemInfo.email));
	nameValuePairs.add(new BasicNameValuePair("password",
		SystemInfo.password));
	nameValuePairs.add(new BasicNameValuePair("cond", "1"));

	// process wifi mac addresses
	nameValuePairs.add(new BasicNameValuePair("numWifi", numMacId
		.toString()));
	for (Integer i = 1; i <= numMacId; i++) {
	    nameValuePairs.add(new BasicNameValuePair("macid" + i.toString(),
		    macId.get(i - 1).mac));
	}

	// process GPS if there is a signal
	SystemInfo.GWDhelper.listenMyGps();
	if (SystemInfo.GWDhelper.isGPSupdated()) {// check again if updated
	    nameValuePairs.add(new BasicNameValuePair("lat", Double
		    .toString(SystemInfo.GWDhelper.getGPSlat())));
	    nameValuePairs.add(new BasicNameValuePair("lon", Double
		    .toString(SystemInfo.GWDhelper.getGPSlong())));
	} else {// GPS is not getting a signal
	    nameValuePairs.add(new BasicNameValuePair("lat", ""));
	    nameValuePairs.add(new BasicNameValuePair("lon", ""));
	}

	// Get the post data to fill the friends array
	JSONObject result = data.post(nameValuePairs, URL);

	try {
	    // Create the array of names
	    JSONArray response = result.getJSONArray("response");
	    JSONArray pref = result.getJSONArray("pref");
	    JSONArray hid = result.getJSONArray("hid");
	    // Fill the game array with the JSONObject
	    for (int i = 0; i < response.length(); i++) {
		if (response.getInt(i) == 10)
		    game.add(new Game("Wifi", hid.getInt(i), pref.getString(i)));
		else
		    game.add(new Game("GPS", hid.getInt(i), pref.getString(i)));
	    }

	} catch (JSONException ex) {
	    game.add(new Game("", -1, "No games to join."));
	}
    } // end generateList()
}
/* end: Join.java */