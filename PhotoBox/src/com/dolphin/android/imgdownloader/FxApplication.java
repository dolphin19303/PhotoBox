package com.dolphin.android.imgdownloader;

import java.util.ArrayList;

import android.app.Application;
import android.content.Context;

import com.dolphin.android.imgdownloader.activity.ImagePagerActivity;
import com.dolphin.android.imgdownloader.connectmanager.ConnectManager;
import com.dolphin.android.imgdownloader.download.ImageDownloadManager;
import com.dolphin.android.imgdownloader.file.FileManager;
import com.dolphin.android.imgdownloader.storage.PreferenceManager;
import com.dolphin.android.imgdownloader.storage.SqliteManager;
import com.dolphin.android.imgdownloader.utils.GeneralLayout;
import com.dolphin.android.imgdownloader.utils.LayoutUtil;
import com.dolphin.android.imgdownloader.utils.T;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import dolphin.android.sdk.album.facebook.FacebookAlbumManager;
import dolphin.android.sdk.album.facebook.FacebookLogin;
import dolphin.android.sdk.album.instagram.InstagramLogin;
import dolphin.android.sdk.album.instagram.InstagramPhotoManager;
import dolphin.android.sdk.album.model.Photo;
import dolphin.android.sdk.album.twitter.TwitterLogin;
import dolphin.android.sdk.album.twitter.TwitterPhotoManager;

/**
 * Created by Administrator on 2/13/14.
 */
public class FxApplication extends Application {
	public static ArrayList<Photo> mPhotoList = new ArrayList<Photo>();

	public static FacebookAlbumManager mFacebookAlbumManager;
	public static FacebookLogin mFacebookLogin;

	public static TwitterLogin mTwitterLogin;
	public static TwitterPhotoManager mTwitterPhotoManager;

	public static InstagramLogin mInstagramLogin;
	public static InstagramPhotoManager mInstagramPhotoManager;

	public static ImageDownloadManager mImageDownloadManager;

	public static ImageLoader imageLoader = ImageLoader.getInstance();

	public static PreferenceManager mPreferenceManager;

	public static SqliteManager mSqliteManager;

	public static FileManager mFileManager;

	public static ConnectManager mConnectManager;

	public static ImagePagerActivity mImagePager;

	@Override
	public void onCreate() {
		super.onCreate();
		T.init(this);
		LayoutUtil.init(this);
		GeneralLayout.init(this);
		mImageDownloadManager = new ImageDownloadManager(this);
		mPreferenceManager = new PreferenceManager(this);
		mConnectManager = new ConnectManager(this);
		mSqliteManager = new SqliteManager(this);
		mFileManager = new FileManager(
				mPreferenceManager.getString(PreferenceManager.DOWNLOAD_PATH));
		// initial ImageLoader
		initImageLoader(this);
	}

	public ImageDownloadManager getDownloadManager() {
		if (mImageDownloadManager == null) {
			mImageDownloadManager = new ImageDownloadManager(this);
		}
		return mImageDownloadManager;
	}

	public static String[] getPhotoList() {

		String[] imageUrls = new String[FxApplication.mPhotoList.size()];
		int iCounter = 0;
		for (Photo mPhoto : FxApplication.mPhotoList) {
			imageUrls[iCounter] = mPhoto.getLink();
			iCounter++;
		}
		return imageUrls;
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}
}
