/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.AbstractActionList.ActionListFactory;
import fr.neatmonster.nocheatplus.actions.types.CommandAction;
import fr.neatmonster.nocheatplus.actions.types.CommandActionWithColor;
import fr.neatmonster.nocheatplus.actions.types.DummyAction;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;

public abstract class AbstractActionFactory <D extends ActionData, L extends AbstractActionList<D, L>>{

    // TODO: static ?
    protected static final Map<String, Object> lib = new HashMap<String, Object>();
    protected final ActionListFactory<D, L> listFactory;

    /**
     * Instantiates a new action factory.
     * 
     * @param library
     *            the library
     */
    public AbstractActionFactory(final Map<String, Object> library, final ActionListFactory<D, L> listFactory) {
        this.listFactory = listFactory;
        lib.putAll(library);
    }



    public abstract Action<D, L> createAction(String actionDefinition);

    /**
     * Creates a new Action object.
     * 
     * @param definition
     *            the definition
     * @param permission
     *            The ordinary check bypass permission, which will be extended
     *            by '.silent' to obtain the log action bypass permission.
     * @return the action list
     */
    public L createActionList(final String definition, final RegisteredPermission permission) {
        final RegisteredPermission permissionSilent = permission == null ? null 
                : NCPAPIProvider.getNoCheatPlusAPI().getPermissionRegistry().getOrRegisterPermission(
                        permission.getStringRepresentation() + ".silent");
        final L list = listFactory.getNewActionList(permissionSilent);

        // Do check for null, to allow removing default actions, for better robustness.
        if (definition == null) return list;

        boolean first = true;

        for (String s : definition.split("vl>")) {
            s = s.trim();

            if (s.length() == 0) {
                first = false;
                continue;
            }

            try {
                Integer vl;
                String def;
                if (first) {
                    first = false;
                    vl = 0;
                    def = s;
                } else {
                    final String[] listEntry = s.split("\\s+", 2);
                    vl = Integer.parseInt(listEntry[0]);
                    def = listEntry[1];
                }
                list.setActions(vl, createActions(def.split("\\s+")));
            } catch (final Exception e) {
                StaticLog.logWarning("Couldn't parse action definition 'vl:" + s + "'.");
            }
        }

        return list;
    }

    /**
     * Creates a new Action object.
     * 
     * @param definitions
     *            the definitions
     * @return the action[]
     */
    @SuppressWarnings("unchecked")
    public Action<D, L>[] createActions(final String... definitions) {
        final List<Action<D, L>> actions = new ArrayList<Action<D, L>>();

        for (final String def : definitions) {
            if (def.length() == 0)
                continue;
            try {
                actions.add(createAction(def));
            } catch (final IllegalArgumentException e) {
                StaticLog.logWarning("Failed to create action: " + e.getMessage());
                actions.add(new DummyAction<D, L>(def));
            }
        }

        return (Action<D, L>[]) actions.toArray(new Action<?, ?>[actions.size()]);
    }

    /**
     * Default: without replacing color codes.
     * 
     * @param definition
     * @return
     */
    protected <PH extends ParameterHolder, LPH extends AbstractActionList<PH, LPH>> Action<PH, LPH> parseCmdAction(
            final String definition) {
        return parseCmdAction(definition, false);
    }


    /**
     * Parses the cmd action.
     * 
     * @param definition
     *            the definition
     * @return the action
     */
    protected <PH extends ParameterHolder, LPH extends AbstractActionList<PH, LPH>> Action<PH, LPH> parseCmdAction(
            final String definition, final boolean replaceColor) {
        final String[] parts = definition.split(":");
        final String name = parts[0];
        final Object command = lib.get(parts[0]);
        int delay = 0;
        int repeat = 0;

        if (command == null)
            throw new IllegalArgumentException("NoCheatPlus doesn't know command '" + name
                    + "'. Have you forgotten to define it?");

        if (parts.length > 1)
            try {
                delay = Integer.parseInt(parts[1]);
                repeat = Integer.parseInt(parts[2]);
            } catch (final Exception e) {
                StaticLog.logWarning("Couldn't parse details of command '" + definition
                        + "', will use default values instead.");
                delay = 0;
                repeat = 0;
            }

        return replaceColor ? new CommandActionWithColor<PH, LPH>(name, delay, repeat, command.toString()) 
                : new CommandAction<PH, LPH>(name, delay, repeat, command.toString());
    }
}
