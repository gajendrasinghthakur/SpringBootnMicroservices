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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Employment;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;

/**
 * This class is responsible for creating generic 502 request.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.21</td><td>AXA Life Phase 1</td><td>Prior Insurance</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWS502Invoker extends AxaWSInvokerBase{

    /**
     * constructor from superclass
     * @param userVO
     * @param nbaTXLife
     */
    public AxaWS502Invoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation, userVO, nbaTXLife, nbaDst, object);
    }

    /**
     * This method is used for creating generic 502 request
     * @return nbaReqTXLife
     */
    public NbaTXLife createRequest() throws NbaBaseException {
    	NbaTXLife nba502ReqTXLife = createTXLifeRequest(NbaOliConstants.TC_TYPE_RATECLASSCHANGE, NbaOliConstants.TC_SUBTYPE_HOLDING_CHANGE_TXN,
				getUserVO().getUserID());
    	addChangeSubType((ArrayList) getObject(), nba502ReqTXLife);
    	OLifE newOlifE = getNbaTXLife().getOLifE().clone(false);
    	nba502ReqTXLife.setOLifE(newOlifE);
		addDeletedObjectsInTrans((ArrayList) getObject(), nba502ReqTXLife);
		return nba502ReqTXLife;
    }
    
	/**
	 * @param list
	 * @param nba502ReqTXLife
	 * @param nbaTXLife
	 * @throws NbaBaseException
	 */
	protected void addDeletedObjectsInTrans(ArrayList changeSubTypeList, NbaTXLife nba502ReqTXLife) throws NbaBaseException {
		Iterator changeSubTypeItr = changeSubTypeList.iterator();
		while(changeSubTypeItr.hasNext()){
			ChangeSubType changeSubType = (ChangeSubType) changeSubTypeItr.next();
			if (NbaOliConstants.OLI_CHG_SUB_STANDARD_RATING == changeSubType.getChangeTC()){
				SubstandardRating substandardRating = getNbaTXLife().getSubstandardRatingById(changeSubType.getChangeID());
				if(substandardRating != null && substandardRating.getActionIndicator().isDelete()){
					LifeParticipant lifePart502 = nba502ReqTXLife.getLifeParticipantById(substandardRating.getParentIdKey());
					substandardRating.setAction(null);
					SubstandardRating newsubstanRating = substandardRating.clone(false);
					lifePart502.addSubstandardRating(newsubstanRating);
					substandardRating.setActionDelete();
				}
			} 
			if (NbaOliConstants.OLI_CHG_COVCHG == changeSubType.getChangeTC()){
				Coverage coverage = getNbaTXLife().getCoverage(changeSubType.getChangeID());
				if(coverage.getActionIndicator().isDelete()){
					coverage.setAction(null);
					Coverage newCoverage = coverage.clone(false);
					coverage.setActionDelete();
					nba502ReqTXLife.getLife().addCoverage(newCoverage);
				}
			} 
			if (changeSubType.getChangeTC() == NbaOliConstants.OLI_CHG_CHGRELATION){
				Relation relation = getNbaTXLife().getRelationFromId(changeSubType.getChangeID());
				if(relation.getActionIndicator().isDelete()){
					//Add deleted relation in 502 Tx
					nba502ReqTXLife.getOLifE().addRelation(createRelation(nba502ReqTXLife, relation));
					//Add related deleted party and related deleted relations also
					String partyId = relation.getRelatedObjectID();
					String holdingId = relation.getOriginatingObjectID();
					NbaParty nbaParty = getNbaTXLife().getParty(partyId);
					Holding holding = getNbaTXLife().getHolding(holdingId);//QC#10129/APSL2624
					if(nbaParty != null && nbaParty.getParty().getActionIndicator().isDelete()) {
						addPartyAndRelatedRelations(partyId, getNbaTXLife(), nba502ReqTXLife);	
					}
					//Begin QC#10129/APSL2624
					if (holding != null && holding.getActionIndicator().isDelete()) {
						addHoldingAndRelatedRelations(holdingId, getNbaTXLife(), nba502ReqTXLife);
					}//End QC#10129/APSL2624
				}
			}
			if (changeSubType.getChangeTC() == NbaOliConstants.OLI_CHG_PARTYINFO) {
				NbaParty nbaParty = getNbaTXLife().getParty(changeSubType.getChangeID());
				if(nbaParty != null && nbaParty.getParty().getActionIndicator().isDelete()){
					addPartyAndRelatedRelations(changeSubType.getChangeID(), getNbaTXLife(), nba502ReqTXLife);
				}
			}
		}
	}

	public NbaTXLife createTXLifeRequest(long transType, long transSubType, String businessUser) {
		NbaTXLife nba502ReqTXLife = super.createTXLifeRequest(transType, transSubType, businessUser);
		nba502ReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		return nba502ReqTXLife;
	}
    
	/**
	 * Set the change subType from request
	 * @param changeSubTypesInfo
	 * @param nba502ReqTXLife
	 * @param txLife
	 */
	//New Method AXAL3.7.21
	protected void addChangeSubType(ArrayList changeSubTypeList, NbaTXLife txLife502) {
		TXLifeRequest nbaResTXLifeRequest = getTXLifeRequest(txLife502);
		NbaOLifEId nbaOLifEId = new NbaOLifEId(txLife502);
		Iterator changeSubTypeItr = changeSubTypeList.iterator();
		while (changeSubTypeItr.hasNext()) {
			ChangeSubType changeSubType = (ChangeSubType) changeSubTypeItr.next();
			nbaOLifEId.setId(changeSubType);
			nbaResTXLifeRequest.addChangeSubType(changeSubType);
		}
	}
	
	/**
	 * Add party and related relation information
	 * @param partyId
	 * @param txLife
	 * @param olife
	 */
	//New Method AXAL3.7.21
	protected void addPartyAndRelatedRelations(String partyId, NbaTXLife txLife, NbaTXLife txLife502) throws NbaBaseException {
		OLifE olife = txLife502.getOLifE();
		NbaParty nbaParty = txLife.getParty(partyId);
		if (nbaParty != null && txLife502.getParty(partyId) == null) {
			Party newParty = createParty(partyId, txLife, txLife502);
			olife.addParty(newParty);
			OLifE oLifeMain = txLife.getOLifE();
			List relationList = oLifeMain.getRelation();
			int relationCount = relationList.size();
			for (int x = 0; x < relationCount; x++) {
				Relation aRelation = (Relation) relationList.get(x);
				if (aRelation.getRelatedObjectID().equalsIgnoreCase(partyId) && aRelation.getActionIndicator().isDelete()
						&& txLife502.getRelationFromId(aRelation.getId()) == null) {
					olife.addRelation(createRelation(txLife502, aRelation));
				}
			}
		}
	}
	
	/**
	 * @param nba502ReqTXLife
	 * @return
	 */
	protected TXLifeRequest getTXLifeRequest(NbaTXLife nba502ReqTXLife) {
		TXLife resTXLife = nba502ReqTXLife.getTXLife();
		UserAuthRequestAndTXLifeRequest userResandTXReq = resTXLife.getUserAuthRequestAndTXLifeRequest();
		TXLifeRequest nbaResTXLifeRequest = userResandTXReq.getTXLifeRequestAt(0);
		return nbaResTXLifeRequest;
	}
	
	/**
	 * Create Party object from old txLife to new Txlife
	 * @param partyId
	 * @param txlife
	 * @param txlife502
	 * @return
	 */
	public Party createParty(String partyId, NbaTXLife txlife, NbaTXLife txlife502) {
		Party newParty = new Party();

		NbaParty nbaParty = txlife.getParty(partyId);
		if (nbaParty != null) {
			Party origParty = nbaParty.getParty();
			newParty.setPartyTypeCode(origParty.getPartyTypeCode());
			newParty.setId(origParty.getId());
			newParty.setCompanyKey(origParty.getCompanyKey());
			newParty.setFullName(origParty.getFullName());
			newParty.setGovtID(origParty.getGovtID());
			PersonOrOrganization perOrg = new PersonOrOrganization();
			if (origParty.getPersonOrOrganization().isPerson()) {
				perOrg.setPerson(origParty.getPersonOrOrganization().getPerson().clone(false));
			} else {
				perOrg.setOrganization(origParty.getPersonOrOrganization().getOrganization().clone(false));
			}
			newParty.setPersonOrOrganization(perOrg);

			for (int i = 0; i < origParty.getAddressCount(); i++) {
				Address origAddr = origParty.getAddressAt(i);
				newParty.addAddress(origAddr.clone(false));
			}

			for (int i = 0; i < origParty.getPhoneCount(); i++) {
				Phone origPhone = origParty.getPhoneAt(i);
				newParty.addPhone(origPhone.clone(false));
			}
			newParty.setClient(origParty.getClient() != null ? origParty.getClient().clone(false) : null);
			newParty.setProducer(origParty.getProducer() != null ? origParty.getProducer().clone(false) : null);
			for (int i = 0; i < origParty.getEmploymentCount(); i++) {
				Employment employment = origParty.getEmploymentAt(i);
				newParty.addEmployment(employment.clone(false));
			}
		}
		return newParty;
	}

	/**
	 * Creates the relation object..
	 * 
	 * @return - the created relation object
	 * @param relCount - the count of relation on the olife.
	 * @param orgObjId - the originating object id
	 * @param relObjId - the relation object id
	 * @param orgType - the originating object type
	 * @param relType - the relation object type
	 * @param roleCode - the relation role code
	 */
	// AXAL3.7.31 New Method
	protected Relation createRelation(NbaTXLife txlife, Relation relation)
			throws NbaBaseException {
		if (txlife == null) {
			throw new NbaBaseException("Unable to create Relation object");
		}
		Relation rel = new Relation();
		rel.setId(relation.getId());
		rel.setOriginatingObjectID(relation.getOriginatingObjectID());
		rel.setRelatedObjectID(relation.getRelatedObjectID());
		rel.setOriginatingObjectType(relation.getOriginatingObjectType());
		rel.setRelatedObjectType(relation.getRelatedObjectType());
		rel.setRelationRoleCode(relation.getRelationRoleCode());
		return rel;
	}
    
    /**
     * This method is responsible for cleaning up the generic 1203 request created.
     * @return void
     */
    public void cleanRequest() throws NbaBaseException {  //APSL371 APSL372
    	super.cleanRequest();
		Policy policy = getNbaTxLifeRequest().getPolicy();
		getNbaTxLifeRequest().getPrimaryHolding().setSystemMessage(new ArrayList());//ALS1246 removal of all the System Messages from TX 500
		getNbaTxLifeRequest().getPrimaryHolding().setAttachment(new ArrayList());//AXAL3.7.14
		getNbaTxLifeRequest().getOLifE().setPolicyProduct(new ArrayList());
		getNbaTxLifeRequest().getPolicy().setRequirementInfo(new ArrayList());
		//Start NBLXA -1554[NBLXA-1878]
		Iterator formInstanceItr = getNbaTxLifeRequest().getOLifE().getFormInstance().iterator();
		while (formInstanceItr.hasNext()) {
			FormInstance formInstance = (FormInstance) formInstanceItr.next();
			if (formInstance != null && !NbaUtils.isBlankOrNull(formInstance.getId())) {
				Iterator<Relation> relation = getNbaTxLifeRequest().getOLifE().getRelation().iterator();
				while (relation.hasNext()) {
					Relation rel = relation.next();
					if (formInstance.getId().equalsIgnoreCase(rel.getRelatedObjectID())
							|| formInstance.getId().equalsIgnoreCase(rel.getOriginatingObjectID())) {
						relation.remove();
					}
				}
			}			
		}
		//END NBLXA -1554[NBLXA-1878]
		getNbaTxLifeRequest().getOLifE().setFormInstance(new ArrayList()); //ALS2412 - ALS2414 Removed the Form Instance's as well.
		Iterator relationItr = getNbaTxLifeRequest().getOLifE().getRelation().iterator();
		//AXAL3.7.27 Removed the relations related to Requirements. Since we are removing the requirements above.
		while (relationItr.hasNext()) {
			Relation relation = (Relation) relationItr.next();
			if (NbaOliConstants.OLI_REL_PHYSICIAN == relation.getRelationRoleCode() ||
					NbaOliConstants.OLI_REL_MEDPROVIDER == relation.getRelationRoleCode()) { //TODO NEED Scarab defect
				relationItr.remove();
			}
		}
		policy.setEndorsement(new ArrayList());//ALS2989
		policy.setFinancialActivity(new ArrayList());//AXAL3.7.22 removing the Financial Activity objects from 502
	}
    
    /**
	 * Add Holding and related relation information for 502Txlife
	 * @param partyId
	 * @param txLife
	 * @param txLife502
	 */
	//New Method QC#10129/APSL2624
    protected void addHoldingAndRelatedRelations(String holdingId, NbaTXLife txLife, NbaTXLife txLife502) throws NbaBaseException {
		OLifE olife = txLife502.getOLifE();
		Holding holding = txLife.getHolding(holdingId);
		if (holding != null && txLife502.getHolding(holdingId) == null) {
			Holding newHolding = createHolding(holdingId, txLife);
			olife.addHolding(newHolding);
			OLifE oLifeMain = txLife.getOLifE();
			List relationList = oLifeMain.getRelation();
			int relationCount = relationList.size();
			for (int x = 0; x < relationCount; x++) {
				Relation aRelation = (Relation) relationList.get(x);
				if (aRelation.getRelatedObjectID().equalsIgnoreCase(holdingId) && txLife502.getRelationFromId(aRelation.getId()) == null) {
					olife.addRelation(createRelation(txLife502, aRelation));
				}
			}
		}
	}
    
    /**
	 * Create sample Holding object to new 502Txlife
	 * @param holdingId
	 * @param txlife
	 * @return Holding
	 * New Method QC#10129/APSL2624
	 */
	public Holding createHolding(String holdingId, NbaTXLife txlife) {
		Holding extHolding = txlife.getHolding(holdingId);
		Holding newHolding = new Holding();
		if (extHolding != null) {
			newHolding.setId(holdingId);
			newHolding.setHoldingTypeCode(extHolding.getHoldingTypeCode());
		}
		return newHolding;
	}
}
