package jmr.util.http;


/*

	This would just be  org.apache.http.entity.ContentType,
	but using that sometimes results in a  
	java.lang.NoSuchFieldError: IMAGE_PNG

 */
public enum ContentType {

    TEXT_HTML( "text/html" ),
    TEXT_PLAIN( "text/plain" ),
    
	IMAGE_PNG( "image/png" ),
    IMAGE_JPEG( "image/jpeg" ),
    
    APP_JSON( "application/json" ),
    JSON_TEXT( "application/json" ),
    XML( "application/xml" ),

    JAVASCRIPT( "application/javascript" ),

    BINARY( "application/octet-stream" ),
    ZIP( "application/zip" ),
    X7Z( "application/x-7z-compressed" ),
    ;
	
	private final String strMimeType;
	
	ContentType( final String strMimeType ) {
		this.strMimeType = strMimeType;
	}
	
	public String getMimeType() {
		return strMimeType;
	}
	
	public static ContentType getContentType( final String strValue ) {
		if ( null==strValue ) return null;
		
		for ( final ContentType type : ContentType.values() ) {
			if ( type.name().equalsIgnoreCase( strValue ) ) {
				return type;
			}
			
			if ( type.getMimeType().equalsIgnoreCase( strValue ) ) {
				return type;
			}
		}
		return null;
	}
	
}
