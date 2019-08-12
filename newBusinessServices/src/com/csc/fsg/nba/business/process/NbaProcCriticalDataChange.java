package com.csc.fsg.nba.business.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.csc.fs.SystemAccess;
import com.csc.fs.accel.valueobject.WorkItem;
import com.csc.fs.accel.valueobject.WorkItemSource;
import com.csc.fs.svcloc.ServiceLocator;
import com.csc.fsg.nba.database.NbaCriticalDataChangeAccessor;
import com.csc.fsg.nba.exception.AxaErrorStatusException;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaLockedException;
import com.csc.fsg.nba.foundation.AxaStatusDefinitionConstants;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaAwdRetrieveOptionsVO;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaCriticalDataChangeVO;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaSource;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.NbaUserVO;

/**
 * NbaProcCriticalDataChange is the class that processes nbAccelerator cases that needs to 
 * be updated with some Critical Data deep through means data/LOBs should be updated for the transactions as well for the case Automatically. It reviews the NBA_CRITICAL_DATA_CHANGE table  to 
 * determine if the all the transaction of the case needs to be updated with the critical data that has been changed on case like FSNM,LSNM,DOB,SSN at any timestamp.  
 * <p>The NbaProcCriticalDataChange class extends the NbaAutomatedProcess class.  
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>APSL4067</td><td>Discretionary</td><td>Critical Data Change</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @see NbaAutomatedProcess
 */



public class NbaProcCriticalDataChange extends NbaAutomatedProcess{

	
	
		protected static final String CASE = "C";

		protected static final String CASE_SEARCH_VO = "NbaSearchResultVO";

		private static final String NOT_LOGGED_ON_ERROR = "SYS0111";// APSL3016-NBA283(Retrofit)
		
		protected static boolean appProcessed = true;

		/**
		 * This constructor calls the superclass constructor which will set
		 * the appropriate statues for the process.
		 */
		public NbaProcCriticalDataChange() {
			super();
		}

		/**
		 * This method drives the critical Data change process.
		 * <P>After obtaining a list of contracts that needs to be updated deep through 
		 * all the transactions by critical data(i.e. FSNM,LSNM,MINM,DOB and SSN),
		 * from the Database, the process searches that contract in the AWD
		 * and tries to get lock on the workitem and it's all transactions. If it is able
		 * to get the lock it takes the nbaTXlife object and get the updated lob values from the nbaCase 
		 * that needs to be updated on all transactions of the locked case. 
		 * @param user the user for whom the work was retrieved, in this case APCLSCHK.
		 * @param work the AWD case to be processed
		 * @return NbaAutomatedProcessResult containing the results of the process
		 * @throws NbaBaseException 
		 */
		public NbaAutomatedProcessResult executeProcess(NbaUserVO user, NbaDst work) throws NbaBaseException {
			NbaDst matchingCase = null;
			ArrayList contracts = new ArrayList();
			setUser(user);
			int batchSize = Integer.parseInt(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(
					NbaConfigurationConstants.CRITICAL_DATA_BATCH_SIZE));
			boolean continueDataChange = false; // PERF-APSL308
			NbaCriticalDataChangeVO criticalDataChangeVO = new NbaCriticalDataChangeVO();
			getLogger().logDebug("Execution of Critical Data Change Process Started");
			try {
				contracts = NbaCriticalDataChangeAccessor.selectCriticalDataCases();
			} catch (Exception exp) {
				getLogger().logError("Exeception occoured while retrieving list of contracts from Database -" + NbaUtils.getStackTrace(exp)); // PERF-APSL308
			}
			if (contracts.size() > 0) {
				for (int i = 0; i < contracts.size(); i++) {
					if (i >= (batchSize)) { // If counter reaches batch size
						getLogger().logDebug("Execution of Batch Ends");
						continueDataChange = true; // PERF-APSL308
						break;
					}
					criticalDataChangeVO = (NbaCriticalDataChangeVO) contracts.get(i);					
					try {						
						matchingCase = retrieveCaseAndTransactions(criticalDataChangeVO.getWorkItemId(), user); // get the case and it's all transactions
						doUdateProcess(matchingCase, criticalDataChangeVO);//Update all transactions of matchingCase
						NbaCriticalDataChangeAccessor.delete(criticalDataChangeVO);// delete record for the matching case from database after update
					} catch (NbaLockedException lockedExp) {
						getLogger().logError("Locking Exeception occurred while retrieving contract -" + criticalDataChangeVO.getContractKey());
						continue;
					} 
					if(!appProcessed){
						NbaCriticalDataChangeAccessor.updateCriticalDataIndicator(criticalDataChangeVO.getContractKey(), 1);// Set Critical Data Change indicator to '0' to make it exclude in next loop	
					}
					
				}
			}
			getLogger().logDebug("Execution of Critical Data Change Process Ends");
			if (continueDataChange) {
				return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.SUCCESSFUL, "", "");
			}
			// end PERF-APSL308
			return new NbaAutomatedProcessResult(NbaAutomatedProcessResult.NOWORK, "", "");
	}

		/**
		 * This method retrieves the matching case by the workitemID.
		 * Each case is retrieved and locked. If unable to retrieve with lock,
		 * this work item will be suspended for a brief period of time due to the auto
		 * suspend flag being set.
		 * 
		 * @param searchResults
		 *            the results of the previous AWD lookup
		 * @throws NbaBaseException
		 *             NbaLockException
		 */
		public NbaDst retrieveMatchingCase(NbaUserVO user, String workItemID) throws NbaBaseException {
			NbaDst aWorkItem = null;
			if (workItemID != null) {
				NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
				retOpt.setWorkItem(workItemID, true);
				//Code Deleted - Performance changes of SR534322 Retrofit 
				retOpt.setLockWorkItem();
				//Code Deleted - Performance changes of SR534322 Retrofit
				aWorkItem = retrieveWorkItem(user, retOpt);
			}
			return aWorkItem;
		}
		
		/**
		 * Retrieve the Case and Transactions associated
		 * @param  dst workItem / case, for which the sibiling transactions / child transactions need to be retrieved along with the case
		 * @param  user AWD user id  
		 * @param  locked lock indicator
		 * @return NbaDst containing the case and all the transactions
		 * @throws NbaBaseException
		 */
		
		protected NbaDst retrieveCaseAndTransactions(String workItemID, NbaUserVO user) throws NbaBaseException { // ALS5177
			NbaDst aWorkItem = null;
			if (workItemID != null) {
				NbaAwdRetrieveOptionsVO retOpt = new NbaAwdRetrieveOptionsVO();
				retOpt.setWorkItem(workItemID, true);
				retOpt.requestSources();
				retOpt.requestTransactionAsChild();				
				retOpt.setLockWorkItem();
				retOpt.setLockTransaction();
				retOpt.setNbaUserVO(user);
				aWorkItem = retrieveWorkItem(user, retOpt);				
			}
			return aWorkItem;
		}
		

		//Overloading parent method
		public void handleHostResponse(NbaTXLife aTXLifeResponse, boolean addAddComments) {
			if (aTXLifeResponse != null && aTXLifeResponse.isTransactionError()) {//NBA094
				setNbaTxLife(null);
				getLogger().logError("Error retrieving TxLife from database.");
			}
		}

		/**
		 * Logon the user. First logoff the user to clear any session data, then logon.
		 * @throws NbaBaseException
		 */
		//APSL3016-NBA283(Retrofit) New Method    
		protected void logon() throws NbaBaseException {
			SystemAccess sysAccess = (SystemAccess) ServiceLocator.lookup(SystemAccess.SERVICENAME);
			String system = null;
			try {
				system = NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.DEFAULT_SYSTEM);
			} catch (NbaBaseException exp) {
				getLogger().logException("Exception occured while reading external system from configuration : ", exp);
				throw exp;
			}
			sysAccess.logout(system);
			sysAccess.login(system);
		}

		/**
		 * Determine if the error is a not logged on condition
		 * @param whoops
		 * @return
		 */
		//APSL3016-NBA283(Retrofit) New Method 
		private boolean isNotLoggedOn(Exception whoops) {
			return whoops.getMessage() != null && whoops.getMessage().indexOf(NOT_LOGGED_ON_ERROR) > -1;
		}
		
		
		/**
		 * This Method processes the case on which Critical Data have been changed/updated. 
		 * @param matchingCase: Case picked from NBA_Critical_Data table
		 * @param criticalDataChangeVO: value object for Critical Data Change  
		 * @return void
		 * @Throws NbaBaseException
		 * **/
		protected void doUdateProcess(NbaDst matchingCase,NbaCriticalDataChangeVO criticalDataChangeVO) throws NbaBaseException{
			if (matchingCase != null) {
				String dataToBeChange = criticalDataChangeVO.getCriticalData();
				WorkItem workItem;
				NbaDst nbaDst=null;
				String[] lobsToUpdate;
				setWork(matchingCase);
				Iterator workItemItr = matchingCase.getTransactions().iterator();
				getLogger().logDebug("getting all transactions list for Contract: "+ criticalDataChangeVO.getContractKey());
				getLogger().logDebug("Lobs to change on each transaction: " + dataToBeChange);
				if (!NbaUtils.isBlankOrNull(dataToBeChange)) {
					lobsToUpdate = dataToBeChange.split("#"); // taking the array of all the lob names that have to be changed on individual transaction
					while (workItemItr.hasNext()) {
						workItem = (WorkItem) workItemItr.next();
						nbaDst = new NbaDst();
						nbaDst.setUserID(user.getUserID());
						nbaDst.setPassword(user.getPassword());
						nbaDst.addTransaction(workItem);
						if((A_WT_REQUIREMENT.equals(nbaDst.getWorkType()) || A_WT_MISC_MAIL.equals(nbaDst.getWorkType()) || A_WT_MISC_WORK.equals(nbaDst.getWorkType())) && (nbaDst.getNbaLob().getReqPersonCode() == NbaOliConstants.OLI_REL_INSURED)){
							updateNbaTransactionAndSource(lobsToUpdate, nbaDst, matchingCase);											
							nbaDst.setUpdate();										
							statusProvider = new NbaProcessStatusProvider(user, nbaDst);
							setStatusProvider(statusProvider);
							update(nbaDst);// Update current Workitem										
						}										
					}
				}
				try {
					unlockWork(matchingCase);
				} catch (NbaBaseException ex) {
					appProcessed = false;
					throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, criticalDataChangeVO.getContractKey());
				}
			}			
		}
		
		/**
		 * This Method gets the associated Sources for the given transaction of a case on which Critical Data have been changed/updated. 
		 * @param lobsToUpdate: Contains the list of lobs which have been changed on the case
		 * @param lob: NbaLob object of transaction or case that has to be updated 
		 * @param mainApplWI: main case on which Critical data has been updated.
		 * @return void
		 * @throws NbaBaseException 
		 * **/
		protected void updateNbaTransactionAndSource(String[] lobsToUpdate, NbaDst nbaDst, NbaDst mainApplWI) throws NbaBaseException{
			List sourceList = nbaDst.getNbaSources();
			Iterator sourceItr;
			try{
			if (sourceList.size() > 0) {
				sourceItr = sourceList.iterator();
				NbaSource nbaSource;
				getLogger().logDebug("getting source for current transaction, to update LOBs on it. ");
				while (sourceItr.hasNext()) {
					nbaSource = (NbaSource) sourceItr.next();
					updateLobValues(lobsToUpdate, nbaSource.getNbaLob(), mainApplWI);// updating source lobs
					nbaSource.setUpdate();
				}				
			}
			updateLobValues(lobsToUpdate, nbaDst.getNbaLob(), mainApplWI);// updating transaction's lobs
			}catch (NbaBaseException ex) {
				appProcessed = false;
				throw new AxaErrorStatusException(AxaStatusDefinitionConstants.VARIANCE_KEY_TECH, nbaDst.getID());					
			}
		}
		
		/**
		 * This Method updates the Lobs(Critical Data that have been updated on a case) in NbaDst Object it may be NbaSource/NbaTransaction.
		 * @param lobsToUpdate: Contains the list of lobs which have been changed on the case
		 * @param lob: NbaLob object of transaction or case that has to be updated 
		 * @param mainApplWI: main case on which Critical data has been updated.
		 * @return void
		 * @throws NbaBaseException 
		 * **/
		protected void updateLobValues(String[] lobsToUpdate, NbaLob lob, NbaDst mainApplWI) throws NbaBaseException {
			NbaLob mainAppWIlob = mainApplWI.getNbaLob();
			for (int i = 0; i < lobsToUpdate.length; i++) {
				if (NbaLob.A_LOB_FIRST_NAME.equalsIgnoreCase(lobsToUpdate[i])) {
					if (!mainAppWIlob.getFirstName().equalsIgnoreCase(lob.getFirstName())) {
						lob.setFirstName(mainAppWIlob.getFirstName());
					}
				} else if (NbaLob.A_LOB_LAST_NAME.equalsIgnoreCase(lobsToUpdate[i])) {
					if (!mainAppWIlob.getLastName().equalsIgnoreCase(lob.getLastName())) {
						lob.setLastName(mainAppWIlob.getLastName());
					}
				} else if (NbaLob.A_LOB_MIDDLE_INITIAL.equalsIgnoreCase(lobsToUpdate[i])) {
					if (!mainAppWIlob.getMiddleInitial().equalsIgnoreCase(lob.getMiddleInitial())) {
						lob.setMiddleInitial(mainAppWIlob.getMiddleInitial());
					}
				} else if (NbaLob.A_LOB_SSN_TIN.equalsIgnoreCase(lobsToUpdate[i])) {
					if (!mainAppWIlob.getSsnTin().equalsIgnoreCase(lob.getSsnTin())) {
						lob.setSsnTin(mainAppWIlob.getSsnTin());
					}
				} else if (NbaLob.A_LOB_DOB.equalsIgnoreCase(lobsToUpdate[i])) {
					if (!mainAppWIlob.getDOB().equals(lob.getDOB())) {
						lob.setDOB(mainAppWIlob.getDOB());
					}
				}
			}
		}

	
}
