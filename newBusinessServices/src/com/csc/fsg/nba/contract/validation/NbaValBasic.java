package com.csc.fsg.nba.contract.validation;
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
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.PolicyProduct;
import com.csc.fs.dataobject.accel.product.PolicyProductExtension;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.txlife.AccountingActivity;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.FinancialActivity;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Rider;
import com.csc.fsg.nba.vo.txlife.Risk;
import com.csc.fsg.nba.vo.txlife.SubstandardRating;
import com.csc.fsg.nba.vo.txlife.SubstandardRatingExtension;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;
/**
 * NbaValBasic performs basic contract validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>SPR1705</td><td>Version 4</td><td>Vantage Annuity validation</td></tr> 
 * <tr><td>SPR1764</td><td>Version 4</td><td>Redundant error messages are generated when ApplicationInfo.SignedDate is missing</td></tr> 
 * <tr><td>SPR1707</td><td>Version 4</td><td>Severe errors are generated for Substandard Extras</td></tr>
 * <tr><td>SPR1778</td><td>Version 4</td><td>Correct problems with SmokerStatus and RateClass in Automated Underwriting, and Validation.</td></tr>
 * <tr><td>SPR1800</td><td>Version 4</td><td>Effective and Issue date usage is incorrect.</td></tr>
 * <tr><td>SPR1466</td><td>Version 4</td><td>Scheduled Withrawals BF should be enabled for Vantage.</td></tr>
 * <tr><td>SPR1945</td><td>Version 4</td><td>Correct inconsistent contract validation edits for String values</td></tr>
 * <tr><td>SPR1994</td><td>Version 4</td><td>Correct user validation example </td></tr>
 * <tr><td>SPR1956</td><td>Version 4</td><td>Set TermDate and PayUpDate for CovOptions</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1234</td><td>Version 4</td><td>General source code clean up </td></tr>
 * <tr><td>SPR2090</td><td>Version 4</td><td>Set Policy.LineOfBusiness </td></tr> 
 * <tr><td>NBA104</td><td>Version 4</td><td>Pending Contract Calculations</td></tr>
 * <tr><td>NBA110</td><td>Version 4</td><td>Vntg issue to admin</td></tr>
 * <tr><td>SPR2148</td><td>Version 4</td><td>Changing the gender of an insured is not replicated out to the LifeParticipant/Participant</td></tr>
 * <tr><td>SPR1676</td><td>Version 5</td><td>Basic Validation (P031) does not create variable text for system message # 1015</td></tr>
 * <tr><td>SPR1677</td><td>Version 5</td><td>Basic Validation (P032) does not create variable text for system message # 1015</td></tr>
 * <tr><td>SPR1671</td><td>Version 5</td><td>Basic Validation (P022) does not create the correct variable text value for system message # 1009 </td></tr>
 * <tr><td>SPR2405</td><td>Version 5</td><td>Issues related to status bar on status page</td></tr>
 * <tr><td>SPR2439</td><td>Version 5</td><td>"Contract Signatures missing" is not generated for a missing AppProposedInsuredSignatureOK value</td></tr>
 * <tr><td>SPR2686</td><td>Version 5</td><td>Contract Status Bar incorrectly displaying the contract status for negatively disposed cases</td></tr>
 * <tr><td>SPR2251</td><td>Version 6</td><td>Handle Requested Issue Date for Reissued Contracts</td></tr>
 * <tr><td>SPR3047</td><td>Version 6</td><td>Basic validation P035 does not create system message # 1017 when it is invoked by APAPPSUB process and the signature is missing.</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>AXAL3.7.40</td> <td>AXA Life Phase 1</td><td>Contract Validation for Biling Subset</td></tr>
 * <tr><td>AXAL3.7.18</td><td>AXA Life Phase 1</td><td>Producer Interfaces</td></tr>
 * <tr><td>NBA254</td><td>Version 8</td><td>Automatic Closure and Refund of CWA</td></tr>
 * <tr><td>NBA231</td><td>Version 8</td><td>Replacement Processing</td></tr>
 * <tr><td>ALS2847</td><td>AXA Life Phase 1</td><td>QC # 1570  - End to End: Status in nbA does not match AXADistributors.com</td></tr>
 * <tr><td>ALS4876</td><td>AXA Life Phase 1</td><td>QC # 4028 - Informals End to End - Offer Expired Status incorrect in AXADistribuotrs</td></tr>
 * <tr><td>ALS4832</td><td>AXA Life Phase 1</td><td>QC # 3893 - Internal XML Status is Offer but UnderwritingResult is blank</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>NBA297</td><td>Version 8</td><td>Suitability</td></tr>
 * <tr><td>CR60519</td><td>AXA Life Phase 2</td><td>IUL Refresh</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaValBasic
	extends NbaContractValidationCommon
	implements NbaContractValidationBaseImpl, NbaContractValidationImpl {	//SPR1234
	/**
	 * Perform one time initialization.
	 */
	 //NBA237 changed method signature
	public void initialze(NbaDst nbaDst, NbaTXLife nbaTXLife, Integer subset, NbaOLifEId nbaOLifEId, AccelProduct nbaProduct, NbaUserVO userVO) { //AXAL3.7.18
		super.initialze(nbaDst, nbaTXLife, subset, nbaOLifEId, nbaProduct, userVO); //AXAL3.7.18
		initProcesses();
		Method[] allMethods = this.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("process_")) {
				// SPR3290 code deleted
				processes.put(aMethodName.substring(8).toUpperCase(), aMethod);
			}
		}
	}
	// SPR1994 code deleted
	/**
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationImpl#validate()
	 */
	// ACN012 changed signature
	public void validate(ValProc nbaConfigValProc, ArrayList objects) {
		if (nbaConfigValProc.getUsebase()) {  //ACN012
			super.validate(nbaConfigValProc, objects);
		} else{ //ALS2600
		    if (getUserImplementation() != null) {
		        getUserImplementation().validate(nbaConfigValProc, objects);
		    }
		} //ALS2600    
	}
	/**
	 * Verify HoldingType is "2" (Policy), if not then set to "2"..
	 */
	protected void process_P001() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBasic.process_P001()");//NBA103
			if (!getHolding().hasHoldingTypeCode() || getHolding().getHoldingTypeCode() != OLI_HOLDTYPE_POLICY) {
				getHolding().setHoldingTypeCode(OLI_HOLDTYPE_POLICY);
				getHolding().setActionUpdate();
			}
		}
	}
	/**
	 * Set Holding.Policy.CarrierAdminSystem from SourceInfo.FileControlId 
	 */
	protected void process_P9005() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P9005()");//NBA103
			if (!getPolicy().hasCarrierAdminSystem()) {
				if (getOLifE().hasSourceInfo()) {
					getPolicy().setCarrierAdminSystem(getOLifE().getSourceInfo().getFileControlID());
					getPolicy().setActionUpdate();
				}
			}
		}
	}
	/**
	 * Validate CarrierCode against table NBA_Company.
	 */
	protected void process_P002() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P002()");//NBA103
			if (!isValidCompany(getPolicy().getCarrierCode())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Carrier Code: ", getPolicy().getCarrierCode()), getIdOf(getPolicy()));
			}
		}
	}
	/**
	 * Validate AdministeringCarrierCode against table NBA_Company.  
	 * Set AdministeringCarrierCode value equal to CarrierCode if none present.
	 */
	protected void process_P003() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P003()");//NBA103
			if (!isValidCompany(getPolicy().getAdministeringCarrierCode())) {
				if (isValidCompany(getPolicy().getCarrierCode())) {
					getPolicy().setAdministeringCarrierCode(getPolicy().getCarrierCode());
					getPolicy().setActionUpdate();
				} else {
					addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concat("Administrating Carrier Code: ", getPolicy().getAdministeringCarrierCode()),
						getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	 * Validate that the Statutory CompanyCode against table OLIEXT_LU_STATCOMPANY.  
	 */
	protected void process_P004() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P004()");//NBA103
			if (!isValidStatutoryCompany(getPolicyExtension().getStatutoryCompanyCode())) {
				//NBA110 - removed code
				addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Statutory Company Code: ", getPolicyExtension().getStatutoryCompanyCode()),
					getIdOf(getPolicy()));
			}
		}
	}
	//NBA110
	private boolean isValidStatutoryCompany(String statutoryCompanyCode) {
		return statutoryCompanyCode != null && isValidTableValue(NbaTableConstants.OLIEXT_LU_STATCOMPANY, statutoryCompanyCode);
	}
	/**
	 * Set the ApplicationType to "1" (New) if none present. Validate ApplicationType against table OLI_LU_APPTYPE.
	 */
	protected void process_P005() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P005()");//NBA103
			// SPR3290 code deleted
			if (!getApplicationInfo().hasApplicationType()) {
				if(!NbaUtils.isBlankOrNull(getNbaDst().getNbaLob().getApplicationType())) {//ALS5856
					getApplicationInfo().setApplicationType(getNbaDst().getNbaLob().getApplicationType());//ALS5856
				}else{
					getApplicationInfo().setApplicationType(OLI_APPTYPE_NEW);	
				}
				getApplicationInfo().setActionUpdate();
			}
			long type = getApplicationInfo().getApplicationType();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_APPTYPE, type)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Application Type: ", type), getIdOf(getApplicationInfo()));
			}
		}
	}
	/**
	 * Verify the contract EffDate (effective date).  If none present, use VP/MS model to determine the contract effective date.  
	 * Set effective date for all coverages, coverage options and extras to equal the contract effective date.
	 * Effective date for base plan, coverages, coverage options and extras.  Date at which contract goes into force.  
	 * This date may be different from the date of issue. For CyberLife, this is a temporary date at the time of submission.  
	 * It is usually the application SignedDate, but can also be: The medical exam date or RequestedPolDate. 
	 */
	protected void process_P006() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P006()");//NBA103			
			//SPR1800 code deleted
			//Begin AXAL3.7.40
			long ageRule = 11; //Age last Birthdate
			ArrayList coverageList = getLife().getCoverage();
			if (coverageList != null) {
				for (int i = 0; i < coverageList.size(); i++) {
					coverage = (Coverage) coverageList.get(i);
					if (coverage.getIndicatorCode() == OLI_COVIND_BASE) {
						PolicyProduct policyProduct = getPolicyProductFor(coverage);
						if (policyProduct.hasAgeCalculationType()) {
							ageRule = policyProduct.getAgeCalculationType();
						}
					}
				}
			}
			Map skipAttributes = new HashMap();
			skipAttributes.put("A_AgeRule", new Long(ageRule).toString());
			performVpmsCalculation(skipAttributes, null, true);
			//End AXAL3.7.40
			NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the effective date
			if (nbaVpmsResultsData != null) {
				if (nbaVpmsResultsData.wasSuccessful()) {
					String effStringDate = (String) nbaVpmsResultsData.getResultsData().get(0);
					try {
						Date effDate = get_YYYY_MM_DD_sdf().parse(effStringDate);
						getPolicy().setEffDate(effDate);
						getPolicy().setActionUpdate();
					} catch (ParseException e) {
						addNewSystemMessage(
							getNbaConfigValProc().getMsgcode(),
							concat("Unknown date format returned from VP/MS model: ", getNbaConfigValProc().getModel(), ", Date: ", effStringDate),
							getIdOf(getPolicy()));
					}
				//SPR1080 code deleted
				}
			//begin SPR1080
			} else {
				addNewSystemMessage(
					INVALID_VPMS_CALC,
					concat("Process: ", getNbaConfigValProc().getId(), ", Model: ", getNbaConfigValProc().getModel()),
					getIdOf(getPolicy()));
			//end SPR1080
			}
			//SPR1800 code deleted
			if (getPolicy().hasEffDate()) {
				Date effDate = getPolicy().getEffDate();
				if (getLife() != null) { // Life products
					for (int i = 0; i < getLife().getCoverageCount(); i++) {
						Coverage coverage = getLife().getCoverageAt(i);
						coverage.setEffDate(effDate);
						coverage.setActionUpdate();
						for (int j = 0; j < coverage.getCovOptionCount(); j++) {
							CovOption covOption = coverage.getCovOptionAt(j);
							covOption.setEffDate(effDate);
							covOption.setActionUpdate();	//SPR1956
							for (int k = 0; k < covOption.getSubstandardRatingCount(); k++) {
								SubstandardRating substandardRating = covOption.getSubstandardRatingAt(k);
								SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
								substandardRatingExtension.setEffDate(effDate);
								substandardRatingExtension.setActionUpdate();
							}
						}
						for (int j = 0; j < coverage.getLifeParticipantCount(); j++) {
							LifeParticipant lifeParticipant = coverage.getLifeParticipantAt(j);
							for (int k = 0; k < lifeParticipant.getSubstandardRatingCount(); k++) {
								SubstandardRating substandardRating = lifeParticipant.getSubstandardRatingAt(k);	//SPR1707
								SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
								substandardRatingExtension.setEffDate(effDate);
								substandardRatingExtension.setActionUpdate();
							}
							}
						}
					} else if (getAnnuity() != null) { // Annuity products
					//begin SPR1996
					for (int i = 0; i < getAnnuity().getRiderCount(); i++) {
						Rider rider = getAnnuity().getRiderAt(i);
						rider.setEffDate(effDate);
						for (int j = 0; j < rider.getCovOptionCount(); j++) {
							CovOption covOption = rider.getCovOptionAt(j);
							covOption.setEffDate(effDate);
							covOption.setActionUpdate();
							for (int k = 0; k < covOption.getSubstandardRatingCount(); k++) {
								SubstandardRating substandardRating = covOption.getSubstandardRatingAt(k);
								SubstandardRatingExtension substandardRatingExtension = NbaUtils.getFirstSubstandardExtension(substandardRating);
								substandardRatingExtension.setEffDate(effDate);
								substandardRatingExtension.setActionUpdate();
							}
						}
					}
					//end SPR1996
				}
			}
		}
	}
	/**
	 * Verify application SignedDate is present.  Default the application SignedDate to the Policy.EffDate (effective date) if none present.
	 */
	protected void process_P007() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P007()");//NBA103
			if (!getApplicationInfo().hasSignedDate()) {
				if (getPolicy().hasEffDate() && isValidDate(getPolicy().getEffDate())) {
					getApplicationInfo().setSignedDate(getPolicy().getEffDate());
					getApplicationInfo().setActionUpdate();
				}
			}
		}
	}
	/**
	 * Verifies application signed date is valid.
	 */
	protected void process_P9001() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P008()");//NBA103
			if (!getApplicationInfo().hasSignedDate() || !isValidDate(getApplicationInfo().getSignedDate())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getApplicationInfo()));
			}
		}
	}
	/**
	 * Verify application SignedDate is not beyond backdating limit for submitted application. Based on company business rules. Model returns 1 - True
	 * 0 - False
	 */
	protected void process_P008() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P008()");//NBA103
			if (getApplicationInfo().hasSignedDate()) {
				NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the backdating date
				boolean invalid = true;
				if (nbaVpmsResultsData != null) {
					if (nbaVpmsResultsData.wasSuccessful()) {
						if (((String) nbaVpmsResultsData.getResultsData().get(0)).equals("1")) {
							invalid = false;
						}
					}
				}
				if (invalid) {
					String signedDate = NbaUtils.getStringInUSFormatFromDate(getApplicationInfo().getSignedDate());
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Signed date: ", signedDate), getIdOf(getApplicationInfo()));
				}
			} 
			//SPR1764 code deleted
		}
	}
	/**
	 * Validate ApplicationJurisdiction (state) against table NBA_STATES. If not generate severe error message 1902: State signed at omitted - on
	 * application.
	 */
	protected void process_P010() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P010()");//NBA103			
			if (!isValidState(getApplicationInfo().getApplicationJurisdiction())) {
				setGenerateRequirement(true); //AXAL3.7.40
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "On Application", getIdOf(getApplicationInfo())); //AXAL3.7.40
			}			
		}
	}
	/**
	 * Verify the presence of an HOAssignedAppNumber (application number) for a submitted (new) application.
	 */
	protected void process_P011() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P011()");//NBA103
			// NBA104 deleted code
			if (!(getApplicationInfo().hasHOAssignedAppNumber()
				&& getApplicationInfo().getHOAssignedAppNumber().trim().length() > 0)) { //SPR1945
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getApplicationInfo()));
			}
			// NBA104 deleted code
		}
	}
	/**
	 * SetPolicy.Jurisdiction (issue state) equal to the ApplicationInfo.ApplicationJurisdiction if none present.
	 * Validate Policy.Jurisdiction against table NBA_STATES.
	 */
	protected void process_P012() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P012()");//NBA103
			if (!getPolicy().hasJurisdiction() || getPolicy().getJurisdiction() != getApplicationInfo().getApplicationJurisdiction()) {
				getPolicy().setJurisdiction(getApplicationInfo().getApplicationJurisdiction());
				getPolicy().setActionUpdate();
			} 
			
			if (!isValidState(getPolicy().getJurisdiction())) {
				addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Issue Jurisdiction: ", getPolicy().getJurisdiction()),
					getIdOf(getPolicy()));
			}
		}
	}
	/**
	 * Set Party.ResidenceState to Address.AddressStateTC if Party.ResidenceState is missing
	 * and there is only one address.
	 * Verify Party.ResidenceState (residence state) is a valid state.
	 */
	protected void process_P013() {
		if (verifyCtl(PARTY)) {
			logDebug("Performing NbaValBasic.process_P013() for " ,  getParty());//NBA103
			NbaParty nbaParty = nbaTXLife.getParty( getParty().getId());
			Address rAddress = nbaParty.getAddress(OLI_ADTYPE_HOME);
			if ( rAddress != null && rAddress.hasAddressStateTC() ){
				getParty().setResidenceState( rAddress.getAddressStateTC() );
				if ( rAddress.hasAddressCountryTC() ){
					getParty().setResidenceCountry(rAddress.getAddressCountryTC() );
				}else {
					getParty().setResidenceCountry(NbaOliConstants.OLI_UNKNOWN);
				}
				getParty().setActionUpdate();
				//AXAL3.7.40
				if (!isValidState(getParty().getResidenceState())) {
					addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concat("Residence State: ", getParty().getResidenceState()),
						getIdOf(getParty()));
				}
			//begin AXAL3.7.40
			} else {
				getParty().setResidenceState(null);
				getParty().setActionUpdate();
			}
			//end AXAL3.7.40
		} 
	}
	/**
	 * Validate AssignmentCode against table OLI_LU_ASSIGNED.  Set to tc=1 (Unassigned) if invalid.
	 */
	protected void process_P014() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBasic.process_P014()");//NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_ASSIGNED, getHolding().getAssignmentCode())) {
				getHolding().setAssignmentCode(OLI_ASSIGNED_NONE);
				getHolding().setActionUpdate();
			}
		}
	}
	/**
	 * Validate RestrictionCode against table OLI_LU_RESTRICT.  Set to tc=1 (None) if missing or invalid.
	 */
	protected void process_P015() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBasic.process_P015()");//NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_RESTRICT, getHolding().getRestrictionCode())) {
				getHolding().setRestrictionCode(OLI_RESTRICT_NONE);
				getHolding().setActionUpdate();
			}
		}
	}
	/**
	 * Validate ReplacementType against table OLI_LU_REPLACETYPE.  Set to tc=1 (Not a Replacement Contract) if missing or invalid.
	 */
	protected void process_P016() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P016()");//NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_REPLACETYPE, getPolicy().getReplacementType())) {
				getPolicy().setReplacementType(OLI_REPTY_NONE);
				getPolicy().setActionUpdate();
			}
		}
	}
	/**
	 * Set QualifiedCode from PolicyProductExtension.PensionCode.
	 * STP is using some different translations than ACORD for FBRPENSN because of ACORD's interpretation 
	 * of the maintenance request to add this tag.
	 */
	protected void process_P017() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBasic.process_P017()");//NBA103
			PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
			if (policyProductExtension == null || !policyProductExtension.hasPensionCode()) {
				if (!getHolding().hasQualifiedCode()) { //NBA237
					getHolding().setQualifiedCode(NbaOliConstants.OLI_UNKNOWN); //Zero for Not a Pension. //NBA237
				} //NBA237
			} else {
				getHolding().setQualifiedCode(policyProductExtension.getPensionCode()); //SPR1705
				//NBA237 code deleted
			}
			getHolding().setActionUpdate();
		}
	}
	/**
	 * Set QualifiedCode from PolicyProductExtension.QualifiedCode.
	 */
	//SPR1705 new method
	protected void process_P033() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBasic.process_P033()");//NBA103
			PolicyProductExtension policyProductExtension = getPolicyProductExtensionForPlan();
			if (policyProductExtension == null || !policyProductExtension.hasQualifiedCode()) {
				addPlanInfoMissingMessage("PolicyProductExtension.QualifiedCode", getIdOf(getHolding()));
			} else {
				getHolding().setQualifiedCode(policyProductExtension.getQualifiedCode());
				getHolding().setActionUpdate();
			}
		}
	}
	/**
	 * Calculate contract issue date. Bypass calculation and use requested issue date, if present.
	 */
	protected void process_P018() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P018()");//NBA103
			if (getApplicationInfo().hasRequestedPolDate()) {
				getPolicy().setIssueDate(getApplicationInfo().getRequestedPolDate());
				getPolicy().setActionUpdate();
			} else {
				NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the effective date
				if (nbaVpmsResultsData != null) {
					if (nbaVpmsResultsData.wasSuccessful()) {
						String issueStringDate = (String) nbaVpmsResultsData.getResultsData().get(0);
						try {
							Date effDate = get_YYYY_MM_DD_sdf().parse(issueStringDate);
							getPolicy().setIssueDate(effDate);
							getPolicy().setActionUpdate();
						} catch (ParseException e) {
							addNewSystemMessage(
								getNbaConfigValProc().getMsgcode(),
								concat("Unknown date format: ", issueStringDate),
								getIdOf(getPolicy()));
						}
					}
				} else {
					addNewSystemMessage(
						INVALID_VPMS_CALC,
						concat("Process: ", getNbaConfigValProc().getId(), ", Model: ", getNbaConfigValProc().getModel()),
						getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	 * Set last anniversary date equal to contract effective date.
	 */
	protected void process_P019() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P019()");//NBA103
			getHolding().setLastAnniversaryDate(getPolicy().getEffDate());
		}
	}
	/**
	 *  Set the last accounting date to the most recent accounting activity date.
	 */
	protected void process_P020() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBasic.process_P020()");//NBA103
			Date highDate = null;
			int finCount = getPolicy().getFinancialActivityCount();
			for (int i = 0; i < finCount; i++) {
				FinancialActivity financialActivity = getPolicy().getFinancialActivityAt(i);
				if (!financialActivity.isActionDelete()) {
					int acctCount = financialActivity.getAccountingActivityCount();
					for (int j = 0; j < acctCount; j++) {
						AccountingActivity accountingActivity = financialActivity.getAccountingActivityAt(j);
						if (!accountingActivity.isActionDelete()) {
							if (accountingActivity.hasActivityDate()) {
								if (highDate == null || highDate.compareTo(accountingActivity.getActivityDate()) < 0) {
									highDate = accountingActivity.getActivityDate();
								}
							}
						}
					}
				}
			}
			getHoldingExtension().setLastAccountingDate(highDate);
		}
	}
	/**
	 * Compare application SignedDate vs. RequestedPolDate to verify that RequestedPolDate is not more than (x period) 
	 * prior to SignedDate, where x period is determined based on company rules.
	 */
	protected void process_P021() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P021()");//NBA103
			if (getApplicationInfo().hasRequestedPolDate()) {
				boolean dateError = true;
				String reqIssueDate = NbaUtils.getStringInUSFormatFromDate(getApplicationInfo().getRequestedPolDate());
				String signedDate = "unknown";
				if (getApplicationInfo().hasSignedDate()) {
					signedDate = NbaUtils.getStringInUSFormatFromDate(getApplicationInfo().getSignedDate());
					NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the limit
					if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
						Date calcDate = addVpmsPeriod(getApplicationInfo().getRequestedPolDate(), nbaVpmsResultsData);
						//Request Date + period needs to be greater than or equal to application date to be within limit
						if (calcDate != null && NbaUtils.compare(calcDate, getApplicationInfo().getSignedDate()) > -1) {
							dateError = false;
						}
					}
				}
				if (dateError) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Signed date: ", signedDate, ", Requested date: ", reqIssueDate),
							getIdOf(getApplicationInfo()));
				}
			}
		}
	}
	/**
	 * Compare application SignedDate vs. Policy.EffDate to verify that Policy.EffDate is not more than (x period) prior to SignedDate, where x period
	 * is determined based on company rules.
	 */
	protected void process_P022() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P022()");//NBA103
			boolean dateError = true;
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());//ALS3601
			//Do not generate CV if back date age indicator is set to yes
			if (!(appInfoExt != null && OLI_LU_BACKDATE_SAVE_AGE == appInfoExt.getBackDateType())) {//ALS3601
				if (getPolicy().hasEffDate() && getApplicationInfo().hasSignedDate()) {
					NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the limit
					if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
						Date calcDate = addVpmsPeriod(getPolicy().getEffDate(), nbaVpmsResultsData);
						//Eff Date + period needs to be greater than or equal to application date to be within limit
						if (calcDate != null && NbaUtils.compare(calcDate, getApplicationInfo().getSignedDate()) > -1) {
							dateError = false;
						}
					}
				}
				if (dateError) {
					String effDate = "unknown";
					String signedDate = "unknown";
					if (getPolicy().hasEffDate()) {
						effDate = NbaUtils.getStringInUSFormatFromDate(getPolicy().getEffDate()); //SPR1671
					}
					if (getApplicationInfo().hasSignedDate()) {
						signedDate = NbaUtils.getStringInUSFormatFromDate(getApplicationInfo().getSignedDate());
					}
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Signed date: ", signedDate, ", Effective date: ", effDate),
							getIdOf(getApplicationInfo())); //SPR1671
				}
			}
		}
	}
	/**
	 * Compare application SignedDate vs. RequestedPolDate to verify that RequestedPolDate is not more than (x period) in advance of SignedDate, where
	 * x period is determined based on company rules.
	 */
	protected void process_P9002() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P9002()");//NBA103
			if (getApplicationInfo().hasRequestedPolDate()) {
				boolean dateError = true;
				String reqIssueDate = NbaUtils.getStringInUSFormatFromDate(getApplicationInfo().getRequestedPolDate());
				String signedDate = "unknown";
				if (getApplicationInfo().hasSignedDate()) {
					signedDate = NbaUtils.getStringInUSFormatFromDate(getApplicationInfo().getSignedDate());
					NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the  limit
					if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
						Date calcDate = addVpmsPeriod(getApplicationInfo().getSignedDate(), nbaVpmsResultsData);
						//Signed Date + period needs to be greater than or equal to Requested date to be within limit
						if (calcDate != null && NbaUtils.compare(calcDate, getApplicationInfo().getRequestedPolDate()) > -1) {
							dateError = false;
						}
					}
				}
				if (dateError) {
					addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concat("Signed date: ", signedDate, ", Requested date: ", reqIssueDate),
						getIdOf(getApplicationInfo()));
				}
			}
		}
	}
	/**
	 * Compare Policy.EffDate to verify that Policy.EffDate is not more than (x period)
	 * in advance of SignedDate, where x period is determined based on company rules.  
	 */
	protected void process_P9003() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P022()");//NBA103
			boolean dateError = true;
			if (getPolicy().hasEffDate() && getApplicationInfo().hasSignedDate()) {
				NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //Get the  limit
				if (nbaVpmsResultsData != null && nbaVpmsResultsData.wasSuccessful()) {
					Date calcDate = addVpmsPeriod(getApplicationInfo().getSignedDate(), nbaVpmsResultsData);
					//Application date + period needs to be greater than or equal to Eff date to be within limit
					if (calcDate != null && NbaUtils.compare(calcDate, getPolicy().getEffDate()) > -1) {
						dateError = false;
					}
				}
			}
			if (dateError) {
				String effDate = "unknown";
				String signedDate = "unknown";
				if (getPolicy().hasEffDate()) {
					effDate = NbaUtils.getStringInUSFormatFromDate(getPolicy().getEffDate());
				}
				if (getApplicationInfo().hasSignedDate()) {
					signedDate = NbaUtils.getStringInUSFormatFromDate(getApplicationInfo().getSignedDate());
				}
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Signed date: ", signedDate, ", Effective date: ", effDate), getIdOf(getApplicationInfo()));
			}
		}
	}
	/**
	 * Set PendingContractStatus.
	 * character 1 = 0 
	 * character 2 = 0
	 * 
	 * For non-issued policies
	 * character 3 = Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingApproval  
	 * character 4 = Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingStatus
	 * 
	 * For issued policies
	 * character 3 = 0
	 * character 4 = 1
	 */
	protected void process_P023() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P023()");//NBA103
			String char1 = "0";
			String char2 = "0";
			String char3 = "0";
			String char4 = "0";
			ApplicationInfo applicationInfo = getApplicationInfo();
			if (applicationInfo != null && !applicationInfo.isActionDelete()) {
				ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
				if (applicationInfoExtension != null && !applicationInfoExtension.isActionDelete()) {
					if (applicationInfoExtension.hasUnderwritingApproval()) {
						String undwrtApproval =
							translateToBESValue(
								String.valueOf(
									applicationInfoExtension
										.getUnderwritingApproval()),
								NbaTableConstants.OLIEXT_LU_UNDAPPROVAL); //SPR2686
								if(null != undwrtApproval){//SPR2686
									char3 = undwrtApproval;//SPR2686
								}//SPR2686
								// Beging NBA254
								if ( (new Date().after(applicationInfo.getPlacementEndDate())&& applicationInfoExtension.getUnderwritingApproval() == OLIX_UNDAPPROVAL_UNDERWRITER)) {
										char1 = "8";
								}
								//End NBA254
					}
					
					if (applicationInfoExtension.hasUnderwritingStatus()) {
						String undwrtStatus =
							translateToBESValue(
								String.valueOf(
									applicationInfoExtension
										.getUnderwritingStatus()),
								NbaTableConstants.NBA_FINAL_DISPOSITION); //SPR2686
								if(null != undwrtStatus){//SPR2686
									char4 = undwrtStatus;//SPR2686
								}//SPR2686
					}
					//Begin SPR2405
					if ((OLI_POLSTAT_ISSUED == getPolicy().getPolicyStatus())
						&& (OLIX_UNDAPPROVAL_UNDERWRITER == applicationInfoExtension.getUnderwritingApproval())) {
						char3 = "0";
						char4 = "1";
					}
					//End SPR2405
				}
			}
			getPolicyExtension().setPendingContractStatus(concat(char1, char2, char3, char4));
			getPolicyExtension().setActionUpdate();
		}
	}
	/**
	 * Set initial PendingContractStatus to "Pending"  (UX)
	 */
	protected void process_P9004() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P9004()");//NBA103
			getPolicyExtension().setPendingContractStatus("UX");
			getPolicyExtension().setActionUpdate();
		}
	}
	/**
	 * Verifies that Policy.ProductCode is present in PolicyProduct.ProductCode and that the 
	 * corresponding plan definition business rules have been defined. 
	 */
	protected void process_P024() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P024()");//NBA103
			if (getPolicyProductForPlan() == null) {
				String productCode = "unknown";
				if (getPolicy().hasProductCode() && getPolicy().getProductCode().trim().length() > 0) { //SPR1945
					productCode = getPolicy().getProductCode();
				}
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Product Code: ", productCode), getIdOf(getPolicy()));
			}
		}
	}
	/**
	 * Verify Coverage.ProductCode is valid and that the corresponding plan definition business rules have been defined. 
	 */
	protected void process_P025() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBasic.process_P025()");//NBA103
			if (getPolicyProductFor(getCoverage()) == null) {
				String productCode = "unknown";
				if (getCoverage().hasProductCode() && getCoverage().getProductCode().trim().length() > 0) { //SPR1945
					productCode = getCoverage().getProductCode();
				}
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Product Code: ", productCode), getIdOf(getCoverage()));
			}
		}
	}
	/**
	 * If Policy.ProductCode is missing, use ProductCode from base Coverage.
	 * Verify that Policy.ProductCode is present in PolicyProduct.ProductCode and that the 
	 * corresponding plan definition business rules have been defined.   
	 */
	protected void process_P026() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P026()");//NBA103
			if (!(getPolicy().hasProductCode() && getPolicy().getProductCode().trim().length() > 0)) { //SPR1945
				Coverage coverage = getNbaTXLife().getPrimaryCoverage();
				if (coverage != null
					&& !coverage.isActionDelete()
					&& (coverage.hasProductCode() && coverage.getProductCode().trim().length() > 0)) { //SPR1945
					getPolicy().setProductCode(coverage.getProductCode());
					getPolicy().setActionUpdate();
				}
			}
			if (getPolicyProductForPlan() == null) {
				String productCode = "unknown";
				if (getPolicy().hasProductCode() && getPolicy().getProductCode().trim().length() > 0) { //SPR1945
					productCode = getPolicy().getProductCode();
				}
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Product Code: ", productCode), getIdOf(getPolicy()));
			}
		}
	}
	/**
	 * Verifies that RiderCode is present and that the corresponding plan definition business rules have been defined.
	 */
	protected void process_P027() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValBasic.process_P027()");//NBA103
			if (getPolicyProductFor(getRider()) == null) {
				String productCode = "unknown";
				if (getRider().hasRiderCode() && getRider().getRiderCode().trim().length() > 0) { //SPR1945
					productCode = getRider().getRiderCode();
				}
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Rider Code: ", productCode), getIdOf(getRider()));
			}
		}
	}
	/**
	 * Set FormNo (policy form) from PolicyProduct.FiledFormNumber.
	 */
	protected void process_P028() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBasic.process_P028()");//NBA103
			PolicyProduct policyProduct = getPolicyProductFor(getCoverage());
			if (policyProduct == null || !(policyProduct.hasFiledFormNumber() && policyProduct.getFiledFormNumber().trim().length() > 0)) { //SPR1945
				addPlanInfoMissingMessage("PolicyProduct.FiledFormNUmber", getIdOf(getCoverage()));
			} else {
				getCoverage().setFiledFormNumber(policyProduct.getFiledFormNumber());	//NBA100
				if (!getCoverage().hasFormNo()){	//NBA100
					getCoverage().setFormNo(policyProduct.getFiledFormNumber());	//NBA100
				}			
				getCoverage().setActionUpdate();
			}
		}
	}
	/**
	 * Set HoldingForm from PolicyProduct.PolicyProductForm.  
	 */
	protected void process_P029() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBasic.process_P029()");//NBA103
			PolicyProduct policyProduct = getPolicyProductForPlan();
			if (policyProduct == null || !policyProduct.hasPolicyProductForm()) {
				addPlanInfoMissingMessage("PolicyProduct.PolicyProductForm", getIdOf(getHolding()));
			} else {
				getHolding().setHoldingForm(policyProduct.getPolicyProductForm());
				getHolding().setActionUpdate();
			}
		}
	}
	/**
	 * Set Policy.LineOfBusiness from PolicyProduct.LineOfBusiness.//SPR2090
	 */
	protected void process_P030() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P030()");//NBA103
			PolicyProduct policyProduct = getPolicyProductForPlan();
			if (policyProduct == null || !policyProduct.hasLineOfBusiness()) {
				addPlanInfoMissingMessage("PolicyProduct.LineOfBusiness", getIdOf(getPolicy()));
			} else {
				getPolicy().setLineOfBusiness(policyProduct.getLineOfBusiness());
				getPolicy().setActionUpdate();
			}
		}
	}
	/**
	 * If IssueGender is not specified or different than Party.Person.Gender, use Party.Person.Gender.
	 * Validate IssueGender against table OLI_LU_GENDER. 
	 */
	protected void process_P031() {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValBasic.process_P031() for " , getLifeParticipant());//NBA103
			// begin SPR2148
			if (findParty(getLifeParticipant().getPartyID())) {
				if (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson()) {
					Person person = getParty().getPersonOrOrganization().getPerson();
					if (!getLifeParticipant().hasIssueGender() || getLifeParticipant().getIssueGender() != person.getGender()) {
						getLifeParticipant().setIssueGender(person.getGender());
			// end SPR2148
						getLifeParticipant().setActionUpdate();
					}
				}
			}
			long gender = getLifeParticipant().getIssueGender();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_GENDER, gender)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(),  " Value = " + String.valueOf(gender) + ".", getIdOf(getLifeParticipant()));//SPR1676
			}
		}
	}
	/**
	 * If IssueGender is not specified or different than Party.Person.Gender, use Party.Person.Gender.
	 * Validate IssueGender against table OLI_LU_GENDER. 
	 */
	protected void process_P032() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValBasic.process_P032() for " ,  getParticipant());//NBA103
			// begin SPR2148
			if (findParty(getParticipant().getPartyID())) {
				if (getParty().hasPersonOrOrganization() && getParty().getPersonOrOrganization().isPerson()) {
					Person person = getParty().getPersonOrOrganization().getPerson();
					if (!getParticipant().hasIssueGender() || getParticipant().getIssueGender() != person.getGender()) {
						getParticipant().setIssueGender(person.getGender());
			// end SPR2148
						getParticipant().setActionUpdate();
					}
				}
			}
			long gender = getParticipant().getIssueGender();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_GENDER, gender)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(),  " Value = " + String.valueOf(gender) + ".", getIdOf(getParticipant()));//SPR1677
			}
		}
	}
	/**
	 * Update Person.SmokerStat based on Party.Risk.TobaccoInd    
	 */
	//SPR1778 New Method
	protected void process_P034() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValBasic.process_P034() for " ,  getPerson());//NBA103
			if (!getPerson().hasSmokerStat()) {
				getPerson().setSmokerStat(OLI_TOBACCO_NEVER); //Default value
				Risk risk = getParty().getRisk();
				if (risk != null) {
					if (risk.getTobaccoInd()) {
						getPerson().setSmokerStat(OLI_TOBACCO_CURRENT);
					}
				}
				getPerson().setActionUpdate();
			}
		}
	}
	/**
	 * Invoke a VPMS model to verify that Contract signature is not missing based on company business rules.  
	 * Model returns 1 - True (not missing)   0 - False (missing)
	 */
	protected void process_P035() {
        if (verifyCtl(APPLICATIONINFO)) {
            logDebug("Performing NbaValBasic.process_P035()");//NBA103
            NbaVpmsResultsData nbaVpmsResultsData = performVpmsCalculation(); //SPR3047
            int answer = FALSE;	//SPR3047
            if (nbaVpmsResultsData != null) {
                if (nbaVpmsResultsData.wasSuccessful()) {
                      answer = Integer.parseInt((String) nbaVpmsResultsData.getResultsData().get(0)); //SPR3047
                }
            }
            if (answer != TRUE) {	//SPR3047
            	setGenerateRequirement(true); //AXAL3.7.40
                addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Contract Signature Missing", getIdOf(getApplicationInfo()));//SPR2439	
            }            
        }
    }
	/**
	 * Set the arrangement start date is equal to the contract EffDate (effective date) if set to issue indicator is checked.
	 */
	// SPR1466 New Method
	protected void process_P036() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValBasic.process_P036 for ", getArrangement());// NBA103
			//Start APSL5145
			if (!NbaUtils.isBlankOrNull(getArrangement().getProductCode()) && getArrangement().getProductCode().equals(NbaConstants.ATS)) {
				getArrangement().setStartDate(NbaUtils.addMonthsToDate(getPolicy().getEffDate(), 1));
				getArrangement().setActionUpdate(); 
			} else if (!getArrangement().hasStartDate() || getArrangement().getStartDate().before(getPolicy().getEffDate())) {  //End APSL5145
				getArrangement().setStartDate(getPolicy().getEffDate());
				getArrangement().setActionUpdate();
			}

		}
	}
	
	/**
	 * Verify Holding.LastAnniversaryDate is present
	 */
	//NBA077 New Method
	protected void process_P038() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValBasic.process_P038");//NBA103
			if (!getHolding().hasLastAnniversaryDate()) {
				addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Last Anniversary Process Date: ", getHolding().getLastAnniversaryDate()),
					getIdOf(getHolding()));
			}
		}
	}
	/**
	 * If a requested policy date (ApplicationInfo.RequestedPolDate) is present, set the policy effective date (Policy.EffDate) to the requested policy date.
	 */
	// SPR2251 New Method	
	protected void process_P039() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P039()");
			if (getApplicationInfo() != null && getApplicationInfo().hasRequestedPolDate()) {
				getPolicy().setEffDate(getApplicationInfo().getRequestedPolDate());
				getPolicy().setActionUpdate();
			}
		}
	}
	/**
	 * If a requested policy date (ApplicationInfo.RequestedPolDate) is present, set the coverage effective date 
	 * (Coverage.EffDate) to the requested policy date if the coverage is the primary coverage or if the effective 
	 * date is less than the requested policy date.
	 */
	// SPR2251 New Method	
	protected void process_P040() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValBasic.process_P040()");
			if (getApplicationInfo().hasRequestedPolDate()) {
				if (OLI_COVIND_BASE == getCoverage().getIndicatorCode()
					|| !getCoverage().hasEffDate()
					|| NbaUtils.compare(getCoverage().getEffDate(), getApplicationInfo().getRequestedPolDate()) < 0) {
					getCoverage().setEffDate(getApplicationInfo().getRequestedPolDate());
					getCoverage().setActionUpdate();
				}
			}
		}
	}
	/**
	 * If a requested policy date (ApplicationInfo.RequestedPolDate) is present, set the CovOption effective date 
	 * (CovOption.EffDate) to the requested policy date if the CovOption effective date is less than the requested policy date.
	 */
	// SPR2251 New Method	
	protected void process_P041() {
		if (verifyCtl(COVOPTION)) {
			logDebug("Performing NbaValBasic.process_P041()");
			if (getApplicationInfo().hasRequestedPolDate()) {
				if (!getCovOption().hasEffDate() || NbaUtils.compare(getCovOption().getEffDate(), getApplicationInfo().getRequestedPolDate()) < 0) {
					getCovOption().setEffDate(getApplicationInfo().getRequestedPolDate());
					getCovOption().setActionUpdate();
				}
			}
		}
	}
	/**
	 * If a requested policy date (ApplicationInfo.RequestedPolDate) is present, set the SubstandardRating effective date 
	 * (SubstandardRating.SubstandardRatingExtension.EffDate) to the requested policy date if the SubstandardRating effective date is less than the requested policy date.
	 */
	// SPR2251 New Method	
	protected void process_P042() {
		if (verifyCtl(SUBSTANDARDRATING)) {
			logDebug("Performing NbaValBasic.process_P042()");
			if (getApplicationInfo().hasRequestedPolDate()) {
				if (!getSubstandardRatingExtension().hasEffDate()
					|| NbaUtils.compare(getSubstandardRatingExtension().getEffDate(), getApplicationInfo().getRequestedPolDate()) < 0) {
					getSubstandardRatingExtension().setEffDate(getApplicationInfo().getRequestedPolDate());
					getSubstandardRatingExtension().setActionUpdate();
				}
				//NBLXA-2122 Begins
				if (getSubstandardRatingExtension().hasDuration()) {
					NbaUtils.setTempEndDate(getSubstandardRating(), getSubstandardRatingExtension().getEffDate(), getSubstandardRatingExtension().getDuration());
				}
				//NBLXA-2122 Ends
			}
		}
	} 
	/**
	 * If a requested policy date (ApplicationInfo.RequestedPolDate) is present, set the Rider effective date 
	 * (Rider.EffDate) to the requested policy date if the Rider effective date is less than the requested policy date.
	 */
	// SPR2251 New Method	
	protected void process_P043() {
		if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValBasic.process_P043()");
			if (getApplicationInfo().hasRequestedPolDate()) {
				if (!getRider().hasEffDate() || NbaUtils.compare(getRider().getEffDate(), getApplicationInfo().getRequestedPolDate()) < 0) {
					getRider().setEffDate(getApplicationInfo().getRequestedPolDate());
					getRider().setActionUpdate();
				}
			}
		}
	}
	/**
	 * Set the Closure Type and Pending ClosureDate for New/Formal Applications and Reg60 Nigo Cases
	 
	* NBA254 New Method validate new, and formal applications. 
	*Update Method for APSL2461 QC#9579, Handling Reg60 Nigo Cases.
	*Update method for APSL4140 SR#662330 to check Reg60 reopen case
	 * @throws ParseException 
	*/
	protected void process_P049() throws ParseException {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P049()");
			try {
				if (NbaUtils.isReg60Nigo(nbaTXLife)|| (NbaUtils.isReg60Reopen(nbaTXLife)))//APSL4140 SR#662330
					setPlacementEndDateForReg60Nigo();
				else
					setPlacementEndDateAndClosureType();
			} catch (NbaVpmsException nbe) {
				addNewSystemMessage(INVALID_VPMS_CALC, concat("Process: ", getNbaConfigValProc().getId(), ", Model: ",
						NbaVpmsAdaptor.REPLACEMENTS_PROCESSING), getIdOf(getPolicy()));
				getLogger().logError(nbe.getMessage());
			} catch (NbaBaseException nbe) {
				getLogger().logError(nbe.getMessage());
			}
		}
	}	
	
	/**
	 * Set the Closuer Type and Pending ClosureDate for Informal (Trial) Applications
	 * @throws NbaBaseException 
	 * @throws ParseException 
	 */
	// NBA254 New Method validate informal (trial) applications. Method updated for informal application to set the Placement End date with 
	// current date + PLACEMENT_END_DATE_DAYS attribute value in Nbaconfiguration 
	// set the date only when the case is Appvoed. i.e ApplicationInfoExtension.InformalAppApproval = 1000500001
	protected void process_P050() throws NbaBaseException, ParseException {//QC11621-APSL3588 added throws 
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P050()");
			//Begin ALS4822
			ApplicationInfoExtension appInfoExtn = NbaUtils.getFirstApplicationInfoExtension(getApplicationInfo());
			if (appInfoExtn != null){
				if(NbaUtils.isWholeSale(getPolicy()) || NbaUtils.isRetail(getPolicy())){ // ALS5041, NBLXA-1353-Removed isOfferMade condition for retail cases
					setPlacementEndDateAndClosureType(); //ALS4720
				}
			}
			//End ALS4822
		}
	}

	/**
	 * Set PendingContractStatus for Informal (Trial) Application.
	 * character 1 = 0 
	 * character 2 = 0
	 * 
	 * If contract Submitted for Closure 
	 * character 1 = 9
	 * 
	 * For non-issued policies
	 * character 3 = Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingApproval  
	 * character 4 = Holding.Policy.ApplicationInfo.ApplicationInfoExtension.UnderwritingStatus
	 * 
	 * For issued policies
	 * character 3 = 0
	 * character 4 = 1
	 */
	//NBA254 New Method
	protected void process_P051() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValBasic.process_P051()");
			//Begin ALS4707
			//If this is not a case, and Status is Await Review, return (because we may not have UNDQ on transactions)
			if (! getNbaDst().isCase() && getPolicy().getPolicyStatus() == NbaOliConstants.OLI_POLSTAT_1009800002) {
				return;
			}
			//If UNDQ LOB is not defined, set status to Await Review
			String undq = getNbaDst().getNbaLob().getUndwrtQueue();
			if (getNbaDst().isCase() && (NbaUtils.isBlankOrNull(undq) || undq == "-") && !NbaUtils.isNegativeDisposition(getNbaDst())) {//ALS5041 Status should not be reset on nigative disposed cases
				getPolicyExtension().setPendingContractStatus(NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_AWAIT_REVIEW);
				getPolicy().setPolicyStatus(NbaOliConstants.OLI_POLSTAT_1009800002); //ALS2001
				getPolicy().setActionUpdate(); //ALS2847
				getPolicyExtension().setActionUpdate();
				return;
			}
			//End ALS4707
			String char1 = "0";
			String char2 = "0";
			String char3 = "0";
			String char4 = "0";
			ApplicationInfo applicationInfo = getApplicationInfo();
			boolean informalOffer = false; //ALS4790
			if (applicationInfo != null && !applicationInfo.isActionDelete()) {
				ApplicationInfoExtension applicationInfoExtension = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
				if (applicationInfoExtension != null && !applicationInfoExtension.isActionDelete()) {
					//Begin ALS5022
					if (applicationInfoExtension.hasInformalAppApproval()
							&& applicationInfoExtension.getInformalAppApproval() == OLIX_INFORMALAPPROVAL_OFFERACCEPTED && !isAutoClosed()) {//APSL3551
						
						char1 = "9";
						char2 = "1";
						char3 = "1";
						char4 = "1";
					//End ALS5022
					} else if (applicationInfoExtension.hasInformalAppApproval() && !isAutoClosed()) { //ALS4790 ALS4832
						informalOffer = true; //ALS4790
						getPolicyExtension().setPendingContractStatus(String.valueOf(applicationInfoExtension.getInformalAppApproval()));
					} else { //ALS4790
						if (applicationInfoExtension.hasUnderwritingApproval()) {
							String undwrtApproval =	translateToBESValue(String.valueOf(applicationInfoExtension.getUnderwritingApproval()),	NbaTableConstants.OLIEXT_LU_UNDAPPROVAL); 
							if(null != undwrtApproval){
								char3 = undwrtApproval;
							}
							if (isAutoClosed()) { //ALS4832
								char1 = "9";
							}
						}
						if (NbaOliConstants.NBA_FINALDISPOSITION_OFFEREXPIRED == applicationInfoExtension.getUnderwritingStatus()) { //ALS4876
							char1 = "9";   //ALS4876
						}  //ALS4876
						if (applicationInfoExtension.hasUnderwritingStatus()) {
							//ALS2797 code deleted
							char4 = translateUnderwritingStatus(applicationInfoExtension.getUnderwritingStatus());  //ALS2797	
						}
						if ((OLI_POLSTAT_ISSUED == getPolicy().getPolicyStatus())
							&& (OLIX_UNDAPPROVAL_UNDERWRITER == applicationInfoExtension.getUnderwritingApproval())) {
							char3 = "0";
							char4 = "1";
						}
					} //ALS4790
				}
			}
			
			if (!informalOffer) { //ALS4790
				getPolicyExtension().setPendingContractStatus(concat(char1, char2, char3, char4));
			} //ALS4790
			
			getPolicyExtension().setActionUpdate();
		}
	}
	/*
	 * determine if case was autoclosed
	 */
	//ALS4832 new method
	private boolean isAutoClosed() {
		if (null != getApplicationInfoExtension() && getApplicationInfoExtension().getClosureInd() == NbaConstants.NEGATIVE_DISP_DONE) {
			return true;
		}
		return false;
	}
	
	/** ALS2797 Added new method
	 * This method will translate underwriting status into 1 character values.
	 *                27 -> 2  //Decline
	 *                23 -> 3  //Incomplete
	 *                29 -> 4  //Postponed
	 *                39 -> 5  //Cancelled
	 *        1009800001 -> 6  //Withdraw
	 *                 7 -> 7  //NTO
	 * @param int underwritingStatus
	 * @return true if the underwriting status is a negative disposition; false otherwise.
	 */
	// ALS2797 Added new method
	protected String translateUnderwritingStatus(long underwritingStatus) {
		String statusChar = "0";
		if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_DECLINED) {
			statusChar =  NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_DECLINE;
		} 
		else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_INCOMPLETE) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_INCOMPLETE;
		}
		else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_POSTPONED) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_POSTPONED;
		}
		else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_CANCELLED) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_CANCELLED;
		}
		else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_WITHDRAW) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_WITHDRAW;
		}
		else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_NTO) {
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_NTO;
		} else if (underwritingStatus == NbaOliConstants.NBA_FINALDISPOSITION_OFFEREXPIRED) { //ALS4790
			statusChar = NbaOliConstants.NBA_PENDINGCONTRACTSTATUS_CANCELLED; //ALS4790
		}  //ALS4790
		
		return statusChar;
	}
	
	/**
	 * Validate Reg60 Review.  IF pending or NIGO, add message
	 */
	//NBA231 new method
	protected void process_P052() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValBasic.process_P052()");
			ApplicationInfoExtension appInfoExt = NbaUtils.getFirstApplicationInfoExtension(applicationInfo);
			if (appInfoExt != null && !appInfoExt.isActionDelete()) {
				if (isReg60PendingOrNIGO(appInfoExt)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getApplicationInfo()));
				}
			}
		}
	}
	/*
	 * return true is Reg60Review is pending or NIGO
	 */
	//NBA231 new method
	protected boolean isReg60PendingOrNIGO(ApplicationInfoExtension appInfoExt ) {
		
		return appInfoExt.getReg60Review() == NbaOliConstants.NBA_REG60REVIEW_PENDING || 
				appInfoExt.getReg60Review() ==  NbaOliConstants.NBA_REG60REVIEW_NIGO;
	}
	
	
}
