package jmr.pr131.data;

import java.util.HashMap;
import java.util.Map;

import jmr.pr104.util.http.ContentType;
import jmr.pr123.storage.GCSFactory;
import jmr.pr123.storage.GCSFileReader;
import jmr.pr123.storage.GCSFileWriter;

public class FileCache {

	private final static Map<String,FileRecord> CACHE = new HashMap<>();
	
	public static class FileRecord {
		private final GCSFileReader file;
		private final long lGeneration;
		private final String strContent;
		
		public FileRecord( final GCSFileReader file ) {
			this.file = file;
			this.lGeneration = file.getGeneration();
			this.strContent = new String( file.getContent() );
		}

		public GCSFileReader getFile() {
			return file;
		}

		public long getGeneration() {
			return lGeneration;
		}

		public String getContent() {
			return strContent;
		}
	}

	
	private final GCSFactory factory;
	
	private final String strBucketName;
	
	public FileCache( final String strBucketName ) {
		this.factory = new GCSFactory( strBucketName );
		this.strBucketName = strBucketName;
	}
	
	
	public Long getCurrentGeneration( final String strName ) {
		final GCSFileReader file = factory.getFile( strName );
		if ( null==file ) return null;
		final long lGeneration = file.getGeneration();
		return lGeneration;
	}
	
	
	public String getBucketName() {
		return this.strBucketName;
	}
	
	
	public FileRecord getFileRecord( final String strName ) {
		
		if ( CACHE.containsKey( strName ) ) {
			final FileRecord record = CACHE.get( strName );
			final long lCurrentGeneration = getCurrentGeneration( strName );
			if ( record.getGeneration() == lCurrentGeneration ) {
				return record;
			} else {
				CACHE.remove( strName );
			}
		}
		
		final GCSFileReader file = factory.getFile( strName );
		if ( null==file ) return null;
		
		final FileRecord record = new FileRecord( file );
		CACHE.put( strName, record );
		return record;
	}
	
	
	public String loadFileContent( final String strName ) {
		final FileRecord record = getFileRecord( strName );
		if ( null==record ) {
			return null;
		} else {
			return record.getContent();
		}
	}
	
	
	public boolean saveFileContent( final String strName,
								 	final String strNewContent ) {
		if ( null==strName ) return false;
		if ( null==strNewContent ) return false;
		
		final FileRecord record = getFileRecord( strName );
		if ( null!=record ) {
			final String strExistingContent = record.getContent();
			if ( strNewContent.equals( strExistingContent ) ) {
				return false;
			}
		}
		
//		GCSFileWriter writer = new GCSFileWriter( factory, strBucketName, 
//							strName, null );
		final GCSFileWriter file = 
						factory.create( strName, ContentType.TEXT_PLAIN );
		file.upload( strNewContent.getBytes() );
		return true;
	}
	
}
