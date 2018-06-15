package jmr.pr121.servlets;

public interface ServletConstants {

	
	

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
	    		+ "    const width = window.outerWidth;\n"
	    		+ "    const height = window.outerHeight;\n"
	    		+ "    const screenX = window.screen.width;\n"
	    		+ "    const screenY = window.screen.height;\n"
	    		+ "    const info = '' + width + 'x' + height "
	    					+ "+ ',' + screenX + 'x' + screenY;\n"
	    		+ "    $.get(\"/ui/input?CLIENT_INFO=\" + info + \"\", function(data, status){\n" 
//	    		+ "        img.attr( 'src', '/images/check-outline-512.png' );\n"
//	    		+ "        alert(\"Data: \" + data + \"\\nStatus: \" + status);\n" 
	    		+ "    });\n"
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
