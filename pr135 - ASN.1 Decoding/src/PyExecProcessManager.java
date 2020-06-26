import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class PyExecProcessManager {
	

	public final static String WORKING_DIR = System.getProperty( "user.dir" ) 
					+ "\\files\\test_exe.006\\dist\\asn1tools_003";
	
	public static final String PROG_FILE = "asn1tools_003.exe";
	
	public static final String CMD_PYTHON = 
			"C:\\Development\\Runtimes\\Python 3.8.1 (32bit)\\python.exe";
	
	public final static String[] CMD_PARAMS = {
			
//			CMD_PYTHON,
//			"-u",
//			"-O",
//			"C:\\Development\\CM\\Git_20190124.002\\Projects__20190125\\"
//						+ "pr135 - ASN.1 Decoding\\files\\asn1tools_003.py",

			WORKING_DIR + "\\" + PROG_FILE
		};
	
	public final static String[] TIMED_INPUT = {
			"",
			"",
			"",
			"",
			"",
			"",
//			"help",
			"k BCCH-DL-SCH-Message",
			"h 000E8409D590440590808FC208817A100802411016C1E00242348261892228880800",
			"decode",
			"",
			"exit"
		};

	
	public static void attachOutput( 	final InputStream is,
										final String strPrefix,
										final Process process ) {
		final Thread threadStdOut = new Thread() {
			
			public void run() {
				
//				System.out.println( "Output monitor thread starting." );
				
				final InputStreamReader isr = new InputStreamReader( is );
				try ( final BufferedReader br = new BufferedReader( isr ) ) {
					
					String strLine = null;
					while ( process.isAlive() ) {
						while ( ( strLine = br.readLine() ) != null 
													&& process.isAlive() ) {
							System.out.println( strPrefix + strLine );
						}
					}
					
				} catch ( final IOException e ) {
					System.out.println( e.toString() + " encountered " );
				}
//				System.out.println( "Output monitor thread ending." );
			};

		};
		threadStdOut.start();

	}
	
	
	
	public static void main( final String[] args ) 
					throws IOException, InterruptedException {
		
		final String[] arrCommand = CMD_PARAMS;
		
		final File fileDir = new File( WORKING_DIR );
		
		System.out.println( "Launching process:" );
		for ( final String strCommand : arrCommand ) {
			System.out.println( "\t" + strCommand );
		}
		
		final Process process = Runtime.getRuntime().exec( 
									arrCommand, new String[]{}, fileDir );
		
		final InputStream isOut = process.getInputStream();
		final InputStream isErr = process.getErrorStream();
		final OutputStream os = process.getOutputStream();

		attachOutput( isOut, "out> ", process );
		attachOutput( isErr, "err> ", process );
		
		int iCount = 0;
		
		while ( process.isAlive() ) {
			iCount++;
//			System.out.println( "Process is still running" );
			TimeUnit.SECONDS.sleep( 1 );

			final String strInput;
			
			if ( iCount < TIMED_INPUT.length ) {
				strInput = TIMED_INPUT[ iCount ];
			} else {
				strInput = "";
			}
			
			if ( ! strInput.isEmpty() ) {
				System.out.println( "sending> " + strInput );
				try {
					os.write( strInput.getBytes() );
					os.write( "\n".getBytes() );
					os.flush();
				} catch ( final Exception e ) {
					System.err.println( ">>> Exception: " + e.toString() );
				}
			}
		}
		
		final int iExit = process.exitValue();
		System.out.println( "Process ended, exit code: " + iExit );
	}
	
}
