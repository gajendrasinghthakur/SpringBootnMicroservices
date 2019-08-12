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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.datamanipulation.NbaContractDataAccessConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaReasonsData;
import com.csc.fsg.nba.tableaccess.NbaReplacedCompanyAddressData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.AxaCorrespondenceReplacementInfoVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.SpecialInstruction;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.UnderwritingResult;
import com.csc.fsg.nba.vo.txlife.UnderwritingResultExtension;
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
 * <tr><td>AXAL3.7.13</td><td>AXA Life Phase 1</td><td>Formal Correspondence</td></tr>
 * <tr><td>ALPC195</td><td>AXA Life Phase 1</td><td>AUD Negative Correspondence</td></tr>
 * <tr><td>ALS4542</td><td>AXA Life Phase 1</td><td>QC # 3606 (Delivery Instruction information is missing from the TX1203)</td></tr>
 * <tr><td>CR1455063</td><td>Discretionary</td><td>Joint Insured Correspondence</td></tr>
 * </table>
 * <p>
 */

//New class coded as part of ALS4231
public class AXACorrespondenceVariableResolverProcessor extends AXACorrespondenceProcessorBase{
	
	Map replacementDataMap = new HashMap();
	Map replacement1035DataMap = new HashMap();//CR13444989
	Map internalReplacementDataMap = new HashMap();//APSL3725 
	Map internalReplacement1035DataMap = new HashMap();//APSL3725
	public static final String EXCHANGE_1035 = "1035";//CR13444989
	private NbaDst nbaParentDst; //ALS4476
	private String qualifier; // APSL3264(QC12223)
	
	/**
     * Default constructor
     */
	public AXACorrespondenceVariableResolverProcessor(){
		super();
		
	}
	/**
     * Parameterized constructor
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
	public AXACorrespondenceVariableResolverProcessor(NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) throws NbaBaseException {
		super(userVO, nbaTXLife, nbaDst, object);
		initilizeReplacementData();
		//ALS4476 - begin
		if ( object!=null && ((HashMap)object).keySet().contains(AXACorrespondenceConstants.PARENT_DST)){
		 	nbaParentDst = (NbaDst)((HashMap)object).get(AXACorrespondenceConstants.PARENT_DST);
		}else {
			nbaParentDst= null;
		}
		//ALS4476 - end
	}
	
	/**
     * Method used for initilizing the Replacemnt data used to retrieve value for OINK variables
	 * @throws NbaBaseException
     */
	private void initilizeReplacementData() throws NbaBaseException {
		try{
			AxaCorrespondenceReplacementInfoVO corrReplInfoVO = null;
			AxaCorrespondenceReplacementInfoVO corrReplInfo1035VO = null;//CR13444989
			if (getNbaTXLife().isReplacement()){
				Map allHoldingRelMap= getNbaTXLife().getAllRelationsForRole(NbaOliConstants.OLI_REL_HOLDINGCO);
				Relation holdingCoRel = null;
				Iterator iterator  = allHoldingRelMap.keySet().iterator();
				while( iterator.hasNext() ){
					String companyKey = (String) iterator.next();
					holdingCoRel = (Relation) allHoldingRelMap.get(companyKey);
					String originatingObjectID = holdingCoRel.getOriginatingObjectID();
					Holding replCompHolding = getNbaTXLife().getHolding(originatingObjectID);
					//ALS5595 Refactored
					if (replCompHolding != null && replCompHolding.getPolicy() != null) {
						PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(replCompHolding.getPolicy());
						
                        //now get the party using relatedObjectID - Code Refactored APSL1117 - SR#581629
						String relatedObjectId = holdingCoRel.getRelatedObjectID();
						NbaParty companyParty = getNbaTXLife().getParty(relatedObjectId);
						
						String replCompPartyKey = companyParty.getParty().getPartyKey(); //APSL1117 - SR#581629
						boolean isCompMONY = NbaConstants.AXA_COMPANY_MONY001.equalsIgnoreCase(replCompPartyKey) || NbaConstants.AXA_COMPANY_MONY002.equalsIgnoreCase(replCompPartyKey); //APSL1117 - SR#581629
						
						if (polExt.getReplacementIndCode() == NbaOliConstants.NBA_ANSWERS_YES) {//APSL3725
							//Begin APSL3725
							Map tempReplacementMap = null;
							Map tempReplacement1035Map = null;
							if(replCompHolding.getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_EXTERNAL || isCompMONY){
								tempReplacementMap = replacementDataMap;
								tempReplacement1035Map = replacement1035DataMap;
							}else if(replCompHolding.getPolicy().getReplacementType() == NbaOliConstants.OLI_REPTY_INTERNAL){
								tempReplacementMap = internalReplacementDataMap;
								tempReplacement1035Map = internalReplacement1035DataMap;
							}
							//end APSL3725
							if(tempReplacementMap != null && tempReplacement1035Map != null) {
								// now lookup this key in Map and if the Object is present then retrieve that and update only policy number in Map
								if (tempReplacementMap.containsKey(replCompPartyKey)) {
									corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) tempReplacementMap.get(replCompPartyKey);
								} else {
									corrReplInfoVO = createReplacementCompanyVO(companyParty);
									tempReplacementMap.put(replCompPartyKey, corrReplInfoVO);
								}
								//being CR13444989
								corrReplInfoVO.getPolicyNumberList().add(replCompHolding.getPolicy().getPolNumber());

								if (is1035Holding(replCompHolding)) {
									if (tempReplacement1035Map.containsKey(companyParty.getParty().getPartyKey())) {
										corrReplInfo1035VO = (AxaCorrespondenceReplacementInfoVO) tempReplacement1035Map.get(companyParty.getParty()
												.getPartyKey());
									} else {
										corrReplInfo1035VO = createReplacementCompanyVO(companyParty);
										tempReplacement1035Map.put(companyParty.getParty().getPartyKey(), corrReplInfo1035VO);
									}

									long mecStatus = getMECStatus(replCompHolding);
									corrReplInfo1035VO.getPolicyNumberList().add(replCompHolding.getPolicy().getPolNumber());
									corrReplInfo1035VO.getMECContractList().add(new Boolean(mecStatus == NbaOliConstants.OLI_MECSTATUS_CONTRACT_IS_MEC));
									corrReplInfo1035VO.getWithin7PayList().add(new Boolean(mecStatus == NbaOliConstants.OLI_MECSTATUS_WITHIN_7PAY));
								}
							}//end CR13444989
						}
					}
				}
			}
		}catch ( Exception e ){
			NbaBaseException nce = new NbaBaseException(e);
			NbaLogFactory.getLogger(AxaWSInvoker.class).logException("Unable to initilize replacement data " , nce); 
			// throw nce;   //ALS4853 
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
		if ( variables !=null && variables.size()>0) {
			Iterator itr = variables.iterator();
			while( itr.hasNext() ){
				String var = (String) itr.next();
				// now remove the qualifier if any
				StringTokenizer tokens = null;
				String variable = null;
				qualifier = null;
				tokens = new StringTokenizer(var, "_"); 
				if (tokens.countTokens() >= 2) {
					variable = tokens.nextToken();
					setQualifier(tokens.nextToken()); // APSL3264(QC12223)
				}else {
					variable = tokens.nextToken();
				}
				String val = getValue(variable);//CR13444989, APSL3264(QC12223)
				resolvedValueMap.put(var, val);
				NbaLogFactory.getLogger(AXACorrespondenceVariableResolverProcessor.class).logDebug("Retrieved value of variable " + var + " = " + val);  
			}
		}
		return resolvedValueMap;
	}
	
	/**
     * Method used for get the AUD Reason string  
     * @return String
     */	
	public String retrieveAUDReason() { // APSL3264(QC12223) Qualifier parameter removed
		long partyId = NbaContractDataAccessConstants.PARTY_JOINT_INSURED.equalsIgnoreCase(qualifier) ? NbaOliConstants.OLI_REL_JOINTINSURED
				: NbaOliConstants.OLI_REL_INSURED; // CR1455063
		String reasonStr = new String();
		StringBuffer reasonStrBuffer = new StringBuffer();
		ArrayList reasonList = new ArrayList();
		NbaTableAccessor nta = new NbaTableAccessor();
		try {
			ApplicationInfo api = getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo();
			if (api != null) {
				ApplicationInfoExtension apiExt = NbaUtils.getFirstApplicationInfoExtension(api);
				if (apiExt != null) {
					List urList = new ArrayList();
					// urList = apiExt.getUnderwritingResult();
					ArrayList urListtemp = apiExt.getUnderwritingResult(); // APSL5175
					urList = NbaUtils.getUWReasonResultList(urListtemp); // APSL5175
					int uwResultCount = urList.size();
					UnderwritingResultExtension uwResultExt = null;
					for (int j = 0; j < uwResultCount; j++) {
						UnderwritingResult uwResult = (UnderwritingResult) urList.get(j);
						//get the reason and reasontype and fire the query to DB to get AUD letter text.
						long uwReasonTc = uwResult.getUnderwritingResultReason();
						uwResultExt = NbaUtils.getFirstUnderwritingResultExtension(uwResult);
						if (uwResultExt != null
								&& uwResultExt.getUnderwritingReasonType() == Long.valueOf(NbaOliConstants.OLI_EXT_FINAL_DISP).longValue()
								&& ((uwResult.getRelatedObjectID() != null && uwResult.getRelatedObjectID().equalsIgnoreCase(
										getNbaTXLife().getPartyId(partyId))) || (uwResult.getRelatedObjectID() == null && partyId == NbaOliConstants.OLI_REL_INSURED))) { // CR1455063
							HashMap caseData = new HashMap();
							caseData.put("backendSystem", getNbaDst().getNbaLob().getBackendSystem());
							caseData.put("plan", getNbaDst().getNbaLob().getPlan());
							caseData.put("company", getNbaDst().getNbaLob().getCompany());
							NbaTableData data[] = nta.getAUDLetterText(caseData, NbaOliConstants.OLI_EXT_FINAL_DISP, String.valueOf(uwReasonTc));
							if (data.length > 0) {
								reasonStr = ((NbaReasonsData) data[0]).getAudLettersText();
							}
							//QC#5288 APSL728 SR540843 begin
							if (reasonStr != null && reasonStr.indexOf(NbaConstants.AUDLETTER_VAR) != -1) {
								reasonStr = uwResult.getSupplementalText() != null ? reasonStr.replaceFirst(NbaConstants.AUDLETTER_VAR, uwResult
										.getSupplementalText()) : reasonStr.replaceFirst(NbaConstants.AUDLETTER_VAR, "");
							}
							//QC#5288 APSL728 SR540843 end
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
			int count = reasonList.size();

			switch (count) {
			case 0:
				break;
			case 1:
				reasonStrBuffer.append((String) reasonList.get(0));
				reasonStr = reasonStrBuffer.toString();
				break;
			case 2:
				reasonStrBuffer.append((String) reasonList.get(0));
				reasonStrBuffer.append(" and ");
				reasonStrBuffer.append((String) reasonList.get(1));
				reasonStr = reasonStrBuffer.toString();
				break;
			default:
				for (int k = 0; k < count - 1; k++) {
					reasonStrBuffer.append((String) reasonList.get(k));
					reasonStrBuffer.append(", ");
				}
				reasonStr = reasonStrBuffer.toString();
				if (reasonStrBuffer.length() > 0) {
					reasonStr = reasonStrBuffer.substring(0, reasonStrBuffer.length() - 2); //Remove last comma and space from the list
				}
				reasonStr = reasonStr + " and " + ((String) reasonList.get(count - 1));

			}
		} catch (Exception e) {
			getLogger().logException("Unable to get Value for AUDReason ", e);
		}
		return reasonStr; 
	}
	
	
	/**
     * Method used for get the Comments from NbaAuxiliary Comments table   
     * @return String
     */	
	//ALS4476 - New method
	public String retrieveSpecHandInst() {
		StringBuffer specialInstructionBuffer = new StringBuffer();
		SpecialInstruction specInst = null; //ALS4954
		String specInstStr = new String();
		try {
			Holding holding = getNbaTXLife().getPrimaryHolding();
			//begin ALS4542
			for (int i=0; i<holding.getAttachmentCount();i++)
			{
				Attachment  attach = holding.getAttachmentAt(i);
				if(NbaOliConstants.OLI_ATTACH_SPEC_HANDL_INST == attach.getAttachmentType()){//ALS5608
						String pcData = attach.getAttachmentData().getPCDATA();   //ALS4954
						specInst = SpecialInstruction.unmarshal(new ByteArrayInputStream(pcData.getBytes()));  //ALS4954
						specialInstructionBuffer.append(specInst.getComment()); //ALS4954
						specialInstructionBuffer.append(AXACorrespondenceConstants.SPECIAL_INSTRUCTION_DELIMITER);
				}
			}
			//end ALS4542
			//remove last delimiter from string
			if ( specialInstructionBuffer.length() > 2){
				specInstStr = specialInstructionBuffer.substring(0, specialInstructionBuffer.length() - 2);
			}
		}catch ( Exception e){
			getLogger().logException("Unable to get Value for SpecHandInst" , e); //ALS4954
		}
		return specInstStr;
	}
	
	/**
	 * @param retrieveCommentsResult
	 */
	//ALS4476 - New method added
	protected List retrieveComments(AccelResult retrieveCommentsResult) {
		NbaDst nbaDst = null;
		List commentsForWorkItem = null;
		if (retrieveCommentsResult != null && !retrieveCommentsResult.hasErrors()) {
			nbaDst = (NbaDst) retrieveCommentsResult.getFirst();
			if (nbaDst != null) {
				if (nbaDst.isCase()) {
					commentsForWorkItem = nbaDst.getWorkItem().getComments();
				}
			}
		}
		return commentsForWorkItem;
	}
	
	
	
	/**
     * Method used for get the Replaceemnt Company Full name at specific index 
     * @param index
     * @return String
     */	
	//CR13444989 method refactored
	public String retrieveReplacementCompanyAt(int index){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( index > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
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
	//CR13444989 method refactored
	public String retrieveOffZipAt(int index){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( index > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
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
	//CR13444989 method refactored
	public String retrieveOffCityAt(int index){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( index > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
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
	//CR13444989 method refactored
	public String retrieveOffStateAt(int index){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( index > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
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
	//CR13444989 method refactored
	public String retrieveOffLineAt1(int index){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( index > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
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
	//CR13444989 method refactored
	public String retrieveOffLineAt2(int index){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( index > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
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
	//CR13444989 method refactored
	public String retrievePolicyNumberAtij(int i , int j){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( i > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == i ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
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
     * Method used for get the Replaceemnt Company particulart policy number at specific index 
     * @param i
     * @param j
     * @return String
     */	
	//CR13444989 method refactored
	public String retrieveMECContractAtij(int i , int j){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( i > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == i ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
				ArrayList mecList = (ArrayList) corrReplInfoVO.getMECContractList();
				if ( !( j > mecList.size())){
					value = ((Boolean) mecList.get(j-1)).toString();
					break;
				}
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
	//CR13444989 method refactored
	public String retrieveWithin7PayAtij(int i , int j){ // APSL3264(QC12223) Qualifier parameter removed
		Map replacementDataMapToUse = replacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = replacement1035DataMap;
		}
		String value = "";
		if ( i > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == i ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
				ArrayList within7PayList = (ArrayList) corrReplInfoVO.getWithin7PayList();
				if ( !( j > within7PayList.size())){
					value = ((Boolean) within7PayList.get(j-1)).toString();
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
     * @param variableName
     * @return String
     */	
	//Refactored CR13444989
	// APSL3264(QC12223) qualifier removed
	public String getValue (String variableName) {
		if ("AUDReason".equalsIgnoreCase(variableName)) {
			return retrieveAUDReason(); // CR1455063, APSL3264(QC12223)
		}else if ("ReplacementCompany1".equalsIgnoreCase(variableName)) {
			return retrieveReplacementCompanyAt(1); 
		}else if ("ReplacementCompany2".equalsIgnoreCase(variableName)) {
			return retrieveReplacementCompanyAt(2);
		}else if ("ReplacementCompany3".equalsIgnoreCase(variableName)) {
			return retrieveReplacementCompanyAt(3);
		}else if ("ReplacementCompany4".equalsIgnoreCase(variableName)) {
			return retrieveReplacementCompanyAt(4);
		}else if ("ReplacementCompany5".equalsIgnoreCase(variableName)) {
			return retrieveReplacementCompanyAt(5);
		}else if ("OffZip1".equalsIgnoreCase(variableName)) {
			return retrieveOffZipAt(1);
		}else if ("OffZip2".equalsIgnoreCase(variableName)) {
			return retrieveOffZipAt(2);
		}else if ("OffZip3".equalsIgnoreCase(variableName)) {
			return retrieveOffZipAt(3);
		}else if ("OffZip4".equalsIgnoreCase(variableName)) {
			return retrieveOffZipAt(4);
		}else if ("OffZip5".equalsIgnoreCase(variableName)) {
			return retrieveOffZipAt(5);
		}else if ("OffCity1".equalsIgnoreCase(variableName)) {
			return retrieveOffCityAt(1);
		}else if ("OffCity2".equalsIgnoreCase(variableName)) {
			return retrieveOffCityAt(2);
		}else if ("OffCity3".equalsIgnoreCase(variableName)) {
			return retrieveOffCityAt(3);
		}else if ("OffCity4".equalsIgnoreCase(variableName)) {
			return retrieveOffCityAt(4);
		}else if ("OffCity5".equalsIgnoreCase(variableName)) {
			return retrieveOffCityAt(5);
		}else if ("OffState1".equalsIgnoreCase(variableName)) {
			return retrieveOffStateAt(1);
		}else if ("OffState2".equalsIgnoreCase(variableName)) {
			return retrieveOffStateAt(2);
		}else if ("OffState3".equalsIgnoreCase(variableName)) {
			return retrieveOffStateAt(3);
		}else if ("OffState4".equalsIgnoreCase(variableName)) {
			return retrieveOffStateAt(4);
		}else if ("OffState5".equalsIgnoreCase(variableName)) {
			return retrieveOffStateAt(5);
		}else if ("OffLine11".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt1(1);
		}else if ("OffLine12".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt2(1);
		}else if ("OffLine21".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt1(2);
		}else if ("OffLine22".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt2(2);
		}else if ("OffLine31".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt1(3);
		}else if ("OffLine32".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt2(3);
		}else if ("OffLine41".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt1(4);
		}else if ("OffLine42".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt2(4);
		}else if ("OffLine51".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt1(5);
		}else if ("OffLine52".equalsIgnoreCase(variableName)) {
			return retrieveOffLineAt2(5);
		}else if ("PolNumber11".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(1,1);
		}else if ("PolNumber12".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(1,2);
		}else if ("PolNumber13".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(1,3);
		}else if ("PolNumber14".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(1,4);
		}else if ("PolNumber15".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(1,5);
		}else if ("PolNumber21".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(2,1);
		}else if ("PolNumber22".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(2,2);
		}else if ("PolNumber23".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(2,3);
		}else if ("PolNumber24".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(2,4);
		}else if ("PolNumber25".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(2,5);
		}else if ("PolNumber31".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(3,1);
		}else if ("PolNumber32".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(3,2);
		}else if ("PolNumber33".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(3,3);
		}else if ("PolNumber34".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(3,4);
		}else if ("PolNumber35".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(3,5);
		}else if ("PolNumber41".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(4,1);
		}else if ("PolNumber42".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(4,2);
		}else if ("PolNumber43".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(4,3);
		}else if ("PolNumber44".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(4,4);
		}else if ("PolNumber45".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(4,5);
		}else if ("PolNumber51".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(5,1);
		}else if ("PolNumber52".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(5,2);
		}else if ("PolNumber53".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(5,3);
		}else if ("PolNumber54".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(5,4);
		}else if ("PolNumber55".equalsIgnoreCase(variableName)) {
			return retrievePolicyNumberAtij(5,5);
		}else if ("MECContract11".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(1,1);
		}else if ("MECContract12".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(1,2);
		}else if ("MECContract13".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(1,3);
		}else if ("MECContract14".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(1,4);
		}else if ("MECContract15".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(1,5);
		}else if ("MECContract21".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(2,1);
		}else if ("MECContract22".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(2,2);
		}else if ("MECContract23".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(2,3);
		}else if ("MECContract24".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(2,4);
		}else if ("MECContract25".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(2,5);
		}else if ("MECContract31".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(3,1);
		}else if ("MECContract32".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(3,2);
		}else if ("MECContract33".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(3,3);
		}else if ("MECContract34".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(3,4);
		}else if ("MECContract35".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(3,5);
		}else if ("MECContract41".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(4,1);
		}else if ("MECContract42".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(4,2);
		}else if ("MECContract43".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(4,3);
		}else if ("MECContract44".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(4,4);
		}else if ("MECContract45".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(4,5);
		}else if ("MECContract51".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(5,1);
		}else if ("MECContract52".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(5,2);
		}else if ("MECContract53".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(5,3);
		}else if ("MECContract54".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(5,4);
		}else if ("MECContract55".equalsIgnoreCase(variableName)) {
			return retrieveMECContractAtij(5,5);
		}else if ("Within7Pay11".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(1,1);
		}else if ("Within7Pay12".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(1,2);
		}else if ("Within7Pay13".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(1,3);
		}else if ("Within7Pay14".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(1,4);
		}else if ("Within7Pay15".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(1,5);
		}else if ("Within7Pay21".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(2,1);
		}else if ("Within7Pay22".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(2,2);
		}else if ("Within7Pay23".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(2,3);
		}else if ("Within7Pay24".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(2,4);
		}else if ("Within7Pay25".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(2,5);
		}else if ("Within7Pay31".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(3,1);
		}else if ("Within7Pay32".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(3,2);
		}else if ("Within7Pay33".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(3,3);
		}else if ("Within7Pay34".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(3,4);
		}else if ("Within7Pay35".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(3,5);
		}else if ("Within7Pay41".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(4,1);
		}else if ("Within7Pay42".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(4,2);
		}else if ("Within7Pay43".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(4,3);
		}else if ("Within7Pay44".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(4,4);
		}else if ("Within7Pay45".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(4,5);
		}else if ("Within7Pay51".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(5,1);
		}else if ("Within7Pay52".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(5,2);
		}else if ("Within7Pay53".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(5,3);
		}else if ("Within7Pay54".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(5,4);
		}else if ("Within7Pay55".equalsIgnoreCase(variableName)) {
			return retrieveWithin7PayAtij(5,5);
		}else if ("SpecHandInst".equalsIgnoreCase(variableName)) {
			return retrieveSpecHandInst();
		}else if ("InternalReplacementCompany1".equalsIgnoreCase(variableName)) {//Begin APSL3725
			return retrieveInternalReplacementCompanyAt(1); 
		}else if ("InternalReplacementCompany2".equalsIgnoreCase(variableName)) {
			return retrieveInternalReplacementCompanyAt(2);
		}else if ("InternalReplacementCompany3".equalsIgnoreCase(variableName)) {
			return retrieveInternalReplacementCompanyAt(3);
		}else if ("InternalReplacementCompany4".equalsIgnoreCase(variableName)) {
			return retrieveInternalReplacementCompanyAt(4);
		}else if ("InternalReplacementCompany5".equalsIgnoreCase(variableName)) {
			return retrieveInternalReplacementCompanyAt(5);			
		}//end APSL3725
		return "";
	}
	
	//ALS4766
	public NbaDst getNbaParentDst() {
		return nbaParentDst;
	}

	//ALS4766
	public void setNbaParentDst(NbaDst nbaParentDst) {
		this.nbaParentDst = nbaParentDst;
	}
	
	//CR13444989 new method added
	public boolean is1035Holding(Holding holding) {
		Life life = holding.getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
		LifeUSAExtension lifeUsext = NbaUtils.getFirstLifeUSAExtension(life.getLifeUSA());
		if (lifeUsext != null && NbaUtils.isAnsweredYes(lifeUsext.getExch1035IndCode())) {
			return true;
}
		return false;
	}
	//CR13444989 new method added
	public long getMECStatus(Holding holding) {
		Policy policy = holding.getPolicy();
		if (policy != null) {
			Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			LifeUSAExtension lifeUsext = NbaUtils.getFirstLifeUSAExtension(life.getLifeUSA());
			if (lifeUsext != null) {
				return lifeUsext.getMECStatus();
			}
		}
		return NbaOliConstants.OLI_TC_NULL;
	}
	/**
	 * @return the qualifier
	 */
	// APSL3264(QC12223)
	public String getQualifier() {
		return qualifier;
	}
	/**
	 * @param qualifier the qualifier to set
	 */
	// APSL3264(QC12223)
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	
	/**
     * Method used for get the Replaceemnt Company Full name at specific index 
     * @param index
     * @return String
     */	
	//New Method APSL3725
	public String retrieveInternalReplacementCompanyAt(int index){
		Map replacementDataMapToUse = internalReplacementDataMap;
		if(qualifier!= null && qualifier.equals(EXCHANGE_1035)){
			replacementDataMapToUse = internalReplacement1035DataMap;
		}
		String value = "";
		if ( index > replacementDataMapToUse.size()){
			return value;
		}
		int counter =1;
		Iterator iterator  = replacementDataMapToUse.keySet().iterator();
		while ( iterator.hasNext()){
			String key = (String) iterator.next();
			if ( counter == index ){
				//get the object of this key and return the value
				AxaCorrespondenceReplacementInfoVO corrReplInfoVO = (AxaCorrespondenceReplacementInfoVO) replacementDataMapToUse.get(key);
				value = corrReplInfoVO.getCompanyFullName();
				break;
			}
			counter++;
		}
		return value;
	}
	
	
}
