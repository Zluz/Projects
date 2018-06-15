package jmr.pr123.storage;

import java.util.EnumMap;
import java.util.Map;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Blob.BlobSourceOption;

import jmr.pr122.DocMetadataKey;

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
			try {
				return this.blob.getContent();
			} catch ( final Exception e ) {
				// java.lang.NoSuchMethodError: 
				// com.google.common.base.Preconditions.checkArgument() ...
				// ignore for now .. 
			}
			return this.blob.getContent( BlobSourceOption.generationMatch() );
		} else {
			return new byte[]{};
		}
	}
	
	public long getSize() {
		return this.blob.getSize();
	}
	
	public Map<DocMetadataKey, String> getMap() {
		final EnumMap<DocMetadataKey, String> 
						mapDMK = new EnumMap<>( DocMetadataKey.class );
		final Map<String, String> mapRaw = this.blob.getMetadata();
		for ( final DocMetadataKey key : DocMetadataKey.values() ) {
			final String strKey = key.name();
			if ( mapRaw.containsKey( strKey ) ) {
				final String strValue = mapRaw.get( strKey );
				mapDMK.put( key, strValue );
			}
		}
		return mapDMK;
	}
	
	public String get( final DocMetadataKey key ) {
		if ( null==key ) return null;
		
		final String strKey = key.name();
		final String strResult = this.blob.getMetadata().get( strKey );
		return strResult;
	}
	
	// remove this in the future?
//	public Blob getBlob() {
//		return this.blob;
//	}
	
	
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
