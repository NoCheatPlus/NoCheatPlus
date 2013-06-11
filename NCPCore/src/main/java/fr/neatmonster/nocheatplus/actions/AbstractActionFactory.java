package fr.neatmonster.nocheatplus.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.neatmonster.nocheatplus.actions.AbstractActionList.ActionListFactory;
import fr.neatmonster.nocheatplus.actions.types.CommandAction;
import fr.neatmonster.nocheatplus.actions.types.DummyAction;
import fr.neatmonster.nocheatplus.logging.LogUtil;

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
     *            the permission
     * @return the action list
     */
    public L createActionList(final String definition, final String permission) {
        final L list = listFactory.getNewActionList(permission);
        
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
                LogUtil.logWarning("[NoCheatPlus] Couldn't parse action definition 'vl:" + s + "'.");
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
            	LogUtil.logWarning("[NoCheatPlus] Failed to create action: " + e.getMessage());
                actions.add(new DummyAction<D, L>(def));
            }
        }

        return (Action<D, L>[]) actions.toArray(new Action<?, ?>[actions.size()]);
    }
    
    /**
     * Parses the cmd action.
     * 
     * @param definition
     *            the definition
     * @return the action
     */
    protected <PH extends ParameterHolder, LPH extends AbstractActionList<PH, LPH>> Action<PH, LPH> parseCmdAction(final String definition) {
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
            	LogUtil.logWarning("[NoCheatPlus] Couldn't parse details of command '" + definition
                        + "', will use default values instead.");
                delay = 0;
                repeat = 0;
            }

        return new CommandAction<PH, LPH>(name, delay, repeat, command.toString());
    }
}
