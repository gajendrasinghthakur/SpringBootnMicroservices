package com.csc.fsg.nba.utility;

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

import java.util.List;

/**
 * This interface expose reflection utility functions for nbA.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA297</td><td>Version 1201</td><td>Suitability</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */

public interface NbaReflectionUtils {
	
	public static final String REFLECTION_UTILS = "newBusiness/comp/ReflectionUtils";
	
	public Object getValue(Object dataObject, String objName,Class[] parameterTypes, Object[] args ) throws Exception; 
	public Object getValue(Object dataObject, String attribute) throws Exception;
	public List getValueFromClass(List fieldsName) throws Exception;
	public boolean isValue(Object dataObject, String objName ) throws Exception ;
	public boolean hasMethod(Object dataObject, String attribute);
	
	
}
