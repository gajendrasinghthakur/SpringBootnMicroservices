package com.csc.fsg.nba.servlet;


import java.io.FileInputStream;
import java.io.FileWriter;
import java.rmi.RemoteException;

import com.csc.fsg.nba.contract.validation.NbaContractValidation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaTXRequestVO;
import com.csc.fsg.nba.vo.NbaUserVO;
import com.csc.fsg.nba.vo.txlife.Holding;
import com.csc.fsg.nba.vo.txlife.SourceInfo;
import com.csc.fsg.nba.vo.txlife.SystemMessage;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
/**
* TestNbaContractValidation is a test for nbA contract validation.
*  
* Five arguments are required for processing:
* - a backend system
* - a company code
* - a Contract number which will be used to read the contract fom the appropriate data source
* - a user id
* - a Y/N value to re-parse NbaConfiguration.xml 
*   
* Example 
* VNTG VCS NVU00000000103V APAPPSUB Y
* 
* The default mode of TestNbaContractValidation reads the contract from the backend system/database
* by invoking NbaContractAccessFacade. An optional sixth parameter will instead read the contract
* from the file location identified in the parameter. Note, the "" around the file location
* must be present.
*   
* Example using NbaContractAccessFacade to read the contract:
* CLIF 00 KEN0329007 APAUUND Y 
* 
* Example reading the contract from a the file location:
* CLIF 00 XXXXXX APAPPSUB Y "C:\nbademo\nbA_CLIF_Test XML Stream_NBCL001_SPDA.xml"
* 
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA064</td><td>Version 3</td><td>Contract Validation</td></tr>
* <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
* @version 3.0.0
* @since New Business Accelerator - Version 3
*/
public class TestNbaContractValidation{ //extends TestNbaAutomatedProcess {
	private static com.csc.fsg.nba.foundation.NbaLogger logger = null;
	protected NbaUserVO nbaUserVO;
	protected NbaTXRequestVO nbaTXRequestVO;
	protected NbaDst nbaDst;
	protected static String contract;
	protected static String backendSystem;
	protected static String company;
	protected static String user;
	protected static String password;
	protected static String workItemId;
	protected static boolean parseConfig;
	protected static String fileloc = "";
	//private NbaContractAccessFacade cafEjb = null;
	/*private NbaContractAccessFacade nbaContractAccessFacade = null;
	private NbaNetServerAccessor nsa = null;*/
	private NbaTXRequestVO nbaTXRequest;
	/*public static Test suite() {
		//$JUnit-BEGIN$
		TestSuite suite = new TestSuite(TestNbaContractValidation.class);
		//$JUnit-END$
		return suite;
	}*/
	/**
	* TestNbaContractValidation constructor comment.
	* @param name java.lang.String
	*/
	public TestNbaContractValidation(String name) {
		//super(name);
	}
	/**
	* Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	* @return com.csc.fsg.nba.foundation.NbaLogger
	*/
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(TestNbaContractValidation.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("TestNbaContractValidation could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	/*protected void initializeEJB() throws NbaBaseException {
		if (cafEjb == null) {
			try {
				NbaServiceLocator sl = NbaServiceLocator.getInstance();
				javax.ejb.EJBHome home = null;
				home = sl.getHome(NbaServiceLocator.JNDI_CONTRACT_ACCESS_FACADE_BEAN);
				NbaContractAccessFacadeHome cafHome = (NbaContractAccessFacadeHome) home;
				cafEjb = cafHome.create();
			} catch (RemoteException re) {
				throw new NbaBaseException(re);
			} catch (CreateException ce) {
				throw new NbaBaseException(ce);
			}
		}
	}*/
	/**
	* Runs the test case.
	* @param args an array of command-line arguments
	*/
	public static void main(java.lang.String[] args) {
		if (args.length >= 5) {
			setBackendSystem(args[0]);
			setCompany(args[1]);
			setContract(args[2]);
			setUser(args[3]);
			setParseConfig(args[4].equalsIgnoreCase("Y"));
			if (args.length > 5) {
				setFileloc(args[5]);
			}
			//junit.textui.TestRunner.run(suite());
		} else {
			getLogger().logError("Expected 5 arguments, found " + args.length);
			getLogger().logError(
				"Program arguments missing: Backend, Company, Contract, User, Y/N  (Y/N = re-parse NbaConfiguration.xml to reload validation processes)");
		}
	}
	/**
	 * Sets up the test fixture.
	 * Tests the method to get a <code>Class</code> for a particular name.
	 *
	 * Called before every test case method.
	 * @exception java.lang.ClassNotFoundException No definition for the class with the specifed name could be found.
	 */
	/*protected void setUp() {
		if (cafEjb == null) {
			try {
				initializeEJB();
			} catch (NbaBaseException nbe) {
				getLogger().logError("Unable to initialize EJB");
			}
		}
		if (isParseConfig()) {
			getLogger().logDebug("NbaConfigurationParser starting");
			try {
				NbaConfiguration.getInstance(); //ACN012
			} catch (Exception e) {
				getLogger().logDebug(e);
			}
			getLogger().logDebug("NbaConfigurationParser ending");
		}
	}*/
	/**
	 * Tears down the test fixture.
	 *
	 * Called after every test case method.
	 */
	protected void tearDown() {
	}
	/**
	* Invoke com.csc.fsg.nba.contract.validation.NbaContractValidation to perform contact
	* validation for App Submit.
	*/
	public String testContractValidation(String fileName) throws Exception {
		try {
			//setUp();
			if(fileName==null || fileName.trim().length()==0){
				return null;
			}
			String path = "C:\\AXALIFE\\";
			
			NbaTXLife nbaTXLife = new NbaTXLife(TestServletUtils.editXML(path + fileName));
			nbaTXLife.setBusinessProcess("A2APPSUB");
			//nbaTXLife.setBusinessProcess("UWAPPLYCWA");
			setBackendSystem("CAPS");
			/*NbaTXLife nbaTXLife;
			if (isReadFromFile()) {
				//Read NbaTXLife from file
				nbaTXLife = getHoldingInquiryFromFile();
			} else {
				//Read NbaTXLife from database
				NbaTXLife tempNbaTXLife = new NbaTXLife(getInquiryNbaTXRequest());
				nbaTXLife = cafEjb.doContractInquiry(getReadNbaTXRequest(getNbaDst(tempNbaTXLife)));
				handleHostResponse(nbaTXLife);
			}*/
			//Validate
			//FileWriter fr;
			//fr = new FileWriter("C:\\IOF\\input.xml");
			//fr.write(nbaTXLife.toXmlString());
			//fr.close();
			new NbaContractValidation().validate(nbaTXLife, getNbaDst(nbaTXLife), getNbaUserVO());
			Holding holding = nbaTXLife.getPrimaryHolding();
			int systemMessageCount = holding.getSystemMessageCount();
			for (int msgIdx = systemMessageCount - 1; msgIdx > -1; msgIdx--) {
				SystemMessage systemMessage = holding.getSystemMessageAt(msgIdx);
				if (systemMessage.isActionDelete()) {
					holding.removeSystemMessageAt(msgIdx);
				}
			}			
			FileWriter fr = new FileWriter(path +"output_"+fileName);
			fr.write(nbaTXLife.toXmlString());
			fr.close();
			return nbaTXLife.toXmlString();
		} catch (RemoteException e) {
			getLogger().logError(e.getMessage());
			e.printStackTrace();
		} catch (NbaBaseException e) {
			getLogger().logError(e.getMessage());
		} catch (Throwable e) {
			getLogger().logError(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * Returns the nbaDst.
	 * @return NbaDst
	 */
	protected NbaDst getNbaDst(NbaTXLife nbaTxLife) throws Exception {		 
			nbaDst = new NbaDst();
			nbaDst.initForTestMode();
			nbaDst.getNbaLob().setCompany(getCompany());
			nbaDst.getNbaLob().setPolicyNumber(getContract());
			nbaDst.getNbaLob().setBackendSystem(getBackendSystem());
			nbaDst.updateLobFromNbaTxLife(nbaTxLife);		 
		return nbaDst;
	}
	/**
	 * Returns the nbaUserVO.
	 * @return NbaUserVO
	 */
	protected NbaUserVO getNbaUserVO() throws Exception {
		if (nbaUserVO == null) {
			setNbaUserVO(new NbaUserVO());
			nbaUserVO.setUserID(getUser());
			nbaUserVO.setPassword("");
		}
		return nbaUserVO;
	}
	/**
	 * Sets the userVO.
	 * @param userVO The userVO to set
	 */
	protected void setNbaUserVO(NbaUserVO userVO) {
		this.nbaUserVO = userVO;
	}
	/**
	 * Returns the contract.
	 * @return String
	 */
	protected String getContract() {
		return contract;
	}
	/**
	 * Sets the contract.
	 * @param contract The contract to set
	 */
	protected static void setContract(String aContract) {
		contract = aContract;
	}
	/**
	 * Returns the user.
	 * @return String
	 */
	protected String getUser() {
		return user;
	}
	/**
	 * Sets the user.
	 * @param user The user to set
	 */
	protected static void setUser(String aUser) {
		user = aUser;
	}
	/*protected NbaContractAccessFacade getNbaContractAccessFacade() throws NbaBaseException {
		if (nbaContractAccessFacade == null) {
			try {
				NbaServiceLocator sl = NbaServiceLocator.getInstance();
				javax.ejb.EJBHome home = null;
				home = sl.getHome(NbaServiceLocator.JNDI_CONTRACT_ACCESS_FACADE_BEAN);
				NbaContractAccessFacadeHome cafHome = (NbaContractAccessFacadeHome) home;
				nbaContractAccessFacade = cafHome.create();
			} catch (RemoteException re) {
				throw new NbaBaseException(re);
			} catch (CreateException ce) {
				throw new NbaBaseException(ce);
			}
		}
		return nbaContractAccessFacade;
	}
	*//**
	* Returns the nsa.
	* @return NbaNetServerAccessor
	*//*
	protected NbaNetServerAccessor getNsa() throws Exception {
		if (nsa == null) {
			NbaServiceLocator sl = NbaServiceLocator.getInstance();
			javax.ejb.EJBHome home = null;
			home = sl.getHome(NbaServiceLocator.JNDI_NETSERVER_ACCESSOR_BEAN);
			NbaNetServerAccessorHome nsaHome = (NbaNetServerAccessorHome) home;
			setNsa(nsaHome.create());
		}
		return nsa;
	}
	*//**
	 * Sets the nsa.
	 * @param nsa The nsa to set
	 *//*
	protected void setNsa(NbaNetServerAccessor nsa) {
		this.nsa = nsa;
	}*/
	/**
	 * Returns the parseConfig.
	 * @return boolean
	 */
	protected static boolean isParseConfig() {
		return parseConfig;
	}
	/**
	 * Sets the parseConfig.
	 * @param parseConfig The parseConfig to set
	 */
	protected static void setParseConfig(boolean parseConfig) {
		TestNbaContractValidation.parseConfig = parseConfig;
	}
	/**
	 * Returns the nbaTXRequest for Inquiry.
	 * @return NbaTXRequestVO
	 */
	protected NbaTXRequestVO getInquiryNbaTXRequest() throws Exception {
		if (nbaTXRequest == null) {
			nbaTXRequest = new NbaTXRequestVO();
			nbaTXRequest.setAccessIntent(NbaConstants.READ);
			nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_NEWBUSSUBMISSION);
			nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
			//nbaTXRequest.setNbaLob(getLob());
			nbaTXRequest.getNbaLob().setCompany(getCompany());
			nbaTXRequest.getNbaLob().setPolicyNumber(getContract());
			nbaTXRequest.getNbaLob().setBackendSystem(getBackendSystem());
			nbaTXRequest.setNbaUser(getNbaUserVO());
			nbaTXRequest.setBusinessProcess(getUser());
		}
		return nbaTXRequest;
	}
	/**
	 * Returns the nbaTXRequest for Inquiry.
	 * @return NbaTXRequestVO
	 */
	protected NbaTXRequestVO getReadNbaTXRequest(NbaDst nbaDst) throws Exception {
		if (nbaTXRequest == null) {
			nbaTXRequest = new NbaTXRequestVO();
		}
		nbaTXRequest.setAccessIntent(NbaConstants.READ);
		nbaTXRequest.setTransType(NbaOliConstants.TC_TYPE_HOLDINGINQ);
		nbaTXRequest.setTransMode(NbaOliConstants.TC_MODE_ORIGINAL);
		nbaTXRequest.setInquiryLevel(NbaOliConstants.TC_INQLVL_OBJECTALL);
		nbaTXRequest.setNbaLob(nbaDst.getNbaLob());
		nbaTXRequest.setNbaUser(getNbaUserVO());
		return nbaTXRequest;
	}
	/**
	 * Returns the lob.
	 * @return NbaLob
	 */
	/*protected NbaLob getLob() {
		if (lob == null) {
			setLob(new NbaLob());
		}
		return lob;
	}*/
	/**
	 * Sets the lob.
	 * @param lob The lob to set
	 */
	/*protected void setLob(NbaLob lob) {
		this.lob = lob;
	}*/
	/**
	 * Handle the NbaTXLife inquiry response .
	 * @param nbaTXLife the NbaTXLife response
	 */
	protected void handleHostResponse(NbaTXLife nbaTXLife) throws NbaBaseException {
		UserAuthResponseAndTXLifeResponseAndTXLifeNotify allResponses = nbaTXLife.getTXLife().getUserAuthResponseAndTXLifeResponseAndTXLifeNotify();
		if (allResponses != null && allResponses.getTXLifeResponseCount() > 0) {
			TXLifeResponse theResponse = allResponses.getTXLifeResponseAt(0);
			TransResult aTransResult = theResponse.getTransResult();
			if (aTransResult.getResultCode() > 1) {
				String errMsg = "Read failed, reason = ";
				for (int i = 0; i < aTransResult.getResultInfoCount(); i++) {
					errMsg = errMsg + " " + aTransResult.getResultInfoAt(i).getResultInfoDesc();
				}
				throw new NbaBaseException(errMsg);
			}
		}
	}
	/**
	 * Returns the value object representation of an ACORD holding inquiry.
	 * The source is the sample XML file idenrified by the fourth input parameter
	 * @return A sample holding inquiry as if from a back-end system.
	 */
	protected NbaTXLife getHoldingInquiryFromFile() throws NbaBaseException {
		try {
			NbaTXLife nbaTXLife = null;
			nbaTXLife = new NbaTXLife(new FileInputStream(getFileloc()));
			nbaTXLife.getPrimaryHolding().getPolicy().setPolNumber(getContract());
			SourceInfo sourceInfo;
			if (nbaTXLife.getOLifE().hasSourceInfo()) {
				sourceInfo = nbaTXLife.getOLifE().getSourceInfo();
			} else {
				sourceInfo = new SourceInfo();
				nbaTXLife.getOLifE().setSourceInfo(sourceInfo);
			}
			sourceInfo.setSourceInfoName("nbA");
			sourceInfo.setFileControlID(getBackendSystem());
			nbaTXLife.setAccessIntent(NbaConstants.READ);
			nbaTXLife.setBusinessProcess(getUser());
			nbaTXLife.doXMLMarkUp();
			//
			// Reset ids now so that database and breeze ids match
			new NbaOLifEId(nbaTXLife).resetIds(nbaTXLife);
			return nbaTXLife;
		} catch (Exception e) {
			throw new NbaBaseException("Unable to create NbaTXLife from " + getFileloc(), e);
		}
	}
	/**
	 * @return
	 */
	protected static String getFileloc() {
		return fileloc;
	}
	/**
	 * @param string
	 */
	protected static void setFileloc(String string) {
		fileloc = string;
	}
	/**
	 * @param string
	 */
	protected boolean isReadFromFile() {
		return getFileloc().length() > 0;
	}
	/**
	 * 
	 * @return
	 */
	protected static String getBackendSystem() {
		return backendSystem;
	}
	/**
	 * 
	 * @return
	 */
	protected static String getCompany() {
		return company;
	}
	/**
	 * 
	 * @param string
	 */
	protected static void setBackendSystem(String string) {
		backendSystem = string;
	}
	/**
	 * 
	 * @param string
	 */
	protected static void setCompany(String string) {
		company = string;
	}
	
	protected void editXML(String originalXml) throws Exception {
		String modifiedXml = "";
		int beginIndex = 0;
		int matchIndex = originalXml.indexOf("<?xml");
		while (matchIndex != -1) {
			modifiedXml += originalXml.substring(beginIndex, matchIndex - 1);
			modifiedXml += "<![CDATA[";
			int attBeginIndex = originalXml.indexOf("?>", matchIndex) + 2;
			int attEndIndex = originalXml.indexOf("</TXLife>", attBeginIndex) + 9;
			modifiedXml += originalXml.substring(attBeginIndex, attEndIndex);
			modifiedXml += "]]>";
			
			beginIndex = attEndIndex + 1;
			matchIndex = originalXml.indexOf("<?xml", beginIndex);
		}
			
		modifiedXml += originalXml.substring(beginIndex);
		
		modifiedXml = modifiedXml.replaceAll("Height & Weight", "Height And Weight");
		System.out.println(modifiedXml);
	}
}
