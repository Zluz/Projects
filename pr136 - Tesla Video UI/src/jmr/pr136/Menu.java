package jmr.pr136;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Menu {

	private final static Logger 
						LOGGER = Logger.getLogger( Menu.class.getName() );

	public static class Item {
		
		final String strId;
		final List<Item> listChildren = new LinkedList<>();
		String strText, strTextShort;
		boolean bSelected;
		Runnable runnable;
		
		public Item( final String strId ) {
			this.strId = strId;
			this.strText = strId;
			this.bSelected = false;
		}
		
		public void setText( final String strText ) {
			if ( null == strText ) return;
			this.strText = strText;
		}
		
		public void setShortText( final String strTextShort ) {
			if ( null == strTextShort ) return;
			this.strTextShort = strTextShort;
		}
		
		public void addItem( final Item item ) {
			this.listChildren.add( item );
		}
		
		public List<Item> getChildren() {
			return this.listChildren;
		}
		
		public String getId() {
			return this.strId;
		}
		
		public String getText() {
			return this.strText;
		}
		
		public String getTextShort() {
			if ( null != this.strTextShort ) {
				return this.strTextShort;
			} else {
				return this.strText;
			}
		}
		
		public boolean isSelected() {
			return this.bSelected;
		}
		
		public void setSelected( final boolean bSelected ) {
			this.bSelected = bSelected;
		}
		
		public void setSelected( final Item item ) {
			if ( null == item ) return;
			if ( ! this.listChildren.contains( item ) ) return;
			
			this.listChildren.forEach( i -> i.bSelected = false );
			item.bSelected = true;
			if ( null != item.runnable ) {
				item.runnable.run();
			}
		}
		
		public synchronized Item getSelectedChild() {
			if ( this.listChildren.isEmpty() ) return null;
			
			Item itemSelected = null;
			for ( final Item item : this.listChildren ) {
				if ( item.bSelected ) {
					if ( null == itemSelected ) {
						itemSelected = item;
					} else {
						item.bSelected = false;
					}
				}
			}
			if ( null == itemSelected ) {
				itemSelected = this.listChildren.get( 0 );
				this.setSelected( itemSelected );
			}
			return itemSelected;
		}
		
		public synchronized void changeSelectedChild( final int iDir ) {
			if ( 0 == iDir ) return;
			if ( this.listChildren.isEmpty() ) return;
			
			int iSelected = -1;
			for ( int i=0; i < this.listChildren.size(); i++ ) {
				final Item item = listChildren.get( i );
				if ( item.bSelected ) {
					iSelected = i;
				}
			}
			if ( -1 == iSelected ) {
				iSelected = 0;
			} else {
				iSelected = iSelected + iDir;
				final int iSize = listChildren.size();
				if ( iSelected >= iSize ) {
					iSelected -= iSize;
				} else if ( iSelected < 0 ) {
					iSelected += iSize;
				}
			}
			final Item item = listChildren.get( iSelected );
			this.setSelected( item );
		}

		public Item findItem( final String strId ) {
			for ( final Item item : this.listChildren ) {
				if ( strId.equals( item.getId() ) ) {
					return item;
				}
			}
			return null;
		}

		public void setRunnable( final Runnable runnable ) {
			this.runnable = runnable;
			if ( this.bSelected ) {
				this.runnable.run();
			}
		}
	}

	
	public enum MenuItem {
		DASHCAM_ON( "DASHCAM:Blackvue Dashcam (R1)/ON" ),
		DASHCAM_OFF( "DASHCAM/OFF", "Blackvue" ),
		DASHCAM_AUTO_GEO( "DASHCAM/AUTO-GEO" ),
			
		NETWORK_ON( "NETWORK:Vehicle Network (R2)/ON" ),
		NETWORK_OFF( "NETWORK/OFF", "V Network" ),
		NETWORK_AUTO_LAN( "NETWORK/AUTO-LAN" ),
			
		UI_THEME_DAY( "UI-THEME:Day-Night Theme/DAY", "UI Theme" ),
		UI_THEME_NIGHT( "UI-THEME/NIGHT" ),
		UI_THEME_AUTO_TIME( "UI-THEME/AUTO-TIME" ),
	
		DISPLAY_MONITORS( "DISPLAY:MCU (this) Display/MONITORS:Gauges" ),
		DISPLAY_GPS_MAP( "DISPLAY/GPS-MAP:Map", "Display" ),
		DISPLAY_NEAR_WIFI( "DISPLAY/NEAR-WIFI:WiFi" ),
		DISPLAY_DEBUG( "DISPLAY/DEBUG:Debug" ),
		DISPLAY_DIAGNOSTIC( "DISPLAY/DIAGNOSTIC:Diag" ),
	
		OVERHEAD_VOLTAGE( "OVERHEAD:Overhead Display/VOLTAGE:Accy VDC" ),
		OVERHEAD_NEAR_WIFI( "OVERHEAD/NEAR-WIFI:Near WiFi", "Overhead" ),
		OVERHEAD_GEO_LOCATION( "OVERHEAD/GEO-LOCATION:GPS-Map" ),
		OVERHEAD_DEVICE_IPS( "OVERHEAD/DEVICE-IPS:Device IPs" ),
		OVERHEAD_POWER_OPTIONS( "OVERHEAD/POWER-OPTIONS:Power" ),
			
		AUX_POWER_1_ON( "AUX-POWER-1/ON", "Aux-1" ),
		AUX_POWER_1_OFF( "AUX-POWER-1/OFF" ),
		AUX_POWER_2_ON( "AUX-POWER-2/ON", "Aux-2" ),
		AUX_POWER_2_OFF( "AUX-POWER-2/OFF" ),
		;
		final String strLine;
		final String strParentShort;
		
		MenuItem( 	final String strLine,
					final String strParentShort ) {
			this.strParentShort = strParentShort;
			this.strLine = strLine;
		}
		
		MenuItem( final String strLine ) {
			this( strLine, null );
		}
	};
	
	public final static String[] ITEMS_ = new String[] {
			"DASHCAM:Blackvue Dashcam (R1)/ON",
			"DASHCAM/OFF",
			"DASHCAM/AUTO-GEO",
			
			"NETWORK:Vehicle Network (R2)/ON",
			"NETWORK/OFF",
			"NETWORK/AUTO-LAN",
			
			"UI-THEME:Day-Night Theme/DAY",
			"UI-THEME/NIGHT",
			"UI-THEME/AUTO-TIME",

			"DISPLAY:MCU (this) Display/MONITORS:Gauges",
			"DISPLAY/GPS-MAP:Map",
			"DISPLAY/NEAR-WIFI:WiFi",
			"DISPLAY/DEBUG:Debug",
			"DISPLAY/DIAGNOSTIC:Diag",

			"OVERHEAD:Overhead Display/VOLTAGE:Accy VDC",
			"OVERHEAD/NEAR-WIFI:Near WiFi",
			"OVERHEAD/GEO-LOCATION:GPS-Map",
			"OVERHEAD/DEVICE-IPS:Device IPs",
			"OVERHEAD/POWER-OPTIONS:Power",
			
			"AUX-POWER-1/ON",
			"AUX-POWER-1/OFF",
			"AUX-POWER-2/ON",
			"AUX-POWER-2/OFF",
	};
    
	private final Item itemRoot = new Item( "(root)" );
	
	
	private final static Menu instance = new Menu();
	
	private Menu() {
//		for ( final String strItem : ITEMS ) {
		for ( final MenuItem item : MenuItem.values() ) {
			final String strItem = item.strLine;
			final String strParentShort = item.strParentShort;
			final String[] arrParts = strItem.split( "/" );
//			final String strParent = arrParts[ 0 ];
//			final String strChild = arrParts[ 1 ];
			
			final String[] arrParent = split( arrParts[ 0 ] );
			final String[] arrChild = split( arrParts[ 1 ] );
			
//			final String strPId;
//			final String strPText;
//			if ( strParent.contains( ":" ) ) {
//				final String[] arrPParts = strParent.split( ":" );
//				strPId = arrPParts[ 0 ];
//				strPText = arrPParts[ 1 ];
//			} else {
//				strPId = strParent;
//				strPText = null;
//			}
			
			if ( null == findItem( arrParent[ 0 ] ) ) {
				final Item itemParent = new Item( arrParent[ 0 ] );
				itemRoot.addItem( itemParent );
			}
			
			final Item itemParent = findItem( arrParent[ 0 ] );
			
			itemParent.setText( arrParent[ 1 ] );
			itemParent.setShortText( strParentShort );
			
			final Item itemChild = new Item( arrChild[ 0 ] );
			itemChild.setText( arrChild[ 1 ] );
			itemParent.addItem( itemChild );
		}
	}
	
	private static String[] split( final String strField ) {
		final String[] arrParts = strField.split( ":" );
		if ( arrParts.length > 1 ) {
			return new String[] { arrParts[ 0 ], arrParts[ 1 ] };
		} else {
			return new String[] { strField, null };
		}
	}
	
	private Item findItem( final String strId ) {
		for ( final Item item : itemRoot.listChildren ) {
			if ( strId.equals( item.getId() ) ) {
				return item;
			}
		}
		return null;
	}
	
	public static List<Item> getItems() {
		final List<Item> list = new LinkedList<>();
		list.addAll( instance.itemRoot.getChildren() );
		return list;
	}
	
	public static Item getRoot() {
		return instance.itemRoot;
	}
	
	
	public static Item findChildItem( final String strId ) {
		final String[] arrIds = strId.split( "/" );
		final Item itemParent = instance.itemRoot.findItem( arrIds[ 0 ] );
		if ( null == itemParent ) return null;
		final Item itemChild = itemParent.findItem( arrIds[ 1 ] );
		return itemChild;
	}
	
	public static boolean addRunnable( 	final String strCompoundId,
										final Runnable runnable ) {
		final Item item = findChildItem( strCompoundId );
		if ( null != item ) {
			item.setRunnable( runnable );
			return true;
		} else {
			LOGGER.warning( "Menu item not found: " + strCompoundId );
			return false;
		}
	}
	
}
