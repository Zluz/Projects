package jmr.rpclient.swt;

import org.eclipse.swt.graphics.Rectangle;

import jmr.s2db.tables.Job;

public class S2Button {
	
	private final String strName;
	private final Rectangle rect;
	private final int iIndex;
	
	private ButtonState state;
	private Job job;

	
	public static enum ButtonState {
		READY,
		ACTIVATED,
		WORKING,
		DISABLED,
		;
	}
	
	public S2Button( 	final int iIndex,
						final String strName,
						final Rectangle rect ) {
		this.iIndex = iIndex;
		this.strName = strName;
		this.rect = rect;
	}

	public ButtonState getState() {
		if ( null!=job ) {
			switch ( job.getState() ) {
				case REQUEST: return ButtonState.WORKING;
				case WORKING: return ButtonState.WORKING;
				case COMPLETE: return ButtonState.READY;
			default:
				break;
			}
		}
		return state;
	}
	
	public void setState( final ButtonState state ) {
		this.state = state;
	}
	
	public Job getJob() {
		return job;
	}

	public void setJob( final Job job ) {
		this.job = job;
	}

	public String getName() {
		return strName;
	}

	public Rectangle getRect() {
		return rect;
	}

	public int getIndex() {
		return iIndex;
	}
	
	
}