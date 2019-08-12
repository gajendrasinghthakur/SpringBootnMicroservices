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

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;

/**
 * This class is responsible for creating request for contract number generation webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.34</td><td>Version 7</td><td>ContractService Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSContractNumberInvoker extends AxaWSInvokerBase {

    private static final String CATEGORY = "Contract";

    private static final String FUNCTIONID = "ContractServices";

    
    /**
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
    public AxaWSContractNumberInvoker(String operation,NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation,userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID);
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);
    }

    /* (non-Javadoc)
     * @see com.csc.fsg.nba.webservice.invoke.AxaWSInvokerBase#createRequest()
     */
    /**
     * Create request for contract number generate webservice
     */
    public NbaTXLife createRequest() throws NbaBaseException {
    	NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_CONTRACT_SERVICE );
		nbaTXRequest.setTransSubType(NbaOliConstants.TC_SUBTYPE_GET_CONTRACT_NUMBER );
        	nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setNbaUser(getUserVO());
		nbaTXRequest.setNbaLob(getNbaDst() != null ? getNbaDst().getNbaLob() : null);
		NbaTXLife nbaReqTXLife = new NbaTXLife(nbaTXRequest);
		TXLife reqTXLife = nbaReqTXLife.getTXLife();
		TXLifeRequest aNbaTXLifeRequest = reqTXLife.getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
		aNbaTXLifeRequest.setChangeSubType(new ArrayList());
		OLifE olife = nbaReqTXLife.getOLifE();
		if (null == olife) {
			olife = new OLifE();
		}
		Holding aHolding = nbaReqTXLife.getPrimaryHolding();
		if (null == nbaReqTXLife.getPrimaryHolding()){
			aHolding = new Holding();
			olife.addHolding(aHolding);
		}
		Policy aPolicy = nbaReqTXLife.getPolicy();
		if (null == nbaReqTXLife.getPolicy()){
			aPolicy = new Policy();
			aHolding.setPolicy(aPolicy);
		}
		Party  aParty = new Party();
		Relation aRelation = new Relation();
		PersonOrOrganization aPersonOrOrg = new PersonOrOrganization(); 
		ApplicationInfo aappinfo = new ApplicationInfo();
		aHolding.setId(HOLDING_ID);
		aParty.setId(PARTY_ID);
		aRelation.setId(RELATION_ID);
		aPolicy.setLineOfBusiness(getNbaTXLife().getPrimaryHolding().getPolicy().getLineOfBusiness());
		aPolicy.setProductType(getNbaTXLife().getPrimaryHolding().getPolicy().getProductType());
		String tmpProductCode = getNbaTXLife().getPrimaryHolding().getPolicy().getProductCode();
		String productCode = null;
		if (ULBASE.equals(tmpProductCode)) {
			productCode = SERIES;
		} else {
			productCode = tmpProductCode;
		}
		aPolicy.setProductCode(productCode);
		aPolicy.setCarrierCode(getNbaTXLife().getPrimaryHolding().getPolicy().getCarrierCode());
		aappinfo.setSignedDate(getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo().getSignedDate());
		aappinfo.setFormalAppInd(getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo().getFormalAppInd());
		//TODO uncommment when AXA is ready
		//OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY); 
		PolicyExtension policyExt = getNbaTXLife().getPrimaryHolding().getPolicy().getOLifEExtensionAt(0).getPolicyExtension();
		//olifeExt.setPolicyExtension(policyExt);
		//aPolicy.addOLifEExtension(olifeExt);
		aPolicy.setApplicationInfo(aappinfo);
		
		aRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_PRIMAGENT);
		if (getNbaTXLife().getWritingAgent() != null) {
			aParty.setProducer(getNbaTXLife().getWritingAgent().getParty().getProducer());
		}
		aPersonOrOrg.setPerson(new Person()); //required, even if empty 
		aParty.setPersonOrOrganization(aPersonOrOrg);
		//TODO remove when AXA is ready with DistributionChannel
		Producer prod;
		if (aParty.hasProducer()) {
			prod = aParty.getProducer();
		} else {
			prod = new Producer();
		}
		long carrierApptCode;
		if (null != policyExt && policyExt.getDistributionChannel() == 6) { //wholesale
			carrierApptCode = 2;
		} else {
			carrierApptCode = 1;
		}
		
		if (prod.getCarrierAppointmentCount() > 0) {
			prod.getCarrierAppointmentAt(0).setCarrierApptTypeCode(carrierApptCode);
		} else  {
		CarrierAppointment ca = new CarrierAppointment();
		ca.setId(CARRIER_APPT_ID);
		ca.setPartyID(aParty.getId());
			ca.setCarrierApptTypeCode(carrierApptCode);
			prod.addCarrierAppointment(ca);
		}

		aParty.setProducer(prod);
		//end TODO
		aRelation.setOriginatingObjectID(aHolding.getId());
		aRelation.setRelatedObjectID(aParty.getId());
		olife.addParty(aParty);
		olife.addRelation(aRelation);
		aNbaTXLifeRequest.setOLifE(olife);		
		 if (getLogger().isDebugEnabled()) {
	            getLogger().logDebug("request for Contract Service 1009800001=" + nbaReqTXLife.toXmlString());
	        }
		return nbaReqTXLife;
    }


}
