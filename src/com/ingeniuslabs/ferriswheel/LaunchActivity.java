package com.ingeniuslabs.ferriswheel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Launch Activity with launch image
 * @author Xiao
 *
 */
public class LaunchActivity extends Activity {

	private Handler mHandler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launch);		
		
		/**
		 * use ui handler to post a 1500 milliseconds delay activity change
		 */
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Intent intent = new Intent(LaunchActivity.this, MainActivity.class);
				/**
				 * new task should be assigned to MainActivity, since we can not get back to launch activity 
				 */
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				overridePendingTransition(R.anim.loadin, R.anim.loadout);
				finish();
			}
		}, 1500);
	}
}
