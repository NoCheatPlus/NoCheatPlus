package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import cc.co.evenprime.bukkit.nocheat.config.tree.ActionOption;

/**
 * 
 * @author Evenprime
 * 
 */
public class ActionOptionGui extends JPanel {

    /**
     * 
     */
    private static final long  serialVersionUID = -3262934728041595488L;

    private final JTextField   textField;
    private final ActionOption option;

    public ActionOptionGui(ActionOption o) {
        textField = new JTextField();

        this.option = o;

        this.setBorder(new EmptyBorder(0, 0, 0, 0));
        this.setLayout(new FlowLayout(0, 0, 0));

        textField.setEnabled(option.isActive());

        textField.setText(option.getStringValue());

        textField.setColumns(50);

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

        this.add(textField, BorderLayout.CENTER);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        textField.setEnabled(enabled);
    }
}
