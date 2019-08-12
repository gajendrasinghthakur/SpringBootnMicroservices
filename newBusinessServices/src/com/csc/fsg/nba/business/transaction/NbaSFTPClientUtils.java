/**
 * 
 */
package com.csc.fsg.nba.business.transaction;

import java.nio.charset.Charset;
import java.util.Properties;

import com.csc.fsg.nba.exception.NbaBaseException;
import com.csc.fsg.nba.foundation.NbaBase64;
import com.csc.fsg.nba.foundation.NbaConfigurationConstants;
import com.csc.fsg.nba.foundation.NbaLogFactory;
import com.csc.fsg.nba.foundation.NbaLogger;
import com.csc.fsg.nba.vo.NbaConfiguration;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * 
 * This class will setup the SFTP connection from CSC/AXA to AXA network via JSCH third party lib and upload/retrieve the file from the AXA specific machine
 * 
 * @author CSC FSG Developer
 * @version 7.0.0
 * @since New Business Accelerator - Version 3
 */
public class NbaSFTPClientUtils {

	private NbaLogger logger;
	private String server;
	private String port;
	private String userID;
	private String password;
	private String path;
	private JSch jsch = null;
	private Session session = null;
	private Channel channel = null;
	private ChannelSftp channelSftp = null;

	public NbaSFTPClientUtils() {
		try {
			logger = NbaLogFactory.getLogger(this.getClass());
			setServer(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.SFTP_AXA_URL));
			setPort(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.SFTP_AXA_PORT));
			setUserID(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.SFTP_AXA_USER_ID));
			setPassword(NbaConfiguration.getInstance().getBusinessRulesAttributeValue(NbaConfigurationConstants.SFTP_AXA_USER_PASSWORD));
			connect();
		} catch (NbaBaseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Connects to the server and does some commands.
	 */
	private void connect() {
		try {
			logger.logDebug("Initializing jsch");
			jsch = new JSch();
			session = jsch.getSession(getUserID(), getServer(), Integer.parseInt(getPort()));

			// Java 6 version also need to decode the password and send the password to the library into the byte code.
			session.setPassword(NbaBase64.decodeToString(getPassword()).getBytes(Charset.forName("ISO-8859-1")));

			// Java 5 version
			// session.setPassword(password.getBytes("ISO-8859-1"));

			logger.logDebug("Jsch set to StrictHostKeyChecking=no");
			Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			logger.logInfo("Connecting to " + getServer() + ":" + getPort());
			session.connect();
			logger.logInfo("Connected !");

			// Initializing a channel
			logger.logDebug("Opening a channel ...");
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			logger.logDebug("Channel sftp opened");

		} catch (JSchException e) {
			logger.logError(e.getStackTrace());
		}
	}

	/**
	 * Uploads a file to the sftp server
	 * 
	 * @param sourceFile
	 *            String path to sourceFile
	 * @param destinationFile
	 *            String path on the remote server
	 * @throws InfinItException
	 *             if connection and channel are not available or if an error occurs during upload.
	 */
	public void uploadFile(String sourceFile, String destinationFile) throws NbaBaseException {
		if (channelSftp == null || session == null || !session.isConnected() || !channelSftp.isConnected()) {
			throw new NbaBaseException("Connection to server is closed. Open it first.");
		}

		try {
			logger.logDebug("Uploading file to server");
			channelSftp.put(sourceFile, destinationFile);
			logger.logInfo("Upload successfull.");
		} catch (SftpException e) {
			throw new NbaBaseException(e);
			// logger.logError(e);
		} finally {
			disconnect();
		}
	}

	/**
	 * Retrieves a file from the sftp server
	 * 
	 * @param destinationFile
	 *            String path to the remote file on the server
	 * @param sourceFile
	 *            String path on the local fileSystem
	 * @throws InfinItException
	 *             if connection and channel are not available or if an error occurs during download.
	 */
	public void retrieveFile(String sourceFile, String destinationFile) throws NbaBaseException {
		if (channelSftp == null || session == null || !session.isConnected() || !channelSftp.isConnected()) {
			throw new NbaBaseException("Connection to server is closed. Open it first.");
		}

		try {
			logger.logDebug("Downloading file to server");
			channelSftp.get(sourceFile, destinationFile);
			logger.logInfo("Download successfull.");
		} catch (SftpException e) {
			throw new NbaBaseException(e.getMessage(), e);
		} finally {
			disconnect();
		}
	}

	/**
	 * @purpose : This method will disconnect all the channels and session for SFTP communication
	 */
	private void disconnect() {
		if (channelSftp != null) {
			logger.logDebug("Disconnecting sftp channel");
			channelSftp.disconnect();
		}
		if (channel != null) {
			logger.logDebug("Disconnecting channel");
			channel.disconnect();
		}
		if (session != null) {
			logger.logDebug("Disconnecting session");
			session.disconnect();
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
