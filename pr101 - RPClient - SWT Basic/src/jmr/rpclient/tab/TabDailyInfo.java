package jmr.rpclient.tab;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import jmr.rpclient.RPiTouchscreen;
import jmr.rpclient.swt.UI;
import jmr.util.NetUtil;

public class TabDailyInfo extends TabBase {

	public CTabItem tab = null;
	
	@Override
	public CTabItem getTab() {
		return this.tab;
	}
	

	public TopSection getMenuItem() {
		return TopSection.DAILY_INFO;
	}

	final public static String DATE_FORMAT_SHORT = "hh:mm aa";
	final public static SimpleDateFormat FORMATTER_SHORT;
	
	final public static TimeZone 
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

	
	private Label lblTime, lblAMPM;
	private Label lblDate;
	
	
	private void updateTime( final Date now ) {
		final String strTime = FORMATTER_SHORT.format( now );
		lblTime.setText( strTime.split(" ")[0] );
		lblAMPM.setText( Text.DELIMITER + "  " + strTime.split(" ")[1] );
	}
	
	
	private void updateDate( final Date now ) {
		final String strDate = FORMATTER_LONG.format( now );
		lblDate.setText( strDate );
	}
	
	final static int SPLIT_LEFT = 5;
	final static int SPLIT_RIGHT = 2;
	final static int ALIGN_TOTAL = SPLIT_LEFT + SPLIT_RIGHT; 
	
	public Composite buildUI( final Composite parent ) {
	    final Composite comp = new Composite( parent, SWT.NONE );
//	    comp.setLayout( new FillLayout( SWT.VERTICAL ) );
	    comp.setLayout( new GridLayout( ALIGN_TOTAL, true ) );
	    final Display display = parent.getDisplay();

	    comp.setBackground( UI.COLOR_BLACK );
	    
	    final FontData fdHuge = display.getSystemFont().getFontData()[0];
	    fdHuge.setHeight( 140 );
		final Font fontHuge = new Font( display, fdHuge );
		
	    final FontData fdLarge = display.getSystemFont().getFontData()[0];
	    fdLarge.setHeight( 40 );
		final Font fontLarge = new Font( display, fdLarge );

	    final FontData fdMedium = display.getSystemFont().getFontData()[0];
	    fdMedium.setHeight( 27 );
		final Font fontMedium = new Font( display, fdMedium );

		final GridData gd1 = new GridData( SWT.FILL, SWT.FILL, true, true );
		gd1.heightHint = 80;
		gd1.horizontalSpan = ALIGN_TOTAL;
		final GridData gd2l = new GridData( SWT.FILL, SWT.FILL, true, true );
		gd2l.heightHint = 200;
		gd2l.horizontalSpan = SPLIT_LEFT;
		final GridData gd2r = new GridData( SWT.FILL, SWT.FILL, true, true );
		gd2r.heightHint = 200;
		gd2r.horizontalSpan = SPLIT_RIGHT;
		final GridData gd3 = new GridData( SWT.FILL, SWT.FILL, true, true );
		gd3.heightHint = 180;
		gd3.horizontalSpan = ALIGN_TOTAL;
	    
	    final Label lblSession = new Label( comp, SWT.CENTER );
	    lblSession.setLayoutData( gd1 );
	    lblSession.setBackground( UI.COLOR_BLACK );
	    lblSession.setForeground( UI.COLOR_GREEN );
	    lblSession.setText( NetUtil.getSessionID() );
	    lblSession.setFont( fontMedium );
	    
//	    final Composite compTime = new Composite( comp, SWT.NONE );
//		compTime.setLayoutData( gd2 );
//		compTime.setBackground( UI.COLOR_BLACK );
		lblTime = new Label( comp, SWT.RIGHT );
		lblTime.setLayoutData( gd2l );
		lblTime.setBackground( UI.COLOR_BLACK );
		lblTime.setForeground( UI.COLOR_WHITE );
	    lblTime.setText( "00:00" );
	    lblTime.setFont( fontHuge );
	    
		lblAMPM = new Label( comp, SWT.LEFT );
		lblAMPM.setLayoutData( gd2r );
		lblAMPM.setBackground( UI.COLOR_BLACK );
		lblAMPM.setForeground( UI.COLOR_GRAY );
		lblAMPM.setText( "AM" );
		lblAMPM.setFont( fontLarge );
	    
	    lblDate = new Label( comp, SWT.CENTER );
	    lblDate.setLayoutData( gd3 );
	    lblDate.setBackground( UI.COLOR_BLACK );
	    lblDate.setForeground( UI.COLOR_GRAY );
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
	    			    		if ( lblTime.isVisible() ) {
	    			    			updateTime( now );
	    			    			updateDate( now );
	    			    		}
	    			    	}
	    			    });
	    			    				
	    			} while ( true );
				} catch ( final InterruptedException e ) {
					// just quit
				}
	    	}
	    };
	    threadTimer.start();
	    
	    
//	    comp.addMouseMoveListener( new MouseMoveListener() {
//			@Override
//			public void mouseMove( final MouseEvent event ) {
//				if ( event.x < comp.getBounds().x + 100 ) {
//					ShellTopMenu.get().show( true );
//				}
//			}
//		});
	    
	    
	    return comp;
	}

	
	@Override
	public CTabItem addToTabFolder( final CTabFolder tabs ) {
		
	    final CTabItem tabDailyInfo = new CTabItem( tabs, SWT.NONE );
//	    tabDailyInfo.setText( TAB_PAD_PREFIX + this.getName() + TAB_PAD_SUFFIX );
	    tabDailyInfo.setShowClose( false );
	    
	    final Composite compDailyInfo = this.buildUI( tabs );
	    tabDailyInfo.setControl( compDailyInfo );
	    if ( RPiTouchscreen.getInstance().isEnabled() ) {
	    	compDailyInfo.setCursor( UI.CURSOR_HIDE );
	    }
	    this.tab = tabDailyInfo;
	    return tabDailyInfo;
	}
	
	
	
}
