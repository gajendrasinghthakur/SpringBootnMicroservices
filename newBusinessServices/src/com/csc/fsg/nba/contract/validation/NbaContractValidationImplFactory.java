package com.csc.fsg.nba.contract.validation;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.configuration.ValidationClass;
import com.csc.fsg.nba.vo.configuration.ValidationImpls;
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
/**
 * Contract validation factory.
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
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaContractValidationImplFactory {
	/**
	 * Constructor for NbaContractValidationImplFactory.
	 */
	public NbaContractValidationImplFactory() {
		super();
	}
	/**
	 * Answer the Contract Validation implementation. It may be either a base or a user implemetation. 
	 * If both are present, the base implemenation encapsulates the user implementation.
	 */
	public static NbaContractValidationImpl getImplementation(NbaTXLife nbaTXLife, Integer subset) throws NbaBaseException {
		NbaContractValidationImpl nbaContractValidationImpl = null;
		ValidationImpls allImpls = null;	//ACN012
		ValidationClass implClass = null;	//ACN012
		//ACN012 CODE DELETED
		try {
			allImpls = NbaConfiguration.getInstance().getValidationImpls();	//ACN012
		} catch (NbaBaseException e) {
			postErrorForImplementationNotDefined("Subset: " + subset + " configuration implementation information missing");
		}
		// ACN012 code deleted
		implClass = NbaConfiguration.getInstance().getValidationClass(subset.intValue(), nbaTXLife.getBackendSystem());	//ACN012
		if (implClass == null) {
			postErrorForImplementationNotDefined("Subset: " + subset + " configuration information missing for subset");
		}
		String baseImplName = getBaseClassNameFrom(allImpls, implClass);
		String userImplName = getUserClassNameFrom(allImpls, implClass);
		if (baseImplName.length() > 0) {
			try {
				nbaContractValidationImpl = (NbaContractValidationImpl) NbaUtils.classForName(baseImplName).newInstance();
			} catch (Exception e) {
				postErrorForImplementationNotDefined("Subset: " + subset + ", Class not found " + baseImplName);
			}
			if (userImplName.length() > 0) {
				try {
					NbaContractValidationImpl userImpl = (NbaContractValidationImpl) NbaUtils.classForName(userImplName).newInstance();
					((NbaContractValidationBaseImpl) nbaContractValidationImpl).setUserImplementation(userImpl);
				} catch (Exception e) {
					postErrorForImplementationNotDefined("Subset: " + subset + ", Class not found " + userImplName);
				}
			}
		} else if (userImplName.length() > 0) {
			try {
				nbaContractValidationImpl = (NbaContractValidationImpl) NbaUtils.classForName(userImplName).newInstance();
			} catch (Exception e) {
				postErrorForImplementationNotDefined("User implementation class not found " + userImplName);
			}
		} else {
			postErrorForImplementationNotDefined("No implementation classes defined for subset " + subset);
		}
		return nbaContractValidationImpl;
	}
	public static void postErrorForImplementationNotDefined(String reason) throws NbaBaseException {
		StringBuffer buf = new StringBuffer();
		buf.append("Validation implementation error: ");
		buf.append(reason);
		throw new NbaBaseException(buf.toString());
	}
	/**
	 * Answer the Base System implementation class name. If the base class from the NbaConfigValidationClass entry
	 * contains a period, assume that it contains the package name.  Otherwise pre-pend it with
	 * the base package name from NbaConfigValidationImpls.
	 */
	// ACN012 changed signature
	protected static String getBaseClassNameFrom(ValidationImpls allImpls, ValidationClass implClass) {
		StringBuffer clazz = new StringBuffer();
		String baseImpl = implClass.getBaseimpl(); // ACN012
		if (baseImpl != null && baseImpl.length() > 0) { //ACN012
			if (baseImpl.indexOf(".") < 0) { //Pre-pend package name if missing
				clazz.append(allImpls.getNbaPackage().trim());
				clazz.append(".");
			}
			clazz.append(baseImpl.trim());
		}
		return clazz.toString();
	}
	/**
	 * Answer the User implementation class name. If the user class from the NbaConfigValidationClass entry
	 * contains a period, assume that it contains the package name.  Otherwise pre-pend it with
	 * the user package name from NbaConfigValidationImpls.
	 */
	// ACN012 changed signature
	protected static String getUserClassNameFrom(ValidationImpls allImpls, ValidationClass implClass) {
		StringBuffer clazz = new StringBuffer();
		String userImpl = implClass.getUserimpl(); // ACN012
		if (userImpl != null && userImpl.length() > 0) { // ACN012
			if (userImpl.indexOf(".") < 0) { //Pre-pend package name if missing
				clazz.append(allImpls.getUserPackage().trim());
				clazz.append(".");
			}
			clazz.append(userImpl.trim());
		}
		return clazz.toString();
	}
}
