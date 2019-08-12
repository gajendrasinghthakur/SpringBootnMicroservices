package com.csc.fsg.nba.datamanipulation; //NBA201

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
 *     Confidential. Not for protectedation.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */

import java.lang.reflect.Method;
import java.util.HashMap;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaAcdb;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.ac.DefaultValues;
import com.csc.fsg.nba.vo.ac.SummaryValues;

/**
 *  NbaRetrieveAcdbData retrieves information from an NbaAcdb object. A static 
 *  initializer method generates a Map containing the variable names that may be used 
 *  and the Method objects used to access them. Map entries are present for all methods
 *  of the class whose method name starts with the string "retrieve" and which accept
 *  an NbaOinkRequest as an argument. This Map of variables is returned to the 
 *  NbaOinkDataAccess when the NbaTXLife source is initialized.
 *
 *  When retrieving information, all values that satisfy the variable qualifier and 
 *  filter values are retrieved from the NbaAcdb, up to the limit in the count field. 
 *  Formatting information (phone, social security, etc) is also stored for use by 
 *  the formatter. If a table is associated with the field, the table name is also 
 *  stored.	
 * <p>  
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ACP002</td><td>Version 4</td><td> IU - Summary Information Model-Driver</td></tr>
 * <tr><td>ACP015</td><td>Version 4</td><td> Profile Evaluation</td></tr>
 * <tr><td>SPR2475</td><td>Version 5</td><td>Summary values related to the ACSumExam model ,ACSumBloodPressure,ACSumTvc are not correctly stored,when the requirement result is provided for the second time.</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * 
 * </table>
 * </p>
 * @author CSC FSG Developer
 * @version 7.0.0
 *  @see com.csc.fsg.nba.datamanipulation.NbaContractDataAccess
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 * @since New Business Accelerator - Version 4 
 */
public class NbaRetrieveAcdbData extends NbaContractDataAccess {
	static HashMap variables = new HashMap();
	private static NbaLogger logger = null;
	private NbaAcdb acdb;

	static {
		NbaRetrieveAcdbData aAcRetrieveAcDbData = new NbaRetrieveAcdbData();
		String thisClassName = aAcRetrieveAcDbData.getClass().getName();
		Method[] allMethods = aAcRetrieveAcDbData.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("retrieve")) {
				Class[] parmClasses = aMethod.getParameterTypes();
				if (parmClasses.length == 1 && parmClasses[0].getName().equals("com.csc.fsg.nba.datamanipulation.NbaOinkRequest")) {
					Object[] args = { thisClassName, aMethod };
					variables.put(aMethodName.substring(8).toUpperCase(), args);
				}
			}
		}
	}

	/**
	 * This method initializes superclass objects.
	 * @param objOLife com.csc.fsg.nba.vo.txlife.OLifE
	 */
	protected void initializeObjects(NbaTXLife nbaTXLife) throws NbaBaseException {
		if (nbaTXLife == null) {
			throw new NbaBaseException("Invalid NbaTXLife");
		}
		setOLifE(nbaTXLife);
		setNbaTXLife(nbaTXLife);  
		initPartyIndices();
		setUpdateMode(false);
	}

	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	protected static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaRetrieveContractData.class.getName());
			} catch (Exception e) {
				NbaBootLogger.log("NbaRetrieveAcdbData could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}

	/**
	 * Obtain the value for DefaultGender
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDefaultGender(NbaOinkRequest aNbaOinkRequest) {		
		Object[] args = aNbaOinkRequest.getArgs(); 
		if (args != null) {
			
			DefaultValues dv =  getNbaAcdb().getDefaultValues(args);
			if (dv != null){
				aNbaOinkRequest.addValue(dv.getDefaultGender());
			}
		}
	}
	/**
	 * Obtain the value for DefaultCalculatedAge
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDefaultCalculatedAge(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			DefaultValues dv =  getNbaAcdb().getDefaultValues(args);
			if (dv != null){
				aNbaOinkRequest.addValue(dv.getDefaultCalculatedAge());
			}
		}
	}	
	/**
	 * Obtain the value for DefaultApplicationSignDate
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDefaultApplicationSignDate(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			DefaultValues dv =  getNbaAcdb().getDefaultValues(args);
			if (dv != null){
				aNbaOinkRequest.addValue(dv.getDefaultApplicationSignDate());
			}
		}
	}	
	/**
	 * Obtain the value for DefaultApplicationStateCd
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDefaultApplicationStateCd(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			DefaultValues dv =  getNbaAcdb().getDefaultValues(args);
			if (dv != null){
				aNbaOinkRequest.addValue(dv.getDefaultApplicationStateCd());
			}
		}
	}	
	/**
	 * Obtain the value for DefaultTrialApplication
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveDefaultTrialApplication(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			DefaultValues dv =  getNbaAcdb().getDefaultValues(args);
			if (dv != null){
				aNbaOinkRequest.addValue(dv.getDefaultTrialApplication());
			}
		}
	}	
	
	/**
	 * Obtain the value for SumBpAvgSystolic
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumBpAvgSystolic(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumBpAvgSystolic());
			}
		}
	}

	/**
	 * Obtain the value for SumBpAvgDiastolic
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumBpAvgDiastolic(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumBpAvgDiastolic());
			}
		}
	}
	/**
	 * Obtain the value for SumPulseRest 
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulseRest(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPulseRest());
			}
		}
	}
	
	/**
	 * Obtain the value for SumPulseRestIrrInd 
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulseRestIrrInd(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				boolean value = sumValue.getSumPulseRestIrrInd();
				aNbaOinkRequest.addValue(value);
			}
		}
	}	
	
	/**
	 * Obtain the value for SumPulseExercise 
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulseExercise(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPulseExercise());
			}
		}
	}
	
	/**
	 * Obtain the value for SumPulseExerciseIrrInd 
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulseExerciseIrrInd(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				boolean value = sumValue.getSumPulseExerciseIrrInd();
				aNbaOinkRequest.addValue(value);				
			}
		}
	}	

	/**
	 * Obtain the value for SumPulsePostExercise 
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulsePostExercise(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPulsePostExercise());
			}
		}
	}
	
	/**
	 * Obtain the value for SumPulsePostExerciseIrrInd
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulsePostExerciseIrrInd(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				boolean value = sumValue.getSumPulsePostExerciseIrrInd();
				aNbaOinkRequest.addValue(value);
			}
		}
	}
	
	/**
	 * Obtain the value for SumHeight
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumHeight(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumHeight());
			}
		}
	}

	/**
	 * Obtain the value for SumWeight
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumWeight(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumWeight());
			}
		}
	}
	
	/**
	 * Obtain the value for SumCigaretteHabitTxt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumCigaretteHabitTxt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumCigaretteHabitTxt());
			}		
		}
	}		
	/**
	 * Obtain the value for SumTobaccoHabitTxt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTobaccoHabitTxt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTobaccoHabitTxt());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcDataInd
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcDataInd(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				boolean value = sumValue.getSumTvcDataInd();
				aNbaOinkRequest.addValue(value);
			}
		}
	}

	/**
	 * Obtain the value for SumTvcPrFvcAct
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPrFvcAct(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPrFvcAct());
			}
		}
	}
	
	/**
	 * Obtain the value for SumTvcPrFevAct
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPrFevAct(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPrFevAct());
			}
		}
	}
	
	/**
	 * Obtain the value for SumTvcPoFvcAct
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPoFvcAct(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPoFvcAct());
			}
		}
	}
	
	/**
	 * Obtain the value for SumTvcPoFevAct
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPoFevAct(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPoFevAct());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPreInter
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPreInter(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPreInter());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPosInter
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPosInter(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){				
				aNbaOinkRequest.addValue(sumValue.getSumTvcPosInter());
			}
		}
	}
	
	/**
	 * Obtain the value for SumTvcPrFvcPec
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPrFvcPec(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPrFvcPec());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFevPec
	 * @param aNbaOinkRequest - data request container
	 */
	//ACP015 new Method
	public void retrieveSumSmkQuitMonths(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumSmkQuitMths());
			}
		}
	}
	
	/**
	 * Obtain the value for SumSmkQuitMonths
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPrFevPec(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
		
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPrFevPec());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcP0FevPec
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPoFevPec(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPoFevPec());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFvcPec
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPoFvcPec(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPoFvcPec());
			}
		}
	}
	/**
	 * Obtain the value for SumNicotineTxt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumNicotineTxt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumNicotineTxt());
			}
		}
	}
	/**
	 * Obtain the value for SumNicotineCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumNicotineCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumNicotineCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumNicotineAvg
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumNicotineAvg(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumNicotineAvg());
			}
		}
	}
	/**
	 * Obtain the value for SumHivTxt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumHivTxt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumHivTxt());
			}
		}
	}
	/**
	 * Obtain the value for SumChrolCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumChrolCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumChrolCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumChrolDate
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumChrolDate(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumChrolDate());
			}
		}
	}
	/**
	 * Obtain the value for SumHdlChrolCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumHdlChrolCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumHdlChrolCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumHdlChrolDate
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumHdlChrolDate(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumHdlChrolDate());
			}
		}
	}
	/**
	 * Obtain the value for SumChlHdlCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumHdlChlCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumHdlChlCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumHdlChlDate
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumHdlChlDate(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumHdlChlDate());
			}
		}
	}
	/**
	 * Obtain the value for SumCotinineSal
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumCotinineSal(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumCotinineSal());
			}
		}
	}
	/**
	 * Obtain the value for SumDbsChrolTxt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumDbsChrolTxt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumDbsChrolTxt());
			}
		}
	}
	/**
	 * Obtain the value for SumDbsChrolDate
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumDbsChrolDate(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumDbsChrolDate());
			}
		}
	}
	/**
	 * Obtain the value for SumBuildRequirement
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumBuildRequirement(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumBuildRequirement());
			}
		}
	}
	/**
	 * Obtain the value for SumBuildRequirementDate
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumBuildRequirementDate(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumBuildRequirementDate());
			}
		}
	}
	/**
	 * Obtain the value for SumWeightChange
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumWeightChange(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumWeightChange());
			}
		}
	}
	/**
	 * Obtain the value for SumWeightChangeInd
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumWeightChangeInd(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumWeightChangeInd());
			}
		}
	}
	/**
	 * Obtain the value for SumCigaretteUsage
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumCigaretteUsage(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumCigaretteUsage());
			}
		}
	}
	/**
	 * Obtain the value for SumCigaretteDate
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumCigaretteDate(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumCigaretteDate());
			}
		}
	}
	/**
	 * Obtain the value for SumTobaccoDate
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTobaccoDate(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTobaccoDate());
			}
		}
	}
	/**
	 * Obtain the value for SumPulseRestCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulseRestCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPulseRestCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumPulseExerciseCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulseExerciseCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPulseExerciseCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumPulsePostExerciseCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPulsePostExerciseCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPulsePostExerciseCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumBpAvgSystolicCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumBpAvgSystolicCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumBpAvgSystolicCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumBpAvgDiastolicCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumBpAvgDiastolicCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumBpAvgDiastolicCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumBpHighSystolic
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumBpHighSystolic(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumBpHighSystolic());
			}
		}
	}
	/**
	 * Obtain the value for SumBpHighDiastolic
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumBpHighDiastolic(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumBpHighDiastolic());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFvcActCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPrFvcActCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPrFvcActCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFevActCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPrFevActCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPrFevActCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFvcPecCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPrFvcPecCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPrFvcPecCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFevPecCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPrFevPecCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPrFevPecCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFvcActCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPoFvcActCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPoFvcActCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFevActCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPoFevActCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPoFevActCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFevPecCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPoFevPecCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPoFevPecCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFvcPecCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTvcPoFvcPecCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTvcPoFvcPecCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumSmkSmoker
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumSmkSmoker(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				boolean value = sumValue.getSumSmkSmoker();
				aNbaOinkRequest.addValue(value);
			}
		}
	}
	/**
	 * Obtain the value for SumSmkTobacco
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumSmkTobacco(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				boolean value = sumValue.getSumSmkTobacco();
				aNbaOinkRequest.addValue(value);
			}
		}
	}
	/**
	 * Obtain the value for SumSmkHist
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumSmkHist(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumSmkHist());
			}
		}
	}
	/**
	 * Obtain the value for SumSmkQuitMths
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumSmkQuitMths(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumSmkQuitMths());
			}
		}
	}
	/**
	 * Obtain the value for SumSmkStatus
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumSmkStatus(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumSmkStatus());
			}
		}
	}
	/**
	 * Obtain the value for SumSmkPremium
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumSmkPremium(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumSmkPremium());
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyLife
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPolicyLife(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPolicyLife());
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyADB
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPolicyADB(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPolicyADB());
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyWP
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPolicyWP(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPolicyWP());
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyGIR
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPolicyGIR(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPolicyGIR());
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyAA
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPolicyAA(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPolicyAA());
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyPremiumAmt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPolicyPremiumAmt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPolicyPremiumAmt());
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyGIRUnitCnt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPolicyGIRUnitCnt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPolicyGIRUnitCnt());
			}
		}
	}
	/**
	 * Obtain the value for SumPolicySinglePremium
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumPolicySinglePremium(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumPolicySinglePremium());
			}
		}
	}
	/**
	 * Obtain the value for SumInforceTotalLife
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumInforceTotalLife(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumInforceTotalLife());
			}
		}
	}
	/**
	 * Obtain the value for SumInforceTotalADB
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumInforceTotalADB(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumInforceTotalADB());
			}
		}
	}
	/**
	 * Obtain the value for SumInforceTotalWP
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumInforceTotalWP(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumInforceTotalWP());
			}
		}
	}
	/**
	 * Obtain the value for SumInforceTotalRepLife
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumInforceTotalRepLife(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumInforceTotalRepLife());
			}
		}
	}
	/**
	 * Obtain the value for SumTotalAmtGIR
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTotalAmtGIR(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTotalAmtGIR());
			}
		}
	}
	/**
	 * Obtain the value for SumApplicationAmt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumApplicationAmt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumApplicationAmt());
			}
		}
	}
	/**
	 * Obtain the value for SumRetentionAmt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumRetentionAmt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumRetentionAmt());
			}
		}
	}
	/**
	 * Obtain the value for SumTotalAmt
	 * @param aNbaOinkRequest - data request container
	 */
	public void retrieveSumTotalAmt(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumTotalAmt());
			}
		}
	}
	
	/**
	 * Obtain the value for SumExamNormalAppearance
	 * @param aNbaOinkRequest - data request container
	 */
	//SPR2475 New Method
	public void retrieveSumExamNormalAppearance(NbaOinkRequest aNbaOinkRequest) {
		Object[] args = aNbaOinkRequest.getArgs();
		if (args != null) {
			SummaryValues sumValue = getNbaAcdb().getSummaryValues(args);
			if (sumValue != null){
				aNbaOinkRequest.addValue(sumValue.getSumExamNormalAppearance());
			}
		}
	}
					
	/**
	 * @return variables HashMap of all retrieve variables available in this class
	 */
	public static HashMap getVariables() {
		return variables;
	}

	/**
	 * @param map HashMap of all retrieve variables available in this class 
	 */
	public static void setVariables(HashMap map) {
		variables = map;
	}

}
