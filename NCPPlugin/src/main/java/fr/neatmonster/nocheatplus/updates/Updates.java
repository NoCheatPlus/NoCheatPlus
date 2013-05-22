package fr.neatmonster.nocheatplus.updates;

import fr.neatmonster.nocheatplus.config.ConfigFile;

public class Updates {
	
	/**
	 * 
	 * @param neededVersion Version needed.
	 * @param config
	 * @return
	 */
	public static boolean isConfigOutdated(int neededVersion, ConfigFile config){
        try {
            final int configurationVersion = Integer.parseInt(
            		config.options().header().split("-b")[1].split("\\.")[0]);
            if (neededVersion > configurationVersion)
                return true;
        } catch (final Exception e) {}
        return false;
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
