package com.csc.fsg.nba.tableaccess;

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
 */
import java.util.ArrayList;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.newBusiness.NewBusinessHibernateService;
import com.csc.fs.accel.util.ServiceHelper;
import com.csc.fs.accel.valueobject.AccelValueDataObject;
import com.csc.fs.om.DataObjectBase;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.nbaschema.NbpAgents;
import com.csc.fsg.nba.vo.nbaschema.NbpInsureds;
import com.csc.fsg.nba.vo.nbaschema.NbpParties;
import com.csc.fsg.nba.vo.nbaschema.PendingInfo;


/**
 * NbaNbpTable provides access to the NBP database to update nbp_pendinginfo, nbp_agents, 
 * nbp_insureds and nbp_parties tables.The NbaPendingInfoSessionfacadeBean instantiates this 
 * class
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */

public class NbaNbpTable extends NewBusinessHibernateService {
    
    private static NbaLogger logger;	
    
    /**
     * Default constructor.
     *
     */
    public NbaNbpTable() throws NbaBaseException {
    }
    
    /**
     * NbaNbpTable provides access to the NBP database to update nbp_pendinginfo, nbp_agents, 
     * nbp_insureds and nbp_parties tables. It invokes the Accel Services to update the 
     * tables.
     */
    public void update(List doList) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Preparing to execute update on NBP schema");
        }
        Result result = null;
        int polseq = -1;
        List dataObjects = new ArrayList();
        PendingInfo pendingInfo = (PendingInfo) ServiceHelper.getObjectFromList(doList, PendingInfo.class);
        dataObjects.add(pendingInfo);

        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Preparing to RetrievePendiingInfo from database");
        }
        result = invokeService(NbaConstants.SRVC_RETRIEVE_PENDING_INFO, dataObjects);
        dataObjects.clear();
        String outcome = processErrors(result);
        if (!"".equals(outcome)) {
            getLogger().logError(outcome + " during RetrievePendingInfo");
            throw new NbaBaseException("NbaNbpTable update failed");  
        } else if (result.getFirst() != null) {
            pendingInfo = (PendingInfo) result.getFirst();
            if (pendingInfo.getPolSeq() != -1) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().logDebug("Deleting the records from Database for POlicy Sequence Number: " + pendingInfo.getPolSeq());
                }
                NbpParties nbpParties = new NbpParties();
                nbpParties.setPolSeq(pendingInfo.getPolSeq());
                nbpParties.setId(DataObjectBase.LONG_NULL);
                nbpParties.markDeleted();
                dataObjects.add(nbpParties);

                NbpAgents nbpAgents = new NbpAgents();
                nbpAgents.setPolSeq(pendingInfo.getPolSeq());
                nbpAgents.markDeleted();
                dataObjects.add(nbpAgents);

                NbpInsureds nbpInsureds = new NbpInsureds();
                nbpInsureds.setPolSeq(pendingInfo.getPolSeq());
                nbpInsureds.setId(DataObjectBase.LONG_NULL);
                nbpInsureds.markDeleted();
                dataObjects.add(nbpInsureds);

                pendingInfo.markDeleted();
                dataObjects.add(pendingInfo);
            }
        } 

        //Get the next sequence number to insert

        NbaTableAccessor nts = new NbaTableAccessor();
        String seqNumber = null;
        seqNumber = nts.getSeedNumber("nbProducer", "1");

        if (seqNumber == null) {
            throw new NbaDataAccessException("Seed Number not found");
        }
        if (seqNumber.equals("-1")) {
            // maximum value exceeded... throw an exception
            throw new NbaDataAccessException("Maximum seed number value for nbProducer has been exceeded.");
        }
        polseq = Integer.parseInt(seqNumber);
        updatePolicySequence(polseq, doList);
        dataObjects.addAll(doList);
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Preparing to Insert or Update Records for NBP schema in the database");
        }
        result = invokeService(NbaConstants.SRVC_UPDATE_NBP, dataObjects);
        outcome = processErrors(result);
        if (!"".equals(outcome)) {
            getLogger().logError(outcome + " during RetrievePendingInfo");
            throw new NbaBaseException("NbaNbpTable update failed");  
        }
    }
    
    /**
     * Update the Policy Sequence Number with the newly fetched Sequence Number
     * @param polSeq the Sequence Number
     * @param doList
     */
    protected void updatePolicySequence(int polSeq, List doList) {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Updating the existing objects with the new Policy Sequence Number: " + polSeq);
        }

        AccelValueDataObject obj = null;
        PendingInfo pendingInfo = null;
        NbpParties nbpParties = null;
        for (int i = 0; i < doList.size(); i++) {
            obj = (AccelValueDataObject) doList.get(i);
            if (obj instanceof PendingInfo) {
                pendingInfo = (PendingInfo) obj;
                pendingInfo.setPolSeq(polSeq);
            } else {
                nbpParties = (NbpParties) obj;
                nbpParties.setPolSeq(polSeq);
            }
        }
    }
    
	/**
	 * Updates the nbp pendingInfo database with the work flow status.
	 * @param polNumber policy number
	 * @param backendID system ID
	 * @param companyCode company Code
	 * @param workflowStatus queue Name
	 * @param workflowStausDesc translated queue name
	 * @param lastUnderwritingDate last underwriting date
	 * @exception NbaBaseException
	 */

	public void updateWorkflowStatus(String polNumber, String backendId, String companyCode, String workflowQueue, String workflowQueueDesc,java.sql.Date lastUnderwritingDate) throws NbaBaseException {
        if (getLogger().isDebugEnabled()) {
            getLogger().logDebug("Preparing to UpdateWorkflow Status for Pending Info");
        }

        Result result = null;
        
        PendingInfo pendingInfo = new PendingInfo();
        pendingInfo.setCarrierCode(companyCode);
        pendingInfo.setPolicyNumber(polNumber);
        pendingInfo.setSystemId(backendId);

        List doList = new ArrayList();
        doList.add(pendingInfo);
        
        result = invokeService(NbaConstants.SRVC_RETRIEVE_PENDING_INFO, doList);
        
        doList.clear();
        String outcome = processErrors(result);
        if (!"".equals(outcome)) {
            getLogger().logError(outcome + " during RetrievePendingInfo");
            throw new NbaBaseException("NbaNbpTable update failed");  
        } else if (result.getFirst() != null) {
            pendingInfo = (PendingInfo) result.getFirst();
            pendingInfo.setWorkflowStatusCode(workflowQueue);
            pendingInfo.setWorkflowStatusDescription(workflowQueueDesc);
            pendingInfo.setLastUnderwritingActivityDate(lastUnderwritingDate);
            doList.add(pendingInfo);
            result = invokeService(NbaConstants.SRVC_UPDATE_NBP, doList);
            outcome = processErrors(result);
            if (!"".equals(outcome)) {
                getLogger().logError(outcome + " during RetrievePendingInfo");
                throw new NbaBaseException("NbaNbpTable update failed");  
            }
        }
	}
    
}




