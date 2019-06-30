package jmr.pr130;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

public class DeletionScheduleUI {


	private List<Schedule> listSchedules = new LinkedList<>();
	private boolean bActive = true;
	
	private static DeletionScheduleUI instance;
	
	public static synchronized DeletionScheduleUI get() {
		if ( null==instance ) {
			instance = new DeletionScheduleUI();
		}
		return instance;
	}
	
	
	public static class Schedule {
		
		final File filePath;
		final String strFileRegex;
		final int iMaxSeconds;
		boolean bActive;
		
		Schedule( final File filePath,
				  final String strFileRegex,
				  final int iMaxSeconds ) {
			this.filePath = filePath;
			this.strFileRegex = strFileRegex;
			this.iMaxSeconds = iMaxSeconds;
			this.bActive = this.filePath.isDirectory();
		}
		
		Schedule( final String strPath,
				  final String strFileRegex,
				  final int iMaxSeconds ) {
			this( new File( strPath ), strFileRegex, iMaxSeconds );
		}
	}
	
	
	public void addSchedule( final Schedule schedule ) {
		synchronized ( listSchedules ) {
			listSchedules.add( schedule );
		}
	}
	
	
	public static void log( final String strMessage ) {
		System.out.println( strMessage );
	}
	
	private Thread threadDeletionSchedule = null;
	
	public void start() {
		final Thread thread = new Thread( "Deletion schedule" ) {
			@Override
			public void run() {
				log( "Deletion schedule started" );
				while ( bActive ) {
					
					final long lTimeNow = System.currentTimeMillis();

					synchronized ( listSchedules ) {
						for ( final Schedule schedule : listSchedules ) {
							if ( schedule.bActive ) {
								checkSchedule( schedule, lTimeNow );
							}
						}
					}
					
					try {
						Thread.sleep( TimeUnit.SECONDS.toMillis( 14 ) );
					} catch ( final InterruptedException e ) {
						bActive = false;
					}
				}
				
				log( "Deletion schedule stopped" );
			}
		};
		thread.start();
		this.threadDeletionSchedule = thread;
	}

	
	public void stop() {
		this.bActive = false;
		if ( null!=this.threadDeletionSchedule ) {
			this.threadDeletionSchedule.interrupt();
		}
		DeletionScheduleUI.pool.shutdown();
	}
	
	
	
	public void checkSchedule( final Schedule schedule,
							   final long lTimeNow ) {
		if ( null==schedule ) return;
		if ( ! schedule.bActive ) return;
		
		final long lTimeLimit = 
					lTimeNow - TimeUnit.SECONDS.toMillis( schedule.iMaxSeconds );
		
		final FileFilter filter = new FileFilter() {
			@Override
			public boolean accept( final File file ) {
				if ( null==file ) return false;
//				if ( ! file.isFile() ) return false;
				if ( ! file.exists() ) return false;
				
				final String strName = file.getName();
				final String strRegex = schedule.strFileRegex;
				if ( ! strName.matches( strRegex ) ) return false;
				
				final long lModDate = file.lastModified();
				if ( lModDate > lTimeLimit ) return false;
				
//				System.out.println( "--- File passes: " + file.getAbsolutePath() );
				return true;
			}
		};
		
		
		//TODO exclude the two latest files
		
		final List<File> list = new LinkedList<>( 
				Arrays.asList( schedule.filePath.listFiles(filter) ) );
		if ( list.size() > 3 ) {
			Collections.sort( list, new Comparator<File>() {
				public int compare( final File fileLHS, final File fileRHS ) {
					return (int)( fileRHS.lastModified() - fileLHS.lastModified() );
				};
			} );
			
			list.remove( 0 );
			list.remove( 0 );
			
			for ( final File file : list ) {
				delete( file );
			}
		}
	}
	
	
	
	final static ThreadPoolExecutor 
				pool = (ThreadPoolExecutor)Executors.newFixedThreadPool( 4 );
	

	
	private void delete( final File file ) {
//		final Thread thread = new Thread( "Deleting: " + file.getName() ) {
		final Runnable runnable = new Runnable() {
			public void run() {
				try {
					FileUtils.forceDelete( file );
				} catch ( final FileNotFoundException e ) {
					// ignore; temporary file probably disappeared on its own
				} catch ( final IOException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		};
//		thread.start();
		pool.execute( runnable );
	}
	
	
	public static void main( final String[] args ) {
//		final String strFile = "capture_vid0.jpg";
//		final String strRegex = "capture_vid._.*.jpg";
		final String strFile = ".webcam-lock-1385094481-tmp1004066708410256806";
		final String strRegex = ".webcam-lock-.*-tmp.*";
		
		System.out.println( strFile.matches( strRegex ) );
	}
	

}
