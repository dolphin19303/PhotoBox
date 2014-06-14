package com.dolphin.android.imgdownloader.download;

import java.util.ArrayList;

import android.content.Context;

import com.dolphin.android.imgdownloader.FxApplication;
import com.dolphin.android.imgdownloader.FxConstants;
import com.dolphin.android.imgdownloader.download.ImageDownloader.ImageDownloaderListener;
import com.dolphin.android.imgdownloader.storage.PreferenceManager;
import com.dolphin.android.imgdownloader.utils.L;

import dolphin.android.sdk.album.model.Photo;

public class ImageDownloadManager implements FxConstants {
	private ArrayList<Photo> listDownload;
	private Context mContext;
	private String dowloadFolder = "dolphin";
	private boolean isDownloading;
	private ImageDownloadManagerListener managerListener;
	private String folderPath;
	DownloadProgressItem mProgressItem;

	public ImageDownloadManager(Context context) {
		mContext = context;
		isDownloading = false;
		listDownload = new ArrayList<Photo>();
		mProgressItem = new DownloadProgressItem();
	}

	public void download(Photo url) {
		if (url != null && !url.isDownloaded()) {
			listDownload.add(url);
			mProgressItem.addItem(1);
			startDownload();
		}
	}

	public void download(ArrayList<Photo> urls) {
		if (urls != null) {
			ArrayList<Photo> listCanDownload = new ArrayList<Photo>();
			for (Photo photo : urls) {
				if (!photo.isDownloaded()) {
					listCanDownload.add(photo);
				}
			}
			if (listCanDownload.size() > 0) {
				listDownload.addAll(listCanDownload);
				mProgressItem.addItem(listCanDownload.size());
				startDownload();
			}
		}
	}

	private void startDownload() {
		folderPath = FxApplication.mPreferenceManager
				.getString(PreferenceManager.DOWNLOAD_PATH);
		if (!isDownloading) {
			onProgressNotify(mProgressItem.getPercent(), listDownload.get(0)
					.getLink());
			nextDownload();
		}
	}

	private void nextDownload() {
		if (listDownload != null && listDownload.size() > 0 && !isDownloading) {
			ImageDownloader2 mDownloader = new ImageDownloader2();
			mDownloader.setFolderPath(folderPath);
			mDownloader.setName(listDownload.get(0).getId() + ".jpg");
			isDownloading = true;
			mDownloader.setCallback(new ImageDownloaderListener() {

				@Override
				public void onDownload(int result, String uri, String localUri) {
					onDownloadNotify(result, uri, localUri);
					isDownloading = false;
					mProgressItem.addDownloadedItem(1);
					onProgressNotify(mProgressItem.getPercent(), listDownload
							.get(0).getLink());
					if (listDownload.size() > 0) {
						listDownload.remove(0);
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					nextDownload();
					return;
				}
			});
			mDownloader.execute(listDownload.get(0).getLink());
			// mDownloader.downloadFile(listDownload.get(0).getLink(),
			// new ImageDownloaderListener() {
			//
			// @Override
			// public void onDownload(int result, String uri,
			// String localUri) {
			// onDownloadNotify(result, uri, localUri);
			// isDownloading = false;
			// mProgressItem.addDownloadedItem(1);
			// onProgressNotify(mProgressItem.getPercent(),
			// listDownload.get(0).getLink());
			// if (listDownload.size() > 0) {
			// listDownload.remove(0);
			// }
			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// nextDownload();
			// return;
			// }
			// });
			return;
		} else {
			L.e("Im out of loop !!");
			isDownloading = false;
			onDownloadNotify(DOWNLOAD_COMPLETE_ALL, null, null);
			mProgressItem.reset();
			return;
		}
	}

	private class DownloadProgressItem {
		private int totalItem = 0;
		private int downloadedItem = 0;

		public DownloadProgressItem() {
		}

		public void reset() {
			totalItem = 0;
			downloadedItem = 0;
		}

		public void setTotalItem(int total) {
			this.totalItem = total;
		}

		public void setDownloadedItem(int item) {
			downloadedItem = item;
		}

		public void addItem(int num) {
			totalItem += num;

		}

		public void addDownloadedItem(int num) {
			downloadedItem += num;

		}

		public int getPercent() {
			if (totalItem > 0) {
				return downloadedItem * 100 / totalItem;
			}
			return 100;
		}
	}

	public void setDownloadListener(ImageDownloadManagerListener managerListener) {
		if (managerListener != null) {
			this.managerListener = managerListener;
		}
	}

	private void onDownloadNotify(int status, String uri, String currentUri) {
		if (managerListener != null) {
			managerListener.onDownload(status, uri, currentUri);
		}
	}

	private void onProgressNotify(int percent, String currentUri) {
		if (managerListener != null) {
			managerListener.onProgress(percent, currentUri);
		}
	}

	public interface ImageDownloadManagerListener {
		public void onDownload(int status, String uri, String localUri);

		public void onProgress(int percent, String currentUri);
	}
}
