/* begin: HostList.java */
package edu.ucdavis.WickedAwesome;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

// Display lists of users currently in the host's game
public class HostList extends ListActivity {
    private PostData data = new PostData();
    protected ArrayList<String> UserList = new ArrayList<String>();

    private static final int MENU_REFRESH = Menu.FIRST;
    private static final int MENU_QUIT = Menu.FIRST + 1;

    private static final String URL = "http://mmayfield.com/ecs152c/hostlist.php";
    private static final String QUIT_URL = "http://mmayfield.com/ecs152c/quit.php";

    private static final String TAG = "HostList****";

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	bluetoothBitch(); // keep BT discovery alive for 5 mins
	// getList() in onResume, not needed here
    }

    // disable back button
    @Override
    public void onBackPressed() {
	return;
    }

    // on resume, generate new list
    @Override
    protected void onResume() {
	super.onResume();
	getList(); // gen user list
    }

    // get list of users in host's game
    public void getList() {
	Log.d(TAG, "Generating User List");
	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	nameValuePairs
		.add(new BasicNameValuePair("key", SystemInfo.SERVER_KEY));
	nameValuePairs.add(new BasicNameValuePair("email", SystemInfo.email));
	nameValuePairs.add(new BasicNameValuePair("password",
		SystemInfo.password));

	// Post and parse result
	JSONObject result = data.post(nameValuePairs, URL);

	try {
	    JSONArray res = result.getJSONArray("response");
	    Integer usercount = res.getInt(0);
	    UserList.clear(); // clear list to rebuild
	    if (usercount == -1) {
		Log.d(TAG, "Error Getting User List");
		Toast.makeText(HostList.this, "Error Getting User List!",
			Toast.LENGTH_LONG).show();
	    } else if (usercount == 0) {
		Toast.makeText(HostList.this, "No Users In Game",
			Toast.LENGTH_LONG).show();
	    } else { // have users so build list
		JSONArray res2 = result.getJSONArray("name");
		for (int i = 0; i < usercount; i++) {
		    UserList.add(res2.getString(i)); // build list
		}
	    }
	} catch (JSONException ex) {
	}

	// display list
	setListAdapter(new ArrayAdapter<String>(this, R.layout.list, UserList));
	ListView lv = getListView();
	lv.setTextFilterEnabled(true);
    }// end getList

    // create menu button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	boolean result = super.onCreateOptionsMenu(menu);
	menu.add(0, MENU_REFRESH, 0, R.string.hostlist_refresh);
	menu.add(0, MENU_QUIT, 0, R.string.hostlist_quit);
	return result;
    }

    // menu button select
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case MENU_REFRESH:
	    bluetoothBitch(); // refresh bluetooth discoverability
	    getList(); // refresh user list
	    return true;
	case MENU_QUIT:
	    Log.d(TAG, "Ending Game...");
	    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	    nameValuePairs.add(new BasicNameValuePair("key",
		    SystemInfo.SERVER_KEY));
	    nameValuePairs
		    .add(new BasicNameValuePair("email", SystemInfo.email));
	    nameValuePairs.add(new BasicNameValuePair("password",
		    SystemInfo.password));

	    data.post(nameValuePairs, QUIT_URL);

	    Toast.makeText(HostList.this, "Game Ended", Toast.LENGTH_SHORT)
		    .show();
	    SystemInfo.inGame = false;
	    SystemInfo.isHost = false;
	    SystemInfo.gameTitle = null;
	    finish();
	    return true;
	}
	return super.onContextItemSelected(item);
    }

    // fire up Bluetooth Discoverability for 5 mins (max value)
    public void bluetoothBitch() {
	Intent discoverableIntent = new Intent(
		BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
	discoverableIntent.putExtra(
		BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
	startActivity(discoverableIntent);
    }
}
/* end: HostList.java */