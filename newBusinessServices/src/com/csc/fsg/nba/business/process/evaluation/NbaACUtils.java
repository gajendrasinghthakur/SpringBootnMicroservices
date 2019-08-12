package com.csc.fsg.nba.business.process.evaluation;

/*
 * **************************************************************************<BR>
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
 * **************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.rules.AdditionalApproval;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.nbaschema.InsurableParty;
import com.csc.fsg.nba.vo.nbaschema.Requirement;
import com.csc.fsg.nba.vo.nbaschema.RequirementControlSource;
import com.csc.fsg.nba.vo.txlife.Coverage;

/**
 * This class defines utility functions for nbA.
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */

public class NbaACUtils {

	// SPR3290 - code deleted
   /**This method is used to pass RequirementList  and Number of RequirementList as deoink variable. 
	* @param deOink    as Map to store variables
	* @param partyId   for which  requirement is to be generated
	* @param rcs       an instance of RequirementControlSource 
	* @param nbaTxLife an instance of NbaTxLife
	*/
	public static void deOinkRequirementFields(Map deOink,String partyId,RequirementControlSource rcs,NbaTXLife nbaTxLife)throws NbaBaseException {
	    // Begin SPR3290
		List reqList = getRequirementList(rcs, nbaTxLife, partyId);
		String oinkVarName = "RequirementList_INS";

		if (reqList != null) {
		    int listSize = reqList.size();
			Requirement req = null;
			int reqCode;
			for (int i=0; i<listSize; i++) {
				req = (Requirement) reqList.get(i);
				reqCode = req.getCode();
				if (i == 0)
					deOink.put("A_" + oinkVarName, String.valueOf(reqCode));
				else
					deOink.put("A_" + oinkVarName + "[" + i + "]",String.valueOf(reqCode));
			}
			deOink.put("A_No_Of_" + oinkVarName,String.valueOf(listSize));
		} else {
			deOink.put("A_No_Of_" + oinkVarName, String.valueOf(0));
		}
		// End SPR3290
	}
   /** This method is used to get the requirement from Insurable party.
	* @param  partyId   for which  requirement is to be generated
	* @param  rcs       an instance of RequirementControlSource 
	* @param  nbaTxLife an instance of NbaTxLife
	* @return ArrayList of requirements
	*/
	protected static List getRequirementList(RequirementControlSource rcs,NbaTXLife nbaTxLife,String partyId) throws NbaBaseException { // SPR3290
	    // Begin SPR3290
	    // List size cannot be predetermined, so accept default
	    List reqList = new ArrayList();
		if (rcs != null && nbaTxLife!= null && partyId != null) {
			InsurableParty ins = null;
		    int partyCount = rcs.getInsurablePartyCount();
		    String personSeq = null;
		    int relationCode;
			for (int i=0; i<partyCount; i++) {
				ins = rcs.getInsurablePartyAt(i);
				personSeq = ins.getPersonSequence();
				relationCode = ins.getPersonCode();
				if (partyId.equals(nbaTxLife.getPartyId(relationCode,String.valueOf(personSeq)))) {
					reqList = ins.getRequirement();
					break;
				}
			}
		}
		return reqList;
		// End SPR3290
	}
   /** This method is used to get the DeOink values for First party.
	* @param  oinkData     for which  requirement is to be generated
	* @param  variableName oink variable  to  be  passed 
	* @param  ownerVarMap   an instance of NbaTxLife
	*/
	public static void  getFirstPartyDeOinkValues(NbaOinkDataAccess oinkData, String variableName,Map ownerVarMap) throws NbaBaseException{
	   NbaOinkRequest oinkRequest = new NbaOinkRequest();				
	   oinkRequest.setVariable(variableName);
	   oinkRequest.setPartyFilter(0); 
	   String value = oinkData.getStringValueFor(oinkRequest);			
	   ownerVarMap.put("A_"+variableName, value);
	 }
	 
	/**This method is used to pass ProductCodList  and Number of ProductCodes as deoink variable. 
	 * @param nbaTxLife an instance of NbaTxLife
	 * @param deOink    as Map to store variables
	 * @param partyId   for which  requirement is to be generated
	 */	 
	public static void deOinkProductCode(NbaTXLife nbaTxLife, Map deOinkMap, String partyID) throws NbaBaseException {
		List coverageList = nbaTxLife.getCoveragesFor(partyID); // SPR3290
		List productCodeList = new ArrayList(); // SPR3290
		int count = coverageList.size();
		Coverage coverage = null;
		String productCode = null;
		for(int i=0;i<count;i++){
			coverage = (Coverage)coverageList.get(i);
			if(coverage!=null){
				productCode = coverage.getProductCode();
				if(productCode!=null && !productCode.equals("")){
					productCodeList.add(productCode);
				}
			}
		}
		int productCodeCount = productCodeList.size();
		deOinkMap.put("A_no_of_ProductCode_INS",String.valueOf(productCodeCount));
		if(productCodeCount==0){
			deOinkMap.put("A_ProductCode_INS","");
		}else{
			for(int i=0;i<productCodeCount;i++){
				if(i==0){
					deOinkMap.put("A_ProductCode_INS",(String)productCodeList.get(i));
				}else{
					deOinkMap.put("A_ProductCode_INS["+i+"]",(String)productCodeList.get(i));
				}
			}
		}
	}
	
	// NBLXA-2085 Begin
	public static void deoinkApproveWithRatingValues(Map deOinkMap, NbaOinkDataAccess oinkData) throws NbaDataAccessException {
		String logonRole = (String) deOinkMap.get("A_LogonRole");
		String uwQueue = (String) deOinkMap.get("A_UWQueue");//NBLXA-2489
		AdditionalApproval additional = new AdditionalApproval(oinkData);
		if (additional.getA_Disposition_LVL1() != null) {
			additional.getDbAccessor().getAdditionalUndrtApproveWithRating(logonRole, additional.getA_FaceAmountLOB(),
					additional.getA_FirstExtraTableRating_PINS(), additional.getA_FirstExtraAmt_PINS(), deOinkMap,
					NbaUtils.isApproved(additional.getA_Disposition_LVL1()), NbaUtils.isDeclinedOrDeferred(additional.getA_Disposition_LVL1()),NbaUtils.getCompany(additional.getA_CompanyLOB()),additional.getA_IssueAge_PINS(),additional.getA_RateClass_PINS(),NbaUtils.getTermOrPermCase(additional.getA_ProductTypSubtypLOB(),additional.getA_PlanTypeLOB()),uwQueue); //NBLXA-2489
		}
	}	
	// End NBLXA-2085

}
