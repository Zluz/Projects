package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;

public class Input implements IPage {

	@Override
	public boolean doGet(	final EnumMap<ParameterName, String> map, 
							final HttpServletResponse resp ) throws IOException {
		Log.add( "Input.doGet()" );
		
		final String strButton = map.get( ParameterName.BUTTON );
		Log.add( "\tButton: " + strButton );
		
		try {
			
		    resp.setContentType( ContentType.TEXT_PLAIN.getMimeType() );

			Thread.sleep( 3000 );
			
		    final PrintWriter writer = resp.getWriter();
		    writer.print( "{ \"result\":\"success\" }" );

		    return true;
		    
		} catch ( final InterruptedException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

}
