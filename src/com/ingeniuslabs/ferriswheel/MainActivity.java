package com.ingeniuslabs.ferriswheel;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.ingeniuslabs.ferriswheel.model.MenuItem;
import com.ingeniuslabs.ferriswheel.ui.FerrisWheelView;
import com.ingeniuslabs.ferriswheel.ui.FerrisWheelView.FerrisWheelObserver;

/**
 * Main activity for the ferris wheel
 * 
 * @author Xiao
 * 
 */
public class MainActivity extends Activity implements FerrisWheelObserver {

	private static final String TAG = "MainActivity";

	/**
	 * diameter rate of the ferris wheel
	 */
	public static final float DIAMETER_OF_MENU_ITEM = 0.8130841121495327f;
	/**
	 * menu item width rate
	 */
	public static final float MENU_ITEM_WIDTH = 0.1775700934579439f;
	/**
	 * menu image height percentage
	 */
	public static final float MENU_IMAGE_HEIGHT_RATE = 0.75f;
	
	/**
	 * radius of the ferris wheel
	 */
	private float radius;

	/**
	 * screen width and height;
	 */
	private int screenWidth;
	private int screenHeight;

	/**
	 * UI components
	 */
	private RelativeLayout rl_root;
	private FerrisWheelView fw_view;
	private CheckBox cb_rotate_before_content;
	private View topView = null;
	private RelativeLayout contentView;
	private RelativeLayout rl_title;
	private Button btn_back;
	private ImageView iv_title;

	/**
	 * menuitem array track of menuitems
	 */
	private MenuItem[] default_menus;
	/**
	 * menuitem view list
	 */
	private List<View> default_menu_views = new ArrayList<View>();
	/**
	 * menu width and height;
	 */
	private int menuitemHeight;
	private int menuitemWidth;

	/**
	 * inflater for loading xml file into view
	 */
	private LayoutInflater mInflater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**
		 * calculate menuitem's width and height and radius of the round
		 */
		screenWidth = getResources().getDisplayMetrics().widthPixels;
		screenHeight = getResources().getDisplayMetrics().heightPixels;
		menuitemHeight = menuitemWidth = (int) ((float) Math.min(screenHeight,
				screenWidth) * MENU_ITEM_WIDTH);
		radius = (float) Math.min(screenHeight, screenWidth)
				* DIAMETER_OF_MENU_ITEM / 2f;
		setContentView(R.layout.activity_main);
		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		findViews();
		setListeners();
	}

	/**
	 * load default 12 menu items with resources
	 */
	private void loadDefaultMenus() {
		default_menus = new MenuItem[] {
				new MenuItem(this, R.drawable.menu1, R.string.menu1_label, 0,
						radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu2, R.string.menu2_label,
						Math.PI / 6, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu3, R.string.menu3_label,
						Math.PI * 2 / 6, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu4, R.string.menu4_label,
						Math.PI / 2, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu5, R.string.menu5_label,
						Math.PI * 2 / 3, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu6, R.string.menu6_label,
						Math.PI * 5 / 6, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu7, R.string.menu7_label,
						Math.PI, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu8, R.string.menu8_label,
						Math.PI * 7 / 6, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu9, R.string.menu9_label,
						Math.PI * 4 / 3, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu10, R.string.menu10_label,
						Math.PI * 3 / 2, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu11, R.string.menu11_label,
						Math.PI * 5 / 3, radius, menuitemWidth, menuitemHeight),
				new MenuItem(this, R.drawable.menu12, R.string.menu12_label,
						Math.PI * 11 / 6, radius, menuitemWidth, menuitemHeight) };
		for (int i = 0; i < default_menus.length; i++) {
			View v = mInflater.inflate(R.layout.menu_item, null);
			ImageView iv_menu = (ImageView) v.findViewById(R.id.iv_menu);
			TextView tv_menu = (TextView) v.findViewById(R.id.tv_menu);
			iv_menu.setImageDrawable(default_menus[i].getMenuDrawable());
			tv_menu.setText(default_menus[i].getMenuLabel());
			
			LinearLayout.LayoutParams llparmas = (LinearLayout.LayoutParams) iv_menu.getLayoutParams();
			llparmas.height = (int)(default_menus[i].getMenuHeight() * MENU_IMAGE_HEIGHT_RATE);
			iv_menu.setLayoutParams(llparmas);
			
			llparmas = (LinearLayout.LayoutParams) tv_menu.getLayoutParams();
			llparmas.height = (int)(default_menus[i].getMenuHeight() * (1 - MENU_IMAGE_HEIGHT_RATE));
			tv_menu.setLayoutParams(llparmas);
			
			v.setTag(default_menus[i]);
			default_menu_views.add(v);
			rl_root.addView(v, new RelativeLayout.LayoutParams(0, 0));
		}
	}

	/**
	 * findView is used to bind UI components from xml config file
	 */
	private void findViews() {
		rl_root = (RelativeLayout) findViewById(R.id.rl_root);
		fw_view = (FerrisWheelView) findViewById(R.id.fw_view);
		cb_rotate_before_content = (CheckBox) findViewById(R.id.cb_rotateToTop);
		cb_rotate_before_content.setChecked(true);
		contentView = (RelativeLayout) mInflater.inflate(
				R.layout.activity_content, null);

		/**
		 * add content View to this layout
		 */
		rl_root.addView(contentView, new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		btn_back = (Button) contentView.findViewById(R.id.btn_back);
		rl_title = (RelativeLayout) contentView.findViewById(R.id.rl_title);
		iv_title = (ImageView) contentView.findViewById(R.id.iv_title);

		contentView.setVisibility(View.GONE);

		/**
		 * set MainActivity as an observer of FerrisWheelView
		 */
		fw_view.addObserver(this);

		/**
		 * load menus
		 */
		loadDefaultMenus();

		/**
		 * config the layout params of menus
		 */
		layoutMenus();
	}

	/**
	 * set callback listeners to ui components
	 */
	private void setListeners() {
		btn_back.setOnClickListener(backListener);
	}

	/**
	 * callback method called from ferriswheel view to tell Main Activity the
	 * angle change of every menu
	 * 
	 * @param angle
	 *            the angle change should be applied to all the menus
	 */
	@Override
	public void notify(double angle) {
		for (int i = 0; i < default_menus.length; i++) {
			default_menus[i].increaseAngle(angle);
		}
		layoutMenus();
	}

	/**
	 * calculate the layout params for every menu according to their angles
	 */
	private void layoutMenus() {
		double xOffset = getResources().getDisplayMetrics().widthPixels / 2
				- radius;
		double yOffset = getResources().getDisplayMetrics().heightPixels / 2
				- radius;
		RelativeLayout.LayoutParams rlparams;
		MenuItem item;
		for (View v : default_menu_views) {
			item = (MenuItem) (v.getTag());
			rlparams = (RelativeLayout.LayoutParams) v.getLayoutParams();
			rlparams.leftMargin = (int) (xOffset + item.getCorX() - item
					.getMenuWidth() / 2f);
			rlparams.topMargin = (int) (yOffset + item.getCorY() - item
					.getMenuHeight() / 2f);
			rlparams.width = item.getMenuWidth();
			rlparams.height = item.getMenuHeight();
			v.setLayoutParams(rlparams);
		}
	}

	/**
	 * callback method from ferris wheel view to get the angle from current
	 * positon to the top
	 */
	@Override
	public double rotateToTop(double x, double y) {
		for (View v : default_menu_views) {
			MenuItem mi = (MenuItem) v.getTag();
			double left = (double) mi.getCorX() - (double) mi.getMenuWidth()
					/ (double) 2;
			double right = (double) mi.getCorX() + (double) mi.getMenuWidth()
					/ (double) 2;
			double top = (double) mi.getCorY() - (double) mi.getMenuHeight()
					/ (double) 2;
			double bottom = (double) mi.getCorY() + (double) mi.getMenuHeight()
					/ (double) 2;
			if (x > left && x < right && y > top && y < bottom) {
				topView = v;
				/**
				 * if checkbox rotate before content is not checked, we don't need to rotate to top and then start the animation
				 * else if the checkbox is checked, we have to do rotate to top firstly
				 */
				if (!cb_rotate_before_content.isChecked()) return -1;
				if (mi.getCorX() > mi.getMenuRadius()) {
					return Math.PI * 2 - (mi.getMenuAngle() - Math.PI / 2);
				} else {
					double needRotateAngle = Math.PI / 2 - mi.getMenuAngle();
					return needRotateAngle > 0 ? needRotateAngle
							: needRotateAngle + Math.PI * 2;
				}
			}
		}
		return 0;
	}

	/**
	 * callback method from ferris wheel view to make an animation to switch to
	 * another content view
	 */
	@Override
	public void gotoContent() {
		if (topView == null)
			return;
		MenuItem item = (MenuItem) topView.getTag();
		RelativeLayout.LayoutParams rlparams = (RelativeLayout.LayoutParams) topView
				.getLayoutParams();
		contentView.bringToFront();
		iv_title.setImageDrawable(item.getMenuDrawable());
		ScaleAnimation sa = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				rlparams.leftMargin + item.getMenuWidth() / 2,
				rlparams.topMargin + item.getMenuHeight() / 2);
		sa.setDuration(500);
		sa.setFillAfter(true);
		contentView.startAnimation(sa);
		contentView.setVisibility(View.VISIBLE);
		btn_back.setVisibility(View.VISIBLE);
	}

	/**
	 * back button onclick listener used to reverse the animation of the another
	 * content view
	 */
	private View.OnClickListener backListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ScaleAnimation sa;
			if (topView == null) {
				sa = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
						screenWidth / 2, screenHeight / 2);
			} else {
				MenuItem item = (MenuItem) topView.getTag();
				RelativeLayout.LayoutParams rlparams = (RelativeLayout.LayoutParams) topView
						.getLayoutParams();
				sa = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
						rlparams.leftMargin + item.getMenuWidth() / 2,
						rlparams.topMargin + item.getMenuHeight() / 2);
			}
			sa.setDuration(500);
			sa.setFillAfter(true);
			contentView.startAnimation(sa);
			contentView.setVisibility(View.GONE);
			btn_back.setVisibility(View.GONE);
		}
	};

	/**
	 * release ferris wheel sound player
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (fw_view != null)
			fw_view.releaseSoundPool();
	}
}
