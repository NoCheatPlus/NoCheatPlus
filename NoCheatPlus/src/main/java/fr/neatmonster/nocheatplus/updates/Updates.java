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

public class Updates {

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
