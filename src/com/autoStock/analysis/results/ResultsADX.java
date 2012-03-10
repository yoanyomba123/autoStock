/**
 * 
 */
package com.autoStock.analysis.results;

import java.util.Date;

/**
 * @author Kevin Kowalewski
 *
 */
public class ResultsADX extends ResultsBase {
	
	public Date[] arrayOfDates;
	public double[] arrayOfADX;
	public double[] arrayOfPrice;
	
	public ResultsADX(int length){
		this.arrayOfDates = new Date[length];
		this.arrayOfADX = new double[length];
		this.arrayOfPrice = new double[length];
	}
}
