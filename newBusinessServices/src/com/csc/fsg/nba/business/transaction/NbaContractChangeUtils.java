package com.csc.fsg.nba.business.transaction;

/**
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.csc.fsg.nba.business.contract.merge.CopyBox;
import com.csc.fsg.nba.business.contract.merge.CopyManager;
import com.csc.fsg.nba.business.contract.merge.Matcher;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.ApplicationInfo;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.CovOptionExtension;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.HoldingExtension;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeExtension;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.LifeParticipantExtension;
import com.csc.fsg.nba.vo.txlife.LifeUSA;
import com.csc.fsg.nba.vo.txlife.LifeUSAExtension;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.PartyExtension;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
import com.csc.fsg.nba.vo.txlife.RequirementInfoExtension;

/** 
 * 
 * This class provides common functions for Reissue, Complex Change,
 * Reinstatement, Rerate and Increase type of contract change.   
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change</td></tr>
 * <tr><td>NBA126</td><td>Version 6</td><td>Vantage Contract Changes</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-4</td><td>Version 7</td><td>Performance Tuning and Testing - Incremental change 4</td></tr> 
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA187</td><td>Version 7</td><td>nbA Trial Application Project</td></tr> 
 * <tr><td>SPR3605</td><td>Version 8</td><td>An insured Relation for a non-Primary Holding causes processing errors.</td></tr>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr>
 * <tr><td>P2AXAL016CV</td><td>Axa Life Phase 2</td><td>Life 70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaContractChangeUtils implements NbaOliConstants {
	public static final String NBA_EVIDENCE_INS = "NBAEVIDENCEINS.JSP";
	public static final String NBA_MEDICAL_INFO = "NBAMEDICALINFO.JSP";
	public static final String NBA_REINSTATEMENT = "NBAREINSTATEMENT.JSP";
	public static final String NBA_INCREASE = "NBAINCREASE.JSP";
	protected static final String QUOTE = "\"";
	protected static final String VENDORCODE = "VendorCode";
	protected static final String EQUALS = "=";
	protected static final String SPACE = " ";
	private static NbaLogger logger;
	private Map partyMap = new HashMap(); 
	protected NbaTXLife nbAContract ; //P2AXAL016CV
	protected NbaTXLife beContract ; //P2AXAL016CV - changed all references from capsContract to beContract

	//new constructor AXAL3.7.04
	public NbaContractChangeUtils(NbaTXLife nbA, NbaTXLife backend)throws NbaBaseException{
		// if  backEndHoldingInquiry is null throw NbaBaseException
		if (backend == null) {
			throw new NbaBaseException("Backend holding inquiry is null or invalid");
		}
		beContract = (NbaTXLife) backend.clone(false);
		beContract = replaceVendorId(beContract, NbaOliConstants.AXA_VENDOR_CODE, NbaOliConstants.CSC_VENDOR_CODE);
		if(nbA != null) {
			nbAContract = (NbaTXLife) nbA.clone(false) ;
			initialize();
		}
	}
	
	//P2AXAL016CV default constructor
	public NbaContractChangeUtils() {
	}
	
	
	/**
	 * To be used by Contract Change Business Functions.<BR>
	 * mergeContract method merges the NBATXLife objects and returns the merged object.<BR> 
	 * The method clones both arguments "nbaContract" and "beContract". Hence the state of both 
	 * these objects is preserved and is not affected by changes in returned object.<BR>
	 * 
	 * Logic Used in this method :<BR>
	 * <Code>
	 * This method assumes beContract is not null. 
	 * 		IF nbaContract is passed as null to this method, it returns beContract
	 * 		OR if nbaContract is not null. This method clone nbaContract and beContract and merge 
	 * 		contract as sepcified in the AXAL3.7.04 enhancement spec.	 * 
	 * </Code>
	 * <BR> This method assumes following :<BR>
	 * <li> This method assumes beContract is not null.</li>
	 * <li> If the policy is not Life, it is Annuity.</li>
	 * @return com.csc.fsg.nba.vo.NbaTXLife  This is a merged form of beContract and nbaContract objects.     
	 * @throws NbaBaseException This exception is thrown in mentioned cases.
	 * 
	 * 
	 */
	//Refactored AXAL3.7.04
	public NbaTXLife mergeContract() throws NbaBaseException {
		//if nbAHoldingInquiry is null returns the clone of backEndHoldingInquiry object.
		if (nbAContract == null) {
			return beContract;
		}
		//Merge parties
		mergeParties(nbAContract, beContract);
		//Merge Relations
		mergeRelations(nbAContract, beContract);
		//Merge Holding and Children
		mergeHolding(nbAContract.getPrimaryHolding(), beContract.getPrimaryHolding());
		NbaOLifEId olifeId = new NbaOLifEId(nbAContract);
		olifeId.resolveDuplicateIds(nbAContract);
        olifeId.resetIds(nbAContract);
		return nbAContract;
	}

	/**
	 * Merges 2 Holding Objects and its children <BR>
	 * This method merges the details from secondHolding into the reference of firstHolding. This method then returns firstHolding's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if secondHolding is null, return firstHolding
	 * 		if firstHolding is null, return secondHolding. (CAUTION: Any changes to firstHolding member objects references will affect the state of secondHolding.)
	 * 	  </code>
	 * 
	 * @param firstHolding a Holding object 
	 * @param secondHolding a Holding object
	 */
	//Refactored AXAL3.7.04
	//P2AXAL016CV changed the method access modifier from protected to public
	public void mergeHolding(Holding firstHolding, Holding secondHolding) throws NbaBaseException {
		if (secondHolding == null) {
			throw new NbaBaseException("Backend Holding is missing or invalid");
		}
		if (firstHolding != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_HOLDING);
			copyBox.copy(firstHolding, secondHolding);
			//Merge Holding Extension
			mergeHoldingExtension(firstHolding, secondHolding);
			//merge Policy
			mergePolicy(firstHolding.getPolicy(), secondHolding.getPolicy());
		}
	}

	/**
	 * Merges 2 Policy Objects and its children <BR>
	 * This method merges the details from bePolicy into the reference of nbAPolicy. This method then returns nbAPolicy's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if bePolicy is null, return nbAPolicy
	 * 		if nbAPolicy is null, return bePolicy. (CAUTION: Any changes to returned Policy objects references will affect the state of bePolicy.)
	 * 	  </code>
	 * 
	 * @param nbAPolicy a Policy object 
	 * @param bePolicy a Policy object
	 */
	//Refactored AXAL3.7.04
	protected void mergePolicy(Policy nbAPolicy, Policy bePolicy) throws NbaBaseException {

		if (nbAPolicy != null && bePolicy != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_POLICY);
			copyBox.copy(nbAPolicy, bePolicy);
			//Merge Policy Extensions
			mergePolicyExtension(nbAPolicy, bePolicy);
			//Merge Holding.Policy.LifeIncludes Coverage, CovOption, Substandard – totally replace so no orphans?
			mergeLife(nbAContract.getLife(), beContract.getLife());
			mergeApplicationInfo(nbAPolicy.getApplicationInfo(), bePolicy.getApplicationInfo());
			//merge RequirementInfo tags
			Iterator beRequirementInfosItr = bePolicy.getRequirementInfo().iterator();
			while (beRequirementInfosItr.hasNext()) {
				RequirementInfo beRequirementInfo = (RequirementInfo) beRequirementInfosItr.next();
				if (partyMap.get(beRequirementInfo.getAppliesToPartyID()) != null) {
					beRequirementInfo.setAppliesToPartyID((String) partyMap.get(beRequirementInfo.getAppliesToPartyID()));
				}
				RequirementInfo nbARequirementInfo = (RequirementInfo) Matcher.match(nbAPolicy.getRequirementInfo(), beRequirementInfo);
				if (nbARequirementInfo != null) {
					mergeRequirementInfo(nbARequirementInfo, beRequirementInfo);
				}
			}
		}
	}
	
	/**
	 * Merges 2 Life Objects and its children <BR>
	 * This method merges the details from beLife into the reference of nbALife. This method then returns firstLife's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beLife is null, return nbALife
	 * 		if nbALife is null, return beLife. (CAUTION: Any changes to nbALife member objects references will affect the state of beLife.)
	 * 	  </code>
	 * 
	 * @param nbALife a Life object 
	 * @param beLife a Life object
	 */
	//New Method AXAL3.7.04
	protected void mergeLife(Life nbALife, Life beLife) throws NbaBaseException {
		if (nbALife != null && beLife != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_LIFE);
			copyBox.copy(nbALife, beLife);
			mergeLifeExtension(nbALife, beLife);
			mergeLifeUSA(nbALife.getLifeUSA(), beLife.getLifeUSA()); //P2AXAL016CV
			Iterator beCoverageItr = beLife.getCoverage().iterator();
			while (beCoverageItr.hasNext()) {
				Coverage beCoverage = (Coverage) beCoverageItr.next();
				Coverage nbACoverage = (Coverage) Matcher.match(nbALife.getCoverage(), beCoverage);
				if (nbACoverage != null) {
					mergeCoverage(nbACoverage, beCoverage);
				}
			}
		}
	}

	/**
	 * Merges 2 LifeUSA Objects and its children <BR>
	 * This method merges the details from beLifeUSA into the reference of nbALifeUSA. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beLifeUSA is null, return nbALifeUSA
	 * 		if nbALifeUSA is null, return beLifeUSA. (CAUTION: Any changes to nbALifeUSA member objects references will affect the state of beLifeUSA.)
	 * 	  </code>
	 * 
	 * @param nbALifeUSA a LifeUSA object 
	 * @param beLifeUSA a LifeUSA object
	 */
	//New Method P2AXAL016CV
	protected void mergeLifeUSA(LifeUSA nbALifeUSA, LifeUSA beLifeUSA) throws NbaBaseException {
		if (nbALifeUSA != null && beLifeUSA != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_LIFEUSA);
			copyBox.copy(nbALifeUSA, beLifeUSA);
			mergeLifeUSAExtension(nbALifeUSA, beLifeUSA);
		}
	}	
	
	/**
	 * Merges 2 ApplicationInfo Extension  Objects. <BR>
	 * This method merges the details from beApplicationInfo into the reference of nbAApplicationInfo. This method then returns nbAApplicationInfo's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beApplicationInfo is null, return nbAApplicationInfo
	 * 		if nbAApplicationInfo is null, return beApplicationInfo. (CAUTION: Any changes to return object's member objects references will affect the state of beApplicationInfo.)
	 * </code>
	 * @param firstAppInfo a ApplicationInfo extension object
	 * @param secondAppInfo a ApplicationInfo extension object 
	 */
	//Refactored AXAL3.7.04
	protected void mergeApplicationInfo(ApplicationInfo nbAApplicationInfo, ApplicationInfo beApplicationInfo) throws NbaBaseException {
		if (nbAApplicationInfo != null && beApplicationInfo != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_APPLICATIONINFO);
			copyBox.copy(nbAApplicationInfo, beApplicationInfo);
			mergeApplicationInfoExtension(nbAApplicationInfo, beApplicationInfo);
		}
	}
	
	
	/**
	 * Merges 2 ApplicationInfoExtension Objects and its children <BR>
	 * This method merges the details from beApplicationInfoExt into the reference of nbAApplicationInfoExt. This method then returns nbAApplicationInfoExt's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beApplicationInfoExt is null, return nbAApplicationInfoExt
	 * 		if nbAApplicationInfoExt is null, return beApplicationInfoExt. (CAUTION: Any changes to nbAApplicationInfoExt member objects references will affect the state of beApplicationInfoExt.)
	 * 	  </code>
	 * 
	 * @param nbAApplicationInfo a ApplicationInfo object 
	 * @param beApplicationInfo a ApplicationInfo object
	 */
	//New Method AXAL3.7.04
	protected void mergeApplicationInfoExtension(ApplicationInfo nbAApplicationInfo, ApplicationInfo beApplicationInfo)
			throws NbaBaseException {
		ApplicationInfoExtension nbAApplicationInfoExt = NbaUtils.getFirstApplicationInfoExtension(nbAApplicationInfo);
		ApplicationInfoExtension beApplicationInfoExt = NbaUtils.getFirstApplicationInfoExtension(beApplicationInfo);

		if (nbAApplicationInfoExt != null && beApplicationInfoExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_APPLICATIONINFO);
			copyBox.copy(nbAApplicationInfoExt, beApplicationInfoExt);
		}
	}
	
	/**
	 * Merges 2 LifeExtension Objects and its children <BR>
	 * This method merges the details from beLifeExt into the reference of nbALifeExt. This method then returns nbALifeExt's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beLifeExt is null, return nbALifeExt
	 * 		if nbALifeExt is null, return beLifeExt. (CAUTION: Any changes to nbALifeExt member objects references will affect the state of beLifeExt.)
	 * 	  </code>
	 * 
	 * @param nbALife a Life object 
	 * @param beLife a Life object
	 */
	//New Method AXAL3.7.04
	protected void mergeLifeExtension(Life nbALife, Life beLife) throws NbaBaseException {
		LifeExtension nbALifeExt = NbaUtils.getFirstLifeExtension(nbALife);
		LifeExtension beLifeExt = NbaUtils.getFirstLifeExtension(beLife);
		if (nbALifeExt != null && beLifeExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_LIFE);
			copyBox.copy(nbALifeExt, beLifeExt);	
		}
	}
	
	/**
	 * Merges 2 LifeUSAExtension Objects and its children <BR>
	 * This method merges the details from beLifeUSAExt into the reference of nbALifeUSAExt. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beLifeUSAExt is null, return nbALifeUSAExt
	 * 		if nbALifeUSAExt is null, return beLifeUSAExt. (CAUTION: Any changes to nbALifeUSAExt member objects references will affect the state of beLifeUSAExt.)
	 * 	  </code>
	 * 
	 * @param nbALife a Life object 
	 * @param beLife a Life object
	 */
	//New Method P2AXAL016CV
	protected void mergeLifeUSAExtension(LifeUSA nbALifeUSA, LifeUSA beLifeUSA) throws NbaBaseException {
		LifeUSAExtension nbALifeUSAExt = NbaUtils.getFirstLifeUSAExtension(nbALifeUSA);
		LifeUSAExtension beLifeUSAExt = NbaUtils.getFirstLifeUSAExtension(beLifeUSA);
		if (nbALifeUSAExt != null && beLifeUSAExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_LIFEUSA);
			copyBox.copy(nbALifeUSAExt, beLifeUSAExt);
		}
	}
	
	/**
	 * Merges 2 Coverage Objects and its children <BR>
	 * This method merges the details from beCoverage into the reference of nbACoverage. This method then returns nbACoverage's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beCoverage is null, return nbACoverage
	 * 		if nbACoverage is null, return beCoverage. (CAUTION: Any changes to nbACoverage member objects references will affect the state of beCoverage.)
	 * 	  </code>
	 * 
	 * @param nbACoverage a Coverage object 
	 * @param beCoverage a Coverage object
	 */
	//Refactored AXAL3.7.04
	protected void mergeCoverage(Coverage nbACoverage, Coverage beCoverage) throws NbaBaseException {
		if (nbACoverage != null && beCoverage != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_LIFECOVERAGE);
			copyBox.copy(nbACoverage, beCoverage);
			mergeCoverageExtension(nbACoverage, beCoverage);
			//merge CovOption tags
			Iterator beCovOptionItr = beCoverage.getCovOption().iterator();
			while (beCovOptionItr.hasNext()) {
				CovOption beCovOption = (CovOption) beCovOptionItr.next();
				CovOption nbACovOption = (CovOption) Matcher.match(nbACoverage.getCovOption(), beCovOption);
				if (nbACovOption  != null) {
					mergeCovOption(nbACovOption, beCovOption);
				}
			}
			//merge LifeParticipant tags
			Iterator beLifeParticipantsItr = beCoverage.getLifeParticipant().iterator();
			while (beLifeParticipantsItr.hasNext()) {
				LifeParticipant beLifeParticipant = (LifeParticipant) beLifeParticipantsItr.next();
				if(partyMap.get(beLifeParticipant.getPartyID()) != null) {
					beLifeParticipant.setPartyID((String)partyMap.get(beLifeParticipant.getPartyID()));
				}
				LifeParticipant nbALifeParticipant = (LifeParticipant) Matcher.match(nbACoverage.getLifeParticipant(), beLifeParticipant);
				if (nbALifeParticipant  != null) {
					mergeLifeParticipant(nbALifeParticipant, beLifeParticipant);
				}
			}
		}
	}
	
	/**
	 * Merges 2 CoverageExtension Objects and its children <BR>
	 * This method merges the details from beCoverageExt into the reference of nbACoverageExt. This method then returns nbACoverageExt's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beCoverageExt is null, return nbACoverageExt
	 * 		if nbACoverageExt is null, return beCoverageExt. (CAUTION: Any changes to nbACoverageExt member objects references will affect the state of beCoverageExt.)
	 * 	  </code>
	 * 
	 * @param nbACoverage a Coverage object 
	 * @param beCoverage a Coverage object
	 */
	//Refactored AXAL3.7.04
	protected void mergeCoverageExtension(Coverage nbACoverage, Coverage beCoverage) throws NbaBaseException {
		CoverageExtension nbACoverageExt = NbaUtils.getFirstCoverageExtension(nbACoverage);
		CoverageExtension beCoverageExt = NbaUtils.getFirstCoverageExtension(beCoverage);
		if (nbACoverageExt != null && beCoverageExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_COVERAGE);
			copyBox.copy(nbACoverageExt, beCoverageExt);
		}
	}

	/**
	 * Merges 2 CovOption Objects and its children <BR>
	 * This method merges the details from beCovOption into the reference of nbACovOption. This method then returns nbACovOption's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beCovOption is null, return nbACovOption
	 * 		if nbACovOption is null, return beCovOption. (CAUTION: Any changes to nbACovOption member objects references will affect the state of beCovOption.)
	 * 	  </code>
	 * 
	 * @param nbACovOption a CovOption object 
	 * @param beCovOption a CovOption object
	 */
	//New Method AXAL3.7.04
	protected void mergeCovOption(CovOption nbACovOption, CovOption beCovOption) throws NbaBaseException {
		if (nbACovOption != null && nbACovOption != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_COVOPTION);
			copyBox.copy(nbACovOption, beCovOption);
			//merge CovOptionExtension tag
			mergeCovOptionExtension(nbACovOption, beCovOption);
		}
	}
	
	/**
	 * Merges 2 CovOptionExtension Objects and its children <BR>
	 * This method merges the details from beCovOptionExt into the reference of nbACovOptionExt. This method then returns nbACovOptionExt's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beCovOptionExt is null, return nbACovOptionExt
	 * 		if nbACovOptionExt is null, return beCovOptionExt. (CAUTION: Any changes to nbACovOptionExt member objects references will affect the state of beCovOptionExt.)
	 * 	  </code>
	 * 
	 * @param nbACovOption a CovOption object 
	 * @param beCovOption a CovOption object
	 */
	//New Method AXAL3.7.04
    protected void mergeCovOptionExtension(CovOption nbACovOption, CovOption beCovOption) throws NbaBaseException {
        CovOptionExtension nbACovOptionExt = NbaUtils.getFirstCovOptionExtension(nbACovOption);
        CovOptionExtension beCovOptionExt = NbaUtils.getFirstCovOptionExtension(beCovOption);
       //Prod Defect APSL4360 Missing OPAI premium amount :: Start
        if (beCovOptionExt != null) {
            if (nbACovOptionExt == null) {
                OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_COVOPTION);
                nbACovOption.addOLifEExtension(olifeExtension);
                nbACovOptionExt = olifeExtension.getCovOptionExtension();
                if (nbACovOptionExt != null) {// APSL2253
                    nbACovOptionExt.setActionAdd();
                }
            }
            CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_COVOPTION);
            copyBox.copy(nbACovOptionExt, beCovOptionExt);
        }
        //Prod Defect APSL4360 Missing OPAI premium amount :: End
    }
	
	/**
	 * Merges 2 LifeParticipant Objects and its children <BR>
	 * This method merges the details from beLifeParticipant into the reference of nbALifeParticipant. This method then returns nbALifeParticipant's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beLifeParticipant is null, return nbALifeParticipant
	 * 		if nbALifeParticipant is null, return beLifeParticipant. (CAUTION: Any changes to nbALifeParticipant member objects references will affect the state of beLifeParticipant.)
	 * 	  </code>
	 * 
	 * @param nbALifeParticipant a LifeParticipant object 
	 * @param beLifeParticipant a LifeParticipant object
	 */
	//New Method AXAL3.7.04
	protected void mergeLifeParticipant(LifeParticipant nbALifeParticipant, LifeParticipant beLifeParticipant)
			throws NbaBaseException {
		if (nbALifeParticipant != null && beLifeParticipant != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_LIFEPARTICIPANT);
			copyBox.copy(nbALifeParticipant, beLifeParticipant);
			//merge CovOptionExtension tag
			mergeLifeParticipantExtension(nbALifeParticipant, beLifeParticipant);			
		}
	}
		

	/**
	 * Merges 2 LifeParticipantExtension Objects and its children <BR>
	 * This method merges the details from beLifeParticipantExt into the reference of nbALifeParticipantExt. This method then returns nbALifeParticipantExt's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beLifeParticipantExt is null, return nbALifeParticipantExt
	 * 		if nbALifeParticipantExt is null, return beLifeParticipantExt. (CAUTION: Any changes to nbALifeParticipantExt member objects references will affect the state of beLifeParticipantExt.)
	 * 	  </code>
	 * 
	 * @param nbALifeParticipant a LifeParticipant object 
	 * @param beLifeParticipant a LifeParticipant object
	 */	
	//New Method AXAL3.7.04
	protected void mergeLifeParticipantExtension(LifeParticipant nbALifeParticipant, LifeParticipant beLifeParticipant)
			throws NbaBaseException {
		LifeParticipantExtension nbALifeParticipantExt = NbaUtils.getFirstLifeParticipantExtension(nbALifeParticipant);
		LifeParticipantExtension beLifeParticipantExt = NbaUtils.getFirstLifeParticipantExtension(beLifeParticipant);
		if (nbALifeParticipantExt != null  && beLifeParticipantExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_LIFEPARTICIPANT);
			copyBox.copy(nbALifeParticipantExt, beLifeParticipantExt);
		}
	}
	


	/**
	 * Merges 2 Holding Extnsion Objects. <BR>
	 * This method merges the details from secondHolding extension into the reference of firstHolding extension. This method then returns firstHolding
	 * updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if secondHolding extension is null, remove extension from firstHolding
	 * 		if firstHolding extension is null, Add secondHolding extension to firstHolding
	 * </code>
	 * @param firstHolding a Holding object
	 * @param secondHolding a Holding object
	 */
	//Refactored AXAL3.7.04
	protected void mergeHoldingExtension(Holding firstHolding, Holding secondHolding) throws NbaBaseException {
		HoldingExtension firstHoldingExt = NbaUtils.getFirstHoldingExtension(firstHolding);
		HoldingExtension secondHoldingExt = NbaUtils.getFirstHoldingExtension(secondHolding);
		if (firstHoldingExt != null && secondHoldingExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_HOLDING);
			copyBox.copy(firstHoldingExt, secondHoldingExt);
		}
	}
	

	/**
	 * Merges 2 Policy Extnsion  Objects. <BR>
	 * This method merges the details from secondPolicy extension into the reference of firstPolicy. This method then returns firstPolicy's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if secondPolicy extension is null, remove policy extension from firstPolicy
	 * 		if firstPolicy extsnsion is null, set policy extension from secondPolicy
	
	 * </code>
	 * @param firstPolicy a Policy  object
	 * @param secondPolicy a Policy  object 
	 */
	//New Method AXAL3.7.04
	protected void mergePolicyExtension(Policy firstPolicy, Policy secondPolicy) throws NbaBaseException {
		PolicyExtension firstPolicyExt = NbaUtils.getFirstPolicyExtension(firstPolicy);
		PolicyExtension secondPolicyExt = NbaUtils.getFirstPolicyExtension(secondPolicy);
		if (firstPolicyExt  != null && secondPolicyExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_POLICY);
			copyBox.copy(firstPolicyExt, secondPolicyExt);			
		}
	}
	

	/**
	 * Merges 2 RequirementInfo Objects and its children <BR>
	 * This method merges the details from beRequirementInfo into the reference of nbARequirementInfo. This method then returns nbARequirementInfo's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beRequirementInfo is null, return nbARequirementInfo
	 * 		if nbARequirementInfo is null, return beRequirementInfo. (CAUTION: Any changes to firstHolding member objects references will affect the state of beRequirementInfo.)
	 * 	  </code>
	 * 
	 * @param nbARequirementInfo a RequirementInfo object 
	 * @param beRequirementInfo a RequirementInfo object
	 */
	//Refactored AXAL3.7.04
	protected void mergeRequirementInfo(RequirementInfo nbARequirementInfo, RequirementInfo beRequirementInfo) throws NbaBaseException {
		if (nbARequirementInfo != null && beRequirementInfo != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_REQUIREMENTINFO);
			if (Matcher.match(nbARequirementInfo, beRequirementInfo)) {
				copyBox.copy(nbARequirementInfo, beRequirementInfo);
			}
			//merge CovOptionExtension tag
			mergeRequirementInfoExtension(nbARequirementInfo, beRequirementInfo);	
		}
	}
	
	/**
	 * Merges 2 RequirementInfoExtension Objects and its children <BR>
	 * This method merges the details from beRequirementInfo into the reference of nbARequirementInfo. This method then returns nbARequirementInfo's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beRequirementInfo is null, return nbARequirementInfo
	 * 		if nbARequirementInfo is null, return beRequirementInfo. (CAUTION: Any changes to nbARequirementInfo member objects references will affect the state of beRequirementInfo.)
	 * 	  </code>
	 * 
	 * @param nbARequirementInfo a RequirementInfoExtension object 
	 * @param beRequirementInfo a RequirementInfoExtension object
	 */
	//New Method AXAL3.7.04
	protected void mergeRequirementInfoExtension(RequirementInfo nbARequirementInfo, RequirementInfo beRequirementInfo)
			throws NbaBaseException {
		RequirementInfoExtension nbARequirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(nbARequirementInfo);
		RequirementInfoExtension beRequirementInfoExt = NbaUtils.getFirstRequirementInfoExtension(beRequirementInfo);
		if (nbARequirementInfoExt != null && beRequirementInfoExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_REQUIREMENTINFO);
			copyBox.copy(nbARequirementInfoExt, beRequirementInfoExt);
		}
	}
	
	/**
	 * Merges 2 Party Objects and its children <BR>
	 * This method merges the details from beParty into the reference of nbAParty. This method then returns nbAParty's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beParty is null, return nbAParty
	 * 		if nbAParty is null, return beParty. (CAUTION: Any changes to firstHolding member objects references will affect the state of beParty.)
	 * 	  </code>
	 * 
	 * @param nbATXLifeCopy a NbaTXLife object 
	 * @param beTXLifeCopy a NbaTXLife object
	 */
	//Refactored AXAL3.7.04
	protected void mergeParties(NbaTXLife nbATXLifeCopy, NbaTXLife beTXLifeCopy) throws NbaBaseException {
		List bePartyList = beTXLifeCopy.getOLifE().getParty();
		List nbAPartyList = nbATXLifeCopy.getOLifE().getParty();
		Iterator bePartyItr = bePartyList.iterator();
		while (bePartyItr.hasNext()) {
			Party beParty = (Party) bePartyItr.next();
			Party nbAParty = NbaTXLife.getPartyFromId((String) partyMap.get(beParty.getId()), nbAPartyList);
			if (nbAParty != null) {
				if (Matcher.match(nbAParty, beParty)) {
					mergeParty(nbAParty, beParty);
				} else {
					if (checkForPartyDeletion(nbAParty.getId())) {
						nbAPartyList.remove(nbAParty);
						Party addedParty = beParty.clone(false);
						addedParty.setId(nbAParty.getId());
						nbAPartyList.add(addedParty);
					} else {
						HandlePartyAdd(nbAPartyList, beParty);
						updateRelations(nbAParty.getId(), beParty.getId());
					}
				}
			} else {
				HandlePartyAdd(nbAPartyList, beParty);
			}
		}
	}
	
	protected void HandlePartyAdd(List nbAPartyList, Party partyToAdd) throws NbaBaseException {
		if (nbAPartyList != null && partyToAdd != null) {
			Party matchingParty = (Party) Matcher.match(nbAPartyList, partyToAdd);
			if (matchingParty != null) {
				partyMap.put(partyToAdd.getId(), matchingParty.getId());
				mergeParty(matchingParty, partyToAdd);
			} else {
				nbAPartyList.add(partyToAdd);
				partyMap.put(partyToAdd.getId(), partyToAdd.getId());
			}
		}
	}
	
	protected void updateRelations(String nbaPartyId, String bePartyId) {
		if (nbaPartyId != null && bePartyId != null) {
			List nbaRelations = nbAContract.getOLifE().getRelation();
			Iterator nbARelationsItr = nbaRelations.iterator();
			while (nbARelationsItr.hasNext()) {
				Relation nbARelation = (Relation) nbARelationsItr.next();
				List beRelations = getMatchingRelations(beContract, nbARelation);
				Iterator beRelationsItr = beRelations.iterator();
				while (beRelationsItr.hasNext()) {
					Relation beRelation = (Relation) beRelationsItr.next();
					if (beRelation.getOriginatingObjectType() == OLI_PARTY && nbARelation.getOriginatingObjectID().equals(nbaPartyId)
							&& beRelation.getOriginatingObjectID().equals(bePartyId)) {
						nbARelation.setOriginatingObjectID(bePartyId);
					}
					if (beRelation.getRelatedObjectType() == OLI_PARTY && nbARelation.getRelatedObjectID().equals(nbaPartyId)
							&& beRelation.getRelatedObjectID().equals(bePartyId)) {
						nbARelation.setRelatedObjectID(bePartyId);
					}
				}
			}
		}
	}
	
	protected void mergeParty(Party nbAParty, Party beParty) throws NbaBaseException {
		CopyBox partyCopyBox = CopyManager.getCopyBox(OLI_PARTY);
		partyCopyBox.copy(nbAParty, beParty);
		PersonOrOrganization bePersonOrOrganization = beParty.getPersonOrOrganization();
		PersonOrOrganization nbAPersonOrOrganization = nbAParty.getPersonOrOrganization();
		if (bePersonOrOrganization.isPerson()) {
			mergePerson(nbAPersonOrOrganization.getPerson(), bePersonOrOrganization.getPerson());
		} else {
			mergeOrganization(nbAPersonOrOrganization.getOrganization(), bePersonOrOrganization.getOrganization());
		}
	}
	
	protected boolean checkForPartyDeletion(String partyId) {
		if (partyId != null) {
			Set set = partyMap.entrySet();
			int count = 0;
			Iterator itr = set.iterator();
			while (itr.hasNext()) {
				Map.Entry me = (Map.Entry) itr.next();
				if (partyId.equals(me.getValue())) {
					count++;
				}
			}
			if (count > 1) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Merges 2 PartyExtension Objects and its children <BR>
	 * This method merges the details from bePartyExt into the reference of nbAPartyExt. This method then returns nbAPartyExt's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if bePartyExt is null, return nbAPartyExt
	 * 		if nbAPartyExt is null, return bePartyExt. (CAUTION: Any changes to nbAPartyExt member objects references will affect the state of bePartyExt.)
	 * 	  </code>
	 * 
	 * @param nbAParty a Party object 
	 * @param beParty a Party object
	 */
	//New Method AXAL3.7.04
	protected void mergePartyExtension(Party nbAParty, Party beParty) throws NbaBaseException {
		PartyExtension nbAPartyExt = NbaUtils.getFirstPartyExtension(nbAParty);
		PartyExtension bePartyExt = NbaUtils.getFirstPartyExtension(beParty);
		if (nbAPartyExt != null && bePartyExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_PARTY);
			copyBox.copy(nbAPartyExt, bePartyExt);
		}
	}
	/**
	 * Merges 2 PersonExtension Objects and its children <BR>
	 * This method merges the details from bePersonExt into the reference of nbAPersonExt. This method then returns nbAPersonExt's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if bePersonExt is null, return nbAPersonExt
	 * 		if nbAPersonExt is null, return bePersonExt. (CAUTION: Any changes to firstHolding member objects references will affect the state of bePersonExt.)
	 * 	  </code>
	 * 
	 * @param nbAPerson a Person object 
	 * @param bePerson a Person object
	 */
	//New Method AXAL3.7.04
	protected void mergePersonExtension(Person nbAPerson, Person bePerson) throws NbaBaseException {
		PersonExtension nbAPersonExt = NbaUtils.getFirstPersonExtension(nbAPerson);
		PersonExtension bePersonExt = NbaUtils.getFirstPersonExtension(bePerson);
		if (nbAPersonExt != null && bePersonExt != null) {
			CopyBox copyBox = CopyManager.getCopyBox(EXTCODE_PERSON);
			copyBox.copy(nbAPersonExt, bePersonExt);
		}
	}
	
	/**
	 * Merges 2 Person Objects and its children <BR>
	 * This method merges the details from bePerson into the reference of nbAPerson. This method then returns nbAPerson's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if bePerson is null, return nbAPerson
	 * 		if nbAPerson is null, return bePerson. (CAUTION: Any changes to nbAPerson member objects references will affect the state of bePerson.)
	 * 	  </code>
	 * 
	 * @param nbAPerson a Person object 
	 * @param bePerson a Person object
	 */
	//New Method AXAL3.7.04
	protected void mergePerson(Person nbAPerson, Person bePerson) throws NbaBaseException {
		if (nbAPerson != null && bePerson != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_PERSON);
			copyBox.copy(nbAPerson, bePerson);
			//merge PersonExtension tag
			mergePersonExtension(nbAPerson, bePerson);
		}
	}
	
	/**
	 * Merges 2 Organization Objects and its children <BR>
	 * This method merges the details from beOrganization into the reference of nbAOrganization. This method then returns nbAOrganization's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beOrganization is null, return nbAOrganization
	 * 		if nbAOrganization is null, return beOrganization. (CAUTION: Any changes to nbAOrganization member objects references will affect the state of beOrganization.)
	 * 	  </code>
	 * 
	 * @param nbAOrganization a Organization object 
	 * @param beOrganization a Organization object
	 */
	//New Method AXAL3.7.04
	protected void mergeOrganization(Organization nbAOrganization, Organization beOrganization) throws NbaBaseException {
		if (nbAOrganization != null && beOrganization != null) {
			CopyBox copyBox = CopyManager.getCopyBox(OLI_ORGANIZATION);
			copyBox.copy(nbAOrganization, beOrganization);
		}
	}

	/**
	 * Merges all Relation Objects and its children <BR>
	 * This method merges the details from beRelation into the reference of nbaRelation. This method then returns nbaRelation's updated reference. <BR>
	 * The logic of this method handles null arguments as following :<BR>
	 * <code>
	 * 		if beRelation is null, return nbaRelation
	 * 		if nbaRelation is null, return beRelation. (CAUTION: Any changes to nbaRelation member objects references will affect the state of beRelation.)
	 * 	  </code>
	 * 
	 * @param nbATXLifeCopy a NbaTXLife object 
	 * @param beTXLifeCopy a NbaTXLife object
	 */
	//Refactored AXAL3.7.04
	protected void mergeRelations(NbaTXLife nbATXLifeCopy, NbaTXLife beTXLifeCopy) throws NbaBaseException {
		CopyBox relationCopyBox = CopyManager.getCopyBox(OLI_RELATION);
		Iterator beRelationsItr = beTXLifeCopy.getOLifE().getRelation().iterator();
		while (beRelationsItr.hasNext()) {
			Relation beRelation = (Relation) beRelationsItr.next();
			replaceHoldingId(nbATXLifeCopy, beTXLifeCopy, beRelation);
			List nbaRelations = getMatchingRelations(nbATXLifeCopy, beRelation);
			Iterator nbaRelationsItr = nbaRelations.iterator();
			while (nbaRelationsItr.hasNext()) {
				Relation nbARelation = (Relation) nbaRelationsItr.next();
				if (beRelation.getOriginatingObjectType() == OLI_PARTY && partyMap.get(beRelation.getOriginatingObjectID()) != null) {
					beRelation.setOriginatingObjectID((String) partyMap.get(beRelation.getOriginatingObjectID()));
				}
				if (beRelation.getRelatedObjectType() == OLI_PARTY && partyMap.get(beRelation.getRelatedObjectID()) != null) {
					beRelation.setRelatedObjectID((String) partyMap.get(beRelation.getRelatedObjectID()));
				}
				if (Matcher.match(nbARelation, beRelation)) {
					relationCopyBox.copy(nbARelation, beRelation);
				} else {
					getLogger().logDebug("Admin Relation [Relation ID : " + beRelation.getId() + " ] not found in nbA");
				}
			}
		}
	}
	/**
	 * Replace the OriginatingObjectID and  RelatedObjectID in beRelation with the corresponding ids from nbARelation.
	 * <code>
	 * 		if OriginatingObjectType is OLI_HOLDING, replace the OriginatingObjectID
	 * 		if RelatedObjectType is OLI_HOLDING, replace the RelatedObjectID
	 * 	  </code>
	 * 
	 * @param nbATXLifeCopy a NbaTXLife object 
	 * @param beTXLifeCopy a NbaTXLife object
	 * @param beRelation a Relation object
	 */
	//New Method AXAL3.7.04
	protected void replaceHoldingId(NbaTXLife nbATXLifeCopy, NbaTXLife beTXLifeCopy, Relation beRelation) {
		String beHoldingID = beTXLifeCopy.getPrimaryHolding().getId();
		String nbAHoldingID = nbATXLifeCopy.getPrimaryHolding().getId();
		if (beRelation.getOriginatingObjectType() == OLI_HOLDING && NbaUtils.isEqual(beHoldingID, beRelation.getOriginatingObjectID())) {
			beRelation.setOriginatingObjectID(nbAHoldingID);
		}
		if (beRelation.getRelatedObjectType() == OLI_HOLDING && NbaUtils.isEqual(beHoldingID, beRelation.getRelatedObjectID())) {
			beRelation.setRelatedObjectID(nbAHoldingID);
		}
	}
	
	//AXAL3.7.04 Code Deleted
	
	/**
	 * This method creates a map which contains the partyId of backend contract as key and corresponding partyId
	 * of pending contract as value. The corresponding party in the nbA contract is identified based on
	 * RelationRoleCode,  OriginatingObjectType and RelatedObjectType of pending contract Relation object.
	 * @param nbAHoldingInquiry NbaTxLife object contains pending contract
	 * @param backEndHoldingInquiry NbaTxLife object contains backend contract
	 */
	//New Method AXAL3.7.04 ALS5579 ALS5353
	protected void initialize() {
		List roleCodes = getAllRoleCodes(beContract);
		for (int k = 0; k < roleCodes.size(); k++) {
			List relationsForRole = getRelationsForRoleCode(beContract, ((Long) roleCodes.get(k)).longValue());
			Iterator relationItr = relationsForRole.iterator();
			List unmatchedNbaParties = new ArrayList();
			List unmatchedAdminParties = new ArrayList();
			List nbARelations = new ArrayList();
			if (!relationsForRole.isEmpty()) {
				Relation beRel =  (Relation) relationsForRole.get(0);
				if(beRel.getRelatedObjectType() == OLI_PARTY) {
					nbARelations = getMatchingRelations(nbAContract, beRel);
					unmatchedNbaParties = getAllParties(nbARelations);	
				}
			}
			while (relationItr.hasNext()) {
				Relation beRelation = (Relation) relationItr.next();
				//holding to party and party to party relations
				if (beRelation.getRelatedObjectType() == OLI_PARTY) {

					if (!nbARelations.isEmpty()) {
						if (nbARelations.size() == 1) {
							Relation nbARelation = (Relation) nbARelations.get(0);
							if (nbARelation.getRelatedObjectType() == OLI_PARTY) {
								partyMap.put(beRelation.getRelatedObjectID(), nbARelation.getRelatedObjectID());
								unmatchedNbaParties.remove(nbARelation.getRelatedObjectID());
							}
						} else {
							Iterator nbARelItr = nbARelations.iterator();
							while (nbARelItr.hasNext()) {
								Relation rel = (Relation) nbARelItr.next();
								if (rel.getRelatedObjectType() == OLI_PARTY && !partyMap.containsKey(beRelation.getRelatedObjectID())) {
									if (Matcher.match(nbAContract.getParty(rel.getRelatedObjectID()).getParty(), beContract.getParty(
											beRelation.getRelatedObjectID()).getParty())) {
										partyMap.put(beRelation.getRelatedObjectID(), rel.getRelatedObjectID());
										unmatchedNbaParties.remove(rel.getRelatedObjectID());
										break;
									}
								}
							}
							if (!partyMap.containsKey(beRelation.getRelatedObjectID())) {
								unmatchedAdminParties.add(beRelation.getRelatedObjectID());
							}
						}
					}
				}
			}
			for (int j = 0; j < unmatchedAdminParties.size(); j++) {
				partyMap.put(unmatchedAdminParties.get(j), unmatchedNbaParties.get(j));
			}
		}
	}
	
	
	//New Method ALS5579 ALS5353
	public List getAllParties(List relations) {
		List relatedParties = new ArrayList();
		for(int i=0;i<relations.size();i++) {
			Relation relation = (Relation) relations.get(i);
			relatedParties.add(relation.getRelatedObjectID());
		}
		return relatedParties;
	}
	
	//New Method ALS5579 ALS5353
	public List getRelationsForRoleCode(NbaTXLife nbaTXLife,long roleCode) {
		List relations = new ArrayList();
		if(nbaTXLife != null) {
			OLifE olife = nbaTXLife.getOLifE();
	        Relation relation = null;
	        int relationCount = olife.getRelationCount();
	        for (int index = 0; index < relationCount; index++) {
	            relation = olife.getRelationAt(index);
	            if (relation.getRelationRoleCode() == roleCode) {
	                relations.add(relation);
	            }
	        }	
		}
        return relations;
	}
	
	//New Method ALS5579 ALS5353
	public List getAllRoleCodes(NbaTXLife nbaTXLife) {
		List relations = nbaTXLife.getOLifE().getRelation();
		List roleCodes = new ArrayList();
		for(int i=0;i<relations.size();i++) {
			Relation relation = (Relation) relations.get(i);
			if(!roleCodes.contains(new Long(relation.getRelationRoleCode()))) {
				roleCodes.add(new Long(relation.getRelationRoleCode()));
			}
		}
		return roleCodes;
	}

	/**
	 * This method finds the list of matching relations in nbA contract for a given relation from backend system.
	 * It match the relation objects on the basis of of OriginatingObjectType, RelatedObjectType and RelationRoleCode
	 * @param holdingInquiry NbaTxLife object contains pending contract
	 * @param beRelation Relation object contains backend relation object
	 */
	//New Method AXAL3.7.04
	protected List getMatchingRelations(NbaTXLife holdingInquiry, Relation beRelation) {
		List relationsList = new ArrayList();
		Iterator relationItr = holdingInquiry.getOLifE().getRelation().iterator();
		while (relationItr.hasNext()) {
			Relation nbARelation = (Relation) relationItr.next();
			if (NbaUtils.isEqual(nbARelation.getOriginatingObjectType(), beRelation.getOriginatingObjectType())
					&& NbaUtils.isEqual(nbARelation.getRelatedObjectType(), beRelation.getRelatedObjectType())
					&& NbaUtils.isEqual(nbARelation.getRelationRoleCode(), beRelation.getRelationRoleCode())){
				relationsList.add(nbARelation);
			}
		}
		return relationsList;
	}
	
	/**
	 * @return Returns the beContract.
	 */
	//New Method AXAL3.7.04
	public NbaTXLife getbeContract() {
		return beContract;
	}
	/**
	 * @param beContract The beContract to set.
	 */
	//New Method AXAL3.7.04
	public void setbeContract(NbaTXLife beContract) {
		this.beContract = beContract;
	}
	/**
	 * @return Returns the nbAContract.
	 */
	//New Method AXAL3.7.04
	public NbaTXLife getNbAContract() {
		return nbAContract;
	}
	/**
	 * @param nbAContract The nbAContract to set.
	 */
	//New Method AXAL3.7.04
	public void setNbAContract(NbaTXLife nbAContract) {
		this.nbAContract = nbAContract;
	}
    /**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	//New Method AXAL3.7.04
    protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass()); //P2AXAL016CV
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory."); //P2AXAL016CV
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
    
	/**
	 * Reset vendorCode1 with vendorCode2 in nbaTXLife extension objects.
	 * @param nbaTXLife - a NbaTXLife
	 * @param vendorCode1
	 * @param vendorCode2
	 */
    //AXAL3.7.04 New Method
	protected NbaTXLife replaceVendorId(NbaTXLife nbaTXLife, String vendorCode1, String vendorCode2) throws NbaBaseException {
		if (nbaTXLife != null && vendorCode1 != null && vendorCode2 != null) {
			String stringNbaTXLife = nbaTXLife.toXmlString();
			StringBuffer buff = new StringBuffer(stringNbaTXLife);
			int start = buff.indexOf(vendorCode1);
			while (start > 0 && start < buff.length()) {
				if (isVendorCode(buff, start)) {
					buff.replace(start, start + vendorCode1.length(), vendorCode2);
					start = buff.indexOf(vendorCode1, start + vendorCode2.length());
				} else {
					start = buff.indexOf(vendorCode1, start + vendorCode2.length());
				}
			}
			try {
				return new NbaTXLife(buff.toString());
			} catch (Exception e) {
				throw new NbaBaseException("Error while replacing VendorCode", e);
			}
		}
		return nbaTXLife;
	}
	
	/**
	 * Determine if the StringBuffer contains the String 'VendorCode="'  (ignoring white space) immediately preceding the start location
	 * @param buff - the StringBuffer to be examined
	 * @param start - the start location
	 * @return true if the condition is met
	 */
	//AXAL3.7.04 New Method
	protected boolean isVendorCode(StringBuffer buff, int start) {
		int curr = getIndexOfPrevCharacter(buff, start);
		if (curr > -1 && buff.substring(curr, curr + 1).equals(QUOTE)) { //Look for " character
			curr = getIndexOfPrevCharacter(buff, curr);
			if (curr > -1 && buff.substring(curr, curr + 1).equals(EQUALS)) { //Look for = character
				curr = getIndexOfPrevCharacter(buff, curr);
				return (curr > 0 && buff.substring(curr - 9, curr + 1).equalsIgnoreCase(VENDORCODE)); //Look for VendorCode string
			}
		}
		return false;
	}
	
	/**
	 * Determine the location of the first non SPACE character prior to the current location in the StringBuffer
	 * @param buff - the StringBuffer
	 * @param curr - the current location
	 * @return the location
	 */
	//AXAL3.7.04 New Method
	protected int getIndexOfPrevCharacter(StringBuffer buff, int curr) {
		int newLoc = curr -1;
		while (newLoc > -1 && buff.substring(newLoc, newLoc + 1).equals(SPACE)) { //ignore whitespace
			--newLoc;
		}
		return newLoc;
	}
	
	/**
     * To be used by Contract Change Business Functions.<BR>
     * nbA CAPS Re-Issue Change: Since most of the information is already in nbA, we are asking for a filter of the information from CAPS and keeping this information specific to what is required for a Reissue - in other words, we are only asking for the following:
     * 1) Status of the case in CAPS, and
     * 2) The actual address from CAPS (in the event it was changed between the policy issue time and the reissue request)
     * @return com.csc.fsg.nba.vo.NbaTXLife  This is a merged form of beContract and nbaContract objects.     
     * @throws NbaBaseException This exception is thrown in mentioned cases.
     * APSL4094, SR#653140
     */
    public NbaTXLife mergeBackendContractInfo() throws NbaBaseException {
        // if nbAHoldingInquiry is null returns the clone of backEndHoldingInquiry object.
        if (nbAContract == null) {
            return beContract;
        }
        // Merge parties info(address,email,phone)
        mergePartyInfo(nbAContract, beContract);
        NbaOLifEId olifeId = new NbaOLifEId(nbAContract);
        olifeId.resolveDuplicateIds(nbAContract);
        olifeId.resetIds(nbAContract);
        return nbAContract;
    }
    
    /**
     * Merge party information,verify that the party information(address,phone,email) received from CAPS is changed, If changed copy this information in nbaContract.
     * @param nbATXLifeCopy
     * @param beTXLifeCopy
     * @return 
     * APSL4094, SR#653140
     */
    protected void mergePartyInfo(NbaTXLife nbATXLifeCopy, NbaTXLife beTXLifeCopy) throws NbaBaseException {
        List bePartyList = beTXLifeCopy.getOLifE().getParty();
        List nbAPartyList = nbATXLifeCopy.getOLifE().getParty();
        Iterator bePartyItr = bePartyList.iterator();
        while (bePartyItr.hasNext()) {
            Party beParty = (Party) bePartyItr.next();
            Party nbAParty = NbaTXLife.getPartyFromId((String) partyMap.get(beParty.getId()), nbAPartyList);
            if (nbAParty != null && isInsurableParties(nbATXLifeCopy.getOLifE(), nbAParty.getId())) {
                if (Matcher.match(nbAParty, beParty)) {
                    mergePartyAddress(nbAParty, beParty);
                    mergePartyPhone(nbAParty, beParty);
                    mergePartyEmail(nbAParty, beParty);
                }
            }
        }

    }
    
    /**
     * This method is used to check insurable parties
     * @param oLife
     * @param partyId
     * @return boolean
     * APSL4094, SR#653140
     */
    protected boolean isInsurableParties(OLifE oLife, String partyId) {
        List relationList=NbaUtils.getRelationListbyRelatedObj(oLife, partyId);
        for(int i=0;i<relationList.size();i++){
//            if(NbaUtils.isInsurableRelation((Relation)relationList.get(i))){
                return true;
//            }
        }
        return false;
    }
        
    /**
     * This method is used to check for party address,if party address is changed in CAPS,copy address to nbaContract
     * @param nbAParty
     * @param beParty
     * @return
     * APSL4094, SR#653140
     */
    protected void mergePartyAddress(Party nbAParty, Party beParty) throws NbaBaseException {
        List nbAPartyAddressList = nbAParty.getAddress();
        List bePartyAddressList = beParty.getAddress();
        if (!NbaUtils.isBlankOrNull(bePartyAddressList) && bePartyAddressList.size() > 0) {
            for (int i = 0; i < bePartyAddressList.size(); i++) {
                Address bePartyAddress = (Address) bePartyAddressList.get(i);
                Address nbaPartyAddress= getMatchingAddress(nbAPartyAddressList,bePartyAddress);
                if (!Matcher.match(nbaPartyAddress, bePartyAddress)) {
                    CopyBox partyCopyBox = CopyManager.getCopyBox(OLI_ADDRESS);
                    partyCopyBox.copy(nbaPartyAddress, bePartyAddress);
                }else{
                    if (nbaPartyAddress==null && bePartyAddress!=null) {
                        nbAPartyAddressList.add(bePartyAddress);
                    } 
                }
            }
        }
    }
    
    /**
     * This method is used to check for party phone,if party phone is changed in CAPS,copy phone details to nbaContract
     * @param nbAParty
     * @param beParty
     * @return
     * APSL4094, SR#653140
     */
    protected void mergePartyPhone(Party nbAParty, Party beParty) throws NbaBaseException {
        List nbAPartyPhoneList = nbAParty.getPhone();
        List bePartyPhoneList = beParty.getPhone();
        if (!NbaUtils.isBlankOrNull(bePartyPhoneList) && bePartyPhoneList.size() > 0) {
            for (int i = 0; i < bePartyPhoneList.size(); i++) {
                Phone bePartyPhones = (Phone) bePartyPhoneList.get(i);
                Phone nbAPartyPhones =  getMatchingPhone(nbAPartyPhoneList,bePartyPhones);
                if (!Matcher.match(nbAPartyPhones, bePartyPhones)) {
                    CopyBox partyCopyBox = CopyManager.getCopyBox(OLI_PHONE);
                    partyCopyBox.copy(nbAPartyPhones, bePartyPhones);
                }else{
                    if (nbAPartyPhones==null && bePartyPhones!=null) {
                        nbAPartyPhoneList.add(bePartyPhones);
                    } 
                }
            }
        }
    }
    
    /**
     * This method is used to check for party email address,if party email address is changed in CAPS,copy phone email to nbaContract
     * @param nbAParty
     * @param beParty
     * @return
     * APSL4094, SR#653140
     */
    protected void mergePartyEmail(Party nbAParty, Party beParty) throws NbaBaseException {
        List nbAPartyEmailList = nbAParty.getEMailAddress();
        List bePartyEmailList = beParty.getEMailAddress();
        if (!NbaUtils.isBlankOrNull(bePartyEmailList)&& bePartyEmailList.size() > 0) {
            for (int i = 0; i < bePartyEmailList.size(); i++) {
                EMailAddress bePartyEMail = (EMailAddress) bePartyEmailList.get(i);
                EMailAddress nbaPartyEmail= getMatchingEmail(nbAPartyEmailList, bePartyEMail);
                if (!Matcher.match(nbaPartyEmail, bePartyEMail)) {
                    CopyBox partyCopyBox = CopyManager.getCopyBox(OLI_EMAILADDRESS);
                    partyCopyBox.copy(nbaPartyEmail, bePartyEMail);
                }else{
                    if (nbaPartyEmail==null && bePartyEMail!=null) {
                        nbAPartyEmailList.add(bePartyEMail);
                    } 
                }
            }
        }
    }
    
    /**
     * This method finds the matching address in nbA contract with becontract.
     * @param nbaPartyAddressList object contains party addresses
     * @param bePartyAddress object contains be party address
     * APSL4094, SR#653140
     */
    
    protected Address getMatchingAddress(List nbaPartyAddressList, Address bePartyAddress) {
        Address matchAddress = null;
         for (int i=0;i<nbaPartyAddressList.size();i++) {
             Address nbAAddress = (Address)nbaPartyAddressList.get(i) ;
            if(nbAAddress!=null && NbaUtils.isEqual(nbAAddress.getAddressTypeCode(), bePartyAddress.getAddressTypeCode())){
                matchAddress=nbAAddress;
            }
        }
        return matchAddress;
    }
    
    /**
     * This method finds the matching party Emailaddress in nbA contract with becontract.
     * @param nbaPartyEmailList object contains party email list
     * @param bePartyEmail object contains be party email
     * APSL4094, SR#653140
     */
    protected EMailAddress getMatchingEmail(List nbaPartyEmailList, EMailAddress bePartyEmail) {
        EMailAddress matchedEmail = null;
         for (int i=0;i<nbaPartyEmailList.size();i++) {
             EMailAddress nbAEmail = (EMailAddress)nbaPartyEmailList.get(i) ;
            if(nbAEmail!=null && NbaUtils.isEqual(nbAEmail.getEMailType(), bePartyEmail.getEMailType())){
                matchedEmail=nbAEmail;
            }
        }
        return matchedEmail;
    }
    
    /**
     * This method finds the matching party phone in nbA contract with becontract.
     * @param nbaPartyPhoneList object contains party phone list
     * @param bePartyPhone object contains be party phone
     * APSL4094, SR#653140
     */
    protected Phone getMatchingPhone(List nbaPartyPhoneList, Phone bePartyPhone) {
        Phone matchedPhone = null;
         for (int i=0;i<nbaPartyPhoneList.size();i++) {
             Phone nbAPhone = (Phone)nbaPartyPhoneList.get(i) ;
            if(nbAPhone!=null && NbaUtils.isEqual(nbAPhone.getPhoneTypeCode(), bePartyPhone.getPhoneTypeCode())){
                matchedPhone=nbAPhone;
            }
        }
        return matchedPhone;
    }
   
}
