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
package fr.neatmonster.nocheatplus.utilities.build;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class ResourceUtil {
	
	/**
	 * Might have a newline at the end.<br>
	 * TODO: Move to other utility.
	 * 
	 * @param name
	 * @param clazz
	 * @param folderPart
	 * @return
	 */
	public static String fetchResource(Class<?> clazz, String path) {
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		if (!classPath.startsWith("jar")) return null;
		String absPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/"+path;
		try {
			URL url = new URL(absPath);
			BufferedReader r = null;
			try {
				Object obj  = url.getContent();
				if (obj instanceof InputStream){
					r = new BufferedReader(new InputStreamReader((InputStream) obj));
					StringBuilder builder = new StringBuilder();
					String last = r.readLine();
					while (last != null){
						builder.append(last);
						builder.append("\n"); // does not hurt if one too many.
						last = r.readLine();
					}
					r.close();
					return builder.toString();
				}
				else return null;
			} catch (IOException e) {
				if (r != null){
					try {
						r.close();
					} catch (IOException e1) {
					}
				}
				return null;
			}
		} catch (MalformedURLException e) {
		}
		return null;
	}
	
	/**
	 * New line separated entries, lines starting with '#' are ignored (trim + check), otherwise ini-file style x=y.<br>
	 * All keys and values are trimmed, lines without assignment still get added, all mappings will be the empty string or some content.
	 * @param input
	 * @param map
	 */
	public static void parseToMap(String input, Map<String, String> map){
		final String[] split = input.split("\n");
		for (final String line : split){
			final String trimmed = line.trim();
			if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
			final String[] parts = line.split("=", 2);
			if (parts.length == 1){
				map.put(parts[0].trim(), "");
			}
			else{
				map.put(parts[0].trim(), parts[1].trim());
			}
		}
	}
	
	public static Boolean getBoolean(String input, Boolean preset){
		if (input == null) return preset;
		input = input.trim().toLowerCase();
		if (input.matches("1|true|yes")) return true;
		else if (input.matches("0|false|no")) return false;
		else return preset;
	}
	
	
	public static Integer getInteger(String input, Integer preset) {
		if (input == null) return preset;
		try{
			return Integer.parseInt(input);
		}
		catch (NumberFormatException e) {
		}
		return preset;
	}
}
