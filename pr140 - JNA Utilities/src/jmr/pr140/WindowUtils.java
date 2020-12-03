package jmr.pr140;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.WINDOWINFO;
import com.sun.jna.win32.StdCallLibrary;

public class WindowUtils {

	public static class WindowInfo {
		
		public final String strTitle;
		public final HWND hwnd;
		public final int iStyle;
		public final RECT rectWindow;
		
		public WindowInfo( 	final String strTitle,
							final HWND hwnd,
							final int iStyle,
							final RECT rectWindow ) {
			this.strTitle = strTitle;
			this.hwnd = hwnd;
			this.iStyle = iStyle;
			this.rectWindow = rectWindow;
		}
	}
	
	
	
	
	public static HWND getForegroundWindow() {
		final HWND hwnd = User32.INSTANCE.GetForegroundWindow();
		return hwnd;
	}
	
	public static String getWindowTitle( final HWND hwnd ) {
		if ( null==hwnd ) return null;
		char[] buffer = new char[ 2048 ];
		User32.INSTANCE.GetWindowText( hwnd, buffer, 2048 );
		final String strText = Native.toString( buffer );
		return strText;
	}
	
	
	public static WindowInfo getWindowInfo( final HWND hwnd ) {
		final String strTitle = getWindowTitle( hwnd );
		
		final WINDOWINFO pwi = new WINDOWINFO();
		User32.INSTANCE.GetWindowInfo( hwnd,  pwi );
		//TODO lots more info in here, something may be useful
		final RECT rect = pwi.rcWindow;
		final int iStyle = pwi.dwStyle;
		
		final WindowInfo wi = new WindowInfo( strTitle, hwnd, iStyle, rect );
		return wi;
	}
	
	
	// see SO SO35393786
	public interface User32_SO35393786 extends StdCallLibrary {
		User32_SO35393786 INSTANCE = 
				(User32_SO35393786)Native.loadLibrary( 
						"user32", User32_SO35393786.class );
		
		boolean EnumWindows( WinUser.WNDENUMPROC ldEnumFunc, Pointer arg );
		
		WinDef.HWND SetFocus( WinDef.HWND hWnd );
		
		int GetWindowTextA( HWND hWnd, byte[] lpString, int nMaxCount );
		
		boolean SetForegroundWindow( WinDef.HWND hWnd );
	}
	
	static final User32_SO35393786 USER32 = User32_SO35393786.INSTANCE;
	
	
	
	
	public static Map<HWND,String> getAllWindows() {
		final Map<HWND,String> map = new HashMap<>();
		USER32.EnumWindows( new WinUser.WNDENUMPROC() {
			
			@Override
			public boolean callback( final HWND hwnd, final Pointer data ) {
				final byte[] bTitle = new byte[ 512 ];
				USER32.GetWindowTextA( hwnd, bTitle, 512 );
				final String strTitle = Native.toString( bTitle );
				map.put( hwnd, strTitle );
				return true;
			}
		}, null );
		return map;
	}
	
	
	public static WindowInfo getWindowInfo( final String strTitle ) {
		if ( null == strTitle ) return null;
		
		final Map<HWND, String> map = getAllWindows();
		for ( final Entry<HWND, String> entry : map.entrySet() ) {
			if ( strTitle.equals( entry.getValue() ) ) {
				final WindowInfo info = getWindowInfo( entry.getKey() );
				return info;
			}
		}
		return null;
	}
	
	
	public static WindowInfo findWindowMatching( final String strTitle,
												 final int iStyle ) {
		final String strSafe = null != strTitle ? strTitle : "";
		final Map<HWND, String> map = getAllWindows();
		for ( final Entry<HWND, String> entry : map.entrySet() ) {
			if ( strSafe.equals( entry.getValue() ) ) {
				final WindowInfo info = getWindowInfo( entry.getKey() );
				if ( info.iStyle == iStyle ) {
					return info;
				}
			}
		}
		return null;
	}
	
	
	
	public static void main( final String[] args ) {
		// TODO Auto-generated method stub

	}

}
