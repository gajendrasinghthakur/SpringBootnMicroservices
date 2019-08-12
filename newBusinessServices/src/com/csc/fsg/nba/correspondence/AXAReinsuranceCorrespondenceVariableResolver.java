/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
package com.csc.fsg.nba.correspondence;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csc.fsg.nba.datamanipulation.NbaContractDataAccessConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfo;
import com.csc.fsg.nba.vo.txlife.ReinsuranceInfoExtension;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
/**
 * 
 * This is the Reinsurance letter specific class to get specific OINK variable resolved.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead> 
 * <tr><td>AXAL3.7.32</td><td>AXA Life Phase 2</td><td>Reinsurer Interface</td></tr>
 * <tr><td>CR735253-735254</td><td>AXA Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * <tr><td>CR1343973</td><td>Discretionary</td><td>Reinsurance Corr Display</td></tr>
 * </table>
 * <p>
 */

public class AXAReinsuranceCorrespondenceVariableResolver extends AXACorrespondenceProcessorBase {

	private NbaDst nbaParentDst;

	private NbaTXLife xml552 = null;

	static HashMap methodsMap = new HashMap();
	
	private Map medicalRequirements = new HashMap();
	
	private int reqCount = 0; // CR735253-735254
	
	private final String BR = "<BR>"; //CR735253-735254
	
	private final String QUESTION_SIGN = "?"; //CR735253-735254
	
	private String qualifer; // CR1343973

	private Map medicalRequirementsJI = new HashMap(); // CR1343973
	
	private int reqCountJI = 0; // CR1343973

	static {
		AXAReinsuranceCorrespondenceVariableResolver variableResolver = new AXAReinsuranceCorrespondenceVariableResolver();
		Method[] allMethods = variableResolver.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("retrieve")) {
				Class[] parmClasses = aMethod.getParameterTypes();
				if (parmClasses.length == 0) {
					methodsMap.put(aMethodName.substring(8).toUpperCase(), aMethod);
				}
			}
		}
	}

	/**
	 * Default constructor
	 */
	public AXAReinsuranceCorrespondenceVariableResolver() {
		super();

	}

	/**
	 * Parameterized constructor
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 * @param object
	 */
	public AXAReinsuranceCorrespondenceVariableResolver(NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) throws NbaBaseException {
		super(userVO, nbaTXLife, nbaDst, object);
		initialize();
		if (object != null && ((HashMap) object).keySet().contains(AXACorrespondenceConstants.PARENT_DST)) {
			nbaParentDst = (NbaDst) ((HashMap) object).get(AXACorrespondenceConstants.PARENT_DST);
		} else {
			nbaParentDst = null;
		}
	}

	/**
	 * Method used for initilizing the Reinsurance data used to retrieve value for OINK variables
	 * @throws NbaBaseException
	 */
	private void initialize() throws NbaBaseException {
		try {
			setXml552(getXML552Trnsaction(getNbaDst()));
			if(getNbaTXLife() != null) {
				List requirementsInfo = getNbaTXLife().getPolicy().getRequirementInfo();
				for(int i=0;i<requirementsInfo.size();i++) {
					RequirementInfo requirementInfo = (RequirementInfo) requirementsInfo.get(i);
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(requirementInfo);
					if(reqInfoExt != null && reqInfoExt.getMedicalIndicator()) {
						if (requirementInfo.getAppliesToPartyID().equalsIgnoreCase(getNbaTXLife().getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED))) { // CR1343973 begin
							medicalRequirementsJI.put(requirementInfo.getId(), requirementInfo);
						} else if (requirementInfo.getAppliesToPartyID().equalsIgnoreCase(getNbaTXLife().getPartyId(NbaOliConstants.OLI_REL_INSURED))){ // CR1343973 end
							medicalRequirements.put(requirementInfo.getId(), requirementInfo);							
						}
					}
				}
			}
		} catch (Exception e) {
			NbaBaseException nce = new NbaBaseException(e);
			NbaLogFactory.getLogger(AxaWSInvoker.class).logException("Unable to initilize reinsurance correspondence variable resolver ", nce);
			throw nce;
		}

	}

	/**
	 * Returns XML 552 transaction from workitem.
	 * @return the XML 552 transaction
	 */
	protected NbaTXLife getXML552Trnsaction(NbaDst nbaDst) {
		if (nbaDst != null) {
			List list = nbaDst.getNbaSources();
			for (int i = 0; i < list.size(); i++) {
				NbaSource source = (NbaSource) list.get(i);
				if (NbaConstants.A_ST_REINSURANCE_XML_TRANSACTION.equals(source.getSource().getSourceType())) {
					try {
						return new NbaTXLife(source.getText());
					} catch (Exception e) {
						NbaLogFactory.getLogger(AxaWSInvoker.class).logException("Unable to retrieve XML 552 from reinsurance WI ", e);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Method used for get the OINK variables values
	 * @param variablesList
	 * @return Object
	 */
	public Object resolveVariables(Object variablesList) {
		ArrayList variables = (ArrayList) variablesList;
		HashMap resolvedValueMap = new LinkedHashMap();
		// return map from here containing resolved varibale name and it's value
		if (variables != null && variables.size() > 0) {
			Iterator itr = variables.iterator();
			while (itr.hasNext()) {
				String var = (String) itr.next();
				String origVar = var; // CR1343973
				if(var.indexOf("_") > -1) {
					setQualifer(var.substring(var.indexOf("_") + 1)); // CR1343973
					var = var.substring(0,var.indexOf("_"));
				}
				String val = getValue(var);
				resolvedValueMap.put(origVar, val); // CR1343973
				NbaLogFactory.getLogger(AXACorrespondenceVariableResolverProcessor.class)
						.logDebug("Retrieved value of variable " + var + " = " + val);
			}
		}
		return resolvedValueMap;
	}

	/**
	 * Method to get value of particular OINK varible  
	 * @param variableName
	 * @return String
	 */
	public String getValue(String variableName) {
		//Get the Method.
		if(variableName != null) {
			Method method = (Method) methodsMap.get(variableName.toUpperCase());
			try {
				return (String) method.invoke(this, null);
			} catch (Exception e) {
				StringBuffer err = new StringBuffer();
				err.append("Error invoking variable resolution routine:");
				err.append(variableName);
				getLogger().logError(err);
			}	
		}
		return null;
	}

	public String retrieveUWComments() {
		List attachmentList = getXml552().getPrimaryHolding().getAttachment();
		if (attachmentList != null && attachmentList.size() > 0) {
			for (int k = 0; k < attachmentList.size(); k++) {
				Attachment attachment = (Attachment) attachmentList.get(k);
				if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_COMMENT) {
					return attachment.getDescription();
				}
			}
		}
		return null;
	}
	
	public String retrieveTransDate() {
		if (getXml552().getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0) != null) {
			return NbaUtils.getDateWithoutSeparator(getXml552().getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransExeDate());
		}
		return null;
	}
	
	public String retrieveRetainedAmt() {
//		ReinsuranceInfo reinsuranceInfo = getXml552().getPrimaryCoverage().getReinsuranceInfoAt(0);
//		ReinsuranceInfo reinsuranceInfo = getXml552().getPrimaryCoverage().getReinsuranceInfoAt(0);
//		if (reinsuranceInfo != null) {
//			return (new BigDecimal(String.valueOf(reinsuranceInfo.getRetentionAmt()))).toString(); //ALII841
//		}
//		return null;
		ReinsuranceInfo reinsuranceInfo = getXml552().getPrimaryCoverage().getReinsuranceInfoAt(0);
		double retainedAmount = getNbaTXLife().getTotalInsuranceAmount() - reinsuranceInfo.getReinsuredAmt();
		BigDecimal bd = new BigDecimal(retainedAmount);
		if (!NbaUtils.isBlankOrNull(bd)) {
			return (NbaUtils.setScaleTo2(bd).toString());
		}
		return null;//return NbaUtils.setScaleTo2(new BigDecimal(retainedAmount)).toString();
	}
	//Commented in ALLII974
	/*public String retrieveCededAmt() {
		ReinsuranceInfo reinsuranceInfo = getXml552().getPrimaryCoverage().getReinsuranceInfoAt(0);
		if(reinsuranceInfo != null) {
			return (new BigDecimal(String.valueOf(reinsuranceInfo.getReinsuredAmt()))).toString(); //ALII841
		}
		return null;
	}*/
	
	public String retrieveReinsReason() {
		ReinsuranceInfo reinsuranceInfo = getXml552().getPrimaryCoverage().getReinsuranceInfoAt(0);
		ReinsuranceInfoExtension reinInfoExtension = NbaUtils.getFirstReinsuranceInfoExtension(reinsuranceInfo);
		if (reinInfoExtension != null) {
			return reinInfoExtension.getReinsuranceReason();
		}
		return null;
	}
	
	public String retrieveCarrierPartyID() throws NbaBaseException {
		com.csc.fsg.nba.vo.configuration.Reinsurer configReinsurer = NbaConfiguration.getInstance().getReinsurer(
				getNbaDst().getNbaLob().getReinVendorID());
		if (configReinsurer != null) {
			com.csc.fsg.nba.vo.configuration.CedingCompany configCedingInfo = configReinsurer.getCedingCompany();
			if (configCedingInfo != null) {
				return configCedingInfo.getId();
			}
		}
		return null;
	}
	
	public String retrieveCarrierName() throws NbaBaseException {
		com.csc.fsg.nba.vo.configuration.Reinsurer configReinsurer = NbaConfiguration.getInstance().getReinsurer(
				getNbaDst().getNbaLob().getReinVendorID());
		if (configReinsurer != null) {
			com.csc.fsg.nba.vo.configuration.CedingCompany configCedingInfo = configReinsurer.getCedingCompany();
			if (configCedingInfo != null) {
				return configCedingInfo.getName();
			}
		}
		return null;
	}
	
	public String retrieveReinCarrName() {
		ReinsuranceInfo reinsuranceInfo = getXml552().getPrimaryCoverage().getReinsuranceInfoAt(0);
		if (reinsuranceInfo != null) {
			String reinsurerPartyID = reinsuranceInfo.getCarrierPartyID();
			NbaParty nbaParty = getXml552().getParty(reinsurerPartyID);
			return nbaParty.getDBA();
		}
		return null;
	}
	//	CR735253-735254 Modified method
	/**
	 * Modified retrieveReqCode() to get List of Medical Requirements
	 * This method filter Exclusion Requirements List from List of 
	 * medicalRequirements and return medicalRequirements which will send 
	 * in 552 XML,also set ReqCount equal to Requirement in the list.
	 * @return String
	 */
	public String retrieveReqCode() throws NbaBaseException {
		StringBuffer reqCode = new StringBuffer();
		String reqCodeText = null;
		//Start CR735253-735254
		int requirementCount = 0;
		ArrayList exclusionReqList = NbaVPMSHelper.getExclusionRequirements(getNbaTXLife());
		// End CR735253-735254
		// CR1343973 begin
		Map medicalRequirementsMapToUse = medicalRequirements;
		if (NbaContractDataAccessConstants.PARTY_JOINT_INSURED.equalsIgnoreCase(getQualifer())) {
			medicalRequirementsMapToUse = medicalRequirementsJI;
		}
		if(medicalRequirementsMapToUse != null && !medicalRequirementsMapToUse.isEmpty()) {
			Iterator it = medicalRequirementsMapToUse.entrySet().iterator(); // CR1343973 end
			while (it.hasNext()) {
				boolean reinCorrExclusionReqInd = false; // CR735253-735254
				Map.Entry entry = (Map.Entry)it.next();
				RequirementInfo reqInfo = (RequirementInfo) entry.getValue();
				//Start CR735253-735254
				if (exclusionReqList.contains(String.valueOf(reqInfo.getReqCode()))) {
					reinCorrExclusionReqInd = true;
				}				
				if(!reinCorrExclusionReqInd){
					// End CR735253-735254
					reqCodeText = NbaUtils.getRequirementTranslation(String.valueOf(reqInfo.getReqCode()), getNbaTXLife().getPolicy()); //ALII867
					reqCode.append(reqCodeText != null ? reqCodeText : String.valueOf(reqInfo.getReqCode()));
					reqCode.append(QUESTION_SIGN + reqInfo.getReqStatus());//CR735253-735254
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					if(reqInfoExt!= null && reqInfoExt.hasPhysicianPartyID()) { //ALII840
						NbaParty nbaParty = getNbaTXLife().getParty(reqInfoExt.getPhysicianPartyID());
						reqCode.append(QUESTION_SIGN + nbaParty.getFullName());//CR735253-735254
					}
					requirementCount++; //CR735253-735254
					reqCode.append(BR); //CR735253-735254
				}
			}
			// CR1343973 begin
			if (NbaContractDataAccessConstants.PARTY_JOINT_INSURED.equalsIgnoreCase(getQualifer())) {
				setReqCountJI(requirementCount);
			} else { // CR1343973 end
				setReqCount(requirementCount); //CR735253-735254
			}
		}
		if(reqCode.indexOf(BR) > -1){ //CR735253-735254
			reqCode.delete(reqCode.lastIndexOf(BR), reqCode.length());//Remove the last instance of the delimiter <BR>	
		}
		return reqCode.toString();
	}
	
	/**
	 * Modified retrieveReqCount() to get count of 
	 * Medical Requirements sending in 552 XML. 
	 * @return String
	 */
	public String retrieveReqCount() {
		//return String.valueOf(medicalRequirements.size());
		return String.valueOf(getReqCount()); // CR735253-735254
	}
	
	public String retrieveCovOptionProductCodeList() {
		StringBuffer covOptionsList = new StringBuffer("");
		if (getNbaTXLife().getLife() != null) {
			List coverages = getNbaTXLife().getLife().getCoverage();
			Coverage coverage = null;
			for (int i = 0; i < coverages.size(); i++) {
				coverage = (Coverage) coverages.get(i);
				List covOptions = coverage.getCovOption();
				CovOption covOption = null;
				for (int k = 0; k < covOptions.size(); k++) {
					covOption = (CovOption) covOptions.get(k);
					covOptionsList.append(covOption.getPlanName());
					covOptionsList.append(",");
				}
			}
		}
		if(covOptionsList.indexOf(",") > -1){
			covOptionsList.delete(covOptionsList.lastIndexOf(","), covOptionsList.length());//Remove the last instance of the delimiter ','	
		}
		return covOptionsList.toString();
	}
	
	// ALII828 deleted retrieveBirthCountry() 
	//ALII974 new method
	public String retrieveReinsuredAmt() {
		ReinsuranceInfo reinsuranceInfo = getXml552().getPrimaryCoverage().getReinsuranceInfoAt(0);
		if (reinsuranceInfo != null) {
			BigDecimal bd = new BigDecimal(reinsuranceInfo.getReinsuredAmt());
			if (!NbaUtils.isBlankOrNull(bd)) {
				return (NbaUtils.setScaleTo2(bd).toString());
			}
		}
		return null;
	}

	public NbaDst getNbaParentDst() {
		return nbaParentDst;
	}

	public void setNbaParentDst(NbaDst nbaParentDst) {
		this.nbaParentDst = nbaParentDst;
	}

	/**
	 * @return Returns the xml552.
	 */
	public NbaTXLife getXml552() {
		return xml552;
	}

	/**
	 * @param xml552 The xml552 to set.
	 */
	public void setXml552(NbaTXLife xml552) {
		this.xml552 = xml552;
	}
	// CR735253-735254 new method
	/**
	 * @return Returns the reqCount.
	 */
	public int getReqCount() {
		return reqCount;
	}
	//CR735253-735254 new method
	/**
	 * @param reqCount The reqCount to set.
	 */
	public void setReqCount(int reqCount) {
		this.reqCount = reqCount;
	}
	
	/**
	 * @return Returns the qualifer.
	 */
	// CR1343973 New method
	public String getQualifer() {
		return qualifer;
	}
	
	/**
	 * @param qualifer The qualifer to set.
	 */
	// CR1343973 New method
	public void setQualifer(String qualifer) {
		this.qualifer = qualifer;
	}
	
	/**
	 * Obtain the value for a UW result reasons. OLifE.Holding.Policy.Life.Coverage.LifeParticipant.SubstandardRating.SubstandardRatingExtension.UnderwritingResult.UnderwritingResultReason.
	 * @param aNbaOinkRequest - data request container
	 */
	// CR1343973 New method
	public String retrieveUnderwritingResultReason() {
		StringBuffer underwritingResultReasons = new StringBuffer();
		LifeParticipant lifeParticipant = null;
		if (NbaContractDataAccessConstants.PARTY_JOINT_INSURED.equalsIgnoreCase(getQualifer())) {
			lifeParticipant = getNbaTXLife().getLifeParticipantFor(getNbaTXLife().getPartyId(NbaOliConstants.OLI_REL_JOINTINSURED));
		} else {
			lifeParticipant = getNbaTXLife().getLifeParticipantFor(getNbaTXLife().getPartyId(NbaOliConstants.OLI_REL_INSURED));
		}
		if (lifeParticipant != null && !lifeParticipant.isActionDelete()) {
			Set reasonSet = new HashSet();
			int countSR = lifeParticipant.getSubstandardRatingCount();
			for (int i = 0; i < countSR; i++) {
				SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(i);
				SubstandardRatingExtension substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
				if (NbaUtils.isValidRating(substandardRating) && substandardRatingExt != null) {
					int countUR = substandardRatingExt.getUnderwritingResultCount();
					for (int j = 0; j < countUR; j++) {
						UnderwritingResult uwResult = substandardRatingExt.getUnderwritingResultAt(j);
						if (!uwResult.isActionDelete())
							reasonSet.add(uwResult.getDescription());
					}
				}
			}
			Iterator reasonSetItr = reasonSet.iterator();
			while(reasonSetItr.hasNext()) {
				underwritingResultReasons.append(reasonSetItr.next()).append(", ");
			}
		}
		if(underwritingResultReasons.indexOf(",") > -1){
			underwritingResultReasons.delete(underwritingResultReasons.lastIndexOf(","), underwritingResultReasons.length());//Remove the last instance of the delimiter ','	
		}
		return underwritingResultReasons.toString();
	}
	
	/**
	 * Modified retrieveReqCode() to get List of Medical Requirements
	 * This method filter Exclusion Requirements List from List of 
	 * medicalRequirements and return medicalRequirements which will send 
	 * in 552 XML,also set ReqCount equal to Requirement in the list.
	 * @return String
	 */
	// CR1343973 New method
	public String retrieveReqCodeJI() throws NbaBaseException {
		return retrieveReqCode();
	}
	
	/**
	 * Modified retrieveReqCount() to get count of 
	 * Medical Requirements sending in 552 XML. 
	 * @return String
	 */
	// CR1343973 New method
	public String retrieveReqCountJI() {
		//return String.valueOf(medicalRequirements.size());
		return String.valueOf(getReqCountJI()); // CR735253-735254
	}
	
	/**
	 * @return Returns the reqCount.
	 */
	// CR1343973 New method
	public int getReqCountJI() {
		return reqCountJI;
	}
	
	/**
	 * @param reqCount The reqCount to set.
	 */
	// CR1343973 New method
	public void setReqCountJI(int reqCount) {
		this.reqCountJI = reqCount;
	}
}
