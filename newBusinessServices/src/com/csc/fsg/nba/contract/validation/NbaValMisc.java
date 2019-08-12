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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.CreateException;

import com.csc.fs.accel.valueobject.AccelProduct;
import com.csc.fs.dataobject.accel.product.InvestProduct;
import com.csc.fsg.nba.bean.accessors.NbaProductAccessFacadeBean;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.tableaccess.NbaFundsData;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.ValProc;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.ArrDestination;
import com.csc.fsg.nba.vo.txlife.ArrSource;
import com.csc.fsg.nba.vo.txlife.Arrangement;
import com.csc.fsg.nba.vo.txlife.Banking;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Participant;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
import com.csc.fsg.nba.vo.txlife.SubAccount;
import com.csc.fsg.nba.vo.txlife.TaxWithholding;
/**
 * NbaValMisc performs Miscellaneous contract validation.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
 * <tr><td>SPR1774</td><td>Version 4</td><td>Validation error 6001 is generated twice when a government id and type code are missing.</td></tr>
 * <tr><td>SPR1818</td><td>Version 4</td><td>OtherInsuredInd getting set when there is no Other Insured defined.</td></tr>
 * <tr><td>NBA100</td><td>Version 4</td><td>Create Contract Print Extracts for new Business Documents</td></tr>
 * <tr><td>SPR1607</td><td>Version 4</td><td>Asset Reallocation (Rebalancing) Rework for Vantage</td></tr>
 * <tr><td>SPR1466</td><td>Version 4</td><td>Scheduled Withrawals BF rework </td></tr>
 * <tr><td>SPR1945</td><td>Version 4</td><td>Correct inconsistent contract validation edits for String values</td></tr>
 * <tr><td>SPR1994</td><td>Version 4</td><td>Correct user validation example </td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>SPR1234</td><td>Version 4</td><td>General source code clean up </td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>NBA Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR2247</td><td>Version 5</td><td>Target Exception in Miscellaneous Contract Validation on Contract Changes</td></tr>
 * <tr><td>SPR2811</td><td>Version 5</td><td>Doctor information on Requirements is being inserted into the NBA Pending Database</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>SPR3573</td><td>Version 8</td><td>Credit Card Information is not saved</td></tr>
 * <tr><td>SPR3375</td><td>Version 8</td><td>Credit Card Payment and Billing Information Not Displayed in Application Update</td></tr>
 * <tr><td>SPR2731</td><td>Version 8</td><td>Deny Person Does Not Correctly Remove All Coverages Causing an Exception on the View</td></tr>
 * <tr><td>AXAL3.7.40</td> <td>AXA Life Phase 1</td><td>Contract Validation for Biling Subset</td></tr>
 * <tr><td>AXAL3.7.18</td><td>AXA Life Phase 1</td><td>Producer Interfaces</td></tr>
 * <tr><td>NBA237</td><td>Version 8</td><td>Migrate Policy Product Transmittal XML1201 to 2.15.00</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * <tr><td>SR494086</td>Discretionary<td></td><td>ADC Retrofit</td></tr>
 * <tr><td>P2AXAL054</td><td>AXA Life Phase 2</td><td>Omissions and Contract Validations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaValMisc extends NbaContractValidationCommon implements NbaContractValidationBaseImpl, NbaContractValidationImpl, NbaOliConstants { //SPR1234
	protected NbaContractValidationImpl userImpl;
	/**
	 * Perform one time initialization.
	 */
	 //NBA237 change method signaure
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

	//NBA103 - removed getLogger()

	// SPR1994 code deleted
	/**
	 * @see com.csc.fsg.nba.contract.validation.NbaContractValidationImpl#validate()
	 */
	// ACN012 changed signature
	public void validate(ValProc nbaConfigValProc, ArrayList objects) {
		if (nbaConfigValProc.getUsebase()) { //ACN012
			super.validate(nbaConfigValProc, objects);
		} else { //ALS2600
		    if (getUserImplementation() != null) {
		        getUserImplementation().validate(nbaConfigValProc, objects);
		    }
		} //ALS2600    
	}
	/**
	 * Verify existance of a first name and last name values when the party is a person.
	 * Update Person.FullName to be '%L, %F %M, %S' where %L is LastName, %F is FirstName, %M is MiddleName and %S is Suffix
	 */
	protected void process_P001() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValMisc.process_P001() for " ,  getParty()); //NBA103
			// BEGIN SPR2811
			if (isPhysician(getParty().getId()) || isPaymentFacilitator(getParty().getId())) { //SPR3375
				return;
			}
			// END SPR2811
			String last = "";
			String first = "";
			String mid = "";
			String suff = "";
			if (getPerson().hasLastName()) {
				last = getPerson().getLastName().trim();
			}
			if (getPerson().hasFirstName()) {
				first = getPerson().getFirstName().trim();
			}
			if (getPerson().hasMiddleName()) {
				mid = getPerson().getMiddleName().trim();
			}
			if (getPerson().hasSuffix()) {
				suff = getPerson().getSuffix().trim();
			}
			getParty().setFullName(concat(last, ", ", first, ""));
			if (mid.length() > 0) {
				getParty().setFullName(concat(getParty().getFullName(), " ", mid, ""));
			}
			if (suff.length() > 0) {
				getParty().setFullName(concat(getParty().getFullName(), ",  ", suff, ""));
			}
			getParty().setActionUpdate();
			if (first.length() < 1 || last.length() < 1) {
				setGenerateRequirement(true); //AXAL3.7.40
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("First Name: ", first, ", Last Name: ", last), getIdOf(getParty()));
			}			
		}
	}
	/**
	 * Verify FirstName value does not exceed 19 characters.
	 */
	protected void process_P909() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValMisc.process_P909() for " ,  getParty()); //NBA103
			String first = "";
			if (getPerson().hasFirstName()) {
				first = getPerson().getFirstName().trim();
			}
			if (first.length() > 19) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("First Name: ", first), getIdOf(getParty()));
			}
		}
	}
	/**
	 * Verify MiddleName value does not exceed 1 character.
	 */
	protected void process_P910() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValMisc.process_P910() for " ,  getParty()); //NBA103
			String mid = "";
			if (getPerson().hasMiddleName()) {
				mid = getPerson().getMiddleName().trim();
			}
			if (mid.length() > 1) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Middle Name: ", mid), getIdOf(getParty()));
			}
		}
	}
	/**
	 * Verify LastName value does not exceed 19 characters.
	 */
	protected void process_P911() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValMisc.process_P911() for " ,  getParty()); //NBA103
			String last = "";
			if (getPerson().hasLastName()) {
				last = getPerson().getLastName().trim();
			}
			if (last.length() > 19) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Last Name: ", last), getIdOf(getParty()));
			}
		}
	}
	/**
	 * Verify First Name + MiddleName value length does not exceed 20 characters.
	 */
	protected void process_P912() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValMisc.process_P912() for " ,  getParty()); //NBA103
			String first = "";
			String mid = "";
			if (getPerson().hasFirstName()) {
				first = getPerson().getFirstName().trim();
			}
			if (getPerson().hasMiddleName()) {
				mid = getPerson().getMiddleName().trim();
			}
			if (first.length() + mid.length() > 20) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("First Name: ", first, ", Middle Name: ", mid), getIdOf(getParty()));
			}
		}
	}
	/**
	 * Verify LastName value does not exceed 35 characters.
	 */
	protected void process_P913() {
		if (verifyCtl(PERSON)) {
			logDebug("Performing NbaValMisc.process_P913() for " ,  getParty()); //NBA103
			String last = "";
			if (getPerson().hasLastName()) {
				last = getPerson().getLastName().trim();
			}
			if (last.length() > 35) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Last Name: ", last), getIdOf(getParty()));
			}
		}
	}
	/**
	 * Verify existence of a government ID.
	 * if SSN is blank generate severe error message 6922: "" Application question omitted - SSN Omitted for Insured/Owner.
	 */
	protected void process_P002() {
		if (verifyCtl(PARTY)) {
			logDebug("Performing NbaValMisc.process_P002() for ", getParty()); //NBA103
			// QC11824 APSL3107 
			ApplicationInfoExtension appinfoext = NbaUtils.getFirstApplicationInfoExtension(getNbaTXLife().getPolicy().getApplicationInfo());
			boolean ind = true;//If indicator is true we shall check for GOvt ID
			if (appinfoext != null && appinfoext.getIUPOverrideInd()) {// If IUP Override Indicator is set true , then check for Govt ID
				ind = true;
			} else if (!NbaUtils.isIUPParty(getParty())) {//If party is not IUP Party then check for Govt ID
				ind = true;
			} else {
				ind = false;
			}
			if (ind && (!getParty().hasGovtID() || !(getParty().getGovtID().trim().length() == 9))) { //SPR1945
				setGenerateRequirement(true); //AXAL3.7.40
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), " - " + getTranslatedRole(getParty()), getIdOf(getParty())); //AXAL3.7.40 //P2AXAL054
			}
		}
	}
	/**
	 * Validate GovtIDTC (government ID type) against table OLI_LU_GOVTIDTC. Used to determine if the ID is a social security number or a tax id.
	 */
	protected void process_P003() {
		if (verifyCtl(PARTY)) {
			logDebug("Performing NbaValMisc.process_P003() for " ,  getParty()); //NBA103
			//begin SPR1774
			if (getParty().hasGovtID() && (getParty().getGovtID().trim().length() == 9)) { //SPR1945
				long govtIDTC = getParty().getGovtIDTC();
				if (!isValidTableValue(NbaTableConstants.OLI_LU_GOVTIDTC, govtIDTC)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Government ID Type: " + govtIDTC + " - " + getTranslatedRole(getParty()), getIdOf(getParty()));//P2AXAL054
				}
				//end SPR1774
			}
		}
	}
	/**
	 * Verify existence of a DBA  (corporation/organization name) value when the party type is an organization.
	 */
	protected void process_P004() {
		if (verifyCtl(ORGANIZATION)) {
			logDebug("Performing NbaValMisc.process_P004() for " ,  getOrganization());	//NBA103
			if (!getOrganization().hasDBA() || getOrganization().getDBA().trim().length() < 1) { //SPR1945
				Relation relation = getNbaTXLife().getRelationByRelatedId(getParty().getId()); //QC2614 //ALS4331
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), getTranslatedRole(Long.toString(relation.getRelationRoleCode())),
						getIdOf(getParty())); //QC2614 ALS5248
			}
		}
	}
	/**
	 * Verify that at least one beneficiary has been identified for an insured by verifying that there is 
	 * at least one other Relation present with a beneficiary role whose OriginatingObjectID matches the 
	 * RelatedObjectID of this Relation.  
	 * For Vantage, the OriginatingOjbectID is the associated coverage.  
	 * For CyberLife, the OriginatingOjbectID is the associated party. 
	 * 
	 * If missing, generate the severe AXA message 6917: 'Application question omitted -  Primary Beneficiary."	
	 * Provide the ability to validate for missing primary beneficiary.
	*/
	protected void process_P005() {
		if (verifyCtl(RELATION)) {
			logDebug("Performing NbaValMisc.process_P005() for ", getRelation()); //NBA103
			String insuredId = getRelation().getRelatedObjectID();
			if (isSystemIdCAPS()) { //AXAL3.7.40
				int relCount = getNbaTXLife().getOLifE().getRelationCount();
				boolean missing = true;
				for (int i = 0; i < relCount; i++) {
					Relation relation = getNbaTXLife().getOLifE().getRelationAt(i);
					if (!relation.isActionDelete() && relation.getRelationRoleCode() == OLI_REL_BENEFICIARY) {
						if (relation.getOriginatingObjectID().equals(insuredId)) {
							missing = false;
							break;
						}
					}
				}
				//Begin SR494086.2 ADC Retrofit
				if (getApplicationInfoExtension() != null && getApplicationInfoExtension().getDefaultBeneficiary()) {
					missing = false;
				}
				//End SR494086.2
				if (missing) {
					setGenerateRequirement(true); //AXAL3.7.40.28
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Primary Beneficiary", getIdOf(getRelation())); //AXAL3.7.40
				}
			} else if (isSystemIdVantage()) {
				boolean continueProcess = true;
				int idx = 0;
				while (continueProcess) {
					Coverage coverage = findCoverageForId(insuredId, idx++, true);//SPR2731
					if (coverage == null) {
						continueProcess = false;
					} else {
						Relation relation = findBeneficiaryToCovRelation(coverage.getId());
						if (relation == null) {
							addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Primary Beneficiary", getIdOf(getRelation())); //AXAL3.7.40
						}
					}
				}
			}
		}
	}
	/**
	* Verifies presence of a life participant with a beneficiary role 
	*/
	protected void process_P006() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValMisc.process_P006() for " ,  getCoverage());	//NBA103
			for (int j = 0; j < getCoverage().getLifeParticipantCount(); j++) {
				if (!getCoverage().getLifeParticipantAt(j).isActionDelete()) {
					if (NbaUtils.isBeneficiaryParticipant(getCoverage().getLifeParticipantAt(j))) {
						return;
					}
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "No beneficiary role present.", getIdOf(getCoverage()));
		}
	}
	/**
	* Verifies presence of a participant with a beneficiary role 
	*/
	protected void process_P007() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValMisc.process_P007() for "  , getPayout()); //NBA103
			for (int j = 0; j < getPayout().getParticipantCount(); j++) {
				Participant participant = getPayout().getParticipantAt(j);
				if (!participant.isActionDelete()) {
					if (NbaUtils.isBeneficiaryParticipant(participant)) {
						return;
					}
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "No beneficiary role present.", getIdOf(getPayout()));
		}
	}
	/**
	 * Verifies presence of a relation with owner role on contract. If not found,
	 * create one from the first found of Payor, then Primary Insured or annuitant person.
	 */
	protected void process_P010() {
		logDebug("Performing NbaValMisc.process_P010"); //NBA103
		if (getOwnerRelation() == null) {
			setRelation(getPayerRelation());
			if (getRelation() == null) {
				setRelation(getPrimaryInsOrAnnuitantRelation().clone(false));
			}
			if (getRelation() == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", "");
			} else {
				createRelation(getRelation(), OLI_REL_OWNER);
			}
		}
	}
	/**
	 * Verifies presence of a relation with a payor role on the contract.   
	 * If not found, then create one from the primary insured or annuitant person.
	 */
	protected void process_P011() {
		logDebug("Performing NbaValMisc.process_P011()"); //NBA103
		if (getPayerRelation() == null) {
			setRelation(getPrimaryInsOrAnnuitantRelation());
			if (getRelation() == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", "");
			} else {
				createRelation(getRelation(), OLI_REL_PAYER);
			}
		}
	}
	/**
	 * Verify presence minimal address information: Line1 (street or P.O. Box), City, AddressState, Zip
	 * 
	 * "Verify for Party = Owner if other than the Proposed Insured, for TXLIfe.TXLifeRequest.OLifE.Relation.RelationRoleCode = ""8"", address type
	 * must be MAILING <TXLIfe.TXLifeRequest.OLifE.Party.Address.AddressTypeCode>='17' (mailing). If MAILING address type is missing generate severe
	 * message 6902: Owner's mailing address/zipcode omitted. Error will prevent contract print. 
	 * Provide the ability to validate when Owner's mailing address, Zip code is omitted.
	 */
	protected void process_P012() {
		if (verifyCtl(PARTY)) {
			logDebug("Performing NbaValMisc.process_P012() for ", getParty()); //NBA103
			if (getParty().getAddressCount() < 1) {
				setGenerateRequirement(true); //AXAL3.7.40.13
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getParty()));
			} else {
				Address mailingAddress = null;
				for (int i = 0; i < getParty().getAddressCount(); i++) {
					mailingAddress = getParty().getAddressAt(i);
					if (mailingAddress.getAddressTypeCode() == OLI_ADTYPE_MAILING) {
						break;
					}
				}
				if (!NbaUtils.isValidAddress(mailingAddress)) { //APSL563
					setGenerateRequirement(true); //AXAL3.7.40.13
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getParty()));
				}
			}
		}
	}
	/**
	 * Set Policy.BeneficiaryInd and Policy.PolicyExtension.BeneficiaryInd based on whether one or more beneficiaries are present on the contract.
	 */
	protected void process_P013() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValMisc.process_P013()"); //NBA103
			boolean found = false;
			ArrayList relations = getNbaTXLife().getOLifE().getRelation();
			for (int i = 0; i < relations.size(); i++) {
				Relation relation = (Relation) relations.get(i);
				if (!relation.isActionDelete()) {
					found = NbaUtils.isBeneficiaryRelationRoleCode(relation.getRelationRoleCode());
					if (found) {
						break;
					}
				}
			}
			getPolicy().setBeneficiaryInd(found);
			getPolicyExtension().setBeneficiaryInd(found);
			getPolicy().setActionUpdate();
			getPolicyExtension().setActionUpdate();
		}
	}
	/**
	* Set Policy.OtherInsuredInd and Policy.PolicyExtension.OtherInsuredInd to "1" based on whether one or more other insured present on the contract.  
	*/
	protected void process_P014() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValMisc.process_P014()"); //NBA103
			getPolicy().setOtherInsuredInd(hasCoverageOrRiderForOtherInsured());
			getPolicyExtension().setOtherInsuredInd(getPolicy().getOtherInsuredInd());	//SPR1818
			getPolicy().setActionUpdate();
			getPolicyExtension().setActionUpdate();
		}
	}
	/**
	* Verify total beneficiary distribution percent equals 100. 
	*/
	protected void process_P015() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValMisc.process_P015() " , getCoverage());	//NBA103
			double totalPct = 0;
			int count = getCoverage().getLifeParticipantCount();
			for (int j = 0; j < count; j++) {
				LifeParticipant lifeParticipant = getCoverage().getLifeParticipantAt(j);
				if (!lifeParticipant.isActionDelete()) {
					if (NbaUtils.isBeneficiaryParticipant(lifeParticipant)) {
						if (lifeParticipant.hasBeneficiaryPercentDistribution()) {
							totalPct += lifeParticipant.getBeneficiaryPercentDistribution();
						}
					}
				}
			}
			if (totalPct != 100) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Percentage: ", totalPct), getIdOf(getCoverage()));
			}
		}
	}
	/**
	 * Verifies presence of primary insured role (tc="1") Life Participant.
	 */
	protected void process_P901() {
		if (verifyCtl(COVERAGE)) {
			logDebug("Performing NbaValMisc.process_P901() for " ,  getCoverage());	//NBA103
			int idx = 0;
			while (findNextLifeParticipant(idx++)) {
				if (!getLifeParticipant().isActionDelete() && getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_PRIMARY) {
					return;
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getCoverage()));
		}
	}
	/**
	 * Verifies presence of Annuitiant role (tc="27") Participant
	 */
	protected void process_P902() {
		if (verifyCtl(PAYOUT)) { //Make sure we are pointing to a Payout object
			logDebug("Performing NbaValMisc.process_P902 for " , getPayout());	//NBA103
			int idx = 0;
			while (findNextParticipant(idx++)) {
				if (!getLifeParticipant().isActionDelete() && getLifeParticipant().getLifeParticipantRoleCode() == OLI_PARTICROLE_ANNUITANT) {
					return;
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPayout()));
		}
	}
	/**
	 * Verifies presence of birthdate for a LifeParticipant
	 */
	protected void process_P903() {
		if (verifyCtl(LIFEPARTICIPANT)) { //Make sure we are pointing to a LifeParticipant object
			logDebug("Performing process_P903 for " + getLifeParticipant().getPartyID()); //NBA103
			if (findParty(getLifeParticipant().getPartyID())) {
				if (getParty().hasPersonOrOrganization() && getNbaParty().isPerson()) {
					setPerson(getParty().getPersonOrOrganization().getPerson());
					if (isValidDate(getPerson().getBirthDate()) && isNotFutureDated(getPerson().getBirthDate())) {
						return;
					}
				} else {
					return;
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getLifeParticipant().getPartyID());
		}
	}
	/**
	* Verifies presence of birthdate for a Participant 
	*/
	protected void process_P904() {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValMisc.process_P904 for " + getParticipant().getPartyID()); //NBA103
			if (findParty(getParticipant().getPartyID())) {
				if (getParty().hasPersonOrOrganization() && getNbaParty().isPerson()) {
					setPerson(getParty().getPersonOrOrganization().getPerson());
					if (!getPerson().isActionDelete() && isValidDate(getPerson().getBirthDate()) && isNotFutureDated(getPerson().getBirthDate())) {
						return;
					}
				} else {
					return;
				}
			}
			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getParticipant().getPartyID());
		}
	}
	/**
	* Verify total beneficiary distribution percent equals 100 for a Payout  
	*/
	protected void process_P016() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValMisc.process_P016 for " , getPayout());	//NBA103
			double totalPct = 0;
			int count = getPayout().getParticipantCount();
			for (int j = 0; j < count; j++) {
				Participant participant = getPayout().getParticipantAt(j);
				if (!participant.isActionDelete()) {
					if (NbaUtils.isBeneficiaryParticipant(participant)) {
						if (participant.hasBeneficiaryPercentDistribution()) {
							totalPct += participant.getBeneficiaryPercentDistribution();
						}
					}
				}
			}
			if (totalPct != 100) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Percentage: ", totalPct), getIdOf(getPayout()));
			}
		} else if (verifyCtl(RIDER)) {
			logDebug("Performing NbaValMisc.process_P016 for " , getRider());	//NBA103
			double totalPct = 0;
			int count = getRider().getParticipantCount();
			for (int j = 0; j < count; j++) {
				Participant participant = getRider().getParticipantAt(j);
				if (!participant.isActionDelete()) {
					if (NbaUtils.isBeneficiaryParticipant(participant)) {
						if (participant.hasBeneficiaryPercentDistribution()) {
							totalPct += participant.getBeneficiaryPercentDistribution();
						}
					}
					//					}
				}
			}
			if (totalPct != 100) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Percentage: ", totalPct), getIdOf(getRider()));
			}
		}
	}
	/**
	* Verify that a BeneficiaryDistributionOption has been specified. 
	* Set to unknown (tc=0)if invalid.
	*/
	protected void process_P017() {
		if (verifyCtl(RELATION)) { //Make sure we are pointing to a relation object
			logDebug("Performing NbaValMisc.process_P017 for " , getRelation()); //NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_DISTOPTION, getRelationExtension().getBeneficiaryDistributionOption())) {
				getRelationExtension().setBeneficiaryDistributionOption(OLI_UNKNOWN);
				getRelationExtension().setActionUpdate();
			}
		}
	}
	/**
	* Verify total beneficiary  distribution InterestPercent equals 100 when the distribution option of the insured is a percentage.
	* For Vantage, the OriginatingOjbectID is the associated coverage.  For CyberLife, it is the associated party. 
	*/
	protected void process_P018() {
		if (verifyCtl(RELATION)) {
			//AXAL3.7.40 Code deleted
			logDebug("Performing NbaValMisc.process_P018 for " , getRelation()); //NBA103
			String insuredId = getRelation().getRelatedObjectID();
			ArrayList relations = getNbaTXLife().getOLifE().getRelation();
			RelationExtension relationExtension;
			if (isSystemIdCyberLife() || isSystemIdCAPS()) {//AXAL3.7.40
				double totalPct = 0;
				double totalPctCont = 0; //ALS3783
				boolean pBene = false;
				boolean cBene = false;
				for (int i = 0; i < relations.size(); i++) {
					Relation relation = (Relation) relations.get(i);
					if (!relation.isActionDelete()) {
						if (NbaUtils.isBeneficiaryRelationRoleCode(relation.getRelationRoleCode())) {
							if (relation.getOriginatingObjectID().equals(insuredId)) {
								//Begin ALS3783
								if (NbaOliConstants.OLI_REL_BENEFICIARY == relation.getRelationRoleCode()) {
									pBene = true;
									totalPct += relation.getInterestPercent();
								} else if (NbaOliConstants.OLI_REL_CONTGNTBENE == relation.getRelationRoleCode()) {
									cBene = true;
									totalPctCont += relation.getInterestPercent();
								}//End ALS3783
								relationExtension = getRelationExtension(relation);
								relationExtension.setBeneficiaryDistributionOption(OLI_DISTOPTION_PERCENT);
								relationExtension.setActionUpdate();
							}
						}
					}
				}
				if (pBene && totalPct != 100) {
					setGenerateRequirement(true); //AXAL3.7.40
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Percentage: ", totalPct), getIdOf(getRelation()));
				}
				//Begin ALS3783
				if (cBene && totalPctCont != 100) {
					setGenerateRequirement(true);
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Percentage: ", totalPctCont), getIdOf(getRelation()));
				}//End ALS3783

			} else if (isSystemIdVantage()) {
				// SPR3290 code deleted
				int idx = 0;
				Coverage coverage;
				while ((coverage = findCoverageForId(insuredId, idx++, true)) != null) {//SPR2731
					double totalPct = 0;
					String coverageId = coverage.getId();
					for (int i = 0; i < relations.size(); i++) {
						Relation relation = (Relation) relations.get(i);
						if (!relation.isActionDelete()) {
							if (NbaUtils.isBeneficiaryRelationRoleCode(relation.getRelationRoleCode())) {
								if (relation.getOriginatingObjectID().equals(coverageId)) {
									totalPct += relation.getInterestPercent();
									relationExtension = getRelationExtension(relation);
									relationExtension.setBeneficiaryDistributionOption(OLI_DISTOPTION_PERCENT);
									relationExtension.setActionUpdate();
								}
							}
						}
					}
					if (totalPct != 100) {
						setGenerateRequirement(true); //AXAL3.7.40
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Percentage: ", totalPct), getIdOf(getRelation()));
					}
				}
			}			
		}
	}
	
	/**
	* Verify that a flat InterestAmount has been specified for each beneficiary 
	* when the distribution option of the insured is a flat amount (tc=5).  
	*/
	protected void process_P019() {
		if (verifyCtl(RELATION)) {
			logDebug("Performing NbaValMisc.process_P019 for " , getRelation()); //NBA103
			if (getRelationExtension().getBeneficiaryDistributionOption() == OLI_DISTOPTION_FLATAMT) {
				String insuredId = getRelation().getRelatedObjectID();
				ArrayList relations = getNbaTXLife().getOLifE().getRelation();
				RelationExtension relationExtension;
				if (isSystemIdCyberLife()) {
					for (int i = 0; i < relations.size(); i++) {
						Relation relation = (Relation) relations.get(i);
						if (!relation.isActionDelete()) {
							if (NbaUtils.isBeneficiaryRelationRoleCode(relation.getRelationRoleCode())) {
								if (relation.getOriginatingObjectID().equals(insuredId)) {
									relationExtension = getRelationExtension(relation);
									if (!(relationExtension.hasInterestAmount() && relationExtension.getInterestAmount() > 0)) {
										addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(relation));
									}
									relationExtension.setBeneficiaryDistributionOption(OLI_DISTOPTION_FLATAMT);
									relationExtension.setActionUpdate();
								}
							}
						}
					}
				} else if (isSystemIdVantage()) {
					// SPR3290 code deleted
					int idx = 0;
					Coverage coverage;
					while ((coverage = findCoverageForId(insuredId, idx++, true)) != null) {//SPR2731
						String coverageId = coverage.getId();
						for (int i = 0; i < relations.size(); i++) {
							Relation relation = (Relation) relations.get(i);
							if (!relation.isActionDelete()) {
								if (NbaUtils.isBeneficiaryRelationRoleCode(relation.getRelationRoleCode())) {
									if (relation.getOriginatingObjectID().equals(coverageId)) {
										relationExtension = getRelationExtension(relation);
										if (!(relationExtension.hasInterestAmount() && relationExtension.getInterestAmount() > 0)) {
											addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(relation));
										}
										relationExtension.setBeneficiaryDistributionOption(OLI_DISTOPTION_FLATAMT);
										relationExtension.setActionUpdate();
									}
								}
							}
						}
					}
				}
			}
		}
	}
	/**
	* Validate ProxyType against table OLIEXT_LU_PROXYTYPE.  
	*/
	protected void process_P020() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValMisc.process_P020"); //NBA103
			long proxyType = getPolicyExtension().getProxyType();
			if (!isValidTableValue(NbaTableConstants.OLIEXT_LU_PROXYTYPE, proxyType)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Proxy Type: ", proxyType), getIdOf(getPolicy()));
			}
		}
	}
	/**
	* Verify presence of a ProxyClientNumber if the Proxy Type is List (tc=2).  
	*/
	protected void process_P021() {
		if (verifyCtl(POLICY)) {
			logDebug("Performing NbaValMisc.process_P021"); //NBA103
			if (getPolicyExtension().getProxyType() == OLIX_PROXYTYPE_LIST) {
				if (!(getPolicyExtension().hasProxyClientNumber() && getPolicyExtension().getProxyClientNumber().trim().length() > 0)) { //SPR1945
					addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concat("Proxy Client Number: ", getPolicyExtension().getProxyClientNumber()),
						getIdOf(getPolicy()));
				}
			}
		}
	}
	/**
	* Validate requested IncomeOption (settlement option) against table OLI_LU_INCOPTION. 
	* If none requested then set based on plan business rules.  
	*/
	protected void process_P022() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValMisc.process_P022 for " , getPayout());	//NBA103
			if (!getPayout().hasIncomeOption()) {
				getPayout().setIncomeOption(1);
			}
			if (!isValidTableValue(NbaTableConstants.OLI_LU_INCOPTION, getPayout().getIncomeOption())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Settlement Option: ", getPayout().getIncomeOption()), getIdOf(getPayout()));
			}
		}
	}
	/**
	* Verify than an ArrType (arrangement type) has been specified an is valid based on plan business rules. 
	*/
	protected void process_P023() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P023 for " , getArrangement()); //NBA103
			long type = getArrangement().getArrType();
			//P2AXAL016 Code by SPR1607 Deleted 
			if (!isValidTableValue(NbaTableConstants.OLI_LU_ARRTYPE, type)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Arrangement Type: ", type), getIdOf(getArrangement()));
			}
		}
	}
	/**
	* Verify that there is no more than one arrangement of the same ArrType.   
	*/
	protected void process_P024() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValMisc.process_P024 for " , getHolding()); //NBA103
			Map types = new HashMap();
			for (int i = 0; i < getHolding().getArrangementCount(); i++) {
				String type = Long.toString(getHolding().getArrangementAt(i).getArrType());
				if (types.containsKey(type)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getHolding()));
					break;
				} else {
					types.put(type, type);
				}
			}
		}
	}
	/**
	* Verify that there are no conflicting arrangement types.  These types are not allowed in combination: 7,8,9,13,14, 42,43,44,45 and 46.  
	*/
	protected void process_P914() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValMisc.process_P914 for " ,  getHolding()); //NBA103
			boolean anotherExists = false;
			for (int i = 0; i < getHolding().getArrangementCount(); i++) {
				long type = getHolding().getArrangementAt(i).getArrType();
				if (type == OLI_ARRTYPE_SPECAMTNETWITH
					|| type == OLI_ARRTYPE_SPECAMTGROSSWITH
					|| type == OLI_ARRTYPE_SURRFREEWITH
					|| type == OLI_ARRTYPE_REQMINWITH
					|| type == OLI_ARRTYPE_PCTVALWITH
					|| type == 42
					|| type == 43
					|| type == 44
					|| type == 45
					|| type == 46) {
					if (anotherExists) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getHolding()));
						break;
					} else {
						anotherExists = true;
					}
				}
			}
		}
	}
	/**
	* Verify that there are no conflicting arrangement types.  These types are not allowed in combination: 7,8,10,14 and 19.  
	*/
	protected void process_P915() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValMisc.process_P915 for " , getHolding()); //NBA103
			boolean anotherExists = false;
			for (int i = 0; i < getHolding().getArrangementCount(); i++) {
				long type = getHolding().getArrangementAt(i).getArrType();
				if (type == OLI_ARRTYPE_SPECAMTNETWITH
					|| type == OLI_ARRTYPE_SPECAMTGROSSWITH
					|| type == OLI_ARRTYPE_INTNETWITH
					|| type == OLI_ARRTYPE_PCTVALWITH
					|| type == 19) {
					if (anotherExists) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getHolding()));
						break;
					} else {
						anotherExists = true;
					}
				}
			}
		}
	}
	
	/**
	 * Verify presence of an arrangement StartDate. Verify StartDate is greater than or equal to the contract EffDate (effective date). If StartDate
	 * is missing or invalid, set equal to the contract EffDate.
	 */
	protected void process_P025() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P025 for ", getArrangement()); // NBA103
			//Start APSL5145
			if (!NbaUtils.isBlankOrNull(getArrangement().getProductCode()) && getArrangement().getProductCode().equals(NbaConstants.ATS)) {
				getArrangement().setStartDate(NbaUtils.addMonthsToDate(getPolicy().getEffDate(), 1));// APSL5145
				getArrangement().setActionUpdate();
			} else if (!getArrangement().hasStartDate() || getArrangement().getStartDate().before(getPolicy().getEffDate())) { //End APSL5145
				getArrangement().setStartDate(getPolicy().getEffDate());
				getArrangement().setActionUpdate();
			}
		}
	}

	/**
	* Verify presence of a payout Start Date.  Verify StartDate is greater than or equal to the contract EffDate. 
	* If StartDate is missing or invalid, set equal to the contract EffDate.  
	*/
	protected void process_P026() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValMisc.process_P026 for " , getPayout());	//NBA103
			if (getPolicy().hasEffDate()) {
				if (!getPayout().hasStartDate() || getPayout().getStartDate().before(getPolicy().getEffDate())) {
					getPayout().setStartDate(getPolicy().getEffDate());
					getPayout().setActionUpdate();
				}
			}
		}
	}
	/**
	* Verify presence of an arrangement EndDate.  Verify CeaseDate is less than or equal to the contract TermDate (maturity date). 
	* If EndDate is missing or invalid, set equal to the contract TermDate.   
	*/
	protected void process_P027() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P027 for " , getArrangement()); //NBA103
			if (getPolicy().hasTermDate()) {
				if (!getArrangement().hasEndDate() || getArrangement().getEndDate().after(getPolicy().getTermDate())) {
					getArrangement().setEndDate(getPolicy().getTermDate());
					getArrangement().setActionUpdate();
				}
			}
		}
	}
	/**
	* For Dollar Cost Averaging (ArrType="2") arrangement, verify the presence of either an arrangement 
	* EndDate or NumberOfTransfers, but not both.  
	* If EndDate is present, verify that EndDate is less than or equal to the contract TermDate.    
	*/
	protected void process_P028() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P028 for " , getArrangement()); //NBA103
			boolean error = false;
			long transferNumber = 0;
			if (getArrangementExtension().hasTransferNumber() && getArrangementExtension().getTransferNumber().trim().length() > 0) { //SPR1945
				transferNumber = new Long(getArrangementExtension().getTransferNumber().trim()).longValue(); //SPR1945
			}
			if (getArrangement().hasEndDate() && transferNumber > 0) { //Both present
				error = true;
			} else if (!getArrangement().hasEndDate() && !(transferNumber > 0)) { //Neither present
				error = true;
			} else if (getArrangement().hasEndDate() && getArrangement().getEndDate().after(getPolicy().getTermDate())) {
				error = true;
			}
			if (error) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement()));
			}
		}
	}
	/**
	* For all arrangements other than Dollar Cost Averaging (ArrType="2"), verify the presence of an 
	* arrangement EndDate.  Verify EndDate is less than or equal to the contract TermDate (maturity date). 
	* If EndDate is missing or invalid, set equal to the contract TermDate.  
	*/
	protected void process_P029() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P029 for " , getArrangement()); //NBA103
				if (!getArrangement().hasEndDate() || 
					getArrangement().getEndDate().after(getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity().getRequestedMaturityDate())) { //NBA107
					getArrangement().setEndDate(getPolicy().getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty().getAnnuity().getRequestedMaturityDate()); //NBA107
					getArrangement().setActionUpdate();
				}
		}
	}
	/**
	* Validate ArrMode (arrangement frequency) against table OLI_LU_PAYMODE.  
	*/
	protected void process_P030() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P030 for " , getArrangement()); //NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_PAYMODE, getArrangement().getArrMode())) {
				addNewSystemMessage(
					getNbaConfigValProc().getMsgcode(),
					concat("Arrangement Frequency: ", getArrangement().getArrMode()),
					getIdOf(getArrangement()));
			}
		}
	}
	/**
	* Validate PayoutMode against table OLI_LU_PAYMODE. 
	*/
	protected void process_P031() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValMisc.process_P031 for " , getPayout());	//NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_PAYMODE, getPayout().getPayoutMode())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Payout Mode: ", getPayout().getPayoutMode()), getIdOf(getPayout()));
			}
		}
	}
	/**
	* Validate WithdrawalBasis against table OLI_LU_STMTBASIS. 
	* If WithdrawalBasis is missing or invalid for an RMD arrangement (tc="13"), set basis to calendar year.  
	* If value is missing or invalid for other arrangements, set basis to policy year.   
	*/
	protected void process_P032() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P032 for " ,  getArrangement()); //NBA103
			if (!isValidTableValue(NbaTableConstants.OLI_LU_STMTBASIS, getArrangementExtension().getWithdrawalBasis())) {
				if (getArrangement().getArrType() == OLI_ARRTYPE_REQMINWITH) {
					getArrangementExtension().setWithdrawalBasis(OLI_STMTBASIS_CALENDAR);
				} else {
					getArrangementExtension().setWithdrawalBasis(OLI_STMTBASIS_ANNIV);
				}
				getArrangementExtension().setActionUpdate();
			}
		}
	}
	/**
	* Validate WithdrawalAllocationRule against table OLIEXT_LU_FUNDALLOCRULE. 
	* If WithdrawalAllocationRule is missing or invalid sets rule to pro-rata (tc="1").  
	*/
	protected void process_P033() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P033 for " , getArrangement()); //NBA103
			if (!isValidTableValue(NbaTableConstants.OLIEXT_LU_FUNDALLOCRULE, getArrangementExtension().getWithdrawalAllocationRule())) {
				getArrangementExtension().setWithdrawalAllocationRule(OLIX_FUNDALLOCRULE_SPDAPRORATA);
				getArrangementExtension().setActionUpdate();
			}
		}
	}
	/**
	* Verify that at least one source fund has been specified.  
	*/
	protected void process_P034() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P034 for " , getArrangement()); //NBA103
			if (!(getArrangement().getArrSourceCount() > 0)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Source Fund", getIdOf(getArrangement()));
			}
		}
	}
	/**
	 * Verify the source fund is valid based on plan business rules.  
	 */
	protected void process_P905() {
		if (verifyCtl(ARRSOURCE)) {
			logDebug("Performing NbaValMisc.process_P905 for " , getArrSource()); //NBA103
			if (!(getArrSource().hasSubAcctID() && getArrSource().getSubAcctID().trim().length() > 0)) { //SPR1945
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrSource()));
			}
		}
	}
	/**
	 * Verify that TransferAmt is greater than 0 for Scheduled Withdrawalarrangement types 7, 8 
	 * and Dollar Cost Averaging arrangement type. 
	 */
	protected void process_P035() {
		if (verifyCtl(ARRSOURCE)) {
			logDebug("Performing NbaValMisc.process_P035 for " , getArrSource()); //NBA103
			long arrType = getArrangement().getArrType();
			if (arrType == OLI_ARRTYPE_COSTAVG || arrType == OLI_ARRTYPE_SPECAMTNETWITH || arrType == OLI_ARRTYPE_SPECAMTGROSSWITH) {
				if (!(getArrSource().hasTransferAmt() && getArrSource().getTransferAmt() > 0)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Source Fund Transfer Amount", getIdOf(getArrSource()));
				}
			}
		}
	}
	/**
	 * For Scheduled Withdrawalarrangement types 7, 8 and Dollar Cost Averaging arrangement type, set type to amount (tc="2").
	 * Otherwise, validate TransferAmtType against table OLI_LU_TRNSFRAMTTYPE.  If invalid, set type to amount (tc="2").     
	 */
	protected void process_P036() {
		if (verifyCtl(ARRSOURCE)) {
			logDebug("Performing NbaValMisc.process_P036 for " , getArrSource()); //NBA103
			long arrType = getArrangement().getArrType();
			if (arrType == OLI_ARRTYPE_COSTAVG
				|| arrType == OLI_ARRTYPE_SPECAMTNETWITH
				|| arrType == OLI_ARRTYPE_SPECAMTGROSSWITH
				|| !isValidTableValue(NbaTableConstants.OLI_LU_TRNSFRAMTTYPE, getArrSource().getTransferAmtType())) {
				getArrSource().setTransferAmtType(OLI_TRANSAMTTYPE_AMT);
				getArrSource().setActionUpdate();
			}
		}
	}

	//SPR1466 - removed process_P037

	/**
	 * For Scheduled Withdrawal arrangement types 14 and 43:  Verify the presence of a TransferPct.  
	 * Verify transfer percent greater than 0 and not greater than 100. 
	 * Verify value for TransferAmtType.  If missing or invalid, set type to percentage (tc=3).    
	 */
	protected void process_P038() {
		if (verifyCtl(ARRSOURCE)) {
			logDebug("Performing NbaValMisc.process_P038 for " ,  getArrSource()); //NBA103
			if (isAutoWdwlPctArrangement()) {
				double pct = 0;
				if (getArrSource().hasTransferPct()) {
					pct = getArrSource().getTransferPct();
				}
				if (!(pct > 0) || pct > 100) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Source Fund Transfer Percentage: ", pct), getIdOf(getArrSource()));
				}
				if (getArrSource().getTransferAmtType() != OLI_TRANSAMTTYPE_PCT) {
					getArrSource().setTransferAmtType(OLI_TRANSAMTTYPE_PCT);
					getArrSource().setActionUpdate();
				}
			}
		}
	}
	/**
	 * Verify PayoutPct and PayoutAmt are not both specified.  Can only have one or the other.   
	*/
	protected void process_P039() {
		if (verifyCtl(PAYOUT)) {
			logDebug("Performing NbaValMisc.process_P039 for " , getPayout());	//NBA103
			if (getPayout().hasPayoutPct() && getPayout().getPayoutPct() > 0 && getPayout().hasPayoutAmt() && getPayout().getPayoutAmt() > 0) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getPayout()));
			}
		}
	}
	/**
	 * Interest Withdrawal Arrangements can only be expressed as percentages.
	 * For Scheduled Withdrawal arrangement types 42 and 44 verify presence of a TransferPct. 
	 * Set to 100% if missing or if greater than 100.  
	 * Set the value for TransferAmtType to percentage (tc=3).  
	 */
	protected void process_P040() {
		if (verifyCtl(ARRSOURCE)) {
			logDebug("Performing NbaValMisc.process_P040 for " , getArrSource()); //NBA103
			if (isAutoWdwlIntArrangement()) {
				double pct = 0;
				if (getArrSource().hasTransferPct()) {
					pct = getArrSource().getTransferPct();
				}
				if (!(pct > 0) || pct > 100) {
					getArrSource().setTransferPct(100);
					getArrSource().setActionUpdate();
				}
				if (getArrSource().getTransferAmtType() != OLI_TRANSAMTTYPE_PCT) {
					getArrSource().setTransferAmtType(OLI_TRANSAMTTYPE_PCT);
					getArrSource().setActionUpdate();
				}
			}
		}
	}
	/**
	 * Verify that Client Payee (PaymentPartyID) and Contract Payee (PolNumber) are not both specified.   
	 */
	protected void process_P041() {
		if (verifyCtl(ARRDESTINATION)) {
			logDebug("Performing NbaValMisc.process_P041 for " , getArrDestination());	//NBA103
			if (getArrDestination().hasPaymentPartyID()
				&& getArrDestination().getPaymentPartyID().trim().length() > 0
				&& getArrDestination().hasPolNumber()
				&& getArrDestination().getPolNumber().trim().length() > 0) { //SPR1945
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrDestination()));
			}
		}
	}
	/**
	 * Verify that at least one destination fund has been specified.  
	 */
	protected void process_P042() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P042 for " , getArrangement()); //NBA103
			if (!(getArrangement().getArrDestinationCount() > 0)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Destination Fund", getIdOf(getArrangement()));
			}
		}
	}
	/**
	 * Verify the presence of Start Date for SystematicActivityType of '3'(scheduled withdrawals) and '5'(DCA). 
	 */
	//SPR1607
	protected void process_P054() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P054 for " , getArrangement()); //NBA103
			if (getArrangement() != null && getArrangement().getStartDate() != null) {
				return;
			} else {
				for (int i = 0; arrangement.getArrSourceCount() > i; i++) {
					arrSource = arrangement.getArrSourceAt(i);
					if ((arrSource != null) && (arrSource.getSubAcctID() != null)) {
						for (int j = 0; j < investment.getSubAccountCount(); j++) {
							subAccount = investment.getSubAccountAt(j);
							if ((subAccount != null)
								&& (subAccount.getId() != null)
								&& subAccount.getId().equals(arrSource.getSubAcctID())
								&& ((OLI_SYSACTTYPE_WTHDRW == subAccount.getSystematicActivityType()) ||
									(OLI_SYSACTTYPE_DOLLARCOSTAVG == subAccount.getSystematicActivityType()))) {
								addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("StartDate:", getArrangement().getStartDate()), getIdOf(getArrangement()));								
							}
						}
					}
				}				
			}
		}
	}
	/**
	 * Verify presence of a Start Date when the aartype = �3� and aarmode = �1- 4�.  
	 */
	//SPR1607
	protected void process_P055() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P055 for " , getArrangement()); //NBA103
			if (getArrangement() != null 
				&& getArrangement().getArrType() == OLI_ARRTYPE_ASSALLO
				&& (getArrangement().getArrMode() > 0 && getArrangement().getArrMode() < 5)
				&& getArrangement().getStartDate() == null){
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("StartDate:", getArrangement().getStartDate()), getIdOf(getArrangement()));
			}
		}
	}
//	/**
//	 * Verify StartDate is greater than or equal to the contract EffDate (effective date) but less than or equal to the contract TermDate (maturity date). 
//	 */
//	//SPR1607
//	protected void process_P056() {
//		if (verifyCtl(ARRANGEMENT)) {
//			logDebug("Performing NbaValMisc.process_P056 for " ,  getIdOf(getArrangement())); //NBA103
//			if (getArrangement() != null && getArrangement().getPaymentForm() > 0) {
//				return;
//			}
//			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement()));
//		}
//	}
	/**
	 * Verify if there is no Start Date when the aartype = �3� and the aarmode not equal to �1 � 4�.
	 */
	//SPR1607
	protected void process_P057() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P057 for " , getArrangement()); //NBA103
			if (getArrangement() != null 
				&& getArrangement().getArrType() == OLI_ARRTYPE_ASSALLO
				&& getArrangement().getArrMode() > 4
				&& getArrangement().getStartDate() != null){
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("StartDate:", getArrangement().getStartDate()), getIdOf(getArrangement()));
			}
		}
	}
	/**
	 * Verify the End Date is greater than the Start Date.
	 */
	//SPR1607
	protected void process_P058() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P058 for " , getArrangement()); //NBA103
			if (getArrangement() != null
				&& getArrangement().getEndDate() != null
				&& arrModeRequiresStartDate(getArrangement().getArrMode())  
				&& getArrangement().getEndDate().before(getArrangement().getStartDate())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("EndDate:", getArrangement().getEndDate()), getIdOf(getArrangement()));
			}
		}
	}
	//SPR1607
	private boolean arrModeRequiresStartDate(long mode) {
		return mode != OLI_PAYMODE_ANNUALCLNDR
			&& mode != OLI_PAYMODE_MNTHLYCLNDR
			&& mode != OLI_PAYMODE_QUARTLYCLNDR
			&& mode != OLI_PAYMODE_SEMIANNUALCLNDR
			&& mode != OLI_PAYMODE_ANNUALPOLICY
			&& mode != OLI_PAYMODE_MNTHLYPOLICY
			&& mode != OLI_PAYMODE_QUARTLYPOLICY
			&& mode != OLI_PAYMODE_SEMIANNUALPOLICY;
	}
//	/**
//	 * Verify the End Date is greater than the Start Date but less than the maturity date. 
//	 */
//	//SPR1607
//	protected void process_P059() {
//		if (verifyCtl(ARRANGEMENT)) {
//			logDebug("Performing NbaValMisc.process_P059 for " ,  getIdOf(getArrangement())); //NBA103
//			if (getArrangement() != null && getArrangement().getEndDate().before(getPolicy().getTermDate())) {
//				return;
//			}
//			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement()));
//		}
//	}
	/**
	 * Verify the Start Date for a scheduled asset reallocation (aartype = �3�) and an unscheduled asset reallocation (aartype=�21�) is not equal
	 */
	//SPR1607
	protected void process_P060() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValMisc.process_P060 for " , getHolding()); //NBA103
			Arrangement unscheduledAAR = getArrangementByType(OLI_ARRTYPE_AA);
			Arrangement scheduledAAR = getArrangementByType(OLI_ARRTYPE_ASSALLO);
			if (unscheduledAAR == null || scheduledAAR == null) {
				return;
			}
			if (unscheduledAAR.getStartDate() == null || scheduledAAR.getStartDate() == null || unscheduledAAR.getStartDate().equals(scheduledAAR.getStartDate())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement()));
			}
		}
	}
	//SPR1607
	private Arrangement getArrangementByType(long arrType) {
		for (int i = 0; i < getHolding().getArrangementCount(); i++) {
			if (arrType == getHolding().getArrangementAt(i).getArrType()) {
				return getHolding().getArrangementAt(i);
			}
		}
		return null;
	}
	/**
	 * Verify if there is no End Date when the aartype = �21�
	 */
	//SPR1607
	protected void process_P061() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P061 for " , getArrangement()); //NBA103
			if (getArrangement() != null && getArrangement().getArrType() == OLI_ARRTYPE_AA && getArrangement().getEndDate() != null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("EndDate:", getArrangement().getEndDate()), getIdOf(getArrangement()));
			}
		}
	}
	/**
	 * Verify presence of a Start Date when the aartype = �21�.   
	 */
	//SPR1607
	protected void process_P062() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P062 for " , getArrangement()); //NBA103
			if (getArrangement() != null && getArrangement().getArrType() == OLI_ARRTYPE_AA && getArrangement().getStartDate() == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("StartDate:", getArrangement().getStartDate()), getIdOf(getArrangement()));
			}
		}
	}
//	/**
//	 * Verify EndDate is greater than or equal to the contract EffDate (effective date) but less than or equal to the contract TermDate (maturity date). 
//	 */
//	//SPR1607
//	protected void process_P063() {
//		if (verifyCtl(ARRANGEMENT)) {
//			logDebug("Performing NbaValMisc.process_P063 for " ,  getIdOf(getArrangement())); //NBA103
//			if (getArrangement() != null && getArrangement().getPaymentForm() > 0) {
//				return;
//			}
//			addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement()));
//		}
//	}
	
	/**
	 * Verify the funds being used for systematicactivitytype = �6� are valid.
	 */
	//SPR1607
	protected void process_P065() throws RemoteException, CreateException, NbaBaseException {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P065 for " , getArrangement()); //NBA103
			validateFunds(OLI_SYSACTTYPE_ASSETREALLOC);
		}
	}
	//SPR1607, SPR1466	
	private void validateFunds(long systematicActivityType) throws CreateException, RemoteException, NbaBaseException {
		boolean invalidProduct = true;
		for (Iterator iter = getInvestment().getSubAccount().iterator(); iter.hasNext();) {
			SubAccount subAccount = (SubAccount)iter.next();
			if (subAccount.getSystematicActivityType() == systematicActivityType) {
				for (Iterator iterr = getPolicyProduct().getOLifE().getInvestProduct().iterator(); iterr.hasNext();) {
					InvestProduct investProduct = (InvestProduct)iterr.next();
					if (investProduct.getProductCode().equals(subAccount.getProductCode())) {
						invalidProduct = false;
						break;
					}
				}
				if (invalidProduct) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("ProductCode:", subAccount.getProductCode()), getIdOf(getArrangement()));
				}
			}
		}
	}
	//SPR1607
	//NBA237 changed method signature
	private AccelProduct getPolicyProduct() throws CreateException, RemoteException, NbaBaseException {
		NbaProductAccessFacadeBean nbaProductAccessFacade = new NbaProductAccessFacadeBean();  //NBA213
		return nbaProductAccessFacade.doProductInquiry(getNbaTXLife());
	}
	/**
	 * Verify that at least two source funds have been specified.
	 */
	//SPR1607
	protected void process_P066() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P066 for " , getArrangement()); //NBA103
			if (getInvestment() == null || getSubAccountCount(getInvestment(), OLI_SYSACTTYPE_ASSETREALLOC) < 2) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement()));
			}
		}
	}
	/**
	 * Verify that for any given contract only one arrangment of type 3 and one of type 21 exist
	 */
	//SPR1607
	protected void process_P067() {
		if (verifyCtl(HOLDING)) {
			logDebug("Performing NbaValMisc.process_P067 for " , getHolding()); //NBA103
	
			if (checkForMultipleArrangementsOfSameType(OLI_ARRTYPE_AA)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement()));			
			}
			
			if (checkForMultipleArrangementsOfSameType(OLI_ARRTYPE_ASSALLO)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement()));			
			}
		}		
	}
	//SPR1607
	private boolean checkForMultipleArrangementsOfSameType(long arrType) {
		boolean anotherExists = false;
		for (int i = 0; i < getHolding().getArrangementCount(); i++) {
			if (arrType == getHolding().getArrangementAt(i).getArrType()) {
				if (anotherExists) {
					return true;
				}
				anotherExists = true;
			}
		}
		return false;
	}
	/**
//SPR1466 - removed p068
	* Verify there is banking information when the payment form is EFT (ArrDestination.PaymentForm='7').
	*/
	//SPR1466
	protected void process_P069() {
		if (verifyCtl(ARRDESTINATION)) {
			logDebug("Performing NbaValMisc.process_P069 for " , getArrDestination()); //NBA103
			if (getArrDestinationExtension() != null && getArrDestinationExtension().getPaymentForm() == OLI_PAYFORM_EFT) {
				Banking banking = getNbaTXLife().getBankingForBilling(); //SPR3573
				if (banking == null || !banking.hasBankAcctType() || !banking.hasAccountNumber() || !banking.hasRoutingNum()) { //SPR3573
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrangement())); //SPR3573
				} //SPR3573
			}
		}
	}
	/**
	* Verify the frequency (arrmode='1', '2', '3', '4') for scheduled withdrawals is Monthly, Quarterly, Semi-Annual or Annual.
	*/
	//SPR1466
	protected void process_P070() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P070 for " , getArrangement()); //NBA103
			if (getArrangement() != null && !isValidArrMode(getArrangement())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("ArrMode:", arrangement.getArrMode()), getIdOf(getArrangement()));
			}
		}
	}
	//SPR1466
	private boolean isValidArrMode(Arrangement arrangement) {
		return arrangement.getArrMode() == OLI_PAYMODE_ANNUAL
			|| arrangement.getArrMode() == OLI_PAYMODE_BIANNUAL
			|| arrangement.getArrMode() == OLI_PAYMODE_QUARTLY
			|| arrangement.getArrMode() == OLI_PAYMODE_MNTHLY;
	}

	/**
	* Verify the arrangement type for scheduled withdrawals is one of the following arrtype='7', '8', '10', or  '14'.
	*/
	//SPR1466
	protected void process_P071() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P071 for " , getArrangement()); //NBA103
			if (getArrangement() != null && !isValidArrType(getArrangement())) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("ArrType:", arrangement.getArrType()), getIdOf(getArrangement()));
			}
		}
	}
	//SPR1466
	private boolean isValidArrType(Arrangement arrangement) {
		return arrangement.getArrType() == OLI_ARRTYPE_SPECAMTNETWITH
			|| arrangement.getArrType() == OLI_ARRTYPE_SPECAMTGROSSWITH
			|| arrangement.getArrType() == OLI_ARRTYPE_INTNETWITH
			|| arrangement.getArrType() == OLI_ARRTYPE_PCTVALWITH;
	}

	/**
	* Verify there is a modalamt when applicable.
	*/
	//SPR1466
	protected void process_P072() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P072 for " , getArrangement());//NBA103
			if (getArrangement() != null 
				&& (getArrangement().getArrType() == OLI_ARRTYPE_INTNETWITH)
				&& getArrangement().getModalAmt() >= 0) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat(" ArrType:", getArrangement().getArrType()) + concat(" ModalAmt:", getArrangement().getModalAmt()), getIdOf(getArrangement()));			
			}

			if (getArrangement() != null 
				&& ((getArrangement().getArrType() == OLI_ARRTYPE_SPECAMTGROSSWITH) || getArrangement().getArrType() == OLI_ARRTYPE_SPECAMTNETWITH || getArrangement().getArrType() == OLI_ARRTYPE_PCTVALWITH)
				&& getArrangement().getModalAmt() < 1) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat(" ArrType:", getArrangement().getArrType()) + concat(" ModalAmt:", getArrangement().getModalAmt()), getIdOf(getArrangement()));			
			}
			
		}
	}
//	/**
//	* Verify for ProductType=9 or 10 and Holding.Policy.Annuity.QualPlanType=2 the taxdisbusementtype is not equal to 18, 19 or blank.
//	*/
//	//SPR1466
//	protected void process_P073() {
//		if (verifyCtl(ARRANGEMENT)) {
//			logDebug("Performing NbaValMisc.process_P073 for " ,  getIdOf(getArrangement())); //NBA103
//		}
//	}
	/**
	* Verify that the Arrangement.TransactionEffectiveDate is present for scheduled withdrawals.
	*/
	//SPR1466
	protected void process_P074() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P074 for " , getArrangement()); //NBA103
			if (!hasTransactionEffectiveDate()) {				
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "TransactionEffectiveDate:null", getIdOf(getArrangement()));
			}
		}
	}
	private boolean hasTransactionEffectiveDate() {
		return getTransactionEffectiveDate() != null;
	}
	private Date getTransactionEffectiveDate() {
		if (getArrangement() != null && 
			getArrangement().getOLifEExtensionAt(0) != null && 
			getArrangement().getOLifEExtensionAt(0).getArrangementExtension() != null) { 
			return getArrangement().getOLifEExtensionAt(0).getArrangementExtension().getTransactionEffectiveDate();
		}
		return null;
	}
	/**
	* Verify that the Arrangement.TransactionEffectiveDate equal to or greater than the than the contract EffDate (effective date)
	*/
	//SPR1466
	protected void process_P075() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P075 for " ,getArrangement()); //NBA103
			Date effectiveDate = getTransactionEffectiveDate();
			if (effectiveDate != null && effectiveDate.before(getPolicy().getEffDate())) {			
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("TransactionEffectiveDate:", effectiveDate), getIdOf(getArrangement()));
			}
		}
	}	
	/**
	* Verify that the Arrangement.TransactionEffectiveDate equal to or less than the StartDate.
	*/
	//SPR1466
	protected void process_P076() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P076 for " , getArrangement()); //NBA103
			Date effectiveDate = getTransactionEffectiveDate();			
			if (effectiveDate != null && effectiveDate.after(getStartDate())) {			
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("TransactionEffectiveDate:", effectiveDate), getIdOf(getArrangement()));
			}
		}
	}
	private Date getStartDate() {
		if (getArrangement() != null) { 
			return getArrangement().getStartDate();
		}
		return null;
	}
	/** 
    * Set PaymentForm to eft if arrdestination exists else to cash 
	*/
	//SPR1466 New Method
	protected void process_P077() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P077 for " , getArrangement()); //NBA103
			if (getArrangement() != null && getArrangement().getPaymentForm() < 0) {
				if (getArrDestination() == null) {
					getArrangement().setPaymentForm(OLI_PAYFORM_CASH);
				} else {
					getArrangement().setPaymentForm(OLI_PAYFORM_EFT);				
				}
				getArrangement().setActionUpdate();
			} else if (getArrangement() != null 
				&& (getArrangement().getPaymentForm() != OLI_PAYFORM_CASH && getArrangement().getPaymentForm() != OLI_PAYFORM_EFT)) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("PaymentForm:", getArrangement().getPaymentForm()), getIdOf(getArrangement()));	
			}
		}
	}
	/**
	 * Verify the funds being used for systematicactivitytype = �3� are valid.
	 */
	//SPR1466
	protected void process_P078() throws RemoteException, CreateException, NbaBaseException {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P065 for " , getArrangement()); //NBA103
			validateFunds(OLI_SYSACTTYPE_WTHDRW);
		}
	}
	/**
	 * Verify the destination fund is valid based on plan business rules.  
	 */
	protected void process_P906() {
		if (verifyCtl(ARRDESTINATION)) {
			logDebug("Performing NbaValMisc.process_P906 for " , getArrDestination());	//NBA103
			if (!(getArrDestination().hasSubAcctID() && getArrDestination().getSubAcctID().trim().length() > 0)) { //SPR1945
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrDestination()));
			}
		}
	}
	/**
	 * If TransferAmtType is neither amount (tc=2) or percent (tc=3), set based on the presence of a non-zero amount or percent. 
	 */
	protected void process_P907() {
		if (verifyCtl(ARRDESTINATION)) {
			logDebug("Performing NbaValMisc.process_P907 for " , getArrDestination());	//NBA103
			long type = getArrDestination().getTransferAmtType();
			if (!(type == OLI_TRANSAMTTYPE_AMT || type == OLI_TRANSAMTTYPE_PCT)) {
				if ((getArrDestination().hasTransferAmt() && getArrDestination().getTransferAmt() > 0)) {
					getArrDestination().setTransferAmtType(OLI_TRANSAMTTYPE_AMT);
					getArrDestination().setTransferPct(0); //Set pct value to 0
					getArrDestination().setActionUpdate();
				} else if ((getArrDestination().hasTransferPct() && getArrDestination().getTransferPct() > 0)) {
					getArrDestination().setTransferAmtType(OLI_TRANSAMTTYPE_PCT);
					getArrDestination().setTransferAmt(0); //Set amount value to 0
					getArrDestination().setActionUpdate();
				} else {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrDestination()));
				}
			}
		}
	}
	/**
	 * If TransferAmtType is amount (tc=2), verify that TransferAmt > 0. 
	 * If TransferAmtType is percent (tc=3), verify that TransferPct > 0 and not greater than 100.
	 */
	protected void process_P043() {
		if (verifyCtl(ARRDESTINATION)) {
			logDebug("Performing NbaValMisc.process_P043 for " , getArrDestination());	//NBA103
			boolean invalid = true;
			if (getArrDestination().getTransferAmtType() == OLI_TRANSAMTTYPE_AMT) {
				if ((getArrDestination().hasTransferAmt() && getArrDestination().getTransferAmt() > 0)) {
					invalid = false; //Value matches type
				}
			} else if (getArrDestination().getTransferAmtType() == OLI_TRANSAMTTYPE_PCT) {
				if ((getArrDestination().hasTransferPct() && getArrDestination().getTransferPct() > 0 && !(getArrDestination().getTransferPct() > 100))) {
					invalid = false; //Value matches type
				}
			}
			if (invalid) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "", getIdOf(getArrDestination()));
			}
		}
	}
	/**
	 * Verify TransferPct total from ArrDest is equal to 100% when TransferAmtType is percent.  
	 */
	protected void process_P044() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P044 for " , getArrangement()); //NBA103
			if (getArrangement().getArrDestinationCount() > 0 && getArrangement().getArrDestinationAt(0).getTransferAmtType() == OLI_TRANSAMTTYPE_PCT) {
				double totalPct = 0;
				for (int i = 0; i < getArrangement().getArrDestinationCount(); i++) {
					ArrDestination arrDestination = getArrangement().getArrDestinationAt(i);
					if (arrDestination.hasTransferPct()) {
						totalPct += arrDestination.getTransferPct();
					}
				}
				if (totalPct != 100) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concatPct("Percentage: ", totalPct), getIdOf(getArrangement()));
				}
			}
		}
	}
	/**
	 * Verify that the TransferAmt total is equal to the transfer amount in the arrangement source object when the TransferAmtType is amount.  
	 */
	protected void process_P045() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P045 for " , getArrangement()); //NBA103
			if (getArrangement().getArrDestinationCount() > 0 && getArrangement().getArrDestinationAt(0).getTransferAmtType() == OLI_TRANSAMTTYPE_AMT) {
				double sourceAmt = 0;
				double destAmt = 0;
				for (int i = 0; i < getArrangement().getArrSourceCount(); i++) {
					ArrSource arrSource = getArrangement().getArrSourceAt(i);
					if (arrSource.hasTransferAmt()) {
						sourceAmt += arrSource.getTransferAmt();
					}
				}
				for (int i = 0; i < getArrangement().getArrDestinationCount(); i++) {
					ArrDestination arrDestination = getArrangement().getArrDestinationAt(i);
					if (arrDestination.hasTransferAmt()) {
						destAmt += arrDestination.getTransferAmt();
					}
				}
				if (sourceAmt != destAmt) {
					addNewSystemMessage(
						getNbaConfigValProc().getMsgcode(),
						concatAmt("Source: ", sourceAmt, ", Destination: ", destAmt),
						getIdOf(getArrangement()));
				}
			}
		}
	}
	/**
	 * Validate only one occurrence of the same TaxWithholdingPlace within an arrangement. 
	 */
	protected void process_P908() {
		if (verifyCtl(ARRANGEMENT)) {
			logDebug("Performing NbaValMisc.process_P908 for " , getArrangement()); //NBA103
			Map places = new HashMap();
			for (int i = 0; i < getArrangement().getTaxWithholdingCount(); i++) {
				TaxWithholding taxWithholding = getArrangement().getTaxWithholdingAt(i);
				if (taxWithholding.hasTaxWithholdingPlace()) {
					String place = Long.toString(taxWithholding.getTaxWithholdingPlace());
					if (places.containsKey(place)) {
						addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Tax Withholding Place: ", place), getIdOf(getArrangement()));
					} else {
						places.put(place, place);
					}
				}
			}
		}
	}
	/**
	 * Validate TaxWithholdingPlace value against table OLI_LU_TAXPLACE.   
	 */
	protected void process_P046() {
		if (verifyCtl(TAXWITHHOLDING)) {
			logDebug("Performing NbaValMisc.process_P046 for " , getTaxWithholding());	//NBA103
			long place = getTaxWithholding().getTaxWithholdingPlace();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_TAXPLACE, place)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Tax Withholding Place: ", place), getIdOf(getTaxWithholding()));
			}
		}
	}
	/**
	 * Validate TaxDisbursementType value against table OLI_LU_TAXDISBURSETYPE.  
	 */
	protected void process_P047() {
		if (verifyCtl(TAXWITHHOLDING)) {
			logDebug("Performing NbaValMisc.process_P047 for " , getTaxWithholding());	//NBA103
			long type = getTaxWithholding().getTaxDisbursementType();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_TAXDISBURSETYPE, type)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Tax Disbursement Type: ", type), getIdOf(getTaxWithholding()));
			}
		}
	}
	/**
	 * Validate TaxWithholdingType value against table OLI_LU_WITHCALCMTH.  
	 */
	protected void process_P048() {
		if (verifyCtl(TAXWITHHOLDING)) {
			logDebug("Performing NbaValMisc.process_P048 for " , getTaxWithholding());	//NBA103
			long type = getTaxWithholding().getTaxWithholdingType();
			if (!isValidTableValue(NbaTableConstants.OLI_LU_WITHCALCMTH, type)) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("Tax Withholding Type: ", type), getIdOf(getTaxWithholding()));
			}
		}
	}
	/**
	 * Verify presence of either a TaxWithheldPct or TaxWithheldAmt.   
	 */
	protected void process_P049() {
		if (verifyCtl(TAXWITHHOLDING)) {
			logDebug("Performing NbaValMisc.process_P049 for " , getTaxWithholding());	//NBA103
			if (!(getTaxWithholding().hasTaxWithheldPct() || getTaxWithholding().hasTaxWithheldAmt()) //Neither present
				|| (getTaxWithholding().hasTaxWithheldPct() && getTaxWithholding().hasTaxWithheldAmt())) //Both present
				{
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Tax Withholding Percentage / Tax Withholding Amount", getIdOf(getTaxWithholding()));
			}
		}
	}
	/**
	 * Validate TaxWithholdingPercentBasis against table OLIEXT_LU_PCTBASIS.  
	 * When tax withholding place is a 'jurisdiction' and a withholding percentage has been indicated.
	 */
	protected void process_P050() {
		if (verifyCtl(TAXWITHHOLDING)) {
			logDebug("Performing NbaValMisc.process_P050 for " , getTaxWithholding());	//NBA103
			//BEGIN SPR1466		
			if (getTaxWithholding().hasTaxWithholdingPlace() && getTaxWithholding().getTaxWithholdingPlace() == OLI_TAXPLACE_JURISDICTION) {
				if (getTaxWithholding().hasTaxWithheldPct() && !getTaxWithholdingExtension().hasTaxWithholdingPercentBasis()) {
					addNewSystemMessage(getNbaConfigValProc().getMsgcode(), "Tax Withholding Percent Basis: ", getIdOf(getTaxWithholding()));
				}
			}
			//END SPR1466
		}
	}
	/**
	 * Set ApplicationInfo.HOUnderwriterName from the Underwriter Queue (UNDQ) LOB field.
	 */
	//NBA100 New Method
	protected void process_P051() {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValMisc.process_P051"); //NBA103
			getApplicationInfo().setHOUnderwriterName(getNbaDst().getNbaLob().getUndwrtQueue());
			getApplicationInfo().setActionUpdate();
		}
	}
	/**
	 * Set ApplicationInfo.CarrierInputDate from the AWD CreateTime for the Case 
	 */
	//NBA100 New Method
	protected void process_P052() throws Exception {
		if (verifyCtl(APPLICATIONINFO)) {
			logDebug("Performing NbaValMisc.process_P052"); //NBA103
			Date createDate = null;
			if( getNbaDst().isCase()) { // SPR2247
				//NBA208-32
				String createDateTime = getNbaDst().getCase().getCreateDateTime(); 
				if (createDateTime != null && createDateTime.trim().length() > 0) { //SPR1945
					createDate = get_YYYY_MM_DD_sdf().parse(createDateTime);
				}
			// BEGIN SPR2247
			} else if (getNbaDst().isTransaction()) {
				//NBa208-32
				String createDateTime = getNbaDst().getTransaction().getCreateDateTime();
				if (createDateTime != null && createDateTime.trim().length() > 0) { //SPR1945
					createDate = get_YYYY_MM_DD_sdf().parse(createDateTime);
				}
			}
			// END SPR2247
			getApplicationInfo().setCarrierInputDate(createDate);
			getApplicationInfo().setActionUpdate();
		}
	}
	/**
	 * Set SubAccount.ProductFullName from table NBA_FUNDS.FundIdTranslation 
	 */
	//NBA100 New Method
	protected void process_P053() {
		if (verifyCtl(SUBACCOUNT)) {
			logDebug("Performing NbaValMisc.process_P053 for " , getSubAccount());	//NBA103
			String productCode = getSubAccount().getProductCode();
			String productFullName = null;
			try {
				NbaTableData aNbaTableData = getNbaTableAccessor().getDataForOlifeValue(getTblKeys(), NbaTableConstants.NBA_FUNDS, productCode);
				if (aNbaTableData != null) {
					productFullName = ((NbaFundsData) aNbaTableData).getFundIdTranslation();
				}
			} catch (NbaDataAccessException e) { //Will be handled by null check
			}
			if (productFullName == null) {
				addNewSystemMessage(getNbaConfigValProc().getMsgcode(), concat("SubAccount ProductCode: ", productCode), getIdOf(getSubAccount()));
			} else {
				getSubAccount().setProductFullName(productFullName);
			}
		}
	}
	/**
	 * Update LifeParticipant.ParticipantName with Person.FirstName + Person.MiddleName +  Person.LastName 
	 */
	// NBA122 New Method
	protected void process_P008() throws RemoteException, CreateException, NbaBaseException {
		if (verifyCtl(LIFEPARTICIPANT)) {
			logDebug("Performing NbaValMisc.process_P008() for ", getLifeParticipant());
			NbaParty nbaParty = getNbaTXLife().getParty(getLifeParticipant().getPartyID());
			if (nbaParty != null) {
				getLifeParticipant().setParticipantName(NbaUtils.convertStringInProperCase(nbaParty.getDisplayName()));
				getLifeParticipant().setActionUpdate();
			}
		}
	}
	/**
	 * Update Participant.ParticipantName with Person.FirstName + Person.MiddleName + Person.LastName 
	 */
	// NBA122 New Method
	protected void process_P009() throws RemoteException, CreateException, NbaBaseException {
		if (verifyCtl(PARTICIPANT)) {
			logDebug("Performing NbaValMisc.process_P009() for ", getParticipant());
			NbaParty nbaParty = getNbaTXLife().getParty(getParticipant().getPartyID());
			if (nbaParty != null) {
				getParticipant().setParticipantName(NbaUtils.convertStringInProperCase(nbaParty.getDisplayName()));
				getParticipant().setActionUpdate();
			}
		}
	}

}
