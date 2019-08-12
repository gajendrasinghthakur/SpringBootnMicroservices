package com.csc.fs.accel.async;

public abstract class AsynchronousTransactionHelper implements Runnable {

    private Throwable processException;
    
    /**
     * @return Returns the processException.
     */
    public Throwable getProcessException() {
        return processException;
    }
    /**
     * @param processException The processException to set.
     */
    public void setProcessException(Throwable processException) {
        this.processException = processException;
    }
}
