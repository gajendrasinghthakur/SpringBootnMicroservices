package com.csc.fsg.nba.business.transaction;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

import java.util.Date;
import java.util.List;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaConfigurationException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaWebClientFaultException;
import com.csc.fsg.nba.exception.NbaWebServerFaultException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.configuration.MessageCode;
import com.csc.fsg.nba.vo.configuration.MessageSeverityCode;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vo.txlife.AttachmentExtension;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.PolicyExtension;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapter;
import com.csc.fsg.nba.webservice.client.NbaWebServiceAdapterFactory;


/**
 * This class does the agent advance processing for Agent Advances as well as Agent Chargebacks.
 * It is an interface for Agent Advance Processing for nbA. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA137</td><td>Version 6</td><td>nbA Agent Advances</td></tr>
 * <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 6
 */

public class NbaAgentAdvancesRequestor {

	private NbaLob nbaLob;
	private NbaTXLife nbaTXLife;
	private String user;
	private String errorMessage;
	private static NbaLogger nbaLogger = null;
	
	//constants for Attachment object
	public final static String USER_CODE_AGTAGVREQNEEDED = "1"; 
	public final static String USER_CODE_AGTADVREQSENT = "2";
	public final static String USER_CODE_AGTAGVCHGBKREQNEEDED = "3";
	public final static String USER_CODE_AGTAGVCHGBKREQSENT = "4";
	public final static String USER_CODE_NOAGTADVTXNSENT = "5";

	private static final String AGT_ADV_ATTACHMENT_FILENAME = "TXLife 103, subtype=1000500022";
	private static final String AGT_ADV_CHGBK_ATTACHMENT_FILENAME = "TXLife 505, no subtype";	
	private static final String AGT_ADV_ATTACHMENT_ID = "Initial_Agent_Advance_Status";
	private static final String AGT_ADV_CHGBK_ATTACHMENT_ID = "Agent_Advance_Chargeback_Status";	
	private static final String INITIAL_AGT_ADV_REQUEST_NEEDED = "Initial Agent Advance Request Needed";
	private static final String INITIAL_AGT_ADV_REQUEST_SENT = "Initial Agent Advance Request Done";
	private static final String AGT_ADV_CHGBK_REQUEST_NEEDED = "Final Disposition Agent Advance Request Needed";
	private static final String AGT_ADV_CHGBK_REQUEST_SENT = "Final Disposition Agent Advance Request Done";
	private static final String NO_AGT_ADV_TXNS_SENT = "No Agent Advance Transactions Sent";
	
	/**
	 * Constructor for NbaAgentAdvancesRequestor
	 */
	public NbaAgentAdvancesRequestor() {
		super();
	}	

    /**
     * Returns the NbaLogger object
     * @return NbaLogger
     */
    protected static NbaLogger getLogger() { // NBA208-26
		if (nbaLogger == null) {
			try {
			    nbaLogger = NbaLogFactory.getLogger(NbaAgentAdvancesRequestor.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaAgentAdvanceRequestor could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return nbaLogger;
    }

    /**
     * Returns NbaTXLife
     * @return nbaTXLife.
     */
    public NbaTXLife getNbaTXLife() {
        return nbaTXLife;
    }
    /**
     * Sets the nbaTXLife
     * @param nbaTXLife to set.
     */
    public void setNbaTXLife(NbaTXLife nbaTXLife) {
        this.nbaTXLife = nbaTXLife;
    }
    	
    /**
     * Processes the Agent Advances. It first analyses if the Advance or Chargeback transaction is required or
     * if there are any agent advance prohibitors available. Then depending upon the request type, it processes
     * the agent Advance transaction or agent advances chargeback transaction.
     * @param nbaTXLife nbaTXLife object to be updated with the attachment objects created/updated 
     * @param nbaLob	nbaLob
     * @param requestType	Agent Advances or chargeback request
     * @throws NbaBaseException
     */
    public void processAgentAdvances(NbaTXLife nbaTXLife, NbaLob nbaLob, String requestType) throws NbaBaseException {  
        setNbaTXLife(nbaTXLife);
        setNbaLob(nbaLob);
        String businessProcess = nbaTXLife.getBusinessProcess();
        setUser(businessProcess);
        
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Starting nbA Agent Advances " + (NbaConstants.PROC_FINAL_DISPOSITION.equals(businessProcess) ? "Chargeback" :"")
                    + "processing through " + businessProcess + " business process for contract " + nbaLob.getPolicyNumber());
        }        
        
		boolean agentAdvanceOrChargebackRequestNeeded = isAgentAdvanceOrChargebackRequestNeeded();
        if (agentAdvanceOrChargebackRequestNeeded) {
            if (NbaConfigurationConstants.WEBSERVICE_FUNCTION_AGT_ADV.equals(requestType)) {
                boolean hasAgentAdvanceProhibitors = getAgentAdvanceProhibitors();
                if (getLogger().isDebugEnabled()) {
                    getLogger().logDebug("Processing Agent Advances");
                }               
                if (!hasAgentAdvanceProhibitors) {
                    processAgentAdvanceRequest(requestType);
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().logDebug("bypassing Agent Advance processing as Agent Advance Prohibitors available on the case");
                    }                
                    
                    //There are agent advance prohibitors on the contract, so not to send the request at this time and create/update
                    //the attachment with the user code of request needed.
                    Attachment attachment = null;
                    attachment = getAgentAdvanceAttachment(USER_CODE_AGTAGVREQNEEDED); //check if any such attachment already exists on the contract.
                    if (attachment == null) {
                        createAgentAdvanceAttachment(USER_CODE_AGTAGVREQNEEDED, AGT_ADV_ATTACHMENT_ID, AGT_ADV_ATTACHMENT_FILENAME,
                                INITIAL_AGT_ADV_REQUEST_NEEDED);
                    }
                }
            } else if (NbaConfigurationConstants.WEBSERVICE_FUNCTION_AGT_ADV_CHGBK.equals(requestType)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().logDebug("Processing Agent Advances Chargeback");
                }                
                processAgentAdvanceChargebackRequest(requestType);
            }
        } else if (NbaConfigurationConstants.WEBSERVICE_FUNCTION_AGT_ADV_CHGBK.equals(requestType) && !agentAdvanceOrChargebackRequestNeeded) {
            //either the chargeback request has happened successfully or there has been no initial advance request sent
            Attachment attachment = null;
            attachment = getAgentAdvanceAttachment(USER_CODE_AGTAGVCHGBKREQSENT);
            if (attachment == null) { //no successful chargeback request has been made so far
                //check if any attachment exists with usercode = 1, if found then update the usercode to 5 to indicate that no 
                //agent advance or chargeback request was ever sent.
                attachment = getAgentAdvanceAttachment(USER_CODE_AGTAGVREQNEEDED);
                if (attachment != null) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().logDebug("bypassing agent advance chargeback processing as no agent advance request was ever sent");
                    }                
                    
                    updateAgentAdvanceAttachment(attachment, USER_CODE_NOAGTADVTXNSENT, NO_AGT_ADV_TXNS_SENT);
                }
            }
        }
        
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Finishing nbA Agent Advances " + (NbaConstants.PROC_FINAL_DISPOSITION.equals(businessProcess) ? "Chargeback" : "")
                    + "processing through " + businessProcess + " business process for contract " + nbaLob.getPolicyNumber());
        }       
    }

    /**
     * Process the agent chargeback requests. Creates the 505 webservice request and invokes the webservice.
     * Response is parsed and the attachment objects are created/updated in the nbaTxlife object
     * @param requestType whether an agentAdvance or chargeback request
     * @throws NbaBaseException
     */
    protected void processAgentAdvanceChargebackRequest(String requestType) throws NbaBaseException {
        NbaTXLife txLifeResponse = null;
        NbaTXLife txLifeRequest = NbaAgentAdvanceTransaction.createAgentAdvanceChargebackRequest(getNbaTXLife(), getNbaLob());

        NbaWebServiceAdapter service = null;
        try {
            service = NbaWebServiceAdapterFactory.createWebServiceAdapter(nbaTXLife.getBackendSystem(), NbaConfigurationConstants.AGENT, requestType);
        } catch (NbaConfigurationException e) {
            e.forceFatalExceptionType();
            throw e;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Agent Advance Chargeback request: " + txLifeRequest.toXmlString());
        }
        txLifeResponse = service.invokeWebService(txLifeRequest);
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Agent Advance Chargeback response: " + txLifeResponse.toXmlString());
        }

        //Parse the result and add/update the Attachment object
        if (txLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseCount() > 0) {
            TransResult transResult = txLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0)
                    .getTransResult();
            if (transResult != null) {
                long resultCode = transResult.getResultCode();
                if (NbaOliConstants.TC_RESCODE_SUCCESS == resultCode || NbaOliConstants.TC_RESCODE_SUCCESSINFO == resultCode) {
                    // Add/Update Chargeback attachment with success response
                    createOrUpdateAgentAdvanceAttachment(requestType, resultCode);
                    
                    if (transResult.getResultInfoCount() > 0 && transResult.getResultInfoAt(0).hasResultInfoDesc()) {
                        getLogger().logDebug(transResult.getResultInfoAt(0).getResultInfoDesc());
                    }
                } else if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
                    getLogger().logDebug("Agent Advance Processing failed: Error returned from WebService ");

                    if (transResult.getResultInfoCount() > 0 && transResult.getResultInfoAt(0).hasResultInfoCode()) {
                        if (transResult.getResultInfoAt(0).getResultInfoCode() == NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL) {
                            if (getLogger().isFatalEnabled()) {
                                getLogger().logFatal("System not available message returned from WebService");
                            }

                            throw new NbaBaseException(NbaWebServerFaultException.SYSTEM_NOT_AVAILABLE_ERROR, NbaExceptionType.FATAL);
                        } else {
                            if (getLogger().isFatalEnabled()) {
                                getLogger().logFatal("Webservice failure occured");
                            }
                            if (transResult.getResultInfoAt(0).hasResultInfoDesc()) {
                                this.setErrorMessage(transResult.getResultInfoAt(0).getResultInfoDesc());
                            }
                        }
                        //Add/Update Chargeback attachment with failure response
                        createOrUpdateAgentAdvanceAttachment(requestType, transResult.getResultCode());
                    } else { //If the result is a failure but the ResultInfoCode is missing, an error should still be thrown
                        if (getLogger().isFatalEnabled()) {
                            getLogger().logFatal("No ResultInfoCode received in webservice response");
                        }
                        throw new NbaWebClientFaultException("No ResultInfoCode received in webservice response");
                    }
                } else {
                    if (getLogger().isFatalEnabled()) {
                        getLogger().logFatal("Unexpected Response received from Webservice");
                    }

                    throw new NbaWebClientFaultException("Unexpected Response received from Webservice");                  
                }                
            }
        } else {
            if (getLogger().isFatalEnabled()) {
                getLogger().logFatal("No TXLilfeResponse received from webservice");
            }
            throw new NbaWebClientFaultException("No TXLilfeResponse received from webservice");           
        }
    }

    /**
     * Process the agent advance requests. Creates the 103 webservice request and invokes the webservice.
     * Response is parsed and the attachment objects are created/updated in the nbaTxlife object
     * @param requestType whether an agentAdvance or chargeback request
     * @throws NbaBaseException
     */
    protected void processAgentAdvanceRequest(String requestType) throws NbaBaseException {
        NbaTXLife txLifeResponse = null;
        //NbaTXLife txLifeRequest = NbaAgentAdvanceTransaction.createAgentAdvanceRequest(getNbaTXLife(), getNbaLob());
        NbaTXLife txLifeRequest = NbaAgentAdvanceTransaction.createAgentAdvanceRequestTransaction(getNbaTXLife(), getNbaLob());

        NbaWebServiceAdapter service;
        try {
            service = NbaWebServiceAdapterFactory.createWebServiceAdapter(nbaTXLife.getBackendSystem(), NbaConfigurationConstants.AGENT, requestType);
        } catch (NbaConfigurationException e) {
            e.forceFatalExceptionType();
            throw e;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Agent Advance request: " + txLifeRequest.toXmlString());
        }
        txLifeResponse = service.invokeWebService(txLifeRequest);
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Agent Advance response: " + txLifeResponse.toXmlString());
        }
        //Parse the result and add/update the Attachment object
        if (txLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseCount() > 0) {
            TransResult transResult = txLifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0)
                    .getTransResult();
            if (transResult != null) {
                long resultCode = transResult.getResultCode();
                if (NbaOliConstants.TC_RESCODE_SUCCESS == resultCode || NbaOliConstants.TC_RESCODE_SUCCESSINFO == resultCode) {
                    // Add/Update attachment with success response i.e. user code status of 2
                    createOrUpdateAgentAdvanceAttachment(requestType, resultCode);
                    
                    if (transResult.getResultInfoCount() > 0 && transResult.getResultInfoAt(0).hasResultInfoDesc()) {
                        getLogger().logDebug(transResult.getResultInfoAt(0).getResultInfoDesc());
                    }                    
                } else if (NbaOliConstants.TC_RESCODE_FAILURE == resultCode) {
                    getLogger().logDebug("Agent Advance Processing failed: Error returned from WebService ");

                    if (transResult.getResultInfoCount() > 0 && transResult.getResultInfoAt(0).hasResultInfoCode()) {
                        if (getLogger().isFatalEnabled()) {
                            getLogger().logFatal("Webservice failure occured");
                        }
                        //add/update attachment with a usercode status of 1
                        createOrUpdateAgentAdvanceAttachment(requestType, resultCode);                        
                    } else {//If the result is a failure but the ResultInfoCode is missing, an error should still be thrown
                        if (getLogger().isFatalEnabled()) {
                            getLogger().logError("No ResultInfoCode received in webservice response");
                        }
                        throw new NbaWebClientFaultException("No ResultInfoCode received in webservice response");
                    }
                } else {
                    if (getLogger().isFatalEnabled()) {
                        getLogger().logFatal("Unexpected Response received from Webservice");
                    }

                    throw new NbaWebClientFaultException("Unexpected Response received from Webservice");
                }
            }
        } else {
            if (getLogger().isFatalEnabled()) {
                getLogger().logError("No TXLilfeResponse received from webservice");
            }
            throw new NbaWebClientFaultException("No TXLilfeResponse received from webservice");           
        }
    }

	
	/**
	 * Creates or updates the agent advance attachment
	 * @param requestType whether an Agent Advance or Chargeback request
     * @param resultCode	Result code of the response received from webservice
     */
    protected void createOrUpdateAgentAdvanceAttachment(String requestType, long resultCode) {
        Attachment attachment = null;
        if (NbaConfigurationConstants.WEBSERVICE_FUNCTION_AGT_ADV_CHGBK.equals(requestType)) {
            //check if result code is success and an attachment with usercode 3 exisits if (yes) update usercode to 4, 
            // else create a new attachment with a usercode 4. However if result code is fail, check if there any attachment with usercode 3
            // if none available, create an attachment with userCode of 3
            attachment = getAgentAdvanceAttachment(USER_CODE_AGTAGVCHGBKREQNEEDED);
            if (NbaOliConstants.TC_RESCODE_SUCCESS == resultCode) {
                if (attachment != null) {
                    updateAgentAdvanceAttachment(attachment, USER_CODE_AGTAGVCHGBKREQSENT, AGT_ADV_CHGBK_REQUEST_SENT);
                } else {
                    createAgentAdvanceAttachment(USER_CODE_AGTAGVCHGBKREQSENT, AGT_ADV_CHGBK_ATTACHMENT_ID, AGT_ADV_CHGBK_ATTACHMENT_FILENAME,
                            AGT_ADV_CHGBK_REQUEST_NEEDED);
                }
            } else {
                if (attachment == null) {//for failure response, create attachment object if it is null. If already exisiting, no need to update
                    createAgentAdvanceAttachment(USER_CODE_AGTAGVCHGBKREQNEEDED, AGT_ADV_CHGBK_ATTACHMENT_ID, AGT_ADV_CHGBK_ATTACHMENT_FILENAME,
                            AGT_ADV_CHGBK_REQUEST_NEEDED);
                }
            }
        } else { //attachment object for Initial agent Advance
            //check if result code is success and an attachment with usercode 1 exisits if (yes) update usercode to 2, 
            // else create a new attachment with a usercode 2. However if result code is fail, check if there any attachment with usercode 1
            // if none available, create an attachment with userCode of 1            
            attachment = getAgentAdvanceAttachment(USER_CODE_AGTAGVREQNEEDED);
            if (NbaOliConstants.TC_RESCODE_SUCCESS == resultCode) {
                if (attachment != null) {
                    updateAgentAdvanceAttachment(attachment, USER_CODE_AGTADVREQSENT, INITIAL_AGT_ADV_REQUEST_SENT);
                } else {
                    createAgentAdvanceAttachment(USER_CODE_AGTADVREQSENT, AGT_ADV_ATTACHMENT_ID, AGT_ADV_ATTACHMENT_FILENAME,
                            INITIAL_AGT_ADV_REQUEST_NEEDED);
                }
            } else {
                if (attachment == null) { // for failure response, create attachment object if it is null. If already exisiting, no need to update
                    createAgentAdvanceAttachment(USER_CODE_AGTAGVREQNEEDED, AGT_ADV_ATTACHMENT_ID, AGT_ADV_ATTACHMENT_FILENAME,
                            INITIAL_AGT_ADV_REQUEST_NEEDED);
                }
            }
        }
    }
    
    
    
    /**
     * Creates the attachment object and updates the nbaTxlife object
     * @param userCode		for the attachment object
     * @param attachmentId	for the attachment object
     * @param fileName		for the attachment object
     * @param description	for the attachment object
     */
    protected void createAgentAdvanceAttachment(String userCode, String attachmentId, String fileName, String description) {
        
		Attachment attachment = new Attachment();
		attachment.setId(attachmentId);
		attachment.setDateCreated(new Date());
		attachment.setUserCode(userCode);
		attachment.setAttachmentBasicType(NbaOliConstants.OLI_LU_BASICATTMNTTY_TEXT);
		attachment.setDescription(description);
		attachment.setAttachmentType(NbaOliConstants.OLI_ATTACH_AGTADVSTATUS);
		OLifEExtension olifeExt = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_ATTACHMENT);
		attachment.addOLifEExtension(olifeExt);
		
		AttachmentExtension attachmentExtension = olifeExt.getAttachmentExtension();
		attachmentExtension.setFileName(fileName);
		
		attachmentExtension.setActionAdd();
		attachment.setActionAdd();
		
		getNbaTXLife().getPrimaryHolding().addAttachment(attachment); //Add the attachment to holding.		
    }

    /**
	 * Add an Attachement which identifies the result from Agent Advance Webservice
	 * @param attachment	Attachment object to be updated
	 * @param userCode		to be updated in the attachment object
	 * @param description 	to be updated in the attachment object  
	 */
	protected void updateAgentAdvanceAttachment(Attachment attachment, String userCode, String description) {
		attachment.setUserCode(userCode);
		attachment.setDescription(description);
		attachment.setDateCreated(new Date());
		attachment.setActionUpdate();
	}	
	
    /**
     * Parses the nbaTxlife object and returns the attachment object for the given userCode.
     * Returns null if no such object is found
     * @param userCode	for which Attachment object is required
     * @return Attachment	attachment object for the given userCode
     */
	protected Attachment getAgentAdvanceAttachment(String userCode) {
        Attachment attachment = null;
        Holding primaryHolding = getNbaTXLife().getPrimaryHolding();
        int attachmentCount = primaryHolding.getAttachmentCount();
		for (int i = 0; i < attachmentCount; i++) {
			attachment = primaryHolding.getAttachmentAt(i);
			String attachmentUserCode = attachment.getUserCode();
			if (NbaOliConstants.OLI_ATTACH_AGTADVSTATUS == attachment.getAttachmentType() && attachmentUserCode.equals(userCode)) {
			    return attachment;
			}
		}		

		return null;
    }     
    

    /**
	 * Verifies the attachment object in the txlife for any attachment type 1000500004 and 
	 * establishes if the agentAdvance or chargeback request is needed. 
	 * @return boolean
	 */
	protected boolean isAgentAdvanceOrChargebackRequestNeeded() {
		boolean requestNeeded = true;
		Holding primaryHolding = getNbaTXLife().getPrimaryHolding();
		int attachmentCount = primaryHolding.getAttachmentCount();
		
	    if (NbaConstants.PROC_FINAL_DISPOSITION.equalsIgnoreCase(nbaTXLife.getBusinessProcess())) {
	        requestNeeded = false; //initial resetting for Final Disp process
		}
		
		Attachment attachment = null;
		String userCode = null;
		for (int i = 0; i < attachmentCount; i++) {
			attachment = primaryHolding.getAttachmentAt(i);
			userCode = attachment.getUserCode();
			if (NbaOliConstants.OLI_ATTACH_AGTADVSTATUS == attachment.getAttachmentType()) {
			    //to check that User should not be final disposition auto process as there is separate processing for final disposition
			    if (! NbaConstants.PROC_FINAL_DISPOSITION.equalsIgnoreCase(nbaTXLife.getBusinessProcess())) {
					if (! USER_CODE_AGTAGVREQNEEDED.equals(userCode)) {
						requestNeeded = false;
						break;
					}
			    } else {
			        if (USER_CODE_AGTAGVREQNEEDED.equals(userCode) || USER_CODE_AGTAGVCHGBKREQSENT.equals(userCode)
                            || USER_CODE_NOAGTADVTXNSENT.equals(userCode)) {
			            requestNeeded = false;
			            break;
			        } else if (USER_CODE_AGTADVREQSENT.equals(userCode)) {
			            requestNeeded = true; //not breaking because for FinalDisp, iterate further for another same attachment type attachment
			        } else if (USER_CODE_AGTAGVCHGBKREQNEEDED.equals(userCode)) {
			            requestNeeded = true;
			            break;
			        }
			    }
			}
		}
		
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Agent Advance or Chargeback request needed: " + requestNeeded);
        }		
		return requestNeeded;
	}	

	/**
	 * Checks all the system messages for any agent advance prohibitors available on the contract
     * @return whether or not any agentAdvanceProhibitors exist that prohibit webservice call
     */
	protected boolean getAgentAdvanceProhibitors() throws NbaBaseException {
        boolean agentAdvanceProhibitorsAvailable = false;
        
        if (getAgentErrorsInd()) {
            agentAdvanceProhibitorsAvailable = true;
        } else {
            if (getNbaLob().getPortalCreated() && getNbaLob().getAgentID() == null) {
                agentAdvanceProhibitorsAvailable = true;
            } else {
                Holding primaryHolding = getNbaTXLife().getPrimaryHolding();
                int systemMessageCount = primaryHolding.getSystemMessageCount();

                SystemMessage systemMessage = null;
                for (int i = 0; i < systemMessageCount; i++) {
                    systemMessage = primaryHolding.getSystemMessageAt(i);
                    if (isMessageProhibitor(systemMessage)) {
                        agentAdvanceProhibitorsAvailable = true;
                        break;
                    }
                }
            }
        }
        
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Agent Advance prohibitors available: " + agentAdvanceProhibitorsAvailable);
        }        
        
        return agentAdvanceProhibitorsAvailable;
    }
	
    /**
	 * Returns the agentErrorInd that is set by the contract validation (agent validations -subset=3)
     * @return agentErrorInd value. false if policyextension is null
     */
    protected boolean getAgentErrorsInd() {
        PolicyExtension policyExtension = NbaUtils.getFirstPolicyExtension(getNbaTXLife().getPrimaryHolding().getPolicy());
        
        if (policyExtension != null) {
            return policyExtension.getAgentErrorsInd();
        }
        
        return false;
    }

    /**
	 * Checks if the given systemMessage is a prohibitor for Agent Advances
     * @param systemMessage
     * @return boolean if the messagecode or the messageseveritycode for give system message turns out to be a prohibitor
     * @throws NbaBaseException
     */	
	protected boolean isMessageProhibitor(SystemMessage systemMessage) throws NbaBaseException {
	    return isMessageCodeProhibitor(systemMessage) || isMessageSeverityCodeProhibitor(systemMessage);
	}

	/**
	 * Checks if the given systemMessage has a messageSeverityCode that is configured to be an Agent Advances prohibitor
     * @param systemMessage
     * @return boolean 
     * @throws NbaBaseException
     */
	protected boolean isMessageSeverityCodeProhibitor(SystemMessage systemMessage) throws NbaBaseException {
	    boolean messageIsProhibitor = false;
	    long messageSeverityCode = systemMessage.getMessageSeverityCode();
	    
        List msgSeverityCodes = NbaConfiguration.getInstance().getAgentAdvanceRequestRules().getAgentAdvanceProhibitors().getMessageSeverityCode();
        int size = msgSeverityCodes.size();
        for (int i = 0; i < size; i++) {
            if (messageSeverityCode == ((MessageSeverityCode) msgSeverityCodes.get(i)).getTc()) {
                messageIsProhibitor = true;
                break;
            }
        }            

        return messageIsProhibitor;
    }

	
    /**
	 * Checks if the given systemMessage identified by the messagecode is configured to be an Agent Advances Prohibitor
     * @param systemMessage
     * @return boolean 
     * @throws NbaBaseException
     */
    protected boolean isMessageCodeProhibitor(SystemMessage systemMessage) throws NbaBaseException {
	    boolean messageIsProhibitor = false;

	    List msgCodes = NbaConfiguration.getInstance().getAgentAdvanceRequestRules().getAgentAdvanceProhibitors().getMessageCode();
        int size = msgCodes.size();
        for (int i = 0; i < size; i++) {
            if (systemMessage.getMessageCode() == ((MessageCode) msgCodes.get(i)).getTc()) {
                messageIsProhibitor = true;
                break;
            }
        }            
	    
        return messageIsProhibitor;
    }

    /**
     * Returns the business process
     * @return String user
     */
    public String getUser() {
        return user;
    }
    /**
     * Sets the business process as the user
     * @param user The user to set.
     */
    public void setUser(String user) {
        this.user = user;
    }
    
    /**
     * Returns NbaLob object
     * @return NbaLob
     */
    public NbaLob getNbaLob() {
        return nbaLob;
    }
    /**
     * Sets the NbaLob object
     * @param nbaLob The nbaLob to set.
     */
    public void setNbaLob(NbaLob nbaLob) {
        this.nbaLob = nbaLob;
    }
    
    
    /**
     * Returns the error message from the webservice
     * @return String
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    /**
     * Sets the webservice failure message as error message
     * @param errorMessage
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
