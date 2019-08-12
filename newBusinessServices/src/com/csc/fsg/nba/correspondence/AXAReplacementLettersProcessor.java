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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.tableaccess.NbaReplacedCompanyAddressData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.AxaCorrespondenceReplacementInfoVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
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
 * <tr><td>AXAL3.7.13</td><td>Version 7</td><td>Formal Correspondence</td></tr>
 * </table>
 * <p>
 */

public class AXAReplacementLettersProcessor extends AXACorrespondenceProcessorBase{
	
	Map replacementDataMap = new HashMap();
	
	/**
     * Default constructor
     */
	public AXAReplacementLettersProcessor(){
		super();
		
	}
	/**
     * Parameterized constructor
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
	 * @throws NbaBaseException
     */
	public AXAReplacementLettersProcessor(NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) throws NbaBaseException{
		super(userVO, nbaTXLife, nbaDst, object);
		initilizeReplacementData();	
	}
	
	/**
     * Method used for initilizing the Replacemnt data used to retrieve value for OINK variables
	 * @throws NbaBaseException
     */
	private void initilizeReplacementData() throws NbaBaseException {
		try{
			AxaCorrespondenceReplacementInfoVO corrReplInfoVO = null;
			if (getNbaTXLife().isReplacement()){
				Map allHoldingRelMap= getNbaTXLife().getAllRelationsForRole(NbaOliConstants.OLI_REL_HOLDINGCO);
				Relation holdingCoRel = null;
				Iterator iterator  = allHoldingRelMap.keySet().iterator();
				while( iterator.hasNext() ){
					String companyKey = (String) iterator.next();
					holdingCoRel = (Relation) allHoldingRelMap.get(companyKey);
					String originatingObjectID = holdingCoRel.getOriginatingObjectID();
					Holding replCompHolding = getNbaTXLife().getHolding(originatingObjectID);
					// now get the party using relatedObjectID
					String relatedObjectId = holdingCoRel.getRelatedObjectID();
					NbaParty companyParty = getNbaTXLife().getParty(relatedObjectId);
					// now lookup this key in Map and if the Object is present then retrieve that and update only policy number in Map
					if ( replacementDataMap.containsKey(companyParty.getParty().getPartyKey())){
						corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMap.get(companyParty.getParty().getPartyKey());
					}else{
						corrReplInfoVO = createReplacementCompanyVO(companyParty);	
						replacementDataMap.put(companyParty.getParty().getPartyKey(), corrReplInfoVO);
					}	
					
					corrReplInfoVO.getPolicyNumberList().add(replCompHolding.getPolicy().getPolNumber());
				}
			}
		}catch ( Exception e ){
			NbaBaseException nce = new NbaBaseException(e);
			NbaLogFactory.getLogger(AxaWSInvoker.class).logException("Unable to initilize replacement data " , nce); 
			throw nce;
		}
		
		
	}
	
	/**
     * Method used for get the company address details
     * @param companyParty
     * @return AxaCorrespondenceReplacementInfoVO
     * @throws NbaDataAccessException
     */
	protected AxaCorrespondenceReplacementInfoVO createReplacementCompanyVO(NbaParty companyParty) throws NbaDataAccessException {
		AxaCorrespondenceReplacementInfoVO corrReplInfoVO = new AxaCorrespondenceReplacementInfoVO();
		corrReplInfoVO.setCompanyKey(companyParty.getParty().getPartyKey());
		corrReplInfoVO.setCompanyFullName(companyParty.getParty().getFullName());
		NbaTableAccessor nta = new NbaTableAccessor();

		NbaTableData[] replaced_Company_details = nta.getReplacedCompanyAddressDetails(corrReplInfoVO.getCompanyKey());
		if(replaced_Company_details.length > 0){
			NbaReplacedCompanyAddressData nbaReplCompAdd = (NbaReplacedCompanyAddressData)replaced_Company_details[0];
			corrReplInfoVO.setOffLine1(nbaReplCompAdd.getAddressLine1());
			corrReplInfoVO.setOffLine2(nbaReplCompAdd.getAddressLine2());
			corrReplInfoVO.setOffCity(nbaReplCompAdd.getCity());
			corrReplInfoVO.setOffState(nbaReplCompAdd.getState());
			corrReplInfoVO.setOffZip(nbaReplCompAdd.getZip());
		}	
		return corrReplInfoVO ;
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
		if ( variables !=null && variables.size()>0 && replacementDataMap.size() >0) {
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
				NbaLogFactory.getLogger(AXAReplacementLettersProcessor.class).logDebug("Retrieved value of variable " + var + " = " + val);  
			}
		}
		return resolvedValueMap;
	}
	
	/**
     * Method used for get the Replaceemnt Company Full name at specific index 
     * @param index
     * @return String
     */	
	public String retrieveReplacementCompanyAt(int index){
		String value = "";
		if ( index > replacementDataMap.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMap.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMap.get(key);
				value = corrReplInfoVO.getCompanyFullName();
				break;
			}
			counter++;
		}
		return value;
	}

	/**
     * Method used for get the Replaceemnt Company office zip at specific index 
     * @param index
     * @return String
     */	
	public String retrieveOffZipAt(int index){
		String value = "";
		if ( index > replacementDataMap.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMap.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMap.get(key);
				value = corrReplInfoVO.getOffZip();
				break;
			}
			counter++;
		}
		return value;
	}	

	/**
     * Method used for get the Replaceemnt Company office city at specific index 
     * @param index
     * @return String
     */	
	public String retrieveOffCityAt(int index){
		String value = "";
		if ( index > replacementDataMap.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMap.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMap.get(key);
				value = corrReplInfoVO.getOffCity();
				break;
			}
			counter++;
		}
		return value;
	}	

	
	/**
     * Method used for get the Replaceemnt Company office state at specific index 
     * @param index
     * @return String
     */	
	public String retrieveOffStateAt(int index){
		String value = "";
		if ( index > replacementDataMap.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMap.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMap.get(key);
				value = corrReplInfoVO.getOffState();
				break;
			}
			counter++;
		}
		return value;
	}	

	
	/**
     * Method used for get the Replaceemnt Company office address line 1at specific index 
     * @param index
     * @return String
     */	
	public String retrieveOffLineAt1(int index){
		String value = "";
		if ( index > replacementDataMap.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMap.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMap.get(key);
				value = corrReplInfoVO.getOffLine1();
				break;
			}
			counter++;
		}
		return value;
	}	

	
	/**
     * Method used for get the Replaceemnt Company office address line 2 at specific index 
     * @param index
     * @return String
     */	
	
	public String retrieveOffLineAt2(int index){
		String value = "";
		if ( index > replacementDataMap.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMap.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMap.get(key);
				value = corrReplInfoVO.getOffLine2();
				break;
			}
			counter++;
		}
		return value;
	}

	
	/**
     * Method used for get the Replaceemnt Company particulart policy number at specific index 
     * @param i
     * @param j
     * @return String
     */	
	
	public String retrievePolicyNumberAtij(int i , int j){
		String value = "";
		if ( i > replacementDataMap.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMap.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == i ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMap.get(key);
				ArrayList polList = (ArrayList) corrReplInfoVO.getPolicyNumberList();
				if ( !( j > polList.size())){
					value = (String) polList.get(j-1);
					break;
				}
				break;
			}
			counter++;
		}
		return value;
	}
	
	/**
     * Method to get value of particular OINK varible  
     * @param index
     * @return String
     */	
	public String getValue (String variableName) {
		String resolvedValue  = "";
		if ("ReplacementCompany1".equals(variableName)) {
			return retrieveReplacementCompanyAt(1); 
		}else if ("ReplacementCompany2".equals(variableName)) {
			return retrieveReplacementCompanyAt(2);
		}else if ("ReplacementCompany3".equals(variableName)) {
			return retrieveReplacementCompanyAt(3);
		}else if ("ReplacementCompany4".equals(variableName)) {
			return retrieveReplacementCompanyAt(4);
		}else if ("ReplacementCompany5".equals(variableName)) {
			return retrieveReplacementCompanyAt(5);
		}else if ("OffZip1".equals(variableName)) {
			return retrieveOffZipAt(1);
		}else if ("OffZip2".equals(variableName)) {
			return retrieveOffZipAt(2);
		}else if ("OffZip3".equals(variableName)) {
			return retrieveOffZipAt(3);
		}else if ("OffZip4".equals(variableName)) {
			return retrieveOffZipAt(4);
		}else if ("OffZip5".equals(variableName)) {
			return retrieveOffZipAt(5);
		}else if ("OffCity1".equals(variableName)) {
			return retrieveOffCityAt(1);
		}else if ("OffCity2".equals(variableName)) {
			return retrieveOffCityAt(2);
		}else if ("OffCity3".equals(variableName)) {
			return retrieveOffCityAt(3);
		}else if ("OffCity4".equals(variableName)) {
			return retrieveOffCityAt(4);
		}else if ("OffCity5".equals(variableName)) {
			return retrieveOffCityAt(5);
		}else if ("OffState1".equals(variableName)) {
			return retrieveOffStateAt(1);
		}else if ("OffState2".equals(variableName)) {
			return retrieveOffStateAt(2);
		}else if ("OffState3".equals(variableName)) {
			return retrieveOffStateAt(3);
		}else if ("OffState4".equals(variableName)) {
			return retrieveOffStateAt(4);
		}else if ("OffState5".equals(variableName)) {
			return retrieveOffStateAt(5);
		}else if ("OffLine11".equals(variableName)) {
			return retrieveOffLineAt1(1);
		}else if ("OffLine12".equals(variableName)) {
			return retrieveOffLineAt2(1);
		}else if ("OffLine21".equals(variableName)) {
			return retrieveOffLineAt1(2);
		}else if ("OffLine22".equals(variableName)) {
			return retrieveOffLineAt2(2);
		}else if ("OffLine31".equals(variableName)) {
			return retrieveOffLineAt1(3);
		}else if ("OffLine32".equals(variableName)) {
			return retrieveOffLineAt2(3);
		}else if ("OffLine41".equals(variableName)) {
			return retrieveOffLineAt1(4);
		}else if ("OffLine42".equals(variableName)) {
			return retrieveOffLineAt2(4);
		}else if ("OffLine51".equals(variableName)) {
			return retrieveOffLineAt1(5);
		}else if ("OffLine52".equals(variableName)) {
			return retrieveOffLineAt2(5);
		}else if ("PolNumber11".equals(variableName)) {
			return retrievePolicyNumberAtij(1,1);
		}else if ("PolNumber12".equals(variableName)) {
			return retrievePolicyNumberAtij(1,2);
		}else if ("PolNumber13".equals(variableName)) {
			return retrievePolicyNumberAtij(1,3);
		}else if ("PolNumber14".equals(variableName)) {
			return retrievePolicyNumberAtij(1,4);
		}else if ("PolNumber15".equals(variableName)) {
			return retrievePolicyNumberAtij(1,5);
		}else if ("PolNumber21".equals(variableName)) {
			return retrievePolicyNumberAtij(2,1);
		}else if ("PolNumber22".equals(variableName)) {
			return retrievePolicyNumberAtij(2,2);
		}else if ("PolNumber23".equals(variableName)) {
			return retrievePolicyNumberAtij(2,3);
		}else if ("PolNumber24".equals(variableName)) {
			return retrievePolicyNumberAtij(2,4);
		}else if ("PolNumber25".equals(variableName)) {
			return retrievePolicyNumberAtij(2,5);
		}else if ("PolNumber31".equals(variableName)) {
			return retrievePolicyNumberAtij(3,1);
		}else if ("PolNumber32".equals(variableName)) {
			return retrievePolicyNumberAtij(3,2);
		}else if ("PolNumber33".equals(variableName)) {
			return retrievePolicyNumberAtij(3,3);
		}else if ("PolNumber34".equals(variableName)) {
			return retrievePolicyNumberAtij(3,4);
		}else if ("PolNumber35".equals(variableName)) {
			return retrievePolicyNumberAtij(3,5);
		}else if ("PolNumber41".equals(variableName)) {
			return retrievePolicyNumberAtij(4,1);
		}else if ("PolNumber42".equals(variableName)) {
			return retrievePolicyNumberAtij(4,2);
		}else if ("PolNumber43".equals(variableName)) {
			return retrievePolicyNumberAtij(4,3);
		}else if ("PolNumber44".equals(variableName)) {
			return retrievePolicyNumberAtij(4,4);
		}else if ("PolNumber45".equals(variableName)) {
			return retrievePolicyNumberAtij(4,5);
		}else if ("PolNumber51".equals(variableName)) {
			return retrievePolicyNumberAtij(5,1);
		}else if ("PolNumber52".equals(variableName)) {
			return retrievePolicyNumberAtij(5,2);
		}else if ("PolNumber53".equals(variableName)) {
			return retrievePolicyNumberAtij(5,3);
		}else if ("PolNumber54".equals(variableName)) {
			return retrievePolicyNumberAtij(5,4);
		}else if ("PolNumber55".equals(variableName)) {
			return retrievePolicyNumberAtij(5,5);
		}		
		return resolvedValue;
	}
}
