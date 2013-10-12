/* begin: ViewWirelessData.java */
package edu.ucdavis.WickedAwesome;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class ViewWirelessData extends ListActivity {
    public static final int INIT_MYGPS = Menu.FIRST;
    public static final int VIEW_MYGPS = Menu.FIRST + 1;
    public static final int VIEW_MYBLUE = Menu.FIRST + 2;
    public static final int VIEW_MYBLUE2 = Menu.FIRST + 3;
    public static final int VIEW_MYALL = Menu.FIRST + 4;
    public static final int VIEW_APLIST = Menu.FIRST + 5;
    public static final int VIEW_BLUES = Menu.FIRST + 6;
    public static final int COMP_WIFI = Menu.FIRST + 7;
    public static final int COMP_WIFI_HOST = Menu.FIRST + 8;
    public static final int COMP_GPS = Menu.FIRST + 9;
    public static final int COMP_GPS_HOST = Menu.FIRST + 10;

    TextView tv_displaybanner = null;
    TextView tv_displayoutput1 = null;
    TextView tv_displayoutput2 = null; // for viewMyAll() and updateLocation()

    /* Declare object for wireless data functions */
    //private GetWirelessData GWDhelper;//declared in SystemInfo.java now

    /* Objects and constants for GPS functions */
    private static final int GPS_WAIT = 4000;

    /* Constants and variables for Bluetooth functions */
    // Bluetooth Intent request code
    private static final int REQUEST_ENABLE_BT = 3;

    /* Variables for both GPS & Bluetooth */
    // Flag indicates viewMyAll() is called
    boolean viewall = false;
    
    /* Declare object and variables for Game Location functions */
    private static GameLocation tGLocHelper; //object for game location functions
    private static ArrayList<WifiList> thostMacList;
    private boolean hostlistinit = false;  //for testing here, to know host list was initialized
    private static ArrayList<WifiList> tplayerOldMacList;
    private static ArrayList<WifiList> tplayerCurrMacList;
    private double thostlat = 0;
    private double thostlon = 0;
    private String thostBid = "";
    private double tpoldlat = 0.0; //player's previous GPS latitude
    private double tpoldlon = 0.0; //player's previous GPS longitude
    private double tpcurrlat = 0.0; //player's current GPS latitude
    private double tpcurrlon = 0.0; //player's current GPS longitude

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.testdatadisplay);

	/* Initialize objects for Game Location functions */
	thostMacList = new ArrayList<WifiList>();
	tplayerCurrMacList = new ArrayList<WifiList>();
	tplayerOldMacList = new ArrayList<WifiList>();
	tGLocHelper = new GameLocation(this);//SHOULD BE IN Login.java?


	tv_displaybanner = (TextView) findViewById(R.id.displaybanner);
	tv_displaybanner.setText("Select an option using the MENU button\n\n");

	tv_displayoutput1 = (TextView) findViewById(R.id.displayoutput1);
	tv_displayoutput1.setText("No data selected\n");
	tv_displayoutput2 = (TextView) findViewById(R.id.displayoutput2);
	tv_displayoutput2.setText("----------------\n");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	boolean result = super.onCreateOptionsMenu(menu);
	menu.add(0, INIT_MYGPS, 0, R.string.menu_mygps1);
	menu.add(0, VIEW_MYGPS, 0, R.string.menu_mygps2);
	menu.add(0, VIEW_MYBLUE, 0, R.string.menu_myblue);
	menu.add(0, VIEW_MYBLUE2, 0, R.string.menu_myblue2);
	menu.add(0, VIEW_MYALL, 0, R.string.menu_myall);
	menu.add(0, VIEW_APLIST, 0, R.string.menu_aplist);
	menu.add(0, VIEW_BLUES, 0, R.string.menu_blues);
	menu.add(0, COMP_WIFI, 0, R.string.menu_compwifi);
	menu.add(0, COMP_WIFI_HOST, 0, R.string.menu_compwifihost);//NOT READY YET
	menu.add(0, COMP_GPS, 0, R.string.menu_compgps);
	menu.add(0, COMP_GPS_HOST, 0, R.string.menu_compgpshost);
	return result;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case INIT_MYGPS:
	    viewall = false;
	    viewMyGps1();
	    return true;
	case VIEW_MYGPS:
	    viewall = false;
	    viewMyGps2();
	    return true;
	case VIEW_MYBLUE:
	    viewall = false;
	    viewMyBlue(false);
	    return true;
	case VIEW_MYBLUE2:
	    viewMyBlue2();
	    return true;
	case VIEW_MYALL:
	    viewMyAll();
	    return true;
	case VIEW_APLIST:
	    viewApList2();
	    return true;
	case VIEW_BLUES:
	    viewBlues();
	    return true;
	case COMP_WIFI:
		compWiFi();
		return true;
	case COMP_WIFI_HOST:
		compWiFiHost();
		return true;	
	case COMP_GPS:
		compGPS();
		return true;
	case COMP_GPS_HOST:
		compGPSHost();
		return true;
	}
	return super.onOptionsItemSelected(item);
    }

    private void viewMyGps1() {
    	SystemInfo.GWDhelper.listenMyGps();
	if (!viewall) {
	    tv_displaybanner.setText("GPS data display\n\n");
	    tv_displayoutput2.setText("\n");
	}
	tv_displayoutput1.setText("Acquiring GPS\n");
	if (!SystemInfo.GWDhelper.isGPSupdated()) {
	    try {
		Thread.sleep(GPS_WAIT); // wait for signal acquisition
	    } catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
    }

    private void viewMyGps2() {
	String buf = null;
	double lat, lon;
	lat = lon = 0;
	double testLat = 38.559928;
	double testLon = -121.740289;
	double kLat = 38.537284; 	//Kemper Hall
	double kLon = -121.754711;	// coordinates
	if (!SystemInfo.GWDhelper.isGPSlistening()) {
	    tv_displaybanner.setText("GPS data display\n\n");
	    buf = "GPS not listening, select \"Init GPS\" first.\n";
	} else {
	    buf = "Displaying GPS Data:\n\n";
	}
	buf += "isGPSlistening(): " + SystemInfo.GWDhelper.isGPSlistening() + "\n\n";
    buf += "isGPSupdated(): " + SystemInfo.GWDhelper.isGPSupdated() + "\n\n";
    buf += "If isGPSupdated() is not true, try again or increase GPS_WAIT.\n\n";
    lat = SystemInfo.GWDhelper.getGPSlat();
    lon = SystemInfo.GWDhelper.getGPSlong();
    buf += "latitude: " + lat + "\n";
    buf += "longitude: " + lon + "\n";
    buf += "distance to Kemper: " + SystemInfo.GWDhelper.getGPSdiff(lat, lon, kLat, kLon)+ " meters\n";
    buf += "distance to test location: " + SystemInfo.GWDhelper.getGPSdiff(lat, lon, testLat, testLon)+ " meters\n";

    tv_displayoutput1.setText(buf);
    tv_displayoutput2.setText("\n");
    }
    
    public void compGPSHost(){//sets up host data for testing using present position
    	String buf = null;
    	if (!SystemInfo.GWDhelper.isGPSupdated()) {
    	    tv_displaybanner.setText("GPS host setup\n\n");
    	    tv_displayoutput2.setText("\n");
    	    buf = "GPS not up, select \"Init GPS\" first.\n";
    	    tv_displayoutput1.setText(buf);
    	    return;
    	} else {
    	    buf = "Setting host GPS Data:\n\n";
    	}
    	thostlat = SystemInfo.GWDhelper.getGPSlat();
    	thostlon = SystemInfo.GWDhelper.getGPSlong();
    	
    	buf += "Acting as the host, your present coordinates are:\n";
    	buf += "       latitude:  " + thostlat + "\n";
    	buf += "       longitude:  " + thostlon + "\n";
    	
    	tv_displayoutput1.setText(buf);
    }
    
    public void compGPS(){
    	String buf = null;
    	//thostlat = 38.559928;
    	//thostlon = -121.740289;
     	int result;
     	float lasthop;
    	
    	if (thostlat == 0) {
    	    tv_displaybanner.setText("GPS comparison display\n\n");
    	    tv_displayoutput2.setText("\n");
    	    buf = "Set host GPS first.\n";
    	    tv_displayoutput1.setText(buf);
    	    tv_displayoutput2.setText("\n");
    	    return;
    	} else {
    	    buf = "Comparing GPS Data:\n\n";
    	}
    	tpoldlat = tpcurrlat;
    	tpoldlon = tpcurrlon;
    	tpcurrlat = SystemInfo.GWDhelper.getGPSlat();
    	tpcurrlon = SystemInfo.GWDhelper.getGPSlong();
    	
    	result = tGLocHelper.GPSLocComp(thostlat, thostlon, 
     			tpoldlat, tpoldlon,
     			tpcurrlat, tpcurrlon);
    	buf += "host latitude: " + thostlat + "\n";
    	buf += "host longitude: " + thostlon + "\n";
    	buf += "my previous latitude: " + tpoldlat + "\n";
    	buf += "my previous longitude: " + tpoldlon + "\n";
    	buf += "my current latitude: " + tpcurrlat + "\n";
    	buf += "my current longitude: " + tpcurrlon + "\n";
    	buf += "previous distance to host: " + SystemInfo.GWDhelper.getGPSdiff(tpoldlat, tpoldlon, thostlat, thostlon) + " meters\n";
    	buf += "current distance to host: " + SystemInfo.GWDhelper.getGPSdiff(tpcurrlat, tpcurrlon, thostlat, thostlon) + " meters\n";
    	buf += "I've gotten (1-warmer, 2-colder, 3-error): " + result + "\n";
    	lasthop = SystemInfo.GWDhelper.getGPSdiff(tpcurrlat, tpcurrlon, tpoldlat, tpoldlon);
    	buf += "I've travelled " + lasthop + " meters in the last hop.\n";
    	tv_displayoutput1.setText(buf);
    	tv_displayoutput2.setText("\n");
    }

    /*
     * viewMyBlue gets Bluetooth data directly (see viewMyBlue2 to use function
     * from GetWirelessData.java)
     */
    public void viewMyBlue(boolean bterepeat) {
	String btmac;
	//if (!viewall) {
	//	SystemInfo.GWDhelper.stopListenGPS(); // saving your battery :-)
	//}

	if (!viewall) {
	    tv_displaybanner.setText("Displaying Bluetooth Adapter Data:\n\n");
	    tv_displayoutput1.setText("no Bluetooth data\n");
	    tv_displayoutput2.setText("\n");
	} else {
	    tv_displayoutput2.setText("\n no Bluetooth data\n");
	}

	// Get the BluetoothAdapter
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
		.getDefaultAdapter();
	Toast.makeText(this, "Checking Bluetooth adapter", Toast.LENGTH_LONG)
		.show();
	if (mBluetoothAdapter == null) {
	    // Device does not support Bluetooth
	    Toast.makeText(this, "Bluetooth is not available",
		    Toast.LENGTH_LONG).show();
	    // finish();
	    return;
	} else {
	    Toast.makeText(this, "Bluetooth IS available", Toast.LENGTH_LONG)
		    .show();
	}

	// Enable Bluetooth
	if (!mBluetoothAdapter.isEnabled()) {
	    if (!bterepeat) {
		bterepeat = true;
		Intent enableBtIntent = new Intent(
			BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
	    } else {
		// User did not enable Bluetooth or an error occurred
		Toast.makeText(this, "Bluetooth not enabled by user",
			Toast.LENGTH_SHORT).show();
	    }
	} else {
	    // Bluetooth is now enabled, use it

	    // Get my Bluetooth MAC address
	    btmac = mBluetoothAdapter.getAddress();

	    // Display my Bluetooth MAC address
	    if (!viewall) {
		tv_displayoutput1.setText("Bluetooth MAC address:  " + btmac
			+ "\n");
	    } else {
		tv_displayoutput2.setText("\n Bluetooth MAC address:  " + btmac
			+ "\n");
	    }

	}
	return;
    }

    /*
     * View my Bluetooth adapter MAC address
     * 
     * This function assumes Bluetooth has already been enabled.
     */
    public void viewMyBlue2() {
	String btmac;

	SystemInfo.GWDhelper.stopListenGPS(); // saving your battery :-)

	btmac = SystemInfo.GWDhelper.getMyBlue();

	tv_displaybanner.setText("Displaying Bluetooth Adapter Data\n\n");
	tv_displayoutput1.setText("Displaying my Bluetooth MAC address "
		+ "using function getMyBlue() from GetWirelessData.java "
		+ "which assumes Bluetooth is already enabled:\n");
	tv_displayoutput2.setText("Bluetooth MAC address:  " + btmac + "\n");
    }

    /* View my GPS and my Bluetooth on one screen */
    public void viewMyAll() {
	tv_displaybanner
		.setText("Displaying GPS location & Bluetooth adapter MAC address:\n\n");
	viewall = true;
	viewMyGps1();
	viewMyGps2();
	viewMyBlue(false);
	// tv_displayoutput.setText("viewMyAll() not ready yet\n");
    }
    
    /* View Bluetooth device list */
    public void viewBlues() {
		tv_displaybanner.setText("Displaying Bluetooth devices:\n");
		tv_displayoutput1.setText("\n");
		tv_displayoutput2.setText("\n");
		SystemInfo.GWDhelper.scanBlue();
		
		startActivity(new Intent(this, ViewBlueScan.class));
    }



    /* View Wifi Access Point list */
    public void viewApList() {
	String buf1, buf2;
	int aplistsize;
	tv_displaybanner.setText("Displaying Wifi AP list:\n");

	//aplistsize = SystemInfo.GWDhelper.getAPListSizeOld();
	
	//if(!SystemInfo.GWDhelper.initAPList_wifiscan()){
	if(!SystemInfo.GWDhelper.initAPList()){
		buf1 = "initAPlist() failed\n";
		buf2 = "\n";
		return;//fails
	}
	aplistsize = SystemInfo.GWDhelper.getAPListSize();
	
	buf1 = "Access Point list size = ";
	buf1 += aplistsize + "\n";
	tv_displayoutput1.setText(buf1);

	/* display BSSIDs and signal levels */
	buf2 = "BSSID                           Signal Level (dBm):\n";
	for (int i = 0; i < aplistsize; i++) {
	    buf2 += SystemInfo.GWDhelper.getAPList().get(i) + "    "
		    + SystemInfo.GWDhelper.getAPLevels().get(i) + "\n";
	}
	tv_displayoutput2.setText(buf2);
    }

    /* View Wifi Access Point list (using <WifiList> data structure)*/
    public void viewApList2() {
		String buf1, buf2;
		int aplistsize;
		tv_displaybanner.setText("Displaying Wifi AP list:\n");
	
		//aplistsize = SystemInfo.GWDhelper.getAPListSizeOld();
		
		//if(!SystemInfo.GWDhelper.initAPList_wifiscan()){
		if(!SystemInfo.GWDhelper.initAPList()){
			buf1 = "initAPlist() failed\n";
			buf2 = "\n";
			return;//fails
		}
		//aplistsize = SystemInfo.GWDhelper.getAPListSize();
		aplistsize = SystemInfo.GWDhelper.getWifiList().size();
		
		buf1 = "Access Point list size = ";
		buf1 += aplistsize + "\n";
		tv_displayoutput1.setText(buf1);
	
		/* display BSSIDs and signal levels */
		buf2 = "BSSID                           Signal Level (dBm):\n";
		for (int i = 0; i < aplistsize; i++) {
		    buf2 += SystemInfo.GWDhelper.getWifiList().get(i).mac + "    "
			    + SystemInfo.GWDhelper.getWifiList().get(i).signal + "\n";
		}
		tv_displayoutput2.setText(buf2);
    }
    

    public void compWiFi() {
    	String buf = null;
    	tv_displayoutput2.setText("\n");
    	
    	if(!hostlistinit){
    	    tv_displaybanner.setText("WiFi comparison display\n\n");
    	    tv_displayoutput2.setText("\n");
    	    buf = "Set host data first.\n"; 
    	    tv_displayoutput1.setText(buf);
    	    return;
    	}

    	if (!SystemInfo.GWDhelper.initAPList()) {// initialize wifi list (if not already)
    	    tv_displaybanner.setText("WiFi comparison display\n\n");
    	    tv_displayoutput2.setText("\n");
    	    buf = "WiFi not up!\n";
    	    tv_displayoutput1.setText(buf);
    	    return;
    	} else {
    	    buf = "Comparing Wifi Data:\n\n";
    	}
    	
    	tplayerOldMacList.clear();
    	tplayerOldMacList.addAll(tplayerCurrMacList);//save the old list
    	tplayerCurrMacList.clear();
    	tplayerCurrMacList.addAll(SystemInfo.GWDhelper.getWifiList()); //get current wifi list
    	
     	switch(tGLocHelper.WifiCompare(thostMacList, 
     			tplayerOldMacList, tplayerCurrMacList)){
     		case 1:
     			buf += "You have gotten WARMER!\n";
     			break;
     		case 2:
     			buf += "You have gotten COLDER\n";
     			break;
     		case 3:
     			buf += "Can't see enough matching access points, try again somewhere else (COLDER)\n";
     			break;
     		case 0:
     			buf += "WifiCompare Error.\n";
     			break;
     	}	
     	
     	buf += "Size of host list: " + thostMacList.size() + "\n";
		buf += "Previous MACs visible: " + tplayerOldMacList.size() + "\n";
		//buf += "MAC addresses previously in common with host (GD): " + GameData.prevmacint + "\n";
		buf += "Current MACs visible: " + tplayerCurrMacList.size() + "\n";
		//buf += "MAC addresses currently in common with host (GD): " + GameData.currmacint + "\n";
		buf += "MACs previously in common w/ host: " + tGLocHelper.getPrevWifiCount() + "\n";
		buf += "MACs currently in common w/ host: " + tGLocHelper.getCurrWifiCount() + "\n";
		buf += "average signal level difference: " + tGLocHelper.getSigLevelDiff() + "\n";
		buf += "fudge factor 1: " + tGLocHelper.fudgefactor1 + "\n";
		buf += "fudge factor 2: " + tGLocHelper.fudgefactor2 + "\n";
    	    	
    	tv_displayoutput1.setText(buf);
    }
    
    public void compWiFiHost(){//sets up host data for testing using present position
    	String buf = null;
    	tv_displayoutput2.setText("\n");
    	
    	if (!SystemInfo.GWDhelper.initAPList()) {// initialize wifi list (if not already)
    	    tv_displaybanner.setText("WiFi Host setup\n\n");
    	    buf = "WiFi not up!\n";
    	    tv_displayoutput1.setText(buf);
    	    return;
    	} else {
    	    buf = "Setting up Host Wifi Data:\n\n";
    	}
		try {
			Thread.sleep(1500); //need to wait for multiple Wifi scans
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	buf += "Creating host list based on your current position...\n";
    	thostMacList.clear();
    	thostMacList.addAll(SystemInfo.GWDhelper.getWifiList()); //get current wifi list
    	buf += "Host sees " + thostMacList.size() + " access points.\n";
    	buf += "Now move somewhere else and try to find your way back using \"Compare WiFi Lists\"\n";
    	tv_displayoutput1.setText(buf);
    	hostlistinit = true;
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }

    @Override
    protected void onResume() {
     	super.onResume();
    }
    
    @Override
    protected void onDestroy() {
	super.onPause();
	GameData.currmacint = 0;
	GameData.prevmacint = 0;
    }

    /* For returning from various activities */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
	switch (requestCode) {
	case REQUEST_ENABLE_BT:
	    // When the request to enable Bluetooth returns
	    if (resultCode == Activity.RESULT_OK) {
		// Bluetooth is now enabled
		viewMyBlue(true);
	    } else {
		// User did not enable Bluetooth or an error occurred
		Toast.makeText(this, "Bluetooth not enabled by user",
			Toast.LENGTH_SHORT).show();
	    }
	    // finish();
	}
    }
}
/* end: ViewWirelessData.java */