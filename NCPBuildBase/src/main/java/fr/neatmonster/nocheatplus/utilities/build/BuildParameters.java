package fr.neatmonster.nocheatplus.utilities.build;

import java.util.HashMap;
import java.util.Map;

/**
 * Experimental support for build parameters.
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
	
	public static String getString(String path, String preset){
		String input = fileContents.get(path);
		if (input == null) return preset;
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
	
	/** Test level: more testing for higher levels. */
	public static final int testLevel = getInteger("TEST_LEVEL", 0);
	
	/** Debug level: more debug output for higher levels. */
	public static final int debugLevel = getInteger("DEBUG_LEVEL", 0);
	
}
