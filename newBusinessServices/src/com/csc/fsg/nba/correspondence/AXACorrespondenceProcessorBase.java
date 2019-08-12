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
 *     Copyright (c) 2002-2010 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
package com.csc.fsg.nba.correspondence;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * This is the base class for all the correspondence processors that will provide  
 * implementation for OINK retrieve methods for letter specific variables. 
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.13</td><td>Version 7</td><td>Formal Correspondence</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public abstract class AXACorrespondenceProcessorBase {

    private NbaUserVO userVO;

    private NbaTXLife nbaTXLife;

    private NbaDst nbaDst;

    private Object object;

    /**
     *  Default constructor
     */
    protected AXACorrespondenceProcessorBase() {
        super();

    }

    /**
     * @param userVO
     * @param nbaTXLife
     * @param nbaDst
     * @param object
     */
    public AXACorrespondenceProcessorBase(NbaUserVO userVO, NbaTXLife nbaTXLife, NbaDst nbaDst, Object object) {
        super();
        setUserVO(userVO);
        setNbaTXLife(nbaTXLife);
        setNbaDst(nbaDst);
        setObject(object);
    }

  /**
   * Method which will be implemented by all the specific processors. 
   * @return TxLife as response
   */
    public Object resolveVariables(Object object) {
        return null;
    }

    /**
     * @return Returns the nbaTXLife.
     */
    public NbaTXLife getNbaTXLife() {
        return nbaTXLife;
    }

    /**
     * @param nbaTXLife
     *            The nbaTXLife to set.
     */
    public void setNbaTXLife(NbaTXLife nbaTXLife) {
        this.nbaTXLife = nbaTXLife;
    }

    /**
     * @return Returns the userVO.
     */
    public NbaUserVO getUserVO() {
        return userVO;
    }

    /**
     * @param userVO
     *            The userVO to set.
     */
    public void setUserVO(NbaUserVO userVO) {
        this.userVO = userVO;
    }

    /**
     * @return Returns the nbaDst.
     */
    public NbaDst getNbaDst() {
        return nbaDst;
    }

    /**
     * @param nbaDst
     *            The nbaDst to set.
     */
    public void setNbaDst(NbaDst nbaDst) {
        this.nbaDst = nbaDst;
    }


    /**
     * @return Returns the object.
     */
    public Object getObject() {
        return object;
    }

    /**
     * @param object
     *            The object to set.
     */
    public void setObject(Object object) {
        this.object = object;
    }


    /**
     * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
     * 
     * @return the logger implementation
     */
    protected  NbaLogger getLogger() {
        NbaLogger logger =null;
        try {
            logger = NbaLogFactory.getLogger(this.getClass().getName());
        } catch (Exception e) {
            NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
            e.printStackTrace(System.out);
        }

        return logger;
    }
}
