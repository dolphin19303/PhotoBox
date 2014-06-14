package dolphin.android.sdk.album.facebook;

import java.util.ArrayList;

import org.json.JSONObject;

import com.facebook.Session;

import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.facebook.FacebookPhotoManager.GetImageCallback;
import dolphin.android.sdk.album.model.Album;
import dolphin.android.sdk.album.model.FacebookPhoto;

public class FacebookAlbum extends Album implements AppConstants {
	private static final String TAG = "FACEBOOK_ALBUM";
	public static final String FACEBOOK_ALBUM_ID = "id";
	private ArrayList<FacebookPhoto> mFacebookPhotos;

	public FacebookAlbum(JSONObject json) {
		try {
			id = json.get(FACEBOOK_ALBUM_ID).toString();
		} catch (Exception e) {
			id = "";
		}
		if (mFacebookPhotos == null) {
			mFacebookPhotos = new ArrayList<FacebookPhoto>();
		}
	}

	public FacebookAlbum(String id) {
		super(id);
		if (mFacebookPhotos == null) {
			mFacebookPhotos = new ArrayList<FacebookPhoto>();
		}
	}

	public void getListPhoto(Session session,
			final GetImageCallback getImageCallback) {
		if (getImageCallback != null) {
			FacebookPhotoManager mFacebookPhotoManager = new FacebookPhotoManager(
					session, id);
			mFacebookPhotoManager.getListPhoto(getImageCallback);
		}
	}
}
