package jmr.pr136;

public class AnimationIndex {

	private final Long lStart;
	private final Long lEnd;
	
	private final int iStart;
	private final int iEnd;
	
	private final Integer iStepInc;
	private Integer iStepPos;
	
//	private final Consumer<Long> consumerEnd;
	private final Runnable runEnd;
	
	private final Thread thread;
	
	// mode: linear, slow-fast, fast-slow, slow-fast-slow, etc
	
	public AnimationIndex( 	final Long lStart,
							final Long lEnd,
							final int iStart,
							final int iEnd,
							final Integer iStep,
//							final Consumer<Long> consumerEnd
							final Runnable runEnd
									) {
		if ( null != lEnd ) {
			this.lStart = lStart;
			this.lEnd = lEnd;
			
			thread = new Thread() {
				public void run() {
					try {
						final long lNow = System.currentTimeMillis();
						Thread.sleep( lEnd - lNow );
						AnimationIndex.this.runEnd.run();
					} catch ( final InterruptedException e ) {
						return;
					}
				};
			};
			thread.start();
		} else {
			this.lStart = null;
			this.lEnd = null;
			this.thread = null;
		}
		this.iStart = iStart;
		this.iEnd = iEnd;
		
		this.iStepInc = iStep;
		this.iStepPos = iStart;
		
//		this.consumerEnd = consumerEnd;
		this.runEnd = runEnd;
	}
	
	public AnimationIndex( 	final long lTimeNow,
							final long lDuration,
							final int iStart,
							final int iEnd,
							final Runnable runEnd ) {
		this( 	lTimeNow, 
				lTimeNow + lDuration, 
				iStart, 
				iEnd, 
				null,
				runEnd );
	}

	public AnimationIndex( 	final int iStart,
							final int iEnd,
							final int iStep,
							final Runnable runEnd ) {
		this( 	null, null, 
				iStart, 
				iEnd, 
				iStep,
				runEnd );
	}
	
	public int getIndex( final long lNow ) {

		if ( null == this.iStepInc ) {
			
	//		final long lNow = System.currentTimeMillis();
			if ( lNow >= this.lEnd ) {
				return this.iEnd;
			}
			
			final long lElapsed = lNow - this.lStart;
			final int iDistance = iEnd - iStart;
			
			final double dFraction = 
							( 0.0d + lElapsed ) / ( this.lEnd - this.lStart );
			final int iIndex = (int)( dFraction * iDistance ) + iStart;
			return iIndex;
			
		} else {
			
			this.iStepPos += this.iStepInc;
			final boolean bFinished;
			if ( iStepInc > 0 ) {
				bFinished = ( iStepPos > iEnd );
			} else {
				bFinished = ( iStepPos < iEnd );
			}
			if ( bFinished ) {
				iStepPos = iEnd;
				this.runEnd.run();
			}
			
			return iStepPos;
		}
	}
	
	
	
	
}
