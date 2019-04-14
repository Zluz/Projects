package jmr.pr127.functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import jmr.s2db.comm.ConnectionProvider;

public class WaterFilterHealth extends FunctionBase {

	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( WaterFilterHealth.class.getName() );

	private static final long INTERVAL_MAX = 180000L;

	private static final int DATA_POINTS_MIN = 4;

//	final public static int DATA_SPACE = 50;
	final public static int DATA_SPACE = DATA_POINTS_MIN + 20;

	final public static String SQL_QUERY = 
						"SELECT \n" + 
								"`time`, \n" + 
								"`value` \n" + 
						"FROM \n" + 
								"s2db.event e \n" + 
						"WHERE \n" + 
								"subject like 'L_POWER_WELL_PUMP' \n" + 
//								"AND ( time < 1554242052085 ) \n" + 
						"ORDER BY \n" + 
								"seq DESC \n" + 
						"LIMIT \n" + 
								( 2 * DATA_SPACE ) + ";";

	/*
	 * historical:
	 * 		date		time field		evaluated value
	 * 		20190402 - 1554208629457	2.7262333333333335
	 * 		20190403 - (filter reset)
	 * 		20190405 - 1554468341692	2.176816666666667
	 * 		20190408 - 1554468288499	2.2506444444444442
	 * 		20190410 - 1554898575529	2.270033333333333
	 */
	 
	
	
	
	
//	private long[] arrData = new long[ DATA_SPACE ];
//	private float[] arrData = new float[ DATA_SPACE ];
	private List<Double> listData = new LinkedList<>();
	
	
	
	@Override
	public boolean evaluate() {
					
		try ( final Connection conn = ConnectionProvider.get().getConnection();
			  final PreparedStatement stmt = conn.prepareStatement( SQL_QUERY );
			  final ResultSet rs = stmt.executeQuery() ) {
			
			Long lLastOn = null;
			int iIndex = 0;
			boolean bGoodData = true;
			
			while ( bGoodData && rs.next() ) {
				
				final long lTime = rs.getLong( "time" );
				final String strValue = rs.getString( "value" );
				
				if ( listData.isEmpty() ) {
					System.out.println( "time field: " + lTime );
				}
				
				final boolean bPumpOn;
				if ( "true".equals( strValue ) ) {
					bPumpOn = true;
				} else if ( "false".equals( strValue ) ) {
					bPumpOn = false;
				} else {
					throw new IllegalStateException( 
									"Invalid 'value' in ResultSet." );
				}
				
				if ( bPumpOn ) {
					lLastOn = lTime;
				} else {
					if ( null==lLastOn ) {
						if ( 0==iIndex ) {
							super.listMessages.add( 
									"First event is a pump last-off. "
									+ "This can/should be evaluted on a "
									+ "pump last-on event." );
						} else {
							throw new IllegalStateException( 
										"Missing pump last-on event." );
						}
					} else {
						final long lTimeInterval = lLastOn.longValue() - lTime;
						
						if ( lTimeInterval < INTERVAL_MAX ) {
							
							
//							arrData[ iIndex ] = lTimeInterval;

							final double fMinutes = 
											(double)lTimeInterval / 1000 / 60;
//							arrData[ iIndex ] = fMinutes;
							
							listData.add( fMinutes );
							
							iIndex++;
						} else {
							bGoodData = false;
						}
					}
				}
			}
			
			final int iCount = listData.size();
			
			System.out.println( "Collected " + iCount + " data points." );
			
			if ( iIndex < DATA_POINTS_MIN ) {
				super.listMessages.add( "Too few consecutive data points "
								+ "(only collected " + iCount + ", "
								+ "need " + DATA_POINTS_MIN + ")." );
				return false;
			}
			
			// data collected. normalize. 
			
			Collections.sort( listData );
			
			// drop the first 2 (water warm-up)
			listData.remove( 0 );
			listData.remove( 0 );
			// drop the first and last (outliers)
			listData.remove( 0 );
			listData.remove( listData.size() - 1 );
			
			// good clean data.

			System.out.println( "Data points:" );
			for ( int i = 0; i < listData.size(); i++ ) {
				System.out.println( "\t" + listData.get( i ) );
			}
			
			final double[] arrData = 
//							listData.toArray( new double[ listData.size() ] );
//							listData.toArray();
							new double[ listData.size() ];
			for ( int i=0; i<listData.size(); i++ ) {
				arrData[ i ] = listData.get( i );
			}
			
			final DescriptiveStatistics ds = new DescriptiveStatistics( arrData );
			
			final double dMean = ds.getMean();
			
			System.out.println( "Mean: " + dMean );
			
			
			return true;
		} catch ( final SQLException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	
	public static void main( final String[] args ) {
		final WaterFilterHealth wfh = new WaterFilterHealth();
		wfh.evaluate();
		
		ConnectionProvider.get().close();
	}
	
	
}
