package cc.co.evenprime.bukkit.nocheat.file;

import cc.co.evenprime.bukkit.nocheat.config.tree.ActionListOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ActionOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ChildOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ConfigurationTree;
import cc.co.evenprime.bukkit.nocheat.config.tree.Option;
import cc.co.evenprime.bukkit.nocheat.config.tree.ParentOption;

/**
 * An extremely simple YAML configuration generator
 * 
 * @author Evenprime
 * 
 */
public class YamlConfigGenerator {

    private final static String spaces = "    ";

    public static String treeToYaml(ConfigurationTree tree) {

        ParentOption o = (ParentOption) tree.getOption("");

        String s = "";

        for(Option option : o.getChildOptions()) {
            s += optionToYamlString(option, "");
        }

        return s;
    }

    private static String optionToYamlString(Option option, String prefix) {

        String s = "";

        if(option instanceof ParentOption) {

            s += prefix + option.getIdentifier() + ":\r\n";

            prefix += spaces;

            for(Option o : ((ParentOption) option).getChildOptions()) {
                s += optionToYamlString(o, prefix);
            }
        } else if(option instanceof ActionListOption && option.isActive()) {
            s += prefix + option.getIdentifier() + ":\r\n";

            for(ActionOption o : ((ActionListOption) option).getChildOptions()) {
                s += prefix + spaces + o.getIdentifier() + ": \"" + o.getStringValue() + "\"\r\n";
            }
        } else if(option instanceof ChildOption && option.isActive()) {
            s += prefix + option.getIdentifier() + ": \"" + ((ChildOption) option).getStringValue() + "\"\r\n";
        }

        return s;
    }
}
