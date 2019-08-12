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
 
import javax.ejb.SessionBean;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.process.workflow.WorkflowServiceHelper;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * This is a stateless session bean class that routes the case by changing the awd case.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA103</td><td>Version 1</td><td>Logging</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.support.NbaUnderwriterWorkbenchViewHelper
 * @since New Business Accelerator - Version 1
 */

/**
 * This is a Session Bean Class
 */
public class NbaRouteFacadeBean implements SessionBean {
	private javax.ejb.SessionContext mySessionCtx = null;
	private final static long serialVersionUID = 3206093459760846163L;
/**
 * ejbActivate method comment
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbActivate() throws java.rmi.RemoteException {}
/**
 * ejbCreate method comment
 * @exception javax.ejb.CreateException The exception description.
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbCreate() throws javax.ejb.CreateException {}
/**
 * ejbPassivate method comment
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbPassivate() throws java.rmi.RemoteException {}
/**
 * ejbRemove method comment
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbRemove() throws java.rmi.RemoteException {}
/**
 * getSessionContext method comment
 * @return javax.ejb.SessionContext
 */
public javax.ejb.SessionContext getSessionContext() {
	return mySessionCtx;
}
/**
 * setSessionContext method comment
 * @param ctx javax.ejb.SessionContext
 * @exception java.rmi.RemoteException The exception description.
 */
public void setSessionContext(javax.ejb.SessionContext ctx) throws java.rmi.RemoteException {
	mySessionCtx = ctx;
}
	/**
	 * Updates the status of the case that is being routed
	 * @param userVO com.csc.fsg.nba.vo.NbaUserVO the nbA user
	 * @param nbaDst com.csc.fsg.nba.vo.NbaDst the AWD case
	 * @throws NbaBaseException 
	 * @throws java.rmi.RemoteException
	 */
	public void updateStatus(NbaUserVO userVO, NbaDst nbaDst) throws NbaBaseException {
		try {//NBA103
			WorkflowServiceHelper.update(userVO, nbaDst);
			WorkflowServiceHelper.unlockWork(userVO, nbaDst);
		} catch (NbaBaseException e) {//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException(NbaBaseException.ROUTE_STATUS_UPDATE, t);//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		}
	}
}
