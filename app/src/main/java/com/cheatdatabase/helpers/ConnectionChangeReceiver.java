package com.cheatdatabase.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
/**
 * 12.04.2014: NOT USED. USING REACHABOLITY.JAVA INSTEAD TO CHECK INTERNET.
 * IF USING THIS CLASS, ADD THIS TO MANIFEST:
 *   <receiver
            android:name="com.cheatdatabase.helpers.ConnectionChangeReceiver"
            android:label="NetworkConnection" >
            <intent-filter>
                <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
            </intent-filter>
        </receiver>
 * @author Dominik
 *
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (activeNetInfo != null) {
			// Toast.makeText(context, "Active Network Type : " +
			// activeNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();

			//Konstanten.IS_ONLINE = activeNetInfo.isConnected();

			Toast.makeText(context, "IS CONNECTED? " + activeNetInfo.isConnected(), Toast.LENGTH_SHORT).show();

		} else {
			//Konstanten.IS_ONLINE = false;
		}
		if (mobNetInfo != null) {
			// Toast.makeText(context, "Mobile Network Type : " +
			// mobNetInfo.getTypeName(), Toast.LENGTH_SHORT).show();
		}
	}
}