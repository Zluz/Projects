package jmr.s2db.job;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jmr.s2db.tables.Job;

public class JobManager {

	
	/** 
	 * Do not re-query the same job listing within this time.
	 * IOW, if asking for the same listing, wait at least this long.
	 */
	public final static long MIN_RE_QUERY_TIME = 1000;
	
	private static final Map<String,JobListing> 
							JOB_LISTINGS = new HashMap<>();
	
	private final Map<String,String> mapOptions = new HashMap<>();
	
	
	public static class JobListing {

		public Thread thread = null;
		
		public long lLastRequest;
		
		final public List<Job> listJob = new LinkedList<>();
		
	}
	
	
	
	private static JobManager instance = null;
	
	private JobManager() {}

	public static JobManager getInstance() {
		if ( null==instance ) {
			instance = new JobManager();
		}
		return instance;
	}
	
	public void setOptions( final Map<String,String> mapOptions ) {
		this.mapOptions.putAll( mapOptions );
	}
	
	
	public List<Job> getJobListing(	final String strCondition,
									final int iLimit ) {
		final long lNow = System.currentTimeMillis();
		
		final String strKey = ""+iLimit+"/" + strCondition;
		
		final JobListing listing;
		final boolean bRefresh;
		
		
		if ( !JOB_LISTINGS.containsKey( strKey ) ) {
			final JobListing listingEmpty = new JobListing();
			listingEmpty.lLastRequest = lNow;
			listingEmpty.thread = null;
			listing = listingEmpty;
			bRefresh = true;
		} else {
			listing = JOB_LISTINGS.get( strKey );
			bRefresh = ( listing.lLastRequest + MIN_RE_QUERY_TIME ) < lNow;
		}
		
		if ( bRefresh ) {
			final List<Job> listingNew = Job.get( strCondition, iLimit );

//System.out.println( "" //"listingNew: " + listingNew + ", "
//				+ "strCondition = " + strCondition + ", "
//				+ "size() = " + listingNew.size() );
//if ( ! listingNew.isEmpty() ) {
//	final Job job = listingNew.get( 0 );
//	System.out.println( "JobManager - First Job: " + job.getJobSeq() + ", " + job.getJobType() );
//}
			
			// something can be null (noticed on exit)
			if ( null != listingNew && null != listing.listJob ) {
				listing.listJob.addAll( listingNew );
			}
		}
		
		return listing.listJob;
	}
	
}
