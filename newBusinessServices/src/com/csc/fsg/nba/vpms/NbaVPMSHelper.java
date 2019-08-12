/*
 * *******************************************************************************<BR>
 * This program contains trade secrets and confidential information which are
 * proprietary to CSC Financial Services Group�.  The use, reproduction,
 * distribution or disclosure of this program, in whole or in part, without
 * the express written permission of CSC Financial Services Group is prohibited.
 * This program is also an unpublished work protected under the copyright laws
 * of the United States of America and other countries.
 *
 * If this program becomes published, the following notice shall apply:
 *    Property of Computer Sciences Corporation.<BR>
 *    Confidential. Not for publication.<BR>
 *    Copyright (c) 2002-2008 Computer Sciences Corporation. All Rights Reserved.<BR>
 * *******************************************************************************<BR>
 */

package com.csc.fsg.nba.vpms;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.csc.dip.jvpms.runtime.base.VpmsComputeResult;
import com.csc.fsg.nba.datamanipulation.NbaOinkDataAccess;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.exception.NbaVpmsException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaDst;
import com.csc.fsg.nba.vo.NbaLob;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.txlife.FormInstance;
import com.csc.fsg.nba.vpms.results.StandardAttr;
import com.csc.fsg.nba.vpms.results.VpmsModelResult;

/**
 * Helper class to perform VPMS calls.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA300</td><td>AXA Life Phase 2</td><td>Term Conversion</td></tr>
 * <tr><td>APSL1144</td><td>Discretionary</td><td>New App Rebating</td></tr>
 * <tr><td>SR515496</td><td>Discretionary</td><td>New App</td></tr>
 * <tr><td>CR735253-735254</td><td>AXA Life Phase 2</td><td>Reinsurance Interface</td></tr>
 * <tr><td>SR657319</td><td>Discretionary</td><td>Manual selection of Rate Class</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 1
 */
public class NbaVPMSHelper implements NbaVpmsConstants {
	
	protected NbaLogger logger = null; 			
	
	public List getLOBsToCopy(String task) throws NbaBaseException {
		VpmsModelResult vpmsModelResult = new VpmsModelResult();
		NbaOinkDataAccess oinkData = new NbaOinkDataAccess();
		NbaVpmsAdaptor nbaVpmsAdaptor = null;
		List lobList = new ArrayList();
        try {
            nbaVpmsAdaptor = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.AUTO_PROCESS_STATUS);
            nbaVpmsAdaptor.setVpmsEntryPoint(NbaVpmsConstants.EP_GET_LOBS_TO_COPY);	  
            nbaVpmsAdaptor.getSkipAttributesMap().put("A_TASKNAME", task);
            try {
                VpmsComputeResult vpmsComputeResult = nbaVpmsAdaptor.getResults();
    			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(vpmsComputeResult);
    			// check if the vpms call was successfull
    			if (vpmsResultsData.wasSuccessful()) {
    				// got the xml result back
    				String xmlString = (String) vpmsResultsData.getResultsData().get(0);
    				// parsing the xml result
    				NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
    				vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
    			}
    			ArrayList strAttrs = vpmsModelResult.getStandardAttr();
                
                StandardAttr standardAttr = null;
    			for (int i = 0; i < strAttrs.size(); i++) {
    				standardAttr = (StandardAttr)strAttrs.get(i);
    				lobList.add(standardAttr.getAttrValue());
    			}
            } catch (RemoteException e) {
                throw new NbaVpmsException(e);
            }
        } finally {
            if (nbaVpmsAdaptor != null) {
                try {
                    nbaVpmsAdaptor.remove();
                } catch (RemoteException e) {
                    getLogger().logError(NbaBaseException.VPMS_REMOVAL_FAILED);
                }
            }
        }
		return lobList;
	}	
	
	protected NbaLogger getLogger() {//NBA103
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(this.getClass()); //NBA103
			} catch (Exception e) {
				NbaBootLogger.log(this.getClass().getName() + " could not get a logger from the factory."); //NBA103
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	/*
	 * This method check form no of miscMailSource .
	 */
	//New Method ALS3828, NA_AXAL004
	public static boolean isSupplementTabForm(NbaLob reqLob) throws NbaBaseException {
		boolean isValidForm = false;
		NbaVpmsAdaptor proxy = null;
		try {
			proxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(reqLob), NbaVpmsConstants.CONTRACT_PRINT_EXTRACT_TYPES);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_CHECK_CONTRACT_PRINT_FORM_NO);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				if (NbaConstants.TRUE_STR.equalsIgnoreCase((String) vpmsResultsData.getResultsData().get(0))) {
					isValidForm = true;
				}
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
				    NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);

				}
			}
		}
		return isValidForm;
	}

	//NA_AXAL004 new method, A3_AXAL002	
	public static boolean isNewAppFormNumber(NbaTXLife txLife) throws NbaBaseException {
		return isFormNumberForApp(txLife, NbaVpmsConstants.EP_CHECK_NEW_APP_FORM_NO);		
	}
	
	//SR515496 New Method New App Retrofit
	//Term Conv Rewrite  Renamed Method
	public static boolean isNewAppFormNumber(NbaDst dst) throws NbaBaseException {
		return isFormNumberForApp(dst, NbaVpmsConstants.EP_CHECK_NEW_APP_FORM_NO);
	}
	// A3_AXAL002 new method	
	public static boolean isTConvFormNumber(NbaTXLife txLife) throws NbaBaseException {
		return isFormNumberForApp(txLife, NbaVpmsConstants.EP_CHECK_TCONV_FORM_NO);		
	}
	
	//Term Conv Rewrite  New Method	
	public static boolean isTConvFormNumber(NbaDst dst) throws NbaBaseException {
		return isFormNumberForApp(dst, NbaVpmsConstants.EP_CHECK_TCONV_FORM_NO);
	}
	//	APSL2808  New Method	
	public static boolean isSIAppFormNumber(NbaTXLife txLife) throws NbaBaseException {
		return isFormNumberForApp(txLife, NbaVpmsConstants.EP_CHECK_SIAPP_FORM_NO);		
	}
	
	public static boolean isSIAppFormNumber(NbaDst dst) throws NbaBaseException {
		return isFormNumberForApp(dst, NbaVpmsConstants.EP_CHECK_SIAPP_FORM_NO);
	}
	
	//	APSL5318  New Method	
	public static boolean isGIAppFormNumber(NbaTXLife txLife) throws NbaBaseException {
		return isFormNumberForApp(txLife, NbaVpmsConstants.EP_CHECK_GIAPP_FORM_NO);		
	}
	
	//	NBLXA-1338 New Method	
	public static boolean isGILTCFormNumber(FormInstance formInstance) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			if (NbaConstants.FORM_NAME_LTCSUPP.equalsIgnoreCase(formInstance.getFormName()) && formInstance.hasProviderFormNumber()
					&& !NbaUtils.isBlankOrNull(formInstance.getProviderFormNumber())) {
				Map deOink = new HashMap();
				deOink.put("A_FormNumber", formInstance.getProviderFormNumber());
				proxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(), NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
				proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_CHECK_GI_LTCSUPP);
				proxy.setSkipAttributesMap(deOink);
				NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
				if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
					return NbaConstants.TRUE == Integer.parseInt((String) vpmsResultsData.getResultsData().get(0));
				}
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);
				}
			}
		}
		return false;		
	}
	
	public static boolean isGIAppFormNumber(NbaDst dst) throws NbaBaseException {
		return isFormNumberForApp(dst, NbaVpmsConstants.EP_CHECK_GIAPP_FORM_NO);
	}
	
	// A3_AXAL002 new method	
	public static boolean isAMIGVFormNumber(NbaTXLife txLife) throws NbaBaseException {
		return isFormNumberForApp(txLife, NbaVpmsConstants.EP_CHECK_AMIGV_FORM_NO);		
	}
	/**
	 * This method gets all Term Conv deOink variables by calling CONVERSIONUNDERWRITING model	 
	 * @param 
	 * @return java.util.Map : The Hash Map containing all the deOink variables 	
	 * @throws NbaBaseException
	 */
	//NBA300 new method
	public static void deOinkTermConvData(Map deOink, NbaTXLife txLife, NbaLob nbaLob) throws NbaBaseException {
		//Calling a new Term Conversion model to determine if underwriting is required based on certain data 
		//entered on the Replacement view for the term conversion. This model will return two pieces of data:
		//(a)a Boolean to indicate if underwriting is required, (b) a conversion increase amount to be used for underwriting, if applicable
		//The vpmsadaptor object, which provides an interface into the VPMS system
	    NbaVpmsAdaptor vpmsProxy = null;
	    String entryPoint = EP_RESULTXML;
	    try {
			NbaOinkDataAccess oinkData = new NbaOinkDataAccess(txLife);
			oinkData.setContractSource(txLife); //ALII997
			vpmsProxy = new NbaVpmsAdaptor(oinkData, NbaVpmsAdaptor.CONVERSIONUNDERWRITING); 
			vpmsProxy.setVpmsEntryPoint(entryPoint);
			VpmsComputeResult result = vpmsProxy.getResults();
			String undReqdValue = "true";
			String convIncrAmtValue = "0";
			if (!result.isError()) {
	            NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(result);
	            List rulesList = vpmsResultsData.getResultsData();
	            if (!rulesList.isEmpty()) {
	                String xmlString = (String) rulesList.get(0);
		            NbaVpmsModelResult nbaVpmsModelResult = new NbaVpmsModelResult(xmlString);
		            VpmsModelResult vpmsModelResult = nbaVpmsModelResult.getVpmsModelResult();
		            List strAttrs = vpmsModelResult.getStandardAttr();
		            //Generate delimited string if there are more than one parameters returned
		            Iterator itr = strAttrs.iterator();
	        		while (itr.hasNext()) {
		            	StandardAttr stdAttr = (StandardAttr) itr.next();
		            	if("UndRequired".equalsIgnoreCase(stdAttr.getAttrName())){
		            		undReqdValue = stdAttr.getAttrValue();
		            	} else if("ConvIncreaseAmt".equalsIgnoreCase(stdAttr.getAttrName())){
		            		convIncrAmtValue = stdAttr.getAttrValue();
		            	}
	                }
	            }
	        }
			deOink.put("A_UndRequired", undReqdValue);
			deOink.put("A_ConvIncreaseAmt", convIncrAmtValue);
			
		} catch (java.rmi.RemoteException re) {
			throw new NbaVpmsException("Term Conv deoink: " + NbaVpmsException.VPMS_EXCEPTION, re);
		} finally {
			try {
	            if (vpmsProxy != null) {
	                vpmsProxy.remove();
	            }
	        } catch (RemoteException re) {
	        	NbaLogFactory.getLogger("NbaVPMSHelper").logError(NbaBaseException.VPMS_REMOVAL_FAILED);
	        }
		}
	}
	
	
	// A3_AXAL002 new method
	public static boolean isFormNumberForApp(NbaTXLife txLife, String entryPoint) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			proxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(txLife), NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				return NbaConstants.TRUE == Integer.parseInt((String) vpmsResultsData.getResultsData().get(0));
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);
				}
			}
		}
		return false;
	}
	//SR515496 New Method New App Retrofit
	public static boolean isFormNumberForApp(NbaDst dst, String entryPoint) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			Map deOink = new HashMap();
			deOink.put("A_HOAppFormNumber",NbaUtils.isBlankOrNull(dst.getNbaLob().getFormNumber()) ? "" : dst.getNbaLob().getFormNumber());
			proxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(),NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(entryPoint);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				return NbaConstants.TRUE == Integer.parseInt((String) vpmsResultsData.getResultsData().get(0));
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);
				}
			}
		}
		return false;
	}
	
	//APSL1144 - New Method	New App Rebating Retrofit
	public static boolean isNewAppRevFormNumber(NbaTXLife txLife) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			proxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(txLife), NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_CHECK_NEW_APP_REV_FORM_NO); 
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				return NbaConstants.TRUE == Integer.parseInt((String) vpmsResultsData.getResultsData().get(0));
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);
				}
			}
		}
		return false;
	}
	
//	CR735253-735254 new method
	/**
	 * Mothod used to retrieve Exclusion Requirements List from VPMS 
	 * @param NbaTXLife
	 * @throws NbaBaseException
	 * @return ArrayList
	 */
	public static ArrayList getExclusionRequirements(NbaTXLife txLife) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			Map deOink = new HashMap();
            deOink.put("A_Delimiter", NbaVpmsAdaptor.VPMS_DELIMITER[0]); 
			proxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(txLife), NbaVpmsConstants.REINSURANCE);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_REINCORR_EXCLUSION_REQ); 
			proxy.setSkipAttributesMap(deOink);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				return vpmsResultsData.getResultsData();
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);
				}
			}
		}
		return null;
	}
	
	// P2AXAL068 new method	
	public static boolean isGIFormNumber(NbaTXLife txLife) throws NbaBaseException {
		return isFormNumberForApp(txLife, NbaVpmsConstants.EP_CHECK_GI_FORM_NO);		
	}
	
	// SR657319 new method
	public static long getTobaccoPremiumBasisFromRateClass(NbaTXLife nbaTxLife, String rateClassApprovedAt) throws NbaBaseException {		
		NbaVpmsAdaptor proxy = null;
		Map deOink = new HashMap();
		try {
			NbaOinkDataAccess nbaOinkDataAccess = new NbaOinkDataAccess(nbaTxLife);
			deOink.put("A_RATECLASSAPPROVEDAT", rateClassApprovedAt);
			proxy = new NbaVpmsAdaptor(nbaOinkDataAccess, NbaVpmsConstants.CONTRACTVALIDATIONCALCULATIONS);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_TOBACCOPREMIUMBASIS_FROM_RATECLASS);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				return Long.parseLong((String)vpmsResultsData.getResultsData().get(0));
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} catch (NbaBaseException e) {
			throw new NbaBaseException("NbaBaseException Exception occured while processing VP/MS request", e);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);
				}
			}
		}
		return NbaConstants.LONG_NULL_VALUE;
	}
	
	public static double getPremiumDue(NbaTXLife txLife) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			proxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(txLife), NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_PREMIUMDUE);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				String dueAmtStr = ((String) vpmsResultsData.getResultsData().get(0));
				return Double.parseDouble(dueAmtStr.substring(0, dueAmtStr.indexOf('@')));
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);
				}
			}
		}
		return Double.NaN;
	}
	
	//APSL4685 New Method
	public static boolean isWIForPICMPrintNigo(NbaDst dst) throws NbaBaseException {
		NbaVpmsAdaptor proxy = null;
		try {
			Map deOink = new HashMap();
			deOink.put("A_PICMStatus",NbaUtils.isBlankOrNull(dst.getNbaLob().getStatus()) ? "" : dst.getNbaLob().getStatus());
			proxy = new NbaVpmsAdaptor(new NbaOinkDataAccess(),NbaVpmsConstants.CONTRACTVALIDATIONCOMPANYCONSTANTS);
			proxy.setSkipAttributesMap(deOink);
			proxy.setVpmsEntryPoint(NbaVpmsConstants.EP_CHK_PICM_PRINT_NIGO);
			NbaVpmsResultsData vpmsResultsData = new NbaVpmsResultsData(proxy.getResults());
			if (vpmsResultsData != null && vpmsResultsData.getResultsData() != null) {
				return NbaConstants.TRUE == Integer.parseInt((String) vpmsResultsData.getResultsData().get(0));
			}
		} catch (RemoteException t) {
			throw new NbaBaseException("Remote Exception occured while processing VP/MS request", t);
		} finally {
			if (proxy != null) {
				try {
					proxy.remove();
				} catch (RemoteException re) {
					NbaLogFactory.getLogger("NbaVPMSHelper").logError(re);
				}
			}
		}
		return false;
	}
	
	// BEGIN NBLXA-2132
	public static boolean isA4A5FormNumber(NbaTXLife txLife) throws NbaBaseException {
		return isFormNumberForApp(txLife, NbaVpmsConstants.EP_CHECK_A4_A5_FORM_NO);
	}
	// END NBLXA-2132
}
	

