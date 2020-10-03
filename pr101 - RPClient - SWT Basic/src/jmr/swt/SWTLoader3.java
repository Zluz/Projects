package jmr.swt;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import jmr.rpclient.SWTBasic;

/**
 * Copied between pr101, pr136 //TODO try again to make this shared code
 * @author usr111
 *
 */
public class SWTLoader3 {


//	final static URLClassLoader classloader = 
//			(URLClassLoader) ClassLoader.getSystemClassLoader();
	
	final public static ClassLoader 
			SYSTEM_CLASSLOADER = ClassLoader.getSystemClassLoader();
	
	
	

	public static void printLoadedClasses() {
		try {
//			System.out.println( "Currently loaded classes:" );
//
//			final Field field = ClassLoader.class.getDeclaredField( "classes" );
//			field.setAccessible( true );
//			
////			final ClassLoader ccl = Thread.currentThread().getContextClassLoader();
			final ClassLoader ccl = ClassLoader.getSystemClassLoader();
//			@SuppressWarnings("unchecked")
//			final Vector<Class<?>> classes = (Vector<Class<?>>) field.get( ccl );
//			
//			for ( final Class<?> clazz : classes ) {
//				System.out.println( "\t" + clazz.getCanonicalName() );
//			}
			
			System.out.println( "Currently loaded JARs:" );
			
			if ( ccl instanceof URLClassLoader ) {
				for ( final URL url : ((URLClassLoader) ccl).getURLs() ) {
					System.out.println( "\t" + url.toString() );
				}
			}
			
		} catch ( final Exception e ) {
			System.out.println( "Exception encountered: " + e.toString() );
		}
	}
	
	
	
	/*
	 * From:
	 * https://www.chrisnewland.com/select-correct-swt-jar-for-your-os-and-jvm-at-runtime-191
	 */
	public static boolean addJarToClasspath( final String strFile ) {
		if ( null==strFile ) return false;
		if ( strFile.isEmpty() ) return false;
		
		final File fileJar = new File( strFile );
		if ( !fileJar.isFile() ) return false;

		if ( SYSTEM_CLASSLOADER instanceof URLClassLoader ) {
//			System.out.println( "System Classloader is URLClassLoader." );
		} else {
			System.out.println( "WARNING: "
					+ "System Classloader is not a URLClassLoader." );
		}
		

		try {
			System.out.println( "Loading external JAR: " + fileJar );

			final URL url = fileJar.toURI().toURL();
			
			Class<?> clazz = URLClassLoader.class;
			
			final Method method = clazz.getDeclaredMethod( 
					"addURL", new Class<?>[] { URL.class });
			
			method.setAccessible(true);
			method.invoke( SYSTEM_CLASSLOADER, new Object[] { url } );
			
			return true;
			
		} catch ( final Throwable t ) {
			t.printStackTrace();
		}
		return false;
	}
	
	

	public static void loadSWT() {

		final boolean bWin;
		final boolean b64;

		System.out.println( "Attempting to locate SWT libraries.." );
		
		final String strOSName = System.getProperty( "os.name" ).toUpperCase();
		System.out.println( "\tOS Name: " + strOSName );
		final String strOSArch = System.getProperty( "sun.arch.data.model" );
		System.out.println( "\tOS Arch: " + strOSArch );
		
		if ( strOSName.contains( "WINDOWS" ) ) {
			bWin = true;
		} else if ( strOSName.contains( "LINUX" ) ) {
			bWin = false;
		} else {
			bWin = true;
		}
		
		int iArch = 0;
		try {
			iArch = Integer.parseInt( strOSArch );
		} catch ( final NumberFormatException e ) {
			// ignore, but should not happen
		}
		
//		b32 = ( 32==iArch );
		b64 = ( 64==iArch );
		
		System.out.println( "Loading external JARs.." );
		for ( final LibFiles libfiles : LibFiles.values() ) {
			if ( libfiles.matches( bWin, b64 ) ) {
				final List<String> list = libfiles.getFiles();
				boolean bLoaded = false;
				int iIndex = 0;
				for ( iIndex = 0; !bLoaded && iIndex<list.size(); iIndex++ ) {
					final String strFile = list.get( iIndex );
					if ( addJarToClasspath( strFile ) ) {
						bLoaded = true;
						System.out.println( "\tloaded: " + strFile );
					}
				}
			}
		}
		
		printLoadedClasses();
	}

	
	
	public static void main( final String[] args ) {
		
		System.setProperty( "java.util.logging.SimpleFormatter.format", 
						"[%1$tF %1$tT] [%4$-7s] %5$s %n" );
		
		boolean bConsole = false;
		for ( final String arg : args ) {
			if ( arg.toLowerCase().endsWith( "console" ) ) {
				bConsole = true;
			}
		}
		
//		org.eclipse.ui/debug=true
//		org.eclipse.ui/trace/graphics=true
//		System.getenv().put( "org.eclipse.ui/debug", "true" );
//		System.getenv().put( "org.eclipse.ui//debug", "true" );
//		System.getenv().put( "org.eclipse.ui/trace/graphics", "true" );
		if ( !bConsole ) {
			loadSWT();
		} else {
			System.out.println( "Console mode. SWT libraries not loaded." );
		}
//		TestSWT.main( args );
		SWTBasic.main( args );
	}

}
