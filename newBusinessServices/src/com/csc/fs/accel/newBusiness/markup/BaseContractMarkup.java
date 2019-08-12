package com.csc.fs.accel.newBusiness.markup;

/*
 * *******************************************************************************<BR>
 * Copyright 2014, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.util.List;

import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.Activity;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Endorsement;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.Grouping;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.Life;
import com.csc.fsg.nba.vo.txlife.LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.ReinsuranceOffer;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;


/**
 * Provides common functionality listed below for marking up ACORD transactions prior to
 * being sent externally of nbA.
 * <p>
 * The underwriter's secure notes which are stored as attachments to the <code>Holding</code>
 * are removed.  The following attachments at these levels are wrapped with a CDATA tag so
 * that the XML transactions inside these attachments won't be processed by a client side
 * parser.
 * <ul>
 * 		<li>Activity</li>
 * 		<li>Coverage</li>
 * 		<li>FormInstance</li>
 * 		<li>FormResponse</li>
 * 		<li>Grouping</li>
 * 		<li>Holding</li>
 * 		<li>Party</li>
 * 		<li>ReinsuranceOffer</li>
 * 		<li>RequirementInfo</li>
 * 		<li>SignatureInfo</li>
 * <ul>
 * <p>
 * Base system does not currently store any attachments in some of the aggregates above,
 * but this class checks them anyway in case they come in initially on a 103 transaction.
 * <p>
 * For denied parties, we remove any RequirementInfo instances that apply to the
 * denied party.  A denied party is not included in the Holding Inquiry and these
 * RequirementInfo instances would violate the IDREF linking to an ID that didn't exist
 * causing a parser validation error.  For this same reason, we are also checking for any
 * outstanding Relation instances referencing the denied party.
 * <p>
 * FormInstances will also be validated against their related objects.  An amendment/
 * endorsement could be made inactive due to a deny of benefit, coverage, or party and
 * the corresponding FormInstance should not be included in the transaction without its
 * referenced Endoresment.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL4508</td>Websphere 8.5.5 Upgrade</tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 */

public abstract class BaseContractMarkup {

	public static final String CDATA = "<![CDATA[";
	public static final String CDATA_END = "]]>";

	/**
	 * Process the top level objects of <code>OLifE</code>.
	 * @param olife
	 */
	protected void markupOLifE(NbaTXLife txLife) {
		markupActivities(txLife.getOLifE());
		markupFormInstances(txLife.getOLifE());
		markupGroupings(txLife.getOLifE());
		markupHoldings(txLife.getOLifE());
		markupParties(txLife.getOLifE());
  }

	/**
	 * Perform markup on the contract's top level <code>Activity</code> instances.
	 * @param olife
	 */
	protected void markupActivities(OLifE olife) {
		int count = olife.getActivityCount();
		for (int i = 0; i < count; i++) {
			Activity activity = olife.getActivityAt(i);
			markupAttachments(activity.getAttachment());
		}
	}

	/**
	 * Perform markup on the contract's top level <code>FormInstance</code> instances.
	 * @param olife
	 */
	protected void markupFormInstances(OLifE olife) {
		int count = olife.getFormInstanceCount();
		for (int i = 0; i < count; i++) {
			FormInstance formInstance = olife.getFormInstanceAt(i);
			markupAttachments(formInstance.getAttachment());
			markupFormResponses(formInstance.getFormResponse());
		}
	}

	/**
	 * Perform markup on the contract's <code>FormResponse</code> instances.
	 * @param responses
	 */
	protected void markupFormResponses(List<FormResponse> responses) {
		int count = responses.size();
		for (int i = 0; i < count; i++) {
			FormResponse formResponse = responses.get(i);
			markupAttachments(formResponse.getAttachment());
		}
	}

	/**
	 * Perform markup on the contract's top level <code>Grouping</code> instances.
	 * Markup any attachments found on a grouping.
	 * @param olife
	 */
	protected void markupGroupings(OLifE olife) {
		int count = olife.getGroupingCount();
		for (int i = 0; i < count; i++) {
			Grouping grouping = olife.getGroupingAt(i);
			markupAttachments(grouping.getAttachment());
		}
	}

	/**
	 * Perform markup on the contract's top level <code>Holding</code> instances.
	 * Remove any underwriter's notes, markup any attachments, and perform the necessary
	 * markup on the policy.
	 * @param olife
	 */
	protected void markupHoldings(OLifE olife) {
		int count = olife.getHoldingCount();
		for (int i = 0; i < count; i++) {
			Holding holding = olife.getHoldingAt(i);
			if (holding.getAttachment() != null) {
				removeUnderwriterNotes(holding);
				markupAttachments(holding.getAttachment());
			}
			if (holding.getPolicy() != null) {
				markupPolicy(holding.getPolicy());
			}
		}
	}

	/**
	 * Remove any underwriter's notes which are stored as attachments on the primary
	 * <code>Holding</code>.  These notes are only to be viewed by underwriters and will
	 * not be included on any holding inquiry outside of nbA.
	 * @param holding
	 */
	protected void removeUnderwriterNotes(Holding holding) {
		for (int i = holding.getAttachmentCount() - 1; i >= 0; i--) {
			Attachment attachment = holding.getAttachmentAt(i);
			if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE) {
				holding.removeAttachmentAt(i);
			}
		}
	}
	
	protected void removeUnderwriterNotesforParty(Party party) {
		for (int i = party.getAttachmentCount() - 1; i >= 0; i--) {
			Attachment attachment = party.getAttachmentAt(i);
			if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_UNDRWRTNOTE) {
				party.removeAttachmentAt(i);
			}
		}
	}	
	
	/**
	 * Perform markup on the contract's <code>Policy</code> instances.  Perform markup
	 * on any requirements or the <code>Life</code> instance.   
	 * @param policy
	 */
	protected void markupPolicy(Policy policy) {
		markupRequirements(policy.getRequirementInfo());
		LifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty ladh = policy.getLifeOrAnnuityOrDisabilityHealthOrPropertyandCasualty();
		if (ladh != null) {
			if (ladh.isLife()) {
				markupLife(ladh.getLife());
			}
		}
	}

	/**
	 * Perform markup on the contract's <code>Life</code> instance.  Perform markup on
	 * each coverage.
	 * @param life
	 */
	protected void markupLife(Life life) {
		int count = life.getCoverageCount();
		for (int i = 0; i < count; i++) {
			markupCoverage(life.getCoverageAt(i));
		}
	}

	/**
	 * Perform markup on the contract's <code>Coverage</code> instances.  Perform markup
	 * on any attachments and reinsurance offer's.
	 * @param coverage
	 */
	protected void markupCoverage(Coverage coverage) {
		markupAttachments(coverage.getAttachment());

		CoverageExtension coverageExt = NbaUtils.getFirstCoverageExtension(coverage);
		if (coverageExt != null) {
			int count = coverageExt.getReinsuranceOfferCount();
			for (int i = 0; i < count; i++) {
				markupReinsuranceOffer(coverageExt.getReinsuranceOfferAt(i));
			}
		}
	}

	/**
	 * Perform markup on the contract's <code>ReinsuranceOffer</code> instances.
	 * @param offer
	 */
	protected void markupReinsuranceOffer(ReinsuranceOffer offer) {
		markupAttachments(offer.getAttachment());
	}

	/**
	 * Perform markup on a list of <code>RequirementInfo</code> instances.
	 * @param requirements
	 */
	protected void markupRequirements(List<RequirementInfo> requirements) {
		int count = requirements.size();
		for (int i = 0; i < count; i++) {
			RequirementInfo req = requirements.get(i);
			markupAttachments(req.getAttachment());
		}
	}

	/**
	 * Perform markup on the contract's top level <code>Party</code> instances.
	 * @param olife
	 */
	protected void markupParties(OLifE olife) {
		for (int i = olife.getPartyCount() - 1; i >= 0; i--) {
			Party party = olife.getPartyAt(i);
			removeUnderwriterNotesforParty(party);
			markupAttachments(party.getAttachment());
		}
	}

	protected void markupAttachments(List<Attachment> attachments) {
		int count = attachments.size();
		for (int i = 0; i < count; i++) {
			Attachment attachment = attachments.get(i);
			AttachmentData data = attachment.getAttachmentData();
			if (data != null && data.getPCDATA() != null && !data.getPCDATA().contains(CDATA)) {
				String datanew=data.getPCDATA();
				StringBuilder sb = new StringBuilder(datanew.length() + 20);
				sb.append(CDATA).append(datanew).append(CDATA_END);
				data.setPCDATA(sb.toString());
			}
		}
	}


	/**
	 * Returns true if the referenced object is found on the NbaTXLife and is not
	 * marked as denied.
	 * @param nbatxlife
	 * @param type - originating or related object type  
	 * @param id - unique identifier
	 * @return
	 */
	protected boolean isNotReferencedObject(NbaTXLife nbatxlife, long type, String id) {
		if (type == NbaOliConstants.OLI_HOLDING) {
			return nbatxlife.getHolding(id) == null;
		} else if (type == NbaOliConstants.OLI_PARTY) {
//			NbaParty party = nbatxlife.getParty(id);
//			return party == null ||
//					(party.getPartyStatus() != NbaOliConstants.OLI_TC_NULL &&
//					 party.getPartyStatus() != NbaOliConstants.OLI_CLISTAT_ACTIVE);
//		} else if (type == NbaOliConstants.OLI_LIFECOVERAGE) {
			Coverage coverage = nbatxlife.getCoverage(id);
			return coverage == null ||
					(coverage.getLifeCovStatus() != NbaOliConstants.OLI_TC_NULL &&
					 coverage.getLifeCovStatus() != NbaOliConstants.OLI_POLSTAT_ACTIVE);
		} else if (type == NbaOliConstants.OLI_REQUIREMENTINFO) {
			return !isExistingRequirement(nbatxlife.getPolicy(), id);
		} else if (type == NbaOliConstants.OLI_FORMINSTANCE) {
			return nbatxlife.getFormInstance(id) == null;
		} else if (type == NbaOliConstants.OLI_ENDORSEMENT) {
			Policy policy = nbatxlife.getPolicy();
			int count = policy.getEndorsementCount();
			for (int i = 0; i < count; i++) {
				Endorsement endorsement = policy.getEndorsementAt(i);
				if (id.equals(endorsement.getId())) {
					return false;
				}
			}
			//Endorsement is not found
			return true;
		}
		return false;
	}

	/**
	 * Returns true if the FormInstance.RelatedObjectID can not be found on the contract.
	 * This method checks the Party and Endorsement collections to verify that this
	 * related instance has not been denied or made in-active.
	 * @param nbatxlife
	 * @param formInstance
	 * @return
	 */
	protected boolean isNotReferenced(NbaTXLife nbatxlife, FormInstance formInstance) {
		String relatedID = formInstance.getRelatedObjectID();
		if (relatedID == null) {
			return false;
		}
		//in some form instances we are not setting the related object type, so we must
		//check both party and endorsement collections
		if (formInstance.getRelatedObjectType() == NbaOliConstants.OLI_TC_NULL) {
			return isNotReferencedObject(nbatxlife, NbaOliConstants.OLI_PARTY, relatedID) &&
					isNotReferencedObject(nbatxlife, NbaOliConstants.OLI_ENDORSEMENT, relatedID);
		} else if (formInstance.getRelatedObjectType() == NbaOliConstants.OLI_ENDORSEMENT) {
			return isNotReferencedObject(nbatxlife, NbaOliConstants.OLI_ENDORSEMENT, relatedID);
		} else if (formInstance.getRelatedObjectType() == NbaOliConstants.OLI_PARTY) {
			return isNotReferencedObject(nbatxlife, NbaOliConstants.OLI_PARTY, relatedID);
		} else if (formInstance.getRelatedObjectType() == NbaOliConstants.OLI_MEDCONDITION) {
			return false;
		} else if (formInstance.getRelatedObjectType() == NbaOliConstants.OLI_LIFESTYLEACTIVITY) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if the <code>RequirementInfo</code> instance can be found on
	 * the policy.
	 * @param policy
	 * @param id
	 * @return
	 */
	protected boolean isExistingRequirement(Policy policy, String id) {
		if (policy != null && id != null) {
			int count = policy.getRequirementInfoCount();
			for (int i = 0; i < count; i++) {
				RequirementInfo req = policy.getRequirementInfoAt(i);
				if (id.equals(req.getId())) {
					return true;
				}
			}
		}
		return false;
	}
	
}