
DriverISshJdbc es una utilidad para facilitar las conexiones jdbc vía una sesión ssh y además un método simple para cifrar los parámetros del driver.
 
Este driver utiliza librerías adicionales para las conexiones, se necesita la librería jsch www.jcraft.com/jsch para las conexiones ssh y la correspondiente librería del driver jdbc para la base de datos.

Testado con jsch 1.46, Java 1.6/1.7, Tomcat 6/7 con mysql 5.1 para implementar un jndi en Pentaho BI-Server 4.8/5.0.

Parámetros del driver

 user: usuario base de datos (jdbc)

 password: contraseña base de datos (jdbc)

 sshuser: usuario conexión ssh

 sshpassword: contraseña conexión ssh

 sshhost: host conexión ssh. Si se define el valor “local” no se crea sesión ssh y se 	ignoran los parámetros de conexión ssh

 sshport: puerto conexión ssh

 sshdbhost: host base de datos (dentro de conexión ssh)

 sshdbport: puerto base datos (dentro de conexión ssh)

 internalport: defecto 10105, puerto local interno para redirigir el puerto de base de 	datos

 encryptedattributes: lista de atributos separados por coma (,) Se usa la clase 	org.musxav.varis.Encryptor para descifrar el texto

 jdbcurl: url o cadena de conexión jdbc, con la sesión ssh deberá apuntar a 	“localhost:puertointerno”

 jdbcclass: clase del driver jdbc a cargar manualmente Si esta vacío o no existe el 	driver determina la clase a cargar para las definiciones jdbc de MySql, Oracle, 	PostGreSql, Microsoft y DB2

 controlconn: defecto “true”, con ”true” el objeto connection devuelto es una 	modificación de la clase “Connection” que incorpora un control en el método 	“close” para cerrar la sesión ssh cuando corresponda. Con “false” se retorna un 	objecto connection del driver jdbc utilizado.
 

Este proyecto es el resultado de la necesidad de implementar una conexión jndi en un servidor Pentaho así como utilidades (reporting, saiku, etc.) siendo la conexión en un servidor remoto vía Internet y con túnel ssh, además de un sistema sencillo de cifrado para no dejar “a la vista” las contraseñas.
 
Ejemplo de configuración del driver en Tomcat(JNDI):

Copiar las librerias jsch, mysql y DriverISshJdbc en el directorio tomcat/lib y añadir al fichero de contexto xml (tomcat/conf/localhost/contextapp.xml o webapps/app/META-INF/context.xml) el recurso jndi.

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

Nota: El atributo “url” en una sola linea.

Y modificar web.xml para la referencia jndi 

  <resource-ref>
    <description>Jndi db</description>
    <res-ref-name>jdbc/namejndi</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
  </resource-ref>

En Tomcat con Pentaho (BI-Server) es necesario cargar un servlet en el contexto “pentaho” para forzar a cargar las clases del controlador, añadir en pentaho/WEB-INF/web.xml la carga del servlet en la sección correspondiente. 

 <servlet>
   <servlet-name>StartDriverISshJdbc</servlet-name>
   <servlet-class>org.musxav.jdbc.StartDriverISshJdbc</servlet-class>
    <load-on-startup>1</load-on-startup>
 </servlet>
 

 

