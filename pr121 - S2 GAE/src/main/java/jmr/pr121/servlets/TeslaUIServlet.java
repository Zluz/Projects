package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import jmr.pr121.storage.ClientData;
import jmr.util.http.ContentType;
import static jmr.pr121.servlets.TeslaPage.NavItem;

@SuppressWarnings("serial")
//@WebServlet(
//    name = "GCSListing",
//    urlPatterns = {"/ui/gcs"}
//)
public class TeslaUIServlet extends HttpServlet implements IPage {

	
	public enum Frame {
		HEADER( "#FFFFFF" ),
		FOOTER( "#FFFFFF" ),
		NAV( "#FFFFFF" ),
		BODY( ServletConstants.COLOR_CONTENT_BACK ),
		RIGHT( "#FFFFFF" ),
		LEFT_MARGIN( "#C0C0C0" ),
		;
		
		public final String strColor;
		
		Frame( final String strColor ) {
			this.strColor = strColor;
		}
	}
	
	public enum DisplayMode {
		NORMAL,
		FULL_IMAGE,
	}

	
	@Override
	public boolean doGet(	final Map<ParameterName,String> map,
							final HttpServletResponse resp,
							final ClientData client ) throws IOException {
		Log.add( this.getClass().getName() + ".doGet()" );		
		
		final String strName = map.get( ParameterName.NAME );
		final Frame frame = StringUtils.isEmpty( strName ) 
							? null : Frame.valueOf( strName );
		
		final String strFullImage = map.get( ParameterName.FULL_IMAGE );
		final DisplayMode mode = StringUtils.isEmpty( strFullImage )
							? DisplayMode.NORMAL : DisplayMode.FULL_IMAGE;

		final String strURLRequest = map.get( ParameterName.REQUEST_URL );
		final String strURLBase = map.get( ParameterName.REQUEST_BASE );
		

	    resp.setContentType( ContentType.TEXT_HTML.getMimeType() );
	    resp.setCharacterEncoding( "UTF-8" );

		final String strURLHeader = strURLRequest + "?name=HEADER";
		final String strURLFooter = strURLRequest + "?name=FOOTER";
		final String strURLNav    = strURLRequest + "?name=NAV";
		final String strURLLMargin= strURLRequest + "?name=LEFT_MARGIN";
		final String strURLBody   = strURLRequest + "?name=BODY";
		final String strURLRight  = strURLRequest + "?name=RIGHT";
		final String strURLBodyImage = strURLRequest 
						+ "?name=BODY&full_image=" + strFullImage;
	    
//	    if ( StringUtils.isEmpty( strName ) ) {
	    if ( null==frame ) {
	    
		    final PrintWriter writer = resp.getWriter();
		    
			writer.print( "<!DOCTYPE html>\n"
		    		+ "<html><head>\n"
		    		+ "<title>S2 App Engine client</title>\n"
		    		+ "\n\n"
//		    		+ "<script src=\"https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.3.1.min.js\"></script>"
		    		+ ServletConstants.strLoadFromCDNs
		    		+ "\n"
		    		+ ServletConstants.strJS
		    		+ "\n\n\n"
		    		+ ""
		    		+ "</head>\n" );
//		    writer.print( "<body>\n" );
	
//			final String strURLHeader = strURLRequest + "?name=HEADER";
//			final String strURLFooter = strURLRequest + "?name=FOOTER";
//			final String strURLNav    = strURLRequest + "?name=NAV";
//			final String strURLLMargin= strURLRequest + "?name=LEFT_MARGIN";
//			final String strURLBody   = strURLRequest + "?name=BODY";
//			final String strURLRight  = strURLRequest + "?name=RIGHT";
	
			final String strFramesetOpts = " frameborder='0' ";
			final String strFrameOpts = " frameborder='0' scrolling='no' "
					+ "marginheight='0' marginwidth='0' noresize='noresize' ";
			final String strFrameOptResize = " frameborder='0' scrolling='yes' ";
			
		    writer.print( "<frameset" + strFramesetOpts + "id='frameTop' name='frameTop' "
		    		+ " rows='20px,*,46px'>\n" );
		    
		    writer.print( "  <frame" + strFrameOpts + "src='" + strURLHeader + "'/>\n" );
		    
		    if ( DisplayMode.NORMAL.equals( mode ) ) {
		    
			    writer.print( "  <frameset" + strFramesetOpts + "id='frameMiddle' name='frameMiddle' "
			    		+ "cols='" + TeslaPage.NAV_WIDTH + "px,1,*,30px'>\n" );
			    
			    writer.print( "    <frame" + strFrameOpts + "src='" + strURLNav + "'/>\n" );
			    writer.print( "    <frame" + strFrameOpts + "src='" + strURLLMargin + "'/>\n" );
			    writer.print( "    <frame" + strFrameOptResize + "id='frameBody' "
			    		+ "name='frameBody' src='" + strURLBody + "'/>\n" );
			    writer.print( "    <frame" + strFrameOpts + "src='" + strURLRight + "'/>\n" );
			    writer.print( "  </frameset>\n" );
		    } else {
			    writer.print( "    <frame" + strFrameOptResize + "id='frameBody' "
			    		+ "name='frameBody' src='" + strURLBodyImage + "'/>\n" );
		    }
		    writer.print( "  <frame" + strFrameOpts + "src='" + strURLFooter + "'/>\n" );
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
			
			if ( DisplayMode.NORMAL.equals( mode ) ) {
				final String strColor = frame.strColor;
				writer.print( "<body style='background-color:" + strColor + ";'>\n" );
			} else {
				writer.print( "<body>\n" );
			}
			
		    
		    switch ( frame ) {
			    case NAV: {
//				    writer.println( "<br>" );
//				    writer.println( "<a class='info' href='" + strURLNav + "'>NAV link</a>" );
				    writer.println( "<br>" );

				    writer.println( "<script>\n"
				    		+ "function selectNav( sender ) {\n"
				    		+ "    const listNav = document.getElementsByClassName( 'nav-selected' );\n"
				    		+ "    const listFrames = parent.frames;\n"
				    		+ "    var i;\n"
				    		+ "    for ( i=0; i<listNav.length; i++ ) {\n"
				    		+ "        listNav[i].classList.add('nav');\n"
				    		+ "        listNav[i].classList.remove('nav-selected');\n"
				    		+ "    }\n"
				    		+ "    sender.classList.toggle('nav-selected');\n"
				    		
				    		+ "    for ( i=0; i<listFrames.length; i++ ) {\n"
				    		+ "        const frame = listFrames[i];\n"
				    		+ "        if ( frame.id=='frameBody' \n"
				    		+ "                || frame.name=='frameBody' ) {\n"
							+ "            const strNewLocation = '" + strURLBody + "&PAGE=' + sender.id;\n"
							+ "            frame.location = strNewLocation;\n"
							+ "            return;\n"
				    		+ "        }\n"
				    		+ "    }\n"
				    		+ "    alert('Body frame not found');\n"
				    		+ "}\n"
				    		+ "</script>\n" );

				    
			    	final String strURLNavSplit = strURLBase + "/images/nav_split.png";

//			    	writer.println( "<table width='100%' height='100%'>" );
			    	writer.println( "<table class='table-nav'>" );
				    for ( final NavItem item : NavItem.values() ) {
				    	
				    	final String strClass;
				    	if ( NavItem.STILL_CAP.equals( item ) ) {
				    		strClass = "nav-selected";
				    	} else {
				    		strClass = "nav";
				    	}
				    	
				    	writer.println( "<tr height='40' "
				    			+ "width='" + ( TeslaPage.NAV_WIDTH + 2 ) + "px'>" );
				    	writer.println( "<td class='" + strClass + "' "
				    			+ "width='100%' align='right' "
				    			+ "onclick='selectNav(this);' "
				    			+ "id='" + item.name() + "'>" );
				    	writer.println( item.strCaption );
				    	writer.println( "</td></tr>" );
				    	writer.println( "<tr><td align='right'>" );
				    	writer.println( "<img align='right' width='90%' height='1px' src='" + strURLNavSplit + "'>" );
				    	writer.println( "</td></tr>" );
				    }
			    	writer.println( "</table>" );
			    	break;
			    }
			    case FOOTER: {
			    	
			    	final String strImgOptions= strURLBase + "/icons/options.png";
			    	final String strImgBuilding=strURLBase + "/icons/iconmonstr-building-8.svg";
			    	final String strImgGear   = strURLBase + "/icons/iconmonstr-gear-2.svg";
			    	final String strImgRefresh= strURLBase + "/icons/iconmonstr-refresh-3.svg";
			    	final String strImgWindow = strURLBase + "/icons/iconmonstr-window-11.svg";
			    	final String strImgSquare = strURLBase + "/icons/iconmonstr-square-4.svg";
			    	final String strImgSun    = strURLBase + "/icons/iconmonstr-weather-2.svg";
			    	final String strImgMoon   = strURLBase + "/icons/iconmonstr-weather-115.svg";
			    	
			    	writer.println( "<table width='100%'><tr>" );
//			    	writer.println( "<td width='40px' align='right'>" );
			    	writer.println( "<td width='40px' align='center'>" );
//			    	writer.println( "<img align='right' width='30px' height='30px' src='" + strImgSquare + "'>" );
			    	writer.println( "<img class='img-icon' opacity='0.1' src='" + strImgSquare + "'>" );
			    	writer.println( "</td><td width='40px' align='center'>" );
//			    	writer.println( "<img align='right' width='30px' height='30px' src='" + strImgGear + "'>" );
			    	writer.println( "<img class='img-icon' src='" + strImgGear + "'>" );
//			    	writer.println( "</td><td width='40px' align='center'>" );
//			    	writer.println( "<img align='right' width='30px' height='30px' src='" + strImgWindow + "'>" );
			    	writer.println( "</td><td width='40px' align='center'>" );
//			    	writer.println( "<img align='right' width='30px' height='30px' src='" + strImgBuilding + "'>" );
			    	writer.println( "<img class='img-icon' src='" + strImgBuilding + "'>" );
			    	writer.println( "</td><td width='40px' align='center'>" );
//			    	writer.println( "<img align='right' width='30px' height='30px' src='" + strImgMoon + "'>" );
			    	writer.println( "<img class='img-icon' src='" + strImgMoon + "'>" );
			    	writer.println( "</td><td width='40px' align='center'>" );
//			    	writer.println( "<img align='right' width='30px' height='30px' src='" + strImgRefresh + "'>" );
			    	writer.println( "<img class='img-icon' src='" + strImgRefresh + "'>" );
			    	writer.println( "</td>" );
			    	writer.println( "</td><td width='20px' align='center'>" );
			    	writer.println( "</td>" );
			    	writer.println( "<td class='info' id='status_line' width='400px' align='left'>Status messages go here (left-1)<br>Second line for messages</td>" );
			    	writer.println( "<td align='center'>" );
//			    	writer.println( "(footer)" );
				    writer.println( "<a class='info' href='" + strURLFooter + "'>FOOTER link</a>" );
			    	writer.println( "</td>" );
			    	

			    	writer.println( "<td class='info' id='client_info' width='400px' align='right'>(right-1) Resolution: 1234x567<br>Connection: Good</td>" );
			    	writer.println( "<td width='60px'></td>" );
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

				    final String strPage = map.get( ParameterName.PAGE );

			    	final boolean bSuccess;

			    	if ( StringUtils.isNotEmpty( strFullImage ) ) {
			    		
			    		final String strFull = 
			    				strURLBase + "/ui/gcs?name=" + strFullImage;
			    		
				    	writer.println( "<table width='100%'>" );
				    	writer.println( "<tr width='100%'><td align='center'>" );

						writer.println( "<div class='div-fullimage'>" );
						writer.println( "<img class='image-fullimage' "
								+ "src='" + strFull + "' "
								+ "onclick='window.history.back();'>" );
						writer.println( "</div>" );
						
				    	writer.println( "</td></tr></table>" );

			    		bSuccess = true;
			    		
			    	} else {
				    	
//					    final String strPage = map.get( ParameterName.PAGE );
					    final NavItem nav = NavItem.getNavItem( strPage );
				    	
				    	if ( null!=nav ) {
				    		boolean bResult = false;
				    		try {
				    			bResult = TeslaPage.generatePageOutput( 
				    							nav, writer, strURLBase );
				    		} catch ( final Exception e ) {
				    			// ignore for now
				    		}
				    		bSuccess = bResult;
				    	} else {
				    		bSuccess = false;
				    	}
			    	}

			    	if ( !bSuccess ) {
				    	writer.println( "<table class='table-body'><tr><td>" );
				    	
					    writer.print( "<h2>Tesla/" + frame.name() + " 002</h2>" );
					    
					    writer.print( "<h2>strURLRequest: " + strURLRequest + "</h2>" );
					    writer.print( "<h2>Page: " + strPage + "</h2>" );
					    writer.print( "<h2>Mode: " + mode.name() + "</h2>" );
					    writer.print( "<h2>Full_Image: " + strFullImage + "</h2>" );
					    
	
					    writer.println( "<br><br>" );
					    writer.println( "<a class='info' href='" + strURLBody + "'>BODY link</a>" );
					    writer.println( "<br><br>" );
	
				    	writer.println( "</td></tr></table>" );
			    	}
				    
			    	break;
			    }
			    case LEFT_MARGIN: {
			    	// draw border?
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