
DriverISshJdbc és una utilitat per facilitar les connexions jdbc via una sessió ssh i a més un mètode simple per xifrar els paràmetres del driver.
 
Aquest driver utilitza llibreries addicionals per les connexions, es necessita la llibreria jsch www.jcraft.com/jsch per les connexions ssh i la corresponent llibreria del driver jdbc per la base de dades.

Testat amb jsch 1.46, Java 1.6/1.7, Tomcat 6/7 amb mysql 5.1 per implementar una connexió jndi a Pentaho BI-Server 4.8/5.0.

Paràmetres del driver

 user: usuari base de dades (jdbc)

 password: contrasenya base de dades (jdbc)

 sshuser: usuari connexió ssh

 sshpassword: contrasenya connexió ssh

 sshhost: host connexió ssh. Si conté el valor «local» no es crearà la sessió ssh i 	s'ignoraran els paràmetres ssh

 sshport: port connexió ssh

 sshdbhost: host base de dades (dintre de connexió ssh)

 sshdbport: port base de dades (dintre de connexió ssh)

 internalport: defecte 10105, port local intern per redirigir el port de la base de 	dades

 encryptedattributes: llista d'atributs separats per coma (,) S'utilitza la classe 	org.musxav.varis.Encryptor per desxifrar el text

 jdbcurl: url o cadena de connexió jdbc

 jdbcclass: classe del driver jdbc a carregar manualment. Si és buit o no existeix el 	driver determina la classe a carregar per les definicions jdbc de MySql, Oracle, 	PostGreSql, Microsoft y DB2

 controlconn: defecte “true”. Amb ”true” l'objete connection tornat és una 	modificació de la classe “Connection” incorporant un control en el mètode 	“close” per tancar la sessió ssh quan correspongui. Amb “false” es retorna un 	objecte connection del driver jdbc utilitzat.
 

Aquest projecte és el resultat de la necessitat d'implementar una connexió jndi en un servidor Pentaho així com utilitats (reporting, saiku, etc.) sent la connexió en el servidor remot via Internet i amb túnel ssh, a més d'un sistema senzill de xifrat per no deixar “a la vista” les contrasenyes.
 
Exemple de configuració del driver a Tomcat(JNDI):

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

Nota: L'atribut “url” en una sola línia.

I modificar web.xml amb la referencia jndi 

  <resource-ref>
    <description>Jndi db</description>
    <res-ref-name>jdbc/namejndi</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
  </resource-ref>

A Tomcat amb Pentaho (BI-Server) és necessari carregar un servlet en el context “pentaho” per forçar a carregar les classes del controlador, afegir a pentaho/WEB-INF/web.xml la carregà del servlet a la secció corresponent. 

 <servlet>
   <servlet-name>StartDriverISshJdbc</servlet-name>
   <servlet-class>org.musxav.jdbc.StartDriverISshJdbc</servlet-class>
    <load-on-startup>1</load-on-startup>
 </servlet>
 

 

