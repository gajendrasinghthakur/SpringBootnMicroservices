package com.csc.fsg.nba.contract.validation;
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
 * 
 * *******************************************************************************<BR>
 */
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.database.NbaAutoClosureAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAutoClosureContract;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.dashboard.AxaDataChangeVO;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ClientExtension;
import com.csc.fsg.nba.vo.txlife.ContractChangeInfo;
import com.csc.fsg.nba.vo.txlife.ExpenseNeedTypeCodeCC;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Intent;
import com.csc.fsg.nba.vo.txlife.IntentExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
/**
 * NbaValSuitability performs Miscellaneous contract validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version 1201</td><td>Suitability</td></tr>
 * <tr><td>P2AXAL068</td><td>AXA Life Phase 2</td><td>Group Insurance Contract Validations</td></tr>
 * <tr><td>ALII1244</td><td>AXA Life Phase2</td><td>QC 8161 - Temp Exp Case: WI got created for UWCM due to NBMISCWORK WI with status "Suitability went from IGO to NIGO" but case never IGO.</td></tr>
 * <tr><td>P2AXAL021</td><td>AXA Life Phase2</td><td>AXA Suitability</td></tr>
 * <tr><td>APSL2864</td><td>AXA Life Phase2</td><td>CR1455066 Life 2012  nbA Phase 2.1 & 2.2 Suitability Rule Changes.</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 1201
 * @since New Business Accelerator - Version 1201
 */
public class NbaValSuitability extends NbaContractValidationCommon implements NbaContractValidationBaseImpl, NbaContractValidationImpl, NbaOliConstants { 
	private static final String DELIMITER = ".";
	private NbaSuitabilityCVResultsProcessor resultsProcessor;
	//P2AXAL068 Code moved to NbaContractValidationCommon

	/**
	 * Perform one time initialization.
	 */
	public void initialze(NbaDst nbaDst, NbaTXLife nbaTXLife, Integer subset, NbaOLifEId nbaOLifEId, AccelProduct nbaProduct, NbaUserVO userVO) { 
		super.initialze(nbaDst, nbaTXLife, subset, nbaOLifEId, nbaProduct, userVO); 
		initProcesses();
		Method[] allMethods = this.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("process_")) {
				processes.put(aMethodName.substring(8).toUpperCase(), aMethod);
			}
		}
		resultsProcessor = (NbaSuitabilityCVResultsProcessor)ServiceLocator.lookup(NbaSuitabilityCVResultsProcessor.SUITABILITY_CVRESULTS_PROCESSOR);
		//NBLXA-2303[NBLXA-2454]
		try {
			resultsProcessor.setLtcResubmitEffDate(getLTCReqSourceDate());
			resultsProcessor.setBaseCovId(getBaseCoverageId());//NBLXA-2303[NBLXA-2473]
			if(!NbaUtils.isBlankOrNull(getApplicationInfoExtension())) { //NBLXA-2527
				resultsProcessor.setUnderwritingStatus(getApplicationInfoExtension().getUnderwritingStatus());
			}	
		} catch (NbaBaseException e) {
			addNewSystemMessage(0,e.getMessage(), "");
		} //NBLXA-2303[NBLXA-2454]
		//P2AXAL068 Code moved to NbaContractValidationCommon
	}


	/**
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationImpl#validate()
	 */
	public void validate(ValProc nbaConfigValProc, ArrayList objects) {
		if (nbaConfigValProc.getUsebase()) { 
			super.validate(nbaConfigValProc, objects);
		} else { 
		    if (getUserImplementation() != null) {
		        getUserImplementation().validate(nbaConfigValProc, objects);
		    }
		}     
	}
	
	/**
	 * Reset Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.QualifyForSuitabilityInd to false
	 */
	//NBA297 New Method 
	protected void process_P001() {
		if (!verifyCtl(APPLICATIONINFO)) return;
		logDebug("Performing NbaValBasic.process_P001()");
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
		if (appInfoExt != null && !appInfoExt.isActionDelete()) {
			appInfoExt.setQualifyForSuitabilityInd(false); 
			appInfoExt.setActionUpdate();
		}
	}
	
	
	/**
	 * Set Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.QualifyForSuitabilityInd 
	 * true if Holding.Policy.ApplicationInfo.ApplicationType = 1, 3, 4, 5 or 6 AND Holding.Policy.ProductType = 4, 5 or 10
	 */
	//NBA297 New Method 
	protected void process_P002() {
		if (!verifyCtl(APPLICATIONINFO)) return;
		logDebug("Performing NbaValBasic.process_P002()");
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
		if (appInfoExt != null && !appInfoExt.isActionDelete()) {
			appInfoExt.setQualifyForSuitabilityInd(true); 
			appInfoExt.setActionUpdate();
		}
	}
	

	/**
	 * Set ApplicationInfoExtension.PriorSuitabilityIGOStatusInd equal to ApplicationInfoExtension.ReadyForSuitabilityInd.
	 * Set Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.ReadyForSuitabilityInd to true.
	 */
	protected void process_P003() {
		if (!verifyCtl(APPLICATIONINFO)) return;
		logDebug("Performing NbaValSuitability.process_P003 for " ,  getApplicationInfo());			
		ApplicationInfoExtension appInfoExt = getApplicationInfoExtension();
		appInfoExt.setPriorSuitabilityIGOStatusInd(appInfoExt.getReadyForSuitabilityInd());
		appInfoExt.setReadyForSuitabilityInd(true);
		appInfoExt.setActionUpdate();
	}
	
	/**
	 * If field is blank, zero or negative 1, generate error, and set
	 * Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.ReadyForSuitabilityInd to false
	 */
	protected void process_P004() {
	    Object value = null;
	    Object obj = getLastControlObject();		
		logDebug("Performing NbaValSuitability.process_P004 for ", (NbaContractVO) obj);
	    	try {
			//P2AXAL068 Code moved to NbaContractValidationCommon
			value = getFieldValue(obj);
		} catch (Exception ex) {
			addNewSystemMessage(INVALID_CTL_ID, concat("Process ", getNbaConfigValProc().getId(), " has an invalid field: ", getNbaConfigValProc()
					.getField()), "");
	    		return;
	    	}
	    
		if (getCVValidator().isOmitted(value)) { //P2AXAL068
    		setReadyForSuitabilityInd(false);
    		addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf((NbaContractVO) getLastControlObject()));
    	}
    	
		//APSL2893 Begin
		if (value != null
				&& !((value instanceof ArrayList) && (((ArrayList) value).size() > 0) && ((ArrayList) value).get(0) instanceof NbaContractVO)
				&& !(value instanceof NbaContractVO)) {
			resultsProcessor.storeCurrentData(formatDataKey(obj), (value == null) ? "null" : value.toString());
			if(isResubmitField(obj, getNbaConfigValProc().getField())) {
				resultsProcessor.storeResubmitData(getNbaConfigValProc().getField(),getIdOf((NbaContractVO) getLastControlObject())); //NBLXA2303[NBLXA-2304]	
			}
		}
		//APSL2893 End
	}
	
	//NBLXA2303
	private String formatKey(Object obj) {
    	String ctl = getNbaConfigValProc().getCtl();
    	String field = getNbaConfigValProc().getField();
    	StringBuffer key = new StringBuffer().append(ctl).append(DELIMITER).append(field);
    	return key.toString();
	}
	
	
	//P2AXAL068 Code moved to NbaContractValidationCommon
	
	private String formatDataKey(Object obj) {
    	String id = getIdOf((NbaContractVO) obj);
    	String ctl = getNbaConfigValProc().getCtl();
    	String field = getNbaConfigValProc().getField();
    	StringBuffer key = new StringBuffer(id).append(DELIMITER).append(ctl).append(DELIMITER).append(field);
    	return key.toString();
	}
	
	/**
	 * If <MessageCode tc="5"/> generate error, and set Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.ReadyForSuitability to false
	 */
	protected void process_P005() {
		if (!verifyCtl(SYSTEMMESSAGE))
			return;
		logDebug("Performing NbaValSuitability.process_P005 for ", getSystemMessage());
		setReadyForSuitabilityInd(false);
		addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getSystemMessage().getMessageCode() + "", null);
	}	
	
	/**
	 * Loop through each requirement info object and check for the presence of RequirementInfo.ReqCode="165", and if present for EACH one if Reqsat is false, generate error, and set Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.ReadyForSuitabilityInd to false
	 */
	protected void process_P006() {
		if (!verifyCtl(REQUIREMENTINFO)) {
			return;
		}
		logDebug("Performing NbaValSuitability.process_P006");
		setReadyForSuitabilityInd(false);
		String formNumber = (getRequirementInfo().getFormNo() == null) ? "" : getRequirementInfo().getFormNo();
		addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " "+ formNumber, getRequirementInfo().getId());//ALII1051
	}	
	
	/**
	 * @throws NbaBaseException
	 * Calculate an auto closure date as 4 days added to the current system date.  The calculated result will be stored in the nbA Auxilliary table (CLOSURE_DATE) and the pending closure date in the pending contract database (Holding.Policy.ApplicationInfo.PlacementEndDate).
	 * Note: number of days should be put in a new tag in the configuration entry.
	 */
	protected void process_P007() throws NbaBaseException {
		if (!verifyCtl(APPLICATIONINFO)) {
			return;
		}
		logDebug("Performing NbaValSuitability.process_P007");
		//ALII1406 Begin
		ApplicationInfo appInfo = getApplicationInfo();
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);

		if (!appInfoExtn.hasClosureOverrideInd()) {
			appInfoExtn.setClosureOverrideInd(FALSE);//defalut value
		}

		String printExtractGenerated = "No";
		if (appInfoExtn.hasContractPrintExtractDate()) {
			printExtractGenerated = "Yes";
		}
		if (!appInfoExtn.hasClosureOverrideInd()) {
			appInfoExtn.setClosureOverrideInd(FALSE);//defalut value
		}

		if (ClosureCalculationRequired(printExtractGenerated, appInfoExtn.getClosureOverrideInd(), appInfo.getApplicationType(), appInfoExtn
				.getUnderwritingApproval())) {//ALII1406 End
			Date autoClosureDate = calculateClosureDate();
			if (autoClosureDate == null) {
				return;
			}
			//Update the auxiliary database
			NbaAutoClosureContract autoClosureContract = new NbaAutoClosureContract();
			autoClosureContract.setClosureDate(autoClosureDate);
			autoClosureContract.setContractNumber(getPolicy().getPolNumber());
			NbaAutoClosureAccessor.resetClosureDate(autoClosureContract);
			// BEGIN NBLXA-2155[NBLXA-2196]
			if (!nbaTXLife.isUnderwriterApproved()) { // NBLXA-2452 Begin
				RequirementInfo reqInfo = null;
				Date appSubmitDate = getApplicationInfo().getSubmissionDate();
				if (NbaUtils.isTermCase(getPolicy())) {
					reqInfo = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_PREMIUMQUOTE);
				} else {
					reqInfo = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_SIGNILLUS);
				}
				if (appInfo.getApplicationJurisdiction() == NbaOliConstants.OLI_USA_NY && !NbaUtils.isBlankOrNull(reqInfo)
						&& reqInfo.getReqStatus() != NbaOliConstants.OLI_REQSTAT_RECEIVED
						&& NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.ILLUSCV_START_DATE)) { // NBLXA-2155[NBLXA-2300]
					autoClosureDate = NbaUtils.addDaysToDate(appSubmitDate, 10);
				}
				// END NBLXA-2155[NBLXA-2196]
				// Start NBLXA-2155[NBLXA-2202]
				if (appInfo.getApplicationJurisdiction() == OLI_USA_TX) {
					long planType = nbaTXLife.getPolicy().getProductType();
					RequirementInfo signedIllustration = reqInfo = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_SIGNILLUS);
					if (planType == OLI_PRODTYPE_VUL) {
						if (!NbaUtils.isBlankOrNull(signedIllustration) && signedIllustration.getReqStatus() != OLI_REQSTAT_RECEIVED
								&& NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.ILLUSCV_START_DATE)) { // NBLXA-2155[NBLXA-2300]
							autoClosureDate = NbaUtils.addDaysToDate(getApplicationInfo().getSignedDate(), 90);
						}
					} else if (AxaUtils.isPermProduct(planType)) {// NBLXA-2155[NBLXA-2258]
						if (!NbaUtils.isProductCodeCOIL(getNbaTXLife())) {
							RequirementInfo illustrationCertificate = NbaUtils.getLatestReqInfo(nbaTXLife, OLI_REQCODE_1009800011);
							if ((!NbaUtils.isBlankOrNull(illustrationCertificate) && illustrationCertificate.getReqStatus() != OLI_REQSTAT_RECEIVED)
									|| (!NbaUtils.isBlankOrNull(signedIllustration) && signedIllustration.getReqStatus() != OLI_REQSTAT_RECEIVED)
											&& NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.ILLUSCV_START_DATE)) { // NBLXA-2155[NBLXA-2300]
								autoClosureDate = NbaUtils.addDaysToDate(getApplicationInfo().getSignedDate(), 90);
							}
						}
					}
				}
			} // NBLXA-2452 End
			// End NBLXA-2155[NBLXA-2202]
			//Update the pending database
			getApplicationInfo().setPlacementEndDate(autoClosureDate);
			setPendingContractStatus();//ALII1406
			getApplicationInfo().setActionUpdate();  
		}	
	}
//	ALII1356 New Method
	protected void process_P008() {
		if (!verifyCtl(PARTY))
			return;
		logDebug("Performing NbaValSuitability.process_P008 for ", getParty());
		boolean addMessage = false;
		//Relation authRelation = getNbaTXLife().getRelationForRelationRoleCode(OLI_REL_DESRESPDIR, true);  //NBLXA1254 
		Relation authRelation = null;//NBLXA1254 
		if (authRelation == null) {
			authRelation = getNbaTXLife().getRelationForRelationRoleCode(OLI_REL_POWEROFATTRNY, true);
		}		
		if (authRelation == null) {
			addMessage = true;
		} else if ((getParty().getId()).equals(authRelation.getOriginatingObjectID())) {
			NbaParty nbaParty = getNbaTXLife().getParty(authRelation.getRelatedObjectID());
			if (nbaParty != null && nbaParty.getPerson() != null) {
				Person person = nbaParty.getPerson();
				if (NbaUtils.isBlankOrNull(person.getTitle())) {
					addMessage = true;
				}
			}
		}
		if (addMessage) {
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", null);
			setReadyForSuitabilityInd(false);
		}
	}	
	
	/**
	 * If <MessageCode tc="5"/> generate error, and set Holding.Policy.ApplicationInfo.OLifEExtension.ApplicationInfoExtension.ReadyForSuitability to false
	 */
	//P2AXAL021-FUN80839 New Method
	//ALII2070 - THIS CV PROCESS IS NO LONGER CONFIGURED IN NbaConfig.xml
	protected void process_P009() {
		if (!verifyCtl(APPLICATIONINFO))
			return;
		logDebug("Performing NbaValSuitability.process_P009 for ", getApplicationInfoExtension());
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
		if (null != appInfoExt && appInfoExt.hasSuitabilityDecisionStatus()) {
			if (!appInfoExt.getReadyForSuitabilityInd()) {
				appInfoExt.setSuitabilityDecisionStatus(NbaOliConstants.NBA_SUITABILITYDECISIONSTATUS_INVALID);
				appInfoExt.setActionUpdate();
			}
		}
	}
	
	//NBLXA2303[NBLXA2316]
	protected void process_P010() {
		Object value = null;
		Object obj = getLastControlObject();
		logDebug("Performing NbaValSuitability.process_P010 for ", (NbaContractVO) obj);
		try {
			value = getFieldValue(obj);

		} catch (Exception ex) {
			addNewSystemMessage(INVALID_CTL_ID,
					concat("Process ", getNbaConfigValProc().getId(), " has an invalid field: ", getNbaConfigValProc().getField()), "");
			return;
		}
		
		if (value != null
				&& !((value instanceof ArrayList) && (((ArrayList) value).size() > 0) && ((ArrayList) value).get(0) instanceof NbaContractVO)
				&& !(value instanceof NbaContractVO)) {
			resultsProcessor.storeCurrentData(formatKey(obj), (value == null) ? "null" : value.toString());
			if(isResubmitField(obj, getNbaConfigValProc().getField())) {
				resultsProcessor.storeResubmitData(getNbaConfigValProc().getField(), getIdOf((NbaContractVO) getLastControlObject())); // NBLXA2303[NBLXA-2304]
		}
	}
	}

	
	//NBLXA2303[NBLXA2316]
	protected void process_P011() {
		Object value = null;
		Object obj = null;
		List<Object> ltcHolds = NbaUtils.getLTCHoldingList(getNbaTXLife());
		if (!ltcHolds.isEmpty()) {
			for (int i = 0; i < ltcHolds.size(); i++) {
				Holding hold = (Holding) ltcHolds.get(i);
				if (hold != null && hold.getPolicy() != null) {
					obj = hold.getPolicy();
				}
			}
		}
		try {
			if (obj != null) {
				value = getFieldValue(obj);
			}

		} catch (Exception ex) {
			addNewSystemMessage(INVALID_CTL_ID,
					concat("Process ", getNbaConfigValProc().getId(), " has an invalid field: ", getNbaConfigValProc().getField()), "");
			return;
		}

		if (value != null
				&& !((value instanceof ArrayList) && (((ArrayList) value).size() > 0) && ((ArrayList) value).get(0) instanceof NbaContractVO)
				&& !(value instanceof NbaContractVO)) {
			resultsProcessor.storeCurrentData(formatKey(obj), (value == null) ? "null" : value.toString());
			if(isResubmitField(obj, getNbaConfigValProc().getField())) {
				resultsProcessor.storeResubmitData(getNbaConfigValProc().getField(), getIdOf((NbaContractVO) obj)); // NBLXA2303[NBLXA-2304]
			}

		}
	}
	
	/**
	 * @throws NbaBaseException
	 * ALWAYS RUN ME LAST!!!!!!! Evaluate the results of the current invocation against the last
	 */
	protected void process_P999() throws Exception {
		logDebug("Performing NbaValSuitability.process_P999()");
		Policy policy = nbaTXLife.getPolicy();
		//APSL2864 begin 
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(policy.getApplicationInfo());
		long suitabilityDecisionStatus = Long.MIN_VALUE;
		if (null != appInfoExt && appInfoExt.hasSuitabilityDecisionStatus()) {
			suitabilityDecisionStatus = appInfoExt.getSuitabilityDecisionStatus();
		}//APSL2864 end
		//APSL2864 added suitabilityDecisionStatus and policyStatus to the call of processCVResults
		resultsProcessor.processCVResults(policy.getCarrierCode(), policy.getPolNumber(), invokeSuitability(), suitabilityDecisionStatus, policy.getPolicyStatus()); //ALII1244
	}

	/**
	 * Sets the ReadyForSuitabilityInd.
	 * @param readyForSuitabilityInd The readyForSuitabilityInd to set
	 */
	protected void setReadyForSuitabilityInd(boolean readyForSuitabilityInd) {
		ApplicationInfoExtension appInfoExt= getApplicationInfoExtension();
		appInfoExt.setReadyForSuitabilityInd(readyForSuitabilityInd);
		appInfoExt.setActionUpdate();
	}
		
	/**
	 * Calculate the closure date based on VPMS rules.
	 * @throws NbaBaseException 
	 */
	protected Date calculateClosureDate() throws NbaBaseException {
		Date result = null;
		Map skipAttributesMap = new HashMap();
		skipAttributesMap.put(NbaVpmsConstants.A_PROCESS_ID, getNbaTXLife().getBusinessProcess());
		//QC16536/APSL4623
		ApplicationInfo appInfo = getApplicationInfo();
		ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(appInfo);
		//QC16536/APSL4623
		//QC11621-APSL3588 Start
		NbaDst CntChgWI = null;
		Date contractChangeDate = null;
		if (nbaTXLife.unpaidReissue()) {
			skipAttributesMap.put("A_UnpaidReissue", nbaTXLife.unpaidReissue() ? "yes" : "no");
			CntChgWI = super.searchWI(NbaConstants.A_WT_CONTRACT_CHANGE);
			if (!NbaUtils.isBlankOrNull(CntChgWI)) {
				contractChangeDate = NbaUtils.getDateFromStringInAWDFormat(CntChgWI.getNbaLob().getCreateDate());
				skipAttributesMap.put("A_ContractChangeDate", NbaUtils.getStringFromDate(contractChangeDate));
			}
			//Begin APSL5349
			ContractChangeInfo latestContractChange = NbaUtils.getLatestValidContractChangeInfo(getNbaTXLife());
			if (!NbaUtils.isBlankOrNull(latestContractChange)) {
				List<Activity> activityList = getNbaTXLife().getOLifE().getActivity();
				List<Activity> reissueActivityList = NbaUtils.getActivityByTypeCodeAndRelatedObjId(activityList,
						NbaOliConstants.OLI_ACTTYPE_CONTRACTCHANGE, latestContractChange.getId());
				Activity activity = NbaUtils.getActivityByStatus(reissueActivityList, NbaOliConstants.OLIEXT_LU_ACTSTAT_INITIATED);

				if (!NbaUtils.isBlankOrNull(activity)) {
					contractChangeDate = activity.getStartTime().getTime();
					skipAttributesMap.put("A_ContractChangeDate", NbaUtils.getStringFromDate(contractChangeDate));
				}

			}
			//End APSL5349
			if (!NbaUtils.isBlankOrNull(latestContractChange) || !NbaUtils.isBlankOrNull(CntChgWI)) { 
				// QC16536/APSL4623
				boolean ind = false;
				if (appInfoExtn.hasContractPrintExtractDate()) {
					int compareResult = NbaUtils.compare(appInfoExtn.getContractPrintExtractDate(), contractChangeDate); //APSL5349
					if (compareResult >= 0
							|| (appInfoExtn.hasContractReprintDate() && NbaUtils.compare(appInfoExtn.getContractReprintDate(), contractChangeDate) > 0)) {//APSL5349
						ind = true;
						if (appInfoExtn.hasContractReprintDate()) {
							skipAttributesMap.put("A_ContractPrintDate", NbaUtils.getStringFromDate(appInfoExtn.getContractReprintDate()));
						}
					}
					skipAttributesMap.put("A_NewPrintInd", ind ? "yes" : "no");
					if (ind == false)
						skipAttributesMap.put("A_PrintExtractGenerated", "no");// if print is not a new one set printextracted as false
				}
				// QC16536/APSL4623
			}
		}
		
		//QC11621-APSL3588 End		
		//QC16536/APSL4623
		String printExtractGenerated = "No";
		if(appInfoExtn.hasContractPrintExtractDate()){
			printExtractGenerated= "Yes";
		}

		skipAttributesMap.put("A_PrintExtractGenerated", printExtractGenerated);
		skipAttributesMap.put("A_ContractPrintDate", NbaUtils.getStringFromDate(appInfoExtn.getContractPrintExtractDate()));
		skipAttributesMap.put("A_ContractReprintDate", NbaUtils.getStringFromDate(appInfoExtn.getContractReprintDate()));
		skipAttributesMap.put("A_ContractChangeReprintInd", getPolicyExtension().getContractChangeReprintInd()? "true": "false");
		skipAttributesMap.put("A_DistributionChannel",String.valueOf(getPolicyExtension().getDistributionChannel()));
		skipAttributesMap.put("A_InformalOfferMade", NbaUtils.isInformalOfferMade(appInfoExtn)? "1": "0"); 
		skipAttributesMap.put("A_InformalReceiptDate", NbaUtils.getStringFromDate(NbaUtils.getDateFromStringInAWDFormat(getNbaDst().getNbaLob().getCreateDate())));
		skipAttributesMap.put("A_ContractApprovedDate", NbaUtils.getStringFromDate(appInfo.getHOCompletionDate()));
		//QC16536/APSL4623

		NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(skipAttributesMap); //Get the closure date
		if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
			String closureDate = (String) nbaVpmsResultsData.getResultsData().get(0);
			if (NbaVpmsConstants.IGNORE != closureDate) {
				try {
					result = get_YYYY_MM_DD_sdf().parse(closureDate);
				} catch (ParseException e) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Unknown date format: ", closureDate),
							getIdOf(getApplicationInfo()));
				}
			}
		} else {
			addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), ", Model: ", getNbaConfigValProc().getModel()),
					getIdOf(getApplicationInfo()));
		}
		return result;
	}

	/**
	 * "Verify on APP for power of attorney,If owner is entity Suit NIGO CV:7939 will be generated for 3 fields(first name,Last name,Title). Which
	 * will not allow the case to move to SRS until this CV will resolve.
	 */
	// QC17753-APSL5008 Start
	protected void process_APSL5008() {
		if (!verifyCtl(RELATION))
			return;
		logDebug("Performing NbaValSuitability.process_APSL5008 for ", getParty());
		Relation authRelation = getNbaTXLife().getRelationForRelationRoleCode(OLI_REL_POWEROFATTRNY, true);
		Relation ownerRelation = getNbaTXLife().getRelationForRelationRoleCode(OLI_REL_OWNER, true);
		if (!getNbaTXLife().isOwnerSameAsInsured()) {
			NbaParty nbaPartyForOwner = null;
			if (ownerRelation != null) {
				nbaPartyForOwner = getNbaTXLife().getParty(ownerRelation.getRelatedObjectID());
				if (authRelation != null) {
					NbaParty nbaPartyForPowerOfAtorney = getNbaTXLife().getParty(authRelation.getRelatedObjectID());
					if ((nbaPartyForOwner != null)) {
						if (nbaPartyForOwner.isOrganization() && nbaPartyForPowerOfAtorney != null) {
							if (NbaUtils.isBlankOrNull(nbaPartyForPowerOfAtorney.getPerson().getTitle())) {
								addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " Title is missing - Suitability NIGO", null);
								setReadyForSuitabilityInd(false);
							}
							if (NbaUtils.isBlankOrNull(nbaPartyForPowerOfAtorney.getPerson().getFirstName())) {
								addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " First name is missing - Suitability NIGO", null);
								setReadyForSuitabilityInd(false);
							}
							if (NbaUtils.isBlankOrNull(nbaPartyForPowerOfAtorney.getPerson().getLastName())) {
								addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " Last name is missing - Suitability NIGO", null);
								setReadyForSuitabilityInd(false);
							}
						}
					}
				} else {
					if (nbaPartyForOwner != null) {
						if (nbaPartyForOwner.isOrganization()) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " Title is missing - Suitability NIGO", null);
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " First name is missing - Suitability NIGO", null);
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " Last name is missing - Suitability NIGO", null);
							setReadyForSuitabilityInd(false);
						}
					}
				}
			}
		}
	}
	//QC17753-APSL5008 End
	
	
	/*
	 * Method determines if suitability Indicator should be invoked.
	 * If currently is IGO, then set to true
	 * If currently NOT IGO but it was previously, then run (misc. work will generate)
	 * otherwise false (was never IGO)
	 */
	//ALII1244 New Method
	private boolean invokeSuitability() {
		
		if (isIGO()) {
			return true;
		}
		if (!isIGO() && wasIGO()) {
			return true;
		}
		return false;
		
	}
	//ALII1244 New Method
	private boolean isIGO() {
		return getApplicationInfoExtension().getReadyForSuitabilityInd();
	}
	//ALII1244 New Method
	private boolean wasIGO() {
		return getApplicationInfoExtension().getPriorSuitabilityIGOStatusInd();
	}
	
	//NBLXA-188 Start
	/**
	 * If Distribution Channel is Retail (10) and  ProductType is either VUL or IUL (4 or 5), a new process in contract validation will be set up to validate if question 3 on Business Insurance section of FP Cert page is NULL generate the nbA Error Message 
	 * Application question Omitted: variable text: Total Assets on FP Cert
	 * 
	 * <ValProc id="LD117" msgcode="7950" ctl="PARTY" filter="+GROUPAPP" usebase="true" restrict="4"/>
	 */
	protected void process_LD117(){
		if (verifyCtl(CLIENT)) {
			logDebug("Performing NbaValSuitability.process_LD117 for ", getParty());
			ClientExtension clientExtension = getClientExtension();
			PolicyExtension policyExtension = getPolicyExtension();
			Double totalAssets = clientExtension.getTotalAssets();
			if(Double.isNaN(totalAssets)){
				totalAssets = new Double(0);
			}
			
			if(policyExtension!=null && DISTIBUTION_CHANNEL_RETAIL==policyExtension.getDistributionChannel() &&
					clientExtension!=null && totalAssets==0){
				setGenerateRequirement(true);
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), ":Total Assets on FP Cert", getParty().getId());
			}
		}
	}
	/**
	 * If Distribution Channel is Retail (10) and  ProductType is either VUL or IUL (4 or 5), a new process in contract validation will be set up to validate if Total Assets is less than $50 million and question 3b on Business Insurance section of FP Cert page is null  then generate the nbA Error Message 
	 * Application question Omitted: variable text: Net income on FP Cert
	 * 
	 * <ValProc id="LD118" msgcode="7950" ctl="PARTY" filter="+GROUPAPP" usebase="true" restrict="4"/>
	 */
	protected void process_LD118(){
		if (verifyCtl(CLIENT)) {
			logDebug("Performing NbaValSuitability.process_LD118 for ", getParty());
			ClientExtension clientExtension = getClientExtension();
			double totalAssets = clientExtension.getTotalAssets();
			if(!clientExtension.hasTotalAssets()){
				totalAssets = new Double(0);
			}
			if(clientExtension!=null && ASSET_FIFTY_MILLION >= totalAssets 
					&& NbaUtils.isBlankOrNull(clientExtension.getNetIncome())){
				setGenerateRequirement(true);
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), ":Net income on FP Cert", getParty().getId());
			}
		}
	}
	/**
	 * If Distribution Channel is Retail (10) and  ProductType is either VUL or IUL (4 or 5), a new process in contract validation will be set up to validate if Total Assets is less than $50 million and question 3b on Business Insurance section of FP Cert page is null  then generate the nbA Error Message 
	 * Application question Omitted: variable text: Net worth on FP Cert
	 * 
	 * <ValProc id="LD119" msgcode="7950" ctl="PARTY" filter="+GROUPAPP" usebase="true" restrict="4"/>
	 */
	protected void process_LD119(){
		if (verifyCtl(CLIENT)) {
			logDebug("Performing NbaValSuitability.process_LD119 for ", getParty());
			ClientExtension clientExtension = getClientExtension();
			double totalAssets = clientExtension.getTotalAssets();
			if(!clientExtension.hasTotalAssets()){
				totalAssets = new Double(0);
			}
			if(clientExtension!=null && ASSET_FIFTY_MILLION >= totalAssets 
					&& NbaUtils.isBlankOrNull(clientExtension.getNetWorth())){
				setGenerateRequirement(true);
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), ":Net worth on FP Cert", getParty().getId());
			}
		}
	}
	/**
	 * <ValProc id="LD120" msgcode="7950" ctl="PARTY" filter="+GROUPAPP" usebase="true" restrict="4" />
	 * */
	protected void process_LD120(){
		if (verifyCtl(PARTY)) {
			logDebug("Performing NbaValSuitability.process_LD120 for ", getParty());
			boolean error = true;
			List intentsList = getNbaTXLife().getIntentForParty(getParty().getId());
			for (int i = 0; i < intentsList.size(); i++) {
				Intent intent = (Intent) intentsList.get(i);
				IntentExtension intentExt = NbaUtils.getIntentExtension(intent);
				if (intentExt != null) {
					ExpenseNeedTypeCodeCC expNeedTypeCodeCC = intentExt.getExpenseNeedTypeCodeCC();
					if (expNeedTypeCodeCC != null && expNeedTypeCodeCC.getExpenseNeedTypeCodeCount() > 0) {
						error = false;
						break;
					}
				}
			}
			if (error) {
				setGenerateRequirement(true);
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), ":Purpose on FP Cert" , getParty().getId());
			}
		}
	}
	//NBLXA-188 End
	
	
}
