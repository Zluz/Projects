package jmr.pr121.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
    name = "DataMap",
    urlPatterns = {"/map"}
)
public class DataMap extends HttpServlet {

	final List<String> listHistory = new LinkedList<>();
	
	final static Map<String,String> mapData = new HashMap<>();
	
	

	
	@Override
	public void doGet(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) 
		  							throws IOException {
		Log.add( this.getClass().getName() + ".doGet()" );
		
	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
	
	    final PrintWriter writer = resp.getWriter();
		writer.print("DataMap\r\n");
	    
			try {
				writer.print("\r\nParameters:\r\n");
				for ( final Entry<String, String[]> entry : 
							req.getParameterMap().entrySet() ) {
					
					final String strName = entry.getKey();
					final String strValue = req.getParameter( strName );
					writer.print( "\t" + strName + " = " + strValue + "\r\n" );
				}

				final String strName = req.getParameter( "name" );

				final String strData;
				if ( null!=strName && !strName.isEmpty() ) {
					writer.print("\r\nData (name=" + strName + "):\r\n");
					strData = mapData.get( strName );
					writer.print( strData + "\r\n" );
				} else {
					strData = null;
				}
				
				writer.print("\r\nHistory:\r\n");
				for ( final String line : listHistory ) {
					writer.print("\t" + line +"\r\n");
				}
				
				listHistory.add( req.getRequestURL().toString() );
				listHistory.add( "\t\t" + req.getQueryString() );
				if ( null!=strData ) {
					listHistory.add( "\t\tData: " + strData );
				}
				
			} catch ( final Throwable e ) {
				writer.print("\r\nError: " + e.toString() + "\r\n");
			}
	
	
	  }
	
	@Override
	public void doPost(	final HttpServletRequest req, 
		  				final HttpServletResponse resp ) 
		  							throws IOException {
		Log.add( this.getClass().getName() + ".doPost()" );
      
	    resp.setContentType("text/plain");
	    resp.setCharacterEncoding("UTF-8");
	
	    final PrintWriter writer = resp.getWriter();
		writer.print("DataMap\r\n");
    
		try {
			
			writer.print("\r\nParameters:\r\n");
			for ( final Entry<String, String[]> entry : 
						req.getParameterMap().entrySet() ) {
				
				final String strName = entry.getKey();
				final String strValue = req.getParameter( strName );
				writer.print( "\t" + strName + " = " + strValue + "\r\n" );
			}
		
			final String strName = req.getParameter( "name" );
			
			writer.print( "\r\nPOST data:\r\n" );

			{
				final StringBuffer strbuf = new StringBuffer();
				String line = null;
				try {
					BufferedReader reader = req.getReader();
					while ((line = reader.readLine()) != null)
						strbuf.append(line);
				} catch (Exception e) {
				/* report an error */ 
				}
				
				writer.print( strbuf.toString() + "\r\n" );
				
				if ( null!=strName && !strName.isEmpty() ) {
					mapData.put( strName, strbuf.toString() );
					Log.add( "Storing data: " + strbuf.toString() );
				} else {
					Log.add( "Data not stored because name is empty." );
				}
			}


			writer.print("\r\nHistory:\r\n");
			
			{
				writer.print("\r\nHistory:\r\n");
				for ( final String line : listHistory ) {
					writer.print("\t" + line +"\r\n");
				}
			}
			
			listHistory.add( req.getRequestURL().toString() );
			listHistory.add( "\t\t" + req.getQueryString() );
			
		} catch ( final Throwable e ) {
			writer.print("\r\nError: " + e.toString() + "\r\n");
		}


	  }
}