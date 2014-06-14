/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.dolphin.android.imgdownloader.activity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.dolphin.android.imgdownloader.FxApplication;
import com.dolphin.android.imgdownloader.FxBaseActivity;
import com.dolphin.android.imgdownloader.FxConstants;
import com.dolphin.android.imgdownloader.R;
import com.dolphin.android.imgdownloader.file.FileManager;
import com.dolphin.android.imgdownloader.file.FileManager.FileCallback;
import com.dolphin.android.imgdownloader.storage.PreferenceManager;
import com.dolphin.android.imgdownloader.utils.L;
import com.dolphin.android.imgdownloader.utils.T;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import dolphin.android.sdk.album.model.Photo;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
@SuppressLint("NewApi")
public class ImagePagerActivity extends FxBaseActivity implements FxConstants {

	private static final String STATE_POSITION = "STATE_POSITION";

	DisplayImageOptions options;
	private PhotoViewAttacher mAttacher;

	protected ViewPager pager;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	PhotoView mCurrentPhotoView;
	protected int currentPosition;

	private WakeLock mWakeLock;

	private Menu mMenu;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// full screen
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_image_pager);
		FxApplication.mImagePager = this;
		// full screen
		getWindow().setFlags(0x01000000, 0x01000000);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// initial ImageLoader
		initImageLoader(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		Bundle bundle = getIntent().getExtras();
		assert bundle != null;
		String[] imageUrls = bundle.getStringArray(Extra.IMAGES);
		int pagerPosition = bundle.getInt(Extra.IMAGE_POSITION, 0);

		if (savedInstanceState != null) {
			pagerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		// Photoview initial
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error)
				.resetViewBeforeLoading(true).cacheOnDisk(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
				.displayer(new FadeInBitmapDisplayer(400)).build();

		// Pager initial
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new ImagePagerAdapter(imageUrls));
		pager.setCurrentItem(pagerPosition);

		// declare wakelock
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				getString(R.string.app_name));
	}

	@Override
	protected void onStart() {
		super.onStart();

	}

	public void updateActionBarDownloadStatus() {
		isShowDownload = !FxApplication.mPhotoList.get(currentPosition)
				.isDownloaded();
		setMenuDownloadEnabled();
	}

	private void setMenuDownloadEnabled() {
		try {
			doShare(getDefaultIntent());
			if (isShowDownload()) {
				mMenu.findItem(R.id.action_delete).setIcon(
						R.drawable.btn_imagegrid_menu_download);
			} else {
				mMenu.findItem(R.id.action_delete).setIcon(
						R.drawable.btn_imagegrid_menu_delete);
			}
			// showDetail();
		} catch (NullPointerException e) {
			L.e("Null when click menu");
		}
	}

	private boolean isShowDownload;

	private boolean isShowDownload() {
		return isShowDownload;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, pager.getCurrentItem());
	}

	private class ImagePagerAdapter extends PagerAdapter {

		private String[] images;
		private LayoutInflater inflater;

		ImagePagerAdapter(String[] images) {
			this.images = images;
			inflater = getLayoutInflater();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

		@Override
		public int getCount() {
			return images.length;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			currentPosition = position;
			mCurrentPhotoView = (PhotoView) ((View) object)
					.findViewById(R.id.image);
			updateActionBarDownloadStatus();
			// setMenuDeleteEnabled(FxApplication.mPhotoList.get(currentPosition)
			// .isDownloaded());

		}

		@Override
		public Object instantiateItem(ViewGroup view, int position) {
			View imageLayout = inflater.inflate(R.layout.item_pager_image,
					view, false);
			assert imageLayout != null;

			// The MAGIC happens here!
			PhotoView imageView = (PhotoView) imageLayout
					.findViewById(R.id.image);

			mAttacher = new PhotoViewAttacher(imageView);

			// Lets attach some listeners, not required though!
			mAttacher.setOnMatrixChangeListener(new MatrixChangeListener());
			mAttacher.setOnPhotoTapListener(new PhotoTapListener());
			final ProgressBar spinner = (ProgressBar) imageLayout
					.findViewById(R.id.loading);

			imageLoader.displayImage(images[position], imageView, options,
					new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							spinner.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view,
								FailReason failReason) {
							String message = null;
							switch (failReason.getType()) {
							case IO_ERROR:
								message = "Input/Output error";
								break;
							case DECODING_ERROR:
								message = "Image can't be decoded";
								break;
							case NETWORK_DENIED:
								message = "Downloads are denied";
								break;
							case OUT_OF_MEMORY:
								message = "Out Of Memory error";
								break;
							case UNKNOWN:
								message = "Unknown error";
								break;
							}
							T.show(message);

							spinner.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri,
								View view, Bitmap loadedImage) {
							spinner.setVisibility(View.GONE);
						}
					});

			view.addView(imageLayout, 0);
			return imageLayout;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}

	private class PhotoTapListener implements OnPhotoTapListener {

		@Override
		public void onPhotoTap(View view, float x, float y) {

		}
	}

	private class MatrixChangeListener implements OnMatrixChangedListener {

		@Override
		public void onMatrixChanged(RectF rect) {
		}
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

	@Override
	protected void onPause() {
		if (this.mWakeLock != null)
			this.mWakeLock.release();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (this.mWakeLock != null)
			this.mWakeLock.acquire();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		ImageLoader.getInstance().clearMemoryCache();
	}

	ShareActionProvider mShareActionProvider;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.image_pager, menu);

		MenuItem menuItem = menu.findItem(R.id.action_share);
		// Get the provider and hold onto it to set/change the share intent.
		mShareActionProvider = (ShareActionProvider) MenuItemCompat
				.getActionProvider(menuItem);
		// Set history different from the default before getting the action
		// view since a call to MenuItemCompat.getActionView() calls
		// onCreateActionView() which uses the backing file name. Omit this
		// line if using the default share history file is desired.
		Intent i = getDefaultIntent();
		if (i != null) {
			mShareActionProvider.setShareIntent(i);
		}
		return true;
	}

	private Intent getDefaultIntent() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("image/*");
		File mSharedFile = new File(
				FxApplication.mPreferenceManager
						.getString(PreferenceManager.DOWNLOAD_PATH)
						+ "/"
						+ FxApplication.mPhotoList.get(currentPosition).getId()
						+ ".jpg");
		if (mSharedFile.exists()) {
			Uri uri = Uri.fromFile(mSharedFile);
			intent.putExtra(Intent.EXTRA_STREAM, uri);
		} else {
			URL url;
			URI uri = null;
			try {
				url = new URL("http://www.google.com");
				uri = url.toURI();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Some instantiated URL
			catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			intent.putExtra(Intent.EXTRA_STREAM, uri);
		}
		return intent;
	}

	public void doShare(Intent shareIntent) {
		// When you want to share set the share intent.
		if (shareIntent != null)
			mShareActionProvider.setShareIntent(shareIntent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_rotate_left:
			rotateImageLeft();
			break;
		case R.id.action_rotate_right:
			rotateImageRight();
			break;
		case R.id.action_delete:
			if (isShowDownload()) {
				showDownload();
			} else {
				showConfirmDeleteDialog();
			}
			break;
		case android.R.id.home:
			finish();
			overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void rotateImageLeft() {
		if (getCurrentPhotoView() != null) {
			getCurrentPhotoView().setRotationBy(-90);
		}
	}

	private void rotateImageRight() {
		if (getCurrentPhotoView() != null) {
			getCurrentPhotoView().setRotationBy(90);
		}
	}

	private void deleteImage() {
		FxApplication.mFileManager.deleteFile(FxApplication.mPreferenceManager
				.getString(PreferenceManager.DOWNLOAD_PATH),
				FxApplication.mPhotoList.get(currentPosition),
				new FileCallback() {

					@Override
					public void onDelete(int status, String uri) {
						if (status == FileManager.FILE_DELETE_COMPLETE
								|| status == FileManager.FILE_DELETE_COMPLETE_ALL) {
							FxApplication.mPhotoList.get(currentPosition)
									.setDownloadStatus(
											Photo.STATUS_NOT_DOWNLOAD);
							updateActionBarDownloadStatus();
							T.show("Delete " + uri + " complete");
						} else
							T.show("Delete failed");
					}
				});
	}

	private void showDownload() {
		FxApplication.mImageDownloadManager.download(FxApplication.mPhotoList
				.get(currentPosition));
	}

	public void showConfirmDeleteDialog() {
		AlertDialog.Builder alertBulder = new Builder(this);
		alertBulder
				.setMessage(R.string.start_exit_confirm_detail)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								deleteImage();
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
		AlertDialog mDialog = alertBulder.create();
		mDialog.show();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.fade_in, R.anim.slide_out_right);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mAttacher != null) {
			mAttacher.cleanup();
		}
	}

	private PhotoView getCurrentPhotoView() {
		return mCurrentPhotoView;
	}

}