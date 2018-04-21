package jmr.pr115.rules;

import jmr.pr118.DroolsSession;
import jmr.pr118.RuleSet;
//import org.kie.api.io.Resource;

public class RulesProcessing {

	final public static String DROOLS_TITLE = "S2-System-Manager";

	
	private static RulesProcessing instance = null;
	private DroolsSession session = null;
	
	private static boolean bTestLatest = false;
	
	
	private RulesProcessing() {
		initialize();
		if ( !bTestLatest ) {
			new ProcessEvent();
		}
	}
	
	public synchronized static RulesProcessing get() {
		if ( null==instance ) {
			instance = new RulesProcessing();
		}
		return instance;
	}
	
	public synchronized static void setTestMode() {
		bTestLatest = true;
		get();
	}
	
	
	private void initialize() {
		final RuleSet rules = new RuleSet();
		rules.addResources( "jmr/pr115/rules/drl", bTestLatest );
		
		System.out.println( "Identified resources to load:" );
		for ( final String resource : rules.getResourcesAsText() ) {
			System.out.println( "\t" + resource );
		}
		
		if ( rules.getResourcesAsText().isEmpty() ) {
			Runtime.getRuntime().exit( 100 );
		}
		
		session = new DroolsSession( rules, DROOLS_TITLE );
		System.out.println( "DroolsSession initialized." );
	}
	
	
	public boolean process( final Object item ) {
		if ( null==session ) return false;

//		System.out.println( "--> RulesProcessing.process()" );
		
		final int result = session.processItem( item );
		
//		System.out.println( "<-- RulesProcessing.process(), result = " + result );
		return result > 0;
	}
	
	
	public static void main( final String[] args ) {
		setTestMode();
		final RulesProcessing rules = RulesProcessing.get();
		System.out.println( "Rules loaded: " + rules.session.getRulesLoaded() );
		rules.process( null );
//		Thread.sleep( TimeUnit.MINUTES.toMillis( 1 ) );
	}
	
	
}
