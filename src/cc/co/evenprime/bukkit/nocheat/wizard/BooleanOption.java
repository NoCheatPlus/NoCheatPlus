package cc.co.evenprime.bukkit.nocheat.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BooleanOption extends ChildOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2258827414736580449L;
	
	public BooleanOption(String name, boolean initialValue) {
		
		super(name, String.valueOf(initialValue));
		
		this.setLayout(new BorderLayout());

		JLabel l = new JLabel(this.getIdentifier());
		l.setPreferredSize(new Dimension(l.getPreferredSize().width+10, l.getPreferredSize().height));
		
		this.add(l, BorderLayout.CENTER);
		
		JCheckBox checkBox = new JCheckBox();
		checkBox.setSelected(initialValue);
		checkBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				setValue(String.valueOf(((JCheckBox)arg0.getSource()).isSelected()));
			}
		});
		
		this.add(checkBox, BorderLayout.WEST);
	}
}
