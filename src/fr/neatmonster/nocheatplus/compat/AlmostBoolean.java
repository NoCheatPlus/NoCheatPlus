package fr.neatmonster.nocheatplus.compat;

public enum AlmostBoolean{
	YES(true),
	NO(false),
	MAYBE(false);

	public static final AlmostBoolean match(final boolean value) {
		return value ? YES : NO;
	}

	private final boolean decision;
	
	private AlmostBoolean(final boolean decision){
		this.decision = decision;
	}
	
	public boolean decide(){
		return decision;
	}
}
