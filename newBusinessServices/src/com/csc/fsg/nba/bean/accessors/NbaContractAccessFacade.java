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
import java.util.List;
import com.csc.fsg.nba.access.contract.NbaContractAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaContractAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Party;
/**
 * The NbaContractAccess stateless session bean provides an interface 
 * to the datastore to retrieve, insert, update and delete contract data.  
 * This class will, based on the primary datastore, invoke other classes to provide
 * services.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>See SPR2992 for changes for NBA050 NBP001 NBA093 NBA091 NBA064 NBA094 NBA066 SPR1656 SPR1851 SPR1715 NBA077 ACN012 ACN014 NBA103 SPR1719 NBA105 ACN003 ACN005 ACN013 SPR1931 SPR1163 SPR2639 NBA102  SPR2816 </td><tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>Refactored code to com.csc.fsg.nba.access.contract.NbaContractAccess. All pre-existing audit numbers have been preserved in the re-factored code.</td><tr>
 * <tr><td>NBA208-26</td><td>Version 7</td><td>Remove synchronized keyword from getLogger() methods</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
*/
public class NbaContractAccessFacade {
	protected static NbaLogger logger = null;
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected static NbaLogger getLogger() { // NBA208-26
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractAccessFacade.class.getName()); // SPR3290
			} catch (Exception e) {
				NbaBootLogger.log("NbaContractAccessFacade could not get a logger from the factory."); // SPR3290
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/**
	 * Creates a request object that will be used to perform an update to the contract.
	 * Request is manufactured using input values, NbaLob object and User objectdefault values.
	 * @param transMode indicates the type of transaction (add, update, delete, etc.)
	 * @param transContentCode indicates the contents of the transaction (insert, update, etc)
	 * @return an NbaTXRequestVO with initialized values
	 * @throws NbaBaseException
	 */
	public NbaTXRequestVO createRequestObject(NbaDst work, NbaUserVO user) throws NbaBaseException {
		return NbaContractAccess.createRequestObject(work, user);	//SPR2992
	}
	/**
	 * This method retrieves contract data from the appropriate datastore.
	 * When the contract has two sources (a primary and secondary datastore),
	 * information is retrieved from both.
	 * @param nbaTXRequest a request object containing the type of transaction
	 * to be executed, along with values used to locate the data.
	 * @return an NbaTXLife with the newly retrieved data and values
	 * @throws NbaBaseException
	 */
	public NbaTXLife doContractInquiry(NbaTXRequestVO nbaTXRequest) throws NbaBaseException {
		return NbaContractAccess.doContractInquiry(nbaTXRequest);	//SPR2992
	}
	/**
	 * This method retrieves contract data from the appropriate datastore.
	 * When the contract has two sources (a primary and secondary datastore),
	 * information is retrieved from both.
	 * @param nbaTXLifeRequest a request object containing the type of transaction
	 * to be executed, along with values used to locate the data.
	 * @return an NbaTXLife with the newly retrieved data and values
	 * @throws NbaBaseException
	 */
	public NbaTXLife doContractInquiry(NbaTXLife nbaTXLifeRequest) throws NbaBaseException {
		return NbaContractAccess.doContractInquiry(nbaTXLifeRequest); //SPR2992
	}
	// SPR2992 code deleted
	/**
	 * This method updates contract data in the appropriate datastore.
	 * When the contract has two sources (a primary and secondary datastore),
	 * information is updated in both as needed.
	 * @param nbaTXLife the original NbaTXLife object created and returned by
	 * the doContractInquiry() method
	 * @param work an NbaDst object containing information about the work item
	 * being processed
	 * @param user an NbaUserVO containing information about the requesting user 
	 * @return the NbaTXLife object response information included.  The action indicators
	 * for affected objects will be reset by the process.
	 * @throws NbaContractAccessException
	 * @throws NbaBaseException
	 */
	public NbaTXLife doContractUpdate(NbaTXLife nbaTXLife, NbaDst nbaDst, NbaUserVO user) throws NbaContractAccessException, NbaBaseException {
		return NbaContractAccess.doContractUpdate(nbaTXLife, nbaDst, user);	//SPR2992
	}
	// SPR2992 code deleted
	/**
	 * Checks to see if the NbaTXLife object is available to be opened with UPDATE
	 * access.  If so, the NbaTXLife object is locked for the user.  
	 * @param nbaTXLife an NbaTXLife object to be locked
	 * @return a boolean value of <code>true</code> is returned if the object 
	 * has been successfully locked; else, <code>false</code> is returned.
	 * @throws NbaBaseException
	 */
	public boolean requestUpdateAccess(NbaTXLife nbaTXLife) throws NbaBaseException {
		return NbaContractAccess.requestUpdateAccess(nbaTXLife);	//SPR2992
	}
	// SPR2992 code deleted
	/**
	 * This method retrieves party's data from the Party table based on the
	 * values contained in the incoming party object. If incoming party object 
	 * contains only first name then all the parties with that name would be 
	 * returned. It would consider the  standalone mode contracts only.  
	 * @param Party a party object containing the crieteria for query
	 * @param excludePolicies a comma seperated list of policies which are to be excluded while querying the data
	 * @return an List containing the query results. Its a list of NbaPartyData
	 * @throws NbaBaseException
	 */ 
	 //ALS4644 Changed data type of excludedPolicies from String to List
	public List doPartyInquiry(Party party, List excludePolicies) throws NbaBaseException {
		return NbaContractAccess.doPartyInquiry(party, excludePolicies);
	}
	// SPR2992 code deleted
}
 	 
 
