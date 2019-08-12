package com.csc.fsg.nba.bean.accessors;
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
 * *******************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.SessionBean;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaNetServerException;
import com.csc.fsg.nba.foundation.NbaActionIndicator;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaReallocateVO;
import com.csc.fsg.nba.vo.NbaSearchVO;
import com.csc.fsg.nba.vo.NbaUserVO;
/**
 * This is a stateless session bean class for Reallocate Business Function.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA056</td><td>Version 3</td><td>Reallocate Business Function</td></tr>
 * <tr><td>SPR1851</td><td>Version 4</td><td>Locking Issues</td></tr>
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr>
 * <tr><td>NBA179</td><td>Version 7</td><td>Reallocate UI Rewrite</td></tr>
 * <tr><td>NBA213</td><td>Version 7</td><td>Unified User Interface</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaReallocateFacadeBean implements SessionBean {
	private javax.ejb.SessionContext mySessionCtx;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}
	/**
	 * Retrieves the result from netserver for a search criteria and 
	 * polulate <code> NbaReallocateVO </code> with details.
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param searchVO An instance of <code>NbaSearchVO</code>
	 * @return List An array of reallocate value objects.
	 * @throws NbaBaseException
	 */
	//NBA103
	public List doSearch(NbaUserVO user, NbaSearchVO searchVO) throws NbaBaseException {
		searchVO.setIsRecordLimitSet(true);
		searchVO.setResultClassName("NbaReallocateVO");

		try {//NBA103
			// SPR3290 code deleted
			List list = WorkflowServiceHelper.lookupWork(user, searchVO).getSearchResults();
			if(list.size() > 1){
				Collections.sort(list);
			}
			return list;
		} catch (NbaBaseException e) {//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(NbaBaseException.LOOKUP, t);//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(t);//NBA103
			throw e;//NBA103
		}
	}

	/**
	 * Reallocate all the selected workitems and update <code> NbaReallocateVO </code> value object.
	 * @param user An instance of <code>NbaUserVO</code>
	 * @param reallocateList An array of reallocate value objects.
	 * @return List An array of updated reallocate value objects.
	 * @throws NbaBaseException
	 */
	//NBA103
	public List reallocate(NbaUserVO user, List reallocateList) throws NbaBaseException {
		List updatedList = new ArrayList();
		try {//NBA103
			// SPR3290 code deleted
			for (int i = 0; i < reallocateList.size(); i++) {
				NbaReallocateVO reallocate = (NbaReallocateVO) reallocateList.get(i);
				if (reallocate.getActionIndicatorCode() != null
					&& reallocate.getActionIndicatorCode().equals(NbaActionIndicator.ACTION_ROUTE)
					&& reallocate.getNewStatus() != null
					&& reallocate.getNewStatus().length() > 0) {
					try {
						NbaAwdRetrieveOptionsVO retVO = new NbaAwdRetrieveOptionsVO();
						retVO.setWorkItem(reallocate.getWorkitemID(), reallocate.isCase());
						retVO.setLockWorkItem();
						NbaDst dst = WorkflowServiceHelper.retrieveWorkItem(user, retVO); //SPR1851, NBA213
						if (dst == null) {
							throw new NbaBaseException(NbaNetServerException.RETRIEVE_WORK);
						}
						dst.setStatus(reallocate.getNewStatus());
						//Begin NBA179
						if (reallocate.getUndwriterLob() != null) {
                            dst.getNbaLob().setUndwrtQueue(reallocate.getUndwriterLob());
                        }
						//End NBA179
						//APSL4814 Starts
						dst.setUpdQueueDateFlag(true);
						//APSL4814 Ends
						try {
							WorkflowServiceHelper.update(user, dst);
						} catch (Throwable t) {
							WorkflowServiceHelper.unlockWork(user, dst);
							throw new NbaBaseException(NbaNetServerException.UPDATE);
						}
						WorkflowServiceHelper.unlockWork(user, dst);
						reallocate.setAction(NbaActionIndicator.ACTION_ROUTE_SUCCESSFUL);
					} catch (Exception e) {
						reallocate.setAction(NbaActionIndicator.ACTION_ROUTE_FAILED);
					}
				}
				updatedList.add(reallocate);
			}			
		// SPR3290 code deleted
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException();//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		}
		return updatedList;
	}
	//NBA213 code deleted
}
