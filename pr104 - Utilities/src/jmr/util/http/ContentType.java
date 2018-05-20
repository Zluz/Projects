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
	;
	
	private final String strMimeType;
	
	ContentType( final String strMimeType ) {
		this.strMimeType = strMimeType;
	}
	
	public String getMimeType() {
		return strMimeType;
	}
	
}
