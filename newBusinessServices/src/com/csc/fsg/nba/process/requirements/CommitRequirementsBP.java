package com.csc.fsg.nba.process.requirements;

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
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.business.process.NbaProcessStatusProvider;
import com.csc.fsg.nba.business.transaction.NbaRequirementUtils;
import com.csc.fsg.nba.business.uwAssignment.AxaUnderwriterAssignmentEngine;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaReqIndicatorUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.vo.AxaUWAssignmentEngineVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaRequirement;
import com.csc.fsg.nba.vo.NbaRequirementRequestVO;
import com.csc.fsg.nba.vo.NbaRequirementResponseVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransOliCode;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaWorkItemProviderRequest;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;
import com.csc.fsg.nba.vo.txlife.TrackingInfo;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * Commits the updated requirements
 * the <code>NbaCompanionCaseFacadeBean</code>. Requires an <code>NbaDst</code>.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item retrieval</td></tr>
 * <tr><td>SPR3436</td><td>Version 8</td><td>RequirementInfo.ReceivedDate Not Updated For Manually Satisfied Requirements</td></tr>
 * <tr><td>NBA224</td><td>Version 8</td><td>nbA Underwriter Workbench Requirements and Impairments Enhancement</td></tr>
 * <tr><td>ALPC153</td><td>AxaLife Phase 1</td><td>Fast Team</td></tr>
 * <tr><td>ALS5099</td><td>AxaLife Phase 1</td><td>QC # 4266  - 03.07.09 UW Workbench : When manually Receipting/Satisfying a Requirement, nbA creates an Image icon when no image exists</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class CommitRequirementsBP extends NewBusinessAccelBP {
    protected NbaOLifEId nbaOLifEId = null;
    protected Map newDoctorMap = new HashMap(); //NBA224
    protected static final String NEW_DOCTOR_PARTY = "NEW_DOCTOR_PARTY_"; //NBA224
    /**
     * Called to retrieve a List of companion cases for the given case
     * @param an instance of <code>NbaDst</code> object
     * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
     */
    public Result process(Object input) {
        AccelResult result = new AccelResult();
        try {
            result.addResult(processRequirements((NbaRequirementRequestVO) input));
        } catch (Exception e) {
            e.printStackTrace();
            addExceptionMessage(result, e);
        }
        return result;
    }

    /**
     * Apply changes for Requirements and RequirementReviews to the contract.
     * 
     * @param reqNav - RequirementsNavigation
     * @throws NbaBaseException if a Relation for the Person cannot be located
     */
    private NbaRequirementResponseVO processRequirements(NbaRequirementRequestVO reqRequestVO) throws NbaBaseException {
        NbaRequirementResponseVO reqRespVO = new NbaRequirementResponseVO();
        String partyID = null;
        long personCode;
        long personSeq;
        List insuredRelations = reqRequestVO.getRelationsToUpdate();
        int count = insuredRelations.size(); 
        NbaTXLife nbaTXLife = reqRequestVO.getNbaTXLife();
        NbaDst nbaDst = reqRequestVO.getNbaDst();
        reqRespVO.setReqMap(new HashMap(count));
        setPartyMapForLastReqIndicator(reqRequestVO);//NBLXA186-NBLXA1271
        for (int i = 0; i < count; i++) {
            Relation relation = (Relation) insuredRelations.get(i);
            partyID = relation.getRelatedObjectID();
            personCode = relation.getRelationRoleCode();
            personSeq = Long.parseLong(relation.getRelatedRefID());
            List requirements = (List) reqRequestVO.getReqMap().get(partyID);
            if (requirements != null) {
                commitRequirements(reqRequestVO, requirements, partyID, personCode, personSeq, nbaTXLife, nbaDst);
                //TODO passing map back to see if we can make commit/retrieve more efficient
                reqRespVO.getReqMap().put(partyID, requirements);
            }
        }
        reqRespVO.setNbaTXLife(nbaTXLife);
        reqRespVO.setNbaDst(nbaDst);
        return reqRespVO;
        
    }
    /**
     * Commits the <code>Requirements</code> changes to the <code>NbaTXLife</code> and <code>NbaDst</code> so that they can be committed to the
     * back end.
     * 
     * @param reqs Requirements for a party to be committed
     * @param partyID identifier for the insured of these requirements
     * @param personCode person code identifier for the insured
     * @param personSeq person sequence identifier for the insured
     * @throws NbaBaseException
     */
    protected void commitRequirements(NbaRequirementRequestVO reqRequestVO, List reqs, String partyID, long personCode, long personSeq, NbaTXLife nbaTXLife, NbaDst nbaDst)
            throws NbaBaseException {
             
            Policy policy = nbaTXLife.getPolicy(); 
            boolean hasManualOrderedOrFollowedUpWork = false; 
            NbaRequirement nbaReq = null;
            String user = reqRequestVO.getNbaUserVO().getUserID();
            NbaLob lob = reqRequestVO.getNbaDst().getNbaLob();
            int count = reqs.size();
            for (int i = 0; i < count; i++) {
                nbaReq = (NbaRequirement) reqs.get(i);
                if (nbaReq.isActionAdd()) {
                    nbaReq.setPersonCode(personCode);
                    nbaReq.setPersonSeq(personSeq);
                    RequirementInfo reqInfo = new NbaRequirementUtils().createRequirementInfo(reqRequestVO.getNbaTXLife(), lob, nbaReq, getNbaOLifEId(nbaTXLife), partyID);//ALS2886, ALS4243, APSL1427
                    RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo); 
                    NbaRequirementUtils.addProviderInfo(nbaReq.getVendor(), reqInfoExt);
                    reqInfoExt.setReqOrderReason(nbaReq.getReqOrdReason()); //NBLXA-1895
                    reqInfoExt.setFollowUpDate(nbaReq.getFollowUpDate()); //APSL3545
                    reqInfo.setRequirementInfoUniqueID(NbaRequirementUtils.generateRequirementInfoUniqueID(nbaTXLife, reqInfo));
                    nbaReq.setUniqueID(reqInfo.getRequirementInfoUniqueID());
                    reqInfo.setUserCode(user);
                    NbaRequirementUtils.createRequirementTransaction(nbaDst, nbaReq);
                    reqInfo.setAppliesToPartyID(nbaTXLife.getPartyId(personCode));
                    policy.addRequirementInfo(reqInfo);
                    policy.setActionUpdate();
                    if (nbaReq.hasDoctor() || ! NbaUtils.isBlankOrNull(nbaReq.getFullName())) { //NBLXA-1343
                        addDoctorToRequirement(nbaTXLife, nbaReq, reqInfo);
                    }
                    //END NBA130
                   //QC12292 APSL3402 deleted code 
                   } else if (nbaReq.isActionUpdate()) {
                	RequirementInfo reqInfo = nbaTXLife.getRequirementInfo(nbaReq.getRequirementInfoUniqueID()); //NBA208-36
                    RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo); //NBA208-36
                    if (nbaReq.getTransactionID() != null) {	//Null if only Review was changed	//NBA208-36
	                    if (nbaReq.getStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED) {
	                        NbaRequirementUtils.satisfyRequirement(nbaDst, nbaReq, reqInfo);
	                    } else {
	                        NbaRequirementUtils.updateRequirement(nbaDst, nbaReq);
	                    }
	                }	//NBA208-36
                    NbaRequirementUtils.updateRequirement(nbaTXLife, nbaReq);
                    //NBA208-36 code deleted
                    NbaRequirementUtils.addProviderInfo(nbaReq.getVendor(), reqInfoExt); 
                    reqInfo.setReqStatus(nbaReq.getStatus());
                    reqInfo.setRequirementDetails(nbaReq.getMessage());
                    if(reqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_RECEIVED && !reqInfo.hasReceivedDate()) { //SPR3436 //ALS3039
                        reqInfo.setReceivedDate(new Date());	//SPR3436
                        reqInfoExt.setReceivedDateTime(NbaUtils.getStringFromDateAndTime(new Date()));//QC20240
                    }	//SPR3436
                    if(reqInfo.getReqStatus() == NbaOliConstants.OLI_REQSTAT_WAIVED) { //ALS2271
                        reqInfo.setStatusDate(new Date());	//ALS2271
                    } //ALS2271

                    reqInfo.setActionUpdate();
                    if (nbaReq.hasDoctor() || ! NbaUtils.isBlankOrNull(nbaReq.getFullName())) { // NBLXA-1343
                    	updateDoctorToRequirement(nbaTXLife, nbaReq, reqInfo, reqRequestVO); //NBA224
                    }
                    if (nbaReq.getManualOrderedFollowedUp() != null) {
                        updateRequirementForManualOrderedOrFollowedUp(nbaTXLife, nbaReq, reqInfo);
                        hasManualOrderedOrFollowedUpWork = true;
                    }
                }
                commitRequirementReviews(nbaTXLife, nbaReq, partyID, user);
                
                resetLastReqIndicator(nbaTXLife, partyID,reqRequestVO,nbaDst,nbaReq);//NBLXA186-NBLXA127
            }
            
            if (hasManualOrderedOrFollowedUpWork) { 
                createCompletedAggregateWork(reqRequestVO); 
            } 
    }

    /**
     * Updates Physician (Doctor) Party to RequirementInfo object
     * 
     * @param nbaTXLife
     * @param nbaReq
     * @param reqInfo
     * @throws NbaBaseException
     */
    // NBA224 Method signature changed
    protected void updateDoctorToRequirement(NbaTXLife nbaTXLife, NbaRequirement nbaReq, RequirementInfo reqInfo, NbaRequirementRequestVO reqRequestVO){
        Party doctor = NbaRequirementUtils.getDoctorForRequirement(nbaTXLife, reqInfo);
        if (doctor == null) {
            addDoctorToRequirement(nbaTXLife, nbaReq, reqInfo);
        } else {
        	 // begin NBA224
    		if (!doctor.getId().equals(nbaReq.getDrPartyId())) {
				Relation relation = findExistingRelation(reqInfo.getId(), doctor.getId(), nbaTXLife);
				if (relation != null) {
					Party newDoctor;
					String partyId = nbaReq.getDrPartyId();
					boolean isExistingDoctor = partyId.indexOf(NEW_DOCTOR_PARTY) == -1;
					if (isExistingDoctor) {
						relation.setRelatedObjectID(nbaReq.getDrPartyId());
						newDoctor = nbaTXLife.getParty(nbaReq.getDrPartyId()).getParty();
						NbaRequirementUtils.updateDoctorForRequirement(newDoctor, nbaReq, getNbaOLifEId(nbaTXLife));
					} else {
						if (getNewDoctorMap().containsKey(partyId)) {
							newDoctor = (Party) getNewDoctorMap().get(partyId);
						} else {
							OLifE olife = nbaTXLife.getOLifE();
							newDoctor = NbaRequirementUtils.createDoctorForRequirement(nbaReq, getNbaOLifEId(nbaTXLife));
							olife.addParty(newDoctor);
							olife.setActionUpdate();
							getNewDoctorMap().put(partyId, newDoctor);
						}
						relation.setRelatedObjectID(newDoctor.getId());
					}
					//ALS3872 begin
					long drRelationRole = nbaReq.getDrPartyType() == NbaOliConstants.OLI_PT_PERSON ? NbaOliConstants.OLI_REL_PHYSICIAN : NbaOliConstants.OLI_REL_MEDPROVIDER;
					relation.setRelationRoleCode(drRelationRole);
					//ALS3872 end
					relation.setActionUpdate();
					RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
					reqInfoExt.setPhysicianPartyID(newDoctor.getId());
					reqInfoExt.setActionUpdate();
				}
				List relList = getAllRelationsForParty(doctor.getId(), nbaTXLife);
				if (relList.size() == 0 && !reqRequestVO.getDoctorToUpdateList().contains(doctor.getId())) {
					doctor.setActionDelete();
				}
				return;
			}
    		// end NBA224
            NbaRequirementUtils.updateDoctorForRequirement(doctor, nbaReq, getNbaOLifEId(nbaTXLife));
        }
    }
    /**
     * Adds Physician (Doctor) Party to RequirementInfo object
     * 
     * @param nbaTXLife
     * @param nbaReq
     * @param reqInfo
     * @throws NbaBaseException
     */
    private void addDoctorToRequirement(NbaTXLife nbaTXLife, NbaRequirement nbaReq, RequirementInfo reqInfo)  {
    	Party doctor; //NBA224
        RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
        if (reqInfoExt == null) {
            OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
            reqInfo.addOLifEExtension(olifeExt);
            reqInfoExt = olifeExt.getRequirementInfoExtension();
            reqInfoExt.setActionAdd();
        } else {
            reqInfoExt.setActionUpdate();
        }
        // Begin NBA224
        String partyId = nbaReq.getDrPartyId();
        long drRelationRole = nbaReq.getDrPartyType() == NbaOliConstants.OLI_PT_PERSON ? NbaOliConstants.OLI_REL_PHYSICIAN : NbaOliConstants.OLI_REL_MEDPROVIDER; //ALS3872
        boolean isExistingDoctor = partyId.indexOf(NEW_DOCTOR_PARTY) == -1;
        if (partyId != null && isExistingDoctor ) {
			reqInfoExt.setPhysicianPartyID(partyId);
			doctor = nbaTXLife.getParty(partyId).getParty();
			nbaTXLife.createRelation(reqInfo, doctor, drRelationRole);//ALS3872
			NbaRequirementUtils.updateDoctorForRequirement(doctor, nbaReq, getNbaOLifEId(nbaTXLife));
			return;
		} 
    	if(getNewDoctorMap().containsKey(partyId)) {
    		doctor = (Party)getNewDoctorMap().get(partyId);
    		nbaTXLife.createRelation(reqInfo, doctor, drRelationRole);//ALS3872
            reqInfoExt.setPhysicianPartyID(doctor.getId());
            return;
    	}
        // End NBA224
        OLifE olife = nbaTXLife.getOLifE();
        doctor = NbaRequirementUtils.createDoctorForRequirement(nbaReq, getNbaOLifEId(nbaTXLife)); //NBA224
        olife.addParty(doctor);
        nbaTXLife.createRelation(reqInfo, doctor, drRelationRole);//ALS3872
        reqInfoExt.setPhysicianPartyID(doctor.getId());
        olife.setActionUpdate();
        getNewDoctorMap().put(partyId, doctor); //NBA224
    }

    /**
     * Updates requirement info for the follow up completed information
     * 
     * @param nbaTXLife the holding inquiry
     * @param nbaReq the nba requirement
     * @param reqInfo the requirement info
     */
    protected void updateRequirementForManualOrderedOrFollowedUp(NbaTXLife nbaTXLife, NbaRequirement nbaReq, RequirementInfo reqInfo) {
        if (NbaRequirement.MANUALLY_FOLLOWED_UP.equals(nbaReq.getManualOrderedFollowedUp())) {
            RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
            if (reqInfoExt == null) {
                OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
                reqInfo.addOLifEExtension(olifeExt);
                reqInfoExt = olifeExt.getRequirementInfoExtension();
            }
            TrackingInfo trackingInfo = reqInfoExt.getTrackingInfo();
            if (trackingInfo == null) {
                trackingInfo = new TrackingInfo();
                getNbaOLifEId(nbaTXLife).setId(trackingInfo);
                trackingInfo.setActionAdd();
                reqInfoExt.setTrackingInfo(trackingInfo);
            }
            trackingInfo.setFollowUpCompleted(true);
            trackingInfo.setActionUpdate();
        }
    }

    /**
     * Creates the Completed Aggregate work item
     * 
     * @param nbaDst the case work item
     * @throws NbaBaseException
     */
    protected void createCompletedAggregateWork(NbaRequirementRequestVO reqRequestVO) throws NbaBaseException {
        NbaWorkItemProviderRequest request = new NbaWorkItemProviderRequest();
        // get user from request
        request.setNbaUserVO(reqRequestVO.getNbaUserVO());
        request.setDst(reqRequestVO.getNbaDst());
        request.setUserID(NbaConstants.PROC_VIEW_REQUIREMENT_AGGREGATE);
       
        // this is a BP call that needs to happen
        Result res = callService("WorkItemProviderBP", request);  //NBA213
        if (res != null) {
            request = (NbaWorkItemProviderRequest) res.getFirst();
            reqRequestVO.getNbaDst().addTransaction(request.getWorkType(), request.getInitialStatus());
        } else {
            new NbaBaseException("Could not create the completed aggregate work item");
        }
    }
    /**
     * Apply Requirement Review information to RequirementInfo objects for a Party
     * 
     * @param nbaTXLife - the NbaTXLife
     * @param reqs - list of Requirement objects
     * @param partyID - the id of the Party
     * @throws NbaBaseException
     */
    protected void commitRequirementReviews(NbaTXLife nbaTXLife, NbaRequirement nbaReq, String partyID, String user){
        Map reqMap = nbaTXLife.getRequirementInfos(partyID);
        if (nbaReq.getReviewInd() && nbaReq.getReviewDate() == null) {
            RequirementInfo reqInfo = (RequirementInfo) reqMap.get(nbaReq.getRequirementInfoUniqueID()); //SPR3145
            RequirementInfoExtension reqInfoExt = NbaUtils.getFirstRequirementInfoExtension(reqInfo);
            if (reqInfoExt == null) {
                OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_REQUIREMENTINFO);
                reqInfo.addOLifEExtension(olifeExt);
                reqInfoExt = olifeExt.getRequirementInfoExtension();
            }
            reqInfoExt.setReviewedInd(true);
            if (nbaReq.getReviewID() != null) {
                reqInfoExt.setReviewID(nbaReq.getReviewID());
            } else {
                reqInfoExt.setReviewID(user); //default to the current user
            }
            reqInfoExt.setReviewDate(new java.util.Date());
            reqInfoExt.setActionUpdate();
        }
    }
    /**
     * Return a NbaOLifEId with lazy initialization.
     * 
     * @param nbaTXLife
     * @return an initialized NbaOLifEId
     */
    protected NbaOLifEId getNbaOLifEId(NbaTXLife nbaTXLife) {
        if (nbaOLifEId == null) {
            nbaOLifEId = new NbaOLifEId(nbaTXLife);
        }
        return nbaOLifEId;
    }
    
	/**
     * Finds an existing <code>Relation</code> based on the related ids.
     * @param id
     * @param type
     * @return existing relation
     */
	// NBA224 New Method
    protected Relation findExistingRelation(String reqInfoId, String partyId, NbaTXLife nbaTXLife) {
    	OLifE olife = nbaTXLife.getOLifE(); 
        Relation relation = null;
        int relationCount = olife.getRelationCount();
        for (int index = 0; index < relationCount; index++) {
            relation = olife.getRelationAt(index);
            if (reqInfoId.equals(relation.getOriginatingObjectID()) && partyId.equals(relation.getRelatedObjectID())) {
               return relation;
            }
        }
        return null;
    }
    
    /**
     * Gets all the related objects matching a given partyId. It returns a List of the matching relation objects
     * @param partyId inputRole code to match
     * @param nbaTXLife
     * @return a List of matching relation objects
     */
    //NBA224 New Method
    protected List getAllRelationsForParty(String partyId, NbaTXLife nbaTXLife) {
        OLifE olife = nbaTXLife.getOLifE(); 
        List relationList = new ArrayList();
        Relation relation = null;
        int relationCount = olife.getRelationCount();
        for (int index = 0; index < relationCount; index++) {
            relation = olife.getRelationAt(index);
            if (partyId.equals(relation.getOriginatingObjectID()) || partyId.equals(relation.getRelatedObjectID())) {
                relationList.add(relation);
            }
        }
        return relationList;
    }
    
    
	/**
	 * @return Returns the newDoctorMap.
	 */
    // NBA224 New Method
	public Map getNewDoctorMap() {
		return newDoctorMap;
	}
	/**
	 * @param newDoctorMap The newDoctorMap to set.
	 */
	// NBA224 New Method
	public void setNewDoctorMap(Map newDoctorMap) {
		this.newDoctorMap = newDoctorMap;
	}
	
	
	//QC12292 APSL3402 Deleted code
	
	/**
	 * This method resets underwriter and case manager queues
	 * @param nbaTXLife
	 * @param lob
	 * @throws NbaBaseException
	 */
	//ALPC153 New Method
	//ALS4381 Modified signature - Removed parameter NbaLob lob, and added NbaDst work
	public void resetUWCM_Old(NbaTXLife nbaTXLife, NbaDst work) throws NbaBaseException {
		NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(NbaVpmsConstants.UISTATUS_UNDERWRITER_WORKBENCH, ""), work, nbaTXLife);
		String otherStatus = provider.getOtherStatus();
		if (otherStatus.indexOf(work.getNbaLob().getUndwrtQueue()) == -1) {
			String underwriterLOB = determineEquitableQueue(work.getNbaLob(), Arrays.asList(otherStatus.split(",")), NbaLob.A_LOB_ORIGINAL_UW_WB_QUEUE); // ALII1187
			String oldUWQueue = work.getNbaLob().getUndwrtQueue();//ALS5763
			work.getNbaLob().setUndwrtQueue(underwriterLOB);
			String caseManagerResult = provider.getAlternateStatus();
			if (caseManagerResult.indexOf("|") != -1) {
				caseManagerResult = getListOfCMQueues(caseManagerResult, underwriterLOB);
			}
			work.getNbaLob().setCaseManagerQueue(caseManagerResult);
			work.setUpdate();
			if (work.getQueue().equals(oldUWQueue) && (! oldUWQueue.equals(underwriterLOB))) {//ALS5763
				setStatus(work, NbaConstants.PROC_VIEW_UNDERWRITER_QUEUE_REASSIGNMENT, nbaTXLife);//ALS5763
			}
		}
	}

	
    //ALS5763 New Method
    protected void setStatus(NbaDst work, String userID, NbaTXLife contract) throws NbaBaseException {
        NbaProcessStatusProvider provider = new NbaProcessStatusProvider(new NbaUserVO(userID, ""), work, contract);
        work.setStatus(provider.getPassStatus());
        work.increasePriority(provider.getCaseAction(), provider.getCasePriority());
        NbaUtils.setRouteReason(work, work.getStatus());
	}
    
    //New Method NBLXA186-NBLXA1271
	protected void resetLastReqIndicator(NbaTXLife nbaTXLife, String partyID, NbaRequirementRequestVO reqRequestVO, NbaDst nbadst,
			NbaRequirement nbaReq) throws NbaBaseException {
		if (nbaTXLife != null) {
			Policy policy = nbaTXLife.getPolicy();
			ApplicationInfo appInfo = policy.getApplicationInfo();
			boolean anyOtherPartyReqReceived = false;
			long roleCode = -1L;
			String roleTrans = "";
			String reqTrans = "";
			String reqStatusTrans = "";

			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(appInfo);
			if (appInfoExt != null && NbaOliConstants.OLIX_UNDAPPROVAL_UNDERWRITER != appInfoExt.getUnderwritingApproval()) {
				Party currentParty = null;
				PartyExtension partyExtension = null;
				if (nbaTXLife.getParty(partyID) != null) {
					currentParty = nbaTXLife.getParty(partyID).getParty();
					if (currentParty != null) {
						partyExtension = NbaUtils.getFirstPartyExtension(currentParty);
					}
					if (partyExtension != null) {
						Relation relation = NbaUtils.getRelationForParty(currentParty.getId(), nbaTXLife.getOLifE().getRelation().toArray());
						if (relation != null) {
							roleCode = relation.getRelationRoleCode();
							roleTrans = NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REL, roleCode);
						}
						reqStatusTrans = NbaTransOliCode.lookupText(NbaOliConstants.OLI_LU_REQSTAT, Long.valueOf(nbaReq.getStatus()));
						reqTrans = NbaUtils.getRequirementTranslation(String.valueOf(nbaReq.getType()), nbaTXLife.getPolicy());
						// setting an anyOtherPartyReqReceived to true if any other parties requirement has been received on the case i.e
						// LasRequirementIndForParty
						// = RECEIVED for any of the other parties
						for (String partyIDForReq : reqRequestVO.getPartyMapForReq()) {
							if (!partyIDForReq.equalsIgnoreCase(partyID)) {
								if (nbaTXLife.getParty(partyIDForReq) != null) {
									Party party1 = nbaTXLife.getParty(partyIDForReq).getParty();
									long relationRoleCode = -1;
									if (party1 != null) {
										//Begin : Added for NBLXA 1432
										Relation relationForOtherParty = NbaUtils.getRelationForParty(party1.getId(), nbaTXLife.getOLifE()
												.getRelation().toArray()); 
										if (relationForOtherParty != null) {
											relationRoleCode = relationForOtherParty.getRelationRoleCode();
										}
										//End : Added for NBLXA 1432
										PartyExtension partyExtension1 = NbaUtils.getFirstPartyExtension(party1);
										if ((relationRoleCode != NbaOliConstants.OLI_REL_OWNER && relationRoleCode != NbaOliConstants.OLI_REL_DEPENDENT)
												&& partyExtension1 != null
												&& NbaOliConstants.OLI_LU_LASTREQSTAT_RECEIVED == partyExtension1.getLastRequirementIndForParty()) { //Modified for NBLXA1432
											anyOtherPartyReqReceived = true;
										}
									}
								}
							}

						}
						long oldLastReqIndValue = partyExtension.getLastRequirementIndForParty();
						if (nbaReq.isActionUpdate()) {
							long lastReqReceivedStatus = AxaReqIndicatorUtils.validateReceivedRequirements(nbaTXLife, partyID);

							if (!NbaUtils.isBlankOrNull(lastReqReceivedStatus)) {
								partyExtension.setLastRequirementIndForParty(lastReqReceivedStatus);
								partyExtension.setActionUpdate();
								if (roleCode != NbaOliConstants.OLI_REL_OWNER && roleCode != NbaOliConstants.OLI_REL_DEPENDENT) {//NBLXA-1432
									appInfoExt.setLastRequirementInd(lastReqReceivedStatus);
									// Deleted code for NBLXA-1432
									if (NbaOliConstants.OLI_LU_LASTREQSTAT_RECEIVED == appInfoExt.getLastRequirementInd() || anyOtherPartyReqReceived) {
										if (!(nbadst.getNbaLob().getLstNonRevReqRec())) {// NBLXA-2385
											nbadst.getNbaLob().setQueueEntryDate(new Date()); // NBLXA-2385
										}
										nbadst.getNbaLob().setSigReqRecd(true);
										nbadst.getNbaLob().setLstNonRevReqRec(true);// QC18821/APSL5385
										appInfoExt.setLastRequirementInd(lastReqReceivedStatus);
									}
								}
								if ((oldLastReqIndValue != lastReqReceivedStatus)) {
									if (lastReqReceivedStatus != NbaOliConstants.OLI_LU_LASTREQSTAT_COMPLETE) {
										NbaUtils.addAutomatedComment(nbadst, reqRequestVO.getNbaUserVO(), "Last Requirement Indicator for " + roleTrans
												+ "  has been changed to ON due to '" + reqTrans + "' requirement " + reqStatusTrans + " on case.");
									} //NBLXA-1718 changed from general to automated
									// Deleted code for NBLXA-1432
								}
								appInfoExt.setActionUpdate();
								nbadst.setUpdate();
							}
						}

						else if (nbaReq.isActionAdd() && nbaReq.getUWRequirementInd() == 1) {
							partyExtension.setLastRequirementIndForParty(NbaOliConstants.OLI_LU_LASTREQSTAT_INCOMPLETE);
							partyExtension.setActionUpdate();
							if (roleCode != NbaOliConstants.OLI_REL_OWNER && roleCode != NbaOliConstants.OLI_REL_DEPENDENT) {//Added for NBLXA 1432
								appInfoExt.setLastRequirementInd(NbaOliConstants.OLI_LU_LASTREQSTAT_INCOMPLETE);
								// set composite indicator for multiparty
								if (anyOtherPartyReqReceived) {
									appInfoExt.setLastRequirementInd(NbaOliConstants.OLI_LU_LASTREQSTAT_RECEIVED);
									nbadst.getNbaLob().setSigReqRecd(true);
									nbadst.getNbaLob().setLstNonRevReqRec(true);// QC18821/APSL5385
								} else if (nbadst.getNbaLob().getSigReqRecd()) {
									nbadst.getNbaLob().setSigReqRecd(false);
									nbadst.getNbaLob().setLstNonRevReqRec(false);// QC18821/APSL5385
								}
							}
							// Modified for QC18832/APSL5386
							if (oldLastReqIndValue == NbaOliConstants.OLI_LU_LASTREQSTAT_RECEIVED) {
								NbaUtils.addAutomatedComment(nbadst, reqRequestVO.getNbaUserVO(), "Last Requirement Indicator for " + roleTrans
										+ "  has been changed to OFF due to '" + reqTrans + "' requirement added on case."); //NBLXA-1718 changed from general to automated
							} 
							appInfoExt.setActionUpdate();
							nbadst.setUpdate();

						}
					}
				}
			}
		}

	}
    
    //New Method NBLXA186-NBLXA1272
    protected void setPartyMapForLastReqIndicator(NbaRequirementRequestVO reqRequestVO){
    	List insuredRelations = reqRequestVO.getRelationsToUpdate();
    	Set<String> partySet= new HashSet<String>();
        int count = insuredRelations.size(); 
        for (int i = 0; i < count; i++) {
            Relation relation = (Relation) insuredRelations.get(i);
            String partyID = relation.getRelatedObjectID();
            if (reqRequestVO.getNbaTXLife().getPolicy() != null) {
                int count1 = reqRequestVO.getNbaTXLife().getPolicy().getRequirementInfoCount();
                RequirementInfo reqInfo = null;
                for (int j = 0; j < count1; j++) {
                    reqInfo = reqRequestVO.getNbaTXLife().getPolicy().getRequirementInfoAt(j);
                    if (partyID.equals(reqInfo.getAppliesToPartyID())) {
                    		partySet.add(partyID);
                    		break;
                    }
                }
               
            }
        }
        reqRequestVO.setPartyMapForReq(partySet);
    }
}
