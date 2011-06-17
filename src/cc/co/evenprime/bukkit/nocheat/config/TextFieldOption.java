package cc.co.evenprime.bukkit.nocheat.config;

public abstract class TextFieldOption extends ChildOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8189248456599421250L;

	private String value;
	private int length = -1;

	public TextFieldOption(String name, String parentName, String initialValue, int preferredLength) {

		super(name, parentName);
		this.value = initialValue;
		this.length = preferredLength;
	}
	
	public TextFieldOption(String name, String parentName, String initialValue) {

		super(name, parentName);
		this.value = initialValue;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	public boolean setValue(String value) {
		if(isValid(value)) {
			this.value = value;
			return true;
		}
		else
			return false;
	}

	protected boolean isValid(String value) {
		return value != null;
	}
	
	public int getPreferredLength() {
		return length;
	}
	
	public boolean hasPreferredLength() {
		return length != -1;
	}
}
