
DriverISshJdbc �s una utilitat per facilitar les connexions jdbc via una sessi� ssh i a m�s un m�tode simple per xifrar els par�metres del driver.
 
Aquest driver utilitza llibreries addicionals per les connexions, es necessita la llibreria jsch www.jcraft.com/jsch per les connexions ssh i la corresponent llibreria del driver jdbc per la base de dades.

Testat amb jsch 1.46, Java 1.6/1.7, Tomcat 6/7 amb mysql 5.1 per implementar una connexi� jndi a Pentaho BI-Server 4.8/5.0.

Par�metres del driver

 user: usuari base de dades (jdbc)

 password: contrasenya base de dades (jdbc)

 sshuser: usuari connexi� ssh

 sshpassword: contrasenya connexi� ssh

 sshhost: host connexi� ssh. Si cont� el valor �local� no es crear� la sessi� ssh i 	s'ignoraran els par�metres ssh

 sshport: port connexi� ssh

 sshdbhost: host base de dades (dintre de connexi� ssh)

 sshdbport: port base de dades (dintre de connexi� ssh)

 internalport: defecte 10105, port local intern per redirigir el port de la base de 	dades

 encryptedattributes: llista d'atributs separats per coma (,) S'utilitza la classe 	org.musxav.varis.Encryptor per desxifrar el text

 jdbcurl: url o cadena de connexi� jdbc

 jdbcclass: classe del driver jdbc a carregar manualment. Si �s buit o no existeix el 	driver determina la classe a carregar per les definicions jdbc de MySql, Oracle, 	PostGreSql, Microsoft y DB2

 controlconn: defecte �true�. Amb �true� l'objete connection tornat �s una 	modificaci� de la classe �Connection� incorporant un control en el m�tode 	�close� per tancar la sessi� ssh quan correspongui. Amb �false� es retorna un 	objecte connection del driver jdbc utilitzat.
 

Aquest projecte �s el resultat de la necessitat d'implementar una connexi� jndi en un servidor Pentaho aix� com utilitats (reporting, saiku, etc.) sent la connexi� en el servidor remot via Internet i amb t�nel ssh, a m�s d'un sistema senzill de xifrat per no deixar �a la vista� les contrasenyes.
 
Exemple de configuraci� del driver a Tomcat(JNDI):

Copiar les llibreries jsch, mysql y DriverISshJdbc en el directori tomcat/lib i afegir  al fitxer de context xml (tomcat/conf/localhost/contextapp.xml o webapps/app/META-INF/context.xml) el recurs jndi.

	<Resource name="jdbc/namejndi" 
		auth="Container" 
		type="javax.sql.DataSource"
		driverClassName="org.musxav.jdbc.DriverISshJdbc" 

	      url="jdbc:isshjdbc:internalport=10105;sshhost=www.x.com;
      		sshport=22;sshdbhost=xx.com;sshdbport=x;sshuser=tunusr;sshpassword=x;
	      user=x;password=x:jdbc:mysql://localhost:10105/db"
       
		validationQuery="xxxx"/>

o

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

Nota: L'atribut �url� en una sola l�nia.

I modificar web.xml amb la referencia jndi 

  <resource-ref>
    <description>Jndi db</description>
    <res-ref-name>jdbc/namejndi</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
  </resource-ref>

A Tomcat amb Pentaho (BI-Server) �s necessari carregar un servlet en el context �pentaho� per for�ar a carregar les classes del controlador, afegir a pentaho/WEB-INF/web.xml la carreg� del servlet a la secci� corresponent. 

 <servlet>
   <servlet-name>StartDriverISshJdbc</servlet-name>
   <servlet-class>org.musxav.jdbc.StartDriverISshJdbc</servlet-class>
    <load-on-startup>1</load-on-startup>
 </servlet>
 

 

