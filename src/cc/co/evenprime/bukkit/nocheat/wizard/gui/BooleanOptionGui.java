package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import cc.co.evenprime.bukkit.nocheat.config.tree.BooleanOption;

/**
 * 
 * @author Evenprime
 * 
 */
public class BooleanOptionGui extends ChildOptionGui {

    /**
	 * 
	 */
    private static final long     serialVersionUID = 6082180273557581041L;

    private final BooleanOption   option;
    private final BooleanOption   defaults;
    private final ParentOptionGui parentGui;
    private final JCheckBox       checkBox;

    public BooleanOptionGui(BooleanOption o, BooleanOption defaults, ParentOptionGui parent) {

        super(o.isActive());

        checkBox = new JCheckBox();

        this.parentGui = parent;
        this.option = o;
        this.defaults = defaults;

        this.setBorder(new EmptyBorder(0, 0, 0, 0));
        this.setLayout(new FlowLayout(0, 0, 0));

        checkBox.setEnabled(option.isActive());

        if(defaults != null && !option.isActive()) {
            checkBox.setSelected(defaults.getBooleanValue());
        } else {
            checkBox.setSelected(option.getBooleanValue());
        }

        checkBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option.setBooleanValue(checkBox.isSelected());
                parentGui.recreateContent();
            }
        });

        this.add(checkBox, BorderLayout.CENTER);

        checkBox.setHorizontalTextPosition(SwingConstants.LEADING);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        if(active) {
            checkBox.setEnabled(true);
            option.setActive(true);
        } else {
            checkBox.setEnabled(false);
            option.setActive(false);

        }
        checkBox.setSelected(defaults.getBooleanValue());
        option.setBooleanValue(defaults.getBooleanValue());
    }
}
