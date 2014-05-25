
package org.musxav.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.musxav.varis.Encryptor;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/*
 * DriverISshJdbc is a facility to opening jdbc connections via 
 * ssh connection or a simple encrypt text driver parameters. 
 * 
 * You need additional librarys to use driver, this uses the 
 * library jsch www.jcraft.com/jsch to connect the remote server via ssh 
 * and the respective driver jdbc for database.
 *
 * Test with jsch 1.46 in Java 1.6,1.7 and Tomcat 6, 7 (with Pentaho 4.8, 5.0) 
 * with mysql 5.1 in a remote server via Internet.
 * 
 * This project is cause to need a connection in server BI-Pentaho 
 * for multiples querys (pool dbcp) and a simple encrypt for passwords, why 
 * the decrypt it's very simple if you have a DriverISshJdbc.jar.
 * 
 * The example configuration driver in Tomcat:
 *	<Resource name="jdbc/namejndi" auth="Container" type="javax.sql.DataSource"
 *		driverClassName="org.musxav.jdbc.DriverISshJdbc" 
 *
 *      url="jdbc:isshjdbc:internalport=10105;sshhost=www.x.com;
 *      sshport=22;sshdbhost=xx.com;sshdbport=x;sshuser=tunusr;sshpassword=x;
 *      user=x;password=x
 *      :jdbc:mysql://localhost:10105/ticketing_liceu"
 *       
 *		validationQuery="xxxx"/>
 * or
 *	<Resource name="jdbc/namejndi" auth="Container" type="javax.sql.DataSource"
 *		driverClassName="org.musxav.jdbc.DriverISshJdbc" 
 *		username="x"
 *		password="x"
 *
 *      url="jdbc:isshjdbc:internalport=10105;sshhost=www.x.com;
 *      sshport=22;sshpassword=x"
 *      :jdbc:mysql://localhost:10105/ticketing_liceu"
 *      
 *      connectionProperties="sshdbhost=xx.com;sshdbport=x;sshuser=tunusr"
 *       
 *		validationQuery="xxxx"/>
 * Note: the url attribute in one line.
 *
 * In Tomcat with Pentaho is necessary to load a servlet in context pentaho 
 * for load driver classes, add in web.xml 
 * <servlet>
 *   <servlet-name>StartDriverISshJdbc</servlet-name>
 *   <servlet-class>org.musxav.jdbc.StartDriverISshJdbc</servlet-class>
 *    <load-on-startup>1</load-on-startup>
 * </servlet>
 * 
 * Parameters:
 * user: database connection user name 
 * password: database connection password 
 * sshuser: ssh connection user
 * sshpassword: ssh connection password
 * sshhost: ssh host, if value "local" defined, connection without session ssh 
 * 		and parameters for ssh will be ignored  
 * sshport: ssh port connection
 * sshdbhost: database host connection
 * sshdbport: database port connection
 * internalport: default 10105, local port to redirect database remote port
 * encryptedattributes: list of parameters encrypted separated by comma. Used 
 * class org.musxav.varis.Encryptor to text decrypt 
 * jdbcurl: jdbc url database
 * jdbcclass: class to load jdbc driver, if empty, the driver determine 
 * 		the load jdbc class for MySql, Oracle, PostGreSql, Microsoft and DB2
 * controlconn: default true, if true, the object connection is a modify 
 * 		class "Connection" with control in method close for disconnect 
 * 		the session ssh. False, return the object Connection of jdbc driver 
 * 
 * 
 * @author Xavier Massotti
 * @version 1.0
 */
public class DriverISshJdbc implements Driver {

	private static Vector<ContainerSSHSession> vSessions =
		new Vector<ContainerSSHSession>();

	private JSch jsch = null;
	private Session sshSession = null;
	private String sDriverLoaded = null;
	private int index = -1;

	public DriverISshJdbc() {

		try {
			DriverManager.registerDriver(this);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Get sessions in static Vector
	 * @return static object Vector
	 */
	static Vector<ContainerSSHSession> getvSessions() {

		return vSessions;
	}

	/**
	 * output text to console
	 * 
	 * @param s
	 *            text
	 */
	void sep(String s) {

		System.err.println("DriverISshJdbc:" + s);
	}

	/**
	 * Control of sessions ssh
	 * 
	 * @param sshUser
	 *            User
	 * @param sshPwd
	 *            Password
	 * @param sshHost
	 *            Host
	 * @param sshPort
	 *            Port
	 * @param internalPort
	 *            Local port to redirect fwDbPort
	 * @param dbHost
	 *            Host database
	 * @param fwDbPort
	 *            Port database
	 * @return True on success session ssh and port forwarding to internal port
	 */
	boolean controlConnSsh(
		String sshUser, String sshPwd, String sshHost, int sshPort,
		int internalPort, String dbHost, int fwDbPort) {

		boolean res = false;

		String sKey = sshHost + sshUser + dbHost;
		for (int i = 0; i < DriverISshJdbc.getvSessions().size(); i++) {
			if (sKey.equals(DriverISshJdbc.getvSessions().get(i).getKey())) {
				this.jsch = DriverISshJdbc.getvSessions().get(i).getJsch();
				this.sshSession =
					DriverISshJdbc.getvSessions().get(i).getSshSession();
				this.index = i;
				break;
			}
		}

		if (jsch == null) {
			res =
				this.createSessionSsh(
					sshUser, sshPwd, sshHost, sshPort, internalPort, dbHost,
					fwDbPort);
			if (res) {
				DriverISshJdbc.getvSessions().add(
					new ContainerSSHSession(this.jsch, this.sshSession, sKey));
				this.index = DriverISshJdbc.getvSessions().size() - 1;
			}
		}
		else if (this.sshSession.isConnected()) {
			res = true;
		}
		else {
			res =
				this.createSessionSsh(
					sshUser, sshPwd, sshHost, sshPort, internalPort, dbHost,
					fwDbPort);
			DriverISshJdbc.getvSessions().set(
				this.index,
				new ContainerSSHSession(this.jsch, this.sshSession, sKey));
		}

		return res;
	}

	/**
	 * Create session ssh and forwarding port
	 * 
	 * @param sshUser
	 *            User
	 * @param sshPwd
	 *            Password
	 * @param sshHost
	 *            Host
	 * @param sshPort
	 *            Port
	 * @param internalPort
	 *            Local port to redirect fwDbPort
	 * @param dbHost
	 *            Host database
	 * @param fwDbPort
	 *            Port database
	 * @return True on success session ssh and port forwarding to internal port
	 */
	boolean createSessionSsh(
		String sshUser, String sshPwd, String sshHost, int sshPort,
		int internalPort, String dbHost, int fwDbPort) {

		boolean res = false;
		this.jsch = new JSch();
		try {
			this.sshSession = this.jsch.getSession(sshUser, sshHost, sshPort);
			this.sshSession.setPassword(sshPwd);
			Properties prop = new Properties();
			prop.setProperty("StrictHostKeyChecking", "no");
			this.sshSession.setConfig(prop);
			res = true;
		}
		catch (JSchException e) {
			this.sep("ERROR SSH jsch.getSession");
			res = false;
		}

		if (res) {
			try {
				this.sshSession.connect();
				res = true;
			}
			catch (JSchException e) {
				this.sep("ERROR SSH connect");
				res = false;
			}

			if (res) {
				try {
					TimeUnit.MILLISECONDS.sleep(1000);
				}
				catch (InterruptedException e) {
				}

				try {
					this.sshSession.setPortForwardingL(
						internalPort, dbHost, fwDbPort);

				}
				catch (JSchException e) {
					this.sep("ERROR SSH setPortForwardingL");
					res = false;
				}
			}
		}

		return res;
	}

	/**
	 * Extract parameters from jdbc url
	 * 
	 * @param query
	 *            jdbc url
	 * @return Properties wirh parameters
	 */
	public Properties getParams(String query) {

		// sep("getparams:" + query);
		String[] params = query.split(":");
		Properties p = this.getSubParams(params[2]);
		p.put(
			"jdbcurl",
			query.substring(params[0].length() + params[1].length() +
				params[2].length() + 3));
		return p;
	}

	/**
	 * Extract parameters from string
	 * 
	 * @param query
	 *            String parameters separated with ";"
	 * @return Properties with parameters
	 */
	public Properties getSubParams(String query) {

		// sep("getsubparams:" + query);
		String[] params = query.split(";");
		Properties p = new Properties();
		if (query.length() > 2) {
			for (String param : params) {
				String name = param.split("=")[0].toLowerCase();
				String value = param.split("=")[1];
				p.put(name, value);
			}
		}
		return p;
	}

	/**
	 * Add properties to first object properties
	 * 
	 * @param pDef
	 *            Properties result
	 * @param pMer
	 *            Properties to add
	 */
	public void mergeParams(Properties pDef, Properties pMer) {

		for (Enumeration<Object> en = pMer.keys(); en.hasMoreElements();) {
			String sen = en.nextElement().toString();
			pDef.put(sen.toLowerCase(), pMer.getProperty(sen));
		}
	}

	/**
	 * Get parameters fron jdbc url and properties and construct Porperties
	 * 
	 * @param url
	 *            jdbc url
	 * @param info
	 *            Properties parameters
	 * @return Properties with parameters
	 */
	public Properties normaliceParams(String url, Properties info) {

		Properties pConf = new Properties();

		pConf.setProperty("user", "");
		pConf.setProperty("password", "");
		pConf.setProperty("sshuser", "");
		pConf.setProperty("sshpassword", "");
		pConf.setProperty("sshhost", "local");
		pConf.setProperty("sshport", "22");
		pConf.setProperty("sshdbhost", "");
		pConf.setProperty("sshdbport", "0");
		pConf.setProperty("internalport", "10105");
		pConf.setProperty("encryptedattributes", "");
		pConf.setProperty("jdbcurl", "");
		pConf.setProperty("jdbcclass", "");
		pConf.setProperty("controlconn", "true");

		this.mergeParams(pConf, this.getParams(url));
		this.mergeParams(pConf, info);

		return pConf;
	}

	/**
	 * Decrypt parameters
	 * 
	 * @param pConf
	 *            Properties with parameters
	 * @throws Exception
	 */
	void decryptParams(Properties pConf)
		throws Exception {

		if (pConf.getProperty("encryptedattributes").length() > 1) {
			Encryptor aes = new Encryptor();
			for (String attr : pConf.getProperty("encryptedattributes").split(
				",")) {
				pConf.setProperty(
					attr, aes.decrypt(pConf.getProperty(attr, "")));
			}
		}
	}

	/**
	 * Load class driver
	 * 
	 * @param url
	 *            jdbc url database connection
	 * @param jdbcClass
	 *            Class to load, this exclude the jdbc url
	 * @return True on success
	 */
	boolean loadDriver(String url, String jdbcClass) {

		try {
			if (jdbcClass.length() > 0) {
				this.sDriverLoaded = jdbcClass;
				Class.forName(jdbcClass);
			}
			else if (url.startsWith("jdbc:mysql")) {
				this.sDriverLoaded = "com.mysql.jdbc.Driver";
				Class.forName("com.mysql.jdbc.Driver");
			}
			else if (url.startsWith("jdbc:oracle")) {
				this.sDriverLoaded = "oracle.jdbc.driver.OracleDriver";
				Class.forName("oracle.jdbc.driver.OracleDriver");
			}
			else if (url.startsWith("jdbc:postgresql")) {
				this.sDriverLoaded = "org.postgresql.Driver";
				Class.forName("org.postgresql.Driver");
			}
			else if (url.startsWith("jdbc:microsoft")) {
				this.sDriverLoaded =
					"com.microsoft.jdbc.sqlserver.SQLServerDriver";
				Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver");
			}
			else if (url.startsWith("jdbc:db2")) {
				this.sDriverLoaded = "COM.ibm.db2.jdbc.app.DB2Driver";
				Class.forName("COM.ibm.db2.jdbc.app.DB2Driver");
			}
			return true;
		}
		catch (Exception e) {
			sep("ERROR load driver class:" + this.sDriverLoaded);
		}
		return false;
	}

	@Override
	public boolean acceptsURL(String url)
		throws SQLException {

		return (url != null && url.startsWith("jdbc:isshjdbc"));
	}

	@Override
	public Connection connect(String url, Properties info)
		throws SQLException {

		if (!this.acceptsURL(url)) {
			// this.sep("Do not accept url:" + url);
			return null;
		}

		Properties pConf = this.normaliceParams(url, info);

		try {
			this.decryptParams(pConf);
		}
		catch (Exception e) {
			throw new SQLException("DriverISshJdbc: ERROR decrypt parameters");
		}

		if (!this.loadDriver(
			pConf.getProperty("jdbcurl"), pConf.getProperty("jdbcclass"))) {
			throw new SQLException(
				"DriverISshJdbc: ERROR loading driver class jdbc:" +
					pConf.getProperty("jdbcurl") + "(" +
					pConf.getProperty("jdbcclass") + ")");
		}

		if (!pConf.getProperty("sshhost").equals("local")) {
			boolean control =
				this.controlConnSsh(
					pConf.getProperty("sshuser"),
					pConf.getProperty("sshpassword"),
					pConf.getProperty("sshhost"),
					Integer.parseInt(pConf.getProperty("sshport")),
					Integer.parseInt(pConf.getProperty("internalport")),
					pConf.getProperty("sshdbhost"),
					Integer.parseInt(pConf.getProperty("sshdbport")));
			if (control == false) {
				throw new SQLException("DriverISshJdbc: ERROR CONNECT SSH:" +
					pConf.getProperty("sshhost") + "--DB:" +
					pConf.getProperty("sshdbhost"));
			}
		}

		Connection conn = null;
		if (pConf.getProperty("controlconn").equals("true")) {

			conn =
				new ConnectionISshJdbc(
					pConf.getProperty("jdbcurl"), pConf.getProperty("user"),
					pConf.getProperty("password"),
					DriverISshJdbc.getvSessions().get(index));

		}
		else {
			conn =
				DriverManager.getConnection(
					pConf.getProperty("jdbcurl"), pConf.getProperty("user"),
					pConf.getProperty("password"));
		}

		return conn;
	}

	/**
	 * Get the state of connection ssh for this driver
	 * 
	 * @return Text result ssh connection
	 */
	public String stateSessionSsh() {

		if (this.sshSession == null)
			return "state: not connected (null)";
		return "state:" + this.sshSession.isConnected();
	}

	/**
	 * Manual disconnect ssh session for this driver
	 */
	public void disconnectSessionSsh() {

		if (this.sshSession != null)
			this.sshSession.disconnect();
	}

	@Override
	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
		throws SQLException {

		DriverPropertyInfo[] res = new DriverPropertyInfo[5];

		Properties pConf = this.normaliceParams(url, info);

		int i = 0;
		res[i] = new DriverPropertyInfo("user", pConf.getProperty("user"));
		res[i].required = true;
		res[i].description = "User name jdbc";

		res[++i] =
			new DriverPropertyInfo("password", pConf.getProperty("password"));
		res[i].required = true;
		res[i].description = "User password jdbc";

		res[++i] =
			new DriverPropertyInfo("sshuser", pConf.getProperty("sshuser"));
		res[i].required = true;
		res[i].description = "User connection SSH";

		res[++i] =
			new DriverPropertyInfo(
				"sshpassword", pConf.getProperty("sshpassword"));
		res[i].required = true;
		res[i].description = "Password connection SSH";

		res[++i] =
			new DriverPropertyInfo(
				"encryptedattributes", pConf.getProperty("encryptedattributes"));
		res[i].required = false;
		res[i].description =
			"List of driver parameters separated with "
				+ "comma (,) to decrypt value (org.musxav.varis.Encryptor)";

		res[++i] =
			new DriverPropertyInfo("sshhost", pConf.getProperty("sshhost"));
		res[i].required = true;
		res[i].description = "URL connection SSH";

		res[++i] =
			new DriverPropertyInfo("sshport", pConf.getProperty("sshport"));
		res[i].required = true;
		res[i].description = "Port connection SSH";

		res[++i] =
			new DriverPropertyInfo("sshdbhost", pConf.getProperty("sshdbhost"));
		res[i].required = true;
		res[i].description = "URL connection SSH";

		res[++i] =
			new DriverPropertyInfo("sshdbport", pConf.getProperty("sshdbport"));
		res[i].required = true;
		res[i].description =
			"Remote database port for forwarding to " + "internal port";

		res[++i] =
			new DriverPropertyInfo(
				"internalport", pConf.getProperty("internalport"));
		res[i].required = true;
		res[i].description = "Internal port for jdbc connection";

		res[++i] =
			new DriverPropertyInfo("jdbcurl", pConf.getProperty("jdbcurl"));
		res[i].required = true;
		res[i].description = "URL jdbc database";

		res[++i] =
			new DriverPropertyInfo("jdbcclass", pConf.getProperty("jdbcclass"));
		res[i].required = false;
		res[i].description =
			"Class to register jdbc driver database " + "(Class.forName)";

		res[++i] =
			new DriverPropertyInfo(
				"controlconn", pConf.getProperty("controlconn"));
		res[i].required = false;
		res[i].description =
			"Control closed connections with a modified "
				+ "class connection, (true|false) case sensitive";

		return res;
	}

	@Override
	public int getMajorVersion() {

		try {
			return ((Driver) Class.forName(this.sDriverLoaded).newInstance()).getMajorVersion();
		}
		catch (InstantiationException e) {
			this.sep("ERROR InstantiationException");
		}
		catch (IllegalAccessException e) {
			this.sep("ERROR IllegalAccessException");
		}
		catch (ClassNotFoundException e) {
			this.sep("ERROR ClassNotFoundException");
		}
		return 0;
	}

	@Override
	public int getMinorVersion() {

		try {
			return ((Driver) Class.forName(this.sDriverLoaded).newInstance()).getMinorVersion();
		}
		catch (InstantiationException e) {
			this.sep("ERROR InstantiationException");
		}
		catch (IllegalAccessException e) {
			this.sep("ERROR IllegalAccessException");
		}
		catch (ClassNotFoundException e) {
			this.sep("ERROR ClassNotFoundException");
		}
		return 0;
	}

	@Override
	public boolean jdbcCompliant() {

		try {
			return ((Driver) Class.forName(this.sDriverLoaded).newInstance()).jdbcCompliant();
		}
		catch (InstantiationException e) {
			this.sep("ERROR InstantiationException");
		}
		catch (IllegalAccessException e) {
			this.sep("ERROR IllegalAccessException");
		}
		catch (ClassNotFoundException e) {
			this.sep("ERROR ClassNotFoundException");
		}
		return false;
	}

	@Override
	public Logger getParentLogger()
		throws SQLFeatureNotSupportedException {

		try {
			return ((Driver) Class.forName(this.sDriverLoaded).newInstance()).getParentLogger();
		}
		catch (InstantiationException e) {
			this.sep("ERROR InstantiationException");
		}
		catch (IllegalAccessException e) {
			this.sep("ERROR IllegalAccessException");
		}
		catch (ClassNotFoundException e) {
			this.sep("ERROR ClassNotFoundException");
		}
		return null;
	}

}
