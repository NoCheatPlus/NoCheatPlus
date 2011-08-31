package cc.co.evenprime.bukkit.nocheat.file;

import cc.co.evenprime.bukkit.nocheat.Explainations;
import cc.co.evenprime.bukkit.nocheat.config.tree.ChildOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationTree;
import cc.co.evenprime.bukkit.nocheat.config.tree.Option;
import cc.co.evenprime.bukkit.nocheat.config.tree.ParentOption;

/**
 * Create a description file based on a configuration tree (to know what should
 * be in the description file) and data from "Explainations.java".
 * 
 * @author Evenprime
 * 
 */
public class DescriptionGenerator {

    public static String treeToDescription(ConfigurationTree tree) {

        ParentOption o = (ParentOption) tree.getOption("");

        String s = "";

        for(Option option : o.getChildOptions()) {
            s += optionToDescriptionString(option);
        }

        return s;
    }

    private static String optionToDescriptionString(Option option) {

        String s = "";

        if(option instanceof ParentOption) {
            for(Option o : ((ParentOption) option).getChildOptions()) {
                s += optionToDescriptionString(o) + "\r\n";
            }
        } else if(option instanceof ChildOption && option.isActive()) {
            String padding = "    ";

            s += option.getFullIdentifier() + "\r\n" + padding + Explainations.get(option.getFullIdentifier()).replace("\n", "\r\n" + padding) + "\r\n";
        }

        return s;
    }
}
