package com.csc.fsg.nba.business.process;

/*
 * *******************************************************************************<BR>
 * Copyright 2015, Computer Sciences Corporation. All Rights Reserved.<BR>
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.csc.fs.ServiceContext;
import com.csc.fs.ServiceHandler;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fs.util.GUIDFactory;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapterFacade;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaMibPlanFDatabaseRequestVO;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.NbaWorkItem;
import com.csc.fsg.nba.vo.configuration.Company;
import com.csc.fsg.nba.vo.configuration.OrganizationKeys;
import com.csc.fsg.nba.vo.configuration.Provider;
import com.csc.fsg.nba.vo.nbaschema.NbaMibPlanF;
import com.csc.fsg.nba.vo.nbaschema.NbaMibPlanFControl;
import com.csc.fsg.nba.vo.txlife.ApplicationInfoExtension;
import com.csc.fsg.nba.vo.txlife.Carrier;
import com.csc.fsg.nba.vo.txlife.DistinguishedObject;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.MIBRequest;
import com.csc.fsg.nba.vo.txlife.MIBServiceDescriptor;
import com.csc.fsg.nba.vo.txlife.MIBServiceDescriptorOrMIBServiceConfigurationID;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.PersonOrOrganization;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TXLifeResponseExtension;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthRequestAndTXLifeRequest;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;

/**
 *  This automated process retrieves MIB Plan F follow up responses from MIB and stores them in the NBA_MIB_PLANF table in the NBAAUXILIARY  schema. 
 * 
 * MIB follow ups are stored in MIB by date. There may be zero to many follow ups for a date.  The request message to MIB to retrieve the follow ups 
 * supports values which indicate the begin date, end date, begin record number, and number of records to return.
 * 
 *  This automated process uses control records in the NBA_MIB_PLANF_CONTROL table in the NBAAUXILIARY  schema to determine the start date 
 *  (HIGH-WATER_DATE) and record number (NEXT_RECORD) to request from MIB. After successfully requesting  and processing any follow ups, the 
 *  date and record number are updated to indicate which follow ups have been retrieved.
 *  
 *  The NBA_MIB_PLANF_CONTROL table contains rows for each orgCode defined the  <Provider><OrganizationKeys><Company> entries for  
 *  the <Provider name="MIB" > entries in NbaConfiguration.xml.  The entries are automatically created the first time this automated process executes. 
 *  The initial value for the start date is set to the current date - 6 month.
 *  
 *  Processing flow:
 *  For a row in the NBA_MIB_PLANF_CONTROL table:
 *  
 *      If the start date is less than the current date, or if the date is equal to the current date and the begin record number is not equal to 1
 *      
 *          Construct a request message to MIB to request the next set of follow ups.
 *          
 *          Call the MIB service to retrieve the follow ups.
 *       
 *          If no follow ups are returned, advance the start date, reset the start record to 1 and repeat.
 *            
 *          If follow ups are returned,  de-construct the response message into individual messages for each contract in the message and
 *          store  the individual messages as rows in the NBA_MIB_PLANF table. Advance the start date and record number in the NBA_MIB_PLANF_CONTROL 
 *          table based on information in the response message.  Returns a "SUCCESSFULL" response to the poller. The poller will update the 
 *          success count and invoke this  automated process again if it has not been stopped.
 *          
 *  When all follow ups through the current date have been retrieved for an orgCode, process the next orgCode row in the NBA_MIB_PLANF_CONTROL table. 
 *  
 *  When all rows (orgCodes) in the NBA_MIB_PLANF_CONTROL table have been brought current,  return a "NOWORK" response to the 
 *  poller to cause it to enter into its sleep state.
 *  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA308</td><td>Version NB-1301</td><td>MIB Follow Ups</td></tr> 
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see NbaAutomatedProcess
 */
public class NbaProcRetrieveMibFollowUps extends NbaAutomatedProcess {
    protected static final String CARRIER_PARTY_1 = "Carrier_Party_1";
    protected static final Long FIRST_RECORD = 1L;
    protected static final String MIB = "MIB";
    protected static final String MIB_PLAN_F_DATABASE_ACCESS_BP = "MibPlanFDatabaseAccessBP";
    protected static final String TRUE_STRING = Integer.toString(NbaConstants.TRUE);
    protected List carrierCodesList;
    protected List controlRecords;
    protected String currentCarrierCode;
    protected NbaMibPlanFControl currentNbaMibPlanFControl;
    protected Date endDate;
    protected int mibFollowUpDays;
    protected String mibFollowUpInd;
    protected int mibFollowUpMaxRecords;
    protected List responseMessages;
    protected Date startDate;
    protected Long startRecord;
    protected String testIndicator;
    protected Date today;

    /**
     * This constructor calls the superclass constructor which will set
     * the appropriate statues for the process.
     */
    public NbaProcRetrieveMibFollowUps() {
        super();
    }

    /**
     * Call the MIB web service with the 404 request message.
     * @param tx404Request
     * @return a NbaTXLife containing  the <TXLife> in  web service response
     * @throws Exception
     */
    protected NbaTXLife callWebService(String tx404Request) throws Exception {
        NbaProviderAdapterFacade adapter = new NbaProviderAdapterFacade(MIB);
        String targetURL = null; //URL is defined in newBusinessConfig/config/sa/system/WebserviceMIBFollowups.xml
        String response = (String) adapter.sendMessageToProvider(targetURL, tx404Request,getUser(),"MIB");
        return new NbaTXLife(response);
    }

    /**
     * Commit database changes.
     * @throws NbaBaseException 
     */
    protected void commitRecords() throws NbaBaseException {
        if (!getResponseMessages().isEmpty()) {
            NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO = new NbaMibPlanFDatabaseRequestVO();
            nbaMibPlanFDatabaseRequestVO.setOperation(NbaMibPlanFDatabaseRequestVO.UPDATE_RESPONSES);
            nbaMibPlanFDatabaseRequestVO.setNbaMibPlanFResponseList(getResponseMessages());
            AccelResult accelResult = (AccelResult) ServiceHandler.invoke(MIB_PLAN_F_DATABASE_ACCESS_BP, ServiceContext.currentContext(),
                    nbaMibPlanFDatabaseRequestVO);
            NewBusinessAccelBP.processResult(accelResult);
        }
        if (!getControlRecords().isEmpty()) {
            NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO = new NbaMibPlanFDatabaseRequestVO();
            nbaMibPlanFDatabaseRequestVO.setOperation(NbaMibPlanFDatabaseRequestVO.UPDATE_CONTROL);
            nbaMibPlanFDatabaseRequestVO.setNbaMibPlanFControlList(getControlRecords());
            AccelResult accelResult = (AccelResult) ServiceHandler.invoke(MIB_PLAN_F_DATABASE_ACCESS_BP, ServiceContext.currentContext(),
                    nbaMibPlanFDatabaseRequestVO);
            NewBusinessAccelBP.processResult(accelResult);
        }
    }

    /**
     * Compare the orgCode(s) defined the  <Provider><OrganizationKeys><Company> entries for  
    *  the <Provider name="MIB" > entries in NbaConfiguration.xml to the NBA_MIB_PLANF_CONTROL 
    *  table results.
     * If configuration entries are found which are not in the NBA_MIB_PLANF_CONTROL table results,  
     * automatically create the table entries.  The initial value for the start date is set to the 
     * current date - 6 month.
     * @throws NbaBaseException
     */
    protected void createControlRecords() throws NbaBaseException {
        int count = getCarrierCodesList().size();
        for (int i = 0; i < count; i++) {
            String carrierCode = (String) getCarrierCodesList().get(i);
            boolean found = false;
            int ctlCount = getControlRecords().size();
            for (int j = 0; j < ctlCount; j++) {
                NbaMibPlanFControl nbaMibPlanFControl = (NbaMibPlanFControl) getControlRecords().get(j);
                if (nbaMibPlanFControl.getOrganizationCode().equalsIgnoreCase(carrierCode)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                getControlRecords().add(initializeControlRecord(carrierCode));
            }
        }
    }

    /** 
     * Create a  <TXLife> from the associated objects.
     * @param origTXLifeResponse
     * @param relationsList
     * @param formInstanceList
     * @param partyList
     * @return
     */
    protected NbaTXLife createIndividualResponse(TXLifeResponse origTXLifeResponse, Holding currentHolding, List relationsList,
            List formInstanceList, List partyList) {
        NbaTXLife newNbaTXLife = new NbaTXLife();
        TXLife newTXLife = new TXLife();
        newNbaTXLife.setTXLife(newTXLife);
        UserAuthResponseAndTXLifeResponseAndTXLifeNotify ua = new UserAuthResponseAndTXLifeResponseAndTXLifeNotify();
        newTXLife.setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(ua);
        TXLifeResponse newTXLifeResponse = new TXLifeResponse();
        ua.addTXLifeResponse(newTXLifeResponse);
        newTXLifeResponse.setTransRefGUID(origTXLifeResponse.getTransRefGUID());
        newTXLifeResponse.setTransType(origTXLifeResponse.getTransType());
        newTXLifeResponse.setTransExeDate(origTXLifeResponse.getTransExeDate());
        newTXLifeResponse.setTransExeTime(origTXLifeResponse.getTransExeTime());
        newTXLifeResponse.setTestIndicator(origTXLifeResponse.getTestIndicator());
        TXLifeResponseExtension origTXLifeResponseExtension = NbaUtils.getFirstTXLifeResponseExtension(origTXLifeResponse);
        TXLifeResponseExtension newTXLifeResponseExtension = NbaUtils.getFirstTXLifeResponseExtension(newTXLifeResponse);
        if (newTXLifeResponseExtension == null) {

			OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_TXLIFERESPONSE);
			newTXLifeResponse.addOLifEExtension(olifeExtension);
			newTXLifeResponseExtension = olifeExtension.getTXLifeResponseExtension();
			if (newTXLifeResponseExtension != null) {//APSL2253
				newTXLifeResponseExtension.setActionAdd();
			}
		}
        if (origTXLifeResponseExtension == null) {

			OLifEExtension olifeExtension = NbaTXLife.createOLifEExtension(NbaOliConstants.EXTCODE_TXLIFERESPONSE);
			origTXLifeResponse.addOLifEExtension(olifeExtension);
			origTXLifeResponseExtension = olifeExtension.getTXLifeResponseExtension();
			if (origTXLifeResponseExtension != null) {//APSL2253
				origTXLifeResponseExtension.setActionAdd();
			}
		}
        newTXLifeResponseExtension.setTransactionContext(origTXLifeResponseExtension.getTransactionContext());
        newTXLifeResponseExtension.setActionUpdate();
        newTXLifeResponse.setMIBRequest(origTXLifeResponse.getMIBRequest());
        newTXLifeResponse.setTransResult(new TransResult());
        newTXLifeResponse.getTransResult().setResultCode(origTXLifeResponse.getTransResult().getResultCode());
        newTXLifeResponse.getTransResult().setRecordsFound(1);
        OLifE newOlifLifE = new OLifE();
        newTXLifeResponse.setOLifE(newOlifLifE);
        newOlifLifE.addHolding(currentHolding);
        newOlifLifE.getRelation().addAll(relationsList);
        newOlifLifE.getFormInstance().addAll(formInstanceList);
        newOlifLifE.getParty().addAll(partyList);
        return newNbaTXLife;
    }

    /**
     *  Create a  NBA_MIB_PLANF table row containing the  newNbaTXLife and add the 
     * and add the NBA_MIB_PLANF table row to the list of rows to be committed.
     * @param currentHolding
     * @param sourceParty
     * @param newNbaTXLife
     */
    protected void createMibPlanFRow(Holding currentHolding, Party sourceParty, NbaTXLife newNbaTXLife) {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Follow Up set created from TxLife 404 Response Message:\n " + newNbaTXLife.toXmlString());
        }
        NbaMibPlanF nbaMibPlanF = new NbaMibPlanF();
        nbaMibPlanF.setReferenceId(GUIDFactory.create().getHex());
        nbaMibPlanF.setCarrierCode(getCurrentCarrierCode());
        nbaMibPlanF.setTrackingId(currentHolding.getPolicy().getApplicationInfo().getTrackingID());
        Person person = sourceParty.getPersonOrOrganization().getPerson();
        nbaMibPlanF.setLastName(person.getLastName());
        nbaMibPlanF.setFirstName(person.getFirstName());
        nbaMibPlanF.setData(newNbaTXLife.toXmlString());
        nbaMibPlanF.setCreateDate(getToday());
        nbaMibPlanF.setErrorMessage(" ");
        nbaMibPlanF.setStatusCode(NbaMibPlanF.STATUS_UNAPPLIED_FOLLOW_UP);
        getResponseMessages().add(nbaMibPlanF);
    }

    /**
     *  Retrieve and process MIB follow up results. 
     *  Continue processing until 
     *  - all results for all organization codes have been retrieved through the current date
     *  - or follow up results are returned from MIB for a request.
     *  Commit the changes to the MIB_PLANF and MIB_PLANF_CONTROL tables.     
     * 
     * @throws Exception 
     */
    protected void doProcess() throws Exception {
        boolean continueProcessing = true;
        while (continueProcessing) {
            continueProcessing = processFollowUpsRecords();
        }
        commitRecords();
    }

    /**
     * Retrieve follow up responses from MIB. Parse the responses into  <TXLifeResponse>s for the individual contracts
     * in the MIB resonse and store the individual <TXLifeResponse> in a database table.
     * @param user - the NbaUserVO object  
     * @param work - null
     * @return NbaAutomatedProcessResult containing the results of the process
     * @throws NbaBaseException 
     */
    public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Begin Execution of Retrieve MIB Follow Ups.");
        }
        try {
            setToday(new Date());
            setUser(user);
            retrieveConfigurationValues();
            retrieveControlRecords();
            doProcess();
        } catch (NbaBaseException e) {
            e.forceFatalExceptionType();
            throw e;
        } catch (Exception e) {
            NbaBaseException nbaBaseException = new NbaBaseException(e.toString(), e, NbaExceptionType.FATAL);
            throw nbaBaseException;
        }
        int recordsProcessed = getResponseMessages().size();
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("End Execution of Retrieve MIB Follow Ups. Records processed = " + recordsProcessed);
        }
        if (recordsProcessed > 0) {
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", ""));
        } else {
            setResult(new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", ""));
        }
        getResult().setCountSuccessful(recordsProcessed);
        return getResult();
    }

    protected List getCarrierCodesList() {
        return carrierCodesList;
    }

    protected List getControlRecords() {
        if (controlRecords == null) {
            controlRecords = new ArrayList();
        }
        return controlRecords;
    }

    protected String getCurrentCarrierCode() {
        return currentCarrierCode;
    }

    protected NbaMibPlanFControl getCurrentNbaMibPlanFControl() {
        return currentNbaMibPlanFControl;
    }

    protected Date getEndDate() {
        return endDate;
    }

    protected int getMibFollowUpDays() {
        return mibFollowUpDays;
    }

    protected String getMibFollowUpInd() {
        return mibFollowUpInd;
    }

    protected int getMibFollowUpMaxRecords() {
        return mibFollowUpMaxRecords;
    }

    /**
     * Create the 404 request message.
     * @return the message as a String
     */
    protected String getRequestMessage() {
        NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
        nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_MIBFOLLOWUP);
        nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
        nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(getUser()));
        //create txlife with default request fields
        NbaTXLife nbaTXLife = new NbaTXLife(nbaTXRequest);
        TXLife tXLife = nbaTXLife.getTXLife();
        UserAuthRequestAndTXLifeRequest userAuthRequestAndTXLifeRequest = tXLife.getUserAuthRequestAndTXLifeRequest();
        userAuthRequestAndTXLifeRequest.deleteUserAuthRequest();
        TXLifeRequest tXLifeRequest = userAuthRequestAndTXLifeRequest.getTXLifeRequestAt(0);
    	nbaTXLife.getTXLife().setVersion(NbaOliConstants.OLIFE_VERSION_39_02); 
    	OLifE olife = nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0).getOLifE();
     	olife.setVersion(NbaOliConstants.OLIFE_VERSION_39_02); 

        tXLifeRequest.setPrimaryObjectID(CARRIER_PARTY_1);
        tXLifeRequest.setMaxRecords(getMibFollowUpMaxRecords());
        tXLifeRequest.setStartRecord(getStartRecord().intValue());
        tXLifeRequest.setStartDate(NbaUtils.addDaysToDate(getStartDate(),-1));
        tXLifeRequest.setEndDate(NbaUtils.addDaysToDate(getEndDate(),-1));
        tXLifeRequest.setTestIndicator(getTestIndicator());
        MIBRequest mIBRequest = new MIBRequest();
        tXLifeRequest.setMIBRequest(mIBRequest);
        MIBServiceDescriptor mIBServiceDescriptor = new MIBServiceDescriptor();
        MIBServiceDescriptorOrMIBServiceConfigurationID mIBServiceDescriptorOrMIBServiceConfigurationID = new MIBServiceDescriptorOrMIBServiceConfigurationID();
        mIBServiceDescriptorOrMIBServiceConfigurationID.addMIBServiceDescriptor(mIBServiceDescriptor);
        mIBRequest.setMIBServiceDescriptorOrMIBServiceConfigurationID(mIBServiceDescriptorOrMIBServiceConfigurationID);
        mIBServiceDescriptor.setMIBService(NbaOliConstants.TC_MIBSERVICE_CHECKING);
        OLifE oLifE = tXLifeRequest.getOLifE();
        Party party = new Party();
        oLifE.addParty(party);
        party.setId(CARRIER_PARTY_1);
        party.setPartyTypeCode(NbaOliConstants.OLIX_PARTYTYPE_CORPORATION);
        party.setPersonOrOrganization(new PersonOrOrganization());
        party.getPersonOrOrganization().setOrganization(new Organization());
        Carrier carrier = new Carrier();
        party.setCarrier(carrier);
        carrier.setCarrierCode(getCurrentCarrierCode());
        String responseMessage = nbaTXLife.toXmlString();
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("TxLife 404 Request Message:\n " + responseMessage);
        }
        return responseMessage;
    }

    protected List getResponseMessages() {
        if (responseMessages == null) {
            responseMessages = new ArrayList();
        }
        return responseMessages;
    }

    protected Date getStartDate() {
        return startDate;
    }

    protected Long getStartRecord() {
        return startRecord;
    }

    protected String getTestIndicator() {
        return testIndicator;
    }

    protected Date getToday() {
        return today;
    }

    /**
     * Initialize a NBA_MIB_PLANF_CONTROL table  row. 
     * Set the OrganizationCode to the carrierCode. 
     * Set  the start date is set to the current date - 6 month.
     * @param carrierCode
     * @return the initialized a NBA_MIB_PLANF_CONTROL table  row.
     */
    protected NbaMibPlanFControl initializeControlRecord(String carrierCode) {
        NbaMibPlanFControl nbaMibPlanFControl = new NbaMibPlanFControl();
        nbaMibPlanFControl.setReferenceId(GUIDFactory.create().getHex());
        nbaMibPlanFControl.setHighWaterDate(NbaUtils.calcDayFotFutureDate(new Date(), -6));
        nbaMibPlanFControl.setNextRecord(FIRST_RECORD);
        nbaMibPlanFControl.setOrganizationCode(carrierCode);
        return nbaMibPlanFControl;
    }

    /**
     * Set the start date and next record values for the current Carrier Code.
     */
    protected void initValuesFromControlRecord() {
        int count = getControlRecords().size();
        for (int i = 0; i < count; i++) {
            setCurrentNbaMibPlanFControl((NbaMibPlanFControl) getControlRecords().get(i));
            if (getCurrentNbaMibPlanFControl().getOrganizationCode().equalsIgnoreCase(getCurrentCarrierCode())) {
                break;
            }
        }
        setStartDate(getCurrentNbaMibPlanFControl().getHighWaterDate());
        setStartRecord(getCurrentNbaMibPlanFControl().getNextRecord());
    }

    /**
     * Retrieve and process MIB follow up results.
     * Calculate the start and end dates for the 404 request message and create the request message. 
     * Send the  404 request message to MIB and process the results.
     * @return false if: 
     *  - all results for all  organization codes have been retrieved through the current date
     *  - any follow up results are returned from MIB  
     * @throws Exception 
     */
    protected boolean processFollowUpsRecords() throws Exception {
        if (!getCarrierCodesList().isEmpty()) { //Empty when all follow ups have been returned for carriers through the current date
            setCurrentCarrierCode((String) getCarrierCodesList().get(0));
            initValuesFromControlRecord();
            if (!(getStartRecord() > FIRST_RECORD)) {
                setStartDate(NbaUtils.addDaysToDate(getStartDate(), 1)); //Advance the start date if all records have been returned from the previous high water date 
            }
            if (NbaUtils.compare(getStartDate(), getToday()) > 0) { //Today has already been processed
                getCarrierCodesList().remove(getCurrentCarrierCode()); //Done with current Carrier
                return true;
            }
            setEndDate(NbaUtils.addDaysToDate(getStartDate(), getMibFollowUpDays() - 1));
            if (NbaUtils.compare(getEndDate(), getToday()) > 0) {
                setEndDate(getToday());
            }
            String tx404Request = getRequestMessage();
            retrieveMIBFollowups(tx404Request);
            return getResponseMessages().size() != 0; //Return true if no responses were processed 
        }
        return false; //All Carrier codes have been processed
    }

    /**
     * Process the follow response from MIB.
     * Set the new high water date and/or record number.
     * For <DistinguishedObject> in the response, isolate the <Holding> <Party> etc objects associated with it and 
     * create a  <TXLife> from the associated objects.  Create a  a NBA_MIB_PLANF table  row containing the  <TXLife>
     * and add it to the list of rows to be committed.
     * @param tx404Response
     * @throws NbaBaseException
     */
    protected void processWebServiceResponse(NbaTXLife tx404Response) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("TxLife 404 Response Message:\n " + tx404Response.toXmlString());
        }
        TXLifeResponse tXLifeResponse;
        try {
            tXLifeResponse = tx404Response.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
            if (tXLifeResponse.hasNextRecord() && tXLifeResponse.getNextRecord() > FIRST_RECORD) {
                getCurrentNbaMibPlanFControl().setNextRecord(Integer.valueOf(tXLifeResponse.getNextRecord()).longValue());
                getCurrentNbaMibPlanFControl().setHighWaterDate(getEndDate());
            } else {
                getCurrentNbaMibPlanFControl().setNextRecord(FIRST_RECORD);
                getCurrentNbaMibPlanFControl().setHighWaterDate(getEndDate());
            }
            TXLifeResponseExtension tXLifeResponseExtension = NbaUtils.getFirstTXLifeResponseExtension(tXLifeResponse);
            int responseCount = tXLifeResponseExtension.getDistinguishedObjectCount();
            if (responseCount > 0) {
                OLifE oLifE = tXLifeResponse.getOLifE();
                int relationCount = oLifE.getRelationCount();
                int formInstanceCount = oLifE.getFormInstanceCount();
                for (int i = 0; i < responseCount; i++) {
                    List relationsList = new ArrayList();
                    List formInstanceList = new ArrayList();
                    List partyList = new ArrayList();
                    DistinguishedObject distinguishedObject = tXLifeResponseExtension.getDistinguishedObjectAt(i);
                    String holdingId = distinguishedObject.getDistinguishedObjectID();
                    Holding currentHolding = tx404Response.getHolding(holdingId);
                    String sourcePartyId = currentHolding.getPolicy().getRequirementInfoAt(0).getAppliesToPartyID();
                    Party sourceParty = tx404Response.getParty(sourcePartyId).getParty();
                    partyList.add(sourceParty);
                    for (int j = 0; j < relationCount; j++) {
                        Relation replyRelation = oLifE.getRelationAt(j);
                        if (sourcePartyId.equals(replyRelation.getOriginatingObjectID())) {
                            relationsList.add(replyRelation);
                            String replyParty = replyRelation.getRelatedObjectID();
                            partyList.add(tx404Response.getParty(replyParty).getParty());
                            for (int k = 0; k < relationCount; k++) {
                                Relation reportRelation = oLifE.getRelationAt(k);
                                if (reportRelation.getRelationRoleCode() == NbaOliConstants.OLI_REL_FORMFOR
                                        && reportRelation.getRelatedObjectID().equals(replyRelation.getRelatedObjectID())) {
                                    relationsList.add(reportRelation);
                                    for (int l = 0; l < formInstanceCount; l++) {
                                        FormInstance formInstance = oLifE.getFormInstanceAt(l);
                                        if (formInstance.getId().equals(reportRelation.getOriginatingObjectID())) {
                                            formInstanceList.add(formInstance);
                                            if (formInstance.hasProviderPartyID()) {
                                                String providerPartyId = formInstance.getProviderPartyID();
                                                partyList.add(tx404Response.getParty(providerPartyId).getParty());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    NbaTXLife newNbaTXLife = createIndividualResponse(tXLifeResponse, currentHolding, relationsList, formInstanceList, partyList);
                    createMibPlanFRow(currentHolding, sourceParty, newNbaTXLife);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE);
        }
    }

    /**
     * Retrieve configuration values to be used in processing.  
     * If MIB follow ups are not supported, throw a fatal exception to cause the poller to error stop.
     * If the configuration value for the maximum number of follow up days is missing or greater than
     * the maximum supported by MIB, set the value to the maximum supported by MIB (5).
     * If the configuration value for the maximum number of follow up records is missing or greater than
     * the maximum supported by MIB, set the value to the maximum supported by MIB (500).
     * @throws NbaBaseException
     */
    protected void retrieveConfigurationValues() throws NbaBaseException {
        NbaConfiguration config = NbaConfiguration.getInstance();
        setMibFollowUpInd(config.getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_FOLLOWUP_INDICATOR));
        if (!TRUE_STRING.equals(getMibFollowUpInd())) {
            throw new NbaBaseException("Support for MIB Follow Ups is not configured.", NbaExceptionType.FATAL);
        }
        String value = config.getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_FOLLOW_UP_DAYS);
        Integer days = null;
        if (value != null && value.length() > 0) {
            days = Integer.parseInt(value);
        }
        if (days == null || days < 1 || days > 5) {
            days = 5; //Default is maximum allowed
        }
        setMibFollowUpDays(days.intValue());
        value = config.getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_FOLLOW_UP_MAX_RECORDS);
        Integer maxRecords = null;
        if (value != null && value.length() > 0) {
            maxRecords = Integer.parseInt(value);
        }
        if (maxRecords == null || maxRecords < 1 || maxRecords > 500) {
            maxRecords = 500; //Default is maximum allowed
        }
        setMibFollowUpMaxRecords(maxRecords.intValue());
        Provider provider = NbaConfiguration.getInstance().getProvider(MIB);
        if (!provider.hasOrganizationKeys()) {
            throw new NbaBaseException(NbaBaseException.CONFIGURATION_MISSING);
        }
        OrganizationKeys organizationKeys = provider.getOrganizationKeys();
        int elementCount = organizationKeys.getCompanyCount();
        if (!(elementCount > 0)) {
            throw new NbaBaseException(NbaBaseException.CONFIGURATION_MISSING);
        }
        setCarrierCodesList(new ArrayList(elementCount));
        for (int i = 0; i < elementCount; i++) {
            Company company = organizationKeys.getCompanyAt(i);
            if (!getCarrierCodesList().contains(company.getOrgCode())) {
                getCarrierCodesList().add(company.getOrgCode());
            }
        }
        setTestIndicator(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.MIB_TEST_INDICATOR));
    }

    /**
     * Retrieve the  control records from the NBA_MIB_PLANF_CONTROL table in the NBAAUXILIARY  schema. 
     *  The NBA_MIB_PLANF_CONTROL table contains rows for each orgCode defined the  <Provider><OrganizationKeys><Company> entries for  
    *  the <Provider name="MIB" > entries in NbaConfiguration.xml.  The entries are automatically created the first time a new orgCode is found
    *  in the NbaConfiguration.xml entries.
     * @throws NbaBaseException
     */
    protected void retrieveControlRecords() throws NbaBaseException {
        NbaMibPlanFDatabaseRequestVO nbaMibPlanFDatabaseRequestVO = new NbaMibPlanFDatabaseRequestVO();
        nbaMibPlanFDatabaseRequestVO.setOperation(NbaMibPlanFDatabaseRequestVO.RETRIEVE_CONTROL);
        AccelResult accelResult = (AccelResult) ServiceHandler.invoke(MIB_PLAN_F_DATABASE_ACCESS_BP, ServiceContext.currentContext(),
                nbaMibPlanFDatabaseRequestVO);
        NewBusinessAccelBP.processResult(accelResult);
        nbaMibPlanFDatabaseRequestVO = (NbaMibPlanFDatabaseRequestVO) accelResult.getFirst();
        setControlRecords(nbaMibPlanFDatabaseRequestVO.getNbaMibPlanFControlList());
        createControlRecords();
    }

    /**
     * Retrieve the follow responses from MIB and process the results. 
     * @param tx404Request
     * @throws Exception
     */
    protected void retrieveMIBFollowups(String tx404Request) throws Exception {
        NbaTXLife tx404Response = callWebService(tx404Request);
        processWebServiceResponse(tx404Response);
    }

    protected void setCarrierCodesList(List value) {
        this.carrierCodesList = value;
    }

    protected void setControlRecords(List value) {
        this.controlRecords = value;
    }

    protected void setCurrentCarrierCode(String value) {
        this.currentCarrierCode = value;
    }

    protected void setCurrentNbaMibPlanFControl(NbaMibPlanFControl value) {
        this.currentNbaMibPlanFControl = value;
    }

    protected void setEndDate(Date value) {
        this.endDate = value;
    }

    protected void setMibFollowUpDays(int days) {
        this.mibFollowUpDays = days;
    }

    protected void setMibFollowUpInd(String value) {
        this.mibFollowUpInd = value;
    }

    protected void setMibFollowUpMaxRecords(int value) {
        this.mibFollowUpMaxRecords = value;
    }

    protected void setResponseMessages(List value) {
        this.responseMessages = value;
    }

    protected void setStartDate(Date value) {
        this.startDate = value;
    }

    protected void setStartRecord(Long value) {
        this.startRecord = value;
    }

    protected void setTestIndicator(String value) {
        this.testIndicator = value;
    }

    protected void setToday(Date value) {
        this.today = value;
    }
}
