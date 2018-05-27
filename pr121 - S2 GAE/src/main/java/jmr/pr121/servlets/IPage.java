package jmr.pr121.servlets;

import java.io.IOException;
import java.util.EnumMap;

import javax.servlet.http.HttpServletResponse;

public interface IPage {

	public boolean doGet(	final EnumMap<ParameterName,String> map,
							final HttpServletResponse resp ) throws IOException;

}
