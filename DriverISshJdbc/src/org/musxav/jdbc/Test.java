
package org.musxav.jdbc;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.musxav.varis.Encryptor;

public class Test {

	public static void main(String[] args)
		throws IOException, SQLException {

		/**
		 * Test connection
		 */

		DriverISshJdbc drv = new DriverISshJdbc();
		/*
		 * String s = "jdbc:isshjdbc" 
		 * + ":internalport=10105" 
		 * + ";sshhost=www.server.com" 
		 * + ";sshport=22" 
		 * + ";sshdbhost=db.com" 
		 * + ";sshdbport=3306" 
		 * + ";sshuser=user" 
		 * + ";sshpassword=12d354ds6a98" 
		 * + ";user=userdb" 
		 * + ";password=45df6w45f" 
		 * + ";encryptedattributes=sshpassword,password" 
		 * + ";controlconn=false" 
		 * + ";jdbcclass=com.mysql.jdbc.Driver" 
		 * + ":jdbc:mysql://localhost:10105/db";
		 */

		String s = "jdbc:isshjdbc:internalport=10105" 
						+ ";sshhost=www.server.com;sshport=22"
						+ ";sshdbhost=db.com" + ";controlconn=false"
						+ ";sshdbport=3306;sshuser=user;sshpassword=bf2aefd9f8"
						+ ";encryptedattributtes=sshpassword,password"
						+ ":jdbc:mysql://localhost:10105/db";

		Properties pcc = new Properties();
		pcc.setProperty("user", "userdb");
		pcc.setProperty("passWord", "376fcb2aefd9");

		System.out.println("string:" + s);

		System.out.println(drv.stateSessionSsh()); 

		boolean result = false;

		Connection con = drv.connect(s, pcc);
		System.out.println("estat");
		System.out.println(drv.stateSessionSsh());
		Statement st = con.createStatement();
		ResultSet rst =
			st.executeQuery("select * from clientes where id_cliente = 100;");
		ResultSetMetaData rstmd = rst.getMetaData();
		System.out.println("Query "
			+ "select * from clientes where id_cliente = 100;");
		while (rst.next()) {
			int cols = rstmd.getColumnCount();
			String data = null;
			System.out.print("" + rst.getRow() + ":");
			for (int i = 0; i < cols; i++) {
				if (!result)
					result = true;
				data = rst.getString(i + 1);
				if (i > 0)
					System.out.print(",");
				System.out.print(data);
			}
			System.out.println("");
		}
		if (!result)
			System.out.println("No data - query ok");
		rst.close();
		con.close();
		drv.disconnectSessionSsh();

		/**
		 * Test encryptor
		 */

		Encryptor aes = null;
		try {
			aes = new Encryptor();
			String ss;

			ss = aes.encrypt("mipassword");
			System.out.println("Enc:" + ss);
			String ss2 = aes.decrypt(ss);
			System.out.println("Dec:" + ss2);

		}
		catch (InvalidKeyException | NoSuchAlgorithmException
						| NoSuchPaddingException | IllegalBlockSizeException
						| BadPaddingException e) {

			e.printStackTrace();
		}

	}

}
