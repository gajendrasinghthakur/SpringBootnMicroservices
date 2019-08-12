package com.csc.fsg.nba.backendadapter.cyberlife;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaObjectPrinter;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaFundsData;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaRequirementsData;
import com.csc.fsg.nba.tableaccess.NbaRolesData;
import com.csc.fsg.nba.tableaccess.NbaStatesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.tableaccess.NbaUctData;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.AccountHolderNameCC;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.DataTransmittalSubType;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;

/**
* This class handles the formatting for requests to Cyberlife.
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
* <tr><td>SPR1045</td><td>Version 2</td><td>Added translation table for commission code</td></tr>
* <tr><td>SPR1018</td><td>Version 2</td><td>Deleted unused declaration</td></tr>
* <tr><td>NBA006</td><td>Version 2</td><td>Annuity support</td></tr>
* <tr><td>NBA009</td><td>Version 2</td><td>Cashiering Component</td></tr>
* <tr><td>NBA044</td><td>Version 3</td><td>Architecure changes</td></tr>
* <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
* <tr><td>NBA076</td><td>Version 3</td><td>Added logic for sitcode table</td></tr>
* <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
* <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
* <tr><td>SPR1186</td><td>Version 4</td><td>Incorrect table used for Roles for Annuities</td></tr>
* <tr><td>SPR2089</td><td>Version 4</td><td>CovOption delete does not work</td></tr>
* <tr><td>SPR1549</td><td>Version 4</td><td>UFlat/Table Rating applied to Coverage is wrongly translated to Flat Permanent Rating</td></tr>
* <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
* <tr><td>SPR2149</td><td>Version 4</td><td>Face amount LOB is not set when Current amount has 9 or more digits</td></tr>
* <tr><td>SPR2159</td><td>Version 4</td><td>Pull substandard effective date from coverage effective date.</td></tr>
* <tr><td>SPR2278</td><td>Version 5</td><td>Endorsements not appearing in UW in wrappered mode.</td></tr>
* <tr><td>SPR2306</td><td>Version 5</td><td>Endorsement type not translating properly in wrappered mode.</td></tr>
* <tr><td>SPR1163</td><td>Version 5</td><td>Adding Endorsements to an Annuity does not work</td></tr>
* <tr><td>SPR2751</td><td>Version 5</td><td>Entering a semi-colon ( ";") in underwriter notes comments on the underwriter workbench causes case to go to hosterrd when the case is issued (IS00)</td></tr>
* <tr><td>NBA130</td><td>Version 6</td><td>Requirements Reinsurance Project</td></tr>
* <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
* <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
* <tr><td>SPR2151</td><td>Version 8</td><td>Correct the Contract Validation edits and Adaptor logic for EFT, PAC and Credit Card Billing</td></tr>
* <tr><td>SPR1738</td><td>AXA Life Phase 1</td><td>Substandard Rating Should be Arranged Correctly In ACORD XML Message Out from nbA</td></tr>
* <tr><td>NBA223</td><td>AXA Life Phase 1</td><td>Underwriter Final Disposition Project</td></tr>
* <tr><td>SPR3171</td><td>Version 8</td><td>Requirement Restriction of 5 Needs to Be Added to Tables Schema</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 1
*/
public class NbaCyberRequests implements NbaCyberConstants, NbaOliConstants {
	// logger reference
	private NbaLogger logger = null;

	// CyberLife Field Types
	public static final int CT_CHAR = 0;
	public static final int CT_USHORT = 1;
	public static final int CT_DOUBLE = 2;
	public static final int CT_BIT = 3;
	public static final int CT_DATE = 4;
	public static final int CT_DEFAULT = 5; // There was no info in the spreadsheet

	// CyberLife Translation Types
	public static final String CYBTRANS_NONE = "CYBTRANS_NONE";
	public static final String CYBTRANS_DATE = "CYBTRANS_DATE";
	public static final String CYBTRANS_PLAN = "NBA_PLANS";
	public static final String CYBTRANS_ROLES = "NBA_ROLES";
	public static final String CYBTRANS_FUNDS = "NBA_FUNDS";

	// CyberLife Translation Table Types
	public static final int CYBTBL_NONE = 0;
	public static final int CYBTBL_UCT = 1;
	public static final int CYBTBL_STATE = 2;
	public static final int CYBTBL_STATE_TC = 3;
	public static final int CYBTBL_PLAN = 4;
	public static final int CYBTBL_PARTIC_ROLE = 5;
	public static final int CYBTBL_PARTIC_ROLE_F = 6;
	public static final int CYBTBL_RELATION_ROLE = 7;
	public static final int CYBTBL_RELATION_ROLE_F = 8;
	public static final int CYBTBL_PRODUCT_TYPE = 9;
	public static final int CYBTBL_FUNDS = 10;
	public static final int CYBTBL_PLAN_COV_KEY = 11;
	public static final int CYBTBL_UCT_BY_INDEX_TRANS = 12;
	public static final int CYBTBL_REQUIREMENTS = 13;
	public static final int CYBTBL_STATE_ALPHA = 14;
	public static final int CYBTBL_RELATION_ROLE_USER_SPEC = 15;
	public static final int CYBTBL_PARTIC_ROLE_USER_SPEC = 16;

	// Codes for DB queries
	// NBA076
	public String compCode = null;
	public static final String DEFAULT_COVID = "*";

	// Hashtable containing cached tables
	protected HashMap tblMap;

	// Hashtable containing party-relationship to DXE id mapping
	protected HashMap partyMap;

	// formating Constants
	public static final String ZERO_STRING = "0000000000";
	public static final String FLAG_STRING = "XXXXXXXX";
	public static final SimpleDateFormat sdf_iso = new SimpleDateFormat("yyyyMMdd");	//SPR2151

	//SPR1018-deleted declaration

	//Pending constant
	public static final String PENDING = "P";
	protected NbaTableAccessor ntsAccess = null;

	/**
	 * NbaCyberRequests constructor
	 */
	public NbaCyberRequests() {
		ntsAccess = new NbaTableAccessor();
		tblMap = new HashMap();
	}
	/**
	 * Add the party id to the party hash map
	 * 
	 * @param partyId the current id from party
	 * @param roleCode The current role code 
	 * @param dxeId The current dxe tag
	 */
	protected void addPartyId(String partyId, String roleCode, String dxeId) {
		String keyStr = partyId + ":" + roleCode;

		partyMap.put(keyStr, dxeId);
	}
	/**
	 * Create the 109 RateClass change request to send to Cyberlife
	 * @param TXLife input 109 xml
	 * @return java.lang.String containing DXE to send to the host
	 * @throws NbaBaseException
	 */

	protected String create109RateClassChangeRequest(TXLife txLife) throws com.csc.fsg.nba.exception.NbaBaseException {
		String result = new String();

		try {
			NbaTXLife nbaTXLife = new NbaTXLife(); //SPR1186
			nbaTXLife.setTXLife(txLife); //SPR1186
			int relTbl = nbaTXLife.isLife() ? CYBTBL_RELATION_ROLE : CYBTBL_RELATION_ROLE_F; //SPR1186
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();

			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();
			Party party = olife.getPartyAt(0);
			PersonOrOrganization personOrg = party.getPersonOrOrganization();
			Person person = personOrg.getPerson();
			OLifEExtension olifeExt = person.getOLifEExtensionAt(0);
			PersonExtension persExt = olifeExt.getPersonExtension();
			String smokeStatus = persExt.getRateClass(); //SPR1778 
			 
			result =
				CHANGE_RATECLASS_TYPE
					+ COMP_CODE
					+ "="
					+ policy.getCarrierCode()
					+ ";"
					+ POL_NUM
					+ "="
					+ policy.getPolNumber()
					+ ";"
					+ POLICY_STATUS
					+ "="
					+ PENDING
					+ ";";

			int relCount = olife.getRelationCount();
			// SPR3290 code deleted
			int flag2 = 0;
			Relation relation = new Relation();
			while (flag2 < relCount) {
				relation = olife.getRelationAt(flag2);
				String relRoleCode = getCyberValue(relation.getRelationRoleCode(), CYBTRANS_ROLES, relTbl, compCode, DEFAULT_COVID);	//SPR1186
				result = result.concat(PERS_SEQ_ID + "=" + relRoleCode + relation.getRelatedRefID() + ";");
						result = result.concat(createDataExchange(smokeStatus, SMOKE_STATUS, // DXE tag //SPR1778 
						CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1)); //SPR1778 
				flag2++;
			}

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_RATECLASS_REQUEST, e);
		}

		return result;
	}
	/**
     * Create the Cyberlife DXE transaction for requirement transactions 5200 and 5400
     * 
     * @param txlife TXLife input 109 xml object
     * @return String The DXE stream to be sent to the CyberLife host
     * @exception NbaBaseException.
     */
    public String create109Request(TXLife txLife) throws NbaBaseException {
    
    	String hostRequest = new String();
    
    	try {
    		UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
    		TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
    		OLifE olife = txlifeRequest.getOLifE();
    		int relationTot = olife.getRelationCount();
    		Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
    		Policy policy = holding.getPolicy();
    		Relation relation;
    		int i = 0, j = 0, rTot;
    		DataTransmittalSubType dataTransmittalSubType = txlifeRequest.getDataTransmittalSubTypeAt(0);
    		if ((txlifeRequest.getTransType() != TRANSACTION_NB_CHANGE)
    			|| (dataTransmittalSubType.getObjectType() != TRANSMITTAL_SUBTYPE_OBJECT_REQUIREMENT_INFO)
    			|| (txlifeRequest.getTransMode() != TRANSMODE_UPDATE))
    			throw new NbaBaseException("Transaction is incorrect for requirement 109");
    
    		// Check that policy status is pending 	
    		if (policy.getPolicyStatus() != OLI_POLSTAT_PENDING)
    			throw new NbaBaseException("Transaction is pending status");
    
    		// get company code
    		compCode = policy.getCarrierCode();
    
    		rTot = policy.getRequirementInfoCount();
    
    		// for each of our requirementInfo objects
    		for (i = 0; i < rTot; i++) {
    			RequirementInfo requirementInfo = policy.getRequirementInfoAt(i);
    			OLifEExtension oLifEExtension = requirementInfo.getOLifEExtensionAt(0);
    			RequirementInfoExtension requirementInfoExtension = oLifEExtension.getRequirementInfoExtension();
    
    			long reqStat = requirementInfo.getReqStatus();
    			//Begin NBA130
    			String busFunc = null;
    			try {
    			    busFunc = NbaUtils.getBusinessProcessId(userRequest.getUserAuthRequest().getUserLoginNameAndUserPswdOrUserSessionKey().getUserLoginNameAndUserPswd().getUserLoginName());
    			} catch (Exception e) {
    			    busFunc = "";
    			}
    			//End NBA130
    				    
    			switch ((int) requirementInfo.getReqStatus()) {
    				case REQSTAT_ADD :
    				case REQSTAT_ORDER :
    					if (!(dataTransmittalSubType.getTranContentCode() == TRANSMITTAL_SUBTYPE_CONTENT_CODE_INSERT || //NBA130
    						    (busFunc.equalsIgnoreCase(NbaConstants.PROC_POST_REQUIREMENT) && //NBA130
    						     dataTransmittalSubType.getTranContentCode() == TRANSMITTAL_SUBTYPE_CONTENT_CODE_UPDATE ))) //NBA130
    						throw new NbaBaseException("TranContentCode is not insert for add/order");
    					// create transaction data
    					hostRequest =
    						TRANS_REQ_ADD_ORDER
    							+ COMP_CODE
    							+ "="
    							+ compCode
    							+ ";"
    							+ POL_NUM
    							+ "="
    							+ policy.getPolNumber()
    							+ ";"
    							+ POLICY_STATUS
    							+ "="
    							+ PENDING
    							+ ";";
    
    					// create requirement data				
    					hostRequest =
    						hostRequest
    							+ createDataExchange(
    								formatCyberLong(requirementInfo.getReqCode()),
    								REQ_CODE,
    								NbaTableConstants.NBA_REQUIREMENTS,
    								CYBTBL_REQUIREMENTS,
    								CT_CHAR,
    								3);
    					hostRequest =
    						hostRequest
    							+ createDataExchange(
									formatCyberDate(requirementInfoExtension.getCreatedDate()), //NBA130
    								REQ_ORDER_DATE,
    								CYBTRANS_NONE,
    								CYBTBL_NONE,
    								CT_CHAR,
    								9);
    					// Get person id here
    					String appliesToPartyID = requirementInfo.getAppliesToPartyID();
    					String personSeq = new String();
    					for (j = 0; j < relationTot; j++) {
    						relation = olife.getRelationAt(j);
    						if (appliesToPartyID.compareTo(relation.getRelatedObjectID()) == 0) {
    							// Begin NBA006
    							personSeq =
    								getCyberValue(
    									formatCyberLong(relation.getRelationRoleCode()),
    									CYBTRANS_ROLES,
    									CYBTBL_RELATION_ROLE_USER_SPEC,
    									compCode,
    									DEFAULT_COVID,
    									formatCyberLong(policy.getProductType()));
    
    							// End NBA006
    							String relatedRefID = relation.getRelatedRefID();
    							if (relatedRefID.length() == 1)
    								personSeq = personSeq + "0" + relatedRefID;
    							else
    								personSeq = personSeq + relatedRefID;
    							break;
    
    						} // end if
    					} // end for
    					if (personSeq.compareTo("") == 0)
    						throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_REQ_INFO);
    					else
    						hostRequest = hostRequest + createDataExchange(personSeq, REQ_PERS_SEQ_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
    
    					hostRequest =
    						hostRequest
    							+ createDataExchange(
    								formatCyberLong(requirementInfo.getReqStatus()),
    								REQ_STATUS,
    								NbaTableConstants.OLI_LU_REQSTAT,
    								CYBTBL_UCT,
    								CT_CHAR,
    								1);
    					hostRequest =
    						hostRequest
    							+ createDataExchange(requirementInfo.getUserCode(), REQ_DEPT_DESK, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8);  //NBA093
    					hostRequest =
    						hostRequest
    							+ createDataExchange(
        								formatCyberLong(requirementInfo.getRestrictIssueCode()),  //NBA093
	    								REQ_RESTRICTION,
	    								NbaTableConstants.OLI_LU_REQRESTRICTION,
	    								CYBTBL_UCT,
	    								CT_CHAR,
	    								1); //SPR3171
    					break;
    				case REQSTAT_WAIVED :
    				case REQSTAT_RECEIVED :
    					if (dataTransmittalSubType.getTranContentCode() != TRANSMITTAL_SUBTYPE_CONTENT_CODE_UPDATE)
    						throw new NbaBaseException("TranContentCode is not update for receipt/waive");
    					// create transaction data
    					hostRequest =
    						TRANS_REQ_RECEIPT_WAIVE
    							+ COMP_CODE
    							+ "="
    							+ compCode
    							+ ";"
    							+ POL_NUM
    							+ "="
    							+ policy.getPolNumber()
    							+ ";"
    							+ POLICY_STATUS
    							+ "="
    							+ PENDING
    							+ ";";
    
    					// create requirement data				
    					hostRequest =
    						hostRequest
    							+ createDataExchange(
									formatCyberDate(requirementInfoExtension.getCreatedDate()), //NBA130
    								REQ_RECEIPT_WAIVE_REQUEST_DATE,
    								CYBTRANS_NONE,
    								CYBTBL_NONE,
    								CT_CHAR,
    								9);
    					hostRequest =
    						hostRequest
    							+ createDataExchange(
    								requirementInfo.getHORequirementRefID(),
    								REQ_RECEIPT_WAIVE_REQUEST_TIME,
    								CYBTRANS_NONE,
    								CYBTBL_NONE,
    								CT_CHAR,
    								9);
    					hostRequest =
    						hostRequest
    							+ createDataExchange(
    								requirementInfo.getSequence(),  //NBA093
    								REQ_RECEIPT_WAIVE_OCCURRENCE,
    								CYBTRANS_NONE,
    								CYBTBL_NONE,
    								CT_CHAR,
    								1);
    					hostRequest =
    						hostRequest
    							+ createDataExchange(
    								formatCyberLong(reqStat),
    								REQ_RECEIPT_WAIVE_ACTION,
    								NbaTableConstants.OLI_LU_REQSTAT,
    								CYBTBL_UCT,
    								CT_CHAR,
    								1);
    					break;
    				default :
    					throw new NbaBaseException("Requirement action is not receipt/waive or add/order");
    			} //end switch
    
    		} //end for
    	} catch (Exception e) {
    		throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
    	}
    
    	return hostRequest;
    
    }
	/**
	 * create the rating transactions to send to the Cyberlife Host
	 * 
	 * @param txLife input 151 xml input object
	 * @return The DXE stream to be sent to the CyberLife host
	 * @exception NbaBaseException.
	 */
	public String create151Request(TXLife txLife) throws com.csc.fsg.nba.exception.NbaBaseException {

		String hostRequest = new String();

		try {
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();
			//NBA093 deleted 2 lines
			SubstandardRating substandardRating = null; //NBA093
			SubstandardRatingExtension substandardRatingExt = null; //NBA093
			DataTransmittalSubType dataTransmittalSubType = txlifeRequest.getDataTransmittalSubTypeAt(0);
			Relation relation;
			String personSeq = new String();
			String relatedRefID = new String();
			String coverageKey = new String();
			LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealth = null; //NBA093
			Life life = null;
			Coverage coverage = null;

			// check that the incoming transaction is the right type

			if ((dataTransmittalSubType.getTranContentCode() == TRANSMITTAL_SUBTYPE_CONTENT_CODE_ADD)
				|| (dataTransmittalSubType.getTranContentCode() == TRANSMITTAL_SUBTYPE_CONTENT_CODE_REMOVE)) {} else
				throw new NbaBaseException("Transaction is incorrect for transaction 151");

			if (txlifeRequest.getTransMode() != TRANSMODE_UPDATE)
				throw new NbaBaseException("Transaction is incorrect for transaction 151");

			if ((dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_BENEFIT)
				//NBA093 deleted line
				|| (dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_COVERAGE)) {} else
				throw new NbaBaseException("Transaction is incorrect for transaction 151");

			// Check that policy status is pending

			if (policy.getPolicyStatus() != OLI_POLSTAT_PENDING)
				throw new NbaBaseException("Transaction is not pending");

			// get company code
			compCode = policy.getCarrierCode();

			// for Life objects

			if (policy.hasLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty()) { //NBA093
				lifeOrAnnuityOrDisabilityHealth = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); //NBA093
				if (lifeOrAnnuityOrDisabilityHealth.isLife())
					life = lifeOrAnnuityOrDisabilityHealth.getLife();
				else
					throw new NbaBaseException("151 Transaction does not have Life object");
			} else
				throw new NbaBaseException("151 Transaction does not have Life or Annuity object");

			//int coverageTot = life.getCoverageCount();
			coverage = life.getCoverageAt(0); //NBA093

			switch ((int) dataTransmittalSubType.getTranContentCode()) {
				// Add a rating to a coverage, life or benefit
				case TRANSMITTAL_SUBTYPE_CONTENT_CODE_ADD :

					switch ((int) dataTransmittalSubType.getObjectType()) {

						//NBA093 deleted line
						case TRANSMITTAL_SUBTYPE_OBJECT_COVERAGE :
							substandardRating = coverage.getLifeParticipantAt(0).getSubstandardRatingAt(0); //NBA093
							// this is the 5100 add transaction
							hostRequest =
								TRANS_ADD_RATING
									+ COMP_CODE
									+ "="
									+ compCode
									+ ";"
									+ POL_NUM
									+ "="
									+ policy.getPolNumber()
									+ ";"
									+ POLICY_STATUS
									+ "="
									+ PENDING
									+ ";";
							String ratingType = getRatingType(substandardRating, false); //NBA093  //SPR1549
								
							//NBA093 deleted 24 lines
							relation = olife.getRelationAt(0);
							personSeq =
								getCyberValue(
									formatCyberLong(relation.getRelationRoleCode()),
									CYBTRANS_ROLES,
									CYBTBL_RELATION_ROLE,
									compCode,
									DEFAULT_COVID);
							relatedRefID = relation.getRelatedRefID();
							hostRequest =
								hostRequest
									+ createDataExchange(personSeq.concat(relatedRefID), POLICY_PERS_SEQ, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 4);
							hostRequest =
								hostRequest
									+ createDataExchange(
										life.getCoverageAt(0).getCoverageKey(),
										SUB_STAND_COV_ID,
										CYBTRANS_NONE,
										CYBTBL_NONE,
										CT_CHAR,
										3);

							//NBA093 deleted 2 line
							char ratingChar = ratingType.charAt(0);
							substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating); //NBA093

							switch (ratingChar) {
								case SUB_STAND_TYPE_PERM_TABLE :
									// permanent table
										hostRequest = hostRequest + createDataExchange(substandardRating.getPermTableRating(), //NBA093
	SUB_STAND_TABLE_RATE, NbaTableConstants.OLI_LU_RATINGS, CYBTBL_UCT, CT_CHAR, 2);
									hostRequest = hostRequest + createDataExchange(formatCyberDate(coverage.getEffDate()),//SPR2159
										//NBA093
	SUB_STAND_START_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
									break;
								case SUB_STAND_TYPE_PERM_FLAT :
									// permanent flat extra
									hostRequest = hostRequest + createDataExchange(formatCyberDouble(substandardRatingExt.getPermFlatExtraAmt()),
										//NBA093
	SUB_STAND_FLAT_EXTRA, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
									hostRequest = hostRequest + createDataExchange(formatCyberDate(coverage.getEffDate()),//SPR2159
										//NBA093
	SUB_STAND_START_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);

									break;
								case SUB_STAND_TYPE_TEMP_TABLE :
									// temporary table
									hostRequest = hostRequest + createDataExchange(substandardRating.getTempTableRating(),
										//NBA093
	SUB_STAND_TABLE_RATE, NbaTableConstants.OLI_LU_RATINGS, CYBTBL_UCT, CT_CHAR, 2);
									hostRequest = hostRequest + createDataExchange(formatCyberDate(coverage.getEffDate()),//SPR2159
										//NBA093
	SUB_STAND_START_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
									if (substandardRating.hasTempTableRatingEndDate()) { //NBA093
										hostRequest =
											hostRequest + createDataExchange(formatCyberDate(substandardRating.getTempTableRatingEndDate()),
											//NBA093
	SUB_STAND_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
									} else if (substandardRatingExt.hasDuration()) { //NBA093
										hostRequest = hostRequest + createDataExchange(substandardRatingExt.getDuration(),
											//NBA093
	SUB_STAND_DURATION, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
									}
									break;
								case SUB_STAND_TYPE_TEMP_FLAT :
									// temporary flat extra
									hostRequest = hostRequest + createDataExchange(formatCyberDouble(substandardRating.getTempFlatExtraAmt()),
										//NBA093
	SUB_STAND_FLAT_EXTRA, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
										hostRequest = hostRequest + createDataExchange(formatCyberDate(coverage.getEffDate()), //NBA093//SPR2159
	SUB_STAND_START_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
									if (substandardRating.hasTempFlatEndDate()) { //NBA093
											hostRequest =
												hostRequest + createDataExchange(formatCyberDate(substandardRating.getTempFlatEndDate()), //NBA093
	SUB_STAND_END_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
									} else if (substandardRatingExt.hasDuration()) { //NBA093
											hostRequest = hostRequest + createDataExchange(substandardRatingExt.getDuration(), //NBA093
	SUB_STAND_DURATION, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
									}
									break;
								default :
									throw new NbaBaseException("Invalid rating type selected");
							} //end switch

							if (substandardRatingExt.hasExtraPremPerUnit()) { //NBA093
								hostRequest = hostRequest + createDataExchange(Double.toString(substandardRatingExt.getExtraPremPerUnit()),
									//NBA093
	SUB_STAND_EXTRA_UNIT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
							}
								hostRequest = hostRequest + createDataExchange(substandardRating.getRatingReason(), //NBA093
	SUB_STAND_REASON, NbaTableConstants.NBA_RATING_REASON, CYBTBL_UCT, CT_CHAR, 1);
							hostRequest = hostRequest + createDataExchange(substandardRating.getRatingCommissionRule(),
								//NBA093
		SUB_STAND_COMM, NbaTableConstants.OLIEXT_LU_COMMISCODE, //SPR1045
		CYBTBL_UCT, //SPR1045
	CT_CHAR, 1);
							hostRequest = hostRequest + createDataExchange(substandardRatingExt.getRatingStatus(),
								//NBA093
	SUB_STAND_RATE_STATUS, NbaTableConstants.NBA_ACTIVE_SEGMENT_STATUS, CYBTBL_UCT, CT_CHAR, 1);
							hostRequest = hostRequest + createDataExchange(ratingType, SUB_STAND_RATE_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);
							break;

						case TRANSMITTAL_SUBTYPE_OBJECT_BENEFIT :
							// this is the 2000 add ratings for benefits
							//NBA093 deleted line
							CovOption covOption = coverage.getCovOptionAt(0);
							substandardRating = covOption.getSubstandardRatingAt(0); //NBA093
							substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating); //NBA093

							hostRequest = CHANGE_RATECLASS_TYPE + COMP_CODE + "=" + compCode + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";
							hostRequest =
								hostRequest
									+ createDataExchange(covOption.getCovOptionKey(), COV_OPT_BENE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);
							hostRequest =
								hostRequest
									+ createDataExchange(
										covOption.getLifeCovOptTypeCode(),
										COV_OPT_TYPE,
										NbaTableConstants.OLI_LU_OPTTYPE,
										CYBTBL_UCT,
										CT_CHAR,
										2);
							hostRequest =
								hostRequest
									+ createDataExchange(substandardRatingExt.getPermPercentageLoading(), COV_OPT_PERC, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);//NBA093 SPR1738

							break;
					} //end switch
					break;

					// Remove a benefit from a coverage, life or benefit
				case TRANSMITTAL_SUBTYPE_CONTENT_CODE_REMOVE :
					substandardRating = coverage.getLifeParticipantAt(0).getSubstandardRatingAt(0); //NBA093
					substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating); //NBA093
					String ratingType = getRatingType(substandardRating, false); //NBA093 SPR2089 SPR1549
				
					String ratingSubType = ""; //NBA093
					SubstandardRatingExtension substandardExtension = NbaUtils.getFirstSubstandardExtension(substandardRating); //NBA093
					if (substandardExtension != null) { //NBA093
						ratingSubType = substandardExtension.getExtraPremSubtype(); //NBA093
					} //NBA093

					// Get addressability to coverage data				

					switch ((int) dataTransmittalSubType.getObjectType()) {

						case TRANSMITTAL_SUBTYPE_OBJECT_COVERAGE :

							// this is the 3000 remove coverage transaction
							coverage = life.getCoverageAt(0);
							coverageKey = coverage.getCoverageKey();
							hostRequest =
								TRANS_DELETE_RATING
									+ COMP_CODE
									+ "="
									+ compCode
									+ ";"
									+ POL_NUM
									+ "="
									+ policy.getPolNumber()
									+ ";"
									+ TRANS_RATING_SEGMENT_ID
									+ "="
									+ RATING
									+ ";";
							hostRequest = hostRequest + createDataExchange(coverageKey, TRANS_COVERAGE_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);
							hostRequest = hostRequest + createDataExchange(ratingType, TRANS_RATING_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1);	//SPR2089
							hostRequest =
								hostRequest + createDataExchange(ratingSubType, TRANS_RATING_SUBTYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);

							relation = olife.getRelationAt(0);
							//begin SPR2089
							//FSPTRELI has 2 char person code, 1 char seq
							String key = relation.getRelationKey();
							StringBuffer newKey = new StringBuffer();
							if (key.length() > 3) {
								newKey.append(key.substring(0, 2));
								newKey.append(key.substring(3, 4));
							} else {
								newKey.append(key);
							}
							hostRequest = hostRequest + createDataExchange(newKey.toString(), TRANS_PARTY_ID, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 4);
							//end SPR2089							
							break;

						case TRANSMITTAL_SUBTYPE_OBJECT_BENEFIT :

							// this is the 2000 remove substandard rating from benefits
							coverage = life.getCoverageAt(0);
							//begin-NBA093
							CovOption covOption = coverage.getCovOptionAt(0);
							substandardRating = covOption.getSubstandardRatingAt(0);
							substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating); //NBA093
							// SPR2089 code deleted
							//end-NBA093
							hostRequest = CHANGE_RATECLASS_TYPE + COMP_CODE + "=" + compCode + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";
							hostRequest =
								hostRequest
									+ createDataExchange(covOption.getCovOptionKey(), COV_OPT_BENE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);
							hostRequest =
								hostRequest
									+ createDataExchange(
										covOption.getLifeCovOptTypeCode(),
										TRANS_BENEFIT,
										NbaTableConstants.OLI_LU_OPTTYPE,
										CYBTBL_UCT,
										CT_CHAR,
										2);
							hostRequest = hostRequest + createDataExchange(NO_BENEFIT_RATING, COV_OPT_PERC, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 3);

							break;

							//NBA093 deleted 37 lines
					} //end switch

					break;
			} //end switch				

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_SUBSTANDARD_RATING_REQUEST, e);
		}

		return hostRequest;
	}
	/**
	 * Create the 203 request to send to Cyberlife
	 * 
	 * @param TXLife input 203 xml object
	 * @return java.lang.String containing DXE to send to the host
	 * @throws NbaBaseException
	 */
	protected String create203Request(TXLife txLife) throws NbaBaseException {
		String result = new String();

		try {

			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();

			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();

			result =
				HOLDING_TYPE
					+ COMP_CODE
					+ "="
					+ policy.getCarrierCode()
					+ ";"
					+ POL_NUM
					+ "="
					+ policy.getPolNumber()
					+ ";"
					+ HOLDING_SEGMENTS
					+ HOLDING_RESOLVE;

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}

		return result;
	}
	/**
	 * Create the deny transaction (5000) to send to the CyberLife host.
	 * 
	 * @param txLife the holding inquiry
	 * @return java.lang.String the host request
	 * @throws NbaBaseException
	 */
	public String create503Request(TXLife txLife) throws com.csc.fsg.nba.exception.NbaBaseException {
		String hostRequest = new String();
		try {
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();

			// check that the incoming transaction is the right type
			DataTransmittalSubType dataTransmittalSubType = txlifeRequest.getDataTransmittalSubTypeAt(0);

			if ((dataTransmittalSubType.getTranContentCode() != TRANSMITTAL_SUBTYPE_CONTENT_CODE_UPDATE)
				|| (txlifeRequest.getTransMode() != TRANSMODE_UPDATE))
				throw new NbaBaseException("Transaction is incorrect for deny transaction 503");

			if ((dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_DENY_BENEFIT)
				|| (dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_DENY_PERSON)
				|| (dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_DENY_COVERAGE)
				|| (dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_DENY_RIDER)) { // NBA006
			} else
				throw new NbaBaseException("Transaction is incorrect for deny transaction 503");

			// Check that policy status is pending
			if (policy.getPolicyStatus() != OLI_POLSTAT_PENDING) {
				throw new NbaBaseException("Transaction is not pending");
				//logMessage("Error Policy is not a pending transaction");
			} //end if

			// get company code
			compCode = policy.getCarrierCode();

			// create transaction data
			hostRequest = DENY_TRANS + COMP_CODE + "=" + compCode + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";

			// for Life objects
			// NBA006 code deleted
			// begin NBA006
			Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife(); //NBA093
			if (life != null) {
				// end NBA006
				int coverageTot = life.getCoverageCount();
				int j;
				for (j = 0; j < coverageTot; j++) {
					Coverage coverage = life.getCoverageAt(j);
					switch ((int) dataTransmittalSubType.getObjectType()) {
						case TRANSMITTAL_SUBTYPE_OBJECT_DENY_BENEFIT :
							int benefitTot = coverage.getCovOptionCount();
							int b;
							for (b = 0; b < benefitTot; b++) {
								CovOption covOption = coverage.getCovOptionAt(b);
							  if (covOption.getDataRep()!= null) {//NBA093
								if (FULL.compareTo(covOption.getDataRep()) == 0) {
									hostRequest =
										hostRequest
											+ createDataExchange(
												coverage.getCoverageKey(),
												TRANS_COVERAGE_PHASE,
												CYBTRANS_NONE,
												CYBTBL_NONE,
												CT_CHAR,
												2);
									hostRequest =
										hostRequest
											+ createDataExchange(
												covOption.getLifeCovOptTypeCode(),
												FSPBENEF,
												NbaTableConstants.OLI_LU_OPTTYPE,
												CYBTBL_UCT,
												CT_CHAR,
												2);
								} // end if
							  } // end if//NBA093
							} // end for
							break;
						case TRANSMITTAL_SUBTYPE_OBJECT_DENY_PERSON :
							int lifeTot = coverage.getLifeParticipantCount();
							int p;
							for (p = 0; p < lifeTot; p++) {
								LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(p);
							  if (lifeParticipant.getDataRep() != null) {//NBA093
								if (FULL.equals(lifeParticipant.getDataRep())) {
									// Get person id here
									int relationTot = olife.getRelationCount();
									String partyID = new String();
									partyID = lifeParticipant.getPartyID();
									String personSeq = new String();
									int r;
									for (r = 0; r < relationTot; r++) {
										Relation relation = olife.getRelationAt(r);
										if (partyID.compareTo(relation.getRelatedObjectID()) == 0) {
											personSeq =
												getCyberValue(
													formatCyberLong(relation.getRelationRoleCode()),
													CYBTRANS_ROLES,
													CYBTBL_RELATION_ROLE,
													compCode,
													DEFAULT_COVID);
											String relatedRefID = relation.getRelatedRefID();
											if (relatedRefID.length() == 1)
												personSeq = personSeq + "0" + relatedRefID;
											else
												personSeq = personSeq + relatedRefID;
										} // end if
									} // end for
									if (personSeq.compareTo("") == 0)
										throw new NbaBaseException("Person seq invalid on 503 transaction");
									else
										hostRequest =
											hostRequest + createDataExchange(personSeq, TRANS_PERSON, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 12);
								} // end if
							  } // end if//NBA093
							} // end for
							break;
						case TRANSMITTAL_SUBTYPE_OBJECT_DENY_COVERAGE :
							if(coverage.getDataRep() != null){//NBA093
								if (FULL.compareTo(coverage.getDataRep()) == 0)
									hostRequest =
										hostRequest
											+ createDataExchange(coverage.getCoverageKey(), TRANS_COVERAGE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);
							}//NBA093
							break;
						default :
							throw new NbaBaseException("Deny action is not defined");
					} //end switch
				} //end for
			}
			// begin NBA006
			// for Annuity objects
			Annuity annuity = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity(); //NBA093
			if (annuity != null) {
				List riders = annuity.getRider();
				for (int j = 0; j < riders.size(); j++) {
					Rider rider = (Rider) riders.get(j);
					switch ((int) dataTransmittalSubType.getObjectType()) {
						case TRANSMITTAL_SUBTYPE_OBJECT_DENY_BENEFIT :
							// SPR3290 code deleted
							List covOptions = rider.getCovOption(); //NBA093
							for (int b = 0; b < covOptions.size(); b++) {
								CovOption covOption = (CovOption) covOptions.get(b);
							  if (covOption.getDataRep() != null) {//NBA093
								if (FULL.compareTo(covOption.getDataRep()) == 0) {
									hostRequest =
										hostRequest
											+ createDataExchange(rider.getRiderKey(), TRANS_COVERAGE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);
									hostRequest =
										hostRequest
											+ createDataExchange(
												covOption.getLifeCovOptTypeCode(),
												FSPBENEF,
												NbaTableConstants.OLI_LU_OPTTYPE,
												CYBTBL_UCT,
												CT_CHAR,
												2);
								} // end if
							  } // end if//NBA093
							} // end for
							break;
						case TRANSMITTAL_SUBTYPE_OBJECT_DENY_RIDER :
						if (rider.getDataRep() != null){//NBA093
							if (FULL.compareTo(rider.getDataRep()) == 0)
								hostRequest =
									hostRequest
										+ createDataExchange(rider.getRiderKey(), TRANS_COVERAGE_PHASE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2);
						}//NBA093
							break;
						default :
							throw new NbaBaseException("Deny action is not defined");
					} //end switch
				} //end for
			}
			// end NBA006
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}
		return hostRequest;
	}
	/**
	 * Create the 504 Endorsement change request to send to Cyberlife
	 * 
	 * @param TXLife input xml  
	 * @return java.lang.String containing DXE to send to the host
	 * @throws NbaBaseException
	 */
	public String create504EndorsementChangeRequest(TXLife txLife) throws com.csc.fsg.nba.exception.NbaBaseException {
		String result = new String();
		try {
			NbaTXLife nbaTXLife = new NbaTXLife(); //SPR1186
			nbaTXLife.setTXLife(txLife); //SPR1186
			int relTbl = nbaTXLife.isLife() ? CYBTBL_RELATION_ROLE : CYBTBL_RELATION_ROLE_F; //SPR1186
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();
			//NBA093 delete 2 lines
			Endorsement endorsement = policy.getEndorsementAt(0); //NBA093
			result = CHANGE_ENDORSEMENT_TYPE + COMP_CODE + "=" + policy.getCarrierCode() + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";
			//check to see if add or delete
			if (txlifeRequest.getDataTransmittalSubTypeAt(0).getTranContentCode() == 3) {
				result = result.concat(createDataExchange("1", ENDORS_TRANS_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
			} else {
				result = result.concat(createDataExchange("0", ENDORS_TRANS_IND, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1));
			}
			//if the endorsement applies to the contract as a whole, do not set ENDORSE_PERS_ID or TRANS_COVERAGE_PHASE
			if (endorsement.getRelatedObjectType() != OLI_POLICY) { //NBA093
				Relation relation = new Relation();
				relation = olife.getRelationAt(0);
				String relRoleCode = getCyberValue(relation.getRelationRoleCode(), CYBTRANS_ROLES, relTbl, compCode, DEFAULT_COVID);	//SPR1186
				result = result.concat(ENDORSE_PERS_ID + "=" + relRoleCode + relation.getRelatedRefID() + ";");
				result = result.concat(TRANS_PERSON + "=" + relRoleCode + relation.getRelatedRefID() + ";");
				//endorsement applies to a Party
				//begin NBA093
				if (endorsement.getAppliesToPartyID() != null && endorsement.getRelatedObjectType() == OLI_UNKNOWN) {
					result = result.concat(TRANS_COVERAGE_PHASE + "=0;");
				} else {
					LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
					// NBA006
					if (insurance != null) {
						if (endorsement.getRelatedObjectType() == OLI_LIFECOVERAGE) {//SPR2278
							//applies to coverage or coverage option
							Life life = insurance.getLife();
							if (life != null) {
								int count = life.getCoverageCount();
								for (int i = 0; i < count; i++) {
									Coverage coverage = life.getCoverageAt(i);
									if (coverage != null && coverage.getId().equals(endorsement.getRelatedObjectID())) {
										result = result.concat(TRANS_COVERAGE_PHASE + "=" + coverage.getCoverageKey() + ";"); // NBA006
									}
								}
							}
						}
						// begin NBA006
						else if (endorsement.getRelatedObjectType() == OLI_ANNUITY) {//SPR2278
							Annuity annuity = insurance.getAnnuity();
							if (annuity != null) {
								result = result.concat(TRANS_COVERAGE_PHASE + "=" + annuity.getAnnuityKey() + ";");
							}
						} else if (endorsement.getRelatedObjectType() == OLI_ANNRIDER) {//SPR2278
							Annuity annuity = insurance.getAnnuity();
							if (annuity != null) {
								int count = annuity.getRiderCount();
								for (int i = 0; i < count; i++) {
									Rider rider = annuity.getRiderAt(i);
									if (rider != null && rider.getId().equals(endorsement.getRelatedObjectID())) {
										result = result.concat(TRANS_COVERAGE_PHASE + "=" + rider.getRiderKey() + ";");
									}
								}
							}
						}
						// end NBA006
					}
				}
				//end NBA093
			}
			if (endorsement.getRelatedObjectType() == OLI_COVOPTION) { //NBA093
				//begin NBA093
				//set the coverage phase
				LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
				// NBA006
				if (insurance != null) {
					Life life = insurance.getLife();
					if (life != null) {
						int count = life.getCoverageCount();
						for (int i = 0; i < count; i++) {
							Coverage coverage = life.getCoverageAt(i);
							if (coverage != null) {
								int count2 = coverage.getCovOptionCount();
								for (int j = 0; j < count2; j++) {
									CovOption covOption = coverage.getCovOptionAt(j);
									if (covOption != null && covOption.getId().equals(endorsement.getRelatedObjectID())) {
										result = result.concat(TRANS_COVERAGE_PHASE + "=" + coverage.getCoverageKey() + ";");
									}
								}
							}
						}
					}
				}
				//end NBA093
				result =
					result.concat(
						createDataExchange(
							formatCyberLong(getCovOptionTypeCode(policy)),
							FSPBENEF,
							NbaTableConstants.OLI_LU_OPTTYPE,
							CYBTBL_UCT,
							CT_CHAR,
							3));
				// NBA006
				result = result.concat(FSPBUSE_IND + "=1;");
			} else {
				result = result.concat(FSPBUSE_IND + "=0;");
			}
			
			result = result.concat(createDataExchange(endorsement.getEndorsementCode(), ENDORSE_TYPE, NbaTableConstants.NBA_AMENDENDORSETYPE, CYBTBL_UCT, CT_CHAR, 4));//SPR2306
			result = result.concat(ENDORSE_DETAIL_IND + "=" + endorsement.getEndorsementInfo() + ";"); //NBA03
			result =
				result.concat(createDataExchange(formatCyberDate(endorsement.getEndDate()), ENDORSE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_ENDORSEMENT_REQUEST, e);
		}
		return result;
	}
	/**
	 * Create the approve transaction to send to Cyberlife host.
	 * 
	 * @param txLife input xml
	 * @return String The DXE stream to be sent to CyberLife host
	 * @exception com.csc.fsg.nba.exception.NbaBaseException.
	 */
	public String create505Request(TXLife txLife) throws com.csc.fsg.nba.exception.NbaBaseException {

		String hostRequest = new String();

		try {
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();

			// check that the incoming transaction is the right type

			DataTransmittalSubType dataTransmittalSubType = txlifeRequest.getDataTransmittalSubTypeAt(0);

			if ((dataTransmittalSubType.getTranContentCode() != TRANSMITTAL_SUBTYPE_CONTENT_CODE_UPDATE)
				|| (txlifeRequest.getTransMode() != TRANSMODE_UPDATE))
				throw new NbaBaseException("Transaction is incorrect for approve transaction 505");

			if ((dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_APPROVE_BENEFIT)
				|| (dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_APPROVE_CONTRACT)
				|| (dataTransmittalSubType.getObjectType() == TRANSMITTAL_SUBTYPE_OBJECT_APPROVE_COVERAGE)) {} else
				throw new NbaBaseException("Transaction is incorrect for approve transaction 505");

			// Check that policy status is pending 	
			if (policy.getPolicyStatus() != OLI_POLSTAT_PENDING) {
				throw new NbaBaseException("Error Policy is not a pending transaction");
			} //end if	

			// get company code
			compCode = policy.getCarrierCode();

			// for applicationInfo objects
			ApplicationInfo applicationInfo = policy.getApplicationInfo();

			// create transaction data
			hostRequest = APPROVE_TRANS + COMP_CODE + "=" + compCode + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";

			// Get addressability to underwriting disposition data				
			OLifEExtension oLifEExtension = applicationInfo.getOLifEExtensionAt(0);
			ApplicationInfoExtension applicationInfoExtension = oLifEExtension.getApplicationInfoExtension();

			// Translate Acord value for underwriting status to Cyberlife value
			hostRequest =
				hostRequest
					+ createDataExchange(
						applicationInfoExtension.getUnderwritingApproval(),
						APPROVE_UNDERWRITING_TYPE,
						NbaTableConstants.NBA_APPROVAL_TYPE,
						CYBTBL_UCT,
						CT_CHAR,
						4);

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}

		return hostRequest;
	}
	/**
	 * Create the 5600 (Final Disposition) DXE transaction to be sent to the CyberLife host.
	 * @param txLife input xml
	 * @return String The DXE stream to be sent to CyberLife Host
	 * @exception com.csc.fsg.nba.exception.NbaBaseException.
	 */
	public String create507Request(TXLife txLife) throws com.csc.fsg.nba.exception.NbaBaseException {

		String hostRequest = new String();

		try {
			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();
			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();

			// check that the incoming transaction is the right type
			DataTransmittalSubType dataTransmittalSubType = txlifeRequest.getDataTransmittalSubTypeAt(0);

			if ((dataTransmittalSubType.getObjectType() != TRANSMITTAL_SUBTYPE_OBJECT_FINAL_DISP)
				|| (dataTransmittalSubType.getTranContentCode() != TRANSMITTAL_SUBTYPE_CONTENT_CODE_UPDATE)
				|| ((txlifeRequest.getTransMode() != TC_MODE_UPDATE) && (txlifeRequest.getTransMode() != TC_MODE_REPLACE))) //NBA077
				throw new NbaBaseException("Transaction is incorrect for final disposition 507");

			// Check that policy status is pending 	
			if (policy.getPolicyStatus() != OLI_POLSTAT_PENDING) {
				throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_REQ_INFO);
				//logMessage("Error Policy is not a pending transaction");
			} //end if	

			// get company code
			compCode = policy.getCarrierCode();

			// for applicationInfo objects
			ApplicationInfo applicationInfo = policy.getApplicationInfo();

			// create transaction data
			hostRequest =
				FINAL_DISPOSITION_TRANS
					+ COMP_CODE
					+ "="
					+ compCode
					+ ";"
					+ POL_NUM
					+ "="
					+ policy.getPolNumber()
					+ ";"
					+ POLICY_STATUS
					+ "="
					+ PENDING
					+ ";";

			// Get addressability to underwriting disposition data				
			OLifEExtension oLifEExtension = applicationInfo.getOLifEExtensionAt(0);
			ApplicationInfoExtension applicationInfoExtension = oLifEExtension.getApplicationInfoExtension();
			long underwritingStat = applicationInfoExtension.getUnderwritingStatus();

			// Translate Acord value for underwriting status to Cyberlife value
			hostRequest =
				hostRequest
					+ createDataExchange(
						formatCyberLong(underwritingStat),
						FINAL_DISPOSITION_UNDERWRITING_STATUS,
						NbaTableConstants.NBA_FINAL_DISPOSITION,
						CYBTBL_UCT,
						CT_CHAR,
						1);

			if (txlifeRequest.getTransMode() != TC_MODE_REPLACE) { //NBA077
					hostRequest = hostRequest + createDataExchange(formatCyberDate(applicationInfo.getRequestedPolDate()), //NBA093
				FINAL_DISPOSITION_REQUESTED_ISSUE_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9);
			} //NBA077

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.BACKEND_ADAPTER_PARSE, e);
		}

		return hostRequest;
	}
	/**
	* Create the 508 CWA reverse refund request to send to Cyberlife
	* @param TXLife input xml  
	* @return java.lang.String containing DXE to send to the host
	* @throws NbaBaseException
	*/
	public String create508CWAReverseRefundRequest(TXLife txLife) throws com.csc.fsg.nba.exception.NbaBaseException {
		String result = new String();

		try {

			UserAuthRequestAndTXLifeRequest userRequest = txLife.getUserAuthRequestAndTXLifeRequest();

			TXLifeRequest txlifeRequest = userRequest.getTXLifeRequestAt(0);
			OLifE olife = txlifeRequest.getOLifE();
			Holding holding = NbaTXLife.getPrimaryHoldingFromOLifE(olife); //NBA044
			Policy policy = holding.getPolicy();

			//NBA009 begin
			//Go thru all FinancialActivity objects and pick the one whose DataRep value is FULL
			FinancialActivity finAct = null;
			// SPR3290 code deleted
			FinancialActivityExtension finExt = null;
			// NBA093 deleted line
			for (int i = 0; i < policy.getFinancialActivityCount(); i++) {
				finAct = policy.getFinancialActivityAt(i);
				if(finAct.getDataRep() != null){//NBA093
					if (finAct.getDataRep().toUpperCase().equals("FULL")) {
						finExt = NbaUtils.getFirstFinancialActivityExtension(finAct);//NBA093
						// NBA093 deleted line
						break;
					}
				}//NBA093
			}
			//NBA009 end

			result = CWA_REVERSE_REFUND + COMP_CODE + "=" + policy.getCarrierCode() + ";" + POL_NUM + "=" + policy.getPolNumber() + ";";

				result = result.concat(createDataExchange(formatCyberCurrency(finAct.getFinActivityGrossAmt()), //NBA093
	FIN_GROSS_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0));

			result =
				result.concat(createDataExchange(formatCyberDate(finAct.getFinActivityDate()), FIN_ACT_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));

				result = result.concat(createDataExchange(formatCyberLong(finAct.getFinActivityType()), //NBA093
	CWA_TYP_CODE, NbaTableConstants.OLIEXT_LU_CWATYPE, CYBTBL_UCT, CT_CHAR, 1));

				result = result.concat(createDataExchange(formatCyberDate(finAct.getFinActivityDate()), //NBA093
	CWA_ENT_DATE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));

			result =
				result.concat(
					createDataExchange(
						formatCyberLong(finAct.getFinActivityType()),
						CWA_REVERSE_TYPE,
						NbaTableConstants.OLI_LU_FINACTTYPE,
						CYBTBL_UCT,
						CT_CHAR,
						1));
			if (finAct.getFinActivityType() == 34 && finExt != null) {   //NBA093
				result = result.concat(CWA_ERR_CODE + "=" + (formatCyberBoolean(finExt.getErrCorrInd())) + ";");  //NBA093
			}
			result = result.concat(createDataExchange(finAct.getUserCode(), CWA_DPT_DESK, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 6));  //NBA093

			//NBA009 begin
			if (finAct.hasRollloverIntAmt() && finAct.getCostBasisAdjAmt() > 0) { //NBA093 //NBA223
					result = result.concat(createDataExchange(formatCyberCurrency(finAct.getRollloverIntAmt()), //NBA093 //NBA223
	COST_BASIS_ADJ_AMT, CYBTRANS_NONE, CYBTBL_NONE, CT_DOUBLE, 0));
			}
			//NBA009 end    

		} catch (Exception e) {
			throw new NbaBaseException(NbaBaseException.CYBER_ADAPT_CWA_REQUEST, e);
		}

		return result;
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(double inputVal, String cyberTag, String cyberTransTbl, int whichTbl, int dataType, int dataLen) {
		return createDataExchange(formatCyberDouble(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, "");
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @param param Additional data needed for some DB requests
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(
		double inputVal,
		String cyberTag,
		String cyberTransTbl,
		int whichTbl,
		int dataType,
		int dataLen,
		String param) {
		return createDataExchange(formatCyberDouble(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, param);
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(int inputVal, String cyberTag, String cyberTransTbl, int whichTbl, int dataType, int dataLen) {
		return createDataExchange(formatCyberInt(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, "");
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @param param Additional data needed for some DB requests
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(int inputVal, String cyberTag, String cyberTransTbl, int whichTbl, int dataType, int dataLen, String param) {
		return createDataExchange(formatCyberInt(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, param);
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(long inputVal, String cyberTag, String cyberTransTbl, int whichTbl, int dataType, int dataLen) {
		return createDataExchange(formatCyberLong(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, "");
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @param param Additional data needed for some DB requests
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(
		long inputVal,
		String cyberTag,
		String cyberTransTbl,
		int whichTbl,
		int dataType,
		int dataLen,
		String param) {
		return createDataExchange(formatCyberLong(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, param);
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(String inputVal, String cyberTag, String cyberTransTbl, int whichTbl, int dataType, int dataLen) {
		return createDataExchange(formatCyberString(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, ""); //SPR2751
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @param param Additional data needed for some DB requests
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(
		String inputVal,
		String cyberTag,
		String cyberTransTbl,
		int whichTbl,
		int dataType,
		int dataLen,
		String param) {
		String response;

		// initialize the response value
		response = "";
		if (whichTbl != CYBTBL_NONE) {
			response = getCyberValue(inputVal, cyberTransTbl, whichTbl, compCode, DEFAULT_COVID, param);
			if (response != null) {
				response = formatDataExchange(response, cyberTag, dataType, dataLen);
			} else {
				// for the moment, go ahead and build the output stream - might change
				response = formatDataExchange(response, cyberTag, dataType, dataLen);
			}

		} else {
			if (inputVal != null) {
				response = formatDataExchange(inputVal.trim(), cyberTag, dataType, dataLen);
			} else {
				// for the moment, go ahead and build the output stream - might change
				response = formatDataExchange("", cyberTag, dataType, dataLen);
			}
		}
		return response;
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(Date inputVal, String cyberTag, String cyberTransTbl, int whichTbl, int dataType, int dataLen) {
		return createDataExchange(formatCyberDate(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, "");
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @param param Additional data needed for some DB requests
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(
		Date inputVal,
		String cyberTag,
		String cyberTransTbl,
		int whichTbl,
		int dataType,
		int dataLen,
		String param) {
		return createDataExchange(formatCyberDate(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, param);
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(boolean inputVal, String cyberTag, String cyberTransTbl, int whichTbl, int dataType, int dataLen) {
		return createDataExchange(formatCyberBoolean(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, "");
	}
	/**
	 * Format a DXE tag/value pair from the input provided.  Translate values if requested.
	 * @param boolean Boolean value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param cyberTransTbl The translation table used to convert to the back end system value
	 * @param whichTbl The name of the table
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @param param Additional data needed for some DB requests
	 * @return the DXE tag along with the translated host value
	 */
	protected String createDataExchange(
		boolean inputVal,
		String cyberTag,
		String cyberTransTbl,
		int whichTbl,
		int dataType,
		int dataLen,
		String param) {
		return createDataExchange(formatCyberBoolean(inputVal), cyberTag, cyberTransTbl, whichTbl, dataType, dataLen, param);
	}
	/**
	 * Return the appropriate CyberLife dxe value for Boolean 
	 * @param bVar boolean input
	 * @return
	 */
	protected String formatCyberBoolean(boolean bVar) {
		if (bVar == true)
			return "1";
		else
			return "0";
	}
	/**
	 * Return the appropriate CyberLife dxe value for Currency
	 * @param param input double
	 * @return
	 */
	protected String formatCyberCurrency(double param) {
		return formatCyberDouble(param);
	}
	/**
	 * Return the appropriate CyberLife dxe value for Date
	 * @param oLifeDate
	 * @return
	 */
	protected String formatCyberDate(Date oLifeDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		if (oLifeDate != null) {
			String dateString = formatter.format(oLifeDate);
			return dateString;
		} else
			return "";
	}
	/**
	 * Return the appropriate CyberLife dxe value for double
	 * @param dVar input double variable
	 * @return
	 */
	protected String formatCyberDouble(double dVar) {
		// SPR2149 code deleted
		if (Double.isNaN(dVar)) { //SPR2149
			return "";
		} else { //SPR2149
			String response = NbaObjectPrinter.localeUnformattedDecimal(dVar); //SPR2149
			int len = response.length();
			if (len > 2) {
				if (response.charAt(len - 2) == '.' && response.charAt(len - 1) == '0')
					response = response.substring(0, len - 2);
			}
			return response;
		}
	}
	/**
	 * Return the appropriate CyberLife dxe value for int
	 * @param iVar input int value
	 * @return
	 */
	protected String formatCyberInt(int iVar) {
		if (iVar == -1)
			return "";
		return Integer.toString(iVar);
	}
	/**
	 * Return the appropriate CyberLife dxe value for long values
	 * @param lVar input long value
	 * @return
	 */
	protected String formatCyberLong(long lVar) {
		if (lVar == -1)
			return "";
		return Long.toString(lVar);
	}
	/**
	 * Return the appropriate CyberLife dxe value for zip codes
	 * @param zip input zipcode string
	 * @return
	 */
	protected String formatCyberZipCode(String zip) {
		if (zip.length() > 5)
			if (zip.substring(5, 6).compareTo("-") == 0)
				return (zip.substring(0, 5) + zip.substring(6));
		return zip;
	}
	/**
	 * Return the appropriate CyberLife dxe value for string values
	 * replacing any semi-colons (;) with a comma (,) and 
	 * replacing any equal signs (=) with the word " equals " 
	 * @param str input string
	 * @return a new String if substitutions were performed, otherwise return the original String
	 */
	// SPR2751 New Method
	protected String formatCyberString(String str) {
		if (str != null) {
			str = str.replace(';', ',');
			if (str.indexOf('=') != -1) {
				str = str.replaceAll("="," equals ");
			}
		}
		return str;
	}
	/**
	 * Create the DXE tag and host value pair to be sent to CyberLife host
	 * @param inputVal The value to be sent to the host
	 * @param cyberTag The DXE tag to be sent to the host
	 * @param dataType The type of data to be sent to the host
	 * @param dataLen The length of the data to be sent tot the host
	 * @return the DXE tag along with the translated host value
	 */
	protected String formatDataExchange(String inputVal, String cyberTag, int dataType, int dataLen) {
		String responseString;

		responseString = cyberTag + "=";

		if (inputVal != null) {
			if (dataType == CT_BIT) {
				if (dataLen > 1)
					responseString = responseString + FLAG_STRING.substring(0, dataLen - 1);
				responseString = responseString + inputVal;
				responseString = responseString + FLAG_STRING.substring(0, 8 - inputVal.length() - (dataLen - 1));
			} else {
				// GOBACK revisit what to do if dataLen is zero, is this okay?
				if (dataLen > 0 && inputVal.length() > dataLen)
					responseString = responseString + inputVal.substring(0, dataLen);
				else
					responseString = responseString + inputVal;
			}
		}

		responseString = responseString + ";";

		return responseString;
	}
	/**
	 * Format incoming date string as YYYYMMDD
	 * Not localized, this is specific to ISO internals.
	 * @param date java.lang.String
	 * @return java.lang.String
	 */
	protected String formatISODate(String date) {
		if (date.length() < 10) {
			return "";
		} else {
			return (date.substring(0, 4) + date.substring(5, 7) + date.substring(8, 10));
		}
	}
	/**
	 * Answer the coverage key, regardless if it's a life, annuity, coverage, or rider.
	 * @param policy input Policy xml object
	 * @return the coverage key
	 */
	// NBA006 New Method
	protected String getCoverageKey(Policy policy) {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); //NBA093
		if (insurance.getLife() != null) {
			return insurance.getLife().getCoverageAt(0).getCoverageKey();
		} else if (insurance.getAnnuity() != null) {
			// figure out if it's an annuity or rider object
			Annuity annuity = insurance.getAnnuity();
			if (annuity.getAnnuityKey() != null) {
				return annuity.getAnnuityKey();
			} else {
				// dig out the rider key
				return annuity.getRiderAt(0).getRiderKey();
			}
		}
		return ""; // should never get hit
	}
	/**
	 * Answer the CovOption type code, regardless if it's a life, annuity.
	 * @return the CovOption type code
	 */
	// NBA006 New Method
	protected long getCovOptionTypeCode(Policy policy) {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty insurance = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(); //NBA093
		if (insurance.getLife() != null) {
			return insurance.getLife().getCoverageAt(0).getCovOptionAt(0).getLifeCovOptTypeCode();
		} else if (insurance.getAnnuity() != null) {
			// figure out if it's an annuity or rider object//will no longer be an annuity-NBA093
			Annuity annuity = insurance.getAnnuity();
			// SPR1163 code deleted
			Rider rider = NbaUtils.getFirstAnnuityRider(annuity);
			if (rider != null && rider.getCovOptionCount() > 0) {	//SPR1163
				return rider.getCovOptionAt(0).getLifeCovOptTypeCode();	//SPR1163
			}
			// SPR1163 code deleted
			//NBA093 deleted 4 lines
			//NBA093-end
		}
		return 0; // should never get hit
	}
	/**
	 * Translate the int xml value to the appropriate dxe value via database translation
	 * 
	 * @param varToConvert input value
	 * @param tblName Translation table name
	 * @param tblType Table type
	 * @param compCd company code
	 * @param covKey coverage key
	 * @param param optional data for some table types
	 * @return CyberLife value
	 */
	protected String getCyberValue(int varToConvert, String tblName, int tblType, String compCd, String covKey) {
		// Make call with blank parameter
		return getCyberValue(formatCyberInt(varToConvert), tblName, tblType, compCd, covKey, "");
	}
	/**
	 * Translate the int xml value to the appropriate dxe value via database translation
	 * 
	 * @param varToConvert input value
	 * @param tblName Translation table name
	 * @param tblType Table type
	 * @param compCd company code
	 * @param covKey coverage key
	 * @param param optional data for some table types
	 * @return CyberLife value
	 */
	protected String getCyberValue(int varToConvert, String tblName, int tblType, String compCd, String covKey, String param) {
		// Make call with blank parameter
		return getCyberValue(formatCyberInt(varToConvert), tblName, tblType, compCd, covKey, param);
	}
	/**
	 * Translate the long xml value to the appropriate dxe value via database translation
	 * 
	 * @param varToConvert input value
	 * @param tblName Translation table name
	 * @param tblType Table type
	 * @param compCd company code
	 * @param covKey coverage key
	 * @return CyberLife value
	 */
	protected String getCyberValue(long varToConvert, String tblName, int tblType, String compCd, String covKey) {
		// Make call with blank parameter
		return getCyberValue(formatCyberLong(varToConvert), tblName, tblType, compCd, covKey, "");
	}
	/**
	 * Translate the long xml value to the appropriate dxe value via database translation
	 * 
	 * @param varToConvert input value
	 * @param tblName Translation table name
	 * @param tblType Table type
	 * @param compCd company code
	 * @param covKey coverage key
	 * @param param optional data for some table types
	 * @return CyberLife value
	 */
	protected String getCyberValue(long varToConvert, String tblName, int tblType, String compCd, String covKey, String param) {
		// Make call with blank parameter
		return getCyberValue(formatCyberLong(varToConvert), tblName, tblType, compCd, covKey, param);
	}
	/**
	 * Translate the String xml value to the appropriate dxe value via database translation
	 * 
	 * @param varToConvert input value
	 * @param tblName Translation table name
	 * @param tblType Table type
	 * @param compCd company code
	 * @param covKey coverage key
	 * @param param optional data for some table types
	 * @return CyberLife value
	 */
	protected String getCyberValue(String strToConvert, String tblName, int tblType, String compCd, String covKey) {
		// Make call with blank parameter
		return getCyberValue(strToConvert, tblName, tblType, compCd, covKey, "");
	}
	/**
	 * Translate the xml value to the appropriate dxe value via database translation
	 * 
	 * @param varToConvert input value
	 * @param tblName Translation table name
	 * @param tblType Table type
	 * @param compCd company code
	 * @param covKey coverage key
	 * @param param optional data for some table types
	 * @return CyberLife value
	 */
	protected String getCyberValue(String strToConvert, String tblName, int tblType, String compCd, String covKey, String param) {
		// SPR3290 code deleted
		NbaUctData[] uctData;
		NbaStatesData[] statesData;
		NbaPlansData[] plansData;
		NbaRolesData[] rolesData;
		NbaFundsData[] fundsData;
		NbaRequirementsData[] requirementsData;

		boolean inMap = false;
		String tStr;
		String pdCode;
		int iTot;
		String response;

		// Initialize response in case of error
		response = "";

		if (strToConvert == null)
			return response;

		// GOBACK Make sure this is a reasonable assumption
		if (strToConvert.compareTo("") == 0)
			return response;

		// First check to see if the table is already loaded
		if (tblType == CYBTBL_PARTIC_ROLE || tblType == CYBTBL_RELATION_ROLE)
			pdCode = "*";
		else if (tblType == CYBTBL_PARTIC_ROLE_F || tblType == CYBTBL_RELATION_ROLE_F)
			pdCode = "9";
		// Begin NBA006
		else if (tblType == CYBTBL_RELATION_ROLE_USER_SPEC || tblType == CYBTBL_PARTIC_ROLE_USER_SPEC) {
			pdCode = param;
			param = "";
			// End NBA006
		} else
			pdCode = "";

		if (tblMap.containsKey(tblName + pdCode + param)) {
			inMap = true;
		}
		// This switch determines how the data is fetched
		switch (tblType) {
			case CYBTBL_FUNDS :
				if (inMap) {
					fundsData = ((NbaFundsData[]) tblMap.get(tblName + param));
				} else {
					fundsData = ((NbaFundsData[]) getFundsTable(param));
				}
				if (fundsData != null) {
					int i;
					if (!inMap)
						tblMap.put(tblName + param, fundsData);
					iTot = fundsData.length;
					// now that we have the table data, find our field
					for (i = 0; i < iTot; i++) {
						if (fundsData[i].code() != null) {
							if ((fundsData[i].code().trim()).compareTo(strToConvert) == 0) {
								response = fundsData[i].getBesFundId();
								break;
							}
						}
					}
					// report not found error if necessary
					if (i == iTot) {
						if (getLogger().isWarnEnabled())
							getLogger().logWarn(
								"Could not CyberLife value in Funds table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Plan Id '"
									+ param
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					} else {
						// Temporary debug message...
						if (getLogger().isDebugEnabled())
							getLogger().logDebug(
								"CyberLife '"
									+ response
									+ "' value located in Funds table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Plan Id '"
									+ param
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					}
				} else {
					// report error condition
				}
				break;

			case CYBTBL_PARTIC_ROLE :
			case CYBTBL_PARTIC_ROLE_F :
			case CYBTBL_PARTIC_ROLE_USER_SPEC :
			case CYBTBL_RELATION_ROLE :
			case CYBTBL_RELATION_ROLE_F :
			case CYBTBL_RELATION_ROLE_USER_SPEC :
				if (inMap) {
					rolesData = ((NbaRolesData[]) tblMap.get(tblName + pdCode + param));
				} else {
					rolesData = ((NbaRolesData[]) getRolesTable(compCd, pdCode));
				}
				if (rolesData != null) {
					int i;
					if (!inMap)
						tblMap.put(tblName + pdCode + param, rolesData);
					iTot = rolesData.length;
					// now that we have the table data, find our field
					for (i = 0; i < iTot; i++) {
						if (rolesData[i].code() != null) {
							switch (tblType) {
								case CYBTBL_PARTIC_ROLE :
								case CYBTBL_PARTIC_ROLE_F :
									// NBA006
								case CYBTBL_PARTIC_ROLE_USER_SPEC :
									tStr = rolesData[i].getParticipantRoleValue();
									break;
								case CYBTBL_RELATION_ROLE :
								case CYBTBL_RELATION_ROLE_F :
									// NBA006
								case CYBTBL_RELATION_ROLE_USER_SPEC :
									tStr = rolesData[i].getRelationRoleValue();
									break;
								default :
									tStr = "";
							}
							if ((tStr.trim()).compareTo(strToConvert) == 0) {
								//if ((rolesData[i].getProductType()).compareTo(pdCode) == 0) { //NBA006
								response = rolesData[i].getBesValue();
								break;
								//} //NBA006
							}
						}
					}
					// report not found error if necessary
					if (i == iTot) {
						if (getLogger().isWarnEnabled())
							getLogger().logWarn(
								"Could not locate CyberLife value in Roles table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Product Type '"
									+ pdCode
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					} else {
						// Temporary debug message...
						if (getLogger().isDebugEnabled())
							getLogger().logDebug(
								"CyberLife '"
									+ response
									+ "' value located in Roles table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Product Type '"
									+ pdCode
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					}
				} else {
					// report error condition
				}
				break;

			case CYBTBL_PLAN :
			case CYBTBL_PLAN_COV_KEY :
			case CYBTBL_PRODUCT_TYPE :
				// if it is not loaded then go get it, otherwise use version from map
				if (inMap) {
					plansData = ((NbaPlansData[]) tblMap.get(tblName));
				} else {
					plansData = ((NbaPlansData[]) getPlansTable(compCd));
				}
				if (plansData != null) {
					int i;
					if (!inMap)
						tblMap.put(tblName, plansData);
					iTot = plansData.length;
					// now that we have the table data, find our field
					for (i = 0; i < iTot; i++) {
						if (plansData[i].getCoverageKey() != null) {
							tStr = plansData[i].getCoverageKey();
							if ((tStr.trim()).compareTo(strToConvert) == 0) {
								switch (tblType) {
									case CYBTBL_PRODUCT_TYPE :
										response = plansData[i].getProductType();
										break;
									case CYBTBL_PLAN_COV_KEY :
										response = plansData[i].getCoverageKey();
										break;
									default :
										response = plansData[i].getCoverageKey();
								}
								break;
							}
						}
					}
					// report not found error if necessary
					if (i == iTot) {
						if (getLogger().isWarnEnabled())
							getLogger().logWarn(
								"Could not locate CyberLife value in Plans table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					} else {
						// Temporary debug message...
						if (getLogger().isDebugEnabled())
							getLogger().logDebug(
								"CyberLife '"
									+ response
									+ "' value located in UCT table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					}
				} else {
					// report error condition
					if (getLogger().isWarnEnabled())
						getLogger().logWarn(
							"UCT Table '"
								+ tblName
								+ "' is not loaded.  Attempting to convert OLife value '"
								+ strToConvert
								+ "', Company Code = '"
								+ compCd
								+ "', Cov Key = '"
								+ covKey
								+ "'.");
				}
				break;
			case CYBTBL_REQUIREMENTS :
				// if it is not loaded then go get it, otherwise use version from map
				if (inMap) {
					requirementsData = ((NbaRequirementsData[]) tblMap.get(tblName));
				} else {
					requirementsData = ((NbaRequirementsData[]) getRequirementsTable(tblName, compCd, covKey));
				}
				if (requirementsData != null) {
					int i;
					if (!inMap)
						tblMap.put(tblName, requirementsData);
					iTot = requirementsData.length;
					// now that we have the table data, find our field
					for (i = 0; i < iTot; i++) {
						if (requirementsData[i].code() != null) {
							tStr = requirementsData[i].code().trim();
							if (tStr.compareTo(strToConvert) == 0) {
								response = requirementsData[i].besValue;
								break;
							}
						}
					}
					// report not found error if necessary
					if (i == iTot) {
						if (getLogger().isWarnEnabled())
							getLogger().logWarn(
								"Could not locate CyberLife value in requirements table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					} else {
						// Temporary debug message...
						if (getLogger().isDebugEnabled())
							getLogger().logDebug(
								"CyberLife '"
									+ response
									+ "' value located in Requirements table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					}
				} else {
					// report error condition
					if (getLogger().isWarnEnabled())
						getLogger().logWarn(
							"Requirements Table '"
								+ tblName
								+ "' is not loaded.  Attempting to convert OLife value '"
								+ strToConvert
								+ "', Company Code = '"
								+ compCd
								+ "', Cov Key = '"
								+ covKey
								+ "'.");
				}
				break;
			case CYBTBL_UCT :
			case CYBTBL_UCT_BY_INDEX_TRANS :
				// if it is not loaded then go get it, otherwise use version from map
				if (inMap) {
					uctData = ((NbaUctData[]) tblMap.get(tblName));
				} else {
					uctData = ((NbaUctData[]) getUctTable(tblName, compCd, covKey));
				}
				if (uctData != null) {
					int i;
					if (!inMap)
						tblMap.put(tblName, uctData);
					iTot = uctData.length;
					// now that we have the table data, find our field
					for (i = 0; i < iTot; i++) {
						if (uctData[i].code() != null) {
							//nba076
							switch (tblType) {
								case CYBTBL_UCT :
									tStr = uctData[i].code().trim();
									break;
								case CYBTBL_UCT_BY_INDEX_TRANS :
									tStr = uctData[i].getIndexTranslation().trim();
									break;
								default:
									tStr = "";
									break;
							}
							
							if ((tStr.compareTo(strToConvert) == 0)&& (uctData[i].besValue != null)) {
								response = uctData[i].besValue;
								break;
							//end nba076
							}
						}
					}
					// report not found error if necessary
					if (i == iTot) {
						if (getLogger().isWarnEnabled())
							getLogger().logWarn(
								"Could not locate CyberLife value in UCT table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					} else {
						// Temporary debug message...
						if (getLogger().isDebugEnabled())
							getLogger().logDebug(
								"CyberLife '"
									+ response
									+ "' value located in UCT table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					}
				} else {
					// report error condition
					if (getLogger().isWarnEnabled())
						getLogger().logWarn(
							"UCT Table '"
								+ tblName
								+ "' is not loaded.  Attempting to convert OLife value '"
								+ strToConvert
								+ "', Company Code = '"
								+ compCd
								+ "', Cov Key = '"
								+ covKey
								+ "'.");
				}
				break;
			case CYBTBL_STATE :
			case CYBTBL_STATE_TC :
			case CYBTBL_STATE_ALPHA :
				if (inMap) {
					statesData = ((NbaStatesData[]) tblMap.get(tblName));
				} else {
					statesData = ((NbaStatesData[]) getStateTable(tblName));
				}
				if (statesData != null) {
					int i;
					if (!inMap)
						tblMap.put(tblName, statesData);
					iTot = statesData.length;
					// now that we have the table data, find our field
					for (i = 0; i < iTot; i++) {
						if (statesData[i].code() != null) {
							tStr = statesData[i].code();
							if ((tStr.trim()).compareTo(strToConvert) == 0) {

								if (tblType == CYBTBL_STATE_TC)
									response = statesData[i].getCybStateCode();
								else if (tblType == CYBTBL_STATE_ALPHA)
									response = statesData[i].getCybAlphaStateCodeTrans();
								else
									response = statesData[i].getCybShortStateCodeTrans();
								break;
							}
						}
					}
					// report not found error if necessary
					if (i == iTot) {
						if (getLogger().isWarnEnabled())
							getLogger().logWarn(
								"Could not locate CyberLife value in State table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					} else {
						// Temporary debug message...
						if (getLogger().isDebugEnabled())
							getLogger().logDebug(
								"CyberLife '"
									+ response
									+ "' value located in States table '"
									+ tblName
									+ "', OLife value '"
									+ strToConvert
									+ "', Company Code = '"
									+ compCd
									+ "', Cov Key = '"
									+ covKey
									+ "'.");
					}
				} else {
					// report error condition
				}
				break;
			default :
				// report error condition
				response = "";
		}
		return response.trim();
	}
	/**
	 * Translate the boolean xml value to the appropriate dxe value via database translation
	 * 
	 * @param varToConvert input value
	 * @param tblName Translation table name
	 * @param tblType Table type
	 * @param compCd company code
	 * @param covKey coverage key
	 * @return CyberLife value
	 */
	protected String getCyberValue(boolean varToConvert, String tblName, int tblType, String compCd, String covKey) {
		// Make call with blank parameter
		return getCyberValue(formatCyberBoolean(varToConvert), tblName, tblType, compCd, covKey, "");
	}
	/**
	 * Translate the boolean xml value to the appropriate dxe value via database translation
	 * 
	 * @param varToConvert input value
	 * @param tblName Translation table name
	 * @param tblType Table type
	 * @param compCd company code
	 * @param covKey coverage key
	 * @param param optional data for some table types
	 * @return CyberLife value
	 */
	protected String getCyberValue(boolean varToConvert, String tblName, int tblType, String compCd, String covKey, String param) {
		// Make call with blank parameter
		return getCyberValue(formatCyberBoolean(varToConvert), tblName, tblType, compCd, covKey, param);
	}
	/**
	 * Retrieve the funds table data for a plan
	 * 
	 * @param param input plan
	 * @return table data
	 */
	protected NbaTableData[] getFundsTable(String param) {
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); //SPR1018
		aCase.put("plan", param);
		aCase.put("assumedInterestRate", "0");

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading NBA_FUNDS");

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, "NBA_FUNDS");

		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("Data Access Exception Loading NBA_FUNDS");
		}
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded NBA_FUNDS");
		return (tArray);
	}
	/**
	 * Retrieve/Create logger object instance for this class
	 * 
	 * @return logger object
	 */
	private NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaCyberRequests.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaServiceLocator could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Remove the numeric value after an underscore in a tag
	 * @param tag input string
	 * @return integer value of number following the underscore
	 */
	protected int getOccurance(String tag) {
		int iUscore;
		// SPR3290 code deleted

		iUscore = tag.lastIndexOf("_");
		if (iUscore == -1)
			return -1;
		else {
			return Integer.parseInt(tag.substring(iUscore + 1)); //SPR2992
		}
	}
	/**
	 * Get party id for a party/rolecode combination in hashmap
	 * 
	 * @param partyId input party id
	 * @param roleCode input role code
	 * @return string containing party id
	 */
	protected String getPartyId(String partyId, String roleCode) {
		String keyStr = partyId + ":" + roleCode;

		if (partyMap.containsKey(keyStr))
			return (String) partyMap.get(keyStr);
		else
			return "";
	}
	/**
	* Calls translation table to retrieve list of Plan information
	* @param compCode company code
	* @return NbaTableData[] containing requested table data
	*/
	protected NbaTableData[] getPlansTable(String compCode) {

		Date currentDate = new Date();
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("appState", "*");
		aCase.put("appDate", currentDate);
		aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); //SPR1018

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading NBA_PLANS");

		NbaTableData[] tArray = null;

		try {
			tArray = ntsAccess.getDisplayData(aCase, "NBA_PLANS");
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("NbaDataAccessException Loading NBA_PLANS");
		}

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded NBA_PLANS");

		return (tArray);
	}
	/**
	 * Calls translation table to retrieve list of requirement information.
	 * @param tableName table name
	 * @param compCode company code
	 * @param covKey coverage key
	 * @return NbaTableData[] containing requested table data
	 */
	protected NbaTableData[] getRequirementsTable(String tableName, String compCode, String covKey) {
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("tableName", tableName);
		aCase.put("plan", covKey);
		aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); //SPR1018

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading requirements table " + tableName);

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, tableName);
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("NbaDataAccessException Loading requirements table " + tableName);
		}
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded requirements " + tableName);
		return (tArray);
	}
	/** 
	 * Retrieve roles table data
	 * @param compCode company code
	 * @return NbaTableData[] containing requested table data
	 */
	protected NbaTableData[] getRolesTable(String compCode) {
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("productTypSubtyp", "*");
		aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); //SPR1018

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading NBA_ROLES");

		NbaTableData[] tArray = null;

		try {
			tArray = ntsAccess.getDisplayData(aCase, "NBA_ROLES");
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logDebug("NbaDataAccessException Loading NBA_ROLES");

		}

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded NBA_ROLES");
		return (tArray);
	}
	/**
	 * Retrieve the Roles table from the database
	 * @param compCode Company code
	 * @param prodType Product type
	 * @return NbaTableData[] containing requested table data
	 */
	protected NbaTableData[] getRolesTable(String compCode, String prodType) {
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("productTypSubtyp", prodType);
		aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); //SPR1018

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading NBA_ROLES");

		NbaTableData[] tArray = null;

		try {
			tArray = ntsAccess.getDisplayData(aCase, "NBA_ROLES");
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("NbaDataAccessException Loading NBA_ROLES");
		}

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded NBA_ROLES");

		return (tArray);
	}
	/**
	* Calls translation table to retrieve list of States
	* @param stateTable state table name
    * @return NbaTableData[] containing requested table data
	*/
	protected NbaTableData[] getStateTable(String stateTable) {
		HashMap aCase = new HashMap();
		aCase.put("company", "*");
		aCase.put("tableName", stateTable);

		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading NBA_STATES");

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, stateTable);
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("NbaDataAccessException Loading NBA_STATES");
		}
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded NBA_STATES");
		return (tArray);
	}
	/**
	 * Calls the translation tables for UCT Tables
	 * @param tableName The name of the UCT table.
	 * @param compCode Company code.
	 * @param covKey Coverage key(pdfKey).
	 * @return NbaTableData[] containing requested table data
	 */
	protected NbaTableData[] getUctTable(String tableName, String compCode, String covKey) {
		HashMap aCase = new HashMap();
		aCase.put("company", compCode);
		aCase.put("tableName", tableName);
		aCase.put("plan", covKey);
		//nba076
		if (tableName.equals("OLIEXT_LU_SITCODE"))
			aCase.put("backendSystem", "ACF"); 
		else
			aCase.put("backendSystem", NbaConstants.SYST_CYBERLIFE); //SPR1018
		//end nba076
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loading UCT " + tableName);

		NbaTableData[] tArray = null;
		try {
			tArray = ntsAccess.getDisplayData(aCase, tableName);
		} catch (NbaDataAccessException e) {
			if (getLogger().isWarnEnabled())
				getLogger().logWarn("NbaDataAccessException Loading UCT " + tableName);
		}
		if (getLogger().isDebugEnabled())
			getLogger().logDebug("Loaded UCT " + tableName);
		return (tArray);
	}
	/**
	 * Zero pad string to a certain length
	 * 
	 * @param strToPad input string
	 * @param padLen total length of field
	 * @return zero-padded string
	 */
	protected String zeroPadString(String strToPad, int padLen) {
		if (strToPad.length() >= padLen)
			return strToPad;
		else {
			String padStr = ZERO_STRING.substring(0, padLen - strToPad.length()) + strToPad;
			return padStr;
		}
	}
	/**
	 * Answer the coverage key, regardless if it's a life, annuity, coverage, or rider.
	 * @param substandardRating substandard rating
	 * @return the substandardRating type
	 */
	// NBA093 New Method
	// SPR1549 Added isIS00 parameter
	protected String getRatingType(SubstandardRating substandardRating, boolean isIS00) {
		String ratingType = null;
		SubstandardRatingExtension substandardRatingExt = NbaUtils.getFirstSubstandardExtension(substandardRating);
		if (substandardRating.hasPermTableRating()) {
			if (substandardRatingExt != null && substandardRatingExt.hasPermPercentageLoading() && !isIS00) {	//SPR1549
				ratingType = SUB_TYPE_PERCENT;	//CyberLife stores Perm table ratings as Pct ratings	//SPR1549
			} else {	//SPR1549
				ratingType = SUB_TYPE_PERM_TABLE;
			}	//SPR1549
		} else if (substandardRating.hasTempTableRating()) {
			if (substandardRatingExt != null && substandardRatingExt.hasTempPercentageLoading() && !isIS00) {	//SPR1549
				ratingType = SUB_TYPE_PERCENT;	//CyberLife stores Temp table ratings as Pct ratings	//SPR1549
			} else {	//SPR1549
				ratingType = SUB_TYPE_TEMP_TABLE;
			}	//SPR1549
		} else if (substandardRating.hasTempFlatExtraAmt()) {
			ratingType = SUB_TYPE_TEMP_FLAT;
		} else if (substandardRatingExt != null) {
			if (substandardRatingExt.hasPermFlatExtraAmt()) {
				ratingType = SUB_TYPE_PERM_FLAT;
			}
		}

		return ratingType;
	}
	/**
	 * Find the special frequency mode for billing
	 * @param policy The Policy XML object
	 * @return the special frequency
	 */
	//NBA104 New Method
	protected String getSpcMode(Policy policy) {
		switch ((int) policy.getPaymentMode()) {
			case (int)OLI_PAYMODE_WKLY :
				return ("1");
			case (int)OLI_PAYMODE_BIWKLY :
				return ("2");
			case (int)OLI_PAYMODE_MNTH49 :
				return ("9");
			case (int)OLI_PAYMODE_4WKLY :
				return ("4");
			case (int)OLI_PAYMODE_MNTH410 :
				return ("A");
			default :
				return ("0");
		}
	}
	
	 /**
     * Returns the rating type of the lifeParticipant.
     * @param LifeParticipant
     * @return the rating type
     */
    //SPR1738 New Method
    protected String getRatingType(LifeParticipant lifeparticipant) {
        String ratingType = null;
		if (lifeparticipant.hasPermTableRating()) {
			ratingType = SUB_TYPE_PERM_TABLE;
		} else if (lifeparticipant.hasTempTableRating()) {
			ratingType = SUB_TYPE_TEMP_TABLE;
		} else if (lifeparticipant.hasTempFlatExtraAmt()) {
			ratingType = SUB_TYPE_TEMP_FLAT;
		} else if (lifeparticipant.hasPermFlatExtraAmt()) {
			ratingType = SUB_TYPE_PERM_FLAT;
		}
		return ratingType;
    }
	/**
	 * Add the information applicable to Billing Control (33) segment to the message if a Banking object for Billing
	 * is present on the NbaTXLife
	 * @param nbaTXLife - the NbaTXLife
	 * @param policy - the Policy
	 * @return the DXE for the 33 (Billing Control)
	 */
	//SPR2151 New Method
	protected String createBillingControl(NbaTXLife nbaTXLife, Policy policy) {
		Banking banking = nbaTXLife.getBankingForBilling(); //SPR3573
		StringBuffer billingInfo = new StringBuffer();
		if (banking != null) {
			BankingExtension bankingExtension = NbaUtils.getFirstBankingExtension(banking);
			if (banking.hasBankAcctType()) {	//Electronic billing 
				billingInfo.append(createDataExchange("98", BILL_CONTROL_IDENT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2)); //Turn on Flag A bit 0
				createBillingControlForElectronicBanking(banking, billingInfo, bankingExtension);
			} else { //Not electronic billing 
				billingInfo.append(createDataExchange("99", BILL_CONTROL_IDENT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
				if (banking.hasBankingKey() && banking.getBankingKey().length() > 0) { //Billing Occurance DXE: FBCOCCUR must follow FBCPIDNT
					billingInfo.append(createDataExchange(banking.getBankingKey(), INF_BILLING_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
				} else {
					billingInfo.append(createDataExchange("01", INF_BILLING_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2)); //Default FBCOCCUR tp "01"
				}
			}
			//Information common to both Electronic and non-Electronic billing
			billingInfo.append(createDataExchange("1", INF_BILLING_CRDR_TYPE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 1)); //Force Debit billing			

			if (bankingExtension != null) {				
				if (bankingExtension.hasBillControlEffDate()) {//Bill Control Eff. Date DXE: FBCEFFDT
					billingInfo.append(createDataExchange(bankingExtension.getBillControlEffDate(), BILL_CONTROL_EFF_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
				}
			}
			if (policy != null && policy.hasBillNumber()) { // Billing Control or List bill number  DXE: FBCBBNUM
				billingInfo.append(createDataExchange(policy.getBillNumber(), LIST_BILL_NUM, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 26));
			} else if (bankingExtension != null && bankingExtension.hasBillControlNumber()){
				billingInfo.append(createDataExchange(bankingExtension.getBillControlNumber(), LIST_BILL_NUM, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 26));				
			}
		}
		return billingInfo.toString();
	}
	/**
	 * Add the information applicable to Electronic billing to the message
	 * @param banking - the Banking object for Billing
	 * @param billingInfo - the message
	 * @param bankingExtension - the Extension objection for the the Banking object
	 */
	//SPR2151 New Method
	protected void createBillingControlForElectronicBanking(Banking banking, StringBuffer billingInfo, BankingExtension bankingExtension) {
		if (banking.hasBankingKey() && banking.getBankingKey().length() > 0) { //Billing Occurance DXE: FBCOCCUR must follow FBCPIDNT
			billingInfo.append(createDataExchange(banking.getBankingKey(), INF_BILLING_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2));
		} else {
			billingInfo.append(createDataExchange("01", INF_BILLING_KEY, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 2)); //Default FBCOCCUR tp "01"
		}
		long acctType = banking.getBankAcctType();
		billingInfo.append(createDataExchange(acctType, BANK_ACCT_TYPE, NbaTableConstants.OLI_LU_BANKACCTTYPE, CYBTBL_UCT, CT_CHAR, 1)); //Electronic fund account type - DXE: FBCACTYP
		if (banking.hasAccountNumber()) { //Banking or Credit account Number - DXE: FBCBKACT
			billingInfo.append(createDataExchange(banking.getAccountNumber(), PAYEE_BANK_ACCT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 17));
		}
		if (acctType == OLI_BANKACCT_CREDCARD || acctType == OLI_BANKACCT_DEBITCARD) { //Credit or debit Card
			if (banking.hasCreditCardType()) { //Type of credit card - DXE: FBCCTYPE
				billingInfo.append(createDataExchange(banking.getCreditCardType(), CREDIT_CARD_TYPE, NbaTableConstants.OLI_LU_CREDCARDTYPE, CYBTBL_UCT, CT_CHAR, 1));
			}
			if (banking.hasCreditCardExpDate()) { //Credit Card Expiration Date - DXE: FBCCEXPR
				billingInfo.append(createDataExchange(formatYearMonthDate(banking.getCreditCardExpDate()), CREDIT_CARD_EXP_DT, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 8));
			}
		} else { //Checking or Savings
			if (banking.hasRoutingNum()) { //Transit routing number of the destination institution - FBCTRNSO
				billingInfo.append(createDataExchange(banking.getRoutingNum(), TRANSIT_ROUTING_NUM, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 17));
			}
			if (bankingExtension != null) {
				if (bankingExtension.hasBranchNumber()) { // Branch Number (Holding.Policy) DXE: FBCTRNBR
					billingInfo.append(createDataExchange(bankingExtension.getBranchNumber(), BRANCH_NUM, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
				}
			}
			if (bankingExtension.hasAchInd()) {
				billingInfo.append(createDataExchange(bankingExtension.getAchInd(), MEDIA_CODE, CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 9));
			}
		}
		if (bankingExtension != null && bankingExtension.hasAccountHolderNameCC()) { //Signature Name 1-3 - DXE: FBCNAME
			AccountHolderNameCC acntHolderCC = bankingExtension.getAccountHolderNameCC();
			int count = acntHolderCC.getAccountHolderNameCount();
			if (count > 3) {
				count = 3;
			}
			for (int j = 0; j < count; j++) {
				billingInfo.append(createDataExchange(acntHolderCC.getAccountHolderNameAt(j), ACCOUNT_HOLDER_NAME + (j + 1), CYBTRANS_NONE, CYBTBL_NONE, CT_CHAR, 20));
			}
		}
	}
	
	/**
	 * Format a YYYY-MM date String (xsd:gYearMonth) to a YYYYMMDD String, defaulting the day to the last day of the month.
	 * @param aDate -  YYYY-MM date String
	 * @return - YYYYMMDD String
	 */
	 //SPR2151 New Method
	protected String formatYearMonthDate(String aDate) {
		try {
			StringBuffer newDate = new StringBuffer();
			if (aDate != null && aDate.indexOf("-") == 4) {
				newDate.append(aDate.substring(0, 4)).append(aDate.substring(5, 7)).append("01");
				Date tempDate = sdf_iso.parse(newDate.toString());
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(tempDate);
				int day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
				newDate.setLength(6);
				newDate.append(Integer.toString(day));
			}
			return newDate.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

}
