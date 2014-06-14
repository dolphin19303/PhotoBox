package dolphin.android.sdk.album.facebook;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.model.FacebookPhoto;
import dolphin.android.sdk.album.utils.L;

public class FacebookPhotoManager implements AppConstants {
	private static final String TAG = "FACEBOOK_PHOTO_MANAGER";
	private String albumID;
	private Session mSession;
	private String mQueryParam = "/photos";

	public FacebookPhotoManager() {
	}

	public FacebookPhotoManager(Session session, String albumID) {
		this.albumID = albumID;
		mSession = session;
	}

	public void getListPhoto(final GetImageCallback getImageCallback) {
		if (mSession == null) {
			getImageCallback.onReceivedPhoto(FACEBOOK_GETPHOTO_NOT_AUTHORIZED,
					null);
			return;
		}
		if (getImageCallback != null && albumID != null && !albumID.equals("")) {
			new Request(mSession, "/" + albumID + mQueryParam, null,
					HttpMethod.GET, new Request.Callback() {
						public void onCompleted(Response response) {
							GraphObject mGraphObject = response
									.getGraphObject();

							try {
								ArrayList<FacebookPhoto> mFacebookPhotos = new ArrayList<FacebookPhoto>();
								JSONArray mArray = mGraphObject
										.getInnerJSONObject().getJSONArray(
												"data");
								for (int i = 0; i < mArray.length(); i++) {
									mFacebookPhotos.add(new FacebookPhoto(
											(JSONObject) mArray.get(i)));
								}
								getImageCallback.onReceivedPhoto(
										FACEBOOK_GETPHOTO_SUCCESS,
										mFacebookPhotos);
							} catch (Exception e) {
								getImageCallback.onReceivedPhoto(
										FACEBOOK_GETPHOTO_FAILED, null);
								L.e(TAG + " Query failed: " + e);
							}
						}
					}).executeAsync();
		} else {
			getImageCallback.onReceivedPhoto(FACEBOOK_GETPHOTO_FAILED, null);
			L.e(TAG + " Session error");
		}
	}

	public interface GetImageCallback {
		public void onReceivedPhoto(short status,
				ArrayList<FacebookPhoto> result);
	}
}
