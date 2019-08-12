package com.csc.fsg.nba.contract.auditor;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Iterator;
import java.util.Set;

import com.csc.fsg.nba.database.NbaAuditDataBaseAccessor;
import com.csc.fsg.nba.exception.NbaAuditorException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaAuditingContext;
import com.csc.fsg.nba.foundation.NbaAuditor;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.vo.NbaAuditorVO;

/**
 * NbaAuditor implementation, storing the audit information to datastore synchronously
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA102</td><td>Version 5</td><td>nbA Transaction Logging Project</td><tr>
 * <tr><td>NBA208-22</td><td>Version 7</td><td>Performance Changes for Batch Updates</td><tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 5
 */
public class NbaSynchronousAuditor implements NbaAuditor {

	/** 
	 * Uses NbaAuditDatabaseAccessesor to save Audit information to NBAAUDIT datastore, in the same transaction as caller.
	 * @param nbAAuditorVO Auditing information
	 * @param conn Connection to NBAAUDIT datastore
	 * @throws NbaAuditorException, exception indicates failure of audit service
	 * @throws NbaDataAccessException, exception indicates SQL exception occured while auditing
	 * @see com.csc.fsg.nba.contract.auditor.NbaAuditor#audit(com.csc.fsg.nba.vo.NbaAuditorVO, java.sql.Connection)
	 */
	public void audit(NbaAuditorVO nbAAuditorVO, Connection conn) throws NbaDataAccessException,NbaAuditorException {
		if(NbaConfigurationConstants.NO_LOGGING.equals(nbAAuditorVO.getAuditLevel())) {
			// no logging
			return;
		}
		java.util.Date eventTimeStamp = NbaAuditDataBaseAccessor.getAuditEventTimeStamp(conn);
		nbAAuditorVO.setEventTimeStamp(eventTimeStamp);
		try {
			long auditVersionNo = NbaAuditDataBaseAccessor.getInstance().insertToTransactionHistory(conn,nbAAuditorVO.getUserId(),nbAAuditorVO.getContractKey(),nbAAuditorVO.getBackendKey(),nbAAuditorVO.getCompanyKey(),nbAAuditorVO.getBusinessFunc(),nbAAuditorVO.getEventTimeStamp(),nbAAuditorVO.getAuditLevel());
			nbAAuditorVO.setAuditVersion(auditVersionNo);	
		} catch (NbaDataAccessException e) {
			throw new NbaAuditorException("Unable to insert to NBA_TRANSACTION_HISTORY",e);
		}
		// insert record to history
		if(nbAAuditorVO.getAuditLevel().equals(NbaConfigurationConstants.EVENT_LOGGING)) {
			// inserted to history
			return;
		}		
		// insert audit data 
		if(nbAAuditorVO.getInsertList() != null) {
			insertAudit(nbAAuditorVO.getInsertList(), conn, NbaAuditingContext.OBJECT_INSERTED,nbAAuditorVO.getAuditVersion(),eventTimeStamp );
		}
		
		if(nbAAuditorVO.getUpdateList() != null) {
			insertAudit(nbAAuditorVO.getUpdateList(), conn, NbaAuditingContext.OBJECT_UPDATED,nbAAuditorVO.getAuditVersion(),eventTimeStamp );
		}

		if(nbAAuditorVO.getDeleteList() != null) {
			insertAudit(nbAAuditorVO.getDeleteList(), conn, NbaAuditingContext.OBJECT_DELETED,nbAAuditorVO.getAuditVersion(),eventTimeStamp );
		}
	}

	/**
	 * Insert NbaValueObject with version, type of modification and time of modification to NBAAUDIT datastore 
	 * @param list list of modified NbaValueObject object
	 * @param conn Connection to datastore
	 * @param actionIndicator specfies the type of modification on the NbaValueObject object (Insert / Update / Delete)
	 * @param auditVersion, Version of modification
	 * @param eventTimeStamp, date / time of modification
	 * @throws NbaAuditorException, Audit service failure
	 * @throws NbaDataAccessException, SQL exception while inserting information to NBAAUDIT datastore
	 */
	protected void insertAudit(Set list, Connection conn, String actionIndicator, long auditVersion, java.util.Date eventTimeStamp) throws NbaDataAccessException,NbaAuditorException{
		Iterator itr = list.iterator();
		while(itr.hasNext()) {
			Object obj = itr.next();
			Class[] parameters = new Class[] {obj.getClass(),Connection.class,actionIndicator.getClass(),Long.class, java.util.Date.class};
			Method insertMeth = null;
			Object[] arguments = new Object[] {obj,conn,actionIndicator,new Long(auditVersion),eventTimeStamp };
			try {
				insertMeth = NbaAuditDataBaseAccessor.getInstance().getClass().getMethod("insert", parameters); //NBA208-22
				insertMeth.invoke(NbaAuditDataBaseAccessor.getInstance(), arguments);
			} catch (NoSuchMethodException e) {
				// NbaAuditDataBaseAccessor needs to be regenerated, and NBAAUDIT schema should be verified to be in sync with NBAPEND
				// severe error, Poller should be error stopped
				throw new NbaAuditorException("NbaAuditDataBaseAccessor does not support "+obj.getClass().getName());
			} catch (IllegalAccessException e) {
				// this exception indicates the generated methods do not have proper access specified,
				throw new NbaAuditorException("Audit service Failure",e);
			} catch (InvocationTargetException e) {
				// Results because of (SQL Error)DataAccessException, and NbaContractAccessBean should
				// take decision as it takes PendingContract
				if(e.getTargetException() instanceof NbaDataAccessException) {
					throw  (NbaDataAccessException)e.getTargetException();
				} 
				// if not DataAccessException, severe error, as this is not expected
				throw new NbaAuditorException("Audit service Failure",e);
			}
		}
		
	}

}
