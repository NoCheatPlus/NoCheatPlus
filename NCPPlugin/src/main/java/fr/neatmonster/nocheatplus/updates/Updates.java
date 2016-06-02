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

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.DefaultConfig;

public class Updates {
	
	/**
	 * 
	 * @param config
	 * @return null if everything is fine, a string with a message stating problems otherwise.
	 */
	public static String isConfigUpToDate(ConfigFile config){
        Object created = config.get(ConfPaths.CONFIGVERSION_CREATED);
        if (created != null && created instanceof Integer){
        	int buildCreated = ((Integer) created).intValue();
        	if (buildCreated < DefaultConfig.buildNumber){
        		// Potentially outdated Configuration.
        		return "Your configuration might be outdated.\n" + "Some settings could have changed, you should regenerate it!";
        	}
        	else if (buildCreated > DefaultConfig.buildNumber){
        		// Installed an older version of NCP.
        		return "Your configuration seems to be created by a newer plugin version.\n" + "Some settings could have changed, you should regenerate it!";
        	}
        	else{
        		return null;
        	}
        }
        // Error or not: could not determine versions, thus ignore.
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
//        	final String[] split = versionString.split("-b");
//            final int currentVersion = Integer.parseInt(split[split.length - 1]);
//            final URL url = new URL("http://nocheatplus.org:8080/job/NoCheatPlus/lastSuccessfulBuild/api/json");
//            final URLConnection connection = url.openConnection();
//            connection.setConnectTimeout(updateTimeout);
//            connection.setReadTimeout(2 * updateTimeout);
//            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//            String line, content = "";
//            while ((line = bufferedReader.readLine()) != null)
//                content += line;
//            final int jenkinsVersion = Integer.parseInt(content.split("\"number\":")[1].split(",")[0]);
//            updateAvailable = currentVersion < jenkinsVersion;
//        } catch (final Exception e) {}
//        finally{
//        	if (bufferedReader != null) try{bufferedReader.close();}catch (IOException e){};
//        }
        return updateAvailable;
	}
}
