package jmr.pr141.ui;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.SWT;
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
	
	public CharacteristicsForm( final Display display,
								final Device device ) {
		this.device = device;
		
		final int iStyle = SWT.TOOL | SWT.RESIZE | SWT.CLOSE;
		this.shell = new Shell( display, iStyle );
		shell.setLayout( new FillLayout() );
		
		shell.setText( "Characteristics for " + device.getName() );
		
		this.tree = new Tree( shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
		tree.setItemCount( 0 );
//		tree.setHeaderVisible( true );
//		tree.setLinesVisible( true );
		tree.setHeaderVisible( false );
		tree.setLinesVisible( false );
		
//		final TreeColumn colName = new TreeColumn( tree, SWT.LEFT );
//		colName.setText( "name" );
//		colName.setWidth( 260 );
//		final TreeColumn colValues = new TreeColumn( tree, SWT.LEFT );
//		colValues.setText( "values" );
//		colValues.setWidth( 200 );
		
//		drawTreeStyle1( tree );
		drawTreeStyle2( tree );
		
		tree.addListener( SWT.PaintItem, event -> {
			final GC gc = event.gc;
			final TreeItem ti = (TreeItem)event.item;
			if ( null != ti.getParentItem() ) {
				gc.setForeground( display.getSystemColor( SWT.COLOR_DARK_BLUE ) );
				final String strText = "              " + ti.getText();
				gc.drawText( strText, event.x + 0, event.y );
			}
		});
		
		shell.pack();
	}
	
	private void drawTreeStyle1( final Tree tree ) {
		final Map<String, String> map = device.getCharacteristicsMap();
		for ( final Entry<String, String> entry : map.entrySet() ) {
			final String strKey = entry.getKey();
			final String strValue = entry.getValue();
			
			final TreeItem ti = new TreeItem( tree, 0 );
			
			if ( strValue.contains( "," ) ) {
				final String[] arrFields = strValue.split( "," );
				final String strLabel = 
							"        (" + arrFields.length + " values)";
				final String[] arrCols1 = new String[] { strKey, strLabel };
				ti.setText( arrCols1 );
				
				for ( final String strField : arrFields ) {
					final TreeItem tiField = new TreeItem( ti, 0 );
					final String[] arrCols2 = new String[] { null, strField };
					tiField.setText( arrCols2 );
				}
				
				ti.setExpanded( true );

			} else {
				final String[] arrCols1 = new String[] { strKey, strValue };
				ti.setText( arrCols1 );
			}
		}
	}

	private void drawTreeStyle2( final Tree tree ) {
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
	}
	
	public void open() {
		this.shell.open();
	}
	
}
