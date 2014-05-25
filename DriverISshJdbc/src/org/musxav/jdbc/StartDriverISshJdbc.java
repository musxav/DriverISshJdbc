package org.musxav.jdbc;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Servlet implementation class StartJshSqlDriver
 */
public class StartDriverISshJdbc extends HttpServlet {
	private static final long serialVersionUID = 165465463L;

    /**
     * Default constructor. 
     * @throws ClassNotFoundException 
     */
    public StartDriverISshJdbc() throws ClassNotFoundException {
    	
    	/*
    		DriverISshJdbc md = new DriverISshJdbc();
    		md.stateSessionSsh();
    		md = null;
    	*/
    	
    	Class.forName("org.musxav.jdbc.DriverISshJdbc");
	
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
	    
		DriverISshJdbc md = new DriverISshJdbc();
		md.stateSessionSsh();
		md = null;
	}

}
