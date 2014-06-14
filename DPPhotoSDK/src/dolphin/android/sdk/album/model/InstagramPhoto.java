package dolphin.android.sdk.album.model;

import org.json.JSONObject;

import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.facebook.FacebookParser;
import dolphin.android.sdk.album.utils.L;

public class InstagramPhoto extends Photo implements AppConstants {
	private static final String TAG = "INSTAGRAM_PHOTO";
	public static final String INSTAGRAM_PHOTO_FULLSIZE = "standard_resolution";
	public static final String INSTAGRAM_PHOTO_THUMNAIL = "thumbnail";
	public static final String INSTAGRAM_PHOTO_HALFSIZE = "low_resolution";
	public static final String INSTAGRAM_PHOTO_URL = "url";
	public static final String INSTAGRAM_PHOTO_DATE = "created_time";
	public static final String INSTAGRAM_PHOTO_ID = "id";

	public InstagramPhoto() {
	}

	public InstagramPhoto(JSONObject imagesJson) {
		host = INSTAGRAM;
		try {
			link = FacebookParser.toHTML(imagesJson.getJSONObject(
					INSTAGRAM_PHOTO_FULLSIZE).getString(INSTAGRAM_PHOTO_URL));
			link_thumbnail = FacebookParser.toHTML(imagesJson.getJSONObject(
					INSTAGRAM_PHOTO_THUMNAIL).getString(INSTAGRAM_PHOTO_URL));
		} catch (Exception e) {
			link = "";
			link_thumbnail = "";
			L.e(TAG + " " + e.toString());
		}
	}
}
