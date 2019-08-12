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

import java.io.ByteArrayInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.accel.ui.util.SortingHelper;
import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.CovOptionProduct;
import com.csc.fs.dataobject.accel.product.CovOptionProductExtension;
import com.csc.fs.dataobject.accel.product.CoverageProduct;
import com.csc.fs.dataobject.accel.product.JurisdictionApproval;
import com.csc.fs.dataobject.accel.product.JurisdictionApprovalExtension;
import com.csc.fs.dataobject.accel.product.LifeProduct;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fs.dataobject.accel.product.PolicyProductExtension;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.MessageTrailer;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.BankingExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
/**
 * This class is responsible for creating request for Contract Print.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.17</td><td>AXA Life Phase 1</td><td>CAPS Interface</td></tr>
 * <tr><td>P2AXAL010</td><td>AXA Life Phase 2</td><td>Life 70 Issue/Reissue</td></tr>
 * <tr><td>CR60972</td><td>AXA Life Phase 2 R2</td><td>Free Form Text</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSSubmitPolicyInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "CAPSInforceSubmit";

	private static final String FUNCTIONID = "submitPolicy";
	
	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSSubmitPolicyInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}

	public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUserVO())); //SPR2639
		nbaTXRequest.setNbaLob(getNbaDst().getNbaLob());
		nbaTXRequest.setNbaUser(getUserVO());
		return createIssueTransaction(nbaTXRequest);
	}
	
	/**
	 * Create a 103 transaction from holding inquiry and workitem's LOBs 
	 * to issue/reissue a policy on admin system. 
	 * @return the NbaTXLife for the Issue transaction
	 * @throws NbaBaseException when unable to retrieve the PolicyProduct for the Contract
	 */
	protected NbaTXLife createIssueTransaction(NbaTXRequestVO nbaTXRequest) throws NbaBaseException {
		//create txlife with default request fields
		NbaTXLife txLifeIssue = new NbaTXLife(nbaTXRequest);
		OLifE olifeIssue = getNbaTXLife().getOLifE().clone(false);
		Holding holdingIssue = NbaTXLife.getPrimaryHoldingFromOLifE(olifeIssue);
		Policy policyIssue = holdingIssue.getPolicy(); //SPR1738
		//ALII1161 and ALII1165 Code deleted
		//Delete Risk information
		int count = olifeIssue.getPartyCount(); //SPR3158
		// ALII53 code deleted
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
		NbaOLifEId olifeId = new NbaOLifEId(txLifeIssue);//ALS2655
		processFinancialActivities(policyIssue, olifeId); //NBA228//ALS2655

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
		modify103Request(txLifeIssue);//AXAL3.7.17
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Issue 103 Transaction " + txLifeIssue.toXmlString());
		}
		return txLifeIssue;
	}
	
	/**
	 * The method processes the financial activities that are to be submitted to the admin system. The reverse/defunded financial activities are
	 * removed. The applied financial activities are modified so that the financial activities represent the modal premium payments.
	 * 
	 * @param policyIssue Policy object
	 */	
	//NBA228 new method
	//ALS2655 Changed Signature
	private void processFinancialActivities(Policy policyIssue, NbaOLifEId olifeId) {
		//Delete reversal or refund FinancialActivity
		Iterator it = policyIssue.getFinancialActivity().iterator();
		FinancialActivity issFinancialActivity;
		List financialActivityList = new ArrayList();
		boolean CAPS = NbaConstants.SYST_CAPS.equalsIgnoreCase(getNbaTXLife().getBackendSystem());
		while (it.hasNext()) {
			issFinancialActivity = (FinancialActivity) it.next();
			if (issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWAREVERSAL
					|| issFinancialActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_CWAREFUND) {
				it.remove();
			} else if (CAPS){ //P2AXAL010
				financialActivityList.add(issFinancialActivity);
				it.remove();
			}
		}
		if(CAPS) { //P2AXAL010
			addModalFinancialActivities(financialActivityList, policyIssue,olifeId);//ALS2655
		} else {
			addShortageFinancialActivityForPerm(policyIssue, olifeId); //P2AXAL010
		}
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
				AccelProduct nbaprod = npa.doProductInquiry(getNbaTXLife());
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
	 * Organize SubstandardRatings on a Life contract.
	 * @param policyIssue policy object to be processed.
	 */
	//SPR1738 New Method
	protected void organizeRatings(Policy policyIssue){
		if (getNbaTXLife().isLife()) {
			Life life = policyIssue.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
			int  covCount = life.getCoverageCount();
			for (int i = 0; i < covCount; i++) {
				life.setCoverageAt(life.getCoverageAt(i).clone(false), i);//ALS2688
			}
		}
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
		Double shortageWaiver=calculateShortageWaiver(financialActivityList);//NBLXA-2107
		long modalPayments = 1; //shortage or exact CWA amt condition, APSL2640
		System.out.println("shortageWaiver="+shortageWaiver);
		if(((policyIssue.getApplicationInfo().getCWAAmt()+shortageWaiver) - policyIssue.getPaymentAmt()) > 0) { //APSL2640 NBLXA-2107 
			 //overage condition
			 DecimalFormat  decimalFormat = new DecimalFormat("0.00");
			 String modalPaymentsdecimalValue=decimalFormat.format(((policyIssue.getApplicationInfo().getCWAAmt()+shortageWaiver) / policyIssue.getPaymentAmt()));
			 modalPayments = Math.round(Math.floor(new Double(modalPaymentsdecimalValue)));
		}
        System.out.println("modalPayments="+modalPayments);
		
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
			if (pendFinActivity.getPaymentCount() > 0) {
				pendFinActivity.getPaymentAt(0).setId(null);
				olifeId.setId(pendFinActivity.getPaymentAt(0));
			}
			pendFinActivity.setId(null); // setting id to null to get unique id from olife
			olifeId.setId(pendFinActivity);
			// ALS2655 End
			policyIssue.addFinancialActivity(pendFinActivity);
		}
		//Update the total CWA Amount for including the overage/shortage amount
		policyIssue.getApplicationInfo().setCWAAmt(modalPayments * policyIssue.getPaymentAmt());
	}
	
	//P2AXAL010 new method
	private void addShortageFinancialActivityForPerm(Policy policyIssue, NbaOLifEId olifeId) {
		List financialActivityList = policyIssue.getFinancialActivity();
		//Verify if the CWA amount is less than the minInitialPremiumAmount
		double diff = policyIssue.getApplicationInfo().getCWAAmt() - policyIssue.getMinPremiumInitialAmt();
		double shortage = (diff < 0) ? Math.abs(diff) : 0; //skips creating account extract for overage for advanced life products	
		if(shortage > 0 ) {
			FinancialActivity pendFinActivity = (FinancialActivity) financialActivityList.get(financialActivityList.size() - 1);
			//update the financial activity to account for shortage amount
			double newFinGrossAmt = pendFinActivity.getFinActivityGrossAmt() + shortage;
			pendFinActivity.setFinActivityGrossAmt(newFinGrossAmt);

			//Update the total CWA Amount to account for the shortage amount
			policyIssue.getApplicationInfo().setCWAAmt(policyIssue.getMinPremiumInitialAmt());
		}
	}
	
	/**
	 * The method rearranges the financial activities in the acending order of financial activity date. 
	 * @param financialActivityList list of applied financial activities
	 */		
	//NBA228 new method
	private void rearrangeFinancialActivities(List finActivityList) {
		SortingHelper.sortData(finActivityList, true, "finActivityDate");

	}
	
	/**
	 * This method modifies the xml 103 transaction and adds MortalityOrMorbidityTable, ReserveIntRate,AgeCalculationType from product database
	 * @param issueTxLife issuing NbaTXLife object
	 */
	//AXAL3.7.17 New Method
	public void modify103Request(NbaTXLife issueTxLife) throws NbaBaseException {
		Life life = null;
		Holding holdingIssue = issueTxLife.getPrimaryHolding();
		try {
			if (holdingIssue != null && holdingIssue.hasPolicy()) {
				NbaProductAccessFacadeBean npa = new NbaProductAccessFacadeBean();
				AccelProduct nbaprod = npa.doProductInquiry(issueTxLife);
				PolicyProduct policyProduct = nbaprod.getOLifE().getPolicyProductAt(0);
				PolicyProductExtension policyProductExtension = null;
				if (policyProduct != null) {
					policyProductExtension = AccelProduct.getFirstPolicyProductExtension(policyProduct); //NBA237
				}

				Policy policy = holdingIssue.getPolicy();
				if (policy != null) {
					if (policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty() != null) {
						life = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
					}
				}

				if (life != null) {
					//ALII1208 deleted
					int count = life.getCoverageCount();
					for (int i = 0; i < count; i++) {
						Coverage coverage = life.getCoverageAt(i);

						if (coverage != null && NbaOliConstants.OLI_COVIND_BASE == coverage.getIndicatorCode()) {
							CoverageExtension coverageExtension = NbaUtils.getFirstCoverageExtension(coverage);
							if (coverageExtension == null) {
								OLifEExtension oLifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVERAGE);
								coverage.addOLifEExtension(oLifeExtension);
								coverageExtension = oLifeExtension.getCoverageExtension();
							}
							if (policyProductExtension != null) {
								coverageExtension.setMortalityOrMorbidityTable(policyProductExtension.getMortalityOrMorbidityTable());
								coverageExtension.setReserveIntRate(policyProductExtension.getReserveIntRate());
							}
						}

						for (int k = 0; k < coverage.getLifeParticipantCount(); k++) {
							LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(k);
							if (lifeParticipant != null && NbaOliConstants.OLI_PARTICROLE_PRIMARY == lifeParticipant.getLifeParticipantRoleCode()) {
								LifeParticipantExtension lifeParticipantExtension = NbaUtils.getFirstLifeParticipantExtension(lifeParticipant);
								if (lifeParticipantExtension == null) {
									OLifEExtension oLifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_LIFEPARTICIPANT);
									lifeParticipant.addOLifEExtension(oLifeExtension);
									lifeParticipantExtension = oLifeExtension.getLifeParticipantExtension();
								}
								lifeParticipantExtension.setAgeCalculationType(policyProduct.getAgeCalculationType());
							}
						}
						
						//For LBR coverage option, the Filed Form number is populated in the CovOption from ppfl
						CovOption covOption = NbaUtils.getCovOption(coverage, NbaConstants.COVOPTION_LBR);
						ApplicationInfo applicatinInfo = policy.getApplicationInfo();
						long jurisdiction = applicatinInfo.getApplicationJurisdiction();
						if (covOption != null) {
							String productCode = covOption.getProductCode();
							LifeProduct lifeProduct = policyProduct.getLifeProductOrAnnuityProduct().getLifeProduct();
							Iterator covrageProductItr = lifeProduct.getCoverageProduct().iterator();
							while (covrageProductItr.hasNext()) {
								CoverageProduct coverageProduct = (CoverageProduct) covrageProductItr.next();
								CovOptionProduct covOptionProduct = AccelProduct.getCovOptionProduct(coverageProduct, productCode);
								CovOptionProductExtension covOptionProductExt = AccelProduct.getFirstCovOptionProductExtension(covOptionProduct);
								if (covOptionProductExt != null) {
									//NBA237 Changed reference from NBAUtils to AccelProduct
									JurisdictionApproval jurisdictionApproval = AccelProduct.getJurisdictionApproval(covOptionProductExt, jurisdiction);
									JurisdictionApprovalExtension jurisdictionApprovalExt = AccelProduct.getFirstJurisdictionApprovalExtension(jurisdictionApproval);
									if (jurisdictionApprovalExt != null) {
										covOption.setFiledFormNumber(jurisdictionApprovalExt.getFiledFormNumber());
										break;
									}
								}
							}
						}
						
						// Begin NBLXA-1525 
						
						NbaUtils.removeLTCData(coverage,issueTxLife); 
						
						if(coverage.getLifeCovTypeCode()==NbaOliConstants.OLI_COVTYPE_CHILDTERM){
						NbaUtils.removeCTIRData(coverage,issueTxLife);
						}
						// End NBLXA-1525
						
						if (coverage != null && coverage.hasEquivalentUnderwritingClass()) { //APSL485
							coverage.deleteEquivalentUnderwritingClass();
						}
						//Begin NBLXA-2664
						long coverageStatus = coverage.getLifeCovStatus();
						if (coverageStatus == NbaOliConstants.OLI_POLSTAT_TERMINATE || coverageStatus == NbaOliConstants.OLI_POLSTAT_DECISSUE
								|| coverageStatus == NbaOliConstants.OLI_POLSTAT_INVALID) {
							life.removeCoverage(coverage);
						}
						//End NBLXA-2664
					}
				}
			}
		} catch (Exception exp) {
			throw new NbaBaseException("Unable to load Policy Product information for " + holdingIssue.getPolicy().getProductCode(), exp,
					NbaExceptionType.ERROR);
		}
	}
    /**
     * @param nbaTXLife
     * @throws NbaBaseException
     */
    protected void handleResponse(NbaTXLife nbaTXLifeResponse) throws NbaBaseException {
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		if (transResult != null) {
			long resultCode = transResult.getResultCode();
			if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
				//if failure (5) result code is received, then throw an exception to stop poller.
				throw new NbaBaseException("CAPS WebService not available", NbaExceptionType.FATAL);
			}
		}
	}
    
    /**
	 * This method is responsible for cleaning up the voided message trailer attachments.
	 * @return void
	 */
    // CR60972 New method
    public void cleanRequest() throws NbaBaseException {
    	super.cleanRequest();
    	Attachment attachment = null;
    	List attachmentList = getNbaTxLifeRequest().getPrimaryHolding().getAttachment();    	
    	if (null != attachmentList && attachmentList.size() > 0) {
    		Iterator iterator = attachmentList.iterator();
    		while (iterator.hasNext()) {
				attachment = (Attachment) iterator.next();
				if (NbaOliConstants.OLI_ATTACH_L70_MESSAGE_TRAILER == attachment.getAttachmentType()) {
					MessageTrailer messageTrailer = null;
					try {
						messageTrailer = MessageTrailer.unmarshal(new ByteArrayInputStream(attachment.getAttachmentData().getPCDATA().getBytes()));
						if (messageTrailer.getVoidInd()) {
							iterator.remove();
						} else {
							attachment.getAttachmentData().setPCDATA(messageTrailer.getComment());
						}
					} catch (Exception exp) {
						throw new NbaBaseException("Unable to unmarshal Message Trailer comment for " + attachment.getId(), exp,
								NbaExceptionType.ERROR);
					}
				}
			}
		}
    }
 
    // NBLXA-2107 
	public double calculateShortageWaiver(List financialActivityList) {
		FinancialActivity finActivity;
		double shortageAmt = 0.0;
		for (int i = 0; i < financialActivityList.size(); i++) {
			finActivity = (FinancialActivity) financialActivityList.get(i);
			if (finActivity != null && finActivity.getFinActivityType() == NbaOliConstants.OLI_FINACT_PYMNTSHORTAGE) {
				shortageAmt = finActivity.getFinActivityGrossAmt();
			}
		}
		return shortageAmt;
	}
    
}