package com.dolphin.android.imgdownloader;

import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2/13/14.
 */
public class FxBaseFragment extends Fragment {
	protected String TAG;

	public String getFxTag() {
		return TAG;
	}

	public void setFxTag(String tag) {
		TAG = tag;
	}
}
