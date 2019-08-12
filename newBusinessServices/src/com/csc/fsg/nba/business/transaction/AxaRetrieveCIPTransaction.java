/*
 * ************************************************************** <BR>
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */

package com.csc.fsg.nba.business.transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeConstants;
import com.csc.fsg.nba.business.transaction.datachange.AxaDataChangeEntry;
import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.AxaUtils;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.evaluate.GenerateEvaluateWorkItemBP;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaEvaluateRequest;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSearchResultVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSConstants;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvoker;
import com.csc.fsg.nba.webservice.invoke.AxaWSInvokerFactory;

/**
 * 
 * This class encapsulates checks whenever following changes are made to the Insured or Owner roles on the policy. - Name. - Address. - Tax
 * Identification. - Tax Identification Type. - Gender/Sex. - Date of Birth and following changes are made on a contract - Policy Status - Plan Change
 * - Agent information
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.25</td>
 * <td>AXA Life Phase 2</td>
 * <td>Client Interface</td>
 * <td>AXAL3.7.07</td>
 * <td>AXA Life Phase 2</td>
 * <td>Data Change Architecture</td>
 * </tr>
 * </table>
 * <p>
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
// APSL4412 - New Class
public class AxaRetrieveCIPTransaction extends AxaDataChangeTransaction {
	protected NbaLogger logger = null;
	protected static long[] changeTypes = { DC_OWNER_CHANGE, DC_OWNER_ADDED,
			DC_OWNER_NAME,
			DC_OWNER_SSN,
			DC_OWNER_SSNTYPE,
			DC_OWNER_PARTY_TYPE,
			DC_OWNER_ADDRESS,
			DC_OWNER_DOB,
			//DC_OWNER_GENDER, //NBLXA-2152
			// Begin NBLXA-1352
			DC_INSURED_CHANGE, DC_INSURED_ADDED, DC_INSURED_FIRSTNAME, DC_INSURED_LASTNAME, DC_INSURED_ADDRESS, DC_INSURED_PARTY_TYPE,
			DC_INSURED_DOB, DC_INSURED_GENDER, DC_INSURED_SSN,
			DC_JNT_INSURED_CHANGE,
			DC_JNT_INSURED_ADDED,
			DC_JNT_INSURED_FIRSTNAME,
			DC_JNT_INSURED_LASTNAME,
			DC_JNT_INSURED_ADDRESS,
			DC_JNT_INSURED_PARTY_TYPE,
			DC_JNT_INSURED_DOB,
			DC_JNT_INSURED_GENDER,
			DC_JNT_INSURED_SSN,
			// End NBLXA-1352
			// NBLXA-1254 Start
			DC_BENOWNER_NAME, DC_BENOWNER_DOB, DC_BENOWNER_ADDRESS, DC_BENOWNER_SSN, DC_BENOWNER_ADD, DC_BENOWNER_CHANGE, DC_TRUSTEEOWNER_NAME,
			DC_TRUSTEEOWNER_DOB, DC_TRUSTEEOWNER_ADDRESS, DC_TRUSTEEOWNER_SSN, DC_TRUSTEEOWNER_ADD, DC_TRUSTEEOWNER_CHANGE, DC_CONTPERSONOWNER_NAME,
			DC_CONTPERSONOWNER_DOB, DC_CONTPERSONOWNER_ADDRESS, DC_CONTPERSONOWNER_SSN, DC_CONTPERSONOWNER_ADD, DC_CONTPERSONOWNER_CHANGE,
			DC_AUTHPERSONOWNER_NAME, DC_AUTHPERSONOWNER_DOB, DC_AUTHPERSONOWNER_ADDRESS, DC_AUTHPERSONOWNER_SSN, DC_AUTHPERSONOWNER_ADD,
			DC_AUTHPERSONOWNER_CHANGE,
	// NBLXA-1254 End
			DC_OWNER_BAE_CHANGE //NBLXA-2152
	};

	// NBLXA-1352 start
	protected static long[] primaryInsuredChangeTypes = { DC_INSURED_CHANGE, DC_INSURED_ADDED, DC_INSURED_FIRSTNAME, DC_INSURED_LASTNAME,
			DC_INSURED_ADDRESS, DC_INSURED_PARTY_TYPE, DC_INSURED_DOB, DC_INSURED_GENDER, DC_INSURED_SSN };

	protected static long[] jointInsuredChangeTypes = { DC_JNT_INSURED_CHANGE, DC_JNT_INSURED_ADDED, DC_JNT_INSURED_FIRSTNAME,
			DC_JNT_INSURED_LASTNAME, DC_JNT_INSURED_ADDRESS, DC_JNT_INSURED_PARTY_TYPE, DC_JNT_INSURED_DOB, DC_JNT_INSURED_GENDER, DC_JNT_INSURED_SSN };

	// NBLXA-1352 end

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * 
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#callInterface(com.csc.fsg.nba.vo.NbaTXLife)
	 */
	protected NbaDst callInterface(NbaTXLife nbaTxLife, NbaUserVO user, NbaDst nbaDst) throws NbaBaseException {
		if (isCallNeeded()
				&& !nbaTxLife.isInformalApplication()
				&& !(NbaConstants.PROC_APP_SUBMIT.equalsIgnoreCase(nbaTxLife.getBusinessProcess()) || NbaConstants.PROC_GI_APP_SUBMIT
						.equalsIgnoreCase(nbaTxLife.getBusinessProcess())) ) { // NBLXA-1770
			doCIPInquiry(nbaTxLife, user, nbaDst);
		}
		return nbaDst;
	}

	/*
	 * This method first iterates over all the owner parties and invokes CIP Request for each. It adds the response from the cip service to the
	 * attachment of the party.
	 */
	// APSL4412 - New Method
	protected void doCIPInquiry(NbaTXLife nbaTXLife, NbaUserVO userVO, NbaDst nbaDst) throws NbaBaseException { // SR564247(APSL2525) - New Method
		if (nbaTXLife != null) {
			// NBLXA-1254 Begins
			Iterator registerChangesItr = registeredChanges.iterator();
			
			while (registerChangesItr.hasNext()) {
				AxaDataChangeEntry change = (AxaDataChangeEntry) registerChangesItr.next();
				Party party = NbaTXLife.getPartyFromId(change.getChangedObjectId(), nbaTXLife.getOLifE().getParty());
				// NBLXA-2152 Starts
				if ((nbaTXLife.isInsured(party.getId()) || nbaTXLife.isJointInsured(party.getId())) && !nbaTXLife.isOwner(party.getId())) { // NBLXA-2152
					deleteCDDAttachment(nbaTXLife, party, NbaOliConstants.OLI_ATTACH_1009800001); // NBLXA-2152
				} else {
					if (change.getChangeType() == AxaDataChangeConstants.DC_OWNER_BAE_CHANGE) {
						deleteCDDAttachment(nbaTXLife, party, NbaOliConstants.OLI_ATTACH_BAE);
					}
					deleteCDDAttachment(nbaTXLife, party, NbaOliConstants.OLI_ATTACH_BRIDGERCIP_1009800015); // NBLXA-2152
				}
				// NBLXA-2152 Ends

			}
		//NBLXA-2152 Code moved to method CreateReEvalWI 
			 createReEvalWI( nbaTXLife, userVO, nbaDst); 
			// NBLXA-1254 Ends.
		}
	}

	// NBLXA-1254 New method
	private void deleteCDDAttachment(NbaTXLife nbaTXLife, Party party, long attachmentType) {
		if (party != null) {
			List attachmentList = AxaUtils.getAttachmentsByType(party, attachmentType);
			if (attachmentList.size() > 0) {
				Attachment attachment = (Attachment) attachmentList.get(0);
				attachment.getAttachmentData().setPCDATA(NbaConstants.CDD_RETRIGGER);
				attachment.getAttachmentData().setActionUpdate();
			}

		}
	}
	// NBLXA-1254 New method
	private void createReEvalWI(NbaTXLife nbaTXLife,NbaUserVO userVO,NbaDst nbaDst) {
		if (!nbaTXLife.isSIApplication()) {
			NbaEvaluateRequest req = new NbaEvaluateRequest();
			req.setNbaUserVO(userVO);
			req.setWork(nbaDst);
			req.setContract(nbaTXLife);
			req.setUserFunction("NBEVAL");
			req.setOverrideContractCommit(true);
			req.setResetUWWB(true);
			GenerateEvaluateWorkItemBP newReEvalWorkItem = new GenerateEvaluateWorkItemBP();
			Result result = newReEvalWorkItem.process(req);
			if (result.hasErrors()) {
				getLogger().logError("Error creating NBREEVAL work item for contract " + nbaTXLife.getPolicy().getPolNumber());
			}
		}
	}

		//NBLXA-2152 Removed method createTransactionForCIP 

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaBusinessTransaction#getDataChangeTypes()
	 */
	protected long[] getDataChangeTypes() {
		return changeTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.csc.fsg.nba.business.transaction.AxaDataChangeTransaction#isTransactionAlive()
	 */
	protected boolean isTransactionAlive() {
		return NbaUtils.isConfigCallEnabled(NbaConfigurationConstants.ENABLE_CLIENT_INTERFACE_CALL);
	}

	/**
	 * 
	 * @param resultCode
	 * @return
	 */
	// ALS3374 new method
	protected boolean errorStop(long resultCode) {
		if (NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == resultCode || NbaOliConstants.TC_RESINFO_SECVIOLATION == resultCode) {
			return true;
		}
		return false;
	}


}
