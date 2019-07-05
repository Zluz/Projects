package jmr.pr130;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import jmr.SessionPath;
import jmr.pr130.DeletionScheduleUI.Schedule;
import jmr.s2db.Client;
import jmr.s2db.Client.ClientType;
import jmr.util.NetUtil;
import jmr.util.OSUtil;
import jmr.util.SystemUtil;

public class S2TrayIcon {

	final static Display display;
	final static Tray tray;
	
	static {
		display = Display.getDefault();
		tray = display.getSystemTray();
	}
	
	final private Shell shell;
	
	private boolean bActive;
	
	
//	final static String ICON_S2 = "/resources/icons/1872821.png"; // 3-line
//	final static String ICON_S2 = "/resources/icons/121155.png"; // cyan
	final static String ICON_S2 = "/resources/icons/116048.png"; // on-off

	private TrayItem trayitem;
	
	
	public static Image getS2Icon() {
		final Class<?> classThis = S2TrayIcon.class;
		final InputStream is = classThis.getResourceAsStream( ICON_S2 );
		final Image image = new Image( display, is );
		return image;
	}
	
	
	public S2TrayIcon() {
		
		bActive = true;
		
		this.trayitem = new TrayItem( tray, SWT.NONE );
		trayitem.setToolTipText( "Desktop Assistant" + "\n" 
							+ NetUtil.getMAC() + "\n" 
							+ OSUtil.getProgramName() );
		
		
		final Image image = getS2Icon();
		trayitem.setImage( image );
		
		this.shell = new Shell( display, SWT.NONE );
		final Menu menu = new Menu( shell, SWT.POP_UP );

		final MenuItem miProgram = new MenuItem( menu, SWT.PUSH );
		final MenuItem miSession = new MenuItem( menu, SWT.PUSH );
		new MenuItem( menu, SWT.SEPARATOR );

		final MenuItem miShowSchedule = new MenuItem( menu, SWT.PUSH );
		final MenuItem miShowDevices = new MenuItem( menu, SWT.PUSH );
		final MenuItem miLaunchClient = new MenuItem( menu, SWT.PUSH );
		new MenuItem( menu, SWT.SEPARATOR );
		final MenuItem miShowMenu = new MenuItem( menu, SWT.RADIO );
		final MenuItem miClipboardMulti = new MenuItem( menu, SWT.RADIO );
		final MenuItem miClipboardLine = new MenuItem( menu, SWT.RADIO );
		final MenuItem miPostEvent = new MenuItem( menu, SWT.RADIO );
		new MenuItem( menu, SWT.SEPARATOR );
		final MenuItem miClose = new MenuItem( menu, SWT.PUSH );

		miProgram.setText( OSUtil.getProgramName() );
		miSession.setText( NetUtil.getMAC() );

		miShowSchedule.setText( "Show Scheduler" );
		miShowDevices.setText( "Show Devices" );
		miLaunchClient.setText( "Launch S2 client" );
		
		miShowMenu.setText( "Show this pop-up menu" );
		miClipboardMulti.setText( "Clipboard full to plain text" );
		miClipboardLine.setText( "Clipboard line to plain text" );
		miPostEvent.setText( "Post S2 status event" );
		
		miShowMenu.setSelection( true );
		
		miClose.setText( "Close" );

		trayitem.addListener( SWT.Selection, new Listener() {
			@Override
			public void handleEvent( final Event e ) {
				if ( miShowMenu.getSelection() ) {
					menu.setVisible( true );
				} else if ( miClipboardMulti.getSelection() ) {
					doSetClipboardToPlainText( false );
				} else if ( miClipboardLine.getSelection() ) {
					doSetClipboardToPlainText( true );
				} else if ( miPostEvent.getSelection() ) {
					doPostStatusEvent();
				}
			}
		});
		
		trayitem.addListener( SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent( final Event event ) {
				menu.setVisible( true );
			}
		});
		
		miShowSchedule.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected( final SelectionEvent e ) {
				CameraSchedulerUI.get().open();
			}
		});
		
		miClose.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected( final SelectionEvent e ) {
				close();
			}
		});
	}
	
	
	public boolean isActive() {
		return this.bActive;
	}
	
	public void close() {
		CameraSchedulerUI.get().close();
		DeletionScheduleUI.get().stop();
		this.bActive = false;
		
		final Thread threadShutdown = new Thread( "Shutting down" ) {
			public void run() {
				try {
					TimeUnit.SECONDS.sleep( 2 );
				} catch (InterruptedException e) {
					// just ignore, quitting anyway
				}
				if ( ! display.isDisposed() ) {
					display.syncExec( new Runnable() {
						@Override
						public void run() {
							shell.dispose();
							trayitem.dispose();
						}
					});
				}
				Runtime.getRuntime().exit( 0 );
			};
		};
		threadShutdown.start();
	}
	
	
	public void doSetClipboardToPlainText( final boolean bSingleLine ) {
		System.out.println( "--- doSetClipboardToPlainText()" );
		
		final Clipboard clipboard = new Clipboard( display );
		final TextTransfer transfer = TextTransfer.getInstance();
		final Object objContents = clipboard.getContents( transfer );
		if ( null==objContents ) {
			System.out.println( "Clipboard is empty." );
			return;
		}
		final String strContents = objContents.toString();
		String strFormatted = strContents;
		
		if ( bSingleLine && strContents.contains( "\n" ) ) {
			for ( String strLine : strContents.split( "\\n" ) ) {
				strLine = strLine.trim();
				if ( StringUtils.isNotEmpty( strLine ) ) {
					strFormatted = strLine;
					break;
				}
			}
		}
		
		System.out.println( "Setting clipboard to:\n"
								+ "---start---\n" 
								+ strFormatted + "\n"
								+ "---end---" );
		
		clipboard.setContents( new String[] { strFormatted }, 
								new Transfer[] { transfer } );
		clipboard.dispose();
		
		showMessage( "Clipboard contents set to plain text", strFormatted );
	}
	
	
	public void doPostStatusEvent() {
		System.out.println( "--- doPostStatusEvent()" );
		showMessage( "Not supported", "Posting event not yet supported" );
	}
	
	
	public void showMessage( final String strTitle,
							 final String strBody ) {
		final int iStyle = SWT.BALLOON | SWT.ICON_INFORMATION;
		final ToolTip tooltip = new ToolTip( this.shell, iStyle );

		tooltip.setText( strTitle );
		tooltip.setMessage( strBody );
		tooltip.setAutoHide( false );
		
		this.trayitem.setToolTip( tooltip );
		
		tooltip.setVisible( true );
		
		final Thread thread = new Thread( "Delayed close of tooltip" ) {
			public void run() {
				try {
					Thread.sleep( TimeUnit.SECONDS.toMillis( 4 ) );
				} catch ( final InterruptedException e ) {}
				display.asyncExec( new Runnable() {
					@Override
					public void run() {
						tooltip.setVisible( false );
						tooltip.dispose();
					}
				});
			};
		};
		thread.start();
	}
	
	
	public static void initializeDeletionSchedule() {
		
		final DeletionScheduleUI scheduler = DeletionScheduleUI.get();
		scheduler.start();
		
		//NOTE: keep jar_cache*.tmp (embedded jars extracted to tmp) 
		
		scheduler.addSchedule( new Schedule( 
				SystemUtil.getTempDir(), ".webcam-lock-.*", 10 * 60 ) );
		
		scheduler.addSchedule( new Schedule( 
				SystemUtil.getTempDir(), "capture_vid.-.*.jpg", 60 ) );
		
		scheduler.addSchedule( new Schedule( 
				SystemUtil.getTempDir(), "_capture_vid.-.*.jpg", 60 ) );
		scheduler.addSchedule( new Schedule( 
				SystemUtil.getTempDir(), "capture_vid.-.*.jpg_", 10 ) );
		scheduler.addSchedule( new Schedule( 
				SystemUtil.getTempDir(), "Capture_.*.jpg", 60 ) );

		scheduler.addSchedule( new Schedule( 
				SessionPath.getSessionDir(), "capture_vid.-t.*.jpg", 5 * 60 ) );
		scheduler.addSchedule( new Schedule( 
				SessionPath.getSessionDir(), "capture_vid.-t.*.jpg_", 10 ) );
		scheduler.addSchedule( new Schedule( 
				SessionPath.getSessionDir(), "pr125_.*.pid", 10 * 60 ) );

		scheduler.addSchedule( new Schedule( 
				SystemUtil.getTempDir(), "pr124_.*jar", 24 * 2 * 3600 ) );
		scheduler.addSchedule( new Schedule( 
				SystemUtil.getTempDir(), "BridJExtractedLibraries.*", 10 * 60 ) );

		scheduler.addSchedule( new Schedule( 
				Paths.get( "." ).toFile(), "hs_err_pid.*.log", 10 ) );
		scheduler.addSchedule( new Schedule( 
				Paths.get( "." ).toFile(), "replay_pid.*.log", 10 ) );
		scheduler.addSchedule( new Schedule( 
				SystemUtil.getTempDir(), "hs_err_pid.*.log", 10 ) );
		
		//BridJExtractedLibraries7152577194854756978
	}
	
	

	public static void main( final String[] args ) {

		final Long seqSession = Client.get().register( ClientType.TRAY_GUI, 
					"Desktop Assistant", S2TrayIcon.class.getSimpleName() );
		
		if ( null==seqSession ) {
			System.err.println( "Failed to initialize client session." );
			System.out.println( "Another client instance may already be running." );
			System.out.println( "Exiting." );
			Runtime.getRuntime().exit( 0 );
		}

		final S2TrayIcon trayicon = new S2TrayIcon();

		initializeDeletionSchedule();

		// UI loop seems to go busy until a shell is used
		trayicon.showMessage( "Desktop Assistant", 
						"Client registered, session " + seqSession );
		
	    while ( trayicon.isActive() 
	    				&& null!=display
						&& ! display.isDisposed() 
						&& ! trayicon.shell.isDisposed() 
	    				) {
		      if ( display.readAndDispatch()) {
		    	  display.sleep();
		      }
		}
	    if ( ! display.isDisposed() ) {
	    	display.dispose();
	    }
	    
	    DeletionScheduleUI.get().stop();
	}
	

}
