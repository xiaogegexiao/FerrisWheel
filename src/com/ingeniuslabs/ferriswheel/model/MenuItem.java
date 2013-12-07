package com.ingeniuslabs.ferriswheel.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Menu Item Model class with menu drawable,label,angle, radius, coordicateX, coordinateY, menuWidth and menuHeight
 * @author Xiao
 *
 */
public class MenuItem {
	private Drawable menuDrawable;
	private CharSequence menuLabel;
	private double menuAngle;
	private float menuRadius;
	private double corX;
	private double corY;
	private int menuWidth;
	private int menuHeight;

	public MenuItem(Drawable argDrawable, CharSequence argLabel,
			double argAngle, float argRadius, int argmenuWidth,
			int argmenuHeight) {
		menuDrawable = argDrawable;
		menuLabel = argLabel;
		menuAngle = argAngle;
		menuRadius = argRadius;
		menuHeight = argmenuHeight;
		menuWidth = argmenuWidth;

		fixAngle();
		calculateCorXY();
	}

	public MenuItem(Context context, int argDrawableId, int argLabeId,
			double argAngle, float argRadius, int argmenuWidth,
			int argmenuHeight) {
		menuDrawable = context.getResources().getDrawable(argDrawableId);
		menuLabel = context.getText(argLabeId);
		menuAngle = argAngle;
		menuRadius = argRadius;
		menuHeight = argmenuHeight;
		menuWidth = argmenuWidth;

		fixAngle();
		calculateCorXY();
	}

	public float getMenuRadius() {
		return menuRadius;
	}

	public void setMenuRadius(float menuRadius) {
		this.menuRadius = menuRadius;
	}

	public Drawable getMenuDrawable() {
		return menuDrawable;
	}

	public void setMenuDrawable(Drawable menuDrawable) {
		this.menuDrawable = menuDrawable;
	}

	public CharSequence getMenuLabel() {
		return menuLabel;
	}

	public void setMenuLabel(CharSequence menuLabel) {
		this.menuLabel = menuLabel;
	}

	public double getMenuAngle() {
		return menuAngle;
	}

	public void setMenuAngle(double menuAngle) {
		this.menuAngle = menuAngle;
		fixAngle();
		calculateCorXY();
	}

	public void increaseAngle(double increaseAngle) {
		this.menuAngle += increaseAngle;
		fixAngle();
		calculateCorXY();
	}

	public double getCorX() {
		return corX;
	}

	public void setCorX(double corX) {
		this.corX = corX;
	}

	public double getCorY() {
		return corY;
	}

	public void setCorY(double corY) {
		this.corY = corY;
	}

	public int getMenuWidth() {
		return menuWidth;
	}

	public void setMenuWidth(int menuWidth) {
		this.menuWidth = menuWidth;
	}

	public int getMenuHeight() {
		return menuHeight;
	}

	public void setMenuHeight(int menuHeight) {
		this.menuHeight = menuHeight;
	}

	/**
	 * fix angle method
	 * used to adjust the angle to proper range [0, 360)
	 */
	private void fixAngle() {
		if (menuAngle >= 2 * Math.PI) {
			menuAngle -= 2 * Math.PI;
		} else if (menuAngle < 0) {
			menuAngle += 2 * Math.PI;
		}
	}

	/**
	 * calculate the coordinate X and Y for the Menu according to the angle
	 */
	public void calculateCorXY() {
		if (menuAngle > 0 && menuAngle < Math.PI / 2) {
			corX = menuRadius * (1 - Math.cos(menuAngle));
			corY = menuRadius * (1 - Math.sin(menuAngle));
		} else if (menuAngle > Math.PI / 2 && menuAngle < Math.PI) {
			corX = menuRadius * (1 + Math.abs(Math.cos(menuAngle)));
			corY = menuRadius * (1 - Math.sin(menuAngle));
		} else if (menuAngle > Math.PI && menuAngle < 3 * Math.PI / 2) {
			corX = menuRadius * (1 + Math.abs(Math.cos(menuAngle)));
			corY = menuRadius * (1 + Math.abs(Math.sin(menuAngle)));
		} else if (menuAngle > 3 * Math.PI / 2 && menuAngle < 2 * Math.PI) {
			corX = menuRadius * (1 - Math.cos(menuAngle));
			corY = menuRadius * (1 + Math.abs(Math.sin(menuAngle)));
		} else if (menuAngle == 0) {
			corX = 0;
			corY = menuRadius;
		} else if (menuAngle == Math.PI / 2) {
			corX = menuRadius;
			corY = 0;
		} else if (menuAngle == Math.PI) {
			corX = 2 * menuRadius;
			corY = menuRadius;
		} else {
			corX = menuRadius;
			corY = 2 * menuRadius;
		}
	}
}
