package jmr.pr136;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

public abstract class GaugeBase {
	
	protected final Rectangle rect;
	protected final Monitor monitor;
	
	protected GaugeBase( final Monitor monitor,
						 final Rectangle rect ) {
		this.monitor = monitor;
		this.rect = rect;
	}
	
	public abstract void paint( final long lNow,
								final GC gc,
								final Image image );

}
