package dolphin.android.sdk.album.model;

import dolphin.android.sdk.album.AppConstants;

public class TwitterPhoto extends Photo implements AppConstants {
	private String SIZE_PREFIX_MEDIUM = "medium";
	private String SIZE_PREFIX_THUMB = "thumb";
	private String SIZE_PREFIX_SMALL = "small";
	private String SIZE_PREFIX_LARGE = "large";

	/*
	 * "medium": { "w": 600, "h": 399, "resize": "fit" }, "thumb": { "w": 150,
	 * "h": 150, "resize": "crop" }, "small": { "w": 340, "h": 226, "resize":
	 * "fit" }, " large": { "w": 800, "h": 532, "resize": "fit"
	 */
	public TwitterPhoto() {
		super();
	}

	public TwitterPhoto(String id, String link) {
		super(id, link);
		this.link = link + ":" + SIZE_PREFIX_LARGE;
		this.link_thumbnail = link + ":" + SIZE_PREFIX_THUMB;
		host = TWITTER;
	}
}
