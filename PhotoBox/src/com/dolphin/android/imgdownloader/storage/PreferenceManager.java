package com.dolphin.android.imgdownloader.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.dolphin.android.imgdownloader.FxConstants;

public class PreferenceManager implements FxConstants {
	private static final String TAG = "PreferenceManager";
	private Context mContext;
	private String MyPREFERENCES = "dolphin.pre";
	private Editor editor;
	private SharedPreferences sharedpreferences;

	// user define
	public static final String SHOW_CONFIRM_TWITTER = "is.confirm.twitter";
	public static final String SHOW_ACCOUNT_FRAGMENT = "is.show.account.fragment";
	public static final String DOWNLOAD_PATH = "download.path";

	public PreferenceManager(Context context) {
		this.mContext = context;
	}

	private SharedPreferences getPreference() {
		if (sharedpreferences == null) {
			sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES,
					Context.MODE_PRIVATE);
		}
		return sharedpreferences;
	}

	private Editor getEditor() {
		if (editor == null) {
			editor = getPreference().edit();
		}
		return editor;
	}

	public void putString(String key, String value) {
		getEditor().putString(key, value);
		getEditor().commit();
	}

	public void putBoolean(String key, Boolean value) {
		getEditor().putBoolean(key, value);
		getEditor().commit();
	}

	public void putInt(String key, int value) {
		getEditor().putInt(key, value);
		getEditor().commit();
	}

	public int getInt(String key) {
		if (getPreference() != null) {
			return getPreference().getInt(key, 0);
		}
		return 0;
	}

	public boolean getBoolean(String key) {
		if (getPreference() != null) {
			return getPreference().getBoolean(key, false);
		}
		return false;
	}

	public String getString(String key) {
		if (key.equals(DOWNLOAD_PATH)) {
			return getPreference().getString(key, DEFAULT_DOWNLOAD_PATH);

		}
		if (getPreference() != null) {
			return getPreference().getString(key, "");
		}
		return "";
	}
}
