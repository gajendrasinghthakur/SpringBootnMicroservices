package com.csc.fsg.nba.backendadapter;

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
 * 
 * *******************************************************************************<BR>
 */
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaTXLife;


/**
 * This is the interface for connecting to the backendadaptor
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA103</td><td>Version 1</td><td>Logging</td></tr>
 * </table>
 * @author: CSC FSG Developer
 * @version 7.0.0
 * @since New Business Automation - Version 1
 */
public interface NbaBackEndAdapter {
	/**
	 * Interface method  for call from client to send DXE to the host
	 * @param NbatxLife The current NbatxLife request.
	 * @return NbatxLife Contains NbatxLife reponse from the host
	 * @exception java.rmi.RemoteException and NbaBaseException.
	 */
	NbaTXLife submitRequestToHost(NbaTXLife NbatxLife) throws NbaBaseException;
	
	/**
	 * Interface method  for call from client to send DXE to the host
	 * @param XML document java.lang.String 
	 * @return java.lang.String containing XML reponse from the host
	 * @exception java.rmi.RemoteException and NbaBaseException.
	 */
	String submitRequestToHost(String xmlDoc) throws NbaBaseException;
}
