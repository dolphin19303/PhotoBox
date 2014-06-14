package dolphin.android.sdk.album.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.facebook.FacebookParser;
import dolphin.android.sdk.album.utils.L;

public class FacebookPhoto extends Photo implements AppConstants {
	private static final String TAG = "FACEBOOK_PHOTO";
	public static final String FACEBOOK_PHOTO_SOURCE = "source";
	public static final String FACEBOOK_PHOTOS_SIZE = "images";
	public static final String FACEBOOK_PHOTOS_DATE = "created_time";
	public static final String FACEBOOK_PHOTOS_ID = "id";

	private static String thunailSize = "130x130";

	public FacebookPhoto() {
	}

	private Date dateParse(String input) {
		SimpleDateFormat isoFormat = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss");
		isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		try {
			return isoFormat.parse(input);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public FacebookPhoto(JSONObject jsonObject) {
		host = FACEBOOK;
		try {
			date = dateParse(jsonObject.getString(FACEBOOK_PHOTOS_DATE)
					.substring(0, 19));

			id = jsonObject.getString(FACEBOOK_PHOTOS_ID);

			link = FacebookParser.toHTML(jsonObject.get(FACEBOOK_PHOTO_SOURCE)
					.toString());

			JSONArray mArray = jsonObject.getJSONArray(FACEBOOK_PHOTOS_SIZE);
			boolean isThumbnailExisted = false;
			for (int i = 0; i < mArray.length(); i++) {
				String thumnailUrl = ((JSONObject) mArray.get(i)).get(
						FACEBOOK_PHOTO_SOURCE).toString();
				if (thumnailUrl.contains(thunailSize)) {
					link_thumbnail = FacebookParser.toHTML(thumnailUrl);
					isThumbnailExisted = true;
				}
			}
			if (!isThumbnailExisted) {
				link_thumbnail = link;
				L.e(TAG + " " + "get thumbnail failed");
			}
		} catch (Exception e) {
			link = "";
			link_thumbnail = "";
			L.e(TAG + " " + e.toString());
		}
	}
}
