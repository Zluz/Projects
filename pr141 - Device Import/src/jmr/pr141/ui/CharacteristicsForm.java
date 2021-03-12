package jmr.pr141.ui;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import jmr.pr141.device.Device;

public class CharacteristicsForm {

	final private Shell shell;
	final private Tree tree;
	final private Device device;
	final private Display display;
	
	public CharacteristicsForm( final Display display,
								final Device device ) {
		this.device = device;
		this.display = display;
		
		final int iStyle = SWT.TOOL | SWT.RESIZE | SWT.CLOSE;
		this.shell = new Shell( display, iStyle );
		shell.setLayout( new FillLayout() );
		
		shell.setText( "Characteristics for " + device.getName() );
		
		this.tree = new Tree( shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		drawTree( tree );
		
		shell.pack();
	}
	
	private void drawTree( final Tree tree ) {
		final Map<String, String> map = device.getCharacteristicsMap();
		for ( final Entry<String, String> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final String strValue = entry.getValue();
			
			final TreeItem ti = new TreeItem( tree, 0 );
			ti.setText( strKey );
			
			if ( strValue.contains( "," ) ) {
				final String[] arrFields = strValue.split( "," );
				
				for ( final String strField : arrFields ) {
					final TreeItem tiField = new TreeItem( ti, 0 );
					tiField.setText( strField );
				}
				
			} else {
				final TreeItem tiField = new TreeItem( ti, 0 );
				tiField.setText( strValue );
			}
			ti.setExpanded( true );
		}
		
		final Color color = display.getSystemColor( SWT.COLOR_DARK_BLUE );
		tree.addListener( SWT.PaintItem, event -> {
			final GC gc = event.gc;
			final TreeItem ti = (TreeItem)event.item;
			if ( null != ti.getParentItem() ) {
				gc.setForeground( color );
				final String strText = "              " + ti.getText();
				gc.drawText( strText, event.x, event.y );
			}
		});
	}
	
	public void open() {
		this.shell.open();
	}
	
}
