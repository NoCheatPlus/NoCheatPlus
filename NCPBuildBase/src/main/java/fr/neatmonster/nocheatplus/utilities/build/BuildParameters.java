package fr.neatmonster.nocheatplus.utilities.build;

import java.util.HashMap;
import java.util.Map;

/**
 * Support for parameters present or set at building time. They are read from BuildParameters.properties
 * @author mc_dev
 *
 */
public class BuildParameters {
	
	private static final Map<String, String> fileContents = new HashMap<String, String>();
	
	static{
		// Fetch file content from resources.
		String content = null;
		try{
			content = ResourceUtil.fetchResource(BuildParameters.class, "BuildParameters.properties");
		}
		catch(Throwable t){
			t.printStackTrace();
		}
		// Parse properties.
		if (content != null){
			ResourceUtil.parseToMap(content, fileContents);
		}
	}
	
	//////////////////////
	// Auxiliary methods.
	/////////////////////
	
	/**
	 * This gets the raw mapping value, might be something like "${...}" in case the parameter has not been present during building.
	 * @param path
	 * @param preset
	 * @return
	 */
	public static String getMappingValue(String path, String preset){
		String input = fileContents.get(path);
		if (input == null) return preset;
		else return input;
	}
	
	/**
	 * Get a string mapping value, excluding missing maven build parameters like '${...}'.
	 * @param path
	 * @param preset
	 * @return
	 */
	public static String getString(String path, String preset){
		String input = fileContents.get(path);
		if (input == null) return preset;
		else if (input.startsWith("${") && input.endsWith("}")) return preset;
		else return input;
	}
	
	public static Boolean getBoolean(String path, Boolean preset){
		String input = fileContents.get(path);
		if (input == null) return preset;
		else return ResourceUtil.getBoolean(input, preset);
	}
	
	public static Integer getInteger(String path, Integer preset){
		String input = fileContents.get(path);
		if (input == null) return preset;
		else return ResourceUtil.getInteger(input, preset);
	}
	
	//////////////////////
	// Public members.
	//////////////////////
	
	/** Timestamp from build (maven). "?" if not present. */
	public static final String buildTimeString = getString("BUILD_TIMESTAMP", "?");
	
	/** The build number as given by Jenkins. Integer.MIN_VALUE if not present. */
	public static final int buildNumber = getInteger("BUILD_NUMBER", Integer.MIN_VALUE);
	
	/** Test level: more testing for higher levels. Defaults to 0. */
	public static final int testLevel = getInteger("TEST_LEVEL", 0);
	
	/** Debug level: more debug output for higher levels. Defaults to 0. */
	public static final int debugLevel = getInteger("DEBUG_LEVEL", 0);
	
}
