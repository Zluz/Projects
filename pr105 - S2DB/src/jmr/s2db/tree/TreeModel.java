package jmr.s2db.tree;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmr.s2db.comm.ConnectionProvider;
import jmr.s2db.tables.Page;

public class TreeModel {

	@SuppressWarnings("unused")
	private static final Logger 
			LOGGER = Logger.getLogger( TreeModel.class.getName() );

	
	final public static String DELIM = "/" ;
	
	public List<Node> listRoots = new LinkedList<Node>();
	
	final Map<String,Node> mapAllNodes = new HashMap<String,Node>();
	
	
	public static class Node {
		
		final public String strName;
		final public String strFull;
		final public List<Node> list = new LinkedList<Node>();
		final Node parent;
		final long seqPath;
		final long seqPage;
		
		final Date dateModified;
		
		Object data;
		
		public Node( 	final String strFull,
						final Node parent,
						final long seqPath,
						final long seqPage,
						final Date dateModified ) {
			this.strFull = strFull;
			final int iPos = strFull.lastIndexOf( DELIM );
			if ( iPos<0 ) throw new IllegalStateException( "Missing delimiter" );
			
			this.strName = strFull.substring( iPos + 1 );
			this.parent = parent;
			
			if ( null!=parent ) {
				parent.list.add( this );
			}
			
			this.seqPath = seqPath;
			this.seqPage = seqPage;
			this.dateModified = dateModified;
			
//			System.out.println( "Node added: " + strFull );
		}
		
		public Map<String,String> getMap() {
			final Page tPage = new Page();
			return tPage.getMap( seqPage );
		}
		
		public Long getPageSeq() {
			return this.seqPage;
		}
	}
	
	
	public Node addNode(	final String strFull,
							final long seqPath,
							final long seqPage,
							final Date dateEffective ) {
		if ( null==strFull ) return null;
		if ( mapAllNodes.containsKey( strFull ) ) {
			return mapAllNodes.get( strFull );
		}
		
		int iPos = strFull.indexOf( DELIM, 2 );
		Node nodeParent = null;

		for (;;) {
			final String strCurrentPath = strFull.substring( 0, iPos );
			final boolean bExists = mapAllNodes.containsKey( strCurrentPath );
			if ( !bExists ) {
				final Node node = new Node( strCurrentPath, nodeParent,
						seqPath, seqPage, dateEffective );
				mapAllNodes.put( strCurrentPath, node );
				
				if ( 0==strCurrentPath.lastIndexOf( DELIM ) ) {
					listRoots.add( node );
				}
				if ( strFull.equals( node.strFull ) ) {
					return node;
				}
				nodeParent = node;
			} else {
				nodeParent = mapAllNodes.get( strCurrentPath );
			}
			iPos = strFull.indexOf( DELIM, iPos + 1 );
			if ( -1 == iPos ) {
				iPos = strFull.length();
			}
		}
	}
	
	
	public void build() {
		listRoots.clear();
		mapAllNodes.clear();


		final String strQuery = 
		 "SELECT  "
		 + "	page.seq, "
		 + "    page.last_modified, "
		 + "    path.seq, "
		 + "    path.name "
		 + "FROM  "
		 + "	page, "
		 + "    path "
		 + "WHERE "
		 + "	TRUE "
		 + "	AND ( page.state = \'A\' ) "
		 + "	AND ( page.seq_path = path.seq ) "
//		 + "GROUP BY "
//		 + "	path.seq "
		 + "ORDER BY "
		 + "	path.name ASC, "
		 + "    page.last_modified DESC ";
		 
		try (	final Connection conn = ConnectionProvider.get().getConnection();
				final Statement stmt = conn.createStatement() ) {

//			stmt.executeQuery( strQuery );

			try ( final ResultSet rs = stmt.executeQuery( strQuery ) ) {
				while ( rs.next() ) {
					final long seqPage = rs.getLong( 1 );
					final Date dateModified = new Date( rs.getTimestamp( 2 ).getTime() );
					final long seqPath = rs.getLong( 3 );
					final String strName = rs.getString( 4 );
					
//					final Node node = findNode( 
					addNode( strName, seqPath, seqPage, dateModified );
//					if ( null==node.dateModified ) { // new node
////						node.dateModified = dateModified;
////						node.seqPath = seqPath;
////						node.seqPage = seqPage;
//						
//						
//						System.out.print( "Node updated: " + strName ); 
////								",  " + dateModified );
//						final Map<String, String> map = node.getMap();
//						System.out.print( "  (" + map.size() + " keys)" );
//						System.out.println();
//					}
				}
			}
			
			
		} catch ( final SQLException e ) {
			e.printStackTrace();
			LOGGER.log( Level.SEVERE, "Insert SQL: " + strQuery, e );
		}
		
	}

	
	public static void main( final String[] args ) {

		ConnectionProvider.get();
		
		final TreeModel tree = new TreeModel();
		tree.build();
	}
	
}
