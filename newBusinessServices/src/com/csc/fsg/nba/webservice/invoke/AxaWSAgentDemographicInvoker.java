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

import com.axa.fsg.nba.vo.AxaProducerVO;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.DistributionChannelInfo;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;

/**
 * This class is responsible for creating request for agent demographic webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.18</td><td>Version 7</td><td>Producer Interface</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class AxaWSAgentDemographicInvoker extends AxaWSInvokerBase {

    private static final String CATEGORY = "AxaProducerInfo";

    private static final String FUNCTIONID = "GetDistributorInfo";
    
    /**
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
    public AxaWSAgentDemographicInvoker(String operation,NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation,userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID);
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);

    }

    /* (non-Javadoc)
     * @see com.csc.fsg.nba.webservice.invoke.AxaWSInvokerBase#createRequest()
     */
    public NbaTXLife createRequest() throws NbaBaseException {
        AxaProducerVO producerVO = (AxaProducerVO) getObject();
        NbaTXLife nbaTXLife = createTXLifeRequest(producerVO.getTransType(),producerVO.getTransSubType(), getUserVO().getUserID());
        OLifE olifE = new OLifE();
        olifE.setVersion("2.9.03");
        olifE.setSourceInfo(createSoureInfo());

        Holding holding = new Holding();
        holding.setId("Holding_1");
        holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);

        Policy policy = new Policy();
        policy.setId("Policy_1");
        policy.setLineOfBusiness(producerVO.getLineOfBusiness());
        policy.setProductCode(producerVO.getProductCode());
        policy.setCarrierCode(producerVO.getCarrierCode());
        holding.setPolicy(policy);
        OLifEExtension olifeExt =  NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
        PolicyExtension extension = new PolicyExtension();
        extension.setDistributionChannel(producerVO.getDistributionChannel());
        olifeExt.setPolicyExtension(extension);
        policy.addOLifEExtension(olifeExt);
        
        Party party = new Party();
        party.setId("Party_1");
        party.setPartyTypeCode(producerVO.getPartyTypeCode());
                    
        if(String.valueOf(NbaOliConstants.OLI_PT_PERSON).equals(producerVO.getPartyTypeCode())){
            Person person = new Person();
            PersonOrOrganization personOrOrganization = new PersonOrOrganization();
            personOrOrganization.setPerson(person);
            party.setPersonOrOrganization(personOrOrganization);
        } else {
            Organization organization= new Organization();
            PersonOrOrganization personOrOrganization = new PersonOrOrganization();
            personOrOrganization.setOrganization(organization);
            party.setPersonOrOrganization(personOrOrganization);
            
        }   
        
        Producer producer = new Producer();
        CarrierAppointment appointment = new CarrierAppointment();
        appointment.setId("CarrierAppointment_1");
        appointment.setPartyID(party.getId());
        appointment.setCarrierAppointmentSysKey((ArrayList)producerVO.getCarriearAptSysKey());
        appointment.setCompanyProducerID(producerVO.getCompanyProducerID());
        appointment.setCarrierCode(producerVO.getProducerCarrierCode());
        appointment.setCarrierApptTypeCode(producerVO.getCarrierApptTypeCode());
        
        DistributionChannelInfo channelInfo = new DistributionChannelInfo();
        channelInfo.setId("DistributionChannelInfo_1");
        channelInfo.setDistributionChannel(producerVO.getDistributionChannel());

        appointment.addDistributionChannelInfo(channelInfo);
        producer.addCarrierAppointment(appointment);
        party.setProducer(producer);
        
        
        Relation agentRelation = new Relation();
        agentRelation.setId("Relation_1" );
        agentRelation.setOriginatingObjectID("Holding_1");
        agentRelation.setRelatedObjectID("Party_1");
        agentRelation.setRelationRoleCode(producerVO.getRelationRoleCode());
        
        olifE.addHolding(holding);
        olifE.addParty(party);
        olifE.addRelation(agentRelation);
        if(producerVO.getBgaCaseManagerParty()!=null){
        	olifE.addParty(producerVO.getBgaCaseManagerParty());
        	Relation bgaCMRelation = producerVO.getBgaCaseManagerRelation().clone(true); //ALS5629
        	bgaCMRelation.setId("Relation_2");
        	bgaCMRelation.setOriginatingObjectID(party.getId());
        	olifE.addRelation(bgaCMRelation);
        }
        
        nbaTXLife.setOLifE(olifE);
        return nbaTXLife;
    }
    
    // NBLXA-2553 Override method for modifying webservice response for Charles Bailey Agent. Reverted from July 2019 release.
//  public void handleResponse() throws NbaBaseException {
//  	handleAgentWebserviceResponse();
//  }    
  
}
