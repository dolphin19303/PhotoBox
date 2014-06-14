package com.dolphin.android.imgdownloader.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.dolphin.android.imgdownloader.R;

public class AccountLoginButton extends RelativeLayout {
	private boolean isOn;
	AccountButtonLoginListener mOnclickLisenter;
	private boolean canClick;

	public AccountLoginButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		isOn = false;
		canClick = true;
		setBackgroundResource(R.drawable.account_login);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (canClick) {
			canClick = false;
			isOn = !isOn;
			if (isOn) {
				setBackgroundResource(R.drawable.account_login_on);
			} else {
				setBackgroundResource(R.drawable.account_login);
			}
			notifyStatusChanged(isOn);

			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					canClick = true;
				}
			}, 3000);
		}
		canClick = false;
		return super.onTouchEvent(event);
	}

	private void notifyStatusChanged(boolean login) {
		if (mOnclickLisenter != null) {
			mOnclickLisenter.onClick(login, this);
		}
	}

	public void setOn() {
		isOn = true;
		notifyStatusChanged(isOn);
		setBackgroundResource(R.drawable.account_login_on);
	}

	public void setOff() {
		isOn = false;
		notifyStatusChanged(isOn);
		setBackgroundResource(R.drawable.account_login);
	}

	public interface AccountButtonLoginListener {
		public void onClick(boolean status, View v);
	}

	public void setOnClickListener(AccountButtonLoginListener listener) {

	}
}
