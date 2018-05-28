package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
	http://localhost:8080/hello?name=value&name_2=value_2

	https://pr121-s2gae.appspot.com/hello
 */


@SuppressWarnings("serial")
@WebServlet(
    name = "Log",
    urlPatterns = {"/log"}
)
public class Log extends HttpServlet implements IPage {

	final static List<String> listLog = new LinkedList<>();

	public static void add( final String strText ) {
		listLog.add( strText );
	}
	
	public static void add( final HttpServletRequest req ) {
		if ( null==req ) return;
		
		Log.add( "New request: " + req.getRequestURL().toString() );
		Log.add( "\tUser Principal: " + req.getUserPrincipal() );
		Log.add( "\tUser-Agent: " + req.getHeader( "User-Agent" ) );
	}

	
	@Override
	public void doGet(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) 
		  							throws IOException, ServletException {
		Log.add( req );

		// authenticate, authorize
		final UserAuth user = new UserAuth( req, resp );
		if ( !user.require( 2 ) ) return;
		if ( user.isAborted() ) return;
		

		final EnumMap<ParameterName,String> 
				mapParams = new EnumMap<>( ParameterName.class );
		
		this.doGet( mapParams, resp );
	}

	@Override
	public boolean doGet(	final EnumMap<ParameterName,String> map,
							final HttpServletResponse resp ) throws IOException {
		
	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
	
	    final PrintWriter writer = resp.getWriter();
    
//		writer.print( "\tHttpServletRequest.getUserPrincipal(): " + req.getUserPrincipal() + "\r\n" );

		try {
			
			writer.print("\r\nLog:\r\n");
			for ( final String line : listLog ) {
				writer.print( line +"\r\n" );
			}
			
//			listLog.add( req.getRequestURL().toString() );
//			listLog.add( "\t\t" + req.getQueryString() );
			
			return true;
		} catch ( final Throwable e ) {
			writer.print("\r\nError: " + e.toString() + "\r\n");
		}
		return false;
	}
}