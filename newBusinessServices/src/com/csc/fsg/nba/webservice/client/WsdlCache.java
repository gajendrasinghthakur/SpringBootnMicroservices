/*
 * This software and/or documentation contains trade secrets and
 * confidential information, which are proprietary to
 * Computer Sciences Corporation.
 * The use, reproduction, distribution, or disclosure of this
 * software and/or documentation, in whole or in part, without the express
 * written permission of Computer Sciences Corporation is prohibited.
 * This software and/or documentation is also an unpublished work protected
 * under the copyright laws of the United States of America and other countries.
 * If this software and/or documentation becomes published, the following
 * notice shall apply:
 *
 * Copyright © 2004 Computer Sciences Corporation. All Rights Reserved.
 */

/** 
 * 
 * This class provides a WSDL cache.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.68</td><td>Version 6</td><td>Single Sign On</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 6.0.0
 * @since New Business Accelerator - Version 2
 */

package com.csc.fsg.nba.webservice.client;

import java.util.HashMap;
import java.util.Map;


public class WsdlCache {
	private Map wsdl =  new HashMap();
	
	public Object getWsdl(String key){
		return wsdl.get(key);
	}
	
	public void addWsdl(String key , Object obj){
		if(key!=null && obj!=null){
			wsdl.put(key,obj);
		}
	}
}
