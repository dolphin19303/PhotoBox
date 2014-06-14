package com.dolphin.android.imgdownloader.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.Bitmap;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.dolphin.android.imgdownloader.FxApplication;
import com.dolphin.android.imgdownloader.FxConstants;
import com.dolphin.android.imgdownloader.R;
import com.dolphin.android.imgdownloader.utils.GeneralLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import dolphin.android.sdk.album.model.Photo;

public class ImageAdapter extends BaseAdapter implements FxConstants {
	SparseBooleanArray mSelectedList = new SparseBooleanArray();
	String[] imageUrls;
	LayoutInflater mInflarer;
	DisplayImageOptions options;

	public ImageAdapter(String[] imageUrls, LayoutInflater mInflarer) {
		this.imageUrls = imageUrls;
		this.mInflarer = mInflarer;
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.ic_stub)
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error).cacheInMemory(true)
				.cacheOnDisk(true).considerExifParams(true)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(200)).build();
	}

	public void setSelectedItem(int position) {
		mSelectedList.put(position, !mSelectedList.get(position));
	}

	public boolean isSelected(int position) {
		return mSelectedList.get(position);
	}

	public ArrayList<Photo> getSelectedItem() {
		ArrayList<Photo> mResult = new ArrayList<Photo>();
		for (int i = 0; i < mSelectedList.size(); i++) {
			mResult.add(FxApplication.mPhotoList.get(mSelectedList.keyAt(i)));
		}
		return mResult;
	}

	public void deSelectedAll() {
		// for (int i = 0; i < FxApplication.mPhotoList.size(); i++) {
		// mSelectedList.put(i, false);
		// }
		mSelectedList = new SparseBooleanArray();
	}

	public void selectAll() {
		for (int i = 0; i < FxApplication.mPhotoList.size(); i++) {
			mSelectedList.put(i, true);
		}
	}

	@Override
	public int getCount() {
		return imageUrls.length;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	public void updateData() {
		imageUrls = FxApplication.getPhotoList();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View mView = convertView;
		if (mView == null) {
			mView = mInflarer.inflate(
					GeneralLayout.getLayout(R.array.item_grid_image), parent,
					false);
			holder = new ViewHolder();
			assert mView != null;
			holder.imageView = (ImageView) mView.findViewById(R.id.image);
			holder.progressBar = (ProgressBar) mView
					.findViewById(R.id.progress);
			mView.setTag(holder);
			holder.imageWrapper = (RelativeLayout) mView
					.findViewById(R.id.imageWrapper);
		} else {
			holder = (ViewHolder) mView.getTag();
		}
		updateImageWrapper(position, holder.imageWrapper,
				mSelectedList.get(position));
		FxApplication.imageLoader.displayImage(imageUrls[position],
				holder.imageView, options, new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						// holder.progressBar.setProgress(0);
						// holder.progressBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						holder.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						holder.progressBar.setVisibility(View.GONE);
					}
				}, new ImageLoadingProgressListener() {
					@Override
					public void onProgressUpdate(String imageUri, View view,
							int current, int total) {
//						holder.progressBar.setProgress(Math.round(100.0f
//								* current / total));
					}
				});
		return mView;
	}

	public class HostComparator implements Comparator<Photo> {
		@Override
		public int compare(Photo o1, Photo o2) {
			return (String.valueOf(o1.getHost()).compareTo(String.valueOf(o2
					.getHost())));
		}
	}

	public class DownloadComparator implements Comparator<Photo> {
		@Override
		public int compare(Photo o1, Photo o2) {
			if (o1.isDownloaded() == o2.isDownloaded())
				return 0;
			if (o1.isDownloaded() != o2.isDownloaded() && o1.isDownloaded())
				return 1;
			else
				return -1;
		}
	}

	public class DateComparator implements Comparator<Photo> {
		@Override
		public int compare(Photo o1, Photo o2) {
			if (o1.getDate().before(o2.getDate())) {
				return -1;
			} else if (o1.getDate().after(o2.getDate())) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public void sortByHost(boolean isAcending) {
		Collections.sort(FxApplication.mPhotoList, new HostComparator());
		if (isAcending) {
			Collections.reverse(FxApplication.mPhotoList);
		}
		imageUrls = FxApplication.getPhotoList();
		notifyDataSetChanged();
	}

	public void sortByDownload(boolean isAcending) {
		Collections.sort(FxApplication.mPhotoList, new DownloadComparator());
		if (isAcending) {
			Collections.reverse(FxApplication.mPhotoList);
		}
		imageUrls = FxApplication.getPhotoList();
		notifyDataSetChanged();
	}

	public void sortByDate(boolean isAcending) {
		Collections.sort(FxApplication.mPhotoList, new DateComparator());
		if (isAcending) {
			Collections.reverse(FxApplication.mPhotoList);
		}
		imageUrls = FxApplication.getPhotoList();
		notifyDataSetChanged();
	}

	class ViewHolder {
		ImageView imageView;
		ProgressBar progressBar;
		RelativeLayout imageWrapper;
	}

	public void updateImageWrapper(int position, RelativeLayout wrapper,
			boolean isSelected) {
		if (FxApplication.mPhotoList != null
				&& FxApplication.mPhotoList.size() > 0) {
			switch (FxApplication.mPhotoList.get(position).getHost()) {
			case FACEBOOK:

				if (FxApplication.mPhotoList.get(position).getDownloadStatus() == Photo.STATUS_NOT_DOWNLOAD) {
					wrapper.setBackgroundResource(isSelected ? R.drawable.image_container_facebook_selected
							: R.drawable.image_container_facebook);
				} else {
					wrapper.setBackgroundResource(isSelected ? R.drawable.image_container_facebook_selected_downloaded
							: R.drawable.image_container_facebook_downloaded);
				}
				break;
			case INSTAGRAM:
				if (FxApplication.mPhotoList.get(position).getDownloadStatus() == Photo.STATUS_NOT_DOWNLOAD) {
					wrapper.setBackgroundResource(isSelected ? R.drawable.image_container_instagram_selected
							: R.drawable.image_container_instagram);
				} else {
					wrapper.setBackgroundResource(isSelected ? R.drawable.image_container_instagram_selected_downloaded
							: R.drawable.image_container_instagram_downloaded);
				}
				break;
			case TWITTER:
				if (FxApplication.mPhotoList.get(position).getDownloadStatus() == Photo.STATUS_NOT_DOWNLOAD) {
					wrapper.setBackgroundResource(isSelected ? R.drawable.image_container_twitter_selected
							: R.drawable.image_container_twitter);
				} else {
					wrapper.setBackgroundResource(isSelected ? R.drawable.image_container_twitter_selected_downloaded
							: R.drawable.image_container_twitter_downloaded);
				}
				break;
			default:
				break;
			}
		}
	}
}