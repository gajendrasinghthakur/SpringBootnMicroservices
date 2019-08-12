package com.csc.fsg.nba.business.process;

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
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 */
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.fs.accel.valueobject.LobData;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapter;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapterFactory;
import com.csc.fsg.nba.correspondence.NbaCorrespondenceUtils;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.reinsurance.rgaschema.AttachedFile;
import com.csc.fsg.nba.reinsurance.rgaschema.Case;
import com.csc.fsg.nba.reinsurance.rgaschema.Cases;
import com.csc.fsg.nba.reinsurance.rgaschema.Document;
import com.csc.fsg.nba.reinsurance.rgaschema.Documents;
import com.csc.fsg.nba.reinsurance.rgaschema.NbaRgaRequest;
import com.csc.fsg.nba.reinsurance.rgaschema.ReinsuranceCases;
import com.csc.fsg.nba.reinsuranceadapter.NbaReinsuranceAdapterFacade;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Reinsurer;
import com.csc.fsg.nba.vo.txlife.Attachment;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsResultsData;

/**
 * NbaProcReinsurerCommunications is the abstract class that provides basic processing
 * for all reinsurer communications.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA038</td><td>Version 3</td><td>Reinsurance</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>SPR2380</td><td>Version 5</td><td>Cleanup</td></tr> 
 * <tr><td>NBA122</td><td>Version 5</td><td>Underwriter Workbench Rewrite</td></tr>
 * <tr><td>SPR3303</td><td>Version 7</td><td>Images Excluded from RGA XML Request for Facultative Reinsurance</td></tr>
 * <tr><td>NBA212</td><td>Version 7</td><td>Content Services</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>AXAL3.7.32</td><td>Axa Life Phase 2</td><td>Reinsurer Interface</td></tr>
 * <tr><td>CR1343967</td><td>Axa Life Phase 2 R2</td><td>Generali Reinsurance CR</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public abstract class NbaProcReinsurerCommunications extends NbaAutomatedProcess implements NbaConstants {
    protected java.lang.String target = null;
    protected Reinsurer configRien; //ACN012
    public final java.lang.String FILENAME = "FILENAME";
    public final java.lang.String DATA = "DATA";
    //SPR2380 removed logger
    protected NbaDst parent = null;
    /**
     * NbaProcProviderCommunications default constructor.
     */
    public NbaProcReinsurerCommunications() {
        super();
    }

    //NBA103 - removed method

    /**
     * This abstract method allows each reinsurer communication subclass the ability
     * to perform any processing that may be required specifically for their reinsurer.
     * @param aSource an <code>NbaSource</code> object that contains a reinsurer-ready  transaction
     * @return an Object determined by the subclass
     */
    //AXAL3.7.32 Method Modified
    public Object doReinsurerSpecificProcessing(NbaSource aSource) throws NbaBaseException {
        NbaRgaRequest nbaRgaRequest = new NbaRgaRequest(aSource.getText());
        ReinsuranceCases reinsuranceCases = nbaRgaRequest.getReinsuranceCases();
        //Add all the images in the resinurance request.
        List images = getDocumentsToCopy(reinsuranceCases.getCases());
        for(int i = 0;i<images.size();i++) {
            AttachedFile attachedFile = new AttachedFile();
            attachedFile.setFileName((String)((Map)images.get(i)).get(FILENAME));
            attachedFile.setFile(((String)((Map)images.get(i)).get(DATA)));
            reinsuranceCases.addAttachedFile(attachedFile);
        }
        //Create attachedFile object for Correspondence Letter
        AttachedFile attachedCorrespondenceLetter = new AttachedFile();
        String letterName = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.REINSURANCE_LETTER_NAME);
        attachedCorrespondenceLetter.setFileName(letterName);
        byte[] letter = generateCorrespondenceLetter(letterName);
        saveReinsuranceLetterAsSource(work, letter, A_ST_CORRESPONDENCE_LETTER);//NBLXA-1331 //LOB values to be set on source can be passed as argument.  
        attachedCorrespondenceLetter.setFile(NbaBase64.encodeBytes(letter));
        reinsuranceCases.addAttachedFile(attachedCorrespondenceLetter);
        Cases cases = reinsuranceCases.getCases();
        Case aCase = cases.getCaseAt(0);
        if(aCase != null) {
            Documents documents = aCase.getDocuments();
            if (documents == null) {//ALII377
                documents = new Documents();
                documents.setCount("0");
                aCase.setDocuments(documents);
            }
            Document document = new Document();
            documents.addDocument(document);
            long count = documents.getCount().longValue() + 1; 
            document.setID(String.valueOf(count));
            document.setFilename(letterName);
            documents.setCount(String.valueOf(count));
        }
        return nbaRgaRequest.toXmlString();
    }
    
	// NBLXA-1331 Starts
	protected void saveReinsuranceLetterAsSource(NbaDst work, byte[] letter, String sourceType) throws NbaBaseException {// TODO verify lob values to
																															// be set on
																															// correspondence letter
																															// source
		String encodeFinalImage = NbaBase64.encodeBytes(letter);
		if (encodeFinalImage != null) {
			WorkItemSource newWorkItemSource = new WorkItemSource();
			newWorkItemSource.setCreate("Y");
			newWorkItemSource.setRelate("Y");
			newWorkItemSource.setLobData(new ArrayList());
			newWorkItemSource.setBusinessArea(A_BA_NBA);
			newWorkItemSource.setSourceType(sourceType);
			newWorkItemSource.setSize(0);
			newWorkItemSource.setPages(1);
			LobData newLob1 = new LobData();
			newLob1.setDataName(NbaLob.A_LOB_DISTRIBUTION_CHANNEL);
			newLob1.setDataValue(Long.toString(getWorkLobs().getDistChannel()));
			newWorkItemSource.getLobData().add(newLob1);
			LobData newLob2 = new LobData();
			newLob2.setDataName(NbaLob.A_LOB_LETTER_TYPE);
			if (getWork().getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_TEMP_REINSURANCE))
				newLob2.setDataValue("Additional Info Reinsurance Letter");
			else
				newLob2.setDataValue("Reinsurance Cover Letter");
			newWorkItemSource.getLobData().add(newLob2);

			LobData newLob3 = new LobData();
			newLob3.setDataName(NbaLob.A_LOB_POLICY_NUMBER);
			newLob3.setDataValue(getWorkLobs().getPolicyNumber());
			newWorkItemSource.getLobData().add(newLob3);
			newWorkItemSource.setText(NbaUtils.getGUID());
			newWorkItemSource.setFileName(null);
			newWorkItemSource.setFormat(NbaConstants.A_SOURCE_IMAGE);
			newWorkItemSource.setSourceStream(encodeFinalImage);
			work.getNbaTransaction().addNbaSource(new NbaSource(newWorkItemSource));
			if (getWork().getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_TEMP_REINSURANCE))
				work.getNbaLob().setLetterType(NbaConstants.ADDITIONAL_INFO_LETTER);
			else
				work.getNbaLob().setLetterType(NbaConstants.REINSURANCE_LETTER);
		} else {
			addComment("No Image returned from xPressions ");
		}
	}

	// NBLXA-1331 Ends
	/**
     * Copies the document files from the workflow system listed in the reinsurance request.
     * @param cases
     * @return list
     */
    //SPR3303 New Method
    protected List getDocumentsToCopy(Cases cases) throws NbaBaseException {
        List aList = new ArrayList();
        Documents documents = getDocuments(cases);
        Document doc = null;
        List images;    //NBA212
        List sources = getAllSources();
        if (documents != null) {
            int count = documents.getDocumentCount();
            int imageCount; //NBA212
            for (int i = 0 ; i < count; i++) {
                doc = documents.getDocumentAt(i);
                NbaSource nbaSource = getSource(sources, doc.getFilename());
                if (nbaSource != null && !nbaSource.isTextFormat()) {//AXAL3.7.32
                    images = retrieveWorkflowImage(nbaSource);  //NBA212 AXAL3.7.32
                    imageCount = images.size(); //NBA212
                    for (int j = 0; j < imageCount; j++) {  //NBA212
                        Map aMap = new HashMap();//AXAL3.7.32
                        aMap.put(FILENAME, doc.getFilename());
                        aMap.put(DATA, images.get(j));  //NBA212
                        aList.add(aMap);
                    }   //NBA212    
                }
            }
        }
        return aList;
    }
    
    /**
     * Returns a source from the list of source, whose source id is equal to the id passd in the parameter.
     * @param sources
     * @param source id
     * @return
     */
    //AXAL3.7.32 New Method
    protected NbaSource getSource(List sources , String sourceID) {
        if(sources != null && sourceID != null){
            for(int i =0; i<sources.size();i++) {
                NbaSource source = (NbaSource) sources.get(i);
                if(sourceID.equalsIgnoreCase(source.getID())) {
                    return source;
                }
            }
        }
        return null;
    }
    
    /**
     * Returns a list of documents from the reinsurance request.
     * @param cases
     * @return
     */
    //SPR3303 New Method
    protected Documents getDocuments(Cases cases) {
        Documents documents = null;
        int count = cases.getCaseCount();
        for (int i = 0; i < count; i++) {
            Case rgaCase = cases.getCaseAt(i);
            if (documents == null) {
                documents = rgaCase.getDocuments();
            } else {
                documents.getDocument().addAll(rgaCase.getDocuments().getDocument());
            }
        }
        return documents;
    }
    
    /**
     * This method allows each reinsurer communication subclass the ability
     * to evaluate the response from their reinsurer to determine if the process was
     * successful. 
     * @param response a <code>String</code> that contains the response from the reinsurer
     * @return <code>true</code> if the response is successful and <code>false</code> if unsuccessful
     */
    public boolean evaluateResponse(String response) throws NbaBaseException {
        if (response != null && response.trim().length() > 0) {
            return true;
        }
        return false;
    }
    /**
     * This method drives the Provider Communications process.  It first initializes the statuses for
     * the work item and then retrieves the work item from AWD.  It then looks through the NbaSources
     * to locate the reinsurer-ready transaction. If the reinsurer-ready transaction is not found, 
     * the process fails.
     * An <code>NbaReinsuranceAdapterFacade</code> is created to help process the work item.  Reinsurer
     * specific processing is executed so that the reinsurer communication will succeed and then the
     * transaction is submitted to the reinsurer.  When the response is received, it is evaluated and, if
     * unsuccessful, an error is repoted.  If the transaction was sent successfully, the process will 
     * determine if the work item needs to be suspended by calling a VP/MS model, and, if so, suspends
     * the work item.  If no suspension is required, the work item is updated to move to the next queue.
     * @param user the user/process for whom the process is being executed
     * @param work a DST value object for which the process is to occur
     * @return an NbaAutomatedProcessResult containing information about the success or failure of the process
     */
    public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
        // Initialization
        if (!initialize(user, work)) {
            return statusProcessFailed();
        }
        retrieveWork();
        retrieveParent();//AXAL3.7.32 Now retrieve the parent for all NBREINSURE also
        
        try{
            java.util.ListIterator li = getWork().getNbaSources().listIterator();
            NbaSource xmlTransactionSource = null;
            while (li.hasNext()) {
                NbaSource tempSource = (NbaSource) li.next();
                if (tempSource.getSource().getSourceType().equals(NbaConstants.A_ST_REINSURANCE_TRANSACTION)) {
                    xmlTransactionSource = tempSource;
                    break; //NBA212
                }
            }
            if (xmlTransactionSource == null) {
                setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "No Reinsurer Transaction", getAWDFailStatus()));
                return result;
            }

            initializeTarget();
            //AXAL3.7.32 Code Deleted
            Object data = doReinsurerSpecificProcessing(xmlTransactionSource);
            if (data == null) {
                setResult(
                    new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "No Reinsurer Transaction for Requirement Status", getFailStatus()));
                return result;
            }

            NbaReinsuranceAdapterFacade adapter = new NbaReinsuranceAdapterFacade(getWork(), getUser());
            adapter.setConfigRien(getConfigRien());
            NbaTXLife txLife  = (NbaTXLife) adapter.sendMessageToProvider(target, data);
            if (txLife != null) {
                setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "SUCCESSFUL", getPassStatus()));
                int suspendDays = 0;
                if(getWork().getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_TEMP_REINSURANCE)){              
                    processAdditionalInfo();                
                }else{
                    suspendDays = getSuspendDays();
                }
                changeStatus(getResult().getStatus());
                if (suspendDays == 0) {
                    doUpdateWorkItem();
                } else {
                    suspendTransaction(suspendDays);
                }
            } else {
                addComment("Unable to evaluate Reinsurer response"); //NBA103
                setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "FAILURE", getFailStatus()));
                changeStatus(getResult().getStatus());
                doUpdateWorkItem();
            }
        }catch(NbaBaseException ex) {//AXAL3.7.32
            //APSL3874 code deleted
            unlockParentWork(); 
            throw ex; //APSL3874
        }
        unlockParentWork();//AXAL3.7.32
        return getResult();
    }
    /**
     * Call the correspondence webservice and generates the reinsurance correspondence letter.
     * @return byte[] correspondence letter as byte array
     */
    // AXAL3.7.32 New Method
    protected byte[] generateCorrespondenceLetter(String letterName) throws NbaBaseException{
        try {
            NbaCorrespondenceAdapter adapter = new NbaCorrespondenceAdapterFactory().getAdapterInstance();
            adapter.initializeObjects(getWork(), getUser());
            adapter.setLetterType(NbaCorrespondenceUtils.LETTER_EVENTDRIVEN);
            return adapter.getLetterAsPDF(letterName, null);
        } catch (NbaBaseException e) {
            getLogger().logError("Error in generating the reinsurance correspondence letter " + e.getMessage()); 
            throw new NbaBaseException(e, NbaExceptionType.FATAL);
        }
    }
    
    /**
     * This method creates and initializes an <code>NbaVpmsAdaptor</code> object to
     * call VP/MS to execute the entryPoint.
     * @param entryPoint the entry point in the VP/MS model to be executed
     * @return the results from the VP/MS call in the form of an <code>NbaVpmsResultsData</code> object
     */
    public NbaVpmsResultsData getDataFromVpms(String entryPoint) throws NbaBaseException, NbaVpmsException {
        NbaVpmsAdaptor vpmsProxy = null; //SPR3362
        try {
            NbaOinkDataAccess oinkData = new NbaOinkDataAccess(getWork().getNbaLob());
            vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.REINSURANCE); //SPR3362
            vpmsProxy.setVpmsEntryPoint(entryPoint);
            NbaVpmsResultsData data = new NbaVpmsResultsData(vpmsProxy.getResults());
            //SPR3362 code deleted
            return data;
        } catch (java.rmi.RemoteException re) {
            throw new NbaVpmsException("Reinsurer Communication Process Problem" + NbaVpmsException.VPMS_EXCEPTION, re);
        //begin SPR3362 
        } finally {
            try {
                if (vpmsProxy != null) {
                    vpmsProxy.remove();                 
                }
            } catch (RemoteException re) {
                getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED); 
            }
        }
        //end SPR3362
    }
    /**
     * Answers the reinsurer configuration from the NbaConfiguration file.
     * @return an NbaConfigProvider
     */
    // ACN012 changed return type
    public Reinsurer getConfigRien() throws NbaBaseException { 
        if (configRien == null) {
            configRien = NbaConfiguration.getInstance().getReinsurer(getWork().getNbaLob().getReinVendorID());
        }
        return configRien;
    }
    /**
     * This method retrieves the number of suspend days from the VP/MS Reinsurance model.
     * @return the number of suspend days from the model or 0 if no value returned
     */
    public int getSuspendDays() throws NbaBaseException {
        getLogger().logDebug("Getting suspend days");
        NbaVpmsResultsData data = getDataFromVpms(NbaVpmsAdaptor.EP_REIN_ORDER_SUSPEND_DAYS);
        int suspendDays = 0;
        if (data != null && data.getResultsData() != null) {
            suspendDays = Integer.parseInt((String) data.getResultsData().get(0));
        }
        return suspendDays;
    }
    /**
     * Answers the target for the reinsurer.  The target may be a URL or a file path from the 
     * NbaConfiguration file.
     * @return the target for this reinsurer
     */
    public java.lang.String getTarget() {
        return target;
    }
    /**
     * This abstract method allows each provider communication subclass the ability
     * to initialize the target needed for their provider and for the specific reinsurer,
     * if necessary.
     */
    public abstract void initializeTarget() throws NbaBaseException;
    /**
     * Retrieves the work item and all of it's associated sources from AWD.  Once retrieved,
     * it set's this process' work item to the retrieved one.
     */
    public void retrieveWork() throws NbaBaseException {
        //NBA213 deleted code
        NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
        retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
        retrieveOptionsValueObject.requestSources();
        retrieveOptionsValueObject.setLockWorkItem();
        setWork(retrieveWorkItem(getUser(), retrieveOptionsValueObject));  //NBA213
        //NBA213 deleted code
    }
    /**
     * Retrieves the parent case and sibling work items from AWD.  
     */
    public void retrieveParent() throws NbaBaseException {
        //NBA213 deleted code
        NbaAwdRetrieveOptionsVO retrieveOptionsValueObject = new NbaAwdRetrieveOptionsVO();
        retrieveOptionsValueObject.setWorkItem(getWork().getID(), false);
        retrieveOptionsValueObject.requestCaseAsParent();
        retrieveOptionsValueObject.requestSources();//AXAL3.7.32
        retrieveOptionsValueObject.requestTransactionAsSibling();
        retrieveOptionsValueObject.setLockWorkItem();
        retrieveOptionsValueObject.setLockParentCase();
        setParent(retrieveWorkItem(getUser(), retrieveOptionsValueObject));  //NBA213
        //NBA213 deleted code
    }
    /**
     * Sets the reinsurer configuration infomation from the NbaConfiguration file.
     * @param newProvider the provider retrieved from the NbaConfiguration file
     */
    // ACN012 changed return type
    public void setConfigRien(Reinsurer newConfig) {
        configRien = newConfig;
    }
    /**
     * Sets the target for the reinsurer.  The target may be a URL or a file path from the 
     * NbaConfiguration file.
     * @param newTarget the target from the NbaConfiguration file
     */
    public void setTarget(java.lang.String newTarget) {
        target = newTarget;
    }
    /**
     * This method suspends a work item by using the work item information and
     * the supplied suspend date to populate the suspendVO.
     * @param suspendDays the number of days to suspend
     */
    public void suspendTransaction(int suspendDays) throws NbaBaseException {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_WEEK, suspendDays);
        Date reqSusDate = (calendar.getTime());
        addComment("Suspended awaiting matching work item");
        NbaSuspendVO suspendVO = new NbaSuspendVO();
        suspendVO.setTransactionID(getWork().getID());
        suspendVO.setActivationDate(reqSusDate);
        updateForSuspend(suspendVO);
    }
    /**
     * Since the work item must be suspended before it can be unlocked, this method
     * is used instead of the superclass method to update AWD.
     * <P>This method updates the work item in the AWD system, suspends the 
     * work item using the supsendVO, and then unlocks the work item.
     * @param suspendVO the suspend value object created by the process to be used
     * in suspending the work item.
     */
    public void updateForSuspend(NbaSuspendVO suspendVO) throws NbaBaseException {
        getLogger().logDebug("Starting updateForSuspend");
        updateWork(getUser(), getWork());  //NBA213
        suspendWork(getUser(), suspendVO);  //NBA213
        // APSL5055-NBA331.1 code deleted
    }
    /**
     * Answers parent case
     * @return
     */
    public NbaDst getParent() {
        return parent;
    }

    /**
     * Sets parent case
     * @param dst
     */
    public void setParent(NbaDst dst) {
        parent = dst;
    }

    /**
     * This method go through with all transaction to find the original reinsurance request workitem.
     * It then process that work item by associating the source from the temp work item to the
     * permanent work item.
     */
    protected void processAdditionalInfo()throws NbaBaseException{
        if(getParent() != null){
            List list = getParent().getNbaTransactions();
            NbaLob workLob = getWork().getNbaLob();
            for(int i = 0; i<list.size();i++){
                NbaTransaction trans = (NbaTransaction)list.get(i);
                if ((!trans.getID().equals(getWork().getID()))
                    && trans.getTransaction().getWorkType().equalsIgnoreCase(NbaConstants.A_WT_REINSURANCE)) {
                    NbaLob lob = trans.getNbaLob();
                    if(workLob.getReinVendorID().equalsIgnoreCase(lob.getReinVendorID()) && workLob.getPlan().equalsIgnoreCase(lob.getPlan())){
                        for (int j = 0; j < getWork().getNbaSources().size(); j++) {
                            NbaSource aSource = (NbaSource) getWork().getNbaSources().get(j);
                            if (!NbaConstants.A_ST_REINSURANCE_XML_TRANSACTION.equals(aSource.getSource().getSourceType())){//NBA122
                                trans.addNbaSource(aSource);
                            }//NBA122
                        }
                        updateWork(getUser(), getParent());  //NBA213
                        
                        //Sources are already created remove create and relate tags
                        List newSources = trans.getSources();
                        for (int sourceCount = 0; sourceCount < newSources.size(); sourceCount++) {
                            //NBA208-32
                            WorkItemSource newSource = (WorkItemSource) newSources.get(sourceCount);
                            newSource.setCreate(null);
                            newSource.setRelate(null);
                        }
                        
                        //Break temp reinsurance workitem relation with case                        
                        List transactions = getParent().getTransactions();
                        //NBA208-32
                        WorkItem tempTrans = null;
                        for(int k=0; k < transactions.size();k++){
                            //NBA208-32
                            WorkItem transaction = (WorkItem)transactions.get(k);
                            if(transaction.getItemID().equals(getWork().getID())){
                                tempTrans = transaction;    
                                transaction.setBreakRelation("Y");
                                //break source relation with workitem
                                //NBA208-32
                                for (int z = 0; z < getWork().getTransaction().getSourceChildren().size(); z++) {
                                    WorkItemSource source = (WorkItemSource) getWork().getTransaction().getSourceChildren().get(z);
                                    source.setBreakRelation("Y");                           
                                }
                                break;
                            }
                        }
                        updateWork(getUser(), getParent());  //NBA213
                        getParent().getTransactions().remove(tempTrans);
                        //unlockWork(getUser(), getParent());  //NBA213 AXAL3.7.32
                        getWork().getSources().clear();
                        return;
                    }                   
                }
            }
        }
        addComment("Parent case null"); //NBA103
        setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.FAILED, "FAILURE", getFailStatus()));
    }

    /**
     * Retrieve the base64 encoded image(s) from the workflow system for a WorkItemSource.
     * @param sourceID
     * @return List containing the base64 encoded image(s)
     * @throws NbaBaseException
     */
    //SPR3303 New Method
    //NBA212 changed method signature
    //AXAL3.7.32 Method signature changed
    protected List retrieveWorkflowImage(NbaSource nbaSource) throws NbaBaseException {
        //NBA208-32
        try {
            return WorkflowServiceHelper.getBase64SourceImage(getUser(), nbaSource);  //NBA213 AXAL3.7.32
        } catch (NbaBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new NbaBaseException("Error retrieving workflow images", e);
        }
    }
    
    //AXAL3.7.32 New Method
    protected List getAllSources() throws NbaBaseException {
        List sources = getParent().getNbaSources();
        Iterator it = getParent().getNbaTransactions().iterator();
        NbaTransaction tran;
        while (it.hasNext()) {
            tran = (NbaTransaction) it.next();
            sources.addAll(tran.getNbaSources());
        }
        return sources;
    }
    
    /**
     * Unlock all work items retrieved during processing
     * @throws NbaBaseException 
     */
    // AXAL3.7.32 New Method
    protected void unlockParentWork() throws NbaBaseException {
        getLogger().logDebug("Unlocking Work Items");
        if (getParent() != null) {
            NbaDst dst = getParent();
            dst.getWorkItem().getWorkItemChildren().clear();
            if (dst.isLocked(getUser().getUserID())) {
                unlockWork(getUser(), dst);
            }
        }
    }
    
}