package com.dolphin.android.imgdownloader.connectmanager;

import java.net.InetAddress;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class ConnectManager {
	Application mApplication;
	public static final String CONNECTIVITY_SERVICE = "connectivity";

	public ConnectManager(Application mApplication) {
		this.mApplication = mApplication;

	}

	public void hasInternet(final NetworkCallback mNetworkCallback) {
		(new AsyncTask<Boolean, Integer, Boolean>() {
			@Override
			protected Boolean doInBackground(Boolean... params) {
				boolean isConnectWifi = isNetWorkOnline();
				boolean isConnectInternet = true;
				try {
					InetAddress.getByName("www.google.com").isReachable(5000);
				} catch (Exception e) {
					isConnectInternet = false;
				}
				return isConnectInternet && isConnectWifi;
			}

			@Override
			protected void onPostExecute(Boolean aBoolean) {
				mNetworkCallback.onConnect(aBoolean);
				super.onPostExecute(aBoolean);
			}

		}).execute();
	}

	public interface NetworkCallback {
		public void onConnect(boolean status);
	}

	// Check NetWork
	public boolean isNetWorkOnline() {
		try {
			NetworkInfo activeNetwork = connMan().getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null ? activeNetwork
					.isConnectedOrConnecting() : false;
			return isConnected;
		} catch (Exception e) {
			return false;
		}
	}

	private ConnectivityManager connMan() {
		return (ConnectivityManager) mApplication
				.getSystemService(CONNECTIVITY_SERVICE);
	}

}
