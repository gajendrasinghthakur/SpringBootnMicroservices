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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
package com.csc.fsg.nba.webservice.invoke;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ChangeSubType;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SignatureInfo;
import com.csc.fsg.nba.vo.txlife.SourceInfo;

/**
 * This class is responsible for creating request for Compensation ECS webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.22</td>
 * <td>AXA Life Phase 1</td>
 * <tr><td>SR494086.6</td><td>ADC</td><td>Interfaces</td></tr>
 * <tr><td>CR57907</td><td></td><td>Xpress Commission Transaction</td></tr>
 * <td>Compensation Interface</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSECSUpdateInvoker extends AxaWS502Invoker {

	private static final String CATEGORY = "COMPENSATIONUPDATE";

	private static final String FUNCTIONID = "UpdateExpressCompensation";

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSECSUpdateInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}
	
	/**
     * This method is used for creating generic 1203 request
     * @return nbaReqTXLife
     */
	//AXAL3.7.22 method overridden
    public NbaTXLife createRequest() throws NbaBaseException {
    	NbaTXLife nba502ReqTXLife = createTXLifeRequest(NbaOliConstants.TC_TYPE_RATECLASSCHANGE, NbaOliConstants.TC_SUBTYPE_HOLDING_CHANGE_TXN,
				getUserVO().getUserID());
		addChangeSubType((ArrayList)((Map) getObject()).get("changeSubTypeList"), nba502ReqTXLife);
		OLifE newOlifE = getNbaTXLife().getOLifE().clone(false);
    	nba502ReqTXLife.setOLifE(newOlifE);
    	addSourceInfoObject(nba502ReqTXLife);
    	addDeletedObjectsInTrans((ArrayList)((Map) getObject()).get("changeSubTypeList"), nba502ReqTXLife);
		return nba502ReqTXLife;
    }
    
    /**
	 * @param list
	 * @param nba502ReqTXLife
	 * @param nbaTXLife
	 */
    //AXAL3.7.22 method overridden
	public void addSourceInfoObject(NbaTXLife nba502ReqTXLife) {
		OLifE olife = nba502ReqTXLife.getOLifE();
		SourceInfo sourceInfo = null;
		if (getNbaTXLife().getOLifE().getSourceInfo() != null) {
			sourceInfo = getNbaTXLife().getOLifE().getSourceInfo().clone(false);
		} else {//ALS3337 code refactored.
			sourceInfo = new SourceInfo();
			sourceInfo.setSourceInfoName("nbA_Life");
			sourceInfo.setFileControlID(getBackEnd()); //P2AXAL008
		}
		if ((String) ((Map) getObject()).get("transRefGUID") != null) {
			sourceInfo.setSourceInfoComment("one of a pair");
			nba502ReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransRefGUID(
					(String) ((Map) getObject()).get("transRefGUID"));
		}
		olife.setSourceInfo(sourceInfo);
	}
	
	//AXAL3.7.22 new method to remove the PWA party and relations from the Tx 502 request other than that have changed.
	public void cleanRequest() throws NbaBaseException {  //APSL371 APSL372
		super.cleanRequest();
		removeOtherPWAParties();//ALS3337,ALS3591. Tx500 should contain only the deleted Primary Writing agent and not the newly added one. So
								// removing the newly added one from 502
	}
	
	/**
	 * The method removes PWAs on the case other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	protected void removeOtherPWAParties() {
		String changedPartyId = null;
		Iterator changeSubTypeIterator = ((ArrayList) ((Map) getObject()).get("changeSubTypeList")).iterator();
		while (changeSubTypeIterator.hasNext()) {
			ChangeSubType changeSubType = (ChangeSubType) changeSubTypeIterator.next();
			if (NbaOliConstants.OLI_CHG_PARTYINFO == changeSubType.getChangeTC()) {
				changedPartyId = changeSubType.getChangeID();
			}
		}
		if (changedPartyId != null) {
			removeOtherPWAParties(changedPartyId);
		}
	}
	
	/**
	 * @param partyIdToBeRemoved
	 * The method removes PWAs on the case other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	protected void removeOtherPWAParties(String changedPartyId) {
		String partyIdToBeRemoved = null;
		Iterator relationsItr = getNbaTxLifeRequest().getOLifE().getRelation().iterator();
		while (relationsItr.hasNext()) {
			Relation relation = (Relation) relationsItr.next();
			if (NbaOliConstants.OLI_REL_PRIMAGENT == relation.getRelationRoleCode()) {
				if (changedPartyId != null && !changedPartyId.equalsIgnoreCase(relation.getRelatedObjectID())) {
					partyIdToBeRemoved = relation.getRelatedObjectID();
					removePartyRelatedObjects(partyIdToBeRemoved);
				}
			}
		}
	}
	
	/**
	 * @param partyIdToBeRemoved
	 * The method removes PWAs on the case other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	protected void removePartyRelatedObjects(String partyIdToBeRemoved) {
		removePartyHoldings(partyIdToBeRemoved);
		removeRelations(partyIdToBeRemoved);
		removeCoverages(partyIdToBeRemoved);
		removeSignatureInfo(partyIdToBeRemoved);
		removeFormInstanceSignatureInfo(partyIdToBeRemoved);
		removePartyFromTxLife(partyIdToBeRemoved);
	}
	
	/**
	 * @param partyId
	 * The method removes Party Holdings for PWA on the case, other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	public void removePartyHoldings(String partyId) {
		if ( partyId == null ) return ;
		List relations = getNbaTxLifeRequest().getOLifE().getRelation();
		Relation relation = null;
		String primaryHoldingId = getNbaTxLifeRequest().getPrimaryHolding().getId();
		if (relations != null) {
			Iterator relItr = relations.iterator();
			while (relItr.hasNext()) {
				relation = (Relation) relItr.next();
				if (partyId.equalsIgnoreCase(relation.getRelatedObjectID()) && relation.getOriginatingObjectType() == NbaOliConstants.OLI_HOLDING) {
					if (!primaryHoldingId.equalsIgnoreCase(relation.getOriginatingObjectID())) {
						removeHolding(relation.getOriginatingObjectID());
					}
				}
			}
		}
	}
	
	/**
	 * @param partyId
	 * The method removes Holdings for PWA on the case, other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	public void removeHolding(String holdingId) {
		removeRelations(holdingId);
		Holding tempHolding = NbaTXLife.getHoldingFromId(holdingId, getNbaTxLifeRequest().getOLifE().getHolding());
		if (tempHolding != null) {
			getNbaTxLifeRequest().getOLifE().getHolding().remove(tempHolding);
		}
	}
	
	/**
	 * @param partyId
	 * The method removes Relations for PWA on the case, other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	public void removeRelations(String objectId) {
		if ( objectId == null ) return ;
		List relations = getNbaTxLifeRequest().getOLifE().getRelation();
		Relation relation = null;
		if (relations != null) {
			Iterator relItr = relations.iterator();
			while (relItr.hasNext()) {
				relation = (Relation) relItr.next();
				if (objectId.equalsIgnoreCase(relation.getOriginatingObjectID()) || objectId.equalsIgnoreCase(relation.getRelatedObjectID())) {
					relItr.remove();
				}
			}
		}
	}
	
	/**
	 * @param partyId
	 * The method removes Coverages for PWA on the case, other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	public void removeCoverages(String partyId) {
		if (partyId == null)
			return;
		List coverages = getNbaTxLifeRequest().getCoveragesFor(partyId);
		int coverageCount = (coverages == null) ? 0 : coverages.size();
		Coverage coverage = null;
		for (int i = 0; i < coverageCount; i++) {
			coverage = (Coverage) coverages.get(i);
			Iterator lifePartItr = coverage.getLifeParticipant().iterator();
			while (lifePartItr.hasNext()) {
				LifeParticipant lifePar = (LifeParticipant) lifePartItr.next();
				if (lifePar != null && partyId.equalsIgnoreCase(lifePar.getPartyID())) {
					lifePartItr.remove();
				}
			}
		}
	}
	
	/**
	 * @param partyId
	 * The method removes SignatureInfos for PWA on the case, other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	public void removeSignatureInfo(String partyId) {
		List signs = getNbaTxLifeRequest().getPolicy().getApplicationInfo().getSignatureInfo();
		if (partyId != null && signs != null & signs.size() > 0) {
			Iterator signsIt = signs.iterator();
			while (signsIt.hasNext()) {
				SignatureInfo signInfo = (SignatureInfo) signsIt.next();
				if (partyId.equals(signInfo.getSignaturePartyID())) {
					signsIt.remove();
				}
			}
		}
	}
	
	/**
	 * @param partyId
	 * The method removes Form Instance Signature Infos for PWA on the case, other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	public void removeFormInstanceSignatureInfo(String partyId) {
		ArrayList formInstanceList = getNbaTxLifeRequest().getOLifE().getFormInstance();
		ArrayList signatureInfoList = null;
		SignatureInfo signInfo = null;
		Iterator itr = formInstanceList.iterator();
		FormInstance frmInstance = null;
		while (itr.hasNext()) {
			frmInstance = (FormInstance) itr.next();
			signatureInfoList = frmInstance.getSignatureInfo();
			Iterator itrSignInfo = signatureInfoList.iterator();
			while (itrSignInfo.hasNext()){
				signInfo = (SignatureInfo) itrSignInfo.next();
				if ( partyId != null && partyId.equals(signInfo.getSignaturePartyID())){
					itrSignInfo.remove();
				}
			}
		}	
	}
	
	/**
	 * @param partyId
	 * The method removes Party for PWA on the case, other than that have changed.
	 */
	//ALS3337,ALS3591 new method added.
	public void removePartyFromTxLife(String partyId) {
		NbaParty nbaParty = getNbaTxLifeRequest().getParty(partyId);
		if (nbaParty != null) {
			Party party = nbaParty.getParty();
			getNbaTxLifeRequest().getOLifE().getParty().remove(party);
		}
	}

	//AXAL3.7.22 overridden the method to stop compensation interface call for informal Application.
	public boolean isCallNeeded() {
		//Begin CR57907 Retrofit
		boolean ecsSupress = false;
		NbaUserVO user = getUserVO();
		String buisnessProcess=getNbaTXLife().getBusinessProcess();
		if (null != buisnessProcess
				&& (buisnessProcess.equalsIgnoreCase(NbaConstants.PROC_UW_APPROVE_CONTRACT)
						|| buisnessProcess.equalsIgnoreCase(NbaConstants.PROC_UW_DISPOSITION) || NbaConstants.PROC_AUTO_UNDERWRITING
						.equalsIgnoreCase(buisnessProcess))) {//QC#8323 APSL1900
			ecsSupress = true;
		}
		//End CR57907
		if (getNbaTXLife().isInformalApplication() || getNbaTXLife().isPaidReIssue() || NbaUtils.isAdcApplication(getNbaDst()) || ecsSupress) {//ALS4493, APSL459, SR494086
			return false;
		}
		return true;
	}
}
