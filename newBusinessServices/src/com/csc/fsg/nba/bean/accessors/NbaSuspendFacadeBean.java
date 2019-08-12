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
import com.csc.fsg.nba.vo.NbaSuspendVO;
import com.csc.fsg.nba.vo.NbaUserVO;
/**
 * This is a stateless Session Bean that is used to suspend the work Object in the AWD.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA103</td><td>Version 4/td><td>Logging/tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see com.csc.fsg.nba.business.support.NbaSuspendViewHelper
 * @since New Business Accelerator - Version 1
 */
public class NbaSuspendFacadeBean implements SessionBean {
	private javax.ejb.SessionContext mySessionCtx = null;
	private final static long serialVersionUID = 3206093459760846163L;

/**
 * ejbActivate method comment.
 */
public void ejbActivate() throws java.rmi.RemoteException {}
/**
 * ejbCreate method comment
 * @exception javax.ejb.CreateException The exception description.
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbCreate() throws javax.ejb.CreateException {
}
/**
 * ejbPassivate method comment.
 */
public void ejbPassivate() throws java.rmi.RemoteException {}
/**
 * ejbRemove method comment.
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
 * setSessionContext method comment.
 */
public void setSessionContext(javax.ejb.SessionContext arg1) throws java.rmi.RemoteException {}
	/**
	 * This Method will call Netserver to suspend a workitem. It will also unlock suspended workitem.
	 * @param userVO - Currently logged on user
	 * @param suspendVO - Value object for suspend.
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	//NBA103
	public void suspendWork(NbaUserVO userVO, NbaSuspendVO suspendVO, NbaDst nbaDst) throws NbaBaseException {
		try {//NBA103
			// SPR3290 code deleted
			WorkflowServiceHelper.suspendWork(userVO, suspendVO);
		} catch (NbaBaseException e) {//NBA103
			e.addMessage("Unable to suspend the work");//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		} catch (Throwable t) {//NBA103
			NbaBaseException e = new NbaBaseException("Unable to suspend the work", t);//NBA103
			NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
			throw e;//NBA103
		}
	
		unlockWork(userVO, nbaDst);
	}

	//NBA103	
	private void unlockWork(NbaUserVO userVO, NbaDst nbaDst) throws NbaBaseException {
		try {
			WorkflowServiceHelper.unlockWork(userVO, nbaDst);
		} catch (NbaBaseException e) {
			e.addMessage("Suspend successful but unable to unlock the work");
			NbaLogFactory.getLogger(this.getClass()).logException(e);
			throw e;
		} catch (Throwable t) {
			NbaBaseException e = new NbaBaseException("Suspend successful but unable to unlock the work", t);			
			NbaLogFactory.getLogger(this.getClass()).logException(e);			
			throw e;
		}
	}
}
