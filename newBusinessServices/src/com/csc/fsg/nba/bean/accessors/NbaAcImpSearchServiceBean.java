package com.csc.fsg.nba.bean.accessors;
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
 * *******************************************************************************<BR>
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.CriteriaOrCriteriaExpression;
import com.csc.fsg.nba.vo.txlife.ImpairmentInfo;
import com.csc.fsg.nba.vo.txlife.OLifE;
import com.csc.fsg.nba.vo.txlife.OLifEExtension;
import com.csc.fsg.nba.vo.txlife.ResultInfo;
import com.csc.fsg.nba.vo.txlife.TXLife;
import com.csc.fsg.nba.vo.txlife.TXLifeRequest;
import com.csc.fsg.nba.vo.txlife.TXLifeResponse;
import com.csc.fsg.nba.vo.txlife.TransResult;
import com.csc.fsg.nba.vo.txlife.UserAuthResponseAndTXLifeResponseAndTXLifeNotify;
import com.csc.fsg.nbaac.database.NbaAcImpSearchDataBaseAccessor;
import com.csc.fsg.nbaac.vo.NbaAcImpSearchHitRecord;
import com.csc.fsg.nbaac.vo.NbaAcImpSearchImpairmentIndex;
import com.csc.fsg.nbaac.vo.NbaAcImpSearchImpairmentThesaurus;
import com.csc.fsg.nbaac.vo.NbaAcImpSearchSortHit;
import com.csc.fsg.nbaac.vo.NbaAcImpSearchThesaurus;

/**
 * Bean implementation class for Enterprise Bean: NbaAcImpSearchService
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>SPR2746</td><td>Version 6</td><td>Impairment Search Types of Exact and Start With are mapped to the same index value</td></tr>
 * <tr><td>SPR2992</td><td>Version 6</td><td>General Code Clean Up Issues for Version 6</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7<td><tr>
 * <tr><td>ALPC7</td><td>Version 7</td><td>Schema migration from 2.8.90 to 2.9.03</td></tr> 
 * <tr><td>NBA224</td><td>Version 8</td><td>nbA Underwriter Workbench Requirements and Impairments Enhancement Project<td><tr> 
 *  <tr><td>NBA256</td><td>Version 8</td><td>WAS 7 and RAD 7.5 Upgradation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 4

 */
public class NbaAcImpSearchServiceBean implements javax.ejb.SessionBean {
	
	private long ResultCodeInfo=0;
	private ArrayList arrNoise = null;
		private Vector vecAlternate = new Vector();
		private boolean alterFlag = false;
		private int count = 0;
		private NbaAcImpSearchSortHit impSortHit = null;
		private NbaAcImpSearchImpairmentIndex[] impOutput = null;  
		private int ResultCode = -100;
		private NbaAcImpSearchDataBaseAccessor acDbAccess = new NbaAcImpSearchDataBaseAccessor();
		private int DSIGNIF_FACTOR = 3; 
		private int DESCRIPTLEN = 60;
		private int MAXINWORDS = 10;
		private int WORDLEN = 25;
		private int SPELLCHKLEN = 16;
		private int MAXMATCHESFORCLIENT = -100;
		static private char[] separatorChars = { '-', '.', ',', ')', '(', '\\', '/', '=', '_', '\"' };
		static private Hashtable htAlternate = new Hashtable();
		static char vowels_wh[] = { 'A', 'E', 'I', 'O', 'U', 'Y', 'W', 'H' };
		static char slient_th[] = { 'S', 'T', 'H' };
									 /* ABCDEFGHIJKLMNOPQRSTUVWXYZ */
		static char spellchk_map[] = "01230120022455012623010202".toCharArray();

		static {
			htAlternate.put("K", "CH");
			htAlternate.put("PS", "S");
			htAlternate.put("PH", "F");
			htAlternate.put("PN", "N");

			htAlternate.put("CH", "K");
			htAlternate.put("S", "PS");
			htAlternate.put("F", "PH");
			htAlternate.put("N", "PN");
		}
	private javax.ejb.SessionContext mySessionCtx;
	/**
	 * getSessionContext
	 */
	public javax.ejb.SessionContext getSessionContext() {
		return mySessionCtx;
	}
	/**
	 * setSessionContext
	 */
	public void setSessionContext(javax.ejb.SessionContext ctx) {
		mySessionCtx = ctx;
	}
	/**
	 * ejbCreate
	 */
	public void ejbCreate() throws javax.ejb.CreateException {
	}
	/**
	 * ejbActivate
	 */
	public void ejbActivate() {
	}
	/**
	 * ejbPassivate
	 */
	public void ejbPassivate() {
	}
	/**
	 * ejbRemove
	 */
	public void ejbRemove() {
	}
	
	private static NbaLogger logger;
		/**
		 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
		 * @return com.csc.fsg.nba.foundation.NbaLogger
		 */
		private static NbaLogger getLogger() {
			if (logger == null) {
				try {
					logger = NbaLogFactory.getLogger(NbaAcImpSearchServiceBean.class.getName());
				} catch (Exception e) {
					NbaBootLogger.log("NbaAcImpSearchServiceBean could not get a logger from the factory.");
					e.printStackTrace(System.out);
				}
			}
			return logger;
		}
	/**
	 * @param txlife
	 * @return
	 * @throws java.rmi.RemoteException
	 */
	/** This function will get the data from the WebService client in the form of XML and retrieve 
		 * the details about the Search Key, Type...etc and calls the corresponding methods based on the 
		 * search type and returns the XML element to the client with result from IMPSEARCH service.  
		 * @param txlife
		 * @return Txlife
		 * @throws java.rmi.RemoteException
		 */
		public NbaTXLife getImpSearchWords(NbaTXLife nbaTXLife) throws java.rmi.RemoteException {
			
			String strSearchString = null;
			long searchType = 0;//SPR2992
			initData();
			
			TXLifeRequest txReq =nbaTXLife.getTXLife().getUserAuthRequestAndTXLifeRequest().getTXLifeRequestAt(0);
			
			//strSerachString = txReq.getCriteriaExpression().getCriteria().getPropertyValue().getPCDATA();
			CriteriaOrCriteriaExpression cri=txReq.getCriteriaExpression().getCriteriaOperatorAndCriteriaOrCriteriaExpressionAndOLifEExtension().getCriteriaOrCriteriaExpressionAt(0);//ALPC7
			strSearchString = cri.getCriteria().getPropertyValue().getPCDATA();
			searchType  = cri.getCriteria().getOperation();//SPR2992
			//SPR2992 code deleted
			MAXMATCHESFORCLIENT = txReq.getMaxRecords();
			//SPR2992 code deleted

			//Begin SPR2746
			if (searchType == NbaOliConstants.OLI_OP_EQUAL) {//SPR2992
				exactMatch(strSearchString, true);
			} else if (searchType == NbaOliConstants.OLI_OP_LIKE) {//SPR2992
				exactMatch(strSearchString, false);
			} else if (searchType == NbaOliConstants.OLI_OPERATION_THESAURUS) {//SPR2992
				impOutput = wordSearch(strSearchString);
			} else if (searchType == NbaOliConstants.OLI_OPERATION_PHONETIC) {//SPR2992
				impOutput = spellCheck(strSearchString);
			}
			//End SPR2746


				if (count > (MAXMATCHESFORCLIENT-1)) {
					count = MAXMATCHESFORCLIENT-1;
					ResultCode = 2;
					ResultCodeInfo=710;
				}
				if (count > 0 && ResultCode == -100)
					ResultCode = 1;
				else if (count == 0 && ResultCode == -100)
				{				
					ResultCode = 2;
					ResultCodeInfo=2001;
				}		
				nbaTXLife.setTXLife(addReturnResult(impOutput, searchType));//SPR2992
			return nbaTXLife;
		}		
		/**This function will appends the result from IMPSEARCH to the input XML element and also includes 
		 * the result code and size to the XML.
		 * @param impOutput1
		 * @param searchType
		 * @return TXLife
		 */
		// NBA224 Access modifier is changed from private to protected  
		protected TXLife addReturnResult(NbaAcImpSearchImpairmentIndex[] impOutput1,
												long searchType) {//SPR2992
		
			ArrayList arrImpInfo=new ArrayList();
			if (impOutput1 != null) {
				for (int i = 0; i < impOutput1.length; i++) {
					NbaAcImpSearchImpairmentIndex impIItemp = impOutput[i];
					if (impIItemp != null) {
						 ImpairmentInfo impinfo=new ImpairmentInfo();
						 
						impinfo.setId(Integer.toString(i));
						impinfo.setDescription(impIItemp.getImpairmentDesc());
						impinfo.setImpairmentClass(impIItemp.getRiskType());
						impinfo.setImpairmentType(impIItemp.getImpairmentTc());
						impinfo.setRestrictApprovalInd(impIItemp.isRestrictApprovalInd()); //NBA224
						arrImpInfo.add(i,impinfo);
					}
				}
				
				//Add UNKNOWN to result list when search type is Like, Phonetic or Thesaurus
				if (searchType == NbaOliConstants.OLI_OP_LIKE || searchType == NbaOliConstants.OLI_OPERATION_PHONETIC
                    || searchType == NbaOliConstants.OLI_OPERATION_THESAURUS)//SPR2746//SPR2992
				{
				impOutput=null;
				impSortHit=null;
				exactMatch("UNKNOWN", true);
				  if (impOutput != null) {
						NbaAcImpSearchImpairmentIndex impIItemp = impOutput[0];
						if (impIItemp != null) {
							 ImpairmentInfo impinfo=new ImpairmentInfo();
						 
							impinfo.setId(Integer.toString(arrImpInfo.size()));
							impinfo.setDescription(impIItemp.getImpairmentDesc());
							impinfo.setImpairmentClass(impIItemp.getRiskType());
							impinfo.setImpairmentType(impIItemp.getImpairmentTc());
							impinfo.setRestrictApprovalInd(impIItemp.isRestrictApprovalInd()); //NBA224
							arrImpInfo.add(arrImpInfo.size(),impinfo);
							if (count == (MAXMATCHESFORCLIENT-1)) 
							  count++;
						}
				  }
				}		 						
				
			}

			TXLife txlife=new TXLife();
						txlife.setUserAuthResponseAndTXLifeResponseAndTXLifeNotify(new UserAuthResponseAndTXLifeResponseAndTXLifeNotify());
						txlife.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().addTXLifeResponse(new TXLifeResponse());
						TXLifeResponse txRes=txlife.getUserAuthResponseAndTXLifeResponseAndTXLifeNotify().getTXLifeResponseAt(0);
						txRes.setOLifE(new OLifE());
						txRes.getOLifE().addOLifEExtension(new OLifEExtension());
						OLifEExtension olEx=txRes.getOLifE().getOLifEExtensionAt(0);
						txRes.setTransResult(new TransResult());
						txRes.getTransResult().setResultCode(ResultCode);
						txRes.getTransResult().setRecordsFound(count);
						if(ResultCode==2 || ResultCode==5)
						{
							ResultInfo resultCode=new ResultInfo();
							resultCode.setResultInfoCode(ResultCodeInfo);
							txRes.getTransResult().addResultInfo(resultCode);
						}

			olEx.setImpairmentInfo(arrImpInfo);
						
		    return txlife;
		}
		/**
		 * Function will check whether the Word length should be with in the limits. 
		 * @param strWord
		 * @return String
		 */
		private String checkWordLength(String strWord) {
			if (strWord.length() > WORDLEN)
				return strWord.substring(0, WORDLEN);
			return strWord;
		}
		/**Function will get the type code for all the input words from database and remove 
		 * the noise words, whose type code is zero.
		 * @param words
		 * @return Array of String
		 * @throws Throwable
		 */
		private String[] removeNoiseWoords(String[] words) throws Throwable {

			String[] wordsToReturn = new String[words.length];
			String[] wordsToReturn1 = null;
			arrNoise = acDbAccess.selectNoiseWords(words);
			int k = 0;
			// SPR3290 code deleted

			top : for (int i = 0; i < words.length; i++) {
				if (arrNoise != null) {
					for (int j = 0; j < arrNoise.size(); j++) {
						NbaAcImpSearchThesaurus theas = (NbaAcImpSearchThesaurus) arrNoise.get(j);
					
						if (theas.getWordText().equals(words[i]) && theas.getTypeCd() == 0)
							continue top;
						else if (theas.getWordText().equals(words[i]) && theas.getTypeCd() > 0) {
							wordsToReturn[k++] = words[i];
							continue top;
						}
					}
				}
				wordsToReturn[k++] = words[i];
			}
			wordsToReturn1 = new String[k];
			for (int i = 0; i < wordsToReturn1.length; i++) {
				wordsToReturn1[i] = wordsToReturn[i];
			}

			return wordsToReturn1;
		}	
		/**Function will remove the noise words and word ending (S,ED,ING)(For Word Serach only)
		 * @param words
		 * @param flagForRemoveAtEnd
		 * @return
		 * @throws Throwable
		 */
		private String[] checkNoiseWoords(String[] words, boolean flagForRemoveAtEnd) throws Throwable {

			for (int i = 0; i < words.length; i++) {
				words[i] = checkWordLength(words[i]);
			}
			String[] wordsToReturn = removeNoiseWoords(words);
			if (flagForRemoveAtEnd == true) {
				for (int j = 0; j < wordsToReturn.length; j++) {
					wordsToReturn[j] = removeAtEnd(wordsToReturn[j]);
				}
			}
			wordsToReturn = removeNoiseWoords(wordsToReturn);
			return wordsToReturn;
		}
		/**
		 * Fuction will remove the any of the S,ED,ING from the word ending
		 * @param strWord
		 * @return String
		 */
		private String removeAtEnd(String strWord) {
			if (strWord.toUpperCase().endsWith("S"))
				return strWord.substring(0, (strWord.length() - 1));
			else if (strWord.toUpperCase().endsWith("ED"))
				return strWord.substring(0, (strWord.length() - 2));
			else if (strWord.toUpperCase().endsWith("ING"))
				return strWord.substring(0, (strWord.length() - 3));

			return strWord;
		}
		/**
		 * Function will find the D&(AND)C in the input phrase words and will
		 * replace it with the DILATATION&(AND)CURRETAGE
		 * @param words 
		 * @param indexForD
		 */
		private void checkForDC(String[] words, int indexForD) {
			if ((indexForD + 2 < words.length) && (words[indexForD + 1] == "AND" || words[indexForD + 1] == "&") && words[indexForD + 2] == "C") {
				words[indexForD] = "DILATATION";
				words[indexForD + 2] = "CURRETAGE";
			}
		}

		/**
		 * @param arrWords
		 * @return Array of String
		 * @throws Throwable
		 */
		private String[] sortByWordOcc(String[] arrWords) throws Throwable {
			String[] wordsToReturn = new String[arrWords.length];

			Hashtable htMap = new Hashtable();
			int[] countArr = new int[arrWords.length];
			for (int i = 0; i < arrWords.length; i++) {
				int temp = acDbAccess.selectWordOccCount(checkWordLength(arrWords[i]));
				htMap.put(Integer.toString(temp), checkWordLength(arrWords[i]));
				countArr[i] = temp;
			}
			for (int i = 0; i < arrWords.length; i++) {
		
				for (int j = i + 1; j < countArr.length; j++) {
					if (impSortHit.getRecordHits(j).getHits() > 0) {
						if (countArr[i] < countArr[j]) {

							String temp = arrWords[i];
		
							arrWords[j] = arrWords[i];
							arrWords[i] = temp;
						}
					}
				}
	
			}

			Arrays.sort(countArr);
			for (int i = 0; i < countArr.length; i++) {
				wordsToReturn[i] = (String) htMap.get(Integer.toString(countArr[i]));
			}
			return arrWords;
		}

		/**
		 * 
		 */
		private void initSortHit() {
			impSortHit = new NbaAcImpSearchSortHit();

			for (int i = 0; i < impSortHit.getRecordHits().length; i++) {
				impSortHit.getRecordHits(i).setHits(0);
				impSortHit.getRecordHits(i).setIidesc("");
			}
			for (int i = 0; i < impSortHit.getWordMatches().length; i++) {
				for (int j = 0; j < impSortHit.getWordMatches(i).getMatchedWords().length; j++) {
					impSortHit.getWordMatches(i).setMatchedWords("", j);
				}
			}
		}

		/**
		 * @param impDesc
		 * @param wordTxt
		 */
		private void updateHitList(String impDesc, String wordTxt) {

			if (impSortHit == null)
				initSortHit();

			for (int i = 0; i < impSortHit.getRecordHits().length; i++) {
				if (impSortHit.getRecordHits(i).getIidesc().equals(impDesc)) {
					impSortHit.getRecordHits(i).setHits(impSortHit.getRecordHits(i).getHits() + 1);
					//count++;
					updateMatchedWords(wordTxt, i);
					return;
				} else if (impSortHit.getRecordHits(i).getIidesc().equals("")) {
					impSortHit.getRecordHits(i).setIidesc(impDesc);
					impSortHit.getRecordHits(i).setHits(impSortHit.getRecordHits(i).getHits() + 1);
					count++;
					updateMatchedWords(wordTxt, i);
					return;
				}				
			}
		}
		/**
		 * @param wordTxt
		 * @param i
		 */		
		private void updateMatchedWords(String wordTxt, int i) {

			for (int j = 0; j < impSortHit.getWordMatches().length; j++) {
				if (impSortHit.getWordMatches(i).getMatchedWords(j).equals("")) {
					impSortHit.getWordMatches(i).setMatchedWords(checkWordLength(wordTxt), j);
					return;
				} else if (impSortHit.getWordMatches(i).getMatchedWords(j).equals(checkWordLength(wordTxt))) {
					impSortHit.getRecordHits(i).setHits(impSortHit.getRecordHits(i).getHits() - 1);
					//count--; 			
					return;
				}
			}
		}
		/**
		 * @param words
		 * @throws Throwable
		 */
		private void searchII(String[] words) throws Throwable {
			initSortHit();
			words = sortByWordOcc(words);
			for (int i = 0; i < words.length; i++) {
				ArrayList arr = acDbAccess.selectFromIITH(checkWordLength(words[i]), false);
				for (int j = 0; j < arr.size(); j++) {
					NbaAcImpSearchImpairmentThesaurus impTh = (NbaAcImpSearchImpairmentThesaurus) arr.get(j);
					updateHitList(impTh.getImpairmentDesc(), impTh.getWordTxt());
				}
			}
		}
		/**
		 * @param hashValue
		 * @param startChar
		 * @param alterChar
		 * @throws Throwable
		 */
		private void searchIIforHash(String hashValue, String startChar, String alterChar) throws Throwable {
			ArrayList arrTh = acDbAccess.selectFromTHforSpell(hashValue, startChar, alterChar);
			for (int i = 0; i < arrTh.size(); i++) {
				ArrayList arr = acDbAccess.selectFromIITH(checkWordLength(((NbaAcImpSearchThesaurus) arrTh.get(i)).getWordText()), true);
				for (int j = 0; j < arr.size(); j++) {
					NbaAcImpSearchImpairmentThesaurus impTh = (NbaAcImpSearchImpairmentThesaurus) arr.get(j);
					updateHitList(impTh.getImpairmentDesc(), impTh.getWordTxt());
				}
			}
		}
		/**
		 * @param words
		 * @throws Throwable
		 */
		private void searchIIforSpell(String[] words) throws Throwable {
			initSortHit();
			words = sortByWordOcc(words);
			for (int i = 0; i < words.length; i++) {
				if (alterFlag && vecAlternate.get(i) != null)
					searchIIforHash(ConvertWord(words[i]), words[i], (String) vecAlternate.get(i));
				else
					searchIIforHash(ConvertWord(words[i]), words[i], "-1");
			}
		}
		/**
		 * @param inputStr
		 * @return String
		 */
		private String ConvertWord(String inputStr) {
			int strLen = inputStr.length();
			char[] word = inputStr.toUpperCase().toCharArray();
			char[] result = new char[strLen];
			char scode;
			StringBuffer strbuf = new StringBuffer();
			int j = 0;
			for (int i = 0; i < (strLen); i++) {
				if (isAlpha(word[i])) {
					if (!isVowel(word[i])) {
						if (word[i] == slient_th[0] && (i + 2) < strLen && isSTH(word, i)) {
							result[j++] = word[i];
							i += 2;
						} else
							result[j++] = word[i];

					}
				}
			}
			strLen = j;
			j = 0;
			for (int i = 0; i < (strLen); i++) {
				scode = (char) get_scode(result[i]);
				if (j == 0 || strbuf.charAt(j - 1) != scode) {
					strbuf.append(scode);
					j++;
				}
			}
			return checkSpellCheckLength(strbuf.toString());
		}

		/**
		 * @param string
		 * @return String
		 */
		private String checkSpellCheckLength(String string) {
			if (string.length() > SPELLCHKLEN)
				return string.substring(0, SPELLCHKLEN);
			return string;
		}
		/**
		 * @param word
		 * @param i
		 * @return boolean
		 */
		private boolean isSTH(char[] word, int i) {
			if ((i + 2) < word.length && word[i + 1] == slient_th[1] && word[i + 2] == slient_th[2])
				return true;
			return false;
		}

		/**
		 * @param c
		 * @return boolean
		 */
		private boolean isVowel(char c) {
			for (int i = 0; i < vowels_wh.length; i++)
				if (c == vowels_wh[i])
					return true;
			return false;
		}

		/**
		 * @param c
		 * @return boolean
		 */
		private boolean isAlpha(char c) {
			if ((c - 'A') >= 0 && (c - 'A') < 26)
				return true;
			return false;
		}

		/**
		 * @param ch
		 * @return int
		 */
		private int get_scode(char ch) {
			/* If alpha, map input letter to spell check code. If not, return 0 */

			if (!isAlpha(ch)) /*error if not alpha */
				return -1;
			else
				return (spellchk_map[ch - 'A']);
		}
		/**
		 * @throws Throwable
		 */
		private void sortList() throws Throwable {
			//impSortHit=new NbaAcImpSearchSortHit();
			int iinum = 0;
			/*
			**  Calculate the match percentages.
			*/
			for (int i = 0; i < count; i++) {
				iinum = (ParseWords(impSortHit.getRecordHits(i).getIidesc())).length;
				impSortHit.setMatchedPercentage((float) impSortHit.getRecordHits(i).getHits() / (float) iinum, i);
			}

			/*
			**  Calculate the average significance factor for the descriptions.
			*/
			for (int i = 0; i < count; i++) {
				float avgsignif = findAvgSignif(i);
				impSortHit.setMatchedPercentage((impSortHit.getMatchedPercentage(i) * avgsignif), i);
			}

			/*
			**  Sort by number of hits.
			*/
			for (int i = 0; i < count; i++) {
				if (impSortHit.getRecordHits(i).getHits() > 0) {
					for (int j = i + 1; j < count; j++) {
						if (impSortHit.getRecordHits(j).getHits() > 0) {
							if (impSortHit.getRecordHits(i).getHits() < impSortHit.getRecordHits(j).getHits()) {

								NbaAcImpSearchHitRecord tempHT = impSortHit.getRecordHits(i);
								float tempPer = impSortHit.getMatchedPercentage(i);

								impSortHit.setRecordHits(impSortHit.getRecordHits(j), i);
								impSortHit.setMatchedPercentage(impSortHit.getMatchedPercentage(j), i);

								impSortHit.setRecordHits(tempHT, j);
								impSortHit.setMatchedPercentage(tempPer, j);
							}
						}
					}
				}
			}

			/*
			**  Now, sort by match percentage within number of hits.
			*/
			for (int i = 0; i < count; i++) {
				if (impSortHit.getRecordHits(i).getHits() > 0) {
					for (iinum = i + 1; iinum < count && impSortHit.getRecordHits(i).getHits() == impSortHit.getRecordHits(iinum).getHits(); iinum++);
					for (int j = i; j < iinum; j++) {
						if (impSortHit.getRecordHits(j).getHits() > 0) {
							if (impSortHit.getMatchedPercentage(i) < impSortHit.getMatchedPercentage(j)) {

								NbaAcImpSearchHitRecord tempHT = impSortHit.getRecordHits(i);
								float tempPer = impSortHit.getMatchedPercentage(i);

								impSortHit.setRecordHits(impSortHit.getRecordHits(j), i);
								impSortHit.setMatchedPercentage(impSortHit.getMatchedPercentage(j), i);

								impSortHit.setRecordHits(tempHT, j);
								impSortHit.setMatchedPercentage(tempPer, j);
							}
						}
					}
				}
			}
		}

		/**
		 * @param i
		 * @return float
		 */
		private float findAvgSignif(int i) throws Throwable {

			float avgsignif = 0; // SPR3290
			int wCount = 0;

			String[] strWords = impSortHit.getWordMatches(i).getMatchedWords();
			ArrayList arrWord = acDbAccess.selectNoiseWords(strWords);
			if(arrWord!=null)
			{
			for (int j = 0; j < arrWord.size(); j++) {
				NbaAcImpSearchThesaurus nlTh = (NbaAcImpSearchThesaurus) arrWord.get(j);
				if (nlTh == null || nlTh.getWordText().equals(""))
					continue;
						

				if (nlTh.getWordText().equals(strWords[j])) {
					avgsignif += (float) ((NbaAcImpSearchThesaurus) arrWord.get(j)).getTypeCd();
					wCount++;
				} else {
					avgsignif += (float) DSIGNIF_FACTOR;
					wCount++;
				}
			
			}
			}
			avgsignif /= (float) wCount;
			return avgsignif;
		}
		
		// SPR3290 code deleted
		
		/**
		 * @param ibuffer
		 * @return Array of String
		 */
		private String[] ParseWords(String ibuffer) {

			String[] word_array;

			char bufferChar[] = ibuffer.toUpperCase().trim().toCharArray();
			int i;

			/*
			**  Convert separator chars to spaces.
			*/
			for (i = 0; i < bufferChar.length; i++) {
				for (int j = 0; j < separatorChars.length; j++) {

					if (bufferChar[i] == separatorChars[j])
						bufferChar[i] = ' ';
				}
			}
		
			StringTokenizer strConvertToArray = new StringTokenizer(new String(bufferChar), " ");
			word_array = new String[strConvertToArray.countTokens()];
			for (int j = 0; j < MAXINWORDS && strConvertToArray.hasMoreTokens(); j++) {
				word_array[j] = strConvertToArray.nextToken(); // SPR3290
			}
			return (word_array);

		}

		/**
		 * @param strWord
		 * @return Array of NbaAcImpSearchImpairmentIndex
		 */
		private NbaAcImpSearchImpairmentIndex[] wordSearch(String strWord) {

			String[] strAfterNoise = null;
			//step 1
			try {
				strWord=strWord.toUpperCase();
				//step 2
				boolean exactFlag = true;

				exactFlag = exactMatch(strWord, true);
				//step 3
				if (exactFlag == false) {
					String strAfterParse[] = ParseWords(strWord);
					//step4
					for (int i = 0; i < strAfterParse.length; i++) {
						if (strAfterParse[i].equals("D"))
							checkForDC(strAfterParse, i);
					}
					//step5			
					strAfterNoise = checkNoiseWoords(strAfterParse, true);
					//step6
					exactFlag = exactMatch(strAfterNoise.toString(), true);
					//step7
					if (exactFlag == false) {
						searchII(strAfterNoise);
						//impSortHit.toPrint();
						//step8	
						sortList();
						//impSortHit.toPrint();
					}			  
				}
				//step9			
				if (count == 0)
					return spellCheck(strWord);
				else
					return acDbAccess.findMatches(impSortHit, checkForMax());

			} catch (Throwable e) {
				ResultCode = 5;
				ResultCodeInfo=600;
				getLogger().logError(e);
			}
			return null;
		}

		/**
		 * 
		 * @return int
		 */
		private int checkForMax() {

			if (count > (MAXMATCHESFORCLIENT-1)) {
				count = MAXMATCHESFORCLIENT-1;
				ResultCode = 2;
				ResultCodeInfo=710;
			}
			return count;
		}
		/**
		 * @param strWord
		 * @param b
		 * @return boolean
		 */
		private boolean exactMatch(String strWord, boolean b) {

			try {
				ArrayList arExact = acDbAccess.selectExactData(strWord, b, DESCRIPTLEN);
				if (arExact == null)
					return false;
				else {

					for (int i = 0; i < arExact.size(); i++) {
						updateHitList(((NbaAcImpSearchImpairmentIndex) arExact.get(i)).getImpairmentDesc(), strWord);
					}

					impOutput = acDbAccess.findMatches(impSortHit, checkForMax());
				}
			} catch (Throwable e) {

				ResultCode = 5;
				ResultCodeInfo=600;
				getLogger().logError(e);
			}
//			if(b==true)
//			ResultCode = 1;
			return true;
		}
		/**
		 * @param strWord
		 * @return Array of NbaAcImpSearchImpairmentIndex
		 */
		private NbaAcImpSearchImpairmentIndex[] spellCheck(String strWord) {
			String[] strAfterNoise = null;
			//step 1
			strWord = strWord.toUpperCase();
			//step 2
			boolean exactFlag = true;
			try {
				exactFlag = exactMatch(strWord, true);
				//step 3
				if (exactFlag == false) {
					String strAfterParse[] = ParseWords(strWord);
					//step4			
					strAfterNoise = checkNoiseWoords(strAfterParse, false);

					for (int i = 0; i < strAfterNoise.length; i++) {
						if (checkAlternate(strAfterNoise[i]))
							alterFlag = true;
					}
					//step5
					searchIIforSpell(strAfterNoise);
					impSortHit.toPrint();

					//step6
					sortList();
					impSortHit.toPrint();							  

				}
				//step7
				return acDbAccess.findMatches(impSortHit, checkForMax());
			} catch (Throwable e) {
				ResultCode = 5;
				ResultCodeInfo=600;
				getLogger().logError(e);
			}
			return null;
		}
		/**
		 * 
		 */
		private void initData() {
			arrNoise = null;
			vecAlternate = new Vector();
			alterFlag = false;
			count = 0;
			impSortHit = null;
			impOutput = null;
			ResultCode = -100;
			MAXMATCHESFORCLIENT = 150;
			ResultCodeInfo=0;
		}
		/**
		 * @param strWord
		 * @return boolean
		 */
		private boolean checkAlternate(String strWord) {
			Enumeration enumer = htAlternate.keys();  //NBA256
			int i = 0;
			while (enumer.hasMoreElements()) {  //NBA256
				String alterElement = (String) enumer.nextElement(); //NBA256
				if(getLogger().isInfoEnabled()) { //SPR3290
				    getLogger().logInfo(htAlternate.get(alterElement)); //SPR3290
				} //SPR3290
				if (strWord.toUpperCase().startsWith(alterElement)) {
					vecAlternate.add(i++, htAlternate.get(alterElement)); // SPR3290
					return true;
				}
			}
			return false;
		}
}
