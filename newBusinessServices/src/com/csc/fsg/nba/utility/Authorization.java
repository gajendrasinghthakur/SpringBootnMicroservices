/*
 * Created on Aug 20, 2012
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.csc.fsg.nba.utility;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Authorization is used for storing authorization info
 */
public class Authorization {

	String role;

	String page;

	Map visibility;

	Map enablement;

	public Authorization() {
	}

	public Authorization(Map m1, Map m2) {
		visibility = m1;
		enablement = m2;
	}

	/**
	 * @return Returns the page.
	 */
	public String getPage() {
		return page;
	}

	/**
	 * @param page
	 *            The page to set.
	 */
	public void setPage(String page) {
		this.page = page;
	}

	/**
	 * @return Returns the role.
	 */
	public String getRole() {
		return role;
	}

	/**
	 * @param role
	 * The role to set.
	 */
	public void setRole(String role) {
		this.role = role;
	}

	/*
	 Function populateData() populates keyMap containing page,rights values for buttons.
	 */
	public Map populateData(String visible, String page) {
		int count = 0;
		Iterator iterEnablement = enablement.entrySet().iterator();
		while (iterEnablement.hasNext()) {
			Map.Entry entry = (Map.Entry) iterEnablement.next();
			count++;
		}
		Map keyMap = new HashMap();
		try {
			if (visible.equals("U")) {
				Iterator iterEnablement1 = enablement.entrySet().iterator();
				while (iterEnablement1.hasNext()) {
					Map.Entry entry = (Map.Entry) iterEnablement1.next();
					String key = (String) entry.getKey();
					String val = (String) entry.getValue();
					if (val.equals("enabled") || val.equals("enable")) {
						val = "U";
					} else {
						val = "V";
					}
					if (count > 1) {
						String page1 = page + key;
						keyMap.put(page1, val);
					} else {
						keyMap.put(page, val);
					}
				}
			} else {
				Iterator iterEnablement2 = enablement.entrySet().iterator();
				while (iterEnablement2.hasNext()) {
					Map.Entry entry = (Map.Entry) iterEnablement2.next();
					String key = (String) entry.getKey();
					if (count > 1) {
						String page1 = page + key;
						keyMap.put(page1, "X");
					} else {
						keyMap.put(page, "X");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyMap;
	}

	/*
	 Function populateSpecialData() populates keyMap for special pages.
	 keyMap contains (page,rights) values ie initial tab rendering values.Example:- (ApplicationEntry,U/show) or (ApplicationEntry,X/hide)
	 */
	public Map populateSpecialData(String page) {
		Iterator iterVisibility = visibility.entrySet().iterator();
		Map keyMap = new HashMap();
		while (iterVisibility.hasNext()) {
			Map.Entry entry = (Map.Entry) iterVisibility.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			if ((page.equals("pagecode.workFlow.file.nba.Inbox") && key.equals("empcase")) || key.equals("UnderwriterQueueReassignment")
					|| key.equals("securecommentsadd")) {
				continue;
			}
			if (val.equals("show") || val.equals("enabled") || val.equals("enable")) {
				val = "U";
			} else {
				val = "X";
			}
			keyMap.put(key, val);
		}
		Iterator iterEnablement = enablement.entrySet().iterator();
		while (iterEnablement.hasNext()) {
			Map.Entry entry = (Map.Entry) iterEnablement.next();
			String key = (String) entry.getKey();
			String val = (String) entry.getValue();
			if (page.equals("pagecode.workFlow.file.nba.Inbox") && key.equals("empCase") || key.equals("securecommentsadd")) {
				continue;
			}
			if (val.equals("enabled") || val.equals("enable")) {
				val = "U";
			} else {
				val = "X";
			}
			keyMap.put(key, val);
		}
		return keyMap;
	}
}
