package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import cc.co.evenprime.bukkit.nocheat.config.tree.StringOption;

/**
 * 
 * @author Evenprime
 * 
 */
public class StringOptionGui extends ChildOptionGui {

    /**
	 * 
	 */
    private static final long  serialVersionUID = 6082180273557581041L;

    private final StringOption option;
    private final StringOption defaults;
    private final JTextField   textField;

    public StringOptionGui(StringOption o, StringOption defaults) {

        super(o.isActive());

        textField = new JTextField();
        this.option = o;
        this.defaults = defaults;
        this.setLayout(new FlowLayout(0, 0, 0));
        this.setBorder(new EmptyBorder(0, 0, 0, 0));

        textField.setEnabled(option.isActive());

        if(defaults != null && !option.isActive()) {
            textField.setText(defaults.getStringValue());
        } else {
            textField.setText(option.getStringValue());
        }

        if(option.hasPreferredLength()) {
            textField.setColumns(option.getPreferredLength());
        }

        textField.setInputVerifier(new InputVerifier() {

            @Override
            public boolean verify(JComponent arg0) {

                if(option.setStringValue(textField.getText())) {
                    return true;
                } else {
                    JOptionPane.showMessageDialog(textField, "Illegal value for this field");
                    textField.setText(option.getStringValue());
                    return false;
                }
            }
        });

        textField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option.setStringValue(textField.getText());
            }
        });

        this.add(textField);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if(active) {
            textField.setEnabled(true);
            option.setActive(true);
            textField.setText(defaults.getStringValue());
        } else {
            textField.setEnabled(false);
            option.setActive(false);
            textField.setText(defaults.getStringValue());
        }
        textField.setText(defaults.getStringValue());
        option.setStringValue(defaults.getStringValue());
    }
}
