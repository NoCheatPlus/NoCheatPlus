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
package fr.neatmonster.nocheatplus.updates;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.DefaultConfig;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class Updates {

    /**
     * 
     * @param globalConfig
     * @return null if everything is fine, a string with a message stating problems otherwise.
     */
    public static String isConfigUpToDate(ConfigFile globalConfig) {
        Object created_o = globalConfig.get(ConfPaths.CONFIGVERSION_CREATED);
        int buildCreated = -1;
        if (created_o != null && created_o instanceof Integer) {
            buildCreated = ((Integer) created_o).intValue();
        }
        if (buildCreated < 0) {
            return null;
        }
        if (buildCreated < DefaultConfig.buildNumber) {
            // Potentially outdated Configuration.
            return "Your configuration might be outdated.\n" + "Some settings could have changed, you should regenerate it!";
        }
        else if (buildCreated > DefaultConfig.buildNumber) {
            // Installed an older version of NCP.
            return "Your configuration seems to be created by a newer plugin version.\n" + "Some settings could have changed, you should regenerate it!";
        }
        // So far so good... test individual paths.
        final List<String> problems = new LinkedList<String>();
        final ConfigFile defaultConfig = new DefaultConfig();
        final Map<String, Integer> lastChangedBuildNumbers = defaultConfig.getLastChangedBuildNumbers();
        int maxBuild = DefaultConfig.buildNumber;
        // TODO: Consider some behavior for entire nodes ?
        for (final Entry<String, Integer> entry : lastChangedBuildNumbers.entrySet()) {
            final int defaultBuild = entry.getValue();
            if (defaultBuild <= buildCreated) {
                // Ignore, might've been changed on purpose.
                continue;
            }
            final String path = entry.getKey();
            final Object defaultValue = defaultConfig.get(path);
            if (defaultValue instanceof ConfigurationSection) {
                problems.add("Changed with build " + defaultBuild + ", can not handle entire configuration sections yet: " + path);
                continue;
            }
            final Object currentValue = globalConfig.get(path);
            if (currentValue == null || defaultValue == null) {
                // To be handled elsewhere (@Moved / whatever).
                continue;
            }
            if (defaultBuild > buildCreated && !defaultValue.equals(currentValue)) {
                problems.add("Changed with build " + defaultBuild + ": " + path);
                maxBuild = Math.max(defaultBuild, maxBuild);
                continue;
            }
        }
        if (!problems.isEmpty()) {
            problems.add(0, "The following configuration default values have changed:");
            problems.add("(Remove/update individual values or set configversion.created to " + maxBuild + " to ignore all, then reload the configuration with the 'ncp reload' command.)");
            return StringUtil.join(problems, "\n");
        }
        // No errors could be determined (or versions coudl not be determined): ignore.
        return null;
    }

    /**
     * To be called from an async task.
     * @param versionString Current version string (getDescription().getVersion()).
     * @param updateTimeout
     * @return
     */
    public static boolean checkForUpdates(String versionString, int updateTimeout) {
        //		BufferedReader bufferedReader = null;
        boolean updateAvailable = false;
        //        try {
        //            final String[] split = versionString.split("-b");
        //            final int currentVersion = Integer.parseInt(split[split.length - 1]);
        //            final URL url = new URL("http://nocheatplus.org:8080/job/NoCheatPlus/lastSuccessfulBuild/api/json");
        //            final URLConnection connection = url.openConnection();
        //            connection.setConnectTimeout(updateTimeout);
        //            connection.setReadTimeout(2 * updateTimeout);
        //            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        //            String line, content = "";
        //            while ((line = bufferedReader.readLine()) != null) {
        //                content += line;
        //            }
        //            final int jenkinsVersion = Integer.parseInt(content.split("\"number\":")[1].split(",")[0]);
        //            updateAvailable = currentVersion < jenkinsVersion;
        //        }
        //        catch (final Exception e) {}
        //        finally {
        //            if (bufferedReader != null) {
        //                 try{bufferedReader.close();}catch (IOException e) {};
        //            }
        //        }
        return updateAvailable;
    }
}
