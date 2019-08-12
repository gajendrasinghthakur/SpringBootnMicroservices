package com.csc.fsg.nba.asynch.helper;
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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.csc.fs.accel.async.AsynchronousTransactionHelper;
import com.csc.fsg.nba.contract.auditor.NbaAuditorFactory;
import com.csc.fsg.nba.database.NbaDatabaseUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBatchProcessingContext;
import com.csc.fsg.nba.vo.NbaAuditorVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaLob;
/**
 * NbaAsynchronousAuditProcessingHelper
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-9</td><td>Version 7</td><td>Asynchronous Services</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaAsynchronousAuditProcessingHelper extends AsynchronousTransactionHelper {
    protected NbaAuditorVO auditorVO = null;

    protected Connection auditConn = null;
    
    protected NbaLob nbaLob = null;

    /**
     * 
     */
    public NbaAsynchronousAuditProcessingHelper(NbaAuditorVO _auditorVO, NbaLob _lob,Connection _auditConn) {
        auditorVO = _auditorVO;
        auditConn = _auditConn;
        nbaLob = _lob;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
		    NbaAuditorFactory.getInstance().getAuditor().audit(auditorVO,auditConn);
		    //begin NBA208-22
		    if (NbaConfiguration.isConfigurationEnabledBatch() && NbaDatabaseUtils.isBatchEnabled(auditConn)) {
		        Statement stmt = NbaBatchProcessingContext.getStatementForBatch(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob
                    .getCompany());
		        if (stmt != null) {
		            stmt.executeBatch();
		            NbaBatchProcessingContext.clear(nbaLob.getPolicyNumber(), nbaLob.getBackendSystem(), nbaLob.getCompany());
		        }
		    }
        } catch (NbaBaseException e) {
            setProcessException(e);
        } catch (SQLException e) {
            setProcessException(new NbaBaseException(e));
        }
    }

}
