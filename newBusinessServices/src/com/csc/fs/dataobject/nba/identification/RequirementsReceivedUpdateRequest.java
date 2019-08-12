package com.csc.fs.dataobject.nba.identification;

 

import java.util.ArrayList;
import java.util.List;

import com.csc.fs.dataobject.accel.AccelDataObject;
import com.csc.fsg.nba.vo.NbaWorkItem;

/**
 * An update request data object used to associate the underwriting requirements
 * received to the application work item.  This information will be used later by the
 * work item identification service.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331</td><td>Version NB-1401</td><td>AWD REST</td></tr>
 * <tr><td>APSL5055</td><td>Version</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class RequirementsReceivedUpdateRequest extends AccelDataObject {

	private static final long serialVersionUID = -7307324684242395500L;

	private String workItemID = null;
	private List<String> workItems = new ArrayList<String>();

	/**
	 * Returns the application's work item ID.
	 * @return
	 */
	public String getApplicationWorkItemID() {
		return workItemID;
	}

	/**
	 * Sets the application's work item ID.
	 * @param id
	 */
	public void setApplicationWorkItemID(String id) {
		workItemID = id;
	}

	/**
	 * Returns the list of requirement work items received.  Use this method to
	 * clear the list. 
	 * @return
	 */
	public List<String> getRequirementWorkItems() {
		return workItems;
	}

	/**
	 * Adds a requirement work item received to the list of requirement work items.
	 * @param work
	 */
	public void addRequirementWorkItem(String work) {
		workItems.add(work);
	}
}
