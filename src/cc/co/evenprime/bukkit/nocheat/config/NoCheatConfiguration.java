package cc.co.evenprime.bukkit.nocheat.config;

import java.lang.reflect.Field;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;
import cc.co.evenprime.bukkit.nocheat.actions.Action;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionFactory;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionList;

public class NoCheatConfiguration extends YamlConfiguration {

    private ActionFactory factory;

    @Override
    public String saveToString() {
        // Some reflection wizardry to avoid having a lot of 
        // linebreaks in the yml file
        try {
            Field op;
            op = YamlConfiguration.class.getDeclaredField("yamlOptions");
            op.setAccessible(true);
            DumperOptions options = (DumperOptions) op.get(this);
            options.setWidth(200);
        } catch(Exception e) {}

        return super.saveToString();
    }

    /**
     * Do this after reading new data
     */
    public void regenerateActionLists() {
        factory = new ActionFactory(((MemorySection) this.get(ConfPaths.STRINGS)).getValues(false));
    }

    /**
     * A convenience method to get action lists from the config
     * @param path
     * @return
     */
    public ActionList getActionList(String path) {

        String value = this.getString(path);
        return factory.createActionList(value);
    }

    /**
     * Create actions from some string representations
     * @param definitions
     * @return
     */
    public Action[] createActions(String... definitions) {

        return factory.createActions(definitions);
    }

    /**
     * Savely store ActionLists back into the yml file
     * @param path
     * @param list
     */
    public void set(String path, ActionList list) {
        String string = "";

        for(int treshold : list.getTresholds()) {
            if(treshold > 0) {
                string += " vl>" + treshold;
            }
            for(Action action : list.getActions(treshold)) {
                string += " " + action;
            }
        }

        set(path, string.trim());
    }
}
