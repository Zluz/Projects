package jmr.pr140;

import java.util.LinkedList;
import java.util.List;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.Tlhelp32.PROCESSENTRY32;
import com.sun.jna.platform.win32.WinDef.DWORD;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class ProcessUtils {
	
	public static class Process {
		
		public final String strName;
		public final int iPid;
		public final int iPPid;
		public final long lThreads;
		public final long lPriority;
		
		Process(	final String strName,
					final int iPid,
					final int iPPid,
					final long lThreads,
					final long lPriority ) {
			this.strName = strName;
			this.iPid = iPid;
			this.iPPid = iPPid;
			this.lThreads = lThreads;
			this.lPriority = lPriority;
		}
	}
	
	private static final Kernel32 kernel = 
					(Kernel32)Native.loadLibrary( Kernel32.class );

	// see SO 34553440
	public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32 {
		Kernel32 INSTANCE = (Kernel32)Native.loadLibrary( 
						"kernel32", Kernel32.class );
		
		boolean Process32FirstW( HANDLE hSnapshot, Tlhelp32.PROCESSENTRY32 lppe );
		boolean Process32NextW( HANDLE hSnapshot, Tlhelp32.PROCESSENTRY32 lppe );
	}
	
	
	public static List<Process> getProcessList() {
		final List<Process> list = new LinkedList<>();
		HANDLE snapshot = null;
		
		try {
			snapshot = kernel.CreateToolhelp32Snapshot( 
					Tlhelp32.TH32CS_SNAPPROCESS,  new DWORD( 0 ) );
			final PROCESSENTRY32 entry = new PROCESSENTRY32();
			kernel.Process32FirstW( snapshot,  entry );
			
			do {
				final String strName = Native.toString( entry.szExeFile );
				final int iPid = entry.th32ProcessID.intValue();
				final int iPPid = entry.th32ParentProcessID.intValue();
				final long lThreads = entry.cntThreads.longValue();
				final long lPriority = entry.pcPriClassBase.longValue();
				final Process process = new Process( 
						strName, iPid, iPPid, lThreads, lPriority );
				list.add( process );
			} while ( kernel.Process32NextW( snapshot, entry ) );
			
		} finally {
			kernel.CloseHandle( snapshot );
		}
		return list;
	}

	
	
	public static void main( final String[] args ) {
		final List<Process> list = getProcessList();
		for ( final Process process : list ) {
			System.out.println( 
					"\tprocess " + process.iPid + ":\t" + process.strName );
		}
	}
	
}
