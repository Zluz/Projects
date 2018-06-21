package jmr.pr115.rules.drl;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

//import org.apache.http.entity.ContentType;

import com.google.gson.JsonObject;

import jmr.pr102.DataRequest;
import jmr.pr115.actions.SendMessage;
import jmr.pr115.actions.SendMessage.MessageType;
import jmr.pr115.schedules.run.NestJob;
import jmr.pr115.schedules.run.TeslaJob;
import jmr.pr120.Command;
import jmr.pr120.EmailEvent;
import jmr.pr122.CommGAE;
import jmr.pr122.DocKey;
import jmr.pr122.DocMetadataKey;
import jmr.s2.ingest.Import;
import jmr.s2db.imprt.WebImport;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSession.ImageLookupOptions;
import jmr.s2fs.FileSessionManager;
import jmr.util.TimeUtil;
import jmr.util.http.ContentType;
import jmr.util.transform.JsonUtils;

public class Simple implements RulesConstants {
	
	
	
	static {
		TimeUtil.isHourOfDay(); // just to get the import
	}
	
	static NestJob nest = null;

	
	

	public static void doRefreshNest() {
		System.out.println( "--- doRefreshNest(), "
					+ "time is " + LocalDateTime.now().toString() );
		try {
			
			// just to separate the jobs.
			Thread.sleep( TimeUnit.MINUTES.toMillis( 1 ) );
			
			if ( null==nest ) {
				nest = new NestJob( true );
			}
			
			nest.run();
			
//			final long lNow = System.currentTimeMillis();
//			final FullStatus status = nest.callNest();
//			final boolean bResult = nest.process( lNow, status );
			
			
		} catch ( final Throwable t ) {
			System.err.println( 
						"Error during doRefreshNest(): " + t.toString() );
			t.printStackTrace();
		}
	}

	public static void doRefreshWeather() {
		System.out.println( "--- doRefreshWeather(), "
					+ "time is " + LocalDateTime.now().toString() );
		try {
			final Import source = Import.WEATHER_FORECAST__YAHOO;
			
			final String strURL = source.getURL();
			final String strTitle = source.getTitle();
	
			final WebImport wi = new WebImport( strTitle, strURL );
			final Long seq = wi.save();
			
			System.out.println( "Weather refreshed. Result: seq = " + seq );
			
		} catch ( final Throwable t ) {
			System.err.println( 
						"Error during doRefreshWeather(): " + t.toString() );
			t.printStackTrace();
		}
	}
	
	
	public final static List<Job> JOBS = new LinkedList<>();
	
	
	public static synchronized void queueJob( final Job job ) {
		if ( null==job ) return;
		
		if ( null==job.getPartCount() || job.getPartCount()<2 ) {
			workJobs( Collections.singletonList( job ) );
		} else {
			JOBS.add( job );
			
			final long lPartSeq = job.getPartSeq();
			final int iPartCount = job.getPartCount();
			final List<Job> list = new LinkedList<>();
			for ( final Job element : JOBS ) {
				if ( lPartSeq == element.getPartSeq().longValue() ) {
					list.add( element );
				}
			}
			if ( iPartCount == list.size() ) {
				
				for ( final Job element : list ) {
					JOBS.remove( element );
				}
				
				Collections.sort( list, new Comparator<Job>() {
					@Override
					public int compare( final Job lhs, final Job rhs ) {
						return Long.compare( lhs.getJobSeq(), rhs.getJobSeq() );
					}
				} );
				
				workJobs( list );
			}
		}
	}

	
	public static void workJobs( final List<Job> jobs ) {
		if ( null==jobs ) return;
		if ( jobs.isEmpty() ) return;
		
		final LocalDateTime now = LocalDateTime.now();
		final JobType type = jobs.get( 0 ).getJobType();
		
		if ( ( JobType.TESLA_READ == type ) 
				|| ( JobType.TESLA_WRITE == type ) ) {
			
			final TeslaJob tj = new TeslaJob( false );
			for ( final Job job : jobs ) {
				job.setState( JobState.WORKING );
				tj.addJob( job );
			}
			
			final JsonObject jo = tj.request();

			if ( null!=jo ) {
				System.out.println( "Combined JsonObject "
									+ "from Tesla (size): " + jo.size() );
			} else {
				System.err.println( "Combined JsonObject from Tesla is null" );
			}
			
			if ( jobs.size() >= 3 ) {
				final StringBuilder strbuf = new StringBuilder();
				strbuf.append( "Tesla Combined JSON\n"
						+ now.toString() + "\n\n"
						+ "Jobs:\n" );
				for ( final Job job : jobs ) {
					strbuf.append( "\t" + job.getJobSeq() );
					strbuf.append( "\t" + job.getRequest() + "\n" );
				}
				strbuf.append( "\n\nCombined JSON:\n" );
				final String strPrettyCombined = JsonUtils.getPretty( jo );
				strbuf.append( strPrettyCombined );
				
				SendMessage.send( MessageType.EMAIL, 
						"Tesla Combined JSON", strbuf.toString() );
				
				CloudUtilities.saveJson( "TESLA_Combined.json", 
							strPrettyCombined, ContentType.APP_JSON, null );
				CloudUtilities.saveJson( "TESLA_Combined.txt", 
							strbuf.toString(), ContentType.TEXT_PLAIN, null );

				if ( STORE_TO_LOCAL_APP_ENGINE ) {
					final CommGAE comm = new CommGAE();
					try {
						comm.store( DocKey.TESLA_COMBINED, strPrettyCombined );
					} catch ( final Exception e ) {
						//TODO look into later..
						e.printStackTrace();
					}
				}
			}
			
			
			for ( final Job job : jobs ) {
				job.setState( JobState.COMPLETE );
			}

//			return jo;
		}
	}
	
	
	

	public static JsonObject doCheckTeslaState( final Object obj ) {
		System.out.println( "--- doCheckTeslaState(), "
				+ "time is " + LocalDateTime.now().toString() );
		
//		if ( 1==1 ) return null;
		
		if ( null==obj ) {

			
			try {
			
				final TeslaJob job = new TeslaJob();
				final JsonObject jo = job.request();
				
				if ( null!=jo ) {
					System.out.println( "Combined JsonObject "
										+ "from Tesla (size): " + jo.size() );
				} else {
					System.err.println( "Combined JsonObject from Tesla is null" );
				}
				return jo;
	
			} catch ( final Throwable t ) {
				System.err.println( 
							"Error during doCheckTeslaState(): " + t.toString() );
				t.printStackTrace();
				return null;
			}

			
			
		} else if ( obj instanceof Job ) {
			final Job job = (Job)obj;
			if ( ( JobType.TESLA_READ == job.getJobType() ) 
					|| ( JobType.TESLA_WRITE == job.getJobType() ) ) {
//				job.setState( JobState.WORKING );
				queueJob( job );
			}
		}
		return null;
		
	}
	
	
	
	public static String getEmailCommandHelp() {
		final StringBuilder strbuf = new StringBuilder();
		
		strbuf.append( "Available email commands:\n" );
		for ( final Command command : Command.values() ) {
			strbuf.append( "\t" + command.name() + "\n" );
		}
		
		return strbuf.toString();
	}
	
	public static void emailSendDeviceCaptureStills() {
		sendDeviceFiles( false, true, true );
	}

	public static void emailSendDeviceScreenshots() {
		sendDeviceFiles( true, false, true );
	}
	
	public static void updateDeviceThumbnails() {
		final ImageLookupOptions[] options = { 
						FileSession.ImageLookupOptions.ONLY_THUMB,
						FileSession.ImageLookupOptions.SINCE_PAST_HOUR };
		sendDeviceFiles( false, true, false, options );
		sendDeviceFiles( true, false, false, options );
	}

	public static void sendDeviceFiles(	
							final boolean bScreenshot,
							final boolean bCaptureStill,
							final boolean bSendEmail,
							final FileSession.ImageLookupOptions... options ) {
		final FileSessionManager fsm = FileSessionManager.getInstance();
		final Map<String, FileSession> map = fsm.getSessionMap();

		final StringBuilder strbuf = new StringBuilder();
//		final File[] attachments = new File[ map.size() ];
		final List<File> listFiles = new LinkedList<>();
		
		strbuf.append( "Device listing:\n" );
		
		final long lCutoff = 
				System.currentTimeMillis() - TimeUnit.DAYS.toMillis( 1 );
		
		final CommGAE comm = new CommGAE();

		for ( final Entry<String, FileSession> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final FileSession session = entry.getValue();

			boolean bCurrent = false;
			
			final EnumMap<DocMetadataKey, String> 
					mapMetadata = new EnumMap<>( DocMetadataKey.class );

			final String strIP = session.getIP();
			mapMetadata.put( DocMetadataKey.DEVICE_IP, strIP );
			mapMetadata.put( DocMetadataKey.DEVICE_MAC, strKey );
			
			
			if ( bScreenshot ) {
				final File[] arrScreenshots = session.getScreenshotImageFiles();
				for ( final File file : arrScreenshots ) {
					if ( null!=file && file.isFile() 
							&& file.getName().contains( "thumb" ) ) {
						if ( file.lastModified() > lCutoff ) {
							listFiles.add( file );
							bCurrent = true;
							
							if ( STORE_TO_LOCAL_APP_ENGINE) {
								comm.store( DocKey.DEVICE_SCREENSHOT, 
												strKey + "/" + file.getName(), 
												file, mapMetadata );
							}
							
							final String strGCSName = 
									"SCREENSHOT_" + strKey + "_" + file.getName();
							CloudUtilities.saveImage( strGCSName, file, 
									ContentType.IMAGE_PNG, mapMetadata );
						}
					}
				}
			}

			if ( bCaptureStill ) {
				
				final List<File> files = 
								session.getCaptureStillImageFiles( options );
				for ( final File file : files ) {
					if ( null!=file && file.isFile() ) {
						if ( file.lastModified() > lCutoff ) {
							listFiles.add( file );
							bCurrent = true;
							
							final String strDescription = 
									session.getDescriptionForImageSource( file );
							
							mapMetadata.put( DocMetadataKey.SENSOR_DESC, 
									null!=strDescription ? strDescription : "" );
							
							if ( STORE_TO_LOCAL_APP_ENGINE) {
								final String strCommName = 
											strKey + "/" + file.getName();
								comm.store( DocKey.DEVICE_STILL_CAPTURE, 
											strCommName, file, mapMetadata );
							}
							
							final String strGCSName = 
									"CAPTURE_" + strKey + "_" + file.getName();
							CloudUtilities.saveImage( strGCSName, file, 
									ContentType.IMAGE_JPEG, mapMetadata );
						}
					}
				}
				
			}

			if ( bCurrent ) {
				strbuf.append( "\t" + strKey + "\n" );
				strbuf.append( "\t\tDeviceInfo: " + session.getDeviceInfo() + "\n" );
				strbuf.append( "\t\tAllSystemInfo: " + session.getAllSystemInfo() + "\n" );
				strbuf.append( "\n" );
			}
		}
		
		if ( bSendEmail ) {
			SendMessage.send( MessageType.EMAIL, "Device details", 
						strbuf.toString(), 
						listFiles.toArray( new File[ listFiles.size() ] ) );
		}
	}
	
	
	public static void submitJob_TeslaRefresh3() {
		System.out.println( "--- Simple.submitJob_TeslaRefresh3()" );
		try {
			final Job.JobSet set = new Job.JobSet( 3 );
			Job.add( JobType.TESLA_READ, set, DataRequest.CHARGE_STATE.name() );
			Job.add( JobType.TESLA_READ, set, DataRequest.VEHICLE_STATE.name() );
			Job.add( JobType.TESLA_READ, set, DataRequest.CLIMATE_STATE.name() );
		} catch ( final Throwable t ) {
			System.err.println( "ERROR in Simple.submitJob_TeslaRefresh3()" );
			t.printStackTrace();
		}
	}
	
	
	public static boolean bCheckedHomeArrival = false;

	public static void resetHomeArrival() {
		System.out.println( "Resetting home arrival trigger." );
		bCheckedHomeArrival = false;
	}
	
	public static void doHomeArrival() {
		try {
			System.out.println( "Garage pedestrian door opened (home arrival trigger)." );

			if ( bCheckedHomeArrival ) return; // already home

			bCheckedHomeArrival = true;
			
			// Simple.doCheckTeslaState( null );
			Simple.submitJob_TeslaRefresh3();
			
		} catch ( final Throwable t ) {
			t.printStackTrace();
		}
	}
	
	
	public static void doHandleEmailEvent( final EmailEvent event ) {
		if ( null==event ) return;
		
		final Command command = event.getCommand();
		if ( null==command ) return;
		
		System.out.println( "--- Simple.doHandleEmailEvent() - "
							+ "Command: " + command.name() );
		
		switch ( command ) {
			case HELP: {
				SendMessage.send( MessageType.EMAIL, 
						"Email Command Help", getEmailCommandHelp() );
				break;
			}
			case TESLA_REFRESH: {
				submitJob_TeslaRefresh3();
				break;
			}
			case GET_SCREENSHOT: {
				emailSendDeviceScreenshots();
				break;
			}
			case GET_CAPTURE_STILLS: {
				emailSendDeviceCaptureStills();
				break;
			}
			default: {
				System.out.println( "Command not matched, no action performed." );
			}
		}
	}
	
	
	
	public static void main( final String[] args ) {

//		sendDeviceFiles( true, true, false );
		
		updateDeviceThumbnails();
		
//		final CommGAE comm = new CommGAE();
//
//		final File file = new File( "S:\\Sessions\\B8-27-EB-13-8B-C0\\screenshot.png" );
//		final String strId = "B8-27-EB-13-8B-C0";
//		
////		comm.store( file, strName, ContentType.IMAGE_PNG );
//		comm.store( DocKey.DEVICE_SCREENSHOT, strId, file, null  );

	}
	
	
}
