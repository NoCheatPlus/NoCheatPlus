package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import cc.co.evenprime.bukkit.nocheat.config.tree.ActionListOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ActionOption;
import cc.co.evenprime.bukkit.nocheat.wizard.Wizard;

/**
 * 
 * @author Evenprime
 * 
 */
public class ActionListOptionGui extends ChildOptionGui {

    /**
     * 
     */
    private static final long      serialVersionUID = 527734534503546802L;

    private final ActionListOption option;
    private final ActionListOption defaults;

    public ActionListOptionGui(ActionListOption option, ActionListOption defaults) {

        super(option.isActive());

        this.option = option;
        this.defaults = defaults;

        this.setLayout(new GridBagLayout());
        recreateContent();
    }

    private void recreateContent() {

        this.removeAll();

        int line = 0;

        if(this.isActive()) {

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 1;
            c.gridy = line;
            c.anchor = GridBagConstraints.WEST;
            c.ipadx = 10;
            c.weightx = 0;

            this.add(createAddButton(), c);

            for(ActionOption o : option.getChildOptions()) {
                line++;
                add(o, line);
            }
        } else {
            for(ActionOption o : defaults.getChildOptions()) {
                line++;
                add(o, line);
            }
        }

        this.revalidate();
    }

    private Component createAddButton() {
        JButton b = new JButton("new Line");
        b.setToolTipText("Adds a new line to this action list.");

        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                String result = JOptionPane.showInputDialog("Please enter a new threshold: ");
                try {
                    int treshold = Integer.parseInt(result);
                    if(treshold >= 0) {
                        for(ActionOption ao : option.getChildOptions()) {
                            if(ao.getTreshold() == treshold) {
                                return;
                            }
                        }
                    }
                    option.add(treshold, "");
                    recreateContent();
                } catch(Exception e) {}
            }
        });
        return b;
    }

    private Component createDeleteButton(final ActionOption o) {
        JButton b = new JButton("remove");
        b.setToolTipText("Removes this line from the action list.");

        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                option.remove(o);
                recreateContent();
            }
        });
        return b;
    }

    private void add(final ActionOption child, int line) {

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = line;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        c.ipadx = 10;
        c.weightx = 0;

        JLabel l = new JLabel(child.getIdentifier() + " : ");
        if(!option.isActive()) {
            l.setForeground(Wizard.disabled);
        } else {
            l.setForeground(Wizard.enabled);
        }

        this.add(l, c);

        c.gridx++;
        c.gridy = line;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.ipadx = 5;
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        JComponent tmp = ChildOptionGuiFactory.create(child);

        this.add(tmp, c);

        if(!option.isActive()) {
            tmp.setEnabled(false);
        } else {
            c.gridx++;
            c.gridy = line;
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.ipadx = 1;
            c.weightx = 0;

            this.add(createDeleteButton(child), c);
        }

        c.gridx++;
        c.gridy = line;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.WEST;
        c.ipadx = 5;
        c.weightx = 1;

        this.add(Box.createHorizontalGlue(), c);
    }

    public void setActive(boolean active) {
        super.setActive(active);
        option.setActive(active);
        option.getChildOptions().clear();
        for(ActionOption ao : defaults.getChildOptions()) {
            option.getChildOptions().add(ao.clone());
        }
    }
}
