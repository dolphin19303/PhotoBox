package com.dolphin.android.imgdownloader.fragment;

import oauth.signpost.basic.DefaultOAuthProvider;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dolphin.android.imgdownloader.FxApplication;
import com.dolphin.android.imgdownloader.FxBaseFragment;
import com.dolphin.android.imgdownloader.FxConstants;
import com.dolphin.android.imgdownloader.R;
import com.dolphin.android.imgdownloader.activity.StartActivity;
import com.dolphin.android.imgdownloader.connectmanager.ConnectManager.NetworkCallback;
import com.dolphin.android.imgdownloader.storage.PreferenceManager;
import com.dolphin.android.imgdownloader.ui.AccountLoginButton;
import com.dolphin.android.imgdownloader.ui.AccountLoginButton.AccountButtonLoginListener;
import com.dolphin.android.imgdownloader.utils.GeneralLayout;
import com.dolphin.android.imgdownloader.utils.L;
import com.dolphin.android.imgdownloader.utils.T;
import com.facebook.Session;
import com.facebook.SessionState;

import dolphin.android.sdk.album.facebook.FacebookLogin;
import dolphin.android.sdk.album.facebook.FacebookLogin.FacebookLoginCallback;
import dolphin.android.sdk.album.instagram.InstagramLogin;
import dolphin.android.sdk.album.instagram.InstagramLogin.InstagramLoginCallback;
import dolphin.android.sdk.album.instagram.InstagramSession;
import dolphin.android.sdk.album.twitter.TwitterLogin;
import dolphin.android.sdk.album.twitter.TwitterLogin.TwitterLoginCallback;

/**
 * Created by Administrator on 2/18/14.
 */

public class AccountFragment extends FxBaseFragment implements FxConstants {
	private String TAG = "AboutUsFragment";
	protected AccountLoginButton btnFacebookLogin, btnTwitterLogin,
			btnInstagramLogin;

	// Facebook declare
	private FacebookLogin mFacebookLogin;
	private ImageView mFacebookLoginIcon;
	SessionStatusCallback mSessionStatusCallback = new SessionStatusCallback();

	// Twitter declare
	private TwitterLogin mTwitterLogin;
	private ImageView mTwitterLoginIcon;

	// Instagram declare
	private InstagramLogin mInstagramLogin;
	private ImageView mInstagramLoginIcon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initial facebookLogin
		mFacebookLogin = FxApplication.mFacebookLogin;

		mFacebookLogin = new FacebookLogin(this);

		// Initial TwitterLogin
		mTwitterLogin = new TwitterLogin(getActivity());
		new DefaultOAuthProvider("https://api.twitter.com/oauth/request_token",
				"https://api.twitter.com/oauth/access_token",
				"https://api.twitter.com/oauth/authorize");

		// Initial InstagramLogin
		mInstagramLogin = new InstagramLogin(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.TAG = TAG;
		View view = inflater.inflate(
				GeneralLayout.getLayout(R.array.layout_fragment_account),
				container, false);
		((StartActivity) getActivity()).getSupportActionBar().setTitle(
				getString(R.string.account));
		btnFacebookLogin = (AccountLoginButton) view
				.findViewById(R.id.btFacebookConnect);
		btnTwitterLogin = (AccountLoginButton) view
				.findViewById(R.id.btTwitterConnect);
		btnInstagramLogin = (AccountLoginButton) view
				.findViewById(R.id.btInstagramConnect);

		btnFacebookLogin.setOnClickListener(mLoginButtonListener);
		btnTwitterLogin.setOnClickListener(mOnClickAction);
		btnInstagramLogin.setOnClickListener(mOnClickAction);

		mFacebookLoginIcon = (ImageView) view.findViewById(R.id.ivFacebookIcon);
		mTwitterLoginIcon = (ImageView) view.findViewById(R.id.ivTwitterIcon);
		mInstagramLoginIcon = (ImageView) view
				.findViewById(R.id.ivInstagramIcon);

		updateView();
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mFacebookLogin.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStart() {
		super.onStart();
		mFacebookLogin.getSession().addCallback(mSessionStatusCallback);
	}

	@Override
	public void onStop() {
		super.onStop();
		mFacebookLogin.getSession().removeCallback(mSessionStatusCallback);
	}

	@SuppressWarnings("static-access")
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mFacebookLogin.getSession().saveSession(mFacebookLogin.getSession(),
				outState);
	}

	public void updateData() {
		try {
			((StartActivity) getActivity()).updateDataFromSNS();
		} catch (NullPointerException e) {
			L.e("Null when login complete and start updateData from server");
		}
	}

	private void updateView() {
		if (mFacebookLogin.isLoggedIn()) {
			mFacebookLoginIcon.setImageResource(R.drawable.account_facebook);
			// btnFacebookLogin.setText(R.string.account_connected);
			btnFacebookLogin.setOn();
			btnFacebookLogin.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogoutFacebook();
				}
			});
		} else {
			mFacebookLoginIcon
					.setImageResource(R.drawable.account_facebook_inactive);
			// btnFacebookLogin.setText(R.string.account_connect);
			btnFacebookLogin.setOff();
			btnFacebookLogin.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLoginFacebook();
				}
			});
		}

		if (mTwitterLogin != null && mTwitterLogin.isLoggedIn()) {
			mTwitterLoginIcon.setImageResource(R.drawable.account_twitter);
			btnTwitterLogin.setOn();
			btnTwitterLogin.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogoutTwitter();
				}
			});
		} else {
			mTwitterLoginIcon
					.setImageResource(R.drawable.account_twitter_inacive);
			btnTwitterLogin.setOff();
			btnTwitterLogin.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLoginTwitter();
				}
			});
		}

		if (mInstagramLogin != null && mInstagramLogin.isLoggedIn()) {
			mInstagramLoginIcon.setImageResource(R.drawable.account_instagram);
			btnInstagramLogin.setOn();
			btnInstagramLogin.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLogoutInstagram();
				}
			});
		} else {
			mInstagramLoginIcon
					.setImageResource(R.drawable.account_instagram_inactive);
			btnInstagramLogin.setOff();
			btnInstagramLogin.setOnClickListener(new OnClickListener() {
				public void onClick(View view) {
					onClickLoginInstagram();
				}
			});
		}

	}

	private void onClickLoginFacebook() {
		if (FxApplication.mConnectManager != null) {
			FxApplication.mConnectManager.hasInternet(new NetworkCallback() {

				@Override
				public void onConnect(boolean status) {
					if (status) {
						if (mFacebookLogin != null) {
							mFacebookLogin.logIn(mFacebookLoginCallback);
						}
					} else {
						updateView();
						T.show("No internet. Try again later.");
					}
				}
			});
		}
	}

	private void onClickLogoutFacebook() {
		if (mFacebookLogin != null) {
			mFacebookLogin.logOut();
			FxApplication.mSqliteManager.deleteByHost(FACEBOOK);
			updateView();
		}
	}

	private void onClickLoginTwitter() {
		if (FxApplication.mConnectManager != null) {
			FxApplication.mConnectManager.hasInternet(new NetworkCallback() {

				@Override
				public void onConnect(boolean status) {
					if (status) {
						if (mTwitterLogin != null) {
							FxApplication.mPreferenceManager.putBoolean(
									PreferenceManager.SHOW_CONFIRM_TWITTER,
									true);
							mTwitterLogin.askOAuth();
							killMe();
						}
					} else {
						updateView();
						T.show("No internet. Try again later.");
					}
				}
			});
		}
	}

	private void onClickLogoutTwitter() {
		if (mTwitterLogin != null) {
			mTwitterLogin.logOut();
			FxApplication.mSqliteManager.deleteByHost(TWITTER);
			updateView();
		}
	}

	private void onClickLoginInstagram() {
		if (FxApplication.mConnectManager != null) {
			FxApplication.mConnectManager.hasInternet(new NetworkCallback() {

				@Override
				public void onConnect(boolean status) {
					if (status) {
						if (mInstagramLogin != null) {
							mInstagramLogin.logIn(mInstagramLoginCallback);
							updateView();
						}
					} else {
						updateView();
						T.show("No internet. Try again later.");
					}
				}
			});
		}
	}

	private void onClickLogoutInstagram() {
		if (mInstagramLogin != null) {
			mInstagramLogin.logOut();
			FxApplication.mSqliteManager.deleteByHost(INSTAGRAM);
			updateView();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// if (!mTwitterLogin.isLoggedIn()) {
		// mTwitterLogin.logIn(mTwitterLoginCallback);
		// }

		updateView();
	}

	public void killMe() {
		((StartActivity) getActivity()).killAccountFragment();
		((StartActivity) getActivity()).killImageGridFragment();
		((StartActivity) getActivity()).finishActivity();
	}

	AccountButtonLoginListener mLoginButtonListener = new AccountButtonLoginListener() {

		@Override
		public void onClick(boolean status, View v) {
			switch (v.getId()) {
			case R.id.btFacebookConnect:
				if (mFacebookLogin.isLoggedIn()) {
					onClickLogoutFacebook();
				} else {
					mFacebookLogin.logIn(mFacebookLoginCallback);
				}
				break;

			case R.id.btTwitterConnect:
				if (mTwitterLogin.isLoggedIn()) {
					mTwitterLogin.logOut();
				} else {
					onClickLoginTwitter();
				}
				updateView();
				break;

			case R.id.btInstagramConnect:
				if (mInstagramLogin.isLoggedIn()) {
					mInstagramLogin.logOut();
				} else {
					mInstagramLogin.logIn(mInstagramLoginCallback);
				}
				break;

			default:
				break;
			}
		}
	};
	OnClickListener mOnClickAction = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btFacebookConnect:
				if (mFacebookLogin.isLoggedIn()) {
					onClickLogoutFacebook();
				} else {
					mFacebookLogin.logIn(mFacebookLoginCallback);
				}
				break;

			case R.id.btTwitterConnect:
				if (mTwitterLogin.isLoggedIn()) {
					mTwitterLogin.logOut();
				} else {
					onClickLoginTwitter();
				}
				updateView();
				break;

			case R.id.btInstagramConnect:
				if (mInstagramLogin.isLoggedIn()) {
					mInstagramLogin.logOut();
				} else {
					mInstagramLogin.logIn(mInstagramLoginCallback);
				}
				break;

			default:
				break;
			}
		}
	};

	TwitterLoginCallback mTwitterLoginCallback = new TwitterLoginCallback() {

		@Override
		public void onLogin(int result, String mess) {
			if (result == TWITTER_LOGIN_SUCCESS) {
				updateView();
				updateData();
			}
		}
	};
	InstagramLoginCallback mInstagramLoginCallback = new InstagramLoginCallback() {

		@Override
		public void onLogin(int result, InstagramSession mSession) {
			if (result == INSTAGRAM_LOGIN_SUCCESS) {
				updateView();
				updateData();
			}
		}

		@Override
		public void onAlreadyLogin(int result, InstagramSession mSession) {

		}
	};

	FacebookLoginCallback mFacebookLoginCallback = new FacebookLoginCallback() {

		@Override
		public void onLogin(int result, Session mSession) {
			if (result == FACEBOOK_LOGIN_SUCCESS) {
				mFacebookLogin.requestPermission();
				updateView();
				updateData();
			}
		}

		@Override
		public void onAlreadyLogin(int result, Session mSession) {

		}
	};

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			updateView();
		}
	}
}
