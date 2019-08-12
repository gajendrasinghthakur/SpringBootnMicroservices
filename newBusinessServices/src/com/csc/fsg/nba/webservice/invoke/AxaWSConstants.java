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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
package com.csc.fsg.nba.webservice.invoke;

/**
 * Collection of web service constants generally used to avoid literals scattered throughout the code. Using meaningful constants also makes the
 * source more readable than using literals.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.18</td><td>Version 7</td><td>Producer Interface</td></tr>
 * <tr><td>AXAL3.7.26</td><td>AXA Life Phase 1</td><td>OLSA Interface</td></tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * <tr><td>P2AXAL038</td><td>AXA Life Phase 2</td><td>Zip Code Interface</td></tr>
 * <tr><td>P2AXAL041</td><td>AXA Life Phase 2</td><td>Message received from OLSA Unit Number Validation Interface</td></tr>
 * <tr><td>P2AXAL039</td><td>AXA Life Phase 2</td><td>Reg60 Database Interface</td></tr>
 * <tr><td>P2AXAL016CV</td><td>AXA Life Phase 2</td><td>Life 70 Calculations</td></tr>
 * <tr><td>P2AXAL029</td><td>AXA Life Phase 2</td><td>Contract Print</td></tr>
 * <tr><td>PP2AXAL021</td><td>AXA Life Phase 2</td><td>Suitability</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Analytics Full Implementation</td></tr> 
 * </table>
 * <p> 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public interface AxaWSConstants {

	public final static String WS_OP_SEARCHPRODUCER = "SearchProducer";

	public final static String WS_OP_AGENTVALIDATION = "ProducerLicenseStatus";

	public final static String WS_OP_AGENTDEMOGRAPICS = "GetDistributorInfo";

	public final static String WS_OP_CONTRACT_PRINT = "PolicyPrint";

	public final static String WS_OP_CONTRACT_SERVICE = "generateContractNumber";

	public final static String WS_OP_PCS = "submitProductCompensation";

	public final static String WS_OP_PIRS = "submitPIRequirementCompensation";

	public final static String WS_OP_RCS = "submitReplacementCompensation";

	public final static String WS_OP_RTS = "sendReplacementData";

	public final static String WS_OP_SUBMIT_POLICY = "submitAdministrationPolicy";

	public final static String WS_OP_UPDATE_POLICY = "updateInforcePolicy";//AXAL3.7.17PC

	public final static String WS_OP_RETRIEVE_POLICY = "retrieveInforcePolicy";

	public final static String WS_OP_UNADMIT_REPL = "retrievePolicyActivity";

	public final static String WS_OP_CIF_TRANSMIT = "transmitClientHolding";//AXAL3.7.25

	public final static String WS_OP_ECSSUBMIT = "submitExpressCompensation";//AXAL3.7.22

	public final static String WS_OP_ECSUPDATE = "updateExpressCompensation";//AXAL3.7.22

	public final static String WS_OP_TAI_SERVICE_TRANSMIT = "transmitReinsuranceNewBusiness"; //AXAL3.7.16

	public static final String WS_OP_TAI_SERVICE_RETRIEVE = "retrieveReinsurance";//AXAL3.7.05

	public static final String WS_OP_RET_PRIOR_INSURANCE = "retrievePriorInsurance";//AXAL3.7.05

	public final static String WS_OP_OLSA = "sendInitialPremium";//AXAL3.7.26

	public static final String WS_OP_UPD_PRIOR_INSURANCE = "updatePriorInsurance";//AXAL3.7.21

	public static final String WS_OP_ADD_PRIOR_INSURANCE = "addPriorInsurance";//AXAL3.7.21

	public static final String WS_OP_VALIDATE_POLICY = "validateClientPolicy"; //AXAL3.7.25

	public final static String ADMIN_ID = "CAPS";

	public final static String SOURCEINFO = "nbA_Life";

	//public final static String FILECONTROLID = "CAPS";

	public final static String HOLDING_ID = "Holding_1";

	public final static String FINANCIAL_ACTIVITY_ID = "FinancialActivity_";

	public final static String PARTY_ID = "Party_1";

	public final static String RELATION_ID = "Relelation_1";

	public final static String CARRIER_APPT_ID = "CarrierAppointment_1";

	public final static String SERIES = "100_ULBA";

	public final static String ULBASE = "ULBASE";

	public final static int MAX_RECORDS = 50;

	public static final String WS_OP_CORRESPONDENCE_GETCATEGORIES = "getListOfCategories"; //AXAL3.7.13

	public final static String WS_OP_CORRESPONDENCE_GETLETTERS = "getListOfDocuments"; //AXAL3.7.13

	public final static String WS_OP_CORRESPONDENCE_GETPDF = "requestDocumentsWithData"; //AXAL3.7.13

	public final static String WS_OP_CORRESPONDENCE_GETVARIABLES = "getDocumentVariables"; //AXAL3.7.13

	public static final String WS_OP_CORRESPONDENCE_GETCATEGORIES_RETURN = "getListOfCategoriesReturn"; //AXAL3.7.13

	public static final String WS_OP_CORRESPONDENCE_GETLETTERS_RETURN = "getListOfDocumentsReturn"; //AXAL3.7.13

	public static final String WS_OP_CORRESPONDENCE_GETVARIABLES_RETURN = "getDocumentVariablesReturn"; //AXAL3.7.13

	public static final String WS_OP_CORRESPONDENCE_GETPDF_RETURN = "streamContent"; //AXAL3.7.13

	public static final String WS_OP_TRANSMIT_ACCOUNTING_INFO = "transmitAccountingInformation"; //AXAL3.7.23

	public static final String WS_OP_SEND_PREMIUM_REFUND = "sendPremiumRefund";//AXAL3.7.28

	public static final String WS_OP_TRANSMIT_CHECK_DEPOSIT_INFO = "transmitCheckDepositInformation";//AXAL3.7.23

	public final static String WS_OP_PAL = "submitPALCompensation";//P2AXAL007

	public static final String WS_OP_SUBMIT_REINSURER_REQUEST = "submitReinsurance";//AXAL3.7.32

	public final static String WS_OP_VALIDATE_ZIP = "isZipCodeValid";//P2AXAL038

	public static final String WS_OP_CHECK_BILLING_UNIT_NUMBER = "validateSalaryAllotmentUnitCode";//P2AXAL041

	public static final String WS_OP_REG60DB_TRANSMIT = "submitPresaleData";//P2AXAL039

	public static final String WS_OP_L70_CALCULATIONS = "retrieveL70Calcs"; //P2AXAL016CV

	public static final String WS_OP_L70_PRINT_CALCULATIONS = "retrieveL70PrintCalcs"; //P2AXAL029

	public static final String WS_OP_SUBMIT_SUITABIITY_REQUEST = "EvaluateSuitability"; //P2AXAL021

	public static final String WS_OP_PREDICTIVE_ORCHESTRATION_REQUEST = "performPredictiveOrchestration"; //SR564247(APSL2525)

	public static final String WS_OP_CIP = "retrieveCIData"; //SR564247(APSL2525)

	public static final String WS_OP_REFUND_ACHPAYMENT = "refundACHPayment";//APSL2735

	public static final String WS_OP_SUBMIT_ACHPAYMENT = "submitACHPayment";//APSL2735

	public static final String WS_OP_INQUIRE_ACHPAYMENT = "inquireACHPayment";//APSL2735

	public static final String WS_OP_SUBMIT_SCOR = "submitUWRequestSCOR";//APSL2808

	public static final String WS_OP_UPDATE_SCOR = "submitUpdateRequestSCOR";//APSL2808

	public static final String WS_OP_RETRIEVE_SCOR = "retrieveUWResultSCOR";//APSL2808
	
	public static final String WS_OP_SEND_PRINT_PREVIEW_STATUS = "sendPrintPreviewStatus";//APSL5100
	
	public static final String WS_OP_SEARCH_CUSTOMER = "searchCustomer";// NBLXA-2152 
	public static final String WS_OP_BAE_CLIENT_RISK_SCORE ="retrieveClientRiskScore";// NBLXA-2152
	public static final String WS_OP_LEXIS_NEXIS_BRIDGER_RETRIEVE_CIDATA="bridgerRetrieveCIData";  // NBLXA-2152
	public static final String RS_MAGNUM_DETAILED_DECISION = "detailedDecision"; //NBLXA-2402(NBLXA-2494)
	public static final String RS_MAGNUM_BULK_SUBMIT_DELETION = "bulk/delete"; //NBLXA-2402(NBLXA-2531) US#362487
	public static final String RS_MAGNUM_GET_CASE_DATA = "data"; //NBLXA-2402(NBLXA-2550)	
	public static final String RS_MAGNUM_BULK = "bulk"; //NBLXA-2402(NBLXA-2534)
	public static final String RS_MAGNUM_GET_CASE_SUMMARY = "summary"; // NBLXA-2402(NBLXA-2557)	
	
}
