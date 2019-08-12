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
import java.util.Arrays;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.accel.valueobject.LobData;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaTransactionValidationException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaHolding;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaReplacementRequest;
import com.csc.fsg.nba.vo.NbaReplacementVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTermConversionVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.AnnuityUSA;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.Loan;
import com.csc.fsg.nba.vo.txlife.LoanExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.TermConvBenefitsCC;
import com.csc.fsg.nba.vo.txlife.TermConvRidersCC;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
//APSL3099,CR1455126(QC10224) 
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
//APSL3099,CR1455126(QC10224) END

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
public class SaveReplacementInfoBP extends LoadReplacementInfoBP {
	
	public final static String REPLACEMENT = "REPLACEMENT"; //ALS4875
	public final static String DELETEREPLACEMENT = "DELETEREPLACEMENT"; //P2AXAL025
	protected static NbaLogger logger = null;
	
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	private static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(SaveReplacementInfoBP.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("SaveReplacementInfoBP could not get a logger from the factory.");
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
        	NbaReplacementRequest req = (NbaReplacementRequest) input;
            result.addResult(saveReplacementInfo(req.getNbaUserVO(), req.getNbaDst(), req.getTxlife(), req.getReplacementContracts(),
					req.getReplacementType()));
        } catch (NbaTransactionValidationException e) {//NBA103
        	addMessage(result, e.getMessage()); //P2AXAL040
		} catch (Exception e) {
            result = new AccelResult();
            addExceptionMessage(result, e);
        }
        return result;
    }
    
	/**
	 * Saves replacement details for contracts provided by the calling program. 
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param aNbaDst An instance of <code>NbaDst</code>
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @param replacementContracts An object containing replacement contracts.
	 * @return List A list of <code>NbaReplacementVO</code> objects containing updated information
	 * @throws NbaBaseException
	 */
	//NBA103
	//NBA213 changed return type and replacmentContracts parameter to List
	public List saveReplacementInfo(NbaUserVO user, NbaDst aNbaDst, NbaTXLife aNbaTXLife, List replacementContracts, long replacementCode)
		throws NbaBaseException {
		List updatedContracts = new ArrayList();  //NBA213
		
		long replacementType = getReplacementType(aNbaTXLife); //NBA084
		setReplacementType(aNbaTXLife, replacementCode);  //NBA093
		// NBA050 CODE DELETED
		//SPR1851 code deleted
		boolean addToResult = true;
		NbaReplacementVO contract = null;
		//Begin NBA084
		NbaDst newDst = null;
		List existingContracts = new ArrayList();  //NBA213
		NbaReplacementVO oldContract = null;
		try {//NBA103
			existingContracts = loadReplacementInfo(aNbaTXLife);
			newDst = retrieveWorkItem(user, aNbaDst);
			//End NBA084	
			for (int i = 0; i < replacementContracts.size(); i++) {
				contract = (NbaReplacementVO) replacementContracts.get(i);
				//Begin NBA084
				if (i >= existingContracts.size()){
					oldContract = null;
				}else{
					oldContract = (NbaReplacementVO) existingContracts.get(i);
				}
				//End NBA084
				addToResult = true;
				if (!contract.isActionDisplay()) {
					handleActions(aNbaTXLife, contract, aNbaDst, newDst, oldContract, replacementType); //NBA084
					addToResult = !contract.isActionDelete();
					contract.setActionSuccessful(); //Assume everything passed
				}
				if (addToResult) {
					updatedContracts.add(contract);
				}
			}
			//ALII1310 - revereted code put here for ALII1187 fix, to not set RPCM LOB from this place. 
			// As per Kathy's comment, RPCM will be manually assigned for in-flight cases missing this LOB.
			//Begin NBA084
			aNbaDst.setUpdate();
			WorkflowServiceHelper.update(user, aNbaDst); //Save AWD LOB fields
		} catch (NbaBaseException e) {//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}
		//End NBA084
		
        try {
        	if(updatedContracts.size() <= 0){
        		aNbaTXLife.setBusinessProcess(DELETEREPLACEMENT); //P2AXAL025
        	} else {
        		aNbaTXLife.setBusinessProcess(REPLACEMENT); //ALS4875
        	}
        	//APSL3099,CR1455126(QC10224) 
        	if(aNbaTXLife.getJointParty()!=null) {
        		if (aNbaTXLife.getJointParty().getPerson() != null ){
        	Person primaryperson = aNbaTXLife.getPrimaryParty().getPerson();
        	Person jointperson = aNbaTXLife.getJointParty().getPerson();
        	PersonExtension personExtension = NbaUtils.getFirstPersonExtension(primaryperson);
    		personExtension.setRateClassAppliedFor(contract.getTermConversionVO().getRateClass_primaryinsured());
    		personExtension.setActionUpdate();
    		personExtension = NbaUtils.getFirstPersonExtension(jointperson);
    		personExtension.setRateClassAppliedFor(contract.getTermConversionVO().getRateClass_jointinsured());
    		personExtension.setActionUpdate();
        		}}//APSL3099,CR1455126(QC10224) END
            aNbaTXLife = NbaContractAccess.doContractUpdate(aNbaTXLife, aNbaDst, user); // NBA050 SPR1851
            //SPR1851 code deleted
		} catch (NbaTransactionValidationException e) {//NBA103
			throw e;//NBA103
		} catch (NbaBaseException e) {//NBA103
			setActionFailedForContract(replacementContracts, contract);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			setActionFailedForContract(replacementContracts, contract);//NBA103
			NbaBaseException e = new NbaBaseException(t);//NBA103
			getLogger().logException(e);//NBA103
			throw e;//NBA103
		}
		return updatedContracts;
	}
	
	/**
	 * Retrieves the replacement type.
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @return long	 
	 */
	// NBA093 NEW METHOD
	//NBA213 changed method visibility and removed throwing NbaBaseException
	protected long getReplacementType(NbaTXLife aNbaTXLife) {
		//begin NBA213
		long replacementType = NbaOliConstants.OLI_TC_NULL; 
		NbaHolding nbaHolding = aNbaTXLife.getNbaHolding();
		if (nbaHolding != null) {
			replacementType = nbaHolding.getReplacementType();
		}
		return replacementType;
		//end NBA213
	}

	/**
	 * Sets the replacement type.
	 * @param aNbaTXLife An instance of <code>NbaTXLife</code>
	 * @param aType A replacement type to set.
	 */
	// NBA093 NEW METHOD
	protected void setReplacementType(NbaTXLife aNbaTXLife, long aType) {
		if (aType > -1) { 
			Holding holding = aNbaTXLife.getPrimaryHolding();
			Policy policy = holding.getPolicy();
			if (policy != null) {
				policy.setReplacementType(aType);
				policy.setActionUpdate();
				//begin SPR2697
                ApplicationInfo applicationInfo = policy.getApplicationInfo();
                if (applicationInfo == null) {
                    applicationInfo = new ApplicationInfo();
                    applicationInfo.setActionAdd();
                    policy.setApplicationInfo(applicationInfo);
                }
                applicationInfo.setReplacementInd(NbaOliConstants.OLI_REPTY_NONE != aType);
                applicationInfo.setActionUpdate();
                //end SPR2697
			}
		}
	}
	
	/**
	 * Retrieves a work item based for the value object passed in. If the work item needed.
	 * @param user an instance of <code>NbaUserVO</code>
	 * @param aNbaDst an instance of <code>NbaDst</code>
	 * @param nsa A <code>NbaNetServerAccessor</code> instance
	 * @return NbaDst
	 * @throws Exception
	 */
	//NBA084 New Method
	protected NbaDst retrieveWorkItem(NbaUserVO user, NbaDst aNbaDst) throws Exception {
		NbaAwdRetrieveOptionsVO options = new NbaAwdRetrieveOptionsVO();
		options.setWorkItem(aNbaDst.getID(), true); // true indicates Case
		return WorkflowServiceHelper.retrieveWorkItem(user, options); //SPR1851
	}
	
	/**
	 * Sets the failed action indicator on all replacement contracts if the action
	 * indicator is currently marked for update.
	 * @param replacementContracts
	 * @param contract
	 */
	//NBA103
	//NBA213 changed replacementContracts to List
	private void setActionFailedForContract(List replacementContracts, NbaReplacementVO contract) {
		for (int i = 0; i < replacementContracts.size(); i++) {
			contract = (NbaReplacementVO) replacementContracts.get(i);
			if (!contract.isActionDisplay()) {
				contract.setActionFailed();
			}
		}
	}
	
	/**
	 * Handles ADD, DELETE or UPDATE actions on a replacement contract.
	 * @param aNbaTXLife
	 * @param contract
	 * @param user
	 * @param aNbaDst
	 * @param newDst
	 * @param oldContract
	 * @param replacementType
	 */
	//	NBA084 Add parameters user, aNbaDst, newDst, oldContract, replacementType
	protected void handleActions(
		NbaTXLife aNbaTXLife,
		NbaReplacementVO contract,
		NbaDst aNbaDst,
		NbaDst newDst,
		NbaReplacementVO oldContract,
		long replacementType) {
		OLifE oLifE = aNbaTXLife.getOLifE();
		Party party = null;
		Holding holding = null;
		Relation relation = null;
		NbaLob nbaLob = aNbaDst.getNbaLob(); //NBA084
		NbaLob newLob = newDst.getNbaLob(); //NBA084
		if (contract.isActionDelete() && contract.getHoldingId()!=null) {
			NbaTXLife.getHoldingFromId(contract.getHoldingId(), oLifE.getHolding()).setActionDelete();
			if(contract.getPartyId() != null && NbaTXLife.getPartyFromId(contract.getPartyId(), oLifE.getParty())!=null){ //NBA300
					NbaTXLife.getPartyFromId(contract.getPartyId(), oLifE.getParty()).setActionDelete();
			}
			for (int i = 0; i < oLifE.getRelationCount(); i++) {
				relation = oLifE.getRelationAt(i);
				if (relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING
					&& relation.getRelatedObjectType() == NbaOliConstants.OLI_HOLDING
					&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_REPLACEDBY
					&& contract.getHoldingId().equals(relation.getRelatedObjectID())) {
					relation.setActionDelete();
				} else if (
					relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING		//AXAL3.7.02
						&& relation.getRelatedObjectType() == NbaOliConstants.OLI_PARTY	//AXAL3.7.02
						&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_HOLDINGCO
						&& contract.getHoldingId().equals(relation.getOriginatingObjectID())	//AXAL3.7.02
						&& contract.getPartyId().equals(relation.getRelatedObjectID())) {//AXAL3.7.02
					relation.setActionDelete();
				} else if (	//Begin AXAL3.7.02
					relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING	
						&& relation.getRelatedObjectType() == NbaOliConstants.OLI_PARTY		
						&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_INSURED
						&& contract.getHoldingId().equals(relation.getOriginatingObjectID())) {//AXAL3.7.02
					relation.setActionDelete();
					//End AXAL3.7.02
				} else if ( //NBA300
						relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING		//AXAL3.7.02
						&& relation.getRelatedObjectType() == NbaOliConstants.OLI_HOLDING	//AXAL3.7.02
						&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_CONVERTED
						&& contract.getHoldingId().equals(relation.getRelatedObjectID())) {
					relation.setActionDelete();
				} else if (	//NBA300
						relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING	
						&& relation.getRelatedObjectType() == NbaOliConstants.OLI_PARTY		
						&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_SPOUSE
						&& contract.getHoldingId().equals(relation.getOriginatingObjectID())) {
					relation.setActionDelete();
				} else if (	//NBA300
						relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING	
						&& relation.getRelatedObjectType() == NbaOliConstants.OLI_PARTY		
						&& relation.getRelationRoleCode() == NbaOliConstants.OLI_REL_AUTHORIZEDPERSON
						&& contract.getHoldingId().equals(relation.getOriginatingObjectID())) {
					relation.setActionDelete();
				}
			}
			//Call function to locate LOB and null out values
			deleteReplacementLOB(nbaLob, newLob, oldContract, replacementType); //NBA084
		} else {
			NbaOLifEId aNbaOLifEId = new NbaOLifEId(oLifE);
			if (contract.isActionAdd()) {
				if(contract.getReplaceConvertType()==null || contract.getReplaceConvertType().equalsIgnoreCase("-1") 
						|| contract.getReplaceConvertType().equalsIgnoreCase(String.valueOf(NbaOliConstants.NBA_RPLCCONVTYPE_1000500001))){ //NBA300
					party = new Party();
					party.setActionAdd();
					//Begin AXAL3.7.02
					party.setPartyTypeCode(NbaOliConstants.OLI_PT_ORG);
					PersonOrOrganization personOrOrganization = party.getPersonOrOrganization();
					if (personOrOrganization == null) {
						personOrOrganization = new PersonOrOrganization();
						personOrOrganization.setActionAdd();
						party.setPersonOrOrganization(personOrOrganization);
						Organization organization = new Organization();
						organization.setActionAdd();
						personOrOrganization.setOrganization(organization);
					}
					//End AXAL3.7.02
					oLifE.addParty(party);
					holding = new Holding();
					holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);	//AXAL3.7.02
					holding.setActionAdd();
					oLifE.addHolding(holding);
					updateOlifeFromVO(contract, holding, party, aNbaOLifEId);
					//------------1. Holding to Holding-----------
					relation = new Relation();
					relation.setActionAdd();
					relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
					relation.setRelatedObjectType(NbaOliConstants.OLI_HOLDING);
					relation.setOriginatingObjectID(aNbaTXLife.getPrimaryHolding().getId());
					relation.setRelationRoleCode(NbaOliConstants.OLI_REL_REPLACEDBY);
					//NbaUtils.setRelatedRefId(relation, oLifE.getRelation());
					oLifE.addRelation(relation);
					aNbaOLifEId.setId(holding); //create the holding IDs first
					relation.setRelatedObjectID(holding.getId());
					aNbaOLifEId.setId(relation);
					//-----------2. Holding to Party ( replaced company )----------- 
					relation = new Relation();
					relation.setActionAdd();
					relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);	//AXAL3.7.02
					relation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);		//AXAL3.7.02
					relation.setRelationRoleCode(NbaOliConstants.OLI_REL_HOLDINGCO);
					relation.setOriginatingObjectID(holding.getId());				//AXAL3.7.02 ALS2924
					//NbaUtils.setRelatedRefId(relation, oLifE.getRelation());
					oLifE.addRelation(relation);
					aNbaOLifEId.setId(relation);
					aNbaOLifEId.setId(party, relation); //create Party Ids last
					relation.setRelatedObjectID(party.getId());					//AXAL3.7.02
					//-----------3. Holding to Insured Party - Create primary insured relations.-----------
					//Begin AXAL3.7.02	
					//ALII2061 & ALII2062 code deleted
					//End AXAL3.7.02
					contract.setHoldingId(holding.getId());
					contract.setPartyId(party.getId());
					//Call function to locate last field and add value.
					addReplacementLOB(nbaLob, newLob, contract, aNbaTXLife, replacementType); //NBA084
					updateOlifeFromLoanCarryover(holding, party, aNbaOLifEId, contract);//P2AXAL025
				} else {
					holding = new Holding();
					holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);
					holding.setActionAdd();
					oLifE.addHolding(holding);
					updateOlifeFromVO(contract, holding, null, aNbaOLifEId);
					//------------ Holding to Holding-----------
					relation = new Relation();
					relation.setActionAdd();
					relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
					relation.setRelatedObjectType(NbaOliConstants.OLI_HOLDING);
					relation.setOriginatingObjectID(aNbaTXLife.getPrimaryHolding().getId());
					relation.setRelationRoleCode(NbaOliConstants.OLI_REL_CONVERTED);
					oLifE.addRelation(relation);
					aNbaOLifEId.setId(holding); //create the holding IDs first
					relation.setRelatedObjectID(holding.getId());
					aNbaOLifEId.setId(relation);
					HoldingExtension holExt = NbaUtils.getFirstHoldingExtension(holding);
					if (holExt == null) {
						OLifEExtension lifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_HOLDING);
						holding.addOLifEExtension(lifeExtension);
						holExt = lifeExtension.getHoldingExtension();
						holExt.setActionAdd();
					} else {
						holExt.setActionUpdate();
					}
					if(contract.getReplaceConvertType()!=null && 
							contract.getReplaceConvertType().equalsIgnoreCase(String.valueOf(NbaOliConstants.NBA_RPLCCONVTYPE_1000500002))) {
						holExt.setHoldingSubType(NbaOliConstants.OLI_HOLDSUBTYPE_TERMCONV);
					} else {
						holExt.setHoldingSubType(NbaOliConstants.OLI_HOLDSUBTYPE_OPAI);
					}
					
				}
				//start ALII2061 & ALII2062
				//------------ Holding to Party Relation-----------
				relation = new Relation();
				String partyID = contract.getInsuredPartyId();
				relation.setActionAdd();
				relation.setOriginatingObjectType(NbaOliConstants.OLI_HOLDING);
				relation.setRelatedObjectType(NbaOliConstants.OLI_PARTY);
				relation.setOriginatingObjectID(holding.getId());
				relation.setRelatedObjectID(partyID);
				if(aNbaTXLife.isPrimaryInsured(partyID)){
					relation.setRelationRoleCode(NbaOliConstants.OLI_REL_INSURED);
				}else if(aNbaTXLife.isJointInsured(partyID)){
					relation.setRelationRoleCode(NbaOliConstants.OLI_REL_JOINTINSURED);
				}else if(aNbaTXLife.isOwner(partyID)){
					relation.setRelationRoleCode(NbaOliConstants.OLI_REL_OWNER);
				}
				oLifE.addRelation(relation);
				aNbaOLifEId.setId(relation);
				//end ALII2061 & ALII2062
			} else if (contract.isActionUpdate()) {
				holding = NbaTXLife.getHoldingFromId(contract.getHoldingId(), oLifE.getHolding());
				if(contract.getPartyId() != null) {
					party = NbaTXLife.getPartyFromId(contract.getPartyId(), oLifE.getParty());
				}
				updateOlifeFromVO(contract, holding, party, aNbaOLifEId);
				//Call function to locate LOB and modify values.
				updateReplacementLOB(nbaLob, newLob, contract, oldContract, aNbaTXLife, replacementType); //NBA084
				if(party != null){ //NBA300
					updateOlifeFromLoanCarryover(holding, party, aNbaOLifEId, contract);//P2AXAL025
				}
			}
		}
	}
	
	/**
	 * Updates the <code>NbaReplacementVO</code> instance data to OLife Objects.
	 * @param contract
	 * @param holding
	 * @param party
	 */
	protected void updateOlifeFromVO(NbaReplacementVO contract, Holding holding, Party party, NbaOLifEId aNbaOLifEId) {
		Policy policy = holding.getPolicy();
		if (policy == null) {
			policy = new Policy();
			policy.setActionAdd();
			holding.setPolicy(policy);
		}
		policy.setActionUpdate(); //Update the Action Indicator
		policy.setPolNumber(contract.getContractNumber());
		policy.setLineOfBusiness(NbaOliConstants.OLI_LINEBUS_LIFE);	//AXAL3.7.02
		policy.setIssueDate(contract.getIssueDate());				//AXAL3.7.02, ALII255, NBA243
		policy.setProductCode(contract.getPlan());
		if (policy.isActionAdd()) { //set ids as late as possible
			aNbaOLifEId.setId(policy);
		}

		if (NbaOliConstants.OLI_LINEBUS_LIFE == policy.getLineOfBusiness()) { //Life
			Life life = (Life) getInsuranceObject(holding.getPolicy(), true);
			life.setActionUpdate(); //Update the Action Indicator
			life.setNetSurrValueAmt(contract.getSurrenderValue());
			life.setTotalRiskAmt(contract.getFaceAmt()); //AXAL3.7.02
			//Begin NBA298
			LifeUSA lifeUSA = life.getLifeUSA();
			if (lifeUSA == null) {
				lifeUSA = new LifeUSA();
				lifeUSA.setActionAdd();
				life.setLifeUSA(lifeUSA);
			} else {
				lifeUSA.setActionUpdate(); //Update the Action Indicator
			}
			
			if (contract.getPreTefraAmt() > 0.0 || contract.getPostTefraAmt() > 0.0) {
				lifeUSA.setBasis1035(contract.getPreTefraAmt());
				lifeUSA.setAmount1035(contract.getPostTefraAmt());
			}

			LifeUSAExtension lifeUSAExt = NbaUtils.getFirstLifeUSAExtension(lifeUSA);
			if (lifeUSAExt == null) {
				OLifEExtension extn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_LIFEUSA);
				lifeUSAExt = extn.getLifeUSAExtension();
				lifeUSA.addOLifEExtension(extn);
				lifeUSAExt.setActionAdd();
			} else {
				lifeUSAExt.setActionUpdate();
			}
			lifeUSAExt.setMECStatus(contract.getMECStatus());
			lifeUSAExt.setSevenPayPremStartDate(contract.getPayStartDate());
			//End NBA298
			lifeUSAExt.setExch1035FundsRequestedDate(contract.getRequestedExchangeDate()); //P2AXAL025
			lifeUSAExt.setExch1035MoneyReceivedDate(contract.getMoneyReceivedDate()); //P2AXAL025

			if (contract.getFaceAmt() > 0.0) {
				Coverage coverage = getBaseCoverage(life, true);
				coverage.setActionUpdate(); //Update the Action Indicator
				coverage.setCurrentAmt(contract.getFaceAmt());
				if (coverage.isActionAdd()) { //set ids as late as possible
					aNbaOLifEId.setId(coverage);
				}
			}
			// NBA093 code deleted
			//APSL4704 Begins
			
			ApplicationInfo applicationInfo = policy.getApplicationInfo();
			if(applicationInfo == null){
				applicationInfo = new ApplicationInfo();
				applicationInfo.setActionAdd();
				policy.setApplicationInfo(applicationInfo);
			}
			ApplicationInfoExtension ext = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if(ext== null) {
				OLifEExtension extn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
				ext = extn.getApplicationInfoExtension();
		         applicationInfo.addOLifEExtension(extn);
				ext.setActionAdd();
			}
			if(!ext.isActionAdd()){
				ext.setActionUpdate();
			}
			 		ext.setEstimatedAmount(contract.getEstimatedAmount());
				
				//APSL4704 Ends
		} else if (NbaOliConstants.OLI_LINEBUS_ANNUITY == holding.getPolicy().getLineOfBusiness()) { //Annuity				
			Annuity annuity = (Annuity) getInsuranceObject(holding.getPolicy(), true);
			annuity.setActionUpdate(); //Update the Action Indicator
			annuity.setSurrenderValue(contract.getSurrenderValue());
			if (contract.getPreTefraAmt() > 0.0 || contract.getPostTefraAmt() > 0.0) {
				AnnuityUSA annuityUSA = annuity.getAnnuityUSA();
				if (annuityUSA == null) {
					annuityUSA = new AnnuityUSA();
					annuityUSA.setActionAdd();
					annuity.setAnnuityUSA(annuityUSA);
				}
				annuityUSA.setActionUpdate(); //Update the Action Indicator
				annuityUSA.setPreTEFRACostBasis(contract.getPreTefraAmt());
				annuityUSA.setPostTEFRACostBasisAmt(contract.getPostTefraAmt());  //NBA093
			}
			// NBA093 code deleted
		} else { //DisabilityHealth				
			getInsuranceObject(holding.getPolicy(), true); //Make sure a DH object is created				
		}
		PolicyExtension extension = NbaUtils.getFirstPolicyExtension(policy); //AXAL3.702 refactored.
		if (extension == null) {
			OLifEExtension extn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			extn.setActionAdd();
			policy.addOLifEExtension(extn);
			extension = NbaUtils.getFirstPolicyExtension(policy); //try again
		}
		extension.setActionUpdate(); //Update the Action Indicator
		extension.setIncontestabilityDate(contract.getIncontestabilityDate());
		//Begin AXAL3.7.47
		extension.setReplacementCompanyID(contract.getCompanyId());
		extension.setReplacementCompanyType(contract.getCompanyType());
		extension.setReplacementAddressType(contract.getAddressType());
		extension.setReplacementSpecialInstructions(contract.getSpecialInstructions());
		//End AXAL3.7.47
		extension.setReplFormMailDate(contract.getDateMailed());//AXAL3.7.53
		extension.setReplFormReceivedDate(contract.getDateReceived());//AXAL3.7.53
		extension.setReplaceProductType(contract.getLineOfBusiness()); //AXAL3.7.02
		//extension.setYearIssued(contract.getIssueDate()); //AXAL3.7.02, ALII255, NBA243
		if(contract.getReplaceConvertType()==null || contract.getReplaceConvertType().equalsIgnoreCase("-1") ||
				contract.getReplaceConvertType().equalsIgnoreCase(String.valueOf(NbaOliConstants.NBA_RPLCCONVTYPE_1000500001))){ //NBA300
			extension.setReplacementIndCode(NbaOliConstants.NBA_ANSWERS_YES); //AXAL3.7.02
			extension.setReplaceConvertType(String.valueOf(NbaOliConstants.NBA_RPLCCONVTYPE_1000500001)); 
		} else {
			extension.setReplaceConvertType(contract.getReplaceConvertType());
		}
		extension.setExistingCoverageSourceInd(contract.isExistingCoverageSourceInd());   // CR57912
		updateOlifeFromTermConvVO(extension, contract);//NBA300
		
		//everything from this view is replacement.
		extension.setPreSaleMailDate(contract.getPresaledateMailed());//NBA243
		extension.setPreSaleReceivedDate(contract.getPresaledateReceived());//NBA243
		if(party != null){ //NBA300
			party.setFullName(contract.getReplacedCompany());
			if(party.getPersonOrOrganization().getOrganization()!= null){//APSL3099
				party.getPersonOrOrganization().getOrganization().setDBA(contract.getReplacedCompany()); //AXAL3.7.02
				
				party.getPersonOrOrganization().getOrganization().setActionUpdate();	//AXAL3.7.02
			}
			party.setPartyKey(contract.getCompanyId());	//AXAL3.7.02
			party.setActionUpdate();
			//Begin AXAL3.7.47
			if (party.getAddressCount() <= 0) {
				Address partyAddress = new Address();
				setPartyAddressFromVO(contract,partyAddress);
				partyAddress.setActionAdd();
				ArrayList addressList = new ArrayList();
				addressList.add(partyAddress);
				party.setAddress(addressList);
			} else {
				Address partyAddress = party.getAddressAt(0);
				setPartyAddressFromVO(contract,partyAddress);
				partyAddress.setActionUpdate();
			}
		}
		//End AXAL3.7.47
	}
	
	/**
	 * 
	 * @param PolicyExtension extension
	 * @param NbaReplacementVO contract
	 */
	//NBA300 New Method
	protected void updateOlifeFromTermConvVO(PolicyExtension extension, NbaReplacementVO contract) {
		NbaTermConversionVO termConvVO = contract.getTermConversionVO();
		extension.setTermConvSubType(termConvVO.getConversionType());
		extension.setTermConvPolicyStatus(termConvVO.getContractStatus());
		extension.setTermConvPlanSeries(termConvVO.getPlanSeries());
		extension.setTermConvRateClass(termConvVO.getRateClass());
		extension.setTermConvFaceAmt(termConvVO.getConvertedAmount());
		if(termConvVO.getRider()!=null && termConvVO.getRider().length>0){
			ArrayList ridersList = new ArrayList();
			String[] riders = termConvVO.getRider();
			for(int i=0; i<termConvVO.getRider().length;i++){
				ridersList.add(riders[i]);
			}
			TermConvRidersCC tcRidersCC = extension.getTermConvRidersCC();
			if (tcRidersCC == null) {
				tcRidersCC = new TermConvRidersCC();
				tcRidersCC.setActionAdd();
				extension.setTermConvRidersCC(tcRidersCC);
			} else{
				tcRidersCC.setActionUpdate(); //Update the Action Indicator
			}
			tcRidersCC.setTermConvRiders(ridersList);
			extension.setTermConvRidersCC(tcRidersCC);
		}
		if(termConvVO.getBenefit()!=null && termConvVO.getBenefit().length>0){
			ArrayList benefitsList = new ArrayList();
			String[] benefits = termConvVO.getBenefit();
			for(int i=0; i<termConvVO.getBenefit().length;i++){
				benefitsList.add(benefits[i]);
			}
			TermConvBenefitsCC tcBenefitsCC = extension.getTermConvBenefitsCC();
			if (tcBenefitsCC == null) {
				tcBenefitsCC = new TermConvBenefitsCC();
				tcBenefitsCC.setActionAdd();
				extension.setTermConvBenefitsCC(tcBenefitsCC);
			} else{
				tcBenefitsCC.setActionUpdate(); //Update the Action Indicator
			}
			tcBenefitsCC.setTermConvBenefits(benefitsList);
			extension.setTermConvBenefitsCC(tcBenefitsCC);
		}
		extension.setTermConvTableRatingInd(termConvVO.isTableRating());
		extension.setTermConvFlatExtraInd(termConvVO.isFlatExtra());
		extension.setTermConvReinsuranceInd(termConvVO.isReinsured());
		extension.setRemoveTermConvExclRdrInd(termConvVO.isExclusionRider());
		extension.setTermConvRatingReductionInd(termConvVO.isReqReductionInRating());
		extension.setTermConvIncreaseUWReqrdInd(termConvVO.isIncreaseReqUW());
		extension.setTermConvRiderAddUWReqrdInd(termConvVO.isRiderAdditionReqUW());
		extension.setTermConvBenefitAddUWReqrdInd(termConvVO.isBenefitAdditionReqUW());
		extension.setOptionOffSchedule(termConvVO.isOptionOffSchedule()); //CR1345266
	}

	/**
	 * 
	 * @param NbaReplacementVO contract
	 * @param Holding hold, 
	 * @param Party party
	 */
	//P2AXAL025 New Method
	protected void updateOlifeFromLoanCarryover(Holding hold, Party party, NbaOLifEId aNbaOLifEId, NbaReplacementVO contract) {
		//ALII1178 begin
		if (contract.isDeleteLoanCarryover()) {
			int loanCount = hold.getLoanCount();
			Loan loan = null;
			Loan loanTemp = null;
			for(int i=0;i<loanCount;i++) {
				loanTemp = hold.getLoanAt(i);
				if(loanTemp.getFinancialInstitutionPartyID()!=null && loanTemp.getFinancialInstitutionPartyID().equalsIgnoreCase(party.getId())){
					loan = loanTemp;
					loan.setActionDelete();
				}
			}
		} else if(ifLoanCarryoverEntered(contract)) { 
		//ALII1178 end
			int loanCount = hold.getLoanCount();
			Loan loan = null;
			Loan loanTemp = null;
			for(int i=0;i<loanCount;i++) {
				loanTemp = hold.getLoanAt(i);
				if(loanTemp.getFinancialInstitutionPartyID()!=null && loanTemp.getFinancialInstitutionPartyID().equalsIgnoreCase(party.getId())){
					loan = loanTemp;
					loan.setActionUpdate();
				}
			}
			if(loan == null){
				loan = new Loan();
				loan.setActionAdd();
				hold.addLoan(loan);
			}
			if (loan.isActionAdd()) {
				aNbaOLifEId.setId(loan);
			}
			loan.setLoanBalance(contract.getLoanAmtPd());
			loan.setLoanIntPaidToDate(contract.getIntPaidToDate());
			loan.setFinancialInstitutionPartyID(party.getId());
			
			LoanExtension loanExtn = NbaUtils.getFirstLoanExtension(loan);
			if(loanExtn == null){
				OLifEExtension extn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_LOAN);
				extn.setActionAdd();
				loan.addOLifEExtension(extn);
				loanExtn = NbaUtils.getFirstLoanExtension(loan); //try again
			}
			loanExtn.setActionUpdate();
			loanExtn.setLastActivityDate(contract.getLastActivityDate());
			loanExtn.setLoanIntCodeType(contract.getInterestCode());
			loanExtn.setLoanInvestCodeType(contract.getInvestmentCode());
		}
		//ALII1178 code deleted
	}
	
	protected boolean ifLoanCarryoverEntered(NbaReplacementVO contract){
		if(contract.getLoanAmtPd()>0 || contract.getIntPaidToDate()!=null || contract.getLastActivityDate()!=null || (contract.getInterestCode()!=null && !"-1".equalsIgnoreCase(contract.getInterestCode())) || (contract.getInvestmentCode()!=null && !"-1".equalsIgnoreCase(contract.getInvestmentCode()))) {
			return true;
		}
		return false;
	}
	
	/**
	 * Sets the Party Address from VO to the Address Object.
	 * @param contract NbaReplacementVO object to revtieve address
	 * @param partyAddress Address object to copy the address
	 */
	//AXAL3.7.47 New Method
	private void setPartyAddressFromVO(NbaReplacementVO contract, Address partyAddress){
		partyAddress.setLine1(contract.getAddress1());
		partyAddress.setLine2(contract.getAddress2());
		partyAddress.setCity(contract.getCity());
		partyAddress.setAddressState(contract.getState());
		partyAddress.setZip(contract.getZip());
	}
	
	/**
	 * Compares the work item Id and the Id of the <code>NbaDst</code> instance.
	 * Returns work item sequence number or highest sequence number depending on flag.
	 * @param newLob
	 * @param oldContract
	 * @param aNbaTXLife
	 * @param replacementType
	 * @param getLastSeq
	 * @return int
	 */
	//NBA084 New Method
	protected int findReplacementLOB(NbaLob newLob, NbaReplacementVO oldContract, long replacementType, boolean getLastSeq) {
		LobData lobField = null;  //NBA208-32
		String sequence;
		int seq = 0;
		int highestseq = 0;
		//Make sure there are contracts to search
		if (!getLastSeq) {
			if (oldContract == null) {
				return seq;
			}
		}
		//begin NBA208-32
		List lobs = newLob.getLobs();
		int fieldcount = 0;
		if(lobs != null){
			fieldcount = lobs.size();
		}
		//end NBA208-32
		for (int i = 0; i < fieldcount; i++) {
			lobField = (LobData)lobs.get(i);  //NBA208-32
			if (lobField == null) {
				return seq;
			}
			//As per NBA243, not to set Replacement Company and Replaced Number LOBs as they are no longer supported by Base.
		}
		if (!getLastSeq) {
			return 0; //LOB fields not found.
		}
		return seq;
	}
	
	/**
	 * Deletes work item LOB fields.
	 * @param nbaLob
	 * @param newLob
	 * @param oldContract
	 * @param aNbaTXLife
	 * @param replacementType
	 * @return boolean
	 */
	//NBA084 New Method
	protected boolean deleteReplacementLOB(NbaLob nbaLob, NbaLob newLob, NbaReplacementVO oldContract, long replacementType) {
		int idx = findReplacementLOB(newLob, oldContract, replacementType, false);
		if (idx > 0) {
			//As per NBA243, not to set Replacement Company and Replaced Number LOBs as they are no longer supported by Base.
			nbaLob.setReplacementIndicatorAt(null, idx); 
			return true;
		} 
		return false;
	}
	/**
	 * Compares the work item LOB fields with the values retrieved from the Replacement view.
	 * @param nbaLob
	 * @param newLob
	 * @param contract
	 * @param oldContract
	 * @param aNbaTXLife
	 * @param replacementType
	 * @return boolean
	 */
	//NBA084 New Method
	protected boolean updateReplacementLOB(NbaLob nbaLob, NbaLob newLob, NbaReplacementVO contract,
										   NbaReplacementVO oldContract, NbaTXLife aNbaTXLife, long replacementType) {
		long newReplacementType = getReplacementType(aNbaTXLife);
		int idx = findReplacementLOB(newLob, oldContract, replacementType, false);
		if (idx > 0) {
			//As per NBA243, not to set Replacement Company and Replaced Number LOBs as they are no longer supported by Base.
			nbaLob.setReplacementIndicatorAt(String.valueOf(newReplacementType), idx);
			return true;
		} 
		return false;
	}
	/**
	 * Sets LOB fields to values retrieved from the Replacement view.
	 * @param nbaLob
	 * @param newLob
	 * @param contract
	 * @param aNbaTXLife
	 * @param replacementType
	 */
	//NBA084 New Method
	protected void addReplacementLOB(NbaLob nbaLob, NbaLob newLob, NbaReplacementVO contract, NbaTXLife aNbaTXLife, long replacementType) {
		long newReplacementType = getReplacementType(aNbaTXLife);
		int idx = findReplacementLOB(newLob, contract, replacementType, true);
		//As per NBA243, not to set Replacement Company and Replaced Number LOBs as they are no longer supported by Base.
		nbaLob.setReplacementIndicatorAt(String.valueOf(newReplacementType), idx + 1);
	}
}
