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
import java.util.List;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.CarrierAppointmentExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.FormResponseExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Producer;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationProducerExtension;

/**
 * This class is responsible for creating request for agent validaiton webservice .
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
public class AxaWSAgentValidationInvoker extends AxaWSInvokerBase {

    private static final String CATEGORY = "AxaProducerInfo";

    private static final String FUNCTIONID = "ProducerLicenseStatus";

    /**
     * constructor from superclass
     * @param userVO
     * @param nbaTXLife
     */
    public AxaWSAgentValidationInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super(operation,userVO, nbaTXLife, nbaDst, object);
        setBackEnd(ADMIN_ID);
        setCategory(CATEGORY);
        setFunctionId(FUNCTIONID);

    }

    /**
     * Create webservice request for Agent validation
     */
    public NbaTXLife createRequest() throws NbaBaseException {

        NbaTXLife nbaTXLife = createTXLifeRequest(NbaOliConstants.OLI_TRANSTYPE_AGENTVALIDAION, NbaOliConstants.OLI_TRANSSUBTYPE_AGENTVALIDATION,
                getUserVO().getUserID());
        OLifE olifE = new OLifE();
        olifE.setVersion("2.9.03");
        olifE.setSourceInfo(createSoureInfo());

        Holding holding = new Holding();
        holding.setId("Holding_1");
        holding.setHoldingTypeCode(NbaOliConstants.OLI_HOLDTYPE_POLICY);

        Policy oldPolicy = getNbaTXLife().getPolicy();
        Policy policy = new Policy();
        policy.setId("Policy_1");

        policy.setLineOfBusiness(oldPolicy.getLineOfBusiness());
        policy.setProductCode(oldPolicy.getProductCode());
        policy.setPlanName(oldPolicy.getPlanName());
        policy.setCarrierCode(oldPolicy.getCarrierCode());
        
       
        /*APSL3393 :: Start code for including base coverage and covoption in the Request*/
        
        policy.setLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty(new LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty());
        policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().setLife(new Life());
        Life life  = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
        Life oldLife = oldPolicy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife();
        life.setInitialPremAmt(oldLife.getInitialPremAmt());
        life.setFaceAmt(oldLife.getFaceAmt());
        life.setFaceUnits(oldLife.getFaceUnits());
        for (int i = 0; i < oldLife.getCoverageCount(); i++) {
            Coverage oldCoverage = oldLife.getCoverageAt(i);
            if (oldCoverage.getIndicatorCode() == NbaOliConstants.OLI_COVIND_BASE) {
                Coverage coverage = new Coverage();
                coverage.setId("Coverage_1");
                coverage.setCoverageKey(oldCoverage.getCoverageKey());
                coverage.setPlanName(oldCoverage.getPlanName());
                coverage.setProductCode(oldCoverage.getProductCode());
                coverage.setLifeCovStatus(oldCoverage.getLifeCovStatus());
                coverage.setLifeCovTypeCode(oldCoverage.getLifeCovTypeCode());
                coverage.setIndicatorCode(oldCoverage.getIndicatorCode());
                coverage.setLivesType(oldCoverage.getLivesType());
                coverage.setDeathBenefitOptType(oldCoverage.getDeathBenefitOptType());
                coverage.setCurrentAmt(oldCoverage.getCurrentAmt());
                coverage.setInitCovAmt(oldCoverage.getInitCovAmt());
                coverage.setIntialNumberOfUnits(oldCoverage.getIntialNumberOfUnits());
                coverage.setCurrentNumberOfUnits(oldCoverage.getCurrentNumberOfUnits());
                coverage.setValuePerUnit(oldCoverage.getValuePerUnit());
                coverage.setEffDate(oldCoverage.getEffDate());
                coverage.setIssuedAsAppliedInd(oldCoverage.getIssuedAsAppliedInd());
               
                for (int j = 0 ; j < oldCoverage.getCovOptionCount(); j++ ){
                    CovOption oldCovOption = oldCoverage.getCovOptionAt(j);
                    CovOption newCovOption = oldCovOption.clone(false);
                    if(newCovOption!=null){    //APSL3500
	                    newCovOption.setLifeParticipantRefID(null);
	                    coverage.setId("Coverage_1");
	                    coverage.addCovOption(newCovOption);
                    }    
                }
                life.addCoverage(coverage);
                break;
            }
        }
        /*APSL3393 :: End code for including base coverage and covoption in the Request*/

        ApplicationInfo applInfo = new ApplicationInfo();
        applInfo.setApplicationType(oldPolicy.getApplicationInfo().getApplicationType());
        applInfo.setApplicationJurisdiction(oldPolicy.getApplicationInfo().getApplicationJurisdiction());
        applInfo.setSignedDate(oldPolicy.getApplicationInfo().getSignedDate());
        policy.setApplicationInfo(applInfo);
        policy.setOLifEExtension(oldPolicy.getOLifEExtension());
        verifyLTCReplacement(policy); //APSL4036
        holding.setPolicy(policy);
        
        Party newOwnerParty = new Party();
        if (!NbaUtils.isBlankOrNull(getNbaTXLife().getPrimaryOwner())) {//QC7712 added if
        Party ownerParty = getNbaTXLife().getPrimaryOwner().getParty();
	        newOwnerParty.setId("Party_1");
	        newOwnerParty.setPartyTypeCode(ownerParty.getPartyTypeCode());
	        newOwnerParty.setPartyKey(ownerParty.getPartyKey());
	        newOwnerParty.setPartySysKey(ownerParty.getPartySysKey());
	        newOwnerParty.setResidenceState(ownerParty.getResidenceState());
	        newOwnerParty.setPersonOrOrganization(ownerParty.getPersonOrOrganization());
        }
        Party oldPrimaryWritingAgentParty = getNbaTXLife().getParty(((Relation) getObject()).getRelatedObjectID()).getParty();
        Party newPrimaryWritingAgentParty = new Party();
        newPrimaryWritingAgentParty.setId("Party_2");
        newPrimaryWritingAgentParty.setPartyTypeCode(oldPrimaryWritingAgentParty.getPartyTypeCode());
        newPrimaryWritingAgentParty.setPartyKey(oldPrimaryWritingAgentParty.getPartyKey());
        newPrimaryWritingAgentParty.setPartySysKey(oldPrimaryWritingAgentParty.getPartySysKey());
        newPrimaryWritingAgentParty.setResidenceState(oldPrimaryWritingAgentParty.getResidenceState());
        newPrimaryWritingAgentParty.setPersonOrOrganization(oldPrimaryWritingAgentParty.getPersonOrOrganization());

        CarrierAppointment oldCarrAppointment = oldPrimaryWritingAgentParty.getProducer().getCarrierAppointmentAt(0);
        Producer producer = new Producer();
        CarrierAppointment appointment = new CarrierAppointment();
        appointment.setId("CarrierAppointment_1");
        appointment.setPartyID(newPrimaryWritingAgentParty.getId());
        appointment.setCompanyProducerID(oldCarrAppointment.getCompanyProducerID());
        appointment.setCarrierCode(oldPolicy.getCarrierCode());
        appointment.setCarrierApptTypeCode(oldCarrAppointment.getCarrierApptTypeCode());

        //APSL3447
        CarrierAppointmentExtension originalCarrierAppointmentExtension = NbaUtils.getFirstCarrierAppointmentExtension(oldCarrAppointment);
        CarrierAppointmentExtension newCarrierAppointmentExtension = new CarrierAppointmentExtension();   
        if (originalCarrierAppointmentExtension != null) {
			newCarrierAppointmentExtension.setHVTInd(originalCarrierAppointmentExtension.getHVTInd());
		}
        OLifEExtension oLifeCarrAppExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_CARRIERAPPOINTMENT);
        oLifeCarrAppExt.setCarrierAppointmentExtension(newCarrierAppointmentExtension);
		appointment.addOLifEExtension(oLifeCarrAppExt);
		//End APSL3447
		
        producer.addCarrierAppointment(appointment);
        newPrimaryWritingAgentParty.setProducer(producer);

        Relation ownerRelation = new Relation();
        if (!NbaUtils.isBlankOrNull(getNbaTXLife().getPrimaryOwner())) {//APSL1299 added if
	        ownerRelation.setId("Relation_1");
	        ownerRelation.setOriginatingObjectID("Holding_1");
	        ownerRelation.setRelatedObjectID("Party_1");
	        ownerRelation.setRelationRoleCode(NbaOliConstants.OLI_REL_OWNER);
        }
        Relation writingAgentRelation = new Relation();
        writingAgentRelation.setId("Relation_2");
        writingAgentRelation.setOriginatingObjectID("Holding_1");
        writingAgentRelation.setRelatedObjectID("Party_2");
        writingAgentRelation.setRelationRoleCode(((Relation) getObject()).getRelationRoleCode());

        if (((Relation) getObject()).getOLifEExtensionCount() > 0) {
            ArrayList oLifeExtList = new ArrayList();
            OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_RELATIONPRODUCER);
            RelationProducerExtension relationProducerExtension = new RelationProducerExtension();
            RelationProducerExtension oldRelProdExtn = ((Relation) getObject()).getOLifEExtensionAt(0).getRelationProducerExtension();
            if (oldRelProdExtn != null) {
                relationProducerExtension.setSituationCode(oldRelProdExtn.getSituationCode());
            }

            olifeExt.setRelationProducerExtension(relationProducerExtension);
            oLifeExtList.add(olifeExt);
            writingAgentRelation.setOLifEExtension(oLifeExtList);

        }

        olifE.addHolding(holding);
        if (!NbaUtils.isBlankOrNull(getNbaTXLife().getPrimaryOwner())) {//APSL1299 added if
	        olifE.addParty(newOwnerParty);
	        olifE.addRelation(ownerRelation);
        }
        olifE.addParty(newPrimaryWritingAgentParty);
        olifE.addRelation(writingAgentRelation);

        nbaTXLife.setOLifE(olifE);
        return nbaTXLife;

    }

    /**
     * Override method for handling validation resopnse as its need to be added as system messages
     * 
     * @param nbaTXLife
     * @throws NbaBaseException
     */
    public void handleResponse() throws NbaBaseException { //ALS3675
        //Validation call will handle response
    }
    
  //APSL4036 New Method
	protected void verifyLTCReplacement(Policy requestPolicy) {
		FormInstance formInstance = NbaUtils.getFormInstance(getNbaTXLife(), NbaConstants.FORM_NAME_LTCSUPP);
		if (formInstance != null) {
			PolicyExtension polExtn = requestPolicy.getOLifEExtensionAt(0).getPolicyExtension();
			if (NbaUtils.isAnsweredYes(getFormResponseByAbbr(formInstance, NbaConstants.LTCS_REPLACEORINTENTLTC))) {
				if (NbaUtils.isBlankOrNull(polExtn)) {
					OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
					requestPolicy.addOLifEExtension(olifeExtension);
					polExtn = olifeExtension.getPolicyExtension();
				}
				polExtn.setLTCReplacementIndCode(NbaOliConstants.NBA_ANSWERS_YES);
			} else {
				if (!NbaUtils.isBlankOrNull(polExtn)) {
					polExtn.setLTCReplacementIndCode(null);
				}
			}
		}
	}
		
		//APSL4036 New Method  
	protected long getFormResponseByAbbr(FormInstance formInstance, String abbrev) {
		List formResponseList = formInstance.getFormResponse();
		if (formInstance != null && abbrev != null) {
			for (int i = 0; formResponseList != null && i < formResponseList.size(); i++) {
				FormResponse formResponse = (FormResponse) formResponseList.get(i);
				FormResponseExtension formResponseExt = NbaUtils.getFirstFormResponseExtension(formResponse);
				if (formResponseExt != null && abbrev.equalsIgnoreCase(formResponseExt.getQuestionTypeAbbr())) {
					return formResponse.getResponseCode();
				}
			}
		}
		return -1;
	}
    
}
