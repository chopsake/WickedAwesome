/* begin: GameLocation.java */
package edu.ucdavis.WickedAwesome;

/* Class GameLocation is used to calculate weather the player's
 * most recent move has gotten them closer to ("warmer") or farther
 * from ("colder") the host.  This is based on a comparison of the
 * GPS and/or Wifi data from the host and player's devices.
 * 
 * The functions used depend on what type of game the player is
 * playing (Wifi or GPS).  This decision is made outside 
 * this class, and executed by calling the appropriate function
 * in this class.
 * 
 * For GPS games, the result is based on using the GPS coordinates
 * of the player and host to calculate the linear distance between
 * them, and comparing that with the previous measurement.
 * 
 * For Wifi games, the result is based on several successive criteria:
 * 	1)	Check if the number of access points in common with the host has 
 * 		increased or decreased...
 *  2)  If the number is equal, then check if the average of the signal
 *  	differences has increased or decreased...
 *  3)	If the average hasn't changed, then check if more/less signals
 *  	have gotten stronger vs. gotten weaker...
 *  4)	If there is still no difference, treat this as a "colder" case.
 *  	This may have occurred if the player didn't move, in which
 *  	case "colder" should prompt them to.
 * 
 * Inputs:
 * 	- ArrayList<Wifi> objects representing the host, player previous and
 * 	  player current Wifi scan lists.
 *  - GPS previous coordinates as double datatypes.
 *  
 * Outputs:
 *  - Integer codes indicating "warmer," "colder," or an error has occurred.
 *  
 * Public methods available in this class 
 *
 * 	GPS function:
 *		GPSLocComp()
 * 
 * 	Wifi function:
 * 		WifiCompare()
 * 		
 */

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class GameLocation {
	private final Context context;

	private static final int ENOUGH_APS = 1;  //minimum # of host's APs that should be visible
	private static final int L_ERROR = 0; //error in location comparison code
	private static final int L_WARMER = 1; //player getting warmer
	private static final int L_COLDER = 2; //player getting colder
	private static final int L_TOOFAR = 3; //player too far for signal attempted
	private static final int S_STRONGER = 4; //player getting warmer based on signal strength
	private static final int S_WEAKER = 5; //player getting colder based on signal strength
	private static final int S_NO_CHANGE = 6; //no change in signal strength
	private static final int B_ERROR = 0; //bluetooth scanning code error
	private static final int B_FOUND = 1; //host bluetooth MAC address found
	private static final int B_NOTSEEN = 2; //host bluetooth not found
	
	private ArrayList<WifiList> hwifi; //host WiFi AP list
	private ArrayList<WifiList> pwifiold; //player's old wifi list
	private ArrayList<WifiList> pwificur; //player's current wifi list
	private ArrayList<WifiList> prev_int; //count of previous intersection of host & player lists
	private ArrayList<WifiList> curr_int; //count of current intersection of host & player lists
	//private ArrayList<Wifi> signaldelta = new ArrayList<Wifi>();//not necessary, but might be useful later
	public float fudgefactor1;
	public float fudgefactor2;
	
	/* Stuff for Bluetooth scan */
	//public final ArrayAdapter<String>mArrayAdapter; //for bluetooth scan
	BluetoothAdapter mBluetoothAdapter;
	IntentFilter intentfilter; //for bluetooth scan
	//private ArrayList<String> bluescans; //stores the results of the bluetooth scan
	String hostblue;
	int bluefound = 0;
	private static final int BLUESCAN_WAIT = 0; //wait for discovery during BlueScan
	
    // -- constructor --
    public GameLocation(Context ctx){
        this.context = ctx;
        hwifi = new ArrayList<WifiList>();
        pwifiold = new ArrayList<WifiList>();
        pwificur = new ArrayList<WifiList>();
        prev_int = new ArrayList<WifiList>();
        curr_int = new ArrayList<WifiList>();
        //bluescans = new ArrayList<String>();
        //mArrayAdapter = new ArrayAdapter<String>(context, 0, bluescans);
    }
	
    
    /* Broadcast receiver for bluetooth scan. 
     * This is turned on by BlueScan() and stays on until GameLocation object
     * is destroyed.
     */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        //Clear the scan list on each new scan 
	        
	        /*
	        if(mBluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
	        	bluescans.clear();
	        	//bluefound = 0;
	        }*/

	        // When discovery finds a device
	        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
	            // Get the BluetoothDevice object from the Intent
	            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
	            
	            if(device.getAddress().equals(hostblue) ){
	            	bluefound = 1;
	            	/*if(bluescans.contains(hostblue)){
	            		bluescans.add(hostblue);
	            	}*/
	            	Toast.makeText(context, "Host bluetooth detected\n",
	        	    	Toast.LENGTH_LONG).show();
	            }
	        }	        
	    }
	};
	
    public int BlueScan(String hostBlue){
    	//if(mBluetoothAdapter.isDiscovering()){
	    	if(bluefound == 1){
	    		return B_FOUND; //found host's bluetooth!
	    	}
    	//}//otherwise, start a new scan
    	
    	hostblue = hostBlue;
    	// Get the BluetoothAdapter
    	mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	Toast.makeText(context, "Checking Bluetooth adapter", Toast.LENGTH_LONG).show();
    	if (mBluetoothAdapter == null) {
    	    // Device does not support Bluetooth
    	    Toast.makeText(context, "Bluetooth is not available",
    		    Toast.LENGTH_LONG).show();
    	    return B_ERROR;
    	}
    	else {
    	    //Toast.makeText(context, "Bluetooth IS available", Toast.LENGTH_LONG).show();
    	}

    	/* Assume Bluetooth is enabled at this point, if not,
    	 * this will fail.
		*/
    	if (mBluetoothAdapter.isEnabled()) {
    		//turn on host discoverable
    		
    	    mBluetoothAdapter.startDiscovery();
    	    
    		Toast.makeText(context, "Scanning for Bluetooth devices",
        			Toast.LENGTH_SHORT).show();
    		
    		// Register the BroadcastReceiver
    		intentfilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    		context.registerReceiver(mReceiver, intentfilter); // Don't forget to unregister during onDestroy  

    		//Delay, if needed
    		/*
			try {
				Thread.sleep(BLUESCAN_WAIT); //need to wait for Wifi enabling
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/

    		
        	if(bluefound == 1){
        		return B_FOUND; //found host's bluetooth!
        	}
        	else{
        		return B_NOTSEEN; //can't see host's bluetooth
        	}
    	}
    	else {
    		// User did not enable Bluetooth or an error occurred
    		Toast.makeText(context, "Bluetooth not enabled by user",
        			Toast.LENGTH_SHORT).show();
    	}
    	return B_ERROR;
    }
    	
    public void stopBlueScan(){
    	mBluetoothAdapter.cancelDiscovery();
    	context.unregisterReceiver(mReceiver);
    }
    
    
    public int WifiCompare(ArrayList<WifiList> hostMacList,
    						  ArrayList<WifiList> playerOldMacList,
    						  ArrayList<WifiList> playerCurrMacList){
    	int response = L_ERROR;
    	
    	hwifi.clear();
    	pwifiold.clear();
    	pwificur.clear();
    	hwifi.addAll(hostMacList);
    	pwifiold.addAll(playerOldMacList);
    	pwificur.addAll(playerCurrMacList);
    	float sigleveldiff;
    	//
    	prev_int.clear();
    	curr_int.clear();
     	
    	/* Make sure data is ready */
        if(hwifi.isEmpty() || pwificur.isEmpty()){
    	    Toast.makeText(context, "checking hwifi and pwificur not empty", Toast.LENGTH_SHORT).show();
        	return L_ERROR;
        }
        
    	/* If pwifiold is null, then player hasn't moved yet! */
        if(pwifiold.isEmpty()){
        	return L_COLDER;
        }
        
        /* prev_int = intersection of wifi lists by MAC(host, player's previous) */
        for(int i=0; i<hwifi.size(); i++){
        	for(int j=0; j<pwifiold.size(); j++){
        		if(hwifi.get(i).getMac().equals(pwifiold.get(j).getMac())){
        			prev_int.add(pwifiold.get(j));
        		}
        	}
        }
        
        /* curr_int = intersection of wifi lists by MAC(host, player's previous) */
        for(int i=0; i<hwifi.size(); i++){
        	for(int j=0; j<pwificur.size(); j++){
        		if(hwifi.get(i).getMac().equals(pwificur.get(j).getMac())){
        		//if(hwifi.get(i).mac.toString() == pwificur.get(j).mac.toString()){
        			curr_int.add(pwificur.get(j));
        		}
        	}
        }
        /* Set near enough flag if signal level threshold reached */
        GameData.prevsigdiff = GameData.currsigdiff; //save previous data
        GameData.currsigdiff = getSigLevelDiff();
    	if(GameData.currsigdiff <= GameData.WIFI_NEAR){
    		GameData.near_enough = true;//near enough to start scanning for host bluetooth
    	}
    	else{
    		GameData.near_enough = false;//not near enough
    	}
    	
        
        /* For testing in ViewWirelessData */
        GameData.prevmacint = GameData.currmacint;
        
        /* Make sure current scan is seeing enough of the host's access points */
        if(curr_int.isEmpty()){
        	GameData.currmacint = 0;//for testing in ViewWirelessData
        	//return L_TOOFAR;
        }

        /* For testing in ViewWirelessData */
        GameData.currmacint = curr_int.size();
        if(prev_int.isEmpty()){
        	GameData.prevmacint = 0;
        }
        else{
        	GameData.prevmacint = prev_int.size();
        }
        
       
        if(curr_int.size() < ENOUGH_APS){ //below min threshold for matching MACs
        	return L_TOOFAR;
        }
        fudgefactor1 = (float)(curr_int.size() - prev_int.size()) * (GameData.currsigdiff - GameData.prevsigdiff);
        fudgefactor2 = ((float)curr_int.size() * GameData.currsigdiff);
        
        /* Compare wifi lists of host and player to determine "warmer" or "colder":
        * 	1) Find # of APs in common between host's list and player's previous
        * 	2) If counts equal, compare signal levels from previous to current
        */
       if(curr_int.size() > prev_int.size()){ //if(current count > previous count)
    	   return L_WARMER;
       }
       else if(curr_int.size() < prev_int.size()){
    	   return L_COLDER;
       }
       else{ //current count == previous count, or avg sig diff conflicts
    	   switch(CompareSigLevels()){ //so compare signal levels
    	   case S_STRONGER: //if signal levels stronger
    		   response = L_WARMER;
    		   break;
    	   case S_WEAKER: //if signal levels weaker
    		   response =  L_COLDER;
    		   break;
    	   case S_NO_CHANGE: //No change in signal level, should be very unlikely
    		   //unless player didn't move :-(
    		   //	This is neither warmer or colder, so tell player to move in different 
    		   //direction (or move at all) by saying "colder."
    		   response =  L_COLDER;   
    		   break;
    	   default:
    		   response = L_ERROR;
    		   break;
    	   }
       }
    	
	return response;
    }

   public int getPrevWifiCount(){
	   if(prev_int.isEmpty()){
		   return 0;
	   }
	   else{
		   return prev_int.size();
	   }
   }

   public int getCurrWifiCount(){
	   if(curr_int.isEmpty()){
		   return 0;
	   }
	   else{
		   return curr_int.size();
	   }
   }
   
    /* Only used if both positions see the same number of APs in common with the host.
     * This compares signal levels in -dBm, not SNR values. 
     * Also assumes pwificur and pwifiold are not empty (shouldn't have gotten here
     * if they were).
     */
    private int CompareSigLevels(){
    	int delta = 0;
    	int deltasum = 0;
    	float deltaavg = 0;
    	int strongersigs = 0;
    	int weakersigs = 0;
    	int samesigs = 0;
    	int cur_count = pwificur.size();
    	int old_count = pwifiold.size();
    	
    	/* Find common MACs, and calculate signal change */
        for(int i=0; i<cur_count; i++){
        	for(int j=0; j<old_count; j++){
        		if(pwifiold.get(j).getMac().equals(pwificur.get(i).getMac())){
        			delta = pwificur.get(i).getSignal() - pwifiold.get(j).getSignal();
        			deltasum += delta;
        			if(delta > 0){
        				strongersigs++;
        			}
        			else if(delta < 0){
        				weakersigs++;
        			}
        			else{
        				samesigs++;
        			}
        		}
        	}
        }
        deltaavg = ((float)deltasum) / ((float)(strongersigs + weakersigs + samesigs));
        if(deltaavg > (float)0){ //on average, signals are stronger
        	return S_STRONGER;
        }
        else if(deltaavg < (float)0){ //on average, signals are weaker
        	return S_WEAKER;
        }
        else{ //on average, no stronger or weaker, so check each
        	if(strongersigs > weakersigs){
        		return S_STRONGER;
        	}
        	else if(strongersigs < weakersigs){
        		return S_WEAKER;
        	}
        	else{ //really unlikely at this point, but here it is
        		return S_NO_CHANGE;
        	}
        }
    }
    
    /* getSigLevelDiff finds the average signal level difference
     * between the player and the host.  Generally this should be
     * less than the host, and represented in the output of this
     * function as the absolute value.
     */
    public float getSigLevelDiff(){
    	int delta = 0;
    	int deltasum = 0;
    	float deltaavg = 0;
    	int cur_count = pwificur.size();
    	int host_count = hwifi.size();
    	int divisor = 0;
    	/* Find common MACs, and calculate signal change */    	
        for(int i=0; i<cur_count; i++){
        	for(int j=0; j<host_count; j++){
        		if(hwifi.get(j).getMac().equals(pwificur.get(i).getMac())){
        			delta = hwifi.get(j).getSignal() - pwificur.get(i).getSignal();
        			if(delta>0){//only count if delta reasonable
        				deltasum += delta;
        				divisor++;
        			}
        		}
        	}
        }
        //deltaavg = ((float)deltasum) / ((float)(cur_count));
        if(divisor==0){//prevent divide by zero
        	deltaavg = GameData.WIFI_NEAR+100; //likely too far anyway
        }
        else{        	
        	deltaavg = deltasum / divisor;
        	deltaavg = deltaavg * ((float)host_count / (float)cur_count);//weighting factor
        }
        return deltaavg;    	
    }
    
    /* GPSLocComp compares the old position relative to the host to the new position,
     * and returns whether the player has gotten closer or not.
     * 
     * If this function is called, it is assumed that GPS is working, and that the
     * host coordinates are valid.  BUT, this may be the first position check, in 
     * which case the old coordinates are invalid.  This case is treated as a
     * "colder" situation, prompting the player to move to establish a second set
     * of coordinates to compare with.
     * 
     */
    public int GPSLocComp(double hostlat, double hostlon, 
    		double poldlat, double poldlon, 
    		double pcurlat, double pcurlon){
 
    	if(poldlat == 0.0 || poldlon == 0.0){
    		return L_COLDER; //must be player's first position check - they need to move again
    	}
    	int response = L_ERROR;
    	float old_distance, new_distance; //ranges to host
    	
    	/* Uses built-in Android function to find distance between points in meters: */
    	old_distance = SystemInfo.GWDhelper.getGPSdiff(hostlat, hostlon, poldlat, poldlon);
    	new_distance = SystemInfo.GWDhelper.getGPSdiff(hostlat, hostlon, pcurlat, pcurlon);
    	
    	/* Check if near enough threshold reached */
    	if(new_distance <= GameData.GPS_NEAR){
    		GameData.near_enough = true;//near enough to start scanning for host bluetooth
    	}
    	else{
    		GameData.near_enough = false;//not near enough
    	}
    	
    	if(new_distance < old_distance){
    		response = L_WARMER;
    	}
    	else if(new_distance > old_distance){
    		response = L_COLDER;
    	}
    	else{
    		response = L_ERROR;
    	}
    	return response;
    }

    protected void onDestroy() {
    	stopBlueScan();
    }
}
/* end: GameLocation.java */
