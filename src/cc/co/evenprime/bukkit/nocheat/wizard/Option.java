package cc.co.evenprime.bukkit.nocheat.wizard;


import java.awt.Dimension;

import javax.swing.JPanel;

public abstract class Option extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6710455693749974103L;
		
	private final String identifier;
	
	public Option(String identifier) { 
		this.identifier = identifier; 
		this.setMinimumSize(new Dimension(this.getPreferredSize().height, this.getPreferredSize().width));
	}
		
	public String getIdentifier() {
		return identifier;
	}
}
