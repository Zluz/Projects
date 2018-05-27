package jmr.pr121.servlets;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SessionRegistry {

	public static final List<HttpSession> SESSIONS = new LinkedList<>();
	
	public static void register( final HttpSession session ) {
		if ( null==session ) return;
		SESSIONS.add( session );
	}
	
	public static void register( final HttpServletRequest request ) {
		if ( null==request ) return;
		register( request.getSession( false ) );
	}
	
	public static void invalidate() {
		for ( final HttpSession session : SESSIONS ) {
			if ( null!=session ) {
				session.invalidate();
			}
		}
	}
	
}
