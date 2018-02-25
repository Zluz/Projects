package jmr.rpclient.tiles;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.UI;
import jmr.s2db.Client;

public class NestTile extends TileBase {

	public static final String NODE_PATH = "/External/Ingest/Nest - Thermostat";

	final static Map<String,String> map = new HashMap<>();
	
	private static Thread threadUpdater;

	
	public NestTile() {
		if ( null==threadUpdater ) {
			threadUpdater = new Thread( "TeslaTile Updater" ) {
				@Override
				public void run() {
					try {
						Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
			
						for (;;) {
							try {
								updatePage();
							} catch ( final Exception e ) {
								// ignore.. 
								// JDBC connection may have been dropped..
							}
			
							Thread.sleep( TimeUnit.SECONDS.toMillis( 4 ) );
			//						Thread.sleep( TimeUnit.SECONDS.toMillis( 2 ) );
						}
					} catch ( final InterruptedException e ) {
						// just quit
					}
				}
			};
		}
	}


	private void updatePage() {

		final String strPath = NODE_PATH;
		
		final Map<String, String> map = 
					Client.get().loadPage( strPath );

		synchronized ( NestTile.map ) {
			NestTile.map.clear();
			NestTile.map.putAll( map );
		}
	}
	
	
	public String getTemperature(	final String strKey, 
									final boolean bUnit ) {
		String strTempF = "<?>";
		final String strTempC = map.get( strKey );
		if ( null!=strTempC && !strTempC.isEmpty() ) {
			final float fTempC = Float.parseFloat( strTempC );
			final float fTempF = (fTempC * 9f / 5f ) + 32f;
			strTempF = String.format( "%.1f", fTempF );
			if ( bUnit ) {
				strTempF = strTempF + " °F";
			}
		}
		return strTempF;
	}
	
	public String getTemperature( final String strKey ) {
		return getTemperature( strKey, true );
	}

	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
		if ( !threadUpdater.isAlive() ) {
			threadUpdater.start();
		}
		
		final Rectangle r = image.getBounds();
		
		gc.setBackground( UI.COLOR_BLACK );
		gc.fillRectangle( r.x, r.y, r.width, r.height );
		
		final GCTextUtils text = new GCTextUtils( gc );
		text.setRect( gc.getClipping() );
		
//		gc.setFont( Theme.get().getFont( 11 ) );
//		text.println( "              Limit - High:    " + getTemperature( "target_temperature_high" ) );
		gc.setFont( Theme.get().getFont( 18 ) );
		text.println( "Temperature:  " + getTemperature( "current_temperature" ) );
		gc.setFont( Theme.get().getFont( 11 ) );
//		text.println( "              Limit - Low:    " + getTemperature( "target_temperature_low" ) );
		text.println( "                    Range:   " 
					+ getTemperature( "target_temperature_low", false ) 
					+ " - " + getTemperature( "target_temperature_high" ) );
		gc.setFont( Theme.get().getFont( 16 ) );
		text.println( "        Humidity:  " + map.get( "current_humidity" ) + "%" );
		
		gc.setFont( Theme.get().getFont( 9 ) );
		text.addSpace( 6 );
		text.println( "target_temperature_type: " + map.get( "target_temperature_type" ) );
		text.println( "temperature_lock: " + map.get( "temperature_lock" ) );
		text.println( "hvac_fan_state: " + map.get( "hvac_fan_state" ) );
		text.println( "fan_current_speed: " + map.get( "fan_current_speed" ) );
		
		final GCTextUtils text2 = new GCTextUtils( gc );
		text2.setRect( new Rectangle( 164, 94, 150, 80 ) );

		text2.println( "auto_away: " + map.get( "auto_away" ) );
		text2.println( "auto_away_enable: " + map.get( "auto_away_enable" ) );
		
		
//		gc.drawLine( 0,0, r.width, r.height );
//		gc.drawLine( 0,r.height, r.width,0 );

	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
