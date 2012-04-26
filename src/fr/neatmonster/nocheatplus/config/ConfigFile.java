package fr.neatmonster.nocheatplus.config;

import java.lang.reflect.Field;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionFactory;
import fr.neatmonster.nocheatplus.actions.types.ActionList;

public class ConfigFile extends YamlConfiguration {

    private ActionFactory factory;

    /**
     * A convenience method to get action lists from the config
     * 
     * @param path
     * @return
     */
    public ActionList getActionList(final String path, final String permission) {

        final String value = this.getString(path);
        return factory.createActionList(value, permission);
    }

    /**
     * Do this after reading new data
     */
    public void regenerateActionLists() {
        factory = new ActionFactory(((MemorySection) this.get(ConfPaths.STRINGS)).getValues(false));
    }

    @Override
    public String saveToString() {
        // Some reflection wizardry to avoid having a lot of
        // linebreaks in the yml file, and get a "footer" into the file
        try {
            Field op;
            op = YamlConfiguration.class.getDeclaredField("yamlOptions");
            op.setAccessible(true);
            final DumperOptions options = (DumperOptions) op.get(this);
            options.setWidth(200);
        } catch (final Exception e) {}

        final String result = super.saveToString();

        return result;
    }

    /**
     * Savely store ActionLists back into the yml file
     * 
     * @param path
     * @param list
     */
    public void set(final String path, final ActionList list) {
        final StringBuffer string = new StringBuffer();

        for (final int treshold : list.getTresholds()) {
            if (treshold > 0)
                string.append(" vl>").append(treshold);
            for (final Action action : list.getActions(treshold))
                string.append(" ").append(action);
        }

        set(path, string.toString().trim());
    }
}