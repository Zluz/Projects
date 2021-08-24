package jmr.pr141;

import java.util.List;

import jmr.pr141.device.Device;

public interface DeviceProvider {
	
	Device getDevice( final DeviceReference ref );
	
	List<DeviceReference> findReferences( final long lTAC );
	
	String getName();
	
	public long getTotalReferences();
	public long getTotalUniqueTACs();

}
