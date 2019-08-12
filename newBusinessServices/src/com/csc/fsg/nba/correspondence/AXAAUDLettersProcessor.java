/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
package com.csc.fsg.nba.correspondence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaReasonsData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.vo.txlife.UnderwritingResultExtension;
/**
 * 
 * This is the Replacement letters specific class to get specific OINK variable resolved.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead> 
 * <tr><td>ALPC195</td><td>AXA Life Phase 1</td><td>AUD Negative Correspondence</td></tr>
 * </table>
 * <p>
 */

public class AXAAUDLettersProcessor extends AXACorrespondenceProcessorBase{
	
	/**
     * Default constructor
     */
	public AXAAUDLettersProcessor(){
		super();
		
	}
	/**
     * Parameterized constructor
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
	public AXAAUDLettersProcessor(NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object){
		super(userVO, nbaTXLife, nbaDst, object);
	}
	
	
	/**
     * Method used for get the OINK variables values
     * @param variablesList
     * @return Object
     */
	public Object resolveVariables(Object variablesList ){
		ArrayList variables = (ArrayList) variablesList; 
		HashMap resolvedValueMap = new HashMap();
		// return map from here containing resolved varibale name and it's value
		if ( variables !=null && variables.size()>0) {
			Iterator itr = variables.iterator();
			while( itr.hasNext() ){
				String var = (String) itr.next();
				// now remove the qualifier if any
				StringTokenizer tokens = null;
				String variable = null;
				String qualifier = null;
				tokens = new StringTokenizer(var, "_"); 
				if (tokens.countTokens() >= 2) {
					variable = tokens.nextToken();
					qualifier = tokens.nextToken();
				}else {
					variable = tokens.nextToken();
				}
				String val = getValue(variable);
				resolvedValueMap.put(var, val);
				NbaLogFactory.getLogger(AXAAUDLettersProcessor.class).logDebug("Retrieved value of variable " + var + " = " + val);  
			}
		}
		return resolvedValueMap;
	}
	
	/**
     * Method used for get the AUD Reason string  
     * @return String
     */	
	public String retrieveAUDReason() {
		String reasonStr = new String();
		StringBuffer reasonStrBuffer = new StringBuffer();
		ArrayList reasonList = new ArrayList();
		NbaTableAccessor nta = new NbaTableAccessor();
		try{
			ApplicationInfo api =getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo();
			if (api != null) {
				ApplicationInfoExtension apiExt= NbaUtils.getFirstApplicationInfoExtension(api);
				if (apiExt != null) {
					List urList = new ArrayList();
					List urListtemp = apiExt.getUnderwritingResult(); // APSL5175
					urList = NbaUtils.getUWReasonResultList((ArrayList) urListtemp); // APSL5175
					urList = apiExt.getUnderwritingResult();
					int uwResultCount = urList.size();
					UnderwritingResultExtension uwResultExt = null; 
						for (int j = 0; j < uwResultCount; j++) {
							UnderwritingResult uwResult = (UnderwritingResult) urList.get(j);
							//get the reason and reasontype and fire the query to DB to get AUD letter text.
							long uwReasonTc = uwResult.getUnderwritingResultReason();
							uwResultExt = NbaUtils.getFirstUnderwritingResultExtension(uwResult);
							if ( uwResultExt != null  && uwResultExt.getUnderwritingReasonType() == Long.valueOf(NbaOliConstants.OLI_EXT_FINAL_DISP).longValue()){
								HashMap caseData = new HashMap();
								caseData.put("backendSystem", getNbaDst().getNbaLob().getBackendSystem());
								caseData.put("plan", getNbaDst().getNbaLob().getPlan());
								caseData.put("company", getNbaDst().getNbaLob().getCompany());
								NbaTableData data[] = nta.getAUDLetterText(caseData, NbaOliConstants.OLI_EXT_FINAL_DISP, String.valueOf(uwReasonTc));
								if ( data.length>0){
									reasonStr = ((NbaReasonsData)  data[0]).getAudLettersText();
								}
							//QC#5822 APSL728 SR540843 begin
							if (reasonStr != null && reasonStr.indexOf(NbaConstants.AUDLETTER_VAR) != -1) {
								reasonStr = uwResult.getSupplementalText() != null ? reasonStr.replaceFirst(NbaConstants.AUDLETTER_VAR, uwResult
										.getSupplementalText()) : reasonStr.replaceFirst(NbaConstants.AUDLETTER_VAR, "");
							}
							//QC#5822 APSL728 SR540843 end
							else {
								if (!NbaUtils.isBlankOrNull(uwResult.getSupplementalText())) {
									reasonStr = reasonStr.concat(" ");
									reasonStr = reasonStr.concat(uwResult.getSupplementalText());
								}
							}
								reasonList.add(reasonStr.toString());
							}
						}
					}
				}
			int count =  reasonList.size();
			
			switch ( count){
			case 1 : 
					reasonStrBuffer.append((String)reasonList.get(0));	
					reasonStr = reasonStrBuffer.toString();
					break;
			case 2 :
					reasonStrBuffer.append((String)reasonList.get(0));	
					reasonStrBuffer.append(" and ");
					reasonStrBuffer.append((String)reasonList.get(1));
					reasonStr = reasonStrBuffer.toString();
					break; 
			default:
					for ( int k=0; k<count-1; k++){
						reasonStrBuffer.append((String)reasonList.get(k));
						reasonStrBuffer.append(", ");
					}
					reasonStr = reasonStrBuffer.toString();
					if (reasonStrBuffer.length() > 0) {
						reasonStr = reasonStrBuffer.substring(0, reasonStrBuffer.length() - 2); //Remove last comma and space from the list
					}
					reasonStr = reasonStr + " and " + ((String)reasonList.get(count-1));
			
			}
		}catch (Exception e ){
			getLogger().logException("Unable to get Value for AUDReason " , e);
		}
		return reasonStr; 
	}
	
	/**
     * Method to get value of particular OINK varible  
     * @param variableName
     * @return String
     */	
	public String getValue (String variableName) {
		String resolvedValue  = "";
		if ("AUDReason".equals(variableName)) {
			return retrieveAUDReason(); 
		}
		return resolvedValue;
	}
}
