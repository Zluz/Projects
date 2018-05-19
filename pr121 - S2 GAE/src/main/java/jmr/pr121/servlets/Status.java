package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

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
    name = "Status",
    urlPatterns = {"/status"}
)
public class Status extends HttpServlet {

	final static LocalDateTime ldtStart;
	
	static {
		ldtStart = LocalDateTime.now();
	}
	
	
	final static List<String> listLog = new LinkedList<>();

	public static void add( final String strText ) {
		listLog.add( strText );
	}

	
	@Override
	public void doGet(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) 
		  							throws IOException {
      
	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
	
	    final PrintWriter writer = resp.getWriter();
    
		try {
			writer.print("Status\r\n");
			
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