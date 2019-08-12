package com.csc.fs.dataobject.awd;

/*
 * *******************************************************************************<BR>
 * Copyright 2014, Computer Sciences Corporation. All Rights Reserved.<BR>
 *
 * CSC, the CSC logo, nbAccelerator, and csc.com are trademarks or registered
 * trademarks of Computer Sciences Corporation, registered in the United States
 * and other jurisdictions worldwide. Other product and service names might be
 * trademarks of CSC or other companies.<BR>
 *
 * Warning: This computer program is protected by copyright law and international
 * treaties. Unauthorized reproduction or distribution of this program, or any
 * portion of it, may result in severe civil and criminal penalties, and will be
 * prosecuted to the maximum extent possible under the law.<BR>
 * *******************************************************************************<BR>
 */

import java.io.Serializable;


/**
 * W01U999S is a data object representing a single row from the W01U999S table in the AWD
 * database schema.  This class is used to query and data in the W01U999S table.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA331.1</td><td>Version NB-1402</td><td>AWD REST</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version NB-1402
 * @see 
 * @since New Business Accelerator - Version NB-1402
 */

public class W01U999S implements Serializable {
	
	private static final long serialVersionUID = -8034191877993472630L;

	private String crDatTim = null;
	private String recordCd = null;
	private String crNode = null;
	private Integer seqNbr = null;
	private String dataName = null;
	private String dataValue = null;

	
	/**
	    * @param crDatTim
	    * @param recordCd
	    * @param crNode
	    * @param seqNbr
	    * @param dataName
	    * @param dataValue
	    */
	    public W01U999S(String crDatTim, String recordCd, String crNode, Integer seqNbr, String dataName, String dataValue) {
	       super();
	       this.crDatTim = crDatTim;
	       this.recordCd = recordCd;
	       this.crNode = crNode;
	       this.seqNbr = seqNbr;
	       this.dataName = dataName;
	       this.dataValue = dataValue;
	    }
	/**
	 * @return the crDatTim
	 */
	public String getCrDatTim() {
		return crDatTim;
	}
	/**
	 * @param crDatTim the crDatTim to set
	 */
	public void setCrDatTim(String crDatTim) {
		this.crDatTim = crDatTim;
	}
	/**
	 * @return the recordCd
	 */
	public String getRecordCd() {
		return recordCd;
	}
	/**
	 * @param recordCd the recordCd to set
	 */
	public void setRecordCd(String recordCd) {
		this.recordCd = recordCd;
	}
	/**
	 * @return the crNode
	 */
	public String getCrNode() {
		return crNode;
	}
	/**
	 * @param crNode the crNode to set
	 */
	public void setCrNode(String crNode) {
		this.crNode = crNode;
	}
	/**
	 * @return the seqNbr
	 */
	public Integer getSeqNbr() {
		return seqNbr;
	}
	/**
	 * @param seqNbr the seqNbr to set
	 */
	public void setSeqNbr(Integer seqNbr) {
		this.seqNbr = seqNbr;
	}
	/**
	 * @return the dataName
	 */
	public String getDataName() {
		return dataName;
	}
	/**
	 * @param dataName the dataName to set
	 */
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}
	/**
	 * @return the dataValue
	 */
	public String getDataValue() {
		return dataValue;
	}
	/**
	 * @param dataValue the dataValue to set
	 */
	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o != null && o instanceof W01U999S) {
			String key = getKey();
			if (key != null) {
				return key.equals(((W01U999S) o).getKey());
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		String key = getKey();
		if (key != null) {
			return key.hashCode();
		}
		return 0;
	}

	protected String getKey() {
		StringBuilder sb = new StringBuilder(100);
		sb.append(getCrDatTim());
		sb.append(getRecordCd());
		sb.append(getCrNode());
		sb.append(getDataName());
		sb.append(getSeqNbr());
		return sb.toString();
	}
}
