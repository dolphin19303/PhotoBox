package dolphin.android.sdk.album.instagram;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.instagram.InstagramApp.OAuthAuthenticationListener;

public class InstagramLogin implements AppConstants {
	private Context mContext;
	private InstagramApp mApp;

	public InstagramLogin(Context context) {
		this.mContext = context;
		mApp = new InstagramApp(mContext, INSTAGRAM_CLIENT_ID,
				INSTAGRAM_CLIENT_SECRET, INSTAGRAM_CALLBACK_URL);
	}

	public InstagramLogin(Activity context) {
		this.mContext = context;
		mApp = new InstagramApp(mContext, INSTAGRAM_CLIENT_ID,
				INSTAGRAM_CLIENT_SECRET, INSTAGRAM_CALLBACK_URL);
	}

	public InstagramLogin(Fragment fragment) {
		this.mContext = fragment.getActivity();
		mApp = new InstagramApp(mContext, INSTAGRAM_CLIENT_ID,
				INSTAGRAM_CLIENT_SECRET, INSTAGRAM_CALLBACK_URL);
	}

	public InstagramApp getInstagram() {
		return mApp;
	}

	OAuthAuthenticationListener listener = new OAuthAuthenticationListener() {
		@Override
		public void onSuccess() {

		}

		@Override
		public void onFail(String error) {

		}
	};

	public void logIn(final InstagramLoginCallback mCallback) {
		if (mApp.hasAccessToken()) {
			mCallback.onLogin(INSTAGRAM_LOGIN_SUCCESS, mApp.getSession());
			mCallback.onAlreadyLogin(INSTAGRAM_LOGIN_RESTORETOKEN_SUCCESS,
					mApp.getSession());
		} else {
			mApp.setListener(new OAuthAuthenticationListener() {

				@Override
				public void onSuccess() {
					mCallback.onLogin(INSTAGRAM_LOGIN_SUCCESS,
							mApp.getSession());
				}

				@Override
				public void onFail(String error) {
					mCallback.onLogin(INSTAGRAM_LOGIN_FAILED, null);
				}
			});
			mApp.authorize();
		}
	}

	public void restoreToken(InstagramLoginCallback mCallback) {
		if (isLoggedIn()) {
			logIn(mCallback);
		} else {
			mCallback.onAlreadyLogin(INSTAGRAM_LOGIN_RESTORETOKEN_FAILED,
					mApp.getSession());
		}
	}

	public InstagramSession getSession() {
		if (mApp != null) {
			return mApp.getSession();
		} else
			return null;
	}

	public boolean isLoggedIn() {
		return mApp.hasAccessToken();
	}

	public interface InstagramLoginCallback {
		public void onLogin(int result, InstagramSession mSession);

		public void onAlreadyLogin(int result, InstagramSession mSession);
	}

	public void logOut() {
		mApp.resetAccessToken();
	}
}
