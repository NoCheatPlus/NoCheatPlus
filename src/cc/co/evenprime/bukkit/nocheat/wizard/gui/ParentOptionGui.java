package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cc.co.evenprime.bukkit.nocheat.config.ChildOption;
import cc.co.evenprime.bukkit.nocheat.config.CustomActionOption;
import cc.co.evenprime.bukkit.nocheat.config.Option;
import cc.co.evenprime.bukkit.nocheat.config.ParentOption;

public class ParentOptionGui extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5277750257203546802L;

	private final ParentOption option;
	private final LinkedList<Option> children = new LinkedList<Option>();

	public ParentOptionGui(ParentOption option) {
		this.option = option;

		if(option.getIdentifier().length() > 0) {
			this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5,5,5,5), BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2,
							2, 2, Color.BLACK), "  " + option.getIdentifier() + ":  "), 
							BorderFactory.createEmptyBorder(5,5,5,5))));
		}
		this.setLayout(new GridBagLayout());

		recreateContent();
	}

	private void recreateContent() {

		this.removeAll();
		this.children.clear();

		int line = 0;

		if(this.option.isEditable()) {
			
			final JTextField nameField = new JTextField("actionname");
			nameField.setColumns(14);

			JPanel p2 = new JPanel();
			p2.add(nameField);
			
			JButton createNew = new JButton("new");
			
			createNew.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					option.add(new CustomActionOption(nameField.getText(), "yourcommand [player]"));
					recreateContent();
				}
			});
			
			JPanel p = new JPanel();
			p.add(createNew);
			
			GridBagConstraints c = new GridBagConstraints();


			
			c.gridx = 0;
			c.gridy = line;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 5;
			c.ipady = 15;
			c.weightx = 1;
			
			this.add(p, c);
			
			c.gridx = 1;
			c.gridy = line;
			c.gridwidth = 3;
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 5;
			c.ipady = 15;
			c.weightx = 1;
			

			
			
			this.add(p2, c);
			line++;
		}

		for(Option o : this.option.getChildOptions()) {
			add(o, line);
			children.add(o);
			line++;
		}
		

		this.revalidate();
	}

	private void add(final Option child, int line) {
		if(child instanceof ParentOption) {
			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 0;
			c.gridy = line;

			c.gridwidth = 4; // Spans over three columns
			if(this.option.isEditable()) c.gridwidth = 5; // Spans over four columns
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 5;
			c.ipady = 15;
			c.weightx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;

			this.add(new ParentOptionGui((ParentOption)child), c);
		}
		else if(child instanceof ChildOption)
		{
			GridBagConstraints c = new GridBagConstraints();

			c.gridx = 0;
			c.gridy = line;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 10;
			c.weightx = 0;

			this.add(new JLabel(child.getIdentifier()  + ":"), c);

			c.gridx++;
			c.gridy = line;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 5;
			c.weightx = 0;
			c.fill = GridBagConstraints.HORIZONTAL;

			JComponent tmp = ChildOptionGuiFactory.create((ChildOption)child);

			this.add(tmp, c);
			/*
			c.gridx++;
			c.gridy = line;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.CENTER;
			c.ipadx = 0;
			c.insets = new Insets(0, 5, 0, 5);
			c.weightx = 0;
			
			JButton help = new JButton("?");
			
			help.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					
					JOptionPane.showMessageDialog(null, "get help", "help is here", JOptionPane.INFORMATION_MESSAGE);
					
				}
				
			});
			
			help.setMargin(new Insets(0, 0, 0, 0));
			this.add(help, c);*/

			c.gridx++;
			c.gridy = line;
			c.gridwidth = 1;
			c.anchor = GridBagConstraints.WEST;
			c.ipadx = 5;
			c.weightx = 1;


			this.add(Box.createHorizontalGlue(), c);

			if(this.option.isEditable()) {
				c.gridx++;
				c.gridy = line;
				c.gridwidth = 1;
				c.anchor = GridBagConstraints.WEST;
				c.ipadx = 5;
				c.weightx = 1;

				JButton removeButton = new JButton("delete");

				removeButton.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {
						option.remove(child);
						recreateContent();
					}
				});

				this.add(removeButton, c);
			}
		}

	}
}
