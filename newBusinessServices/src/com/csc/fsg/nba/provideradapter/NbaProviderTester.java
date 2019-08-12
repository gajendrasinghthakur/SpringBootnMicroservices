package com.csc.fsg.nba.provideradapter;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.csc.fsg.nba.vo.NbaTXLife;
import com.csc.fsg.nba.vo.configuration.Provider;
/**
 * Insert description here.
 * <p>
 * <b>Modifications:</b><br>
 * <table border=0 cellspacing=5 cellpadding=5>
 * <thead>
 * <th align=left>Project</th><th align=left>Release</th><th align=left>Description</th>
 * </thead>
 * <tr><td>NBA008</td><td>Version 2</td><td>Requirements Ordering and Receipting</td><tr>
 * <tr><td>ACN012</td><td>Version 4</td><td>Architecture Changes</td></tr>
 * </table>
 * <p>
 * @author CSC FSG Developer
 * @version 7.0.0
 * @see insert reference here (optional - delete if not used)
 * @since New Business Accelerator - Version 2
 */
public class NbaProviderTester {
	public static Provider provider; //ACN012
	public com.csc.fsg.nba.vo.NbaTXLife requirement = new NbaTXLife();
/**
 * Insert the method's description here.
 * @param queue java.lang.String
 */
public static void addToQueue(String queue) {
	System.out.println("addToQueue for " + queue);
}
/**
 * Insert the method's description here.
 */
public static void createMIB() {
	System.out.println("createMIB");
}
/**
 * Insert the method's description here.
 */
public static void createParamed() {
	System.out.println("createParamed");
}
/**
 * Insert the method's description here.
 */
public static void createRequirement() {
	System.out.println("createRequirement");
}
/**
 * Insert the method's description here.
 * @return com.csc.fsg.nba.configuration.NbaConfigProvider
 */
//ACN012 CHANGED SIGNATRUE
public Provider getProvider() {
	return provider;
}
/**
 * Insert the method's description here.
 * @return com.csc.fsg.nba.vo.NbaTXLife
 */
public com.csc.fsg.nba.vo.NbaTXLife getRequirement() {
	return requirement;
}
/**
 * Insert the method's description here.
 * @param args java.lang.String[]
 */
public static void main(String[] args) {
	try {
		if( args.length != 3) {
			printUsage();
			return;
		}
		provider = NbaConfiguration.getInstance().getProvider(args[0]);
		if( args[1].equalsIgnoreCase("MIB")) {
			createMIB();
		} else if( args[1].equalsIgnoreCase("PARAMED")) {
			createParamed();
		} else {
			createRequirement();
		}
		addToQueue(args[2]);
	} catch (NbaBaseException nbe) {
		nbe.printStackTrace();
	}
}
/**
 * Insert the method's description here.
 */
public static void printUsage() {
	System.out.println("Usage:    NbaProviderTester provider requirement queue");
	System.out.println("Example:  NbaProviderTester EMSI paramed APORDERD");
}
/**
 * Insert the method's description here.
 * @param newProvider com.csc.fsg.nba.configuration.NbaConfigProvider
 */
//ACN012 CHANGED SIGNATRUE
public void setProvider(Provider newProvider) {
	provider = newProvider;
}
/**
 * Insert the method's description here.
 * @param newRequirement com.csc.fsg.nba.vo.NbaTXLife
 */
public void setRequirement(com.csc.fsg.nba.vo.NbaTXLife newRequirement) {
	requirement = newRequirement;
}
}
