/**
 * 
 */
package com.autoStock.tools;

/**
 * @author Kevin Kowalewski
 *
 */
public class ArrayTools {
	public static float[] shiftArrayDown(float[] array, int shift){
		float[] arrayOfFloat = new float[array.length];
		
		for (int i=shift; i<array.length; i++){
			arrayOfFloat[i] = array[i-shift];
		}
		
		return arrayOfFloat;
	}
	
	public static double[] shiftArrayDown(double[] array, int shift){
		double[] arrayOfdouble = new double[array.length];
		
		for (int i=shift; i<array.length; i++){
			arrayOfdouble[i] = array[i-shift];
		}
		
		return arrayOfdouble;
	}
	
	public static double[] subArray(double[] array, int start, int end){
		double[] arrayOfDouble = new double[end-start];
		
		for (int i=start; i<end; i++){
			arrayOfDouble[i] = array[i];
		}
		
		return arrayOfDouble;
	}
}
