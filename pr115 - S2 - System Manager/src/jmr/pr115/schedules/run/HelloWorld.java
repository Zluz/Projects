package jmr.pr115.schedules.run;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class HelloWorld {

	public static void main( final String[] args ) {
		
		try {
			final ClassLoader cl = HelloWorld.class.getClassLoader();
			final Charset cs = Charset.defaultCharset();

			
			System.out.println( "IOUtils.LINE_SEPARATOR = " 
								+ IOUtils.LINE_SEPARATOR );
			System.out.println( "IOUtils.LINE_SEPARATOR_WINDOWS = " 
								+ IOUtils.LINE_SEPARATOR_WINDOWS );
			System.out.println( "IOUtils.EOF = " 
								+ IOUtils.EOF );
			
			StringUtils.abbreviate( "test", 10 );
			
			IOUtils.toInputStream( "test", cs );
			
			final String strLoad002 = IOUtils.resourceToString(
					"/jmr/pr115/schedules/ScheduleManager.java", 
					cs, cl );
			
			final String strSnippet = 
						StringUtils.abbreviate( strLoad002, 80 * 10 );
			
			System.out.println( strSnippet );
			
		} catch ( final IOException e ) {
			System.err.println( "Problem loading resources." );
		}
		
	}

}
