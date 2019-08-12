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
import java.util.HashSet;
import java.util.Set;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.SourceInfo;

/**
 * This class is responsible for creating request for Zip Code validate webservice.
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>P2AXAL038</td><td>AXA Life Phase 2</td><td>Zip Code Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class AxaWSValidateZipCodeInvoker extends AxaWSInvokerBase {
	
	private static final String CATEGORY = "ValidateZipCode";
	private static final String FUNCTIONID = "isZipCodeValid";

	
	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */
	
	public AxaWSValidateZipCodeInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);
		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);
	}
	
	/**
	 * This method first calls the superclass createRequest() and then set the request specefic attribute.
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {
		NbaTXLife nbaReqTXLife = createRequestSkeleton();
		ArrayList partyList = new ArrayList();
		ArrayList relationList = new ArrayList();
		OLifE oLife = getNbaTXLife().getOLifE();
		Relation relation = null;
		Set partyIdSet = new HashSet();
		for (int index = 0; index < oLife.getRelationCount(); index++) {
			relation = (Relation) oLife.getRelationAt(index).clone();
			if (!NbaConstants.PRIMARY_HOLDING_ID.equalsIgnoreCase(relation.getOriginatingObjectID())) {
				relation.setOriginatingObjectID(NbaConstants.PRIMARY_HOLDING_ID);
			}
			if (isIncludedZipRelation(relation.getRelationRoleCode())) {
				Party party = getNbaTXLife().getParty(relation.getRelatedObjectID()).getParty();
				ArrayList addressList = null;
				if (party != null && !party.isDeleted()) {
					if (partyIdSet.contains(party.getId())) {//If Party already added
						relationList.add(relation);
					} else {//If Party not already added
						addressList = new ArrayList();
						Address address = null;
						for (int i = 0; i < party.getAddress().size(); i++) {
							address = (Address) party.getAddress().get(i);
							if (!address.isDeleted()
									&& (NbaOliConstants.OLI_NATION_USA == address.getAddressCountryTC() || NbaUtils.isBlankOrNull(address
											.getAddressCountryTC()))) {
								addressList.add(party.getAddressAt(i));
							}
						}
						if (addressList.size() > 0) {//If atlest one address is present to validate
							Party newParty = new Party();
							newParty.setId(party.getId());
							newParty.setPartyTypeCode(party.getPartyTypeCode());
							newParty.setPersonOrOrganization(party.getPersonOrOrganization());
							newParty.setAddress(addressList);
							partyList.add(newParty);
							partyIdSet.add(newParty.getId());
							relationList.add(relation);
						}
					}
				}
			}
		}
		Holding holding = new Holding();
		holding.setId(getNbaTXLife().getPrimaryHolding().getId());
		Policy policy = new Policy();
		policy.setId(getNbaTXLife().getPrimaryHolding().getPolicy().getId());
		policy.setPolNumber(getNbaTXLife().getPrimaryHolding().getPolicy().getPolNumber());
		holding.setPolicy(policy);
		ArrayList holdinglist = new ArrayList();
		holdinglist.add(holding);
		nbaReqTXLife.getOLifE().setHolding(holdinglist);
		nbaReqTXLife.getOLifE().setParty(partyList);
		nbaReqTXLife.getOLifE().setRelation(relationList);
		return nbaReqTXLife;
	}	
	
    /**
     * Validate txLife request and return true 
     * There are party, and addresses to validate then true
     * @return
     * @throws NbaBaseException
     */
	//ALII677
    protected boolean validate() throws NbaBaseException {
		return getNbaTxLifeRequest() != null && getNbaTxLifeRequest().getOLifE().getPartyCount() > 0;
	}
    
	protected boolean isCallNeeded(){
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ZIP_INTERFACE_CALL_SWITCH);
	}
	 
	private NbaTXLife createRequestSkeleton(){
		NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_PARTYINQ);
		nbaTXRequest.setTransSubType(NbaOliConstants.OLI_TRANSSUBTYPE_ZIPCODEVALIDATION);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaUser(getUserVO());
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		OLifE newoLife = nbaReqTXLife.getOLifE();
		SourceInfo sourceInfo = new SourceInfo();
		sourceInfo.setFileControlID(getBackEnd());
		sourceInfo.setSourceInfoName("nbA");
		newoLife.setSourceInfo(sourceInfo);
		return nbaReqTXLife;
	}

	protected boolean isIncludedZipRelation(long relDesc) {
		int length = NbaConstants.INCLUDED_ZIP_REL.length;
		for (int i = 0; i < length; i++) {
			if (relDesc == NbaConstants.INCLUDED_ZIP_REL[i]) {
				return true;
			}
		}
		return false;
	}
}
