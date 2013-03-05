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
	
	/**
	 * Test level: more testing for higher levels. Defaults to 0.
	 * <hr>
	 * Currently only 0 and 1 are used, later there might be more levels and some general policy for level setup (concerning rough time needed on some reference hardware, console output etc.).<br>
	 * Compare to debugLevel. 
	 * 
	 */
	public static final int testLevel = getInteger("TEST_LEVEL", 0);
	
	/**
	 * Debug level: more debug output for higher levels. Defaults to 0.
	 * <hr>
	 * Currently only 0 and 1 are used, however at some point this will follow some guidelines (to be documented here):<br>
	 * <li>0 is meant for few output, just enough for user debug reports or simple testing. </li>
	 * <li>There are major levels every 100 units (100, 200, ....)</li>
	 * <li>Consequently minor levels are between major levels to distinguish minor differences like flags</li>
	 * 
	 */
	public static final int debugLevel = getInteger("DEBUG_LEVEL", 0);
	
}
