package com.autoStock.algorithm;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

import com.autoStock.Co;
import com.autoStock.algorithm.reciever.ReceiverOfQuoteSlice;
import com.autoStock.analysis.AnalysisOfBB;
import com.autoStock.analysis.AnalysisOfCCI;
import com.autoStock.analysis.AnalysisOfDI;
import com.autoStock.analysis.AnalysisOfMACD;
import com.autoStock.analysis.AnalysisOfRSI;
import com.autoStock.analysis.AnalysisOfSTORSI;
import com.autoStock.analysis.AnalysisOfTRIX;
import com.autoStock.analysis.CommonAnlaysisData;
import com.autoStock.analysis.results.ResultsBB;
import com.autoStock.analysis.results.ResultsCCI;
import com.autoStock.analysis.results.ResultsDI;
import com.autoStock.analysis.results.ResultsMACD;
import com.autoStock.analysis.results.ResultsRSI;
import com.autoStock.analysis.results.ResultsSTORSI;
import com.autoStock.analysis.results.ResultsTRIX;
import com.autoStock.chart.ChartForAlgorithmTest;
import com.autoStock.finance.Account;
import com.autoStock.position.PositionGovernor;
import com.autoStock.position.PositionGovernorResponse;
import com.autoStock.position.PositionManager;
import com.autoStock.signal.Signal;
import com.autoStock.signal.SignalControl;
import com.autoStock.signal.SignalDefinitions.SignalSource;
import com.autoStock.signal.SignalOfCCI;
import com.autoStock.signal.SignalOfDI;
import com.autoStock.signal.SignalOfMACD;
import com.autoStock.signal.SignalOfPPC;
import com.autoStock.signal.SignalOfRSI;
import com.autoStock.signal.SignalOfSTORSI;
import com.autoStock.signal.SignalOfTRIX;
import com.autoStock.taLib.MAType;
import com.autoStock.tables.TableController;
import com.autoStock.tables.TableDefinitions.AsciiTables;
import com.autoStock.tools.ArrayTools;
import com.autoStock.tools.Benchmark;
import com.autoStock.tools.DataExtractor;
import com.autoStock.tools.DateTools;
import com.autoStock.tools.MathTools;
import com.autoStock.tools.StringTools;
import com.autoStock.types.Exchange;
import com.autoStock.types.QuoteSlice;
import com.autoStock.types.Symbol;

/**
 * @author Kevin Kowalewski
 *
 */
public class AlgorithmTest extends AlgorithmBase implements ReceiverOfQuoteSlice {
	private int periodLength = SignalControl.periodLength;
	
	private boolean enableChart = false;
	private boolean enableTable = false;
	
	private AnalysisOfCCI analysisOfCCI = new AnalysisOfCCI(periodLength, false);
	private AnalysisOfDI analysisOfDI = new AnalysisOfDI(periodLength, false);
	private AnalysisOfMACD analysisOfMACD = new AnalysisOfMACD(periodLength, false);
	private AnalysisOfBB analysisOfBB = new AnalysisOfBB(periodLength, false);
	private AnalysisOfTRIX analysisOfTRIX = new AnalysisOfTRIX(periodLength, false);
	private AnalysisOfRSI analysisOfRSI = new AnalysisOfRSI(periodLength, false);
	
	private ArrayList<ArrayList<String>> listOfDisplayRows = new ArrayList<ArrayList<String>>();
	private ArrayList<QuoteSlice> listOfQuoteSlice = new ArrayList<QuoteSlice>();
	public Signal signal = new Signal(SignalSource.from_analysis);
	private PositionGovernor positionGovener = new PositionGovernor();
	private ChartForAlgorithmTest chart;
	
	public AlgorithmTest(boolean canTrade, Exchange exchange, Symbol symbol) {
		super(canTrade, exchange, symbol);
		if (enableChart){chart = new ChartForAlgorithmTest();}
	}

	@Override
	public void receiveQuoteSlice(QuoteSlice quoteSlice) {
		//Co.println("Received backtest quote: " + DateTools.getPrettyDate(typeQuoteSlice.dateTime) + ", " + typeQuoteSlice.priceClose);
		
		listOfQuoteSlice.add(quoteSlice);
	
		if (listOfQuoteSlice.size() > (periodLength)){
			double analysisPrice = quoteSlice.priceClose;
			
			if (listOfQuoteSlice.size() > (periodLength)){
				listOfQuoteSlice.remove(0);
			}
			
			CommonAnlaysisData.setAnalysisData(listOfQuoteSlice);
			
			analysisOfCCI.setDataSet(listOfQuoteSlice);
			analysisOfDI.setDataSet(listOfQuoteSlice);
			analysisOfBB.setDataSet(listOfQuoteSlice);
			analysisOfMACD.setDataSet(listOfQuoteSlice);
			analysisOfRSI.setDataSet(listOfQuoteSlice);
			analysisOfTRIX.setDataSet(listOfQuoteSlice);
			
			ResultsCCI resultsCCI = analysisOfCCI.analyize();
			ResultsDI resultsDI = analysisOfDI.analize();
			ResultsBB resultsBB = analysisOfBB.analyize(MAType.Ema);
			ResultsMACD resultsMACD = analysisOfMACD.analize();
			ResultsRSI resultsRSI = analysisOfRSI.analyize();
			ResultsTRIX resultsTRIX = analysisOfTRIX.analyize();
			
			double[] arrayOfPriceClose = CommonAnlaysisData.arrayOfPriceClose;
			double analysisOfCCIResult = resultsCCI.arrayOfCCI[0];
			double analysisOfDIResultPlus = resultsDI.arrayOfDIPlus[0];
			double analysisOfDIResultMinus = resultsDI.arrayOfDIMinus[0];
			double analysisOfBBResultUpper = resultsBB.arrayOfUpperBand[0];
			double analysisOfBBResultLower = resultsBB.arrayOfLowerBand[0];
			double analysisOfMACDResult = resultsMACD.arrayOfMACDSignal[0];
			double analysisOfRSIResult = resultsRSI.arrayOfRSI[0];
			double analysisOfTrixResult = resultsTRIX.arrayOfTRIX[0];
			
			SignalOfPPC signalOfPPC = new SignalOfPPC(ArrayTools.subArray(arrayOfPriceClose, 0, periodLength), SignalControl.periodAverageForPPC);
			SignalOfDI signalOfDI = new SignalOfDI(ArrayTools.subArray(resultsDI.arrayOfDIPlus, 0, 1), ArrayTools.subArray(resultsDI.arrayOfDIMinus, 0, 1), SignalControl.periodAverageForDI);
			SignalOfCCI signalOfCCI = new SignalOfCCI(ArrayTools.subArray(resultsCCI.arrayOfCCI, 0, 1), SignalControl.periodAverageForCCI);
			SignalOfMACD signalOfMACD = new SignalOfMACD(ArrayTools.subArray(resultsMACD.arrayOfMACDSignal, 0, 1), SignalControl.periodAverageForMACD);
			SignalOfRSI signalOfRSI = new SignalOfRSI(ArrayTools.subArray(resultsRSI.arrayOfRSI, 0, 1), SignalControl.periodAverageForRSI);
			SignalOfTRIX signalOfTRIX = new SignalOfTRIX(ArrayTools.subArray(resultsTRIX.arrayOfTRIX, 0, 1), SignalControl.periodAverageForTRIX);
			
			signal.reset();
//			signal.addSignalMetrics(signalOfDI.getSignal(), signalOfCCI.getSignal(), signalOfMACD.getSignal(), signalOfTRIX.getSignal());
//			signal.addSignalMetrics(signalOfPPC.getSignal(), signalOfDI.getSignal(), signalOfTRIX.getSignal(), signalOfCCI.getSignal());
			signal.addSignalMetrics(signalOfDI.getSignal());
			
			if (enableChart){
				chart.listOfDate.add(quoteSlice.dateTime);
				chart.listOfPrice.add(quoteSlice.priceClose);
				chart.listOfSignalDI.add(signalOfDI.getSignal().strength);
				chart.listOfSignalCCI.add(signalOfCCI.getSignal().strength);
				chart.listOfSignalPPC.add(signalOfPPC.getSignal().strength);
				chart.listOfSignalMACD.add(signalOfMACD.getSignal().strength);
				chart.listOfSignalRSI.add(signalOfRSI.getSignal().strength);
				chart.listOfSignalTRIX.add(signalOfTRIX.getSignal().strength);
				chart.listOfSignalTotal.add((int)signal.getCombinedSignal().strength);
				
				chart.listOfDI.add(analysisOfDIResultPlus - analysisOfDIResultMinus);
				chart.listOfCCI.add(analysisOfCCIResult);
				chart.listOfMACD.add(analysisOfMACDResult);
				chart.listOfRSI.add(analysisOfRSIResult);
			}
			
			if (algorithmListener != null){
				algorithmListener.recieveSignal(signal, quoteSlice);
			}
			
			ArrayList<String> columnValues = new ArrayList<String>();
			
//			columnValues.add(DateTools.getPrettyDate(typeQuoteSlice.dateTime));
//			columnValues.add(String.valueOf(typeQuoteSlice.priceClose));
//			columnValues.add(String.valueOf(StringTools.addPlusToPositiveNumbers(MathTools.roundToTwoDecimalPlaces(typeQuoteSlice.priceClose - listOfQuoteSlice.get(listOfQuoteSlice.size()-2).priceClose))));
//			columnValues.add(String.valueOf(signalOfMACD.getValue()));
//			columnValues.add(String.valueOf(signalOfMACD.getSignal().strength + "," + signalOfMACD.getSignal().signalTypeMetric.name()));
//			columnValues.add(String.valueOf(signalOfPPC.getValue()));
//			columnValues.add(String.valueOf((analysisOfDIResultPlus - analysisOfDIResultMinus)));
//			columnValues.add(String.valueOf(MathTools.roundToTwoDecimalPlaces(analysisOfCCIResult)));
//			columnValues.add(String.valueOf(MathTools.roundToTwoDecimalPlaces(analysisOfBBResultUpper)));
//			columnValues.add(String.valueOf(MathTools.roundToTwoDecimalPlaces(analysisOfBBResultLower)));
//			columnValues.add(String.valueOf(analysisOfMACDResult));
//			columnValues.add(String.valueOf(MathTools.roundToTwoDecimalPlaces(analysisOfSTORSIResultK)));
//			columnValues.add(String.valueOf(MathTools.roundToTwoDecimalPlaces(analysisOfSTORSIResultD)));
			
			if (enableTable){
				columnValues.add(DateTools.getPrettyDate(quoteSlice.dateTime));
				columnValues.add(String.valueOf(quoteSlice.priceClose));
				columnValues.add(String.valueOf(StringTools.addPlusToPositiveNumbers(MathTools.round(quoteSlice.priceClose - listOfQuoteSlice.get(listOfQuoteSlice.size()-2).priceClose))));
				columnValues.add(String.valueOf(signalOfPPC.getSignal().strength));
				columnValues.add(String.valueOf(signalOfDI.getSignal().strength));
				columnValues.add(String.valueOf(signalOfCCI.getSignal().strength));
				columnValues.add(String.valueOf(signalOfRSI.getSignal().strength));
				columnValues.add(String.valueOf(signalOfMACD.getSignal().strength));
				columnValues.add(String.valueOf(signalOfTRIX.getSignal().strength));
				columnValues.add(String.valueOf(signal.getCombinedSignal().strength));
			}
			
			PositionGovernorResponse positionGovenorResponse = positionGovener.informGovener(quoteSlice, signal, exchange);
			
			if (enableTable){
				if (positionGovenorResponse.changedPosition){
					columnValues.add(signal.currentSignalType.name() + ", " + positionGovenorResponse.typePosition.positionType.name());
					columnValues.add(positionGovenorResponse.typePosition.units + ", " + positionGovenorResponse.typePosition.lastKnownPrice + ", " + (positionGovenorResponse.typePosition.units * positionGovenorResponse.typePosition.lastKnownPrice));
					columnValues.add(String.valueOf(Account.instance.getBankBalance()));
				}else{
					columnValues.add("");
					columnValues.add("");
					columnValues.add("");
				}
				
				columnValues.add("");
				listOfDisplayRows.add(columnValues);
			}	
		}
	}

	@Override
	public void endOfFeed(Symbol symbol) {
		if (enableChart){chart.display();}
//		new TableController().displayTable(AsciiTables.analysis_test, listOfDisplayRows);
		if (enableTable){new TableController().displayTable(AsciiTables.algorithm_test, listOfDisplayRows);}
		if (algorithmListener != null){algorithmListener.endOfAlgorithm();}
	}
}
