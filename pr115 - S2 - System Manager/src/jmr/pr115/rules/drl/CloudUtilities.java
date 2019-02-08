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

	
	final private static GCSFactory factory;
	
	static {
		final String strBucket = SystemUtil.getProperty( SUProperty.GCS_BUCKET );
		factory = new GCSFactory( strBucket );
	}
	
	
	public static GCSFactory getFactory() {
		return factory;
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
			if ( file.exists() ) {
				writer.upload( file );
			}
		} catch ( final IOException e ) {
			System.err.println( e.toString() + " encountered while uploading "
					+ "an image to Google Cloud Storage." );
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
