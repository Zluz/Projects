package jmr.rpclient.tiles;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.s2db.Client;
import jmr.s2db.job.JobManager;
import jmr.s2db.tables.Job;
import jmr.util.transform.DateFormatting;

public class JobListingTile extends TileBase {

//	final static Map<String,Long> map = new HashMap<>();

	final static List<Job> listing = new LinkedList<>();
	


	private Thread threadUpdater;

	public JobListingTile() {
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
		
		final List<Job> listingRefresh = manager.getJobListing( 
//				"( job.request LIKE \"%\" )" );
				"( job.state = \"R\" )" );

		synchronized (listing) {
			listing.clear();
			listing.addAll( listingRefresh );
		}
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
		
//		if ( 450 == rect.width ) {
//			iX_MAC = 10;	iX_Exec = 10;	iX_Desc = 290;
//		} else {
			iX_RequestTime = 10;	
//			iX_MAC = 10;	
			iX_RequestText = 60;
//		}
		
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

		synchronized ( listing ) {
			for ( final Job job : listing ) {
//			for ( final Map<String, String> map : map2.values() ) {
				
				final long lRequestTime = job.getRequestTime();
				final long lElapsed = lNow - lRequestTime;
//				final String strElapsed = String.format( "%.2f s", lElapsed );
				final String strElapsed = DateFormatting.getSmallTime( lElapsed );
				gc.setFont( Theme.get().getFont( 8 ) );
				gc.drawText( strElapsed, iX_RequestTime, iY );

				final String strRequest = job.getRequest();
				gc.drawText( strRequest, iX_RequestText, iY );
	//			strText += strIPFit + "   " + strExecFit + "   " + strName + "\n";
				iY += 18;
			}
		}

//		drawTextCentered( strText, 10 );
	}


	@Override
	public void click( final Point point ) {
		// TODO Auto-generated method stub
	}

}
