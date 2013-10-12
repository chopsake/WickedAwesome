/* begin: SystemInfo.java */
package edu.ucdavis.WickedAwesome;

import android.app.Application;

public class SystemInfo extends Application {
    protected static final String SERVER_KEY = "5bnLHqqqZLw60uctCYh1";
    protected static String email = "";
    protected static String password = "";
    protected static String gameTitle = "";
    protected static boolean inGame = false;
    protected static boolean isHost = false;
    protected static GetWirelessData GWDhelper;
    protected static int GPS_WAIT = 4000; //milliseconds to wait for GPS signal acquisition

    @Override
    public void onCreate() {
	super.onCreate();
	email = "";
	password = "";
	gameTitle = "";
	inGame = false;
    }
}
/* end: SystemInfo.java */