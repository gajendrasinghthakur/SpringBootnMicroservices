package com.csc.fsg.nba.business.transaction;
/*
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
import java.util.Iterator;
import java.util.List;

import com.csc.fs.CloneObject;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.business.process.NbaProcessWorkItemProvider;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaMIBCodedReport;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentData;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.FormInstanceExtension;
import com.csc.fsg.nba.vo.txlife.FormResponse;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonExtension;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Policy;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;
/** 
 * 
 * This class provides utility function for MIB report processing.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN023</td><td>Version 5</td><td>Automatic MIB Update</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>NBA208-36</td><td>Version 7</td><td>Deferred Work Item Retrieval</td></tr>
 * <tr><td>SPR3310</td><td>Version 8</td><td>MIB codes are not automatically transmitted</td></tr>
 * <tr><td>ALS5017</td><td>AXA Life Phase 1</td><td>QC # 4181  - 3.7.31 MIB - reported codes did not transmit to MIB upon final disposition</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaMIBReportUtils {
	protected NbaTXLife nbaTxLife = null;
	protected NbaUserVO user = null;
	protected NbaOLifEId olifeId = null;
	protected boolean addedMIBReport = false;
	protected NbaDst nbaDstWithAllTransactions = null; //ALS5017
	/**
	 * NbaMIBReportUtils constructor.
	 * @param nbaTxLife the contract NbaTXLife object 
	 */
	public NbaMIBReportUtils(NbaTXLife contract, NbaUserVO user) {
		super();
		setNbaTxLife(contract);
		setUser(user);
	}
	/**
	 * This method iterate throgh all MIB requirements on the contract and call processMIBReportsForAMIBTransaction
	 * for each MIB requirement for MIB report processing. This method also updates the AWD and backend database for
	 * any added tempraroty workitem to transmit MIB reports.
	 * @param nbaDst the case workitem
	 * @param processZeroMIBReport if true it will process impairment MIB codes even if there is no un-transmitted report available on the MIB requirement
	 * @return the updated case workitem
	 * @throws NbaBaseException
	 */
	public NbaDst processMIBReportsForAContract(NbaDst nbaDst, boolean processZeroMIBReport) throws NbaBaseException {
		List transactions = getNbaDstWithAllTransactions().getNbaTransactions();//ALS5017 //ALS5344
		NbaDst clonedDst = null;
		try {
			clonedDst = new NbaDst();  //NBA208-32
			clonedDst = (NbaDst) CloneObject.clone(nbaDst, clonedDst);  //NBA208-32
		} catch (Exception e) {
			throw new NbaBaseException("Invalid Dst", e);
		}
		clonedDst.getTransactions().clear(); //clean 
		for (int i = 0; i < transactions.size(); i++) {
			NbaTransaction transaction = (NbaTransaction) transactions.get(i);
			//Begin ALS5017
			NbaLob transLob = transaction.getNbaLob(); 
			if (NbaOliConstants.OLI_REQCODE_MIBCHECK == transLob.getReqType() && NbaConstants.A_WT_REQUIREMENT.equals(transLob.getWorkType())) { //SPR3310
				String partyId = nbaTxLife.getPartyId(transLob.getReqPersonCode(), String.valueOf(transLob.getReqPersonSeq())); //APSL1013
				RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(transaction.getNbaLob().getReqUniqueID());
				if (reqInfo != null && reqInfo.getAppliesToPartyID().equalsIgnoreCase(partyId)) {
					processMIBReportsForAMIBTransaction(clonedDst, transaction, processZeroMIBReport); //porcess with clone case
				}
				//End ALS5017
			}
		}
		if (isAddedMIBReport()) {
			//NBA213 deleted code
			getNbaTxLife().setAccessIntent(nbaDst.isLocked(getUser().getUserID()) ? NbaConstants.UPDATE : NbaConstants.READ);//APSL1013
			nbaTxLife = NbaContractAccess.doContractUpdate(getNbaTxLife(), nbaDst, getUser());  //NBA213
			commitTransactions(getUser(), clonedDst); //NBA208-36
			//NBA213 deleted code
		}
		return nbaDst;
	}

	/**
	 * This method performs:
	 * 	- Retreive list of un-transmitted reports
	 * 	- if there is no un-transmitted report and processZeroMIBReport is false throws NbaBaseException
	 * 	- if there is no un-transmitted report and processZeroMIBReport is true and contract have un-transmitted impairment 
	 * 		MIB codes, create a new MIB report
	 * 	- Iterate throgh all un-transmitted report list.
	 * 		- Add un-transmitted impairment MIB codes to the first un-transmitted report
	 * 		- create a temporary requirement for each report and send it to the communication queue. 
	 * @param nbaDst the case workitem
	 * @param mibReq the MIB requirement NbaTransaction object
	 * @param processZeroMIBReport if true it will process impairment MIB codes even if there is no un-transmitted report available on the MIB requirement
	 * @return the updated case workitem
	 * @throws NbaBaseException
	 */
	public NbaDst processMIBReportsForAMIBTransaction(NbaDst nbaDst, NbaTransaction mibReq, boolean processZeroMIBReport) throws NbaBaseException {
		ArrayList unTransmirredReports = getReportsToTransmit(mibReq);
		NbaLob lob = mibReq.getNbaLob();
		if (unTransmirredReports.size() == 0) {
			if (processZeroMIBReport) {
				String partyId = getNbaTxLife().getPartyId(lob.getReqPersonCode(), String.valueOf(lob.getReqPersonSeq()));
				Party party = getNbaTxLife().getParty(partyId).getParty();
				if (isPartyHasUnTransmittedImpairmentMIBCodes(party)) {
					Attachment attachment = createMIBReport(mibReq);
					unTransmirredReports.add(attachment); //new un-transmitted report for impairment MIB codes
				}
			} else {
				throw new NbaBaseException("MIB report not available for transmission");  //NBA122
			}
		}
		//process all un-transmitted reports
		for (int r = 0; r < unTransmirredReports.size(); r++) {
			Attachment report = (Attachment) unTransmirredReports.get(r);
			AttachmentData data = report.getAttachmentData();
			if (data != null && data.hasPCDATA()) {
				NbaTXLife xml402;
				try {
					xml402 = new NbaTXLife(data.getPCDATA());
				} catch (Exception e) {
					NbaBaseException be = new NbaBaseException("Invalid MIB report data", e);
					NbaLogFactory.getLogger(this.getClass()).logException(e);
					throw be;
				}
				if (r == 0) { // add impairment MIB codes to first un-transmitted report
					processMIBReportForImpairmentMIBCodes(mibReq, xml402);
					data.setPCDATA(xml402.toXmlString());
					data.setActionUpdate();
				}
				prepareReportToTransmit(nbaDst, xml402, lob.getReqUniqueID()); //ACN009 added requniqueid
				report.setDescription(NbaConstants.MIB_UPDATE_PENDING_TRANSMISSION); //ACN009
				report.setActionUpdate();
			}
		}
		return nbaDst;
	}
	/**
	 * This method check the contract for un transmitted reports. If found then
	 * return them as list else return empty list.
	 * @param transaction the MIB requirement object
	 * @return the untransmitted report list.
	 */
	protected ArrayList getReportsToTransmit(NbaTransaction mibReq) throws NbaBaseException {
		ArrayList unTransmirredReports = new ArrayList();
		NbaLob lob = mibReq.getNbaLob();
		RequirementInfo reqInfo = nbaTxLife.getRequirementInfo(mibReq.getNbaLob().getReqUniqueID()) ;//ALS5017
		if ( reqInfo != null ){//ALS5017
			java.util.List attachments = reqInfo.getAttachment();
			for (int i = 0; i < attachments.size(); i++) {
				Attachment attachment = (Attachment) attachments.get(i);
				if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_MIB402) {
					String action = attachment.getActionIndicatorCode();
					if (action == null
						|| NbaActionIndicator.ACTION_ADD_SUCCESSFUL.equals(action)
						|| NbaActionIndicator.ACTION_UPDATE_SUCCESSFUL.equals(action)) {
						if (!(attachment.hasDescription() && attachment.getDescription().trim().length() > 0)) { // not previously transmitted
							unTransmirredReports.add(attachment);
						}
					}
				}
			}
		}//ALS5017
		return unTransmirredReports;
	}
	
	
	/**
	 * This method search untrasmitted mib codes for impairments, if found than
	 * it copies them on the first untransmitted mib report to transmit. This method
	 * also set these mib codes trasmitted once copied on the untransmitted report.
	 * @param mibReq the mib requirement object.
	 * @param xml402 the untransmitted mib report.
	 * @throws NbaBaseException
	 */
	protected void processMIBReportForImpairmentMIBCodes(NbaTransaction mibReq, NbaTXLife xml402) throws NbaBaseException {
		NbaLob lob = mibReq.getNbaLob();
		String partyId = getNbaTxLife().getPartyId(lob.getReqPersonCode(), String.valueOf(lob.getReqPersonSeq()));
		// SPR3290 code deleted
		NbaParty party = getNbaTxLife().getParty(partyId);
		if (party != null) {
			if (party.isPerson()) {
				Person person = party.getParty().getPersonOrOrganization().getPerson();
				PersonExtension personExt = NbaUtils.getFirstPersonExtension(person);
				if (personExt != null) {
					java.util.List forms = getNbaTxLife().getOLifE().getFormInstance();
					OLifE xml402Olife = xml402.getOLifE();
					// SPR3290 code deleted
					FormResponse formResponse = null; //ACN009
					FormInstance xml402frmInst = null; //ACN009
					// begin NBA122
					// must have an OLifE and a FormInstance to continue
					if (xml402Olife == null || xml402Olife.getFormInstanceCount() <= 0) {
						return;
					}
					xml402frmInst = xml402Olife.getFormInstanceAt(0);
					List currentMIBCodes = getCurrentReportMIBCodes(xml402frmInst);
					// end NBA122
					for (int i = 0; i < personExt.getImpairmentInfoCount(); i++) {
						ImpairmentInfo impairmentInfo = personExt.getImpairmentInfoAt(i);
						for (int k = 0; k < forms.size(); k++) {
							FormInstance formIns = (FormInstance) forms.get(k);
							FormInstanceExtension formInsExt = NbaUtils.getFirstFormInstanceExtension(formIns);
							if (isUntransmittedImpairmentMIBCode(formInsExt, impairmentInfo)) {
								// NBA122 deleted code
								// don't duplicate codes already on the report
								if (!currentMIBCodes.contains(formInsExt.getMIBUpdateCode())) {  //NBA122
									formResponse = new FormResponse();
									formResponse.setResponseData(formInsExt.getMIBUpdateCode());
									xml402frmInst.addFormResponse(formResponse);
								}  //NBA122
								//set FormInstance in txlife transmitted
								formInsExt.setMIBTransmitInd(true);
								formInsExt.setActionUpdate();
							}
						}
					}
				}
			}
		}
	}
	/**
	 * Generates a list of MIB codes included in a FormInstance.  The list will not contain
	 * any duplicate entries.
	 * @param formInstance
	 * @return
	 */
	// NBA122 New Method
	protected List getCurrentReportMIBCodes(FormInstance formInstance) {
		List mibCodes = new ArrayList();
		FormResponse formResponse;
		int count = formInstance.getFormResponseCount();
		for (int i = 0; i < count; i++) {
			formResponse = formInstance.getFormResponseAt(i);
			if (!mibCodes.contains(formResponse.getResponseData())) {
				mibCodes.add(formResponse.getResponseData());
			}
		}
		return mibCodes;
	}
	/**
	 * Prepare MIB report (temporary requirement workitem) to transmit. 
	 * @param nbaDst com.csc.fsg.nba.vo.NbaDst
	 * @param mibReport com.csc.fsg.nba.vo.NbaTXLife
	 * @param reqUniqueId String
	 */
	protected void prepareReportToTransmit(NbaDst nbaDst, NbaTXLife mibReport, String reqUniqueId)
		throws NbaBaseException { //ACN009 removed NbaTrasaction
		// SPR3290 code deleted
		try {
			//ACN009 code deleted 
			NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(new NbaUserVO(NbaConstants.PROC_VIEW_REQUIREMENT_MIB, ""), nbaDst); //SPR2639
			NbaTransaction tempTrans = nbaDst.addTransaction(provider.getWorkType(), provider.getInitialStatus());
			tempTrans.increasePriority(provider.getWIAction(), provider.getWIPriority());
			//NBA208-32
			tempTrans.getTransaction().setRelate(null); // don't want to relate with case			
			NbaLob tempLob = tempTrans.getNbaLob();
			tempLob.setReqStatus(Long.toString(0)); //ACN009
			tempLob.setReqVendor(NbaConstants.PROVIDER_MIB);
			tempLob.setReqUniqueID(reqUniqueId); //ACN009
			//ACN009 code deleted
			setAddedMIBReport(true);
		} catch (NbaBaseException ne) {
			throw ne;
		} catch (Exception e) {
			throw new NbaBaseException("Error during MIB report transmission " + e.getMessage(), e);
		}
	}
	/**
	 * Returns true if an insured party has un-transmitted impairment MIB codes.
	 * @param party the insured party object
	 * @return true if an insured party has un-transmitted impairment MIB codes else return false.
	 */
	protected boolean isPartyHasUnTransmittedImpairmentMIBCodes(Party party) {
		if (party != null) {
			PersonOrOrganization personOrOrganization = party.getPersonOrOrganization();
			if (personOrOrganization != null && personOrOrganization.isPerson()) {
				Person person = personOrOrganization.getPerson();
				PersonExtension personExt = NbaUtils.getFirstPersonExtension(person);
				if (personExt != null) {
					java.util.List forms = getNbaTxLife().getOLifE().getFormInstance();
					for (int i = 0; i < personExt.getImpairmentInfoCount(); i++) {
						ImpairmentInfo impairmentInfo = personExt.getImpairmentInfoAt(i);
						for (int k = 0; k < forms.size(); k++) {
							FormInstance formIns = (FormInstance) forms.get(k);
							FormInstanceExtension formInsExt = NbaUtils.getFirstFormInstanceExtension(formIns);
							if (isUntransmittedImpairmentMIBCode(formInsExt, impairmentInfo)) {
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	/**
	 * This method creates a new MIB report to transmit. Throws NbaBaseException id could not create 
	 * MIB report to transmit.
	 * @param mibReq the mib requirement object.
	 * @return the new MIB report attachment object.
	 */
	protected Attachment createMIBReport(NbaTransaction mibReq) throws NbaBaseException {
		NbaLob lob = mibReq.getNbaLob();
		String reqUniqueId = lob.getReqUniqueID();
		Policy policy = getNbaTxLife().getPolicy();
		for (int i = 0; i < policy.getRequirementInfoCount(); i++) {
			RequirementInfo reqInfo = policy.getRequirementInfoAt(i);
			if (reqInfo.getRequirementInfoUniqueID() != null && reqInfo.getRequirementInfoUniqueID().equalsIgnoreCase(reqUniqueId)) {
				Party mibParty = null;
				for (int a = 0; a < reqInfo.getAttachmentCount(); a++) {
					Attachment attachment = reqInfo.getAttachmentAt(a);
					if (attachment.getAttachmentType() == NbaOliConstants.OLI_ATTACH_MIB401) {
						NbaTXLife life;
						try {
							life = new NbaTXLife(attachment.getAttachmentData().getPCDATA());
						} catch (Exception e) {
							throw new NbaBaseException("Original MIB Request is missing or invalid.");
						}
						mibParty = life.getParty(life.getPartyId(lob.getReqPersonCode())).getParty();
						break;
					}
				}
				if (mibParty == null) {
					throw new NbaBaseException("Original MIB Request is missing or invalid.");
				}
				NbaMIBCodedReport mibReport = new NbaMIBCodedReport(mibParty, lob, getNbaTxLife());
				mibReport.setUnderwriter(getUser().getUserID());
				Attachment attachment = new Attachment();
				attachment.setActionAdd();
				getOlifeId().setId(attachment);
				attachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
				attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_MIB402);
				AttachmentData data = new AttachmentData();
				attachment.setAttachmentData(data);
				data.setActionAdd();
				data.setPCDATA(mibReport.toNbaTXLife().toXmlString());
				reqInfo.addAttachment(attachment);
				return attachment;
			}
		}
		throw new NbaBaseException("Could not create MIB report to transmit");
	}
	/**
	 * Retruns true if it is a un transmitted MIB code.
	 * @param formInsExt the FormInstanceExtension object
	 * @param impairmentInfo the ImpairmentInfo object
	 * @return true if FormInstanceExtension has un transmitted impairment MIB code
	 */
	protected boolean isUntransmittedImpairmentMIBCode(FormInstanceExtension formInsExt, ImpairmentInfo impairmentInfo) {
		if (formInsExt != null && formInsExt.getMIBTransmitInd() == false) {
			if (!(formInsExt.getActionIndicatorCode() != null
				&& (formInsExt.isAction(NbaActionIndicator.ACTION_DELETE)
					|| formInsExt.isAction(NbaActionIndicator.ACTION_DELETE_SUCCESSFUL)))) { //if not mark deleted
				if (formInsExt.hasAppliesToImpairmentID()
					&& formInsExt.hasMIBUpdateCode()
					&& formInsExt.getAppliesToImpairmentID().equals(impairmentInfo.getId())) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Return nbaTxLife
	 * @return the nbaTxLife
	 */
	public NbaTXLife getNbaTxLife() {
		return nbaTxLife;
	}
	/**
	 * Sets nbaTxLife
	 * @param life the NbaTXLife object
	 */
	public void setNbaTxLife(NbaTXLife life) {
		nbaTxLife = life;
	}
	/**
	 * Returns user
	 * @return the user
	 */
	public NbaUserVO getUser() {
		return user;
	}
	/**
	 * Sets the user 
	 * @param userVO the NbaUserVO object
	 */
	public void setUser(NbaUserVO userVO) {
		user = userVO;
	}
	/**
	 * Returns the instance of NbaOLifEId. 
	 * @return the instance of NbaOLifEId. 
	 */
	public NbaOLifEId getOlifeId() throws NbaBaseException {
		if (olifeId == null) {
			olifeId = new NbaOLifEId(nbaTxLife.getOLifE());
		}
		return olifeId;
	}
	/**
	 * Returns true if added MIB report workitem
	 * @return true if added MIB report workitem
	 */
	public boolean isAddedMIBReport() {
		return addedMIBReport;
	}
	/**
	 * Sets true if added MIB report workitem
	 * @param b the boolean value
	 */
	public void setAddedMIBReport(boolean b) {
		addedMIBReport = b;
	}
    /**
     * Commit the new Transactions to the workflow system
     * @param userVO the user VO
     * @param clonedDst the DST containing the original Case and new Transactions
     * @throws NbaBaseException
     */
    //NBA208-36 New Method
    protected void commitTransactions(NbaUserVO userVO, NbaDst clonedDst) throws NbaBaseException {
        Iterator it = clonedDst.getNbaTransactions().iterator();
        NbaTransaction nbaTransaction;
        NbaLob nbaLob;
        NbaDst newWorkNbaDst;
        while (it.hasNext()){
            nbaTransaction = (NbaTransaction) it.next();
            nbaLob = nbaTransaction.getNbaLob();
            nbaLob.setTypeCase(false);
            newWorkNbaDst = WorkflowServiceHelper.createCase(userVO, nbaTransaction.getBusinessArea(), nbaTransaction.getWorkType(), nbaTransaction.getStatus(), nbaLob);
            WorkflowServiceHelper.unlockWork(userVO, newWorkNbaDst);
        }
    }
    /**
	 * @return Returns the nbaDstWithAllTransactions.
	 */
	//ALS5017 New Method
	public NbaDst getNbaDstWithAllTransactions() {
		return nbaDstWithAllTransactions;
	}
	/**
	 * @param nbaDstWithAllTransactions The nbaDstWithAllTransactions to set.
	 */
	//ALS5017 New Method
	public void setNbaDstWithAllTransactions(NbaDst nbaDstWithAllTransactions) {
		this.nbaDstWithAllTransactions = nbaDstWithAllTransactions;
	}
}
