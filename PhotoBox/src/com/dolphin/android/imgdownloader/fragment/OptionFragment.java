package com.dolphin.android.imgdownloader.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.dolphin.android.imgdownloader.FxApplication;
import com.dolphin.android.imgdownloader.FxBaseFragment;
import com.dolphin.android.imgdownloader.R;
import com.dolphin.android.imgdownloader.activity.StartActivity;
import com.dolphin.android.imgdownloader.file.DirectoryChooserFragment;
import com.dolphin.android.imgdownloader.storage.PreferenceManager;
import com.dolphin.android.imgdownloader.utils.GeneralLayout;

/**
 * Created by Administrator on 2/18/14.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class OptionFragment extends FxBaseFragment {
	private String TAG = "AboutUsFragment";
	private DirectoryChooserFragment mDialog;

	private EditText etPath;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.TAG = TAG;
		View view = inflater.inflate(
				GeneralLayout.getLayout(R.array.layout_fragment_option),
				container, false);
		((StartActivity) getActivity()).getSupportActionBar().setTitle(
				getString(R.string.option));

		ImageView ivChoosePath = (ImageView) view
				.findViewById(R.id.ivChoosePath);
		etPath = (EditText) view.findViewById(R.id.etPath);
		etPath.setEnabled(false);
		view.findViewById(R.id.chooseLanguage).setVisibility(View.GONE);
		ivChoosePath.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startPicker();
			}
		});

		updateFolderPath();
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		 mDialog = DirectoryChooserFragment.newInstance("Dolphin", null);
//		mDialog = new DirectoryChooserFragment("OptionFragment", null);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void startPicker() {
		mDialog.show(getActivity().getSupportFragmentManager(), null);
	}

	private void updateFolderPath() {
		if (FxApplication.mPreferenceManager != null
				&& FxApplication.mPreferenceManager.getString(
						PreferenceManager.DOWNLOAD_PATH).length() > 0) {
			etPath.setText(FxApplication.mPreferenceManager
					.getString(PreferenceManager.DOWNLOAD_PATH));
		}
	}

	public void dismissFolderPicker() {
		if (mDialog != null) {
			mDialog.dismiss();
			updateFolderPath();
		}
	}
}
