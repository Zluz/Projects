package jmr.rpclient.tiles;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import jmr.rpclient.swt.GCTextUtils;
import jmr.rpclient.swt.S2Button;
import jmr.rpclient.swt.Theme;
import jmr.s2db.job.JobMonitor;
import jmr.s2db.tables.Job;
import jmr.s2db.tables.Job.JobState;

public class JobDetailTile extends TileBase {




	public JobDetailTile(  final Map<String, String> mapOptions  ) {
		JobMonitor.get().initialize( mapOptions );
	}
	


	@Override
	public void paint(	final GC gc, 
						final Image image ) {

		JobMonitor.get().check();
		
		final LinkedList<Job> listing = JobMonitor.get().getListing();

		final Iterator<Job> iterator = listing.iterator();
		if ( iterator.hasNext() ) {
			
			final Job job = iterator.next();
			
			final GCTextUtils text = new GCTextUtils( gc );
			text.setRect( gc.getClipping() );
			
			gc.setFont( Theme.get().getFont( 11 ) );
	
			final long seqJob = job.getJobSeq();
			
			final JobState state = job.getState();
			text.println( " Job " + seqJob + " - " + state.name() );

			final String strRequest = job.getRequest();
			text.println( " " + strRequest );

			text.addSpace( 8 );
			
			final String strResult = job.getResult();
			
			try {
				final JsonParser parser = new JsonParser();
				final JsonElement je = parser.parse( strResult );
				final JsonObject jo = je.getAsJsonObject();
				
				gc.setFont( Theme.get().getFont( 11 ) );

				for ( final Entry<String, JsonElement> entry : jo.entrySet() ) {
					final String strKey = entry.getKey();
					final String strValue = entry.getValue().toString();
					final String strOutput = strKey + ": " + strValue;
//					text.println( strKey + ": " + strValue );
					boolean bFirst = true;
					for ( final String strLine : strOutput.split( "\\\\n" ) ) {
						if ( bFirst ) {
							text.println( strLine );
							bFirst = false;
						} else {
							text.println( "         |" + strLine );
						}
					}
				}
			} catch ( final Exception e ) {
				text.println( strResult );
			}
			
	
		}
	}

	
	@Override
	protected void activateButton( final S2Button button ) {}
	

}
