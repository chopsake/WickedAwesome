/* begin: Play_Join.java */
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

// CURRENTLY USING Play_Join2 class
public class Play_Join extends Activity implements OnClickListener {
    private Button buttonQuit;
    private Button buttonTest;
    private PostData data = new PostData();

    private static final String QUIT_URL = "http://mmayfield.com/ecs152c/quit.php";
    private static final String URL = "http://mmayfield.com/ecs152c/getgamelist.php";
    
    private static final String TAG = "Play_Join****";

    /* Needed for case R.id.joinTest */
    private static GameLocation GLocHelper; // object for game loc functions
    private int game_type; //based on host
    private int check_type; //used to determine sequencing of calls
    //settings for game types:
    private static final int GPS_GAME = 1;
    private static final int WIFI_GAME = 2;
    private static final int BOTH_GAME = 3;
    private static final int END_GAME = 0;
    //settings for location check types:
    private static final int CHECK_GPS = 1;
    private static final int CHECK_WIFI = 2;
    private static final int CHECK_BLUE = 3;
    //return values from location comparisons:
    private static final int WARMER = 1;
    private static final int COLDER = 2;
    private static final int NO_SIGNAL = 3;
    private static final int GAMELOC_ERROR = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.play_joined);

	buttonQuit = (Button) findViewById(R.id.joinEndGame);
	buttonQuit.setOnClickListener(this);
	buttonTest = (Button) findViewById(R.id.joinTest);
	buttonTest.setOnClickListener(this);

	collectList();
	TextView tv = (TextView) findViewById(R.id.joinGameTitle);
	tv.setText(SystemInfo.gameTitle);
    }

    private void collectList() {
    Log.d(TAG, "Starting collectList()");
	GameData.hostMacList = new ArrayList<WifiList>(); //host's list of visible APs
	GameData.prevMacList = new ArrayList<WifiList>(); //player's old list of APs
	GameData.currentList = new ArrayList<WifiList>(); //player's current list of APs
	GLocHelper = new GameLocation(this); //object needed for game location functions

	// Set Post data
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	nameValuePairs
		.add(new BasicNameValuePair("key", SystemInfo.SERVER_KEY));
	nameValuePairs.add(new BasicNameValuePair("email", SystemInfo.email));
	nameValuePairs.add(new BasicNameValuePair("password",
		SystemInfo.password));

	// Get the post data to fill the friends array
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
		    GameData.hostMacList.add(new WifiList(macid.getString(i), snr
			    .getInt(i)));
		}
		
		//check which type of game to play
		game_type = BOTH_GAME;//default if all data available
	    if(GameData.hostMacList == null){//no host list
	    	game_type = GPS_GAME;
	    }
	    if(GameData.hostMacList.size() < 1){//host list too small
	    	game_type = GPS_GAME;
	    }
	    if (GameData.lat == 0.0 || GameData.lon == 0.0
			    || GameData.lat == -999 || GameData.lon == -999) {
	    	if(game_type == GPS_GAME){
			game_type = END_GAME; // No host GPS, so can't play GPS game
	    	}
	    	else{
	    		game_type = WIFI_GAME;
	    	}
	    }
	    
	    }
	} catch (JSONException ex) {
	}
    }

    @Override
    public void onClick(View v) {
	switch (v.getId()) {
	case R.id.joinEndGame:
	    // post to server
	    Log.d(TAG, "Quiting joined game");
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(new BasicNameValuePair("key",
		    SystemInfo.SERVER_KEY));
	    nameValuePairs
		    .add(new BasicNameValuePair("email", SystemInfo.email));
	    nameValuePairs.add(new BasicNameValuePair("password",
		    SystemInfo.password));

	    data.post(nameValuePairs, QUIT_URL);
	    finish();
	    break;
	case R.id.joinTest:
	    Toast.makeText(Play_Join.this, "Using Play_Join", Toast.LENGTH_SHORT)
		    .show();
	    //Intent intentcold = new Intent(this, Cold.class);
	    //Intent intenthot = new Intent(this, Hot.class);
	    /*
	     * Location Comparison Process: 1) Check Bluetooth, use if good or
	     * continue 2) Check WiFi, use if good or continue 3) Check GPS, use
	     * if good or quit game
	     */
	    if(game_type == END_GAME){
	    	Toast.makeText(Play_Join.this, "Bad host data, can't play", Toast.LENGTH_SHORT).show();
	    	endGame();//can't play
	    }

	    //BASED ON GAME TYPE, CHECK DISTANCE, THEN SET CHECK TYPE
	    if(GameData.near_enough){
	    	check_type = CHECK_BLUE;	
	    }
	    else{
	    	if(game_type == BOTH_GAME || game_type == WIFI_GAME){
	    		check_type = WIFI_GAME;
	    	}
	    	else if(game_type == GPS_GAME){
	    		check_type = CHECK_GPS;
	    	}
	    	else{//game_type == END_GAME
	    		endGame();//can't play
	    	}
	    }

	    /*
	     * Check bluetooth if near enough:
	     */	    
	    if(check_type == CHECK_BLUE){
		    switch(GLocHelper.BlueScan(GameData.hostBid)){
		    case 0:
		    	Toast.makeText(Play_Join.this, "Bluetooth not working, trying WiFi", Toast.LENGTH_SHORT).show();
		    	check_type = CHECK_WIFI;
		    	break;
		    case 1:
		    	Toast.makeText(Play_Join.this, "Host's Bluetooth found in scan, you won!", Toast.LENGTH_SHORT).show();
		    	wonGame(); // close enough for bluetooth and host's MAC found
		    	break;
		    case 2:
		    	Toast.makeText(Play_Join.this, "Couldn't find host in Bluetooth scan", Toast.LENGTH_SHORT).show();
		    	check_type = CHECK_WIFI;
		    	break;	    	
		    }	    
	    }

	    /* 
	     * Not close enough for Bluetooth, try WiFi:
	     */
	    if(check_type == CHECK_WIFI && game_type != GPS_GAME){
	    	Toast.makeText(Play_Join.this, "trying WiFi", Toast.LENGTH_SHORT).show();
		    SystemInfo.GWDhelper.initAPList(); // initialize wifi list (if not already) 
		    GameData.prevMacList.clear();
	    	GameData.prevMacList.addAll(GameData.currentList);//save the old list
	    	GameData.currentList.clear();
		    GameData.currentList.addAll(SystemInfo.GWDhelper.getWifiList()); //get current wifi list
	    	
		    switch(GLocHelper.WifiCompare(GameData.hostMacList, GameData.prevMacList, GameData.currentList)){ 
		    case WARMER: 
		    	Toast.makeText(Play_Join.this, "WIFI WARMER", Toast.LENGTH_SHORT).show(); 
		    	startActivity(new Intent(this, Hot.class));
		    	break;
		    case COLDER: 
		    	Toast.makeText(Play_Join.this, "WIFI COLDER", Toast.LENGTH_SHORT).show(); 
		    	startActivity(new Intent(this, Cold.class));
		    	break;
		    case NO_SIGNAL: 
		    	Toast.makeText(Play_Join.this,"Too far for Wifi, going to GPS", Toast.LENGTH_SHORT).show();
		    	check_type = CHECK_GPS; //not close enough, so use GPS instead
		    	break; 
		    case GAMELOC_ERROR: 
		    	Toast.makeText(Play_Join.this, "WifiCompare Error", Toast.LENGTH_SHORT).show(); 
		    	//PUT HERE CODE FOR WIFI FAILING (LOGIC OR CODE ERROR SOMEWHERE) 
		    	break; 
		    }//end switch
		    //GameData.prevMacList = GameData.currentList;
		    if(game_type == WIFI_GAME){ 
		    	break;//end location comparisons (breaks out of case R.id.joinTest) 
		    }//else game_type was toggled to GPS_GAME because not enough APs visible
	    }
	    else{
	    	check_type = CHECK_GPS;
	    }//end WiFi check

	    /* 
	     * Not close enough for WiFi, try GPS
	     */
	    if(check_type == CHECK_GPS && game_type != WIFI_GAME){
			Toast.makeText(Play_Join.this, "trying GPS", Toast.LENGTH_SHORT).show();
		    if (!SystemInfo.GWDhelper.isGPSlistening()) {
				Toast.makeText(Play_Join.this,
					"GPS listener was off, turning it on",
					Toast.LENGTH_SHORT).show();
		    }
			if (!SystemInfo.GWDhelper.listenMyGps()) { // turn on the GPS
								   // listener
				if(game_type == GPS_GAME){
			    game_type = END_GAME; // GPS not on or not working
			    endGame();//can't play GPS game
				}
			    
			    break;// end location comparisons (breaks out of case
				  // R.id.joinTest)
			}
		    if (!SystemInfo.GWDhelper.isGPSupdated()) {
				Toast.makeText(Play_Join.this, "GPS coordinates not updated",
					Toast.LENGTH_SHORT).show();
				/* GPS is on but still waiting for update */
				startActivity(new Intent(this, Cold.class));
				break;// end location comparisons (breaks out of case
				      // R.id.joinTest)
		    }
		    /* GPS is good, so save old coordinates, and get current ones */
		    GameData.poldlat = GameData.pcurrlat;
		    GameData.poldlon = GameData.pcurrlon;
		    GameData.pcurrlat = SystemInfo.GWDhelper.getGPSlat();
		    GameData.pcurrlon = SystemInfo.GWDhelper.getGPSlong();
	
		    /* Compare old GPS to new GPS */
		    switch (GLocHelper.GPSLocComp(GameData.lat, GameData.lon,
			    GameData.poldlat, GameData.poldlon, GameData.pcurrlat,
			    GameData.pcurrlon)) {
		    case WARMER:
				Toast.makeText(Play_Join.this, "GPS WARMER", Toast.LENGTH_SHORT)
					.show();
				startActivity(new Intent(this, Hot.class));
				break;
		    case COLDER:
				Toast.makeText(Play_Join.this, "GPS COLDER", Toast.LENGTH_SHORT)
					.show();
				startActivity(new Intent(this, Cold.class));
				break;
		    case GAMELOC_ERROR:
				Toast.makeText(Play_Join.this, "GPSLocComp Error",
					Toast.LENGTH_SHORT).show();
				// PUT HERE CODE FOR GPS LOCATION COMPARISON FAILING (LOGIC OR
				// CODING ERROR)
			break;
		    }//end switch
	    }//end GPS check
	    break; // end location comparisons (breaks out of case
		   // R.id.joinTest)

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
/* end: Play_Join.java */