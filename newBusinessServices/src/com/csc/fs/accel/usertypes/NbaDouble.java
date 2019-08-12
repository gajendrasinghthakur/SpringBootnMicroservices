package com.csc.fs.accel.usertypes;

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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;

/**
 * NbaDouble class overrides the Default implementation of AccelDouble class to 
 * store -999999999.0 in the database if the value of any Double field comes as NaN.
 * When the Data is retrieved it converts -999999999.0 to NaN.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA201</td><td>Version 7</td><td>Hibernate</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 7
 */
public class NbaDouble extends AccelDouble {
    
    private static final double fixForDefaultDoubles = -999999999.0;  

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#nullSafeGet(java.sql.ResultSet,
     *      java.lang.String[], java.lang.Object)
     */
    public Object nullSafeGet(ResultSet resultSet, String[] columnNames, Object owner) throws HibernateException,
            SQLException {
        Object accelDb = resultSet.getObject(columnNames[0]);
        if (accelDb == null) {
            return new Double(Double.NaN);
        } else {
            Double compDb = new Double(accelDb.toString());
            if (fixForDefaultDoubles == compDb.doubleValue()) {
                return new Double(Double.NaN);
            }
            return compDb;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.hibernate.usertype.UserType#nullSafeSet(java.sql.PreparedStatement,
     *      java.lang.Object, int)
     */
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        Double compDb = new Double(Double.NaN);
        if (!value.equals(compDb)) {
            st.setDouble(index, Double.valueOf(value.toString()).doubleValue());
        } else {
            st.setDouble(index, fixForDefaultDoubles);
        }
    }
}
