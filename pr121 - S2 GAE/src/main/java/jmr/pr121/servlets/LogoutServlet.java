package jmr.pr121.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class LogoutServlet
 */
@WebServlet(
    name = "LogoutServlet",
    urlPatterns = {"/logout"}
)
public class LogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogoutServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(	final HttpServletRequest request, 
							final HttpServletResponse response
									) throws ServletException, IOException {
		request.logout();
		final HttpSession session = request.getSession(true);
		session.invalidate();
		
		SessionRegistry.invalidate();
		
		final PrintWriter out = response.getWriter();
//		out.append( "Served at: ").append(request.getContextPath() + "\r\n" );
		out.append( "Logged out.\r\n" );
		out.append( "User: " + request.getUserPrincipal() + "\r\n" );
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
