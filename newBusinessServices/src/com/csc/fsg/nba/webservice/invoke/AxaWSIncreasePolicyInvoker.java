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

import java.util.HashSet;
import java.util.Set;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

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
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSIncreasePolicyInvoker extends AxaWSInvokerBase {
	private static final String CATEGORY = "CAPSInforceSubmit";

	private static final String FUNCTIONID = "submitPolicy";
	
	private String changeType;

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	public AxaWSIncreasePolicyInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
		if(getObject() != null) {
			setChangeType((String)getObject());
		}
	}

	public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_NEWBUSSUBMISSION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUserVO())); //SPR2639
		nbaTXRequest.setNbaLob(getNbaDst().getNbaLob());
		nbaTXRequest.setChangeSubType(NbaOliConstants.NBA_CHNGTYPE_INCREASE);
		return createIncreaseTransaction(nbaTXRequest);
	}

	/**
	 * Create an Increase (103 with changeSubType 1000500028)transaction from holding inquiry
	 * and workitem's LOBs to process a increase request on admin system. 
	 * @return the NbaTXLife for the Increase transaction
	 * @exception throws NbaBaseException when an Increase rider can not be located
	 */
	protected NbaTXLife createIncreaseTransaction(NbaTXRequestVO nbaTXRequest) throws NbaBaseException {
		//create txlife with default request fields
		NbaTXLife txLifeIncrease = new NbaTXLife(nbaTXRequest);
		OLifE olifeIncrease = txLifeIncrease.getOLifE();
		Policy policyIncrease = txLifeIncrease.getPolicy();
		OLifE nbaOlife = getNbaTXLife().getOLifE();
		Policy nbaPolicy = getNbaTXLife().getPolicy();
		policyIncrease.setCarrierAdminSystem(getNbaDst().getNbaLob().getBackendSystem());
		policyIncrease.setReinsuranceInd(nbaPolicy.getReinsuranceInd());
		if (!policyIncrease.hasApplicationInfo()) {
			policyIncrease.setApplicationInfo(new ApplicationInfo());
		}
		policyIncrease.getApplicationInfo().setUserCode(nbaPolicy.getApplicationInfo().getUserCode());
		Set partyIds = new HashSet();
		if (getNbaTXLife().isLife()) {
			Life nbaLife = getNbaTXLife().getLife();
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
		if (getLogger().isDebugEnabled()) {
			getLogger().logDebug("Increase 103 Transaction " + txLifeIncrease.toXmlString());
		}

		return txLifeIncrease;
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
	 * @return Returns the changeType.
	 */
	public String getChangeType() {
		return changeType;
	}
	/**
	 * @param changeType The changeType to set.
	 */
	public void setChangeType(String changeType) {
		this.changeType = changeType;
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
}
