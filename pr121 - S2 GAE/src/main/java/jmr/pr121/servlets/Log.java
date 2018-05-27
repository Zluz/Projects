package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/*
	http://localhost:8080/hello?name=value&name_2=value_2

	https://pr121-s2gae.appspot.com/hello
 */


@SuppressWarnings("serial")
@WebServlet(
    name = "Log",
    urlPatterns = {"/log"}
)
public class Log extends HttpServlet {

	final static List<String> listLog = new LinkedList<>();

	public static void add( final String strText ) {
		listLog.add( strText );
	}

	
	@Override
	public void doGet(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) 
		  							throws IOException, ServletException {
//		req.logout();
//		req.authenticate( resp );
//		req.login( "121", "4559" );
		
//		final UserAuth ua = new UserAuth( req, resp );
//		if ( !ua.require( 2 ) ) {
//			return;
//		}
//		SessionRegistry.register( req );
		
//		final UserAuth auth = new UserAuth( req, resp );
//		if ( auth.isAborted() ) return;
		
	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
	
	    final PrintWriter writer = resp.getWriter();
    
		writer.print( "\tHttpServletRequest.getUserPrincipal(): " + req.getUserPrincipal() + "\r\n" );

		try {
			
			writer.print("\r\nLog:\r\n");
			for ( final String line : listLog ) {
				writer.print( line +"\r\n" );
			}
			
			listLog.add( req.getRequestURL().toString() );
			listLog.add( "\t\t" + req.getQueryString() );
			
		} catch ( final Throwable e ) {
			writer.print("\r\nError: " + e.toString() + "\r\n");
		}

	}
}