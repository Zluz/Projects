package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import jmr.pr121.storage.ClientData;
import jmr.util.http.ContentType;


@SuppressWarnings("serial")
//@WebServlet(
//    name = "GCSListing",
//    urlPatterns = {"/ui/gcs"}
//)
public class TeslaUIServlet extends HttpServlet implements IPage {

	
	enum Frame {
		HEADER( "#FFFFFF" ),
		FOOTER( "#FFFFFF" ),
		NAV( "#FFFFFF" ),
		BODY( "#F2F2F2" ),
		RIGHT( "#FFFFFF" ),
		;
		
		public final String strColor;
		
		Frame( final String strColor ) {
			this.strColor = strColor;
		}
	}

	enum NavItem {
		STILL_CAP( "Still Image Captures" ),
		SCREENSHOTS( "Device Screenshots" ),
		TESLA_JSON( "Tesla JSON" ),
		NEST_JSON( "Nest JSON" ),
		S2_DEVICES( "S2 Device Status" ),
		S2_EVENTS( "S2 Recent Events" ),
		GAE_UTIL( "App Engine utilities" ),
		;
		
		public final String strCaption;
		
		NavItem( final String strCaption ) {
			this.strCaption = strCaption;
		}
	}
	
	
	
	@Override
	public boolean doGet(	final Map<ParameterName,String> map,
							final HttpServletResponse resp,
							final ClientData client ) throws IOException {
		Log.add( this.getClass().getName() + ".doGet()" );		
		
		final String strName = map.get( ParameterName.NAME );
		final Frame frame = StringUtils.isEmpty( strName ) 
							? null : Frame.valueOf( strName );

		final String strURLRequest = map.get( ParameterName.REQUEST_URL );
		final String strURLBase = map.get( ParameterName.REQUEST_BASE );
		

	    resp.setContentType( ContentType.TEXT_HTML.getMimeType() );
	    resp.setCharacterEncoding( "UTF-8" );
	    
//	    if ( StringUtils.isEmpty( strName ) ) {
	    if ( null==frame ) {
	    
		    final PrintWriter writer = resp.getWriter();
		    
			writer.print( "<!DOCTYPE html>\n"
		    		+ "<html><head>\n"
		    		+ "<title>S2 App Engine client (pr121) for Tesla</title>\n"
		    		+ "\n\n"
		    		+ ServletConstants.strJS
		    		+ "\n"
		    		+ "<script src=\"https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.3.1.min.js\"></script>"
		    		+ "\n\n\n"
		    		+ ""
		    		+ "</head>\n" );
//		    writer.print( "<body>\n" );
	
			final String strURLHeader = strURLRequest + "?name=HEADER";
			final String strURLFooter = strURLRequest + "?name=FOOTER";
			final String strURLNav    = strURLRequest + "?name=NAV";
			final String strURLBody   = strURLRequest + "?name=BODY";
			final String strURLRight  = strURLRequest + "?name=RIGHT";
	
			final String strFramesetOpts = " frameborder='0' ";
			final String strFrameOpts = " frameborder='0' scrolling='no' "
					+ "marginheight='0' marginwidth='0' noresize='noresize' ";
			final String strFrameOptResize = " frameborder='0' scrolling='yes' ";
			
		    writer.print( "<frameset" + strFramesetOpts + "rows='20px,*,50px'>\n" );
		    writer.print( "  <frame" + strFrameOpts + "src='" + strURLHeader + "'>\n" );
		    writer.print( "  <frameset" + strFramesetOpts + "cols='240px,*,30px'>\n" );
		    writer.print( "    <frame" + strFrameOpts + "src='" + strURLNav + "'>\n" );
		    writer.print( "    <frame" + strFrameOptResize + "src='" + strURLBody + "'>\n" );
		    writer.print( "    <frame" + strFrameOpts + "src='" + strURLRight + "'>\n" );
		    writer.print( "  </frameset>\n" );
		    writer.print( "  <frame" + strFrameOpts + "src='" + strURLFooter + "'>\n" );
		    writer.print( "</frameset>\n" );
	
	
		    
//		    writer.print( "<p>&nbsp;</p>\n" );
		    
	
		    
//		    writer.print( "</body>\n" );
		    writer.print( "</html>" );
	    
	    } else {
	    	
		    final PrintWriter writer = resp.getWriter();


			writer.println( "<!DOCTYPE html>\n"
		    		+ "<html><head>\n"
		    		+ "<title>Tesla UI Frame: " + frame.name() + "</title>" );
			writer.println( ServletConstants.strStyle );
			writer.println( "</head>" );
//			writer.println( "<STYLE>\n"
//					+ "html {\n"
//					+ "    font-family: 'Verdana,Geneva';\n"
//					+ "}\n"
//					+ ".info {\n"
//					+ "    font-size: 12px;\n"
//					+ "    font-family: 'Verdana,Geneva';\n"
//					+ "}\n"
//					+ "</STYLE>" );
//			writer.println( ServletConstants.strStyle );
			
			final String strColor = frame.strColor;
			
		    writer.print( "<body style='font-family:\"verdana\"; background-color:" + strColor + ";'>\n" );
			
		    
		    switch ( frame ) {
			    case NAV: {
//				    writer.println( "<h2>Tesla/" + frame.name() + "</h2>" );
				    writer.println( "<br><br><br>" );
				    
			    	final String strURLNavSplit = strURLBase + "/images/nav_split.png";

			    	writer.println( "<table width='100%' height='100%'>" );
				    for ( final NavItem item : NavItem.values() ) {
				    	
				    	final String strClass;
				    	if ( NavItem.STILL_CAP.equals( item ) ) {
				    		strClass = "nav-selected";
				    	} else {
				    		strClass = "nav";
				    	}
				    	
				    	writer.println( "<tr height='40'><td class='" + strClass + "' width='100%' align='right'>" );
//				    	writer.println( "<h5 align='right'>" + item.name() + "</h5>" );
//				    	writer.println( "<br><text style='font-size:20'>" + item.strCaption + "</text><br><br>" );
//				    	writer.println( item.strCaption + "&nbsp;&nbsp;&nbsp;<br>" );
//				    	writer.println( "<img width='80%' height='3px' src='" + strURLNavSplit + "'><br>" );
				    	writer.println( item.strCaption + "</td></tr>" );
				    	writer.println( "<tr><td align='right'>" );
				    	writer.println( "<img align='right' width='80%' height='5px' src='" + strURLNavSplit + "'>" );
				    	writer.println( "</td></tr>" );
				    }
			    	writer.println( "</table>" );
			    	break;
			    }
			    case FOOTER: {
			    	
			    	final String strURLGear = strURLBase + "/images/options.png";
			    	
			    	writer.println( "<table width='100%'><tr>" );
			    	writer.println( "<td width='80px' align='right'>" );
			    	writer.println( "<img align='right' width='30px' height='30px' src='" + strURLGear + "'>" );
			    	writer.println( "</td>" );
			    	writer.println( "<td class='info' id='status_line' width='400px' align='left'>Status messages go here (left-1)<br>Second line for messages</td>" );
			    	writer.println( "<td align='center'>(footer)</td>" );
			    	writer.println( "<td class='info' id='client_info' width='400px' align='right'>(right-1) Resolution: 1234x567<br>Connection: Good</td>" );
			    	writer.println( "<td width='50px'>* * *</td>" );
			    	writer.println( "</tr></table>" );
			    	break;
			    }
			    case HEADER: {
			    	final String strURLTopBar = strURLBase + "/images/top_bar.png";
			    	writer.println( "<table width='100%' style='background-image:url(\"" + strURLTopBar + "\")'><tr>" );
			    	writer.println( "<td width='50px'>* * *</td>" );
//			    	writer.println( "<td id='status_line' width='400px' align='left'>Status messages go here(left-1)</td>" );
//			    	writer.println( "<td width='300px' align='left'>(left-2)</td>" );
			    	writer.println( "<td align='center'><h2>Tesla/" + frame.name() + "</h2></td>" );
//			    	writer.println( "<td width='300px' align='right'>(right-2)</td>" );
//			    	writer.println( "<td id='client_info' width='400px' align='right'>(right-1) Resolution: 1234x567</td>" );
			    	writer.println( "<td width='50px'>* * *</td>" );
			    	writer.println( "</tr></table>" );
			    	break;
			    }
			    case BODY: {
				    writer.print( "<h2>Tesla/" + frame.name() + "</h2>" );
			    	break;
			    }
			    case RIGHT: {
			    	break;
			    }
		    }
			
		    writer.print( "</body>\n</html>" );

	    }
	    
	
		Log.add( "Tesla UI draw complete." );
		return true;
	}
	
}