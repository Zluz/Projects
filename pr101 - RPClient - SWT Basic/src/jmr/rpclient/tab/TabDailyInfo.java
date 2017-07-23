package jmr.rpclient.tab;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import jmr.rpclient.RPiTouchscreen;
import jmr.rpclient.UI;

public class TabDailyInfo extends TabBase {

	@Override
	public String getName() {
		return "Daily Info";
	}
	

	final public static String DATE_FORMAT_SHORT = "hh:mm aa";
	final public static SimpleDateFormat FORMATTER_SHORT;
	
	final public static TimeZone 
//				TIMEZONE = TimeZone.getTimeZone( "UTC-05:00" );
//				TIMEZONE = TimeZone.getTimeZone( "EDT" );
				TIMEZONE = TimeZone.getTimeZone( "US/Eastern" );

//	final public static String DATE_FORMAT_LONG = "yyyy-MM-dd  E \nHH:mm:ss";
	final public static String DATE_FORMAT_LONG = 
						"yyyy-MM-dd   HH:mm:ss\n"
						+ "EEEE, MMMM d";
	final public static SimpleDateFormat FORMATTER_LONG;

	static {
		FORMATTER_SHORT = new SimpleDateFormat( DATE_FORMAT_SHORT );
		FORMATTER_LONG = new SimpleDateFormat( DATE_FORMAT_LONG );
		FORMATTER_SHORT.setTimeZone( TIMEZONE );
		FORMATTER_LONG.setTimeZone( TIMEZONE );
	}

	
	private Label lblTime;
	private Label lblDate;
	
	
	private void updateTime( final Date now ) {
		final String strTime = FORMATTER_SHORT.format( now );
		lblTime.setText( strTime );
	}
	
	
	private void updateDate( final Date now ) {
		final String strDate = FORMATTER_LONG.format( now );
		lblDate.setText( strDate );
	}
	
	
	
	public Composite buildUI( final Composite parent ) {
	    final Composite comp = new Composite( parent, SWT.NONE );
	    comp.setLayout( new FillLayout( SWT.VERTICAL ) );
	    final Display display = parent.getDisplay();

	    comp.setBackground( UI.COLOR_BLACK );
	    
	    final FontData fdHuge = display.getSystemFont().getFontData()[0];
	    fdHuge.setHeight( 120 );
		final Font fontHuge = new Font( display, fdHuge );
	    final FontData fdLarge = display.getSystemFont().getFontData()[0];
	    fdLarge.setHeight( 50 );
		final Font fontLarge = new Font( display, fdLarge );
		
	    
	    lblTime = new Label( comp, SWT.CENTER );
		lblTime.setBackground( UI.COLOR_BLACK );
		lblTime.setForeground( UI.COLOR_WHITE );
	    lblTime.setText( "00:00" );
	    lblTime.setFont( fontHuge );
	    
	    lblDate = new Label( comp, SWT.CENTER );
	    lblDate.setBackground( UI.COLOR_BLACK );
	    lblDate.setForeground( UI.COLOR_WHITE );
	    lblDate.setText( new Date().toString() );
	    lblDate.setFont( fontLarge );

	    
	    final Thread threadTimer = new Thread() {
	    	@Override
	    	public void run() {
	    		try {
	    			do {
	    				Thread.sleep( 1000 );

	    			    final Date now = new Date();
	    			    
	    			    if ( display.isDisposed() ) return;
	    			    
	    			    display.asyncExec( new Runnable() {
	    			    	@Override
	    			    	public void run() {
	    	    			    updateTime( now );
	    	    			    updateDate( now );
	    			    	}
	    			    });
	    			    				
	    			} while ( true );
				} catch ( final InterruptedException e ) {
					// just quit
				}
	    	}
	    };
	    threadTimer.start();
	    
	    
	    return comp;
	}

	
	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {
		
	    final CTabItem tabDailyInfo = new CTabItem( tabs, SWT.NONE );
	    tabDailyInfo.setText( TAB_PAD_PREFIX + this.getName() + TAB_PAD_SUFFIX );
	    tabDailyInfo.setShowClose( false );
	    
	    final Composite compDailyInfo = this.buildUI( tabs );
	    tabDailyInfo.setControl( compDailyInfo );
	    if ( RPiTouchscreen.getInstance().isEnabled() ) {
	    	compDailyInfo.setCursor( UI.CURSOR_HIDE );
	    }
	    return tabDailyInfo;
	}
	
	
	
}
