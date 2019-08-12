package com.csc.fsg.nba.process.mib;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.csc.fs.Result;
import com.csc.fs.accel.result.AccelResult;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaTableAccessConstants;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaTableConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.process.NewBusinessAccelBP;
import com.csc.fsg.nba.tableaccess.NbaTableAccessor;
import com.csc.fsg.nba.vo.NbaMIBTranslatorVO;

/**
 * The Business Process class responsible for translating MIB Codes.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA226</td><td>Version 8</td><td>nba MIB Translation and validation</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 8.0.0
 * @since New Business Accelerator - Version 8
 */

public class TranslateMIBCodeBP extends NewBusinessAccelBP {
	protected NbaLogger logger = null;
	private NbaTableAccessor table = null;	//APSL553

	/*
	 * @see com.csc.fs.accel.AccelBP#process(java.lang.Object)
	 */
	public Result process(Object input) {
		AccelResult result = new AccelResult();
		try {
			if (input instanceof NbaMIBTranslatorVO) {
				NbaMIBTranslatorVO codeVo = (NbaMIBTranslatorVO) input;
				processMIBCode(codeVo);
				result.addResult(codeVo);
			} else if (input instanceof List) {
				List codeVOList = (List) input;
				processMIBCodeList(codeVOList);
				result.addResult(codeVOList);
			}
		} catch (Exception e) {
			getLogger().logException(e);
			addExceptionMessage(result, e);
			result.setErrors(true);
			return result;
		}
		return result;
	}

	/**
	 * Responsible to check if a code is valid or not, sets translation if valid
	 * @param newCode
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	protected void processMIBCode(NbaMIBTranslatorVO newCode) throws NbaBaseException {
		if (!isValid(newCode)) {
			return;
		} else {
			setTranslations(newCode);
		}
	}

	/**
	 * Sets translated values for each code
	 * @param codeList
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	protected void processMIBCodeList(List codeList) throws NbaBaseException {
		int count = codeList.size();
		NbaMIBTranslatorVO codeVO = null;
		for (int i = 0; i < count; i++) {
			codeVO = (NbaMIBTranslatorVO) codeList.get(i);
			setTranslations(codeVO);
		}
	}

	/**
	 * Validates weight
	 * @param newCode
	 * @return boolean
	 */
	protected boolean validateWeight(NbaMIBTranslatorVO newCode) {
		boolean flag = true;
		if (!isValidNumber(newCode.getWeight())) {
			newCode.getValidationMessages().add(NbaConstants.WEIGHT_INVALID2);
			flag = false;
		}
		if (newCode.getWeightLoss().length() > 0 && !isValidNumber(newCode.getWeightLoss())) {
			newCode.getValidationMessages().add(NbaConstants.WEIGHT_LOSS_INVALID2);
			flag = false;
		}
		if (newCode.getWeightLoss().indexOf("?") == 0) {
			newCode.getValidationMessages().add(NbaConstants.WEIGHT_LOSS_INVALID3);
			flag = false;
		}
		return flag;
	}

	/**
	 * Validates Height
	 * @param newCode
	 * @return boolean
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	protected boolean validateHeight(NbaMIBTranslatorVO newCode) {
		try {
			Long.parseLong(newCode.getHeightFeet());
			Long.parseLong(newCode.getHeightInches());
		} catch (NumberFormatException e) {
			newCode.getValidationMessages().add(NbaConstants.HEIGHT_INVALID2);
			return false;
		}
		return true;
	}

	/**
	 * Validates if the code is valid or not
	 * @param newCode
	 * @return boolean
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	//APSL553 Changed Validation
	public boolean isValid(NbaMIBTranslatorVO newCode) throws NbaBaseException {
		boolean isValid = true;
		if (newCode.isHeightWeight()) {//If the code is of type Height weight
			isValid = validateWeight(newCode) && validateHeight(newCode);
			if (!isValid) {
				newCode.getValidationMessages().add(NbaConstants.COMPLETE_CODE_INVALID);
			}
		} else {//If the code other MIB code
			HashMap tableValuesMap = setTableValues(newCode);
			String mibCombinedCode = processTranslation(NbaTableConstants.NBA_MIB_COMBINED_CODES, NbaTableAccessConstants.COMBINED_CODE, newCode
					.getCompleteCode().substring(0, 6), tableValuesMap);
			isValid = NbaTableConstants.EMPTY_STRING.equalsIgnoreCase(mibCombinedCode) ? false : true;
			if (!isValid) {
				if (newCode.isECGCode()) {
					newCode.getValidationMessages().add(NbaConstants.ECG_CODE_INVALID);
				} else {
					newCode.getValidationMessages().add(NbaConstants.COMPLETE_CODE_INVALID);
				}
			}
			if (newCode.isECGCode()) {//APSL553 if ECG Code has ECG Symbols
				if (newCode.getCompleteCode().indexOf(NbaConstants.OPEN_DELIM_MIBCODE) > 0) {//has ECG Symbols
					int size = newCode.getEcgSymbols().size();
					for (int i = 0; i < size; i++) {
						String ecgTrans = processTranslation(NbaTableConstants.NBA_ECG_SYMBOLS, NbaTableAccessConstants.ECG_CODE, (String) newCode
								.getEcgSymbols().get(i), tableValuesMap);
						isValid = NbaTableConstants.EMPTY_STRING.equalsIgnoreCase(ecgTrans) ? false : true;
						if (!isValid) {
							newCode.getValidationMessages().add(NbaConstants.ECG_SUB_CODE_INVALID);
							break;
						}
					}
				}
			}else {
				if (newCode.getCompleteCode().indexOf(NbaConstants.OPEN_DELIM_MIBCODE) > 0) {//has site code
					String siteCode = processTranslation(NbaTableConstants.NBA_SITE_CODES, NbaTableAccessConstants.SITE_CODE, newCode.getSiteCode(),
							tableValuesMap);
					isValid = NbaTableConstants.EMPTY_STRING.equalsIgnoreCase(siteCode) ? false : true;
					if (!isValid) {
						newCode.getValidationMessages().add(NbaConstants.SITE_CODE_INVALID);
					}
				}
			}
		}
		return isValid;
	}

	/**
	 * Validates if the value is a number or not
	 * @param value
	 * @return boolean
	 */
	protected boolean isValidNumber(String value) {
		String code = value.replace('?', '0');
		try {
			Long.parseLong(code);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/**
	 * Sets the table values for accessing the tables
	 * @param newCode
	 * @return Hashmap
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	protected HashMap setTableValues(NbaMIBTranslatorVO newCode) throws NbaBaseException {
		if (table == null) { //APSL553
			table = new NbaTableAccessor();
		}
		HashMap map = table.createDefaultHashMap(NbaTableAccessConstants.WILDCARD);
		map.put(NbaTableAccessConstants.CURRENT_DATE, new Date().toString());
		return map;
	}

	/**
	 * Responsible to fetch the actual translation for given table values
	 * @param tableName
	 * @param columnName
	 * @param columnValue
	 * @param tableValuesMap
	 * @return String
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	protected String processTranslation(String tableName, String columnName, String columnValue, HashMap tableValuesMap) throws NbaBaseException {
		if (table == null) {//APSL553
			table = new NbaTableAccessor();
		}
		tableValuesMap.put(columnName, columnValue);
		return table.getTranslationForMIBCode(tableName, tableValuesMap);
	}

	/**
	 * Sets the translated strings for the code
	 * @param newCode
	 * @return boolean
	 * @exception com.csc.fsg.nba.exception.NbaBaseException
	 */
	protected void setTranslations(NbaMIBTranslatorVO newCode) throws NbaBaseException {
		StringBuffer shortTran = new StringBuffer();
		StringBuffer compTran = new StringBuffer();
		String mibTrans = "";
		String siteTrans = "";
		String ecgTrans = "";
		boolean isValid = true; //QC12188/APSL3400
		
		HashMap tableValuesMap = setTableValues(newCode);
		String timeTrans = processTranslation(NbaTableConstants.NBA_TIME_CODES, NbaTableAccessConstants.TIME_CODE, newCode.getTimeCode(),
				tableValuesMap);
		if (newCode.isHeightWeight()) {
			shortTran.append(newCode.getHeightFeet()).append("'").append(newCode.getHeightInches()).append("\" ").append(newCode.getWeight()).append(
					NbaConstants.WEIGHT_MEASURE).append(timeTrans);
		} else {
			mibTrans = processTranslation(NbaTableConstants.NBA_MIB_CODES, NbaTableAccessConstants.MIB_CODE, newCode.getMibCode(), tableValuesMap);
			shortTran.append(mibTrans).append(" ").append(timeTrans);
		}
		newCode.setShortTranslation(shortTran.toString().trim());

		String modifierTrans = processTranslation(NbaTableConstants.NBA_MODIFIERDEGREE_CODES, NbaTableAccessConstants.MODIFIER_DEGREE_CODE, newCode
				.getModifierCode(), tableValuesMap);
		String sourceTrans = processTranslation(NbaTableConstants.NBA_SOURCE_CODES, NbaTableAccessConstants.SOURCE_CODE, newCode.getSourceCode(),
				tableValuesMap);

		if (newCode.isHeightWeight()) {
			compTran = compTran.append(newCode.getHeightFeet()).append("'").append(newCode.getHeightInches()).append("\" ").append(
					newCode.getWeight()).append(NbaConstants.WEIGHT_MEASURE).append(modifierTrans).append(" ").append(sourceTrans).append(" ")
					.append(timeTrans).append(NbaConstants.WEIGHT_LOSS).append(newCode.getWeightLoss());
		} else {
			if (!NbaUtils.isBlankOrNull(newCode.getSiteCode())) {
				siteTrans = processTranslation(NbaTableConstants.NBA_SITE_CODES, NbaTableAccessConstants.SITE_CODE, newCode.getSiteCode(),
						tableValuesMap);
			}
			if (!NbaUtils.isBlankOrNull(newCode.getEcgCodes())) {
				int size = newCode.getEcgSymbols().size();
				for (int i = 0; i < size; i++) {
					ecgTrans = ecgTrans
							+ " "
							+ processTranslation(NbaTableConstants.NBA_ECG_SYMBOLS, NbaTableAccessConstants.ECG_CODE, (String) newCode
									.getEcgSymbols().get(i), tableValuesMap);
				}
			}
			//Begin QC12188/APSL3400
			String mibCombinedCode = processTranslation(NbaTableConstants.NBA_MIB_COMBINED_CODES, NbaTableAccessConstants.COMBINED_CODE, newCode
					.getCompleteCode().substring(0, 6), tableValuesMap);
			isValid = NbaTableConstants.EMPTY_STRING.equalsIgnoreCase(mibCombinedCode) ? false : true;
			if (!isValid) {
				compTran = compTran.append(mibTrans).append(" ").append(modifierTrans).append(" ").append(sourceTrans).append(" ").append(timeTrans)
						.append(" ").append(siteTrans).append(ecgTrans);
			}else{
				compTran = compTran.append(mibCombinedCode).append(" ").append(siteTrans).append(ecgTrans);			
			}
			//End QC12188/APSL3400
		}
		newCode.setCompleteTranslation(compTran.toString().trim());
	}
	
	/**
	 * Return <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return com.csc.fsg.nba.foundation.NbaLogger
	 */
	protected NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass());
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
}
