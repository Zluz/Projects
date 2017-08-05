package jmr.s2db.tables;

public enum Tables {

	DEVICE( new Device() ),
	SESSION( new Session() ),
	PATH( new Path() ),
	PAGE( new Page() ),
	;
	
	
	final private TableBase impl;
	
	private Tables( final TableBase impl ) {
		this.impl = impl;
	}
	
	public TableBase get() {
		return this.impl;
	}
	
}
