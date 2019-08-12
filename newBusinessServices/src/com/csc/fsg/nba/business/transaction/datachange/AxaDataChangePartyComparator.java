
package com.csc.fsg.nba.business.transaction.datachange;
/*
 * ************************************************************** <BR>
 * This program contains trade secrets and confidential information which<BR>
 * are proprietary to CSC Financial Services Group®.  The use,<BR>
 * part, without the express written permission of CSC Financial Services<BR>
 * Group is prohibited.  This program is also an unpublished work protected<BR>
 * under the copyright laws of the United States of America and other<BR>
 * countries.  If this program becomes published, the following notice shall<BR>
 * apply:
 *     Property of Computer Sciences Corporation.<BR>
 *     Confidential. Not for publication.<BR>
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import com.csc.fsg.nba.foundation.NbaOliConstants;
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.NbaParty;
import com.csc.fsg.nba.vo.txlife.Address;
import com.csc.fsg.nba.vo.txlife.CarrierAppointment;
import com.csc.fsg.nba.vo.txlife.Organization;
import com.csc.fsg.nba.vo.txlife.Person;
import com.csc.fsg.nba.vo.txlife.Producer;

/**
 * 
 * Helper classes to determine Data change 
 * 
 * <p>
 * <b>Modifications: </b> <br>
 * <table border=0 cellspacing=5 cellpadding=5> <thead>
 * <th align=left>Project</th>
 * <th align=left>Release</th>
 * <th align=left>Description</th>
 * </thead>
 * <tr>
 * <td>AXAL3.7.07</td><td>AXA Life Phase 2</td><td>Data Change Architecture</td>
 * <td>AXAL3.7.21</td><td>AXA Life Phase 1</td><td>Prior Insurance</td>
 * <td>P2AXAL038</td><td>AXA Life Phase 2</td><td>Zip Code Interface</td>
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaDataChangePartyComparator extends AxaDataChangeComparator {
	protected NbaParty oldParty;

	protected NbaParty newParty;

	protected Person oldPerson;

	protected Person newPerson;

	protected Organization oldOrganization;

	protected Organization newOrganization;

	/**
	 * @param oldParty
	 * @param newParty
	 */
	public AxaDataChangePartyComparator(NbaParty newParty, NbaParty oldParty) {
		super();
		this.oldParty = oldParty;
		this.newParty = newParty;
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isOrganization()) {
				newOrganization = newParty.getOrganization();
				oldOrganization = oldParty.getOrganization();
			} else if (newParty.isPerson()) {
				newPerson = newParty.getPerson();
				oldPerson = oldParty.getPerson();
			}
		}
	}

	/**
	 * Determines if partType has Changed
	 * 
	 * @return
	 */
	public boolean isPartyTypeChanged() {
		if (oldParty != null && newParty != null) {
			if (oldParty.isPerson() && newParty.isOrganization()) {
				return true;
			}
			if (oldParty.isOrganization() && newParty.isPerson()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if new Party is a new party, corresponding party in database is null
	 * 
	 * @return
	 */
	public boolean isNewParty() {
		if (newParty != null && oldParty == null) {
			if (newParty.getParty().isActionAdd()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines if new Party is a new party, corresponding party in database is null
	 * 
	 * @return
	 */
	public boolean isDeletedParty() {
		if (newParty != null && oldParty != null) {
			if (NbaUtils.isDeletedOnly(newParty.getParty())) {//ALS3680 changed the NbaUtils method called
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPartyChanged() {
		if (!isDeletedParty() && isAgentCodeChanged()) {
			return true;
		}
		return isNewParty() || isPartyNameChanged() || isPartyAddressChanged() || isPartyDOBChanged() || isPartyTypeChanged() || isPartyGenderChanged()
				|| isPartySSNChanged()||isPartySSNTypeChanged() || (isDeletedParty() && !newParty.getParty().isActionDeleteSuccessful());

	}

	/**
	 * 
	 * @return
	 */
	public boolean isPartyNameChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isPerson()) {
				return !matchAttributes(newPerson.getPrefix(), oldPerson.getPrefix())
						|| !matchAttributes(newPerson.getFirstName(), oldPerson.getFirstName())
						|| !matchAttributes(newPerson.getSuffix(), oldPerson.getSuffix())
						|| !matchAttributes(newPerson.getMiddleName(), oldPerson.getMiddleName())
						|| !matchAttributes(newPerson.getLastName(), oldPerson.getLastName());

			} else if (newParty.isOrganization()) {
				return !matchAttributes(newOrganization.getDBA(), oldOrganization.getDBA());
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	//New Method AXAL3.7.21
	public boolean isPartyFirstNameChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isPerson()) {
				return !matchAttributes(newPerson.getFirstName(), oldPerson.getFirstName());

			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	//New Method AXAL3.7.21
	public boolean isPartyLastNameChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isPerson()) {
				return !matchAttributes(newPerson.getLastName(), oldPerson.getLastName());
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	//New Method AXAL3.7.21
	public boolean isPartySuffixChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isPerson()) {
				return !matchAttributes(newPerson.getSuffix(), oldPerson.getSuffix());
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	//New Method AXAL3.7.21
	public boolean isPartyPrefixChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isPerson()) {
				return !matchAttributes(newPerson.getPrefix(), oldPerson.getPrefix());
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	//New Method AXAL3.7.21
	public boolean isPartyMiddleNameChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isPerson()) {
				return !matchAttributes(newPerson.getMiddleName(), oldPerson.getMiddleName());
			}
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPartySSNTypeChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			return !matchAttributes(newParty.getParty().getGovtIDTC(), oldParty.getParty().getGovtIDTC());
		}
		return false;
	}
	/**
	 * 
	 * @return
	 */
	public boolean isPartyDOBChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isPerson() && newPerson.hasBirthDate() && oldPerson.hasBirthDate()) {
				return !matchAttributes(newPerson.getBirthDate(), oldPerson.getBirthDate()); //APSL1451
				
			}
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPartyGenderChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isPerson()) {
				return !matchAttributes(newPerson.getGender(), oldPerson.getGender());
			}
		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPartySSNChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			return !matchAttributes(newParty.getParty().getGovtID(), oldParty.getParty().getGovtID());

		}
		return false;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isPartyAddressChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			int newPartyAddressSize = newParty.getParty().getAddressCount();
			int oldPartyAddressSize = oldParty.getParty().getAddressCount();
			if (oldPartyAddressSize == newPartyAddressSize) {
				for (int j = 0; j < newPartyAddressSize; j++) {
					boolean gotAddressMatch = false;
					Address partyAddress = newParty.getParty().getAddressAt(j);
					if (NbaUtils.isDeletedOnly(partyAddress)) {//One of the addresses has been deleted from the view ALS3680 changed the NbaUtils method called
						return true;
					}
					Address dbPartyAddress = oldParty.getAddress(partyAddress.getId());
					return (partyAddress != null && dbPartyAddress != null && !matchAddress(partyAddress, dbPartyAddress));
				}
			}

		}
		return false;
	}

	//P2AXAL038 new method
	public boolean isAddressChanged() {
		boolean addressChanged = false;
		if (!isPartyTypeChanged() && !isDeletedParty()) {
			int newPartyAddressSize = newParty.getParty().getAddressCount();
			for (int j = 0; j < newPartyAddressSize; j++) {
				Address partyAddress = newParty.getParty().getAddressAt(j);
				if (!NbaUtils.isDeletedOnly(partyAddress)
						&& (NbaOliConstants.OLI_NATION_USA == partyAddress.getAddressCountryTC() || NbaUtils.isBlankOrNull(partyAddress
								.getAddressCountryTC()))) {
					//Addresse is non deleted and US address
					Address dbPartyAddress = oldParty.getAddress(partyAddress.getId());
					if (dbPartyAddress == null) {
						addressChanged = true;
					} else {
						addressChanged = !matchAddress(partyAddress, dbPartyAddress);
					}
				}
				if (addressChanged) {
					break;
				}
			}
		}
		return addressChanged;
	}


	/**
	 * 
	 * @return
	 */
	public boolean isAgentCodeChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.getParty().getProducer() != null && oldParty.getParty().getProducer() != null) {
				CarrierAppointment newCarrierAppt = getCarrierAppointment(newParty.getParty().getProducer());
				CarrierAppointment oldCarrierAppt = getCarrierAppointment(oldParty.getParty().getProducer());
				if (newCarrierAppt != null && oldCarrierAppt != null
						&& !matchAttributes(newCarrierAppt.getCompanyProducerID(), oldCarrierAppt.getCompanyProducerID())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the first <code>CarrierAppointment</code>. If a CarrierAppointment instance does not exist, null is returned.
	 * 
	 * @return
	 */
	public CarrierAppointment getCarrierAppointment(Producer producer) {
		if (producer != null) {
			if (producer.getCarrierAppointmentCount() > 0) {
				return producer.getCarrierAppointmentAt(0);
			}
		}
		return null;
	}

	/**
	 * Compares two Address
	 * 
	 * @param partyAddress
	 * @param dbPartyAddress
	 * @return status
	 */
	private boolean matchAddress(Address partyAddress, Address dbPartyAddress) {
		return matchAttributes(partyAddress.getLine1(), dbPartyAddress.getLine1())
				&& matchAttributes(partyAddress.getLine2(), dbPartyAddress.getLine2())
				&& matchAttributes(partyAddress.getLine3(), dbPartyAddress.getLine3())
				&& matchAttributes(partyAddress.getCity(), dbPartyAddress.getCity())
				&& matchAttributes(partyAddress.getAddressStateTC(), dbPartyAddress.getAddressStateTC())
				&& matchAttributes(partyAddress.getAddressCountry(), dbPartyAddress.getAddressCountry())
				&& matchAttributes(partyAddress.getZip(), dbPartyAddress.getZip());
	}
	
	//NBLXA-2152
	public boolean isPartyDBAChanged() {
		if (!isPartyTypeChanged() && !isNewParty() && !isDeletedParty()) {
			if (newParty.isOrganization()) {
				return !matchAttributes(newOrganization.getDBA(), oldOrganization.getDBA());

			}
		}
		return false;
	}
	
}
