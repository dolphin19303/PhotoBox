package dolphin.android.sdk.album.facebook;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.Settings;

import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.utils.L;

public class FacebookLogin implements AppConstants {
	private Activity mActivity;
	private Fragment mFragment;
	Session session;

	public FacebookLogin(Activity mActivity) {
		this.mActivity = mActivity;
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
	}

	public FacebookLogin(Fragment mFragment) {
		this.mFragment = mFragment;
		if (mFragment != null) {
			this.mActivity = mFragment.getActivity();
		}
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
		Session.setActiveSession(getSession());
	}

	public void logIn(final FacebookLoginCallback mFacebookLoginCallback) {
		if (mFacebookLoginCallback != null && getSession() != null) {
			if (!getSession().isOpened()
					&& !getSession().isClosed()
					&& !getSession().getState().equals(
							SessionState.CREATED_TOKEN_LOADED)) {
				if (mFragment != null) {
					getSession().openForRead(
							new Session.OpenRequest(mFragment)
									.setCallback(new StatusCallback() {

										@Override
										public void call(Session ses,
												SessionState state,
												Exception exception) {
											if (ses != null) {
												session = ses;
												Session.setActiveSession(getSession());
												mFacebookLoginCallback.onLogin(
														FACEBOOK_LOGIN_SUCCESS,
														session);
											} else {
												mFacebookLoginCallback.onLogin(
														FACEBOOK_LOGIN_FAILED,
														session);
											}
										}
									}));
				} else {
					getSession().openForRead(
							new Session.OpenRequest(mActivity)
									.setCallback(new StatusCallback() {

										@Override
										public void call(Session ses,
												SessionState state,
												Exception exception) {
											if (ses != null) {
												session = ses;
												Session.setActiveSession(getSession());
												mFacebookLoginCallback.onLogin(
														FACEBOOK_LOGIN_SUCCESS,
														session);
											} else {
												mFacebookLoginCallback.onLogin(
														FACEBOOK_LOGIN_FAILED,
														session);
											}
										}
									}));
				}
			} else {
				Session.openActiveSession(mActivity, true,
						new StatusCallback() {

							@Override
							public void call(Session ses, SessionState state,
									Exception exception) {
								if (ses != null) {
									session = ses;
									Session.setActiveSession(getSession());
									mFacebookLoginCallback.onLogin(
											FACEBOOK_LOGIN_SUCCESS, session);
									mFacebookLoginCallback
											.onAlreadyLogin(
													FACEBOOK_LOGIN_RESTORETOKEN_SUCCESS,
													getSession());
								} else {
									mFacebookLoginCallback.onLogin(
											FACEBOOK_LOGIN_FAILED, session);
									mFacebookLoginCallback.onAlreadyLogin(
											FACEBOOK_LOGIN_RESTORETOKEN_FAILED,
											getSession());
								}
							}
						});
			}
		} else {
			L.e("Callback or session null");
		}
	}

	public void restoreToken(FacebookLoginCallback mCallback) {
		if (isLoggedIn() && !canSendRequest()) {
			logIn(mCallback);
		} else {
			mCallback.onAlreadyLogin(FACEBOOK_LOGIN_RESTORETOKEN_FAILED,
					getSession());
		}
	}

	public void requestPermission() {
		if (isLoggedIn()) {
			List<String> mPermis = getSession().getPermissions();
			boolean isPermissionExisted = false;
			if (mPermis != null && mPermis.size() > 0) {
				for (String mString : mPermis) {
					if (mString.equals(FACEBOOK_LOGIN_PERMISSION_PHOTO)) {
						isPermissionExisted = true;
					}
				}
			}
			if (!isPermissionExisted) {
				List<String> permissions = new ArrayList<String>();
				permissions.add(FACEBOOK_LOGIN_PERMISSION_PHOTO);
				session.requestNewReadPermissions(new Session.NewPermissionsRequest(
						mActivity, permissions));
			}
		}
	}

	public Session getSession() {
		session = Session.getActiveSession();
		if (session == null && mActivity != null) {
			session = new Session(mActivity);
		}
		return session;
	}

	public boolean isLoggedIn() {
		try {
			return getSession().isOpened()
					|| getSession().getState().equals(
							SessionState.CREATED_TOKEN_LOADED);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean canSendRequest() {
		return getSession().isOpened();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mActivity != null) {
			Session.getActiveSession().onActivityResult(mActivity, requestCode,
					resultCode, data);
		}
	}

	public void logOut() {
		if (!getSession().isClosed()) {
			getSession().closeAndClearTokenInformation();
			getSession().setActiveSession(null);
			session = null;
		}
	}

	public interface FacebookLoginCallback {
		public void onLogin(int result, Session mSession);

		public void onAlreadyLogin(int result, Session mSession);
	}
}
