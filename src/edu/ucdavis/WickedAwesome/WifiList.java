/* begin: WifiList.java */
package edu.ucdavis.WickedAwesome;

// Class for list of Wifi APs
// MACs and Signal strength treated as Strings
// Note: Merged Wifi and WifiList classes as they were essentially the same
public class WifiList {
    protected String mac;
    protected int signal;

    protected WifiList(String mac, int signal) {
	this.mac = mac;
	this.signal = signal;
    }

    public String getMac() {
	return mac;
    }

    public void setMac(String mac) {
	this.mac = mac;
    }

    public int getSignal() {
	return signal;
    }

    public void setSignal(int signal) {
	this.signal = signal;
    }

    public String toString() {
	return mac;
    }

    public boolean equals(Object o) {
	return o instanceof WifiList && ((WifiList) o).mac.compareTo(mac) == 0;
    }
}
/* end: WifiList.java */