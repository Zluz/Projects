package jmr.pr115.rules.drl;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map.Entry;

import org.apache.commons.codec.Charsets;

import jmr.pr122.DocMetadataKey;
import jmr.pr123.storage.GCSFactory;
import jmr.pr123.storage.GCSFileWriter;
import jmr.util.SUProperty;
import jmr.util.SystemUtil;
import jmr.util.http.ContentType;

public class CloudUtilities {

	
	final static GCSFactory factory;
	
	static {
		final String strBucket = SystemUtil.getProperty( SUProperty.GCS_BUCKET );
		factory = new GCSFactory( strBucket );
	}
	
	public static void saveImage(	
						final String strFilename,
						final File file,
						final ContentType type,
						final EnumMap<DocMetadataKey, String> mapMetadata ) {
		final GCSFileWriter writer = factory.create( strFilename, type );
		try {
			for ( final Entry<DocMetadataKey, String> 
										entry : mapMetadata.entrySet() ) {
				final String strKey = entry.getKey().name();
				writer.put( strKey, entry.getValue() );
			}
//			writer.getMap().putAll( mapMetadata );
			writer.upload( file );
		} catch ( final IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public static void saveJson(	
						final String strFilename,
						final String strJson,
						final ContentType type,
						final EnumMap<DocMetadataKey, String> mapMetadata ) {
		final GCSFileWriter writer = factory.create( strFilename, type );
		try {
			if ( null!=mapMetadata ) {
				for ( final Entry<DocMetadataKey, String> 
											entry : mapMetadata.entrySet() ) {
					final String strKey = entry.getKey().name();
					writer.put( strKey, entry.getValue() );
				}
			}
			writer.upload( strJson.getBytes( Charsets.UTF_8 ) );
			
		} catch ( final Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
