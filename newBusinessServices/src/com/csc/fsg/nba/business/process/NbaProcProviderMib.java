 package com.csc.fsg.nba.business.process;

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
import java.util.Map;

import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaExceptionType;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.provideradapter.NbaMibAdapter;
import com.csc.fsg.nba.provideradapter.NbaProviderAdapter;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTransaction;
import com.csc.fsg.nba.vo.txlife.TransResult;

/**
 * <code>NbaProcProviderEmsi</code> handles communications between nbAccelerator
 * and MIB.  It extends the NbaProcProviderCommunications class, which drives the process,
 * and supplies MIB specific functionality.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>ACN009</td><td>Version 4</td><td>MIB 401/402 Migration</td></tr>
 * <tr><td>NBA147</td><td>Version 7</td><td>MIB Web Service Response</td></tr>
 * <tr><td>NBA208-32</td><td>Version 7</td><td>Workflow VO Convergence</td></tr>
 * <tr><td>ALS3229</td><td>Version 7</td><td>QC # 1852 - MIB Poller stops when application received via fax</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaProcProviderMib extends NbaProcProviderCommunications {
public static final String WORKFLOW_ACTION_LOCK = "L"; //NBA147
/**
 * NbaProcProviderEmsi default constructor.
 */
public NbaProcProviderMib() {
	super();
}
/**
 * This method performs provider specific processing as required for MIB.
 * <p>Since MIB communcations are via MIB-LINK/Plus, this method creates a unique filename for 
 * file to be written to the MIB-LINK/Plus folder and retrieves the provider-ready transaction text
 * from the source.  Both of these are added to a HashMap and then written to an ArrayList.
 * <p>Currently, MIB does not require authorizations to order requirements.
 * However, there exists the ability to add the authorization as an additional item in the
 * ArrayList.
 * @param aSource the <code>NbaSource</code> containing the provider-ready transaction
 *               from the work item
 * @param anAuthorization the <code>NbaSource</code> containing the authorization for the 
 *               requirement
 * @return an ArrayList containing HashMap(s) with filename and data
 * @throws NbaBaseException
 */
public Object doProviderSpecificProcessing(Object txLifeString, NbaSource anAuthorization) throws NbaBaseException {//ACN009 changed signature
	//ACN009 code deleted
//	ACN009 begin
	List list = new ArrayList();
	list.add(txLifeString);
    NbaMibAdapter mibAdapter = new NbaMibAdapter();
	Map map = mibAdapter.convertXmlToProviderFormat(list);
	return ((NbaTXLife)map.get(NbaProviderAdapter.TRANSACTION)).toXmlString();
	//ACN009 end
}
/**
 * Answers the result of evaluating the response from the MIB.
 * @param response the response from MIB
 * @return <code>true</code> if the result is processed successfully; otherwise return <code>false</code>
 * @throws NbaBaseException
 */
public boolean evaluateResponse(String response) throws NbaBaseException {
    //begin NBA147
    boolean success = false;
    if (response != null && response.trim().length() > 0) {
        NbaTXLife life;
        try {
            life = new NbaTXLife(response);
        } catch (Exception e) {
            throw new NbaBaseException(NbaBaseException.INVALID_RESPONSE, e);
        }
        if (!life.isTransactionError()) {
            if (life.getTransType() == NbaOliConstants.TC_TYPE_MIBINQUIRY) {
                processInquiryResponse(response);
            }
            success = true;
        } else {
            handleWebServiceFailure(life);
        }
    }
    return success;
    //end NBA147
}
/**
 * For MIB processing,  this will sets the URL (path) from the NbaConfiguration file.
 * @throws NbaBaseException
 */
public void initializeTarget() throws NbaBaseException {
    setTarget(getProvider().getUrl());
}

/**
 * Creates transaction workitem with the worktype and initial status received from VP/MS model. 
 * Refenrece response string with the newly created workitem.
 * @param response the MIB inquiry response.
 * @throws NbaBaseException
 */
//NBA147 New Method
protected void processInquiryResponse(String response) throws NbaBaseException {
    NbaProcessWorkItemProvider provider = new NbaProcessWorkItemProvider(getUser(), getWork());
    NbaDst transactionDst = createNbaDstWithTransaction(provider.getWorkType(), provider.getInitialStatus());
    NbaTransaction transaction = transactionDst.getNbaTransaction();
    
    transaction.increasePriority(provider.getWIAction(), provider.getWIPriority());
    transaction.addNbaSource(new NbaSource(getWorkLobs().getBusinessArea(), NbaConstants.A_ST_PROVIDER_SUPPLEMENT, response));
    transactionDst = update(transactionDst);
    unlockWork(transactionDst);
}
/**
 * Creates new NbaDst with a transaction work item.
 * @param workType the worktype
 * @param status the initial status
 * @return the NbaDst object with a transaction work item
 */
//NBA147 New Method
public NbaDst createNbaDstWithTransaction(String workType, String status) {
	//NBA208-32
    WorkItem awdTransaction = new WorkItem();
    //set Business Area, Work type and Status
    awdTransaction.setLobData(getWorkLobs().getLobs());
    awdTransaction.setBusinessArea(getWorkLobs().getBusinessArea());
    awdTransaction.setWorkType(workType);
    awdTransaction.setStatus(status);
    //NBA208-32
    awdTransaction.setLock("Y");
    awdTransaction.setAction(WORKFLOW_ACTION_LOCK);
    awdTransaction.setCreate("Y");
    NbaDst dst = new NbaDst();
    //NBA208-32
    dst.setUserID(user.getUserID());
    try{
    	dst.addTransaction(awdTransaction);
    }catch(Exception ex){
    }
    return dst;
}

/**
 * Handle web service failure. Throws a fatal exception if a resultInfoCode of 300 is received (Webservice not available). 
 * Add work flow messages for all other errors
 * @param nbaTXLifeResponse webservice failure response
 * @exception NbaBaseException if a resultInfoCode of 300 is received
 */
// NBA147 New Method
protected void handleWebServiceFailure(NbaTXLife nbaTXLifeResponse) throws NbaBaseException {
		TransResult transResult = nbaTXLifeResponse.getTransResult();
		int resultInfoCount = transResult.getResultInfoCount();
		// APSL4165 code deleted
		StringBuffer errors = new StringBuffer(); // APSL4165
		for (int i = 0; i < resultInfoCount; i++) {
			if (NbaOliConstants.TC_RESINFO_SYSTEMNOTAVAIL == transResult.getResultInfoAt(i).getResultInfoCode()) {
				// if failure (5) result code with info code of 300 is received, then throw a fatal exception to stop poller
				throw new NbaBaseException(NbaBaseException.WEBSERVICE_NOT_AVAILABLE, NbaExceptionType.FATAL);
			}
			// APSL4165 code deleted
			errors.append(transResult.getResultInfoAt(i).getResultInfoDesc());// APSL4165
		}
		// APSL4165 code deleted
		throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH_WS, errors.toString());// APSL4165

	}
//ALS3229 New method overrides the base class method
protected Object addAuthorizations(Object data) throws NbaBaseException  {
		NbaTXLife reqTxLife = null;	
        try {
            reqTxLife = new NbaTXLife((String)data);
            setNbaOLifEId(new NbaOLifEId(reqTxLife));
        } catch (Exception whoops) {
            throw new NbaBaseException("Unable to un-marshall XML", whoops, NbaExceptionType.FATAL);
        }
        return reqTxLife.toXmlString();
	}
}
