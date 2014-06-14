package com.dolphin.android.imgdownloader;

import dolphin.android.sdk.album.AppConstants;

/**
 * Created by Administrator on 2/13/14.
 */
public interface FxConstants extends AppConstants {
	public static final boolean releaseMode = false;
	public static final int DOWNLOAD_COMPLETE = 0;
	public static final int DOWNLOAD_FAILED = 1;
	public static final int DOWNLOAD_COMPLETE_ALL = 2;
	public static final String DEFAULT_DOWNLOAD_PATH = "mnt/sdcard/dolphin";

	public static class Extra {
		public static final String IMAGES = "com.nostra13.example.universalimageloader.IMAGES";
		public static final String IMAGE_POSITION = "com.nostra13.example.universalimageloader.IMAGE_POSITION";
	}
}
