package com.csc.fsg.nba.process.replacement;
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
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.NbaReplacementVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTermConversionVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.DisabilityHealth;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.Loan;
import com.csc.fsg.nba.vo.txlife.LoanExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
//APSL3099,CR1455126(QC10224) 
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
//APSL3099,CR1455126(QC10224)  END

/**
 * 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA243</td><td>AXA Life Phase 2</td><td>Replacement View Rewrite</td></tr>
 * <tr><td>NBA054</td><td>Version 3</td><td>Replacement Business Function</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8.90</td></tr>
 * <tr><td>NBA084</td><td>Version 3</td><td>Replacement By States Report</td></tr>
 * <tr><td>SPR1629</td><td>Version 4</td><td>Transaction Validation </td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>SPR1906</td><td>Version 4</td><td>General clean-up</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2697</td><td>Version 6</td><td>Requirement Matching Criteria Needs to Be Expanded</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>AXAL3.7.47</td><td>AXA Life Phase 1</td><td>Replacement UI</td></tr>
 * <tr><td>ALS1399</td><td>AXA Life Phase 1</td><td>Exception error message is displayed when Replaced Company row is deleted.</td></tr>
 * <tr><td>AXAL3.7.53</td><td>AXA Life Phase 1</td><td>Replacement Processing</td></tr>
 * <tr><td>ALS4875</td><td>AXA Life Phase 1</td><td>QC # 4027 - Contract Validation Message appearing on the Contract Messages tab for unknown reason</td></tr>
 * <tr><td>NBA300</td><td>AXA Life Phase 2</td><td>Term Conversion</td></tr>
 * <tr><td>NBA243</td><td>AXA Life Phase 2</td><td>Nba Replacement User Interface Rewrite</td></tr>
 * <tr><td>NBA298</td><td>AXA Life Phase 2</td><td>MEC Processing</td></tr>
 * <tr><td>P2AXAL025</td><td>AXA Life Phase 2</td><td>1035 Exchange</td></tr>
 * <tr><td>CR57912</td><td>AXA Life Phase 2</td><td>Reg60 Approximation Data</td></tr>
 * <tr><td>CR1345266</td><td>AXA Life Phase 2 CR</td><td>Advanced Date for OPAI Election</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class LoadReplacementInfoBP extends NewBusinessAccelBP {
	
	protected static NbaLogger logger = null;
	
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(LoadReplacementInfoBP.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("LoadReplacementInfoBP could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
    /*(non-Javadoc) 
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result.addResult(loadReplacementInfo((NbaTXLife) input));
        } catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }
    
	/**
	 * Creates value objects representing replacement contract information.
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @return List A list of <code>NbaReplacementVO</code> objects to be returned.
	 */
	//NBA103
	//NBA213 changed return type to List
	public List loadReplacementInfo(NbaTXLife aNbaTXLife) throws NbaBaseException {
		try {//NBA103
			List replacementContracts = new ArrayList();  //NBA213
			OLifE oLifE = aNbaTXLife.getOLifE();
			Map replacementIds = new HashMap(); //for holding id/party id storage
			List holdingIds = new ArrayList(); //for holding id
			Relation relation = null;
			
			
			
			Holding primaryHolding = NbaTXLife.getPrimaryHoldingFromOLifE(oLifE); //APSL2796
			for (int i = 0; i < oLifE.getRelationCount(); i++) {
				relation = oLifE.getRelationAt(i);
				if (relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING
					&& relation.getRelatedObjectType() == NbaOliConstants.OLI_HOLDING
					&& (relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_REPLACEDBY ||
							relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_CONVERTED)) { //found a replacement holding, NBA300 to display Term Conv policies also.
					if(primaryHolding!=null && !primaryHolding.getId().equalsIgnoreCase(relation.getRelatedObjectID())) //APSL2796
					holdingIds.add(relation.getRelatedObjectID());
				}
				if (relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING	//AXAL3.7.02
					&& relation.getRelatedObjectType() == NbaOliConstants.OLI_PARTY	//AXAL3.7.02
					&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO) { //found a replacement company (party object)
					replacementIds.put(relation.getOriginatingObjectID(), relation.getRelatedObjectID());	//AXAL3.7.02
				}
			}
			NbaReplacementVO contract = null;
			for (int i = 0; i < holdingIds.size(); i++) {
				//Make sure the ids stored are really for Replacement objects
				try {
					contract = new NbaReplacementVO();
					Object partyId = replacementIds.get(holdingIds.get(i)); //NBA300
					Party party = null; //NBA300
					if(partyId != null){
						party = NbaTXLife.getPartyFromId(partyId.toString(), oLifE.getParty());
					}
					updateVOFromOLife(
						contract,
						NbaTXLife.getHoldingFromId(holdingIds.get(i).toString(), oLifE.getHolding()),
						party,aNbaTXLife);//APSL3099,CR1455126(QC10224) 
					replacementContracts.add(contract);
				} catch (NbaDataAccessException e) {
					//suppress the exception and do not add the contract to the list to display
				} catch (Exception e) {
					//suppress the exception and do not add the contract to the list to display
				}
			}
			return replacementContracts;
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}
	}
	
	/**
	 * Updates the <code>NbaReplacementVO</code> instance with data from OLife Objects.
	 * @param contract
	 * @param holding 
	 * @param party
	 */
	protected void updateVOFromOLife(NbaReplacementVO contract, Holding holding, Party party,NbaTXLife aNbaTXLife) throws NbaDataAccessException {//APSL3099,CR1455126(QC10224) 
		if (holding.getPolicy() == null) { //Junk holding
			throw new NbaDataAccessException();
		} 
		contract.setContractNumber(holding.getPolicy().getPolNumber());
		contract.setPlan(holding.getPolicy().getProductCode());
		//contract.setLineOfBusiness(NbaOliConstants.OLI_LINEBUS_LIFE);	//AXAL3.7.02
		contract.setIssueDate(holding.getPolicy().getIssueDate());	//AXAL3.7.02, ALII255, NBA243
		if (NbaOliConstants.OLI_LINEBUS_LIFE == holding.getPolicy().getLineOfBusiness()) { //Life				
			Life life = (Life) getInsuranceObject(holding.getPolicy(), false);
			if (life != null) {
				contract.setSurrenderValue(life.getNetSurrValueAmt());
				if (life.getLifeUSA() != null) {
					contract.setPreTefraAmt(life.getLifeUSA().getBasis1035());
					contract.setPostTefraAmt(life.getLifeUSA().getAmount1035());
					//Begin NBA298
					LifeUSAExtension lifeUSAExtn = NbaUtils.getFirstLifeUSAExtension(life.getLifeUSA());
					if (lifeUSAExtn != null) {
						contract.setMECStatus(lifeUSAExtn.getMECStatus());
						contract.setPayStartDate(lifeUSAExtn.getSevenPayPremStartDate());
						contract.setRequestedExchangeDate(lifeUSAExtn.getExch1035FundsRequestedDate()); //P2AXAL025
						contract.setMoneyReceivedDate(lifeUSAExtn.getExch1035MoneyReceivedDate()); //P2AXAL025
					}
					//End NBA298
				}
				Coverage coverage = getBaseCoverage(life, false);
				if (coverage != null) {
					contract.setFaceAmt(coverage.getCurrentAmt());
				}
				contract.setFaceAmt(life.getTotalRiskAmt());	//AXAL3.7.02 override
			}
			//APSL4704 Begins
			ApplicationInfo appInfo = holding.getPolicy().getApplicationInfo();
			if (appInfo!= null ) {
				ApplicationInfoExtension ext = NbaUtils.getFirstApplicationInfoExtension(appInfo);
				if (ext != null) {
					contract.setEstimatedAmount(ext.getEstimatedAmount());
				}
				
			}
			//APSL4704 Ends

		} else if (NbaOliConstants.OLI_LINEBUS_ANNUITY == holding.getPolicy().getLineOfBusiness()) { //Annuity				
			Annuity annuity = (Annuity) getInsuranceObject(holding.getPolicy(), false);
			if (annuity != null) {
				contract.setSurrenderValue(annuity.getSurrenderValue());
				if (annuity.getAnnuityUSA() != null) {
					contract.setPreTefraAmt(annuity.getAnnuityUSA().getPreTEFRACostBasis());
					contract.setPostTefraAmt(annuity.getAnnuityUSA().getPostTEFRACostBasisAmt());  //NBA093
				}
			}
		}
		PolicyExtension extension = NbaUtils.getFirstPolicyExtension(holding.getPolicy());
		if (extension != null) {
			contract.setIncontestabilityDate(extension.getIncontestabilityDate());
			//Begin AXAL3.7.47
			contract.setCompanyId(extension.getReplacementCompanyID());
			contract.setCompanyType(extension.getReplacementCompanyType());
			contract.setAddressType(extension.getReplacementAddressType());
			contract.setSpecialInstructions(extension.getReplacementSpecialInstructions());
			//End AXAL3.7.47
			contract.setDateReceived(extension.getReplFormReceivedDate());//AXAL3.7.53
			contract.setDateMailed(extension.getReplFormMailDate());//AXAL3.7.53
			//contract.setIssueDate(extension.getYearIssued()); //AXAL3.7.02, ALII255, NBA243
			contract.setLineOfBusiness(String.valueOf(extension.getReplaceProductType())); //AXAL3.7.02
			contract.setPresaledateMailed(extension.getPreSaleMailDate()); //P2AXAL039
			contract.setPresaledateReceived(extension.getPreSaleReceivedDate()); //P2AXAL039
			contract.setReplaceConvertType(String.valueOf(extension.getReplaceConvertType())); //NBA300
			contract.setExistingCoverageSourceInd(extension.getExistingCoverageSourceInd());  // CR57912
			//APSL3099,CR1455126(QC10224) 
			populateTermConvVO(contract, extension, aNbaTXLife); //NBA300
			//APSL3099,CR1455126(QC10224) END
		}
		contract.setHoldingId(holding.getId());
		if(party != null){ //NBA300
			populateLoanCarryover(contract, holding, party); //P2AXAL025
			contract.setReplacedCompany(party.getFullName());
			contract.setPartyId(party.getId());
			contract.setCompanyId(party.getPartyKey());	//AXAL3.7.02 overrides
			//Begin AXAL3.7.47
			if(party.getAddressCount() > 0){
				Address address = party.getAddressAt(0);
				contract.setAddress1(address.getLine1());
				contract.setAddress2(address.getLine2());
				contract.setCity(address.getCity());
				contract.setZip(address.getZip());
				contract.setState(address.getAddressState());
			}
		}
		//End AXAL3.7.47
	}
	
	/**
	 * 
	 * @param NbaReplacementVO contract
	 * @param PolicyExtension extension
	 */
	//NBA300 New Method
	protected void populateTermConvVO(NbaReplacementVO contract, PolicyExtension extension,NbaTXLife aNbaTXLife) {//APSL3099,CR1455126(QC10224) 
		NbaTermConversionVO termConvVO = new NbaTermConversionVO();
		//APSL3099,CR1455126(QC10224) 
		if(aNbaTXLife.getJointParty()!=null) {
		if (aNbaTXLife.getJointParty().getPerson() != null ) {
		termConvVO.setJointinsured(true);
		Person primaryPerson = aNbaTXLife.getPrimaryParty().getPerson();
		PersonExtension PersonExtension = NbaUtils.getFirstPersonExtension(primaryPerson);
		termConvVO.setRateClass_primaryinsured(PersonExtension.getRateClassAppliedFor());	
		PersonExtension.setActionUpdate();					
		Person JointPerson = aNbaTXLife.getJointParty().getPerson();
		PersonExtension = NbaUtils.getFirstPersonExtension(JointPerson);
		termConvVO.setRateClass_jointinsured(PersonExtension.getRateClassAppliedFor());	
		PersonExtension.setActionUpdate();		
		}}
         //APSL3099,CR1455126(QC10224) END
		termConvVO.setConversionType(extension.getTermConvSubType());
		termConvVO.setContractStatus(extension.getTermConvPolicyStatus());
		termConvVO.setPlanSeries(extension.getTermConvPlanSeries());
		termConvVO.setRateClass(extension.getTermConvRateClass());
		termConvVO.setConvertedAmount(extension.getTermConvFaceAmt());
		if(extension.getTermConvRidersCC()!=null && extension.getTermConvRidersCC().getTermConvRiders()!=null){
			String[] riders = new String[extension.getTermConvRidersCC().getTermConvRidersCount()];
			for(int i=0; i<extension.getTermConvRidersCC().getTermConvRidersCount();i++){
				riders[i] = extension.getTermConvRidersCC().getTermConvRidersAt(i);
			}
			termConvVO.setRider(riders);
		}
		if(extension.getTermConvBenefitsCC()!=null && extension.getTermConvBenefitsCC().getTermConvBenefits()!=null){
			String[] benefits = new String[extension.getTermConvBenefitsCC().getTermConvBenefitsCount()];
			for(int i=0; i<extension.getTermConvBenefitsCC().getTermConvBenefitsCount();i++){
				benefits[i] = extension.getTermConvBenefitsCC().getTermConvBenefitsAt(i);
			}
			termConvVO.setBenefit(benefits);
		}
		
		termConvVO.setTableRating(extension.getTermConvTableRatingInd());
		termConvVO.setFlatExtra(extension.getTermConvFlatExtraInd());
		termConvVO.setReinsured(extension.getTermConvReinsuranceInd());
		termConvVO.setExclusionRider(extension.getRemoveTermConvExclRdrInd());
		termConvVO.setReqReductionInRating(extension.getTermConvRatingReductionInd());
		termConvVO.setIncreaseReqUW(extension.getTermConvIncreaseUWReqrdInd());
		termConvVO.setRiderAdditionReqUW(extension.getTermConvRiderAddUWReqrdInd());
		termConvVO.setBenefitAdditionReqUW(extension.getTermConvBenefitAddUWReqrdInd());
		termConvVO.setOptionOffSchedule(extension.getOptionOffSchedule()); //CR1345266
		contract.setTermConversionVO(termConvVO);
	}
	
	/**
	 * 
	 * @param NbaReplacementVO contract
	 * @param Holding hold, 
	 * @param Party party
	 */
	//P2AXAL025 New Method
	protected void populateLoanCarryover(NbaReplacementVO contract, Holding hold, Party party) {
		int loanCount = hold.getLoanCount();
		Loan loan = null;
		for(int i=0;i<loanCount;i++) {
			loan = hold.getLoanAt(i);
			if(loan.getFinancialInstitutionPartyID()!=null && loan.getFinancialInstitutionPartyID().equalsIgnoreCase(party.getId())){
				contract.setLoanAmtPd(loan.getLoanBalance());
				contract.setIntPaidToDate(loan.getLoanIntPaidToDate());
				LoanExtension loanExtn = NbaUtils.getFirstLoanExtension(loan);
				if(loanExtn != null){
					contract.setLastActivityDate(loanExtn.getLastActivityDate());
					contract.setInterestCode(String.valueOf(loanExtn.getLoanIntCodeType()));
					contract.setInvestmentCode(String.valueOf(loanExtn.getLoanInvestCodeType()));
				}
				break;
			}
		}
	}
	
	/**
	 * Returns an instance of Annuity or Life object based on the type of contract.
	 * It creates an object if the createIfNull falg is set to true.
	 * @param policy
	 * @param createIfNull
	 * @return Object
	 */
	protected Object getInsuranceObject(Policy policy, boolean createIfNull) {
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty product = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
		if (product == null) {
			if (createIfNull) {
				product = new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();  //NBA093
				product.setActionAdd();
				policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(product);  //NBA093
			} else {
				return null;
			}
		}
		if (NbaOliConstants.OLI_LINEBUS_LIFE == policy.getLineOfBusiness()) {
			Life life = product.getLife();
			if (life == null && createIfNull) {
				life = new Life();
				life.setActionAdd();
				product.setLife(life);
			}
			return life;
		} else if (NbaOliConstants.OLI_LINEBUS_ANNUITY == policy.getLineOfBusiness()) {
			Annuity annuity = product.getAnnuity();
			if (annuity == null && createIfNull) {
				annuity = new Annuity();
				annuity.setActionAdd();
				product.setAnnuity(annuity);
			}
			return annuity;
		} else {
			DisabilityHealth dh = product.getDisabilityHealth();
			if (dh == null && createIfNull) {
				dh = new DisabilityHealth();
				dh.setActionAdd();
				product.setDisabilityHealth(dh);
			}
			return dh;
		}
	}
	
	/**
	 * Returns the base coverage for a life insurance contract.
	 * @param life
	 * @return Coverage
	 */
	protected Coverage getBaseCoverage(Life life, boolean createIfNull) {
		Coverage coverage = null;
		for (int i = 0; i < life.getCoverageCount(); i++) {
			coverage = life.getCoverageAt(i);
			if (coverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
				break;
			}
			coverage = null;
		}
		if (coverage == null && createIfNull) {
			coverage = new Coverage();
			coverage.setActionAdd();
			coverage.setIndicatorCode(NbaOliConstants.OLI_COVIND_BASE);
			life.addCoverage(coverage);
		}
		return coverage;
	}
}
