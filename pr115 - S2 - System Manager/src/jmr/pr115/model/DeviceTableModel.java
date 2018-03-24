package jmr.pr115.model;
/*
 * Copyright (C) 2004 by Friederich Kupzog Elektronik & Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */


import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.kupzog.ktable.KTableCellEditor;
import de.kupzog.ktable.KTableCellRenderer;
import de.kupzog.ktable.KTableModel;
import de.kupzog.ktable.KTableSortedModel;
import de.kupzog.ktable.renderers.DefaultCellRenderer;
import de.kupzog.ktable.renderers.FixedCellRenderer;
import jmr.Element;
import jmr.Field;
import jmr.SessionMap;
import jmr.pr115.data.DeviceData;
import jmr.pr115.ui.SWTUtil;

/**
 * Shows how to create a table model that allows sorting the table!
 * Also demonstrates:
 *  - How the sorting works when spanned table cells exist (they get "unspanned" ;-)
 *  - Shows that is it possible to fix also body cells (@see de.kupzog.ktable.KTableModel#getFixedSelectableRowCount())
 *  
 * @author Lorenz Maierhofer <lorenz.maierhofer@logicmindguide.com>
 */
public class DeviceTableModel extends KTableSortedModel {
	

	private final Display display = Display.getCurrent();
	
    private Random rand = new Random();
    private HashMap content = new HashMap();
    
    private KTableCellRenderer m_FixedRenderer = 
        new FixedCellRenderer(FixedCellRenderer.STYLE_PUSH | 
            FixedCellRenderer.INDICATION_SORT | 
            FixedCellRenderer.INDICATION_FOCUS |
            FixedCellRenderer.INDICATION_CLICKED);
    
    private KTableCellRenderer m_DefaultRenderer = 
        new DefaultCellRenderer(0);
    
    
    private final DeviceData data;
    
    
    /**
     * Initialize the underlying model
     */
    public DeviceTableModel() {
        // before initializing, you probably have to set some member values
        // to make all model getter methods work properly.
        initialize();
        data = new DeviceData();
    }
    
    
    public SessionMap getSessionMapForRow( final int iRow ) {
    	return data.getSessionMapForRow( iRow );
    }
    
    
    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableDefaultModel#doGetContentAt(int, int)
     */
    public Object doGetContentAt( int col, int row ) {
    	
    	final Field column = getFieldForColumn( col );
    	
//    	if ( col<Field.values().length ) {
    	if ( null!=column ) {
        	if ( 0==row ) {
        		return column.name();
        	}
        	
        	final Element eValue = data.getValue( row-1, column );
        	
//        	System.out.println( "Column: " + column );
        	
        	if ( column.name().contains( "IMAGE" ) ) {
        		final File file = eValue.getAsFile();
        		if ( null!=file ) {
        			final Image image = SWTUtil.loadImage( file );
//        			final String strFilename = file.getAbsolutePath();
//					final Image image = new Image( display, strFilename );
        			if ( null!=image ) {
        				return image;
        			}
        		}
        	}
    		
    		final String strValue = null!=eValue ? eValue.getAsString() : null;
    		
//    		System.out.println( "\tvalue: " + strValue );
    		
    		if ( null!=strValue ) {
    			return strValue;
    		} else {
    			return "<null>";
    		}
    	}

        String c = (String)content.get(col+"/"+row);
        if (c==null) {
            c = rand.nextInt(100)+" ("+col+"/"+row+")";
            content.put(col+"/"+row, c);
        }
        return c;
    }
    
    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableDefaultModel#doGetCellRenderer(int, int)
     */
    public KTableCellRenderer doGetCellRenderer( int col, int row ) {
//        if ( isHeaderCell( col, row ) ) { 
        if ( ( col < 1 ) || ( row < 1 ) || ( 2==col ) || ( 3==col ) ) { 
            return m_FixedRenderer;
        }
        
        final Field field = this.getFieldForColumn( col );
        if ( null!=field ) {
        	if ( 0==field.getWidth() ) {
        		return new KTableCellRenderer() {
					@Override
					public int getOptimalWidth(GC gc, int col, int row,
							Object content, boolean fixed, KTableModel model) {
						return 0;
					}
					@Override
					public void drawCell(GC gc, Rectangle rect, int col,
							int row, Object content, boolean focus,
							boolean header, boolean clicked,
							KTableModel model) {
						// do nothing
					}
        		};
        	} else if ( field.name().contains( "IMAGE" ) ) {
        		return new KTableCellRenderer() {

					@Override
					public int getOptimalWidth(GC gc, int col, int row,
							Object content, boolean fixed, KTableModel model) {
						// TODO Auto-generated method stub
						return field.getWidth();
					}

					@Override
					public void drawCell(	final GC gc, 
											final Rectangle r, 
											final int col,
											final int row, 
											final Object content, 
											final boolean focus,
											final boolean header, 
											final boolean clicked,
											final KTableModel model ) {
//						System.out.println( "--> drawCell(), content=" + content );
						
						if ( null==content ) return;
						if ( null==gc ) return;
						
						
//						final int iColor = SWT.COLOR_DARK_GRAY;
						final int iColor = SWT.COLOR_GRAY;
						gc.setBackground( display.getSystemColor( iColor ) );
						gc.fillRectangle( r );

//						gc.setForeground( display.getSystemColor( SWT.COLOR_GRAY ) );
//						gc.drawLine( r.x, r.y, r.x+r.width, r.y+r.height );
//						gc.drawLine( r.x, r.y+r.height, r.x+r.width, r.y );
//						gc.drawLine( r.x, r.y+r.height, r.x+r.width, r.y+r.height );

//						gc.setAntialias( SWT.ON );
//						gc.setInterpolation( SWT.HIGH );

						final DrawImage di = new DrawImage();

						final Image image = getImage( content, gc.getDevice() );
//						gc.drawImage( image, r.x, r.y );
						if ( null!=image ) {
//							final Rectangle rect = image.getBounds();
							listDrawImage.add( di );
							di.image = image;
						}

//						gc.drawImage( image, 0, 0, rect.width, rect.height, 
//								r.x, r.y, r.width, r.height );

//						iImageX = r.x;
//						iImageY = r.y;
//						di.rect = r;
						
//						gc.setClipping( 0, 0, r.x+r.width, r.y+r.height );
						
						if ( 0==(row % 2) ) {
							di.rect = new Rectangle( r.x+10, r.y-10, r.width*4/9, r.height+20 );
//							gc.drawImage( image, 0, 0, rect.width, rect.height, 
//									r.x, r.y, r.width*3/7, r.height+20 );
						} else {
							di.rect = new Rectangle( r.x+r.width/2+10, r.y-10, r.width*4/9, r.height+20 );
//							gc.drawImage( image, 0, 0, rect.width, rect.height, 
//									r.x+r.width/2, r.y, r.width*3/7, r.height+20 );
						}
						
					}
        			
        		};
        	}
        }
        
        return m_DefaultRenderer;
    }
    
    
    public static class DrawImage {
    	public Image image;
    	public Rectangle rect;
    }
    
    public static final List<DrawImage> listDrawImage = new LinkedList<>();
    
    
//    public static int iImageX;
//    public static int iImageY;
    
    
    public Image getImage(	final Object object,
    						final Device device ) {
    	
    	if ( object instanceof Image ) {
    		return (Image)object;
    	}

		if ( !( object instanceof Element ) ) return null;
		
		final Element e = (Element) object;
		System.out.println( "\te: " + e.get() );
		
		final File file = e.getAsFile();
		System.out.println( "\tfile=" + file );
		if ( null==file ) return null;
		
//		final String strFilename = file.getAbsolutePath();
//		System.out.println( "\tfilename=" + strFilename );
//		final Image image = new Image( device, strFilename );
		final Image image = SWTUtil.loadImage( file );
//		System.out.println( "\timage=" + image );
		
		return image;
    }
    

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableDefaultModel#doGetCellEditor(int, int)
     */
    public KTableCellEditor doGetCellEditor(int col, int row) {
        // no celleditors:
    	System.out.println( "DeviceTableModel.doGetCellEditor() - "
    			+ "col:" + col + ", row:" + row );
        return null;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableDefaultModel#doSetContentAt(int, int, java.lang.Object)
     */
    public void doSetContentAt(	final int col, 
    							final int row, 
    							final Object value ) {
    	System.out.println( "DeviceTableModel.doSetContentAt() - "
    			+ "col:" + col + ", row:" + row + ", value:" + value );
    }
    
    /** 
     * Implement also cell spans so that it can be demonstrated how
     * the sorting algorithm works in this case: 
     * @see de.kupzog.ktable.KTableDefaultModel#doBelongsToCell(int, int)
     */
    public Point doBelongsToCell(int col, int row) {
//        if ((col==2 || col==3)&& !isFixedCell(col, row)) {
//            int newRow = row;
//            if ((row-getFixedRowCount())%2==1)
//                newRow--;
//            return new Point(2, newRow);
//        }
        return new Point(col,row);
    }
    
    public Field getFieldForColumn( final int iCol ) {
		final ColumnMap column = ColumnMap.get( iCol );
		if ( null==column ) return null;
		return column.getField();
    }
    
    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableDefaultModel#getInitialColumnWidth(int)
     */
    public int getInitialColumnWidth( final int iCol ) {
    	final Field column = getFieldForColumn( iCol );
    	if ( null!=column ) {
//    		return 50;
    		final int iWidth = column.getWidth();
    		return iCharWidth * iWidth;
    	}
    	return 10;
    }
    
    
    private int iCharWidth = 10;
    
    public void setCharWidth( final int iWidth ) {
    	this.iCharWidth = iWidth;
    }
    

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableDefaultModel#getInitialRowHeight(int)
     */
    public int getInitialRowHeight(int row ) {
        return 42;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#getRowCount()
     */
    public int doGetRowCount() {
//       return 1000+getFixedRowCount();
    	final int iSize;
    	if ( null!=this.data && null!=this.data.mapSessionIndex ) {
        	iSize = this.data.mapSessionIndex.size();
    	} else {
    		iSize = 0;
    	}
    	return iSize + getFixedRowCount();
    }
    
    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#getColumnCount()
     */
    public int doGetColumnCount() {
//        return 1000+getFixedColumnCount();
//    	return ColumnMap.values().length + getFixedColumnCount();
    	return ColumnMap.values().length;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#getFixedRowCount()
     */
    public int getFixedHeaderRowCount() {
        return 1;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#getFixedColumnCount()
     */
    public int getFixedHeaderColumnCount() {
        return 4;
    }
    
    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#getFixedSelectableRowCount()
     */
    public int getFixedSelectableRowCount() {
        return 0;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#getFixedSelectableColumnCount()
     */
    public int getFixedSelectableColumnCount() {
        return 0;
    }


    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#isColumnResizable(int)
     */
    public boolean isColumnResizable(int col) {
        return true;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#getFirstRowHeight()
     */
    public int getInitialFirstRowHeight() {
        return 22;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#isRowResizable()
     */
    public boolean isRowResizable(int row) {
       return true;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableModel#getRowHeightMinimum()
     */
    public int getRowHeightMinimum() {
       return 18;
    }

    /* (non-Javadoc)
     * @see de.kupzog.ktable.KTableDefaultModel#doGetTooltipAt(int, int)
     */
    public String doGetTooltipAt(int col, int row) {
        return "Tooltip for cell: "+col+"/"+row;
    }
}
