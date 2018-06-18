

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

function sendClientInfo() {
    const innerX = window.innerWidth;
    const innerY = window.innerHeight;
    const inner = 'inner:' + innerX + 'x' + innerY;
    $.get("/ui/input?CLIENT_INFO=" + inner, function(data, status){});
    const screenX = window.screen.width;
    const screenY = window.screen.height;
    const screen = 'screen:' + screenX + 'x' + screenY;
    $.get("/ui/input?CLIENT_INFO=" + screen, function(data, status){});
    const pixel_depth = 'pixel_depth:' + window.screen.pixelDepth;
    $.get("/ui/input?CLIENT_INFO=" + pixel_depth, function(data, status){});
}

document.addEventListener( 'load', sendClientInfo );
document.addEventListener( 'resize', sendClientInfo );
window.addEventListener( 'load', sendClientInfo );
window.addEventListener( 'resize', sendClientInfo );

function doFullImageLoaded( image ) {
    image.style.height='500px';
}

function doClickOnFullImage( image ) {
    window.history.back();
}

