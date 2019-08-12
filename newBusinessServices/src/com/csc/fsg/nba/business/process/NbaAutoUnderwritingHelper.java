package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */
import java.util.Date;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * It is a helper class for perfroming auto underwriting on Simplified Issue contract
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class NbaAutoUnderwritingHelper {		
	protected static NbaLogger logger = null;
	private static final int DAYS_99 = 99;
	private static final String NOT_APPLICABLE = "0";
	public static final int RETRY_FREQ_ONE = 1;
	public static final int RETRY_FREQ_TWO = 2;
	public static final int RETRY_FREQ_THREE = 3;
	public static final int MAX_OCC_RETRY_ONE = 6;
	public static final int MAX_OCC_RETRY_TWO = 3;
	public static final int MAX_OCC_RETRY_THREE = 3;	
	public static final int INCR_SEC_RETRY_FREQ_ONE = 30;
	public static final int INCR_SEC_RETRY_FREQ_TWO = 420;
	public static final int INCR_SEC_RETRY_FREQ_THREE = 720;
	public static final int OCC_TIME_OUT = 4;	

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected synchronized static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaAutoUnderwritingHelper.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaAutoUnderwritingHelper could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
		
	protected static NbaDst processMIBInquiryResponse(String response, NbaUserVO user, NbaDst work, NbaLob workLob, int reqType) throws NbaBaseException {
		workLob.setReqType(reqType);
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, work);
		NbaDst transactionDst = createNbaDstWithTempReqTransaction(provider.getWorkType(), provider.getInitialStatus(), user.getUserID(), workLob);
		NbaTransaction transaction = transactionDst.getNbaTransaction();
		transaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		transaction.addNbaSource(new NbaSource(workLob.getBusinessArea(), NbaConstants.A_ST_PROVIDER_SUPPLEMENT, response));
		return transactionDst;
	}

	protected static NbaDst processRXCheckResponse(String response, NbaUserVO user, NbaDst work, NbaLob workLob, int reqType) throws NbaBaseException {
		workLob.setReqType(reqType);
		NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(user, work);
		NbaDst transactionDst = createNbaDstWithTempReqTransaction(provider.getWorkType(), provider.getInitialStatus(), user.getUserID(), workLob);
		NbaTransaction transaction = transactionDst.getNbaTransaction();
		transaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
		transaction.addNbaSource(new NbaSource(workLob.getBusinessArea(), NbaConstants.A_ST_PROVIDER_SUPPLEMENT, response));
		return transactionDst;
	}

	private static NbaDst createNbaDstWithTempReqTransaction(String workType, String status, String userId, NbaLob workLob) {
		WorkItem awdTransaction = new WorkItem();
		awdTransaction.setLobData(workLob.getLobs());
		awdTransaction.setBusinessArea(workLob.getBusinessArea());
		awdTransaction.setWorkType(workType);
		awdTransaction.setStatus(status);
		awdTransaction.setLock("Y");
		awdTransaction.setAction("L");
		awdTransaction.setCreate("Y");
		NbaDst dst = new NbaDst();
		dst.setUserID(userId);
		try {
			dst.addTransaction(awdTransaction);
		} catch (Exception ex) {
			getLogger().logException("Exception in adding TempReq transaction for SI APP ", ex);
		}
		return dst;
	}

	public static int getApplicationSuspendDays() {
		int suspendDays = 0;
		NbaVpmsAdaptor adapter = null;
		try {
			adapter = new NbaVpmsAdaptor(new NbaOinkDataAccess(), NbaVpmsAdaptor.REQUIREMENTS);
			Map deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_ReqStatusLOB, NOT_APPLICABLE);
			deOinkMap.put(NbaVpmsAdaptor.A_WorkTypeLOB, NbaConstants.A_WT_APPLICATION);			
			deOinkMap.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaConstants.PROC_SI_AUTO_UNDERWRITING);
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_SUSPEND_DAYS);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			List suspendDayList = vpmsResultsData.getResultsData();
			if (suspendDayList != null && suspendDayList.size() > 0) {
				suspendDays = Integer.parseInt(suspendDayList.get(1).toString());
			}
		} catch (Exception e) {
			suspendDays = DAYS_99;
			getLogger().logDebug("Problem in getting suspend days from VPMS" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return suspendDays;
	}
	
	public static String calculateRateClass(long uwClass, long tobaccoPremiumBasis) {
		String rateClass = "0";
		NbaVpmsAdaptor adapter = null;
		try {
			adapter = new NbaVpmsAdaptor(new NbaOinkDataAccess(), NbaVpmsAdaptor.CONTRACTVALIDATIONCALCULATIONS);
			Map deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_UNDERWRITINGCLASS, String.valueOf(uwClass));
			deOinkMap.put(NbaVpmsAdaptor.A_TOBACCOPREMIUMBASIS, String.valueOf(tobaccoPremiumBasis));
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(NbaVpmsAdaptor.EP_SimplifiedIssue_RateClass);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				rateClass = (String) vpmsResultsData.getResultsData().get(0);
			}
		} catch (Exception e) {
			getLogger().logDebug("Problem in getting rate class from VPMS" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return rateClass;
	}

	public static String calculateSmokerStat(long uwClass, long tobaccoPremiumBasis) {
		String smokerStat = "0";
		NbaVpmsAdaptor adapter = null;
		try {
			adapter = new NbaVpmsAdaptor(new NbaOinkDataAccess(), NbaVpmsAdaptor.CONTRACTVALIDATIONCALCULATIONS);
			Map deOinkMap = new HashMap();
			deOinkMap.put(NbaVpmsAdaptor.A_UNDERWRITINGCLASS, String.valueOf(uwClass));
			deOinkMap.put(NbaVpmsAdaptor.A_TOBACCOPREMIUMBASIS, String.valueOf(tobaccoPremiumBasis));
			adapter.setSkipAttributesMap(deOinkMap);
			adapter.setVpmsEntryPoint(NbaVpmsAdaptor.EP_SimplifiedIssue_SmokerStat);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(adapter.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				smokerStat = (String) vpmsResultsData.getResultsData().get(0);
			}
		} catch (Exception e) {
			getLogger().logDebug("Problem in getting rate class from VPMS" + e.getMessage());
		} finally {
			try {
				if (adapter != null) {
					adapter.remove();
				}
			} catch (Throwable th) {
				// ignore, nothing can be done
			}
		}
		return smokerStat;
	}
	
	public static int getNextIncrementSec(int Retry_Freq) {
		int incrementSec = 0;
		if (Retry_Freq == RETRY_FREQ_ONE) {
			incrementSec = INCR_SEC_RETRY_FREQ_ONE;
		} else if (Retry_Freq == RETRY_FREQ_TWO) {
			incrementSec = INCR_SEC_RETRY_FREQ_TWO;
		} else if (Retry_Freq == RETRY_FREQ_THREE) {
			incrementSec = INCR_SEC_RETRY_FREQ_THREE;
		}
		return incrementSec;
	}

	public static int getNextOccurenceNum(int Retry_Freq, int Occurence) {
		Occurence++;
		if (Retry_Freq == RETRY_FREQ_ONE) {
			if (Occurence > MAX_OCC_RETRY_ONE) {
				Occurence = 1;
			}
		} else if (Retry_Freq == RETRY_FREQ_TWO) {
			if (Occurence > MAX_OCC_RETRY_TWO) {
				Occurence = 1;
			}
		} else if (Retry_Freq == RETRY_FREQ_THREE) {
			if (Occurence > MAX_OCC_RETRY_THREE) {
				Occurence = OCC_TIME_OUT;
			}
		}
		return Occurence;
	}

	public static int getNextRetryFreq(int Retry_Freq, int Occurence) {
		if (Retry_Freq == RETRY_FREQ_ONE && Occurence == MAX_OCC_RETRY_ONE) {
			Retry_Freq = RETRY_FREQ_TWO;
		} else if (Retry_Freq == RETRY_FREQ_TWO && Occurence == MAX_OCC_RETRY_TWO) {
			Retry_Freq = RETRY_FREQ_THREE;
		}
		return Retry_Freq;
	}
	//APSL2808 New Method
	public static String convertScorToNba(String scorResponse) throws NbaBaseException{
		String providerID = "SCOR";
		StringWriter outputStream =null;
		String generatedString=null; 
		try{				 
			System.out.println("scorResponse 1: " +scorResponse);
			scorResponse = removePrefixInResponse(scorResponse,providerID);
			if(scorResponse!=null){
				System.out.println("scorResponse 2: "+ scorResponse);
				String xslFileName = NbaUtils.loadTransformationXSL(providerID, NbaUtils.XSL_REQUIREMENT_VALIDATE, null);
				if (xslFileName != null && xslFileName.length() > 0) {					
					FileInputStream xslFile = new FileInputStream(xslFileName);
					Transformer transformer = TransformerFactory.newInstance().newTemplates(new StreamSource(xslFile)).newTransformer();
					transformer.setParameter("CurrentDate", NbaUtils.getCurrentDateForXSL());
					transformer.setParameter("CurrentTime", new NbaTime().toString());
					transformer.setParameter("TransRefGUID", NbaUtils.getGUID());					
					BufferedReader reader = new BufferedReader(new StringReader(scorResponse.trim()));
					Source source = new StreamSource(reader);
					outputStream = new StringWriter();
					Result target = new javax.xml.transform.stream.StreamResult(outputStream);
					transformer.transform(source, target);
					generatedString = outputStream.toString();
					reader.close();
					System.out.println("scorResponse 3: "+ generatedString);
				}	
			}			
		} catch (Exception e) {
			e.printStackTrace();
			throw new NbaBaseException("Error parsing in Rx response.",e);
		}
		return generatedString;
	}
	//APSL2808 New Method
	protected static String removePrefixInResponse(String scorResponse,String providerID) throws NbaBaseException{
		StringWriter outputStream =null;
		try{
			String xslFileName = NbaUtils.loadTransformationXSL(providerID, NbaUtils.XSL_REQUIREMENT_REQUEST, null);
			if (xslFileName != null && xslFileName.length() > 0){				
				FileInputStream xslFile = new FileInputStream(xslFileName);
				Transformer transformer = TransformerFactory.newInstance().newTemplates(new StreamSource(xslFile)).newTransformer();
				BufferedReader reader = new BufferedReader(new StringReader(scorResponse.trim()));
				Source source = new StreamSource(reader);
				outputStream = new StringWriter();
				Result target = new javax.xml.transform.stream.StreamResult(outputStream);
				transformer.transform(source, target);
			}	
		} catch (Exception e) {
			e.printStackTrace();
			throw new NbaBaseException("Error parsing prefix in Rx response.",e);
		}
		return outputStream.toString(); 
	}	
}