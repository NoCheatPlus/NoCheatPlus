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
package fr.neatmonster.nocheatplus.components.registry.activation;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.versions.GenericVersion;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Convenience class for activation conditions for features, meant for
 * convenient setup with chaining. Adding no conditions means that the feature
 * will always activate. If one sub-condition is not met, the feature will not
 * activate. The implementation is not very efficient, naturally it is meant for
 * registry setup. Conditions are checked at the time of calling isAvailble, not
 * at the time of object creation.
 * 
 * @author asofold
 *
 */
public class Activation implements IDescriptiveActivation {

    /**
     * Try to find delimiters on the right side.
     * 
     * @param version
     *            Lower case expected.
     * @return null on failures.
     */
    private static final String rightSideDelimiters(final String version) {
        String pV = GenericVersion.parseVersionDelimiters(version, "", "-snapshot");
        if (pV == null) {
            pV = GenericVersion.parseVersionDelimiters(version, "", "-b");
        }
        return pV;
    }

    /**
     * This attempt to transform/parse the plugin version such that the result
     * can be used for comparison with a server version. The plugin is fetched
     * for convenience.
     * 
     * @param pluginName
     * @return null in case of not being able to parse properly.
     */
    public static String guessUsablePluginVersion(String pluginName) {
        final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
        final String version = plugin.getDescription().getVersion().toLowerCase();
        // Assume ordinary start.
        String pV = GenericVersion.collectVersion(version, 0);
        // Right hand side delimiters.
        if (pV == null) {
            pV = rightSideDelimiters(version);
        }
        // Try skipping initial characters like in "v1.0".
        if (pV == null) {
            int i = 0;
            while (i < version.length() && !Character.isDigit(version.charAt(i))) {
                i++;
            }
            if (i < version.length()) {
                pV = GenericVersion.collectVersion(version, i);
                // Right hand side delimiters.
                if (pV == null) {
                    pV = rightSideDelimiters(version.substring(i));
                }
            }
        }
        return pV;
    }

    private final List<IActivation> conditions = new LinkedList<IActivation>();
    private boolean conditionsAND = true;

    private String neutralDescription = null;

    private boolean advertise = false;

    public Activation setConditionsAND() {
        conditionsAND = true;
        return this;
    }

    public boolean getConditionsAND() {
        return conditionsAND;
    }

    public Activation setConditionsOR() {
        conditionsAND = false;
        return this;
    }

    public boolean getConditionsOR() {
        return !conditionsAND;
    }

    @Override
    public boolean isAvailable() {
        if (conditionsAND) {
            // AND
            for (IActivation condition : conditions) {
                if (!condition.isAvailable()) {
                    return false;
                }
            }
            return true;
        }
        else {
            // OR
            for (IActivation condition : conditions) {
                if (condition.isAvailable()) {
                    return true;
                }
            }
            return false;
        }
    }

    public Activation neutralDescription(String neutralDescription) {
        // TODO: Another interface to combine description with activation (e.g. IFeature)?
        this.neutralDescription = neutralDescription;
        return this;
    }

    @Override
    public String getNeutralDescription() {
        return neutralDescription;
    }

    /**
     * Demand the plugin to exist (needn't be enabled).
     * 
     * @param pluginName
     * @return This Activation instance.
     */
    public Activation pluginExist(final String pluginName) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return Bukkit.getServer().getPluginManager().getPlugin(pluginName) != null;
            }
        });
        return this;
    }

    /**
     * Demand the plugin to exist and to be enabled.
     * 
     * @param pluginName
     * @return This Activation instance.
     */
    public Activation pluginEnabled(final String pluginName) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return Bukkit.getServer().getPluginManager().isPluginEnabled(pluginName);
            }
        });
        return this;
    }

    /**
     * Demand the plugin to exist (needn't be enabled) and the plugin version to
     * be greater than the given one (or equal, depending on allowEQ).
     * 
     * @param pluginName
     * @param version
     *            The plugin is demanded to have a greater version than this.
     * @param allowEQ
     *            If to allow the versions to be equal.
     * @return This Activation instance.
     */
    public Activation pluginVersionGT(final String pluginName, final String version, final boolean allowEQ) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                String pluginVersion = guessUsablePluginVersion(pluginName);
                if (pluginVersion == null) {
                    return false;
                }
                else {
                    int cmp = GenericVersion.compareVersions(pluginVersion, version);
                    return cmp == 1 || allowEQ && cmp == 0;
                }
            }
        });
        return this;
    }

    /**
     * Demand the plugin to exist (needn't be enabled) and the plugin version to
     * be lesser than the given one (or equal, depending on allowEQ).
     * 
     * @param pluginName
     * @param version
     *            The plugin is demanded to have a lesser version than this.
     * @param allowEQ
     *            If to allow the versions to be equal.
     * @return This Activation instance.
     */
    public Activation pluginVersionLT(final String pluginName, final String version, final boolean allowEQ) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                String pluginVersion = guessUsablePluginVersion(pluginName);
                if (pluginVersion == null) {
                    return false;
                }
                else {
                    int cmp = GenericVersion.compareVersions(pluginVersion, version);
                    return cmp == -1 || allowEQ && cmp == 0;
                }
            }
        });
        return this;
    }

    /**
     * Demand the plugin to exist (needn't be enabled) and the plugin version to
     * be equal to the given one.
     * 
     * @param pluginName
     * @param version
     *            The plugin is demanded to have a lesser version than this.
     * 
     * @return This Activation instance.
     */
    public Activation pluginVersionEQ(final String pluginName, final String version) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                String pluginVersion = guessUsablePluginVersion(pluginName);
                if (pluginVersion == null) {
                    return false;
                }
                else {
                    return GenericVersion.compareVersions(pluginVersion, version) == 0;
                }
            }
        });
        return this;
    }

    /**
     * Demand the plugin to exist (needn't be enabled) and the plugin version to
     * be between the given ones (with allowing equality as given with flags).
     * 
     * @param pluginName
     * @param versionLow
     *            Plugin version must be greater than this. Equality is accepted
     *            iff allowEQlow is set.
     * @param allowEQlow
     * @param versionHigh
     *            Plugin version must be lesser than this. Equality is accepted
     *            iff allowEQlhigh is set.
     * @param allowEQhigh
     * @return
     */
    public Activation pluginVersionBetween(final String pluginName, 
            final String versionLow, final boolean allowEQlow,
            final String versionHigh, final boolean allowEQhigh) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                String pluginVersion = guessUsablePluginVersion(pluginName);
                if (pluginVersion == null) {
                    return false;
                }
                else {
                    return GenericVersion.isVersionBetween(pluginVersion, 
                            versionLow, allowEQlow, versionHigh, allowEQhigh);
                }
            }
        });
        return this;
    }

    /**
     * Demand the Minecraft version to be greater than the given one (or equal, if
     * allowEQ is set).
     * 
     * @param version
     * @param allowEQ
     * @return
     */
    public Activation minecraftVersionGT(final String version, final boolean allowEQ) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                int cmp = ServerVersion.compareMinecraftVersion(version);
                return cmp == 1 || allowEQ && cmp == 0;
            }
        });
        return this;
    }

    /**
     * Demand the server version to be lesser than the given one (or equal, if
     * allowEQ is set).
     * 
     * @param version
     * @param allowEQ
     * @return
     */
    public Activation minecraftVersionLT(final String version, final boolean allowEQ) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                int cmp = ServerVersion.compareMinecraftVersion(version);
                return cmp == -1 || allowEQ && cmp == 0;
            }
        });
        return this;
    }

    /**
     * Demand the server version to be equal to the given one.
     * 
     * @param version
     * @return
     */
    public Activation minecraftVersionEQ(final String version) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return ServerVersion.compareMinecraftVersion(version) == 0;
            }
        });
        return this;
    }

    /**
     * Demand the Minecraft version to be between the given ones (with allowing
     * equality as given with flags).
     * 
     * @param pluginName
     * @param versionLow
     *            Minecraft version must be greater than this. Equality is
     *            accepted iff allowEQlow is set.
     * @param allowEQlow
     * @param versionHigh
     *            Minecraft version must be lesser than this. Equality is
     *            accepted iff allowEQlhigh is set.
     * @param allowEQhigh
     * @return
     */
    public Activation minecraftVersionBetween(
            final String versionLow, final boolean allowEQlow,
            final String versionHigh, final boolean allowEQhigh) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return ServerVersion.isMinecraftVersionBetween(
                        versionLow, allowEQlow, versionHigh, allowEQhigh);
            }
        });
        return this;
    }

    /**
     * Case sensitive contains for the server version string (specific to the
     * mod).
     * 
     * @param content
     * @return
     */
    public Activation serverVersionContains(final String content) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return Bukkit.getServer().getVersion().contains(content);
            }
        });
        return this;
    }

    /**
     * Case insensitive contains for the server version string (specific to the
     * mod).
     * 
     * @param content
     * @return
     */
    public Activation serverVersionContainsIgnoreCase(final String content) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return Bukkit.getServer().getVersion().toLowerCase().contains(content.toLowerCase());
            }
        });
        return this;
    }

    /**
     * Demand the class to exist.
     * 
     * @param className
     * @return This Activation instance.
     */
    public Activation classExist(final String className) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return ReflectionUtil.getClass(className) != null;
            }
        });
        return this;
    }

    /**
     * Demand the class not to exist.
     * 
     * @param className
     * @return This Activation instance.
     */
    public Activation classNotExist(final String className) {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return ReflectionUtil.getClass(className) == null;
            }
        });
        return this;
    }

    /**
     * Set if to advertise presence / possibility somehow.
     * @param advertise
     * @return This Activation instance.
     */
    public Activation advertise(boolean advertise) {
        this.advertise = advertise;
        return this;
    }

    public boolean advertise() {
        return this.advertise;
    }

    /**
     * Add an IActivation instances as condition.
     * 
     * @param condition
     * @return
     */
    public Activation condition(IActivation condition) {
        conditions.add(condition);
        return this;
    }

    private boolean checkUnitTest() {
        NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
        return api == null || api.getClass().getSimpleName().startsWith("UnitTest");
    }

    public Activation unitTest() {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return checkUnitTest();
            }
        });
        return this;
    }

    public Activation notUnitTest() {
        conditions.add(new IActivation() {
            @Override
            public boolean isAvailable() {
                return !checkUnitTest();
            }
        });
        return this;
    }

    // TODO: server version not contains (+ignore case).
    // TODO: Might use testing methods for parts: meetsServerVersionRequirements(), more complicated...
    /*
     * TODO: Consider a getter for filtered conditions, e.g. if configuration
     * overrides something, alternatively provide an optional condition that is
     * checking the configuration (?).
     */

}

