package com.dolphin.android.imgdownloader.storage;

import java.sql.Date;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dolphin.android.imgdownloader.utils.L;

import dolphin.android.sdk.album.AppConstants;
import dolphin.android.sdk.album.model.Photo;

public class SqliteManager implements AppConstants {
	/* Tên database */
	private static final String DATABASE_NAME = "DB_USER";

	/* Version database */
	private static final int DATABASE_VERSION = 1;

	/* Tên tabel và các column trong database */
	private static final String TABLE_IMAGE_DATA = "IMAGE";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_LINK = "link";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_HOST = "host";

	String[] columns = new String[] { COLUMN_ID, COLUMN_LINK, COLUMN_NAME,
			COLUMN_DATE, COLUMN_HOST };

	private static Context context;
	static SQLiteDatabase db;
	private OpenHelper openHelper;

	public SqliteManager(Context c) {
		SqliteManager.context = c;
	}

	public SqliteManager open() throws SQLException {
		openHelper = new OpenHelper(context);
		db = openHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		openHelper.close();
	}

	public void reOpen() {
		if (!db.isOpen()) {
			openHelper = new OpenHelper(context);
			db = openHelper.getWritableDatabase();
		}
	}

	public long createData(String link, String name, String date, String host) {
		reOpen();
		ContentValues cv = new ContentValues();
		cv.put(COLUMN_LINK, link == null ? "" : link);
		cv.put(COLUMN_NAME, name == null ? "" : name);
		cv.put(COLUMN_DATE, date == null ? "" : date);
		cv.put(COLUMN_HOST, host == null ? "" : host);
		return db.insert(TABLE_IMAGE_DATA, null, cv);
	}

	public boolean isDataExisted(Photo mPhoto) {
		reOpen();
		Cursor c = db.query(TABLE_IMAGE_DATA, columns, COLUMN_LINK + "=?",
				new String[] { mPhoto.getLink() }, null, null, null, null);
		boolean result = false;

		if (c != null && c.getCount() > 0) {
			result = true;
		}
		c.close();
		return result;
	}

	// ---deletes a particular title---
	public boolean deleteById(String name) {
		return db.delete(TABLE_IMAGE_DATA, COLUMN_NAME + "=" + name, null) > 0;
	}

	public boolean deleteByLink(String name) {
		return db.delete(TABLE_IMAGE_DATA, COLUMN_LINK + "=" + name, null) > 0;
	}

	public boolean deleteByHost(int host) {
		return db.delete(TABLE_IMAGE_DATA, COLUMN_HOST + "=" + host, null) > 0;
	}

	public ArrayList<Photo> getAllData() {
		reOpen();
		ArrayList<Photo> mPhoto = new ArrayList<Photo>();
		Cursor c = db.query(TABLE_IMAGE_DATA, columns, null, null, null, null,
				null);
		int iRow = c.getColumnIndex(COLUMN_ID);
		int iLink = c.getColumnIndex(COLUMN_LINK);
		int iName = c.getColumnIndex(COLUMN_NAME);
		int iDate = c.getColumnIndex(COLUMN_DATE);
		int iHost = c.getColumnIndex(COLUMN_HOST);

		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			Photo mNewPhoto = new Photo("", c.getString(iLink));
			mNewPhoto.setHost(Integer.parseInt(c.getString(iHost)));
			mNewPhoto.setId(c.getString(iName));
			String sDate = c.getString(iDate);
			Date mDate = new Date(Long.parseLong(sDate));
			mNewPhoto.setDate(mDate);
			mPhoto.add(mNewPhoto);
		}
		c.close();
		return mPhoto;
	}

	// ---------------- class OpenHelper ------------------
	private static class OpenHelper extends SQLiteOpenHelper {

		public OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase arg0) {
			arg0.execSQL("CREATE TABLE " + TABLE_IMAGE_DATA + " (" + COLUMN_ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_LINK
					+ " TEXT NOT NULL, " + COLUMN_NAME + " TEXT NOT NULL, "
					+ COLUMN_DATE + " TEXT NOT NULL, " + COLUMN_HOST
					+ " TEXT NOT NULL);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
			arg0.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGE_DATA);
			onCreate(arg0);
		}
	}
}