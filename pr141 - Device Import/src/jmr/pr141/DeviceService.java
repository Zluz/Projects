package jmr.pr141;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jmr.pr141.device.Device;
import jmr.pr141.device.TSVRecord;

public class DeviceService {

	enum MergeStrategy {
		// collect all records, maps will be merged, favor duplicates
		COLLECT_ALL,
		// merge all records, newer map entries will replace older 
		LATEST_MERGE,
		// only take the latest record
		LATEST_ONLY,
	}
	
	private MergeStrategy strategy;
	private ScanFile scanner = null;
	
	final Map<Long,List<Long>> mapTacPos = new TreeMap<>();
	
	
	public void setStrategy( final MergeStrategy strategy ) {
		this.strategy = strategy;
	}
	
	public List<Device> getAllDeviceRecords( final long lTAC ) {
		final List<Device> list = new LinkedList<>();
		
		final List<String> listRecords = scanner.getDeviceLines( lTAC );
		for ( final String strRecord : listRecords ) {
//			final Device device = Device.fromTSV( strRecord );
			final Device device = TSVRecord.fromTSV( strRecord );
			if ( null != device ) {
				list.add( device );
			}
		}
		
		return list;
	}
	
	public void clear() {
		this.mapTacPos.clear();
	}
	
	public boolean load( final File file ) {
		System.out.println( "Loading: \"" + file.getAbsolutePath() + "\"" );
		final long lTimeStart = System.currentTimeMillis();
		try {
			this.scanner = new ScanFile( file );
			scanner.scan_002( 100000 );
			this.mapTacPos.putAll( scanner.getTacPosMap() );
			
			final long lTimeEnd = System.currentTimeMillis();
			final long lElapsed = lTimeEnd - lTimeStart;
			System.out.println( "Load completed in " + lElapsed + " ms." );
			
			return true;
		} catch ( final Exception e ) {
			return false;
		}
	}
	
	
	public static void main( final String[] args ) {

//		final String strFile = "/data/Development/CM/test.tar";
		final String strFile = "D:\\Tasks\\20210309 - COSMIC-417 - Devices\\"
							+ "catalog.tsv";
		final File file = new File( strFile );

		final DeviceService devices = new DeviceService();

		final int iIterations = 8;
		final long lTimeStart = System.currentTimeMillis();
		for ( int i = 0; i < iIterations; i++ ) {
			devices.clear();
			devices.load( file );
		}
		final long lTimeEnd = System.currentTimeMillis();
		final long lElapsed = lTimeEnd - lTimeStart;
		final double dAvgTime = (double)lElapsed / 1000.0 / iIterations;
		System.out.println( String.format( 
				"Average time elapsed: %.3f seconds.", dAvgTime ) );
		
		final long lTAC = 35888803; //Acer beTouch E400
		final List<Device> list = devices.getAllDeviceRecords( lTAC );
		
		for ( final Device device : list ) {
//			final String strNewTSV = device.toTSV();
			final TSVRecord tsv = new TSVRecord( device );
			final String strNewTSV = tsv.toTSV();
			final String strShort = 
					( strNewTSV.length() > 100 ) 
							? strNewTSV.substring( 0, 100 ) + "..."
							: strNewTSV;
			System.out.println( "Device record: " + strShort );
		}
	}
	
	
}
