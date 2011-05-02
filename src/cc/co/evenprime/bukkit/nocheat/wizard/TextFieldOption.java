package cc.co.evenprime.bukkit.nocheat.wizard;

import java.awt.BorderLayout;

import javax.swing.InputVerifier;
import javax.swing.JLabel;
import javax.swing.JTextField;

public abstract class TextFieldOption extends ChildOption {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8189248456599421250L;

	public TextFieldOption(String name, String initialValue, int width, final InputVerifier inputVerifier) {

		super(name, String.valueOf(initialValue));

		this.setLayout(new BorderLayout());

		this.add(new JLabel(this.getIdentifier()), BorderLayout.CENTER);

		JTextField textField = new JTextField();
		textField.setText(this.getValue());
		textField.setColumns(width);
		textField.setInputVerifier(inputVerifier);

		this.add(textField, BorderLayout.WEST);

	}
}
