package dolphin.android.sdk.album.facebook;

public class FacebookParser {
	public static String toHTML(String rawHTML) {
		if (rawHTML != null)
			return rawHTML.replaceAll("\\/", "/");
		else
			return "";
	}
}
