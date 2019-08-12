package com.csc.fsg.nba.correspondence;

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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.nbaschema.Correspondence;
import com.csc.fsg.nba.vo.nbaschema.RequestXML;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/** 
 * This class validates for Correspondence events by invoking a VP/MS model. If an event is generated,
 * a Correspondence Work Item is created. This Work Item is then routed to a Correspondence queue, which is polled for
 * work by a Correspondence Automated Process. This Automated Process is responsible for generating a letter for
 * each Work Item. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System</td></tr>   
 * <tr><td>SPR1234</td><td>Version 3</td><td>General cleanup</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Nba Pending Database</td></tr>
 * <tr><td>NBA062</td><td>Version 3</td><td>EnCorr Web Service</td></tr> 
 * <tr><td>SPR1720</td><td>Version 4</td><td>Not able to route the status of Requirements in Case Manager Queue.</td></tr> 
 * <tr><td>SPR2061</td><td>Version 4</td><td>NbaBaseException can occur in OINK processing.</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA146</td><td>Version 6</td><td>Workflow integration</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-14</td><td>Version 7</td><td>Make automated Correspondence Processing optional based on configuration</td></tr> 
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>NBA208-34</td><td>Version 7</td><td>Re-using VP/MS adaptor for multiple calls</td></tr> 
 * <tr><td>NBA208-37</td><td>Version 7</td><td>Removing redundant calls to correspondence VPMS model</td></tr>
 * <tr><td>AXAL3.7.13I</td><td>AXA Life Phase 1</td><td>Informal correspondence</td></tr>
 * <tr><td>AXAL3.7.13</td><td>AXA Life Phase 1</td><td>Formal correspondence</td></tr>
 * <tr><td>ALPC96</td><td>AXA Life Phase 1</td><td>xPression OutBound Email</td></tr>
 * <tr><td>ALS2746</td><td>AxaLife Phase 1</td><td>Corrected behavior for Reg60 PreSale applications.</td></tr>
 * <tr><td>ALS4907</td><td>AxaLife Phase 1</td><td>QC # 4064 - Event driven correspondence "Informal_Received_BGA-Email" generated more than once on informal policy</td></tr>
 * <tr><td>PERF-APSL651</td><td>AxaLife Phase 1</td><td>PERF - Event Driven Correspondence Optimization</td></tr>
 * <tr><td>CR58636</td><td>Discretionary</td><td>ADC Retrofit</td></tr>
 * <tr><td>NBA239</td><td>Version 8</td><td>Improving Approve Transaction timing to <=3 seconds</td></tr>
 * <tr><td>ALII1334</td><td>AXA Life Phase 2</td><td>Event Driven Correspondence causing AWD Unlock issue</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaCorrespondenceEvents {
	protected String workItemId;		//NBA050
	protected NbaLogger logger;
	protected NbaTXLife nbaTXLife;		//NBA050
	protected NbaDst aNbaDst;

	protected java.util.Map lettersForEvents;
	protected java.util.Map deOinkAttributes;
	protected final static java.lang.String NOID_PREFIX = "NOID_";
	protected NbaUserVO userVO; //NBA146
	protected String automatedCorrespondenceEnabled = "Y";	//NBA208-14
	public final static int READ = 0; // AXAL3.7.13
	
	//NBA146
	public NbaCorrespondenceEvents(NbaUserVO nbAUserVO){
	    //begin NBA208-14
	    try {
            setAutomatedCorrespondenceEnabled(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
                    NbaConfigurationConstants.AUTOMATED_CORRESPONDENCE_ENABLED));
        } catch (NbaBaseException e) {
        	e.printStackTrace(System.out);
            //ignore and use default
        }
        //end NBA208-14
	    setUserVO(nbAUserVO);
	}
/**
 * This method creates Correspondence Work Items for each letter that needs to be generated.
 * @throws NbaBaseException
 * 
 */
public void createWorkItems() throws NbaBaseException {

	try {
		
		List letters = null;

		if (getLettersForEvents().containsKey(getWorkItemId())) {	//NBA050
			letters = parseVpmsResult((String) getLettersForEvents().get(getWorkItemId()));	//NBA050

			for (int i = 0; i < letters.size(); i++) {
				updateWorkItems(getCorrespondenceObject(letters.get(i).toString(), aNbaDst.isTransaction(), getWorkItemId()));	//NBA050
			}

		} else if (getLettersForEvents().size() > 0) {
			for (int i = 0; i < aNbaDst.getTransactions().size(); i++) {
				WorkItem tx = (WorkItem) aNbaDst.getTransactions().get(i);  //NBA208-32
				if (getLettersForEvents().containsKey(tx.getItemID())) {  //NBA208-32
					letters = parseVpmsResult((String) getLettersForEvents().get(tx.getItemID()));  //NBA208-32
				} else if (getLettersForEvents().containsKey(NOID_PREFIX + i)) {
					letters = parseVpmsResult((String) getLettersForEvents().get(NOID_PREFIX + i));
				}
				if ( letters != null){			//AXAL3.7.13
					for (int j = 0; j < letters.size(); j++) {
						updateWorkItems(getCorrespondenceObject(letters.get(j).toString(), true, tx.getItemID()));  //NBA208-32	
					}
					letters = null; // APSL3382 CDN letters for overridable CV's
				}
			}
		}
	} catch (Exception e) {
		getLogger().logError(NbaConstants.A_WT_CORRESPONDENCE + " work item could not be generated. Error : " + e);
		e.printStackTrace(System.out);
		NbaBaseException nbe = new NbaBaseException("Correspondence work item could not be generated " + e.getMessage()); //ALS5535
		nbe.forceFatalExceptionType(); //ALS5535
		throw nbe; //ALS5535 
	}
}
/**
 * This method returns a new instance of <code>Correspondence</code>.
 * @return com.csc.fsg.nba.vo.nbaschema.Correspondence
 * @param letterName A letter name
 * @param forAwdTransaction A boolean indicate whether the event is for an AWD transaction or not
 * @param workItemId A work Item Id
 */
protected Correspondence getCorrespondenceObject(String letterName, boolean forAwdTransaction, String workItemId) {
	boolean isHostEvent = (getNbaTXLife() != null ? true : false);	//NAB050

	Correspondence corrXML = new Correspondence();
	//Begin CR58636 ADC Retrofit
	corrXML.setLetterName(setLetterName(letterName));
	corrXML.setImage(isImageAttached(letterName));
	//End   CR58636
	corrXML.setLetterType(NbaCorrespondenceUtils.LETTER_EVENTDRIVEN);
	corrXML.setPolicyNumber(aNbaDst.getNbaLob().getPolicyNumber());
	if (isHostEvent) {
		RequestXML request = new RequestXML();
		request.setPCDATA(getNbaTXLife().toXmlString());	//NBA050
		corrXML.setRequestXML(request);
	} else if (forAwdTransaction) {
		corrXML.setObjectRef(workItemId);
	}
	return corrXML;
}
/**
 * This method returns a <code>Map</code> of attributes which would not be resolved by OINK.
 * @return java.util.Map
 */
protected java.util.Map getDeOinkAttributes() {	//NBA208-14 changed method visibility
	if (deOinkAttributes == null) {
		deOinkAttributes = new HashMap();
		deOinkAttributes.put("A_TRANSTYPE", "");
		deOinkAttributes.put("A_TRANSMODE", "");
		deOinkAttributes.put("A_DATATRANSSUBTYPE", "");
	}
	deOinkAttributes.put("A_Delimiter[2]", "%%"); // CR58636 ADC Retrofit
	return deOinkAttributes;
}
/**
 * This method returns a lazy initialized instance of letter names.
 * @return java.util.Map
 */
protected java.util.Map getLettersForEvents() {	//NBA208-14 changed method visibility
    if (lettersForEvents == null) {
        lettersForEvents = new HashMap();
    }
    return lettersForEvents;
}
/**
 * This method returns an instance of <code>NbaLogger</code>.
 * @return com.csc.fsg.nba.foundation.NbaLogger
 */
protected NbaLogger getLogger() {
    if (logger == null) {
        try {
            logger = NbaLogFactory.getLogger(this.getClass().getName());
        } catch (Exception e) {
            NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
            e.printStackTrace(System.out);
        }
    }
    return logger;
}
/**
 * This method invokes a VP/MS model to test for Correspondence events.
 * @param eventID java.lang.String
 * @param adapter  com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 */
 // NBA208-34  Method Signature Changed
protected void invokeVPMS(String eventID, NbaVpmsAdaptor adapter) { 
   // NBA208-34 code deleted
   try {
   	    // NBA208-34 code deleted
        adapter.getProduct().resetSession(); // NBA208-34
        adapter.setSkipAttributesMap(getDeOinkAttributes());
        adapter.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_LETTERS); //ACN012
        getLettersForEvents().put(eventID, adapter.getResults().getResult());
        //SPR3362 code deleted
    } catch (NbaBaseException e) {
        getLogger().logError("Error invoking VPMS Model" + NbaVpmsAdaptor.CORRESPONDENCE + ". " + e);
        e.printStackTrace(System.out);
    } catch (RemoteException e) {
        getLogger().logError("Error invoking VPMS Model" + NbaVpmsAdaptor.CORRESPONDENCE + ". " + e);
        e.printStackTrace(System.out);
        // NBA208-34 code deleted
    } 
}
 
/**
 * This method verifies if the Host response is valid.
 * @return boolean
 */
protected boolean isValidHostResponse(NbaTXLife nbaTxlifeResponse) {
    boolean returnValue = true;
    try {
        List responses = nbaTxlifeResponse.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponse();
        TXLifeResponse theResponse = (TXLifeResponse) responses.get(0);

        if (theResponse.getTransResult().getResultCode() > 1) {
            getLogger().logError("Invalid host response. Correspondence event can not be evaluated!");
            returnValue = false;
        }
    } catch (Exception e) {
        getLogger().logError(e);
        e.printStackTrace(System.out);
        returnValue = false;
    }
    return returnValue;
}
/**
 * This method identifies if the Host transaction is valid. If the Transaction is a Holding
 * Inquiry it returns false;
 * @param nbaTxlifeRequest com.csc.fsg.nba.vo.NbaTXLife
 * @param workItemId java.lang.String
 * @return boolean
 */
protected boolean isValidTransactionRequest(NbaTXLife nbaTxlifeRequest, String workItemId) {
    boolean returnValue = true;
    try {
        if (workItemId == null || workItemId.length() == 0) {
            returnValue = false;
        }
        if (nbaTxlifeRequest.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getTransType() == NbaOliConstants.TC_TYPE_HOLDINGINQ) {
            returnValue = false;
        }
    } catch (Exception e) {
        getLogger().logError("Invalid Transaction Error");
        e.printStackTrace(System.out);
        returnValue = false;
    }
    return returnValue;
}
/**
 * This method parese a VP/MS result and converts it into a <code>List</code> representation.
 * @return java.util.List
 * @param aResult A result string
 */
protected List parseVpmsResult(String aResult) {
    List letters = new ArrayList();
    NbaStringTokenizer result = new NbaStringTokenizer(aResult, NbaVpmsAdaptor.VPMS_DELIMITER[1]);  //AXAL3.7.13
    if (result.countTokens() > 0) {
        //Omit the first token
        result.nextToken();
        while (result.hasMoreTokens()) {
        	String letterName = result.nextToken();		//AXAL3.7.13
        	if ( !NbaUtils.isBlankOrNull(letterName)){  //AXAL3.7.13
        		letters.add(letterName);				//AXAL3.7.13
        	}
        }
    }
    return letters;
}
/**
 * This method sets a new instance of <code>NbaDst</code> 
 * @param newNbaDst An instance of <code>NbaDst</code> 
 */
public void refreshNbaDst(NbaDst newNbaDst) {
    aNbaDst = newNbaDst;
	setWorkItemId(newNbaDst.getID()); //will always be there NBA050
}
/**
 * This method sets a map of variables which should not be resolved by OINK 
 * @param newDeOinkAttributes A <code>Map</code> of variables
 */
protected void setDeOinkAttributes(java.util.Map newDeOinkAttributes) {	//NBA208-14 changed method visibility
    deOinkAttributes = newDeOinkAttributes;
}
/**
 * This method sets "Letters for events"
 * @param newLetters A Map containg letter names for events
 */
protected void setLettersForEvents(java.util.Map newLettersForEvents) {	//NBA208-14 changed method visibility
    lettersForEvents = newLettersForEvents;
}
/**
 * This methods updates the correspondence work items to AWD.
 * @param sourceXml com.csc.fsg.nba.vo.nbaschema.Correspondence
 * @exception com.csc.fsg.nba.exception.NbaBaseException Throw this exception when an AWD update error occurs.
 */
protected void updateWorkItems(Correspondence sourceXml) throws NbaBaseException {
	NbaCorrespondenceUtils utils = new NbaCorrespondenceUtils(getUserVO()); //NBA146
	utils.setTransactionID(getWorkItemId());	//NBA050
	utils.setSourceXML(sourceXml);
	utils.setLetterType(sourceXml.getLetterName());	//NBA062
	utils.setParentCase(aNbaDst);  //NBA239 
	utils.updateWorkItem(false); //ALII1334
	
}
/**
 * This method validates if a Correspondence event has occured for a AWD
 * transaction. If an event has occured, a Correspondence Work Item is created.
 * @param nbaDst An <code>NbaDst</code> instance.
 */
public void validateAwdEvent(NbaDst nbaDst) {
    if (!isAutomatedCorrespondenceEnabled()){	//NBA208-14
        return; //NBA208-14
    } //NBA208-14
    try {
        boolean continueValidation = true;
        //If a Correspondence Work Item is being updated do not test for a correspondence event
        if (nbaDst.isCase()) {
            for (int i = 0; i < nbaDst.getTransactions().size(); i++) {
            	WorkItem tx = (WorkItem) nbaDst.getTransactions().get(i);  //NBA208-32
                if (tx != null && tx.getWorkType() != null && tx.getWorkType().equals(NbaConstants.A_WT_CORRESPONDENCE) && tx.getItemID() == null) {  //NBA208-32, A2AGGCN4 error stop, A2ISSUE error stop
                    continueValidation = false;
                    break;
                }
            }

        } else if (nbaDst.getTransaction().getWorkType().equals(NbaConstants.A_WT_CORRESPONDENCE)) {
            continueValidation = false;
        }
        if (continueValidation) {
			setWorkItemId(nbaDst.getID());	//NBA050
            aNbaDst = nbaDst;
            validateTransaction();
        }
    } catch (Exception e) {
        getLogger().logError(e);
        e.printStackTrace(System.out);
    }
}
/**
 * This method is validates if a Correspondence event has occured for a Host
 * transaction. If an event has occured a Correspondence Work Item is created.
 * @param nbaTxlifeRequest A Host Transaction request
 * @param nbaTxlifeResponse A Host Transaction response 
 * @param transactionId An AWD transaction Id.
 */
public void validateHostEvent(NbaTXLife nbaTxlifeRequest, NbaTXLife nbaTxlifeResponse, String workItemId) {
    if (!isAutomatedCorrespondenceEnabled()){	//NBA208-14
        return;
    }
    if (isValidTransactionRequest(nbaTxlifeRequest, workItemId) && isValidHostResponse(nbaTxlifeResponse)) {
		setNbaTXLife(nbaTxlifeRequest); //NBA050
		setWorkItemId(workItemId); //NBA050
        validateTransaction();
    }
}
//NBA208-14-14 code deleted

/**
 * This method validates if Correspondence events have occured for an AWD or
 * a Host transaction. If such events have occured a Correspondence Work Item
 * is created for each event.
 */
protected void validateTransaction() {
	NbaVpmsAdaptor adapter = null; // NBA208-34
	try {
		boolean isHostEvent = (getNbaTXLife() != null ? true : false);	//NBA050
		NbaOinkDataAccess dataAccess = new NbaOinkDataAccess();
		//for a Host event the data going into VP/MS is retrieved from a different Source
		if (isHostEvent) {
			NbaCorrespondenceUtils utils = new NbaCorrespondenceUtils(getUserVO()); //NBA146
			utils.setTransactionID(getWorkItemId());	//NBA050
			aNbaDst = utils.retrieveCase();
			TXLifeRequest request = getNbaTXLife().getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);	//NBA050
			if (request.getDataTransmittalSubTypeCount() > 0) {
				getDeOinkAttributes().put("A_DATATRANSSUBTYPE", String.valueOf(request.getDataTransmittalSubTypeAt(0).getObjectType()));
			}
			getDeOinkAttributes().put("A_TRANSMODE", String.valueOf(request.getTransMode()));
			getDeOinkAttributes().put("A_TRANSTYPE", String.valueOf(request.getTransType()));
			//dataAccess.setContractSource(aNbaTxLifeRequest, aNbaDst.getNbaLob());
			//currently Oink can not process aNbaTXlifeRequest data
			dataAccess.setDstSource(aNbaDst);
			
			adapter = new NbaVpmsAdaptor(dataAccess, NbaVpmsAdaptor.CORRESPONDENCE); // NBA208-34
			invokeVPMS(getWorkItemId(),adapter); //NBA208-34
		} else {
			//AXAL3.7.13I Begin
			//ALS5325 Code Deleted
			dataAccess.setLobSource(aNbaDst.getNbaLob()); //ALS5325
			
			
			//PERF-APSL651 code deleted
		    //An AWD event will occur only if there is a status change or a new work Item is created
			if (aNbaDst.isTransaction()) {
			    invokeVPMS(getWorkItemId(),adapter);	//NBA050 NBA208-34
			} else { //A Case
					//Only retrieve the 203 record if required
				//begin PERF-APSL651
				String newStatus = "false";
				if (aNbaDst.getWorkItem().getPreviousStatus() != null) {
					newStatus = "true";
				}
				if (pendingRecordRequired(newStatus, getUserVO().getUserID())) {//ALII1985
					NbaTXLife txLife203 = NbaContractAccess.doContractInquiry(createRequestObject(aNbaDst, READ, null)); //AXAL3.7.13
					dataAccess.setContractSource(txLife203);	//AXAL3.7.13
				}
				adapter = new NbaVpmsAdaptor(dataAccess, NbaVpmsAdaptor.CORRESPONDENCE); 
				//end PERF-APSL651
				
				if (NbaConstants.PROC_CLOSURE_CHECK.equals(getUserVO().getUserID())){//ALPC96  //ALS4447
					invokeVPMSwithProcessID(getWorkItemId(),adapter, getUserVO().getUserID());//ALPC96
				} //A new Case will never get created by code, atmost we would have only a Status change
				
				if (NbaConstants.PROC_NTO_NOTIFICATION.equals(getUserVO().getUserID())){//APSL3754
					invokeVPMSwithProcessID(getWorkItemId(),adapter, getUserVO().getUserID());
				}
				else if (aNbaDst.hasNewStatus()) {
					//begin ALS4907
					//PERF-APSL651 code deleted/moved
					getDeOinkAttributes().put("A_HASNEWSTATUSDST", newStatus);
					invokeVPMSwithProcessID(getWorkItemId(),adapter, getUserVO().getUserID());//ALPC96 //ALII1985
					//end ALS4907
				} else{				 
					//there was no Case status change, check if there was a new tx added or a tx status change

					String anId = null;
					for (int i = 0; i < aNbaDst.getTransactions().size(); i++) {
						WorkItem tx = (WorkItem) aNbaDst.getTransactions().get(i);  //NBA208-32
						String workType = tx.getWorkType(); //ALII1334
						anId = tx.getItemID();  //NBA208-32
						boolean invokeVPMS = false;//NBA208-37
						if (!"NBCORRINFO".equalsIgnoreCase(workType)) { //ALII1334
							if (anId == null) { //This is a new Work Item
								getDeOinkAttributes().put("A_ISNEWWORKITEMDST", "Y");
								anId = NOID_PREFIX + i;
								invokeVPMS = true;//NBA208-37
							}
							//NBA208-37 Code Deleted
							//Begin NBA208-37
							if(tx.hasNewStatus()){
								invokeVPMS = true;
								getDeOinkAttributes().put("A_HASNEWSTATUSDST", "Y");
							} 
						} //ALII1334
						if(invokeVPMS) {
						    getDeOinkAttributes().put("A_WORKTYPELOB", workType); //ALII1334
						    //begin ALII1985
						    if(workType.equals("NBREQRMNT")){
						    	getDeOinkAttributes().put("A_REQTYPELOB", tx.getReqType());
						    	getDeOinkAttributes().put(NbaVpmsConstants.A_PROCESS_ID, getUserVO().getUserID());
						    }
						    //end ALII1985
						    // APSL3382 CDN letters for overridable CV's
						    invokeVPMSwithProcessID(anId,adapter, getUserVO().getUserID()); // NBA208-34
						}
                       //End NBA208-37
					}
				}
			}
		}
		
	} catch (Exception e) {
		getLogger().logError(e);
	// begin NBA208-34
	} finally {
		try {
		    if (adapter != null) {
		        adapter.remove();					
			}
		} catch (Exception e) {
			getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			e.printStackTrace(System.out);
		}
	}
    // end NBA208-34
}

//ALPC96 new method
protected void invokeVPMSwithProcessID(String eventID, NbaVpmsAdaptor adapter, String processId) { 
	   try {
	        adapter.getProduct().resetSession();
	        getDeOinkAttributes().put(NbaVpmsConstants.A_PROCESS_ID, processId);
	        adapter.setSkipAttributesMap(getDeOinkAttributes());
	        adapter.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_LETTERS);
	        getLettersForEvents().put(eventID, adapter.getResults().getResult());
	    } catch (NbaBaseException e) {
	        getLogger().logError("Error invoking VPMS Model" + NbaVpmsAdaptor.CORRESPONDENCE + ". " + e);
	        e.printStackTrace(System.out);
	    } catch (RemoteException e) {
	        getLogger().logError("Error invoking VPMS Model" + NbaVpmsAdaptor.CORRESPONDENCE + ". " + e);
	        e.printStackTrace(System.out);
	    } 
	}

/**
 * This method is validates if a Correspondence event has occured
 * for a database update event. If a Correspondence event has occured, 
 * a Correspondence Work Item is created.
 * @param nbaTxlife a NbaTxlife containing the updated contract in its TXLifeResponse object
 */
//NBA050 New Method
public void validateDatabaseEvent(NbaTXLife nbaTxlife, NbaDst nbaDst) {
	NbaVpmsAdaptor adapter = null; //NBA208-34
    if (!isAutomatedCorrespondenceEnabled()){	//NBA208-14
        return;
    }
	// Retrieve a DST with the Case and sources for the current work item 
	try {
		NbaCorrespondenceUtils utils = new NbaCorrespondenceUtils(getUserVO()); //NBA146
		setWorkItemId(nbaDst.getID());
		utils.setTransactionID(nbaDst.getID());
		//NBA213 code deleted
		// Determine which letters are needed

		getDeOinkAttributes().put("A_DATATRANSSUBTYPE", "");
		getDeOinkAttributes().put("A_TRANSMODE", "");
		getDeOinkAttributes().put("A_TRANSTYPE", nbaTxlife.getBusinessProcess());
		getDeOinkAttributes().put("A_HASNEWSTATUSDST", "false"); //ALS4907
		NbaOinkDataAccess dataAccess = new NbaOinkDataAccess();
		if (nbaTxlife != null) {	//SPR2061
			dataAccess.setContractSource(nbaTxlife);	//SPR2061
		}	//SPR2061
		
		dataAccess.setDstSource(nbaDst);	//NBA213
		adapter = new NbaVpmsAdaptor(dataAccess, NbaVpmsAdaptor.CORRESPONDENCE); // NBA208-34
		invokeVPMS(getWorkItemId(),adapter); // NBA208-34
		// SPR1720 code deleted
	} catch (Exception e) {
		getLogger().logError(e); //SPR1720
		e.printStackTrace(System.out);
		// begin NBA208-34
	}finally {
		try {
		    if (adapter != null) {
		        adapter.remove();					
			}
		} catch (Exception e) {
			getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
			e.printStackTrace(System.out);
		}
	}
	// end NBA208-34 
}
	/**
	 * Returns the nbaTXLife.
	 * @return NbaTXLife
	 */
	//NBA050 New Method
	protected NbaTXLife getNbaTXLife() {	//NBA208-14 changed method visibility
		return nbaTXLife;
	}

	/**
	 * Sets the nbaTXLife.
	 * @param nbaTXLife The nbaTXLife to set
	 */
	//NBA050 New Method
	protected void setNbaTXLife(NbaTXLife nbaTXLife) {	//NBA208-14 changed method visibility
		this.nbaTXLife = nbaTXLife;
	}

	/**
	 * Returns the workItemId.
	 * @return String
	 */
	//NBA050 New Method
	protected String getWorkItemId() {	//NBA208-14 changed method visibility
		return workItemId;
	}

	/**
	 * Sets the workItemId.
	 * @param workItemId The workItemId to set
	 */
	//NBA050 New Method
	private void setWorkItemId(String workItemId) {	//NBA208-14 changed method visibility
		this.workItemId = workItemId;
	}

    //NBA146 new method
	protected NbaUserVO getUserVO() {	//NBA208-14 changed method visibility
        return userVO;
    }
	//NBA146 new method
	protected void setUserVO(NbaUserVO userVO) {	//NBA208-14 changed method visibility
        this.userVO = userVO;
    }

	/**
     * @return the automatedCorrespondenceEnabled.
     */
    //NBA208-14 New Method
    protected String getAutomatedCorrespondenceEnabled() {
        return automatedCorrespondenceEnabled;
    }
    /**
     * @param automatedCorrespondenceEnabled The automatedCorrespondenceEnabled to set.
     */
    //NBA208-14 New Method
    protected void setAutomatedCorrespondenceEnabled(String automatedCorrespondenceEnabled) {
        this.automatedCorrespondenceEnabled = automatedCorrespondenceEnabled;
    }
    /**
     * Return true if the value of automatedCorrespondenceEnabled is "Y".
     */
    //NBA208-14 New Method
    protected boolean isAutomatedCorrespondenceEnabled() {
        return NbaConstants.YES_VALUE.equalsIgnoreCase(getAutomatedCorrespondenceEnabled());
    }
    
    // AXAL3.7.13 - New method
    public NbaTXRequestVO createRequestObject(NbaDst nbaDst, int access, String businessProcess) {
    	NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
    	nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
    	nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
    	nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
    	nbaTXRequest.setNbaLob(nbaDst.getNbaLob());
    	nbaTXRequest.setNbaUser(getUserVO());
    	nbaTXRequest.setWorkitemId(nbaDst.getID()); 
    	nbaTXRequest.setCaseInd(nbaDst.isCase()); 
    	if (access != -1) {
    		nbaTXRequest.setAccessIntent(access); 
    	} else {
    		nbaTXRequest.setAccessIntent(READ);
    	}
    	if (businessProcess != null) {
    		nbaTXRequest.setBusinessProcess(businessProcess);
    	} else {
    		nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUserVO())); //SPR2639 
    	}
    	return nbaTXRequest;
    }
    
    //PERF-APSL651 new method //ALII1985 signature changed
	private boolean pendingRecordRequired(String newStatus, String userID) {
		// if closure check, it's required
		if (NbaConstants.PROC_CLOSURE_CHECK.equalsIgnoreCase(userID) || NbaConstants.PROC_REQUIREMENT_DETERMINATION.equalsIgnoreCase(userID)) {//ALII1985
			return true;
		}
		if (NbaConstants.A_WT_APPLICATION.equalsIgnoreCase(aNbaDst.getWorkType()) && NbaConstants.TRUE_STR.equalsIgnoreCase(newStatus)
				&& hasPolicyNumber()) {
			return true;
		}
		return false;
	}   
    
    
    //PERF-APSL651 new method
    private boolean hasPolicyNumber() {
    	String polNum = aNbaDst.getNbaLob().getPolicyNumber();
    	return polNum != null && polNum.trim().length() > 0; //ALII1334
    }
    //CR58636 New Method ADC Retrofit
    public String isImageAttached(String letter)
    {
    	String isImage="false";
    	if(letter.indexOf("%%")!=-1){
        NbaStringTokenizer result = new NbaStringTokenizer(letter, "%%");  
        while (result.hasMoreTokens()) {
        	isImage = result.nextToken();		
        }}
        return isImage;

    }
    
    //CR58636 New Method ADC Retrofit
    public String setLetterName(String letter)
    {
    	String letterName=letter;
        if(letter.indexOf("%%")!=-1){
        NbaStringTokenizer result = new NbaStringTokenizer(letter, "%%"); 
        while (result.hasMoreTokens()) {
        	letterName = result.nextToken();	
        	break;
        }
        }
        return letterName;
    }
}
