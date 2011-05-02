package cc.co.evenprime.bukkit.nocheat.wizard;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class EmptyVerifier extends InputVerifier {

	@Override
	public boolean verify(JComponent input) {
		JTextField text = (JTextField)input;
		ChildOption parent = (ChildOption) text.getParent();

		try{
			parent.setValue(text.getText());
			return true;
		}
		catch(Exception e) {
			text.setText(parent.getValue());
			return false;
		}
	}

}
