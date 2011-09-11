package cc.co.evenprime.bukkit.nocheat.file;

import cc.co.evenprime.bukkit.nocheat.config.tree.ActionListOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ActionOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ChildOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationTree;
import cc.co.evenprime.bukkit.nocheat.config.tree.Option;
import cc.co.evenprime.bukkit.nocheat.config.tree.ParentOption;

/**
 * Create a flat config file based on a ConfigurationTree
 * 
 * @author Evenprime
 * 
 */
public class FlatConfigGenerator {

    public static String treeToFlatFile(ConfigurationTree tree) {

        ParentOption o = (ParentOption) tree.getOption("");

        String s = "# Want to know what these options do? Read the descriptions.txt file.\r\n\r\n";

        for(Option option : o.getChildOptions()) {
            s += optionToFlatString(option) + "\r\n";
        }

        return s;
    }

    private static String optionToFlatString(Option option) {

        String s = "";

        if(option instanceof ParentOption) {

            for(Option o : ((ParentOption) option).getChildOptions()) {
                s += optionToFlatString(o);
            }
        } else if(option instanceof ActionListOption && option.isActive()) {

            for(ActionOption o : ((ActionListOption) option).getChildOptions()) {
                s += option.getFullIdentifier() + "." + o.getIdentifier() + " = \"" + o.getStringValue() + "\"\r\n";
            }
        } else if(option instanceof ChildOption && option.isActive()) {
            s += option.getFullIdentifier() + " = \"" + ((ChildOption) option).getStringValue() + "\"\r\n";
        }

        return s;
    }
}
