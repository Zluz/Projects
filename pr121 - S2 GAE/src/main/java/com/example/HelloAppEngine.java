package com.example;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jmr.pr121.servlets.Log;

/*
	http://localhost:8080/hello?name=value&name_2=value_2

	https://pr121-s2gae.appspot.com/hello
 */


@SuppressWarnings("serial")
@WebServlet(
    name = "HelloAppEngine",
    urlPatterns = {"/hello"}
)
public class HelloAppEngine extends HttpServlet {

	final List<String> listHistory = new LinkedList<>();
	
	
	@Override
	public void doGet(	final HttpServletRequest request, 
		  				final HttpServletResponse response ) 
		  							throws IOException {
		Log.add( this.getClass().getName() + ".doGet()" );

    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");

    final PrintWriter writer = response.getWriter();
	writer.print("Hello App Engine!\r\n");
    
	try {
		writer.print("\r\nParameters:\r\n");
		for ( final Entry<String, String[]> entry : 
					request.getParameterMap().entrySet() ) {
			
			final String strName = entry.getKey();
			final String strValue = request.getParameter( strName );
			writer.print( "\t" + strName + " = " + strValue + "\r\n" );
		}
	
		writer.print("\r\nHistory:\r\n");
		for ( final String line : listHistory ) {
			writer.print("\t" + line +"\r\n");
		}
		
		listHistory.add( request.getRequestURL().toString() );
		listHistory.add( "\t\t" + request.getQueryString() );
		
	} catch ( final Throwable e ) {
		writer.print("\r\nError: " + e.toString() + "\r\n");
	}


  }
}