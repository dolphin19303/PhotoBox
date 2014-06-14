package com.dolphin.android.imgdownloader.download;

import java.io.File;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;

import com.dolphin.android.imgdownloader.FxConstants;
import com.dolphin.android.imgdownloader.utils.L;

/*
 * here we are going to use an AsyncTask to download the image 
 *      in background while showing the download progress
 * */

@SuppressLint("NewApi")
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class ImageDownloader implements FxConstants {
	private Context mActivity;
	private long enqueue;
	private DownloadManager mgr;
	private String folderPath;
	private String fileName = "fileName.jpg";
	private ImageDownloaderListener mCallback;

	public ImageDownloader(Context activity) {
		mActivity = activity;
	}

	public void setFolderPath(String path) {
		folderPath = path;
	}

	public void setName(String name) {
		if (name != null && name.replace(".jpg", "").length() > 0)
			this.fileName = name;
	}

	public String getName() {
		return fileName;
	}

	public String getFolderPath() {
		return folderPath;
	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	public void downloadFile(String uRl, ImageDownloaderListener callback) {
		mCallback = callback;
		File direct = new File(folderPath);

		if (!direct.exists()) {
			direct.mkdirs();
		}

		mgr = (DownloadManager) mActivity
				.getSystemService(Context.DOWNLOAD_SERVICE);
		Uri downloadUri = Uri.parse(uRl);
		DownloadManager.Request request = new DownloadManager.Request(
				downloadUri);

		request.setAllowedNetworkTypes(
				DownloadManager.Request.NETWORK_WIFI
						| DownloadManager.Request.NETWORK_MOBILE)
				.setAllowedOverRoaming(false)
				.setShowRunningNotification(false)
				.setNotificationVisibility(
						DownloadManager.Request.VISIBILITY_HIDDEN)
				.setDestinationUri(
						Uri.fromFile(new File(folderPath + "/" + fileName)));
		L.e(Uri.fromFile(new File(folderPath + "/" + fileName)).toString()
				+ "|" + fileName);
		// .setDestinationInExternalPublicDir("/" + folderPath, fileName);

		enqueue = mgr.enqueue(request);
		registerReceiver();
	}

	private void unRegisterReceiver() {
		mActivity.unregisterReceiver(receiver);
	}

	private void registerReceiver() {
		mActivity.registerReceiver(receiver, new IntentFilter(
				DownloadManager.ACTION_DOWNLOAD_COMPLETE));
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String uri = null, localUri = null;
			int isDownloadComplete = DOWNLOAD_FAILED;
			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
				long downloadId = intent.getLongExtra(
						DownloadManager.EXTRA_DOWNLOAD_ID, 0);
				Query query = new Query();
				query.setFilterById(enqueue);
				Cursor c = mgr.query(query);
				if (c.moveToFirst()) {
					int columnIndex = c
							.getColumnIndex(DownloadManager.COLUMN_STATUS);
					if (DownloadManager.STATUS_SUCCESSFUL == c
							.getInt(columnIndex)) {

						uri = c.getString(c
								.getColumnIndex(DownloadManager.COLUMN_URI));
						localUri = c
								.getString(c
										.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
						isDownloadComplete = DOWNLOAD_COMPLETE;
					}
				}
				c.close();
				c = null;
			}

			if (mCallback != null) {
				mCallback.onDownload(isDownloadComplete, uri, localUri);
			}
			unRegisterReceiver();
		}
	};

	public interface ImageDownloaderListener {
		public void onDownload(int result, String uri, String localUri);
	}
}