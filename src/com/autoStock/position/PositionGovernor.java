package com.autoStock.position;

import com.autoStock.Co;
import com.autoStock.position.PositionDefinitions.PositionType;
import com.autoStock.position.PositionGovernorResponse.PositionGovernorResponseReason;
import com.autoStock.position.PositionGovernorResponse.PositionGovernorResponseStatus;
import com.autoStock.signal.Signal;
import com.autoStock.signal.SignalDefinitions.SignalPointType;
import com.autoStock.signal.SignalPoint;
import com.autoStock.signal.SignalPointMethod;
import com.autoStock.strategy.ReentrantStrategy;
import com.autoStock.strategy.ReentrantStrategy.ReentryStatus;
import com.autoStock.strategy.StrategyOptions;
import com.autoStock.trading.types.Position;
import com.autoStock.types.Exchange;
import com.autoStock.types.QuoteSlice;

/**
 * @author Kevin Kowalewski
 *
 */
public class PositionGovernor {
	private static PositionGovernor instance = new PositionGovernor();
	private PositionManager positionManager = PositionManager.getInstance(); 
	
	public static PositionGovernor getInstance(){
		return instance;
	}
	
	public synchronized PositionGovernorResponse informGovener(QuoteSlice quoteSlice, Signal signal, Exchange exchange, StrategyOptions strategyOptions){
		return informGovener(quoteSlice, signal, exchange, strategyOptions, false);
	}
	
	public synchronized PositionGovernorResponse informGovener(QuoteSlice quoteSlice, Signal signal, Exchange exchange, StrategyOptions strategyOptions, boolean requestExit){
		PositionGovernorResponse positionGovernorResponse = new PositionGovernorResponse();
		Position position = positionManager.getPosition(quoteSlice.symbol);
		SignalPoint signalPoint = new SignalPoint();
		
		if (position == null){
			signalPoint = SignalPointMethod.getSignalPoint(false, signal, PositionType.position_none, strategyOptions.signalPointTactic);
			
			if (signalPoint.signalPointType == SignalPointType.long_entry  && strategyOptions.canGoLong){
				position = governLongEntry(quoteSlice, signal, positionGovernorResponse, exchange);
			}else if (signalPoint.signalPointType == SignalPointType.short_entry && strategyOptions.canGoShort){
				position = governShortEntry(quoteSlice, signal, positionGovernorResponse, exchange);
			}
		} else {
			SignalPoint signalPointForReentry = signalPoint = SignalPointMethod.getSignalPoint(false, signal, PositionType.position_none, strategyOptions.signalPointTactic);
			signalPoint = SignalPointMethod.getSignalPoint(true, signal, position.positionType, strategyOptions.signalPointTactic);
//			ReentrantStrategy reentrantStrategy = new ReentrantStrategy();

			if (position.positionType == PositionType.position_long || position.positionType == PositionType.position_long_entry) {
				if (signalPoint.signalPointType == SignalPointType.long_exit || requestExit) {
					governLongExit(quoteSlice, position, signal, positionGovernorResponse, exchange);
				}else if (signalPointForReentry.signalPointType == SignalPointType.long_entry){
//					Co.println("--> Should re-enter?");
//					if (reentrantStrategy.getReentryStatus(position, signal, strategyOptions) == ReentryStatus.status_reenter){
//						Co.println("--> yes on re-entry");
//						positionGovernorResponse.status = PositionGovernorResponseStatus.changed_long_reentry;
//					}
				}
			}else if (position.positionType == PositionType.position_short || position.positionType == PositionType.position_short_entry) {
				if (signalPoint.signalPointType == SignalPointType.short_exit || requestExit) {
					governShortExit(quoteSlice, position, signal, positionGovernorResponse, exchange);
				}
			}else if (position.positionType == PositionType.position_canceled || position.positionType == PositionType.position_cancelling || position.positionType == PositionType.position_exited || position.positionType == PositionType.position_long_exit || position.positionType == PositionType.position_short_exit){
				Co.println("--> Position is not yet removed");
			}else {
				throw new IllegalStateException("Position type did not match: " + position.positionType.name() + ", " + positionManager.getPositionListSize());
			}
		}

		positionGovernorResponse.position = position;
		positionGovernorResponse.signalPoint = signalPoint;
		signal.currentSignalPoint = signalPoint;

		return positionGovernorResponse;
	} 
	
	private Position governLongEntry(QuoteSlice quoteSlice, Signal signal, PositionGovernorResponse positionGovernorResponse, Exchange exchange){
		Position position = positionManager.executePosition(quoteSlice, exchange, signal, PositionType.position_long_entry);
		if (position == null){
			positionGovernorResponse.getFailedResponse(PositionGovernorResponseReason.failed_insufficient_funds);
		}else{
			positionGovernorResponse.status = PositionGovernorResponseStatus.changed_long_entry;
		}
		
		return position;
	}
	
	private Position governShortEntry(QuoteSlice quoteSlice, Signal signal, PositionGovernorResponse positionGovernorResponse, Exchange exchange){
		Position position = positionManager.executePosition(quoteSlice, exchange, signal, PositionType.position_short_entry);
		if (position == null){
			positionGovernorResponse.getFailedResponse(PositionGovernorResponseReason.failed_insufficient_funds);
		}else{
			positionGovernorResponse.status = PositionGovernorResponseStatus.changed_short_entry;
		}
		
		return position;
	}
	
	private void governLongExit(QuoteSlice quoteSlice, Position position, Signal signal, PositionGovernorResponse positionGovernorResponse, Exchange exchange){
		if (position != null && (position.positionType == PositionType.position_long_exit)){
			throw new IllegalStateException();
		}
		position = positionManager.executePosition(quoteSlice, exchange, signal, PositionType.position_long_exit);
		positionGovernorResponse.status = PositionGovernorResponseStatus.changed_long_exit;
	}
	
	private void governShortExit(QuoteSlice quoteSlice, Position position, Signal signal, PositionGovernorResponse positionGovernorResponse, Exchange exchange){
		if (position != null && (position.positionType == PositionType.position_short_exit)){
			throw new IllegalStateException();
		}
		position = positionManager.executePosition(quoteSlice, exchange, signal, PositionType.position_short_exit);
		positionGovernorResponse.status = PositionGovernorResponseStatus.changed_short_exit;
	}
}
