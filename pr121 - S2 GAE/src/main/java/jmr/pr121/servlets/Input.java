package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

import jmr.pr121.storage.ClientData;

public class Input implements IPage {

	@Override
	public boolean doGet(	final Map<ParameterName, String> map, 
							final HttpServletResponse resp, 
							final ClientData client ) throws IOException {
		Log.add( "Input.doGet()" );
		
		final String strButton = map.get( ParameterName.BUTTON );
		final String strEmail = map.get( ParameterName.EMAIL );
		final String strInfo = map.get( ParameterName.CLIENT_INFO );
		
//		if ( StringUtils.isNotBlank( strEmail ) ) 
		Log.add( "\tButton: " + strButton );
		Log.add( "\tEmail: " + strEmail );
		Log.add( "\tInfo: " + strInfo );
		
		if ( null!=client ) {
			client.setClientInfo( strInfo );
		} else {
			Log.add( "Unable to set client info; client is null." );
		}
		
		String strResult = "in-progress";
		
	    final PrintWriter writer = resp.getWriter();
		try {
			
		    resp.setContentType( ContentType.TEXT_PLAIN.getMimeType() );

		    
			Thread.sleep( 100 );
		    
//		    final String strAddress = Configuration.get().get( "CONTROL_EMAIL_USERNAME" );
//		    final String strPassword = Configuration.get().get( "CONTROL_EMAIL_PASSWORD" );

		    
//		    final EmailControl email = new EmailControl();
//		    final EmailMessage email = new EmailMessage( 
//		    		EmailProvider.GMAIL, strAddress, strPassword.toCharArray() );
//		    email.send( strAddress, "Request from S2 GAE", "+" + strEmail, null );
		    
			
//		    final PrintWriter writer = resp.getWriter();
//		    writer.print( "{ \"result\":\"success\" }" );

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
