package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import javax.swing.JPanel;

import cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationTree;
import cc.co.evenprime.bukkit.nocheat.config.tree.ParentOption;

/**
 * 
 * @author Evenprime
 * 
 */
public class ConfigurationTreeGui extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3311598864103292261L;

    public ConfigurationTreeGui(ConfigurationTree modelRoot) {

        // If the tree has a parent, use that as the main model and the actual
        // model only
        // to supply additional data
        ConfigurationTree parent = modelRoot.getParent();

        if(parent != null) {
            this.add(new ParentOptionGui((ParentOption) modelRoot.getOption(""), (ParentOption) parent.getOption("")));
        } else {
            this.add(new ParentOptionGui((ParentOption) modelRoot.getOption(""), null));
        }
    }
}
