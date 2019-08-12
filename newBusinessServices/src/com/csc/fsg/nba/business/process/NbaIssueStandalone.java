package com.csc.fsg.nba.business.process;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.accel.ui.util.SortingHelper;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.business.transaction.NbaInforcePaymentTransaction;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.FinancialActivityExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payment;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;
/**
 * NbaIssueStandalone performs standalone mode processing for Reinstatements, Increases and Issue. It creates the
 * appropriate TXLife transaction for the request and invokes a Web Service adaptor.  The Web Service adaptor generates
 * the the back end specific transaction and communicates with back end system. 
 * If the case is Reinstatement create a TXLife 508 transaction. Call the web service adaptor. If the reinstatment 
 * is successful mark the the lapsed policy as active. 
 * If the Case is an Increase, create a TXLife TransType=103, TransSubType=1000500004, changeSubType=1000500028 transaction 
 * from the increase rider in the nbaTxLife and workitem's LOBs. Call the web service adaptor.
 * Otherwise treat the case as a new issue. Create a TXLife TransType 103, TransSubType 1000500004 transaction 
 * and call the web service adaptor.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR1931</td><td>Version 5</td><td>Order of Trans Val and Issue Process is Wrong - Should Validate First</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>SPR2248</td><td>Version 6</td><td>Reinstatements not sent correctly for Traditional Term products and Advanced Products</td></tr>
 * <tr><td>SPR2968</td><td>Version 6</td><td>Test web service should determine XML file name dynamically</td></tr>
 * <tr><td>NBA126</td><td>Version 6</td><td>Vantage Contract Change</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement/Reinsurance Changes</td></tr>
 * <tr><td>SPR3158</td><td>Version 7</td><td>Received 'Out of Bounds Exception' on Replacement Case</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR1738</td><td>AXA Life Phase 1</td><td>Substandard Rating Should be Arranged Correctly In ACORD XML Message Out from nbA</td></tr>
 * <tr><td>SPR3143</td><td>Version 8</td><td>Proposed Substandard Ratings Should be Ignored in Creating XML Message to Backend Systems</td></tr>
 * <tr><td>AXAL3.7.17</td><td>AXA Life Phase 1</td><td>CAPS Interface</td>
 * <tr><td>AXAL3.7.05</td><td>AXA Life Phase 1</td><td>Prior Insurance</td>
 * <tr><td>NBA228</td><td>Version 8</td><td>Cash Management Enhancement</td>
 * <tr><td>AXAL3.7.22</td><td>AXA Life Phase 1</td><td>Compensation Interface</td>
 * <tr><td>AXAL3.7.23</td><td>Axa Life Phase 1</td><td>Accounting Interface</td></tr>
 * <tr><td>AXAL3.7.26</td><td>Axa Life Phase 1</td><td>OLSA Interface</td></tr>
 * <tr><td>ALS4467</td><td>Axa Life Phase 1</td><td>QC #3235 - 3.7.17 CAPS - duplicate policies sent to CAPS submitAdministrationPolicy in QA</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr> 
 * <tr><td>P2AXAL010</td><td>AXA Life Phase 2</td><td>Life 70 Issue/Reissue</td></tr>
 *  </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaIssueStandalone {
	protected boolean debugLogging;
	protected NbaLogger logger = null;
	protected NbaDst nbaDst;
	protected NbaLob nbaLob;
	protected NbaTXLife nbaTxLife;
	protected NbaUserVO nbaUserVO;
	protected NbaTXLife response;
	private boolean ISSUE_PROCESS = false; //APSL459
	/**
	 * Default constructor.
	 */
	//APSL459
	public NbaIssueStandalone() {
		super();
	}
	//APSL459
	public NbaIssueStandalone(boolean isIssueProcess) {
		this.ISSUE_PROCESS = isIssueProcess;
	}
	/**
	 * Add the PolicyProduct information for the Contract to the OLifE.
	 * @param olifeIssue
	 * @param holdingIssue
	 * @throws NbaBaseException when unable to retrieve the PolicyProduct for the Contract
	 */
	protected void addPolicyProduct(OLifE olifeIssue, Holding holdingIssue) throws NbaBaseException {
		try {
			if (holdingIssue.hasPolicy()) {
				ApplicationInfo appInfoIssue = new ApplicationInfo();
				Date signedDate;
				if (holdingIssue.getPolicy().hasApplicationInfo()) {
					appInfoIssue = holdingIssue.getPolicy().getApplicationInfo();
					signedDate = appInfoIssue.getSignedDate();
				} else {
					signedDate = Calendar.getInstance().getTime();
				}
				NbaProductAccessFacadeBean npa = new NbaProductAccessFacadeBean();  //NBA213
				AccelProduct nbaprod = npa.doProductInquiry(nbaTxLife); //NBA237
				olifeIssue.setPolicyProduct(nbaprod.getOLifE().getPolicyProduct());
			}
		} catch (Exception exp) {
			throw new NbaBaseException(
				"Unable to load Policy Product information for " + holdingIssue.getPolicy().getProductCode(),
				exp,
				NbaExceptionType.ERROR);
		}
	}
	/**
	 * Create a new FinancialActivity object with FinActivityType of REINPYMT - 248. In this new
	 * FinancialActivity object the AcctgExtractInd will be set to true so that extracts will not be generated for this
	 * Reinstatement payment.
	 * @param suspenseFinActivity FinancialActivity object
	 * @param olifeId NbaOLifEId
	 * @return FinancialActivity
	 */
	protected FinancialActivity createFinActForReinstatement(FinancialActivity suspenseFinActivity, NbaOLifEId olifeId) {
		FinancialActivity finActForReinstatement = suspenseFinActivity.clone(false);
		finActForReinstatement.deleteId();
		olifeId.setId(finActForReinstatement);
		finActForReinstatement.setAccountingActivity(new ArrayList());
		finActForReinstatement.setFinActivityType(NbaOliConstants.OLI_FINACT_REINPYMT);
		FinancialActivityExtension finActExtension = NbaUtils.getFirstFinancialActivityExtension(finActForReinstatement);
		if (finActExtension != null) {
			finActExtension.setDisbursedInd(false);
			finActExtension.setAcctgExtractInd(true);
			finActExtension.deleteErrCorrInd();
		}
		return finActForReinstatement;
	}
	/**
	 * Create an Increase (103 with changeSubType 1000500028)transaction from holding inquiry
	 * and workitem's LOBs to process a increase request on admin system. 
	 * @return the NbaTXLife for the Increase transaction
	 * @exception throws NbaBaseException when an Increase rider can not be located
	 */
	protected NbaTXLife createIncreaseTransaction() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setChangeSubType(NbaOliConstants.NBA_CHNGTYPE_INCREASE);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getNbaUserVO())); //SPR2639
		nbaTXRequest.setNbaLob(getNbaLob());
		//create txlife with default request fields
		NbaTXLife txLifeIncrease = new NbaTXLife(nbaTXRequest);
		OLifE olifeIncrease = txLifeIncrease.getOLifE();
		Policy policyIncrease = txLifeIncrease.getPolicy();
		OLifE nbaOlife = getNbaTxLife().getOLifE();
		Policy nbaPolicy = getNbaTxLife().getPolicy();
		policyIncrease.setCarrierAdminSystem(getNbaLob().getBackendSystem());
		policyIncrease.setReinsuranceInd(nbaPolicy.getReinsuranceInd());
		if (!policyIncrease.hasApplicationInfo()) {
			policyIncrease.setApplicationInfo(new ApplicationInfo());
		}
		policyIncrease.getApplicationInfo().setUserCode(nbaPolicy.getApplicationInfo().getUserCode());
		Set partyIds = new HashSet();
		if (nbaTxLife.isLife()) {
			Life nbaLife = nbaTxLife.getLife();
			Life lifeIncrease = null;
			if (txLifeIncrease.isLife()) {
				lifeIncrease = txLifeIncrease.getLife();
			} else {
				LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty lifeAnnDisProp = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
				lifeAnnDisProp.setLife(new Life());
				lifeIncrease = lifeAnnDisProp.getLife();
				policyIncrease.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(lifeAnnDisProp);
			}
			int maxPhaseCode = getMaxPhaseCode(nbaLife);
			for (int i = 0; i < nbaLife.getCoverageCount(); i++) {
				Coverage coverage = nbaLife.getCoverageAt(i);
				//Increase rider
				if (NbaOliConstants.DATAREP_TYPES_FULL.equalsIgnoreCase(coverage.getDataRep())) {
					//Set key if not present
					if (coverage.getCoverageKey() == null || coverage.getCoverageKey().trim().length() == 0) {
						coverage.setCoverageKey(String.valueOf(++maxPhaseCode));
					}
					//keep all life parties in set and will be included in transaction later.
					for (int l = 0; l < coverage.getLifeParticipantCount(); l++) {
						partyIds.add(coverage.getLifeParticipantAt(l).getPartyID());
					}
					lifeIncrease.addCoverage(coverage.clone(false)); //SPR1738 - do not process benefits //ALS2688
				}
			}
			if (lifeIncrease.getCoverageCount() == 0) {
				throw new NbaBaseException("Unable to locate Increase Rider");
			}
		} else {
			throw new NbaBaseException("Unable to locate Increase Rider");
		}
		//if reinsurance indicator is true add the party and relation for the Assignee (rolecode 143).
		if (nbaPolicy.getReinsuranceInd()) {
			for (int index = 0; index < nbaOlife.getRelationCount(); index++) {
				if (nbaOlife.getRelationAt(index).getRelationRoleCode() == NbaOliConstants.OLI_REL_COVERTOREINSURER) { //NBA130
					partyIds.add(nbaOlife.getRelationAt(index).getRelatedObjectID()); //add it to the party id collection
				}
			}
		}
		Object parties[] = partyIds.toArray();
		for (int p = 0; p < parties.length; p++) {
			String partyId = (String) parties[p];
			for (int index = 0; index < nbaOlife.getPartyCount(); index++) {
				if (nbaOlife.getPartyAt(index).getId().equals(partyId)) {
					olifeIncrease.addParty(nbaOlife.getPartyAt(index));
					break;
				}
			}
			//check for all relation for this party
			for (int index = 0; index < nbaOlife.getRelationCount(); index++) {
				if (nbaOlife.getRelationAt(index).getRelatedObjectID().equals(partyId)) {
					olifeIncrease.addRelation(nbaOlife.getRelationAt(index));
				}
			}
		}
		if (isDebugLogging()) {
			getLogger().logDebug("Increase 103 Transaction " + txLifeIncrease.toXmlString());
		}

		return txLifeIncrease;
	}
	/**
	 * Create a 103 transaction from holding inquiry and workitem's LOBs 
	 * to issue/reissue a policy on admin system. 
	 * @return the NbaTXLife for the Issue transaction
	 * @throws NbaBaseException when unable to retrieve the PolicyProduct for the Contract
	 */
	protected NbaTXLife createIssueTransaction() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getNbaUserVO())); //SPR2639
		//create txlife with default request fields
		NbaTXLife txLifeIssue = new NbaTXLife(nbaTXRequest);
		OLifE olifeIssue = getNbaTxLife().getOLifE().clone(false);
		Holding holdingIssue = NbaTXLife.getPrimaryHoldingFromOLifE(olifeIssue);
		Policy policyIssue = holdingIssue.getPolicy(); //SPR1738
		//remove any replcement policies
		//begin SPR3158
		//Delete non-primary Holdings
		Iterator it = olifeIssue.getHolding().iterator();
		Holding issHolding;
		while (it.hasNext()) {
			issHolding = (Holding) it.next();

			// Remove all the system messages
			issHolding.setSystemMessage(new ArrayList());//AXAL3.7.17

			if (issHolding.getId() != holdingIssue.getId()) {
				// Begin AXAL3.7.17
				Iterator relationIterator = olifeIssue.getRelation().iterator();
				// Delete the relations which refers non primary holdings
				while (relationIterator.hasNext()) {
					Relation relation = (Relation) relationIterator.next();
					if (relation != null && issHolding.getId().equalsIgnoreCase(relation.getOriginatingObjectID())) {
						if (relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO) {
							Iterator partyIterator = olifeIssue.getParty().iterator();
 							while (partyIterator.hasNext()) {
								// Delete the holding company party
								Party party = (Party) partyIterator.next();
								if (party != null && party.getId().equalsIgnoreCase(relation.getRelatedObjectID())) {
									partyIterator.remove();
								}
							}
						}
						relationIterator.remove();
					}	
				}
				//End AXAL3.7.17
				it.remove();
			}
		}

		it = olifeIssue.getRelation().iterator();
		Relation issRelation;
		while (it.hasNext()) {
			issRelation = (Relation) it.next();
			if (issRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_REPLACEDBY) {
				it.remove();
			}
		}
		//end SPR3158
		//Delete Risk information
		int count = olifeIssue.getPartyCount(); //SPR3158
		String bes = getNbaLob().getBackendSystem();//AXAL3.7.17
		// Do not delete the risk information, if the backend system is CAPS
		if (!NbaConstants.SYST_CAPS.equalsIgnoreCase(bes)) {//AXAL3.7.17
			for (int i = 0; i < count; i++) {
				olifeIssue.getPartyAt(i).deleteRisk();
			}
		}
		//Delete ProductObjective information
		if (holdingIssue.hasInvestment()) {
			count = holdingIssue.getInvestment().getSubAccountCount();
			for (int i = 0; i < count; i++) {
				holdingIssue.getInvestment().getSubAccountAt(i).deleteProductObjective();
			}
		}
		//Set the ghost values to non null to prevent them from being retrieved from the database.
		holdingIssue.setIntentGhost(new ArrayList());
		policyIssue.setAltPremModeGhost(new ArrayList()); //SPR1738
		policyIssue.setRequirementInfoGhost(new ArrayList()); //SPR1738
		count = policyIssue.getFinancialActivityCount(); //SPR1738
			
		//NBA228 code deleted
		NbaOLifEId olifeId= new NbaOLifEId(txLifeIssue);//ALS2655 
		processFinancialActivities(policyIssue,olifeId); //NBA228//ALS2655 

		if (olifeIssue.getSourceInfo().getFileControlID().equalsIgnoreCase(NbaConstants.SYST_CYBERLIFE)) {
			addPolicyProduct(olifeIssue, holdingIssue); //add policy product information
		}
		UserAuthRequestAndTXLifeRequest requestIssue = txLifeIssue.getTXLife().getUserAuthRequestAndTXLifeRequest();
		if (requestIssue != null) {
			if (requestIssue.getTXLifeRequestCount() > 0) {
				requestIssue.getTXLifeRequestAt(0).setOLifE(olifeIssue);
			}
		}
		//For Life contracts, organize the SubstandardRating objects as per acord format
		organizeRatings(policyIssue); //SPR1738
		if (isDebugLogging()) {
			getLogger().logDebug("Issue 103 Transaction " + txLifeIssue.toXmlString());
		}
		return txLifeIssue;
	}
	
	/**
	 * Create a xml508 transaction so that it can be passed to CLIF adapter or VNTG webservice. For CyberLife, the adapter
	 * creates a U101 Reinstatement payment.
	 * @param suspenseFinActivity FinancialActivity object
	 * @param olifeId NbaOLifEId
	 * @return xml508 NbaTXLife transaction
	 * @exception throws NbaBaseException
	 */
	protected NbaTXLife createXml508Transaction(FinancialActivity suspenseFinActivity, NbaOLifEId olifeId) throws NbaBaseException{
		NbaTXLife xml508 = new NbaInforcePaymentTransaction().createTXLife508(getNbaDst(), getNbaLob(), getNbaUserVO());
		Policy policy = xml508.getPrimaryHolding().getPolicy();
		Policy contractPolicy = getNbaTxLife().getPrimaryHolding().getPolicy();
		policy.setPolNumber(contractPolicy.getPolNumber());
		policy.setProductCode(contractPolicy.getProductCode());
		policy.setProductType(contractPolicy.getProductType());	//SPR2248
		policy.setCarrierCode(contractPolicy.getCarrierCode());
		policy.setPaymentDueDate(contractPolicy.getPaymentDueDate());
		policy.setReinstatementDate(contractPolicy.getReinstatementDate());
		ArrayList finActivities = policy.getFinancialActivity();
		int finActCount = finActivities.size();
		for (int i = finActCount; i > 0; i--) {
			policy.removeFinancialActivityAt(i - 1);
		}
		policy.addFinancialActivity(createFinActForReinstatement(suspenseFinActivity, olifeId));
		if (isDebugLogging()) {
			getLogger().logDebug("Reinstatement 508 Transaction " + xml508.toXmlString());
		}
		return xml508;
	}
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Returns highest coverage phase code on a contract 
	 * @param life the Life object
	 * @return the highest phase code
	 */
	protected int getMaxPhaseCode(Life life) {
		int phaseCode = 0;
		for (int i = 0; i < life.getCoverageCount(); i++) {
			Coverage coverage = life.getCoverageAt(i);
			if (coverage.hasCoverageKey() && coverage.getCoverageKey().trim().length() > 0) {
				try {
					int temp = Integer.parseInt(coverage.getCoverageKey());
					if (temp > phaseCode) {
						phaseCode = temp;
					}
				} catch (NumberFormatException ne) {
					//expected so ignore
				}
			}
		}
		return phaseCode;
	}
	/**
	 * Answer the NbaDst for the Case
	 * @return NbaDst
	 */
	protected NbaDst getNbaDst() {
		return nbaDst;
	}
	/**
	 * Answer the NbaLob for the Case
	 * @return NbaLob
	 */
	protected NbaLob getNbaLob() {
		return nbaLob;
	}
	/**
	 * Answer the NbaTxLife for the Case
	 * @return NbaTxLife
	 */
	protected NbaTXLife getNbaTxLife() {
		return nbaTxLife;
	}
	/**
	 * Answer the NbaUserVO for the user
	 * @return NbaUserVO
	 */
	protected NbaUserVO getNbaUserVO() {
		return nbaUserVO;
	}
	/**
	 * Return the response NbaTXLife
	 * @return NbaTXLife
	 */
	protected NbaTXLife getResponse() {
		return response;
	}
	/**
	 * Return the FinancialActivity object which has FinActivityType of UNAPPLDCASHIN - 278
	 * @return FinancialActivity
	 */
	protected FinancialActivity getSuspenseMoney() {
		Policy policy = getNbaTxLife().getPrimaryHolding().getPolicy();
		int finActCount = policy.getFinancialActivity().size();
		FinancialActivity finActivity = null;
		for (int i = 0; i < finActCount; i++) {
			finActivity = policy.getFinancialActivityAt(i);
			if (NbaOliConstants.OLI_LU_FINACT_UNAPPLDCASHIN == finActivity.getFinActivityType()) {
				return finActivity;
			}
		}
		return null;
	}
	/**
	 * Return true if debug logging is enabled.
	 * @return boolean - debugLogging 
	 */
	protected boolean isDebugLogging() {
		return debugLogging;
	}
 
	/**
	 * Return true if the aTXLife response indicates an error. 
	 * @return boolean
	 */
	protected boolean isErrorResponse() {
		return getResponse() != null && getResponse().isTransactionError();
	}
	/**
	 * Determine if the Case is an increase by checking the CHTP LOB field for a value of 1000500028 
	 * @return true if the Case is an increase
	 */
	protected boolean isIncrease() {
		return getNbaLob().getContractChgType() != null && NbaOliConstants.NBA_CHNGTYPE_INCREASE == Long.parseLong(getNbaLob().getContractChgType());
	}
	//	APSL459 Method Deleted
	/**
	 * Determine if the Case is a reinstatement by checking the CHTP LOB field for a value of 1000500900 
	 * @return true if the Case is a reinstatement
	 */
	protected boolean isReinstatment() {
		return getNbaLob().getContractChgType() != null
			&& NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT == Long.parseLong(getNbaLob().getContractChgType());
	}
	/**
	 * Return a modified copy of the coverage object. Move the first occurance of
	 * SubStandardRating to parent LifeParticipant object. 2-n occurance will
	 * be added as SubStandardRating to the LifeParticipant.
	 * @param coverage the Coverage object
	 * @param processBenefits indicator to tell whether to organize ratings on benefits or not.  
	 * @return the modified Coverage object
	 */
	//SPR1738 added new parameter - processBenefits
	protected Coverage prepareCoverageForSubStandardRating(Coverage coverage, boolean processBenefits) {
		Coverage clonedCoverage = coverage.clone(false);
		for (int k = 0; k < coverage.getLifeParticipantCount(); k++) {
			LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(k);
			LifeParticipant clonedLifeParticipant = clonedCoverage.getLifeParticipantAt(k);
			//begin SPR3143
			int ratingCount = lifeParticipant.getSubstandardRatingCount();
            int z = 0;
            for (; z < ratingCount; z++) {
				clonedLifeParticipant.getSubstandardRating().clear();
				SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(z);
				SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
                if (substandardRatingExtension != null && !substandardRatingExtension.getProposedInd()) {
	                //end SPR3143
				LifeParticipantExtension clonedLifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(clonedLifeParticipant);
				if (clonedLifeParticipantExtension == null) {
					OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_LIFEPARTICIPANT);
					clonedLifeParticipantExtension = olifeExt.getLifeParticipantExtension();
					clonedLifeParticipant.addOLifEExtension(olifeExt);
				}
				clonedLifeParticipant.setRatingReason(substandardRating.getRatingReason());
				clonedLifeParticipant.setPermTableRating(substandardRating.getPermTableRating());
				clonedLifeParticipant.setTempTableRating(substandardRating.getTempTableRating());
				clonedLifeParticipant.setTempTableRatingEndDate(substandardRating.getTempTableRatingEndDate());
				clonedLifeParticipant.setTempFlatEndDate(substandardRating.getTempFlatEndDate());
				clonedLifeParticipant.setTempFlatExtraAmt(substandardRating.getTempFlatExtraAmt());
				clonedLifeParticipant.setRatingCommissionRule(substandardRating.getRatingCommissionRule());
				if (substandardRatingExtension != null) {
					clonedLifeParticipant.setPermFlatExtraAmt(substandardRatingExtension.getPermFlatExtraAmt());
					clonedLifeParticipantExtension.setEffDate(substandardRatingExtension.getEffDate());
					clonedLifeParticipantExtension.setSpecialClass(substandardRatingExtension.getSpecialClass());
					clonedLifeParticipantExtension.setPermPercentageLoading(substandardRatingExtension.getPermPercentageLoading());
					clonedLifeParticipantExtension.setRatingStatus(substandardRatingExtension.getRatingStatus()); //SPR1738
					clonedLifeParticipantExtension.setDuration(substandardRatingExtension.getDuration()); //SPR1738

				}
				//begin SPR3143
					break;
				} 
			}
			for (int i = z + 1; i < ratingCount; i++) {
				SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(lifeParticipant.getSubstandardRatingAt(i));
	        	//skip any proposed ratings
	            if (substandardRatingExtension == null || !substandardRatingExtension.getProposedInd()) { //SPR3143
	            	clonedLifeParticipant.addSubstandardRating(lifeParticipant.getSubstandardRatingAt(i));
	            }				
			}
		} 
		//end SPR3143
		//begin SPR1738
		if (processBenefits) {
			int covOptCount = coverage.getCovOptionCount();
			for (int i = 0; i < covOptCount; i++) {
				clonedCoverage.setCovOptionAt(prepareCovOptionForSubStandardRating(coverage.getCovOptionAt(i)), i);
			}
		}
		//end SPR1738
		return clonedCoverage;
	}
	/**
	 * @throws NbaBaseException
	 */
	/**
	 * Perform standalone mode processing for Reinstatements, Increases and Issue. If the case is Reinstatement case create a xml508 transaction. Call
	 * the web service adaptor. If the communication is successful mark the the lapsed policy as active. If the Case is an increase, create a TXLife
	 * TransType=103, TransSubType=1000500004, changeSubType=1000500028 transaction from the increase rider in the nbaTxLife and workitem's LOBs. Call
	 * the web service adaptor. Otherwise treat the case as a new issue. Create a TXLife TransType 103, TransSubType 1000500004 transaction and call
	 * the web service adaptor.
	 * @param nbaUserVO
	 * @param nbaDst
	 * @param nbaTxLife
	 * @return NbaTXLife rsponse
	 * @throws NbaBaseException
	 */
	public NbaTXLife processStandalone(NbaUserVO nbaUserVO, NbaDst nbaDst, NbaTXLife nbaTxLife) throws NbaBaseException {
		setDebugLogging(getLogger().isDebugEnabled());
		setNbaUserVO(nbaUserVO);
		setNbaDst(nbaDst);
		setNbaLob(getNbaDst().getNbaLob());
		setNbaTxLife(nbaTxLife);
		//Call web service
		//begin NBA126
		String bes = getNbaLob().getBackendSystem();
		boolean reinstatment = isReinstatment();
		NbaWebServiceAdapter service = null;
		if (reinstatment && NbaConstants.SYST_VANTAGE.equalsIgnoreCase(bes)) {
			service = NbaWebServiceAdapterFactory.createWebServiceAdapter(bes, "Payment", "InfPayment");
		} else if (!(NbaConstants.SYST_CAPS.equalsIgnoreCase(bes) || NbaConstants.SYST_LIFE70.equalsIgnoreCase(bes))) {//AXAL3.7.17 P2AXAL010
			service = NbaWebServiceAdapterFactory.createWebServiceAdapter(bes, "InforceSubmit", "InforceSubmit");
		}
		if (reinstatment) {
			//end NBA126
			FinancialActivity suspenseFinActivity = getSuspenseMoney();
			NbaOLifEId olifeId = new NbaOLifEId(getNbaTxLife());
			NbaTXLife xml508 = createXml508Transaction(suspenseFinActivity, olifeId);
			//begin AXAL3.7.17
			if (NbaConstants.SYST_CAPS.equalsIgnoreCase(bes)) {
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SUBMIT_POLICY, nbaUserVO,
						nbaTxLife, getNbaDst(), Long.toString(NbaOliConstants.NBA_CHNGTYPE_REINSTATEMENT));
				setResponse((NbaTXLife) webServiceInvoker.execute());
			} else {
				setResponse(service.invokeWebService(xml508)); // SPR2968
			}
			//end AXAL3.7.17
			if (!isErrorResponse()) {
				getNbaTxLife().getPrimaryHolding().getPolicy().setPolicyStatus(NbaOliConstants.OLI_POLSTAT_ACTIVE);
			}
		} else if (isIncrease()) {
			//begin AXAL3.7.17
			if (NbaConstants.SYST_CAPS.equalsIgnoreCase(bes)) {
				AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SUBMIT_POLICY, nbaUserVO, 
						nbaTxLife, getNbaDst(), Long.toString(NbaOliConstants.NBA_CHNGTYPE_INCREASE));
				setResponse((NbaTXLife) webServiceInvoker.execute());
			} else {
				setResponse(service.invokeWebService(createIncreaseTransaction())); // SPR2968
			}
			//end AXAL3.7.17
		} else {
			//begin AXAL3.7.17
			if (NbaConstants.SYST_CAPS.equalsIgnoreCase(bes) || NbaConstants.SYST_LIFE70.equalsIgnoreCase(bes)) {//P2AXAL010
				if (ISSUE_PROCESS) {//AXAL3.7.17PC, APSL459
					AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_SUBMIT_POLICY, nbaUserVO, nbaTxLife, getNbaDst(), null);
					setResponse((NbaTXLife) webServiceInvoker.execute());
				}else {
					AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_UPDATE_POLICY, nbaUserVO, nbaTxLife, getNbaDst(), null);
					setResponse((NbaTXLife) webServiceInvoker.execute());	
				}
			} else {
				setResponse(service.invokeWebService(createIssueTransaction())); // SPR2968
			}
			handleWebServiceFailure(getResponse());
			//end AXAL3.7.17
			//ALS4467 code deleted..moved to NbaProcPostIssue
		}
		//begin AXAL3.7.22
		//ALS4467 code deleted..moved to NbaProcPostIssue
		return getResponse();
	}

	/**
	 * @throws NbaBaseException
	 */
	//AXAL3.7.26 new method added.
	protected void invokeOLSA() throws NbaBaseException {
		AxaWSInvoker webServiceInvoker = AxaWSInvokerFactory.createWebServiceRequestor(AxaWSConstants.WS_OP_OLSA, getNbaUserVO(), getNbaTxLife(),
				null, null);
		webServiceInvoker.execute();
	}
	/**
	 * 
	 */
	//AXAL3.7.26 new method added.
	protected boolean isOLSACallNeeded() throws NbaBaseException {
		NbaVpmsAdaptor olsaVpmsAdaptor = getVpmsAdaptor();
		olsaVpmsAdaptor.setVpmsEntryPoint(NbaVpmsAdaptor.EP_OLSACALLNEEDED);
		Map deOinkMap = new HashMap();
		deOinkMap.put("A_PaymentFound", getNbaTxLife().getPolicy().getFinancialActivityCount() > 0 ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
		deOinkMap.put("A_OLSAPaymentFound", isOLSAPaymentFound() ? NbaConstants.TRUE_STR : NbaConstants.FALSE_STR);
		olsaVpmsAdaptor.setSkipAttributesMap(deOinkMap);
		VpmsComputeResult computeResult = null;
		try {
			computeResult = olsaVpmsAdaptor.getResults();
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_STATUS, re);
        //Begin APSL588	
		} finally {
			try {
				if (olsaVpmsAdaptor != null) {
					olsaVpmsAdaptor.remove();
				}
			} catch (Throwable th) {
				getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		//End APSL588
		}
		return NbaConstants.TRUE_STR.equalsIgnoreCase(computeResult.getResult());
	}
	
	/**
	 * @param nbaDst
	 * @return
	 * @throws NbaBaseException
	 * @throws NbaVpmsException
	 */
	//AXAL3.7.26 new method added.
	protected NbaVpmsAdaptor getVpmsAdaptor() throws NbaBaseException, NbaVpmsException {
		NbaVpmsAdaptor olsaVpmsAdaptor;
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
		oinkData.setContractSource(getNbaTxLife());
		olsaVpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.ISSUE);
		return olsaVpmsAdaptor;
	}
	
	/**
	 * @return
	 */
	//AXAL3.7.26 new method added.
	protected boolean isOLSAPaymentFound() {
		boolean olsaPaymentFound = false;
		Iterator finActItr = getNbaTxLife().getPolicy().getFinancialActivity().iterator();
		outer: while (finActItr.hasNext()) {
			FinancialActivity finAct = (FinancialActivity) finActItr.next();
			if (NbaOliConstants.OLI_FINACT_PREMIUMINIT == finAct.getFinActivityType()) {
				Iterator paymentItr = finAct.getPayment().iterator();
				while (paymentItr.hasNext()) {
					Payment payment = (Payment) paymentItr.next();
					if (NbaOliConstants.SOURCE_FUNDS_DETAILS_1213.equalsIgnoreCase(payment.getSourceOfFundsDetails())) {
						olsaPaymentFound = true;
						break outer;
					}
				}
			}
		}
		return olsaPaymentFound;
	}
	
	/**
	 * Set the debug logging indicator
	 * @param b
	 */
	protected void setDebugLogging(boolean b) {
		debugLogging = b;
	}
	/**
	 * Set the NbaDst for the Case
	 * @param dst
	 */
	protected void setNbaDst(NbaDst dst) {
		nbaDst = dst;
	}
	/**
	 * Set the NbaLob for the Case
	 * @param lob
	 */
	protected void setNbaLob(NbaLob lob) {
		nbaLob = lob;
	}
	/**
	 * Set the NbaTxLife for the Case
	 * @param life
	 */
	protected void setNbaTxLife(NbaTXLife life) {
		nbaTxLife = life;
	}
	/**
	 * Set the NbaUserVO for the user
	 * @param userVO
	 */
	protected void setNbaUserVO(NbaUserVO userVO) {
		nbaUserVO = userVO;
	}
	/**
	 * Set the response NbaTXLife
	 * @param life
	 */
	protected void setResponse(NbaTXLife life) {
		response = life;
	}
	
	/**
	 * Organize SubstandardRatings on a Life contract.
	 * @param policyIssue policy object to be processed.
	 */
	//SPR1738 New Method
	protected void organizeRatings(Policy policyIssue){
		if (getNbaTxLife().isLife()) {
			Life life = policyIssue.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			int  covCount = life.getCoverageCount();
			for (int i = 0; i < covCount; i++) {
				life.setCoverageAt(life.getCoverageAt(i).clone(false), i);//ALS2688
			}
		}
	}
	/**
	 * Organize Substandard ratings for outbound coverage option objects  
	 * @param covOption
	 * @return
	 */
	//SPR1738 New Method
	protected CovOption prepareCovOptionForSubStandardRating(CovOption covOption){
        CovOption clonedCovOption = covOption.clone(false); //deep copy
        int origRatingCount = covOption.getSubstandardRatingCount();
        SubstandardRating origRating = null;
        SubstandardRatingExtension origRatingExtension = null;
        CovOptionExtension clonedCovOptExt = null;
        
        if (origRatingCount > 0) {
			origRating = covOption.getSubstandardRatingAt(0);
			origRatingExtension = NbaUtils.getFirstSubstandardExtension(origRating);
			clonedCovOption.getSubstandardRating().clear();
			clonedCovOption.setRatingReason(origRating.getRatingReason());
			clonedCovOption.setPermTableRating(origRating.getPermTableRating());
			clonedCovOption.setTempTableRating(origRating.getTempTableRating());
			clonedCovOption.setTempTableRatingEndDate(origRating.getTempTableRatingEndDate());
			clonedCovOption.setTempFlatEndDate(origRating.getTempFlatEndDate());
			clonedCovOption.setTempFlatExtraAmt(origRating.getTempFlatExtraAmt());
			clonedCovOption.setRatingCommissionRule(origRating.getRatingCommissionRule());
			clonedCovOptExt = NbaUtils.getFirstCovOptionExtension(clonedCovOption);
			//create covOption extension if not present.	
			if (clonedCovOptExt == null) {
				OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVOPTION);
				clonedCovOptExt = olifeExt.getCovOptionExtension();
				clonedCovOption.addOLifEExtension(olifeExt);
			}
			if (origRatingExtension != null) {
				clonedCovOption.setPermFlatExtraAmt(origRatingExtension.getPermFlatExtraAmt());
				clonedCovOption.setEffDate(origRatingExtension.getEffDate());
				clonedCovOption.setPermPercentageLoading(origRatingExtension.getPermPercentageLoading());
			}
		}
        for (int i = 1; i < origRatingCount; i++) {
			origRating = covOption.getSubstandardRatingAt(i);
			origRatingExtension = NbaUtils.getFirstSubstandardExtension(origRating);
			clonedCovOption.addSubstandardRating(covOption.getSubstandardRatingAt(i));
		}
		return clonedCovOption;
	}
	
	
	// AXAL3.7.17 New method
	protected void handleWebServiceFailure(NbaTXLife nbaTXLifeResponse ) throws NbaBaseException {
		if (nbaTXLifeResponse != null) {
			UserAuthResponseAndTXLifeResponseAndTXLifeNotify txlifeResponseParent =
				nbaTXLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
			if (txlifeResponseParent.getTXLifeResponseCount() > 0 && txlifeResponseParent.getTXLifeResponseAt(0).hasTransResult()) {
				TXLifeResponse txLifeResponse = txlifeResponseParent.getTXLifeResponseAt(0);
				TransResult transResult = txLifeResponse.getTransResult();
				long resultCode = transResult.getResultCode();
				if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
					//if failure (5) result code is received, then throw an exception to stop poller.
					throw new NbaBaseException("CAPS WebService not available", NbaExceptionType.FATAL);  
				}
			}
		}
	}
	/**
	 * The method processes the financial activities that are to be submitted to the admin system. The reverse/defunded financial activities are  
	 * removed. The applied financial activities are modified so that the financial activities represent the modal premium payments.
	 * 
	 * @param policyIssue Policy object
	 */	
	//NBA228 new method
	//ALS2655 Changed Signature
	private void processFinancialActivities(Policy policyIssue,NbaOLifEId olifeId) {
		//Delete reversal or refund FinancialActivity
		Iterator it = policyIssue.getFinancialActivity().iterator();
		FinancialActivity issFinancialActivity;
		List financialActivityList = new ArrayList();
		while (it.hasNext()) {
			issFinancialActivity = (FinancialActivity) it.next();
			if (issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWAREVERSAL
					|| issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWAREFUND) {
				it.remove();
			} else {
				financialActivityList.add(issFinancialActivity);
				it.remove();
			}
		}
		addModalFinancialActivities(financialActivityList, policyIssue,olifeId);//ALS2655
	}
	
	/**
	 * The method processes the financial activities that are to be submitted to the admin system. The total CWA amount is adjusted for 
	 * overage/shortage amount. The gross amount of the financial activities are modified and set equal to the modal premium
	 * payment amount. The total number of financial activities will be equal to the CWA amount divided by modal premium amount.
	 * 
	 * @param financialActivityList list of applied financial activities
	 * @param policyIssue Policy object
	 */	
	//NBA228 new method
	//	ALS2655 Changed Signature
	private void addModalFinancialActivities(List financialActivityList, Policy policyIssue,NbaOLifEId olifeId) {
		// Reaarange the list of financial activities in the ascending order of CWA date. In case, the number of modal premiums is lesser than the
		// total number of CWA payments, consider CWA payments with earlier dates
		rearrangeFinancialActivities(financialActivityList);
		long modalPayments = Math.round(policyIssue.getApplicationInfo().getCWAAmt() / policyIssue.getPaymentAmt());

		for (int i = 0; i < modalPayments; i++) {
			//update/create financial activities
			FinancialActivity pendFinActivity = null;
			if (i >= financialActivityList.size()) {
				//Clone the last financial activity
				pendFinActivity = ((FinancialActivity) financialActivityList.get(financialActivityList.size() - 1)).clone(false);
			} else {
				pendFinActivity = (FinancialActivity) financialActivityList.get(i);
			}
			//update the financial activity with gross amount = modal premium
			pendFinActivity.setFinActivityGrossAmt(policyIssue.getPaymentAmt());
			
			// ALS2655 Begin
			// APSL1289 Begin
			if (pendFinActivity.getPaymentAt(0) != null) {
				pendFinActivity.getPaymentAt(0).setId(null);
			}
			//APSL1289 End
			pendFinActivity.setId(null); // setting id to null to get unique id from olife
			olifeId.setId(pendFinActivity.getPaymentAt(0));
			olifeId.setId(pendFinActivity);
			// ALS2655 End
			policyIssue.addFinancialActivity(pendFinActivity);
		}
		//Update the total CWA Amount for including the overage/shortage amount
		policyIssue.getApplicationInfo().setCWAAmt(modalPayments * policyIssue.getPaymentAmt());
	}
	
	/**
	 * The method rearranges the financial activities in the acending order of financial activity date. 
	 * @param financialActivityList list of applied financial activities
	 */		
	//NBA228 new method
	private void rearrangeFinancialActivities(List finActivityList) {
		SortingHelper.sortData(finActivityList, true, "finActivityDate");

	}	
}
