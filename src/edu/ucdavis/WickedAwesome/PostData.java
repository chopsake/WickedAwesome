/* begin: PostData.java */
package edu.ucdavis.WickedAwesome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

public class PostData {
    private static final int HTTP_STATUS_OK = 200;

    public JSONObject post(List<NameValuePair> values, String postURL) {
	// Create a new HttpClient and Post Header
	HttpClient httpclient = new DefaultHttpClient();
	HttpPost httppost = new HttpPost(postURL);

	try {
	    httppost.setEntity(new UrlEncodedFormEntity(values));

	    // Execute HTTP Post Request
	    HttpResponse response = httpclient.execute(httppost);

	    // Check for a good connect and whether is has been established
	    if (response.getStatusLine().getStatusCode() == HTTP_STATUS_OK) {
		HttpEntity entity = response.getEntity();

		if (entity != null) {
		    InputStream instream = entity.getContent();

		    // Load the page converted to a string into a JSONObject
		    JSONObject myAwway = new JSONObject(
			    convertStreamToString(instream));

		    instream.close();

		    return myAwway;

		} // end if
	    } // end if
	} catch (ClientProtocolException e) {
	} catch (IOException e) {
	} catch (JSONException ex) {
	}

	JSONObject ret = new JSONObject();
	return ret;

    }

    private static String convertStreamToString(InputStream is) {
	/*
	 * To convert the InputStream to String we use the
	 * BufferedReader.readLine() method. We iterate until the BufferedReader
	 * return null which means there's no more data to read. Each line will
	 * appended to a StringBuilder and returned as String.
	 */
	BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	StringBuilder sb = new StringBuilder();

	String line = null;
	try {
	    while ((line = reader.readLine()) != null) {
		sb.append(line + "\n");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		is.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
	return sb.toString();
    }
}
/* end: PostData.java */