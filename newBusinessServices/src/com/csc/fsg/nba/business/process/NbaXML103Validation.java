package com.csc.fsg.nba.business.process; //NBA201

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
 * 
 * *******************************************************************************<BR>
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkDefaultFormatter;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaRolesData;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaCase;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Annuity;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Payout;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.TempInsAgreementInfo;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;

/**
 * This class does the validation of XML103Source. 
 * Validations include checking for null or empty of the required business fields;
 * checking if the Party and Relation object, that a LifeParticipant/Participant
 * is tied to, have been created 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA023</td><td>Version 2</td><td>Forms Tracking and Decisioning</td></tr>
 * <tr><td>NBA093</td><td>Version 3</td><td>Upgrade to ACORD 2.8</td></tr>
 * <tr><td>SPR1335</td><td>Version 3</td><td>Vantage Beneficiary Changes and Validation</td></tr>
 * <tr><td>SPR1825</td><td>Version 4</td><td>Handle situation where PRESUBMITVALIDATION can not connect to the ODBC data source</td></tr>
 * <tr><td>SPR2594</td><td>Version 5</td><td>Portal process error stops if SourceInfo not present on incoming 103 XML</td></tr>
 * <tr><td>SPR2602</td><td>Version 5</td><td>APPORTAL error Stops when the Ripped 103 XML is not well formed.</td></tr>  
 * <tr><td>SPR3329</td><td>Version 7</td><td>Prevent erroneous "Retrieve variable name is invalid" messages from being generated by OINK</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */

public class NbaXML103Validation implements NbaTableAccessConstants {

	protected ApplicationInfo objApplicationInfo = null;
	protected Relation objRelation = null;
	protected List validationErrors = null;
	protected ArrayList PartyIds = null;

	/** Contains HoldingId, PartyId and Role of LifeParticipant */
	protected List c_alHoldingParty = null;

	protected Life objLife = null;
	protected Annuity objAnnuity = null;
	protected NbaTXLife objNbaTXLife = null;
	protected Policy objPolicy = null;
	protected com.csc.fsg.nba.vo.NbaCase aCase; //NBA023
/**
 * NbaXML103Validation constructor.
 */
public NbaXML103Validation() {
	super();
}
/**
 * This method checks for relationship and partys. 
 */
//NBA023 new method
protected void doRelationshipValidation() {

	//Check if the Party that the LifeParticipants/Participants refers to
	//have been defined
	validatePartysForLifeParticipantOrAnnuity();

	//Validate to see if a Relation object containing the Role and Holding
	//that the LifeParticipant is tied to has been defined.
	validatePartyRelation();

}
/**
 * This is the only exposed method in this class. This method makes a call to the protected methods
 * 		- to call the VP/MS model to the null check 
 *		- to perform relationship validation
 * @param NbaCase 
 * @return List of validation errors. If there are no validation errors, List will be null.
 */
//NBA023 new method
public List doXML103Validation(NbaCase aCase) {
	try {
		//initilize member objects
		initializeData(aCase);
		//Check for null
		validateForNullOrEmpty();

		validateIfTIAAmountPresent();//APSL2735
		//NBA023 deleted

		//Perform relationship validation
		doRelationshipValidation(); //NBA023 
		
		//Begin NBLXA-2137
		if(NbaUtils.isGIApplication(getCase().getNbaLob())){
			validateForEmployerEmployeeSelectionOfGIApp();
		}
		//End NBLXA-2137 

		return getValidationErrors();

	} catch (Exception e) {
		setValidationErrors(new String(e.getMessage()));
		return getValidationErrors();
	}
}
/**
 * This returns the list containing the concatenated values of HoldingId, PartyId and 
 * the LifeParticipantRoleCode/ParticipantRoleCode to which the LifeParticipant/Participant
 * are tied. 
 * @return List 
 */
protected List getAllRoleCodeAndPartyId() {
	return c_alHoldingParty;
}
/**
 * Gets the Annuity value object
 * @return com.csc.fsg.nba.vo.txlife.Annuity
 */
protected Annuity getAnnuity() {
	return objAnnuity;
}
/**
 * Gets AplicationInfo object
 * @return ApplicationInfo value object
 */
protected ApplicationInfo getApplicationInfo() {
	return objApplicationInfo;
}
/**
 * Return NbaCase object
 * @return NbaCase
 */
//NBA023
public NbaCase getCase() {
	return aCase;
}
/**
 * Gets the Life value object
 * @return com.csc.fsg.nba.vo.txlife.Life
 */
protected Life getLife() {
	return objLife;
}
/**
 * Gets NbaTXLife object
 * @return com.csc.fsg.nba.vo.NbaTXLife
 */
protected NbaTXLife getNbaTXLife() {
	return objNbaTXLife;
}
/**
 * Gets a list of PartyIds that are referred to by LifeParticipants or Participants 
 * @return List PartyIds
 */
protected List getPartIds() {
	return PartyIds;
}
/**
 * Gets the Policy value object
 * @return com.csc.fsg.nba.vo.txlife.Policy
 */
protected Policy getPolicy() {
	return objPolicy;
}
/**
 * Gets Relation object
 * @return Relation value object
 */
protected Relation getRelation() {
	return objRelation;
}
/**
 * Gets the list of validation errors
 * @return List containing the validation errors
 */
protected List getValidationErrors() {	
	return validationErrors;
}
/**
 * This method does the initialization of member objects. Since the member objects are used 
 * in other methods, the initialization needs to be done before any other methods are called.
 * @param aCase An instance of NbaCase containing XML103.
 * @exception com.csc.fsg.nba.exception.NbaBaseException   
 */
//NBA023 new method 
protected void initializeData(NbaCase aCase) throws NbaBaseException { //SPR2602
	setCase(aCase);
	setNbaTXLife(getCase().getXML103Source());
	if (validationErrors == null)
		validationErrors = new ArrayList();
	if (c_alHoldingParty == null)
		c_alHoldingParty = new ArrayList();
	if (getNbaTXLife() != null) {
		setPolicy(getNbaTXLife().getPrimaryHolding().getPolicy());
		setApplicationInfo(getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo());

		if (getNbaTXLife().isAnnuity()) {
			setAnnuity(getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity());  //NBA093 SPR3290
			//Store all the PartyIds that Participants refer to
			setPartyIds(getAnnuity());
			setAllParticipantRoleCodeAndPartyId();

		} else if (getNbaTXLife().isLife()) {
			setLife(getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getLife());  //NBA093 SPR3290
			//Store all the PartyIds that LifeParticipants refer to
			setPartyIds(getLife());
			setAllLifeParticipantRoleCodeAndPartyId();
		}
	}

}
/**
 * This method creates a list of concatenated values of HoldingId, PartyId and 
 * the LifeParticipantRoleCode to which the LifeParticipant are tied. 
 */
protected void setAllLifeParticipantRoleCodeAndPartyId() {
	NbaTableAccessor ntsBean = null;
	String strTranslatedRoleCode = null;
	String strHoldingId = getNbaTXLife().getPrimaryHolding().getId();
	int iCoverageCount = getLife().getCoverageCount();
	
	for (int intCoverageCtr = 0; intCoverageCtr < iCoverageCount; intCoverageCtr++) {
		Coverage objCoverage = getLife().getCoverageAt(intCoverageCtr); // SPR3290
		int iLifeParticipantCount = objCoverage.getLifeParticipantCount();
		for (int intLifePartCtr = 0; intLifePartCtr < iLifeParticipantCount; intLifePartCtr++) {
			LifeParticipant objLifeParticipant = objCoverage.getLifeParticipantAt(intLifePartCtr); // SPR3290
			if (!objLifeParticipant.hasLifeParticipantRoleCode()) {
				setValidationErrors("The LifePartcipantRoleCode is either missing or invalid");
			} else {
				try {
					//Translate LifeParticipantRoleCode to RelationRoleCode
					ntsBean = new NbaTableAccessor();
					String fileControlID = getNbaTXLife().getBackendSystem();//SPR1335  //SPR2594
					Map aCase = ntsBean.createDefaultHashMap(fileControlID); //SPR1335
					//Override
					aCase.put("company", getNbaTXLife().getCarrierCode());
					aCase.put("productTypSubtyp", String.valueOf(getNbaTXLife().getPrimaryHolding().getPolicy().getProductType()));
					NbaTableData translatedValue =
						ntsBean.getRolesTranslationData(aCase, XML_PARTICIPANT_RELATION_TYPE, String.valueOf(objLifeParticipant.getLifeParticipantRoleCode()));
					if (translatedValue != null) {
							strTranslatedRoleCode = ((NbaRolesData) translatedValue).getRelationRoleValue();
							//SPR1335 begin
							if (strTranslatedRoleCode.equals(String.valueOf(NbaOliConstants.OLI_REL_BENEFICIARY)) //P2AXAL001 Added LIFE70 in OR condition to associate beneficairy with Coverage.
									&& (NbaConstants.SYST_VANTAGE.equals(fileControlID) || NbaConstants.SYST_LIFE70.equals(fileControlID)) && getNbaTXLife().isLife()) {
								c_alHoldingParty.add(objCoverage.getId() + "~" + objLifeParticipant.getPartyID() + "~" + strTranslatedRoleCode);
							} else if (String.valueOf(NbaOliConstants.OLI_REL_DEPENDENT).equals(strTranslatedRoleCode)) {
								//Begin AXAL3.7.02
								LifeParticipant pLifeParticipant = null;
								if (intLifePartCtr > 0) {
									pLifeParticipant = objCoverage.getLifeParticipantAt(0);
								}
								c_alHoldingParty.add(pLifeParticipant.getPartyID() + "~"+ objLifeParticipant.getPartyID() + "~" + strTranslatedRoleCode);
								//End AXAL3.7.02
							} else {
								c_alHoldingParty.add(strHoldingId + "~" + objLifeParticipant.getPartyID() + "~" + strTranslatedRoleCode);
							}
							//SPR1335 end
						} else {
						setValidationErrors("The LifePartcipantRoleCode is either missing or invalid");
					}
				} catch (NbaBaseException exBase) {
					setValidationErrors("The LifePartcipantRoleCode is either missing or invalid");
				}
			}
		}
	}
}
/**
 * This method creates a list of concatenated values of HoldingId, PartyId and 
 * the ParticipantRoleCode to which the Participant are tied. 
 */
protected void setAllParticipantRoleCodeAndPartyId() {
	String strTranslatedRoleCode = null;
	NbaTableAccessor ntsBean = null;
	String strHoldingId = getNbaTXLife().getPrimaryHolding().getId();

	//Get all Annuities within the Holding				
	int iPayOutCount = getAnnuity().getPayoutCount();
	for (int intCovCtr = 0; intCovCtr < iPayOutCount; intCovCtr++) {
		Payout objPayout = getAnnuity().getPayoutAt(intCovCtr); // SPR3290
		if (objPayout == null) {
			setValidationErrors("There are no payouts defined for the Holding " + strHoldingId);
			break;
		}

		//Get all the Participants within the current Annuity
		int iParticipantCount = objPayout.getParticipantCount();
		for (int intPartCtr = 0; intPartCtr < iParticipantCount; intPartCtr++) {
			Participant objParticipant = objPayout.getParticipantAt(intPartCtr);
			if (!objParticipant.hasParticipantRoleCode())
				setValidationErrors("The PartcipantRoleCode is either missing or invalid");
			else {
				try {
					//Translate LifeParticipantRoleCode to RelationRoleCode
					ntsBean = new NbaTableAccessor();
					Map aCase = ntsBean.createDefaultHashMap(getNbaTXLife().getBackendSystem());  //SPR2594
					aCase.put("company", getNbaTXLife().getCarrierCode());
					aCase.put("productTypSubtyp", String.valueOf(getNbaTXLife().getPrimaryHolding().getPolicy().getProductType()));
					NbaTableData translatedValue =
						ntsBean.getRolesTranslationData(aCase, XML_PARTICIPANT_RELATION_TYPE, String.valueOf(objParticipant.getParticipantRoleCode()));						
					if (translatedValue != null) {
						strTranslatedRoleCode = ((NbaRolesData) translatedValue).getRelationRoleValue();
						c_alHoldingParty.add(strHoldingId + "~" + objParticipant.getPartyID() + "~" + strTranslatedRoleCode);
					} else
						setValidationErrors("The PartcipantRoleCode is either missing or invalid");
				} catch (NbaBaseException exBase) {
					setValidationErrors("The PartcipantRoleCode is either missing or invalid");
				}
			}
		}

	}
}
/**
 * Sets the Annuity object from NbaTXlife.
 * @param aAnnuity com.csc.fsg.nba.vo.txlife.Annuity
 */
protected void setAnnuity(Annuity aAnnuity) {
	if (objAnnuity == null)
		objAnnuity = aAnnuity;
}
/**
 * Assigns value to the member variable  
 * @param appInfo com.csc.fsg.nba.vo.txlife.ApplicationInfo
 */
protected void setApplicationInfo(ApplicationInfo appInfo) {
	if (objApplicationInfo == null)
		objApplicationInfo = appInfo;
}
/**
 * Set NbaCase
 * @param newACase 
 */
//NBA023 new method
protected void setCase(NbaCase newACase) {
	aCase = newACase;
}
/**
 * Sets the Life object to the member variable.
 * @param aLife The instance of Life
 */
protected void setLife(Life aLife) {
	if (objLife == null)
		objLife = aLife;
}
/**
 * Set NbaTXLife object to the member variable.
 * @param aNbaTXLife The instance of XML103
 */
protected void setNbaTXLife(NbaTXLife aNbaTXLife) {
	if (objNbaTXLife == null)
		objNbaTXLife = aNbaTXLife;
}
/**
 * This stores the Party Ids of all the Party that the Participant object refers to
 * @param annuity The instance of Annuity
 */
protected void setPartyIds(Annuity annuity) {
	ArrayList alPayOuts = new ArrayList();
	ArrayList alParticipants = new ArrayList();
	if (PartyIds == null)
		PartyIds = new ArrayList();

	//Get all Annuities within the Holding				
	alPayOuts = annuity.getPayout();
	for (int intCovCtr = 0; intCovCtr < alPayOuts.size(); intCovCtr++) {
		Payout objPayout = (Payout) alPayOuts.get(intCovCtr);
		//Get all the Participants within the current Annuity
		alParticipants = objPayout.getParticipant();
		for (int intPartCtr = 0; intPartCtr < alParticipants.size(); intPartCtr++) {
			Participant objParticipant = objPayout.getParticipantAt(intPartCtr);
			if (objParticipant.hasParticipantRoleCode()) {
				if (!PartyIds.contains(objParticipant.getPartyID()))
					PartyIds.add(objParticipant.getPartyID());
			}
		}
	}

}
/**
 * This stores the Party Ids of all the Party that the LifeParticipant object 
 * refers to.
 * @param Life The instance of Life
 */
protected void setPartyIds(Life life) {
	if (PartyIds == null)
		PartyIds = new ArrayList();
	int iCoverageCount = getLife().getCoverageCount();
	for (int intCoverageCtr = 0; intCoverageCtr < iCoverageCount; intCoverageCtr++) {
		Coverage objCoverage = getLife().getCoverageAt(intCoverageCtr); // SPR3290
		//Get all the Life Participants within the current Coverage
		int iLifeParticipantCount = objCoverage.getLifeParticipantCount();
		for (int intLifePartCtr = 0; intLifePartCtr < iLifeParticipantCount; intLifePartCtr++) {
			LifeParticipant objLifeParticipant = objCoverage.getLifeParticipantAt(intLifePartCtr); // SPR3290
			if (objLifeParticipant.hasLifeParticipantRoleCode()) {
				if (!PartyIds.contains(objLifeParticipant.getPartyID()))
					PartyIds.add(objLifeParticipant.getPartyID());
			}
		}
	}
}
/**
 * Set Policy object to the member variable. 
 * @param aPolicy The instance of Policy
 */
protected void setPolicy(Policy aPolicy) {
	if ( objPolicy == null)
		objPolicy = aPolicy;
}
/**
 * Assigns value to the member variable
 * @param relation The instance of Relation
 */
protected void setRelation(Relation relation) {
	if (objRelation == null)
		objRelation = relation;
}
/**
 * Assigns validation error to the member variable  
 * @param String Error message
 */
protected void setValidationErrors(String strValidationErrors) {
	validationErrors.add(strValidationErrors);
}
/**
 * NBA023 comments changed
 * This method invokes the VP/MS model, PreSubmitValidation, to do the null or empty validation. 
 * The validation errors returned by the model are written to the member variable.    
 */
protected void validateForNullOrEmpty() {
	//NBA023 deleted

	//NBA023 begin
    NbaVpmsAdaptor vpmsProxy = null; //SPR3362
	try {	
		boolean isPortalCreated = getCase().getNbaLob().getPortalCreated();
		String formNumber = null;
		List sources = getCase().getNbaSources();
		String cwor=""; //APSL2735
		if (sources != null) {
			for (int i = 0; i < sources.size(); i++) {
				if (((NbaSource) sources.get(i)).getSource().getSourceType().equals(NbaConstants.A_ST_APPLICATION))
					formNumber = ((NbaSource) sources.get(i)).getNbaLob().getFormNumber();
				
				if (((NbaSource) sources.get(i)).getSource().getSourceType().equals(NbaConstants.A_ST_CWA_CHECK)) //APSL2735
					cwor = ((NbaSource) sources.get(i)).getNbaLob().getPaymentMoneySource();     //APSL2735
			}
		}

		Map deOink = new HashMap();
		if (formNumber!=null) {
			deOink.put("A_FORMNUMBERLOB", formNumber);
		}		
		deOink.put("A_PORTALCREATEDLOB", String.valueOf(isPortalCreated));
		deOink.put("A_CWORLOB", cwor); //APSL2735
			
		NbaOinkDataAccess oinkDataAccess = new NbaOinkDataAccess(getNbaTXLife());
		NbaOinkDefaultFormatter formatter = new NbaOinkDefaultFormatter();
		formatter.setDateSeparator(NbaOinkDefaultFormatter.DATE_SEPARATOR_DASH);
		oinkDataAccess.setFormatter(formatter);
		oinkDataAccess.setLobSource(getCase().getNbaLob());//SPR3329
		vpmsProxy = new NbaVpmsAdaptor(oinkDataAccess, NbaVpmsAdaptor.PRESUBMITVALIDATION); //SPR3362
		vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_PRESUBMITVALIDATION_RESULTS);
		vpmsProxy.setSkipAttributesMap(deOink);

		VpmsComputeResult vpmsComputeResult = vpmsProxy.getResults();	//SPR1825
		List errors = new ArrayList();
		if (vpmsComputeResult.isFatalError()) {	//SPR1825
			String error = "VP/MS fatal error - " + vpmsComputeResult.getMessage();	//SPR1825
			errors.add(error);
			setValidationErrors(error);
		//begin SPR1825
		} else {
			NbaStringTokenizer tokens = new NbaStringTokenizer(vpmsComputeResult.getResult(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
			tokens.nextToken();  
			while (tokens.hasMoreTokens()) {
				String error = tokens.nextToken();
				errors.add(error);
				setValidationErrors(error);
			}
		}
		//SPR3662 code deleted
		//end SPR1825

	} catch (Exception e) {
		setValidationErrors(e.getMessage());
	//begin SPR3362
        } finally {
            try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();
                }
            } catch (Throwable th) {
                LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
            }
     //end SPR3362
     }
	//NBA023 end
}
/**
 * This method checks for the presence of a Relation aggregate that defines 
 * the HoldingId and the Party that the LifeParticipants or Participants are attached to.    
 */
protected void validatePartyRelation() {
	String strSearchValue = null;
	Relation objRelation = null;	
	ArrayList alRelationRole = new ArrayList(); //Contains concatenated value of HoldingId, PartyId and RelationRoleCode of all relation objects  

	int intTopEntityCount = getNbaTXLife().getOLifE().getRelationCount();
	//Iterate thru all Relation aggregates and store the  HoldingId and Party that each Relation 
	//aggregate points to
	for (int intRelationCtr = 0; intRelationCtr < intTopEntityCount; intRelationCtr++) {
		objRelation = getNbaTXLife().getOLifE().getRelationAt(intRelationCtr); // SPR3290
		strSearchValue =
			objRelation.getOriginatingObjectID() + "~" + objRelation.getRelatedObjectID() + "~" + String.valueOf(objRelation.getRelationRoleCode());
		alRelationRole.add(strSearchValue);
	}

	//Check if the Relation object defines the Holding, Party and the role that 
	//LifeParticipant/Participant defines.
	for (int intLPCtr = 0; intLPCtr < c_alHoldingParty.size(); intLPCtr++) {
		//Check if the Relation belongs to the Holding that contained the Primary Insured
		//Since Hashtable is being used, it is important to typecast to String					
		if (!alRelationRole.contains(c_alHoldingParty.get(intLPCtr)))
			//Generate error message
			setValidationErrors(c_alHoldingParty.get(intLPCtr) + " has not been defined");
	}

}
/**
 * This method checks if the Parties, which the LifeParticipant/Participant refers to, have been defined.
 */
protected void validatePartysForLifeParticipantOrAnnuity() {
	Party objParty = null;
	int iPartiesCount = getNbaTXLife().getOLifE().getPartyCount();
	ArrayList alPartyIds = (ArrayList) getPartIds();
	boolean blnPartyDefined = false;

	for (int iPartyIdCtr = 0; iPartyIdCtr < alPartyIds.size(); iPartyIdCtr++) {
		for (int iPartyCtr = 0; iPartyCtr < iPartiesCount; iPartyCtr++) {
			objParty = getNbaTXLife().getOLifE().getPartyAt(iPartyCtr);
			if (objParty.getId().equals(alPartyIds.get(iPartyIdCtr))) {
				blnPartyDefined = true;
				break;
			}
		}
		if (!blnPartyDefined)
			setValidationErrors("The Party, " + alPartyIds.get(iPartyIdCtr) + ", that the LifeParticipant/ Participant defines has not been defined");
	}

}

	//APSL2735
	protected void validateIfTIAAmountPresent(){
		ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo());
		if(appInfoExt.getInitialPremiumPaymentForm()== NbaOliConstants.OLI_PAYFORM_EFT){
			TempInsAgreementInfo tempInfo = appInfoExt.getTempInsAgreementInfo();
			if(tempInfo != null && NbaUtils.isAnsweredYes(tempInfo.getMoneyWithAppIndCode())){
				if(NbaUtils.isBlankOrNull(tempInfo.getTIACashAmt())){
					setValidationErrors("TIA Amount is missing");
				}
			}
		}
	}
	//APSL2735 Ends
	
	
	//Begin NBLXA-2137 
	/**
	 * @Purpose This method will Check If user select any option either Employee or Employer owned of GI Application 
	 */
		protected void validateForEmployerEmployeeSelectionOfGIApp(){
			ApplicationInfoExtension appInfoExt = NbaUtils.getAppInfoExtension(getNbaTXLife().getPrimaryHolding().getPolicy().getApplicationInfo());
			boolean isEmployee_Employer_Owned = false;
			if (null != appInfoExt) {
				isEmployee_Employer_Owned = (appInfoExt.getOwnerTypeCode() == 0);
				if(NbaUtils.isBlankOrNull(appInfoExt.getOwnerTypeCode()) || isEmployee_Employer_Owned){
					setValidationErrors("Application must be Employer or Employee Owned");
				}
				
			}
				
		}
		//End NBLXA-2137
}
