package jmr.pr141;

import jmr.pr141.device.Device;

public abstract class DeviceReference {
	
	private final DeviceProvider provider;
	
	public DeviceReference( final DeviceProvider provider ) {
		this.provider = provider;
	}
	
	public abstract String getName();
	
	public DeviceProvider getProvider() {
		return this.provider;
	}
	
	public Device resolve() {
		final Device device = this.provider.getDevice( this );
		return device;
	}

}
