package com.csc.fsg.nba.bean.accessors;

/*
 * ******************************************************************************* <BR> 
 * This program contains trade secrets and confidential
 * information which <BR> are proprietary to CSC Financial Services Group®. The use, <BR> reproduction, distribution or disclosure of this program, in
 * whole or in <BR> part, without the express written permission of CSC Financial Services <BR> Group is prohibited. This program is also an
 * unpublished work protected <BR> under the copyright laws of the United States of America and other <BR> countries. If this program becomes
 * published, the following notice shall <BR> apply: Property of Computer Sciences Corporation. <BR> Confidential. Not for publication. <BR> Copyright
 * (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved. <BR>
 * ******************************************************************************* <BR>
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import com.csc.fs.accel.ui.AuthorizationAccess;
import com.csc.fs.accel.ui.AuthorizationAccess.AuthorizationDetails;
import com.csc.fsg.nba.utility.Authorization;

/**Some Assumptions:-
1) When tab has "show" access, check for its button authorization as follow:- 
  a) When button has enable value,it is represented as 'U' in CSV ie user has update access.
  b) When button has disable value, it is represented as 'V' ie user has read only access.
2) When tab has hidden value then it is represented by X ie that particular tab will not be visible.

Initial tab authorization ie show/hide value can be found in pages listed below.These pages are considered special pages in the code:-
#pagecode.desktops.nbaCoordinator
#pagecode.desktops.Workload
#pagecode.authorization.NbaAuthorization
#pagecode.action.NbaAuthorization
#pagecode.comments.CommentsNavigation
#pagecode.desktops.AxaBackOffice
#pagecode.workFlow.file.nba.Inbox
#pagecode.workflow.WorkflowFile
#pagecode.search.SearchIntray
#pagecode.search.SearchResultTable
*/
	
/**
 * NbaAuthorizationWebServiceBean provides a Web Service interface used to display the current security provided in nba.
*/
public class NbaAuthorizationWebServiceBean {

	public static ResourceBundle configProperties = ResourceBundle.getBundle("authorisation");
	public static boolean CASE_SENSITIVE = true ;
	public static List authFileList ;             // All three auth.xml- newBusinessConfig,axalifeConfig,workflowConfig
	private List listofPages = new ArrayList();   // List of distinct page beans in auth.xml's 
	private List listofRoles = new ArrayList();   // List of distinct role in auth.xml's  
	private List listofSytems = new ArrayList();  // List of Systems in auth.xml's   
	private List specialPages = new ArrayList();  // Pages which have Business Functions/Tab initial rendering
	private Map dataMap=new HashMap();            // Used for storing authorization objects 
	private Map valueMap=new HashMap();          // Used for simple page value retrieval
	private Map specialValueMap=new HashMap();   // Used for specialPage value retrieval
			
	static {
		authFileList = new ArrayList();
		try {
			String configVals = configProperties.getString("authorization.xml.list");
			StringTokenizer strToken = new StringTokenizer(configVals, ",");
			while (strToken.hasMoreTokens()) {
				authFileList.add(CASE_SENSITIVE ? strToken.nextToken().toUpperCase() : strToken.nextToken());
			}
		} catch (MissingResourceException ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	public NbaAuthorizationWebServiceBean(){
		initialize();
	}
	/*
	  Function initialize():-
      1)Loads listofPages,listofRoles,listofSytems.
	  2)Ignore pages not required.
	  3)Populates dataMap with Authorization objects.
   */
	public void initialize() {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			for (int i = 0; i < authFileList.size(); i++) {
				Document doc1 = db.parse("////127.0.0.1//nbaworkspace//" + authFileList.get(i));
				NodeList pageList = doc1.getElementsByTagName("authorization");
				addToList("pagename", pageList, listofPages);
				NodeList roleList = doc1.getElementsByTagName("role");
				addToList("name", roleList, listofRoles);
				NodeList systemList = doc1.getElementsByTagName("system");
				addToList("name", systemList, listofSytems);
			}
			List ignorePages = getConfigValuesAsList("pages.ignore");
			for (int k = 0; k < ignorePages.size(); k++) {
				String currentPage = (String) ignorePages.get(k);
				if (listofPages.contains(currentPage)) {
					listofPages.remove(currentPage);
				}
			}
			loadAuthorizations(); //Populates dataMap with Authorization objects.
			specialPages = getConfigValuesAsList("pages.special");
			for (int j = 0; j < specialPages.size(); j++) {
				String specialPage = (String) specialPages.get(j);
				if (listofPages.contains(specialPage)) {
					listofPages.remove(specialPage);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	  Function loadAuthorizations() populates dataMap with Authorization objects.
   */
	public void loadAuthorizations() {
		try {
			AuthorizationAccess au = AuthorizationAccess.Factory.getInstance();
			for (int p = 0; p < listofPages.size(); p++) {
				String page=(String)listofPages.get(p);
				Map authMap = new HashMap();
				authMap.put(AuthorizationAccess.PAGE_NAME, page);
				authMap.put(AuthorizationAccess.DESKTOP, "backoffice");
				authMap.put(AuthorizationAccess.SYSTEM, "nbaGroup");
				for (int r = 0; r < listofRoles.size(); r++) {
					String role=(String)listofRoles.get(r);
					authMap.put(AuthorizationAccess.ROLE, role);
					AuthorizationDetails auth = au.getAuthorizations(authMap);
					dataMap.put(page+'-'+role,new Authorization(auth.visibility, auth.enablement));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	/*
    * Function displayResult() is used to populate maps(specialValueMap,valueMap) and use that maps to display values to csv file.
    */  
	public String displayResult() throws Exception {
		StringBuffer sb = new StringBuffer();
		try{
			populateCsvMaps();
			//DISPLAY START
			for (int i = 0; i < listofRoles.size(); i++) {
				String role = (String) listofRoles.get(i);
				HashMap keyMap=(HashMap)valueMap.get(role);
				Iterator iterKeys = keyMap.keySet().iterator();
				HashMap specialkeyMap=(HashMap)specialValueMap.get(role);
				Iterator iterSpecialKeys = specialkeyMap.keySet().iterator();
				//HEADER START
				if(i == 0){
					sb.append("Roles,");
					//-------------------------WRITE BUTTON HEADINGS-----------------------------//
					while (iterKeys.hasNext()) {
						String val=(String)iterKeys.next();
						String pageName="";
						try{
							pageName=configProperties.getString("label." + val);
							sb.append(pageName);
							sb.append(",");
						} catch (MissingResourceException ex) {
							System.out.println(ex.getMessage());
							sb.append(val);
							sb.append(",");
						}
					}	
					//-------------------------WRITE TAB HEADINGS-----------------------------_//
					while (iterSpecialKeys.hasNext()) {
						String val=(String)iterSpecialKeys.next();
						String pageName="";
						try{
							pageName=configProperties.getString("label." + val);
							sb.append(pageName);
							sb.append(",");
						} catch (MissingResourceException ex) {
							System.out.println(ex.getMessage());
							sb.append(val);
							sb.append(",");
						}
					}
					sb.append("\n");
				}	
				//HEADER END
				
				//VALUE START
				//-------------------------WRITE BUTTON VALUES-----------------------------//
				iterKeys = keyMap.keySet().iterator();
				sb.append(role + ",");
				while (iterKeys.hasNext()) {
					String val=(String)iterKeys.next();
					String value=(String)keyMap.get(val);
					sb.append(value);
					sb.append(",");
				}
				//-------------------------WRITE TAB VALUES-----------------------------//
				iterSpecialKeys = specialkeyMap.keySet().iterator();
				while (iterSpecialKeys.hasNext()) {
					String val=(String)iterSpecialKeys.next();
					String value=(String)specialkeyMap.get(val);
					sb.append(value);
					sb.append(",");
				}
				sb.append("\n");
				//VALUE END
			}
			//DISPLAY END
			
		}	
		catch(Exception e){
			e.printStackTrace();
		}
		return sb.toString();
	} 
	/*
    * Function populateCsvMaps() is used to populate maps(specialValueMap,valueMap).
    */
	public void populateCsvMaps() {
		for (int i = 0; i < listofRoles.size(); i++) {
			String role = (String) listofRoles.get(i);
			Map keyMap = new HashMap();
			for (int j = 0; j < specialPages.size(); j++) {
				String page = (String) specialPages.get(j);
				Authorization auth = (Authorization) dataMap.get(page + '-' + role);
				keyMap.putAll(auth.populateSpecialData(page));
			}
			specialValueMap.put(role, keyMap);
		}
		for (int i = 0; i < listofRoles.size(); i++) {
			String role = (String) listofRoles.get(i);
			Map keyMap = new HashMap();
			for (int j = 0; j < listofPages.size(); j++) {
			 	String page = (String) listofPages.get(j);
				String configVal="";
				try{
				  configVal = configProperties.getString(page);
				}
				catch(MissingResourceException e){
					System.out.println(e.getMessage());
					configVal=page;
				}
				//Lookup for initial tab visibility in special pages map
				Map visibilityMap = (HashMap) specialValueMap.get(role);
				String visibility = (String) visibilityMap.get(configVal);
				if (visibility == null)
					visibility = "";
				//Display the rights according to the visibility of tab
				Authorization auth = (Authorization) dataMap.get(page + '-' + role);
				keyMap.putAll(auth.populateData(visibility, page));
			   
			}
			valueMap.put(role, keyMap);
		}
	}	
	 
	private List addToList(String attName, NodeList list,List currentList) {
		for (int j = 0; j < list.getLength(); j++) {
			Element element = (Element) list.item(j);
			if (element.hasAttributes()) {
				String atValue = element.getAttribute(attName) ;
				if (atValue != null && atValue.length() > 0 && !currentList.contains(atValue)) {
					currentList.add(atValue);
				}
			}
		 }
		return currentList;
	}

	public static List getConfigValuesAsList(String configEntry) {
		String configVals ="";
		if (configProperties != null && configEntry != null) {
			configVals=configProperties.getString(configEntry.trim());
		}
		List configValues = new ArrayList();
		if (configVals != null) {
			StringTokenizer strToken = new StringTokenizer(configVals, ",");
			while (strToken.hasMoreTokens()) {
				configValues.add(strToken.nextToken());
			}
		}
		return configValues;
	}
	
}

