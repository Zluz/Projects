package jmr.rpclient.tiles;

import java.util.Map.Entry;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.util.NetUtil;
import jmr.util.hardware.rpi.CPUMonitor;
import jmr.util.hardware.rpi.DeviceExamine;

public class SystemInfoTile extends TileBase {

	
	private final CPUMonitor monitor = CPUMonitor.get();

	
	public SystemInfoTile() {
//		monitor.
	}
	
	@Override
	public void paint(	final GC gc, 
						final Image image ) {
		
//		String strText = "";
//		strText += "Process:\n    " + NetUtil.getProcessName();
//		strText += "\nIP:\n    " + NetUtil.getIPAddress();
//		strText += "\nMAC:\n    " + NetUtil.getMAC();
//		strText += "\n";
//
//		drawTextCentered( strText, 10 );
		
		final GCTextUtils text = new GCTextUtils( gc );
		text.setRect( gc.getClipping() );
		gc.setFont( Theme.get().getFont( 10 ) );
		
		text.println( "Process: " + NetUtil.getProcessName() );
//		text.println( "IP: " + NetUtil.getIPAddress() );
//		text.println( "MAC: " + NetUtil.getMAC() );
//		final String strTemp = String.format( "%.1f", monitor.getTemperature() );
//		text.println( "CPU temp: " + strTemp );
		text.addSpace( 4 );
		final JsonObject jo = monitor.updateData();
		if ( null!=jo ) {
			for ( final Entry<String, JsonElement> entry : jo.entrySet() ) {
				final String strKey = entry.getKey();
				final String strValue = entry.getValue().toString();
				text.println( strKey + ": " + strValue );
			}
		} else {
			text.println( "CPU Monitor data is null" );
		}
		text.addSpace( 8 );
		final DeviceExamine examine = DeviceExamine.get();
		text.println( DeviceExamine.Key.CPU_THROTTLE.strKey + ":  " 
					+ examine.getValue( DeviceExamine.Key.CPU_THROTTLE ) );
//		text.println( "DHT temp:  " 
//				+ examine.getValue( DeviceExamine.Key.SENSOR_TEMPERATURE_VALUE ) );
//		text.println( "DHT humid:  " 
//				+ examine.getValue( DeviceExamine.Key.SENSOR_HUMIDITY_VALUE ) );
		text.println( examine.getThrottleStatus() );
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
