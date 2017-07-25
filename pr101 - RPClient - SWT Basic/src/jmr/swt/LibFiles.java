package jmr.swt;
import java.util.LinkedList;
import java.util.List;

public enum LibFiles {

	LNX_SWT_GTK(	false, true, true,
					"/usr/lib/java/swt-gtk-3.8.2.jar",
					"/usr/share/java/swt-gtk-3.8.2.jar",
					"/Share/Resources/lib/swt-gtk.jar",
					"" ),
	LNX_SWT(		false, true, true,
					"/usr/lib/java/eclipse/plugins/org.eclipse.swt_3.8.2.jar",
					"/usr/share/java/swt.jar",
					"/Share/Resources/lib/Lnx32/swt.jar",
					"" ),
	WIN_SWT_64(		true, true, false,
					"C:\\Development\\Libraries\\SWT\\Win64\\swt.jar",
					"S:\\Resources\\lib\\Win64\\swt.jar",
					"" ),
	WIN_SWT_32(		true, false, true,
					"C:\\Development\\Libraries\\SWT\\Win32\\swt.jar",
					"S:\\Resources\\lib\\Win32\\swt.jar",
					"" ),
	;

	final private String[] arrFiles;
	final boolean bWin;
	final boolean b64;
	final boolean b32;
	
	private LibFiles(	final boolean bWin,
						final boolean b64,
						final boolean b32,
						final String... strFiles ) {
		this.arrFiles = strFiles;
		this.bWin = bWin;
		this.b64 = b64;
		this.b32 = b32;
	}
	
//	public List<File> getFiles() {
//		final List<File> list = new LinkedList<>();
//		for ( final String strFile : arrFiles ) {
//			if ( !strFile.isEmpty() ) {
//				final File file = new File( strFile );
//				if ( file.isFile() ) {
//					list.add( file );
//				}
//			}
//		}
//		return list;
//	}

	public List<String> getFiles() {
		final List<String> list = new LinkedList<>();
		for ( final String strFile : arrFiles ) {
			if ( !strFile.isEmpty() ) {
				list.add( strFile );
			}
		}
		return list;
	}
	
	public boolean matches(	final boolean bWin, 
							final boolean b64 ) {
		if ( bWin != this.bWin ) return false;
		if ( b64 && this.b64 ) return true;
		if ( !b64 && this.b32 ) return true;
		return false;
	}
							

}
