package jmr.pr118;

import java.util.List;
import java.util.function.Predicate;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.builder.InternalKieBuilder;

public class DroolsSession {


    final static String KMODULE_TEXT = 
    		"<kmodule xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
            "         xmlns=\"http://www.drools.org/xsd/kmodule\">\n" +
            "  <kbase name=\"kbase1\" default=\"true\" "
                		+ "eventProcessingMode=\"stream\" "
                		+ "equalsBehavior=\"identity\" "
                		+ "scope=\"javax.enterprise.context.ApplicationScoped\">\n" +
            "    <ksession name=\"ksession1\" type=\"stateful\" "
            			+ "default=\"true\" clockType=\"realtime\" "
            			+ "scope=\"javax.enterprise.context.ApplicationScoped\"/>\n" +
            "  </kbase>\n" +
            "</kmodule>";

	
    private final KieSession session;
    private final RuleSet rules;


	public DroolsSession(	final RuleSet rules,
							final String strTitle ) {
		this.rules = rules;

        final KieServices ks = KieServices.Factory.get();

        final ReleaseId releaseId1 = 
        			ks.newReleaseId( "org.kie", "test-kie-builder", "1.0.0" );

        final KieModule km = createAndDeployJar( ks,
                                           KMODULE_TEXT,
                                           releaseId1,
                                           rules.getResources() );

        final KieContainer kc = ks.newKieContainer( km.getReleaseId() );
        this.session = kc.newKieSession();
	}
	
	
	public synchronized int processItems( final List<Object> items ) {
		if ( null==items ) return -1;
		if ( items.isEmpty() ) return 0;
		
		for ( final Object item : items ) {
			session.insert( item );
		}
		
		final int iFired = session.fireAllRules();
		return iFired;
	}
	

	public synchronized int processItem( final Object item ) {
		if ( null==item ) return -1;
		
		final FactHandle handle = session.insert( item );
		
		try {
			
			final int iFired = session.fireAllRules();
			return iFired;
			
		} catch ( final Exception e ) {
			System.err.println( e.toString() + " encountered while "
					+ "processing Rules." );
			e.printStackTrace();
			return 0;
			
		} finally {
			session.delete( handle );
		}
		
		
	}
	
	
	public int getRulesLoaded() {
		if ( null==session ) return -1;
		
		return this.rules.getResources().size();
	}
	
	


	public byte[] createJar(	final KieServices ks, 
								final String kmoduleContent,
								final Predicate<String> classFilter, 
								final ReleaseId releaseId,
								final List<Resource> resources ) {
		
		final KieFileSystem kfs = ks.newKieFileSystem()
				.generateAndWritePomXML(releaseId)
				.writeKModuleXML(kmoduleContent);
		
		for ( final Resource resource : resources ) {
			if ( null!=resource ) {
				kfs.write( resource );
			}
		}

		final KieBuilder kieBuilder = ks.newKieBuilder( kfs );
		try {
			((InternalKieBuilder) kieBuilder).buildAll( classFilter );
		} catch ( final Throwable e ) {
			System.err.println( "WARNING: "
					+ "Error encountered during buildAll()." );
			System.err.println( "Configuration may be invalid." );
			Runtime.getRuntime().exit( 100 );
		}
		
		final InternalKieModule kieModule = 
				(InternalKieModule) ks.getRepository().getKieModule(releaseId);
		
		if ( null==kieModule ) {
			System.err.println( "WARNING: "
					+ "Unable to initialize InternalKieModule." );
			System.err.println( "Configuration may be invalid." );
			Runtime.getRuntime().exit( 100 );
		}
		
		final byte[] jar = kieModule.getBytes();
		return jar;
	}


	public KieModule createAndDeployJar(	final KieServices ks,
											final String kmoduleContent, 
											final ReleaseId releaseId, 
											final List<Resource> resources ) {
		
		final byte[] jar = createJar( 
				ks, kmoduleContent, o -> true, releaseId, resources );

		Resource jarRes = ks.getResources().newByteArrayResource(jar);
		KieModule km = ks.getRepository().addKieModule(jarRes);
		return km;
	}
	
	
	
}
