package com.dolphin.android.imgdownloader.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.os.AsyncTask;
import dolphin.android.sdk.album.model.Photo;

public class FileManager {
	private String folderPath;
	private File f;
	public static final int FILE_DELETE_COMPLETE = 11;
	public static final int FILE_DELETE_FAILED = 12;
	public static final int FILE_DELETE_COMPLETE_ALL = 13;

	public FileManager(String folderPath) {
		this.folderPath = folderPath;
		f = new File(this.folderPath);
		if (!f.exists())
			f.mkdirs();
	}

	public boolean isFileExisted(final Photo mPhoto) {
		File[] result = f.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.lastIndexOf('.') > 0
						&& name.substring(0, name.lastIndexOf('.')).equals(
								mPhoto.getId())) {
					return true;

				}
				return false;
			}
		});
		if (result != null && result.length > 0)
			return true;
		return false;
	}

	public void deleteFile(final String path, final ArrayList<Photo> mPhoto,
			final FileCallback mCallback) {
		deleteFiles(0, path, mPhoto, mCallback);
	}

	public void deleteFiles(final int iDex, final String path,
			final ArrayList<Photo> mPhoto, final FileCallback mCallback) {
		(new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				File file = new File(path + "/" + mPhoto.get(iDex).getId()
						+ ".jpg");
				return file.delete();
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (mCallback != null)
					mCallback.onDelete(result ? FILE_DELETE_COMPLETE
							: FILE_DELETE_FAILED, mPhoto.get(iDex).getLink());
				if (iDex < mPhoto.size() - 1) {
					deleteFiles(iDex + 1, path, mPhoto, mCallback);
				} else {
					mCallback.onDelete(FILE_DELETE_COMPLETE_ALL,
							mPhoto.get(iDex).getLink());
				}
			}
		}).execute();
	}

	public void deleteFile(final String path, final Photo mPhoto,
			final FileCallback mCallback) {
		(new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				File file = new File(path + "/" + mPhoto.getId() + ".jpg");
				return file.delete();
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (mCallback != null)
					mCallback.onDelete(result ? FILE_DELETE_COMPLETE
							: FILE_DELETE_FAILED, mPhoto.getLink());

			}
		}).execute();
	}

	public interface FileCallback {
		public void onDelete(int status, String uri);
	}
	// public String getAllFile() {
	// String path = Environment.getExternalStorageDirectory().toString()
	// + "/Pictures";
	// File f = new File(path);
	// ArrayList<File> mResult = new ArrayList<File>();
	// f. q q
	// return mResult;
	// }

}
