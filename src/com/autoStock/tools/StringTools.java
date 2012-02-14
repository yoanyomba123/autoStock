/**
 * 
 */
package com.autoStock.tools;

/**
 * @author Kevin Kowalewski
 *
 */
public class StringTools {
	public static String removePrefix(String string, String prefix){
		return string.substring(string.indexOf(prefix)+1);
	}
	
	public static String addPlusToPositiveNumbers(int number){
		if (number > 0){
			return "+" + number;
		}else{
			return String.valueOf(number);
		}
	}
	
	public static String addPlusToPositiveNumbers(float number){
		if (number > 0){
			return "+" + number;
		}else{
			return String.valueOf(number);
		}
	}
	
	public static String addPlusToPositiveNumbers(String number){
		float value = Float.valueOf(number);
		if (value > 0){
			return "+" + number;
		}else{
			return String.valueOf(number);
		}
	}
	
	public static String blankZeroValues(String number){
		float value = Float.valueOf(number);
		
		if (value == 0){
			return "";
		}else{
			return number;
		}
	}
}
