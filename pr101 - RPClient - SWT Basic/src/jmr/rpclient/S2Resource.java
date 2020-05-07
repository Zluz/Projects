package jmr.rpclient;

import java.io.File;

import jmr.util.OSUtil;

public class S2Resource {

	public final String strInputPath;
	
	public final String strFilePath;
	
	public S2Resource( final String strInputPath ) {
		this.strInputPath = strInputPath;
		
		final int iPosResources = strInputPath.indexOf( "Resources" );
		String strFilePath = strInputPath.substring( iPosResources );
		if ( OSUtil.isWin() ) {
			final String strVM = "S:/" + strFilePath;
			final File fileVM = new File( strVM );
			if ( fileVM.exists() ) {
				this.strFilePath = strVM;
			} else {
				final String strDev = "H:/Share/" + strFilePath;
				final File fileDev = new File( strDev );
				if ( fileDev.exists() ) {
					this.strFilePath = strDev;
				} else {
					this.strFilePath = "//192.168.6.223/Share/" + strFilePath;
				}
			}
		} else {
			this.strFilePath = "/Share/" + strFilePath;
		}
	}
	
	public String getPath() {
		return this.strFilePath;
	}
	
	public static String resolvePath( final String strOriginal ) {
		final S2Resource resource = new S2Resource( strOriginal );
		return resource.getPath();
	}
	
}
