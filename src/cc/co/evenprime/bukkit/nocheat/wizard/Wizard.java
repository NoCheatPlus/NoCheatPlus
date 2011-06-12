package cc.co.evenprime.bukkit.nocheat.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.wizard.gui.ParentOptionGui;

public class Wizard extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8798111079958779207L;

	
	public Wizard() {
		
		JScrollPane scrollable = new JScrollPane();

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		this.setContentPane(scrollable);
		
		JPanel inside = new JPanel();
		scrollable.setViewportView(inside);
		
		inside.setLayout(new BoxLayout(inside,BoxLayout.Y_AXIS));
		
		final NoCheatConfiguration config = new NoCheatConfiguration(new File("NoCheat/nocheat.yml"));
		
		ParentOptionGui root2 = new ParentOptionGui(config.getRoot());
		
		inside.add(root2);
		
		JButton b = new JButton("Save");

		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				NoCheatConfiguration.writeConfigFile(new File("NoCheat/nocheat.yml"), config);
				
				JOptionPane.showMessageDialog(null, "Saved");
			} });

		b.setAlignmentY(0.0F);
		inside.add(b);
		
		this.doLayout();
		
		this.pack();
		
		this.setSize(900, 700);
		
		this.setTitle("NoCheat configuration utility");
	}
}
