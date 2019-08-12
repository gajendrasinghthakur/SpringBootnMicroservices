package com.csc.fsg.nba.workflow.api.rest;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.ObjectRepository;
import com.csc.fs.Result;
import com.csc.fs.dataobject.accel.workflow.ConsolidatedComment;
import com.csc.fs.dataobject.accel.workflow.ConsolidatedHistory;
import com.csc.fs.dataobject.accel.workflow.ItemID;
import com.csc.fs.sa.SystemAPI;
import com.csc.fs.sa.SystemService;

/**
 * RetrieveHistoryPostProcessor contains post-process logic for retrieval of AWD history comments work items
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL50550-NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @since New Business Accelerator - Version NB-1401
 */

 public class RetrieveHistoryPostProcessor extends com.csc.fsg.nba.workflow.api.rest.RestPostProcessor{
	/**
	 * Post-process logic for system AWD REST retrieve history and comments
	 * For each ConsolidatedHistory in the response:
	 * -	Set the itemID to the work item ID
	 * -	Update the userID to contain only the ID of the user
	 * -	Update the DateTime to contain only the date/time
	 * -	Update the beginDateTime to contain only the date/time
	 * -	Set  the startDateTime to contain the beginDateTime
	 * -	Set the systemName to the systemName from the API
	 * -	Set endPrty to the value of priority
	 * For each ConsolidatedComment
	 * -	If the text value is "Object unassigned.", ignore it
	 * -	Set the itemID to the work item ID
	 * -	Update the userID to contain only the ID of the user
	 * -	Set commentType to �M�
	 * -	Set description to �Manual Comment�
	 * -	Set the systemName to the systemName from the API
	 * 
	 */
	public Result systemApi(List input, Result result, SystemService service,
			SystemAPI api, ObjectRepository or) {

		ItemID itemID = null;
		Iterator inputIterator = input.iterator();
		while (inputIterator.hasNext()) {
			Object inputObj = inputIterator.next();
			if (inputObj instanceof ItemID) {
				itemID = (ItemID) inputObj;
				break;
			}
		}

		if (itemID == null) {
			return result;
		}

		String itemKey = itemID.getItemID();
		List newData = new ArrayList();

		if (!result.hasErrors()) {
			List data = result.getReturnData();
			if (data != null && !data.isEmpty()) {
				Result result1 = (Result) data.get(0);
				if (!result1.hasErrors()) {
					List currentData = result1.getReturnData();
					Iterator dataObjects = currentData.iterator();
					while (dataObjects.hasNext()) {
						Object currentObj = dataObjects.next();
						if (currentObj instanceof ConsolidatedHistory) {
							ConsolidatedHistory histDO = (ConsolidatedHistory)currentObj;
							histDO.setItemID(itemKey);
//							//NAB331.1 code deleted
							histDO.setUserID(getValue(histDO.getUserName(), "(", ")"));
							histDO.setDateTime(formatDateTime(histDO.getDateTime(), histDO.getStartTime(), " "));
							histDO.setBeginDateTime(formatDateTime(histDO.getBeginDate(), histDO.getBeginTime(), " "));
							histDO.setStartTime(histDO.getBeginDateTime());
							histDO.setSystemName(api.getSystemName());
							//convert priority to string field for display on history page
							Integer priority = histDO.getPriority();
							if (priority!=null) {
								histDO.setEndPrty(priority.toString());
							}
 							newData.add(histDO);
						} else if (currentObj instanceof ConsolidatedComment) {
                            ConsolidatedComment commDO = (ConsolidatedComment) currentObj;
                            if (!"Object unassigned.".equalsIgnoreCase(commDO.getText())) {
                                commDO.setItemID(itemKey);
                                commDO.setUserID(getValue(commDO.getUserID(), "(", ")"));
                                commDO.setStartTime(formatDateTimeWithOffset(commDO.getDateTime(), commDO.getStartTime())); //NBA331.1
                           		//NBA331.1 code deleted
                                commDO.setCommentType("M");
                                commDO.setDescription("Manual Comment");
                           		//NBA331.1 code deleted                                
                                commDO.setSystemName(api.getSystemName());
                                newData.add(commDO);
                            }
                        } else {
							newData.add(currentObj);
						}
					}
					Result tempResult = result1;
					result1.clear();
					result1.addMessages(tempResult.getMessages());
					result1.addResults(newData);
				}
			}
		}

		return result;

	}


}
