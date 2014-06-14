package com.dolphin.android.imgdownloader.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dolphin.android.imgdownloader.FxBaseFragment;
import com.dolphin.android.imgdownloader.R;
import com.dolphin.android.imgdownloader.activity.StartActivity;
import com.dolphin.android.imgdownloader.utils.GeneralLayout;

/**
 * Created by Administrator on 2/18/14.
 */
public class AboutUsFragment extends FxBaseFragment {
	private String TAG = "AboutUsFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.TAG = TAG;
		View view = inflater.inflate(
				GeneralLayout.getLayout(R.array.layout_fragment_about_us),
				container, false);
		((StartActivity) getActivity()).getSupportActionBar().setTitle(
				getString(R.string.about_us));
		return view;
	}
}
