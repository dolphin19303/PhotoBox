package dolphin.android.sdk.album.model;

import java.util.Date;

public class Photo {
	public static final int STATUS_DOWNLOADED = 11;
	public static final int STATUS_DOWNLOADING = 12;
	public static final int STATUS_NOT_DOWNLOAD = 13;
	protected String id;
	protected String link;
	protected String link_thumbnail;
	protected Date date;
	protected int host;
	protected int downloadStatus;

	public Photo() {
		this.downloadStatus = STATUS_NOT_DOWNLOAD;
	}

	public Photo(String id, String link) {
		this.id = id;
		this.link = link;
		this.downloadStatus = STATUS_NOT_DOWNLOAD;
	}

	public String getLink() {
		return link;
	}

	public int getDownloadStatus() {
		return downloadStatus;
	}

	public String getThumbnail() {
		return link_thumbnail;
	}

	public String getId() {
		return id;
	}

	public int getHost() {
		return host;
	}

	public Date getDate() {
		return date;
	}

	public Photo setDownloadStatus(int status) {
		downloadStatus = status;
		return this;
	}

	public Photo setLink(String link) {
		this.link = link;
		return this;
	}

	public Photo setThumbnail(String thumb) {
		this.link_thumbnail = thumb;
		return this;
	}

	public Photo setId(String id) {
		this.id = id;
		return this;
	}

	public Photo setHost(int host) {
		this.host = host;
		return this;
	}

	public Photo setDate(Date date) {
		this.date = date;
		return this;
	}

	@Override
	public boolean equals(Object object) {

		if (object != null && object instanceof Photo) {
			Photo thing = (Photo) object;
			if (link == null) {
				return (thing.link == null);
			} else {
				return link.equals(thing.link);
			}
		}
		return false;
	}

	public boolean isDownloaded() {
		return downloadStatus == STATUS_DOWNLOADED;
	}
}
