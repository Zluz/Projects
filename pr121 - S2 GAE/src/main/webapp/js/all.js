

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
// window.addEventListener( 'resize', sendClientInfo );


function doFullImageLoaded( image ) {
    const iHeight = window.innerHeight - 50;
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


// https://blog.jedox.com/screen-shot-web-via-javascript-saving-back-server/
function doTakeScreenshot() {
	console.log( 'Taking a screenshot..' );
	
	const body = window.parent.document.body;
//	const body = window.getFrame('frameBody').content.document.body
	
	html2canvas( body, {
		onrendered: function( canvas ) {
			console.log( 'In onrendered function..' );
			var canvasData = canvas.toDataURL( "image/png" );
			var ajax = new XMLHttpRequest();
			// const iTime = (new Date).getTime();
//			const strTime = (new Date).toISOString();
			const d = new Date;
			function pad(n) {return n<10 ? '0'+n : n}
			const strTime = '-' + d.getUTCFullYear()
			        + pad( d.getUTCMonth() + 1 )
			        + pad( d.getUTCDate() )
			        + '-' + pad( d.getUTCHours() )
			        + pad( d.getUTCMinutes() )
			        + pad( d.getUTCSeconds() );
			const strFilename = "web-screenshot-" + strTime + ".png"; 
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
