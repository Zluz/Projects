package jmr.pr132.file;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jmr.pr131.data.FileCache;
import jmr.pr131.web.Constants;
import jmr.util.http.ContentRetriever;
import jmr.util.http.ContentType;

public class Handler {

	final static SimpleDateFormat DATE_FORMATTER = 
							new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	final static String HOST_SERVICE = "https://pr131-s2ae-control.appspot.com";
	final static String HOST_LOCAL = "http://localhost:8080";
	final static String URL_LOCATION_GET = "/file/reload";
	final static String URL_LOCATION_POST = "/file";
	
	public final static String FILENAME = Constants.FILE_CONTROL;
	
	final FileCache filecache = new FileCache( Constants.BUCKET_NAME );

	final ContentRetriever crGet;
	final ContentRetriever crPost;
	

	
	public Handler( final boolean bLocal ) {
		if ( bLocal ) {
			crGet = new ContentRetriever( HOST_LOCAL + URL_LOCATION_GET );
			crPost = new ContentRetriever( HOST_LOCAL + URL_LOCATION_POST );
		} else {
			crGet = new ContentRetriever( HOST_SERVICE + URL_LOCATION_GET );
			crPost = new ContentRetriever( HOST_SERVICE + URL_LOCATION_POST );
		}
	}

	public Handler() {
		this( false );
	}
	
	
	
	
//	public String loadControlFile() {
//		final FileRecord record = filecache.getFileRecord( FILENAME );
//		final String strContent = record.getContent();
//		return strContent;
//	}
	

	public String loadControlFile() {
		try {
			final String strContent = crGet.getContent( ContentType.TEXT_PLAIN );
			if ( strContent.startsWith( "<!DOCTYPE html" ) ) {
				System.err.println( "ERROR: Wrong content retrieved "
							+ "from Control File service." );
			}
			return strContent;
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	
	public boolean saveControlFile( final String strContent ) {
//		final boolean bResult = filecache.saveFileContent( FILENAME, strContent );
//		return bResult;
		try {
			crPost.addFormValue( "textarea", strContent );
			crPost.addFormValue( "filename", "control.txt" );
			crPost.addPropertyEncoded( "textarea", strContent );
			crPost.addProperty( "filename", "control.txt" );
			crPost.postForm();
			
			final String strRemoteContent = loadControlFile();
			if ( ! strContent.equals( strRemoteContent ) ) {
				System.err.println( "ERROR: "
						+ "Content does not appear to have been updated." );
				return false;
			}
			
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	private Operation examine( final String strLine ) {
		if ( null==strLine ) return null;
		if ( strLine.isEmpty() ) return null;
		
		final String strNorm = strLine.trim().toUpperCase();
		for ( final Operation op : Operation.values() ) {
			if ( strNorm.equals( op.name() ) ) {
				return op;
			}
		}
		return null;
	}
	
	public List<Operation> checkForNewWork() {
		
		final Date dateNow = new Date();
		
		final List<Operation> list = new LinkedList<>();
		
		final String strContent = loadControlFile();
		final StringBuilder sb = new StringBuilder();
		
		boolean bInControl = false;
		boolean bEdited = false;
		for ( final String strRaw : strContent.split( "\\n" ) ) {
			final String strLine = strRaw.trim();
			final String strWrite;
			if ( ! strLine.isEmpty() && '#' != strLine.charAt( 0 ) ) {
				
				if ( ! bInControl ) {
					if ( "[CONTROL]".equalsIgnoreCase( strLine ) ) {
						bInControl = true;
					}
					strWrite = strLine;
				} else if ( '[' == strLine.charAt( 0 ) ) {
					bInControl = false;
					strWrite = strLine;
				} else {
					
					final Operation op = examine( strLine );
		
					if ( null!=op ) {
						list.add( op );
					}
					
					strWrite = "# " + strLine 
							+ " # " + DATE_FORMATTER.format( dateNow );
					bEdited = true;
				}
			} else {
				strWrite = strLine;
			}
			
			sb.append( strWrite );
			sb.append( '\n' );
		}
		
		if ( bEdited ) {
			saveControlFile( sb.toString() );
		}
		
		return list;
	}
	
	
	
	public static void main( final String[] args ) {
		System.out.println( "Instantiating Handler.." );
		final Handler handler = new Handler( true );
		System.out.println( "Checking for new work.." );
		final List<Operation> list = handler.checkForNewWork();
		System.out.println( "Work found: " + list.size() + " items" );
		for ( final Operation op : list ) {
			System.out.println( "\t" + op.name() );
		}
	}
	
}
