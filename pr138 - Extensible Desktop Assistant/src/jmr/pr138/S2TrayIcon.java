package jmr.pr138;

import java.util.concurrent.TimeUnit;

//TODO from pr130
//import javax.json.JsonException;
//import org.apache.commons.lang3.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

import jmr.ui.ColorIcon;


public class S2TrayIcon {

	final static Display display;
	final static Tray tray;
	
	static {
		display = Display.getDefault();
		tray = display.getSystemTray();
	}
	
	final private Shell shell;
	
	private boolean bActive;
	
	private TrayItem trayitem;
	
	
	public static Image getS2Icon() {
		
		final ColorIcon ci = new ColorIcon( display );
		return ci.getIcon( new RGB( 60, 80, 250 ) );
		
//		final Class<?> classThis = S2TrayIcon.class;
//		final InputStream is = classThis.getResourceAsStream( ICON_S2 );
//		if ( null != is ) {
//			final Image image = new Image( display, is );
//			return image;
//		} else {
//			return null;
//		}
	}
	
	
	public S2TrayIcon() {
		
		bActive = true;
		
		this.trayitem = new TrayItem( tray, SWT.NONE );
		trayitem.setToolTipText( "Desktop Assistant" + "\n" 
				//TODO from pr130
//							+ NetUtil.getMAC() + "\n" 
//							+ OSUtil.getProgramName() 
							);
		
		
		final Image image = getS2Icon();
		if ( null != image ) {
			trayitem.setImage( image );
		}
		
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

		//TODO from pr130
//		miProgram.setText( OSUtil.getProgramName() );
//		miSession.setText( NetUtil.getMAC() );
		miProgram.setText( "OSUtil.getProgramName()" );
		miSession.setText( "NetUtil.getMAC()" );

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
				//TODO from pr130
//				CameraSchedulerUI.get().open();
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
		//TODO from pr130
//		CameraSchedulerUI.get().close();
//		DeletionScheduleUI.get().stop();
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
				System.out.println( "S2TrayIcon.close()" );
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
				//TODO from pr130
//				if ( StringUtils.isNotEmpty( strLine ) ) {
//					strFormatted = strLine;
//					break;
//				}
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
	
	

	public static void main( final String[] args ) {

//				//TODO from pr130
//final JsonException je = new JsonException( "test" );
		
		final S2TrayIcon trayicon = new S2TrayIcon();

		// UI loop seems to go busy until a shell is used
		trayicon.showMessage( "Desktop Assistant", 
						"Application started" );
		
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
	    
		//TODO from pr130
//	    DeletionScheduleUI.get().stop();
	}
	

}
