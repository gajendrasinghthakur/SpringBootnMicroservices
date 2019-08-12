package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SignatureInfoExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
/**
 * It is a helper class for Submit Application in nbA 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SR515492</td><td>Discretionary</td><td>E-App Integration</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaXML103SubmitPolicyHelper {
	
	/**
	 * Translate the iPipeline value into corresponding nbA value
	 * 
	 * @param fieldName The name of the field, whose value needs to be converted
	 * @param fieldValue The value of the field, which needs to be converted
	 * @throws NbaBaseException
	 */
	protected String getTranslatedNbaValue(String fieldName, String fieldValue) throws NbaBaseException {
		Map deOinkMap = new HashMap(2, 1);
		deOinkMap.put("A_FieldName", fieldName);
		deOinkMap.put("A_FieldValue", fieldValue);
		NbaVpmsAdaptor vpmsAdaptor  = null;
		try {
			NbaOinkDataAccess data = new NbaOinkDataAccess();
			vpmsAdaptor = new NbaVpmsAdaptor(data, NbaVpmsConstants.INDEX);
			vpmsAdaptor.setSkipAttributesMap(deOinkMap);
			vpmsAdaptor.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_IPIPELINE_TRANSLATION);
			// get the string out returned by VP / MS Model
			VpmsComputeResult rulesProxyResult = vpmsAdaptor.getResults();
			if (!rulesProxyResult.isError()) {
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(rulesProxyResult);
				List rulesList = vpmsResultsData.getResultsData();
				if (!rulesList.isEmpty()) {
					String returnStr = (String) rulesList.get(0);
					if (!NbaUtils.isBlankOrNull(returnStr)) {
						return returnStr;
					}
				}
			}
			return null;
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (vpmsAdaptor != null) {
				try {
					vpmsAdaptor.remove();
				} catch (RemoteException re) {
					LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
				}
			}
		}
	}
	
	/**
	 * Retrieves the Nba Requireemnt Type from NBA_FORMS_VALIDATION table.
	 * 
	 * @param currentSource
	 *            the NbaSource object
	 * @throws NbaBaseException
	 */
	 //New Method QC7979, APSL2808 method moved from NbaProcAutoAppSubmit.java
	protected String encodeReplacementCompany(String companyName) throws NbaBaseException {
		if(companyName != null) {
	        NbaTableAccessor nta = new NbaTableAccessor();
	        NbaTableData data = nta.getReplacementCompanyValue(companyName);
	        if (data != null) { 
				return data.code();
			}
		}
		return null;
	}
	
	/**
	 * Retrieves the olife encoded value.
	 * 
	 * @param currentSource
	 *            the NbaSource object
	 * @throws NbaBaseException
	 */
	 //New Method QC7923, APSL2808 method moved from NbaProcAutoAppSubmit.java
	protected String encodeOlifeValue(String oliveTranslatedValue, String tableName) throws NbaBaseException {
		if(oliveTranslatedValue != null) {
	        NbaTableAccessor nta = new NbaTableAccessor();
	        NbaTableData data = nta.getOlifeEncodedValue(oliveTranslatedValue, tableName);
	        if (data != null) { 
				return data.code();
			}
		}
		return null;
	}
	
	/**
     * Determine the replacement type based on company key parameter.  
     * None is returned if the company information is not supplied.  
     * Internal if the company is identified as AXA company based on predefined list.
     * External otherwise.
     * Note: The evaluation only takes place only when the replacement type is not identified 
     * already from other processes. e.g. risk pollers identifies replacement due to other reasons.  
     * @param replacedPartyKey
     * @return
     */
    //QC9291(APSL2272) New Method, APSL2808 method moved from NbaProcAutoAppSubmit.java
    protected long evalReplacementType(String replacedPartyKey, long replacementType) {
		if (replacementType == -1L || replacementType == NbaOliConstants.OLI_REPTY_NONE || replacementType == NbaOliConstants.OLI_REPTY_INTERNAL || replacementType == NbaOliConstants.OLI_REPTY_EXTERNAL) {
			if (NbaUtils.isBlankOrNull(replacedPartyKey)) {
				replacementType = NbaOliConstants.OLI_REPTY_NONE;
			} else if (NbaUtils.isInternalReplacementCompany(replacedPartyKey)) {
				replacementType = NbaOliConstants.OLI_REPTY_INTERNAL;
			} else {
				replacementType = NbaOliConstants.OLI_REPTY_EXTERNAL;
			}
		}
		return replacementType;
	}
    
    /**
	 * Returns the value of ProductInfoType based on the value of productType 
	 *  @param nbATXLife The nbATXLife to set.
	 */
    // APSL2808 method moved from NbaProcAutoAppSubmit.java
	public long getProductInfoType(long productType, String productCode) { //APSL2432
		if (productType == NbaOliConstants.OLI_PRODTYPE_TERM || productType == NbaOliConstants.OLI_PRODTYPE_INDETERPREM) {
			return NbaOliConstants.AXA_PRODUCTINFOTYPE_TERM;
		}else if (productType == NbaOliConstants.OLI_PRODTYPE_UL) {//APSL2377
		  //Begin APSL3640/QC13061
			if (productCode.equalsIgnoreCase("FUL302") || productCode.equalsIgnoreCase("FUL303")) { 
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_AUL;
			} else if (productCode.equalsIgnoreCase("FSU106") || productCode.equalsIgnoreCase("FSU107")) {
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_ASUL;
			} //End APSL3640/QC13061
		} else if (productType == NbaOliConstants.OLI_PRODTYPE_VUL) {//APSL2377
			if (productCode.equalsIgnoreCase("FWL100")) { //Start APSL2432
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_ILLEGACY;
			}
			else if (productCode.equalsIgnoreCase("FWL102")) { //SR798402 ALII2069
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_ILLEGACY3;
			}else if (productCode.equalsIgnoreCase("FTV100")) {
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_ILOPT;
			} //End APSL2432
			else if (productCode.equalsIgnoreCase("FAA104")) { //APSL3640/QC13061
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_SIL;
			} 
			else if (productCode.equalsIgnoreCase("FTV102")) { //APSL4579/QC16695
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_ILOPT3;
			} 
			else if (productCode.equalsIgnoreCase("FTV103")) { //NBLXA-2406
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_VULILOPT;
			}
			else if (productCode.equalsIgnoreCase("FWL103")) { //NBLXA-2407
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_VULILLEGACY;
			}
		} else if (productType == NbaOliConstants.OLI_PRODTYPE_INDXUL) {//APSL2377
		  // Begin APSL3640/QC13061
			if (productCode.equalsIgnoreCase("FWM100")||productCode.equalsIgnoreCase("FWM102")) { 
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_AIUL;
			} else if (productCode.equalsIgnoreCase("FWM104")) {
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_BrightLifeProtect;
			} else if (productCode.equalsIgnoreCase("FWM106")) {
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_BrightLifeGrow;
			} // End APSL3640/QC13061
			// Begin NBLXA-187
			else if (productCode.equalsIgnoreCase("FWM108")|| productCode.equalsIgnoreCase("FWM109")) {     // NBLXA-2311
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_IULProtect;
			} else if (productCode.equalsIgnoreCase("FWM107")) {
				return NbaOliConstants.AXA_PRODUCTINFOTYPE_IULPerform;
			} // End NBLXA-187
		}
		return NbaConstants.LONG_NULL_VALUE;
	}
	
	/**
	 * Process the eSignature and copy it to the various sections of the application
	 * 
	 * @param nbATXLife
	 *            The nbATXLife to set.
	 */
	// APSL2808 method moved from NbaProcAutoAppSubmit.java
	public SignatureInfo createSignatureInfo(String signCode, long signRoleCode, long signPurpose){
		SignatureInfo signatureInfo = new SignatureInfo();
		signatureInfo.setSignatureCode(signCode);
		signatureInfo.setSignatureRoleCode(signRoleCode);
		signatureInfo.setSignaturePurpose(signPurpose);
		return signatureInfo;
	}
	

	/**
	 * Process the eSignature and copy it to the various sections of the application 
	 *  @param nbATXLife The nbATXLife to set.
	 */
	// APSL2808 method moved from NbaProcAutoAppSubmit.java
	public void updateSignatureInfo(SignatureInfo fromSignature, SignatureInfo toSignatureInfo){
		if(fromSignature != null && toSignatureInfo != null) {
			toSignatureInfo.setSignatureDate(fromSignature.getSignatureDate());
			toSignatureInfo.setSignatureCity(fromSignature.getSignatureCity());
			toSignatureInfo.setSignatureState(fromSignature.getSignatureState());
			toSignatureInfo.setSignaturePartyID(fromSignature.getSignaturePartyID());//QC7933
			SignatureInfoExtension fromSignInfoExt = NbaUtils.getFirstSignatureInfoExtension(fromSignature);
			if(fromSignInfoExt != null) {
				//APSL2650 Begin
				SignatureInfoExtension toSignatureInfoExt = NbaUtils.getFirstSignatureInfoExtension(toSignatureInfo);
                if(toSignatureInfoExt == null) { 
					OLifEExtension oliExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_SIGNATUREINFO);
					toSignatureInfo.addOLifEExtension(oliExt);
					toSignatureInfoExt = oliExt.getSignatureInfoExtension();	
				}
				//APSL2650 End
				toSignatureInfoExt.setSignatureType(NbaOliConstants.OLI_SIGFORMAT_ESIGN);
				toSignatureInfoExt.setSignatureOKIndCode(fromSignInfoExt.getSignatureOKIndCode());//QC7760
			}
		}
	}
	
	/**
	 * Process the eSignature and copy it to the various sections of the application 
	 *  @param nbATXLife The nbATXLife to set.
	 */
	// APSL2808 method moved from NbaProcAutoAppSubmit.java
	public SignatureInfo findSignatureInfo(List signatureInfoList, String signaturePartyID, long relationRoleCode) {//QC8291 changed method signature
		if (signatureInfoList != null && signaturePartyID != null) {
			for(int i=0; i< signatureInfoList.size(); i++) {
				SignatureInfo signatureInfo = (SignatureInfo) signatureInfoList.get(i);
				if(signaturePartyID.equalsIgnoreCase(signatureInfo.getSignaturePartyID())
						&& relationRoleCode == signatureInfo.getSignatureRoleCode()) {//QC8291
					return signatureInfo;
				}
			}
		}
		return null;
	}
	
	/**
	 * Process the eSignature and copy it to the various sections of the application 
	 *  @param nbATXLife The nbATXLife to set.
	 */
	// APSL2808
	protected SignatureInfo findSignatureInfo(List signatureInfoList, String signCode, long signRoleCode, long signPurpose) {//QC8291 changed method signature
		if (signatureInfoList != null) {
			for(int i=0; i< signatureInfoList.size(); i++) {
				SignatureInfo signatureInfo = (SignatureInfo) signatureInfoList.get(i);
				if(signCode.equalsIgnoreCase(signatureInfo.getSignatureCode())
						&& signRoleCode == signatureInfo.getSignatureRoleCode()
						&& signPurpose == signatureInfo.getSignaturePurpose()) {//QC8291
					return signatureInfo;
				}
			}
		}
		return null;
	}
}
