package jmr.pr120;


public class EmailEvent {
	
	public final Command command;
	public final Parameter parameter;
	
	EmailEvent( final Command command,
				final Parameter parameter ) {
		this.command = command;
		this.parameter = parameter;
	}

	public Command getCommand() {
		return command;
	}

	public Parameter getParameter() {
		return parameter;
	}
	
}