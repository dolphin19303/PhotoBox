package com.dolphin.android.imgdownloader.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

public class GeneralLayout {

	private static Context mContext;
	private static DisplayMetrics displayMetrics;
	private static float radio;

	private static float maxWidth = 320;
	private static float baseWidth = 480;
	private static float radioWidthDp = 1.0f;

	public static void init(final Context pActivity) {
		mContext = pActivity;
		displayMetrics = mContext.getResources().getDisplayMetrics();
		radio = displayMetrics.densityDpi / 160f;
		final int w = (int) ((float) displayMetrics.widthPixels / displayMetrics.density);
		if (w >= 720) {
			maxWidth = 720;
		} else if (w >= 600) {
			maxWidth = 600;
		} else if (w >= 480) {
			maxWidth = 480;
		} else if (w >= 360) {
			maxWidth = 360;
		} else if (w >= 320) {
			maxWidth = 320;
		} else if (w >= 240) {
			maxWidth = 240;
		}
		radioWidthDp = baseWidth / maxWidth;
	}

	public static DisplayMetrics getDisplay() {
		return displayMetrics;
	}

	public static void resizeView(final View pView) {
		final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) pView
				.getLayoutParams();
		layoutParams.width = (int) convertDpToPixel(layoutParams.width);
		layoutParams.height = (int) convertDpToPixel(layoutParams.height);
		layoutParams.topMargin = (int) convertDpToPixel(layoutParams.topMargin);
		layoutParams.leftMargin = (int) convertDpToPixel(layoutParams.leftMargin);
		pView.setLayoutParams(layoutParams);
	}

	public static void resizeView(final View pView, float width, float height,
			float top, float left) {
		final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) pView
				.getLayoutParams();
		layoutParams.width = (int) convertDpToPixel(width);
		layoutParams.height = (int) convertDpToPixel(height);
		layoutParams.topMargin = (int) convertDpToPixel(top);
		layoutParams.leftMargin = (int) convertDpToPixel(left);
		pView.setLayoutParams(layoutParams);
	}

	public static float convertDpToPixel(float dp) {
		float px = (dp / radioWidthDp) * radio;
		return px;
	}

	public float getScaleValue(float sourceWidth) {
		float scale = 1.0f;
		scale = convertDpToPixel(sourceWidth) / sourceWidth;
		return scale;
	}

	public static int getLayout(int pArrayLayout) {
		int index = 0;
		switch ((int) maxWidth) {
		case 720:
			index = 5;
			break;
		case 600:
			index = 4;
			break;
		case 480:
			index = 3;
			break;
		case 360:
			index = 2;
			break;
		case 320:
			index = 1;
			break;
		case 240:
			index = 0;
			break;
		}
		TypedArray typeArray = mContext.getResources().obtainTypedArray(
				pArrayLayout);
		if (typeArray.length() == 1) {
			index = 0;
		}
		int resource = typeArray.getResourceId(index, 0);
		typeArray.recycle();
		return resource;

	}

	public float getScaleScreen() {
		float index = 1.0f;
		switch ((int) maxWidth) {
		case 720:
			index = 2.25F;
			break;
		case 600:
			index = 1.875F;
			break;
		case 480:
			index = 1.5F;
			break;
		case 360:
			index = 1.125F;
			break;
		case 320:
			index = 1.0F;
			break;
		case 240:
			index = 0.75F;
			break;
		}
		return index;
	}

	public void destroy() {
		mContext = null;
		displayMetrics = null;
	}
}