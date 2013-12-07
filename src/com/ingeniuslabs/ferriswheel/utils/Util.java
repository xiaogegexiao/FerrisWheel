package com.ingeniuslabs.ferriswheel.utils;

/**
 * Util method
 * @author Xiao
 *
 */
public class Util {
	public static final double ONE_DEGREE_VALUE = Math.PI / 180;

	/**
	 * used to convert degree to value, 180° = π
	 * @param degree
	 * @return
	 */
	public static double convertFromDegreeToValue(double degree) {
		return degree * ONE_DEGREE_VALUE;
	}

	/**
	 * used to convert value to degree, π/2 = 90°
	 * @param value
	 * @return
	 */
	public static double convertFromValueToDegree(double value) {
		return value / ONE_DEGREE_VALUE;
	}
}
