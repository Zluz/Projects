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
		LEFT_MARGIN( "#C0C0C0" ),
		;
		
		public final String strColor;
		
		Frame( final String strColor ) {
			this.strColor = strColor;
		}
	}

	enum NavItem {
		STILL_CAP( "Still Captures" ),
		SCREENSHOTS( "Screenshots" ),
		TESLA_JSON( "Tesla JSON" ),
		NEST_JSON( "Nest JSON" ),
		SPOTIFY( "Spotify" ),
//		S2_DEVICES( "S2 Devices" ),
//		S2_EVENTS( "S2 Events" ),
		GAE_UTIL( "App Engine utils" ),
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

		final String strURLHeader = strURLRequest + "?name=HEADER";
		final String strURLFooter = strURLRequest + "?name=FOOTER";
		final String strURLNav    = strURLRequest + "?name=NAV";
		final String strURLLMargin= strURLRequest + "?name=LEFT_MARGIN";
		final String strURLBody   = strURLRequest + "?name=BODY";
		final String strURLRight  = strURLRequest + "?name=RIGHT";
	    
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
			
		    writer.print( "<frameset" + strFramesetOpts + "id='frameTop' name='frameTop' rows='20px,*,46px'>\n" );
		    writer.print( "  <frame" + strFrameOpts + "src='" + strURLHeader + "'/>\n" );
		    writer.print( "  <frameset" + strFramesetOpts + "id='frameMiddle' name='frameMiddle' cols='280px,1,*,30px'>\n" );
		    writer.print( "    <frame" + strFrameOpts + "src='" + strURLNav + "'/>\n" );
		    writer.print( "    <frame" + strFrameOpts + "src='" + strURLLMargin + "'/>\n" );
		    writer.print( "    <frame" + strFrameOptResize + "id='frameBody' name='frameBody' src='" + strURLBody + "'/>\n" );
		    writer.print( "    <frame" + strFrameOpts + "src='" + strURLRight + "'/>\n" );
		    writer.print( "  </frameset>\n" );
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
			
			final String strColor = frame.strColor;
			
		    writer.print( "<body style='background-color:" + strColor + ";'>\n" );
			
		    
		    switch ( frame ) {
			    case NAV: {
//				    writer.println( "<h2>Tesla/" + frame.name() + "</h2>" );
			    	
				    writer.println( "<br>" );
				    writer.println( "<a class='info' href='" + strURLNav + "'>NAV link</a>" );
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
				    		
//				    		+ "    alert( listFrames.length );\n"
//				    		+ "    alert( window.document.getElementsByTagName('frame').length );\n"
//				    		+ "    const listFrames = window.document.getElementsByTagName('frame');\n"
//				    		+ "    for ( i=0; i<listFrames.length; i++ ) {\n"
//				    		+ "        alert( listFrames[i] );\n"
//				    		+ "    }\n"
//				    		+ "    alert( $('frameset[]') );\n"
//				    		+ "    alert( $('frame[id=frameBody]') );\n"
//				    		+ "    alert( frameBody );\n"
//				    		+ "    alert( window.frames );\n"
//				    		+ "    alert( window.frames[0] );\n"
//				    		+ "    alert( parent.frames.length );\n"
				    		+ "    for ( i=0; i<listFrames.length; i++ ) {\n"
				    		+ "        const frame = listFrames[i];\n"
//				    		+ "        alert( listFrames[i] );\n"
				    		+ "        if ( frame.id=='frameBody' \n"
				    		+ "                || frame.name=='frameBody' ) {\n"
//				    		+ "            alert('frame found!');\n"
							+ "            const strNewLocation = '" + strURLBody + "&PAGE=' + sender.id;\n"
//							+ "            alert( strNewLocation );\n"
//							+ "            frame.src = strNewLocation;\n"
							+ "            frame.location = strNewLocation;\n"
							+ "            return;\n"
				    		+ "        }\n"
				    		+ "    }\n"
				    		+ "    alert('Body frame not found');\n"
//				    		+ "    const frameParent = p"
//				    		+ "    const frameTop = window.frames['frameTop'];\n"
//				    		+ "    const frameMiddle = window.frames['frameMiddle'];\n"
//				    		+ "    const frameBody = frameMiddle.frames['frameMiddle'];\n"
//				    		+ "    frameBody.src = '" + strURLBody + "?PAGE=' + sender.id;\n"
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
				    	
				    	writer.println( "<tr width='282px' height='40'>" );
				    	writer.println( "<td class='" + strClass + "' "
				    			+ "width='100%' align='right' "
				    			+ "onclick='selectNav(this);' "
				    			+ "id='" + item.name() + "'>" );
//				    	writer.println( "<h5 align='right'>" + item.name() + "</h5>" );
//				    	writer.println( "<br><text style='font-size:20'>" + item.strCaption + "</text><br><br>" );
//				    	writer.println( item.strCaption + "&nbsp;&nbsp;&nbsp;<br>" );
//				    	writer.println( "<img width='80%' height='3px' src='" + strURLNavSplit + "'><br>" );
				    	writer.println( item.strCaption );
//				    	writer.println( "&nbsp;&nbsp;&nbsp;&nbsp;" );
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
			    	
			    	writer.println( "<table class='table-body'><tr><td>" );
			    	
				    writer.print( "<h2>Tesla/" + frame.name() + " 002</h2>" );
				    
				    final String strPage = map.get( ParameterName.PAGE );

				    writer.print( "<h2>Page: " + strPage + "</h2>" );

				    writer.println( "<br><br>" );
				    writer.println( "<a class='info' href='" + strURLBody + "'>BODY link</a>" );
				    writer.println( "<br><br>" );

			    	writer.println( "</td></tr></table>" );
				    
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