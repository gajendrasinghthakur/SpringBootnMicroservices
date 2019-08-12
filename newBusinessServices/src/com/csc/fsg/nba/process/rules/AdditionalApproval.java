package com.csc.fsg.nba.process.rules;

import java.lang.reflect.Field;
import com.csc.fsg.nba.database.AxaRulesDataBaseAccessor;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.datamanipulation.NbaOinkRequest;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaConstants;

/* 
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group�.  The use,<BR>
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
 * NBLXA-2085 Arcos changes
 */


public class AdditionalApproval {

	public int A_FirstExtraTableRating_PINS = 0;

	public int A_FirstExtraAmt_PINS = 0;

	public int A_FaceAmountLOB;
	
	public String A_CompanyLOB="";

	public String A_RateClass_PINS="";
	
	public String A_Disposition_LVL1 = null;
	
	public String A_IssueAge_PINS;
	
	public String A_ProductTypSubtypLOB="";

	public String A_PlanTypeLOB="";

	public String MaxIssueAge;

	public String RateClass;
	
	public String AllowedCompany;

	public String TermOrPermCase;

	public String NextStatus;

	public String NextQueue;

	AxaRulesDataBaseAccessor dbAccessor;

	NbaOinkDataAccess oinkData;

	public AdditionalApproval(NbaOinkDataAccess oink) {
		oinkData = oink;
		dbAccessor = AxaRulesDataBaseAccessor.getInstance();
		initialize();
	}

	public void initialize() {
		NbaOinkRequest aNbaOinkRequest = new NbaOinkRequest();
		Field[] fields = this.getClass().getFields();
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].getName().startsWith("A_")) {
				aNbaOinkRequest.setVariable(fields[i].getName().substring(2));
				try {
					String[] oinkValues = oinkData.getStringValuesFor(aNbaOinkRequest);
					if (oinkValues.length > 0) {
						final Class<?> type = fields[i].getType();
						if (type.equals(Integer.TYPE)) {
							if (oinkValues[0].equalsIgnoreCase(NbaConstants.BLANK_STRING))
								fields[i].set(this, 0);
							else
								fields[i].set(this, Integer.parseInt(oinkValues[0]));

						} else if (type.equals(String.class)) {
							fields[i].set(this, oinkValues[0]);
						}
					}
				} catch (NbaBaseException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public int getA_FirstExtraTableRating_PINS() {
		return A_FirstExtraTableRating_PINS;
	}

	public void setA_FirstExtraTableRating_PINS(int a_FirstExtraTableRating_PINS) {
		A_FirstExtraTableRating_PINS = a_FirstExtraTableRating_PINS;
	}

	public int getA_FirstExtraAmt_PINS() {
		return A_FirstExtraAmt_PINS;
	}

	public void setA_FirstExtraAmt_PINS(int a_FirstExtraAmt_PINS) {
		A_FirstExtraAmt_PINS = a_FirstExtraAmt_PINS;
	}

	public int getA_FaceAmountLOB() {
		return A_FaceAmountLOB;
	}

	public void setA_FaceAmountLOB(int a_FaceAmountLOB) {
		A_FaceAmountLOB = a_FaceAmountLOB;
	}

	public String getMaxIssueAge() {
		return MaxIssueAge;
	}

	public void setMaxIssueAge(String maxIssueAge) {
		MaxIssueAge = maxIssueAge;
	}

	public String getRateClass() {
		return RateClass;
	}

	public void setRateClass(String rateClass) {
		RateClass = rateClass;
	}

	public String getAllowedCompany() {
		return AllowedCompany;
	}

	public void setAllowedCompany(String allowedCompany) {
		AllowedCompany = allowedCompany;
	}

	public String getTermOrPermCase() {
		return TermOrPermCase;
	}

	public void setTermOrPermCase(String termOrPermCase) {
		TermOrPermCase = termOrPermCase;
	}

	public String getNextStatus() {
		return NextStatus;
	}

	public void setNextStatus(String nextStatus) {
		NextStatus = nextStatus;
	}

	public String getA_Disposition_LVL1() {
		return A_Disposition_LVL1;
	}

	public void setA_Disposition_LVL1(String a_Disposition_LVL1) {
		A_Disposition_LVL1 = a_Disposition_LVL1;
	}

	public String getNextQueue() {
		return NextQueue;
	}

	public void setNextQueue(String nextQueue) {
		NextQueue = nextQueue;
	}

	public AxaRulesDataBaseAccessor getDbAccessor() {
		return dbAccessor;
	}

	public void setDbAccessor(AxaRulesDataBaseAccessor dbAccessor) {
		this.dbAccessor = dbAccessor;
	}

	public String getA_CompanyLOB() {
		return A_CompanyLOB;
	}

	public void setA_CompanyLOB(String a_CompanyLOB) {
		A_CompanyLOB = a_CompanyLOB;
	}
	
	public String getA_RateClass_PINS() {
		return A_RateClass_PINS;
	}

	public void setA_RateClass_PINS(String a_RateClass_PINS) {
		A_RateClass_PINS = a_RateClass_PINS;
	}

	public String getA_IssueAge_PINS() {
		return A_IssueAge_PINS;
	}

	public void setA_IssueAge_PINS(String a_IssueAge_PINS) {
		A_IssueAge_PINS = a_IssueAge_PINS;
	}
	
	public String getA_ProductTypSubtypLOB() {
		return A_ProductTypSubtypLOB;
	}

	public void setA_ProductTypSubtypLOB(String a_ProductTypSubtypLOB) {
		A_ProductTypSubtypLOB = a_ProductTypSubtypLOB;
	}

	public String getA_PlanTypeLOB() {
		return A_PlanTypeLOB;
	}

	public void setA_PlanTypeLOB(String a_PlanTypeLOB) {
		A_PlanTypeLOB = a_PlanTypeLOB;
	}
	
	
}
