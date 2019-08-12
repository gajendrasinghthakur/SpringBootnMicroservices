package com.csc.fsg.nba.contract.validation;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.csc.fsg.nba.database.NbaContractLock;
import com.csc.fsg.nba.database.NbaSuitabilityProcessingAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaSuitabilityProcessingContract;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
 * reproduction, distribution or disclosure of this program, in whole or in<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2012 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version 1201</td><td>Suitability</td></tr>
 * <tr><td>ALII1244</td><td>AXA Life Phase2</td><td>QC 8161 - Temp Exp Case: WI got created for UWCM due to NBMISCWORK WI with status "Suitability went from IGO to NIGO" but case never IGO.</td></tr>
 * <tr><td>APSL2864</td><td>AXA Life Phase2</td><td>CR1455066 Life 2012  nbA Phase 2.1 & 2.2 Suitability Rule Changes.</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1201
 * @since New Business Accelerator - Version 1201
 */

public class NbaSuitabilityCVResultsProcessor {
	public static final String SUITABILITY_CVRESULTS_PROCESSOR = "SuitabilityCVResultsProcessor";
	private static final String DATA = "Data";
	private static final String NAME = "Name";
	private static final String ID = "Id";
	private static final String MODE = "Mode";
	private static final String SUITABILITY_FIELD = "SuitabilityField";
	private static final String SUITABILITY_FIELDS = "SuitabilityFields";
	private static final String FACE_AMT = NbaConstants.KEY_FACE_AMT; //APSL2864
	private static final String PAYMENT_AMT = NbaConstants.KEY_PAYMENT_AMT; //APSL2864
	private static final String PAYMENT_MODE = NbaConstants.KEY_PAYMENT_MODE; //APSL2864
	private static final String LTCREPLACEMENT_IND_CODE = NbaConstants.KEY_LTCREPLACEMENT_IND_CODE; //NBLXA2316[NBLXA2303]
	private static final String COVOPT_LTC = NbaConstants.KEY_COVOPT_LTC; //NBLXA2316[NBLXA2303]
	private static final String BIRTHDATE = NbaConstants.KEY_BIRTHDATE; //NBLXA2303[NBLXA-2312]
	private static final long SEMI_ANNUAL = NbaOliConstants.OLI_PAYMODE_BIANNUAL; //APSL2864
	private static final long QUATERLY = NbaOliConstants.OLI_PAYMODE_QUARTLY; //APSL2864
	private static final long MONTHLY= NbaOliConstants.OLI_PAYMODE_MNTHLY;//APSL2864
	private static final long ISSUED = NbaOliConstants.OLI_POLSTAT_ISSUED;//APSL2864
	private static final long SUITABILITY_PASS = NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_PASS;//APSL2864
	private static final long SUITABILITY_FAIL = NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_FAIL;//NBLXA2303[NBLXA2304]
	private static final long SUITABILITY_ADDNRWRQD = NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_ADDLRVWREQ;//NBLXA2303[NBLXA2304]
	private static final long SUITABILITY_PENDING = NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_PENDING;//NBLXA2303[NBLXA2304]
	private static final long SUITABILITY_INVALID = NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_INVALID;//NBLXA2303[NBLXA2304]
	private static final String Holding_ID = "Holding_1";//NBLXA2316[NBLXA2303]
	private String baseCovId;//NBLXA2316[NBLXA2303,NBLXA-2473]
	private static NbaLogger logger = null;
	private Map currentRunMap = new HashMap();
	private Map resubmitMap = new HashMap();
	private List<ChangeSubType> changeSubTypeList = new ArrayList<ChangeSubType>(); //NBLXA2303[NBLXA-2304]
	private Date ltcResubmitEffDate; //NBLXA-2303[NBLXA-2454]
	private long underwritingStatus = -1L; //NBLXA-2527
	
	//ALI1244 changed method signature
	//APSL2864 changed method signature. Added suitabilityDecisionStatus and policyStatus
	public void processCVResults(String companyCode, String contractNumber, boolean runSuitability, long suitabilityDecisionStatus, long policyStatus)
			throws Exception {
		NbaSuitabilityProcessingContract suitabilityContract = NbaSuitabilityProcessingAccessor.retrieve(companyCode, contractNumber);
		if (!suitabilityContract.isRunSuitability()) { // NBLXA-2303[NBLXA-2316] Begin
			Map priorRunMap = parseSuitabilityRequiredData(suitabilityContract.getSuitabilityRequiredData());
			// APSL2864 begin
			if (suitabilityDecisionStatus == SUITABILITY_INVALID || suitabilityDecisionStatus == SUITABILITY_PASS
					|| suitabilityDecisionStatus == SUITABILITY_FAIL || suitabilityDecisionStatus == SUITABILITY_ADDNRWRQD
					|| suitabilityDecisionStatus == SUITABILITY_PENDING) {
				suitabilityContract.setSubmitRequired(subsequentCallNeeded(priorRunMap, policyStatus));
			} else {
				suitabilityContract.setSubmitRequired(dataDifferencesExist(priorRunMap));
			}
			// APSL2864 end
			suitabilityContract.setSuitabilityRequiredData(buildSuitabilityRequiredData(currentRunMap));
			suitabilityContract.setSuitabilityResubmitData(buildSuitabilityResubmitData(changeSubTypeList)); // NBLXA2303[NBLXA-2304]
			suitabilityContract.setRunSuitability(runSuitability); // ALII1244
			persistSuitabilityProcessingContract(suitabilityContract);
		} // NBLXA-2303[NBLXA-2316] Begin
	}

	protected void persistSuitabilityProcessingContract(NbaSuitabilityProcessingContract suitabilityContract) throws NbaBaseException {
		if (insertNeeded(suitabilityContract)) {
			NbaSuitabilityProcessingAccessor.insert(suitabilityContract);
		} else {
			NbaSuitabilityProcessingAccessor.update(suitabilityContract);
		}
	}
		
	private boolean insertNeeded(NbaSuitabilityProcessingContract suitabilityContract) {
		return suitabilityContract.getEventDate() == null;
	}

	public void storeCurrentData(String key, String value) {		
		currentRunMap.put(key, value);
	}
	
	protected boolean dataDifferencesExist(Map priorRunMap) {
		if (priorRunMap == null) {
			return false;
		}

		if (currentRunMap.size() != priorRunMap.size()) {
			return true;
		}

		for (Iterator iter = priorRunMap.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			if (currentRunMap.containsKey(key)) {
				String value = (String) currentRunMap.get(key);
				if (!value.equals(priorRunMap.get(key))) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}
	
	
	//APSL2864 added new method
	/**
	 * If for a case appInfoExt.getSuitabilityDecisionStatus() was set to NBA_SUITABILITYDECISIONSTATUS_PASS in the first call to Suitability then a
	 * subsequent call to SRS would be made only if the following method returns 'true'
	 * @param priorRunMap
	 * @param policyStatus
	 * @return
	 * @throws ParseException 
	 */
	protected boolean subsequentCallNeeded(Map priorRunMap, long policyStatus) {
		double priorPaymentAmt = Double.NaN;
		double currentPaymentAmt = Double.NaN;
		long priorPaymentMode = -1;
		long currentPaymentMode = -1;
		String priorBirthDate = "";
		String currentBirthDate = "";
		long priorLTCReplacementIndCode = -1;
		long currentLTCReplacementIndCode = -1;
		boolean response = false;
		Date effectiveDate = NbaUtils.getDateFromStringInUSFormat(NbaConstants.MAR2019DEFFDATE); //NBLXA-2303[NBLXA-2454]

		for (Iterator iter = priorRunMap.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			//BEGIN NBLXA-2303[NBLXA-2473]
			if (key.contains(PAYMENT_AMT) && currentRunMap.containsKey(key)) {
				priorPaymentAmt = Double.parseDouble((String) priorRunMap.get(key));
				currentPaymentAmt = Double.parseDouble((String) currentRunMap.get(key));
				String paymentmodeId = resubmitMap.get(NbaConstants.PAYMENT_AMT_STR)+NbaConstants.HEIGHTWEIGHT_CODE_SYMBOL+PAYMENT_MODE;
				priorPaymentMode = Long.parseLong((String) priorRunMap.get(paymentmodeId));
				currentPaymentMode = Long.parseLong((String) currentRunMap.get(paymentmodeId));
				//END NBLXA-2303[NBLXA-2473]
				/*
				 * If a NaN value is retrieved for current amounts, it signifies omission, making the case NIGO. This situation is already handled by
				 * process_P004 of CV subset '9'. Hence a call to SRS is not required
				 */
				if (new Double(currentPaymentAmt).isNaN()) {
					response = false;
				}

				/*
				 * Converting NaN to 0 so that these values may be used in calculations and comparisons. If the previous values were NaN, they signify
				 * omission in the prior run, and are therefore treated as 0 for calculations
				 */
				priorPaymentAmt = new Double(priorPaymentAmt).isNaN() ? 0.0D : priorPaymentAmt;
				double annualPaymentAmtIncrease = convertToAnnualPaymentAmt(currentPaymentAmt, currentPaymentMode)
						- convertToAnnualPaymentAmt(priorPaymentAmt, priorPaymentMode);
				// Begin NBLXA2303[NBLXA-2304]
				if (annualPaymentAmtIncrease >= 600) { // NBLXA2303[NBLXA-2304]
					if (resubmitMap.get(NbaConstants.PAYMENT_AMT_STR) != null) {
						ChangeSubType changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_PREMAMT,
								String.valueOf(resubmitMap.get(NbaConstants.PAYMENT_AMT_STR)), NbaOliConstants.TC_CONTENT_UPDATE);
						if (changeSubType != null) {
							changeSubType.setElementName(NbaConstants.PAYMENT_AMT_STR);
							changeSubTypeList.add(changeSubType);
						}
						response = true;
					}
				}
				// Begin NBLXA2303[NBLXA-2304]
			} // Begin NBLXA2303[NBLXA-2312,NBLXA-2473]
			else if (key.contains(BIRTHDATE) && currentRunMap.containsKey(key) && key.contains((CharSequence) resubmitMap.get(NbaConstants.BIRTH_DATE))) {
				priorBirthDate = (String) priorRunMap.get(key);
				currentBirthDate = (String) currentRunMap.get(key);
				//END NBLXA-2303[NBLXA-2473]
				// Begin NBLXA-2324
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				SimpleDateFormat sd = new SimpleDateFormat("E MMM dd hh:mm:ss Z yyyy");
				Date oldBirthDate = null;
				Date newBirthDate = null;
				try {
					oldBirthDate = sdf.parse(priorBirthDate);
				} catch (ParseException e) {
					try {
						oldBirthDate = sd.parse(priorBirthDate);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}

				try {
					newBirthDate = sdf.parse(currentBirthDate);
				} catch (ParseException e) {
					try {
						newBirthDate = sd.parse(currentBirthDate);
					} catch (ParseException e1) {
						e1.printStackTrace();
					}
				}
				// End NBLXA-2324
				if (oldBirthDate != null && newBirthDate != null && oldBirthDate.compareTo(newBirthDate) != 0) { // NBLXA-2324
					if (resubmitMap.get(NbaConstants.BIRTH_DATE) != null) {
						ChangeSubType changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_CHGPARTICBIRTHDATE,
								String.valueOf(resubmitMap.get(NbaConstants.BIRTH_DATE)), NbaOliConstants.TC_CONTENT_UPDATE);
						if (changeSubType != null) {
							changeSubType.setElementName(NbaConstants.BIRTH_DATE);
							changeSubTypeList.add(changeSubType);
						}
						response = true;
					}
				} // End NBLXA2303[NBLXA-2312]

			}
		}
		// Begin NBLXA2303[NBLXA2316]
		if (!NbaUtils.isBlankOrNull(priorRunMap.get(LTCREPLACEMENT_IND_CODE))) {
			priorLTCReplacementIndCode = Long.parseLong((String) priorRunMap.get(LTCREPLACEMENT_IND_CODE));
		}
		if (!NbaUtils.isBlankOrNull(currentRunMap.get(LTCREPLACEMENT_IND_CODE))) {
			currentLTCReplacementIndCode = Long.parseLong((String) currentRunMap.get(LTCREPLACEMENT_IND_CODE));
		}

		if (priorLTCReplacementIndCode != currentLTCReplacementIndCode 
				&& !NbaUtils.isNegativeDisposition(getUnderwritingStatus())) {//NBLXA-2527
			if (resubmitMap.get(NbaConstants.LTCREPLACEMENT_IND_CODE) != null) {
				if (NbaUtils.isAnsweredYes(currentLTCReplacementIndCode)) {
					if(!(getLtcResubmitEffDate() != null && getLtcResubmitEffDate().before(effectiveDate) && priorRunMap.get(LTCREPLACEMENT_IND_CODE) == null)) { //NBLXA-2303[NBLXA-2454]
					ChangeSubType changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_OPTCHG,
							String.valueOf(resubmitMap.get(NbaConstants.LTCREPLACEMENT_IND_CODE)), NbaOliConstants.TC_CONTENT_INSERT);// NBLXA-2404[NBLXA2328]
					if (changeSubType != null) {
						changeSubType.setElementName(NbaConstants.LTCREPLACEMENT_IND_CODE);
						changeSubTypeList.add(changeSubType);
					}
						response = true;
					}
				} else if (priorRunMap.get(LTCREPLACEMENT_IND_CODE) != null) { //NBLXA-2303[NBLXA-2454]
					ChangeSubType changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_OPTCHG,
							String.valueOf(resubmitMap.get(NbaConstants.LTCREPLACEMENT_IND_CODE)), NbaOliConstants.TC_CONTENT_DELETE);// NBLXA-2404[NBLXA2328]
					if (changeSubType != null) {
						changeSubType.setElementName(NbaConstants.LTCREPLACEMENT_IND_CODE);
						changeSubTypeList.add(changeSubType);
					}
					response = true;
				}
			} else {
				ChangeSubType changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_OPTCHG, Holding_ID,
						NbaOliConstants.TC_CONTENT_DELETE);// NBLXA-2404[NBLXA2328]
				if (changeSubType != null) {
					changeSubType.setElementName(NbaConstants.LTCREPLACEMENT_IND_CODE);
					changeSubTypeList.add(changeSubType);
				}
				response = true;
			}
		}
		if (((priorRunMap.containsKey(COVOPT_LTC) && !currentRunMap.containsKey(COVOPT_LTC))
				|| (!priorRunMap.containsKey(COVOPT_LTC) && currentRunMap.containsKey(COVOPT_LTC)))
				&& !NbaUtils.isNegativeDisposition(getUnderwritingStatus())){//NBLXA-2527
			if (resubmitMap.get(NbaConstants.LifeCovOptTypeCode) != null) {
				if(!(getLtcResubmitEffDate() != null && getLtcResubmitEffDate().before(effectiveDate))) { //NBLXA-2303[NBLXA-2454]
					ChangeSubType changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_OPTCHG,
							String.valueOf(resubmitMap.get(NbaConstants.LifeCovOptTypeCode)), NbaOliConstants.TC_CONTENT_INSERT);// NBLXA-2404[NBLXA2328]
					if (changeSubType != null) {
						changeSubType.setElementName(NbaConstants.LifeCovOptTypeCode);
						changeSubTypeList.add(changeSubType);
					}
					response = true;
				}
			} else {
				ChangeSubType changeSubType = NbaUtils.createChangeSubType(NbaOliConstants.OLI_CHG_OPTCHG, getBaseCovId(),
						NbaOliConstants.TC_CONTENT_DELETE);// NBLXA-2404[NBLXA2328,NBLXA-2473]
				if (changeSubType != null) {
					changeSubType.setElementName(NbaConstants.LifeCovOptTypeCode);
					changeSubTypeList.add(changeSubType);
				}
				response = true;
			}
		}
		// End NBLXA2303[NBLXA2316]
		return response;
	}

	//APSL2864 added new method
	/**
	 * This method calculates the annual paymentAmt based on the paymentMode
	 * If the paymentMode argument value is anything other than SEMI_ANNUAL, QUATERLY or MONTHLY it is considered ANNUAL
	 * @param paymentAmt
	 * @param paymentMode
	 * @return
	 */
	protected double convertToAnnualPaymentAmt(double paymentAmt, long paymentMode){
		
		if(paymentMode == SEMI_ANNUAL)
			return paymentAmt * 2;
		else if(paymentMode == QUATERLY)
			return paymentAmt * 4;
		else if(paymentMode == MONTHLY) 
			return paymentAmt * 12; 
		else 
			return paymentAmt;
	}
	
	protected Map parseSuitabilityRequiredData(String xml) throws Exception {
		Map map = new HashMap();
		if (xml == null) {
			return null;
		}
		
		Document document = new SAXReader().read(new StringReader(xml));
		Element root = document.getRootElement();
        for ( Iterator i = root.elementIterator(SUITABILITY_FIELD); i.hasNext(); ) {
            Element element = (Element) i.next();
            map.put(element.element(NAME).getData(), element.element(DATA).getData());
        }
        return map;
	}

	protected String buildSuitabilityRequiredData(Map map) {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(SUITABILITY_FIELDS);
		Iterator entries = map.entrySet().iterator();
		while (entries.hasNext()) {
		    Map.Entry entry = (Map.Entry) entries.next();
		    Element element = root.addElement(SUITABILITY_FIELD);
			element.addElement(NAME).addText((String)entry.getKey());
			element.addElement(DATA).addText((String)entry.getValue());
		}		
		return root.asXML();
	}

	//NBLXA2303[NBLXA-2304]
	public static List<ChangeSubType> parseSuitabilityResubmitData(String xml) throws Exception {
		List<ChangeSubType> changeList = new ArrayList<ChangeSubType>();
		if (xml == null) {
			return null;
		}		
		Document document = new SAXReader().read(new StringReader(xml));
		Element root = document.getRootElement();
        for (Iterator i = root.elementIterator(SUITABILITY_FIELD); i.hasNext(); ) {
            Element element = (Element) i.next();
            ChangeSubType changeSubType = new ChangeSubType();
            changeSubType.setChangeID((String)element.element(ID).getData());
            changeSubType.setChangeTC((String)element.element(DATA).getData());
            changeSubType.setElementName((String)element.element(NAME).getData());
            changeSubType.setTranContentCode((String)element.element(MODE).getData());
            changeList.add(changeSubType);
        }
        return changeList;
	}
	
	//NBLXA2303[NBLXA-2304] New Method
	protected String buildSuitabilityResubmitData(List<ChangeSubType> changeSubTypeList) {
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement(SUITABILITY_FIELDS);
		for (ChangeSubType changeSubType: changeSubTypeList) {
			Element element = root.addElement(SUITABILITY_FIELD);
			element.addElement(ID).addText(changeSubType.getChangeID());
			element.addElement(NAME).addText(changeSubType.getElementName());
			element.addElement(DATA).addText(Long.toString(changeSubType.getChangeTC()));
			element.addElement(MODE).addText(Long.toString(changeSubType.getTranContentCode()));			
		}	
		return root.asXML();
	}
	
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractLock.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaSuitabilityCVResultsProcessor could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	//NBLXA2303[NBLXA-2304]
	protected void addDataChangeToList(ChangeSubType changeSubType) {
		changeSubTypeList.add(changeSubType);		
	}
	
	//NBLXA2303[NBLXA-2304]
	protected void storeResubmitData(String key, String value) {
		resubmitMap.put(key,value);	
	}
	
	//NBLXA-2303[NBLXA-2454]
	public Date getLtcResubmitEffDate() {
		return ltcResubmitEffDate;
	}
	
	//NBLXA-2303[NBLXA-2454]
	public void setLtcResubmitEffDate(Date ltcResubmitEffDate) {
		this.ltcResubmitEffDate = ltcResubmitEffDate;
	}
	
	public String getBaseCovId() {
		return baseCovId;
	}

	public void setBaseCovId(String baseCovId) {
		this.baseCovId = baseCovId;
	}

	public long getUnderwritingStatus() {
		return underwritingStatus;
	}

	public void setUnderwritingStatus(long underwritingStatus) {
		this.underwritingStatus = underwritingStatus;
	}
}