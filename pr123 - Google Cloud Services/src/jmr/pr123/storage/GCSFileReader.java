package jmr.pr123.storage;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Blob.BlobSourceOption;

public class GCSFileReader {

	private final Blob blob;
	
	public GCSFileReader( final Blob blob ) {
		this.blob = blob;
	}
	
	public String getName() {
		return this.blob.getName();
	}

	public String getContentType() {
		return this.blob.getContentType();
	}
	
	public byte[] getContent() {
		if ( ! this.blob.isDirectory() ) {
			return this.blob.getContent( BlobSourceOption.generationMatch() );
		} else {
			return new byte[]{};
		}
	}
	
	public long getSize() {
		return this.blob.getSize();
	}
	
	
//	public InputStream asInputStream() {
//		
//		final long lSize = this.blob.getSize();
//		
//		try ( final ReadChannel reader = this.blob.reader() ) {
//			
//			final ByteBuffer bytes = ByteBuffer.allocate( 1024 * 64 );
//			while ( reader.read( bytes ) > 0 ) {
//				
//			}
//		}
//	}

}
