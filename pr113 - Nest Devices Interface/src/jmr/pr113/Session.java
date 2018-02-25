package jmr.pr113;

import java.util.Set;

import com.bwssystems.nest.controller.Nest;
import com.bwssystems.nest.controller.NestSession;
import com.bwssystems.nest.controller.Thermostat;
import com.bwssystems.nest.protocol.error.LoginException;
import com.bwssystems.nest.protocol.status.DeviceDetail;
import com.bwssystems.nest.protocol.status.SharedDetail;

public class Session {

	
	
	
	
	private final char[] cUsername;
	private final char[] cPassword;
	
	NestSession session;
	
	public Session(	final char[] cUsername,
						final char[] cPassword ) {
		this.cUsername = cUsername;
		this.cPassword = cPassword;

		this.initSession();
	}
	
	private void initSession() {
		if ( null!=this.session ) {
			this.session.close();
		}
		try {
			this.session = new NestSession( cUsername, cPassword );
		} catch ( final LoginException e ) {
			System.err.println( "Exception encountered during Nest login." );
			e.printStackTrace();
			this.session = null;
		}
	}
	
	public FullStatus getStatus() {
		final Nest nest = new Nest( session );

//		final Set<String> setHomeNames = nest.getHomeNames(); 
//		final String strHomeName = setHomeNames.iterator().next();
//		final Home home = nest.getHome( strHomeName );
		
		final Set<String> setDeviceNames = nest.getThermostatNames();
		final String strDeviceName = setDeviceNames.iterator().next();

		final Thermostat thermostat = nest.getThermostat( strDeviceName );
		
		final DeviceDetail device = thermostat.getDeviceDetail();
		final SharedDetail shared = thermostat.getSharedDetail();
		
		final FullStatus status = new FullStatus( device, shared );
		return status;
	}
	
	
}
