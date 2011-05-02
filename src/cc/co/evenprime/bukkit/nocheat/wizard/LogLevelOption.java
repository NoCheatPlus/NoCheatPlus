package cc.co.evenprime.bukkit.nocheat.wizard;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

public class LogLevelOption extends ChildOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1609308017422576285L;

	public LogLevelOption(String identifier, String initialValue) {

		super(identifier, initialValue);

		this.setLayout(new BorderLayout());

		this.add(new JLabel(this.getIdentifier()), BorderLayout.CENTER);
		
		JComboBox box = new JComboBox();

		String options[] = { "off", "low", "med", "high" };

		boolean found = false;
		
		box.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				setValue(arg0.getItem().toString());
			}
		});
		
		for(String s : options) {
			box.addItem(s);
			if(s.equals(initialValue)) { box.setSelectedItem(s); found = true; }
		}
		
		
		if(!found) box.setSelectedItem("off");
		
		this.add(box, BorderLayout.WEST);
		
		
	}
}
