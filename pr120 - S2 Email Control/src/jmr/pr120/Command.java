package jmr.pr120;

public enum Command {

	HELP,
	LIST_COMMANDS,
	
	LIST_DEVICES,
	
	TESLA_REFRESH,
//	TESLA_REQUEST( Parameter.TESLA_REQUEST ),
//	TESLA_COMMAND( Parameter.TESLA_REQUEST ),
	
	NEST_SET_TEMPERATURE( Parameter.TEMPERATURE ),

	GET_CAPTURE_STILLS,
	GET_SCREENSHOT( Parameter.DEVICE ),
//	GET_ALL_SCREENSHOTS,
//	GET_THREADS( Parameter.DEVICE ),
	
	
	;

	public static char COMMAND_PREFIX = '+';
	
	
	final Parameter[] parameters;
	
	Command( Parameter... parameters ) {
		this.parameters = parameters;
	}
	
	public static Command getCommandFrom( final String strLine ) {
		if ( null==strLine ) return null;
		String strNorm = strLine.trim();
		if ( strNorm.isEmpty() ) return null;
		if ( COMMAND_PREFIX == strNorm.charAt( 0 ) ) {
			strNorm = strNorm.substring( 1 ).trim();
			if ( strNorm.isEmpty() ) return null;
		}
		
		strNorm = strNorm.toUpperCase();
		strNorm = strNorm.replace( ' ', '_' );
		
		for ( final Command command : Command.values() ) {
			if ( strNorm.startsWith( command.name() ) ) {
				return command;
			}
		}
		
		return null;
	}
	
}
