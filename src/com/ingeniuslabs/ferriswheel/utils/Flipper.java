package com.ingeniuslabs.ferriswheel.utils;

import com.ingeniuslabs.ferriswheel.R;

import android.content.Context;
import android.graphics.Bitmap;

public class Flipper {
	private Bitmap[] flippers;
	private int currentIndex;
	private Context mContext;

	public Flipper(Context context, int width, int height) {
		mContext = context;
		flippers = new Bitmap[2];
		flippers[0] = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_4444);
		flippers[1] = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_4444);
		currentIndex = 0;
	}

	public Bitmap getNewBm() {
		flippers[currentIndex%2].eraseColor(mContext.getResources().getColor(
				R.color.main_bg_color));
		currentIndex++;
		return flippers[currentIndex % 2];
	}

	public void release() {
		flippers[0].recycle();
		flippers[0] = null;

		flippers[1].recycle();
		flippers[1] = null;
	}
}
