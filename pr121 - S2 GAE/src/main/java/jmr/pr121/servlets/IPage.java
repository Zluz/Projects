package jmr.pr121.servlets;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import jmr.pr121.storage.ClientData;

public interface IPage {

	public boolean doGet(	final Map<ParameterName,String> map,
							final HttpServletResponse resp,
							final ClientData client ) throws IOException;

}
