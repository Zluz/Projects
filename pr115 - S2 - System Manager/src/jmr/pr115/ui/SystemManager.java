package jmr.pr115.ui;

import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.kupzog.ktable.KTable;
import de.kupzog.ktable.KTableCellSelectionListener;
import de.kupzog.ktable.SWTX;
import jmr.Element;
import jmr.Field;
import jmr.SessionMap;
import jmr.pr115.model.DeviceTableModel;
import jmr.pr115.model.DeviceTableModel.DrawImage;

public class SystemManager {

	
	public static void main( final String[] args ) {
		
		final Display display = Display.getDefault();
		final Shell shell = new Shell( display, SWT.SHELL_TRIM );
		
		shell.setText( "System Manager" );
		shell.setLayout( new FillLayout() );
//		shell.setSize( 1300, 500 );


		final KTable table = new KTable( shell, 
//				SWT.NULL );
				SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL
				| SWTX.FILL_WITH_LASTCOL | SWTX.EDIT_ON_KEY );
		
		final DeviceTableModel model = new DeviceTableModel();
		table.setModel( model );
		
		final KTableCellSelectionListener listenerSelection = 
									new KTableCellSelectionListener() {
			@Override
			public void cellSelected( int col, int row, int statemask ) {
				System.out.println();
		    	System.out.println( "DeviceTableKTableCellSelectionListenerModel.cellSelected() - "
		    			+ "col:" + col + ", row:" + row + ", statemask:" + statemask );
		    	final DeviceTableModel model = (DeviceTableModel) table.getModel();
		    	if ( col > 0 ) {
			    	final Object content = model.doGetContentAt( col, row );
			    	System.out.println( "\tContent: " + content );
		    	} else {
		    		final SessionMap sm = model.getSessionMapForRow( row );
		    		if ( null!=sm ) {
			    		System.out.println( "SessionMap " + sm.hashCode() );
			    		for ( final Entry<Field, Element> entry : sm.entrySet() ) {
			    			System.out.println( "\t" 
			    						+ entry.getKey().name()
			    						+ ":" + entry.getValue().getAsString() );
			    		}
		    		} else {
		    			System.out.println( "(Null SessionMap)" );
		    		}
		    	}
			}

			@Override
			public void fixedCellSelected(int col, int row,
					int statemask) {}
		};
		table.addCellSelectionListener( listenerSelection );
		final Integer[] arrWidth = new Integer[]{ 0 };
		
		table.addPaintListener( new PaintListener() {
			@Override
			public void paintControl( final PaintEvent event ) {
				if ( 0==arrWidth[0] ) {
					final int iWidth = event.gc.textExtent( "0123456789" ).x;
					final int iX = Math.round( iWidth / 10 );
					arrWidth[0] = iX;
			    	final DeviceTableModel model = 
			    						(DeviceTableModel) table.getModel();
					model.setCharWidth( iX );
				};

				
//				final Iterator<Image> iterator = 
//										SWTUtil.MAP_IMAGES.values().iterator();
//				if ( iterator.hasNext() ) {
//System.out.println( "drawing image.." );					
//					final Image image = iterator.next();
//					if ( null!=image ) {
//						event.gc.drawImage( image, DeviceTableModel.iImageX, DeviceTableModel.iImageY );
//					}
//				}


				event.gc.setAntialias( SWT.OFF );
				event.gc.setInterpolation( SWT.LOW );
				
				for ( final DrawImage di : DeviceTableModel.listDrawImage ) {
					final Image image = di.image;
					final Rectangle r = image.getBounds();
					final Rectangle rect = di.rect;
					event.gc.drawImage( image, 
							0, 0, r.width, r.height, 
							rect.x, rect.y, rect.width, rect.height ); 
				}
				DeviceTableModel.listDrawImage.clear();
				
			}
		});
		
		shell.setSize( 1800, 800 );
		shell.open();
		table.redraw();

		while ( !shell.isDisposed() ) {
			if ( display.readAndDispatch() ) {
				display.sleep();
			}
		}
		System.exit( 1 );
	}
	
}
