package com.dolphin.android.imgdownloader.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import com.dolphin.android.imgdownloader.R;
import com.dolphin.android.imgdownloader.utils.GeneralLayout;

public class BottomActionBar extends LinearLayout {
	// bottom menu
	private TranslateAnimation BottomAnimation;
	private boolean isFirstTimeActionBar;
	private boolean isActionBarEnabled;

	public BottomActionBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(GeneralLayout.getLayout(R.array.bottom_action_bar),
				this, true);
		setVisibility(View.GONE);
		isActionBarEnabled = false;
		setOnClickListener(null);
	}

	public boolean isShowed() {
		return isActionBarEnabled;
	}

	// show action bar
	/**
	 * Method used to show action bar.
	 */
	public void show() {
		isActionBarEnabled = true;
		setVisibility(View.VISIBLE);
		int height = getHeight();
		if (height <= 0)
			height = 100;
		BottomAnimation = new TranslateAnimation(0, 0, height, 0);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			BottomAnimation.setStartOffset(20);
		else {
			BottomAnimation.setStartOffset(150);
			if (isFirstTimeActionBar) {
				BottomAnimation.setStartOffset(3000);
				BottomAnimation.setStartTime(3000);
				BottomAnimation.setFillBefore(true);
				isFirstTimeActionBar = false;
			}
		}
		BottomAnimation.setDuration(100);
		setAnimation(BottomAnimation);
	}

	/**
	 * Method used to hide action bar.
	 */
	public void hide() {
		isActionBarEnabled = false;
		setVisibility(View.GONE);
		int height = getHeight();
		if (height <= 0)
			height = 100;
		BottomAnimation = new TranslateAnimation(0, 0, 0, height);
		BottomAnimation.setDuration(200);
		setAnimation(BottomAnimation);
	}
}
