package com.ingeniuslabs.ferriswheel.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ingeniuslabs.ferriswheel.R;
import com.ingeniuslabs.ferriswheel.utils.Flipper;
import com.ingeniuslabs.ferriswheel.utils.Util;

/**
 * FerrisWheelView for 
 * @author Xiao
 *
 */
public class FerrisWheelView extends View {
	private static final String TAG = "FerrisWheelView";
	
	private static final int MSG_INVALIDATE = 1;
	
	/**
	 * invalid angle
	 * in case user press the center point of the ferris wheel, the angle is invalid
	 */
	private static final double INVALID_ANGLE = 50 * Math.PI;
	
	
	/** 
	 * sound pool for rotation
	 */
	private SoundPool pool;
	private int streamId;
	
	/**
	 * track list of observers
	 */
	private List<FerrisWheelObserver> observers;
	
	private Context mContext;
	private Resources mResources;
	/**
	 * local canvas used to draw bitmap before post it on to screen canvas
	 */
	private Canvas mCanvas;
	
	/**
	 * screen width and height
	 */
	private int screenWidth;
	private int screenHeight;
	private int squareWidth;
	
	/**
	 * the inner Axle length and width
	 */
	private double innerAxleLength = 0;
	private double innerAxleWidth = 0;
	
	/**
	 * the outer Axle length and width
	 */
	private double outerAxleLength = 0;
	private double outerAxleWidth = 0;
	
	/**
	 * three angles
	 * finger start angle is used to track the angle the first time ACTION_DOWN occurs
	 * finger old angle is used to track the last time angle of ontouch event
	 * finger current angle is used to track the current angle of ontouch event
	 */
	private double fingerStartAngle = 0;
	private double fingerOldAngle = 0;
	private double fingerCurrentAngle = 0;
	
	/**
	 * touch down timemillis
	 */
	private long downTime;
	
	/**
	 * touch up timemillis
	 */
	private long upTime;
	
	/**
	 * max duration for single tap
	 * if (@upTime - @downTime  < @SINGLE_TAP_MAX_DURATION) indicates that it is a single tap event
	 */
	private static final int SINGLE_TAP_MAX_DURATION = 200;
	
	/**
	 * max angle duration for single tap
	 * if (@fingerCurrentAngle - @fingerOldAngle < @SINGLE_TAP_MAX_SCROLL_ANGLE) indicates that it is a single tap event
	 */
	private static final double SINGLE_TAP_MAX_SCROLL_ANGLE = Math.PI / 90;
	
	/**
	 * the angle need to rotate alone for single tap
	 */
	private double needRotateAngleAlone = 0;
	
	/**
	 * the angle need to rotate for single tap
	 */
	private double needRotateAngle = 0;
	
	/**
	 * single tap move every time
	 */
	private static final double SINGLE_TAP_MOVE_PER_TIME = Math.PI / 27;
	
	/**
	 * radius params for background circles
	 */
	private float[] radiuses = new float[] { 0.8130841121495327f,
			0.7850467289719626f, 0.691588785046729f, 0.6635514018691589f,
			0.1214953271028037f, 0.0654205607476636f, 0.0467289719626168f };

	/**
	 * colors params for background circles
	 */
	private int[] colors = new int[] { R.color.round1_color,
			R.color.main_bg_color, R.color.round2_color, R.color.main_bg_color,
			R.color.round3_color, R.color.round4_color, R.color.main_bg_color };

	/**
	 * bg_bm is the background bitmap
	 */
	private Bitmap bg_bm = null;
	private Bitmap axle_bm;
	private double axle_aspect_ratio;
	private Paint mPaint;
	private Handler uiHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_INVALIDATE:
				invalidate();
				break;
			}
		};
	};
	
	/**
	 * bitmap flipper
	 */
	private Flipper mFlipper;

	@SuppressWarnings("deprecation")
	public FerrisWheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		mResources = context.getResources();
		observers = new ArrayList<FerrisWheelView.FerrisWheelObserver>();
		mCanvas = new Canvas();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		updateSize();
		mFlipper = new Flipper(context, squareWidth, squareWidth);
		createBgBitmap();
		setBackground(new BitmapDrawable(bg_bm));
		notifyObservers(0);
		prepareSoundPlayer();
	}
	
	/**
	 * prepare the sound player for rotation 
	 */
	private void prepareSoundPlayer() {
		try {
			pool = new SoundPool(1,  AudioManager.STREAM_MUSIC, 0);
			streamId = pool.load(mContext, R.raw.sound, 1);
		} catch (Exception e) {
			e.printStackTrace();
			pool = null;
		}
	}
	
	/**
	 * add Observer to current observer list
	 * @param observer
	 */
	public void addObserver(FerrisWheelObserver observer) {
		if (observer != null && !observers.contains(observer))
			observers.add(observer);
	}
	
	/**
	 * clear all the observers
	 */
	public void clearObserver() {
		observers.clear();
	}

	/**
	 * onMeasure here used to resize the size of ferris wheel view
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		updateSize();
		setMeasuredDimension(squareWidth, squareWidth);
	}

	/**
	 * update the size for ferris wheel view
	 */
	private void updateSize() {
		screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		screenHeight = mContext.getResources().getDisplayMetrics().heightPixels;
		squareWidth = Math.min(screenHeight, screenWidth);
	}

	/**
	 * create the ferris wheel view background bitmap
	 * 1, create a bitmap with alpha channel
	 * 2, draw circles of ferris wheel with radius and colors
	 * 3, calculate the axle length and with
	 * 4, draw the axles
	 */
	private void createBgBitmap() {
		bg_bm = Bitmap.createBitmap(squareWidth, squareWidth,
				Bitmap.Config.ARGB_4444);
		mCanvas = new Canvas(bg_bm);
		mCanvas.clipRect(0, 0, squareWidth, squareWidth);
		mCanvas.drawColor(mContext.getResources().getColor(R.color.main_bg_color));
		int circlenum = Math.min(radiuses.length, colors.length);
		for (int i = 0; i < circlenum; i++) {
			mPaint.setColor(getResources().getColor(colors[i]));
			mCanvas.drawCircle(squareWidth / 2, squareWidth / 2, radiuses[i]
					* squareWidth / 2, mPaint);
		}
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
        axle_bm = BitmapFactory.decodeResource(mResources, R.drawable.axle, opts);
        axle_aspect_ratio = (double)opts.outWidth / (double)opts.outHeight;
        
		innerAxleLength = (radiuses[3] - radiuses[4]) * squareWidth / 2;
		innerAxleWidth = innerAxleLength * axle_aspect_ratio;
		
		outerAxleLength = (radiuses[1] - radiuses[3]) * squareWidth / 2;
		outerAxleWidth = (radiuses[0] - radiuses[1]) * squareWidth / 2;
		
		mPaint.setStrokeWidth((float)outerAxleWidth);
		mPaint.setColor(getResources().getColor(colors[0]));
		double innerbottom = (1 - radiuses[4]) * squareWidth / 2;
		double outerbottom = (1 - radiuses[3]) * squareWidth / 2;

		// TODO the rotate doesn't work. I am trying to figure out what's wrong
		for (double rotateangle = 0.0; rotateangle < Math.PI * 2; rotateangle += Math.PI / 6) {
			mCanvas.drawBitmap(axle_bm, null, new RectF(
					(float) ((squareWidth - innerAxleWidth) / 2),
					(float) (innerbottom - innerAxleLength),
					(float) ((squareWidth + innerAxleWidth) / 2), (float) innerbottom),
					null);
			mCanvas.drawLine((float) ((squareWidth - outerAxleWidth) / 2),
					(float) (outerbottom - outerAxleLength),
					(float) ((squareWidth - outerAxleWidth) / 2),
					(float) outerbottom, mPaint);
			mCanvas.rotate(30, squareWidth / 2, squareWidth / 2);
		}
	}

	/**
	 * override onDraw
	 * 1, try to rotate the circles of ferris wheel 
	 * 2, if it needs to rotate alone, try to calculate the menus' increase angle and call invalidate
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (needRotateAngle != 0) {
			Bitmap bm = mFlipper.getNewBm();
			mCanvas.setBitmap(bm);
			canvas.clipRect(0, 0, squareWidth, squareWidth);
			mCanvas.drawBitmap(bg_bm, 0, 0, mPaint);
			mCanvas.rotate((float)Math.toDegrees(needRotateAngle),
					squareWidth / 2, squareWidth / 2);
			canvas.drawColor(mContext.getResources().getColor(R.color.main_bg_color));
			canvas.drawBitmap(bm, 0, 0, null);
			needRotateAngle = 0;
	    }
		if (needRotateAngleAlone != 0) {
			if(needRotateAngleAlone > 0) {
				if (needRotateAngleAlone > SINGLE_TAP_MOVE_PER_TIME) {
					notifyObservers(SINGLE_TAP_MOVE_PER_TIME);					
					needRotateAngleAlone -= SINGLE_TAP_MOVE_PER_TIME;
				} else {
					notifyObservers(needRotateAngleAlone);
					needRotateAngleAlone =0;
					uiHandler.postDelayed(gotoContent, 500);
				}
			} else {
				if (-needRotateAngleAlone > SINGLE_TAP_MOVE_PER_TIME) {
					notifyObservers(-SINGLE_TAP_MOVE_PER_TIME);
					needRotateAngleAlone += SINGLE_TAP_MOVE_PER_TIME;
				} else {
					notifyObservers(needRotateAngleAlone);
					needRotateAngleAlone = 0;
					uiHandler.postDelayed(gotoContent, 500);
				}
			}
		}
	}
	
	/**
	 * once the automatically rotate has finished we should load the content with an animation 
	 */
	private Runnable gotoContent = new Runnable() {
		@Override
		public void run() {
			if(observers.size() > 0) {
				observers.get(0).gotoContent();
			}
		}
	};
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/**
		 * get current touch X and Y
		 */
		double currentX1 = event.getX(0);
		double currentY1 = event.getY(0);
		
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/**
			 * When user press down, start playing the music
			 */
			if (pool != null) {
				pool.play(streamId, 1.0f, 1.0f, 0, -1, 1.0f);
			}
			/**
			 * once user pressed, the automatically rotate should be interrupted and stopped
			 */
			needRotateAngleAlone = 0;
			
			/**
			 * calculate the finger start angle and finger old angle
			 */
			fingerOldAngle = fingerStartAngle = calculateAngle(currentX1, currentY1);
			/**
			 * record down time for single tap
			 */
			downTime = System.currentTimeMillis();
			break;
		case MotionEvent.ACTION_MOVE:
			/**
			 * if finger old angle is valid, try to update the menus' angle.
			 * otherwise just record the current angle as the old angle;
			 */
			fingerCurrentAngle = calculateAngle(currentX1, currentY1);
			if (fingerOldAngle != INVALID_ANGLE) {
				notifyObservers(fingerCurrentAngle - fingerOldAngle);
			}
			fingerOldAngle = fingerCurrentAngle;
			break;
		case MotionEvent.ACTION_UP:
			/**
			 * once user release press, we should stop playing the music
			 */
			if (pool != null) {
				pool.autoPause();
			}
			fingerCurrentAngle = calculateAngle(currentX1, currentY1);
			/**
			 * record uptime for single tap
			 */
			upTime = System.currentTimeMillis();
			/**
			 * if uptime - downtime is less than the single tap max duration and the finger angle move is less than the single tap max angle
			 * we should consider this event as a single tap event and do rotate automatically
			 */
			if (upTime - downTime < SINGLE_TAP_MAX_DURATION && Math.abs(fingerCurrentAngle - fingerStartAngle) < SINGLE_TAP_MAX_SCROLL_ANGLE) {
				if (observers.size() > 0) {
					needRotateAngleAlone = observers.get(0).rotateToTop(
							currentX1 - (squareWidth / 2 * (1 - radiuses[0])),
							currentY1 - (squareWidth / 2 * (1 - radiuses[0])));
					if (needRotateAngleAlone == -1) {
						uiHandler.post(gotoContent);
					} else {
						uiHandler.removeMessages(MSG_INVALIDATE);
						uiHandler.sendEmptyMessage(MSG_INVALIDATE);
					}
				}
			} else if (fingerOldAngle != INVALID_ANGLE) {
				notifyObservers(fingerCurrentAngle - fingerOldAngle);
			}
			/**
			 * set finger old angle and start angle to INVALID;
			 */
			fingerOldAngle = INVALID_ANGLE;
			fingerStartAngle = INVALID_ANGLE;
			break;
		default:
			break;
		}
		return true;
	}
	
	/**
	 * calculate the angle according to current x and y position
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return
	 */
	private double calculateAngle(double x, double y) {
		double XLength = Math.abs((double)x - squareWidth /2);
		double YLength = Math.abs((double)y - squareWidth / 2);
		double centerLenght = Math.sqrt(XLength * XLength + YLength * YLength);
		double angle = Math.asin(YLength / centerLenght);
		if (y < squareWidth/2 && x < squareWidth/2) {
			angle = angle;
		} else if (y < squareWidth/2 && x > squareWidth/2) {
			angle = (Math.PI - angle);
		} else if (y > squareWidth/2 && x > squareWidth/2) {
			angle = (Math.PI + angle);
		} else if (y > squareWidth/2 && x < squareWidth/2) {
			angle = (2 * Math.PI - angle);
		} else if (y == squareWidth/2 && x < squareWidth/2) {
			angle = 0;
		} else if (y == squareWidth/2 && x > squareWidth/2) {
			angle = Math.PI;
		} else if (y < squareWidth/2 && x == squareWidth/2) {
			angle = Math.PI / 2;
		} else if (y > squareWidth/2 && x == squareWidth/2) {
			angle = 3 * Math.PI / 2;
		} else {
			angle = INVALID_ANGLE;
		}
		return angle;
	}

	/**
	 * notify all the observers with the increase angle to update their menus positions
	 * and try to rotate the ferris wheel circles itself
	 * @param increaseAngle
	 */
	private void notifyObservers(double increaseAngle) {
		needRotateAngle += increaseAngle;
		for (FerrisWheelObserver observer : observers) {
			observer.notify(increaseAngle);
		}
		uiHandler.removeMessages(MSG_INVALIDATE);
		uiHandler.sendEmptyMessage(MSG_INVALIDATE);
	}

	/**
	 * test method 
	 * @param bm
	 * @param filepath
	 */
	public static void saveBmToString(Bitmap bm, String filepath) {
		if (bm == null || TextUtils.isEmpty(filepath))
			return;
		File f = new File(filepath);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream fos = null;
		try {
			f.createNewFile();
			fos = new FileOutputStream(f);
			bm.compress(Bitmap.CompressFormat.PNG, 1, fos);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null)
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * interface for FerrisWheel observer
	 * @author Xiao
	 *
	 */
	public static interface FerrisWheelObserver {
		public void notify(double angle);
		
		public double rotateToTop(double x, double y);
		
		public void gotoContent();
	}
	
	/**
	 * release sound player
	 */
	public void releaseSoundPool() {
		if (pool != null) {
			pool.release();
			pool = null;
		}
	}
}
