package dolphin.android.sdk.album.facebook;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.utils.L;

public class FacebookAlbumManager implements AppConstants {
	private static final String TAG = "FACEBOOK_ALBUM_MANAGER";
	GetAlbumCallback mAlbumCallback;
	private Session mSession;
	private FacebookLogin mFacebookLogin;
	private static final String mQueryParam = "/me/albums";

	public FacebookAlbumManager(FacebookLogin mFacebookLogin) {
		if (mFacebookLogin != null && mFacebookLogin.getSession() != null) {
			mSession = mFacebookLogin.getSession();
			this.mFacebookLogin = mFacebookLogin;
		}
	}

	private Session getSession() {
		if (mSession != null) {
			if (mFacebookLogin != null && !mSession.equals(mFacebookLogin.getSession())) {
				mSession = mFacebookLogin.getSession();
			}
		} else {
			if (mFacebookLogin != null) {
				mSession = mFacebookLogin.getSession();
			}
		}
		return mSession;
	}

	public void getListAlbum(final GetAlbumCallback getAlbumCallback) {
		if (mFacebookLogin.isLoggedIn()) {
			new Request(getSession(), mQueryParam, null, HttpMethod.GET,
					new Request.Callback() {
						public void onCompleted(Response response) {

							GraphObject mGraphObject = response
									.getGraphObject();
							JSONArray mArrray = null;
							try {
								mArrray = mGraphObject.getInnerJSONObject()
										.getJSONArray("data");
							} catch (JSONException e) {
								getAlbumCallback.onReceivedAlbum(
										FACEBOOK_GETALBUM_FAILED, null);
								L.e(TAG + " Query album failed: " + e);
							} catch (NullPointerException e) {
								L.e(TAG + " Null pointer " + e);
							}
							if (mArrray != null && mArrray.length() > 0) {
								ArrayList<FacebookAlbum> mAlbums = new ArrayList<FacebookAlbum>();
								for (int i = 0; i < mArrray.length(); i++) {
									try {
										mAlbums.add(new FacebookAlbum(
												(JSONObject) mArrray.get(i)));
									} catch (JSONException e) {
										L.e(TAG + " Exception " + e);
									}
								}
								getAlbumCallback.onReceivedAlbum(
										mAlbums.size() > 0 ? FACEBOOK_GETALBUM_SUCCESS
												: FACEBOOK_GETALBUM_FAILED,
										mAlbums);
							} else {
								getAlbumCallback.onReceivedAlbum(
										FACEBOOK_GETALBUM_FAILED, null);
								L.e(TAG + " got 0 album");
							}
						}
					}).executeAsync();
		} else {
			getAlbumCallback.onReceivedAlbum(FACEBOOK_GETALBUM_NOT_AUTHORIZED,
					null);
			L.e(TAG + " Session error");
		}
	}

	public interface GetAlbumCallback {
		public void onReceivedAlbum(short status,
				ArrayList<FacebookAlbum> result);
	}
}
