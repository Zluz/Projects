package jmr.rpclient.tiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.google.gson.JsonObject;

import jmr.rpclient.screen.TextScreen;
import jmr.rpclient.swt.ColorCache;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.S2Button.ButtonState;
import jmr.rpclient.swt.Theme;
import jmr.rpclient.swt.Theme.Colors;
import jmr.rpclient.swt.UI;
import jmr.s2db.tables.Job;
import jmr.util.transform.JsonUtils;

public abstract class TileBase implements Tile {

//	private final List<Button> buttons = new LinkedList<>();
	private final Map<Integer,S2Button> buttons = new HashMap<>();
	
	
	protected GC gc = null;
	protected Rectangle rect = null;
	protected int iXC;
	protected int iYC;
	
	protected long iNowPaint;
	private boolean bQueueRemoveButtons = false;


	
	@Override
	public void paint( final TextScreen screen ) {}
	
	@Override
	public boolean pressKey( final char c ) {
		return false;
	}
	
	public void paint(	final Image imageBuffer, 
						final long lNowPaint ) {
		this.iNowPaint = lNowPaint;
		rect = imageBuffer.getBounds();
		gc = new GC( imageBuffer );
		gc.setBackground( Theme.get().getColor( Colors.BACKGROUND ) );
		gc.setForeground( Theme.get().getColor( Colors.TEXT ) );
		gc.fillRectangle( rect );

		this.iXC = rect.x + rect.width / 2;
		this.iYC = rect.y + rect.height / 2;

//		buttons.clear();
		if ( bQueueRemoveButtons ) {
			this.buttons.clear();
			bQueueRemoveButtons = false;
		}
		
		paint( gc, imageBuffer );
		gc.dispose();
		gc = null;
		rect = null;
	}
	
	@Override
	public abstract void paint( final GC gc, Image imageBuffer );

	
	@Override
	public boolean clickCanvas( final Point point ) {
		return false;
	}
	
	
	@Override
	public boolean clickButtons( final Point point ) {

		boolean bButton = false;
		for ( final S2Button button : buttons.values() ) {
			final Rectangle r = button.getRect();
			if ( ( point.x > r.x ) && ( point.x < r.x + r.width )
					&& ( point.y > r.y ) && ( point.y < r.y + r.height ) ) {
				
				System.out.println( "Button pressed: " + button.getName() );
				
				button.setState( ButtonState.ACTIVATED );
				activateButton( button );
				
				bButton = true;
			}
		}
		
		return bButton;	
	}
	
	
	protected abstract void activateButton( final S2Button button );

	
	public S2Button getButton( final int iIndex ) {
		return buttons.get( iIndex );
	}

	
	
	private final static Map<String,Integer> SIZE_CACHE = new HashMap<>();
	
	private String getCacheKey( final String strText,
								final int iGCWidth,
								final int iMin,
								final int iMax ) {
		return ""+iGCWidth + ":" + iMin + "-" + iMax + ":" + strText;
	}
	
	public void setButtonState(	final int iIndex,
								final ButtonState state ) {
		if ( null==state ) return;
		final S2Button button = buttons.get( iIndex );
		if ( null==button ) return;
		button.setState( state );
	}
	
	
	
	
	//TODO optimize
	protected void drawTextCentered(	final String strText,
										final int iY,
										final int iMin,
										final int iMax ) {
		int iSize;
		final String strKey = getCacheKey( strText, rect.width, iMin, iMax );
		if ( !SIZE_CACHE.containsKey( strKey ) ) {
		
			iSize = iMax + 1;
			final int iWidth = rect.width - 6;
			Point ptTest;
			do {
				iSize = iSize - 1;
				gc.setFont( Theme.get().getFont( iSize ) );
				ptTest = gc.textExtent( strText );
			} while ( ( iWidth < ptTest.x ) && ( iSize >= iMin ) );
			iSize = Math.max( iSize, iMin );

			if ( SIZE_CACHE.size() > 20 ) SIZE_CACHE.clear();
			
			SIZE_CACHE.put( strKey, iSize );
			System.out.println( "SIZE_CACHE size: " + SIZE_CACHE.size() );
		} else {
			iSize = SIZE_CACHE.get( strKey );
		}
		
		
		gc.setFont( Theme.get().getFont( iSize ) );
		final Point ptExtent = gc.textExtent( strText );
		
		final int iX = iXC - ( ptExtent.x / 2 );
		gc.drawText( strText, iX, iY );
	}

	protected void drawTextCentered(	final String strText,
										final int iY ) {
		drawTextCentered( strText, iY, 8, 50 );
	}

	
	
	final static List<Long> listScheduledJobRefreshes = new ArrayList<>( 10 );
	
	private void scheduleButtonJobCheck( final S2Button button ) {
		if ( null==button ) return;
		if ( null==button.getJob() ) return;
		
		final Job job = button.getJob();
		
		if ( job.getTimeSinceRefresh() > 500 ) {
			final long lSeq = job.getJobSeq();
			synchronized ( listScheduledJobRefreshes ) {
				if ( listScheduledJobRefreshes.contains( lSeq ) ) {
					return;
				}
				listScheduledJobRefreshes.add( lSeq );
			}
			final Thread thread = new Thread( "Job " + lSeq + " refresh " ) {
				@Override
				public void run() {
					job.refresh();
					listScheduledJobRefreshes.remove( lSeq );
				}
			};
			thread.start();
		}
	}
	
	
	protected void removeAllButtons() {
//		this.buttons.clear();
		this.bQueueRemoveButtons = true;
	}
	
	protected S2Button addButton(	final GC gc,
									final int iIndex,
									final int iX, final int iY,
									final int iW, final int iH,
									final String strText ) {
		if ( null==gc || gc.isDisposed() ) return null;
		if ( null==strText ) return null;

		final S2Button button;
		final int iBrightBase;
		final Color colorText;
		
		if ( this.buttons.containsKey( iIndex ) ) {
			button = this.buttons.get( iIndex );
			switch ( button.getState() ) {
				case ACTIVATED: {
					iBrightBase = 60;
					colorText = UI.COLOR_RED;
					scheduleButtonJobCheck( button );
					break;
				}
				case READY: {
					iBrightBase = 100;
					colorText = UI.COLOR_BLACK;
					break;
				}
				case DISABLED: {
					iBrightBase = 80;
					colorText = UI.COLOR_GRAY;
					break;
				}
				case WORKING: {
					iBrightBase = 60;
					colorText = UI.COLOR_GREEN;
					scheduleButtonJobCheck( button );
					break;
				}
				default: {
					iBrightBase = 100;
					colorText = UI.COLOR_BLACK;
					break;
				}
			}
		} else {
			final Rectangle rect = new Rectangle( iX, iY, iW, iH );
			button = new S2Button( iIndex, strText, rect );
			button.setState( ButtonState.READY );
			this.buttons.put( iIndex, button );
			iBrightBase = 200;
			colorText = UI.COLOR_BLACK;
		}
//		final Rectangle rect = new Rectangle( iX, iY, iW, iH );
//		button.rect = rect;

		final Job job = ( null!=button ) ? button.getJob() : null;
		final String strData = ( null!=job ) ? job.getResult() : null;
		final String strCaption;
		if ( null!=strData && !strData.isEmpty() ) {
			
			if ( !job.getState().isActive() ) {
				button.setJob( null );
				buttons.remove( iIndex );
			}
			
			final JsonObject jo = JsonUtils.getJsonObjectFor( strData );
			if ( jo.has( "caption" ) ) {
				strCaption = "" + jo.get( "caption" ).getAsString();
			} else {
				strCaption = "";
			}
		} else {
			strCaption = "";
		}
		final boolean bCaption = !strCaption.isEmpty();
		
		
		gc.setAdvanced( true );
		gc.setAntialias( SWT.ON );
		
//		gc.setForeground( UI.COLOR_GRAY );
		gc.setForeground( ColorCache.getGray( iBrightBase + 100 ) );
		gc.drawRoundRectangle( iX-1, iY-2, iW, iH, 20, 20 );
		gc.setForeground( ColorCache.getGray( iBrightBase + 40 ) );
		gc.drawRoundRectangle( iX-1, iY-1, iW, iH, 20, 20 );

//		gc.drawRoundRectangle( iX, iY, iW, iH, 20, 20 );
		gc.setBackground( ColorCache.getGray( iBrightBase + 10 ) );
//		gc.setBackground( Theme.get().getColor( Colors.TEXT_LIGHT ) );
		gc.fillRoundRectangle( iX, iY, iW, iH, 20, 20 );

		gc.setBackground( ColorCache.getGray( iBrightBase + 15 ) );
		gc.fillRoundRectangle( iX+3, iY+0, iW-6, iH-10, 20, 20 );
		gc.setBackground( ColorCache.getGray( iBrightBase + 20 ) );
		gc.fillRoundRectangle( iX+6, iY+1, iW-12, iH-20, 20, 20 );
		gc.setBackground( ColorCache.getGray( iBrightBase + 25 ) );
		gc.fillRoundRectangle( iX+9, iY+4, iW-16, iH-25, 20, 20 );

		final int iTextYNext;
		{
			gc.setFont( Theme.get().getBoldFont( 11 ) );
	
			final Point ptSize = gc.textExtent( strText );
			
			final int iTextX = iX + (int)((float)iW/2 - (float)ptSize.x/2);
			final int iTextY = iY + (int)((float)iH/2 - (float)ptSize.y/2) 
												- ( bCaption ? 4 : 2 );
			iTextYNext = iY + (int)((float)iH/2 + (float)ptSize.y/2) - 2; 
			
			gc.setForeground( ColorCache.getGray( iBrightBase + 50 ) );
			gc.drawText( strText, iTextX+1, iTextY+1, true );
			gc.setForeground( colorText );
			gc.drawText( strText, iTextX, iTextY, true );
		}

		if ( bCaption ) {
			gc.setFont( Theme.get().getBoldFont( 9 ) );
	
			final Point ptSize = gc.textExtent( strCaption );
			
			final int iTextX = iX + (int)((float)iW/2 - (float)ptSize.x/2);
//			final int iTextY = iY + (int)((float)iH/2 - (float)ptSize.y/2);
			final int iTextY = iTextYNext;
			
			gc.setForeground( UI.COLOR_GRAY );
			gc.drawText( strCaption, iTextX, iTextY, true );
		}

		
		
//		gc.setForeground( UI.COLOR_WHITE );
//		gc.drawRoundRectangle( iX, iY, iW, iH, 20, 20 );
//
////		final Button button = new Button();
////		button.rect = new Rectangle( iX, iY, iW, iH );
//		final Rectangle rect = new Rectangle( iX, iY, iW, iH );
////		button.iIndex = iIndex;
//		
//		if ( this.buttons.containsKey( iIndex ) ) {
//			this.buttons.get( iIndex ).rect = rect;
//		} else {
//			final Button button = new Button();
//			button.iIndex = iIndex;
//			button.rect = rect;
//			button.state = ButtonState.READY;
//			this.buttons.put( iIndex, button );
//		}
//		this.buttons.add( button );
//		this.buttons.put( iIndex, button );
		
		return button;
	}



}
