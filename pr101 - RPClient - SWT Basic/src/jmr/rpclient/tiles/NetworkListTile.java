package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.rpclient.tiles.Theme.Colors;
import jmr.s2db.tables.Page;
import jmr.s2db.tables.Path;
import jmr.util.transform.DateFormatting;

public class NetworkListTile extends TileBase {

//	final static Map<String,Long> map = new HashMap<>();

	final static Map<String,Map<String,String>> map2 = new HashMap<>();


	private Thread threadUpdater;

	public NetworkListTile() {
		threadUpdater = new Thread( "NetworkList Updater" ) {
			@Override
			public void run() {
				try {
					Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
		
					for (;;) {
						synchronized ( map2 ) {
							try {
								updateMap();
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
	


	private void updateMap() {

		final String strPath = "/Sessions/";

		final Map<String, Long> mapSessions = 
						new Path().getChildPages( strPath, true );
		
//		map.clear();
//		map.putAll( mapSessions );

		final Page page = new Page();
		
		map2.clear();
		for ( final Entry<String, Long> entry : mapSessions.entrySet() ) {

			final String strSession = entry.getKey();
			final long lPageSeq = entry.getValue();
			
			final Map<String, String> map = 
//						Client.get().loadPage( strPath );
						page.getMap( lPageSeq );
			
			map2.put( strSession, map );
		}
		
	}

	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
//		String strText = "";
//		for ( final String strSession : map.keySet() ) {
//			strText += strSession + "\n";
//		}
		
		int iY = 2;

		final int iX_IP;
		final int iX_Exec;
		final int iX_Desc;
		
		if ( 450 == rect.width ) {
			iX_IP = 170;	iX_Exec = 10;	iX_Desc = 290;
		} else {
			iX_IP = 10;		iX_Exec = 0;	iX_Desc = 130;
		}
		
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );

		synchronized ( map2 ) {
			for ( final Map<String, String> map : map2.values() ) {
				
				if ( iX_Exec>0 ) {
					final String strExec = map.get( "executable" );
					gc.setFont( Theme.get().getFont( 8 ) );
					gc.drawText( strExec, iX_Exec, iY + 4 );
				}
	
				final String strIP = map.get( "device.ip" );
				gc.setFont( Theme.get().getFont( 14 ) );
				gc.drawText( strIP, iX_IP, iY );

				final String strName = map.get( "device.name" );
				gc.setFont( Theme.get().getFont( 7 ) );
				gc.drawText( strName, iX_Desc, iY );
	
				final String strStarted = map.get( "session.start" );
				final String strAge = DateFormatting.getSmallTime( strStarted );
				gc.drawText( "Age: " + strAge, iX_Desc + 10, iY + 12  );
	
	//			strText += strIPFit + "   " + strExecFit + "   " + strName + "\n";
				iY += 30;
			}
		}

//		drawTextCentered( strText, 10 );
	}

	@Override
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
