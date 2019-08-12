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
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * 
 * *******************************************************************************<BR>
 */
import java.lang.reflect.Method;
import java.util.HashMap;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaDataAccessException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.ac.DefaultValues;
import com.csc.fsg.nba.vo.ac.SummaryValues;
import com.csc.fsg.nba.vo.txlife.Party;
/**
 * NbaUpdateAcdb stores information to an NbaAcdb object. A static initializer 
 * method generates a Map containing the variable names that may be used and the 
 * Method objects used to access them. Map entries are present for all methods of 
 * the class whose method name starts with the string "store" and which accept an 
 * NbaOinkRequest as an argument. This Map of variables is returned to the 
 * NbaOinkDataAccess when the NbaAcdb destination is initialized.
 * 
 * Values from the NbaOinkRequest are stored in the NbaTXLife, up to the limit in 
 * the count field, into fields that satisfy the variable qualifier and filters. 
 * <p>  
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr><td>ACP002</td><td>Version 4</td><td> IU - Summary Information Model-Driver</td></tr>
 * <tr><td>SPR2741</td><td>Version 6</td><td>Re-evaluation is generating insert errors</td></tr>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * <tr><td>SPR3290</td><td>Version 7</td><td>General source code clean up during version 7</td></tr>
 * </table>
 * </p>
 * @author CSC FSG Developer
 * @version 7.0.0
 *  @see com.csc.fsg.nba.datamanipulation.NbaContractDataAccess
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess
 *  @see com.csc.fsg.nba.datamanipulation.NbaOinkRequest
 * @since New Business Accelerator - Version 2 
 */
public class NbaUpdateAcdb extends NbaContractDataAccess {
	static HashMap variables = new HashMap();
	private static NbaLogger logger = null;
	// SPR3290 code deleted
	/**
	 * This method initializes superclass objects.
	 * @param nbaTXLife com.csc.fsg.nba.vo.NbaTXLife
	 */
	protected void initializeObjects(NbaTXLife nbaTXLife) throws NbaBaseException {
		if (nbaTXLife == null) {
			throw new NbaBaseException("Invalid NbaTXLife");
		}
		setOLifE(nbaTXLife);
		setNbaTXLife(nbaTXLife); //NBA053
		initPartyIndices();
		setUpdateMode(false);
	}
	static {
		NbaUpdateAcdb aNbaUpdateAcdb = new NbaUpdateAcdb();
		String thisClassName = aNbaUpdateAcdb.getClass().getName();
		Method[] allMethods = aNbaUpdateAcdb.getClass().getDeclaredMethods();
		for (int i = 0; i < allMethods.length; i++) {
			Method aMethod = allMethods[i];
			String aMethodName = aMethod.getName();
			if (aMethodName.startsWith("store")) {
				Class[] parmClasses = aMethod.getParameterTypes();
				if (parmClasses.length == 1 && parmClasses[0].getName().equals("com.csc.fsg.nba.datamanipulation.NbaOinkRequest")) {
					Object[] args = { thisClassName, aMethod };
					variables.put(aMethodName.substring(5).toUpperCase(), args);
				}
			}
		}
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
	 * @return variables HashMap of all update variables available in this class
	 */
	public static HashMap getVariables() {
		return variables;
	}
	/**
	 * @param map HashMap of all update variables available in this class
	 */
	public static void setVariables(HashMap map) {
		variables = map;
	}
	public DefaultValues createDefaultValues(Object args[]) {
		DefaultValues dv = new DefaultValues();
		if (args != null) {
			dv.setParentIdKey((String) args[0]);
			dv.setContractKey((String) args[1]);
			dv.setCompanyKey((String) args[2]);
			dv.setBackendKey((String) args[3]);
		}
		return dv;
	}
	public SummaryValues createSummaryValues(Object args[]) {
		SummaryValues sv = new SummaryValues();
		if (args != null) {
			sv.setParentIdKey((String) args[0]);
			sv.setContractKey((String) args[1]);
			sv.setCompanyKey((String) args[2]);
			sv.setBackendKey((String) args[3]);
		}
		return sv;
	}
	/**
	 * Obtain the value for DefaultGender
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeDefaultGender(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			DefaultValues dv = getNbaAcdb().getDefaultValues(args);
			if (dv != null) {
				dv.setDefaultGender(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateDefaultValues(dv);
			} else {
				dv = createDefaultValues(args);
				dv.setDefaultGender(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addDefaultValues(dv);
			}
		}
	}
	/**
	 * Obtain the value for DefaultCalculatedAge
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeDefaultCalculatedAge(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			DefaultValues dv = getNbaAcdb().getDefaultValues(args);
			if (dv != null) {
				dv.setDefaultCalculatedAge(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateDefaultValues(dv);
			} else {
				dv = createDefaultValues(args);
				dv.setDefaultCalculatedAge(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addDefaultValues(dv);
			}
		}
	}
	/**
	 * Obtain the value for DefaultApplicationSignDate
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeDefaultApplicationSignDate(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			DefaultValues dv = getNbaAcdb().getDefaultValues(args);
			if (dv != null) {
				dv.setDefaultApplicationSignDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateDefaultValues(dv);
			} else {
				dv = createDefaultValues(args);
				dv.setDefaultApplicationSignDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addDefaultValues(dv);
			}
		}
	}
	/**
	 * Obtain the value for DefaultApplicationStateCd
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeDefaultApplicationStateCd(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			DefaultValues dv = getNbaAcdb().getDefaultValues(args);
			if (dv != null) {
				dv.setDefaultApplicationStateCd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateDefaultValues(dv);
			} else {
				dv = createDefaultValues(args);
				dv.setDefaultApplicationStateCd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addDefaultValues(dv);
			}
		}
	}
	/**
	 * Obtain the value for DefaultTrialApplication
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeDefaultTrialApplication(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			DefaultValues dv = getNbaAcdb().getDefaultValues(args);
			if (dv != null) {
				dv.setDefaultTrialApplication(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateDefaultValues(dv);
			} else {
				dv = createDefaultValues(args);
				dv.setDefaultTrialApplication(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addDefaultValues(dv);
			}
		}
	}
	/**
	 * Obtain the value for SumBpAvgSystolic
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumBpAvgSystolic(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumBpAvgSystolic(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumBpAvgSystolic(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumBpAvgDiastolic
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumBpAvgDiastolic(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumBpAvgDiastolic(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumBpAvgDiastolic(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulseRest
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulseRest(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPulseRest(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPulseRest(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulseExercise
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulseExercise(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPulseExercise(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPulseExercise(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulsePostExercise
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulsePostExercise(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPulsePostExercise(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPulsePostExercise(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulseRestIrrInd
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulseRestIrrInd(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPulseRestIrrInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPulseRestIrrInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulseExerciseIrrInd
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulseExerciseIrrInd(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPulseExerciseIrrInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPulseExerciseIrrInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulsePostExerciseIrrInd
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulsePostExerciseIrrInd(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPulsePostExerciseIrrInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPulsePostExerciseIrrInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumHeight
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumHeight(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumHeight(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumHeight(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumWeight
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumWeight(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumWeight(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumWeight(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumCigaretteHabitText
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumCigaretteHabitText(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumCigaretteHabitTxt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumCigaretteHabitTxt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTobaccoHabitText
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTobaccoHabitText(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTobaccoHabitTxt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTobaccoHabitTxt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcDataInd
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcDataInd(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcDataInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcDataInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFvcAct
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPrFvcAct(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPrFvcAct(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPrFvcAct(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFevAct
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPrFevAct(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPrFevAct(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPrFevAct(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFvcAct
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPoFvcAct(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPoFvcAct(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPoFvcAct(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFevAct
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPoFevAct(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				//sv.setSumTvcPoFevAct(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				//sv.setSumTvcPoFevAct(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPreInter
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPreInter(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				//sv.setSumTvcPreInter(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				//sv.setSumTvcPreInter(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPosInter
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPosInter(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				//sv.setSumTvcPosInter(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				//sv.setSumTvcPosInter(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFvcPec
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPrFvcPec(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPrFvcPec(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPrFvcPec(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFevPec
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPrFevPec(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPrFevPec(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPrFevPec(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFvcPec
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPoFvcPec(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPoFvcPec(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPoFvcPec(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFevPec
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPoFevPec(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPoFevPec(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPoFevPec(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumNicotineTxt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumNicotineTxt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumNicotineTxt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumNicotineTxt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumNicotineCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumNicotineCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumNicotineCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumNicotineCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumNicotineAvg
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumNicotineAvg(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumNicotineAvg(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumNicotineAvg(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumHivTxt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumHivTxt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumHivTxt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumHivTxt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumChrolCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumChrolCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumChrolCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumChrolCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumChrolDate
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumChrolDate(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumChrolDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumChrolDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumHdlChrolCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumHdlChrolCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumHdlChrolCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumHdlChrolCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumHdlChrolDate
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumHdlChrolDate(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumHdlChrolDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumHdlChrolDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumChlHdlCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumHdlChlCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumHdlChlCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumHdlChlCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumHdlChlDate
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumHdlChlDate(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumHdlChlDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumHdlChlDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumCotinineSal
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumCotinineSal(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumCotinineSal(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumCotinineSal(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumDbsChrolTxt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumDbsChrolTxt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
			Party party = getParty(aNbaOinkRequest, 0);
			if (party != null) {
				Object [] args = {party.getId(),party.getContractKey(),party.getCompanyKey(),party.getBackendKey()};
				SummaryValues sv = getNbaAcdb().getSummaryValues(args);
				if (sv != null){
					sv.setSumDbsChrolTxt(aNbaOinkRequest.getStringValue());
					getNbaAcdb().updateSummaryValues(sv);				
				}else{
					sv = createSummaryValues(args);
					sv.setSumDbsChrolTxt(aNbaOinkRequest.getStringValue());
					getNbaAcdb().addSummaryValues(sv);
				}
			}
		}		
	/**
	 * Obtain the value for SumDbsChrolDate
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumDbsChrolDate(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumDbsChrolDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumDbsChrolDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumBuildRequirement
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumBuildRequirement(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumBuildRequirement(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumBuildRequirement(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumBuildRequirementDate
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumBuildRequirementDate(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumBuildRequirementDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumBuildRequirementDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumWeightChange
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumWeightChange(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumWeightChange(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumWeightChange(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumWeightChangeInd
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumWeightChangeInd(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumWeightChangeInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumWeightChangeInd(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumCigaretteUsage
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumCigaretteUsage(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumCigaretteUsage(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumCigaretteUsage(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumCigaretteDate
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumCigaretteDate(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object [] args = {party.getId(),party.getContractKey(),party.getCompanyKey(),party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null){
				sv.setSumCigaretteDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);				
			}else{
				sv = createSummaryValues(args);
				sv.setSumCigaretteDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}		
	/**
	 * Obtain the value for SumTobaccoDate
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTobaccoDate(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTobaccoDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTobaccoDate(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulseRestCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulseRestCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPulseRestCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPulseRestCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulseExerciseCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulseExerciseCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPulseExerciseCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPulseExerciseCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPulsePostExerciseCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPulsePostExerciseCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object [] args = {party.getId(),party.getContractKey(),party.getCompanyKey(),party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null){
				sv.setSumPulsePostExerciseCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);				
			}else{
				sv = createSummaryValues(args);
				sv.setSumPulsePostExerciseCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}		
	/**
	 * Obtain the value for SumBpAvgSystolicCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumBpAvgSystolicCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumBpAvgSystolicCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumBpAvgSystolicCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumBpAvgDiastolicCnt
	 * @param aNbaOinkRequest - data request container
	 */
	//SPR2741 added throws clause
	public void storeSumBpAvgDiastolicCnt(NbaOinkRequest aNbaOinkRequest)throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumBpAvgDiastolicCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumBpAvgDiastolicCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumBpHighSystolic
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumBpHighSystolic(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumBpHighSystolic(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumBpHighSystolic(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumBpHighDiastolic
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumBpHighDiastolic(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumBpHighDiastolic(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumBpHighDiastolic(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFvcActCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPrFvcActCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPrFvcActCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPrFvcActCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFevActCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPrFevActCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPrFevActCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPrFevActCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFvcPecCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPrFvcPecCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPrFvcPecCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPrFvcPecCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPrFevPecCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPrFevPecCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPrFevPecCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPrFevPecCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFvcActCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPoFvcActCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPoFvcActCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPoFvcActCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFevActCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPoFevActCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPoFevActCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPoFevActCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFevPecCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPoFevPecCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPoFevPecCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPoFevPecCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTvcPoFvcPecCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTvcPoFvcPecCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTvcPoFvcPecCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTvcPoFvcPecCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumSmkSmoker
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumSmkSmoker(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumSmkSmoker(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumSmkSmoker(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumSmkTobacco
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumSmkTobacco(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumSmkTobacco(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumSmkTobacco(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumSmkHist
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumSmkHist(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumSmkHist(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumSmkHist(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumSmkQuitMths
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumSmkQuitMths(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumSmkQuitMths(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumSmkQuitMths(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumSmkStatus
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumSmkStatus(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumSmkStatus(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumSmkStatus(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumSmkPremium
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumSmkPremium(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumSmkPremium(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumSmkPremium(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyLife
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPolicyLife(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPolicyLife(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPolicyLife(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyADB
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPolicyADB(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPolicyADB(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPolicyADB(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyGIR
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPolicyGIR(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPolicyGIR(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPolicyGIR(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyAA
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPolicyAA(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPolicyAA(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPolicyAA(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyPremiumAmt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPolicyPremiumAmt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPolicyPremiumAmt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPolicyPremiumAmt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPolicyGIRUnitCnt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPolicyGIRUnitCnt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPolicyGIRUnitCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPolicyGIRUnitCnt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumPolicySinglePremium
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumPolicySinglePremium(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumPolicySinglePremium(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumPolicySinglePremium(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumInforceTotalLife
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumInforceTotalLife(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumInforceTotalLife(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumInforceTotalLife(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumInforceTotalADB
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumInforceTotalADB(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumInforceTotalADB(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumInforceTotalADB(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumInforceTotalWP
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumInforceTotalWP(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumInforceTotalWP(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumInforceTotalWP(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumInforceTotalRepLife
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumInforceTotalRepLife(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumInforceTotalRepLife(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumInforceTotalRepLife(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTotalAmtGIR
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTotalAmtGIR(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTotalAmtGIR(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTotalAmtGIR(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumApplicationAmt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumApplicationAmt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumApplicationAmt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumApplicationAmt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumRetentionAmt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumRetentionAmt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumRetentionAmt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumRetentionAmt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
	/**
	 * Obtain the value for SumTotalAmt
	 * @param aNbaOinkRequest - data request container
	 * @throws NbaDataAccessException
	 */
	//SPR2741 added throws clause
	public void storeSumTotalAmt(NbaOinkRequest aNbaOinkRequest) throws NbaDataAccessException {
		Party party = getParty(aNbaOinkRequest, 0);
		if (party != null) {
			Object[] args = { party.getId(), party.getContractKey(), party.getCompanyKey(), party.getBackendKey()};
			SummaryValues sv = getNbaAcdb().getSummaryValues(args);
			if (sv != null) {
				sv.setSumTotalAmt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().updateSummaryValues(sv);
			} else {
				sv = createSummaryValues(args);
				sv.setSumTotalAmt(aNbaOinkRequest.getStringValue());
				getNbaAcdb().addSummaryValues(sv);
			}
		}
	}
}
