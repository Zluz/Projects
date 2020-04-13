package jmr.pr136;

import java.util.LinkedList;
import java.util.List;

public class Menu {

	public static class Item {
		
		final String strId;
		final List<Item> listChildren = new LinkedList<>();
		String strText;
		boolean bSelected;
		
		public Item( final String strId ) {
			this.strId = strId;
			this.strText = strId;
			this.bSelected = false;
		}
		
		public void setText( final String strText ) {
			if ( null == strText ) return;
			this.strText = strText;
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
		
		public boolean getSelected() {
			return this.bSelected;
		}
		
		public void setSelected( final boolean bSelected ) {
			this.bSelected = bSelected;
		}
	}

	
	public final static String[] ITEMS = new String[] {
			"DASHCAM:Blackvue Dashcam/ON",
			"DASHCAM/OFF",
			"DASHCAM/AUTO-GEO",
			
			"NETWORK:Vehicle Network/ON",
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
		for ( final String strItem : ITEMS ) {
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
	
}
