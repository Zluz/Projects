package jmr.pr121.servlets;

public interface ServletConstants {

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
	
	final String strStyle = 
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
			"    .table-nav { \n" + 
//			"        border: 2px solid grey;\n" + 
			"        width: 281px;\n" +
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
			"        height: 80px;\n" +
			"        padding-right: 30px;\n" +
			"    }\n" +
			"    .nav-selected { \n" + 
			"        font-family: fontNormal;\n" + 
			"        font-weight: bold;\n" + 
			"        font-size: 28px;\n" + 
//			"        padding: 22;\n" + 
			"        color: #000000;\n" + 
			"        height: 80px;\n" +
			"        letter-spacing: 1px;\n" +
			"        padding-right: 30px;\n" +
			"    }\n" +
			"</style>\n";

    final String strJS = 
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
	    		+ "}"
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
