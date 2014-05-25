DriverISshJdbc
==============

Facility to open connections jdbc with session SSH


driverISshJdbcJV6.jar version for java 1.6
driverISshJdbcJV7.jar version for java 1.7



DriverISshJdbc is a facility to opening jdbc connections via ssh connection or a simple 
encrypt text driver parameters.
 
You need additional library to use this driver, this uses the library 
jsch www.jcraft.com/jsch for connection ssh and the respective driver jdbc for database.

Test with jsch 1.46, Java 1.6/1.7, Tomcat 6/7 with mysql 5.1 to implementing 
connection jndi in Pentaho bi-server 4.8/5.0.

Parameters driver

 user: database connection user name
 
 password: database connection password 
 
 sshuser: ssh connection user
 
 sshpassword: ssh connection password
 
 sshhost: ssh host, if value "local" defined, connection without session ssh 
 		and parameters for ssh will be ignored
 		
 sshport: ssh port connection
 
 sshdbhost: database host connection
 
 sshdbport: database port connection
 
 internalport: default 10105, local port to redirect database remote port
 
 encryptedattributes: list of parameters encrypted separated by comma. Used 
 		class org.musxav.varis.Encryptor to text decrypt 
 		
 jdbcurl: jdbc url database
 
 jdbcclass: class to load jdbc driver, if empty, the driver determine 
 		the load jdbc class for MySql, Oracle, PostGreSql, Microsoft and DB2
 		
 controlconn: default true, if true, the object connection is a modify 
 		class "Connection" with control in method close for disconnect 
 		the session ssh. False, return the object Connection of jdbc driver 
 

This project is a need solution to implementing jndi in Pentaho bi-server, 
report designer, saiku, pivot4j, etc. in a remote server via ssh and a simple 
encrypt for passwords.
 
The example configuration driver in Tomcat(JNDI):

Copy librarys jsch, mysql and DriverISshJdbc in folder tomcat/lib and add 
to file xml context (tomcat/conf/localhost/contextapp.xml or 
webapps/app/META-INF/context.xml) the resource jndi. 

	<Resource name="jdbc/namejndi" 
		auth="Container" 
		type="javax.sql.DataSource"
		driverClassName="org.musxav.jdbc.DriverISshJdbc" 

	      url="jdbc:isshjdbc:internalport=10105;sshhost=www.x.com;
      		sshport=22;sshdbhost=xx.com;sshdbport=x;sshuser=tunusr;sshpassword=x;
	      user=x;password=x:jdbc:mysql://localhost:10105/db"
       
		validationQuery="xxxx"/>

or

	<Resource name="jdbc/namejndi" 
		auth="Container" 
		type="javax.sql.DataSource"
		driverClassName="org.musxav.jdbc.DriverISshJdbc" 
		username="x"
		password="x"

	      url="jdbc:isshjdbc:internalport=10105;sshhost=www.x.com;
      		sshport=22;sshpassword=x:jdbc:mysql://localhost:10105/db"
      
      		connectionProperties="sshdbhost=xx.com;sshdbport=x;sshuser=tunusr"
       
		validationQuery="xxxx"/>

Note: the url attribute in one line.

And modify web.xml for reference jndi

  <resource-ref
  >
  	<description>Jndi db</description>
    	<res-ref-name>jdbc/namejndi</res-ref-name>
    	<res-type>javax.sql.DataSource</res-type>
    	<res-auth>Container</res-auth>
  </resource-ref
  >


In Tomcat with Pentaho is necessary to load a servlet in context pentaho 
for load driver classes, add definition in pentaho/WEB-INF/web.xml to load servlet. 

 <servlet 
 >
	<servlet-name>StartDriverISshJdbc</servlet-name>
   	<servlet-class>org.musxav.jdbc.StartDriverISshJdbc</servlet-class>
   	<load-on-startup>1</load-on-startup>
  </servlet 
 >
 

 

