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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.AnnuityProduct;
import com.csc.fs.dataobject.accel.product.CoverageProduct;
import com.csc.fs.dataobject.accel.product.LifeProductExtension;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.database.AxaRulesDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaPlansData;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.transaction.validation.NbaTransactionValidation;
import com.csc.fsg.nba.transaction.validation.NbaTransactionValidationFactory;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.BusValidation;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.configuration.ValRule;
import com.csc.fsg.nba.vo.configuration.ValidationDef;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.HHFamilyInsurance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Intent;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.SystemMessageExtension;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TempInsAgreementDetails;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVPMSHelper;
/**
 * NbaContractValidation performs contract validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>SPR1705</td><td>Version 3</td><td>Vantage Annuity validation</td></tr>
 * <tr><td>NBA112</td><td>Version 4</td><td>Agent Name and Address</td></tr>
 * <tr><td>SPR1466</td><td>Version 4</td><td>Scheduled Withrawals BF rework </td></tr>
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1234</td><td>Version 4</td><td>General source code clean up </td></tr>
 * <tr><td>SPR1607</td><td>Version 4</td><td>AAR contract val</td></tr>
 * <tr><td>SPR2251</td><td>Version 6</td><td>Handle Requested Issue Date for Reissued Contracts</td></tr>
 * <tr><td>NBA133</td><td>Version 6</td><td>nbA CyberLife Interface for Calculations and Contract Print</td></tr>
 * <tr><td>SPR2359</td><td>Version 6</td><td>Increase Coverage Effective Date Always Defaults to Policy Effective Date</td></tr>
 * <tr><td>SPR3098</td><td>Version 6</td><td>Contract Validation Processes and Edits Should Bypass Proposed Substandard Ratings</td></tr>
 * <tr><td>SPR3117</td><td>Version 6</td><td>New Application Filter in Contract Validation Needs to Be Changed to Account for Rerate, Increase, and Reinstatement</td></tr>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA211</td><td>Version 7</td><td>nbA Partial Application</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.79</td><td>AXA Life Phase 1</td><td>Shared AWD</td></tr>
 * <tr><td>AXAL3.7.40</td><td>AXA Life Phase 1</td><td>Contract Validation</td></tr>
 * <tr><td>AXAL3.7.18</td><td>AXA Life Phase 1</td><td>Producer Interfaces</td></tr>
 * <tr><td>NBA234</td><td>Version 8</td><td> nbA ACORD Transformation Service Project</td></tr> 
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * <tr><td>NBA254</td><td>Version 8</td><td>Automatic Closure and Refund of CWA</td></tr>
 * <tr><td>ALPC066</td><td>AXA Life Phase 1</td><td>Term Series Qualified</td></tr>
 * <tr><td>SR494086.2</td><td>Discretionary</td><td>Contract Validation</td></tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>P2AXAL027</td><td>AXA Life Phase 2</td><td>Omission and Misc Validation</td></tr> 
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>FNB020</td><td>AXA Life Phase 1</td><td>Performance Tuning</td></tr>
 * <tr><td>NBA297</td><td>Version 8</td><td>Suitability</td></tr>
 * <tr><td>P2AXAL021</td><td>AXA Life Phase 2</td><td>Suitability</td></tr> 
 * <tr><td>P2AXAL054</td><td>AXA Life Phase 2</td><td>Omissions and Contract Validations</td></tr> 
 * <tr><td>A4_AXAL001</td><td>AXA Life NewApp</td><td>New Application � Application Entry A4</td></tr>
 * <tr><td>CR1346709</td><td>Discretionary</td><td>Loan Carryover Indicator</td></tr>
 * <tr><td>CR1346706(APSL2724)</td><td>Discretionary</td><td>IUP March Updates</td></tr>
 * <tr><td>APSL2808</td><td>Discretionary</td><td>Simplified Issue</td></tr>
 * <tr><td>APSL3527</td><td>Discretionary</td><td>CHAUG002 Omission CV Rules for Financial Supplement</td></tr>
 * <tr><td>SR805869(APSL3818)</td><td>Discretionary</td><td> LTCSR  for California</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaContractValidation
	extends NbaContractValidationCommon
	implements NbaConstants, NbaTableConstants, NbaTableAccessConstants {	//SPR1234
	class ObjectEntry {
		protected String ctlVals;
		protected int end;
		protected String objectID;
		protected int sequence = 0;
		protected int start;
		public ObjectEntry(String objectID, String ctlVals, int start, int end) {
			super();
			setObjectID(objectID);
			setCtlVals(ctlVals);
			setStart(start);
			setEnd(end);
		}
		/**
		 * Returns the ctlVals.
		 * @return String
		 */
		public String getCtlVals() {
			return ctlVals;
		}
		/**
		 * Returns the end.
		 * @return int
		 */
		public int getEnd() {
			return end;
		}
		/**
		 * Returns the objectID.
		 * @return String
		 */
		public String getObjectID() {
			return objectID;
		}
		/**
		 * Returns the sequence.
		 * @return int
		 */
		public int getSequence() {
			return sequence;
		}
		/**
		 * Returns the start.
		 * @return int
		 */
		public int getStart() {
			return start;
		}
		/**
		 * Sets the ctlVals.
		 * @param ctlVals The ctlVals to set
		 */
		public void setCtlVals(String ctlVals) {
			this.ctlVals = ctlVals;
		}
		/**
		 * Sets the end.
		 * @param end The end to set
		 */
		public void setEnd(int end) {
			this.end = end;
		}
		/**
		 * Sets the objectID.
		 * @param objectID The objectID to set
		 */
		public void setObjectID(String objectID) {
			this.objectID = objectID;
		}
		/**
		 * Sets the sequence.
		 * @param sequence The sequence to set
		 */
		public void setSequence(int sequence) {
			this.sequence = sequence;
		}
		/**
		 * Sets the start.
		 * @param start The start to set
		 */
		public void setStart(int start) {
			this.start = start;
		}
	}
	protected static HashMap filters = new HashMap();
	static java.util.HashMap validationMessageTable = new java.util.HashMap();
	static String[] defaultMinusFilters; //SR494086.2, ADC Retrofit
	private static HashMap valRules = new HashMap(); //NBA297
	protected boolean totalInternalRepAmtFlag = false;//APSL3527 
	protected double totalInternalRepAmt = 0;//APSL3527
	protected static java.util.List noProductRequired = new java.util.ArrayList(); //ALII2041
	static {
		NbaContractValidation nbaContractValidation = new NbaContractValidation();
		// SPR3290 code deleted
		Method[] allMethods = nbaContractValidation.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("filter_")) {
				filters.put(aMethodName.substring(7).toUpperCase(), aMethod);
			}
		}
		//Begin SR494086.2, ADC Retrofit
		try {
			StringTokenizer st = new StringTokenizer(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_MINUS_FILTERS), ",");
			defaultMinusFilters = new String[st.countTokens()];
			int i = -1;
			while (st.hasMoreTokens()) {
				defaultMinusFilters[++i] = st.nextToken();
			}
		} catch (NbaBaseException ex) {
			defaultMinusFilters = new String[0];
		}
		//End SR494086.2
		//NBA297 Begin
		List valRuleList = NbaConfiguration.getInstance().getValRuleList();
		for (int i = 0; i < valRuleList.size(); i++) {
			ValRule valRule = (ValRule) valRuleList.get(i);
			valRules.put(valRule.getName(), valRule);
		}
		//NBA297 End
		
		noProductRequired.add(NbaConstants.PROC_REQUIREMENTS);  //ALII2041 REQUIREMENTS does not require product
		
	}
	/**
	 * Returns the filters.
	 * @return HashMap
	 */
	protected static HashMap getFilters() {
		return filters;
	}
	
	/**
	 * @return Returns the valRules.
	 */
	//NBA297 New Method
	public static HashMap getValRules() {
		return valRules;
	}
	
	//NBA103 - removed getLogger()
	
	/**
	 * Returns the validationMessageTable.
	 * @return java.util.HashMap
	 */
	protected static java.util.HashMap getvalidationMessageTable() {
		return validationMessageTable;
	}
	/**
	 * Sets the validationMessageTable.
	 * @param validationMessageTable The validationMessageTable to set
	 */
	protected static void setvalidationMessageTable(java.util.HashMap validationMessageTable) {
		NbaContractValidation.validationMessageTable = validationMessageTable;
	}
	protected String formattedDate;
	protected int msgCount = 0;
	protected NbaContractValidationImpl nbaContractValidationImpl;
	protected ArrayList objectStack = new ArrayList();
	protected ArrayList procs; //ACN012
	protected boolean subSetComplete;
	/**
	 * Constructor for NbaContractValidation.
	 */
	public NbaContractValidation() {
		super();
	}
	/**
	 * Add entries to the Object Stack.
	 * @param ctlString the ctl token string
	 * @param fromIndex the location at which to start evaluating the tokens
	 * @param current the location in the process stack of the ObjectEntry
	 * containing the ctl tokens. 
	 */
	protected void addToObjectStack(String ctlString, int fromIndex, int current) {
		if (ctlString.equals("FINISHED")) {
			setSubSetComplete(true);
		} else {
			String objectID;
			while (fromIndex < ctlString.length()) { //Add new values to stack
				int toIndex = ctlString.indexOf(".", fromIndex);
				if (toIndex < 0) {
					toIndex = ctlString.length();
				}
				objectID = ctlString.substring(fromIndex, toIndex); //The string for the object, i.e. COV
				String ctlVals = ctlString.substring(0, toIndex); //Substring up to and including the object
				int end = lastOccurrence(ctlVals, current); //Last occurrence of ctlVals in the Process Stack
				addToObjectStack(objectID, ctlVals, current, end);
				fromIndex = toIndex + 1;
			}
		}
	}
	/**
	* Add an entry to the Object Stack if the object exists.
	* @param objectID the identifier for the object
	* @param ctlVals the Substring up to and including the current node
	* @param occurrence the occurrence to search for
	*/
	protected void addToObjectStack(String objectID, String ctlVals, int current, int end) {
		if (hasOccurrence(objectID, 0)) {
			getobjectStack().add(new ObjectEntry(objectID, ctlVals, current, end));
		}
	}
	/**
	 * Adjust the object stack.  Determine if any objects in the stack have
	 * another occurrence that needs to be validated. If not, perform processing
	 * for the current processing entry.
	 * @param lastCtl the ctl tokens from the previous entry in the Object Stack.
	 * @param current the location in the process stack of the ObjectEntry
	 * @return true if the last loop in the Object Stack should be repeated
	 */
	protected boolean adjustObjectStack(String lastCtl, int current) {
		String nextCtl = getCtlAt(current);
		logDebug("adjustObjectStack for " + nextCtl + " last = " + lastCtl);//NBA103
		int fromIndex;
		ObjectEntry processEntry;
		if (lastCtl.length() < 1 || nextCtl.startsWith(lastCtl)) { //first time or a child of the previous
			if (lastCtl.length() > 0) {
				fromIndex = lastCtl.length() + 1;
			} else {
				fromIndex = 0;
			}
			if (current < getProcs().size()) { //ACN012
				setNbaConfigValProc((ValProc)getProcs().get(current)); //ACN012
			}
			addToObjectStack(nextCtl, fromIndex, current); //perform the current process
			return false;
		} else {
			//Check range of previous loop entry
			processEntry = getObjectStackBottom();
			if (processEntry == null) {
				addToObjectStack(nextCtl, 0, current); //stack fully unwound
				return false;
			} else {
				if (current > processEntry.getEnd()) { //end of range for previous entry
					if (finishedWithObject(processEntry)) {
						popObjectStack(); //done with prevous
						processEntry = getObjectStackBottom();
						if (processEntry == null) {
							return false; //stack fully unwound
						} else {
							return adjustObjectStack(processEntry.getCtlVals(), current); // check again with next higher level
						}
					} else {
						if (!getCtlAt(processEntry.getStart()).equals(processEntry.getCtlVals())) {
							addToObjectStack(
								getCtlAt(processEntry.getStart()),
								processEntry.getCtlVals().length() + 1,
								processEntry.getStart());
						}
						return true; //repeat with a new occurrence of the object
					}
				} else {
					fromIndex = processEntry.getCtlVals().length() + 1;
					addToObjectStack(nextCtl, fromIndex, current);
					return false;
				}
			}
		}
	}
	/**
	 * Determine if the contract structure is invalid.
	 * @return true if a major portion is missing
	 */
	protected boolean contractStructureProblem() {
		boolean errorFound = false;
		String errorString = "";
		String relatedId = "";
		errorFound = setAndVerifyOLifE(getNbaTXLife().getOLifE());
		if (errorFound) {
			errorString = "OLifE object is missing or invalid";
		}
		errorFound = setAndVerifyHolding(getNbaTXLife().getPrimaryHolding());
		if (errorFound && errorString.length() < 1) {
			errorString = "Primary Holding is missing or invalid";
			relatedId = getHolding().getId();
		}
		errorFound = setAndVerifyPolicy(getHolding().getPolicy());
		if (errorFound && errorString.length() < 1) {
			errorString = "Policy is missing or invalid";
			relatedId = getHolding().getId();
		}
		if (errorFound) {
			addNewSystemMessage(CONTRACT_INVALID, errorString, relatedId);
		}
		return errorFound;
	}
	/**
	 * Filter for Asset Reallocation arrangement (ArrType ="3").
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is Asset Reallocation.
	 */
	// ACN012 changed signature
	protected boolean filter_AAR(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && (getArrangement().getArrType() == OLI_ARRTYPE_ASSALLO || getArrangement().getArrType() == OLI_ARRTYPE_AA); //SPR1607
	}
	/**
	 * Filter for CyberLife agent system.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	// ACN012 changed signature
	protected boolean filter_ACF(ValProc nbaConfigValProc, ArrayList objects) {
		return getNbaTXLife().isAgentSystemACF();
	}
	/**
	 * Filter for Annuity contract. Return true if Policy.Annuity is present.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is an Annuity contract.
	 */
	// ACN012 changed signature
	protected boolean filter_Ann(ValProc nbaConfigValProc, ArrayList objects) {
		return getAnnuity() != null;
	}
	/**
	 * Filter for CyberLife.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is CyberLife.
	 */
	// ACN012 changed signature
	protected boolean filter_Clif(ValProc nbaConfigValProc, ArrayList objects) {
		return isSystemIdCyberLife();
	}
	/**
	 * Filter for Dollar Cost Averaging arrangement (ArrType="2").
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is Dollar Cost Averaging.
	 */
	// ACN012 changed signature
	protected boolean filter_DCA(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && getArrangement().getArrType() == OLI_ARRTYPE_COSTAVG;
	}
	/**
	 * Filter for Scheduled Withdrawal arrangement (ArrType="7", "8", "9", "10", "13", "14", "42", "43", "44", "45" or "46").
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is Scheduled Withdrawal.
	 */
	//SPR1466 New Method
	// ACN012 changed signature
	protected boolean filter_SWD(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && (getArrangement().getArrType() == OLI_ARRTYPE_SPECAMTNETWITH //7
		|| getArrangement().getArrType() == OLI_ARRTYPE_SPECAMTGROSSWITH //8
		|| getArrangement().getArrType() == OLI_ARRTYPE_SURRFREEWITH //9
		|| getArrangement().getArrType() == OLI_ARRTYPE_INTNETWITH //10
		|| getArrangement().getArrType() == OLI_ARRTYPE_REQMINWITH //13
		|| getArrangement().getArrType() == OLI_ARRTYPE_PCTVALWITH //14
		|| getArrangement().getArrType() == OLI_ARRTYPE_INTBETWITH //42
		|| getArrangement().getArrType() == OLI_ARRTYPE_INTEARNISS //43
		|| getArrangement().getArrType() == OLI_ARRTYPE_PROJECTINT //44
		|| getArrangement().getArrType() == OLI_ARRTYPE_FIXEDPERIOD //45
		|| getArrangement().getArrType() == OLI_ARRTYPE_SPECFUNDS //46
		);
	}
	/**
	 * Filter for Flexible Premium contract. For Annuity contracts, return true if 
	 * AnnuityProduct.PremType equals 2.  For Life contracts, return true if 
	 * LifeProductExtension.LifeExtension.PremType equals 2.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is Flexible Premium.
	 */
	// ACN012 changed signature
	protected boolean filter_Flex(ValProc nbaConfigValProc, ArrayList objects) {
		if (getAnnuity() != null) {
			AnnuityProduct annuityProduct = getAnnuityProductForPlan();
			if (annuityProduct != null && annuityProduct.hasPremType()) {
				return annuityProduct.getPremType() == OLI_ANNPREM_FLEX;
			} else if (getAnnuity().hasPremType()) {
				return getAnnuity().getPremType() == OLI_ANNPREM_FLEX;
			}
		} else if (getLife() != null) {
			LifeProductExtension lifeProductExtension = getLifeProductExtensionForPlan();
			if (lifeProductExtension != null && lifeProductExtension.hasPremType()) {
				return lifeProductExtension.getPremType() == OLI_ANNPREM_FLEX;
			} else if (getLifeExtension().hasPremType()) {
				return getLifeExtension().getPremType() == OLI_ANNPREM_FLEX;
			}
		}
		return false;
	}
	/**
	 * Filter for Fixed Premium contract. For Annuity contracts, return true if 
	 * AnnuityProduct.PremType equals 3.  For Life contracts, return true if 
	 * LifeProductExtension.LifeExtension.PremType equals 3.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is Flexible Premium.
	 */
	// ACN012 changed signature
	protected boolean filter_Fixed(ValProc nbaConfigValProc, ArrayList objects) {
		if (getAnnuity() != null) {
			AnnuityProduct annuityProduct = getAnnuityProductForPlan();
			if (annuityProduct != null && annuityProduct.hasPremType()) {
				return annuityProduct.getPremType() == OLI_ANNPREM_FIXED;
			} else if (getAnnuity().hasPremType()) {
				return getAnnuity().getPremType() == OLI_ANNPREM_FIXED;
			}
		} else if (getLife() != null) {
			LifeProductExtension lifeProductExtension = getLifeProductExtensionForPlan();
			if (lifeProductExtension != null && lifeProductExtension.hasPremType()) {
				return lifeProductExtension.getPremType() == OLI_ANNPREM_FIXED;
			} else if (getLifeExtension().hasPremType()) {
				return getLifeExtension().getPremType() == OLI_ANNPREM_FIXED;
			}
		}
		return false;
	}
	/**
	 * Filter for JtFirst Coverage. Returns true if LivesType is Joint First To Die.
	 * Holding.Policy.Life.Coverage.LivesType 
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is Joint First To Die.
	 */
	// NBA104 New Method
	// ACN012 changed signature	
	protected boolean filter_JtFirst(ValProc nbaConfigValProc, ArrayList objects) {
		if (getCoverage() != null) {
			CoverageProduct coverageProduct = getCoverageProductFor(getCoverage());
			if (coverageProduct != null && coverageProduct.hasLivesType()) {
				return coverageProduct.getLivesType() == OLI_COVLIVES_JOINTFTD;
			} else if (getCoverage().hasLivesType()) {
				return getCoverage().getLivesType() == OLI_COVLIVES_JOINTFTD;
			}
		}
		return false;
	}
	
	/**
	 * Filter for Insured.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the object  is an Insured Person.
	 */
	// ACN012 changed signature
	protected boolean filter_Ins(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Participant) {
				return NbaUtils.isInsuredParticipant((Participant) objects.get(i));
			} else if (objects.get(i) instanceof LifeParticipant) {
				return NbaUtils.isInsuredParticipant((LifeParticipant) objects.get(i));
			} else if (objects.get(i) instanceof Relation) {
				return getNbaTXLife().isInsurableRelation((Relation) objects.get(i)); //SPR1747
			} else if (objects.get(i) instanceof Party) {
				return isInsurable(getParty().getId());
			}
		}
		return false;
	}
	/**
	 * Filter for Annuitant.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the object is an Annuitant.
	 */
	//SPR1705 new method
	// ACN012 changed signature
	protected boolean filter_Annuit(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Participant) {
				return NbaUtils.isAnnuitantParticipant((Participant) objects.get(i));
			} else if (objects.get(i) instanceof Relation) {
				return NbaUtils.isAnnuitantRelation((Relation) objects.get(i));
			} else if (objects.get(i) instanceof Party) {
				return isAnnuitant(getParty().getId());
			}
		}
		return false;
	}
	/**
	 * Filter for Life contract.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is a Life contract.
	 */
	// ACN012 changed signature
	protected boolean filter_Life(ValProc nbaConfigValProc, ArrayList objects) {
		return getLife() != null;
	}
	/**
	 * Filter for Owner Person.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	// ACN012 changed signature
	protected boolean filter_Own(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return getRelation() == getOwnerRelation();
			} else if (objects.get(i) instanceof Party) {
				return isOwner(getParty().getId());
			}else if (objects.get(i) instanceof SignatureInfo) {  // AXAL3.7.40
				return isOwnerSignature(getSignatureInfo());
			}else if (objects.get(i) instanceof TempInsAgreementDetails) {//A4_AXAL001
				return isOwner(getTempInsAgreementDetails().getPartyID());
			}
		}
		return false;
	}
	/**
	 * Filter for primary Annuitant Person.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	// ACN012 changed signature
	protected boolean filter_Pann(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Participant) {
				return NbaUtils.isAnnuitantParticipant((Participant) objects.get(i));
			} else if (objects.get(i) instanceof Relation) {
				return (Relation) objects.get(i) == getAnnuitantRelation();
			} else if (objects.get(i) instanceof Party) {
				return isPrimaryAnnuitant(getParty().getId());
			}
		}
		return false;
	}
	/**
	 * Filter for Payor Person.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	// ACN012 changed signature
	protected boolean filter_Pay(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return getRelation() == getPayerRelation();
			} else if (objects.get(i) instanceof Party) {
				String orgObjID = getNbaTXLife().getPrimaryHolding() != null ? getNbaTXLife().getPrimaryHolding().getId() : null; //APSL3351
				return isPayor(getParty().getId(),orgObjID);
			}
		}
		return false;
	}
	
	/**
	 * Filter for fetching the Ach Payor Party. 
	 * @param nbaConfigValProc
	 * @param objects
	 * @return
	 */
	// Begin APSL3351
	protected boolean filter_SysPay(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return getRelation() == getAchPayerRelation(); // APSL3431
			} else if (objects.get(i) instanceof Party) {
				String orgObjID = getNbaTXLife().getAchHolding() != null ? getNbaTXLife().getAchHolding().getId() : null;
				if (orgObjID == null) {   // APSL3431
					return false;        //  APSL3431 
				} else {
					return isPayor(getParty().getId(), orgObjID);
				}
			}
		}
		return false;
	}
	// End APSL3351
	
	/**
	 * Filter for primary Insured Person.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	// ACN012 changed signature
	protected boolean filter_Pins(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Participant) {
				return NbaUtils.isPrimaryInsuredParticipant((Participant) objects.get(i));// AXAL3.7.40
			} else if (objects.get(i) instanceof LifeParticipant) {
				return NbaUtils.isPrimaryInsuredParticipant((LifeParticipant) objects.get(i));// AXAL3.7.40
			} else if (objects.get(i) instanceof Relation) {
				return getRelation() == getPrimaryInsRelation();
			} else if (objects.get(i) instanceof Party) {
				return isPrimaryInsured(getParty().getId());
			} else if (objects.get(i) instanceof SignatureInfo) { // AXAL3.7.40
				return isPrimaryInsured(getSignatureInfo());// AXAL3.7.40 //P2AXAL054
			} else if (objects.get(i) instanceof Intent) { //P2AXAL021
				return isPrimaryInsured(getIntent().getPartyID()); //P2AXAL021
			} else if (objects.get(i) instanceof TempInsAgreementDetails) { //A4_AXAL001
				return isPrimaryInsured(getTempInsAgreementDetails().getPartyID());
			}
		}
		return false;
	}
	/**
	 * Filter for Performance Plus agent system.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	// ACN012 changed signature
	protected boolean filter_PPlus(ValProc nbaConfigValProc, ArrayList objects) {
		return getNbaTXLife().isAgentSystemPERF();
	}
	/**
	 * Filter for Primary Coverage.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the coverage is the primary coverage.
	 */
	// ACN012 changed signature
	protected boolean filter_PrimeCov(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Coverage) {
				Coverage coverage = (Coverage) objects.get(i);
				return coverage.getIndicatorCode() == OLI_COVIND_BASE;
			}
		}
		return false;
	}
	/**
	 * Filter for Submitted Application.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the application is the new or a conversion
	 */
	// ACN012 changed signature, ALII1206
	protected boolean filter_SubApp(ValProc nbaConfigValProc, ArrayList objects) {
        // begin NBA104
        if (getNbaDst().getNbaLob().getContractChgType() == null && getApplicationInfo() != null) { //SPR3117
            return true; //NBA211, P2AXAL048
        }
        return false;
        // end NBA104
    }
	/**
	 * Filter for a Substandard Rating on a Coverage.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the coverage has a SubStandardRating.
	 */
	// ACN012 changed signature
	protected boolean filter_SubstCov(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Coverage) {
				return NbaUtils.isRated((Coverage) objects.get(i)); //SPR3098
			}
		}
		return false;
	}
	/**
	 * Filter for a Substandard Rating on a CovOption.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the CovOption is rated.
	 */
	// ACN012 changed signature
	protected boolean filter_SubstCovopt(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				return isRated((CovOption) objects.get(i));
			}
		}
		return false;
	}
	/**
	 * Filter for Traditional Life contract.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is a Traditional Life contract.
	 */
	// ACN012 changed signature
	protected boolean filter_Trad(ValProc nbaConfigValProc, ArrayList objects) {
		if (getProductTypeString().length() > 0) {
			for (int i = 0; i < NbaConstants.PRODUCT_TYPE_TRADITIONAL.length; i++) {
				if (NbaConstants.PRODUCT_TYPE_TRADITIONAL[i].equals(getProductTypeString())) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Filter for a contract which allows Variable funds. Return true if 
	 * Policy.ProductType is 4, 8 or 10.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract allows Variable funds.
	 */
	// ACN012 changed signature
	protected boolean filter_VAR(ValProc nbaConfigValProc, ArrayList objects) {
		if (getPolicy().hasProductType()) {
			long productType = getPolicy().getProductType();
			return productType == OLI_PRODTYPE_VUL || productType == OLI_PRODTYPE_VWL || productType == OLI_PRODTYPE_VAR;
		} else {
			return false;
		}
	}
	
	/**
	 * @param nbaConfigValProc
	 * @param objects
	 * @return
	 */
	//ALS3346 New method to identify a TERM plan 
	protected boolean filter_TERM(ValProc nbaConfigValProc, ArrayList objects) {
		return getNbaTXLife().isTermLife();
	}	

	/**
	 * @param nbaConfigValProc
	 * @param objects
	 * @return
	 */
	//ALS3346 New method to identify a TERM plan 
	protected boolean filter_TERMONE(ValProc nbaConfigValProc, ArrayList objects) {
		return getNbaTXLife().isTermOneLife();
	}	
	/**
	 * Filter for Vantage.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is Vantage.
	 */
	// ACN012 changed signature
	protected boolean filter_Vntg(ValProc nbaConfigValProc, ArrayList objects) {
		return isSystemIdVantage();
	}
	/**
	 * Filter for Writing Agent.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	// ACN012 changed signature
	protected boolean filter_Wriagt(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return NbaUtils.isPrimaryWritingAgentRelation((Relation) objects.get(i));
			} else if (objects.get(i) instanceof Party) {
				return isWritingAgent(getParty().getId());
			}
		}
		return false;
	}
	/**
	 * Filter for Agent.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	//SPR1705 new method
	// ACN012 changed signature
	protected boolean filter_Agt(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return NbaUtils.isAgentRelation((Relation) objects.get(i));
			} else if (objects.get(i) instanceof Party) {
				return isAgent(getParty().getId());
			}
		}
		return false;
	}
	
	/**
	 * Filter for NCF party.
	 * 
	 * @param nbaConfigValProc
	 *            the configuration information for a validation process
	 * @param objects
	 *            an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return
	 */
	// NBLXA1850 new method

	protected boolean filter_Ncf(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
			if (objects.get(i) instanceof Relation) {
				return NbaUtils.isNCFRelation((Relation) objects.get(i));
			} else if (objects.get(i) instanceof Party) {
				return isNCFParty(getParty().getId());
			}
		}
		return false;
	}
	
	/**
	 * Filter for servicing agent and servicing agency relations.
	 * @param nbaConfigValProc
	 * @param objects
	 * @return
	 */
	// NBA112 New Method
	// ACN012 changed signature	
	protected boolean filter_SvcAgt(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return (
					NbaUtils.isServicingAgentRelation((Relation) objects.get(i)) || NbaUtils.isServicingAgencyRelation((Relation) objects.get(i)));
			} else if (objects.get(i) instanceof Party) {
				return isServicingAgent(getParty().getId());
			}
		}
		return false;
	}
	//ALS5383
	protected boolean filter_CASE(ValProc nbaConfigValProc, ArrayList objects) {
		return getNbaDst().isCase() ;
	}
	/**
	 * 
	 * Return the loop start value for the item at the bottom of the stack.
	 * 
	 */
	protected int findLoopBegin() {
		return getObjectStackBottom().getStart();
	}
	/**
	 * Return the loop end value for the item at the bottom of the stack.
	 * 
	 */
	protected int findLoopEnd() {
		if (getobjectStack().size() > 0) {
			return getObjectStackBottom().getEnd() + 1;
		} else {
			return -1;
		}
	}
	/**
	 * Determine if another occurrence is present for the object
	 * @param processEntry the ObjectEntry referencing the object
	 */
	protected boolean finishedWithObject(ObjectEntry processEntry) {
		int seq = processEntry.getSequence();
		processEntry.setSequence(++seq);
		return !hasOccurrence(processEntry.getObjectID(), seq);
	}
	/**
	 * Answer the business process.
	 */
	protected String getBusinessProcess() {
		return getNbaTXLife().getBusinessProcess();
	}
	protected String getCtlAt(int idx) {
		if (idx < getProcs().size()) {	//ACN012
			String ctl = ((ValProc) getProcs().get(idx)).getCtl();//ACN012
			if (ctl.length() < 1) {//ACN012
				ctl = NO_ID;//ACN012
			}//ACN012
			return ctl.trim(); //ACN012
		}
		return "FINISHED";
	}
	/**
	 * Returns the formattedDate.
	 * @return String
	 */
	protected String getFormattedDate() {
		return formattedDate;
	}
	/**
	 * Returns the msgCount.
	 * @return int
	 */
	protected int getMsgCount() {
		return ++msgCount;
	}
	/**
	 * Get the NbaPlansRidersData for the current coverage.
	 * @param currentDate The currentDate to set
	 */
	protected NbaTableData getNbaAllowableRiders(String aCoverageKey) throws NbaBaseException {
		Map tblKeys = getNbaTableAccessor().setupTableMap(getNbaDst());
		NbaTableData nbaTableData;
		nbaTableData = getNbaTableAccessor().getDataForOlifeValue(tblKeys, NBA_ALLOWABLE_RIDERS, aCoverageKey);
		if (nbaTableData == null) {
			tblKeys.put(C_COVERAGE_KEY, "*");
			//See if Rider is applicable to all plans
			nbaTableData = getNbaTableAccessor().getDataForOlifeValue(tblKeys, NBA_ALLOWABLE_RIDERS, aCoverageKey);
		}
		return nbaTableData;
	}
	/**
	 * Returns the nbaContractValidationImpl.
	 * @return NbaContractValidationImpl
	 */
	protected NbaContractValidationImpl getNbaContractValidationImpl() {
		return nbaContractValidationImpl;
	}
	/**
	 * Get the NBA_PLANS for the current plan.
	 * @param currentDate The currentDate to set
	 */
	protected NbaPlansData getNbaPlansData() throws NbaBaseException {
		Map tblKeys = getNbaTableAccessor().setupTableMap(getNbaDst());
		return getNbaTableAccessor().getPlanData(tblKeys);
	}
	//NBA213 deleted code
	// ACN012 changed signature
	protected ArrayList getObjectsFor(ValProc nbaConfigValProc) {
		ArrayList objects = new ArrayList();
		String ctlString = nbaConfigValProc.getCtl().trim();	//ACN012
		int fromIndex = 0;
		String objectID;
		while (fromIndex < ctlString.length()) {
			int toIndex = ctlString.indexOf(".", fromIndex);
			if (toIndex < 0) {
				toIndex = ctlString.length();
			}
			objectID = ctlString.substring(fromIndex, toIndex); //The string for the object, i.e. COV
			// ACN012 code deleted
			Object controlObject = getControlObject(objectID);//NBA297
			if (controlObject!=null) {//NBA297
				objects.add(controlObject);//NBA297
			}else {
				addNewSystemMessage(
					VALIDATION_PROCESSING,
					concat("Process ", nbaConfigValProc.getId(), ":, Unknown objectID: ", objectID),
					"");
			}
			fromIndex = toIndex + 1;
		}
		return objects;
	}
	/**
	 * Returns the objectStack.
	 * @return ArrayList
							 */
	protected ArrayList getobjectStack() {
		return objectStack;
	}
	/**
	 * Return the ObjectEntry item at the specified location.
	 * 
	 */
	protected ObjectEntry getObjectStackAt(int idx) {
		return (ObjectEntry) getobjectStack().get(idx);
	}
	/**
	 * Return the ObjectEntry item at the bottom of the stack.
	 * 
	 */
	protected ObjectEntry getObjectStackBottom() {
		if (getobjectStack().size() > 0) {
			return getObjectStackAt(getobjectStack().size() - 1);
		} else {
			return null;
		}
	}
	/**
	 * Return the ObjectEntry item at the bottom of the stack.
	 * 
	 */
	protected ObjectEntry getObjectStackPrevious() {
		if (getobjectStack().size() > 0) {
			return getObjectStackAt(getobjectStack().size() - 1);
		} else {
			return null;
		}
	}
	/**
	 * Returns the procs.
	 * @return Object[]
	 */
	// ACN012 changed signature
	protected ArrayList getProcs() {
		return procs;
	}
	/**
	 * Answer the string representing the policy's product type.
	 * @return the product type
	 */
	protected String getProductTypeString() {
		return Long.toString(getHolding().getPolicy().getProductType());
	}
	/**
	 * Answer the validation subsets for the current business process.
	 */
	protected ArrayList getValidationSubsets() {
		if (getBusinessProcess().length() < 1) {
			addNewSystemMessage(VALIDATION_PROCESSING, "Business Function not specified in NbaTXLife", "");
			return new ArrayList();
		} else {
			try {
				return (NbaConfiguration.getInstance().getBusValidation(getBusinessProcess().toUpperCase())).getSubset(); //ACN012
			} catch (NbaBaseException e) {
				addNewSystemMessage(VALIDATION_PROCESSING, "Configuration BusValidation entry missing for " + getBusinessProcess(), "");
				return new ArrayList();
			}
		}
	}
	/**
	 * Answer the validation subsets for the current business process.
	 */
	//ACN012 New Method
	protected BusValidation getBusinessValidation() {
		if (getBusinessProcess().length() < 1) {
			addNewSystemMessage(VALIDATION_PROCESSING, "Business Function not specified in NbaTXLife", "");
			return null;
		} else {
			try {
				return NbaConfiguration.getInstance().getBusValidation(getBusinessProcess().toUpperCase()); //ACN012
			} catch (NbaBaseException e) {
				addNewSystemMessage(VALIDATION_PROCESSING, "Configuration BusValidation entry missing for " + getBusinessProcess(), "");
				return null;
			}
		}
	}
	/**
	 * Determine if there is a participant with the specified role on an Annuity.
	 * @return true if there is a participant with the specified role
	 */
	protected boolean hasAnnuityRole(String[] roles) {
		if (getAnnuity() != null) {
			if (!getAnnuity().isActionDelete()) {
				int payoutCount = getAnnuity().getPayoutCount();
				for (int i = 0; i < payoutCount; i++) {
					Payout payout = getAnnuity().getPayoutAt(i);
					if (!payout.isActionDelete()) {
						if (roleMatch(participant.getParticipantRoleCode(), roles)) {
							return true;
						}
					}
				}
				return hasAnnuityRiderRole(roles);
			}
		}
		return hasAnnuityRiderRole(roles);
	}
	/**
	 * Returns true if there is a coverage or rider with a beneficiary.
	 * @return Life
	 */
	protected boolean hasBeneficiary() {
		return hasRole(NbaConstants.BENEFICIARY_PARTICROLES);
	}
	/**
	 * Returns true if there is a coverage for a person with the specified role.
	 * @return Life
	 */
	protected boolean hasLifeRole(String[] roles) {
		boolean roleFound = false;
		Life life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
		if (!life.isActionDelete()) {
			int coverageCount = getLife().getCoverageCount();
			mainLoop : for (int covIdx = 0; covIdx < coverageCount; covIdx++) {
				Coverage coverage = getLife().getCoverageAt(covIdx);
				if (!coverage.isActionDelete()) {
					int lifeParticipantCount = coverage.getLifeParticipantCount();
					for (int partIdx = 0; partIdx < lifeParticipantCount; partIdx++) {
						LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(partIdx);
						if (!lifeParticipant.isActionDelete()) {
							if (roleMatch(lifeParticipant.getLifeParticipantRoleCode(), roles)) {
								roleFound = true;
								break mainLoop;
							}
						}
					}
				}
			}
		}
		return roleFound;
	}
	/**
	* Determine if the occurrence of the object exists and create a pointer to it.
	* @param objectID the identifier for the object
	* @param occurrence the occurrence to search for
	*/
	protected boolean hasOccurrence(String objectID, int occurrence) {
		if (objectID.equals(NO_ID)) {
			return occurrence < 1;
		} else if (objectID.equals(ADDRESS)) {
			return findNextAddress(occurrence);
		} else if (objectID.equals(ANNUITY)) {
			return findNextAnnuity(occurrence);
		} else if (objectID.equals(APPLICATIONINFO)) {
			return findNextApplicationInfo(occurrence);
		} else if (objectID.equals(ARRANGEMENT)) {
			return findNextArrangement(occurrence);
		} else if (objectID.equals(ARRDESTINATION)) {
			return findNextArrDestination(occurrence);
		} else if (objectID.equals(ARRSOURCE)) {
			return findNextArrSource(occurrence);
		} else if (objectID.equals(BANKING)) {
			return findNextBanking(occurrence);
		} else if (objectID.equals(CARRIERAPPOINTMENT)) {
			return findNextCarrierAppointment(occurrence);
		} else if (objectID.equals(COVERAGE)) {
			return findNextCoverage(occurrence);
		} else if (objectID.equals(COVOPTION)) {
			return findNextCovOption(occurrence);
		} else if (objectID.equals(FINANCIALACTIVITY)) {
			return findNextFinancialActivity(occurrence);
		} else if (objectID.equals(HOLDING)) {
			return findNextHolding(occurrence);
		} else if (objectID.equals(INVESTMENT)) {
			return findNextInvestment(occurrence);
		} else if (objectID.equals(LIFE)) {
			return findNextLife(occurrence);
		} else if (objectID.equals(LIFEPARTICIPANT)) {
			return findNextLifeParticipant(occurrence);
		} else if (objectID.equals(ORGANIZATION)) {
			return findNextOrganization(occurrence);
		} else if (objectID.equals(PARTICIPANT)) {
			return findNextParticipant(occurrence);
		} else if (objectID.equals(PARTY)) {
			return findNextParty(occurrence);
		} else if (objectID.equals(PAYOUT)) {
			return findNextPayout(occurrence);
		} else if (objectID.equals(PERSON)) {
			return findNextPerson(occurrence);
		} else if (objectID.equals(POLICY)) {
			return findNextPolicy(occurrence);
		} else if (objectID.equals(PRODUCER)) {
			return findNextProducer(occurrence);
		} else if (objectID.equals(RIDER)) {
			return findNextRider(occurrence);
		} else if (objectID.equals(RELATION)) {
			return findNextRelation(occurrence);
		} else if (objectID.equals(REQUIREMENTINFO)) {
			return findNextRequirementInfo(occurrence);
		} else if (objectID.equals(RISK)) {
			return findNextRisk(occurrence);
		} else if (objectID.equals(SUBACCOUNT)) {
			return findNextSubAccount(occurrence);
		} else if (objectID.equals(SUBSTANDARDRATING)) {
			return findNextSubstandardRating(occurrence);
		} else if (objectID.equals(TAXWITHHOLDING)) {
			return findNextTaxWithholding(occurrence);
		}else if (objectID.equals(SIGNATUREINFO)) {		//AXAL3.7.40
			return findNextSignatureInfo(occurrence);		
		}else if (objectID.equals(APPSIGNATUREINFO)) {	//AXAL3.7.40
			return findNextAppSignatureInfo(occurrence);		
		}else if (objectID.equals(FORMINSTANCE)) {		//AXAL3.7.40
			return findNextFormInstance(occurrence);
		}else if (objectID.equals(HHFAMILYINSURANCE)) {		//AXAL3.7.40
			return findNextHHFamilyInsurance(occurrence);
		}else if (objectID.equals(BANKHOLDING)) {		//ALS3600
			return findNextBankHolding(occurrence); //ALS3600
		}else if (objectID.equals(FORMRESPONSE)) {		//P2AXAL004
			return findNextFormResponse(occurrence); //P2AXAL004
		}
		//Begin NBA297
		else if (objectID.equals(EMPLOYMENT)) {		
			return findNextEmployment(occurrence); 
		}else if (objectID.equals(FINANCIALEXPERIENCE)) {		
			return findNextFinancialExperience(occurrence); 
		}else if (objectID.equals(INTENT)) {		
			return findNextIntent(occurrence); 
		}else if (objectID.equals(SYSTEMMESSAGE)) {		
			return findNextSystemMessage(occurrence); 
		}		
		//End NBA297
		//Begin P2AXAL021
		else if (objectID.equals(CLIENT)) {		
			return findNextClient(occurrence); 
		}else if (objectID.equals(SUITABILITYDETAILSCC)) {		
			return findNextSuitabilityDetailsCC(occurrence); 
		}//End P2AXAL021
		else if (objectID.equals(TEMPINSAGREEMENTDETAILS)) {//	A4_AXAL001
			return findNextTempInsAgreementDetails(occurrence);
		}
		else if (objectID.equals(LIFEUSA)) {	//Begin CR1346709	
			return findNextLifeUSA(occurrence); 
		}// End CR1346709
		//End P2AXAL021		
		addNewSystemMessage(INVALID_CTL_ID, concat("Process ", getNbaConfigValProc().getId(), " control value of: ", objectID), "");
		return false;
	}
	/**
	 * Returns true if there is a coverage or rider with a beneficiary.
	 * 
	 * @return Life
	 */
	protected boolean hasRole(String[] roles) {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty =
			getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (lifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty != null) {
			if (lifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty.isLife()) {
				return hasLifeRole(roles);
			} else if (lifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty.isAnnuity()) {
				return hasAnnuityRole(roles);
			}
		}
		return false;
	}
	/**
	 * If the processing entry passes the filter edits, perform it.
	 * @param nbaConfigValProc the configuration information for a validation process
	 */
	// ACN012 changed signature
	protected void invokeValidationFor(ValProc nbaConfigValProc) {
		logDebug("** Attempting Process " + nbaConfigValProc.getId() + " for subset " + getCurrentSubSet());//NBA103
		ArrayList objects = getObjectsFor(nbaConfigValProc);
		//BEGIN NBLXA-2527
		String field = nbaConfigValProc.getField();
		boolean ignoreObject = false;
		if (!NbaUtils.isBlankOrNull(field) && LifeCovOptTypeCode.equalsIgnoreCase(field) && filter_MAY2019EFFDATE(nbaConfigValProc, objects)) {
			ignoreObject = false;
		} else {
			ignoreObject = isIgnoredObjectPresent(objects);
		}
		//END NBLXA-2527
		try {
			if (!deletedObjectsPresent(objects) && !ignoreObject && passedFilter(nbaConfigValProc, objects)) { //SPR3098,NBLXA-2527
				logDebug("** Performing Process " + nbaConfigValProc.getId());//NBA103
				getNbaContractValidationImpl().validate(nbaConfigValProc, objects);
			}
		} catch (Throwable e) {
			addNewSystemMessage(VALIDATION_PROCESSING, concat("Process ", nbaConfigValProc.getId(), ": ", e.toString()), "");
		}
	}
	/**
	 * Returns the subSetComplete.
	 * @return boolean
	 */
	protected boolean isSubSetComplete() {
		return subSetComplete;
	}
	/**
	* Find the last occurrence of the set of ctl values in the process stack.
	* @param ctlVals the set of ctl tokens
	* @param current the starting location
	*/
	protected int lastOccurrence(String ctlVals, int current) {
		int last;
		int length = ctlVals.length();
		for (last = current; last < getProcs().size(); last++) { //ACN012
			String ctl = getCtlAt(last);
			if (ctl.length() < length || !ctlVals.equals(ctl.substring(0, length))) {
				break;
			}
		}
		return --last;
	}
	/**
	 * Determine if there are any deleted objects in the object array.
	 * @param objects
	 * @return true if there are
	 */
	protected boolean deletedObjectsPresent(ArrayList objects) {
		boolean deletedObjectsPresent = false;
		for (int i = 0; i < objects.size(); i++) {
			NbaContractVO nbaContractVO = (NbaContractVO) objects.get(i); //NBA234
			if (NbaUtils.isDeleted(nbaContractVO)) { //NBA234 //QC2724
				deletedObjectsPresent = true;
				break;
			}
		}
		return deletedObjectsPresent;
	}

	/**
	 * Determine if there are any ignored objects in the object array.
	 * @param objects the list of all participating objects
	 * @return true if there are any ignored object present
	 */
	//SPR3098 New Method
	protected boolean isIgnoredObjectPresent(List objects) {
        boolean ignoredObjectPresent = false;
        int size = objects.size();
        for (int i = 0; i < size; i++) {
            if (NbaUtils.isIgnoredObject(objects.get(i))) {//P2AXAL016 Method moved to NbaUtils.java
                ignoredObjectPresent = true;
                break;
            }
        }
        return ignoredObjectPresent;
    }

	//P2AXAL016 Method moved to NbaUtils.java
	

	
	/**
	 * Determine if the processing entry passes the filter edits.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 */
	// ACN012 changed signature
	protected boolean passedFilter(ValProc nbaConfigValProc, ArrayList objects) {
		String allFilters = nbaConfigValProc.getFilter();
		//Begin SR494086.2, ADC Retrofit
		for (int i = 0; i < defaultMinusFilters.length; i++) {
			if (allFilters != null && allFilters.indexOf(defaultMinusFilters[i]) == -1) {
				allFilters += ("-" + defaultMinusFilters[i]);
			}
		}
		//End SR494086.2
		int fromIndex = 0;
		int end = allFilters == null ? 0 : allFilters.length(); // ACN012
		StringBuffer filterBuff = new StringBuffer();
		String filterId;
		boolean testTrue;
		boolean passedFilterEdits = true;
		while (passedFilterEdits && fromIndex < end) {
			filterBuff.setLength(0);
			testTrue = allFilters.substring(fromIndex++, fromIndex).equals("+");
			while (fromIndex < end) {
				if (fromIndex == allFilters.length()) {
					break;
				} else {
					String nextChar = allFilters.substring(fromIndex, fromIndex + 1);
					if (nextChar.equals("+") || nextChar.equals("-")) {
						break;
					} else {
						filterBuff.append(allFilters.substring(fromIndex++, fromIndex));
					}
				}
			}
			filterId = filterBuff.toString();
			logDebug("filterId = " + filterId);
			if (getFilters().containsKey(filterId)) {
				Object[] args = new Object[2];
				args[0] = nbaConfigValProc;
				args[1] = objects;
				try {
					passedFilterEdits = ((Boolean) ((Method) getFilters().get(filterId)).invoke(this, args)).booleanValue();
					logDebug("passedFilterEdits = " + passedFilterEdits);
					if (!testTrue) {
						passedFilterEdits = !passedFilterEdits;
					}
				} catch (Throwable e) {
					addNewSystemMessage(FILTER_INVALID, concat("Exception performing filter: ", filterId, " ", e.toString()), "");
					//					logError("Error invoking filter " + filterId);
					passedFilterEdits = false;
				}
			} else if (getValRules().containsKey(filterId)) { // Begin NBA297
				try{
					passedFilterEdits = executeRule(filterId);
					logDebug("passedFilterEdits = " + passedFilterEdits);
					if (!testTrue) {
						passedFilterEdits = !passedFilterEdits;
					}
				}catch(Exception ex){
					addNewSystemMessage(FILTER_INVALID, concat("Exception performing filter: ", filterId, " ", ex.toString()), "");
					passedFilterEdits = false;
				}
			} else { //End NBA297
				addNewSystemMessage(FILTER_INVALID, concat("Process: ", nbaConfigValProc.getId(), ", Filter: ", filterId), "");
				//				logError("Invalid or unknown filter " + filterId);
				passedFilterEdits = false;
			}
		}
		logDebug("Passed filter edit = " + passedFilterEdits);//NBA103
		return passedFilterEdits;
	}
	/**
	 * Obtain the implementation object for the validation subset from the factory and retrieve the processing information from the configuration.
	 * Remove any non-overridden messages and perform validation for the subset.
	 * 
	 * @param subset
	 *            the id of the current subset
	 */
	protected void performValidation() {
		try {
			setNbaContractValidationImpl(NbaContractValidationImplFactory.getImplementation(nbaTXLife, getCurrentSubSet()));
			getNbaContractValidationImpl().initialze(getNbaDst(), getNbaTXLife(), getCurrentSubSet(), getNbaOLifEId(), getNbaProduct(), getUserVO()); //AXAL3.7.18
			NbaContractValidationImpl userImpl = ((NbaContractValidationBaseImpl) getNbaContractValidationImpl()).getUserImplementation();
			if (userImpl != null) {
				userImpl.initialze(getNbaDst(), getNbaTXLife(), getCurrentSubSet(), getNbaOLifEId(), getNbaProduct(), getUserVO()); //AXAL3.7.18
			}
			
			stripErrors();
			ValidationDef nbaConfigValidationDef = NbaConfiguration.getInstance().getValidationDef(getCurrentSubSet().intValue()); //ACN012
			if (nbaConfigValidationDef == null) {
				StringBuffer buf = new StringBuffer();
				buf.append("Validation implementation error: Subset ");
				buf.append(getCurrentSubSet());
				buf.append(" not defined");
				throw new NbaBaseException(buf.toString());
			} else {
				setProcs(NbaConfiguration.getInstance().getValProc(getCurrentSubSet().intValue())); //ACN012
				initTimer();
				performValidationProcs();
				logElapsedTime("Validation subset " + nbaConfigValidationDef.getSubset());
			}
		} catch (NbaBaseException e) {
			setCurrentSubSet(VALIDATION_PROCESS);
			postImplementationNotDefinedError(e);
		} catch (Throwable e) {
			addNewSystemMessage(VALIDATION_PROCESSING, e.toString(), "");
		}
	}
	/**
	 * Perform the validation processing defined in the configuration entries.
	 * Processing is controlled by an object stack. The object stack identifies 
	 * the type of object, its parent object(s), the range of entries from 
	 * the configuration that it is applicable to, and the sequence of the 
	 * current occurrence of the object.
	 */
	protected void performValidationProcs() {
		setSubSetComplete(false);
		int loopbegin = 0;
		int loopend = 1;
		int curr;
		String lastCtl = "";
		String nextCtl = "NONE";
		while (!isSubSetComplete()) {
			for (curr = loopbegin; curr < loopend; curr++) {
				nextCtl = getCtlAt(curr);
				if (nextCtl.equals(lastCtl)) { //last object is still relevant
					if (getObjectStackBottom() == null || nextCtl.equals(getObjectStackBottom().getCtlVals())) { //All objects found //ACN012
						invokeValidationFor((ValProc)getProcs().get(curr)); //validate //ACN012
					}
				} else {
					break;
				}
			}
			boolean repeat = adjustObjectStack(lastCtl, curr); //see if the current should be processed
			if (!isSubSetComplete()) {
				if (repeat) { //repeat processing for another occurrence of an object already in the stack
					loopbegin = findLoopBegin();
					logDebug("--Repeating " + getObjectStackBottom().getCtlVals());//NBA103
					loopend = findLoopEnd();
				} else if (getobjectStack().size() > 0 && getCtlAt(curr).equals(getObjectStackBottom().getCtlVals())) {
					lastCtl = getObjectStackBottom().getCtlVals();
					loopbegin = curr;
					loopend = findLoopEnd();
				} else {
					if (getobjectStack().size() > 0) {
						lastCtl = getObjectStackBottom().getCtlVals();
						loopbegin = ++curr;
					} else {
						adjustObjectStack("", curr);
						if (getobjectStack().size() < 1) {
							loopbegin = ++curr;
						} else {
							loopbegin = getObjectStackBottom().getStart();
							loopend = findLoopEnd();
							lastCtl = getCtlAt(loopbegin);
						}
					}
					nextCtl = getCtlAt(loopbegin);
				}
			}
		}
	}
	/**
	 * Remove the last entry from the Object Stack
	 */
	protected void popObjectStack() {
		if (getobjectStack().size() > 0) {
			logDebug("pop Object Stack for " + getObjectStackBottom().getObjectID());//NBA103
			getobjectStack().remove(getobjectStack().size() - 1);
		}
	}
	/**
	 * Post a severe error when the validation implementation for a subset 
	 * cannot be determined or performed.
	 */
	protected void postImplementationNotDefinedError(NbaBaseException e) {
		addNewSystemMessage(IMPL_NOT_DEFINED, e.getMessage(), "");
	}
	/**
	 * Returns true the role matches one in the array.
	 * @return Life
	 */
	protected boolean roleMatch(long role, long[] roles) {
		for (int i = 0; i < roles.length; i++) {
			if (role == roles[i]) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Sets the primary holding. Verify that the primary holding object is 
	 * present. Create one if not.
	 * @param holding the holding to set
	 * @return true if the primary holding object had to be created.
	 */
	protected boolean setAndVerifyHolding(Holding newHolding) {
		holding = newHolding;
		boolean errorFound = false;
		if (holding == null || holding.isActionDelete()) {
			holding = new Holding();
			getNbaOLifEId().setId(holding);
			holding.setActionAdd();
			getOLifE().addHolding(holding);
		}
		return errorFound;
	}
	/**
	 * Sets the oLifE. Verify that the OLifE object is present. Create one if not.
	 * @param oLifE The oLifE to set
	 * @return true if the OLifE object had to be created.
	 */
	protected boolean setAndVerifyOLifE(OLifE newOLifE) {
		oLifE = newOLifE;
		boolean errorFound = false;
		if (oLifE == null || oLifE.isActionDelete()) { //Initialize enough of the object graph to post SystemMessage objects.
			oLifE = new OLifE();
			oLifE.setActionAdd();
			UserAuthRequestAndTXLifeRequest request = getNbaTXLife().getTXLife().getUserAuthRequestAndTXLifeRequest();
			if (request != null) {
				ArrayList txRequestList = request.getTXLifeRequest();
				if ((txRequestList != null) && (txRequestList.size() > 0)) {
					TXLifeRequest txLifeRequest = (TXLifeRequest) txRequestList.get(0);
					if (txLifeRequest != null) {
						txLifeRequest.setOLifE(oLifE);
					}
				}
			} else {
				UserAuthResponseAndTXLifeResponseAndTXLifeNotify response =
					getNbaTXLife().getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
				if (response != null) {
					java.util.ArrayList txResponseList = response.getTXLifeResponse();
					if ((txResponseList != null) && (txResponseList.size() > 0)) {
						TXLifeResponse txLifeResponse = (TXLifeResponse) txResponseList.get(0);
						if (txLifeResponse != null) {
							txLifeResponse.setOLifE(oLifE);
						}
					}
				}
			}
			errorFound = true;
		}
		return errorFound;
	}
	/**
	 * Sets the policy. Verify that the Policy object is present. Create one if not.
	 * @param policy The policy to set
	 * @return true if the Policy object had to be created.
	 */
	protected boolean setAndVerifyPolicy(Policy newPolicy) {
		policy = newPolicy;
		boolean errorFound = false;
		if (policy == null || policy.isActionDelete()) {
			policy = new Policy();
			getNbaOLifEId().setId(policy);
			policy.setActionAdd();
			getHolding().setPolicy(policy);
			errorFound = true;
		}
		return errorFound;
	}
	/**
	 * Sets the applicationInfo.
	 * @param applicationInfo The applicationInfo to set
	 */
	protected void setApplicationInfo(ApplicationInfo applicationInfo) {
		this.applicationInfo = applicationInfo;
		setApplicationInfoExtension(null);
		if (applicationInfo != null) {
			int extensionCount = applicationInfo.getOLifEExtensionCount();
			for (int extIdx = 0; extIdx < extensionCount; extIdx++) {
				OLifEExtension oLifEExtension = applicationInfo.getOLifEExtensionAt(extIdx);
				if (oLifEExtension.isApplicationInfoExtension()) {
					setApplicationInfoExtension(oLifEExtension.getApplicationInfoExtension());
				}
			}
		}
	}
	/**
	 * Sets the formattedDate.
	 * @param formattedDate The formattedDate to set
	 */
	protected void setFormattedDate(Date aDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
		if (aDate != null) {
			this.formattedDate = formatter.format(aDate);
		} else
			this.formattedDate = "";
	}
	/**
	 * Sets the nbaContractValidationImpl.
	 * @param nbaContractValidationImpl The nbaContractValidationImpl to set
	 */
	protected void setNbaContractValidationImpl(NbaContractValidationImpl nbaContractValidationImpl) {
		this.nbaContractValidationImpl = nbaContractValidationImpl;
	}
	/**
	 * Sets the objectStack.
	 * @param objectStack The objectStack to set
	 */
	protected void setobjectStack(ArrayList objectStack) {
		this.objectStack = objectStack;
	}
	/**
	 * Sets the procs.
	 * @param procs The procs to set
	 */
	// ACN012 changed signature
	protected void setProcs(ArrayList procs) {
		this.procs = procs;
	}
	/**
	 * Sets the subSetComplete.
	 * @param subSetComplete The subSetComplete to set
	 */
	protected void setSubSetComplete(boolean subSetComplete) {
		this.subSetComplete = subSetComplete;
	}
	/**
	 * Remove any existing validation messages on the contract 
	 * for the current subset. 
	 */
	protected void stripErrors() {
		int SystemMessageCount = getHolding().getSystemMessageCount();
		int subset = getCurrentSubSet().intValue();
		for (int msgIdx = 0; msgIdx < SystemMessageCount; msgIdx++) {
			SystemMessage systemMessage = getHolding().getSystemMessageAt(msgIdx);
			SystemMessageExtension systemMessageExtension = NbaUtils.getFirstSystemMessageExtension(systemMessage);
			if (systemMessageExtension!=null && systemMessageExtension.getMsgValidationType() == subset && !systemMessageExtension.getMsgOverrideInd()) {  //APSL3688
				systemMessage.setActionDelete();
			}
		}
	}
	/**
	 * Validate the nbaTXLife. Determine if any validation subsets are applicable to
	 * the current business function and perform them.
	 * @param nbaTXLife the NbaTXLife object 
	 * @param nbaDst an NbaDst object containing information about the work item
	 * being processed
	 * @param nbaUserVO an NbaUserVO containing information about the requesting user 	
	 */
	public void validate(NbaTXLife nbaTXLife, NbaDst nbaDst, NbaUserVO nbaUserVO) throws NbaBaseException {
		try {
			setCurrentSubSet(VALIDATION_PROCESS);
			//Begin NBA254
			if(!nbaTXLife.getOLifE().getSourceInfo().hasCreationDate()){
				DateFormat df = new SimpleDateFormat ("yyyy-MM-dd");
				Date createDate = df.parse(nbaDst.getNbaLob().getCreateDate());
				nbaTXLife.getOLifE().getSourceInfo().setCreationDate(createDate); 
			}
			//End NBA254
			//Begin ALS2020
			NbaTime cTime= nbaTXLife.getOLifE().getSourceInfo().getCreationTime();
			boolean flag = false;
			if (cTime == null || (cTime !=null && cTime.getTime()== null) ) {
				flag = true;
			}
			if (flag ){
				DateFormat df = new SimpleDateFormat ("yyyy-MM-dd-HH.mm.ss");
				Date createDate = df.parse(nbaDst.getNbaLob().getCreateDate());
				nbaTXLife.getOLifE().getSourceInfo().setCreationTime(new NbaTime(createDate)); 
			}	
			//End ALS2020
			setNbaTXLife(nbaTXLife);
			setUserVO(nbaUserVO); //AXAL3.7.18
			NbaProductAccessFacadeBean nbaProductAccessFacade = new NbaProductAccessFacadeBean();  //NBA213
			if (nbaProductAccessFacade == null) {
				return;
			}
			//NBA213 deleted code
			//FNB020 code deleted
			//NBA213 deleted code
			//FNB020 code deleted
            //begin NBA237
            setCurrentSubSet(new Integer(0)); //Strip generic errors
            stripErrors();
            //end NBA237
			setFormattedDate(getCurrentDate());
			BusValidation busVal = getBusinessValidation(); //ACN012
			int subsetCount = busVal == null ? 0 : busVal.getSubsetCount(); //ACN012
			if (subsetCount > 0) {
				if (productRequired(getBusinessProcess().toUpperCase())) {  //ALII2041
					setNbaProduct(nbaProductAccessFacade.doProductInquiry(nbaTXLife));//FNB020  //ALII2041
				} else {
					setNbaProduct( new AccelProduct());//ALII2055-2
				}
				setNbaOLifEId(new NbaOLifEId(getNbaTXLife()));//FNB020
				if (!contractStructureProblem()) {
					setCurrentSubSet(new Integer(0)); //Strip generic errors
					if (getBusinessProcess().toUpperCase().equals(NbaConstants.PROC_APP_SUBMIT) || getBusinessProcess().toUpperCase().equals(NbaConstants.PROC_SI_APP_SUBMIT) || getBusinessProcess().toUpperCase().equals(NbaConstants.PROC_GI_APP_SUBMIT)) { //Handle copied contracts AXAL3.7.79, APSL2808
						getHolding().setSystemMessage(new ArrayList());
					} else {
						stripErrors();
					}
					setNbaDst(nbaDst);
					for (int i = 0; i < subsetCount; i++) {
						setCurrentSubSet(new Integer(busVal.getSubsetAt(i))); //Get subset number //ACN012
						if (getCurrentSubSet().intValue() < 900) { //Transaction validation is handled by NbaTransactionValidation 
							performValidation();
						}
					}
									}
			}
			// NBLXA-1782 Starts
			String PolicySubStatusInd = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConstants.POLICY_SUB_STATUS_IND);
			if (PolicySubStatusInd.equalsIgnoreCase(NbaConstants.TRUE_STR) && !nbaTXLife.isInformalApplication()) {
				setPolicySubStatus(nbaDst);
			}
			// NBLXA-1782 Ends
		} catch (Exception e) {
			addNewSystemMessage(VALIDATION_PROCESSING, concat("Catastrophic Error : ", e.toString()), "");
		}
		//Begin P2AXAL016 
		BusValidation busVal = getBusinessValidation();
		int subsetCount = busVal == null ? 0 : busVal.getSubsetCount();
		if (subsetCount > 0) {
			for (int i = 0; i < subsetCount; i++) {
				setCurrentSubSet(new Integer(busVal.getSubsetAt(i))); //Get subset number
				if (getCurrentSubSet().intValue() >= 900 && !NbaUtils.isGICase(nbaTXLife, nbaDst)) { // NBLXA-188
					NbaTransactionValidation transValidation = NbaTransactionValidationFactory.getTransactionValidatonImplementation(nbaTXLife,
							new Integer(getCurrentSubSet().intValue()));
					transValidation.validate(nbaTXLife, nbaDst, nbaUserVO, getNbaProduct());//P2AXAL016
				}
			}
		}
		//P2AXAL016 Ends
	}
	/**
	 * Filter for a Reissued contract.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract application type is reissue (ApplicationInfo.ApplicationType = 3)
	 */
	// SPR2251 New Method
	protected boolean filter_REISS(ValProc nbaConfigValProc, ArrayList objects) {
		return getNbaTXLife().isReissue();//ALII1206
	}
	/**
	 * Filter for a complex change contract.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract application type is complex change (ApplicationInfo.ApplicationType = 7)
	 */
	// SPR2251 New Method
	protected boolean filter_CMPLX(ValProc nbaConfigValProc, ArrayList objects) {
		return getApplicationInfo() != null && (OLI_APPTYPE_CHANGE == getApplicationInfo().getApplicationType());
	}
	/**
	 * Filter for a reinstatement contract.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract application type is reinstatement (ApplicationInfo.ApplicationType = 2)
	 */
	// SPR2251 New Method
	protected boolean filter_REIN(ValProc nbaConfigValProc, ArrayList objects) {
		return getApplicationInfo() != null && (OLI_APPTYPE_REINSTATEMENT == getApplicationInfo().getApplicationType());
	}
	/**
	 * Filter for a nba calculations.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true nba calculations are supported
	 * @throws NbaBaseException
	 */
	//NBA133 New Method
	protected boolean filter_nbACalcs(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return isNbaCalc();
	}

	/** Filter for an increase complex change.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the complex change type is increase (NbaLob().getContractChgType() = 1000500028)
	 */
	//SPR2359 New Method
	protected boolean filter_INCREASE(ValProc nbaConfigValProc, ArrayList objects) {
        return getNbaDst() != null
                && NbaOliConstants.NBA_CHNGTYPE_INCREASE == NbaUtils.convertStringToLong(getNbaDst().getNbaLob().getContractChgType());
    }
	/** Filter for an trial application
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the application type is trial (OLI_APPTYPE_TRIAL  1000500003) 
	 */
	//NBA187 New Method
	protected boolean filter_TRIAL(ValProc nbaConfigValProc, ArrayList objects) {
        if (getApplicationInfo() != null) {
            return getApplicationInfo().getApplicationType() == OLI_APPTYPE_TRIAL //NBA231
            || getApplicationInfo().getApplicationType() == OLI_APPTYPE_PRESALE; //NBA231;
        }
        return false;
    }
	/** Filter for Reg60 cases
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the application type is 1 and jurisdiction is 37 and ReplaceInd is true
	 */
	//NBA187 New Method
	protected boolean filter_REG60(ValProc nbaConfigValProc, ArrayList objects) {
		if (getApplicationInfo() != null && NbaUtils.isNewApplication(getApplicationInfo().getApplicationType()) 
				&& getApplicationInfo().getApplicationJurisdiction() == OLI_USA_NY 
				&& getApplicationInfo().getReplacementInd()) {//P2AXAL040 added check for apptype 4 and 20
			return true;
		}
		return false;
	}
	
    /**
	 * Filter for Beneficiary Person.
	 * 
	 * @param nbaConfigValProc
	 *            the configuration information for a validation process
	 * @param objects
	 *            an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is a Beneficiary.
	 */
	// AXAL3.7.40 New Method
	protected boolean filter_PrmBene(ValProc nbaConfigValProc, ArrayList objects) {
	    for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return isPrimaryBeneficiaryRelation(getRelation().getId());
			} else if (objects.get(i) instanceof Party) {
				return isBeneficiary(getParty().getId());
			}
		}
		return false;
	}
	/**
	 * Filter for Beneficiary Person.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is a Beneficiary.
	 */
	// AXAL3.7.40 New Method
	protected boolean filter_CntBene(ValProc nbaConfigValProc, ArrayList objects) {
	    for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return isContingentBeneficiaryRelation(getRelation().getId()) ;
			} else if (objects.get(i) instanceof Party) {
				return isContingentBeneficiary(getParty().getId());
			}
		}
		return false;
	}
	/**
	 * Filter for Primary Agent.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Person.
	 */
	// AXAL3.7.40 New Method
	protected boolean filter_PAgt(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return NbaUtils.isPrimaryAgentRelation((Relation) objects.get(i));
			} else if (objects.get(i) instanceof Party) {
				return isPrimaryAgent(getParty().getId());
			}else if (objects.get(i) instanceof SignatureInfo) { 
				return isPrimaryAgentSignature(getSignatureInfo());
			}
		}
		return false;
	}
	/**
	 * Filter for CAPS.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is CAPS.
	 */
   //AXAL3.7.40 New Method
	protected boolean filter_CAPS(ValProc nbaConfigValProc, ArrayList objects) {
		return isSystemIdCAPS();
	}
	/**
	 * Filter for LIFE70.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract is CAPS.
	 */
   //P2AXAL007 New Method
	protected boolean filter_LIFE70(ValProc nbaConfigValProc, ArrayList objects) {
		return isSystemIdLIFE70();
	}
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given FormName.
	 */
	// AXAL3.7.40 New Method
	protected boolean filter_MedSupp(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof FormInstance) {
				return isMedSuppForm(getFormInstance());
			}else if(objects.get(i) instanceof Party) {//Begin P2AXAL054
				FormInstance formInstance = NbaUtils.getFormInstanceForParty(nbaTXLife, FORM_NAME_MEDSUP, ((Party)objects.get(i)).getId());
				return formInstance != null && NbaUtils.isFormReceived(formInstance);
			}//End P2AXAL054
		}
		return false;
	}
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given CTR form.
	 */
	// AXAL3.7.40 New Method
	protected boolean filter_CTR(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof FormInstance) {
				return isCTRForm(getFormInstance());
			} else if (objects.get(i) instanceof Coverage) { //Begin ALII870
				Coverage coverage = (Coverage) objects.get(i);
				return coverage.getLifeCovTypeCode() == OLI_COVTYPE_CHILDTERM;
			} //End ALII870
		}
		return false;
	}
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given FRTS form.
	 */
	// AXAL3.7.40 New Method
	protected boolean filter_FRTS(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof FormInstance) {
				return isFRTSForm(getFormInstance());
			}else if(objects.get(i) instanceof Party) {//Begin P2AXAL054
				FormInstance formInstance = NbaUtils.getFormInstanceForParty(nbaTXLife, FORM_NAME_FRTS, ((Party)objects.get(i)).getId());
				return  formInstance!= null && NbaUtils.isFormReceived(formInstance);
			}//End P2AXAL054
		}
		return false;
	}
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given ReplNy form.
	 */
	// AXAL3.7.40 New Method
	protected boolean filter_ReplNy(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof FormInstance) {
				return isReplNyForm(getFormInstance());
			}
		}
		return false;
	}
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given OptBen form.
	 */
	// AXAL3.7.40 New Method
	protected boolean filter_OptBen(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof FormInstance) {
				return isOptBenForm(getFormInstance());
			}
		}
		return false;
	}
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given FinSuppII form.
	 */
	// AXAL3.7.40 New Method
	//Modified the Method for APSL3527
	protected boolean filter_FinSuppII(ValProc nbaConfigValProc, ArrayList objects) {
		double faceAmnt = getNbaTXLife().getFaceAmount();
		double totalFaceAmt = 0;
		boolean flag = false;
		if (!totalInternalRepAmtFlag) {
			calculateTotalInternalRepAmt();
		}
		totalFaceAmt = faceAmnt + totalInternalRepAmt;
		int age = getNbaTXLife().getPrimaryInuredLifeParticipant().getIssueAge();
		if (totalFaceAmt >= 2000000 && age >= 65) {
			for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
				if (objects.get(i) instanceof FormInstance) {
					flag = isFinSuppIIForm(getFormInstance());
				}
			}
		}
		return flag;
	}
	
	
	/**
	 * Add all the internal replacement policies amount.
	 *  
	 */
	//APSL3527: New Method
	protected void calculateTotalInternalRepAmt() {
		List relList = getNbaTXLife().getOLifE().getRelation();
		NbaParty insured = getNbaTXLife().getPrimaryParty();
		List hhFamilyList = insured.getRisk().getHHFamilyInsurance();
		int count = insured.getRisk().getHHFamilyInsuranceCount();
		for (int i = 0; i < count; i++) {
			HHFamilyInsurance hhFamilyIns = (HHFamilyInsurance) hhFamilyList.get(i);
			String hhFamilyInsId = hhFamilyIns.getId();
			for (int j = 0; j < relList.size(); j++) {
				Relation relation = (Relation) relList.get(j);
				if (relation != null && relation.getOriginatingObjectType() == NbaOliConstants.OLI_HHFAMILYINS
						&& relation.getOriginatingObjectID().equalsIgnoreCase(hhFamilyInsId)) {
					NbaParty companyParty = getNbaTXLife().getParty(relation.getRelatedObjectID());
					if (!NbaUtils.isBlankOrNull(companyParty) && (!NbaUtils.isBlankOrNull(companyParty.getParty().getPartyKey()))
							&& NbaUtils.isInternalReplacementCompany(companyParty.getParty().getPartyKey()) && hhFamilyIns.hasAppliedForInsAmt()
							&& !NbaUtils.isBlankOrNullNonZero(hhFamilyIns.getAppliedForInsAmt())) {
						totalInternalRepAmt += hhFamilyIns.getAppliedForInsAmt();

					}

				}
			}
		}
		totalInternalRepAmtFlag = true;
	}
	
	/**
	 * Filter for Policy on the basis of Payment Method
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the Payment Method = 26.
	 */
    //AXAL3.7.40 New Method
	protected boolean filter_PAC(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Policy) {
				return getPolicy() != null && getPolicy().getPaymentMethod() == OLI_PAYMETH_ETRANS;
			}
		}
		return false;
	}
	/**
	 * Filter for Insured Child.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Insured Child.
	 */
	//AXAL3.7.40
	protected boolean filter_Child(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Party) {
				return isChild(getParty().getId());
			} else if (objects.get(i) instanceof Relation) {
				return isChildRelation(getRelation().getId());
			} else if (objects.get(i) instanceof Participant) {
				return NbaUtils.isChildInsuredParticipant((Participant) objects.get(i));
			} else if (objects.get(i) instanceof LifeParticipant) {
				return NbaUtils.isChildInsuredParticipant((LifeParticipant) objects.get(i));
			}
		}
		return false;
	}
	/**
	 * Filter for a nba qual plan
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if qual plan
	 * @throws NbaBaseException
	 */
	//ALPC066 New Method
	protected boolean filter_QUAL(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Life) {
				return NbaUtils.isQualifiedPlan(getLife().getQualPlanType());
			}
		}
		return false;
	}
	
	/**
	 * Filter for CWA_Amount on the basis of is Amount is present on the case or not
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if Amount is present on the case.
	 */
    //ALS4098 New Method
	protected boolean filter_CWAM(ValProc nbaConfigValProc, ArrayList objects) {
		
		if(getApplicationInfo()!= null){
			if(getApplicationInfo().getCWAAmt()> 0){
				return true;
			}
		}
		return false;
	}

	/**
	 * Filter for Approval on the basis of Case is approved or not
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if Case is Approved.
	 */
    //ALS4098 New Method
	protected boolean filter_APL(ValProc nbaConfigValProc, ArrayList objects) {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if(appInfoExt != null){
			if(appInfoExt.getUnderwritingApproval() == NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER)
				return true;
			
		}
		return false;
	}

	//ALS4335 new filter
	protected boolean filter_CHICOV(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Coverage) {
				Coverage coverage = (Coverage) objects.get(i);
				return OLI_COVIND_RIDER == coverage.getIndicatorCode() && OLI_COVTYPE_CHILDTERM == coverage.getLifeCovTypeCode();
			}
		}
		return false;
	}
	
	//ALS4335 new filter
	protected boolean filter_DPWCOP(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				CovOption covOption = (CovOption) objects.get(i);
				return covOption.getLifeCovOptTypeCode() == OLI_COVTYPE_DREADDISEASE;
			}
		}
		return false;
	}
	
	//CR1346706/APSL2724 new filter
	protected boolean filter_CVPLUSR(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				CovOption covOption = (CovOption) objects.get(i);
				return covOption.getLifeCovOptTypeCode() == OLI_OPTTYPE_CVPLUS;
			}
		}
		return false;
	}
	
	//ALS5342 - New method
	protected boolean filter_POWN(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				NbaParty ownParty = getNbaTXLife().getParty(getRelation().getRelatedObjectID());
				return ownParty == null ? false : isPrimaryOwner(ownParty.getParty());
			} else if (objects.get(i) instanceof Party) {
				return isPrimaryOwner(getParty());
			} else if (objects.get(i) instanceof SignatureInfo) { // AXAL3.7.40
				return isOwnerSignature(getSignatureInfo());
			} else if (objects.get(i) instanceof Intent) { //P2AXAL021
				NbaParty ownParty = getNbaTXLife().getParty(getIntent().getPartyID());
				return ownParty == null ? false : isPrimaryOwner(ownParty.getParty());
			}
		}
		return false;
	}
	
	//ALS5248 New Method
	//Filter RCO returns true if a Party object is related with Holding with Holding Company relation, and the Holding object is indicates
	// replacement.
	protected boolean filter_RCO(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) {
			if (objects.get(i) instanceof Party) {
				return isReplacementParty(getParty().getId());
			}
		}
		return false;
	}
	//ALS5248 New Method
	//Filter RCO returns true if a Party object is related with Holding with Holding Company relation
	protected boolean filter_HCO(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) {
			if (objects.get(i) instanceof Party) {
				return hasHoldingCompanyRelation(getParty().getId());
			}
		}
		return false;
	}
	//ALS5042 New Method
	//Filter NEWAPP returns true if Application Type is New
	protected boolean filter_NEWAPP(ValProc nbaConfigValProc, ArrayList objects) {
		return getApplicationInfo().getApplicationType() == OLI_APPTYPE_NEW;
	}
	
	//P2AXAL027 New Method
	protected boolean filter_SCAD(ValProc nbaConfigValProc, ArrayList objects) {
	    for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return getRelation().getRelationRoleCode() == OLI_REL_ALTERNATEADDRESSEE;
			} else if (objects.get(i) instanceof Party) {
				return isPartyInRole(getParty().getId(), OLI_REL_ALTERNATEADDRESSEE);
			}
		}
		return false;
	}
	
	//P2AXAL027 New Method
	protected boolean filter_APCT(ValProc nbaConfigValProc, ArrayList objects) {
	    for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return getRelation().getRelationRoleCode() == OLI_REL_APPLICANT;
			} else if (objects.get(i) instanceof Party) {
				return isPartyInRole(getParty().getId(), OLI_REL_APPLICANT);
			}
		}
		return false;
	}
	
	//P2AXAL027
	protected boolean filter_UL(ValProc nbaConfigValProc, ArrayList objects) {
		if (getPolicy().hasProductType()) {
			long productType = getPolicy().getProductType();
			return productType == OLI_PRODTYPE_UL;
		} else {
			return false;
		}
	}
	
	/**
	 * Filter for a contract which allows VUL funds. Return true if 
	 * Policy.ProductType is 4 (VUL).
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract allows VUL funds.
	 */
	//P2AXAL027 New Method
	protected boolean filter_VUL(ValProc nbaConfigValProc, ArrayList objects) {
		if (getPolicy().hasProductType()) {
			long productType = getPolicy().getProductType();
			return productType == OLI_PRODTYPE_VUL;
		}
		return false;
	}
	
	/**
	 * Filter for a contract which allows IUL funds. Return true if 
	 * Policy.ProductType is 5.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the contract allows IUL funds.
	 */
	// P2AXAL027 New Method
	protected boolean filter_IUL(ValProc nbaConfigValProc, ArrayList objects) {
		if (getPolicy().hasProductType()) {
			long productType = getPolicy().getProductType();
			return productType == OLI_PRODTYPE_INDXUL;
		} else {
			return false;
		}
	}
	
	/**
	 * Filter for OLI_ARRTYPE_AA arrangement (ArrType="21")
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is .
	 */
	// P2AXAL027 New Method
	protected boolean filter_AA(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && (getArrangement().getArrType() == OLI_ARRTYPE_AA);
	}
	
	/**
	 * Filter for OLI_ARRTYPE_CHARGEDEDUCTION arrangement (ArrType="52") and ArrSubType=27
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is OLI_ARRTYPE_CHARGEDEDUCTION arrangement (ArrType="52") and ArrSubType=27 OLI_ARRSUBTYPE_FUNDBALFT.
	 */
	// P2AXAL027 New Method
	protected boolean filter_CDMSOCRA(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && (getArrangement().getArrType() == OLI_ARRTYPE_CHARGEDEDUCTION) && 
					NbaUtils.getFirstArrangementExtension(getArrangement())!=null && NbaUtils.getFirstArrangementExtension(getArrangement()).hasArrSubType() &&
					NbaUtils.getFirstArrangementExtension(getArrangement()).getArrSubType()==OLI_ARRSUBTYPE_FUNDBALFT;
	}
	/**
	 * Filter for OLI_ARRTYPE_CHARGEDEDUCTION arrangement (ArrType="52") and ArrSubType=BLANK
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is OLI_ARRTYPE_CHARGEDEDUCTION arrangement (ArrType="52") and ArrSubType=BLANK
	 */
	// P2AXAL027 New Method
	protected boolean filter_CD(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && (getArrangement().getArrType() == OLI_ARRTYPE_CHARGEDEDUCTION) //ALII2075
				&& NbaUtils.getFirstArrangementExtension(getArrangement()) != null
				&& NbaUtils.getFirstArrangementExtension(getArrangement()).hasArrSubType()
				&& NbaUtils.getFirstArrangementExtension(getArrangement()).getArrSubType() == OLI_ARRSUBTYPE_CDA;
	}

	/**
	 * Filter for OLI_ARRTYPE_STANDINGALLOC arrangement (ArrType="37").
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is OLI_ARRTYPE_STANDINGALLOC arrangement (ArrType="37").
	 */
	//P2AXAL027 New Method 
	protected boolean filter_SA(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && (getArrangement().getArrType() == OLI_ARRTYPE_STANDINGALLOC);
	}	
	
	/**
	 * Filter for Distribution Channel is Retail (10).
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the Distribution Channel is Retail (10).
	 */
	// P2AXAL027 New Method
	protected boolean filter_RETAIL(ValProc nbaConfigValProc, ArrayList objects) {
		return NbaUtils.isRetail(getPolicy());
	}
	
	/**
	 * Filter for Distribution Channel is WholeSale (6).
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the Distribution Channel is WholeSale (6).
	 */
	// P2AXAL007 New Method
	protected boolean filter_WholeSale(ValProc nbaConfigValProc, ArrayList objects) {
		return NbaUtils.isWholeSale(getPolicy());
	}
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given VULSupp form.
	 */
	// P2AXAL027 New Method
	protected boolean filter_VULSUPP(ValProc nbaConfigValProc, ArrayList objects) {
		return isVULSuppForm(getFormInstance());
	}
	
	/**
	 * Filter for Asset Reallocation arrangement (ProductCode ="WS0").
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is Asset Reallocation.
	 */
	//P2AXAL027 New Method, ALII783 
	protected boolean filter_WSO(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && getArrangement().getArrSource()!= null && getArrSourceBySubAcctProdCode(getArrangement(),SNP3Y_PRODUCT_CODE) != null;
	}
	/**
	 * Filter for Asset Reallocation arrangement (ProductCode ="WP0").
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is Asset Reallocation(ProductCode ="WP0").
	 */
	// P2AXAL027 New Method, ALII783  
	protected boolean filter_WPO(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && getArrangement().getArrSource()!= null && getArrSourceBySubAcctProdCode(getArrangement(),SNP_PRODUCT_CODE) != null;
	}
	/**
	 * Filter for Asset Reallocation arrangement (ProductCode ="WR0").
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is Asset Reallocation(ProductCode ="WR0").
	 */
	// P2AXAL027 New Method, ALII783  
	protected boolean filter_WRO(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && getArrangement().getArrSource()!= null && getArrSourceBySubAcctProdCode(getArrangement(),RUSSELL_PRODUCT_CODE) != null;
	}
	/**
	 * Filter for Asset Reallocation arrangement (ProductCode ="WQ0").
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is Asset Reallocation(ProductCode ="WQ0").
	 */
	//P2AXAL027 New Method, ALII783  
	protected boolean filter_WQO(ValProc nbaConfigValProc, ArrayList objects) {
		return getArrangement() != null && getArrangement().getArrSource()!= null && getArrSourceBySubAcctProdCode(getArrangement(),MSCI_PRODUCT_CODE) != null;
	}
	
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given IUL form.
	 */
	// P2AXAL027 New Method
	protected boolean filter_IULSUPP(ValProc nbaConfigValProc, ArrayList objects) {
		return isIULForm(getFormInstance());
	}
	
	/**
	 * Filter for CovOption on basis of Type Code.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the ConvOption is of type ENLG Rider.
	 */
	// P2AXAL027 New Method
	protected boolean filter_ENLGCOP(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				CovOption covOption = (CovOption) objects.get(i);
				return covOption.getLifeCovOptTypeCode() == OLI_OPTTYPE_1009800001;
			}
		}
		return nbaTXLife.getCovOption(nbaTXLife.getPrimaryCoverage(), OLI_OPTTYPE_1009800001) != null; //ALII1202
	}
	
	/**
	 * Filter for IUL or VUL or UL.
	 */
	//P2AXAL027
	protected boolean filter_IVUL(ValProc nbaConfigValProc, ArrayList objects) {
		boolean dis = false;
		if (getPolicy().hasProductType()) {
			long productType = getPolicy().getProductType();
			if (productType == OLI_PRODTYPE_VUL || productType == OLI_PRODTYPE_INDXUL || productType == OLI_PRODTYPE_UL){ 
				dis = true;
			}
		}

		return dis;
	}
	
	//NBA297 - Code Deleted
	
	//P2AXAL027 New Method
	protected boolean filter_ROPR(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				CovOption covOption = (CovOption) objects.get(i);
				return covOption.getLifeCovOptTypeCode() == OLI_OPTTYPE_ROPR;
			}
		}
		return false;
	}
	
	//P2AXAL027 New Method
	protected boolean filter_CLR(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Coverage) {
				Coverage coverage = (Coverage) objects.get(i);
				return coverage.getLifeCovTypeCode() == OLI_COVTYPE_CLR;
			}
		}
		return false;
	}
	
	/**
	 * Filter for Products on the basis of Product Type.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the productType is permanent product.
	 */
	//P2AXAL027 New Method
	protected boolean filter_PERM(ValProc nbaConfigValProc, ArrayList objects) {
		if (getProductTypeString().length() > 0) {
			for (int i = 0; i < NbaConstants.PERM_LIFE_PRODUCTS.length; i++) {
				if (NbaConstants.PERM_LIFE_PRODUCTS[i].equals(getProductTypeString())) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 *Filter for CovOption on the basis of LifeCovOptTypeCode.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is of type OLI_OPTTYPE_LTCABO = 30.
	 */
	//P2AXAL027 New Method
	protected boolean filter_LTCCOP(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				CovOption covOption = (CovOption) objects.get(i);
				return covOption.getLifeCovOptTypeCode() == OLI_OPTTYPE_LTCABO;
			}
		}
		return false;
	}
	
	/**
	 * Filter for FormInstance on the basis of FormName.
	 *@param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given LTC form.
	 */
	//P2AXAL027 New Method
	protected boolean filter_LTCSUPP(ValProc nbaConfigValProc, ArrayList objects) {
		return isLTCForm(getFormInstance());
	}
	
	/**
	 * Filter for UL or VUL.
	 */
	//P2AXAL027
	protected boolean filter_ULVU(ValProc nbaConfigValProc, ArrayList objects) {
		boolean ulVu = false;
		if (getPolicy().hasProductType()) {
			long productType = getPolicy().getProductType();
			if (productType == OLI_PRODTYPE_UL || productType == OLI_PRODTYPE_VUL) {
				ulVu = true;
			}
		}
		return ulVu;
	}
	
	/* NA_AXAL006, A3_AXAL002
	 * Filter returns true if Application form is ICC11-AXA-LIFE 
	 */
	protected boolean filter_APP2011(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return isNewAppFormNumber();		
	}
	/* APSL1144 Retrofit
	 * APSL 1581
	 * Filter returns true if Application form is New App Rev11 form
	 */
	protected boolean filter_APP2011REV(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException{
		return isNewAppRevFormNumber();
	}
		
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given Term Conv Supp form.
	 */
	// P2AXAL027 New Method A3_AXAl002 method changed
	protected boolean filter_TERMCONVSUPP(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof FormInstance) {
				return isTermConvSuppForm(getFormInstance());
			} else {
				return NbaUtils.getFormInstance(nbaTXLife, FORM_NAME_TERMCONVSUPP) != null ? true : false;
			}
		}
		return false;
	}
	/**
	 *Filter for CovOption on the basis of LifeCovOptTypeCode.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is of type OLI_OPTTYPE_CVPLUS = 1009800002.
	 */
	//P2AXAL016 
	protected boolean filter_CVPLUS(ValProc nbaConfigValProc, ArrayList objects) {
		return getCovOption().getLifeCovOptTypeCode() == OLI_OPTTYPE_CVPLUS;
	}
	/**
	 *Filter for CovOption on the basis of LifeCovOptTypeCode.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is of type OLI_OPTTYPE_CVPLUS = 1009800006.
	 */
	//P2AXAL016 
	protected boolean filter_LEE(ValProc nbaConfigValProc, ArrayList objects) {
		return getCovOption().getLifeCovOptTypeCode() == OLI_OPTTYPE_LOANEXTENDR;
	}
	
	/**
	 *Filter for International Underwriters Program Indicator
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the InternationalUWProgInd = 1
	 */
	//P2AXAL016 
	protected boolean filter_IUP(ValProc nbaConfigValProc, ArrayList objects) {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if(appInfoExt != null){
				return appInfoExt.getInternationalUWProgInd();
		}
		return false;
	}
	/**
	 *Filter for CovOption on the basis of LifeCovOptTypeCode.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is of type OLI_OPTTYPE_CVPLUS = 1009800002.
	 */
	//P2AXAL016
	protected boolean filter_DDWCOP(ValProc nbaConfigValProc, ArrayList objects) {
			return getCovOption().getLifeCovOptTypeCode() == OLI_OPTTYPE_WMD;
	}
	/** Filter for an OPAI application
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the application type is OPAI (OLI_APPTYPE_CONVOPAIAD  20) 
	 */
	//P2AXAL016 New Method
	protected boolean filter_OPAI(ValProc nbaConfigValProc, ArrayList objects) {
        if (getApplicationInfo() != null) {
            return getApplicationInfo().getApplicationType() == OLI_APPTYPE_CONVOPAIAD; 
        }
        return false;
    }	
	/** Filter for an TERM application
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the application type is TERM CONVERSION (OLI_APPTYPE_CONVERSIONNEW  4) 
	 */
	//P2AXAL016 New Method
	protected boolean filter_TERMCONV(ValProc nbaConfigValProc, ArrayList objects) {
        if (getApplicationInfo() != null) {
            return getApplicationInfo().getApplicationType() == OLI_APPTYPE_CONVERSIONNEW; 
        }
        return false;
    }		
	
	/**
	 *Filter for CovOption on the basis of LifeCovOptTypeCode.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is of type OLI_OPTTYPE_CVPLUS = 1009800002.
	 */
	//P2AXAL016 
	protected boolean filter_PCP(ValProc nbaConfigValProc, ArrayList objects) {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if(appInfoExt != null){
   			return NbaOliConstants.NBA_SPECIAL_CASE_PCUP == appInfoExt.getSpecialCase();	    			
		}
		return false;
	}
	
	/**
	 *Filter for International Underwriters Program Indicator
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the InternationalUWProgInd = 1
	 */
	//P2AXAL016 new method
	protected boolean filter_ADDLCOVPLAN(ValProc nbaConfigValProc, ArrayList objects) {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if(appInfoExt != null){
				return appInfoExt.getAddlCoveragePlanInd();
		}
		return false;
	}	
	/**
	 *Filter for CovOption on the basis of LifeCovOptTypeCode.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is of type OLI_COVTYPE_WLORDINARY = 1.
	 */
	//P2AXAL016 New Method
	protected boolean filter_LBR(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				CovOption covOption = (CovOption) objects.get(i);
				return covOption.getLifeCovOptTypeCode() == OLI_OPTTYPE_ABE;
			}
		}
		return false;
	}
	
	
	/**
	 *Filter for CovOption on the basis of LifeCovOptTypeCode.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is of type OLI_OPTTYPE_NOLAPSE = 9.
	 */
	//P2AXAL016 New Method
	protected boolean filter_NLG(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				CovOption covOption = (CovOption) objects.get(i);
				return covOption.getLifeCovOptTypeCode() == OLI_OPTTYPE_NOLAPSE;
			}
		}
		return false;
	}	
	
	/**
	 *Filter for CovOption on the basis of SelectionRule.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is with SelectionRule OLI_RIDERSEL_INHERENTNOPREM = 12 and OLI_RIDERSEL_INHERENTADDLPREM=13 .
	 */
	//P2AXAL016 New Method
	protected boolean filter_INH(ValProc nbaConfigValProc, ArrayList objects) {
		CovOptionExtension covOptionExtn = NbaUtils.getFirstCovOptionExtension(getCovOption());
		if (covOptionExtn != null) {
			return NbaOliConstants.OLI_RIDERSEL_INHERENTNOPREM == covOptionExtn.getSelectionRule() || NbaOliConstants.OLI_RIDERSEL_INHERENTADDLPREM == covOptionExtn.getSelectionRule();
		}
		return false;
	}	
	
	/**
	 *Filter for CovOption on the basis of LifeCovOptTypeCode.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the CovOption is of type OLI_OPTTYPE_OPAI = 10.
	 */
	//P2AXAL016 New Method
	protected boolean filter_OPAICOP(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof CovOption) {
				CovOption covOption = (CovOption) objects.get(i);
				return covOption.getLifeCovOptTypeCode() == OLI_OPTTYPE_OPAI;
			}
		}
		return false;
	}
	//APSL548,QC# 5532
	protected boolean filter_REISSUE(ValProc nbaConfigValProc, ArrayList objects) {
		return getNbaDst().getNbaLob().getContractChgType() != null || getNbaTXLife().isReissue(); //ALII1206
	}
	
	
	/**
	 *Filter for Msg Overridden
	 */
	//NBA297 New Method
	protected boolean filter_OVER(ValProc nbaConfigValProc, ArrayList objects) {

		SystemMessageExtension systemMessageExtension=  NbaUtils.getFirstSystemMessageExtension(getSystemMessage());
		if (systemMessageExtension != null) {
			return systemMessageExtension.getMsgOverrideInd();
		}
		return false;
	}
	
	/**
	 *Filter for Occuptaion (TXLife.TXLifeRequest.OLifE.Party.Person.Occupation).
	 *@param nbaConfigValProc the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the Occuptaion is of type NBA_OCCUPATION_OTHER = OTHER.
	 */
	//P2AXAL021 New Method
	protected boolean filter_OTHER(ValProc nbaConfigValProc, ArrayList objects) {
		if (isPrimaryInsured(getParty().getId()) && getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson()) {
			Person person = getParty().getPersonOrOrganization().getPerson();
			return person.hasOccupation() ? (person.getOccupation()).equals(NbaOliConstants.NBA_OCCUPATION_OTHER) : false;
		}
		return false;
	}
	
	
	/**
	 * Method for executing ValRule's defined in configuration.
	 */
	//NBA297 New Method
	public boolean executeRule(String ruleName) throws NbaBaseException {
		try {
			logDebug("executeRule >> ruleName = " + ruleName);
			ValRule valRule = (ValRule) valRules.get(ruleName);
			Object ctrlObject = getNbaReflectionUtils().getValue(this, valRule.getObj());
			if (null == ctrlObject) {
				return false;
			}
			Object fieldValue = getNbaReflectionUtils().getValue(ctrlObject, valRule.getField());
			List valueList = null;
			if (valRule.getUse().equalsIgnoreCase("literal"))
				valueList = valRule.getValue();
			else if (valRule.getUse().equalsIgnoreCase("resolves"))
				valueList = getNbaReflectionUtils().getValueFromClass(valRule.getValue());
			else{
				throw new NbaBaseException("Incorrect value for use tag");
			}
			return NbaUtils.doComparison(fieldValue, valueList, valRule.getOp());
		} catch (Exception ex) {
			throw new NbaBaseException(ex);
		}
	}
	//SR494086.2 New Method,ADC Retrofit 
	protected boolean filter_ADC(ValProc nbaConfigValProc, ArrayList objects) {
		return NbaConstants.APPPROD_TYPE_ADC.equalsIgnoreCase(getNbaDst().getNbaLob().getAppProdType()) == true;
	}
	
	/**
	 *Filter for International Underwriters Program Overrider Indicator
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the IUPOverrideInd = 1
	 */
	//P2AXAL016 
	protected boolean filter_IUPOVERRIDE(ValProc nbaConfigValProc, ArrayList objects) {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if(appInfoExt != null){
				return appInfoExt.getIUPOverrideInd();
		}
		return false;
	}
	
	/**
	 * Filter for Insured.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the object  is an Insured Person.
	 */
	// P2AXAL054 New Filter
	protected boolean filter_SINS(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof LifeParticipant) {
				return NbaUtils.isSurvivorshipInsured((LifeParticipant) objects.get(i));
			} else if (objects.get(i) instanceof Relation) {
				return getNbaTXLife().isSurvivorshipInsured((Relation) objects.get(i));
			} else if (objects.get(i) instanceof Party) {
				return getNbaTXLife().isSurvivorshipInsured(getParty().getId());
			}  else if (objects.get(i) instanceof SignatureInfo) {
				return isSurvivorshipInsured(getSignatureInfo());
			} else if (objects.get(i) instanceof Intent) {
				return isPrimaryInsured(getIntent().getPartyID());//NO CHANGES NEEDED HERE, SURVIVORSHIP INSURED ALREADY COVERED INSIDE 
			}
		}
		return false;
	}
	
	/**
	 * Filter for Joint Insured Person.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Joint Insured Person.
	 */
	// P2AXAL054 New Method
	protected boolean filter_JNT(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof LifeParticipant) {
				return NbaUtils.isJointInsuredParticipant((LifeParticipant) objects.get(i));
			} else if (objects.get(i) instanceof Relation) {
				return getRelation() == getJointInsRelation();
			} else if (objects.get(i) instanceof Party) {
				return isJointInsured(getParty().getId());
			} else if (objects.get(i) instanceof SignatureInfo) {
				return isJointInsured(getSignatureInfo());
			} else if (objects.get(i) instanceof Intent) {
				return isJointInsured(getIntent().getPartyID());
			} else if (objects.get(i) instanceof TempInsAgreementDetails) {//A4_AXAL001
				return isJointInsured(getTempInsAgreementDetails().getPartyID());
			}
		}
		return false;
	}
	
	
	/* A3_AXAL002
	 * Filter returns true if Application form is TConv 
	 */
	protected boolean filter_TCONV(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {		
		return isTConvFormNumber();		
	}
	
	/* A3_AXAL002
	 * Filter returns true if Application form is AMIGV 
	 */
	protected boolean filter_AMIGV(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return isAMIGVFormNumber();		
	}
	
	/*
	 * A3_AXAL002 Filter returns true if 1035 Exchange is set on the case
	 */
	protected boolean filter_1035EX(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return getNbaTXLife().is1035Exchange();
	}
	/**
	 *Filter for Insured Has Power Of Attorney Indicator
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the InsHasPowerOfAttorneyInd = 1
	 */
	//P2AXAL016,ALII834,ALII835
	protected boolean filter_POAIND(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Party) {
				return getPOAInd(getParty());
			} else if (objects.get(i) instanceof Relation) {
				NbaParty nbaParty = getNbaTXLife().getParty(getRelation().getRelatedObjectID());
				return nbaParty != null ? getPOAInd(nbaParty.getParty()) : false;
			} else if (objects.get(i) instanceof LifeParticipant) {
				NbaParty nbaParty = getNbaTXLife().getParty(getLifeParticipant().getPartyID());
				return nbaParty != null ? getPOAInd(nbaParty.getParty()) : false;
			}
		}
		return false;
	}	
	
	/** Filter for FormInstance on the basis of FormName.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of GI form.
	 */	
	// P2AXAL068 New method
	protected boolean filter_GISUPP(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof FormInstance) {
				return isGISuppForm(getFormInstance());
			} else {
				return NbaUtils.getFormInstance(nbaTXLife, FORM_NAME_GISUPP) != null ? true : false;
			}
		}
		return false;
	}

	/** Filter for a GI application
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the Application form is GI
	 */
	//P2AXAL068 New Method
	protected boolean filter_GIAPP(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return isGIFormNumber();
    }
	
	/** Filter for a GI application type
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the Application type =  OLI_APPTYPE_GROUPAPP
	 */
	//P2AXAL068 New Method
	protected boolean filter_GIAPPTYPE(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return getApplicationInfo().getApplicationType() == OLI_APPTYPE_GROUPAPP;
    }
	
	/**
	 *Filter for IssueType (TXLife.TXLifeRequest.OLifE.Holding.Policy.IssueType).
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the IssueType is not present OR IssueType == OLI_COVISSU_FULL
	 */
	//ALII1106 
	protected boolean filter_UNDRQRD(ValProc nbaConfigValProc, ArrayList objects) {
		return !getHolding().getPolicy().hasIssueType() || getHolding().getPolicy().getIssueType() != OLI_COVISSU_CONVERTED;
	}

	/**
	 *Filter for RePrint 
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the NbaDst object is a Print Work Item and the EXTC LOB indicates a reprint
	 */
	//CR61047 new method 
	protected boolean filter_REPRINT(ValProc nbaConfigValProc, ArrayList objects) {
		return ((getNbaDst().getNbaLob().getWorkType() == A_WT_CONT_PRINT_EXTRACT) && (REPRINT_EXTRACT.equalsIgnoreCase(getNbaDst().getNbaLob().getPrintExtract()))); //APSL5055
	}
  /**
	 *Filter for party's employment status.
	 *@param nbaConfigValProc
	 *the configuration information for a validation process
	 *@param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 *@return true if the EmploymentStatusTC = OLI_EMPSTAT_ACTIVE
	 */
	//QC8437
	protected boolean filter_ACTEMP(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) {
			if (objects.get(i) instanceof Party) {
				ArrayList employmentList = getParty().getEmployment();
				if (!employmentList.isEmpty()) {
					return isActiveEmp((Employment) employmentList.get(0));
				}
			} else if (objects.get(i) instanceof Employment) {
				return isActiveEmp(getEmployment());
			}
		}
		return false;
	}
	
	/*
	 * 
	 * QC9149/ALII1386 Filter returns true if its a Paid Reissue
	 */
	protected boolean filter_PDRISU(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return getNbaTXLife().isPaidReIssue();
	}
	
	
	//SR641590 (APSL2012) SUB-BGA
	protected boolean filter_SUBBGA(ValProc nbaConfigValProc, ArrayList objects) {		
		return hasSubfirm();
	}
	
	//APSL4250 New Method
	protected boolean filter_SUBFIRM(ValProc nbaConfigValProc, ArrayList objects) {		
		for (int i = objects.size() - 1; i > -1; i--) { 
			if (objects.get(i) instanceof Relation) {
				return isSubFirmRelation(getRelation().getId()) ;
			} else if (objects.get(i) instanceof Party) {
				return isSubFirm(getParty().getId());
			}
		}
		return false;
	}
	
	
	//	New Method QC9953/APSL2639
	protected boolean filter_REPL(ValProc nbaConfigValProc, ArrayList objects) {
		return getNbaTXLife().isReplacement();
	}

	/** Filter for NY cases
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the jurisdiction is 37
	 */
	//ALII1659 New Method
	protected boolean filter_NYSTATE(ValProc nbaConfigValProc, ArrayList objects) {
		if (getApplicationInfo() != null && getApplicationInfo().getApplicationJurisdiction() == OLI_USA_NY ) {
			return true;
		}
		return false;
	}
	
//	APSL2808 New Method
	protected boolean filter_SIAPP(ValProc nbaConfigValProc, ArrayList objects) {
		if (getApplicationInfo() != null && (NbaOliConstants.OLI_APPTYPE_SIMPLIFIEDISSUE == getApplicationInfo().getApplicationType())) {
			return true;
		}
		return false;
	}
	
	// APSL3447 New Method
	protected boolean filter_HVT(ValProc nbaConfigValProc, ArrayList objects) {
		if (NbaUtils.isHVTCase(getNbaTXLife())) {
			return true;
		}
		return false;
	}
	/*
	 * if the current businessProcess is contained in 'noProductRequired' then return false
	 * and product retrieval is bypassed
	 */
	//ALII2041 New Method
	protected boolean productRequired(String businessProcess) {
		
		if (noProductRequired.contains(businessProcess)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Filter for Beneficiary.
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if relation is for any beneficiary
	 */
	// APSL3667 New method
	protected boolean filter_BENE(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
			if (objects.get(i) instanceof Relation) {
				return NbaUtils.isBeneficiaryRelationRoleCode(relation.getRelationRoleCode());
			}
		}
		return false;
	}
	
	/** Filter for CA cases
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the jurisdiction is 6
	 */
	//APSL3818(SR805869) New Method
	protected boolean filter_CASTATE(ValProc nbaConfigValProc, ArrayList objects) {
		if (getApplicationInfo() != null && getApplicationInfo().getApplicationJurisdiction() == OLI_USA_CA ) {
			return true;
		}
		return false;
	}
	

	// APSL4428
	
	/**
	 * Filter for Product Code .
	 * @param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the arrangement is Scheduled Withdrawal.
	 */

	protected boolean filter_PROD(ValProc nbaConfigValProc, ArrayList objects) {
		return getPolicy() != null && (getPolicy().getProductCode().equalsIgnoreCase(NBA_PRODUCTCODE_ILOIII));
	}
	
	// APSL4740 New Method
	protected boolean filter_EAPP(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		//BEGIN: NBLXA1554[NBLXA1917]
		long SubmissionType = -1L;
		if (getApplicationInfoExtension() != null && !NbaUtils.isNull(getApplicationInfoExtension().getSubmissionType()) && !NbaUtils.isEqualToZero(getApplicationInfoExtension().getSubmissionType())) {
			SubmissionType = getApplicationInfoExtension().getSubmissionType();
		} else if (!NbaUtils.isBlankOrNull(getApplicationInfo()) && !NbaUtils.isNull(getApplicationInfo().getSubmissionType())) {
			SubmissionType = getApplicationInfo().getSubmissionType();
		}
		if (! NbaUtils.isNull(SubmissionType) && SubmissionType == NbaOliConstants.OLI_APPSUBMITTYPE_ELECTRONIC) {
			return true;
		}		
		return false; //NBLXA1554[NBLXA2157]
		//NBLXA1554[NBLXA2157] code deleted
	}
	// APSL4740
	//APSL 4766 
	protected boolean filter_ACH(ValProc nbaConfigValProc, ArrayList objects) {
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if(appInfoExt != null){
			if(appInfoExt.getInitialPremiumPaymentForm() == NbaOliConstants.OLI_PAYFORM_EFT)
				return true;
			}
		return false;
	}
	
	// APSL4835 New Method
	protected boolean filter_NonSuitAsu(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		NbaTXLife nbaTXLife = getNbaTXLife();
		if (nbaTXLife != null && nbaTXLife.isRetail()) {
			String asuCode = NbaUtils.getWritingAgencyId(getNbaTXLife());
			if (asuCode != null && "HCS".equalsIgnoreCase(asuCode)) {
				return true;
			}
		}
		return false;
	}
	// APSL4835
	
	/**
	 * Filter for FormInstance on the basis of FormName.
	 *@param nbaConfigValProc the configuration information for a validation process
	 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given LTC Personal Worksheet form.
	 */
	// APSL4916 New Method
	protected boolean filter_LTCPW(ValProc nbaConfigValProc, ArrayList objects) {
		return isLTCPWForm(getFormInstance());
	}
	
	// APSL5010 New Method
	protected boolean filter_TCONVNY(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return NbaUtils.isPlainTermConvNY(getNbaTXLife());
	}
	//APSL5015 DOL- New Method
		protected boolean filter_ERISASUPP(ValProc nbaConfigValProc, ArrayList objects) {
			for (int i = objects.size() - 1; i > -1; i--) { 
				if (objects.get(i) instanceof FormInstance) {
					return isERISABICForm(getFormInstance());
				}
			}
			return false;
		}
		//APSL5015 DOL- New Method
		protected boolean filter_IRASUPP(ValProc nbaConfigValProc, ArrayList objects) {
			for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
				if (objects.get(i) instanceof FormInstance) {
					return isIRABICForm(getFormInstance());
				}
			}
			return false;
		}
		//APSL5015 DOL- New Method
	protected boolean filter_QUALBIC(ValProc nbaConfigValProc, ArrayList objects) throws ParseException {
		boolean isQual = false;
		if (getNbaTXLife() != null) {
			isQual = NbaUtils.isQualifiedPlanForBIC(getNbaTXLife());
		}
		return isQual;
	}
		//APSL5015 DOL- New Method
		protected boolean filter_QUALMNY(ValProc nbaConfigValProc, ArrayList objects) {
			boolean qualifiedMoneyInd = false;
			try {
				if (getNbaTXLife() != null) {
					qualifiedMoneyInd = NbaUtils.isQualifiedMoney(getNbaTXLife());
				}
			} catch (Throwable e) {
				addNewSystemMessage(VALIDATION_PROCESSING, concat("Process ", nbaConfigValProc.getId(), ": ", e.toString()), "");
			}
			return qualifiedMoneyInd;
		}
		// NBLXA-188 New Method
		protected boolean filter_GROUPAPP(ValProc nbaConfigValProc, ArrayList objects) {
			if (getApplicationInfo() != null && (NbaOliConstants.OLI_APPTYPE_GROUPAPP == getApplicationInfo().getApplicationType()
					&& !APPPROD_TYPE_GICASE.equalsIgnoreCase(getNbaDst().getNbaLob().getAppProdType()))) {
				return true;
			}
			return false;
		}
		// NBLXA-188 New Method
		protected boolean filter_GROUPCASE(ValProc nbaConfigValProc, ArrayList objects) {
			if (getApplicationInfo() != null && (NbaOliConstants.OLI_APPTYPE_GROUPAPP == getApplicationInfo().getApplicationType()
					&& APPPROD_TYPE_GICASE.equalsIgnoreCase(getNbaDst().getNbaLob().getAppProdType()))) {
				return true;
			}
			return false;
		} 
		
		// NBLXA-188 New Method
		protected boolean filter_GICONSENT(ValProc nbaConfigValProc, ArrayList objects) {
			for (int i = 0; i < getOLifE().getFormInstance().size(); i++) {
				FormInstance formInstance = getOLifE().getFormInstanceAt(i);
				if(GI_CONSENT_FORM.equalsIgnoreCase(formInstance.getFormName())){
					return true;
				}
			}
			return false;
		} 

	// NBLXA-1317 New Method
		protected boolean filter_UNBOUND(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
			PolicyExtension polExtension = getPolicyExtension();
			if (polExtension != null) {
				if (polExtension.hasUnboundInd() && polExtension.getUnboundInd()) {
				return true;

			}
		}

		return false;

	}
		
	// P2AXAL068 New method
	protected boolean filter_GILTCSUPP(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return NbaVPMSHelper.isGILTCFormNumber(getFormInstance());	
	}	
	
	/**
	 * Filter for Beneficial Owner Person originating from owner.
	 * 
	 * @param nbaConfigValProc
	 *            the configuration information for a validation process
	 * @param objects
	 *            an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an beneficial owner Person originating from owner.
	 */
	// NBLXA1254 New Method
	protected boolean filter_OWNERBENEOWNER(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
			if (objects.get(i) instanceof Relation) {
				return isRelationFromRoleCodeAndOriginatingRelationRoleCode(getRelation(), OLI_REL_BENEFICIALOWNER, OLI_REL_OWNER);
			} else if (objects.get(i) instanceof Party) {
				return isPartyFromRoleCodeAndOriginatingRelationRoleCode(getParty().getId(), OLI_REL_BENEFICIALOWNER, OLI_REL_OWNER);
			}
		}
		return false;
	}

	/**
	 * Filter for Beneficial Owner Person originating from payer.
	 * 
	 * @param nbaConfigValProc
	 *            the configuration information for a validation process
	 * @param objects
	 *            an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an beneficial owner Person originating from payer.
	 */
	// NBLXA1254 New Method
	protected boolean filter_PAYERBENEOWNER(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
			if (objects.get(i) instanceof Relation) {
				return isRelationFromRoleCodeAndOriginatingRelationRoleCode(getRelation(), OLI_REL_BENEFICIALOWNER, OLI_REL_PAYER);
			} else if (objects.get(i) instanceof Party) {
				return isPartyFromRoleCodeAndOriginatingRelationRoleCode(getParty().getId(), OLI_REL_BENEFICIALOWNER, OLI_REL_PAYER);
			}
		}
		return false;
	}

	/**
	 * Filter for Controlling Person originating from Owner.
	 * 
	 * @param nbaConfigValProc
	 *            the configuration information for a validation process
	 * @param objects
	 *            an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Controlling Person.
	 */
	// NBLXA1254 New Method
	protected boolean filter_OWNERCTRLPERSON(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
			if (objects.get(i) instanceof Relation) {
				return isRelationFromRoleCodeAndOriginatingRelationRoleCode(getRelation(), OLI_REL_CONTROLLINGPERSON, OLI_REL_OWNER);
			} else if (objects.get(i) instanceof Party) {
				return isPartyFromRoleCodeAndOriginatingRelationRoleCode(getParty().getId(), OLI_REL_CONTROLLINGPERSON, OLI_REL_OWNER);
			}
		}
		return false;
	}
		
	/**
	 * Filter for Controlling Person originating from Payer.
	 * 
	 * @param nbaConfigValProc
	 *            the configuration information for a validation process
	 * @param objects
	 *            an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Controlling Person.
	 */
	// NBLXA1254 New Method
	protected boolean filter_PAYERCTRLPERSON(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
			if (objects.get(i) instanceof Relation) {
				return isRelationFromRoleCodeAndOriginatingRelationRoleCode(getRelation(), OLI_REL_CONTROLLINGPERSON, OLI_REL_PAYER);
			} else if (objects.get(i) instanceof Party) {
				return isPartyFromRoleCodeAndOriginatingRelationRoleCode(getParty().getId(), OLI_REL_CONTROLLINGPERSON, OLI_REL_PAYER);
			}
		}
		return false;
	}
	
		/**
		 * Filter for Beneficial Auth Person.
		 * @param nbaConfigValProc the configuration information for a validation process
		 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
		 * @return true if the participant is an Auth Person.
		 */
		// NBLXA1254  New Method
		protected boolean filter_OWNERAUTHPERSON(ValProc nbaConfigValProc, ArrayList objects) {//NBLXA-1688
			for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
				if (objects.get(i) instanceof Relation) {
					return isRelationFromRoleCodeAndOriginatingRelationRoleCode(getRelation(), OLI_REL_AUTHORIZEDPERSON, OLI_REL_OWNER);//NBLXA-1688
				} else if (objects.get(i) instanceof Party) {
					return isPartyFromRoleCodeAndOriginatingRelationRoleCode(getParty().getId(), OLI_REL_AUTHORIZEDPERSON, OLI_REL_OWNER);//NBLXA-1688
				}
			}
			return false;
		}
		
		

		/**
		 * Filter for Beneficial Trustee.
		 * @param nbaConfigValProc the configuration information for a validation process
		 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
		 * @return true if the participant is an Trustee.
		 */
		// NBLXA1254  New Method
		protected boolean filter_TRUSTEE(ValProc nbaConfigValProc, ArrayList objects) {
			for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
				if (objects.get(i) instanceof Relation) {
					return getRelation() == getTrusteeRelation(getRelation().getId());
				} else if (objects.get(i) instanceof Party) {
					return isTrustee(getParty().getId());
				}
			}
			return false;
		}
		
		// NBLXA1254  New Method For CDD Effective Date
		protected boolean filter_CDDEffDate(ValProc nbaConfigValProc, ArrayList objects) {
			Date signDate = getApplicationInfo().getSignedDate();
			try {
				// NBLXA2299 Begin
				Date cddEffectiveDate = null;
				if (filter_GROUPAPP(nbaConfigValProc, objects)) {
					cddEffectiveDate = NbaUtils.getDateFromStringInUSFormat(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
							NbaConstants.GI_CDD_EFFECTIVE_DATE));
				} else { // NBLXA2299 End
					cddEffectiveDate = NbaUtils.getDateFromStringInUSFormat(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
							NbaConstants.CDD_EFFECTIVE_DATE));					
				}
				if (signDate != null && NbaUtils.compare(signDate, cddEffectiveDate) >= 0) {
					return true;
				}
			} catch (NbaBaseException e) {
				getLogger().logException(e);
			}
			return false;
		}
		
		/**
		 * Filter for Entity Owner.
		 * @param nbaConfigValProc the configuration information for a validation process
		 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
		 * @return true if the participant is an Entity Owner.
		 */
		// NBLXA1254  New Method
	protected boolean filter_ENTOWN(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
			if (objects.get(i) instanceof Party) {
				return isEntityOwner(getParty().getId().toString());
			}
			if (objects.get(i) instanceof Relation) {
				return isEntityOwner(getRelation().getRelatedObjectID());
			}
		}
		return false;
	}
		
		/**
		 * Filter for FormInstance on the basis of FormName.
		 * @param nbaConfigValProc the configuration information for a validation process
		 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
		 * @return true if the formInstance is of given FormName.
		 */
		// NBLXA1254  New Method
		protected boolean filter_SeoSupp(ValProc nbaConfigValProc, ArrayList objects) {
			for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
				if (objects.get(i) instanceof FormInstance) {
					return isSeoSuppForm(getFormInstance());
				}else if(objects.get(i) instanceof Party) {
					FormInstance formInstance = NbaUtils.getFormInstanceForParty(nbaTXLife, FORM_NAME_EntityOwnership, ((Party)objects.get(i)).getId());
					return formInstance != null && NbaUtils.isFormReceived(formInstance);
				}
			}
			return false;
		}
		
		/**
		 * Filter for FormInstance on the basis of FormName.
		 * @param nbaConfigValProc the configuration information for a validation process
		 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
		 * @return true if the formInstance is of given FormName.
		 */
		// NBLXA1254  New Method
		protected boolean filter_SepSupp(ValProc nbaConfigValProc, ArrayList objects) {
			for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
				if (objects.get(i) instanceof FormInstance) {
					return isSepSuppForm(getFormInstance());
				}else if(objects.get(i) instanceof Party) {
					FormInstance formInstance = NbaUtils.getFormInstanceForParty(nbaTXLife, FORM_NAME_ENTITYPAYOR, ((Party)objects.get(i)).getId());
					return formInstance != null && NbaUtils.isFormReceived(formInstance);
				}
			}
			return false;
		}
		
		/**
		 * Filter for Entity Payor.
		 * @param nbaConfigValProc the configuration information for a validation process
		 * @param objects an ArrayList containing the objects defined in the nbaConfigValProc
		 * @return true if the participant is an Entity Owner.
		 */
		// NBLXA1254  New Method
		protected boolean filter_ENTPAYOR(ValProc nbaConfigValProc, ArrayList objects) {
			for (int i = objects.size() - 1; i > -1; i--) { //loop backwards
				 if (objects.get(i) instanceof Party) {
					return isEntityPayor(getParty().getId().toString());
				}
			}
			return false;
		}
		
	// NBLXA-1540 New Method For CV L16943 Effective Date
	protected boolean filter_L16943DEFFDATE(ValProc nbaConfigValProc, ArrayList objects) {
		Date signDate = getApplicationInfo().getSignedDate();
		Date cvEffectiveDate = NbaUtils.getDateFromStringInUSFormat(NbaConstants.L16943DEFFDATE);
		if (signDate != null && NbaUtils.compare(signDate, cvEffectiveDate) >= 0) {
			return true;
		}
		return false;
	}
	
	// NBLXA-1740 New Method For CV NBLXA1740 Effective Date
	protected boolean filter_NBLXA1740DEFFDATE(ValProc nbaConfigValProc, ArrayList objects) {
		Date submissionDate = getApplicationInfo().getSubmissionDate();
		Date cvEffectiveDate = NbaUtils.getDateFromStringInUSFormat(NbaConstants.NBLXA1740DEFFDATE);
		if (submissionDate != null && NbaUtils.compare(submissionDate, cvEffectiveDate) >= 0) {
			return true;
		}
		return false;
	}
		
	/**
	 * Filter for FormInstance on the basis of FormName.
	 * 
	 * @param nbaConfigValProc
	 *            the configuration information for a validation process
	 * @param objects
	 *            an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the formInstance is of given FormName.
	 */
	// NBLXA1254 New Method
	protected boolean filter_TrustConSupp(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
			if (objects.get(i) instanceof FormInstance) {
				return isTrustedContactForm(getFormInstance());
			} else if (objects.get(i) instanceof Party) {
				FormInstance formInstance = NbaUtils.getFormInstanceForParty(nbaTXLife, FORM_NAME_TRUSTEDCONTACT, ((Party) objects.get(i)).getId());
				return formInstance != null;
			} else if (objects.get(i) instanceof Relation) {
				FormInstance formInstance = NbaUtils.getFormInstanceForParty(nbaTXLife, FORM_NAME_TRUSTEDCONTACT, ((Relation) objects.get(i)).getRelatedObjectID());
				return formInstance != null;
			}
			
		}
		return false;
	}

	/**
	 * Filter for Trusted Contact.
	 * 
	 * @param nbaConfigValProc
	 *            the configuration information for a validation process
	 * @param objects
	 *            an ArrayList containing the objects defined in the nbaConfigValProc
	 * @return true if the participant is an Trustee.
	 */
	// NBLXA1254 New Method
	protected boolean filter_TRUSTCONT(ValProc nbaConfigValProc, ArrayList objects) {
		for (int i = objects.size() - 1; i > -1; i--) { // loop backwards
			if (objects.get(i) instanceof Relation) {
				return getRelation() == getTrustedContactRelation(getRelation().getId());
			} else if (objects.get(i) instanceof Party) {
				return isTrusteContact(getParty().getId());
			}
		}
		return false;
	}

	// NBLXA1611 New Method For Trusted Contact Effective Date
	protected boolean filter_TRUSTEDCONTACTEFFDATE(ValProc nbaConfigValProc, ArrayList objects) {
		Date signDate = getApplicationInfo().getSignedDate();
		try {
			// NBLXA2299 Begin
			Date trustedContactEffDate = null;
			if (filter_GROUPAPP(nbaConfigValProc, objects)) {
				trustedContactEffDate = NbaUtils.getDateFromStringInUSFormat(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
						NbaConstants.GI_CDD_EFFECTIVE_DATE));
			} else { // NBLXA2299 End
				trustedContactEffDate = NbaUtils.getDateFromStringInUSFormat(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
						NbaConstants.TRUSTEDCONTACT_EFFECTIVE_DATE));				
			}
			if (signDate != null && NbaUtils.compare(signDate, trustedContactEffDate) >= 0) {
				return true;
			}
		} catch (NbaBaseException e) {
			getLogger().logException(e);
		}
		return false;
	}
	
	// Begin NBLXA-1831
	protected boolean filter_GOLDENTICKET(ValProc nbaConfigValProc, ArrayList objects) {
		boolean goldenTicketInd = false;
		Holding primaryHolding = getNbaTXLife().getPrimaryHolding();
		if (primaryHolding != null) {
			Policy policy = primaryHolding.getPolicy();
			if (policy != null) {
				PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(policy);
				if (polExt != null) {
					goldenTicketInd = polExt.getGoldenTicketInd();
				}
			}
		}
		return goldenTicketInd;
	}
	// End NBLXA-1831
	// NBLXA-1997 Starts
	protected boolean filter_REPLPARTY(ValProc nbaConfigValProc, ArrayList objects) {

		for (int i = objects.size() - 1; i > -1; i--) {
			if (objects.get(i) instanceof Party) {

				return isReplParty(getParty().getId());

			}

		}

		return false;
	}

	// NBLXA-1997Ends
	
	// NBLXA-2043 New Method
			protected boolean filter_REALLOCFUND(ValProc nbaConfigValProc, ArrayList objects) {
				getLogger().logDebug("Enter in NbaContractValidation.filter_REALLOCFUND()");
				return NbaUtils.isFundsCVRequired();
				
			}
			
	// Begin NBLXA-2132
	protected boolean filter_A4A5FORMS(ValProc nbaConfigValProc, ArrayList objects) throws NbaBaseException {
		return isA4A5FormNumber();
	}
	// End NBLXA-2132
	
	// NBLXA-1850 Starts
	protected boolean filter_NCFEFFDATE(ValProc nbaConfigValProc, ArrayList objects) {
		getLogger().logDebug("Enter in NbaContractValidation.filter_NCFEFFDATE()");
		Date signDate = getApplicationInfo().getSignedDate();
		try {
			AxaRulesDataBaseAccessor dataBaseAccessor = AxaRulesDataBaseAccessor.getInstance();
			Date startDateNCF = dataBaseAccessor.getConfigDateValue(NbaConstants.NCF_START_DATE);
			if (signDate != null && NbaUtils.compare(signDate, startDateNCF) >= 0) {
				return true;
			}
		} catch (NbaBaseException e) {
			getLogger().logException(e);
		}
		return false;
	}
	// NBLXA-1850 Ends
	
	//NBLXA-2128 New Method
		protected boolean filter_NDSTATE(ValProc nbaConfigValProc, ArrayList objects) {
			if (getApplicationInfo() != null && getApplicationInfo().getApplicationJurisdiction() == OLI_USA_ND ) {
				return true;
			}
			return false;
		}

	// NBLXA-2223 Starts
	protected boolean filter_IUPMANUALIND(ValProc nbaConfigValProc, ArrayList objects) {
		getLogger().logDebug("Enter in NbaContractValidation.filter_IUPMANUALIND()");
		ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
		if (appInfoExt != null) {
			if (appInfoExt.getIupoverrideIndCode() == NbaOliConstants.NBA_ANSWERS_YES)
				return true;
		}
		return false;
	}
	// NBLXA-2223 Ends
	
	// NBLXA-2155[NBLXA-2300] Starts
	protected boolean filter_ILLUSCVEFFDATE(ValProc nbaConfigValProc, ArrayList objects) {
		getLogger().logDebug("Enter in NbaContractValidation.filter_ILLUSCVEFFDATE()");
		Date appSubmitDate = getApplicationInfo().getSubmissionDate();
		if (NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.ILLUSCV_START_DATE)) {
			return true;
		}
		return false;
	}
	// NBLXA-2155[NBLXA-2300] Ends
	
	// NBLXA-2132 Starts

	protected boolean filter_OWNASINSURED(ValProc nbaConfigValProc, ArrayList objects) {
		if (getNbaTXLife().isOwnerSameAsInsured()) {
			return true;
		}
		return false;

	}

    // NBLXA-2132 Ends

	// NBLXA-2341,NBLXA-2132--March2019 release CV eff date 
	protected boolean filter_MAR2019EFFDATE(ValProc nbaConfigValProc, ArrayList objects) {
		Date submissionDate = getApplicationInfo().getSubmissionDate();
		Date cvEffectiveDate = NbaUtils.getDateFromStringInUSFormat(NbaConstants.MAR2019DEFFDATE);
		if (submissionDate != null && NbaUtils.compare(submissionDate, cvEffectiveDate) >= 0) {
			return true;
		}
		return false;
	}	
	
	// NBLXA-2299 New method
	protected boolean filter_ENTOWNGIAPP(ValProc nbaConfigValProc, ArrayList objects) {
		return NbaUtils.isEntityOwnedGIApplication(getNbaTXLife());
	}
	
	//NBLXA-2527 New Method
	protected boolean filter_MAY2019EFFDATE(ValProc nbaConfigValProc, ArrayList objects) {
		Date submissionDate = getApplicationInfo().getSubmissionDate();
		Date effectiveDate = NbaUtils.getDateFromStringInUSFormat(NbaConstants.MAY2019EFFDATE);
		if (submissionDate != null && NbaUtils.compare(submissionDate, effectiveDate) >= 0) {
			return true;
		}
		return false;
	}
	
	// NBLXA-2600 Starts
	protected boolean filter_JULY2019EFFDATE(ValProc nbaConfigValProc, ArrayList objects) {
		getLogger().logDebug("Enter in NbaContractValidation.filter_JULY2019EFFDATE()");
		Date appSubmitDate = getApplicationInfo().getSubmissionDate();
		if (NbaUtils.compareConfigEffectiveDate(appSubmitDate, NbaConstants.JULY2019EFFDATE)) {
			return true;
		}
		return false;
	}
	// NBLXA-2600 Ends
}