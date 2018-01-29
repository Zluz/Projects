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
	
	
	public static class JobListing {

		public Thread thread = null;
		
		public long lLastRequest;
		
		final public List<Job> listJob = new LinkedList<>();
		
	}
	
	
	
	private static JobManager instance = null;
	
	private JobManager() {};

	public static JobManager getInstance() {
		if ( null==instance ) {
			instance = new JobManager();
		}
		return instance;
	}
	
	
	public List<Job> getJobListing( final String strCondition ) {
		final long lNow = System.currentTimeMillis();
		
		final JobListing listing;
		final boolean bRefresh;
		
		if ( !JOB_LISTINGS.containsKey( strCondition ) ) {
			final JobListing listingEmpty = new JobListing();
			listingEmpty.lLastRequest = lNow;
			listingEmpty.thread = null;
			listing = listingEmpty;
			bRefresh = true;
		} else {
			listing = JOB_LISTINGS.get( strCondition );
			bRefresh = ( listing.lLastRequest + MIN_RE_QUERY_TIME ) < lNow;
		}
		
		if ( bRefresh ) {
			final List<Job> listingNew = Job.get( strCondition );
			listing.listJob.addAll( listingNew );
		}
		
		return listing.listJob;
	}
	
}
