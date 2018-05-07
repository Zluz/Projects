package jmr.pr120;

public enum Command {

	HELP,
	LIST_COMMANDS,
	
	LIST_DEVICES,
	
	TESLA_REFRESH,
	TESLA_REQUEST( Parameter.TESLA_REQUEST ),
	TESLA_COMMAND( Parameter.TESLA_REQUEST ),
	
	NEST_SET_TEMPERATURE( Parameter.TEMPERATURE ),

	GET_SCREENSHOT( Parameter.DEVICE ),
	GET_THREADS( Parameter.DEVICE ),
	;
	
	
	final Parameter[] parameters;
	
	Command( Parameter... parameters ) {
		this.parameters = parameters;
	}
	
}
