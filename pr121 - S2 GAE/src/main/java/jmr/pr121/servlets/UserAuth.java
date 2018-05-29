package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.entity.ContentType;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import jmr.pr121.config.Configuration;

public class UserAuth {

	/*
	 *  -1 = known, deny
	 *   0 = uninitialized
	 *   1 = recognized browser only
	 *   2 = username matched
	 *   3 = basic authentication
	 *   4 = gae user auth
	 */
	
	
	
	final HttpServletRequest request;
	final HttpServletResponse response;
	
	final int iLevel;
	
	boolean bAborted = false;
	
	
	public final static boolean AUTH_USER_BY_HTTP = false;
	public final static boolean AUTH_USER_BY_GAE = true;
	public final static boolean CHECK_HTTPS = true;
	
	
	public UserAuth(	final HttpServletRequest request,
						final HttpServletResponse response ) {

		Log.add( "Authenticating (" + request.getRequestURL().toString() + ").." );

		this.request = request;
		this.response = response;
		
		final Configuration config = Configuration.get();
		
		if ( ! config.isBrowserTestInitialized() ) {
			iLevel = 0;
		} else {
			final boolean bBrowserOk = config.isBrowserAccepted( request );
			if ( ! bBrowserOk ) {
				this.iLevel = - 1;
			} else {

				int iTest = 1;
				
				try {
					
					final String strUsr = config.get( "gae.username" );
					final String strPwd = config.get( "gae.password" );
					
					if ( AUTH_USER_BY_HTTP ) {
						Log.add( "Authenticating using HttpRequest.." );
						
						try {
							Log.add( "\t..trying request.authenticate().." );
							request.authenticate( response );
							
							if ( ! strUsr.equals( request.getUserPrincipal().toString() ) ) {
								Log.add( "\t..trying request.login().." );
								request.login( strUsr, strPwd );
							}
							
							
							final String strPrincipal = 
											request.getUserPrincipal().toString();
							if ( strUsr.equals( strPrincipal ) ) {
								iTest = 2;
							} else {
								iTest = 1;
							}
							
						} catch ( final Exception e ) {
							Log.add( "Exception while authenticating: " + e.toString() );
							
							this.logout();
							
							iTest = 1;
						}
					}
					
					if ( AUTH_USER_BY_GAE ) {
	
						final UserService service = UserServiceFactory.getUserService();
						final User user = service.getCurrentUser();
	
						Log.add( "UserService: " + service );
						Log.add( "User: " + user );
						
						if ( null==user ) {
							
							iTest = 0;
							this.login( service );
							
//							return;
						} else {
	
							Log.add( "\tclass: " + user.getClass().getName() );
							Log.add( "\temail: " + user.getEmail() );
//							Log.add( "\tuser id: " + user.getUserId() );
							Log.add( "\tdomain: " + user.getAuthDomain() );
							
							final String strUserGAE = user.getEmail();
							
		//					if ( strUsr.equals( strUserGAE ) ) {
		//						iTest = 2;
		//					} else {
		//						iTest = 1;
		//					}
							
							final boolean bIsValid = 
									Configuration.get().isValidUser( strUserGAE, null );
							iTest = bIsValid ? 2 : 1;
						}
					}
					
					if ( CHECK_HTTPS ) {
						final boolean bHTTPS;
						final String strUpgrade = "" + request.getHeader( 
										"upgrade-insecure-requests" );
//						final String strReferred = "" + request.getHeader( 
//										"referer" );
						bHTTPS = strUpgrade.equals( "1" )
								; // && strReferred.startsWith( "https://" );
						
						if ( bHTTPS ) {
							Log.add( "HTTPS connection detected." );
						} else if ( Configuration.isGAEDevelopment() ) {
							Log.add( "Not using HTTPS, but is Development." );
						} else {
							Log.add( "Not using HTTPS, not Development. "
									+ "Reducing authentication level." );
							iTest = 1;
						}
					}
					
				} catch ( final Exception e ) {
					Log.add( "Exception encountered while authenticating: " 
										+ e.toString() );
					iTest = 1;
				}
				
				
				this.iLevel = iTest;
			}
		}
		Log.add( "Authentication level: " + this.iLevel );
	}
	
	
	boolean isAborted() {
		return this.bAborted;
	}
	
	
	private void login( final UserService service ) {
		Log.add( "Logging in..." );
		
		this.bAborted = true;

	    response.setContentType( ContentType.TEXT_HTML.getMimeType() );

		Log.add( "User is null, showing log in screen." );

		try {
			final PrintWriter out = response.getWriter();

			out.println( "Please <a href='"
					+ service.createLoginURL( request.getRequestURI() )
					+ "'> Log In </a>" );
			out.flush();

		} catch ( final IOException e ) {
			Log.add( "Exception while trying to show login page: " 
						+ e.toString() );
		}
		return;				
	}
	
	
	private void logout() {
		Log.add( "Logging out.." );

		try {
			request.logout();
		} catch ( final ServletException e ) {
			Log.add( "Exception while logging out: " + e.toString() );
//			e.printStackTrace();
		}
		
		final HttpSession session = request.getSession( false );
		if ( null!=session ) {
			Log.add( "Invalidating session.." );
			session.invalidate();
		}
		
		
		try {
			request.authenticate( response );
		} catch (IOException | ServletException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		try {
			this.bAborted = true;
			final PrintWriter out = response.getWriter();
			out.append( "Logged out.\r\n" );
			out.append( "User: " + request.getUserPrincipal() + "\r\n" );
			out.close();
			response.flushBuffer();
			
		} catch ( final IOException e ) {
			Log.add( "Exception while output after logging out: " + e.toString() );
//			e.printStackTrace();
		}
		
	}
	
	
	public int getLevel() {
		return this.iLevel;
	}
	
	
	public boolean require( final int level ) {
		if ( level > this.getLevel() ) {
			this.bAborted = true;
			try {
				response.setContentType( ContentType.TEXT_PLAIN.getMimeType() );
				final PrintWriter out = this.response.getWriter();
				out.append( "Not authorized.\r\n" );
				out.append( "Requested level: " + level + "\r\n" );
				out.append( "Current level: " + this.getLevel() + "\r\n" );
				out.close();
				this.response.flushBuffer();
			} catch ( final Exception e ) {
				Log.add( "Exception during UserAuth.require(): " + e.toString() );
			}
			return false;
		} else {
			return true;
		}
	}
	
	
}
