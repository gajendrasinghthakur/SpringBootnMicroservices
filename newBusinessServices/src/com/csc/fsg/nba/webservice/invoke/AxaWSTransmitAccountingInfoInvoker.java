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

import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
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
 * This class is responsible for creating request for TAI Transmit webservice .
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.23</td><td>AXA Life Phase 1</td><td>Accounting Interface</td></tr>
 * <tr><td>P2AXAL019</td><td>AXA Life Phase 2</td><td>Cash Management</td></tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaWSTransmitAccountingInfoInvoker extends AxaWSInvokerBase {

	private static final String CATEGORY = "AccountingService";

	private static final String FUNCTIONID = "AccountingInformation";

	/**
	 * constructor from superclass
	 * @param userVO
	 * @param nbaTXLife
	 */

	public AxaWSTransmitAccountingInfoInvoker(String operation, NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
		super(operation, userVO, nbaTXLife, nbaDst, object);

		setBackEnd(ADMIN_ID);
		setCategory(CATEGORY);
		setFunctionId(FUNCTIONID);

	}

	/**
	 * This method is responsible for creating request for the transaction.
	 * @return nbaTXLife
	 */
	public NbaTXLife createRequest() throws NbaBaseException {
		FinancialActivity finActivity = ((FinancialActivity) getObject()).clone(false);
		//AXAL3.7.23 code deleted
		NbaTXRequestVO nbaTXRequestVO = createNbaRequestVO();
		Holding holding = new Holding();
		holding.setId(getNbaTXLife().getPrimaryHolding().getId());//P2AXAL019
		OLifE olife = getNbaTXLife().getOLifE();//P2AXAL019
		Policy policy = new Policy();
		policy.setCarrierAdminSystem(getBackEnd()); // ALII1895
		policy.setPolNumber(getNbaTXLife().getPolicy().getPolNumber());
		//AXAL3.7.23 code deleted
		policy.setProductType(getNbaTXLife().getPolicy().getProductType());
		policy.setProductCode(getNbaTXLife().getPolicy().getProductCode());
		policy.setCarrierCode(getNbaTXLife().getPolicy().getCarrierCode());
		//Begin P2AXAL019 Adds trans sub type and trans mode to distinguish the transactions
		if (NbaConstants.SYST_LIFE70.equalsIgnoreCase(getBackEnd())){
			if (finActivity.hasFinActivitySubType()) {
				nbaTXRequestVO.setTransMode(NbaOliConstants.TC_MODE_UPDATE);
			}
			//Begin QC13592/APSL4000
			if (getNbaTXLife().isReissue() && NbaConstants.PROC_ISSUE.equalsIgnoreCase(getNbaTXLife().getBusinessProcess())) {//ALII1206
				if (getNbaTXLife().isPaidReIssue()) {
					nbaTXRequestVO.setTransSubType(NbaOliConstants.OLI_TRANSSUBTYPE_1009850803);
				} else{
					nbaTXRequestVO.setTransSubType(NbaOliConstants.OLI_TRANSSUBTYPE_1009850802);
				}	
			} else if (NbaConstants.PROC_ISSUE.equalsIgnoreCase(getNbaTXLife().getBusinessProcess())) {
				nbaTXRequestVO.setTransSubType(NbaOliConstants.OLI_TRANSSUBTYPE_1009850801);
			}
			//End QC13592/APSL4000
			OLifEExtension oLifeExtn = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_POLICY);
			PolicyExtension polExt = NbaUtils.getFirstPolicyExtension(getNbaTXLife().getPolicy());
			if (polExt != null) {
				oLifeExtn.getPolicyExtension().setDistributionChannel(polExt.getDistributionChannel());
			}
			policy.addOLifEExtension(oLifeExtn);
		}
		//End P2AXAL019
		
		//APSL3460 :: start
		if ( NbaConstants.SYST_CAPS.equalsIgnoreCase(getBackEnd()) && 
		        getNbaTXLife().getPolicy().getApplicationInfo() != null &&
		        getNbaTXLife().getPolicy().getApplicationInfo().getOLifEExtensionCount() >0){
		    ApplicationInfoExtension extn = getNbaTXLife().getPolicy().getApplicationInfo().getOLifEExtensionAt(0).getApplicationInfoExtension();
		    if(extn != null && extn.getWorkFlowCaseId()!= null && extn.getWorkFlowCaseId().trim().length() >0){
		        ApplicationInfo applicationInfo = new ApplicationInfo();
		        applicationInfo.setTrackingID(getNbaTXLife().getPolicy().getApplicationInfo().getTrackingID());
		        OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_APPLICATIONINFO);
		        applicationInfo.addOLifEExtension(olifeExtension);
				ApplicationInfoExtension applicationInfoExtn = olifeExtension.getApplicationInfoExtension();
		        applicationInfoExtn.setWorkFlowCaseId(extn.getWorkFlowCaseId());
		        //Retrieving all the sources
		        List caseSourceList = getNbaDst().getNbaSources();
		        int caseSourceCount = (caseSourceList != null)? caseSourceList.size():0;
		        for (int i = 0; i < caseSourceCount; i++) {
		            NbaSource aNbaSource = (NbaSource) caseSourceList.get(i);
		            if (NbaConstants.A_ST_CWA_CHECK.equalsIgnoreCase(aNbaSource.getSourceType())) {
		                applicationInfoExtn.setScanStation(aNbaSource.getNbaLob().getCreateStation());
		                applicationInfoExtn.setCbancBatchId(aNbaSource.getNbaLob().getCbancBatchId());
		                applicationInfoExtn.setOriginalTrxDate(aNbaSource.getNbaLob().getCreateDate());
		            }
		        }
		        policy.setApplicationInfo(applicationInfo);
		        finActivity.setFinActivitySubType(NbaOliConstants.OLI_FINACTSUB_REINDEXPAYMENT);
		    }
		    
		}
		//APSL3460 :: End
		
		//create txlife with default request fields
		NbaTXLife nbaTXLife508 = new NbaTXLife(nbaTXRequestVO);
		Party insuredParty, producerParty = null;//P2AXAL019
		holding.setPolicy(policy);
		//Begin P2AXAL019
		OLifE newOlife = nbaTXLife508.getOLifE();
		newOlife.getSourceInfo().setFileControlID(getBackEnd());
		newOlife.addHolding(holding);
		newOlife.getHoldingAt(0).getPolicy().addFinancialActivity(finActivity); //SPR2817
		if (NbaConstants.SYST_LIFE70.equalsIgnoreCase(getBackEnd())) {
			Relation insuredRelation = getInsuredRelationObject(olife);
			if (insuredRelation != null) {
				insuredParty = NbaTXLife.getPartyFromId(insuredRelation.getRelatedObjectID(), olife.getParty());
				generateRelationObject(insuredRelation, newOlife);
				generatePartyforNewTxLife(insuredParty, newOlife);
			}
			Relation producerRelation = getProducerRelation(olife);
			if (producerRelation != null) {
				producerParty = NbaTXLife.getPartyFromId(producerRelation.getRelatedObjectID(), olife.getParty());
				generateRelationObject(producerRelation, newOlife);
				generatePartyforNewTxLife(producerParty, newOlife);
			}
		}
		//End P2AXAL019
		return nbaTXLife508;
	}

	//Begin P2AXAL019
	/**
	 * @param olifE
	 * @return insured relation object if it is present in xml 203
	 */
	private Relation getInsuredRelationObject(OLifE olifE) {
		List relationList = olifE.getRelation();
		for (int i = 0; i < relationList.size(); i++) {
			//Begin QC10215
			Relation relation = (Relation)relationList.get(i);
			if (NbaOliConstants.OLI_REL_INSURED == relation.getRelationRoleCode()&& relation.getOriginatingObjectID().equalsIgnoreCase(getNbaTXLife().getPrimaryHolding().getId())){//QC10215
				return relation;
			}
			//End QC10215
		}
		return null;
	}

	/**
	 * Generates Relation object based on Relation object available in contract xml
	 * 
	 * @param originalRelation Relation object from contract xml203
	 * @param newOlife  OLifE object
	 */
	protected void generateRelationObject(Relation originalRelation, OLifE newOlife) {
		Relation newRelation = new Relation();
		newRelation.setId(originalRelation.getId());
		newRelation.setOriginatingObjectID(originalRelation.getOriginatingObjectID());
		newRelation.setRelatedObjectID(originalRelation.getRelatedObjectID());
		newRelation.setOriginatingObjectType(originalRelation.getOriginatingObjectType());
		newRelation.setRelatedObjectType(originalRelation.getRelatedObjectType());
		newRelation.setRelationRoleCode(originalRelation.getRelationRoleCode());
		newOlife.addRelation(newRelation);
	}
	
  /**
   * Generates party object for 508 xml
   * @param originalParty
   * @param newOlife
   */
	protected void generatePartyforNewTxLife(Party originalParty, OLifE newOlife) {
		if (originalParty != null) {
			Party newParty = new Party();
			newParty.setId(originalParty.getId());
			newOlife.addParty(newParty);
			PersonOrOrganization originalPersonOrOrg = originalParty.getPersonOrOrganization();
			PersonOrOrganization newPersonOrOrg = new PersonOrOrganization();
			if (originalPersonOrOrg.isPerson()) {
				generatePersonObject(originalPersonOrOrg, newPersonOrOrg);
				newParty.setPersonOrOrganization(newPersonOrOrg);
			}
			if (originalPersonOrOrg.isOrganization()) {
				generateOrganizationObject(originalPersonOrOrg, newPersonOrOrg);
				newParty.setPersonOrOrganization(newPersonOrOrg);
			}
			if (originalParty.hasProducer()) {
				generateCarrierAppointmentObject(originalParty, newParty);
			}
			setResidenceState(originalParty, newParty);
		}
	}
	
	/**
	 * Sets Residence State on looping through Address objects available in contract xml
	 * @param originalParty Party object from contract xml203
	 * @param newParty Party object
	 */
	protected void setResidenceState(Party originalParty, Party newParty) {
		for (int i = 0; i < originalParty.getAddressCount(); i++) {
			Address originalAddress = originalParty.getAddressAt(i);
			if (originalAddress.hasAddressStateTC() && NbaOliConstants.OLI_ADTYPE_HOME == originalAddress.getAddressTypeCode()) {
				newParty.setResidenceState(originalAddress.getAddressStateTC());
			}
		}
	}

	/**
	 * Generates CarrierAppointmentObject object based on CarrierAppointmentObject object available in contract xml
	 * @param prodParty object from contract xml203
	 * @param newParty object
	 */
	protected void generateCarrierAppointmentObject(Party prodParty, Party newParty) {
		CarrierAppointment newCarrierAppointment = new CarrierAppointment();
		newCarrierAppointment.setCompanyProducerID(prodParty.getProducer().getCarrierAppointmentAt(0).getCompanyProducerID());
		Producer newProducer = new Producer();
		newProducer.addCarrierAppointment(newCarrierAppointment);
		newParty.setProducer(newProducer);
	}

	/**
	 * Returns relation object for RelationRole tc'"37" (Primary Writing Agent). If no Primary Writing Agent on contract, then user RelationRole
	 * tc"121" (Agency).
	 * @param olife object
	 * @return Relation object
	 */
	protected Relation getProducerRelation(OLifE olife) {
		List relationList = olife.getRelation();
		long[] relationCodeArr = { NbaOliConstants.OLI_REL_PRIMAGENT, NbaOliConstants.OLI_REL_AGENCYOF };
		for (int i = 0; i < relationCodeArr.length; i++) {
			for (int j = 0; j < relationList.size(); j++) {
				if (((Relation) relationList.get(j)).getRelationRoleCode() == relationCodeArr[i]) {
					return (Relation) relationList.get(j);
				}
			}
		}
		return null;
	}

	/**
	 * Generates Person object for XML508 based on Person object available in contract xml
	 * @param originalPersonOrOrg object from contract xml203
	 * @param newPersonOrOrg object
	 */
	protected void generatePersonObject(PersonOrOrganization originalPersonOrOrg, PersonOrOrganization newPersonOrOrg) {
		Person originalPerson = originalPersonOrOrg.getPerson();
		Person newPerson = new Person();
		newPerson.setFirstName(originalPerson.getFirstName());
		newPerson.setMiddleName(originalPerson.getMiddleName());
		newPerson.setLastName(originalPerson.getLastName());
		newPerson.setPrefix(originalPerson.getPrefix());
		newPerson.setSuffix(originalPerson.getSuffix());
		newPersonOrOrg.setPerson(newPerson);
	}

	/**
	 * Generates Organization object for XML508 based on Organization object available in contract xml
	 * @param originalPersonOrOrg object from contract xml203
	 * @param newPersonOrOrg object
	 */
	protected void generateOrganizationObject(PersonOrOrganization originalPersonOrOrg, PersonOrOrganization newPersonOrOrg) {
		Organization originalOrganization = originalPersonOrOrg.getOrganization();
		Organization newOrganization = new Organization();
		newOrganization.setDBA(originalOrganization.getDBA());
		newPersonOrOrg.setOrganization(newOrganization);
	}

	//End  P2AXAL019
	/**
	 * The method creates NbaTXRequestVO object.
	 * @return nbaTXRequest object
	 */
	protected NbaTXRequestVO createNbaRequestVO() {
		NbaTXRequestVO nbaTXRequestVO = new NbaTXRequestVO();
		nbaTXRequestVO.setNbaUser(getUserVO()); //AXAL3.7.23
		nbaTXRequestVO.setTransType(NbaOliConstants.TC_TYPE_CWA);
		nbaTXRequestVO.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequestVO.setBusinessProcess("");
		return nbaTXRequestVO;
	}
}
