package jmr.pr121.servlets;

import com.google.gson.JsonParser;

//import com.google.appengine.repackaged.com.google.gson.JsonParser;

public interface ServletConstants {

	public static final JsonParser JSON_PARSER = new JsonParser();
	
	public final static String COLOR_CONTENT_BACK = "#F2F2F2";
	public final static String COLOR_FONT_DARK = "#606060";
	public final static String COLOR_FONT_MED = "#808080";
	public final static String COLOR_THUMBNAIL_BACK = "#D0D0D0";

	/*
	 * Tesla browser characteristics:

			User-Agent: Mozilla/5.0 (X11; GNU/Linux) AppleWebKit/601.1 (KHTML, like Gecko) Tesla QtCarBrowser Safari/601.1

			color_depth:24
			inner:1200x1465
			inner:1200x690
			jqdim:1200x1465
			jqdim:1200x690
			outer:0x0
			pixel_depth:24
			screen:1080x1920

	 */
	
	final public static String strStyle = 
			"<style>\n" +
			"    @font-face {\n" + 
			"      font-family: fontNormal;\n" + 
			"        src: \n" + 
			"          url( '/fonts/MADE Evolve Sans Light (PERSONAL USE).otf' );\n" + 
//			"          url( '/fonts/MADE Evolve Sans Bold (PERSONAL USE).otf' );\n" + 
			"    }\n" +
			"    @font-face {\n" + 
			"      font-family: fontThin;\n" + 
			"        src: \n" + 
			"          url( '/fonts/MADE Evolve Sans Thin (PERSONAL USE).otf' );\n" + 
//			"          url( '/fonts/MADE Evolve Sans Bold (PERSONAL USE).otf' );\n" + 
			"    }\n" +
			"    @font-face {\n" + 
			"      font-family: fontHeavy;\n" + 
			"        src: \n" + 
//			"          url( '/fonts/MADE Evolve Sans Light (PERSONAL USE).otf' ),\n" + 
			"          url( '/fonts/MADE Evolve Sans Bold (PERSONAL USE).otf' );\n" + 
			"    }\n" +
			// useful reference:
			// https://www.w3schools.com/Css/css_text.asp
			"    p.normal { \n" + 
			"        font-family: fontNormal;\n" + 
			"        font-weight: normal;\n" + 
			"    }\n" +
			"    p { \n" + 
			"        font-family: fontNormal;\n" + 
			"        font-weight: normal;\n" + 
			"    }\n" +
			"    p.thin { \n" + 
			"        font-family: fontHeavy;\n" + 
			"        font-weight: heavy;\n" + 
			"    }\n" +
			
			"    h1 { \n" + 
			"        font-family: fontHeavy;\n" + 
			"        font-weight: bold;\n" + 
			"    }\n" +
			"    h2 { \n" + 
			"        font-family: fontHeavy;\n" + 
			"        font-weight: bold;\n" + 
			"    }\n" +
			
			"    .info { \n" + 
			"        font-family: fontThin;\n" + 
			"        font-weight: normal;\n" + 
			"        font-size: 14px;\n" + 
//			"        color: #606060;\n" + 
			"        color: #000000;\n" + 
			"    }\n" +
			"    .text-normal { \n" + 
			"        font-family: fontNormal;\n" + 
			"        font-weight: normal;\n" + 
			"        font-size: 22px;\n" + 
			"        color: " + COLOR_FONT_MED + ";\n" + 
			"    }\n" +
			"    .text-title { \n" + 
			"        font-family: fontHeavy;\n" + 
			"        font-weight: bold;\n" + 
			"        font-size: 20px;\n" + 
			"        color: " + COLOR_FONT_MED + ";\n" + 
//			"        color: " + "#808080" + ";\n" + 
			"        letter-spacing: 1px;\n" +
			"    }\n" +
			"    .text-image-caption { \n" + 
			"        height: 18px;\n" +
//			"        position: absolute;\n" +
			"        top: -5px;\n" +
			"        font-family: fontNormal;\n" + 
			"        font-weight: normal;\n" + 
			"        font-size: 16px;\n" + 
//			"        vertical-align: middle;\n" + 
			"        color: " + COLOR_FONT_DARK + ";\n" +
			"        text-shadow: 0 0 12px #406080;\n" + 
//			"        background-color: " +  COLOR_THUMBNAIL_BACK + ";\n" +
			"        background: linear-gradient( " + COLOR_THUMBNAIL_BACK + ", #F0F0F0 );" +
			"    }\n" +
			"    .table-nav { \n" + 
//			"        border: 2px solid grey;\n" + 
			"        width: " + ( TeslaPage.NAV_WIDTH + 1 ) + "px;\n" +
//			"        width: 241px;\n" +
			"        height: 100%;\n" +
			"        border-collapse: collapse;\n" + 
			"        padding: 0;\n" + 
			"    }\n" +
			"    .table-body { \n" + 
//			"        border: 2px solid grey;\n" + 
			"        width: 100%;\n" +
			"        height: 100%;\n" +
			"        border-collapse: collapse;\n" + 
			"    }\n" +
			"    .img-icon { \n" + 
			"        width: 24px;\n" +
			"        height: 24px;\n" +
			"        margin-top: 5px;\n" +
			"        margin-left: 9px;\n" +
			"        margin-right: 40px;\n" +
			"        align: center;\n" +
			"        opacity: 0.5;\n" +
			"        border-collapse: collapse;\n" + 
			"    }\n" +
			"    .nav { \n" + 
			"        font-family: fontNormal;\n" + 
			"        font-weight: normal;\n" + 
			"        font-size: 24px;\n" + 
//			"        padding: 20;\n" + 
			"        color: #808080;\n" + 
			"        height: 90px;\n" +
			"        padding-right: 22px;\n" +
			"    }\n" +
			"    .nav-selected { \n" + 
//			"        font-family: fontNormal;\n" + 
			"        font-family: fontHeavy;\n" + 
			"        font-weight: bold;\n" + 
			"        font-size: 26px;\n" + 
//			"        padding: 22;\n" + 
			"        color: #000000;\n" + 
			"        height: 90px;\n" +
			"        letter-spacing: 0.7px;\n" +
			"        padding-right: 22px;\n" +
			"        text-shadow: 2px 2px 2px #E0E0E0;\n" +
			"    }\n" +
			// see   https://www.w3schools.com/Css/css3_images.asp
			"    .div-thumbnail { \n" +
			"        width: 275px;\n" +
//			"        max-width: 275px;\n" + 
			"        background-color: " +  COLOR_THUMBNAIL_BACK + ";\n" + 
			"        box-shadow: 10px 10px 30px rgba( 0, 0, 0, 0.4 ), " +
								"0px 0px 8px rgba( 0, 0, 0, 0.7 );\n" + 
			"    }\n" +
			"    .image-thumbnail { \n" + 
//			"        width: 300px;\n" + 
			"        width: 100%;\n" + 
//			"        background-color: " +  TeslaUIServlet.COLOR_CONTENT_BACK + "\n" + 
//			"        box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 10px 10px 0 rgba(0, 0, 0, 0.19);\n" + 
			"    }\n" +
			// full screen
			"    .div-fullimage { \n" +
//			"        width: 100%;\n" +
			"        height: 100%;\n" +
			"        min-height: 100%;\n" +
//			"        max-width: 1100px;\n" + 
//			"        align: center;\n" + 
//			"        display: block;\n" + 
			"        display: flex;\n" + 
			"        flex-direction: column;\n" + 
//			"        background-color: " +  COLOR_CONTENT_BACK + ";\n" + 
//			"        box-shadow: 0 10px 10px 0 rgba( 0, 0, 0, 0.5 ), "
//								+ "0 20px 20px 0 rgba( 0, 0, 0, 0.19 );\n" + 
			"    }\n" +
			"    .image-fullimage { \n" + 
//			"        width: 300px;\n" + 
//			"        align: center;\n" + 
			"        width: 97%;\n" + 
//			"        height: 90%;\n" + 
			"        padding: 10px;\n" + 
			"        border: 2px solid #ddd;\n" + 
			"        border-radius: 8px;\n" + 
			"        object-fit: fill;\n" + 
			"        padding: 10px;\n" + 
//			"        background-size: auto 100%;\n" + 
//			"        display: flex;\n" + 
//			"        background-color: " +  TeslaUIServlet.COLOR_CONTENT_BACK + "\n" + 
//			"        box-shadow: 0 4px 4px 0 rgba(0, 0, 0, 0.2), 0 10px 10px 0 rgba(0, 0, 0, 0.19);\n" + 
			"    }\n" +

			// not used yet ..
			"    .tr-even { \n" + 
			"        font-family: fontNormal;\n" + 
			"        font-weight: normal;\n" + 
			"        font-size: 20px;\n" + 
			"        color: #808080;\n" + 
//			"        height: 70px;\n" +
//			"        padding-right: 26px;\n" +
			"    }\n" +

			// http://divtable.com/table-styler/
			"table.blueTable {\r\n" + 
			"  border: 1px solid #E2E2E2;\r\n" + 
			"  background-color: #EEEEEE;\r\n" + 
//			"  width: 100%;\r\n" + 
			"  max-width: 500px;\r\n" + 
			"  text-align: left;\r\n" + 
			"  border-collapse: collapse;\r\n" + 
			"}\r\n" + 
			"table.blueTable td, table.blueTable th {\r\n" + 
			"  border: 0px solid #AAAAAA;\r\n" + 
			"  padding: 3px 2px;\r\n" + 
			"}\r\n" + 
			"table.blueTable tbody td {\r\n" + 
			"  font-size: 20px;\n" + 
			"        font-family: fontNormal;\n" + 
			"        color: #606060;" +
			"}\n" + 
			"table.blueTable tr:nth-child(even) {\r\n" + 
			"  background: #EAEAEA;\r\n" + 
			"}\r\n" + 
			"table.blueTable thead {\r\n" + 
			"  background: #1C6EA4;\r\n" + 
			"  background: -moz-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n" + 
			"  background: -webkit-linear-gradient(top, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n" + 
			"  background: linear-gradient(to bottom, #5592bb 0%, #327cad 66%, #1C6EA4 100%);\r\n" + 
			"  border-bottom: 2px solid #444444;\r\n" + 
			"}\r\n" + 
			"table.blueTable thead th {\r\n" + 
			"  font-size: 20px;\r\n" + 
			"  font-weight: bold;\r\n" + 
			"  color: #FFFFFF;\r\n" + 
			"  border-left: 2px solid #444444;\r\n" + 
			"}\r\n" + 
			"table.blueTable thead th:first-child {\r\n" + 
			"  border-left: none;\r\n" + 
			"}\r\n" + 
			"\r\n" + 
			"table.blueTable tfoot {\r\n" + 
			"  font-size: 14px;\r\n" + 
			"  font-weight: bold;\r\n" + 
			"  color: #FFFFFF;\r\n" + 
			"  background: #D0E4F5;\r\n" + 
			"  background: -moz-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n" + 
			"  background: -webkit-linear-gradient(top, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n" + 
			"  background: linear-gradient(to bottom, #dcebf7 0%, #d4e6f6 66%, #D0E4F5 100%);\r\n" + 
			"  border-top: 2px solid #444444;\r\n" + 
			"}\r\n" + 
			"table.blueTable tfoot td {\r\n" + 
			"  font-size: 14px;\r\n" + 
			"}\r\n" + 
			"table.blueTable tfoot .links {\r\n" + 
			"  text-align: right;\r\n" + 
			"}\r\n" + 
			"table.blueTable tfoot .links a{\r\n" + 
			"  display: inline-block;\r\n" + 
			"  background: #1C6EA4;\r\n" + 
			"  color: #FFFFFF;\r\n" + 
			"  padding: 2px 8px;\r\n" + 
			"  border-radius: 5px;\r\n" + 
			"}" +

			// for custom scrollbars
			"$(\"#content-4\").mCustomScrollbar({\n" + 
			"    theme:\"rounded-dots\",\n" + 
			"    scrollInertia:400\n" + 
			"});" +
			"" +
			"</style>\n";
	
	final public static String strLoadFromCDNs =
			
    		"<script src=\"https://ajax.aspnetcdn.com/ajax/jQuery/jquery-3.3.1.min.js\"></script>\n" +
	
    		// Custom scrollbars
    		// http://manos.malihu.gr/repository/custom-scrollbar/demo/examples/complete_examples.html
    		"<script src=\"http://manos.malihu.gr/repository/custom-scrollbar/demo/jquery.mCustomScrollbar.concat.min.js\"></script>\n" +

    		// this JS
    		"<script src=\"/js/all.js\"></script>\n" + 
    		
    		"";

    final public static String _removed_strJS = 
    		"<script>\n"
	    		+ "\n\n\n"
	    		+ "// https://stackoverflow.com/questions/247483/http-get-request-in-javascript"
	    		+ "\n"
	    		+ "function httpGetAsync(theUrl, callback)\n" + 
	    		"{\n" + 
	    		"    var xmlHttp = new XMLHttpRequest();\n" + 
	    		"    xmlHttp.onreadystatechange = function() { \n" + 
	    		"        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)\n" + 
	    		"            callback(xmlHttp.responseText);\n" + 
	    		"    }\n" + 
	    		"    xmlHttp.open(\"GET\", theUrl, true); // true for asynchronous \n" + 
	    		"    xmlHttp.send(null);\n" + 
	    		"}\n"
	    		+ "</script>\n"
	    		+ "\n"
	    		+ "\n"
	    		+ "<script>\n"
	    		+ "// https://www.w3schools.com/Jquery/jquery_get_started.asp"
	    		+ "\n"
	    		+ "function doUpdate_Test() {\n"
	    		+ "    //alert( 'request submitted..' );\n"
	    		+ "    $.get(\"/status\", function(data, status){\n" 
	    		+ "        alert(\"Data: \" + data + \"\\nStatus: \" + status);\n" 
	    		+ "    });\n"
	    		+ "}"
	    		+ "\n\n\n"
	    		+ "\n"
	    		
	    		+ "function doUpdate_Test03() {\n"
	    		+ "    //alert( 'request submitted..' );\n"
	    		+ "    var img = $('#img-status');\n"
	    		+ "    img.attr( 'src', '/images/status-loading.gif' );\n"
	    		+ "    $.get(\"/ui/input?button=test03\", function(data, status){\n" 
	    		+ "        img.attr( 'src', '/images/check-outline-512.png' );\n"
	    		+ "        alert(\"Data: \" + data + \"\\nStatus: \" + status);\n" 
	    		+ "    });\n"
	    		+ "}"
	    		+ "\n"
	    		
	    		+ "function doEmailRequest( img_id, command ) {\n"
	    		+ "    alert( 'preparing to send email..' );\n"
	    		+ "    var img = $( '#' + img_id );\n"
	    		+ "    img.attr( 'src', '/images/status-loading.gif' );\n"
	    		+ "    $.get(\"/ui/input?email=\" + command + \"\", function(data, status){\n" 
	    		+ "        img.attr( 'src', '/images/check-outline-512.png' );\n"
	    		+ "        alert(\"Data: \" + data + \"\\nStatus: \" + status);\n" 
	    		+ "    });\n"
	    		+ "}\n"
	    		+ "\n"
	    		
	    		+ "function doGoTo( url ) {\n"
	    		+ "    alert( 'going to: ' + url );\n"
	    		+ "    window.open( url );\n"
	    		+ "}\n"
	    		+ "\n"
	    		
	    		+ "function sendClientInfo() {\n"
	    		
	    		// does not work in the Tesla
//	    		+ "    const outerX = window.outerWidth;\n"
//	    		+ "    const outerY = window.outerHeight;\n"
//	    		+ "    const outer = 'outer:' + outerX + 'x' + outerY;\n"
//				+ "    $.get(\"/ui/input?CLIENT_INFO=\" + outer, function(data, status){});\n" 
	    		
				// preferred: ui client space
	    		+ "    const innerX = window.innerWidth;\n"
	    		+ "    const innerY = window.innerHeight;\n"
	    		+ "    const inner = 'inner:' + innerX + 'x' + innerY;\n"
				+ "    $.get(\"/ui/input?CLIENT_INFO=\" + inner, function(data, status){});\n" 
	    		
				// preferred: screen device
	    		+ "    const screenX = window.screen.width;\n"
	    		+ "    const screenY = window.screen.height;\n"
	    		+ "    const screen = 'screen:' + screenX + 'x' + screenY;\n"
				+ "    $.get(\"/ui/input?CLIENT_INFO=\" + screen, function(data, status){});\n" 

				// works but skip
//	    		+ "    const jqwinX = $(window).width();\n"
//	    		+ "    const jqwinY = $(window).height();\n"
//	    		+ "    const jqdim = 'jqdim:' + jqwinX + 'x' + jqwinY;\n"
//				+ "    $.get(\"/ui/input?CLIENT_INFO=\" + jqdim, function(data, status){});\n" 

	    		// works but skip
//	    		+ "    const color_depth = 'color_depth:' + window.screen.colorDepth;\n"
//				+ "    $.get(\"/ui/input?CLIENT_INFO=\" + color_depth, function(data, status){});\n" 

				// preferred: color depth
	    		+ "    const pixel_depth = 'pixel_depth:' + window.screen.pixelDepth;\n"
				+ "    $.get(\"/ui/input?CLIENT_INFO=\" + pixel_depth, function(data, status){});\n" 

//	    		+ "    const info = '' + width + 'x' + height "
//	    					+ "+ ',' + screenX + 'x' + screenY;\n"
//	    		+ "    $.get(\"/ui/input?CLIENT_INFO=\" + info + \"\", function(data, status){\n" 
//	    		+ "        img.attr( 'src', '/images/check-outline-512.png' );\n"
//	    		+ "        alert(\"Data: \" + data + \"\\nStatus: \" + status);\n" 
//	    		+ "    });\n"
	    		+ "}\n"
	    		+ "\n"
	    		+ "document.addEventListener( 'load', sendClientInfo );\n"
	    		+ "document.addEventListener( 'resize', sendClientInfo );\n"
	    		+ "window.addEventListener( 'load', sendClientInfo );\n"
	    		+ "window.addEventListener( 'resize', sendClientInfo );\n"
//	    		+ "document.addEventListener( 'load', window.setTimeout( sendClientInfo, 1000 ) );\n"
//	    		+ "document.addEventListener( 'resize', window.setTimeout( sendClientInfo, 1000 ) );\n"
	    		+ "\n"
	    		+ ""
	    		+ "</script>\n";
}
