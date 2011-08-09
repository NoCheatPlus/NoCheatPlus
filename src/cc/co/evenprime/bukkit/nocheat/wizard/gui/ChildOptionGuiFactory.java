package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cc.co.evenprime.bukkit.nocheat.config.BooleanOption;
import cc.co.evenprime.bukkit.nocheat.config.ChildOption;
import cc.co.evenprime.bukkit.nocheat.config.CustomActionOption;
import cc.co.evenprime.bukkit.nocheat.config.LevelOption;
import cc.co.evenprime.bukkit.nocheat.config.TextFieldOption;

public class ChildOptionGuiFactory {

    public static JComponent create(ChildOption option) {

        if(option instanceof BooleanOption) {
            return createBoolean((BooleanOption) option);
        } else if(option instanceof TextFieldOption) {
            return createTextField((TextFieldOption) option);
        } else if(option instanceof LevelOption) {
            return createLogLevel((LevelOption) option);
        } else if(option instanceof CustomActionOption) {
            return createCustomAction((CustomActionOption) option);
        }

        throw new RuntimeException("Unknown ChildOption " + option);
    }

    private static JComboBox createLogLevel(final LevelOption option) {

        final JComboBox comboBox = new JComboBox();

        for(LevelOption.LogLevel o : LevelOption.LogLevel.values())
            comboBox.addItem(o);

        comboBox.setSelectedItem(option.getOptionValue());

        comboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                option.setValue((LevelOption.LogLevel) comboBox.getSelectedItem());

            }

        });

        return comboBox;
    }

    private static JPanel createCustomAction(final CustomActionOption option) {

        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        final JTextField command = new JTextField(option.getCommandValue());

        command.setColumns(55);
        command.setInputVerifier(new InputVerifier() {

            @Override
            public boolean verify(JComponent arg0) {
                option.setCommandValue(command.getText());
                return true;
            }
        });

        final JCheckBox repeat = new JCheckBox();
        repeat.setSelected(option.getRepeatValue());

        repeat.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option.setRepeatValue(repeat.isSelected());
            }

        });

        final JTextField firstAfter = new JTextField(String.valueOf(option.getFirstAfterValue()));

        firstAfter.setColumns(3);

        firstAfter.setInputVerifier(new InputVerifier() {

            @Override
            public boolean verify(JComponent arg0) {
                int value;
                try {
                    value = Integer.parseInt(firstAfter.getText());
                    option.setFirsttAfterValue(value);
                    return true;
                } catch(Exception e) {
                    JOptionPane.showMessageDialog(firstAfter, "Illegal value for this field");
                    firstAfter.setText(String.valueOf(option.getFirstAfterValue()));
                    return false;
                }
            }
        });

        panel.add(firstAfter);
        panel.add(repeat);
        panel.add(command);

        return panel;
    }

    private static JCheckBox createBoolean(final BooleanOption option) {

        final JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(option.getBooleanValue());
        checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option.setValue(checkBox.isSelected());

            }
        });

        return checkBox;
    }

    private static JTextField createTextField(final TextFieldOption option) {

        final JTextField textField = new JTextField(option.getValue());

        if(option.hasPreferredLength()) {
            textField.setColumns(option.getPreferredLength());
        }

        textField.setInputVerifier(new InputVerifier() {

            @Override
            public boolean verify(JComponent arg0) {

                if(option.setValue(textField.getText())) {
                    return true;
                } else {
                    JOptionPane.showMessageDialog(textField, "Illegal value for this field");
                    textField.setText(option.getValue());
                    return false;
                }
            }
        });

        return textField;
    }
}
