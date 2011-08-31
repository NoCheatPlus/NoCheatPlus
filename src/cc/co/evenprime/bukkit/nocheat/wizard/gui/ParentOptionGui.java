package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cc.co.evenprime.bukkit.nocheat.Explainations;
import cc.co.evenprime.bukkit.nocheat.config.tree.BooleanOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ChildOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.Option;
import cc.co.evenprime.bukkit.nocheat.config.tree.ParentOption;

/**
 * 
 * @author Evenprime
 * 
 */
public class ParentOptionGui extends JPanel {

    /**
	 * 
	 */
    private static final long  serialVersionUID = 5277750257203546802L;

    private final ParentOption option;
    private final ParentOption defaults;

    public ParentOptionGui(ParentOption option, ParentOption defaults) {
        this.option = option;
        this.defaults = defaults;

        recreateContent();
    }

    void recreateContent() {

        this.removeAll();

        if(option.getIdentifier().length() > 0) {
            this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK), "  " + option.getIdentifier() + ":  "), BorderFactory.createEmptyBorder(5, 5, 5, 5))));
        }
        this.setLayout(new GridBagLayout());

        int line = 0;

        boolean hideNonMasterOptions = false;
        for(Option o : option.getChildOptions()) {
            if(o instanceof BooleanOption) {
                BooleanOption b = (BooleanOption) o;
                if(b.isMaster() && b.isActive() && !b.getBooleanValue()) {
                    hideNonMasterOptions = true;
                } else if(b.isMaster() && !b.isActive()) {
                    BooleanOption b2 = (BooleanOption) defaults.getChild(b.getIdentifier());
                    if(b2 != null && !b2.getBooleanValue()) {
                        hideNonMasterOptions = true;
                    }
                }
            }
        }

        for(Option o : option.getChildOptions()) {
            if(!hideNonMasterOptions || (o instanceof BooleanOption && ((BooleanOption) o).isMaster())) {
                if(defaults != null) {
                    add(o, defaults.getChild(o.getIdentifier()), line);
                    line++;
                } else {
                    add(o, null, line);
                    line++;
                }
            }
        }

        this.revalidate();
    }

    private void add(final Option child, final Option childDefault, int line) {

        if(child instanceof ParentOption) {
            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = line;

            c.gridwidth = 5;
            c.anchor = GridBagConstraints.WEST;
            c.ipadx = 2;
            c.ipady = 15;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;

            this.add(new ParentOptionGui((ParentOption) child, (ParentOption) childDefault), c);
        } else if(child instanceof ChildOption) {

            JLabel id = new JLabel(child.getIdentifier() + " :  ");
            ChildOptionGui tmp = ChildOptionGuiFactory.create(child, childDefault, this);
            JButton defaults = createAddRemoveButton(this, tmp);
            JButton help = createHelpButton(child.getFullIdentifier());

            if(!tmp.isActive()) {
                id.setEnabled(false);
            }

            GridBagConstraints c = new GridBagConstraints();

            c.gridx = 0;
            c.gridy = line;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.ipadx = 2;
            c.insets = new Insets(0, 1, 0, 1);

            if(childDefault != null) {
                this.add(defaults, c);
                c.gridx++;
            }

            this.add(id, c);
            c.gridx++;

            this.add(tmp, c);

            c.gridx++;
            c.ipadx = 2;
            this.add(help, c);

            c.gridx++;
            c.gridwidth = 5 - c.gridx + 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;

            this.add(Box.createHorizontalGlue(), c);
        } else {
            throw new RuntimeException("Unknown Option " + child);
        }
    }

    private JButton createAddRemoveButton(final ParentOptionGui container, final ChildOptionGui option) {

        final JButton addRemove = new JButton("+");
        addRemove.setToolTipText("Allow setting custom options for this world.");

        if(option.isActive()) {
            addRemove.setText("-");
            addRemove.setToolTipText("Use global settings instead of these custom options.");
        }

        addRemove.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if(addRemove.getText().equals("+")) {
                    option.setActive(true);
                    addRemove.setText("-");
                    addRemove.setToolTipText("Use global settings instead.");
                } else {
                    option.setActive(false);
                    addRemove.setText("+");
                    addRemove.setToolTipText("Allow setting custom options for this world.");
                }
                container.recreateContent();
            }
        });

        addRemove.setMargin(new Insets(0, 0, 0, 0));

        return addRemove;
    }

    private JButton createHelpButton(final String identifier) {
        JButton help = new JButton("?");
        help.setToolTipText("Show help. Usually some instructions or further information about this option.");
        help.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                JOptionPane.showMessageDialog(null, Explainations.get(identifier), "Description of " + identifier, JOptionPane.INFORMATION_MESSAGE);
            }
        });

        help.setMargin(new Insets(0, 0, 0, 0));

        return help;
    }
}
