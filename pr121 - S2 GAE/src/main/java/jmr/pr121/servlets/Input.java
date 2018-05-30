package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

//import jmr.pr116.messaging.EmailMessage;
//import jmr.pr116.messaging.EmailProvider;
import jmr.pr121.config.Configuration;

public class Input implements IPage {

	@Override
	public boolean doGet(	final EnumMap<ParameterName, String> map, 
							final HttpServletResponse resp ) throws IOException {
		Log.add( "Input.doGet()" );
		
		final String strButton = map.get( ParameterName.BUTTON );
		final String strEmail = map.get( ParameterName.EMAIL );
		Log.add( "\tButton: " + strButton );
		Log.add( "\tEmail: " + strEmail );
		
		String strResult = "in-progress";
		
	    final PrintWriter writer = resp.getWriter();
		try {
			
		    resp.setContentType( ContentType.TEXT_PLAIN.getMimeType() );

		    
			Thread.sleep( 100 );
		    
		    final String strAddress = Configuration.get().get( "CONTROL_EMAIL_USERNAME" );
		    final String strPassword = Configuration.get().get( "CONTROL_EMAIL_PASSWORD" );

		    
//		    final EmailControl email = new EmailControl();
//		    final EmailMessage email = new EmailMessage( 
//		    		EmailProvider.GMAIL, strAddress, strPassword.toCharArray() );
//		    email.send( strAddress, "Request from S2 GAE", "+" + strEmail, null );
		    
			
//		    final PrintWriter writer = resp.getWriter();
		    writer.print( "{ \"result\":\"success\" }" );

//		    return true;
			strResult = "success";
		    
		} catch ( final InterruptedException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			strResult = "Exception: " + e.toString();
		}

	    writer.print( "{ \"result\":\"" + strResult + "\" }" );
	    
		return false;
	}

}
