/* begin: GetWirelessData.java */
package edu.ucdavis.WickedAwesome;

/* Public methods available in this class (see ViewWirelessData.java
 * to see how these are used):
 *
 * 	GPS functions:
 * 		Before using any GPS functions, do the following:
 * 			1) Declare class object:
 * 				private GetWirelessData GWDhelper;
 * 			2) Allocate the object in onCreate:
 * 				GWDhelper = new GetWirelessData(this);
 * 			3) Start the listener
 * 				GWDhelper.listenMyGps();
 * 			4) Call the function you want, for example:
 * 				GWDhelper.getGPSlat();
 * 			5) If you don't need GPS for awhile, turn the
 * 			   listener off:
 * 				stopListenGPS();
 *
 * 		public boolean listenMyGps(Context ctx)
 * 			This turns on the GPS listener, need to call this before
 * 			using any GPS functions.  Pass it the context of the activity
 * 			you are calling it from.  Returns false if GPS is off or
 * 			not enabled.
 * 		public boolean isGPSupdated()
 * 			Check to see if GPS is updated.  If false, the coordinates
 * 			returned by getGPSlat() and getGPSlong() are what was
 * 			previously stored on the phone.
 * 		public double getGPSlat()
 * 			Gets the GPS latitude value.  If -999.9 is returned, then
 * 			GPS is turned off or not functioning.  If 0.0 is returned,
 * 			then the GPS is on but still acquiring a signal.
 * 		public double getGPSlong()
 * 			Gets the GPS longitude value.  If -999.9 is returned, then
 * 			GPS is turned off or not functioning.  If 0.0 is returned,
 * 			then the GPS is on but still acquiring a signal.
 * 		public void stopListenGPS()
 * 			Call this after using GPS functions to turn off the listener.
 *
 * 	Bluetooth functions:
 * 		public String getMyBlue()
 * 			Returns the MAC address as a string.
 * 			Note that Bluetooth needs to be enabled on the phone by
 * 			the user before this will work.  Otherwise, it will
 * 			return a bogus MAC address of ZZ:ZZ:ZZ:ZZ:ZZ.  To see an
 * 			automated way of prompting the user to enable Bluetooth,
 * 			see the example in the viewMyBlue function of ViewWirelessData.
 *
 * 	Wifi functions:
 * 		public int initAPList()
 * 			This initializes the getAP____ functions.  It enables Wifi
 * 			if not already enabled, performs a scan, and dumps the
 * 			results in wifiscanlist and the size of the list into
 * 			aplistsize.  This now static data is operated on by the
 * 			following getAP functions.  This function MUST be performed
 * 			before calling the other getAP functions, or they will return
 * 			invalid data.
 * 		public int getAPListSize()
 * 			Returns the size of the AP list, which equals the number
 * 			of access points detected in the scan.
 * 		public ArrayList<String> getAPList()
 * 			Returns the access point list, where each member of the
 * 			list is a string representing the MAC address in the standard
 * 			ff:ff:ff:ff:ff format.  Access an individual item from the list
 * 			with the call getAPList().get(i) where "i" is the integer
 * 			value array index.
 * 		public ArrayList<Integer> getAPLevels()
 * 			Similiar to getAPList, this function returns a list of
 * 			the signal strengths for each AP.  Note that these are integer
 * 			values in dBm, and not signal-to-noise ratios.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.widget.ArrayAdapter;
import android.content.Intent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.app.ListActivity;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.widget.Toast;


public class GetWirelessData {
	private final Context context;

    private boolean test_phone; //Use for areas we want to behave
    							//differently on test phone.

	/* Variables for GPS functions */
	private double gpslat;
	private double gpslong;
	private boolean gpslistening;
	private boolean gpsupdated;
	private LocationListener loclisten;
	private LocationManager locationManager;
	private float gpsdiff[];

	/* Constants and variables for Bluetooth functions */
    // Bluetooth Intent request code
	BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 3;
	public final ArrayAdapter<String>mArrayAdapter;
	IntentFilter intentfilter;

    /* Constants, variables, objects for Wifi */
    private WifiManager wifiManager;
    private List<ScanResult> wifiscanlist = null;//used for single scan
    private ArrayList<WifiList> wifiscanlistavg; //used for multiple scan
    private static final boolean WIFI_MULTISCAN = true; //true to run multi-scan, false for single
    private int aplistsize;
    private static final int WIFI_WAKE_WAIT = 7000;
    private static final int WIFI_SCAN_NUM = 40; //number of WiFi scans to average
    private static final int WIFI_SCAN_WAIT = 50; //wait time (ms) between scans
    private static final int WIFI_MINHITS = 5; //minimum hits in WIFI_SCAN_NUM scans 
    private static final int WIFI_SIGMIN = -85; //minimum threshold for signal level

    // -- constructor --
    public GetWirelessData(Context ctx)
    {
    	context = ctx;
        gpslistening = false;
        test_phone = false;
        wifiscanlistavg = new ArrayList<WifiList>();
        mArrayAdapter = new ArrayAdapter<String>(context, R.layout.testdatadisplayblue, R.id.displayoutput);
        
    }

    /* Toggle test_phone global flag to configure for test phone use
     *
     */
    public boolean setTestPhone(boolean istestphone){
    	test_phone = istestphone;
    	return test_phone;
    }

    public boolean listenMyGps(){
    	// Acquire a reference to the system LocationManager
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){//GPS is off
			Toast.makeText(context, "GPS provider is not enabled, turn GPS on", Toast.LENGTH_LONG).show();
		}

    	if (gpslistening){//listener already on
    		//Toast.makeText(context, "GPS listener already on", Toast.LENGTH_LONG).show();
    		return true;
    	}

    	Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		if(!setLastLocation(lastKnownLocation)){
			Toast.makeText(context, "GPS is not on or still waiting for signal", Toast.LENGTH_LONG).show();
			//return false;
		}
		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	// Called when a new location is found by the network location provider.
		    	if(gpslistening){
		    		updateLocation(location);
		    	}
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}

		  };

		// Register the listener with the Location Manager to receive location updates
		// (battery consumption increases!!)
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		gpslistening = true;
		loclisten = locationListener; //for stopListenGPS()
		return true;
	}

    private boolean setLastLocation(Location location) {
    	gpsupdated = false;
    	if (location == null) { //GPS is off or still waiting for first signal
        	gpslat = -999.9;
        	gpslong = -999.9;
        	return false;
    	}
    	else
    	{	//GPS data is stale
        	gpslat = location.getLatitude();
        	gpslong = location.getLongitude();
        	return true;
    	}

    }

    private void updateLocation(Location location){
    	if (location != null) {
        	gpslat = location.getLatitude();
        	gpslong = location.getLongitude();
        	gpsupdated = true;
	    	//Toast.makeText(context, "GPS:" + gpslat + " " + gpslong, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isGPSlistening(){
    	return gpslistening;
    }

    public boolean isGPSupdated(){
    	return gpsupdated;
    }

    public double getGPSlat(){
    	return gpslat;
    }

    public double getGPSlong(){
    	return gpslong;
    }

    /* Uses built-in Android function to determine distance
     * between two points on the globe in meters.
     */
    public float getGPSdiff(double startLat, double startLon, double endLat, double endLon){
    	float diff = 0;
    	gpsdiff = new float[3];
    	
    	Location.distanceBetween(startLat, startLon, endLat, endLon, gpsdiff);
    	diff = gpsdiff[0];
    	return diff;
    }
    
    
    /* Returns string of satellites in view...if it would work.
     * But this is NOT WORKING YET.
     */
    public int getGPSSats(){ //returns number of satellites in view
        int numsats = 0;
        GpsStatus status = locationManager.getGpsStatus(null);
        Iterable<GpsSatellite> sats = status.getSatellites();
        Iterator<GpsSatellite> satsit = sats.iterator();
        for(int i=0; i<100; i++){
        	if( satsit.hasNext()){
        		numsats++;
        	}
        }
        return numsats;
    }

    public void stopListenGPS(){
    	if(loclisten != null){
     		locationManager.removeUpdates(loclisten); // Turn off location updates
    		gpslistening = false;
    		loclisten = null;
    		Toast.makeText(context, "GPS updates turned off", Toast.LENGTH_LONG).show();
    	}
    }

    public String getMyBlue() {
    	String btmac = "ZZ:ZZ:ZZ:ZZ:ZZ:ZZ";

    	// Get the BluetoothAdapter
    	//BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	Toast.makeText(context, "Checking Bluetooth adapter", Toast.LENGTH_LONG).show();
    	if (mBluetoothAdapter == null) {
    	    // Device does not support Bluetooth
    	    Toast.makeText(context, "Bluetooth is not available",
    		    Toast.LENGTH_LONG).show();
    	    return null;
    	}
    	else {
    	    Toast.makeText(context, "Bluetooth IS available", Toast.LENGTH_LONG).show();
    	}

    	/* Assume Bluetooth is enabled at this point, if not,
    	 * this will fail.
		*/
    	if (mBluetoothAdapter.isEnabled()) {
    		// Get my Bluetooth MAC address
    	    btmac = mBluetoothAdapter.getAddress();
    		Toast.makeText(context, "Bluetooth is enabled",
        			Toast.LENGTH_SHORT).show();
    	}
    	else {
    		// User did not enable Bluetooth or an error occurred
    		Toast.makeText(context, "Bluetooth not enabled by user",
        			Toast.LENGTH_SHORT).show();
    	}
    	return btmac;
    }

	// Create a BroadcastReceiver for ACTION_FOUND
	/* This version only for testing in ViewWireless, working version is in GameLocation */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            // Add the name and address to an array adapter to show in a ListView
	            mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
	            Toast.makeText(context, "Bluetooth device\n" + device.getAddress() + "\ndetected\n",
	        		    Toast.LENGTH_LONG).show();
	            
	        }
	    }
	};

	/* This version only for testing in ViewWireless, working version is in GameLocation */
    public void scanBlue() {   	
    	// Get the BluetoothAdapter
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	Toast.makeText(context, "Checking Bluetooth adapter", Toast.LENGTH_LONG).show();
    	if (mBluetoothAdapter == null) {
    	    // Device does not support Bluetooth
    	    Toast.makeText(context, "Bluetooth is not available",
    		    Toast.LENGTH_LONG).show();
    	    return;
    	}
    	else {
    	    //Toast.makeText(context, "Bluetooth IS available", Toast.LENGTH_LONG).show();
    	}

    	/* Assume Bluetooth is enabled at this point, if not,
    	 * this will fail.
		*/
    	if (mBluetoothAdapter.isEnabled()) {
    		
    	    mBluetoothAdapter.startDiscovery();
    	    
    		Toast.makeText(context, "Scanning for Bluetooth devices",
        			Toast.LENGTH_SHORT).show();
    		
    		// Register the BroadcastReceiver
    		intentfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    		context.registerReceiver(mReceiver, intentfilter); // Don't forget to unregister during onDestroy  
    	}
    	else {
    		// User did not enable Bluetooth or an error occurred
    		Toast.makeText(context, "Bluetooth not enabled by user",
        			Toast.LENGTH_SHORT).show();
    	}
    	return;
    }
    

	/* Function initAPList initializes the Wifi AP list by:
     * 	1) Stop the GPS listener.
     * 	2) Get reference to WifiManager.
     * 	3) Check if Wifi is enabled, enable it if it's not or
     * 	   send error message if it can't enable.  Since the
     * 	   enabling process isn't immediate, wait for milliseconds
     * 	   set by WIFI_WAKE_WAIT constant.
     * 	4) Scan for access points.
     * 	5) Put all scan results in global List<ScanResult> object
     *     wifiscanlist.
     *  6) Put the size of the list in global variable aplistsize.
     *
     *  Returns false if there is an error enabling or scanning Wifi.
     *
     *  Subsequently called functions getAPList() and getAPLevels()
     *  extract the BSSID and signal level fields from the scan
     *  results and return them as ArrayList objects.
     */
    public boolean initAPListOld(){
    	/* Turn off the GPS listener */
    	stopListenGPS();

    	/* Acquire a reference to the system WifiManager */
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		/* Check if Wifi is enabled, enable or error if not */
		if(!wifiManager.isWifiEnabled()){
    		wifiManager.setWifiEnabled(true);
    		Toast.makeText(context, "Wifi was not enabled, enabling now",
        			Toast.LENGTH_SHORT).show();
			try {
				Thread.sleep(WIFI_WAKE_WAIT); //need to wait for Wifi enabling
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		if(wifiManager.isWifiEnabled()){
    			Toast.makeText(context, "Wifi is now enabled, starting scan",
            			Toast.LENGTH_SHORT).show();
    			wifiManager.startScan();
    		}
    		else{
    			Toast.makeText(context, "Error: Wifi not enabling, quitting",
            			Toast.LENGTH_SHORT).show();
    			return false;
    		}
    	}
    	else{
    		Toast.makeText(context, "Wifi enabled",
        			Toast.LENGTH_SHORT).show();
    		wifiManager.startScan();
    	}

		wifiscanlist = wifiManager.getScanResults();
     	if(!test_phone){
     		wifiManager.setWifiEnabled(false);//turn off Wifi, unless on test phone
     	}
     	aplistsize = wifiscanlist.size();
     	return true;
    }

    public boolean initAPList(){
    	/* Turn off the GPS listener */
    	//stopListenGPS();

    	/* Acquire a reference to the system WifiManager */
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

		/* Check if Wifi is enabled, enable or error if not */
		if(!wifiManager.isWifiEnabled()){
    		wifiManager.setWifiEnabled(true);
    		Toast.makeText(context, "Wifi was not enabled, enabling now",
        			Toast.LENGTH_SHORT).show();
			try {
				Thread.sleep(WIFI_WAKE_WAIT); //need to wait for Wifi enabling
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		if(wifiManager.isWifiEnabled()){
//    			Toast.makeText(context, "Wifi is now enabled, starting scan",
//            			Toast.LENGTH_SHORT).show();
        		/* Don't comment out, use WIFI_MULTISCAN to toggle */
        		if(WIFI_MULTISCAN){
        			Toast.makeText(context, "Performing WiFi multiple scan...",
                			Toast.LENGTH_SHORT).show();        			
        			scanWifi(WIFI_SCAN_NUM);//multiple scan
        		}
        		else{
        			wifiManager.startScan();//single scan
        		}
    		}
    		else{
    			Toast.makeText(context, "Error: Wifi not enabling, quitting",
            			Toast.LENGTH_SHORT).show();
    			return false;
    		}
    	}
    	else{
//    		Toast.makeText(context, "Wifi enabled",
//        			Toast.LENGTH_SHORT).show();
    		
    		/* Don't comment out, use WIFI_MULTISCAN to toggle */
    		if(WIFI_MULTISCAN){
    			scanWifi(WIFI_SCAN_NUM);//multiple scan
    		}
    		else{
    			wifiManager.startScan();//single scan
    		}
    	}

		wifiscanlist = wifiManager.getScanResults();
//     	if(!test_phone){
//     		wifiManager.setWifiEnabled(false);//turn off Wifi, unless on test phone
//     	}
     	aplistsize = wifiscanlist.size();
     	return true;
    }

 
    /* Function scanWifi calls wifiManager.startScan() either
     * once, or number of times specified by argument numscans.
     * Signal levels are then averaged for each access point
     * MAC address.
     * 
     * Multiple scans are required for accuracy of AP list in
     * typical volatile Wifi environment.  Use single scan
     * for testing purposes.  If 0 or 1 is provided as argument,
     * assume 1 scan.
     *
     * Returns false if an error occurs or scan list is invalid.
     */
    private boolean scanWifi(int numscans){    	
    	List<ScanResult> wifiscanresults = null; //ScanResult is fixed size, so doesn't need "new"
    	List<ScanResult> wifibucket = null; 
    	wifiscanlistavg.clear();
    	int wsrc = 0; //wifiscanlist index counter
    	int bktc = 0; //wifibucket index counter
    	int levels, avglev;
    	
    	if(numscans <= 1){//just do single scan
    		wifiManager.startScan();
    		wifiscanlist = wifiManager.getScanResults();
	     	for (int i = 0; i < wifiscanlist.size(); i++) {
	     		wifiscanlistavg.add(new WifiList(wifiscanlist.get(i).BSSID.toString(), wifiscanlist.get(i).level));
	    	}
    		return true;
    	}
    	else{
    		/* Notify got here succesfully */
    		Toast.makeText(context, "Trying multiple Wifi scans",
        			Toast.LENGTH_SHORT).show();

    		/* Run multiple scans, appending to single list */
    		for(int i=0; i<numscans; i++){
	    		if(wifiManager.startScan()){ //run scan/probe
	    			if(i==0){
	    				wifiscanresults = wifiManager.getScanResults();
	    			}	
	    			wifiscanresults.addAll(wifiManager.getScanResults());//append results to scan list
	    		}
				try {
					Thread.sleep(WIFI_SCAN_WAIT); //need to wait between scans
					} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					}
    		}//end multiple scans loop
    	}//end multiple scans case
    		
		/* Consolidate list:
		 * 	- eliminate dupes and low counts
		 *  - average signal levels
		 */
    	wifibucket = wifiManager.getScanResults(); //because I couldn't figure out how to instantiate List object
    	wifibucket.clear();
    	
    	/* Purge scan list of all results with signal < WIFI_SIGMIN */
    	int scanlevel;
    	for(int i=0; i<wifiscanresults.size(); i++){
    		scanlevel = wifiscanresults.get(i).level;
    		if(scanlevel < WIFI_SIGMIN){
    			wifiscanresults.remove(i);
    			i=0;
    		}
    	}
    	wsrc = 0;
    	while(!wifiscanresults.isEmpty()){
    		if(wifibucket.isEmpty()){
    			wifibucket.add(wifiscanresults.get(wsrc));//get first element of wifiscanresults
    														//and copy to wifibucketlist
    			wifiscanresults.remove(wsrc);//remove from scan results
    			wsrc = 0;//reset wifiscanresults index
    			bktc = 0;//bucket counter
    		}
    		else{ //comparing MACs in wifiscanresults and wifibucket
    			/* If top of wifiscanresults == wifibucket: */
     			
                if(wifibucket.get(bktc).BSSID.equals(wifiscanresults.get(wsrc).BSSID)){
                	wifibucket.add(wifiscanresults.get(wsrc));//copy to bucket list
                	wifiscanresults.remove(wsrc);//remove from scan results
                    bktc++;     // increment bucketcounter              
                	if(bktc == WIFI_SCAN_NUM){//covered all the scan results already
                		
                		/* Consolidate list for one MAC */
                		levels = avglev = 0;
                		for(int i=0; i<bktc; i++){//sum up signal levels in bucket list
                			levels += wifibucket.get(i).level;
                		}
                		avglev = (int) ((float)levels / (float)bktc);//average signal levels
                		//add MAC, avg signal level to wifiscanlistavg:
                        wifiscanlistavg.add(new WifiList(wifibucket.get(0).BSSID.toString(), avglev));
                        
                        wifibucket.clear();//empty bucket
                		bktc = 0; //reset bucketcounter
                    }

                    /*if(bktc == WIFI_SCAN_NUM){//covered all the scan results already
                		
                		// Consolidate list for one MAC 
                		levels = avglev = 0;
                		for(int i=0; i<wifibucket.size(); i++){//sum up signal levels in bucket list
                			if(wifibucket.get(i).level < WIFI_SIGMIN){
                				wifibucket.remove(i);   
                				i=0;
                			}
                			if(wifibucket.isEmpty()){
                				break;
                			}
                			levels += wifibucket.get(i).level;
                		}
                		if(!wifibucket.isEmpty()){
                			avglev = (int) ((float)levels / (float)wifibucket.size());//average signal levels
                			//add MAC, avg signal level to wifiscanlistavg:
                			wifiscanlistavg.add(new WifiList(wifibucket.get(0).BSSID.toString(), avglev));
                		}
                        
                        wifibucket.clear();//empty bucket
                		bktc = 0; //reset bucketcounter
                    }*/
                }
                /* Else, skip  */
                else{
                	wsrc++; //increment wifiscanresults index
                }              
     		}
    		/* Check if this MAC appeared in enough scans */
    		if(wsrc  == wifiscanresults.size()){//end of pass through wifiscanresults list
    			if(bktc > WIFI_MINHITS){//did this MAC appear in enough scans?
 
    				
            		// Consolidate list for one MAC
            		levels = avglev = 0;
            		for(int i=0; i<bktc; i++){//sum up signal levels in bucket list
            			levels += wifibucket.get(i).level;
            		}
            		avglev = (int) ((float)levels / (float)bktc);//average signal levels
            		//add MAC, avg signal level to wifiscanlistavg:
                    wifiscanlistavg.add(new WifiList(wifibucket.get(0).BSSID.toString(), avglev));
                    
 
            		/*levels = avglev = 0;
            		for(int i=0; i<wifibucket.size(); i++){//sum up signal levels in bucket list

            			if(wifibucket.get(i).level < WIFI_SIGMIN){
            				wifibucket.remove(i);   
            				i=0;
            			}
            			if(wifibucket.isEmpty()){
            				bktc = 0;
            				break;
            			}
            			levels += wifibucket.get(i).level;
            		}
            		if(!wifibucket.isEmpty()){
            			avglev = (int) ((float)levels / (float)wifibucket.size());//average signal levels
            			//add MAC, avg signal level to wifiscanlistavg:
            			wifiscanlistavg.add(new WifiList(wifibucket.get(0).BSSID.toString(), avglev));
            		}*/
            		
                    
    			}//otherwise, not gonna count this one
    			wifibucket.clear();//empty bucket
    			bktc = 0; //reset bucketcounter
    			wsrc = 0; //reset wifiscanresults counter
    		}
    	}//end while loop

    	//wifiscanlist = wifiscanresults; //set global var to consolidated list
    	
    	/* Clean up */
    	wifiscanresults.clear();
    	wifibucket.clear();
    	
    	aplistsize = wifiscanlistavg.size();
    	
     	return true;
    }

    public String getMyWifiMAC() {//NOT WORKING YET
    	String myMAC = "XX:XX:XX:XX:XX:XX";
    	//wifimanager.
    	return myMAC;
    }

    public int getAPListSizeOld() {
    	if(!initAPListOld()){
    		Toast.makeText(context, "Wifi function initAPList() failed",
        			Toast.LENGTH_SHORT).show();
    	}
    	return aplistsize;
    }
    public int getAPListSize() {
    	return aplistsize;
    }

    public ArrayList<String> getAPList(){
    	ArrayList<String> aplist = new ArrayList<String>();
    	for(int i=0; i < wifiscanlist.size(); i++){
    		aplist.add(wifiscanlist.get(i).BSSID.toString());
    	}
    	return aplist;
    }

    public ArrayList<Integer> getAPLevels(){
    	ArrayList<Integer> aplist = new ArrayList<Integer>();
    	for(int i=0; i < wifiscanlist.size(); i++){
    		aplist.add(wifiscanlist.get(i).level);
    	}
    	return aplist;

    }

    // Bundle up data together in WifiList class
    // List and Levels treated as Strings
    // Tested good by Eric 2:12pm 5/25
    public ArrayList<WifiList> getAPWifiList() {
	ArrayList<WifiList> aplist = new ArrayList<WifiList>();
	for (int i = 0; i < wifiscanlist.size(); i++) {
	    aplist.add(new WifiList(wifiscanlist.get(i).BSSID.toString(), wifiscanlist.get(i).level));
	}
	return aplist;
    }

    /* Use this instead of getAPWifiList */
    public ArrayList<WifiList> getWifiList() {
		/* Don't comment out, use WIFI_MULTISCAN to toggle */
		if(WIFI_MULTISCAN){//multiple scan
			return wifiscanlistavg;
		}
		else{ //single scan
	    	ArrayList<WifiList> aplist = new ArrayList<WifiList>();
	     	for (int i = 0; i < wifiscanlist.size(); i++) {
	    	    aplist.add(new WifiList(wifiscanlist.get(i).BSSID.toString(), wifiscanlist.get(i).level));
	    	}
	    	return aplist;
		}

    }
    

    //@Override
    protected void onDestroy() {
    	//stop bluetooth discovery
    	mBluetoothAdapter.cancelDiscovery();
    	//unregister broadcast reciever used in scanBlue
    	context.unregisterReceiver(mReceiver);
    }
}
/* end: GetWirelessData.java */