package com.dolphin.android.imgdownloader.fragment;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dolphin.android.imgdownloader.FxApplication;
import com.dolphin.android.imgdownloader.FxBaseFragment;
import com.dolphin.android.imgdownloader.FxConstants;
import com.dolphin.android.imgdownloader.R;
import com.dolphin.android.imgdownloader.activity.ImagePagerActivity;
import com.dolphin.android.imgdownloader.activity.StartActivity;
import com.dolphin.android.imgdownloader.adapter.ImageAdapter;
import com.dolphin.android.imgdownloader.download.ImageDownloadManager.ImageDownloadManagerListener;
import com.dolphin.android.imgdownloader.file.FileManager;
import com.dolphin.android.imgdownloader.file.FileManager.FileCallback;
import com.dolphin.android.imgdownloader.storage.PreferenceManager;
import com.dolphin.android.imgdownloader.ui.BottomActionBar;
import com.dolphin.android.imgdownloader.ui.ProgressNotifier;
import com.dolphin.android.imgdownloader.utils.GeneralLayout;
import com.dolphin.android.imgdownloader.utils.L;
import com.dolphin.android.imgdownloader.utils.T;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import dolphin.android.sdk.album.facebook.FacebookLogin;
import dolphin.android.sdk.album.instagram.InstagramLogin;
import dolphin.android.sdk.album.model.Photo;
import dolphin.android.sdk.album.twitter.TwitterLogin;

/**
 * Created by Administrator on 2/18/14.cls
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ImageGridFragment extends FxBaseFragment implements FxConstants {
	private static String TAG = "ImageGridFragment";
	private View view;
	private boolean isSelectedMode;

	String[] imageUrls;
	protected AbsListView listView;

	LayoutInflater mInflarer;

	Menu mMenu;
	// grid
	static final int MENU_SET_MODE = 0;
	ImageAdapter mGridAdapter;
	private PullToRefreshGridView mPullRefreshGridView;
	private GridView mGridView;

	// bottom menu
	private BottomActionBar mBottomActionBar;
	private ImageView ivMultipleDelete, ivMultipleDownload, ivSelectAll;
	private boolean isSelectAll;

	private ProgressNotifier mProgressNotifier;

	private boolean sortByDownloadAcending;
	private boolean sortByHostAcending;
	private boolean sortByDateAcending;
	private static final String TEST_DEVICE_ID = "33AF62823C718AB2";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.TAG = TAG;
		mInflarer = inflater;
		setHasOptionsMenu(true);

		((StartActivity) getActivity()).getSupportActionBar().setTitle(
				getString(R.string.imagegrid));
		try {
			view = inflater
					.inflate(GeneralLayout
							.getLayout(R.array.layout_fragment_image_grid),
							container, false);
		} catch (InflateException e) {
			L.e(TAG + e.toString());
		}

		// init view
		initView(view);
		isSelectAll = false;
		sortByDownloadAcending = true;
		sortByHostAcending = true;
		sortByDateAcending = true;

		// The "loadAdOnCreate" and "testDevices" XML attributes no longer
		// available.
		AdView adView = (AdView) view.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice(TEST_DEVICE_ID).build();
		adView.loadAd(adRequest);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initData();
		initGrid(view);
	}

	private void initData() {
		FxApplication.mImageDownloadManager
				.setDownloadListener(new ImageDownloadManagerListener() {

					@Override
					public void onProgress(int percent, String currentUri) {
						mProgressNotifier.setPercent(percent);
					}

					@Override
					public void onDownload(int status, String uri,
							String localUri) {
						if (status == DOWNLOAD_COMPLETE) {
							int position = FxApplication.mPhotoList
									.indexOf(new Photo("", uri));
							FxApplication.mPhotoList.get(position)
									.setDownloadStatus(Photo.STATUS_DOWNLOADED);
							if (position >= 0) {
								try {
									// RelativeLayout mWrapper =
									// (RelativeLayout) mGridView
									// .getChildAt(position).findViewById(
									// R.id.imageWrapper);

									RelativeLayout mWrapper = (RelativeLayout) getViewFromGrid(
											position).findViewById(
											R.id.imageWrapper);

									mGridAdapter.updateImageWrapper(position,
											mWrapper,
											mGridAdapter.isSelected(position));
									FxApplication.mImagePager
											.updateActionBarDownloadStatus();
								} catch (NullPointerException e) {
									L.e("Null when download file. ImageFragment");
								}
							}
						}
						if (status == DOWNLOAD_COMPLETE_ALL) {
							mProgressNotifier.hideProgressBar();
							T.show("Dowload complete");
						}
					}
				});

		if (FxApplication.mPhotoList != null
				&& FxApplication.mPhotoList.size() > 0) {
			imageUrls = new String[FxApplication.mPhotoList.size()];
			int iCounter = 0;
			for (Photo mPhoto : FxApplication.mPhotoList) {
				if (mPhoto.getThumbnail() != null
						&& !mPhoto.getThumbnail().equals("")) {
					imageUrls[iCounter] = mPhoto.getThumbnail();
				} else {
					imageUrls[iCounter] = mPhoto.getLink();
				}
				iCounter++;
			}
		} else {
			imageUrls = new String[] {};
		}
	}

	View getViewFromGrid(int position) {
		int numVisibleChildren = mGridView.getChildCount();
		int firstVisiblePosition = mGridView.getFirstVisiblePosition();
		for (int i = 0; i < numVisibleChildren; i++) {
			int positionOfView = firstVisiblePosition + i;

			if (positionOfView == position) {
				return mGridView.getChildAt(i);
			}
		}
		return null;
	}

	public void refreshGrid() {
		if (mGridAdapter != null && mPullRefreshGridView != null) {
			if (mGridAdapter.getCount() != FxApplication.mSqliteManager
					.getAllData().size()) {
				FxApplication.mPhotoList = FxApplication.mSqliteManager
						.getAllData();
				mGridAdapter.updateData();
				mGridAdapter.notifyDataSetChanged();
				L.e("Update grid!! " + mGridAdapter.getCount());
			}

		} else {
			L.e("Null at " + TAG);
		}
	}

	private void refreshSNSData() {
		((StartActivity) getActivity()).updateDataFromSNS();
	}

	private void initGrid(View v) {
		mPullRefreshGridView = (PullToRefreshGridView) v
				.findViewById(R.id.pull_refresh_grid);
		mGridView = mPullRefreshGridView.getRefreshableView();

		// Set a listener to be invoked when the list should be refreshed.
		mPullRefreshGridView
				.setOnRefreshListener(new OnRefreshListener2<GridView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						new GetDataTask().execute();
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						new GetDataTask().execute();
					}

				});

		TextView tv = new TextView(getActivity());
		tv.setGravity(Gravity.CENTER);
		tv.setText("Pull Down/Up to update photo !");
		mPullRefreshGridView.setEmptyView(tv);

		mGridAdapter = new ImageAdapter(FxApplication.getPhotoList(), mInflarer);
		mGridView.setAdapter(mGridAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (!isSelectedMode) {
					startImagePagerActivity(position);
				} else {
					mGridAdapter.setSelectedItem(position);

					RelativeLayout images = (RelativeLayout) view
							.findViewById(R.id.imageWrapper);
					mGridAdapter.updateImageWrapper(position, images,
							mGridAdapter.isSelected(position));
				}
			}
		});

		mGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				FxApplication.mImageDownloadManager
						.download(FxApplication.mPhotoList.get(position));
				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshSNSData();
	}

	public ImageAdapter getImageAdapter() {
		return mGridAdapter;
	}

	protected void deSelectedAll() {
		if (mGridAdapter != null) {
			mGridAdapter.deSelectedAll();
			for (int i = 0; i < mGridView.getCount(); i++) {
				try {
					RelativeLayout mWrapper = (RelativeLayout) mGridView
							.getChildAt(i).findViewById(R.id.imageWrapper);
					mGridAdapter.updateImageWrapper(i, mWrapper, false);
				} catch (NullPointerException e) {

				}
			}
		}
	}

	protected void selectAll() {
		mGridAdapter.selectAll();
		for (int i = 0; i < mGridView.getCount(); i++) {
			try {
				RelativeLayout mWrapper = (RelativeLayout) mGridView
						.getChildAt(i).findViewById(R.id.imageWrapper);
				mGridAdapter.updateImageWrapper(i, mWrapper, true);
			} catch (NullPointerException e) {

			}
		}
	}

	// initial view
	private void initView(View view) {
		FxApplication.mFacebookLogin = new FacebookLogin(this);
		FxApplication.mTwitterLogin = new TwitterLogin(getActivity());
		FxApplication.mInstagramLogin = new InstagramLogin(getActivity());

		// init actionbar
		mBottomActionBar = (BottomActionBar) view
				.findViewById(R.id.llBottomActionBar);
		// Calculate ActionBar height
		TypedValue tv = new TypedValue();
		int height = 0;
		if (getActivity().getTheme().resolveAttribute(
				android.R.attr.actionBarSize, tv, true)) {
			height = TypedValue.complexToDimensionPixelSize(tv.data,
					getResources().getDisplayMetrics());
		}
		// RelativeLayout.LayoutParams layout_description = new
		// RelativeLayout.LayoutParams(
		// RelativeLayout.LayoutParams.WRAP_CONTENT, height);
		// layout_description.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		// mBottomActionBar.setLayoutParams(layout_description);

		ivMultipleDelete = (ImageView) view.findViewById(R.id.ivMultipleDelete);
		ivMultipleDownload = (ImageView) view
				.findViewById(R.id.ivMultipleDownload);
		ivSelectAll = (ImageView) view.findViewById(R.id.ivSelectAll);
		ivMultipleDelete.setOnClickListener(mBottomBarOnClick);
		ivMultipleDownload.setOnClickListener(mBottomBarOnClick);
		ivSelectAll.setOnClickListener(mBottomBarOnClick);

		mProgressNotifier = new ProgressNotifier(getActivity());
		mProgressNotifier.InitialNotification();

	}

	private void startImagePagerActivity(int position) {
		Intent intent = new Intent(getActivity(), ImagePagerActivity.class);

		intent.putExtra(Extra.IMAGES, FxApplication.getPhotoList());
		intent.putExtra(Extra.IMAGE_POSITION, position);
		startActivity(intent);
		getActivity().overridePendingTransition(R.anim.slide_in_right,
				R.anim.fade_out);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.start, menu);
		super.onCreateOptionsMenu(menu, inflater);
		this.mMenu = menu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (!((StartActivity) getActivity()).isDrawerOpened()) {
			int id = item.getItemId();
			if (id == R.id.action_multiple_select) {
				isSelectedMode = !isSelectedMode;
				if (isSelectedMode) {
					item.setIcon(R.drawable.imagegrid_multiple_choice_selected);
					mBottomActionBar.show();
				} else {
					item.setIcon(R.drawable.imagegrid_multiple_choice);
					hideActionBar();
				}
			}
			if (id == R.id.action_sort) {
				AlertDialog.Builder builderSingle = new AlertDialog.Builder(
						getActivity());
				builderSingle.setIcon(R.drawable.ic_launcher);
				builderSingle.setTitle("Select One Name:-");
				final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
						getActivity(),
						android.R.layout.select_dialog_singlechoice);
				arrayAdapter.add("Host");
				arrayAdapter.add("Download");
				arrayAdapter.add("Date");
				builderSingle.setNegativeButton("cancel",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});

				builderSingle.setAdapter(arrayAdapter,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								switch (which) {
								case 0:
									sortByHost();
									break;
								case 1:
									sortByDownload();
									break;
								case 2:
									sortByDate();
									break;
								default:
									break;
								}
							}
						});
				builderSingle.show();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void sortByDownload() {
		sortByDownloadAcending = !sortByDownloadAcending;
		mGridAdapter.sortByDownload(sortByDownloadAcending);
	}

	private void sortByHost() {
		sortByHostAcending = !sortByHostAcending;
		mGridAdapter.sortByHost(sortByHostAcending);
	}

	private void sortByDate() {
		sortByDateAcending = !sortByDateAcending;
		mGridAdapter.sortByDate(sortByDateAcending);
	}

	public boolean getSelectedMode() {
		return isSelectedMode;
	}

	public void setSelectedMode(boolean mode) {
		isSelectedMode = mode;
	}

	private class GetDataTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Simulates a background job.
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			refreshGrid();
			// Call onRefreshComplete when the list has been refreshed.
			mPullRefreshGridView.onRefreshComplete();
			refreshSNSData();
			super.onPostExecute(result);
		}
	}

	OnClickListener mBottomBarOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.ivMultipleDelete:
				if (mGridAdapter.getSelectedItem().size() > 0) {
					showConfirmDeleteDialog();
				}
				break;
			case R.id.ivMultipleDownload:
				if (mGridAdapter.getSelectedItem().size() > 0) {
					showConfirmDownloadDialog();
				}
				break;
			case R.id.ivSelectAll:
				isSelectAll = !isSelectAll;
				if (isSelectAll) {
					selectAll();
				} else {
					deSelectedAll();
				}
				break;
			default:
				break;
			}
		}
	};

	public void showConfirmDeleteDialog() {
		AlertDialog.Builder alertBulder = new Builder(getActivity());
		alertBulder
				.setMessage(R.string.start_exit_confirm_detail)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								deleteSelectedFile();
								hideActionBar();
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

	public void showConfirmDownloadDialog() {
		AlertDialog.Builder alertBulder = new Builder(getActivity());
		alertBulder
				.setMessage(R.string.start_exit_confirm_detail)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								downloadSelectedFile();
								hideActionBar();
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

	private void downloadSelectedFile() {
		FxApplication.mImageDownloadManager.download(mGridAdapter
				.getSelectedItem());
	}

	private void deleteSelectedFile() {

		FxApplication.mFileManager.deleteFile(FxApplication.mPreferenceManager
				.getString(PreferenceManager.DOWNLOAD_PATH), mGridAdapter
				.getSelectedItem(), new FileCallback() {

			@Override
			public void onDelete(int status, String uri) {
				if (status == FileManager.FILE_DELETE_COMPLETE) {
					if (mGridAdapter != null) {
						int position = FxApplication.mPhotoList
								.indexOf(new Photo("", uri));
						if (position >= 0) {
							FxApplication.mPhotoList.get(position)
									.setDownloadStatus(
											Photo.STATUS_NOT_DOWNLOAD);
							try {
								RelativeLayout mWrapper = (RelativeLayout) getViewFromGrid(
										position).findViewById(
										R.id.imageWrapper);
								mGridAdapter.updateImageWrapper(position,
										mWrapper, false);
							} catch (NullPointerException e) {
								L.e("set display delete failed");
							}
						}

					}
				} else {
					L.e("delete failed");
				}
			}
		});
	}

	public void hideActionBar() {
		isSelectedMode = false;
		deSelectedAll();
		try {
			if (mBottomActionBar.isShowed())
				mBottomActionBar.hide();
			MenuItem mItem = mMenu.findItem(R.id.action_multiple_select);
			mItem.setIcon(R.drawable.imagegrid_multiple_choice);
		} catch (NullPointerException e) {
			L.e("Got null when hideActionBar()");
		}
	}

	public boolean isBottomActionbarShowed() {
		if (mBottomActionBar != null)
			return mBottomActionBar.isShowed();
		return false;
	}

	@Override
	public void onDestroyView() {
		hideActionBar();
		super.onDestroyView();
	}
}
