/* begin: ViewBlueScan.java */
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

public class ViewBlueScan extends ListActivity {
	TextView tv_bscanoutput = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.testdatadisplayblue);
	setListAdapter(SystemInfo.GWDhelper.mArrayAdapter);
	tv_bscanoutput = (TextView) findViewById(R.id.displayoutput);
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

    }


}
/* end: ViewBlueScan.java */