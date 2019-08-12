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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.csc.fsg.nba.contract.calculations.NbaContractCalculationsConstants;
import com.csc.fsg.nba.contract.calculations.NbaContractCalculatorFactory;
import com.csc.fsg.nba.contract.calculations.results.CalcProduct;
import com.csc.fsg.nba.contract.calculations.results.CalculationResult;
import com.csc.fsg.nba.contract.calculations.results.NbaCalculation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;

/**
 * 
 * This is the SI letter specific class to get specific OINK variable resolved.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead> 
 * <tr><td>APSL2808</td><td>Discretionary</td><td>SI Contract Print Variables Resolver</td></tr>
 * </table>
 * <p>
 */

public class AXASICorrespondenceVariableResolver extends AXACorrespondenceVariableResolverProcessor {
	private NbaDst nbaParentDst;
	
	private NbaCalculation docsCalculation;
	private Coverage baseCoverage;
	private Policy primaryPolicy;
	
	private static final String GUARSURRVALUE10    = "CovGuarSurrenderCostIndex10";
	private static final String GUARSURRVALUE20    = "CovGuarSurrenderCostIndex20";
	private static final String GUARNETPYMTVALUE10 = "CovGuarNetPmtCostIndex10";
	private static final String GUARNETPYMTVALUE20 = "CovGuarNetPmtCostIndex20";
	private static final String GUARANNUALPREMIUM    = "PolicyGuarAnnualPremiumWithRatings";
	private static final String GUARANNUALISEDPREMIUM    = "PolicyGuarAnnualisedPremium";//APSL3195(QC12002)
	
	static HashMap methodsMap = new HashMap();
	static {
		AXASICorrespondenceVariableResolver variableResolver = new AXASICorrespondenceVariableResolver();
		Method[] allMethods = variableResolver.getClass().getMethods(); //resolve parent class methods
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
	public AXASICorrespondenceVariableResolver() {
		super();
		
	}
	
	/**
	 * Parameterized constructor
	 * @param userVO
	 * @param nbaTXLife
	 * @param nbaDst
	 * @param object
	 */
	public AXASICorrespondenceVariableResolver(NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) throws NbaBaseException {
		super(userVO, nbaTXLife, nbaDst, object);
		initialize();
		if (object != null && ((HashMap) object).keySet().contains(AXACorrespondenceConstants.PARENT_DST)) {
			nbaParentDst = (NbaDst) ((HashMap) object).get(AXACorrespondenceConstants.PARENT_DST);
		} else {
			nbaParentDst = null;
		}
	}
	
	/**
	 * Method used for initilizing the SI data used to retrieve value for OINK variables
	 * @throws NbaBaseException
	 */
	private void initialize() throws NbaBaseException {
		try {
			NbaTXLife nbaTXLife = getNbaTXLife();
			if(nbaTXLife != null){
				baseCoverage = nbaTXLife.getPrimaryCoverage();
				primaryPolicy = nbaTXLife.getPrimaryHolding().getPolicy();
				getDocsCalculations();				
			}
		} catch (Exception e) {
			NbaBaseException nce = new NbaBaseException(e);
			NbaLogFactory.getLogger(AxaWSInvoker.class).logException("Unable to initilize SI correspondence variable resolver ", nce);
			throw nce;
		}		
	}
	
	/**
	 * Retrieve the NbaCalculation from the applicable Document calculation model.
	 * @return NbaCalculation
	 */
	public NbaCalculation getDocsCalculations() throws NbaBaseException {
		if (docsCalculation == null) {
			
			docsCalculation = NbaContractCalculatorFactory.calculate(NbaContractCalculationsConstants.CALC_AXA_CONTRACT_PRINT, getNbaTXLife());
			
			if (docsCalculation.getCalcResultCode() != NbaOliConstants.TC_RESCODE_SUCCESS) {
				throw new NbaVpmsException("Contract Documents VPMS calculation failure");
			}
		}
		return docsCalculation;
	}
	/**
	 * Retrieve the CalculationResult for the object identified by the id. Return
	 * null if there is no matching CalculationResult.
	 * @param id - the id of the object, including a duration value if applicable
	 * @return CalculationResult
	 * @throws NbaBaseException
	 */
	protected CalculationResult getDocsResultForID(String id) {
		CalculationResult calculationResult = null;
		NbaCalculation docsCalculations = getDocsCalculation();
		if(docsCalculations !=null){
			int resultCount = docsCalculations.getCalculationResultCount();
			int resIdx;
			for (resIdx = 0; resIdx < resultCount; resIdx++) {
				calculationResult = (CalculationResult) docsCalculations.getCalculationResult().get(resIdx);
				if (id.equals(calculationResult.getObjectId())) {
					return calculationResult;
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
					setQualifier(var.substring(var.indexOf("_") + 1)); // APSL3264(QC12223)
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
	
	public String retrieveSCBP10YrValue() {	
		Coverage coverage = getBaseCoverage();
		String value = null;
		if(coverage != null){
			CalculationResult result =  getDocsResultForID(baseCoverage.getId());
			value = getCalculatedValue(result, GUARSURRVALUE10);
		}
		return value;
	}
	
	public String retrieveSCBP20YrValue() {
		Coverage coverage = getBaseCoverage();
		String value = null;
		if(coverage != null){
			CalculationResult result =  getDocsResultForID(coverage.getId());
			value = getCalculatedValue(result, GUARSURRVALUE20);		
		}
		return value;
	}
	
	public String retrieveNPCBP10YrValue() {
		Coverage coverage = getBaseCoverage();
		String value = null;
		if(coverage != null){
			CalculationResult result =  getDocsResultForID(coverage.getId());
			value = getCalculatedValue(result, GUARNETPYMTVALUE10);	
		}
		return value;
	}
	
	public String retrieveNPCBP20YrValue() {
		Coverage coverage = getBaseCoverage();
		String value = null;
		if(coverage != null){
			CalculationResult result =  getDocsResultForID(coverage.getId());
			value = getCalculatedValue(result, GUARNETPYMTVALUE20);	
		}
		return value;
	}
	
	public String retrieveAnnualPremiumAt85() {
		int issueAge = 0; 
		int premAge = 85;
		int duration = 0;
		Coverage coverage = getBaseCoverage();
		Policy policy = getNbaTXLife().getPrimaryHolding().getPolicy();
		String value = null;
		if(coverage != null){
			LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
			if (lifeParticipant != null && lifeParticipant.hasIssueAge()) {
				issueAge = lifeParticipant.getIssueAge();				
			}
			duration = premAge - issueAge;//APSL3195(QC12002)		
		}
		if(policy != null){
			CalculationResult result =  getDocsResultForID(getObjectDurId(policy.getId(),duration));
			value = getCalculatedValue(result, GUARANNUALISEDPREMIUM);//APSL3195(QC12002)	
		}
		return value;
	}
	
	public String retrieveAnnualPremiumAt95() {
		int issueAge = 0; 
		int premAge = 95;
		int duration = 0;
		Coverage coverage = getBaseCoverage();
		Policy policy = getNbaTXLife().getPrimaryHolding().getPolicy();
		String value = null;
		if(coverage != null){
			LifeParticipant lifeParticipant = NbaUtils.getInsurableLifeParticipant(coverage);
			if (lifeParticipant != null && lifeParticipant.hasIssueAge()) {
				issueAge = lifeParticipant.getIssueAge();				
			}
			duration = premAge - issueAge;//APSL3195(QC12002)
		}
		if(policy != null){
			CalculationResult result =  getDocsResultForID(getObjectDurId(policy.getId(),duration));
			value = getCalculatedValue(result, GUARANNUALISEDPREMIUM);//APSL3195(QC12002)	
		}
		return value;
	}
	
	public String retrieveSupplementalText() {
		String supplementTextString = "";
		String delimiter = ",";
		StringBuffer supplementTextBuff = new StringBuffer();
		ApplicationInfo api = getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo();
		if (api != null) {
			ApplicationInfoExtension apiExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (apiExt != null) {
				List urList = new ArrayList();
				urList = apiExt.getUnderwritingResult();
				int uwResultCount = urList.size();				
				for (int j = 0; j < uwResultCount; j++) {
					UnderwritingResult uwResult = (UnderwritingResult) urList.get(j);
					if(uwResult != null && uwResult.getSupplementalText() != null){
						if(j == uwResultCount -1 ){
							supplementTextBuff.append(uwResult.getSupplementalText());
							//supplementTextString +=  uwResult.getSupplementalText();
						}else{	
							supplementTextBuff.append(uwResult.getSupplementalText());
							supplementTextBuff.append(delimiter);
							//supplementTextString +=  uwResult.getSupplementalText()+ ",";
						}
					}					
				}
			}
		}
		supplementTextString  = supplementTextBuff.toString();
		return supplementTextString;
	}
	
	public String retrieveCompanyAssignedCode() {
		String companyAssignedCodeString = "";
		String delimiter = ",";
		StringBuffer companyAssignedCodeBuff = new StringBuffer();
		ApplicationInfo api = getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo();
		if (api != null) {
			ApplicationInfoExtension apiExt = NbaUtils.getFirstApplicationInfoExtension(api);
			if (apiExt != null) {
				List urList = new ArrayList();
				urList = apiExt.getUnderwritingResult();
				int uwResultCount = urList.size();				
				for (int j = 0; j < uwResultCount; j++) {
					UnderwritingResult uwResult = (UnderwritingResult) urList.get(j);
					if(uwResult != null && uwResult.getCompanyAssignedCode() != null){
						if(j == uwResultCount -1 ){
							companyAssignedCodeBuff.append(uwResult.getCompanyAssignedCode());
							//supplementTextString +=  uwResult.getSupplementalText();
						}else{	
							companyAssignedCodeBuff.append(uwResult.getCompanyAssignedCode());
							companyAssignedCodeBuff.append(delimiter);
							//supplementTextString +=  uwResult.getSupplementalText()+ ",";
						}
					}					
				}
			}
		}
		companyAssignedCodeString  = companyAssignedCodeBuff.toString();
		return companyAssignedCodeString;
	}
	
	
	/**
	 * Format the string to be used to identify an object by duration
	 * @param dur
	 */
	protected String getObjectDurId(String id, int dur) {
		StringBuffer buff = new StringBuffer();
		buff.append(id);
		buff.append("[");
		buff.append(String.valueOf(dur));
		buff.append("]");
		return (buff.toString());
	}
	
	private String getCalculatedValue(CalculationResult calculationResult, String resolveField){		
		int prdIdx;
		CalcProduct calcProduct;
		String field;
		if(calculationResult != null){
			int prodCount = calculationResult.getCalcProductCount();
			for (prdIdx = 0; prdIdx < prodCount; prdIdx++) {
				calcProduct = calculationResult.getCalcProductAt(prdIdx);
				field = calcProduct.getType();
				if (resolveField.equalsIgnoreCase(field)) {
					return  calcProduct.getValue();
				}
			}
		}
		return null;
	}

	/**
	 * @return Returns the nbaParentDst.
	 */
	public NbaDst getNbaParentDst() {
		return nbaParentDst;
	}
	/**
	 * @param nbaParentDst The nbaParentDst to set.
	 */
	public void setNbaParentDst(NbaDst nbaParentDst) {
		this.nbaParentDst = nbaParentDst;
	}	
	/**
	 * @return Returns the baseCoverage.
	 */
	public Coverage getBaseCoverage() {
		return baseCoverage;
	}
	/**
	 * @param baseCoverage The baseCoverage to set.
	 */
	public void setBaseCoverage(Coverage baseCoverage) {
		this.baseCoverage = baseCoverage;
	}	
	/**
	 * @return Returns the docsCalculation.
	 */
	public NbaCalculation getDocsCalculation() {
		return docsCalculation;
	}
	/**
	 * @param docsCalculation The docsCalculation to set.
	 */
	public void setDocsCalculation(NbaCalculation docsCalculation) {
		this.docsCalculation = docsCalculation;
	}
	
	
	/**
	 * @return Returns the primaryPolicy.
	 */
	public Policy getPrimaryPolicy() {
		return primaryPolicy;
	}
	/**
	 * @param primaryPolicy The primaryPolicy to set.
	 */
	public void setPrimaryPolicy(Policy primaryPolicy) {
		this.primaryPolicy = primaryPolicy;
	}
}
