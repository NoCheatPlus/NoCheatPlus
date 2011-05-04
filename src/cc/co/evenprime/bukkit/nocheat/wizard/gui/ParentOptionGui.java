package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cc.co.evenprime.bukkit.nocheat.config.ChildOption;
import cc.co.evenprime.bukkit.nocheat.config.Option;
import cc.co.evenprime.bukkit.nocheat.config.ParentOption;

public class ParentOptionGui extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5277750257203546802L;
	
	private ParentOption option;
	private LinkedList<Option> children = new LinkedList<Option>();

	public ParentOptionGui(ParentOption option) {
		this.option = option;

		if(option.getIdentifier().length() > 0) {
			this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2,
					        2, 2, Color.BLACK), "  " + option.getIdentifier() + ":  "), 
					BorderFactory.createEmptyBorder(5,5,5,5))));
		}
		this.setLayout(new GridBagLayout());

		for(Option o : this.option.getChildOptions()) {
			add(o);
			children.add(o);
		}
	}	

	private void add(Option option) {
		if(option instanceof ParentOption) {
			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 0;
			c.gridy = children.size();
			c.gridwidth = 3; // Spans over both columns
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 5;
			c.ipady = 15;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			
			this.add(new ParentOptionGui((ParentOption)option), c);
		}
		else if(option instanceof ChildOption)
		{
			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 0;
			c.gridy = children.size();
			c.gridwidth = 1; // Spans over both columns
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 10;
			c.weightx = 0;
			
			this.add(new JLabel(option.getIdentifier()  + ":"), c);
			
			c.gridx = 1;
			c.gridy = children.size();
			c.gridwidth = 1; // Spans over both columns
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 5;
			c.weightx = 0;
			
			JComponent child = ChildOptionGuiFactory.create((ChildOption)option);
			
			this.add(child, c);
			
			c.gridx = 2;
			c.gridy = children.size();
			c.gridwidth = 1; // Spans over both columns
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 5;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
						
			this.add(Box.createHorizontalGlue(), c);
			
		}

	}
}
