package com.csc.fsg.nba.asynch.helper;
/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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

import com.csc.fs.accel.async.AsynchronousTransactionHelper;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.transaction.validation.NbaTransactionValidationFacade;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;
/**
 * NbaAsynchTransactionValidationHelper
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA208-9</td><td>Version 7</td><td>Asynchronous Services</td></tr>
 * <tr><td>P2AXAL016</td><td>AXA Life Phase 2</td><td>Product Validation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class NbaAsynchTransactionValidationHelper extends AsynchronousTransactionHelper {
     protected NbaTXLife nbaTXLife = null;
     protected NbaDst nbaDst = null;
     protected NbaUserVO user = null;
    
     /**
      * 
      */
     public NbaAsynchTransactionValidationHelper(NbaTXLife _nbaTXLife,NbaDst _nbaDst,NbaUserVO _user) {
         nbaTXLife = _nbaTXLife;
         nbaDst = _nbaDst;
         user = _user;
     }
     
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            NbaTransactionValidationFacade.validateBusinessProcess(nbaTXLife, nbaDst, user, null);//P2AXAL016
            setNbaTXLife(nbaTXLife);
            setNbaDst(nbaDst);
        } catch (NbaBaseException e) {
            setProcessException(e);
        }
    }
    
    /**
     * @return Returns the nbaDst.
     */
    public NbaDst getNbaDst() {
        return nbaDst;
    }
    /**
     * @param nbaDst The nbaDst to set.
     */
    public void setNbaDst(NbaDst nbaDst) {
        this.nbaDst = nbaDst;
    }
    /**
     * @return Returns the nbaTXLife.
     */
    public NbaTXLife getNbaTXLife() {
        return nbaTXLife;
    }
    /**
     * @param nbaTXLife The nbaTXLife to set.
     */
    public void setNbaTXLife(NbaTXLife nbaTXLife) {
        this.nbaTXLife = nbaTXLife;
    }
}
