package com.csc.fsg.nba.correspondence;

/*
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 * 
 */
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;

/** 
 * 
 * This class decides which Correspondence adapter instance should be used based-off entries present
 * in the configuration file.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA013</td><td>Version 2</td><td>Correspondence System</td></tr> 
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 2
 */
public class NbaCorrespondenceAdapterFactory {
	protected NbaCorrespondenceAdapter adapter;
	protected com.csc.fsg.nba.foundation.NbaLogger logger;
/**
 * This constructor initializes all instance variables.
 * 
 */
public NbaCorrespondenceAdapterFactory() {
    try {
        logger = NbaLogFactory.getLogger(this.getClass().getName());
    } catch (Exception e) {
        NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
        e.printStackTrace(System.out);
    }
}
/**
 * This method creates an instance of the Correspondence System adapter, which is decided in the configuration file. A reference to this 
 * instance is returned.
 * @return com.csc.fsg.nba.correspondence.NbaCorrespondenceAdapter
 */
public NbaCorrespondenceAdapter getAdapterInstance() throws NbaBaseException {
    try {
        if (adapter == null) {
            adapter = (NbaCorrespondenceAdapter) NbaUtils.classForName(NbaConfiguration.getInstance().getCorrespondence().getAdapterClass()).newInstance(); //ACN012
        }
    } catch (Exception e) {
        logger.logDebug("Exception encountered in getAdapterInstance()" + e);
        throw new NbaBaseException(this.getClass().getName(), e);
    }
    return adapter;
}
}
