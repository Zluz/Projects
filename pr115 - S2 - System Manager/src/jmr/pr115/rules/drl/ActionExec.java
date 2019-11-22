package jmr.pr115.rules.drl;

import java.util.List;

import org.drools.core.WorkingMemory;
import org.drools.core.base.DefaultKnowledgeHelper;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.definitions.rule.impl.RuleImpl;
import org.drools.core.spi.Activation;
import org.drools.core.spi.KnowledgeHelper;
import org.kie.api.runtime.KieContext;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.RuleContext;

import jmr.s2db.tables.Job;
import jmr.util.report.Reporting;


public class ActionExec {
	
	public static boolean DETAIL = false;
	

	
	public static void run( final Object... objects ) {
		System.out.println( "--> ActionExec.run()" );
		
		DefaultKnowledgeHelper<?> dkh = null;
//		org.kie.api.runtiome.rule.RuleContext rc = null;
		
//		org.drools.core.spi.KnowledgeHelper
		org.kie.api.runtime.rule.RuleContext rc = null;
		org.kie.api.runtime.KieContext kc = null;
		org.drools.core.spi.KnowledgeHelper kh = null;
		Job job = null;
		
		
		
		for ( int i=0; i<objects.length; i++ ) {
			final Object obj = objects[i];
			if ( null!=obj ) {
				System.out.println( "\t" + i + ": " + obj.getClass().toString() );

				System.out.println( "\t\t(super): " + obj.getClass().getSuperclass().toString() );
				final Class<?>[] classInterfaces = obj.getClass().getInterfaces();
				for ( final Class<?> classInterface : classInterfaces ) {
					System.out.println( "\t\t(interface): " + classInterface.toString() );
				}
				
				if ( obj instanceof Job ) {
					job = (Job) obj;
				} else if ( obj instanceof DefaultKnowledgeHelper<?> ) {
					dkh = (DefaultKnowledgeHelper<?>) obj;
				} else if ( obj instanceof RuleContext ) {
					rc = (RuleContext) obj;
				} else if ( obj instanceof KieContext ) {
					kc = (KieContext) obj;
				} else if ( obj instanceof KnowledgeHelper ) {
					kh = (KnowledgeHelper) obj;
				}

				System.out.println( "\t\t" + obj.toString() );
			} else {
				System.out.println( "\t" + i + ": <null>" );
			}
		}
		
		RuleImpl rule = null;
		Activation<?> activation = null;
		WorkingMemory wm = null;
		List<? extends FactHandle> listFacts = null;
		List<Object> listObjects = null;


		if ( null!=dkh ) {
			activation = dkh.getMatch();
			rule = dkh.getRule();
			wm = dkh.getWorkingMemory();
			if ( DETAIL ) {
				System.out.println( "DefaultKnowledgeHelper:" );
				System.out.println( "\tDKH.getRule() = " + rule );
				System.out.println( "\tDKH.getMatch() = " + activation );
				System.out.println( "\tDKH.getTuple() = " + dkh.getTuple() );
				System.out.println( "\tDKH.getWorkingMemory() = " + wm );
			}
		}

		if ( null!=rule ) {
			System.out.println( "RuleImpl:" );
			System.out.println( "\tRI.getName() = " + rule.getName() );
			if ( DETAIL ) {
				System.out.println( "\tRI.getId() = " + rule.getId() );
				System.out.println( "\tRI.getFullyQualifiedName() = " + rule.getFullyQualifiedName() );
				System.out.println( "\tRI.getResource() = " + rule.getResource() );
				System.out.println( "\tRI.getMetaData() = " + rule.getMetaData() );
			}
		}

		if ( null!=activation ) {
			listFacts = activation.getFactHandles();
			listObjects = activation.getObjects();
			if ( DETAIL ) {
				System.out.println( "(Match) Activation<>:" );
				System.out.println( "\tA.getFactHandles() = " + listFacts );
			}
		}

		if ( null!=listObjects ) {
			System.out.println( "List Objects:" );
			for ( final Object obj : listObjects ) {
				System.out.println( "\tObject : " + obj.getClass() );
				System.out.println( "\tObject = " + obj.toString() );

				if ( obj instanceof Job ) {
					job = (Job)obj;
				} else if ( obj instanceof DefaultKnowledgeHelper<?> ) {
					dkh = (DefaultKnowledgeHelper<?>) obj;
				} else if ( obj instanceof RuleContext ) {
					rc = (RuleContext) obj;
				} else if ( obj instanceof KieContext ) {
					kc = (KieContext) obj;
				} else if ( obj instanceof KnowledgeHelper ) {
					kh = (KnowledgeHelper) obj;
				}
			}
		}

		if ( null!=listFacts && DETAIL ) {
			System.out.println( "List FactHandle:" );
			for ( final FactHandle fact : listFacts ) {
				System.out.println( "\tFact : " + fact.getClass() );
				System.out.println( "\tFact = " + fact.toString() );
				
				if ( fact instanceof DefaultFactHandle ) {
					final DefaultFactHandle dfh = (DefaultFactHandle)fact;
					System.out.println( "\tdfh.getExternalForm() : " + dfh.getExternalForm() );
					System.out.println( "\tdfh.getObjectClassName() : " + dfh.getObjectClassName() );
					System.out.println( "\tdfh.getEqualityKey() : " + dfh.getEqualityKey() );
					System.out.println( "\tdfh.getTraitType() : " + dfh.getTraitType() );
					System.out.println( "\tdfh.getDataSource() : " + dfh.getDataSource() );
					System.out.println( "\tdfh.getEntryPoint() : " + dfh.getEntryPoint() );
					System.out.println( "\tdfh.getFirstLeftTuple() : " + dfh.getFirstLeftTuple() );
					System.out.println( "\tdfh.getFirstRightTuple() : " + dfh.getFirstRightTuple() );
				}
				
			}
		}
		
		if ( null!=job ) {
			System.out.println( "Job:" );
			System.out.println( "\tJ.getJobSeq() = " + job.getJobSeq() );
			System.out.println( "\tJ.getJobType() = " + job.getJobType() );
		}

//		if ( null!=wm ) {
//			System.out.println( "WorkingMemory:" );
//		}
		
		boolean bReportStackTrace = false;
		
		try {
			
			if ( null!=rule ) {
				final String strName = rule.getName();
				if ( "PrepareTesla-ShowerTrigger".equals( strName ) ) {
					Simple.doPrepareTesla( true, "water_trigger" );
				} else if ( "Request data from Tesla".equals( strName )) {
					Simple.doCheckTeslaState( job );
				} else if ( "Send command to Tesla".equals( strName )) {
					Simple.doCheckTeslaState( job );
				} else if ( "Refresh Tesla (scheduled)".equals( strName )) {
					Simple.doCheckTeslaState( null );
				} else {
					System.err.println( "WARNING: "
							+ "Rule '" + strName + "' not matched in ActionExec" );
					bReportStackTrace = true;
				}
			}
		} catch ( final Throwable t ) {
			bReportStackTrace = true;
			System.err.println( "ERROR encountered while executing "
					+ "action implementation: " + t.toString() );
		}
		
		if ( bReportStackTrace ) {
			final StackTraceElement[] stes = Thread.currentThread().getStackTrace();
			System.out.println( "Call Stack:" );
			final String strStack = Reporting.reportThreadStack( stes );
			boolean bLastSkipped = false;
			for ( final String strFrame : strStack.split( "\n" ) ) {
				final String strLine = strFrame.trim();
				
				if ( strLine.startsWith( "org.mvel2." ) 
						|| strLine.startsWith( "org.drools.core." ) ) {
					if ( ! bLastSkipped ) {
						System.out.println( "\t<...>" );
						bLastSkipped = true;
					}
				} else {
					bLastSkipped = false;
					System.out.println( "\t" + strLine );
				}
			}
		}
		
		System.out.println( "<-- ActionExec.run()" );
	}
	
}
