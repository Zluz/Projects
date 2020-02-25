package jmr.pr123.storage;

import java.util.EnumMap;
import java.util.Map;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Blob.BlobSourceOption;

import jmr.pr122.DocMetadataKey;

public class GCSFileReader {

	private final Blob blob;
	
	public GCSFileReader( final Blob blob ) {
		if ( null==blob ) throw new IllegalStateException( "Blob is null" );
		this.blob = blob;
	}
	
	
	public String getName() {
		return this.blob.getName();
	}

	public String getContentType() {
		return this.blob.getContentType();
	}
	
	public Long getUpdateTime() {
		return this.blob.getUpdateTime();
	}

	public Long getGeneration() {
		return this.blob.getGeneration();
	}

	public Long getCreateTime() {
		return this.blob.getCreateTime();
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
		if ( null!=mapRaw && !mapRaw.isEmpty() ) {
			for ( final DocMetadataKey key : DocMetadataKey.values() ) {
				final String strKey = key.name();
				if ( mapRaw.containsKey( strKey ) ) {
					final String strValue = mapRaw.get( strKey );
					mapDMK.put( key, strValue );
				}
			}
		}
		return mapDMK;
	}
	
	public String get( final DocMetadataKey key ) {
		if ( null==key ) return null;

		final Map<String, String> map = this.blob.getMetadata();
		if ( null==map ) return null;

		final String strKey = key.name();
		final String strResult = map.get( strKey );
		return strResult;
	}
	
	

	public static void main( final String[] args ) {
		// see GCSFactory for read examples
	}
	
}
