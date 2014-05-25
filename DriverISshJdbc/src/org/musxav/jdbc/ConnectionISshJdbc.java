
package org.musxav.jdbc;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Modified Connection class for close session ssh with control method close 
 * 
 * @author Xavier Massotti
 * @version 1.0
 */
public class ConnectionISshJdbc implements Connection {

	Connection internalconn = null;
	ContainerSSHSession session = null;

	public ConnectionISshJdbc(
		String jdbcUrl, String jdbcUser, String jdbcPW,
		ContainerSSHSession session)
		throws SQLException {

		this.internalconn =
			DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPW);
		try {
			TimeUnit.MILLISECONDS.sleep(300);
		}
		catch (InterruptedException e) {
			// Handle exception
		}

		if (this.internalconn == null) {
			this.internalconn =
				DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPW);
		}

		if (this.internalconn == null) {
			System.err.println("musxav.jdbc.ConnectionISshJdbc:" +
				" ERROR DriverManager.getConnection:" + jdbcUrl + "--" +
				jdbcUser);
		}
		else {
			this.session = session;
			this.session.connCreated();
		}

	}

	@Override
	public void clearWarnings()
		throws SQLException {

		this.internalconn.clearWarnings();
	}

	@Override
	public void close()
		throws SQLException {

		this.internalconn.close();
		this.session.connClosed();
	}

	@Override
	public void commit()
		throws SQLException {

		this.internalconn.commit();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency)
		throws SQLException {

		return this.internalconn.createStatement(
			resultSetType, resultSetConcurrency);
	}

	@Override
	public Statement createStatement(
		int resultSetType, int resultSetConcurrency, int resultSetHoldability)
		throws SQLException {

		return this.internalconn.createStatement(
			resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public Statement createStatement()
		throws SQLException {

		return this.internalconn.createStatement();
	}

	@Override
	public boolean getAutoCommit()
		throws SQLException {

		return this.internalconn.getAutoCommit();
	}

	@Override
	public String getCatalog()
		throws SQLException {

		return this.internalconn.getCatalog();
	}

	@Override
	public int getHoldability()
		throws SQLException {

		return this.internalconn.getHoldability();
	}

	@Override
	public DatabaseMetaData getMetaData()
		throws SQLException {

		return this.internalconn.getMetaData();
	}

	@Override
	public int getTransactionIsolation()
		throws SQLException {

		return this.internalconn.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings()
		throws SQLException {

		return this.internalconn.getWarnings();
	}

	@Override
	public boolean isClosed()
		throws SQLException {

		return this.internalconn.isClosed();
	}

	@Override
	public boolean isReadOnly()
		throws SQLException {

		return this.internalconn.isReadOnly();
	}

	@Override
	public String nativeSQL(String sql)
		throws SQLException {

		return this.internalconn.nativeSQL(sql);
	}

	@Override
	public CallableStatement prepareCall(
		String sql, int resultSetType, int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {

		return this.internalconn.prepareCall(
			sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(
		String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException {

		return this.internalconn.prepareCall(
			sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql)
		throws SQLException {

		return this.internalconn.prepareCall(sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql)
		throws SQLException {

		return this.internalconn.prepareStatement(sql);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames)
		throws SQLException {

		return this.internalconn.prepareStatement(sql, columnNames);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
		throws SQLException {

		return this.internalconn.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(
		String sql, int resultSetType, int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {

		return this.internalconn.prepareStatement(
			sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(
		String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException {

		return this.internalconn.prepareStatement(
			sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
		throws SQLException {

		return this.internalconn.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint)
		throws SQLException {

		this.internalconn.releaseSavepoint(savepoint);
	}

	@Override
	public void rollback()
		throws SQLException {

		this.internalconn.rollback();
	}

	@Override
	public void rollback(Savepoint savepoint)
		throws SQLException {

		this.internalconn.rollback(savepoint);
	}

	@Override
	public void setAutoCommit(boolean autoCommit)
		throws SQLException {

		this.internalconn.setAutoCommit(autoCommit);
	}

	@Override
	public void setCatalog(String catalog)
		throws SQLException {

		this.internalconn.setCatalog(catalog);
	}

	@Override
	public void setHoldability(int holdability)
		throws SQLException {

		this.internalconn.setHoldability(holdability);
	}

	@Override
	public void setReadOnly(boolean readOnly)
		throws SQLException {

		this.internalconn.setReadOnly(readOnly);
	}

	@Override
	public Savepoint setSavepoint(String name)
		throws SQLException {

		return this.internalconn.setSavepoint(name);
	}

	@Override
	public Savepoint setSavepoint()
		throws SQLException {

		return this.internalconn.setSavepoint();
	}

	@Override
	public void setTransactionIsolation(int level)
		throws SQLException {

		this.internalconn.setTransactionIsolation(level);
	}

	@Override
	public boolean isWrapperFor(Class<?> arg0)
		throws SQLException {

		return this.internalconn.isWrapperFor(arg0);
	}

	@Override
	public <T> T unwrap(Class<T> arg0)
		throws SQLException {

		return this.internalconn.unwrap(arg0);
	}

	@Override
	public void abort(Executor arg0)
		throws SQLException {

		this.internalconn.abort(arg0);
	}

	@Override
	public Array createArrayOf(String arg0, Object[] arg1)
		throws SQLException {

		return this.internalconn.createArrayOf(arg0, arg1);
	}

	@Override
	public Blob createBlob()
		throws SQLException {

		return this.internalconn.createBlob();
	}

	@Override
	public Clob createClob()
		throws SQLException {

		return this.internalconn.createClob();
	}

	@Override
	public NClob createNClob()
		throws SQLException {

		return this.internalconn.createNClob();
	}

	@Override
	public SQLXML createSQLXML()
		throws SQLException {

		return this.internalconn.createSQLXML();
	}

	@Override
	public Struct createStruct(String arg0, Object[] arg1)
		throws SQLException {

		return this.internalconn.createStruct(arg0, arg1);
	}

	@Override
	public Properties getClientInfo()
		throws SQLException {

		return this.internalconn.getClientInfo();
	}

	@Override
	public String getClientInfo(String arg0)
		throws SQLException {

		return this.internalconn.getClientInfo(arg0);
	}

	@Override
	public int getNetworkTimeout()
		throws SQLException {

		return this.internalconn.getNetworkTimeout();
	}

	@Override
	public String getSchema()
		throws SQLException {

		return this.internalconn.getSchema();
	}

	@Override
	public boolean isValid(int arg0)
		throws SQLException {

		return this.internalconn.isValid(arg0);
	}

	@Override
	public void setClientInfo(Properties arg0)
		throws SQLClientInfoException {

		this.internalconn.setClientInfo(arg0);

	}

	@Override
	public void setClientInfo(String arg0, String arg1)
		throws SQLClientInfoException {

		this.internalconn.setClientInfo(arg0, arg1);

	}

	@Override
	public void setNetworkTimeout(Executor arg0, int arg1)
		throws SQLException {

		this.internalconn.setNetworkTimeout(arg0, arg1);
	}

	@Override
	public void setSchema(String arg0)
		throws SQLException {

		this.internalconn.setSchema(arg0);
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> arg0)
		throws SQLException {

		this.internalconn.setTypeMap(arg0);
	}

	@Override
	public Map<String, Class<?>> getTypeMap()
		throws SQLException {

		return this.internalconn.getTypeMap();
	}
}
