package com.csc.fs.accel.async;
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

import commonj.work.Work;
/**
 * Work to be executed Asynchronously
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
public class AccelWork implements Work{

    private Runnable runnable = null;
    private boolean isDaemon = false;
    
    /**
     * Constructor
     * @param _runnable the java.lang.Runnable work to be executed asynchronously
     */
    public AccelWork(Runnable _runnable) {
        runnable = _runnable;
        isDaemon = false;
    }
    
    /**
     * Constructor
     * @param _runnable the java.lang.Runnable work to be executed asynchronously
     * @param _isDaemon if the work Daemon process
     */
    public AccelWork(Runnable _runnable, boolean _isDaemon) {
        runnable = _runnable;
        isDaemon = _isDaemon;
    }

    /**
     * Release the runnable Job
     * @see commonj.work.Work#release()
     */
    public void release() {
        runnable = null;
    }

    /**
     * Answer if Work is Daemon
     * @see commonj.work.Work#isDaemon()
     */
    public boolean isDaemon() {
        return isDaemon;
    }

    /**
     * Executes the run method
     * @see java.lang.Runnable#run()
     */
    public void run() {
        runnable.run();
    }
}
