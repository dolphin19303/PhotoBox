package com.dolphin.android.imgdownloader.activity;

import java.util.ArrayList;

import oauth.signpost.basic.DefaultOAuthProvider;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dolphin.android.imgdownloader.FxApplication;
import com.dolphin.android.imgdownloader.FxBaseActivity;
import com.dolphin.android.imgdownloader.FxBaseFragment;
import com.dolphin.android.imgdownloader.FxConstants;
import com.dolphin.android.imgdownloader.R;
import com.dolphin.android.imgdownloader.adapter.ListDrawerAdapter;
import com.dolphin.android.imgdownloader.file.DirectoryChooserFragment;
import com.dolphin.android.imgdownloader.fragment.AboutUsFragment;
import com.dolphin.android.imgdownloader.fragment.AccountFragment;
import com.dolphin.android.imgdownloader.fragment.ImageGridFragment;
import com.dolphin.android.imgdownloader.fragment.OptionFragment;
import com.dolphin.android.imgdownloader.storage.PreferenceManager;
import com.dolphin.android.imgdownloader.utils.L;
import com.facebook.Session;

import dolphin.android.sdk.album.facebook.FacebookAlbum;
import dolphin.android.sdk.album.facebook.FacebookAlbumManager;
import dolphin.android.sdk.album.facebook.FacebookAlbumManager.GetAlbumCallback;
import dolphin.android.sdk.album.facebook.FacebookLogin;
import dolphin.android.sdk.album.facebook.FacebookLogin.FacebookLoginCallback;
import dolphin.android.sdk.album.facebook.FacebookPhotoManager.GetImageCallback;
import dolphin.android.sdk.album.instagram.InstagramLogin;
import dolphin.android.sdk.album.instagram.InstagramPhotoManager;
import dolphin.android.sdk.album.instagram.InstagramPhotoManager.GetImageInstagramCallback;
import dolphin.android.sdk.album.model.FacebookPhoto;
import dolphin.android.sdk.album.model.InstagramPhoto;
import dolphin.android.sdk.album.model.Photo;
import dolphin.android.sdk.album.model.TwitterPhoto;
import dolphin.android.sdk.album.twitter.TwitterLogin;
import dolphin.android.sdk.album.twitter.TwitterLogin.TwitterLoginCallback;
import dolphin.android.sdk.album.twitter.TwitterPhotoManager;
import dolphin.android.sdk.album.twitter.TwitterPhotoManager.GetImageTwitterCallback;

public class StartActivity extends FxBaseActivity implements FxConstants,
		DirectoryChooserFragment.OnFragmentInteractionListener {
	public DrawerLayout mDrawerLayout;
	public ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private String[] mMenuListTitles;

	private FacebookLogin mFacebookLogin;
	private FacebookAlbumManager mFacebookAlbumManager;

	private TwitterLogin mTwitterLogin;
	private TwitterPhotoManager mTwitterPhotoManager;

	private InstagramLogin mInstagramLogin;
	private InstagramPhotoManager mInstagramPhotoManager;

	// Fragment
	public static ImageGridFragment mImageGridFragment;
	public static AccountFragment mAccountFragment;
	public static OptionFragment mOptionFragment;
	public static AboutUsFragment mAboutUsFragment;

	private boolean isAddNewPhoto;

	protected int taskCounter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.activity_start_320);
		taskCounter = 0;
		initLoginHost();

		if (!FxApplication.mPreferenceManager
				.getBoolean(PreferenceManager.SHOW_CONFIRM_TWITTER)) {
			FxApplication.mPreferenceManager = new PreferenceManager(this);
			FxApplication.mSqliteManager.open();
			initData();
			startFragment(ImageGridFragment.class);
			initView();
		}
	}

	private void startLoadInstagramImage() {
		addTaskCounter();

		mInstagramPhotoManager = new InstagramPhotoManager(
				FxApplication.mInstagramLogin);
		mInstagramPhotoManager.getListPhoto(new GetImageInstagramCallback() {

			@Override
			public void onReceivedPhoto(short status,
					ArrayList<InstagramPhoto> photos) {
				removeTaskCounter();
				if (status == INSTAGRAM_GETPHOTO_SUCCESS) {
					L.e("got instagram album");
					for (InstagramPhoto mPhoto : photos) {
						addPhoto(mPhoto);
					}

					showImageGrid();
				}
			}
		});
	}

	private void startLoadFacebookImage() {
		addTaskCounter();

		mFacebookAlbumManager = new FacebookAlbumManager(
				FxApplication.mFacebookLogin);
		mFacebookLogin.restoreToken(new FacebookLoginCallback() {
			@Override
			public void onLogin(int result, Session mSession) {
			}

			@Override
			public void onAlreadyLogin(int result, Session mSession) {
				removeTaskCounter();
				if (result == FACEBOOK_LOGIN_RESTORETOKEN_SUCCESS
						|| FxApplication.mFacebookLogin.isLoggedIn()) {
					L.e("got facebook album");
					if (mFacebookAlbumManager != null) {
						getAllImage();
					} else {
						showImageGrid();
					}
				}
			}
		});
	}

	private void startLoadTwitterImage() {

		mTwitterPhotoManager = new TwitterPhotoManager(
				FxApplication.mTwitterLogin);
		// addTaskCounter();
		mTwitterPhotoManager.getListPhoto(new GetImageTwitterCallback() {
			@Override
			public void onReceivedPhoto(short status,
					ArrayList<TwitterPhoto> photos) {
				// removeTaskCounter();
				if (status == TWITTER_GETPHOTO_SUCCESS) {
					L.e("got twitter album");
					for (TwitterPhoto mPhoto : photos) {
						addPhoto(mPhoto);
					}

					showImageGrid();
				}
			}
		});
	}

	protected void getAllImage() {
		if (mFacebookAlbumManager == null) {
			mFacebookAlbumManager = new FacebookAlbumManager(mFacebookLogin);
		}
		addTaskCounter();
		mFacebookAlbumManager.getListAlbum(new GetAlbumCallback() {
			@Override
			public void onReceivedAlbum(short status,
					ArrayList<FacebookAlbum> result) {
				removeTaskCounter();

				if (status == FACEBOOK_GETALBUM_SUCCESS) {
					getPhotos(0, result);
				} else {
					L.e("Get album failed");
				}
			}
		});
	}

	private int getTaskCounter() {
		if (taskCounter >= 0)
			return taskCounter;
		return 0;
	}

	private void addTaskCounter() {
		taskCounter++;
	}

	private void removeTaskCounter() {
		taskCounter--;
		if (taskCounter <= 0)
			setSupportProgressBarIndeterminateVisibility(false);
	}

	private boolean isCompleteAllTask() {
		if (taskCounter <= 0) {
			return true;
		}
		return false;
	}

	private void initLoginHost() {
		if (FxApplication.mInstagramLogin == null) {
			FxApplication.mInstagramLogin = new InstagramLogin(this);
		}
		mInstagramLogin = FxApplication.mInstagramLogin;
		mInstagramPhotoManager = FxApplication.mInstagramPhotoManager;

		// init facebook
		if (FxApplication.mFacebookLogin == null) {
			FxApplication.mFacebookLogin = new FacebookLogin(this);
		}
		mFacebookLogin = FxApplication.mFacebookLogin;
		mFacebookAlbumManager = FxApplication.mFacebookAlbumManager;

		// Initial Twitter
		if (FxApplication.mTwitterLogin == null) {
			FxApplication.mTwitterLogin = new TwitterLogin(this);
		}
		mTwitterLogin = FxApplication.mTwitterLogin;
		mTwitterPhotoManager = FxApplication.mTwitterPhotoManager;
		new DefaultOAuthProvider("https://api.twitter.com/oauth/request_token",
				"https://api.twitter.com/oauth/access_token",
				"https://api.twitter.com/oauth/authorize");

	}

	private void initData() {
		// FxApplication.mPhotoList = new ArrayList<Photo>();
		// FxApplication.mPhotoList.add(new Photo("",
		// "http://www.defenders.org/sites/default/files/styles/large/public/dolphin-kristian-sekulic-isp.jpg"));
		// for (int i = 0; i < 100; i++) {
		// FxApplication.mPhotoList.add(new Photo("",
		// "http://www.defenders.org/sites/default/files/styles/large/public/dolphin-kristian-sekulic-isp.jpg"));
		// }

		FxApplication.mPhotoList = FxApplication.mSqliteManager.getAllData();
		for (Photo mPhoto : FxApplication.mPhotoList) {
			if (FxApplication.mFileManager.isFileExisted(mPhoto)) {
				mPhoto.setDownloadStatus(Photo.STATUS_DOWNLOADED);
			}
		}
		L.e("Get all data on first time "
				+ FxApplication.mSqliteManager.getAllData().size());
		updateDataFromSNS();
	}

	public void updateDataFromSNS() {
		if (isCompleteAllTask()) {
			setSupportProgressBarIndeterminateVisibility(true);
			isAddNewPhoto = false;
			startLoadFacebookImage();
			startLoadTwitterImage();
			startLoadInstagramImage();
			L.e("Load from server");
		} else {
			L.e("Can't load from server. Task is busy " + getTaskCounter());
		}
	}

	private void addPhoto(Photo mPhoto) {
		if (!FxApplication.mSqliteManager.isDataExisted(mPhoto)) {
			FxApplication.mPhotoList.add(mPhoto);
			String date = String.valueOf(mPhoto.getDate().getTime());
			FxApplication.mSqliteManager.createData(mPhoto.getLink(),
					mPhoto.getId(), date, String.valueOf(mPhoto.getHost()));
			L.e("Add photo. ID: " + mPhoto.getId() + " - " + mPhoto.getDate());
			isAddNewPhoto = true;
		}
	}

	protected void showImageGrid() {

		if (mImageGridFragment != null && isAddNewPhoto) {
			if (mImageGridFragment != null) {
				mImageGridFragment.refreshGrid();
				L.e("ShowGrid - update");
			} else {
				startFragment(ImageGridFragment.class);
			}
			isAddNewPhoto = false;
			L.e("Refresh grid from outside!!");
		}
	}

	public void getPhotos(final int iDex, final ArrayList<FacebookAlbum> inp) {
		addTaskCounter();
		inp.get(iDex).getListPhoto(Session.getActiveSession(),
				new GetImageCallback() {
					@Override
					public void onReceivedPhoto(short status,
							ArrayList<FacebookPhoto> result) {
						removeTaskCounter();
						if (status == FACEBOOK_GETPHOTO_SUCCESS) {
							for (FacebookPhoto mPhoto : result) {
								addPhoto(mPhoto);
							}
						}
						if (inp.size() - 1 > iDex) {
							getPhotos(iDex + 1, inp);
						} else {
							// handle when load finish
							showImageGrid();
						}
					}
				});
	}

	@Override
	protected void initView() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mMenuListTitles = getResources().getStringArray(R.array.menu_drawer);
		mDrawerList.setAdapter(new ListDrawerAdapter(this, mMenuListTitles));
		mDrawerList.setOnItemClickListener(ItemCLickLeftDrawer);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.start_menu, R.string.app_name) {

			/** Called when a drawer has settled in a completely closed state. */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
			}

			@Override
			public void onDrawerStateChanged(int newState) {
				super.onDrawerStateChanged(newState);
				if (mImageGridFragment != null) {
					mImageGridFragment.hideActionBar();
				}

			}

			/** Called when a drawer has settled in a completely open state. */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				getSupportActionBar().setTitle(getString(R.string.start_menu));
			}
		};

		// Set the drawer toggle as the DrawerListener
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	// LeftDrawer item click
	AdapterView.OnItemClickListener ItemCLickLeftDrawer = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// Close Drawer first when item selected
			mDrawerLayout.closeDrawers();

			// Start feature
			switch (position) {
			case 0:
				startFragment(ImageGridFragment.class);
				break;
			case 1:
				startFragment(AccountFragment.class);
				break;
			case 2:
				startFragment(OptionFragment.class);
				break;
			case 3:
				startFragment(AboutUsFragment.class);
				break;
			case 4:
				showExitConfirmDialog();
			default:
				break;
			}

		}
	};

	private void replaceFragment(FxBaseFragment fragment) {
		String backStateName = fragment.getClass().getName();

		FragmentManager manager = getSupportFragmentManager();
		boolean fragmentPopped = manager
				.popBackStackImmediate(backStateName, 0);

		if (!fragmentPopped) { // fragment not in back stack, create it.
			FragmentTransaction ft = manager.beginTransaction();
			ft.setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out);
			ft.replace(R.id.content_frame, fragment, fragment.getFxTag());
			ft.addToBackStack(backStateName);
			ft.commit();
		}
	}

	public String getActiveFragment() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			return null;
		}
		return getSupportFragmentManager().getBackStackEntryAt(
				getSupportFragmentManager().getBackStackEntryCount() - 1)
				.getName();
		// return (FxBaseFragment)
		// getSupportFragmentManager().findFragmentByTag(
		// tag);
	}

	private boolean isFragmentActive(FxBaseFragment fragment) {
		String currentActiveFragment = getActiveFragment();
		if (currentActiveFragment != null && fragment != null) {
			return currentActiveFragment.equals(fragment.getClass().getName());
		}
		return false;
	}

	private void startFragment(Class<?> cls) {
		FxBaseFragment fragment = null;
		boolean canCreateNewFragment = (getActiveFragment() == null);
		if (cls.equals(ImageGridFragment.class)) {
			if (isFragmentActive(mImageGridFragment)) {
				return;
			} else if (mImageGridFragment == null || canCreateNewFragment) {
				mImageGridFragment = new ImageGridFragment();
			}
			fragment = mImageGridFragment;
		} else if (cls.equals(AccountFragment.class)) {
			if (isFragmentActive(mAccountFragment)) {
				return;
			} else if (mAccountFragment == null || canCreateNewFragment) {
				mAccountFragment = new AccountFragment();
			}
			fragment = mAccountFragment;
		} else if (cls.equals(OptionFragment.class)) {
			if (isFragmentActive(mOptionFragment)) {
				return;
			} else if (mOptionFragment == null || canCreateNewFragment) {
				mOptionFragment = new OptionFragment();
			}
			fragment = mOptionFragment;
		} else if (cls.equals(AboutUsFragment.class)) {
			if (isFragmentActive(mAboutUsFragment)) {
				return;
			} else if (mAboutUsFragment == null || canCreateNewFragment) {
				mAboutUsFragment = new AboutUsFragment();
			}
			fragment = mAboutUsFragment;
		}

		if (fragment != null) {
			replaceFragment(fragment);
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerToggle != null) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void finishActivity() {
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent arg2) {
		if (mImageGridFragment != null) {
			mImageGridFragment.onActivityResult(requestCode, resultCode, arg2);
		}
		super.onActivityResult(requestCode, resultCode, arg2);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mTwitterLogin != null
				&& !mTwitterLogin.isLoggedIn()
				&& FxApplication.mPreferenceManager
						.getBoolean(PreferenceManager.SHOW_CONFIRM_TWITTER)) {
			mTwitterLogin = new TwitterLogin(this);
			FxApplication.mPreferenceManager.putBoolean(
					PreferenceManager.SHOW_CONFIRM_TWITTER, false);
			mTwitterLogin.logIn(new TwitterLoginCallback() {

				@Override
				public void onLogin(int result, String mess) {
					if (result == TWITTER_LOGIN_SUCCESS) {
						FxApplication.mPreferenceManager.putBoolean(
								PreferenceManager.SHOW_ACCOUNT_FRAGMENT, true);
						Intent LaunchIntent = getPackageManager()
								.getLaunchIntentForPackage(
										"com.dolphin.android.imgdownloader");
						// LaunchIntent.setFlags(0);
						// LaunchIntent.setPackage(null);
						startActivity(LaunchIntent);
						finishActivity();
					}
				}
			});
		}
		if (FxApplication.mPreferenceManager
				.getBoolean(PreferenceManager.SHOW_ACCOUNT_FRAGMENT)) {
			FxApplication.mPreferenceManager.putBoolean(
					PreferenceManager.SHOW_ACCOUNT_FRAGMENT, false);
			L.e(String.valueOf(FxApplication.mPreferenceManager
					.getBoolean(PreferenceManager.SHOW_ACCOUNT_FRAGMENT)));
			startFragment(AccountFragment.class);

		}

	}

	@SuppressLint("Recycle")
	private void removeFragment(Class<?> cls) {
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction ft = manager.beginTransaction();
		if (cls.equals(ImageGridFragment.class)) {
			if (mImageGridFragment != null) {
				ft.remove(mImageGridFragment);
			}
			mImageGridFragment = null;
		} else if (cls.equals(AccountFragment.class)) {
			if (mAccountFragment != null) {
				ft.remove(mAccountFragment);
			}
			mAccountFragment = null;
		} else if (cls.equals(OptionFragment.class)) {
			if (mOptionFragment != null) {
				ft.remove(mOptionFragment);
			}
			mOptionFragment = null;
		} else if (cls.equals(AboutUsFragment.class)) {
			if (mAboutUsFragment != null) {
				ft.remove(mAboutUsFragment);
			}
			mAboutUsFragment = null;
		}
	}

	public void killAccountFragment() {
		removeFragment(AccountFragment.class);
	}

	public void killImageGridFragment() {
		removeFragment(ImageGridFragment.class);
	}

	private void showExitConfirmDialog() {
		AlertDialog.Builder alertBulder = new Builder(this);
		alertBulder.setMessage(R.string.start_exit_confirm_title)
				.setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finishActivity();
					}
				}).setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		AlertDialog mDialog = alertBulder.create();
		mDialog.show();
	}

	public boolean isDrawerOpened() {
		try {
			return mDrawerLayout.isDrawerOpen(mDrawerList);
		} catch (NullPointerException e) {
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		String currentActiveFragment = getActiveFragment();
		if (getSupportFragmentManager().getBackStackEntryCount() >= 1
				&& isFragmentActive(mImageGridFragment) && !isDrawerOpened()) {
			if (mImageGridFragment != null
					&& mImageGridFragment.isBottomActionbarShowed()) {
				mImageGridFragment.hideActionBar();
				return;
			}
			showExitConfirmDialog();
			return;
		} else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			startFragment(ImageGridFragment.class);
		}

		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		FxApplication.mSqliteManager.close();
	}

	@Override
	public void onSelectDirectory(String path) {
		FxApplication.mPreferenceManager.putString(
				PreferenceManager.DOWNLOAD_PATH, path);
		if (mOptionFragment != null) {
			mOptionFragment.dismissFolderPicker();
		}
	}

	@Override
	public void onCancelChooser() {
		if (mOptionFragment != null) {
			mOptionFragment.dismissFolderPicker();
		}
	}
}