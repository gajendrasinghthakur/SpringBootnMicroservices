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
import java.util.ArrayList;
import java.util.Map;

import javax.ejb.SessionBean;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLoggingContext;
import com.csc.fsg.nba.foundation.NbaNDC;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.tableaccess.NbaTableData;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.configuration.BackEnd;
import com.csc.fsg.nba.vo.configuration.BusinessRules;
import com.csc.fsg.nba.vo.configuration.Netserver;

/**
 * This is a stateless Session Bean which provides various services.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA001</td><td>Version 1</td><td>Initial Development</td></tr>
 * <tr><td>NBA005</td><td>Version 2</td><td>Scheduled Reports</td></tr>
 * <tr><td>NBA021</td><td>Version 2</td><td>Data Resolver</td></tr>
 * <tr><td>NBA044</td><td>Version 3</td><td>Architecture Changes</td></tr> 
 * <tr><td>NBA103</td><td>Version 4</td><td>Logging</td></tr> 
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * <tr><td>NBA123</td><td>Version 6</td><td>Administrator Console Rewrite</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaEjbServicesBean implements SessionBean {
	private javax.ejb.SessionContext mySessionCtx = null;
	private final static long serialVersionUID = 3206093459760846163L;
	private int maxDepth;		// NBA044

/**
 * ejbActivate method comment
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbActivate() throws java.rmi.RemoteException {}
/**
 * ejbCreate method comment
 * @exception javax.ejb.CreateException The exception description.
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbCreate() throws javax.ejb.CreateException {}
/**
 * ejbPassivate method comment
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbPassivate() throws java.rmi.RemoteException {}
/**
 * ejbRemove method comment
 * @exception java.rmi.RemoteException The exception description.
 */
public void ejbRemove() throws java.rmi.RemoteException {}
/**
 * Get the depth of the NDC stack for the current thread. If 
 * the depth is zero, add an entry to the stack identifying
 * this EJB as the "user".  Otherwise, assume that this EJB 
 * is being invoked as part of processing for a real user.
 */
//NBA044 New Method	
private void ndcStart() {
	maxDepth = NbaNDC.getDepth();
	if (maxDepth == 0) {
		NbaNDC.setNDC("Ejb Services");
	}
}
/**
 * If the original depth was zero, invoke remove() to allow
 * garbage collection.
 */
//NBA044 New Method	
private void ndcEnd() {
	if (maxDepth == 0) {
		NbaNDC.removeNDC();
		NbaLoggingContext.clear(); //NBA103
	}
}
/**
 * Answer the AWD/NetServer configuration information.
 * @return com.csc.fsg.nba.configuration.NbaConfigNetServer
 */
//ACN012 CHANGED SIGNATURE
public ArrayList getAllProcesses() throws NbaBaseException {
	try {//NBA103
	//begin NBA044	
	ndcStart();
	ArrayList list = NbaConfiguration.getInstance().getAllAutomatedProcesses(); //ACN012
	ndcEnd();
	return list; //ACN012
	} catch (NbaBaseException e) {//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	} catch (Throwable t) {//NBA103
		NbaBaseException e = new NbaBaseException(t);	//NBA103	
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	}
	//end NBA044
}
/**
 * Answer the Back End System Configuration information matching the supplied systemId
 * @param systemId java.lang.String Back End System Identification
 * @return com.csc.fsg.nba.configuration.NbaConfigBackEndSystem
 */
//ACN012 CHANGED SIGNATURE
public BackEnd getBackEndSystem(String systemId) throws NbaBaseException {
	//begin NBA044	
	try {	//NBA103	
	ndcStart();
	BackEnd nbaConfigBackEndSystem = NbaConfiguration.getInstance().getBackEndSystem(systemId); //ACN012
	ndcEnd();
	return nbaConfigBackEndSystem;
	} catch (NbaBaseException e) {//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	} catch (Throwable t) {//NBA103
		NbaBaseException e = new NbaBaseException(t);//NBA103		
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	}
	//end NBA044
}
/**
 * Answer the nbA Business Rules configuration information.
 * @return com.csc.fsg.nba.configuration.NbaConfigBusinessRules
 */
//ACN012 CHANGED SIGNATURE
public BusinessRules getBusinessRules() throws NbaBaseException {
	//begin NBA044	
	try {//NBA103
	ndcStart();
	BusinessRules nbaConfigBusinessRules = NbaConfiguration.getInstance().getBusinessRules(); //ACN012
	ndcEnd();
	return nbaConfigBusinessRules;
	} catch (NbaBaseException e) {//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	} catch (Throwable t) {//NBA103
		NbaBaseException e = new NbaBaseException(t);//NBA103		
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	}
	//end NBA044
}
/**
 * Answer the String containing the file path matching the supplied fileDescription.
 * @param systemId java.lang.String. The descriptive name of the file.
 * @return systemId java.lang.String. A String containing the path of a file.
 */
public String getFileLocation(String fileDescription) throws NbaBaseException {
	//begin NBA044	
	try {//NBA103
	ndcStart();
	String aString = NbaConfiguration.getInstance().getFileLocation(fileDescription);
	ndcEnd();
	return aString;
	} catch (NbaBaseException e) {//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	} catch (Throwable t) {//NBA103
		NbaBaseException e = new NbaBaseException(t);//NBA103		
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	}
	//end NBA044
}
/**
 * Answer the AWD/NetServer configuration information.
 * @return com.csc.fsg.nba.configuration.NbaConfigNetServer
 */
//ACN012 CHANGED SIGNATURE
public Netserver getNetServer() throws NbaBaseException {
	//begin NBA044	
	try {//NBA103
	ndcStart();
	Netserver nbaConfigNetServer = NbaConfiguration.getInstance().getNetserver(); //ACN012
	ndcEnd();
	return nbaConfigNetServer;
	} catch (NbaBaseException e) {//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	} catch (Throwable t) {//NBA103
		NbaBaseException e = new NbaBaseException(t);//NBA103		
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	}
	//end NBA044
}
/**
 * Answer a AWD/NetServer Business Area from the configuration information.
 * @Param aName the Business Area name
 * @return the Business Area value
 */
public String getNetServerBusinessArea(String aName) throws NbaBaseException {
	//begin NBA044	
	try {//NBA103
	ndcStart();
	String aString = NbaConfiguration.getInstance().getNetserverBusinessArea(aName).getName(); //ACN012
	ndcEnd();
	return aString;
	} catch (NbaBaseException e) {//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	} catch (Throwable t) {//NBA103
		NbaBaseException e = new NbaBaseException(t);//NBA103		
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	}
	//end NBA044
}
//NBA123 code deleted
/**
 * getSessionContext method comment
 * @return javax.ejb.SessionContext
 */
public javax.ejb.SessionContext getSessionContext() {
	return mySessionCtx;
}
/**
 * Answer the support table for the specified map and tablename.
 * @return <code>NbaTableData[]</code> contains the retrieved data objects
 */
//NBA005 New Method
public NbaTableData[] getSupportTableData(Map aMap, String tableName) throws NbaBaseException {
	try {	//NBA103
	//begin NBA044	
	ndcStart();
	NbaTableData[] nbaTableData = new NbaTableAccessor().getDisplayData(aMap, tableName);
	ndcEnd();
	return nbaTableData;
	} catch (NbaBaseException e) {//NBA103
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	} catch (Throwable t) {//NBA103
		NbaBaseException e = new NbaBaseException(t);	//NBA103	
		NbaLogFactory.getLogger(this.getClass()).logException(e);//NBA103
		throw e;//NBA103
	}
}
/**
 * setSessionContext method comment
 * @param ctx javax.ejb.SessionContext
 * @exception java.rmi.RemoteException The exception description.
 */
public void setSessionContext(javax.ejb.SessionContext ctx) throws java.rmi.RemoteException {
	mySessionCtx = ctx;
}
}
