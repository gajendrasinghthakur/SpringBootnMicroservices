package com.csc.fsg.nba.communication;

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
 */
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.configuration.Provider;

/**
 * NbaAbsCommunicator provides a basis for classes that will be used with nbA to
 * communicate with an outside service.
 * A class must extend this class and implement the processMessage method which
 * will submit a message and receive a response.
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN009</td><td>Version 4</td><td>ACORD 401/402 MIB Inquiry and Update Migration</td></tr>
 * <tr><td>AXAL3.7.31</td><td>AXA Life Phase 1</td><td>Provider Interface - MIB</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public abstract class NbaAbsCommunicator {
	/**
	 * This is the abstract method that must be implemented by a class in order
	 * to initialize Provider information for the communications process.  
	 * @param provider a Provider object from the nbaConfiguration file
	 */
	abstract public void initialize(Provider provider);
	/**
	 * This is the abstract method that must be implemented by a class in order
	 * to communicate with a service outside of nbA.  
	 * @param target a path or web address to which the message should be written/posted
	 * @param message the message to be sent to the provider
	 * @return Object an Object representing the result of the communication
	 */
	abstract public Object processMessage(String target, Object message) throws NbaBaseException;

	/**
	 * This is the abstract method that must be implemented by a class in order
	 * to communicate with a service outside of nbA.  
	 * @param target a path or web address to which the message should be written/posted
	 * @param message the message to be sent to the provider
	 * @param user the user object sending the message to provider
	 * @return Object an Object representing the result of the communication
	 */
	//AXAL3.7.31 New method
	abstract public Object processMessage(String target, Object message, NbaUserVO user) throws NbaBaseException;
	
}
