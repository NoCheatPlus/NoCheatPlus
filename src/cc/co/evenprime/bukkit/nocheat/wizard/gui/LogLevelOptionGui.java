package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;

import cc.co.evenprime.bukkit.nocheat.config.tree.LogLevelOption;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * 
 * @author Evenprime
 * 
 */
public class LogLevelOptionGui extends ChildOptionGui {

    private final LogLevelOption option;
    private final LogLevelOption defaults;
    private final JComboBox      comboBox;

    /**
	 * 
	 */
    private static final long    serialVersionUID = -8285257162704341771L;

    public LogLevelOptionGui(final LogLevelOption o, final LogLevelOption defaults) {

        super(o.isActive());
        this.option = o;
        this.defaults = defaults;

        comboBox = new JComboBox();

        for(LogLevel op : LogLevel.values())
            comboBox.addItem(op);

        comboBox.setEnabled(option.isActive());

        if(defaults != null && !option.isActive()) {
            comboBox.setSelectedItem(defaults.getLogLevelValue());
        } else {
            comboBox.setSelectedItem(option.getLogLevelValue());
        }

        comboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                option.setLogLevelValue((LogLevel) comboBox.getSelectedItem());
            }
        });

        this.setLayout(new FlowLayout(0, 0, 0));
        this.setBorder(new EmptyBorder(0, 0, 0, 0));

        this.add(comboBox);

    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);

        if(active) {
            comboBox.setEnabled(true);
            option.setActive(true);
        } else {
            comboBox.setEnabled(false);
            option.setActive(false);
        }
        comboBox.setSelectedItem(defaults.getLogLevelValue());
        option.setLogLevelValue(defaults.getLogLevelValue());
    }

}
