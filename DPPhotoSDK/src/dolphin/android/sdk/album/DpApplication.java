package dolphin.android.sdk.album;

import dolphin.android.sdk.album.utils.LayoutUtil;
import dolphin.android.sdk.album.utils.T;
import android.app.Application;

public class DpApplication extends Application {
	public void onCreate() {
		super.onCreate();
		T.init(getApplicationContext());
		LayoutUtil.init(getApplicationContext());
	}
}
