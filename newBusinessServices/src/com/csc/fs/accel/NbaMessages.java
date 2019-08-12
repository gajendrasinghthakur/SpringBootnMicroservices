package com.csc.fs.accel;

/*
 * *******************************************************************************<BR>
 * Copyright 2015, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.util.ResourceBundle;

import com.csc.fs.Message;
import com.csc.fs.logging.LogHandler;

/**
 * NbaMessages consolidates all the error, warning, and info messages which can be
 * returned from the newBusiness component's business processes and services.  The error
 * message text and variable data are located in the 'NbaServicesMessages.properties'
 * file.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA330</td><td>Version NB-1401</td><td>Product Versioning</td></tr>
 * <tr><td>APSL5055</td><td>Version</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see com.csc.fs.Message
 * @since New Business Accelerator - Version NB-1401
 */

public class NbaMessages {
	/*
	 * New Business Services component value
	 */
	public final static int NBA = 850;

    private static ResourceBundle messageBundle = ResourceBundle.getBundle("properties.NbaServicesMessages");
	
	/*
	 * Error Messages
	 */
	public final static Message ERR_MSG_HBRNATE_INV_INPUT = Message.create(NBA, 600, Message.ERROR, getData("hibernateInvInput"));
	public final static Message ERR_MSG_HBRNATE_EXCEPTION = Message.create(NBA, 601, Message.ERROR, getData("hibernateException"));


	
	/**
	 * Retrieves a string from the NbaServicesMessages.properties file for the specified
	 * key.  If the key is not found in the NbaServicesMessages.properties files, the
	 * value of the key will be returned and an error message will be logged.
	 * @param key
	 * @return
	 */
	public static String getData(String key) {
		String data = key;
		try {
			data = messageBundle.getString(key);
		} catch (Exception e) {
			LogHandler.Factory.LogError(NbaMessages.class, "A problem occurred trying retrieve a property from NbaServicesMessages.properties", e);
		}
		return data;
	}
}
