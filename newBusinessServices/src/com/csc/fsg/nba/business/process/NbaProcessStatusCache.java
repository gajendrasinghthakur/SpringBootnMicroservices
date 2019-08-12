package com.csc.fsg.nba.business.process;

/*
 * **************************************************************************<BR>
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
 * *******************************************************************************<BR>
 */

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.core.RequestSequence;
import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fs.logging.LogHandler;
import com.csc.fsg.nba.contract.calculations.NbaContractCalculationsConstants;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.configuration.AutomatedProcess;
import com.csc.fsg.nba.vo.configuration.AutomatedProcesses;
import com.csc.fsg.nba.vpms.NbaVpmsAdaptor;
import com.csc.fsg.nba.vpms.NbaVpmsConstants;

/**
 * This class caches and provides the NbaProcessStatusProvider for 
 * automated processes. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN024</td><td>Version 4</td><td>Automated Process Restructuring</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.process.NbaAutomatedProcess
 * @since New Business Accelerator - Version 4
 */

public class NbaProcessStatusCache implements Serializable {
	private static NbaProcessStatusCache instance = null;
	private static final long serialVersionUID = 8866187874743338677L;
	private static Map statusMap = new HashMap();

	//begin NBA213 //ALS3928 commented Cache logic
//	static {
//		persistAutoProcessStatuses();
//	}
	//end NBA213
	
	/**
	 * Answer the singleton reference to this class.
	 * @return com.csc.fsg.nba.business.process.NbaProcessStatusCache
	 */
	public static NbaProcessStatusCache getInstance() {
		if (instance == null) {
			instance = new NbaProcessStatusCache();
		}
		return instance;
	}
	/**
	 * Answer the <code>NbaProcessStatusProvider</code> for the automatedProcess
	 * or null if not found.
	 * @param automatedProcess a string containing the name of the automated process
	 * @return NbaProcessStatusProvider for the automated process
	 */
	public NbaProcessStatusProvider getStatusProvider(String automatedProcess) {
		if( statusMap.containsKey(automatedProcess)) {
			return (NbaProcessStatusProvider)statusMap.get(automatedProcess);
		}
		return null;
	}
	/**
	 * Add the <code>NbaProcessStatusProvider</code> for the automatedProcess
	 * to the HashMap.
	 * @param automatedProcess a string containing the name of the automated process
	 * @param statusProvider a NbaProcessStatusProvider containing the statuses and
	 * priorities for the automatedProcess
	 * @return NbaProcessStatusProvider for the automated process
	 */
	public static void addStatusProvider(String automatedProcess, NbaProcessStatusProvider statusProvider) {
		statusMap.put(automatedProcess, statusProvider);
	}
	/**
	 * Answer the cache status of the automatedProcess.
	 * @param automatedProcess the name of the automated process
	 * @return boolean true, if cached; otherwise, false
	 */
	public boolean isCached(String automatedProcess) {
		if (statusMap.containsKey(automatedProcess)) {
			return true;
		}
		return false;
	}

	/**
	 * This method updates the <code>NbaAutoProcessStatusPersister</code> to include an
	 * <code>NbaProcessStatusProvider</code> for auto processes that can be persisted.
	 * An auto process can be persisited if the Process ID is the only attribute required
	 * for the status determination. The AutoProcessStatus VPMS model is invoked to make
	 * this determination.
	 * If the process is unable to access the VPMS model for any reason, an error message
	 * is logged.
	 */
	//NBA213 New Method
	protected static void persistAutoProcessStatuses() {
	    NbaVpmsAdaptor vpmsAdaptor = null;
		try {
			vpmsAdaptor = new NbaVpmsAdaptor(NbaVpmsConstants.AUTO_PROCESS_STATUS);
			vpmsAdaptor.setVpmsEntryPoint(NbaVpmsConstants.EP_WORKITEM_STATUSES);
			vpmsAdaptor.setSkipAttributesMap(new HashMap());
			RequestSequence reqSeq = new RequestSequence();
			if (LogHandler.Factory.isLogging(LogHandler.LOG_DEBUG)) {
				reqSeq.addSetAttribute(NbaContractCalculationsConstants.ATR_DEBUG, NbaContractCalculationsConstants.DEBUG_ON);
				reqSeq.addSetAttribute(NbaContractCalculationsConstants.ATR_TRACE, NbaContractCalculationsConstants.TRACE_OFF);
			}
			reqSeq.addSetAttribute(NbaVpmsConstants.A_DELIMITER, NbaVpmsConstants.VPMS_DELIMITER[0]);
			reqSeq.addSetAttribute(NbaVpmsConstants.A_DELIMITER + "[1]", NbaVpmsConstants.VPMS_DELIMITER[1]);
			Map mymap = new HashMap();
			AutomatedProcess autoProc;
			int computeIndex;
			AutomatedProcesses autoProcs = NbaConfiguration.getInstance().getAutomatedProcesses();
			int count = autoProcs.getAutomatedProcessCount();
			for (int i = 0; i < count; i++) {
				autoProc = autoProcs.getAutomatedProcessAt(i);
				reqSeq.addSetAttribute(NbaVpmsConstants.A_PROCESS_ID, autoProc.getBusfunc());
				computeIndex = reqSeq.addCompute(NbaVpmsConstants.EP_WORKITEM_STATUSES);
				mymap.put(autoProc.getUser(), new Integer(computeIndex));
			}
			List list = vpmsAdaptor.getResults(reqSeq);
			for (int i = 0; i < count; i++) {
				autoProc = autoProcs.getAutomatedProcessAt(i);
				computeIndex = ((Integer) mymap.get(autoProc.getUser())).intValue();
				VpmsComputeResult vpmsResult = (VpmsComputeResult) list.get(computeIndex);
				if (vpmsResult.getReturnCode() == 0) {
					addStatusProvider(autoProc.getUser(), new NbaProcessStatusProvider(vpmsResult));
					LogHandler.Factory.LogInfo("NbaProcessStatusCache", "{0}: Statuses persisted - {1}", new Object[] {autoProc.getUser(), vpmsResult.getResult()});
				} else {
					LogHandler.Factory.LogInfo("NbaProcessStatusCache", "{0}: Unable to persist statuses", new Object[] {autoProc.getUser()});
				}
			}
		} catch (Throwable t) {
			LogHandler.Factory.LogError("NbaProcessStatusCache", "Cannot complete nbA Automated Process Status Cache initialization. ", t);
        } finally {
            try {
                if (vpmsAdaptor != null) {
                    vpmsAdaptor.remove();
                }
            } catch (Throwable th) {
                LogHandler.Factory.LogError("NbaProcessStatusCache", NbaBaseException.VPMS_REMOVAL_FAILED);
            }
        }
	}

}
