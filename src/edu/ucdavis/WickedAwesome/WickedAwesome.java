/* begin: WickedAwesome.java */
package edu.ucdavis.WickedAwesome;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

// main menu - build tab interface
public class WickedAwesome extends TabActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);

	Resources res = getResources();
	TabHost tabHost = getTabHost();

	Intent intent1 = new Intent(this, Join.class);
	Intent intent2 = new Intent(this, Host.class);
	Intent intent3 = new Intent(this, Options.class);

	tabHost.addTab(tabHost.newTabSpec("tab1")
		.setIndicator("Join", res.getDrawable(R.drawable.join))
		.setContent(intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
	tabHost.addTab(tabHost.newTabSpec("tab2")
		.setIndicator("Host", res.getDrawable(R.drawable.host))
		.setContent(intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
	tabHost.addTab(tabHost.newTabSpec("tab3")
		.setIndicator("Options", res.getDrawable(R.drawable.options))
		.setContent(intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
    }
}
/* end: WickedAwesome.java */