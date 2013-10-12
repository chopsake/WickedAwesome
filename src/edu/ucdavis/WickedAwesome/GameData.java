/* begin: GameData.java */
package edu.ucdavis.WickedAwesome;

import java.util.ArrayList;

public class GameData {
    // static class shouldn't have 'new' -- this is not an obj constructor
    protected static ArrayList<WifiList> hostMacList = null;
    protected static ArrayList<WifiList> currentList = null;
    protected static ArrayList<WifiList> prevMacList = null;
    protected static WifiList test;
    protected static double lat = 0;
    protected static double lon = 0;
    protected static double prevDist = 0xffffffff;
    protected static String hostBid = "";
    protected static double poldlat = 0.0; // player's previous GPS latitude
    protected static double poldlon = 0.0; // player's previous GPS longitude
    protected static double pcurrlat = 0.0; // player's current GPS latitude
    protected static double pcurrlon = 0.0; // player's current GPS longitude
    protected static double avgstr = 0xffffffff; // avg signal strength
    protected static int hostStr = 0;
    protected static boolean gameComplete = false; // complete flag
    protected static boolean hostblueseen = false; // true if the host's bluetooth seen by player
    
    /* near_enough should be set to true if player is near enough to 
     * host (by GPS or WiFi calculation, based on game type) to start
     * scanning for host's bluetooth.  If false, bluetooth scan is
     * skipped.
     */
    protected static boolean near_enough = false;//true if near enough to check bluetooth
    protected static final int WIFI_NEAR = 5; //threshold in dBm for avg WiFi signal diff
    protected static final int GPS_NEAR = 10; //threshold meters for GPS
    protected static float prevsigdiff; //used in GameLocation class
    protected static float currsigdiff; //used in GameLocation class
    
    /* For testing in ViewWirelessData, just ignore */
    protected static int prevmacint; //previous count of MACs in common with host
    protected static int currmacint; //current count of MACs in common with host
}
/* end: GameData.java */
