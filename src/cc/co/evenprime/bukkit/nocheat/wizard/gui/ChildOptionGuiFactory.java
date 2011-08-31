package cc.co.evenprime.bukkit.nocheat.wizard.gui;

import cc.co.evenprime.bukkit.nocheat.config.tree.ActionListOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.ActionOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.BooleanOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.LogLevelOption;
import cc.co.evenprime.bukkit.nocheat.config.tree.Option;
import cc.co.evenprime.bukkit.nocheat.config.tree.StringOption;

/**
 * 
 * @author Evenprime
 * 
 */
class ChildOptionGuiFactory {

    protected static ChildOptionGui create(Option option, Option defaults, ParentOptionGui parent) {

        if(option instanceof BooleanOption) {
            return new BooleanOptionGui((BooleanOption) option, (BooleanOption) defaults, parent);
        } else if(option instanceof StringOption) {
            return new StringOptionGui((StringOption) option, (StringOption) defaults);
        } else if(option instanceof LogLevelOption) {
            return new LogLevelOptionGui((LogLevelOption) option, (LogLevelOption) defaults);
        } else if(option instanceof ActionListOption) {
            return new ActionListOptionGui((ActionListOption) option, (ActionListOption) defaults);
        }

        throw new RuntimeException("Unknown Option " + option);
    }

    public static ActionOptionGui create(ActionOption child) {
        return new ActionOptionGui(child);
    }
}
