package cc.co.evenprime.bukkit.nocheat.wizard;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class IntegerVerifier extends InputVerifier {

	private String errorMessage;
	
	public IntegerVerifier(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@Override
	public boolean verify(JComponent input) {
		JTextField text = (JTextField)input;
		ChildOption parent = (ChildOption) text.getParent();

		try{
			Integer.parseInt(text.getText());
			parent.setValue(text.getText());
			return true;
		}
		catch(Exception e) {
			JOptionPane.showMessageDialog(text, errorMessage);

			text.setText(parent.getValue());
			return false;
		}
	}

}
