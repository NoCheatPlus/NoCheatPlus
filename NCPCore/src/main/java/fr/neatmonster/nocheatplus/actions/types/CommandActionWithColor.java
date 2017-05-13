package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.ParameterHolder;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

public class CommandActionWithColor<D extends ParameterHolder, L extends AbstractActionList<D, L>> extends CommandAction<D, L> {

    public CommandActionWithColor(String name, int delay, int repeat, String command) {
        super(name, delay, repeat, command);
    }

    @Override
    protected String getMessage(D violationData) {
        return ColorUtil.replaceColors(super.getMessage(violationData));
    }

    /**
     * Convert the commands data into a string that can be used in the configuration files.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        return "cmdc:" + name + ":" + delay + ":" + repeat;
    }

}
