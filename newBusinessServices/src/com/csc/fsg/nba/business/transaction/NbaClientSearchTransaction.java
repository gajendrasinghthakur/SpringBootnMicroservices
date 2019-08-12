package com.csc.fsg.nba.business.transaction;

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
 * 
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Criteria;
import com.csc.fsg.nba.vo.txlife.CriteriaExpression;
import com.csc.fsg.nba.vo.txlife.CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension;
import com.csc.fsg.nba.vo.txlife.CriteriaOrCriteriaExpression;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PropertyValue;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vpms.NbaVpmsClientSearchData;

/**
 * NbaClientSearchTransaction will be used to create NbaTXLife 301 transaction for WebService.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA067</td><td>Version 3</td><td>Client Search</td></tr>
 * <tr><td>SPR1572</td><td>Version 5</td><td>Portal process does not add party key for the party object</td></tr>
 * <tr><td>SPR3327</td><td>Version 7</td><td>Client Search - Criteria Operator is not correct</td></tr>
 * <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaClientSearchTransaction extends NbaBusinessTransactions{
    //begin SPR3327
    public static final String EXACT = "EXACT";
	public static final String PHONETIC = "PHONETIC";
	public static final String CONTAINS = "CONTAINS";
	public static final String STARTSWITH = "STARTSWITH";
	public static final String PARTY_KEY = "PartyKey";
    //end SPR3327
	/**
	 * Constructor for NbaClientSearchTransaction.
	 */
	public NbaClientSearchTransaction() {
		super();
	}

	/**
	 * This method takes 3 arguments and creates TXLife 301 transaction for WebService.
	 * @param cs_Params The list of search criteria for client
	 * @param nbaDst An instance of NbaDst
	 * @param userVO The value object representation of the logged on user
	 * @return txLife301 NbaTXLife object
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	public NbaTXLife createTXLife301(java.util.List cs_Params, NbaDst nbaDst, NbaUserVO userVO) throws NbaBaseException {
	    //SPR3327 code deleted
		int numOfCriteria = 0;
		java.util.List cs_ParamNames = new ArrayList();
		cs_ParamNames.add("Search");
		cs_ParamNames.add("Client");
		cs_ParamNames.add("LastName");
		cs_ParamNames.add("FirstName");
		cs_ParamNames.add("MiddleName");
		cs_ParamNames.add("BirthDate");
		cs_ParamNames.add("BirthState");
		cs_ParamNames.add("GovtID");
		cs_ParamNames.add("Gender");
		cs_ParamNames.add("DBA");
		cs_ParamNames.add("GovtID");

		//begin SPR3327
        long search_Operator = -1;
        String cs_Params_value = cs_Params.get(0).toString();
        if (EXACT.equalsIgnoreCase(cs_Params_value)) {
            search_Operator = NbaOliConstants.OLI_OP_EQUAL;
        } else if (PHONETIC.equalsIgnoreCase(cs_Params_value)) {
            search_Operator = NbaOliConstants.OLI_OP_LIKE;
        } else if (CONTAINS.equalsIgnoreCase(cs_Params_value)) {
            search_Operator = NbaOliConstants.LOGICAL_OPERATOR_WILDCARDMATCH;
        } else if (STARTSWITH.equalsIgnoreCase(cs_Params_value)) {
            search_Operator = NbaOliConstants.OLI_OP_GREATERTHAN;
        }
        //end SPR3327

			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_PARTYSRCH);
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			nbaTXRequest.setNbaUser(userVO);
			NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
			TXLife reqTXLife = nbaReqTXLife.getTXLife();
			TXLifeRequest aNbaTXLifeRequest = reqTXLife.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);

			CriteriaExpression aCriteriaExpression = new CriteriaExpression();
			CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension aCriteriaExpOrOpr = new CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension();//ALPC7 object changed
	
			for (int i=2; i<=10; i++){
			if (cs_Params.get(i).toString().length() > 0) {
				CriteriaOrCriteriaExpression aCriteriaOrCriteriaExpression = new CriteriaOrCriteriaExpression();
				Criteria aCriteria = new Criteria();
				aCriteria.setObjectType(NbaOliConstants.OLI_PARTY);//SPR3327
				aCriteria.setPropertyName(cs_ParamNames.get(i).toString());
				PropertyValue aPropertyValue = new PropertyValue();
				switch(i) {
					case 5:	String cs_Format_BirthDt = cs_Params.get(i).toString().substring(6)+ "-"+cs_Params.get(i).toString().substring(0,2)+"-"+cs_Params.get(i).toString().substring(3,5);
					        aPropertyValue.setPCDATA(cs_Format_BirthDt);
					        break;
                    case 7:
                    case 10:
                            String cs_Format_ssn = cs_Params.get(i).toString().substring(0,3)+cs_Params.get(i).toString().substring(4,6)+ cs_Params.get(i).toString().substring(7,11);
					        aPropertyValue.setPCDATA(cs_Format_ssn);
					        break;  
    			    default:aPropertyValue.setPCDATA(cs_Params.get(i).toString());
				}             
				if ((CONTAINS.equalsIgnoreCase(cs_Params_value)) && ((i == 2) || (i == 9))) {//SPR3327
					aPropertyValue.setPCDATA(cs_Params.get(i).toString() + "%");
				}
				aCriteria.setPropertyValue(aPropertyValue);
				aCriteria.setOperation(search_Operator);
				aCriteriaOrCriteriaExpression.setCriteria(aCriteria);
				aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(aCriteriaOrCriteriaExpression);
				numOfCriteria++;
			}
			}	
		if (numOfCriteria++ > 1) {
		    aCriteriaExpOrOpr.setCriteriaOperator(NbaOliConstants.LOGICAL_OPERATOR_AND);//SPR3327
		}
		aCriteriaExpression.setCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension(aCriteriaExpOrOpr);//ALPC7 object changed
		aNbaTXLifeRequest.setCriteriaExpression(aCriteriaExpression);
		
		return nbaReqTXLife;
	}
	
    /** This method takes 4 arguments and creates TXLife 301 transaction for WebService for Portal cases.
     *  @param user The value object representation of the logged on user
     *  @param party The Party 
     *  @param data  The result of the VPMS calculation for the  ClientSearch model
     *  @param partyType The party type code
     *  @return NbaTXLife Object
     */	 
		public NbaTXLife createTXLife301(NbaUserVO user, Party party, NbaVpmsClientSearchData data, String partyType) {
		    //SPR3327 code deleted
			int numOfCriteria = 0;
			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_PARTYSRCH);
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			nbaTXRequest.setNbaUser(user);
			NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
			TXLife reqTXLife = nbaReqTXLife.getTXLife();
			TXLifeRequest aNbaTXLifeRequest = reqTXLife.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
			CriteriaExpression aCriteriaExpression = new CriteriaExpression();
			CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension aCriteriaExpOrOpr = new CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension();//ALPC7 object changed

			//if (party.getPartyTypeCode() == 1) {
			if (partyType.equalsIgnoreCase(String.valueOf(NbaOliConstants.OLI_PT_PERSON))){//SPR3327
				if (data.getLastNameOrOrganization().length() > 0) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getLastNameOrOrganization(), party
	                        .getPersonOrOrganization().getPerson().getLastName()));//SPR3327
					numOfCriteria++;
				}
				if (data.getFirstNameOrTaxID().length() > 0) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getFirstNameOrTaxID(), party
	                        .getPersonOrOrganization().getPerson().getFirstName()));//SPR3327
					numOfCriteria++;
				}
				if (data.getMiddleName().length() > 0) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getMiddleName(), party
	                        .getPersonOrOrganization().getPerson().getMiddleName()));//SPR3327
					numOfCriteria++;
				}
				if (data.getBirthDate() != null && data.getBirthDate().length() > 0 && party.getPersonOrOrganization() != null && party.getPersonOrOrganization().getPerson() != null && party.getPersonOrOrganization().getPerson().getBirthDate() != null) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getBirthDate(), NbaUtils
	                        .getStringFromDate(party.getPersonOrOrganization().getPerson().getBirthDate())));//SPR3327
					numOfCriteria++;
				}
				if (data.getGender().length() > 0) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getGender(), String.valueOf(party
	                        .getPersonOrOrganization().getPerson().getGender())));//SPR3327
					numOfCriteria++;
				}
				if (data.getGovtId().length() > 0) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getGovtId(), party.getGovtID()));//SPR3327
					numOfCriteria++;
				}
				if (data.getBirthState() != null) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getBirthState(), String.valueOf(party
	                        .getPersonOrOrganization().getPerson().getBirthJurisdictionTC())));//SPR3327
					numOfCriteria++;
				}
			} else {
				if (data.getLastNameOrOrganization().length() > 0) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getLastNameOrOrganization(), party
	                        .getPersonOrOrganization().getOrganization().getDBA()));//SPR3327
					numOfCriteria++;
				}
				if (data.getFirstNameOrTaxID().length() > 0) {
				    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(data.getFirstNameOrTaxID(), party.getGovtID()));//SPR3327
					numOfCriteria++;
				}

			}
			if (numOfCriteria++ > 1) {
			    aCriteriaExpOrOpr.setCriteriaOperator(NbaOliConstants.LOGICAL_OPERATOR_AND);//SPR3327
			}
			aCriteriaExpression.setCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension(aCriteriaExpOrOpr);//ALPC7 object changed
			aNbaTXLifeRequest.setCriteriaExpression(aCriteriaExpression);
			return nbaReqTXLife;
		}
	/**
	 * CALL the Client Search Web Service and retrieve the Response from it
	 * @param user The value object representation of the logged on user
	 * @param Party The Party 
	 * @return nbaTXLife nbaTXLife Object
	 */
		public NbaTXLife createTXLife301(NbaUserVO user, Party party) {
		    //SPR3327 code deleted
			int numOfCriteria = 0;
			NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_PARTYSRCH);
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			nbaTXRequest.setNbaUser(user);
			NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
			TXLife reqTXLife = nbaReqTXLife.getTXLife();
			TXLifeRequest aNbaTXLifeRequest = reqTXLife.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
			CriteriaExpression aCriteriaExpression = new CriteriaExpression();
			CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension aCriteriaExpOrOpr = new CriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension();//ALPC7 object changed

			if (party.getId().length() > 0) {
			    aCriteriaExpOrOpr.addCriteriaOrCriteriaExpression(createCriteriaOrCriteriaExpression(PARTY_KEY,party.getPartyKey()));//SPR3327
				numOfCriteria++;
			}
			aCriteriaExpression.setCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension(aCriteriaExpOrOpr);//ALPC7 object changed
			aNbaTXLifeRequest.setCriteriaExpression(aCriteriaExpression);
			return nbaReqTXLife;
		}
		
		/**
		 * create and returns the CriteriaOrCriteriaExpression object
		 * @param propertyName propery name
		 * @param propertyValue property value
		 * @return CriteriaOrCriteriaExpression nbaTXLife Object
		 */
		//SPR3327 New Method
		public CriteriaOrCriteriaExpression createCriteriaOrCriteriaExpression(String propertyName, String propertyValue) {
	        CriteriaOrCriteriaExpression aCriteriaOrCriteriaExpression = new CriteriaOrCriteriaExpression();
	        Criteria aCriteria = new Criteria();
	        aCriteria.setObjectType(NbaOliConstants.OLI_PARTY);
	        aCriteria.setPropertyName(propertyName);
	        PropertyValue aPropertyValue = new PropertyValue();
	        aPropertyValue.setPCDATA(propertyValue);
	        aCriteria.setPropertyValue(aPropertyValue);
	        aCriteria.setOperation(NbaOliConstants.OLI_OP_EQUAL);
	        aCriteriaOrCriteriaExpression.setCriteria(aCriteria);
	        return aCriteriaOrCriteriaExpression;
	    }	
}
