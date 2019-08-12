package com.csc.fsg.nba.business.contract.merge;
/**
 * ************************************************************** <BR>
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
 * ************************************************************** <BR>
 * 
 */
import java.util.Iterator;
import java.util.List;

import com.csc.fsg.nba.business.transaction.NbaContractChangeUtils;
import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBootLogger;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaContractVO;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.CovOption;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.EMailAddress;
import com.csc.fsg.nba.vo.txlife.LifeParticipant;
import com.csc.fsg.nba.vo.txlife.Party;
import com.csc.fsg.nba.vo.txlife.Phone;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RequirementInfo;

/** 
 * 
 * This class has various methods to match TXLife ACORD objects.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 *  <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>AXAL3.7.04</td><td>Axa Life Phase 1</td><td>Paid Changes</td></tr>
 * <tr><td>P2AXAL016CV</td><td>Axa Life Phase 2</td><td>Life 70 Calculations</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class Matcher {

	private static NbaLogger logger;

	/**
	 * @param copyToList
	 * @param copyFrom
	 * @return
	 * @throws NbaBaseException
	 */
	public static NbaContractVO match(List copyToList, NbaContractVO copyFrom) throws NbaBaseException {
		if(copyToList != null && copyFrom != null) {
			Iterator contractVOItr = copyToList.iterator();
			while(contractVOItr.hasNext()) {
				NbaContractVO contractVO = (NbaContractVO) contractVOItr.next();
				if(match(contractVO, copyFrom)) {
					return contractVO;
				}
			}
		}
		return null;
	}
	
	/**
	 * @param copyTo
	 * @param copyFrom
	 * @return
	 * @throws NbaBaseException
	 */
	public static boolean match(NbaContractVO copyTo, NbaContractVO copyFrom) throws NbaBaseException {
		if (!copyTo.getClass().getName().equals(copyFrom.getClass().getName())) {
			throw new NbaBaseException("Arguments not of same type");
		}
		if (copyTo instanceof Coverage) {
			return match((Coverage) copyTo, (Coverage) copyFrom);
		}else if(copyTo instanceof CovOption) {
			return match((CovOption) copyTo, (CovOption) copyFrom);
		}else if(copyTo instanceof LifeParticipant) {
			return match((LifeParticipant) copyTo, (LifeParticipant) copyFrom);
		}else if(copyTo instanceof Party) {
			return match((Party) copyTo, (Party) copyFrom);
		}else if(copyTo instanceof Relation) {
			return match((Relation) copyTo, (Relation) copyFrom);
		}else if(copyTo instanceof RequirementInfo) {
			return match((RequirementInfo) copyTo, (RequirementInfo) copyFrom);
		}else if(copyTo instanceof Address) {
            return match((Address) copyTo, (Address) copyFrom);
        }else if(copyTo instanceof Phone) {
            return match((Phone) copyTo, (Phone) copyFrom);
        }else if(copyTo instanceof EMailAddress) {
            return match((EMailAddress) copyTo, (EMailAddress) copyFrom);
        }
		return false;
	}
	
	/**
	 * @param covOption1
	 * @param covOption2
	 * @return
	 */
	public static boolean match(CovOption covOption1, CovOption covOption2) {
		if (NbaUtils.isEqual(covOption1.getLifeCovOptTypeCode(), covOption2.getLifeCovOptTypeCode())) { //P2AXAL016CV
			return true;
		}
		return false;
	}
	
	/**
	 * @param coverage1
	 * @param coverage2
	 * @return
	 */
	public static boolean match(Coverage coverage1, Coverage coverage2) {
		if (NbaUtils.isEqual(coverage1.getProductCode(), coverage2.getProductCode())
				&& NbaUtils.isEqual(coverage1.getIndicatorCode(), coverage2.getIndicatorCode())) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param lifePartcipant1
	 * @param lifePartcipant2
	 * @return
	 */
	public static boolean match(LifeParticipant lifePartcipant1, LifeParticipant lifePartcipant2) {
		if (NbaUtils.isEqual(lifePartcipant1.getLifeParticipantRoleCode(), lifePartcipant2.getLifeParticipantRoleCode())
				&& NbaUtils.isEqual(lifePartcipant1.getPartyID(), lifePartcipant2.getPartyID())) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param party1
	 * @param party2
	 * @return
	 */
	public static boolean match(Party party1, Party party2) {
		NbaParty nbAParty1 = new NbaParty(party1);
		NbaParty nbAParty2 = new NbaParty(party2);
		if ((nbAParty1.isPerson() && nbAParty2.isPerson())
				|| (party1.getPartyTypeCode() == NbaOliConstants.OLI_PT_PERSON && party2.getPartyTypeCode() == NbaOliConstants.OLI_PT_PERSON)) {
			if (NbaUtils.isEqual(nbAParty1.getSSN(), nbAParty2.getSSN())) {
				return true;
			}
			if (NbaUtils.isEqual(nbAParty1.getFirstName(), nbAParty2.getFirstName())
					&& NbaUtils.isEqual(nbAParty1.getLastName(), nbAParty2.getLastName())) {
				return true;
			}
			String party1FullName = nbAParty1.getFirstName() + " " + nbAParty1.getLastName();
			String party2FullName = nbAParty2.getFirstName() + " " + nbAParty2.getLastName();
			if (nbAParty1.getFirstName() == null && nbAParty1.getLastName() == null) {
				party1FullName = party1.getFullName();
			}
			if (nbAParty2.getFirstName() == null && nbAParty2.getLastName() == null) {
				party2FullName = party2.getFullName();
			}
			if (party1FullName != null && party1FullName.equalsIgnoreCase(party2FullName)) {
				return true;
			}
			if(NbaUtils.isEqual(nbAParty1.getWritingAgentId(), nbAParty2.getWritingAgentId())) {
				return true;
			}
		} else if (nbAParty1.isOrganization() && nbAParty2.isOrganization() && NbaUtils.isEqual(nbAParty1.getFullName(), nbAParty2.getFullName())) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param relation1
	 * @param relation2
	 * @return
	 */
	public static boolean match(Relation relation1, Relation relation2) {
		if (NbaUtils.isEqual(relation1.getOriginatingObjectType(), relation2.getOriginatingObjectType())
				&& NbaUtils.isEqual(relation1.getRelatedObjectType(), relation2.getRelatedObjectType())
				&& NbaUtils.isEqual(relation1.getRelationRoleCode(), relation2.getRelationRoleCode())
				&& NbaUtils.isEqual(relation1.getOriginatingObjectID(), relation2.getOriginatingObjectID())
				&& NbaUtils.isEqual(relation1.getRelatedObjectID(), relation2.getRelatedObjectID())) {
			return true;
		}
		return false;
	}
	
	/**
	 * @param reqInfo1
	 * @param reqInfo2
	 * @return
	 */
	public static boolean match(RequirementInfo reqInfo1, RequirementInfo reqInfo2) {
		if (NbaUtils.isEqual(reqInfo1.getAppliesToPartyID(), reqInfo2.getAppliesToPartyID())
				&& NbaUtils.isEqual(reqInfo1.getReqCode(), reqInfo2.getReqCode())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return my <code>NbaLogger</code> implementation (e.g. NbaLogService).
	 * @return the logger implementation
	 */
	public static NbaLogger getLogger() {
		if (logger == null) {
			try {
				logger = NbaLogFactory.getLogger(NbaContractChangeUtils.class);
			} catch (Exception e) {
				NbaBootLogger.log("Matcher could not get a logger from the factory.");
				e.printStackTrace(System.out);
			}
		}
		return logger;
	}
	
	/**
     * @param nbaPartyAddress
     * @param bePartyAddress
     * @return true if both the addresses are same or addresstype is not matched, otherwise false
     * APSL4094, SR#653140
     */
    public static boolean match(Address nbaPartyAddress, Address bePartyAddress) {
        if (nbaPartyAddress != null && bePartyAddress != null) {
            if (NbaUtils.isEqual(nbaPartyAddress.getAddressTypeCode(), bePartyAddress.getAddressTypeCode())) {
                if (!NbaUtils.isEqual(nbaPartyAddress.getAddressCountryTC(), bePartyAddress.getAddressCountryTC())
                        || !NbaUtils.isEqual(nbaPartyAddress.getAddressStateTC(), bePartyAddress.getAddressStateTC())
                        || !NbaUtils.isEqual(nbaPartyAddress.getLine1(), bePartyAddress.getLine1())
                        || !NbaUtils.isEqual(nbaPartyAddress.getLine2(), bePartyAddress.getLine2())
                        || !NbaUtils.isEqual(nbaPartyAddress.getLine3(), bePartyAddress.getLine3())
                        || !NbaUtils.isEqual(nbaPartyAddress.getCity(), bePartyAddress.getCity())
                        || !NbaUtils.isEqual(nbaPartyAddress.getZip(), bePartyAddress.getZip())) {
                    return false;
                }
            }
        }
        return true;
    }
       
    /**
     * @param nbaPartyPhone
     * @param bePartyPhone
     * @return true if both the phone object are same or phonetype is not matched, otherwise false
     * APSL4094, SR#653140
     */
    public static boolean match(Phone nbaPartyPhone, Phone bePartyPhone) {
        if (nbaPartyPhone != null && bePartyPhone != null) {
            if (NbaUtils.isEqual(nbaPartyPhone.getPhoneTypeCode(), bePartyPhone.getPhoneTypeCode())) {
                if (!NbaUtils.isEqual(nbaPartyPhone.getAreaCode(), bePartyPhone.getAreaCode())
                        || !NbaUtils.isEqual(nbaPartyPhone.getDialNumber(), bePartyPhone.getDialNumber())) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * @param nbaPartyEmail
     * @param bePartyEmail
     * @return true if both the mail are same or mailtype is not matched, otherwise false
     * APSL4094, SR#653140
     */
    public static boolean match(EMailAddress nbaPartyEmail, EMailAddress bePartyEmail) {
        if (nbaPartyEmail != null && bePartyEmail != null) {
            if (NbaUtils.isEqual(nbaPartyEmail.getEMailType(), bePartyEmail.getEMailType())) {
                if (!NbaUtils.isEqual(nbaPartyEmail.getAddrLine(), bePartyEmail.getAddrLine())) {
                    return false;
                }
            }
        }
        return true;
    }

}
