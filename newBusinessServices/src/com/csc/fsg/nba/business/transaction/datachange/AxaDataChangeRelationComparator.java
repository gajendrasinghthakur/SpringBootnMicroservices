package com.csc.fsg.nba.business.transaction.datachange;
/*
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
 *     Copyright (c) 2002-2007 Computer Sciences Corporation. All Rights Reserved.<BR>
 * ************************************************************** <BR>
 * 
 */
import com.csc.fsg.nba.foundation.NbaUtils;
import com.csc.fsg.nba.vo.txlife.Coverage;
import com.csc.fsg.nba.vo.txlife.CoverageExtension;
import com.csc.fsg.nba.vo.txlife.Relation;
import com.csc.fsg.nba.vo.txlife.RelationExtension;
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
 * </tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */

public class AxaDataChangeRelationComparator extends AxaDataChangeComparator {
	protected Relation oldRelation;

	protected Relation newRelation;

	protected RelationExtension newRelationExtension;

	protected RelationExtension oldRelationExtension;

	/**
	 * @param oldCoverage
	 * @param newCoverage
	 */
	public AxaDataChangeRelationComparator(Relation newRelation, Relation oldRelation) {
		super();
		this.oldRelation = oldRelation;
		this.newRelation = newRelation;
		oldRelationExtension = NbaUtils.getFirstRelationExtension(oldRelation);
		newRelationExtension = NbaUtils.getFirstRelationExtension(newRelation);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isNewRelationExtension() {
		return (newRelationExtension != null && newRelationExtension.isActionAdd() && oldRelationExtension == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isNewRelation() {
		return (newRelation != null && newRelation.isActionAdd() && oldRelation == null);
	}
	/**
	 * 
	 * @return
	 */
	public boolean isRelationDeleted() {
		return newRelation != null && oldRelation != null && NbaUtils.isDeletedOnly(newRelation);//ALS3680 changed the NbaUtils method called
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isRelationChanged() {
		if(newRelation != null && oldRelation != null && !NbaUtils.isDeletedOnly(newRelation) &&
				(!newRelation.getOriginatingObjectID().equalsIgnoreCase(oldRelation.getOriginatingObjectID())
				|| !newRelation.getRelatedObjectID().equalsIgnoreCase(oldRelation.getRelatedObjectID()))) {//ALS3680 changed the NbaUtils method called
			return true;
		}
		return false;
	}

}
