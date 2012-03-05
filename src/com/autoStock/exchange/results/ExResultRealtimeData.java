/**
 * 
 */
package com.autoStock.exchange.results;

import java.util.ArrayList;

import com.autoStock.trading.platform.ib.core.TickType;
import com.autoStock.trading.platform.ib.definitions.MarketData.TickPriceFields;
import com.autoStock.trading.platform.ib.definitions.MarketData.TickSizeFields;
import com.autoStock.trading.platform.ib.definitions.MarketData.TickTypes;
import com.autoStock.trading.types.TypeHistoricalData;
import com.autoStock.trading.types.TypeRealtimeData;

/**
 * @author Kevin Kowalewski
 *
 */
public class ExResultRealtimeData {
	public class ExResultSetRealtimeData {
		public TypeRealtimeData typeRealtimeData;
		public ArrayList<ExResultRowRealtimeData> listOfExResultRowRealtimeData = new ArrayList<ExResultRowRealtimeData>();
		
		public ExResultSetRealtimeData(TypeRealtimeData typeRealtimeData){
			this.typeRealtimeData = typeRealtimeData;
		}
	}
	
	public static class ExResultRowRealtimeData{
		TickPriceFields field;
		String value;
		
		public ExResultRowRealtimeData(TickPriceFields field, String value){
			this.field = field;
			this.value = value;
		}
	}	
}