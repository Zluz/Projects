import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

public class PyExecProcessManager {
	

	public final static String WORKING_DIR = 
//			"C:\\Development\\CM\\Git_20190124.002\\Projects__20190125\\"
//					+ "pr135 - ASN.1 Decoding\\files";
			"C:\\Development\\CM\\Git_20190124.002\\Projects__20190125\\"
					+ "pr135 - ASN.1 Decoding\\files\\"
						+ "test_exe.006\\dist\\asn1tools_003";
	
	public static final String PROG_PATH = 
//			"C:\\Development\\CM\\Git_20190124.002\\Projects__20190125\\"
//			+ "pr135 - ASN.1 Decoding\\files\\test_exe.005\\dist\\asn1tools_003";
			WORKING_DIR;
	
//	public static final String PROG_FILE = "test_stdio.exe";
//	public static final String PROG_FILE = "asn1tools_003.exe";
	public static final String PROG_FILE = "asn1tools_003.exe";
	
	
	public static final String CMD_PYTHON = 
			"C:\\Development\\Runtimes\\Python 3.8.1 (32bit)\\python.exe";
	

	public final static String[] CMD_PARAMS = {
			
//			"/bin/sh",
//			"/bin/bash",
//			"/Local/scripts/exec_automationhat_input.sh",
			
//			CMD_PYTHON,
//			"-u",
//			"-O",
//			"C:\\Development\\CM\\Git_20190124.002\\Projects__20190125\\"
//						+ "pr135 - ASN.1 Decoding\\files\\asn1tools_003.py",

			PROG_PATH + "\\" + PROG_FILE
						
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
			
			
//			@Override
//			public void run() {
//				String strLine;
//				try {
//					final BufferedReader br = 
//							new BufferedReader( new InputStreamReader( is ) );
//					while (( strLine = br.readLine()) != null) {
//						System.out.println( strLine );
//					}
//					br.close();
//				} catch ( final Exception e ) {
//					e.printStackTrace();
//				}
//			}
			
//			@Override
//			public void run() {
//				try {
//					Streams.copy( process.getInputStream(), System.out );
//				} catch ( final Exception e ) {
//					e.printStackTrace();
//				}
//			}
			
			public void run() {
				
//				System.out.println( "Output monitor thread starting." );
				
				final InputStreamReader isr = new InputStreamReader( is );
				try ( final BufferedReader br = new BufferedReader( isr ) ) {
					
//					System.out.println( "1" );
					
					String strLine = null;
					while ( process.isAlive() ) {
						while ( ( strLine = br.readLine() ) != null 
													&& process.isAlive() ) {
	//						System.out.println( "2" );
							System.out.println( strPrefix + strLine );
						}
//						System.out.println( "3" );
					}
//					System.out.println( "4" );
					
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
		
//		final File file = new File( PROG_PATH, PROG_FILE );
//		final String strCommand = file.getAbsolutePath();
//		final String strCommand = COMMAND;
//		final String[] arrCommand = { strCommand };
		final String[] arrCommand = CMD_PARAMS;
		
//		final ProcessBuilder pb = new ProcessBuilder( file.getAbsolutePath() );
//		final Process process = pb.start();
		
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
//			if ( iCount < 20 ) {
//				strInput = ""+ Math.random() + "";
//			} else {
//				strInput = "exit";
//			}
			
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


	
//	public static void main_( final String[] args ) 
//					throws IOException, InterruptedException {
//		
//		final File file = new File( PROG_PATH, PROG_FILE );
//		
////		final String strCommand = COMMAND;
//		
////		final String strCommand = file.getAbsolutePath();
////		final String[] arrCommand = { strCommand };
//		final String[] arrCommand = CMD_PARAMS;
//		
////		System.out.println( "Starting: " + strCommand );
//		
//		final MonitorProcess monitor = 
//						new MonitorProcess( "test", arrCommand, true );
//
//		monitor.start();
//
//		monitor.addListener( new MonitorProcess.Listener() {
//			@Override
//			public void process( 	final long lTime, 
//									final String strLine ) {
//				System.out.println( "out> " + strLine );
//			}
//		});
//
//
//		int iCount = 0;
//		
//		while ( monitor.isRunning() ) {
//			iCount++;
////			System.out.println( "Still running" );
//			TimeUnit.SECONDS.sleep( 1 );
//		}
//	}

	
//	public static void main( final String[] args ) 
//					throws IOException, InterruptedException {
//		
//		final File file = new File( PROG_PATH, PROG_FILE );
//		
//		
//		final String strCommand = file.getAbsolutePath();
//		final String[] arrCommand = { strCommand };
//		
//		System.out.println( "Starting: " + strCommand );
//		
//		final RunProcess rp = new RunProcess( arrCommand );
//		rp.run();
//		
//		while ( true ) {
//			System.out.println( rp.getOutputLine() );
//			TimeUnit.SECONDS.sleep( 1 );
//		}
//	}

	
}
