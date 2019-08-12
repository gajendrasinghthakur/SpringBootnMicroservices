package com.csc.fs.accel.async;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.csc.fs.Result;
import com.csc.fs.svcloc.ServiceLocator;
import commonj.work.WorkManager;
/**
* AsynchronousProcessor defines the implementation of Asynchronous WorkManager. 
* This implementation can be configured using wm.properties and is accessible via AsynchronousProcessor.factory.getImpl();
* Each AsynchronousProcessor implementation should implement the AsynchronousProcessor
* <p>
* <b>Modifications:</b><br>
* <table border=0 cellspacing=5 cellpadding=5>
* <thead>
* <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
* </thead>
* <tr><td>NBA208-9</td><td>Version 7</td><td>Asynchronous Services</td></tr>
* </table>
* <p>
* @author CSC FSG Developer
 * @version 7.0.0
* @since New Business Accelerator - Version 7
*/
public interface AsynchronousProcessor {
    
    /**
     * Constant to locate the implementation class from Service Locator. 
     */
    public static final String ASYNCHRONOUS_PROCESSOR_IMPL = "AsynchronousProcessorImpl";
    /**
     * Constant to set condition - Wait for all jobs to complete 
     */
    public static final int WAIT_FOR_ALL = 0;
    /**
     * Constant to set condition - Wait for any of the jobs to complete 
     */
    public static final int WAIT_FOR_ANY = 1;
    /**
     * Constant to set condition - Wait for none  
     */
    public static final int WAIT_FOR_NONE = 3;
    
    /**
     * Constant to locate bootstrap URL to Work Manager in application server 
     */
    public static final String BOOTSTRAP_URL = "java.naming.provider.url";

    /**
     * Constant determining JNDI Lookup name of the workmanager 
     */
    public static final String LOOKUP_NAME = "workmanager.jndi.name";
    
    /**
     * Constant to set the default timeout for asynchronous processing 
     */
    public static final String TIMEOUT = "workmanager.schedule.timeout";
    
    /**
     * Submit Job for Asynchronous Processing
     * @param runnableCollection Collection of Runnable Jobs for asynchronous processing
     */
    public Result submit(Collection runnableCollection);

    /**
     * Submit Job for Asynchronous Processing
     * @param runnableCollection Collection of Runnable Jobs for asynchronous processing
     */
    public Result submit(Runnable runnable);
    
    /**
     * Submit Job for Asynchronous Processing
     * @param runnableCollection Collection of Runnable Jobs for asynchronous processing
     * @param listener instance of AsynchronousProcessorListener to handle call back methods during Job processing
     */
    public Result submit(Collection runnableCollection, AsynchronousProcessorListener listener);
    
    /**
     * Submit Job for Asynchronous Processing
     * @param runnableCollection Collection of Runnable Jobs for asynchronous processing
     * @param joinCondition - AsynchronousProcessor.WAIT_FOR_ALL - Wait for all jobs to complete
     * 						- AsynchronousProcessor.WAIT_FOR_ANY - Wait for any of the jobs to complete
     * 						- AsynchronousProcessor.WAIT_FOR_NONE - Wait for none 
     */
    public Result submit(Collection runnableCollection, int joinCondition);
    
    /**
     * Submit Job for Asynchronous Processing
     * @param runnableCollection Collection of Runnable Jobs for asynchronous processing
     * @param listener instance of AsynchronousProcessorListener to handle call back methods during Job processing
     * @param joinCondition - AsynchronousProcessor.WAIT_FOR_ALL - Wait for all jobs to complete
     * 						- AsynchronousProcessor.WAIT_FOR_ANY - Wait for any of the jobs to complete
     * 						- AsynchronousProcessor.WAIT_FOR_NONE - Wait for none 
     */
    public Result submit(Collection runnableCollection, AsynchronousProcessorListener listener, int joinCondition);

    /**
     * Submit Job for Asynchronous Processing
     * @param runnableCollection Collection of Runnable Jobs for asynchronous processing
     * @param listener instance of AsynchronousProcessorListener to handle call back methods during Job processing
     * @param joinCondition - AsynchronousProcessor.WAIT_FOR_ALL - Wait for all jobs to complete
     * 						- AsynchronousProcessor.WAIT_FOR_ANY - Wait for any of the jobs to complete
     * 						- AsynchronousProcessor.WAIT_FOR_NONE - Wait for none 
     * @param timeOut - How long to wait before the job compeltes
     */
    public Result submit(Collection runnableCollection, AsynchronousProcessorListener listener, int joinCondition, long timeout);
    
    /**
     * Factory to get the implementation of Asynchronous processor and Work manager 
     */
    public static AsynchronousProcessorFactory factory = AsynchronousProcessorFactory.getInstance(); 
    
    class AsynchronousProcessorFactory {

        private Properties workManagerProps = new Properties();
        private AsynchronousProcessor impl = null;
        private static AsynchronousProcessorFactory instance = null;
        private static final String WORKMANAGER_PROPERTY_FILE_DEFINITION = "wm.properties";
        private Context initialContext = null;
        
        private AsynchronousProcessorFactory() {
            try {
                impl = (AsynchronousProcessor)ServiceLocator.lookup(ASYNCHRONOUS_PROCESSOR_IMPL);
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(WORKMANAGER_PROPERTY_FILE_DEFINITION);
                workManagerProps.load(is);
            } catch (IOException e) {
                //TODO: Use logger
                System.err.println(" Failed to initialize the asynchrnous services ");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        static synchronized AsynchronousProcessorFactory getInstance() {
            if(instance == null ) {
                instance = new AsynchronousProcessorFactory();
            }
            return instance;
        }
        public AsynchronousProcessor getImpl() {
            return impl;
        }
        public WorkManager getWorkManager() throws AsynchronousServiceFailureException {
            try {
                return (WorkManager)getInitialContext().lookup(workManagerProps.getProperty(LOOKUP_NAME));
            } catch (NamingException e) {
                throw new AsynchronousServiceFailureException("Failed to lookup WorkManager",e);
            }
        }
    	Context getInitialContext() throws NamingException {
    		if (initialContext == null) {
    			initialContext = new InitialContext(workManagerProps);
    		}
    		return initialContext;
    	}
    	public long getSchedulerServiceTimeout() {
    	    return Long.parseLong(workManagerProps.getProperty(LOOKUP_NAME));
    	}
    }
}
