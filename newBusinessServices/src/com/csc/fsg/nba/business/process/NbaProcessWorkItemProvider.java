package com.csc.fsg.nba.business.process;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.access.contract.NbaServerUtility;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkFormatter;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaStringTokenizer;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * The NbaProcessWorkItemProvider services the <code>NbaAutomatedProcess<code> by calling
 * VPMS to retrieve the work type and status for a work item created in the process.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA004</td><td>Version 2</td><td>Automated Process Model Support for Work Items</td></tr>
 * <tr><td>NBA021</td><td>Version 2</td><td>Data Resolver</td></tr> 
 * <tr><td>NBA020</td><td>Version 2</td><td>AWD Priority</td></tr>
 * <tr><td>NBA009</td><td>Version 2</td><td>Cashiering</td></tr>
 * <tr><td>NBA058</td><td>Version 3</td><td>Upgrade to J-VPMS version 1.5.0</td></tr>
 * <tr><td>NBA035</td><td>Version 3</td><td>App submit to Pending DB</td></tr>
 * <tr><td>NBA050</td><td>Version 3</td><td>Pending Database</td></tr>
 * <tr><td>NBA073</td><td>Version 3</td><td>Agent Validation/Retrieve Contract Info</td></tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA077</td><td>Version 4</td><td>Reissues and Complex Change etc.</td></tr>
 * <tr><td>ACN014</td><td>Version 3</td><td>121/1122 Migration</td></tr>
 * <tr><td>SPR2639</td><td>Version 5</td><td>Automated process status should be based business function</td></tr>
 * <tr><td>NBA130</td><td>Version 6</td><td>Requirement/Reinsurance Changes</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3362</td><td>Version 7</td><td>Exceptions in Automated Processes and Logon Service Due to VP/MS Memory Leak</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * <tr><td>APSL3881</td><td>Discretionary</td><td>Follow The Sun</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 2
 */
public class NbaProcessWorkItemProvider {
	/**
	* Provides additional initialization support by setting the
	* case and user objects to the passed in parameters and by
	* creating a reference to the NbaNetServerAccessor EJB.
	* @param newUser the AWD User for the process
	* @param newWork the NbaDst value object to be processed
	* @return <code>true</code> indicates the statuses were successfully
	*         retrieved while <code>false</code> indicates failure.
	* @throws NbaBaseException
	*/
	// NBA050 NEW METHOD
	public NbaTXLife retrieveContract(NbaUserVO newUser, NbaDst newWork) throws NbaBaseException {
		try {
			return NbaContractAccess.doContractInquiry(createRequestObject(newWork, newUser));  //NBA213
		} catch (NbaBaseException nbe) {
			if (nbe instanceof NbaDataAccessException) {
				return null;
			} else {
				throw nbe;
			}
		} catch (Exception re) {
			throw new NbaBaseException(re);
		}
	}
	/** The string representing the work type */
	public java.lang.String workType;
	/** The string representing the initial status */
	public java.lang.String initialStatus;
	/** The string representing the work item priority */
	public java.lang.String wiPriority;//NBA020
	/** The string representing the work item action */
	public java.lang.String wiAction;//NBA020
/**
 * NbaProcessWorkItemProvider constructor comment.
 */
public NbaProcessWorkItemProvider() {
	super();
}
/**
 * NbaProcessWorkItemProvider constructor.
 * 
 * @param user com.csc.fsg.nba.vo.NbaUserVO
 * @throws NbaBaseException
 */
public NbaProcessWorkItemProvider(com.csc.fsg.nba.vo.NbaUserVO user) throws NbaBaseException {
	initializeFields(user, (NbaDst)null,"-"); //NBA077
}
/**
 * NbaProcessWorkItemProvider constructor.
 * 
 * @param user com.csc.fsg.nba.vo.NbaUserVO
 * @param sourceType java.lang.String
 * @throws NbaBaseException
 */
 // NBA021 NEW METHOD
public NbaProcessWorkItemProvider(com.csc.fsg.nba.vo.NbaUserVO user, NbaDst aNbaDst) throws NbaBaseException {
	initializeFields(user, aNbaDst);
}
/**
 * NbaProcessWorkItemProvider constructor.
 * 
 * @param user com.csc.fsg.nba.vo.NbaUserVO
 * @param sourceType java.lang.String
 * @param aNbaDst user com.csc.fsg.nba.vo.NbaDst
 * @throws NbaBaseException
 */
//NBA020 change method signature
public NbaProcessWorkItemProvider(com.csc.fsg.nba.vo.NbaUserVO user, NbaDst aNbaDst, String sourceType) throws NbaBaseException {
	initializeFields(user, aNbaDst, sourceType);
}

/**
 * NbaProcessWorkItemProvider constructor.
 * 
 * @param user com.csc.fsg.nba.vo.NbaUserVO
 * @param aNbaDst user com.csc.fsg.nba.vo.NbaDst
 * @param deOink Map
 * @throws NbaBaseException
 */
//NBA073 New Method
public NbaProcessWorkItemProvider(com.csc.fsg.nba.vo.NbaUserVO user, NbaDst aNbaDst, Map deOink) throws NbaBaseException {
	initializeFields(user, aNbaDst, deOink, null); //SPR2639
}

/**
 * NbaProcessWorkItemProvider constructor.
 * @param user the user value object
 * @param aNbaDst the workitem
 * @param nbaTXLife the holding inquiry object.
 * @param deOink the deOink Map
 * @throws NbaBaseException
 */
//SPR2639 New Method
public NbaProcessWorkItemProvider(NbaUserVO user, NbaDst aNbaDst, NbaTXLife nbaTXLife, Map deOink) throws NbaBaseException {
	initializeFields(user, aNbaDst, deOink, nbaTXLife); //SPR2639
}


/**
 * 
 * NbaProcessWorkItemProvider constructor. * 
 * @param user the user value object
 * @param aNbaDst work item representing contract information
 * @param sourceType source type
 * @param nbaMoney whether its nbA Money or not
 * @throws NbaBaseException
 */
//NBA009 new method
public NbaProcessWorkItemProvider(NbaUserVO user, NbaDst aNbaDst, String sourceType, String nbaMoney) throws NbaBaseException {
	initializeFields(user, aNbaDst, sourceType, nbaMoney);
}
/**
 * NbaProcessWorkItemProvider constructor.
 * 
 * @param user com.csc.fsg.nba.vo.NbaUserVO
 * @param sourceType java.lang.String
 * @throws NbaBaseException
 */
 // NBA021 NEW METHOD
public NbaProcessWorkItemProvider(com.csc.fsg.nba.vo.NbaUserVO user, NbaLob aNbaLob) throws NbaBaseException {
	initializeFields(user, aNbaLob, "-"); //NBA077
}
/**
 * NbaProcessWorkItemProvider constructor.
 * 
 * @param user com.csc.fsg.nba.vo.NbaUserVO
 * @param aNbaLob the NbaLob object
 * @param sourceType java.lang.String
 * @throws NbaBaseException
 */
 // NBA077 NEW METHOD
public NbaProcessWorkItemProvider(NbaUserVO user, NbaLob aNbaLob, String sourceType) throws NbaBaseException {
	initializeFields(user, aNbaLob, sourceType);
}
/**
 * Returns the initial status of a work item.
 * 
 * @return java.lang.String
 */
public String getInitialStatus() {
	return initialStatus;
}
/**
 * Answers the priorityAction
 * @return the work item priority action
 */
//NBA020 New Method
public java.lang.String getWIAction() {
	return wiAction;
}
/**
 * Answers the workPriority
 * @return the work item priority
 */
//NBA020 New Method
public java.lang.String getWIPriority() {
	return wiPriority;
}
/**
 * Returns the work type of the work item.
 * 
 * @return java.lang.String
 */
public String getWorkType() {
	return workType;
}
/**
 * This method sets up and performs the call to VP/MS to determine the
 * aNbaDst type and status for a aNbaDst item based on the user ID and source
 * type. (The user ID is used to get business function for an automated process.)
 * Using an NbaVpmsAdaptor object, VP/MS is called passing in an NbaVpmsVO
 * object.  The result is then used to populate the member variables in the
 * updateFields method.
 * 
 * @param user the process ID
 * @param sourceType java.lang.String
 * @throws NbaBaseException
 */
// NBA021 NEW METHOD
public void initializeFields(NbaUserVO user, NbaDst aNbaDst) throws NbaBaseException {
	NbaOinkDataAccess oink = new NbaOinkDataAccess(aNbaDst.getNbaLob());
	//NBA050 BEGIN
	NbaTXLife aNbaTXLife = retrieveContract(user, aNbaDst);
	//NBA050 END
	//NBA050 CODE DELETED
	//NBA035 code deleted
	//NBA050 CODE DELETED

	if (aNbaTXLife != null) {
		oink.setContractSource(aNbaTXLife);
		oink.setLobSource(aNbaDst.getNbaLob()); //NBA050
	}
	Map deOink = new HashMap();
	deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user)); //SPR2639
	deOink.put(NbaVpmsConstants.A_DATASTORE_MODE, NbaServerUtility.getDataStore(aNbaDst.getNbaLob(), null)); //NBA130
	NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
	vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKTYPE_AND_STATUS);
	vpmsProxy.setSkipAttributesMap(deOink);
	try {
		VpmsComputeResult result = vpmsProxy.getResults();
		if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
			throw new Throwable(result.getMessage());
		}
		updateFields(result);
		//SPR3362 code deleted
	} catch (java.rmi.RemoteException re) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, re);
	} catch (Throwable t) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, t);
	//begin SPR3362
	} finally {
        try {
            if (vpmsProxy != null) {
                vpmsProxy.remove();
            }
        } catch (Throwable th) {
            LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
        }
    }
	//end SPR3362
}
/**
 * This method sets up and performs the call to VP/MS to determine the
 * work type and status for a work item based on the user ID and source
 * type. (The user ID is used to get business function for an automated process.)
 * @param user the process ID
 * @param aNbaDst A work item representing contract information
 * @param source A Correspondence Source
 * @throws NbaBaseException
 */
// ACN012 Change signature
public void initializeFields(NbaUserVO user, NbaDst aNbaDst, com.csc.fsg.nba.vo.nbaschema.Correspondence source) throws NbaBaseException {
    // NBA021 Code deleted
    // NBA021 BEGIN
    NbaOinkDataAccess oink = new NbaOinkDataAccess();
    oink.setLobSource(aNbaDst.getNbaLob());

    Map deOink = new HashMap();
	deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user)); //SPR2639
    deOink.put("A_LETTERTYPE", source.getLetterType());
    // NBA021 END
	deOink.put(NbaVpmsConstants.A_DATASTORE_MODE, NbaServerUtility.getDataStore(aNbaDst.getNbaLob(), null)); //NBA130
    NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsAdaptor.AUTO_PROCESS_STATUS); // NBA021
    vpmsProxy.setSkipAttributesMap(deOink); // NBA021
    vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKTYPE_AND_STATUS); // NBA021
    try {
        VpmsComputeResult result = vpmsProxy.getResults();
        // did we get a bad return code from VP/MS?
        if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
            throw new Throwable(result.getMessage());
        }
        updateFields(result);
        //SPR3362 code deleted
    } catch (java.rmi.RemoteException re) {
        throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, re);
    } catch (Throwable t) {
        throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, t);
        //  begin SPR3362
    } finally {
        try {
            if (vpmsProxy != null) {
                vpmsProxy.remove();
            }
        } catch (Throwable th) {
            LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
        }
    }
    //end SPR3362
}
/**
 * This method sets up and performs the call to VP/MS to determine the
 * work type and status for a work item based on the user ID and source
 * type. (The user ID is used to get business function for an automated process.)
 * Using an NbaVpmsAdaptor object, VP/MS is called passing in an NbaVpmsVO
 * object.  The result is then used to populate the member variables in the
 * updateFields method.
 * 
 * @param user the process ID
 * @param sourceType java.lang.String
 * @param aNbaDst user com.csc.fsg.nba.vo.NbaDst
 * @throws NbaBaseException
 */
//NBA020 change method signature
public void initializeFields(NbaUserVO user, NbaDst aNbaDst, String sourceType) throws NbaBaseException {
	// NBA021 Code deleted
	// NBA021 BEGIN
	// NBA050 BEGIN
	NbaTXLife aNbaTXLife = retrieveContract(user, aNbaDst);
	// NBA050 CODE DELETED
	NbaOinkDataAccess oink = new NbaOinkDataAccess();
	oink.setContractSource(aNbaTXLife);
	oink.setLobSource(aNbaDst.getNbaLob());
	// NBA050 END
	Map deOink = new HashMap();
	deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user)); //SPR2639
	deOink.put("A_SourceTypeLOB", sourceType);
	// NBA021 END
	deOink.put(NbaVpmsConstants.A_DATASTORE_MODE, NbaServerUtility.getDataStore(aNbaDst.getNbaLob(), null)); //NBA130
	NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsAdaptor.AUTO_PROCESS_STATUS); // NBA021
	vpmsProxy.setSkipAttributesMap(deOink); // NBA021
	vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKTYPE_AND_STATUS); // NBA021
	try {
		VpmsComputeResult result = vpmsProxy.getResults();
		// did we get a bad return code from VP/MS?
		if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
			throw new Throwable(result.getMessage());
		}
		updateFields(result);
		//SPR3362 code deleted
	} catch (java.rmi.RemoteException re) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, re);
	} catch (Throwable t) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, t);
		//begin SPR3362
	} finally {
        try {
            if (vpmsProxy != null) {
                vpmsProxy.remove();
            }
        } catch (Throwable th) {
            LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
        }
    }
	//end SPR3362
}
/**
 * This method sets up and performs the call to VP/MS to determine the
 * work type and status for a work item based on the user ID and source
 * type. (The user ID is used to get business function for an automated process.)
 * Using an NbaVpmsAdaptor object, VP/MS is called passing in an NbaVpmsVO
 * object.  The result is then used to populate the member variables in the
 * updateFields method.
 * 
 * @param user the user value object
 * @param aNbaDst work item representing contract information
 * @param sourceType source type
 * @param nbaMoney whether its nbA Money or not
 * @throws NbaBaseException
 */
//NBA009 new method
public void initializeFields(NbaUserVO user, NbaDst aNbaDst, String sourceType, String nbaMoney) throws NbaBaseException {
	NbaOinkDataAccess oink = new NbaOinkDataAccess(aNbaDst.getNbaLob()); //NBA020 initialize with NbaDst
	Map deOink = new HashMap();
	deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user)); //SPR2639
	deOink.put("A_SOURCETYPELOB", sourceType);
	deOink.put("A_NBAMONEY", nbaMoney);
	deOink.put(NbaVpmsConstants.A_DATASTORE_MODE, NbaServerUtility.getDataStore(aNbaDst.getNbaLob(), null)); //NBA130

	NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
	vpmsProxy.setSkipAttributesMap(deOink);
	vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKTYPE_AND_STATUS);
	try {
		VpmsComputeResult result = vpmsProxy.getResults();
		// did we get a bad return code from VP/MS?
		if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
			throw new Throwable(result.getMessage());
		}
		updateFields(result);
		//SPR3362 code deleted
	} catch (java.rmi.RemoteException re) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, re);
	} catch (Throwable t) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, t);
		//begin SPR3362
	} finally {
        try {
            if (vpmsProxy != null) {
                vpmsProxy.remove();
            }
        } catch (Throwable th) {
            LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
        }
    }
	//end SPR3362
}

/**
 * This method sets up and performs the call to VP/MS to determine the
 * work type and status for a work item based on the user ID and source
 * type. (The user ID is used to get business function for an automated process.)
 * Using an NbaVpmsAdaptor object, VP/MS is called passing in an NbaVpmsVO
 * object.  The result is then used to populate the member variables in the
 * updateFields method.
 * 
 * @param user the user value object
 * @param aNbaDst work item representing contract information
 * @param deOink Map the deOink map
 * @param nbaTXLife the holding inquiry
 * @throws NbaBaseException
 */
//NBA073 new method
//SPR2639 Added parameter nbaTXLife
public void initializeFields(NbaUserVO user, NbaDst aNbaDst, Map deOink, NbaTXLife nbaTXLife) throws NbaBaseException {
	NbaOinkDataAccess oink = new NbaOinkDataAccess(aNbaDst.getNbaLob()); //NBA020 initialize with NbaDst
	if (null != nbaTXLife) { //SPR2639
		oink.setContractSource(nbaTXLife); //SPR2639
	} //SPR2639
	oink.getFormatter().setDateSeparator(NbaOinkFormatter.DATE_SEPARATOR_DASH);//ALS5777
	deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user)); //SPR2639
	deOink.put(NbaVpmsConstants.A_DATASTORE_MODE, NbaServerUtility.getDataStore(aNbaDst.getNbaLob(), null)); //NBA130
	
	NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
	vpmsProxy.setSkipAttributesMap(deOink);
	vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKTYPE_AND_STATUS);
	try {
		VpmsComputeResult result = vpmsProxy.getResults();
		// did we get a bad return code from VP/MS?
		if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
			throw new Throwable(result.getMessage());
		}
		updateFields(result);
		//SPR362 code deleted
	} catch (java.rmi.RemoteException re) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, re);
	} catch (Throwable t) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, t);
    //begin SPR3362
    } finally {
        try {
            if (vpmsProxy != null) {
                vpmsProxy.remove();
            }
        } catch (Throwable th) {
            LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
        }
}
//end SPR3362
}

//NBLXA-1696 new method
public NbaProcessWorkItemProvider(com.csc.fsg.nba.vo.NbaUserVO user, NbaDst aNbaDst,NbaOinkRequest oinkRequest,Map deOink) throws NbaBaseException {
	initializeFields(user, aNbaDst,oinkRequest,deOink);
}

//NBLXA- 1696 new method start

public void initializeFields(NbaUserVO user, NbaDst aNbaDst, NbaOinkRequest oinkRequest , Map deOink ) throws NbaBaseException {

		NbaOinkDataAccess oink = new NbaOinkDataAccess(aNbaDst.getNbaLob());
		NbaTXLife aNbaTXLife = retrieveContract(user, aNbaDst);		

		if (aNbaTXLife != null) {
			oink.setContractSource(aNbaTXLife);
			oink.setLobSource(aNbaDst.getNbaLob());
		}
		deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user));
		deOink.put(NbaVpmsConstants.A_DATASTORE_MODE, NbaServerUtility.getDataStore(aNbaDst.getNbaLob(), null));
		NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
		vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKTYPE_AND_STATUS);
		vpmsProxy.setSkipAttributesMap(deOink);
		if(oinkRequest != null){
			vpmsProxy.setANbaOinkRequest(oinkRequest);
		}
		try {
			VpmsComputeResult result = vpmsProxy.getResults();
			if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
				throw new Throwable(result.getMessage());
			}
			updateFields(result);			
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, re);
		} catch (Throwable t) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, t);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}		
	
}




//NBLXA-1696 end


/**
 * This method sets up and performs the call to VP/MS to determine the
 * work type and status for a work item based on the user ID and source
 * type. (The user ID is used to get business function for an automated process.)
 * Using an NbaVpmsAdaptor object, VP/MS is called passing in an NbaVpmsVO
 * object.  The result is then used to populate the member variables in the
 * updateFields method.
 * 
 * @param user the process ID
 * @param aNbaLob the NbaLob object
 * @param sourceType java.lang.String
 * @throws NbaBaseException
 */
//NBA021 NEW METHOD
 // NBA077 added new parameter sourceType
public void initializeFields(NbaUserVO user, NbaLob aNbaLob, String sourceType) throws NbaBaseException {
	NbaOinkDataAccess oink = new NbaOinkDataAccess(aNbaLob);
	Map deOink = new HashMap();
	deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user)); //SPR2639
	deOink.put("A_SourceTypeLOB", sourceType);
	deOink.put(NbaVpmsConstants.A_DATASTORE_MODE, NbaServerUtility.getDataStore(aNbaLob, null)); //NBA130
	NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
	vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKTYPE_AND_STATUS);
	vpmsProxy.setSkipAttributesMap(deOink);
	try {
		VpmsComputeResult result = vpmsProxy.getResults();
		if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
			throw new Throwable(result.getMessage());
		}
		updateFields(result);
		//SPR3362 code deleted
	} catch (java.rmi.RemoteException re) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, re);
	} catch (Throwable t) {
		throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, t);
		//begin SPR3362
	} finally {
        try {
            if (vpmsProxy != null) {
                vpmsProxy.remove();
            }
        } catch (Throwable th) {
            LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
        }
    }
	//end SPR3362
}

/**
 * Sets the initial status of the work item.
 * 
 * @param newInitialStatus
 */
public void setInitialStatus(String newInitialStatus) {
	initialStatus = newInitialStatus;
}
/**
 * Sets the new work type priority action flag.
 * 
 * @param newPriorityAction
 */
//NBA020 New Method
public void setWIAction(String newPriorityAction) {
	wiAction = newPriorityAction;
}
/**
 * Sets the new work type priority.
 * 
 * @param newWorkPriority
 */
//NBA020 New Method
public void setWIPriority(String newWorkPriority) {
	wiPriority = newWorkPriority;
}
/**
 * Sets the new work type for the work item.
 * 
 * @param newWorkType
 */
public void setWorkType(String newWorkType) {
	workType = newWorkType;
}
/**
 * The results from the VPMS model are parsed. The work type and status
 * are retrieved and used to update the field members.
 *
 * @param result the result from the VPMS call
 * @param aDelimiter the delimiter used by the VPMS model to separate status fields
 */
public void updateFields(VpmsComputeResult result) {
	if (result.getReturnCode() == 0) {
		// SPR3290 code deleted
		NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), NbaVpmsAdaptor.VPMS_DELIMITER[0]);
		// NBA021 Code Deleted
		setWorkType(tokens.nextToken());
		setInitialStatus(tokens.nextToken());
		setWIAction(tokens.nextToken()); //NBA020
		setWIPriority(tokens.nextToken()); //NBA020
	}
}
/**
 * The results from the VPMS model are parsed. The work type and status
 * are retrieved and used to update the field members.
 *
 * @param result the result from the VPMS call
 * @param aDelimiter the delimiter used by the VPMS model to separate status fields
 */
public void updateFields(VpmsComputeResult result, String aDelimiter) {
	// SPR3290 code deleted
	NbaStringTokenizer tokens = new NbaStringTokenizer(result.getResult().trim(), aDelimiter);
	String aToken = tokens.nextToken(); // First token is empty - don't need it.
	while (tokens.hasMoreTokens()) {
		aToken = tokens.nextToken();
		StringTokenizer bToken = new StringTokenizer(aToken, ":");
		String statusType = bToken.nextToken();
		String statusValue = bToken.nextToken();
		if (statusType.equals("TYPE"))
			setWorkType(statusValue);
		else if (statusType.equals("STAT"))
			setInitialStatus(statusValue);
	}
}

/**
 * Create a TX Request value object that will be used to retrieve the contract.
 * @param work the workitem object for that holding request is required
 * @param user the user value object
 * @return a value object that is the request
 */
public NbaTXRequestVO createRequestObject(NbaDst work, NbaUserVO user) {
	NbaTXRequestVO nbaTXRequest = new NbaTXRequestVO();
	nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
	nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
	nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
	nbaTXRequest.setNbaLob(work.getNbaLob());
	nbaTXRequest.setNbaUser(user);
	nbaTXRequest.setWorkitemId(work.getID());  
	nbaTXRequest.setCaseInd(work.isCase()); //ACN014
	nbaTXRequest.setAccessIntent(NbaConstants.READ);
	nbaTXRequest.setBusinessProcess(NbaUtils.getBusinessProcessId(user)); //SPR2639
	return nbaTXRequest;
}

	// APSL3381 New method
	public NbaProcessWorkItemProvider(com.csc.fsg.nba.vo.NbaUserVO user, NbaDst aNbaDst, NbaOinkRequest oinkRequest) throws NbaBaseException {
		initializeFields(user, aNbaDst, oinkRequest);
	}
	
	// APSL3381 New method
	public void initializeFields(NbaUserVO user, NbaDst aNbaDst, NbaOinkRequest oinkRequest) throws NbaBaseException {
		NbaOinkDataAccess oink = new NbaOinkDataAccess(aNbaDst.getNbaLob());
		NbaTXLife aNbaTXLife = retrieveContract(user, aNbaDst);		

		if (aNbaTXLife != null) {
			oink.setContractSource(aNbaTXLife);
			oink.setLobSource(aNbaDst.getNbaLob());
		}
		Map deOink = new HashMap();
		deOink.put(NbaVpmsAdaptor.A_PROCESS_ID, NbaUtils.getBusinessProcessId(user));
		deOink.put(NbaVpmsConstants.A_DATASTORE_MODE, NbaServerUtility.getDataStore(aNbaDst.getNbaLob(), null));
		NbaVpmsAdaptor vpmsProxy = new NbaVpmsAdaptor(oink, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
		vpmsProxy.setVpmsEntryPoint(NbaVpmsAdaptor.EP_GET_WORKTYPE_AND_STATUS);
		vpmsProxy.setSkipAttributesMap(deOink);
		if(oinkRequest != null){
			vpmsProxy.setANbaOinkRequest(oinkRequest);
		}
		try {
			VpmsComputeResult result = vpmsProxy.getResults();
			if (result.getReturnCode() != 0 && result.getReturnCode() != 1) {
				throw new Throwable(result.getMessage());
			}
			updateFields(result);			
		} catch (java.rmi.RemoteException re) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, re);
		} catch (Throwable t) {
			throw new NbaBaseException(NbaBaseException.VPMS_AUTOMATED_PROCESS_WORKITEM, t);
		} finally {
			try {
				if (vpmsProxy != null) {
					vpmsProxy.remove();
				}
			} catch (Throwable th) {
				LogHandler.Factory.LogError(this, NbaBaseException.VPMS_REMOVAL_FAILED);
			}
		}		
	}
	
}
