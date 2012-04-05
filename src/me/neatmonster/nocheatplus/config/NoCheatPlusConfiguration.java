package me.neatmonster.nocheatplus.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.actions.Action;
import me.neatmonster.nocheatplus.actions.types.ActionList;

import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

public class NoCheatPlusConfiguration extends YamlConfiguration {

    public static void writeInstructions(final NoCheatPlus plugin) {
        try {
            final InputStream is = plugin.getResource("Instructions.txt");
            final FileOutputStream fos = new FileOutputStream(new File(plugin.getDataFolder(), "Intructions.txt"));
            final byte[] buffer = new byte[64 * 1024];
            int length = 0;
            while ((length = is.read(buffer)) != -1)
                fos.write(buffer, 0, length);
            fos.flush();
            fos.close();
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

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
