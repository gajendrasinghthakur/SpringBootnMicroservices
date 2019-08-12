package com.csc.fsg.nba.business.process;
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
 * 
 * *******************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaOLifEId;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.ac.AcceptableImpairments;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.ImpairmentMessages;
import com.csc.fsg.nba.vo.NbaAcdb;

/**
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>ACN016</td><td>Version 4</td><td>Problems and Requirements Merging</td></tr>
 * <tr><td>SPR2466</td><td>Version 6</td><td>Matching Impairments based on the ACORD Type code is not done for Impairments mentioned in Appendix A.</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up</td></tr>
 * <tr><td>AXAL3.7.07</td><td>AXA Life Phase 1</td><td>Auto Underwriting</td></tr>
 * <tr><td>NBA224</td><td>Version 8</td><td>nbA Underwriter Workbench Requirements and Impairments Enhancement Project</td></tr>
 * <tr><td>ALS2840</td><td>AXA Life Phase 1</td><td>Unless otherwise noted, retrict code should be true</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4
 */
public class NbaImpairmentMerger implements com.csc.fsg.nba.foundation.NbaOliConstants {
    // begin SPR3290
	protected static long lStatusPending = 1000500003;
	protected static long lStatusResolved = 1000500006;
	protected static long lStatusTrivial = 1000500009;  //AXAL3.7.07
	protected static Map htImpToBeComb = new Hashtable(7, 1); //SPR2466
	protected static ArrayList arrMedMsgs = new ArrayList();
	protected static ArrayList arrImpForCon = new ArrayList();
	protected static ArrayList arrImpForReq = new ArrayList(); //AXAL3.7.07
	protected static ArrayList arrImpForSrvRslt = new ArrayList();//SR564247(APSL2525)-Full
	protected static String IMPAIRMENT_CLASS_ONE = "1"; //SPR2466
	protected static String IMPAIRMENT_CLASS_EIGHT = "8";//SPR2466
	protected static String IMPAIRMENT_CLASS_ZERO = "0"; //SPR2466
	protected String processId = "Unknown";
	protected String vpmsModelSource = new String();
	protected static String IMPAIRMENT_CLASS_FIVE = "5"; //AXAL3.7.07
	// end SPR3290
	static {
        //SPR2466 begin
        htImpToBeComb.put(new Long(NbaOliConstants.OLI_MEDCOND_R82), "ABNORMAL FINDINGS IN URINE");
        htImpToBeComb.put(new Long(NbaOliConstants.OLI_MEDCOND_N19), "ABNORMAL KIDNEY FUNCTION TESTS");
        htImpToBeComb.put(new Long(NbaOliConstants.OLI_MEDCOND_D84), "ABNORMAL LABORATORY TESTS");
        htImpToBeComb.put(new Long(NbaOliConstants.OLI_MEDCOND_D75), "ABNORMAL LIPIDS");
        htImpToBeComb.put(new Long(NbaOliConstants.OLI_TESTTYPE_LIVER), "ABNORMAL LIVER FUNCTION TESTS");
        htImpToBeComb.put(new Long(NbaOliConstants.OLI_MEDCOND_E14), "ABNORMAL TESTS FOR DIABETES");
        htImpToBeComb.put(new Long(NbaOliConstants.OLI_MEDCOND_R31), "LAB TESTS INDICATE HEMATURIA");
        //SPR2466 end
        arrMedMsgs.add(0, "Medication(s)");
        arrMedMsgs.add(1, "Future Surgery(s)");
        arrMedMsgs.add(2, "Medical Test(s)");
        arrMedMsgs.add(3, "Hospital Visit(s)");
        arrMedMsgs.add(4, "Treatment(s)");
        arrImpForCon.add(0, NbaConstants.FINANCIAL_SRC);
        arrImpForCon.add(1, NbaConstants.BUYSELL_SRC);
        arrImpForCon.add(2, NbaConstants.KEYPERSON_SRC);
        arrImpForCon.add(3, NbaConstants.BENEOWNERREL_SRC);
        arrImpForCon.add(4, NbaConstants.PREFERRED_SRC);
        arrImpForCon.add(5, NbaConstants.OCCUPATION_SRC);
        // begin AXAL3.7.07
        arrImpForCon.add(6, NbaConstants.NONMEDICALHISTORY_SRC);
        arrImpForCon.add(7, NbaConstants.HEIGHTWEIGHTEVAL_SRC);  
        arrImpForReq.add(0, NbaConstants.HEIGHTWEIGHTEVAL_SRC);  
        arrImpForReq.add(1, NbaConstants.LABBLOODPRESSURE_SRC);  
        // end AXAL3.7.07
        arrImpForReq.add(2,NbaConstants.PREDICTIVEIMPAIRMENTS_SRC);//SR564247(APSL2525) -Full
        arrImpForSrvRslt.add(0,NbaConstants.PREDICTIVEIMPAIRMENTS_SRC);//SR564247(APSL2525) -Full
    }
		
	/**
	 * No arg constructor.
	 */
	public NbaImpairmentMerger(){	
	}
	
	/**
	 * One argument constructor.
	 * @param process
	 */
	public NbaImpairmentMerger(String process){
		processId = process;
	}
	
	
	/**
     * This method is responsible for matching the impairments based on Description and Acord Code and returns the matched index
     * @param existingImps
     * @param newImp
     * @return
     */
    public int matchImpairments(ArrayList existingImps, ArrayList newImps, ImpairmentInfo newImpProcessed, NbaTXLife nbaTxLife,
            int indexImpInfoProcessed) {
        int impSize = existingImps.size(); //SPR2466
        //Determines if the new impairment matches any of the existing impairment.
        //Returns index of the matched existing impairment.
        int matchWithExisting = matchAgainstExistingImpairment(existingImps, newImpProcessed);
        
        //No match found with existing impairment. Search for only impairment code.
        //SPR2466 begin
        if (matchWithExisting == -1) {
            for (int i = 0; i < impSize; i++) {
                ImpairmentInfo compareToImp = (ImpairmentInfo) existingImps.get(i);
                if (eligibleForImpCodeMatch(compareToImp, newImpProcessed)) {
                    String impType = compareToImp.getImpairmentType();
                    if (IMPAIRMENT_CLASS_ONE.equals(compareToImp.getImpairmentClass())) {
                        if (htImpToBeComb.containsKey(impType)) {
                            compareToImp.setDescription((String) htImpToBeComb.get(impType));
                            matchWithExisting = i;
                        }
                    } else if (IMPAIRMENT_CLASS_EIGHT.equals(compareToImp.getImpairmentClass())
                            && (NbaOliConstants.OLI_TESTTYPE_LIVER == NbaUtils.convertStringToLong(impType))) {
                        compareToImp.setDescription((String) htImpToBeComb.get(impType));
                        matchWithExisting = i;
                    }
                }
            }
        }

        return matchWithExisting;
        //SPR2466 end
    }
	
	
	/**
     * Matches the new impairment against old impairments.
     * @param existingImps
     * @param newImp
     * @return
     */
	public int matchAgainstExistingImpairment(ArrayList existingImps, ImpairmentInfo newImp){
		for (int j = 0; j < existingImps.size(); j++) { //ALS4509
			ImpairmentInfo existImp = (ImpairmentInfo) existingImps.get(j);
			if (newImp.getDescription() != null
				&& existImp.getDescription() != null
				&& newImp.getImpairmentType() != null
				&& existImp.getImpairmentType() != null
				&& newImp.getDescription().equalsIgnoreCase(existImp.getDescription()) 
				&& newImp.getImpairmentType().equals(existImp.getImpairmentType()))
				return j;
		}
		return -1;		
	}
	/**
	 * This method is responsible for matching the non-medical impairments messages	
	 * @param existingImp
	 * @param newImp
	 * @return
	 */
	public boolean newMessagesExist(ImpairmentInfo existingImp, ImpairmentInfo newImp, NbaTXLife nbaTxLife)
		throws IndexOutOfBoundsException, NbaBaseException {
		boolean allExistFlag = false;
		
		/*
		 if (existingImp.getImpairmentSource() != null
			&& newImp.getImpairmentSource() != null
			&& existingImp.getImpairmentSource().equalsIgnoreCase("MedicalScreening")
			&& newImp.getImpairmentSource().equalsIgnoreCase("MedicalScreening"))
			return matchMedicalMessages(existingImp, newImp, nbaTxLife);
		*/
		if (existingImp.getImpairmentMessagesCount() == 0 && newImp.getImpairmentMessagesCount() > 0) {
			addNewMessage(existingImp, newImp.getImpairmentMessagesAt(0), existingImp.getImpairmentMessagesCount() - 1, nbaTxLife);
		} else if (existingImp.getImpairmentMessagesCount() > 0 && newImp.getImpairmentMessagesCount() == 0) {
			return allExistFlag;
		}
		if (existingImp.getImpairmentMessagesCount() >= 1 && newImp.getImpairmentMessagesCount() >= 1) {
			top : for (int i = 0; i < newImp.getImpairmentMessagesCount(); i++) {
				for (int j = 0; j < existingImp.getImpairmentMessagesCount(); j++) {
					if (!compareMsgString(existingImp.getImpairmentMessagesAt(j), newImp.getImpairmentMessagesAt(i))) {
						continue top;
					}
				}
				addNewMessage(existingImp, newImp.getImpairmentMessagesAt(i), existingImp.getImpairmentMessagesCount() - 1, nbaTxLife);
				allExistFlag = true;
			}
		}
		return allExistFlag;
	}
	/**
	 * This method is responsible for sorting the medical impairments messages 	
	 * @param newImp
	 */
	private void sortMessages(ImpairmentInfo newImp) {
		ArrayList arrUnsort = newImp.getImpairmentMessages();
		ArrayList arrSort = new ArrayList();
		boolean addFlag = false;
		top : for (int i = 0; i < arrMedMsgs.size(); i++) {
			for (int j = 0; j < arrUnsort.size(); j++) {
				if (((ImpairmentMessages) arrUnsort.get(j)).getImpairmentMessageText().equals(arrMedMsgs.get(i))) {
					arrSort.add(arrUnsort.get(j));
					addFlag = true;
					continue;
				}
				if (addFlag) {
					if (arrMedMsgs.contains(((ImpairmentMessages) arrUnsort.get(j)).getImpairmentMessageText())) {
						addFlag = false;
						continue top;
					} else
						arrSort.add(arrUnsort.get(j));
				}
			}
			addFlag = false;
		}
		newImp.setImpairmentMessages(arrSort);
	}
	/**
	 * This method is responsible for matching the Medical impairments 
	 * @param existingImp
	 * @param newImp
	 * @return
	 */
	public boolean matchMedicalMessages(ImpairmentInfo existingImp, ImpairmentInfo newImp, NbaTXLife nbaTxLife)
		throws IndexOutOfBoundsException, NbaBaseException {
		boolean findFlag = false;
		boolean allExistFlag = false;
		sortMessages(newImp);
		if (existingImp.getImpairmentMessagesCount() == 0) {
			addNewMedMessageForNoPrior(existingImp, newImp, 0);
		} else if (existingImp.getImpairmentMessagesCount() > 0 && newImp.getImpairmentMessagesCount() == 0) {
			return allExistFlag;
		}
		if (existingImp.getImpairmentMessagesCount() >= 1 && newImp.getImpairmentMessagesCount() >= 1) {
			top : for (int i = 0; i < newImp.getImpairmentMessagesCount(); i++) {
				//	existingImp.toXml(System.out);
				for (int j = 0; j < existingImp.getImpairmentMessagesCount(); j++) {
					if (arrMedMsgs.contains(newImp.getImpairmentMessagesAt(i).getImpairmentMessageText())
						&& newImp.getImpairmentMessagesAt(i).getImpairmentMessageText().equals(
							existingImp.getImpairmentMessagesAt(j).getImpairmentMessageText())) {
						findFlag = true;
						if ((i + 1) < newImp.getImpairmentMessagesCount())
							i++;
						else
							break top;
						continue;
					} else if (arrMedMsgs.contains(newImp.getImpairmentMessagesAt(i).getImpairmentMessageText())) {
						findFlag = false;
					}
					if (findFlag) {
						if (compareMsgString(existingImp.getImpairmentMessagesAt(j), newImp.getImpairmentMessagesAt(i))) {
							addNewMessage(existingImp, newImp.getImpairmentMessagesAt(i), j, nbaTxLife);
							j++;
							allExistFlag = true;
						}
						if ((i + 1) < newImp.getImpairmentMessagesCount())
							i++;
						else
							break top;
					}
				}
				int tempInd = addNewMedMessage(existingImp, newImp, i);
				allExistFlag = true;
				if ((i + tempInd - 1) < newImp.getImpairmentMessagesCount())
					i = i + tempInd - 1;
				else
					break top;
				findFlag = false;
			}
		}
		return allExistFlag;
	}
	/**
	 * This method is responsible for adding the new medical impairment message
	 * @param existingImp
	 * @param newImp
	 * @param i
	 */
	private void addNewMedMessageForNoPrior(ImpairmentInfo existingImp, ImpairmentInfo newImp, int in) {
		ArrayList arrNewImp = newImp.getImpairmentMessages();
		ArrayList arrExistImp = existingImp.getImpairmentMessages();
		if (arrExistImp.size() == 0) {
			ImpairmentMessages impMsg = (ImpairmentMessages) arrNewImp.get(0);
			arrExistImp.add(0, impMsg);
		}
		for (int i = 1; i < arrNewImp.size(); i++) {
			if (!(arrMedMsgs.contains(((ImpairmentMessages) arrNewImp.get(i)).getImpairmentMessageText()))) {
				ImpairmentMessages impMsg = (ImpairmentMessages) arrNewImp.get(i);
				arrExistImp.add(i, impMsg);
			} else
				return;
		}
	}
	/**
	 * This method is responsible for adding the new medical impairment message
	 * @param existingImp
	 * @param newImp
	 */
	private int addNewMedMessage(ImpairmentInfo existingImp, ImpairmentInfo newImp, int index) {
		int indexToAdd = -1, indexToGet = -1;
		String nextDescriptionToBreak = "";
		for (int i = 0; i < arrMedMsgs.size(); i++) {
			if (newImp.getImpairmentMessagesAt(index).getImpairmentMessageText().equals(arrMedMsgs.get(i))) {
				indexToGet = i;
				if ((i + 1) < arrMedMsgs.size())
					nextDescriptionToBreak = (String) arrMedMsgs.get(i + 1);
				break;
			}
		}
		int j = 0;
		for (int i = 0; i < existingImp.getImpairmentMessagesCount(); i++) {
			if (existingImp.getImpairmentMessagesAt(i).getImpairmentMessageText().equals(arrMedMsgs.get(j)) && indexToGet > j) {
				indexToAdd = i;
				j++;
			}
		}
		int returnCount = 0;
		ArrayList arrNewImp = newImp.getImpairmentMessages();
		ArrayList arrExistImp = existingImp.getImpairmentMessages();
		top : for (int i = index; i < arrNewImp.size(); i++) {
			boolean addFlag = false;
			for (int k = indexToAdd + 1; k < arrExistImp.size(); k++) {
				if (!(((ImpairmentMessages) arrNewImp.get(i)).getImpairmentMessageText().equals(nextDescriptionToBreak))) {
					ImpairmentMessages impMsg = (ImpairmentMessages) arrNewImp.get(i);
					if (arrMedMsgs.contains(((ImpairmentMessages) arrExistImp.get(k)).getImpairmentMessageText())) {
						addFlag = true;
						continue;
					}
					if (addFlag) {
						arrExistImp.add(k - 1, impMsg);
						returnCount++;
						if ((i + 1) < arrNewImp.size())
							i++;
						else
							break top;
					}
				} else
					break top;
			}
			ImpairmentMessages impMsg = (ImpairmentMessages) arrNewImp.get(i);
			arrExistImp.add(arrExistImp.size(), impMsg);
			returnCount++;
		}
		existingImp.setImpairmentMessages(arrExistImp);
		return returnCount;
	}
	/**
	 * This method is responsible for comparing the impairment message
	 * @param messages
	 * @param messages2
	 */
	private boolean compareMsgString(ImpairmentMessages existingMsg, ImpairmentMessages newMsg) {
		if (existingMsg.getImpairmentMessageText() != null && newMsg.getImpairmentMessageText() != null) {
			if (!(existingMsg.getImpairmentMessageText().startsWith("Prior rating") || existingMsg.getImpairmentMessageText().startsWith("Questionnaire data evaluated on"))) {
				if (!(existingMsg.getImpairmentMessageText().equals(newMsg.getImpairmentMessageText())))
					return true;
			}
		}
		return false;
	}
	/**
	 * This method is responsible for adding the new impairment message
	 * @param existingImp
	 * @param newImp
	 * @param index
	 */
	private void addNewMessage(ImpairmentInfo existingImp, ImpairmentMessages newImpMsgs, int index, NbaTXLife nbaTxLife) throws NbaBaseException {
		//ImpairmentMessages impMsg = newImp;
		//existingImp.addImpairmentMessages(impMsg);
		newImpMsgs.setActionAdd();
		newImpMsgs.setImpairmentMessageDate(new java.util.Date());
		newImpMsgs.setImpairmentMessageUserID(processId);
		NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
		nbaOLifEId.setId(newImpMsgs);
		ArrayList arrExiImp = existingImp.getImpairmentMessages();
		arrExiImp.add(index + 1, newImpMsgs);
		existingImp.setImpairmentMessages(arrExiImp);
	}
	/**
	 * This method is responsible for the following actions.
	 * Add - New Impairment will be added if the impairment is new and there is no match with previous
	 * Merge - When the new impairment is similar to old impairment and all new impairment
	 * 	       messages are not similar to old messages or the new status is not pending
	 * Discard - When the new impairment is similar to old impairment and all new impairment 								           
	 * 			 messages are similar to old messages and the new status is pending.
	 * @param existingImps
	 * @param newImps
	 */
	public void generalImpMerging(ArrayList existingImps, ArrayList newImps, NbaTXLife nbaTxLife) throws NbaBaseException {
		ImpairmentInfo impInfoProcessed = null;
		int indexImpInfoProcessed= 0;
		NbaAcdb nbaAcdb = new NbaAcdb();  //NBA224, ALS2840

		for (int i = 0; i < newImps.size(); i++) {
			impInfoProcessed = (ImpairmentInfo) newImps.get(i);
			int findIndex = matchImpairments(existingImps, newImps, impInfoProcessed, nbaTxLife, indexImpInfoProcessed);
			if( newImps.size() <= 0 ) {
				return;
			}
			//No prior Imp found
			if (findIndex == -1) {
				impInfoProcessed.setActionAdd();
				NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
				nbaOLifEId.setId(impInfoProcessed);
				existingImps.add(impInfoProcessed);
				
				// begin NBA224
				boolean restrict = nbaAcdb.getRestrictApprovalInd(impInfoProcessed.getDescription());
				impInfoProcessed.setRestrictApprovalInd(restrict);
				// end NBA224
				
				// begin AXAL3.7.07
				if (!impInfoProcessed.hasImpairmentDate() ) {
					impInfoProcessed.setImpairmentDate(new Date());
				}
				// end AXAL3.7.07
				if (impInfoProcessed.hasDebit() && impInfoProcessed.getDebit() > 0) {
					ImpairmentMessages ratingMes = getImpMessagesObject();
					ratingMes.setImpairmentMessageText("Evaluation rating of " + impInfoProcessed.getDebit());
					NbaOLifEId nbaOLifEId1 = new NbaOLifEId(nbaTxLife);
					nbaOLifEId1.setId(ratingMes);
					impInfoProcessed.addImpairmentMessages(ratingMes);
				}
				if (impInfoProcessed.getImpairmentMessagesCount() > 0) {
					for (int j = 0; j < impInfoProcessed.getImpairmentMessagesCount(); j++) {
						ImpairmentMessages impaMsg = impInfoProcessed.getImpairmentMessagesAt(j);
						impaMsg.setActionAdd();
						impaMsg.setImpairmentMessageDate(new java.util.Date());
						impaMsg.setImpairmentMessageUserID(processId);
						NbaOLifEId nbaOLifEId1 = new NbaOLifEId(nbaTxLife);
						nbaOLifEId1.setId(impaMsg);
					}
				}
			} else //prior imp found   			
				{
				ImpairmentInfo impToMod = (ImpairmentInfo) existingImps.get(findIndex);
				if (newMessagesExist(impToMod, impInfoProcessed, nbaTxLife) || impInfoProcessed.getImpairmentStatus() != lStatusPending)					//some messages to merge with old	
					{
					//ACP022 starts.
					if (!(impToMod.getImpairmentSource() != null
						&& impToMod.getImpairmentSource().equalsIgnoreCase("FRNTRAVEL")
						&& (impToMod.getImpairmentStatus() == NbaOliConstants.OLIEXT_LU_IMPAIRMENTSTATUS_RESOLVED_BY_UNDERWRITER
							|| impToMod.getImpairmentStatus() == NbaOliConstants.OLIEXT_LU_IMPAIRMENTSTATUS_TRIVIAL
							|| impToMod.getDebit() == 999
							|| (impInfoProcessed.getImpairmentPermFlatExtraAmt() != null
								&& impToMod.getImpairmentPermFlatExtraAmt() != null
								&& Double.parseDouble(impToMod.getImpairmentPermFlatExtraAmt())
									> Double.parseDouble(impInfoProcessed.getImpairmentPermFlatExtraAmt())))))	//ACP022 ends.
						{
						if (!Double.isNaN(impToMod.getDebit()) && impToMod.getDebit() > 0 &&
						        impToMod.getDebit() != impInfoProcessed.getDebit()) {  //AXAL3.7.07
							ImpairmentMessages imMsg = getImpMessagesObject();
							imMsg.setImpairmentMessageText("Prior rating of " + impToMod.getDebit());
							NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
							nbaOLifEId.setId(imMsg);
							impToMod.addImpairmentMessages(imMsg);
						}
						if (!Double.isNaN(impInfoProcessed.getDebit())  && //AXAL3.7.07
						        impToMod.getDebit() != impInfoProcessed.getDebit()) {  //AXAL3.7.07, reduce messages when old same as new
							
							// begin AXAL3.7.07
							if (impInfoProcessed.getImpairmentSource().equalsIgnoreCase(NbaConstants.HEIGHTWEIGHTEVAL_SRC) ||
									impInfoProcessed.getImpairmentSource().equalsIgnoreCase(NbaConstants.LABBLOODPRESSURE_SRC)) {
								ImpairmentMessages imMsg =getImpMessagesObject();
								imMsg.setImpairmentMessageText("Evaluation rating of " + impInfoProcessed.getDebit());
								impToMod.addImpairmentMessages(imMsg);
								impToMod.setDebit(impInfoProcessed.getDebit()); //overwrite the old with new debit value								
								NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
								nbaOLifEId.setId(imMsg);
							}
							// end AXAL3.7.07
						}
						//ACP022 starts
						if (impToMod.getImpairmentSource() != null
							&& impToMod.getImpairmentSource().equalsIgnoreCase("FRNTRAVEL")
							&& impInfoProcessed.getImpairmentPermFlatExtraAmt() != null
							&& impToMod.getImpairmentPermFlatExtraAmt() != null
							&& Double.parseDouble(impToMod.getImpairmentPermFlatExtraAmt())
								< Double.parseDouble(impInfoProcessed.getImpairmentPermFlatExtraAmt())) {
							impToMod.setImpairmentPermFlatExtraAmt(impInfoProcessed.getImpairmentPermFlatExtraAmt());
						}
						//ACP022 ends
						impToMod.setImpairmentStatus(impInfoProcessed.getImpairmentStatus());
						impToMod.setActionUpdate();
					}
				} else if (impInfoProcessed.getImpairmentStatus() == lStatusPending)					//all messages are exist and check for pending status
					continue;
			}
		}
	}
	
	/**
	 * Returns a ImpairmentMessage object.
	 * @return
	 */
	public ImpairmentMessages getImpMessagesObject(){
		ImpairmentMessages impMsg = new ImpairmentMessages();
		impMsg.setActionAdd();
		impMsg.setImpairmentMessageDate(new java.util.Date());
		impMsg.setImpairmentMessageUserID(processId);
		return impMsg;
	}
	/**
	 * This method is responsible for updating the status of the previous impairments to 
	 * "resolved by system" if there are no new impairments 
	 * and applies only for Contract Evaluation only
	 * @param existingImps
	 * @param newImps
	 * @param impSrc
	 * @n
	 */
	public void updateStatusResolvedForContract(ArrayList existingImps, ArrayList newImps, String impSrc, NbaTXLife nbaTxLife) {  //AXAL3.7.07
		int existingImpSize = existingImps.size();
		vpmsModelSource = impSrc;
		ImpairmentInfo impairInfo = null;
		for (int i = 0; i < existingImpSize; i++) {
			impairInfo = ((ImpairmentInfo) existingImps.get(i));
			String impairSrc = null;
			if (impairInfo != null) {
				impairSrc = impairInfo.getImpairmentSource();
				if (impairSrc != null) {
					impairSrc = impairSrc.toUpperCase();
					// begin AXAL3.7.07
					if (impairSrc.equalsIgnoreCase(vpmsModelSource) && arrImpForCon.contains(impairSrc) && 	
							!existsInNewImpairments(newImps,impairSrc, impairInfo.getDescription())) {
						ImpairmentMessages impMsg = new ImpairmentMessages();
						impMsg.setImpairmentMessageText("Prior rating of " + ((ImpairmentInfo)existingImps.get(i)).getDebit());
						NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
						nbaOLifEId.setId(impMsg);
						((ImpairmentInfo)existingImps.get(i)).addImpairmentMessages(impMsg);
						((ImpairmentInfo) existingImps.get(i)).setImpairmentStatus(lStatusTrivial);
						((ImpairmentInfo) existingImps.get(i)).setDebit(0);
						((ImpairmentInfo) existingImps.get(i)).setActionUpdate();
						// end AXAL3.7.07
						//update status to 'Resolved by System'	  
					}
				}
			}
		}
	}
//	New Method SR564247(APSL2525) -Full
	/**
	 * This method is responsible for updating the status of the previous impairments to 
	 * "resolved by system" if there are no new impairments 
	 * and applies only for AP Predictive Hold only
	 * @param existingImps
	 * @param newImps
	 * @param impSrc
	 * @n
	 */
	public void updateStatusResolvedForServiceResult(ArrayList existingImps, ArrayList newImps, String impSrc, NbaTXLife nbaTxLife) {  //AXAL3.7.07
		int existingImpSize = existingImps.size();
		vpmsModelSource = impSrc;
		ImpairmentInfo impairInfo = null;
		for (int i = 0; i < existingImpSize; i++) {
			impairInfo = ((ImpairmentInfo) existingImps.get(i));
			String impairSrc = null;
			if (impairInfo != null) {
				impairSrc = impairInfo.getImpairmentSource();
				if (impairSrc != null) {
					impairSrc = impairSrc.toUpperCase();
					if (impairSrc.equalsIgnoreCase(vpmsModelSource) && arrImpForSrvRslt.contains(impairSrc) && 	
							!existsInNewImpairments(newImps,impairSrc, impairInfo.getDescription())) {
						ImpairmentMessages impMsg = new ImpairmentMessages();
						impMsg.setImpairmentMessageText("Prior rating of " + ((ImpairmentInfo)existingImps.get(i)).getDebit());
						NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
						nbaOLifEId.setId(impMsg);
						((ImpairmentInfo)existingImps.get(i)).addImpairmentMessages(impMsg);
						((ImpairmentInfo) existingImps.get(i)).setImpairmentStatus(lStatusTrivial);
						//ALII1609 Code Deleted
						((ImpairmentInfo) existingImps.get(i)).setActionUpdate();
						
					}
				}
			}
		}
	}
	/**
	 * This method is responsible for updating the status of the previous impairments to 
	 * "resolved by system" if there are no new impairments 
	 * and applies only for Requirement Evaluation only
	 * @param existingImps
	 * @param newImps
	 * @param impSrc
	 * @n
	 */
	// AXAL3.7.07 New Method
	public void updateStatusResolvedForRequirement(ArrayList existingImps, ArrayList newImps, String impSrc, NbaTXLife nbaTxLife) {
		int existingImpSize = existingImps.size();
		vpmsModelSource = impSrc;
		ImpairmentInfo impairInfo = null;
		for (int i = 0; i < existingImpSize; i++) {
			impairInfo = ((ImpairmentInfo) existingImps.get(i));
			String impairSrc = null;
			if (impairInfo != null) {
				impairSrc = impairInfo.getImpairmentSource();
				if (impairSrc != null) {
					impairSrc = impairSrc.toUpperCase();
					if (impairSrc.equalsIgnoreCase(vpmsModelSource) && arrImpForReq.contains(impairSrc) && 							
							!existsInNewImpairments(newImps,impairSrc, impairInfo.getDescription())) {
						ImpairmentMessages impMsg = new ImpairmentMessages();
						impMsg.setImpairmentMessageText("Prior rating of " + ((ImpairmentInfo)existingImps.get(i)).getDebit());
						NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
						nbaOLifEId.setId(impMsg);
						((ImpairmentInfo)existingImps.get(i)).addImpairmentMessages(impMsg);
						((ImpairmentInfo) existingImps.get(i)).setImpairmentStatus(lStatusTrivial);
						((ImpairmentInfo) existingImps.get(i)).setDebit(0);
						((ImpairmentInfo) existingImps.get(i)).setActionUpdate();
						//update status to 'Resolved by System'	  
					}
				}
			}
		}
	}
	
	/**
	 * Checks if this impairment exists in the new impairments.  
	 * @param newImps
	 * @param impairSrc
	 * @param description
	 * @return
	 */
	public boolean existsInNewImpairments(ArrayList newImps,String impairSrc, String description){  //AXAL3.7.07
		boolean exists = false;
		int newImpSize = newImps.size();
		ImpairmentInfo impairInfo = null;
		String newImpairSrc = null;
		for(int j=0;j<newImpSize;j++){
			impairInfo = ((ImpairmentInfo) newImps.get(j));
			newImpairSrc = impairInfo.getImpairmentSource();
			// begin AXAL3.7.07
			if(newImpairSrc.equalsIgnoreCase(impairSrc) &&
					impairInfo.getDescription().equalsIgnoreCase(description)){
			// end AXAL3.7.07
				exists = true;
				break;
			}
		}
		return exists;
	}
	
	/**
	 * This method is responsible for merging the new acceptable impairment with the 
	 * old Impairment if it can.
	 * @param existingImps
	 * @param accepImp
	 * @return
	 */
	public boolean mergeWithExistImp(ArrayList existingImps, AcceptableImpairments accepImp, NbaTXLife nbaTxLife) throws NbaBaseException {
		if (existingImps == null || accepImp == null || nbaTxLife == null)
			return false;
		for (int i = 0; i < existingImps.size(); i++) {
			if (((ImpairmentInfo) existingImps.get(i)).getDescription() != null && accepImp.getDescription() != null) {
				if (((ImpairmentInfo) existingImps.get(i)).getDescription().equals(accepImp.getDescription())) {
					((ImpairmentInfo) existingImps.get(i)).setImpairmentStatus(lStatusPending);
					ImpairmentMessages accepMes = getImpMessagesObject();
					if (accepImp.getMessage() != null) {
						accepMes.setImpairmentMessageText(accepImp.getMessage());
						NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
						nbaOLifEId.setId(accepMes);
						((ImpairmentInfo) existingImps.get(i)).addImpairmentMessages(accepMes);
					}
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * This method is responsible for merging the new acceptable impairment with the 
	 * old acceptable impairment if it can.
	 * @param existingImps
	 * @param existingaccepImps
	 * @param accepImp
	 * @return
	 */
	public boolean mergeWithExistAccImp(ArrayList existingImps, ArrayList existingaccepImps, AcceptableImpairments accepImp, NbaTXLife nbaTxLife)
		throws NbaBaseException {
		if (existingImps == null || existingaccepImps == null || accepImp == null || nbaTxLife == null)
			return false;
		for (int i = 0; i < existingaccepImps.size(); i++) {
			if (((AcceptableImpairments) existingaccepImps.get(i)).getDescription() != null && accepImp.getDescription() != null) {
				if (((AcceptableImpairments) existingaccepImps.get(i)).getDescription().equals(accepImp.getDescription())) {
					ImpairmentInfo newAccImp = new ImpairmentInfo();
					newAccImp.setActionAdd();
					newAccImp.setDescription(accepImp.getDescription());
					// begin AXAL3.7.07
					newAccImp.setImpairmentType(new Long(OLI_MEDCOND_1009800003).toString());
					newAccImp.setImpairmentClass(IMPAIRMENT_CLASS_FIVE);
					newAccImp.setImpairmentSource("LabTest");
					newAccImp.setImpairmentDate(new Date());
					// end AXAL3.7.07
					newAccImp.setImpairmentStatus(lStatusPending);
					NbaOLifEId nbaOLifEId = new NbaOLifEId(nbaTxLife);
					nbaOLifEId.setId(newAccImp);
					existingImps.add(newAccImp);
					ImpairmentMessages accepoldMes = getImpMessagesObject();
					if (((AcceptableImpairments) existingaccepImps.get(i)).getMessage() != null) {
						accepoldMes.setImpairmentMessageText(((AcceptableImpairments) existingaccepImps.get(i)).getMessage());
						NbaOLifEId nbaOLifEId1 = new NbaOLifEId(nbaTxLife);
						nbaOLifEId1.setId(accepoldMes);
						newAccImp.addImpairmentMessages(accepoldMes);
					}
					ImpairmentMessages accepnewMes = getImpMessagesObject();
					if (accepImp.getMessage() != null) {
						accepnewMes.setImpairmentMessageText(accepImp.getMessage());
						NbaOLifEId nbaOLifEId2 = new NbaOLifEId(nbaTxLife);
						nbaOLifEId2.setId(accepnewMes);
						newAccImp.addImpairmentMessages(accepnewMes);
					}
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * This is the main method for merging Accept Imp and it will insert the new data into 
	 * temporary table (AcceptableImpairments)if it can not be merged with either  
	 * @param existingImps
	 * @param existingAccepImps
	 * @param newAccepImp
	 */
	public ArrayList mergeAccepImp(ArrayList existingImps, ArrayList existingAccepImps, ArrayList newAccepImp, NbaTXLife nbaTxLife)
		throws NbaBaseException {
		ArrayList toAddAccepImps = new ArrayList();
		int newAccepImpSize = 0;
		if (newAccepImp != null) {
			newAccepImpSize = newAccepImp.size();	
		}
		for (int i = 0; i < newAccepImpSize; i++) {
			AcceptableImpairments acnewImp = (AcceptableImpairments) newAccepImp.get(i);
			if (mergeWithExistImp(existingImps, acnewImp, nbaTxLife))
				continue;
			else if (mergeWithExistAccImp(existingImps, existingAccepImps, acnewImp, nbaTxLife))
				continue;
			else {
				acnewImp.setActionAdd();
				toAddAccepImps.add(acnewImp);
			}
		}
		return toAddAccepImps;
	}
	/**
     * Check whether impairment type code matches with existing impairment type code and impairment class of new impairment is not blank, zero and
     * other ill-defined and unspecified causes of mortality (impairment class =1 and imparment type code = 1899) 
     * @param existingImp existing impairment
     * @param newImpProcessed new impairment to be added
     * @return true if conditions are met otherwise false
     */
    //SPR2466 New Method
    public boolean eligibleForImpCodeMatch(ImpairmentInfo existingImp, ImpairmentInfo newImpProcessed) {

        if (newImpProcessed.getImpairmentClass() != null && existingImp.getImpairmentClass() != null && existingImp.getImpairmentType() != null) {
            if (!newImpProcessed.getImpairmentClass().equals("")
                    && !IMPAIRMENT_CLASS_ZERO.equals(newImpProcessed.getImpairmentClass())
                    && !(IMPAIRMENT_CLASS_ONE.equals(newImpProcessed.getImpairmentClass()) && NbaOliConstants.OLI_MEDCOND_R99 == NbaUtils
                            .convertStringToLong(newImpProcessed.getImpairmentType()))) {
                if (newImpProcessed.getImpairmentType().equals(existingImp.getImpairmentType())) {
                    return true;
                }
            }
        }
        return false;
    }

}
