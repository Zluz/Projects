
// <tr onClick="toggleRowExpansion( this );" data-expanded='1'> ..
// <td data-detail='detail data' data-summary='summary data'> ..
function toggleRowExpansion( row ) {
	const strExpanded = row.dataset.expanded;

	const cells = row.children;
	for ( var i=0; i<cells.length; i++ ) {
		const cell = cells[i];
		var strText;
		if ( '1' == strExpanded ) {
			strText = cell.dataset.summary;
		} else {
			strText = cell.dataset.detail;
		}
		if ( undefined === strText ) {
			// skip
		} else {
			cell.innerHTML = strText;
		}
	}

	if ( '1' == strExpanded ) {
		row.dataset.expanded = '0';
	} else {
		row.dataset.expanded = '1';
	}
}


// https://stackoverflow.com/questions/247483/http-get-request-in-javascript
function httpGetAsync(theUrl, callback)
{
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function() { 
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText);
    }
    xmlHttp.open("GET", theUrl, true); // true for asynchronous 
    xmlHttp.send(null);
}


// https://www.w3schools.com/Jquery/jquery_get_started.asp
function doUpdate_Test() {
    //alert( 'request submitted..' );
    $.get("/status", function(data, status){
        alert("Data: " + data + "\nStatus: " + status);
    });
}



function doUpdate_Test03() {
    //alert( 'request submitted..' );
    var img = $('#img-status');
    img.attr( 'src', '/images/status-loading.gif' );
    $.get("/ui/input?button=test03", function(data, status){
        img.attr( 'src', '/images/check-outline-512.png' );
        alert("Data: " + data + "\nStatus: " + status);
    });
}
function doEmailRequest( img_id, command ) {
    alert( 'preparing to send email..' );
    var img = $( '#' + img_id );
    img.attr( 'src', '/images/status-loading.gif' );
    $.get("/ui/input?email=" + command + "", function(data, status){
        img.attr( 'src', '/images/check-outline-512.png' );
        alert("Data: " + data + "\nStatus: " + status);
    });
}


function doGoTo( url ) {
    alert( 'going to: ' + url );
    window.open( url );
}


/*
 * just use:  self / self.content / self.content.document
function getCurrentFrame() {
    const listFrames = parent.frames;
    for ( i=0; i<listFrames.length; i++ ) {
       const frameItem = listFrames[i];
       if ( frameItem === this ) {
           return frameItem;
       }
    }
//    alert( 'getCurrentFrame(): Current frame not resolved.' );
}
*/

// ids of frames in the Tesla UI
//   frameBody
//   frameFooter
//   frameNav
//   frameRight
//   frameMargin
//   frameHeader
// (framesets)
//   frameTop:    top FRAMESET
//   frameMiddle: middle FRAMESET
//
function getFrame( strFrameName ) {
    const listFrames = parent.frames;
    for ( i=0; i<listFrames.length; i++ ) {
       const frameItem = listFrames[i];
       if ( frameItem.id==strFrameName 
             || frameItem.name==strFrameName ) {
           return frameItem;
       }
    }
    // alert( 'getFrame(): Frame not found: ' + strFrameName );
}


function doSelectOption( cell, bPost ) {
	const row = cell.parentNode.children;
	for ( var i=0; i<row.length; i++ ) {
		row[i].style = "background-color: #FFFFFF; color: #404040; -webkit-border-radius: 0px;";
	}
							// was	 1470A8
							//   	 1C78A0
	cell.style = "background-color: #1C78A0; color: #FFFFFF; -webkit-border-radius: 6px;";
	const table = cell.parentNode.parentNode.parentNode;
	console.log( "table.id:" + table.id + " = " + cell.textContent );
	if ( bPost ) {
		$.get("/ui/input?CLIENT_INFO=OPTION/" + table.id + ":" + cell.textContent, 
    					function(data, status){});
	}
}


function doInitPage() {
	console.log( '--> doInitPage()' );
	
	// activate options radio-table controls 
	const elements = document.getElementsByClassName( 'init-option' );
//	console.log( 'Initializing ' + elements.length + ' option(s).' );
	for ( var i=0; i<elements.length; i++ ) {
//		console.log( 'element ' + i + ' = ' + elements[i] );
		doSelectOption( elements[i], false );
	}
	
	// this tracks active/idle state
	setupActivityMonitor();

	console.log( '<-- doInitPage()' );
}


// there MUST be a simpler way to do this..
function isCurrentFrame( strFrameName ) {
	const frameTarget = getFrame( strFrameName );
	if ( self == frameTarget ) {
		return true;
	} else {
		return false;
	}
}


// Tesla:
//	inner:1200x1465
//	inner:1200x3343
//	inner:1200x3345
//	inner:1200x3348
//	inner:1200x690
//	inner:1200x693
//	inner:30x624
//	pixel_depth:24
//	screen:1080x1920
//	
function sendClientInfo() {
	
	if ( ! isCurrentFrame( 'frameBody' ) ) {
		return;
	}
//	const frameHere = getCurrentFrame();
//	if ( null==frameHere ) {
////		alert( 'current frame not resolved.' );
//		return;
//	}
//	const strName = frameHere.id;
	
//	alert( '--- sendClientInfo() - 1 - current frame: ' + getCurrentFrame() );
	
    const innerX = window.innerWidth;
    const innerY = window.innerHeight;
    const inner = 'body-frame-inner:' + innerX + 'x' + innerY;
    $.get("/ui/input?CLIENT_INFO=" + inner, function(data, status){});
    // const screenX = window.screen.width;
    // const screenY = window.screen.height;
    // const screen = 'screen:' + screenX + 'x' + screenY;
    // $.get("/ui/input?CLIENT_INFO=" + screen, function(data, status){});
    // const pixel_depth = 'pixel_depth:' + window.screen.pixelDepth;
    // $.get("/ui/input?CLIENT_INFO=" + pixel_depth, function(data, status){});

//	alert( '--- sendClientInfo() - 2' );

    const frameFooter = getFrame( 'frameFooter' );
    if ( null==frameFooter ) {
        alert('sendClientInfo(): Footer frame not found.');
        return;
    }

//	alert( '--- sendClientInfo() - 3' );

    const e = frameFooter.document.getElementById('info-resolution');
    if ( null==e ) {
    	// ignore for now: maybe this happens when still loading..
        // alert('sendClientInfo(): info-resolution not found.');
        return;
    }
//    e.text='Resolution: ' + innerX + 'x' + innerY;
    e.innerHTML = 'Body: ' + innerX + 'x' + innerY;

//	alert( '--- sendClientInfo() - 3' );
}

// document.addEventListener( 'load', sendClientInfo );
document.addEventListener( 'resize', sendClientInfo );
window.addEventListener( 'load', sendClientInfo );
window.addEventListener( 'resize', sendClientInfo );


function doFullImageLoaded( image ) {
    const iHeight = window.innerHeight - 80;
    const iWidth = window.innerWidth;
    const iMaxHeight = iWidth * 5/8;
    iFinalHeight = 0;
    if ( iHeight>iMaxHeight ) {
    	iFinalHeight = iMaxHeight;
    } else {
    	iFinalHeight = iHeight;
    }
    
    console.log( 'setting image height to ' + iFinalHeight );
    image.style.height=iFinalHeight + 'px';
}


function doClickOnFullImage( image ) {
    window.history.back();
}

// web screenshot:
//   https://blog.jedox.com/screen-shot-web-via-javascript-saving-back-server/
// base64 encoding/decoding:
//   https://opinionatedgeek.com/Codecs/Base64Decoder
//   https://www.motobit.com/util/base64-decoder-encoder.asp

function doTakeScreenshot() {
	console.log( 'Taking a screenshot..' );
	
	const body = window.parent.document.body;
//	const body = window.getFrame('frameBody').content.document.body
	
	html2canvas( body, {
		onrendered: function( canvas ) {
			
			console.log( 'In onrendered function..' );

			
			const d = new Date;
			function pad(n) {return n<10 ? '0'+n : n}
			const strTime = '-' + d.getUTCFullYear()
			        + pad( d.getUTCMonth() + 1 )
			        + pad( d.getUTCDate() )
			        + '-' + pad( d.getUTCHours() )
			        + pad( d.getUTCMinutes() )
			        + pad( d.getUTCSeconds() );
			
			
			var dataJPG = canvas.toDataURL( "image/jpeg", 0.5 );
			var ajaxJPG = new XMLHttpRequest();
			const strFilenameJPG = "web-screenshot-" + strTime + ".jpg-b64"; 
			ajaxJPG.open( "POST", '/ui/input' 
						+ '?name=' + strFilenameJPG + '&type=IMAGE_JPEG', false );
			ajaxJPG.setRequestHeader( 'Content-Type', 'application/upload' );
			
			console.log( 'Sending data..' );
			ajaxJPG.send( dataJPG );
			
			console.log( 'Screenshot saved as ' + strFilenameJPG 
					+ ' in ' + dataJPG.length + ' bytes.' );
			

			var canvasData = canvas.toDataURL( "image/png" );
			var ajax = new XMLHttpRequest();
			const strFilename = "web-screenshot-" + strTime + ".png-b64"; 
			ajax.open( "POST", '/ui/input' 
						+ '?name=' + strFilename + '&type=IMAGE_PNG', false );
			ajax.setRequestHeader( 'Content-Type', 'application/upload' );
			
			console.log( 'Sending data..' );
			ajax.send( canvasData );
			
			console.log( 'Screenshot saved as ' + strFilename 
					+ ' in ' + canvasData.length + ' bytes.' );

			
//			alert( 'done' );
		}
	});
}




/*----- monitor activity -----*/
/*
 * see https://www.kirupa.com/html5/detecting_if_the_user_is_idle_or_inactive.htm
 */

var timeoutID;

/** Has the user interacted with the client recently? */
var bIsActive = true;

function setupActivityMonitor() {
    this.addEventListener("mousemove", resetTimer, false);
    this.addEventListener("mousedown", resetTimer, false);
//    this.addEventListener("keypress", resetTimer, false);
//    this.addEventListener("DOMMouseScroll", resetTimer, false);
//    this.addEventListener("mousewheel", resetTimer, false);
    this.addEventListener("touchmove", resetTimer, false);
    this.addEventListener("MSPointerMove", resetTimer, false);
 
    startTimer();
}
//setupActivityMonitor();
 
function startTimer() {
    // wait before calling goInactive
	// original value: 2000 = 2s
	// new value: 60000 = 60s
//    timeoutID = window.setTimeout( goInactive, 60000 );
    timeoutID = window.setTimeout( goInactive, 2000 );
}
 
function resetTimer(e) {
    window.clearTimeout( timeoutID );
 
    goActive();
}
 
function goInactive() {
    // do something

	bIsActive = false;

    const frameFooter = getFrame( 'frameFooter' );
    if ( null!=frameFooter ) {
    	const e = frameFooter.document.getElementById('info-active');
    	if ( null!=e ) {
        	e.innerHTML = 'Client is IDLE';
    	}
	}
}
 
function goActive() {
    // do something

	bIsActive = true;
	
    const frameFooter = getFrame( 'frameFooter' );
    if ( null!=frameFooter ) {
    	const e = frameFooter.document.getElementById('info-active');
    	if ( null!=e ) {
    		e.innerHTML = 'Client is ACTIVE';
    	}
	}

    startTimer();
}



