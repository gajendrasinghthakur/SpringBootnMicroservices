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
package com.csc.fsg.nba.webservice.invoke;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.webservice.invoke.magnum.AxaRSMagnumBulkInvoker;

/**
 * 
 * This is the factory class to get the instance of WebService invoker class. It decides which Web Service invoker
 * instance should be returned based-on entries passed.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead> 
 * <tr><td>AXAL3.7.18</td><td>Version 7</td><td>Producer Interface</td></tr>
 * <tr><td>P2AXAL007</td><td>AXA Life Phase 2</td><td>Producer and Compensation</td></tr>
 * <tr><td>P2AXAL041</td><td>AXA Life Phase 2</td><td>Message received from OLSA Unit Number Validation Interface</td></tr>
 * <tr><td>P2AXAL039</td><td>AXA Life Phase 2</td><td>Reg60 Database Interface</td></tr>
 * <tr><td>P2AXAL016CV</td><td>AXA Life Phase 2</td><td>Life 70 Calculations</td></tr>
 * <tr><td>P2AXAL029</td><td>AXA Life Phase 2</td><td>Contract Print</td></tr>
 * <tr><td>PP2AXAL021</td><td>AXA Life Phase 2</td><td>Suitability</td></tr>
 * <tr><td>SR564247(APSL2525)</td><td>Discretionary</td><td>Predictive Analytics Full Implementation</td></tr> 
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSInvokerFactory implements AxaWSConstants {

    /**
     * This method takes 3 arguments and reads the config file based on them and returns the instance of WebService client class.
     * Need to add condition for creating specific webservice invoker class instantiation
     * @param opearaion 
     * @return AxaWSInvokder which contains instance of specific webservice invokder
     * @throws NbaBaseException
     */
    public static AxaWSInvoker createWebServiceRequestor(String operation, NbaUserVO userVO, NbaTXLife nbaTxLife, NbaDst nbaDst, Object object)
			throws NbaBaseException {
		try {
			if (WS_OP_AGENTVALIDATION.equalsIgnoreCase(operation)) {
				return new AxaWSAgentValidationInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_SEARCHPRODUCER.equalsIgnoreCase(operation)) {
				return new AxaWSAgentSearchInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_AGENTDEMOGRAPICS.equalsIgnoreCase(operation)) {
				return new AxaWSAgentDemographicInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_CONTRACT_PRINT.equalsIgnoreCase(operation)) {
				return new AxaWSContractPrintInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_PCS.equalsIgnoreCase(operation)) {
				return new AxaWSPCSInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_RCS.equalsIgnoreCase(operation)) {
				return new AxaWSRCSInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_PIRS.equalsIgnoreCase(operation)) {
				return new AxaWSPIRSInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_RTS.equalsIgnoreCase(operation)) {
				return new AxaWSRTSInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_CONTRACT_SERVICE.equalsIgnoreCase(operation)) {
                return new AxaWSContractNumberInvoker(operation,userVO, nbaTxLife, nbaDst, object);
            } else if (WS_OP_SUBMIT_POLICY.equalsIgnoreCase(operation)) {
				return new AxaWSSubmitPolicyInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_SUBMIT_POLICY.equalsIgnoreCase(operation)
						&& NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT == Long.parseLong((String)object)) {
				return new AxaWSReinstatementPolicyInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_SUBMIT_POLICY.equalsIgnoreCase(operation) && NbaOliConstants.NBA_CHNGTYPE_INCREASE == Long.parseLong((String) object)) {
				return new AxaWSIncreasePolicyInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_RETRIEVE_POLICY.equalsIgnoreCase(operation)) {
				return new AxaWSRetrievePolicyInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_UNADMIT_REPL.equalsIgnoreCase(operation)) {//AXAL3.7.24
				return new AxaWSUnadmitReplInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_CIF_TRANSMIT.equalsIgnoreCase(operation)) {//AXAL3.7.25
				return new AxaWSCIFInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_ECSSUBMIT.equalsIgnoreCase(operation)) {//AXAL3.7.22
				return new AxaWSECSSubmitInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_ECSUPDATE.equalsIgnoreCase(operation)) {//AXAL3.7.22
				return new AxaWSECSUpdateInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_TAI_SERVICE_TRANSMIT.equalsIgnoreCase(operation)) {//AXAL3.7.16
				return new AXAWSTAITransmitInvoker(operation, userVO, nbaTxLife, nbaDst, object);				
			} else if (WS_OP_TAI_SERVICE_RETRIEVE.equalsIgnoreCase(operation)) {//AXAL3.7.05
				return new AXAWSTAIRetrieveInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_RET_PRIOR_INSURANCE.equalsIgnoreCase(operation)) {//AXAL3.7.05
				return new AXAWSRetrievePriorInsuranceInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_OLSA.equalsIgnoreCase(operation)) {
				return new AxaWSOLSAInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_UPD_PRIOR_INSURANCE.equalsIgnoreCase(operation)) {//AXAL3.7.21
				return new AXAWSPriorInsuranceTransmitInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_UPDATE_POLICY.equalsIgnoreCase(operation)) {
				return new AxaWSUpdatePolicyInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_ADD_PRIOR_INSURANCE.equalsIgnoreCase(operation)) {//AXAL3.7.21
				return new AXAWSPriorInsuranceInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.21
			} else if (WS_OP_VALIDATE_POLICY.equalsIgnoreCase(operation)) {//AXAL3.7.25
				return new AxaWSClientPolicyInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_CORRESPONDENCE_GETCATEGORIES.equalsIgnoreCase(operation)) {//AXAL3.7.13
				return new AxaWSCorresCategoryRequestInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.13
			} else if (WS_OP_CORRESPONDENCE_GETLETTERS.equalsIgnoreCase(operation)) {//AXAL3.7.13
				return new AxaWSCorresLettersRequestInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.13
			} else if (WS_OP_CORRESPONDENCE_GETVARIABLES.equalsIgnoreCase(operation)) {//AXAL3.7.13
				return new AxaWSCorresVariablesRequestInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.13
			} else if (WS_OP_CORRESPONDENCE_GETPDF.equalsIgnoreCase(operation)) {//AXAL3.7.13
				return new AxaWSCorresPDFRequestInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.13
			} else if (WS_OP_TRANSMIT_ACCOUNTING_INFO.equalsIgnoreCase(operation)) {//AXAL3.7.23
				return new AxaWSTransmitAccountingInfoInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.23
			} else if (WS_OP_SEND_PREMIUM_REFUND.equalsIgnoreCase(operation)) {//AXAL3.7.28
				return new AxaWSSendPremiumRefundInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.28
			} else if (WS_OP_TRANSMIT_CHECK_DEPOSIT_INFO.equalsIgnoreCase(operation)) {//AXAL3.7.23
				return new AxaWsTransmitCheckDepositInfoInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.23
			} else if (WS_OP_PAL.equalsIgnoreCase(operation)) {//P2AXAL007
				return new AxaWSPALInvoker(operation, userVO, nbaTxLife, nbaDst, object);//P2AXAL007
			} else if (WS_OP_SUBMIT_REINSURER_REQUEST.equalsIgnoreCase(operation)) {//AXAL3.7.32
				return new AxaWSReinsuranceRequestInvoker(operation, userVO, nbaTxLife, nbaDst, object);//AXAL3.7.32
			} else if (WS_OP_VALIDATE_ZIP.equalsIgnoreCase(operation)) {//P2AXAL038
				return new AxaWSValidateZipCodeInvoker(operation, userVO, nbaTxLife, nbaDst, object);//P2AXAL038
			} else if (WS_OP_CHECK_BILLING_UNIT_NUMBER.equalsIgnoreCase(operation)) {//P2AXAL041
				return new AxaWSCheckBillingUnitNumberInvoker(operation, userVO, nbaTxLife, nbaDst, object);//P2AXAL041
			} else if (WS_OP_REG60DB_TRANSMIT.equalsIgnoreCase(operation)){//P2AXAL039
				return new AXAWSReg60DatabaseInvoker(operation, userVO, nbaTxLife, nbaDst, object);//P2AXAL039
			} else if (WS_OP_L70_CALCULATIONS.equalsIgnoreCase(operation)) {//P2AXAL016CV
				return new AxaWSLife70CalculationInvoker(operation, userVO, nbaTxLife, nbaDst, object); //P2AXAL016CV
			} else if (WS_OP_L70_PRINT_CALCULATIONS.equalsIgnoreCase(operation)) {//P2AXAL029
				return new AxaWSLife70PrintCalculationInvoker(operation, userVO, nbaTxLife, nbaDst, object); //P2AXAL029
			} else if (WS_OP_SUBMIT_SUITABIITY_REQUEST.equalsIgnoreCase(operation)) {//P2AXAL021
				return new AxaWSSuitabilityRequestInvoker(operation, userVO, nbaTxLife, nbaDst, object); //PP2AXAL021
			} else if (WS_OP_PREDICTIVE_ORCHESTRATION_REQUEST.equalsIgnoreCase(operation)) {//SR564247(APSL2525) 
				return new AxaWSPredictiveOrchestrationInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_CIP.equalsIgnoreCase(operation)) {//SR564247(APSL2525) 
				return new AxaWSCIPInvoker(operation, userVO, nbaTxLife, nbaDst, object); 
			} else if (WS_OP_SUBMIT_ACHPAYMENT.equalsIgnoreCase(operation) 
					|| WS_OP_INQUIRE_ACHPAYMENT.equalsIgnoreCase(operation)
					|| WS_OP_REFUND_ACHPAYMENT.equalsIgnoreCase(operation)) { //APSL2735
				return new AxaWSElectronicPaymentInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_SUBMIT_SCOR.equalsIgnoreCase(operation) 
							||WS_OP_UPDATE_SCOR.equalsIgnoreCase(operation)
							||WS_OP_RETRIEVE_SCOR.equalsIgnoreCase(operation)) { //APSL2808
				return new AxaWSSCORInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_SEND_PRINT_PREVIEW_STATUS.equalsIgnoreCase(operation)) { //APSL5100 
				return new AxaWSSendPrintStatusInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_LEXIS_NEXIS_BRIDGER_RETRIEVE_CIDATA.equalsIgnoreCase(operation)) { //APSL5100 
				return new AxaWSLexisNexisCIPInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_SEARCH_CUSTOMER.equalsIgnoreCase(operation)) {// NBLXA-2152
				return new AxaWSSearchCustomerInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (WS_OP_BAE_CLIENT_RISK_SCORE.equalsIgnoreCase(operation)) {// NBLXA-2152
				return new AxaWSBaeClientRiskScoreInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (RS_MAGNUM_DETAILED_DECISION.equalsIgnoreCase(operation)) { //NBLXA-2402(NBLXA-2494)
				return new AxaRSMagnumDetailedDecisionInvoker(operation, userVO, nbaTxLife, nbaDst, object); //NBLXA-2402(NBLXA-2494)
			} else if (RS_MAGNUM_BULK_SUBMIT_DELETION.equalsIgnoreCase(operation)) { // NBLXA-2402(NBLXA-2531)
				return new AxaRSMagnumBulkSubmitDeletionInvoker(operation, userVO, nbaTxLife, nbaDst, object); // NBLXA-2402(NBLXA-2531)
			} else if (RS_MAGNUM_GET_CASE_DATA.equalsIgnoreCase(operation)) {// NBLXA-2402(NBLXA-2550)
				return new AxaRSMagnumGetDataModelInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} else if (RS_MAGNUM_BULK.equalsIgnoreCase(operation)) { // NBLXA-2402(NBLXA-2534)
				return new AxaRSMagnumBulkInvoker(operation, userVO, nbaTxLife, nbaDst, object); // NBLXA-2402(NBLXA-2534)
			} else if (RS_MAGNUM_GET_CASE_SUMMARY.equalsIgnoreCase(operation)) {// NBLXA-2402(NBLXA-2557)
				return new AxaRSMagnumCaseSummaryInvoker(operation, userVO, nbaTxLife, nbaDst, object);
			} 

			return null;
		} catch (Exception exp) {
			NbaBaseException nce = new NbaBaseException(exp);
			NbaLogFactory.getLogger(AxaWSInvoker.class).logException("Unable to create WebService request for " + operation, nce); //NBA103
			throw nce;
		}
	}

}
