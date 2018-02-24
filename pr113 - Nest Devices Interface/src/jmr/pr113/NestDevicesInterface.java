package jmr.pr113;

import java.util.Date;
import java.util.Set;

import com.bwssystems.nest.controller.Home;
import com.bwssystems.nest.controller.Nest;
import com.bwssystems.nest.controller.NestSession;
import com.bwssystems.nest.controller.Thermostat;
import com.bwssystems.nest.protocol.status.DeviceDetail;
import com.bwssystems.nest.protocol.status.SharedDetail;
import com.bwssystems.nest.protocol.status.WhereDetail;
import com.bwssystems.nest.protocol.status.WhereItem;

import jmr.util.SUProperty;
import jmr.util.SystemUtil;

public class NestDevicesInterface {

	public static void main( final String[] args ) throws InterruptedException {
		
		final String strUsername = 
				SystemUtil.getProperty( SUProperty.NEST_USERNAME ); 
		final String strPassword = 
				SystemUtil.getProperty( SUProperty.NEST_PASSWORD ); 

		
		

		System.err.println( "Testing Nest Devices Interface" );
		System.out.println( "Using username: " + strUsername );

		final NestSession session;

		try {
			session = new NestSession(strUsername, strPassword);
		} catch ( final Exception e ) {
			System.out.println("Caught Login Exception, exiting....");
			System.exit(1);
			return;
		}
		
		System.out.println( "Session information" );
		System.out.println( "\tNestSession.getUserid() = " + session.getUserid() );
														// 62657

		final Nest nest = new Nest(session);
		
		System.out.println( "Nest information" );
		System.out.println( "\tNest.toString() = " + nest.toString() );

		final Set<String> setHomeNames = nest.getHomeNames(); /* list of home structures  i.e. MyHouse */
		System.out.println( "Home names:" );
		String strHomeName = null;
		for ( final String name : setHomeNames ) {
			if ( null==strHomeName ) strHomeName = name;
			System.out.println( "\t" + name );
		}
		// home name:  d7d17cb0-ad43-11e1-a9b6-1231381b6879
		
		final Home home = nest.getHome( strHomeName );
		
		final WhereDetail wheres = nest.getWhere( strHomeName );
		System.out.println( "WhereDetail.getOriginalJSON() = " 
						+ wheres.getOriginalJSON() );
		
		System.out.println( "Home information:" );
		System.out.println( "\tHome.getName() = " + home.getName() );
//		System.out.println( "\tHome.getDetail() = " + home.getDetail() );
		
		System.out.println( "Where information:" );
		for ( final WhereItem where : wheres.getWheres() ) {
			System.out.println( "\tWhereDetail.getWhereId() = " + where.getWhereId() );
			System.out.println( "\tWhereDetail.getName() = " + where.getName() );
		}
		

//		aHome.setAway(Boolean.FALSE); /* either TRUE or FALSE */

		final Set<String> setDeviceNames = nest.getThermostatNames(); /* list of thermostats in all structure */
		System.out.println( "Device names:" );
		String strDevice = null;
		for ( final String name : setDeviceNames ) {
			if ( null==strDevice ) strDevice = name;
			System.out.println( "\t" + name );
		}
		// device name:  01AA02AB111205MR
		
		
		for ( int i=1; i<100; i++ ) {

			System.out.println( "Updating Thermostat" );
			final Thermostat thermostat = nest.getThermostat( strDevice );
			
			for ( int j=1; j<4; j++ ) {
				System.out.println( "Updating SharedDetail and DeviceDetail" );
				final SharedDetail shared = thermostat.getSharedDetail();

				final DeviceDetail detail = thermostat.getDeviceDetail();

				for ( int k=1; k<4; k++ ) {
					System.out.println( "\tTime now: " + new Date().toString() );
					System.out.println( "SharedDetail information" );
					final Double lTemp = shared.getCurrentTemperature();
					System.out.println( "\tgetCurrentTemperature() = " + lTemp + " C = " + (lTemp * (9/5) + 32) + " F" );
	//				System.out.println( "\tgetTargetTemperature() = " + shared.getTargetTemperature() );
	//				System.out.println( "\tgetHvacFanState() = " + shared.getHvacFanState() );
	//				System.out.println( "\tgetAutoAway() = " + shared.getAutoAway() );
					final Long lTimestamp = shared.get$timestamp();
					System.out.println( "\tget$timestamp() = " + lTimestamp + " = " + new Date( lTimestamp ).toString() );
	//				System.out.println( "\tgetAutoAwayLearning() = " + shared.getAutoAwayLearning() );
	//				System.out.println( "\tgetCompressorLockoutTimeout() = " + shared.getCompressorLockoutTimeout() );
					System.out.println( "\tJSON: " + shared.getOriginalJSON() );

					System.out.println( "DeviceDetail information" );
					System.out.println( "\tJSON: " + detail.getOriginalJSON() );

					final int iMinutes = 100000;
					Thread.sleep( 1000 * 60 * iMinutes );
				}
			}
		}
		
//		final DeviceDetail detail = thermostat.getDeviceDetail();
//		
//		System.out.println( "DeviceDetail information" );
//		System.out.println( "DeviceDetail.getCurrentHumidity() = " 
//								+ detail.getCurrentHumidity() );
//		System.out.println( "DeviceDetail.getTemperatureScale() = " 
//								+ detail.getTemperatureScale() );
		
//		Float targetTemp = new Float(25.2790765); /* always a float and in celsius */
//		thermo1.setTargetTemperature(targetTemp);
//
//		String targetType = "range"; /* heat, cool, range or off */
//		thermo1.setTargetType(targetType);
//
//		String fanmode = "auto"; /* on or auto */
//		thermo1.setFanMode(fanmode);
		
		
		

	}
	
}
