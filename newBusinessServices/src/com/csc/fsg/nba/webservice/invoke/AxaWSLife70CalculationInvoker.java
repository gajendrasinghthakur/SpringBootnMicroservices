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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaTime;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.UnderwritingAnalysis;

/**
 * This class is responsible for creating request for Life 70 Calculation webservice.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL016CV</td><td>AXA Life Phase 2</td><td>Product Val - Life 70 Calculations</td></tr>
 * <tr><td>ALII2055-4</td><td>AXA Life Phase 2</td><td>Performance - L7 call improvement</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSLife70CalculationInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "L70Calculation";
	private static final String FUNCTIONID = "performCalculation";

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSLife70CalculationInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}
	
    /**
     * This method is used for creating generic 1203 request
     * @return nbaReqTXLife
     */
    public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		Long subType = (Long) getObject();
		nbaTXRequest.setTransSubType(subType.longValue());
		nbaTXRequest.setNbaUser(getUserVO());
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		nbaReqTXLife.setOLifE(getNbaTXLife().getOLifE().clone(false)); 
		
		//Need to remove the deleted objects (where ActionIndicator is Delete or Delete Successful)
		//ALII2055-4 code deleted - duplicate call
		
		nbaReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeDate(new Date());
		nbaReqTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).setTransExeTime(new NbaTime());
		return nbaReqTXLife;
    }
    
    /**
     * Removed ignored or un-wanted objects from txlife. 
     * @param txlife the request 
     * @return the modified request with ignored objects removed.
     * @throws NbaBaseException
     */
    protected void removeDeletedObjects(NbaTXLife txlife) throws NbaBaseException {
		//remove deleted objects
		try {
			txlife = new NbaTXLife(txlife.toXmlString()); //toXMLString will remove deleted objects
		} catch (Exception e) {
			throw new NbaBaseException("Unable to clone calculation request", e);
		}
	}
    
    /**
	 * This method is responsible for cleaning up the generic 1203 request created.
	 * @return void
	 */
    public void cleanRequest() throws NbaBaseException {  //APSL371 APSL372
		super.cleanRequest();
		removeInvalidCoverages(); 
		removeInvalidCovOptions();
		
		getNbaTxLifeRequest().getPrimaryHolding().setAttachment(new ArrayList());
		getNbaTxLifeRequest().getPrimaryHolding().setSystemMessage(new ArrayList());

		getNbaTxLifeRequest().getPolicy().setRequirementInfo(new ArrayList()); //ALII2041
		//ALII2041 deleted code.
		//ALII2041 starts
		Iterator relationItr = getNbaTxLifeRequest().getOLifE().getRelation().iterator();
		//To remove the relations related to Requirements. Since we are removing the requirements above.
		while (relationItr.hasNext()) {
			Relation relation = (Relation) relationItr.next();
			if (NbaOliConstants.OLI_REL_PHYSICIAN == relation.getRelationRoleCode() ||
					NbaOliConstants.OLI_REL_MEDPROVIDER == relation.getRelationRoleCode()) { 
				relationItr.remove();
			}
		}
		//ALII2041 ends
		getNbaTxLifeRequest().getPolicy().setEndorsement(new ArrayList());
		// APSL4304 begin
		
		Iterator formInstanceItr = getNbaTxLifeRequest().getOLifE().getFormInstance().iterator();
		while (formInstanceItr.hasNext()) {
			FormInstance formInstance = (FormInstance) formInstanceItr.next();
			// Begin NBLXA-1554[NBLXA-1749] removes the relations related to formInstance			
			if (formInstance != null && !NbaConstants.FORM_NAME_LTCSUPP.equalsIgnoreCase(formInstance.getFormName())) {
				Iterator<Relation> relation = getNbaTxLifeRequest().getOLifE().getRelation().iterator();
				while (relation.hasNext()) {
					Relation rel = relation.next();
					if (formInstance.getId().equalsIgnoreCase(rel.getRelatedObjectID())
							|| formInstance.getId().equalsIgnoreCase(rel.getOriginatingObjectID())) {
						relation.remove();
					}
				}
				formInstanceItr.remove();
			}			
			// End NBLXA-1554[NBLXA-1749]
		}
		// APSL4304 end
		clearParties();
    }
    
    private void clearParties() {
		OLifE oLifE = getNbaTxLifeRequest().getOLifE();
		int partyCount = oLifE.getPartyCount();
		Party party;
		PartyExtension partyExtension;
		PersonExtension personExtension;
		for (int i = 0; i < partyCount; i++) {
			party = oLifE.getPartyAt(i);
			party.setAttachment(new ArrayList());
			party.setEmployment(new ArrayList());
			//party.setRisk(new Risk()); ALII678 removed line
			partyExtension = NbaUtils.getFirstPartyExtension(party);
			if (partyExtension != null) {
				partyExtension.setUnderwritingAnalysis(new UnderwritingAnalysis());
			}
			if (party.hasPersonOrOrganization()) {
				if (party.getPersonOrOrganization().isOrganization()) {
					party.getPersonOrOrganization().getOrganization().setOrganizationFinancialDataGhost(new ArrayList());
				} else if (party.getPersonOrOrganization().isPerson()) {
					personExtension = NbaUtils.getFirstPersonExtension(party.getPersonOrOrganization().getPerson());
					if (personExtension != null) {
						personExtension.setImpairmentInfo(new ArrayList());
					}
				}
			}
		}
    }
	
    /**
	 * Removes invalid coverages
	 */
    protected void removeInvalidCoverages() {
		List coverages = getNbaTxLifeRequest().getLife().getCoverage();
		Iterator iterator = coverages.iterator();
		while (iterator.hasNext()) {
			Coverage coverage = (Coverage) iterator.next();
			long coverageStatus = coverage.getLifeCovStatus();
			if (coverageStatus == NbaOliConstants.OLI_POLSTAT_TERMINATE || coverageStatus == NbaOliConstants.OLI_POLSTAT_DECISSUE
					|| coverageStatus == NbaOliConstants.OLI_POLSTAT_INVALID) {
				//Begin ALNA569 
				if (coverage.getLifeCovTypeCode() == NbaOliConstants.OLI_COVTYPE_CHILDTERM) {
					removeChildParty(getNbaTxLifeRequest().getPartyId(NbaOliConstants.OLI_REL_DEPENDENT));
				}//End ALNA569 
				removeRelationsForCoverage(coverage);
				iterator.remove();
			}
		}
	}
    
    /**
	 * Remove invalid relations
	 */
    protected void removeRelationsForCoverage(Coverage coverage) {
    	List relations = getNbaTxLifeRequest().getOLifE().getRelation();
    	Iterator iterator = relations.iterator();
    	while (iterator.hasNext()) {
    		Relation relation = (Relation) iterator.next();
    		if(relation.getOriginatingObjectID().equals(coverage.getId())) {
    			iterator.remove();
    		}
    	}
    }
       
    /**
	 * Remove invalid cov options
	 */
    protected void removeInvalidCovOptions() {
		Life life = getNbaTxLifeRequest().getLife();
		int count = life.getCoverageCount();
		for (int i = 0; i < count; i++) {
			removeInvalidCovOptions(life.getCoverageAt(i).getCovOption());
		}
	}
    
    /**
	 * Remove invalid cov options
	 * @param covOptions the cov options list
	 */
    protected void removeInvalidCovOptions(List covOptions) {
		Iterator iterator = covOptions.iterator();
		CovOption covOption = null;
		while (iterator.hasNext()) {
			covOption = (CovOption) iterator.next();
			long covOptionStatus = covOption.getCovOptionStatus();
			if (covOptionStatus == NbaOliConstants.OLI_POLSTAT_TERMINATE || covOptionStatus == NbaOliConstants.OLI_POLSTAT_DECISSUE
					|| covOptionStatus == NbaOliConstants.OLI_POLSTAT_INVALID) {
				iterator.remove();
			}
		}
	}

	/**
	 * Remove party and relation for the partyid
	 * @param partyid 
	 */
    //ALNA569 New Method
    protected void removeChildParty(String partyid) {
		List relationList = getNbaTxLifeRequest().getOLifE().getRelation();
		List partyList = getNbaTxLifeRequest().getOLifE().getParty();
		Iterator iterator = relationList.iterator();
		while (iterator.hasNext()) {
			Relation relation = (Relation) iterator.next();
			if (relation.getRelatedObjectID().equals(partyid)) {
				iterator.remove();
			}
		}
		iterator = partyList.iterator();
		while (iterator.hasNext()) {
			Party party = (Party) iterator.next();
			if (party.getId().equals(partyid)) {
				iterator.remove();
			}
		}
	}

    /**
	 * Override method for handling validation resopnse as its handeled by calling class
	 * 
	 * @param nbaTXLife
	 * @throws NbaBaseException
	 */
	protected void handleResponse() throws NbaBaseException {
    	//TODO - Following error conditions should be handled at the TRIGGER
		// Need to Verify how to handle following Webservice failure conditions :
		// 1. When the calculations are triggered as part of CV - It can be called from view 
		//    commits or autoprocess commits. How the CV errors be handled?
		// 2. Print calculations triggered from Contract Print automated process. My guess is 
		//    we should error stop the process as 500 without the print calc values would make no sense.
		// So, whether to throw exception or not (in this method) would entirely depend on what 
		// calculations are performed (CV or Print)?
	}
}
