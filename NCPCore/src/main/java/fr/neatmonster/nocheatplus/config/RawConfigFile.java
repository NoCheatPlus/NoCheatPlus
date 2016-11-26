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
package fr.neatmonster.nocheatplus.config;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.yaml.snakeyaml.DumperOptions;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;

public class RawConfigFile  extends YamlConfiguration {

    private static String prepareMatchMaterial(String content) {
        return content.replace(' ', '_').replace('-', '_').replace('.', '_');
    }

    /**
     * Attempt to get an int id from a string.<br>
     * Will return out of range numbers, attempts to parse materials.
     * @param content
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Integer parseTypeId(String content) {
        content = content.trim().toUpperCase();
        try {
            return Integer.parseInt(content);
        }
        catch (NumberFormatException e){}
        try {
            Material mat = Material.matchMaterial(prepareMatchMaterial(content));
            if (mat != null) {
                return mat.getId();
            }
        }
        catch (Exception e) {}
        return null;
    }

    /**
     * Attempt to get a Material from a string.<br>
     * Will attempt to match the name but also type ids. 
     * @param content
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Material parseMaterial(String content) {
        content = content.trim().toUpperCase();
        try {
            Integer id = Integer.parseInt(content);
            return Material.getMaterial(id);
        }
        catch (NumberFormatException e){}
        try {
            return Material.matchMaterial(prepareMatchMaterial(content));
        }
        catch (Exception e) {}
        return null;
    }

    ////////////////
    // Not static.
    ////////////////

    /** Meta data: The build number of the last significant change of a value. */
    protected final Map<String, Integer> lastChangedBuildNumbers = new HashMap<String, Integer>();

    /**
     * Set a value depending on the detected Minecraft version.
     * @param path
     * @param cmpVersion The version to compare to (N.N.N).
     * @param valueLT Value to use if the detected version is smaller/less than the given one.
     * @param valueEQ Value to use if the detected version is equal to the given one.
     * @param valueGT Value to use if the detected version is greater than the detected one.
     * @param valueUnknown Value to use if the version could not be detected (e.g. unknown format).
     */
    public void setVersionDependent(final String path, final String cmpVersion, final Object valueLT, final Object valueEQ, final Object valueGT, final Object valueUnknown) {
        set(path, ServerVersion.select(cmpVersion, valueLT, valueEQ, valueGT, valueUnknown));
    }

    /**
     * Return a double value within given bounds, with preset.
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
     * Return a long value within given bounds, with preset.
     * 
     * @param data
     * @param path
     * @param min
     * @param max
     * @param preset
     * @return
     */
    public long getLong(final String path, final long min, final long max, final long preset){
        final long value = getLong(path, preset);
        if (value < min) return min;
        else if (value > max) return max;
        else return value;
    }

    /**
     * Return an int value within given bounds, with preset.
     * 
     * @param data
     * @param path
     * @param min
     * @param max
     * @param preset
     * @return
     */
    public long getInt(final String path, final int min, final int max, final int preset){
        final int value = getInt(path, preset);
        if (value < min) return min;
        else if (value > max) return max;
        else return value;
    }

    /**
     * Attempt to get a type id from the path somehow, return null if nothing found.<br>
     * Will attempt to interpret strings, will return negative or out of range values.
     * @deprecated Not used, will be replaced by getMaterial, if needed.
     * @param path
     * @return
     */
    @Deprecated
    public Integer getTypeId(final String path){
        return getTypeId(path, null);
    }

    /**
     * Attempt to get a type id from the path somehow, return preset if nothing found.<br>
     * Will attempt to interpret strings, will return negative or out of range values.
     * @deprecated Not used, will be replaced by getMaterial, if needed.
     * @param path
     * @param preset
     * @return
     */
    @Deprecated
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
     * Outputs warnings to console.
     * @param path
     * @param target Collection to fill ids into.
     */
    public void readMaterialIdsFromList(final String path, final Collection<Integer> target) {
        final List<String> content = getStringList(path);
        if (content == null || content.isEmpty()) return;
        for (final String entry : content){
            final Integer id = parseTypeId(entry);
            if (id == null){
                StaticLog.logWarning("Bad material entry (" + path +"): " + entry);
            }
            else{
                target.add(id);
            }
        }
    }

    public AlmostBoolean getAlmostBoolean(final String path, final AlmostBoolean defaultValue) {
        final AlmostBoolean choice = AlmostBoolean.match(getString(path, null));
        return choice == null ? defaultValue : choice;
    }

    /**
     * Translate AlmostBoolean to boolean directly.
     * 
     * @param path
     * @param decideOptimistically
     *            If to use decideOptimistically() instead of decide().
     * @param trueForNotSet
     *            In case the property is not set, return true if this is set.
     * @return
     */
    public boolean getAlmostBoolean(final String path, final boolean decideOptimistically, final boolean trueForNotSet) {
        final AlmostBoolean ref = getAlmostBoolean(path, null);
        if (ref == null) {
            return trueForNotSet;
        }
        else if (decideOptimistically) {
            return ref.decideOptimistically();
        }
        else {
            return ref.decide();
        }
    }

    /**
     * Outputs warnings to console.
     * @param path
     * @param target Collection to fill materials into.
     */
    public void readMaterialFromList(final String path, final Collection<Material> target) {
        final List<String> content = getStringList(path);
        if (content == null || content.isEmpty()) return;
        for (final String entry : content){
            final Material mat = parseMaterial(entry);
            if (mat == null){
                StaticLog.logWarning("Bad material entry (" + path +"): " + entry);
            }
            else{
                target.add(mat);
            }
        }
    }

    /**
     * Read double for entity type, ignoring case. Uses bukkit names for
     * EntityType.
     * 
     * @param path
     * @param map
     * @param defaultValue
     * @param allowDefault
     *            If set to true, the default value will be added for the null
     *            key, unless present, and it will be set/overridden if a
     *            section key equals 'default', ignoring case.
     */
    public void readDoubleValuesForEntityTypes(final String sectionPath, final Map<EntityType, Double> map, double defaultValue, final boolean allowDefault) {
        final ConfigurationSection section = getConfigurationSection(sectionPath);
        if (section == null) {
            if (allowDefault && !map.containsKey(null)) {
                map.put(null, defaultValue);
            }
            return;
        }
        if (allowDefault) {
            for (final String key : section.getKeys(false)) {
                final String ucKey = key.trim().toUpperCase();
                final String path = sectionPath + "." + key;
                if (ucKey.equals("DEFAULT")) {
                    defaultValue = getDouble(path, defaultValue);
                    map.put(null, defaultValue);
                }
            }
            if (!map.containsKey(null)) {
                map.put(null, defaultValue);
            }
        }
        for (final String key : section.getKeys(false)) {
            final String ucKey = key.trim().toUpperCase();
            final String path = sectionPath + "." + key;
            if (allowDefault && ucKey.equals("DEFAULT")) {
                // Ignore.
            }
            else {
                // TODO: Validate values further.
                EntityType type = null;
                try {
                    type = EntityType.valueOf(ucKey);
                }
                catch (IllegalArgumentException e) {}
                if (type == null) {
                    // TODO: Log once per file only (needs new framework)?
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.STATUS, "Bad entity type at '" + path + "': " + key);
                }
                else {
                    map.put(type, getDouble(path, defaultValue));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.configuration.file.YamlConfiguration#saveToString()
     */
    @Override
    public String saveToString() {
        // Some reflection wizardly to avoid having a lot of linebreaks in the yaml file, and get a "footer" into the file.
        // TODO: Interesting, but review this: still necessary/useful in CB-1.4 ?.
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
     * Short cut to set a value and the last changed build number.
     * <hr>
     * Performs set(path, value) and setLastChangedBuildNumber(path,
     * lastChangedBuildNumber).
     * 
     * @param path
     * @param value
     * @param lastChangedBuildNumber
     */
    public void set(String path, Object value, int lastChangedBuildNumber) {
        set(path, value);
        setLastChangedBuildNumber(path, lastChangedBuildNumber);
    }

    /**
     * Set the build number at which a fundamental change of a value has
     * happened (thus keeping an old value could be a problem).
     * 
     * @param path
     * @param value
     */
    public void setLastChangedBuildNumber(String path, int value) {
        lastChangedBuildNumbers.put(path, value);
    }

    /**
     * Get the entire map of path -> last changed build number.
     * <hr>
     * Note that querying individual paths is not yet supported, as there may be
     * different ways of handling parent/child node relations with this.
     * 
     * @return
     */
    public Map<String, Integer> getLastChangedBuildNumbers() {
        return lastChangedBuildNumbers;
    }

}
