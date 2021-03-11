package jmr.pr141;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

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
//	private ScanFile scanner = null;
	final List<DeviceProvider> listProviders = new LinkedList<>();
	
//	final Map<Long,List<Long>> mapTacPos = new TreeMap<>();
	
	
	public void setStrategy( final MergeStrategy strategy ) {
		this.strategy = strategy;
	}
	
	public List<DeviceProvider> getAllDeviceProviders() {
		return listProviders; 
	}
	
	public long getTotalReferences() {
		long lCount = 0;
		for ( final DeviceProvider provider : listProviders ) {
			lCount += provider.getTotalReferences();
		}
		return lCount;
	}

	public List<Device> getAllDeviceRecords( final long lTAC ) {
		final List<Device> list = new LinkedList<>();
		
		for ( final DeviceProvider provider : listProviders ) {
//			final List<String> listRecords = scanner.getDeviceLines( lTAC );
			final List<DeviceReference> 
						listReferences = provider.findReferences( lTAC );
//			for ( final String strRecord : listRecords ) {
			for ( final DeviceReference reference : listReferences ) {
	//			final Device device = Device.fromTSV( strRecord );
//				final Device device = TSVRecord.fromTSV( strRecord );
				final Device device = provider.getDevice( reference );
				if ( null != device ) {
					list.add( device );
				}
			}
		}
		
		return list;
	}
	

	public List<DeviceReference> getAllDeviceReferences( final long lTAC ) {
		final List<DeviceReference> listResult = new LinkedList<>();
		
		for ( final DeviceProvider provider : listProviders ) {
			final List<DeviceReference> listProviderReferences = 
									provider.findReferences( lTAC );
			for ( final DeviceReference reference : listProviderReferences ) {
				listResult.add( reference );
			}
		}
		
		return listResult;
	}
	
	
	public void clear() {
//		this.mapTacPos.clear();
		this.listProviders.clear();
	}
	
	public boolean load( final File file,
						 final long lMaxCount ) {
		System.out.println( "Loading: \"" + file.getAbsolutePath() + "\"" );
		final long lTimeStart = System.currentTimeMillis();
		try {
			final ScanFile scanner = new ScanFile( file );
			this.listProviders.add( scanner );
//			scanner.scan_002( 100000 );
			scanner.scan( lMaxCount );
//			this.mapTacPos.putAll( scanner.getTacPosMap() );
			
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

		final int iIterations = 1;
		final long lTimeStart = System.currentTimeMillis();
		for ( int i = 0; i < iIterations; i++ ) {
			devices.clear();
			devices.load( file, 1000 );
		}
		final long lTimeEnd = System.currentTimeMillis();
		final long lElapsed = lTimeEnd - lTimeStart;
		final double dAvgTime = (double)lElapsed / 1000.0 / iIterations;
		System.out.println( String.format( 
				"Average time elapsed: %.3f seconds.", dAvgTime ) );
		
//		final long lTAC = 35888803; // TAC appears early: Acer beTouch E400
		final long lTAC = 35160003; // very repeated TAC: Sony Ericsson K770
		final List<Device> list = devices.getAllDeviceRecords( lTAC );
		
		for ( final Device device : list ) {
//			final String strNewTSV = device.toTSV();
			final TSVRecord tsv = new TSVRecord( device );
			final String strNewTSV = tsv.toTSV();
			final String strShort = 
					( strNewTSV.length() > 200 ) 
							? strNewTSV.substring( 0, 200 ) + "..."
							: strNewTSV;
			System.out.println( "Device record: " + strShort );
		}
	}
	
	
}
