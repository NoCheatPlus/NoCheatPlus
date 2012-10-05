package fr.neatmonster.nocheatplus.config;

import java.lang.reflect.Field;

import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.DumperOptions;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionFactory;
import fr.neatmonster.nocheatplus.actions.ActionList;

/*
 * MM'""""'YMM                   .8888b oo          MM""""""""`M oo dP          
 * M' .mmm. `M                   88   "             MM  mmmmmmmM    88          
 * M  MMMMMooM .d8888b. 88d888b. 88aaa  dP .d8888b. M'      MMMM dP 88 .d8888b. 
 * M  MMMMMMMM 88'  `88 88'  `88 88     88 88'  `88 MM  MMMMMMMM 88 88 88ooood8 
 * M. `MMM' .M 88.  .88 88    88 88     88 88.  .88 MM  MMMMMMMM 88 88 88.  ... 
 * MM.     .dM `88888P' dP    dP dP     dP `8888P88 MM  MMMMMMMM dP dP `88888P' 
 * MMMMMMMMMMM                                  .88 MMMMMMMMMMMM                
 *                                          d8888P                              
 */
/**
 * A special configuration class created to handle the loading/saving of actions lists.
 */
public class ConfigFile extends YamlConfiguration {

    /** The factory. */
    private ActionFactory factory;

    /**
     * A convenience method to get action lists from the configuration.
     * 
     * @param path
     *            the path
     * @param permission
     *            the permission
     * @return the action list
     */
    public ActionList getActionList(final String path, final String permission) {
        final String value = this.getString(path);
        return factory.createActionList(value, permission);
    }
    
    /**
     * Return double within given bounds, with preset. Mainly used for hidden settings.
     * 
     * @param data
     * @param path
     * @param min
     * @param max
     * @param preset
     * @return
     */
    public double getDouble(final String path, final double min, final double max, final double preset){
    	final double value = getDouble(path, preset);
    	if (value < min) return min;
    	else if (value > max) return max;
        else return value;
    }
    
    /**
     * Attempt to get a type id from the path somehow, return null if nothing found.<br>
     * Will attempt to interpret strings, will return negative or out of range values.
     * @param path
     * @return
     */
    public Integer getTypeId(final String path){
        return getTypeId(path, null);
    }
    
    /**
     * Attempt to get a type id from the path somehow, return preset if nothing found.<br>
     * Will attempt to interpret strings, will return negative or out of range values.
     * @param path
     * @param preset
     * @return
     */
    public Integer getTypeId(final String path, final Integer preset){
        String content = getString(path, null);
        if (content != null){
            Integer id = parseTypeId(content);
            if (id != null) return id;
        }
        int id = getInt(path, Integer.MAX_VALUE);
        return id == Integer.MAX_VALUE ? preset : id;
    }

    /**
     * Attempt to get an int id from a string.<br>
     * Will return out of range numbers, attempts to parse materials.
     * @param content
     * @return
     */
    public static Integer parseTypeId(String content) {
        content = content.trim().toUpperCase();
        try{
            return Integer.parseInt(content);
        }
        catch(NumberFormatException e){}
        try{
            Material mat = Material.matchMaterial(content.replace(' ', '_').replace('-', '_').replace('.', '_'));
            if (mat != null) return mat.getId();
        }
        catch (Exception e) {}
        return null;
    }

    /**
     * Do this after reading new data.
     */
    public void regenerateActionLists() {
        factory = new ActionFactory(((MemorySection) this.get(ConfPaths.STRINGS)).getValues(false));
    }

    /* (non-Javadoc)
     * @see org.bukkit.configuration.file.YamlConfiguration#saveToString()
     */
    @Override
    public String saveToString() {
        // Some reflection wizardly to avoid having a lot of linebreaks in the yaml file, and get a "footer" into the
        // file.
        try {
            Field op;
            op = YamlConfiguration.class.getDeclaredField("yamlOptions");
            op.setAccessible(true);
            final DumperOptions options = (DumperOptions) op.get(this);
            options.setWidth(200);
        } catch (final Exception e) {}

        return super.saveToString();
    }

    /**
     * Safely store ActionLists back into the yml file.
     * 
     * @param path
     *            the path
     * @param list
     *            the list
     */
    public void set(final String path, final ActionList list) {
        final StringBuffer string = new StringBuffer();

        for (final int threshold : list.getThresholds()) {
            if (threshold > 0)
                string.append(" vl>").append(threshold);
            for (final Action action : list.getActions(threshold))
                string.append(" ").append(action);
        }

        set(path, string.toString().trim());
    }
}