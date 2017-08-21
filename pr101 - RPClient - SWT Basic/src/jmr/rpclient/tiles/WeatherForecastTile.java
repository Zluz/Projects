package jmr.rpclient.tiles;

import java.util.Map;

import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import jmr.data.WeatherSymbol;
import jmr.s2db.Client;

public class WeatherForecastTile extends TileBase {


	
	
	

	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		for ( int iDay = 0; iDay<9; iDay++ ) {
			
			final int iX = ( rect.width - 20 ) * iDay / 9 + 15;
			
			final String strPath = "/External/Ingest/Weather_Forecast_Yahoo"
					+ "/data/query/results/channel/item/forecast/0" + iDay;
			
			final Map<String, String> map = Client.get().loadPage( strPath );
			
			final String strRange = map.get("low") + "-" + map.get("high");
			
			gc.setFont( Theme.get().getFont( 18 ) );
			gc.drawText( map.get("day"), iX + 18, 0 );
			gc.drawText( strRange, iX + 10, 100 );
			
			final String strText = "  "+map.get("text");
			gc.setFont( Theme.get().getFont( 10 ) );
			gc.drawText( strText, iX, 82 );
			
			final WeatherSymbol symbol = WeatherSymbol.getSymbol( strText );
			final Image imageIcon = symbol.getIcon();
			if ( null!=imageIcon ) {
				gc.drawImage( imageIcon, iX + 5 , 22 );
			}
		}
		
	}

	@Override
	public MouseListener getMouseListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
