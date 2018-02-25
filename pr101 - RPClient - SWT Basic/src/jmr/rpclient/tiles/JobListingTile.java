package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.Client;
import jmr.s2db.job.JobManager;
import jmr.s2db.job.JobType;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;
import jmr.util.transform.DateFormatting;

public class JobListingTile extends TileBase {


	final static List<Job> listing = new LinkedList<>();


	private Thread threadUpdater;
	private final String strName;

	public JobListingTile(  final Map<String, String> mapOptions  ) {
		this.strName = mapOptions.get( "remote" );
		
		threadUpdater = new Thread( "NetworkList Updater" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( TimeUnit.SECONDS.toMillis( 1 ) );
		
					for (;;) {
						synchronized ( listing ) {
							try {
								updateListing();
							} catch ( final Exception e ) {
								// ignore.. 
								// JDBC connection may have been dropped..
							}
						}
		
						Thread.sleep( TimeUnit.SECONDS.toMillis( 10 ) );
					}
				} catch ( final InterruptedException e ) {
					// just quit
				}
			}
		};
//		threadUpdater.start();
	}
	


	private void updateListing() {

		final JobManager manager = Client.get().getJobManager();
		
		final List<Job> listingActive = manager.getJobListing( 
//				"( job.request LIKE \"%\" )" );
				"( job.state = \"R\" )", 100 );

		final List<Job> listingCompleted = manager.getJobListing( 
//				"( job.request LIKE \"%\" )" );
				"( ( job.state = \"C\" ) OR ( job.state = \"F\" ) "
				+ "OR ( job.state = \"W\" ) )", 8 );

		synchronized (listing) {
			listing.clear();
			listing.addAll( listingActive );
			listing.addAll( listingCompleted );
		}
		
		doWorkJobs( listingActive );
	}

	
	private void runRemoteExecute( final Job job ) {
		final Map<String,String> map = job.getJobDetails();
		
		if ( null!=this.strName 
				&& this.strName.equals( map.get( "remote" ) ) ) {

			job.setState( JobState.WORKING );

			final String strCommand = map.get( "command" );
			
			System.out.println( "Running command: " + strCommand );
			
			try {
				final Process process = 
								Runtime.getRuntime().exec( strCommand );
				process.waitFor();
				
				String strResult = "Exit value = " + process.exitValue();
				
				job.setState( JobState.COMPLETE, strResult );
			} catch ( final Exception e ) {
				job.setState( JobState.FAILURE, e.toString() );
			}
			
		}
		
	}
	
	private void doWorkJobs( final List<Job> jobs ) {
		if ( jobs.isEmpty() ) return;
		
		final Thread threadWorkJobs = new Thread( "Work Jobs" ) {
			@Override
			public void run() {
				for ( final Job job : jobs ) {
					final JobType type = job.getJobType();

					// execute job?
					if ( JobType.REMOTE_EXECUTE.equals( type ) ) {
						runRemoteExecute( job );
					}
				}
			}
		};
		threadWorkJobs.start();
	}
	
	




	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
		final long lNow = System.currentTimeMillis();
		
//		String strText = "";
//		for ( final String strSession : map.keySet() ) {
//			strText += strSession + "\n";
//		}
		
		int iY = 2;

		final int iX_RequestText;
//		final int iX_MAC;
		final int iX_RequestTime;
		final int iX_RequestResult;
		final char iX_State;
		
//		if ( 450 == rect.width ) {
//			iX_MAC = 10;	iX_Exec = 10;	iX_Desc = 290;
//		} else {
			iX_State = 5;	
			iX_RequestTime = 20;	
//			iX_MAC = 10;	
			iX_RequestText = 80;
			iX_RequestResult = 450;
//		}
			
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		gc.setFont( Theme.get().getFont( 8 ) );
		if ( null!=strName && !strName.isEmpty() ) {
			gc.drawText( "Local remote name: " + this.strName, 15, 4 );
			iY = iY + 18;
		}
		
		synchronized ( listing ) {
			for ( final Job job : listing ) {
//			for ( final Map<String, String> map : map2.values() ) {

				final JobState state = job.getState();

				final Color color;
				if ( JobState.REQUEST.equals( state ) ) {
					color = Theme.get().getColor( Colors.TEXT_BOLD );
				} else {
					color = Theme.get().getColor( Colors.TEXT_LIGHT );
				}
				gc.setForeground( color );
				
				final long lRequestTime = job.getRequestTime();
				final long lElapsed = lNow - lRequestTime;
//				final String strElapsed = String.format( "%.2f s", lElapsed );
				final String strElapsed = DateFormatting.getSmallTime( lElapsed );
				gc.setFont( Theme.get().getFont( 8 ) );
				gc.drawText( ""+state.getChar(), iX_State, iY );
				gc.drawText( strElapsed, iX_RequestTime, iY );

				final String strRequest = job.getRequest();
				gc.drawText( strRequest, iX_RequestText, iY );
				
				if ( rect.width > 400 ) {
					final String strResult = job.getResult();
					final String strPrintable = null!=strResult ? strResult : "-";
					gc.drawText( "   " + strPrintable, iX_RequestResult, iY );
				}
				
	//			strText += strIPFit + "   " + strExecFit + "   " + strName + "\n";
				iY += 18;
			}
		}

//		drawTextCentered( strText, 10 );
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
