package com.dolphin.android.imgdownloader.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.widget.RemoteViews;

import com.dolphin.android.imgdownloader.R;

public class ProgressNotifier {
	private static NotificationManager nm;
	private static Notification noti;
	private final static int STATUS_BAR_NOTIFICATION = 1;
	static RemoteViews contentView;
	static Builder mBuilder;
	private Context mContext;

	public ProgressNotifier(Context context) {
		mContext = context;
	}

	@SuppressWarnings("deprecation")
	public void InitialNotification() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			nm = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mBuilder = new NotificationCompat.Builder(mContext);
			mBuilder.setContentTitle(mContext.getString(R.string.app_name))
					.setContentText(mContext.getString(R.string.notify_download))
					.setSmallIcon(R.drawable.ic_launcher);
		} else {
			nm = (NotificationManager) mContext
					.getSystemService(Context.NOTIFICATION_SERVICE);
			long when = System.currentTimeMillis();
			noti = new Notification(R.drawable.ic_launcher,
					mContext.getString(R.string.app_name), when);

			Intent notiIntent = new Intent();
			PendingIntent pi = PendingIntent.getService(mContext, 0,
					notiIntent, 0);
			noti.flags = Notification.FLAG_AUTO_CANCEL;

			contentView = new RemoteViews(mContext.getPackageName(),
					R.layout.noti);
			contentView.setImageViewResource(R.id.status_icon,
					R.drawable.ic_launcher);
			noti.contentView = contentView;
			noti.contentIntent = pi;
		}
	}

	static int currentPercent;

	public void setPercent(int percent) {
		// if ((currentPercent + 1) < percent)
		// {
		// CharSequence title = "Uploading: " + percent + "%";
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mBuilder.setProgress(100, percent, false);
			nm.notify(0, mBuilder.build());
		} else {
			noti.contentView.setTextViewText(R.id.status_text,
					mContext.getString(R.string.test_string));
			noti.contentView.setProgressBar(R.id.status_progress, 100, percent,
					false);
			nm.notify(STATUS_BAR_NOTIFICATION, noti);
			currentPercent = percent;
		}
		// }
	}

	public void hideProgressBar() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			mBuilder.setContentText(mContext.getString(R.string.test_string))
					.setProgress(0, 0, false);
			nm.notify(0, mBuilder.build());
			nm.cancel(0);
		} else {
			noti.contentView.setTextViewText(R.id.status_text,
					mContext.getString(R.string.test_string));
			noti.contentView.setProgressBar(R.id.status_progress, 100, 100,
					false);
			nm.notify(STATUS_BAR_NOTIFICATION, noti);
			nm.cancelAll();
		}
	}
}
