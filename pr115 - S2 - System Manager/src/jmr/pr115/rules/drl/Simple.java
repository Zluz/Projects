package jmr.pr115.rules.drl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static jmr.pr102.Command.HVAC_START;
import static jmr.pr102.Command.HVAC_STOP;
import static jmr.pr102.DataRequest.CLIMATE_STATE;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

//import org.apache.http.entity.ContentType;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import jmr.pr102.DataRequest;
import jmr.pr115.actions.SendMessage;
import jmr.pr115.actions.SendMessage.MessageType;
import jmr.pr115.data.ReportTable;
import jmr.pr115.data.ReportTable.Format;
import jmr.pr115.schedules.run.NestJob;
import jmr.pr115.schedules.run.TeslaJob;
import jmr.pr120.Command;
import jmr.pr120.EmailEvent;
import jmr.pr122.CommGAE;
import jmr.pr122.DocKey;
import jmr.pr122.DocMetadataKey;
import jmr.pr123.storage.GCSFactory;
import jmr.pr123.storage.GCSFileWriter;
import jmr.pr128.reports.Report;
import jmr.s2.ingest.Import;
import jmr.s2db.Client;
import jmr.s2db.event.EventType;
import jmr.s2db.imprt.WebImport;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Event;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;
import jmr.s2fs.FileSession;
import jmr.s2fs.FileSession.ImageLookupOptions;
import jmr.s2fs.FileSessionManager;
import jmr.util.FileUtil;
import jmr.util.RunProcess;
import jmr.util.TimeUtil;
import jmr.util.hardware.rpi.pimoroni.Port;
import jmr.util.http.ContentType;
import jmr.util.report.TraceMap;
import jmr.util.transform.JsonUtils;

public class Simple implements RulesConstants {
	
	
	private final static Logger 
					LOGGER = Logger.getLogger(Simple.class.getName());
	
	static {
		TimeUtil.isHourOfDay(); // just to get the import
	}
	
	static NestJob nest = null;

	
	

	public static void doRefreshNest() {
		LOGGER.info( "--- doRefreshNest(), "
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
			LOGGER.severe(
						"Error during doRefreshNest(): " + t.toString() );
			t.printStackTrace();
		}
	}

	public static void doRefreshWeather() {
		LOGGER.info( ()-> "--- doRefreshWeather(), "
					+ "time is " + LocalDateTime.now().toString() );
		try {
			final Import source = Import.WEATHER_FORECAST__YAHOO;
			
			final String strURL = source.getURL();
			final String strTitle = source.getTitle();
	
			final WebImport wi = new WebImport( strTitle, strURL );
			final Long seq = wi.save();

			System.out.println( "Weather refreshed. Result: seq = " + seq );
				
			
		} catch ( final Exception e ) {
			LOGGER.warning( () -> "Failed to update weather, encountered " 
						+ e.toString() );
		} catch ( final Throwable t ) {
			LOGGER.severe( () ->  
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
				LOGGER.info( "Combined JsonObject "
									+ "from Tesla (size): " + jo.size() );
			} else {
				LOGGER.warning( "Combined JsonObject from Tesla is null" );
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
		LOGGER.info( "--- doCheckTeslaState(), "
				+ "time is " + LocalDateTime.now().toString() );
		
//		if ( 1==1 ) return null;
		
		if ( null==obj ) {

			
			try {
			
				final TeslaJob job = new TeslaJob();
				final JsonObject jo = job.request();
				
				if ( null!=jo ) {
					LOGGER.info( "Combined JsonObject "
										+ "from Tesla (size): " + jo.size() );
				} else {
					LOGGER.warning( "Combined JsonObject from Tesla is null" );
				}
				return jo;
	
			} catch ( final Throwable t ) {
				LOGGER.warning( 
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
		LOGGER.info( ()-> "About to send device files.." );
		sendDeviceFiles( false, true, false, options );

		final ImageLookupOptions[] optionsFull = { 
//						FileSession.ImageLookupOptions.ONLY_THUMB,
						FileSession.ImageLookupOptions.SINCE_PAST_HOUR };
		sendDeviceFiles( true, false, false, optionsFull );
		LOGGER.info( ()-> "About to send device files...Done." );
	}

	public static void sendDeviceFiles(	
							final boolean bScreenshot,
							final boolean bCaptureStill,
							final boolean bSendEmail,
							final FileSession.ImageLookupOptions... options ) {
		final FileSessionManager fsm = FileSessionManager.getInstance();
		final Map<String, FileSession> mapSessions = fsm.getSessionMap();

		final StringBuilder strbuf = new StringBuilder();
//		final File[] attachments = new File[ map.size() ];
		final List<File> listFiles = new LinkedList<>();
		
		strbuf.append( "Device listing:\n" );
		
		final long lCutoff = 
				System.currentTimeMillis() - TimeUnit.DAYS.toMillis( 1 );
		
		final CommGAE comm = new CommGAE();

		for ( final Entry<String, FileSession> entry : mapSessions.entrySet() ) {
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
//							&& FileSession.isThumbnail( file.getName() ) 
							) {
						if ( file.lastModified() > lCutoff ) {
							listFiles.add( file );
							bCurrent = true;
							
							final EnumMap<DocMetadataKey, String> 
									mapSS = DocMetadataKey.createMetadataMap( 
											mapMetadata, file );

							if ( STORE_TO_LOCAL_APP_ENGINE) {
								comm.store( DocKey.DEVICE_SCREENSHOT, 
												strKey + "/" + file.getName(), 
												file, mapSS );
							}
							
							final String strGCSName = 
									"SCREENSHOT_" + strKey + "_" + file.getName();
							CloudUtilities.saveImage( strGCSName, file, 
									ContentType.IMAGE_PNG, mapSS );
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
							
							final EnumMap<DocMetadataKey, String> 
									mapCap = DocMetadataKey.createMetadataMap( 
													mapMetadata, file );

							final String strDescription = 
									session.getDescriptionForImageSource( file );
							mapCap.put( DocMetadataKey.SENSOR_DESC, 
									null!=strDescription ? strDescription : "" );
							
							if ( STORE_TO_LOCAL_APP_ENGINE) {
								final String strCommName = 
											strKey + "/" + file.getName();
								comm.store( DocKey.DEVICE_STILL_CAPTURE, 
											strCommName, file, mapCap );
							}
							
							final String strGCSName = 
									"CAPTURE_" + strKey + "_" + file.getName();
							CloudUtilities.saveImage( strGCSName, file, 
									ContentType.IMAGE_JPEG, mapCap );
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
//			final Map<String,Object> map = new HashMap<>();
			final TraceMap map = new TraceMap();
			final Job.JobSet set = new Job.JobSet( 3 );
			map.put( "job-set.first", set.lFirstSeq );
			map.put( "job-set.count", 3 );
			Job.add( JobType.TESLA_READ, set, DataRequest.CHARGE_STATE.name(), map );
			Job.add( JobType.TESLA_READ, set, DataRequest.VEHICLE_STATE.name(), map );
			Job.add( JobType.TESLA_READ, set, DataRequest.CLIMATE_STATE.name(), map );
		} catch ( final Throwable t ) {
			LOGGER.severe( "ERROR in Simple.submitJob_TeslaRefresh3()" );
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
			LOGGER.info( "Garage pedestrian door opened (home arrival trigger)." );

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
		
		LOGGER.info( "--- Simple.doHandleEmailEvent() - "
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
				LOGGER.info( "Command not matched, no action performed." );
			}
		}
	}
	
	
	/**
	 * Wait for a Job to complete (monitor the complete time).
	 * @param job
	 * @param iTimeout time in seconds
	 * @throws InterruptedException
	 */
	public static boolean waitForJob( final Job job,
								   	  final int iTimeout ) 
										   throws InterruptedException {
		LOGGER.info( "Waiting for job: " + job.getRequest() );
		try {
			int i = iTimeout;
			while ( i>0 ) {
				Thread.sleep( 1000 );
				System.out.print( "." );
				job.refresh();
				final Long lCompleted = job.getCompleteTime();
				if ( null!=lCompleted ) {
					System.out.println( "Done." );
					return true;
				}
				i = i - 1;
			}
			System.out.println( "Timeout." );
		} catch ( final InterruptedException e ) {
			LOGGER.info( "Simple.waitForJob() interrupted." );
			e.printStackTrace();
			throw e;
		}
		return false;
		
	}
	
	
	public static Map<String,Long> COOLDOWN = new HashMap<>();
	
	public static boolean checkCooldown( final String strName,
										 final long lTime ) {
		final long lNow = System.currentTimeMillis();
		final boolean bTake; // reset cooldown?
		
		if ( COOLDOWN.containsKey( strName ) ) {
			final long lTimeDown = COOLDOWN.get( strName );
			bTake = ( lNow > lTimeDown );
		} else {
			bTake = true;
		}
		if ( bTake ) {
			COOLDOWN.put( strName, lNow + lTime );
			return true;
		} else {
			LOGGER.info( ()-> "Activity '" + strName + "' still in cooldown." );
			return false;
		}
	}
	
	
	private static void managePrepareTesla( final String strReason ) 
											throws InterruptedException {
		
		int i=0;
		final long lNow = System.currentTimeMillis();

		final Map<String, Object> map = new HashMap<>();
		map.put( "reason", strReason );
		map.put( "time-manage", lNow );
//		final Map<String, Object> mapData = new HashMap<>();
		final TraceMap mapData = new TraceMap();
		mapData.put( "reason", strReason );
//		mapData.put( "time-initiate", lNow );

		boolean bHVACStarted = false;
		do {
			final int iFinal = i;
			LOGGER.info( ()-> "do..while loop, i = " + iFinal );
			if ( i>6 ) {
				LOGGER.warning( "Failed to start Tesla HVAC." );
				return;
			} else {
				i++;
			}
			
			LOGGER.info( "POSTing HVAC_START.." );
			final Job jobActivate = Job.add( 
					JobType.TESLA_WRITE, null, HVAC_START.name(), mapData );

			if ( ! waitForJob( jobActivate, 20 ) ) {
				LOGGER.info( "Request to start Tesla HVAC timed-out.");
				return;
			}
			
			Thread.sleep( TimeUnit.SECONDS.toMillis( 30 ) );

			LOGGER.info( "Requesting CLIMATE_STATE.." );
			final Job jobCheck = Job.add( 
					JobType.TESLA_READ, null, CLIMATE_STATE.name(), mapData );
			if ( ! waitForJob( jobCheck, 10 ) ) {
				LOGGER.info( "Request to check Tesla HVAC timed-out.");
				return;
			}
			
			Thread.sleep( TimeUnit.SECONDS.toMillis( 4 ) );

			final String strPath = CLIMATE_STATE.getResponsePath();
			final Map<String,String> 
							mapPage = Client.get().loadPage( strPath );
			LOGGER.info( ()-> "Looking up page " + strPath + ", " 
							+ mapPage.keySet().size() + " elements." );
			final String strHVAC = mapPage.get( "is_climate_on" );
			LOGGER.info( ()-> "is_climate_on = " + strHVAC ); 
			bHVACStarted = "true".equalsIgnoreCase( strHVAC );

			map.put( "time-confirm", mapPage.get( "timestamp" ) );
			map.put( "inside_temp", mapPage.get( "inside_temp" ) );
			map.put( "driver_temp_setting", mapPage.get( "driver_temp_setting" ) );
			map.put( "usable_battery_level", mapPage.get( "usable_battery_level" ) );
			
		} while ( ! bHVACStarted );
		
		final Event event = Event.add( EventType.SYSTEM, "Tesla_Prepare", 
				strReason, null, map, lNow, null, null, null );
		
		LOGGER.info( "Tesla climate is on. "
						+ "Posted Event " + event.getEventSeq() );
	}
	
	
	private static List<Long> listRecentTriggers = new LinkedList<>();
	
	/**
	 * Prepare the Tesla for driving.
	 * <br><br>
	 * For now this just means turning on the HVAC.
	 * @param bPrepare
	 */
	public static void doPrepareTesla( final boolean bPrepare,
									   final String strReason ) {
		
		LOGGER.info( ()-> "--> doPrepareTesla(), bPrepare = " + bPrepare );


		final long lNow = System.currentTimeMillis();
		final long lOldest = lNow - TimeUnit.HOURS.toMillis( 1 );
		
//		final Map<String, Object> mapData = new HashMap<>();
		final TraceMap mapData = new TraceMap();
		mapData.put( "reason", strReason );
//		mapData.put( "time-initiate", lNow );

		while ( listRecentTriggers.size() > 0 
				&& listRecentTriggers.get( 0 ) < lOldest ) {
			listRecentTriggers.remove( 0 );
		}
		listRecentTriggers.add( listRecentTriggers.size(), lNow );
		
		if ( bPrepare ) {
			if ( listRecentTriggers.size() < 10 ) {
				LOGGER.info( ()-> 
						"Aborting PrepareTesla (too few recent events)." );
				return;
			}
		}
		
		if ( bPrepare ) {
			final boolean bAcquiredCooldown = 
							checkCooldown( "PrepareTesla", 
									TimeUnit.HOURS.toMillis( 4 ) );
			if ( !bAcquiredCooldown ) {
				LOGGER.info( ()-> 
							"Aborting PrepareTesla (still in cooldown)." );
				return;
			}

			final Thread threadPrepareTesla = new Thread() {
				@Override
				public void run() {
					try {
						managePrepareTesla( strReason );
					} catch ( final InterruptedException e ) {
						LOGGER.warning( "PrepareTesla interrupted." );
						e.printStackTrace();
					}
				}
			};
			threadPrepareTesla.start();
			
		} else {
			Job.add( JobType.TESLA_WRITE, null, HVAC_STOP.name(), mapData );
		}
	}
	
	
	public static void doGenerateReport( Report report ) {
		
		LOGGER.info( ()-> "Generating report: " + report.name() );
		
		final Instant time = Instant.ofEpochMilli( System.currentTimeMillis() );

		final String strSQL = report.getSQL();
		final String strName = report.getOutputFilename() + ".html";
		
		final ReportTable table = new ReportTable( strName, strSQL );
		final StringBuilder sb = table.generateReport( Format.HTML );
		final byte[] bytes = sb.toString().getBytes( UTF_8 );
		
//		final CommGAE comm = new CommGAE();
//		comm.store( DocKey.TABLE_REPORT, "Devices", null, bytes );
//		comm.store( DocKey.TEST, "Devices", null, bytes );
		try {
			final GCSFactory factory = CloudUtilities.getFactory();
			final GCSFileWriter writer = 
						factory.create( strName, ContentType.TEXT_HTML );
			writer.put( DocMetadataKey.FILE_DATE.name(), time.toString() );
			writer.upload( bytes );
		} catch ( final Exception e ) {
			e.printStackTrace();
		}
		
		System.out.println( report.getOutputFilename() + ": " 
							+ bytes.length + " bytes sent to GCS." );
	}

	
	//TODO remove this, should be unnecessary..
	public static void doUpdateDevices() {
//		Event event;
//		if ( EventType.event.getEventType()
//		if ( SystemEvent.HEARTBEAT_HOUR.name() != event.getSubject() ) {
//			doGenerateReport( Report.RECENT_EVENTS );
//		}
		doGenerateReport( Report.DEVICES );
	}

	
	public static void doControlParkingAssist( final Event e ) {

//		final Map<String, Object> mapData = new HashMap<>();
		final TraceMap mapData = new TraceMap();
		mapData.putAll( e.getDataAsMap() );
		mapData.put( "event-seq", e.getEventSeq() );

		final Thread thread = new Thread( "Momentary Parking Assist" ) {
			public void run() {
				try {

//					final long lTimeOn = System.currentTimeMillis();
//					mapData.put( "time-on", lTimeOn );
					mapData.addFrame( "time-on" );

//					if ( Boolean.FALSE.equals( bClosed ) ) {
						jmr.s2db.tables.Job.add( JobType.REMOTE_OUTPUT, null,
								new String[] {
								"remote", "GARAGE_LIGHTS",
								"port", Port.OUT_D_1.name(),
								"value", "true",
									}, mapData );
									
						Thread.sleep( TimeUnit.MINUTES.toMillis( 2 ) );
//						Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
//					}

//					final long lTimeOff = System.currentTimeMillis();
//					mapData.put( "time-off", lTimeOff );
					mapData.addFrame( "time-off" );

					jmr.s2db.tables.Job.add( JobType.REMOTE_OUTPUT, null,
							new String[] {
							"remote", "GARAGE_LIGHTS",
							"port", Port.OUT_D_1.name(),
							"value", "false",
						}, mapData );
					
				} catch ( final InterruptedException e ) {
					// just quit
				}
			};
		};
		thread.start();
	}
	
	
	public static void doControlGarageLight( final Event e ) {
		
//		final String strValue = e.getValue();
//		final Boolean bClosed = Boolean.valueOf( strValue );

		final long lNow = System.currentTimeMillis();
		final long lLightDuration = TimeUnit.MINUTES.toMillis( 2 );
		
//		final Map<String, Object> mapData = new HashMap<>();
		final TraceMap mapData = new TraceMap();
		mapData.putAll( e.getDataAsMap() );
		mapData.put( "time-react", lNow );
		mapData.put( "event-seq", e.getEventSeq() );

//		final boolean bAcquireCooldown = checkCooldown( 
//							"GarageFastLight", lLightDuration );
//		if ( ! bAcquireCooldown ) {
//			LOGGER.info( ()-> 
//						"Aborting garage fast lights (still in cooldown)" );
//			return;
//		}
		
		final Thread thread = new Thread( "Momentary Garage Light" ) {
			public void run() {
				try {

//					final long lTimeOn = System.currentTimeMillis();
//					mapData.put( "time-on", lTimeOn );
					mapData.addFrame( "time-on" );

//					if ( Boolean.FALSE.equals( bClosed ) ) {
						jmr.s2db.tables.Job.add( JobType.REMOTE_OUTPUT, null,
								new String[] {
								"remote", "GARAGE_LIGHTS",
								"port", Port.OUT_D_2.name(),
								"value", "true",
									}, mapData );
									
						Thread.sleep( lLightDuration );
//						Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
//					}

//					final long lTimeOff = System.currentTimeMillis();
//					mapData.put( "time-off", lTimeOff );
					mapData.addFrame( "time-off" );

					jmr.s2db.tables.Job.add( JobType.REMOTE_OUTPUT, null,
							new String[] {
							"remote", "GARAGE_LIGHTS",
							"port", Port.OUT_D_2.name(),
							"value", "false",
						}, mapData );
					
				} catch ( final InterruptedException e ) {
					// just quit
				}
			};
		};
		thread.start();
	}
	
	
	public static void performObjectDetection( final Event event ) {
		if ( null==event ) return;
//		if ( ! event.getValue().contains( "Driveway" ) ) return;
		
		final String strName = event.getValue();
		
		final Map<String, Object> map = event.getDataAsMap();
		final Object objFileChanged = map.get( "file-changed" );
		if ( null == objFileChanged ) {
			System.out.println( "Map does not contain 'file-changed'" );
			return;
		}
		
		String strFileChanged = objFileChanged.toString();
		strFileChanged = StringUtils.replace( 
				strFileChanged, "\\Share\\Sessions\\", "S:\\Sessions\\" );
		strFileChanged = StringUtils.replace( 
				strFileChanged, "/Share/Sessions/", "S:/Sessions/" );
		
		System.out.println( "Opening file (1): " + strFileChanged );
		LOGGER.info( "Opening file (2): " + strFileChanged );
		
		final File fileChanged = new File( strFileChanged );
		if ( ! fileChanged.exists() ) {
			LOGGER.warning(  
					"File-changed image missing: " + strFileChanged );
			return;
		}
		
		final Object objExpectedObjects = map.get( "expected-objects" );
		final String strExpectedObjects;
		if ( null!=objExpectedObjects ) {
			strExpectedObjects = objExpectedObjects.toString();
		} else {
			strExpectedObjects = "";
		}
		
//		final String strFilenameChanged = strFileChanged;
		
		System.out.println( "Performing image analysis.." );
		
//		final VisionGUI vision = new VisionGUI();
//		vision.setSourceImage( strFileChanged );
//		vision.analyze( 800, 450 );
//		System.out.println( "Performing image analysis...Done." );
//		
//		System.out.println( "Sending image analysis report via email.." );
//		final Image image = vision.getAnalysisImage();
//		String strReport = vision.getAnalysisReport();

//		final Image imageArr[] = { null };
//		final String strReportArr[] = { null };
		
//		final Device device;
//		final Display device = Display.getCurrent();
//		final Display device = Display.getDefault();
//		device.syncExec( new Runnable() {
//			@Override
//			public void run() {
//				final VisionServiceSWT vss = 
//						new VisionServiceSWT( device, strFilenameChanged );
//				vss.analyze();
//				imageArr[0] = vss.getAnalysisImage( 800, 450 );
//				strReportArr[0] = vss.getAnalysisReport();
//			}
//		});
		
		final String[] strCommand = {
				"java.exe",
				"-jar",
				"S:\\Development\\Export\\pr129_20190609_005.jar",
				strFileChanged
		};
		final RunProcess process = new RunProcess( strCommand );
		process.run();
		final String strAnalysisJsonFile = process.getOutputLine();
		final File fileAnalysisJson = new File( strAnalysisJsonFile );
		if ( ! fileAnalysisJson.exists() ) {
			LOGGER.warning( "Analysis JSON file not found: " 
						+ strAnalysisJsonFile );
			return;
		}
		
		final JsonObject jo;
		try {
			final String strJson = 
					FileUtils.readFileToString( fileAnalysisJson, UTF_8 );
			jo = JsonUtils.getJsonObjectFor( strJson );
		} catch ( final IOException e ) {
			LOGGER.warning( "Failed to read JSON file: " + strAnalysisJsonFile );
			return;
		} catch ( final JsonParseException e ) {
			LOGGER.warning( "Failed to parse JSON file: " + strAnalysisJsonFile );
			return;
		}

		final JsonObject joAnnotations = jo.getAsJsonObject( "annotations" );
		final JsonArray jaAObjects = joAnnotations.get( "objects" ).getAsJsonArray();
		final JsonArray jaAText = joAnnotations.get( "text" ).getAsJsonArray();
		final int iItemsOfInterest = jaAObjects.size() + jaAText.size();
		if ( 0 == iItemsOfInterest ) {
			LOGGER.info( "No items of interest in changed image. "
						+ "No email to be sent." );
			return;
		} else {
			LOGGER.info( "" + iItemsOfInterest + " item(s) of "
						+ "interest detected. Preparing to send email.." );
		}
		
		final List<String> listDetectedUnexpectedObjects = new LinkedList<>();
		final List<String> listDetectedExpectedObjects = new LinkedList<>();
		
		if ( ( ! StringUtils.isBlank( strExpectedObjects ) ) 
				&& ( ! strExpectedObjects.startsWith( "<" ) ) ) {
			final List<String> listExpectedObjects = new LinkedList<>();
			for ( final String str : strExpectedObjects.split( "," ) ) {
				final String strNorm = StringUtils.replace( str, ",", "" );
				listExpectedObjects.add( strNorm.trim() );
			}
			
			for ( final JsonElement je : jaAObjects ) {
				final String strObjectName = je.getAsJsonObject()
						.getAsJsonPrimitive( "name" ).getAsString();
				if ( listExpectedObjects.contains( strObjectName ) ) {
					listDetectedExpectedObjects.add( strObjectName );
				} else {
					listDetectedUnexpectedObjects.add( strObjectName );
				}
			}
		}
		
		if ( listDetectedUnexpectedObjects.isEmpty() && ( 0 == jaAText.size() ) ) {
			LOGGER.info( "All objects detected are expected, no text detected. "
								+ "No email to be sent." );
			return;
		}
		
		final JsonObject joAnalysis = jo.getAsJsonObject( "image-analysis" );
		final String strAnalysisImageFile = joAnalysis.get( "filename" ).getAsString();
		final File fileAnalysisImage = new File( strAnalysisImageFile );

		final JsonObject joReport = jo.getAsJsonObject( "report" );
		final String strReportFile = joReport.get( "filename" ).getAsString();
		final File fileReport = new File( strReportFile );
		final String strReport = FileUtil.readFromFile( fileReport );


//		final String strReport;
//		if ( null==strReportArr[0] ) {
//			strReport = "Report from VisionGUI was null.";
//			LOGGER.warning( strReport );
//		} else {
//			strReport = strReportArr[0];
//			System.out.println( "Analysis report: " 
//						+ StringUtils.abbreviate( strReport, 60 ) );
//		}

		System.out.println( "Sending image analysis report via email.." );

		final File[] fileAttached = 
				new File[] { fileAnalysisImage, fileAnalysisJson };
		
		
		final String strSubject = "Image Change, Analysis - " + strName;
		
		final StringBuilder sbBody = new StringBuilder();
		sbBody.append( "Change detected on camera: " + strName + "\n\n" );
		if ( jaAText.size() > 0 ) {
			sbBody.append( "Text found in image, see below.\n\n" );
		}
		if ( ! listDetectedUnexpectedObjects.isEmpty() ) {
			sbBody.append( "Objects of interest:\n" );
			for ( final String strObject : listDetectedUnexpectedObjects ) {
				sbBody.append( "\t" + strObject + "\n" );
			}
			sbBody.append( "\n\n" );
		}
		sbBody.append( "Event (seq " + event.getEventSeq() + ") details:\n" );
		sbBody.append( JsonUtils.reportMap( map ) + "\n\n" ); 
		sbBody.append( strReport );

		SendMessage.send( MessageType.EMAIL, 
					strSubject, sbBody.toString(), fileAttached );
		
		System.out.println( "Sending image analysis report via email...Done." );
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
