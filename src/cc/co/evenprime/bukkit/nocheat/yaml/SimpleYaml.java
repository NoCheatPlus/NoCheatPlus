package cc.co.evenprime.bukkit.nocheat.yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * An extremely simple YAML parser, not feature complete, but good enough for me
 * 
 * @author Evenprime
 *
 */
public class SimpleYaml {

	private static final String prefix = "    ";
	
	private SimpleYaml() {}
	
		
	public static Map<String, Object> read(File file) throws IOException {
		
		Map<String, Object> root = new HashMap<String, Object>();
		
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		
		LinkedList<String> lines = new LinkedList<String>();
		
		String line = null;
		
 		while(( line = r.readLine()) != null) {
 			lines.add(line);
 		}
 		
 		r.close();
 		
		return parse(root, lines, "");
		
	}

	private static Map<String, Object> parse(Map<String, Object> root, LinkedList<String> lines,  String prefix) throws IOException {

		String line = null;
		
		while(!lines.isEmpty()) {
			line = lines.getFirst();
			if(line.trim().startsWith("#")) { lines.removeFirst();  }
			else if(line.trim().isEmpty()) { lines.removeFirst(); }
			else if(line.startsWith(prefix)) {
				lines.removeFirst(); 
				if(line.contains(":")) {
					String pair[] = line.split(":", 2);
					if(pair[1].trim().isEmpty()) { 
						Map<String, Object> m = new HashMap<String, Object>();
						root.put(pair[0].trim(), parse(m, lines, prefix + SimpleYaml.prefix));
					}
					else
					{
						root.put(pair[0].trim(), removeQuotationMarks(pair[1].trim()));
					}
				}
			}
			else break;
		}
		
		return root;
	}
	
	private static String removeQuotationMarks(String s) {
		if(s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length() -1);
		}
		else if(s.startsWith("\'") && s.endsWith("\'")) {
			return s.substring(1, s.length() -1);
		}
		
		return s;

	}
	
	/* Convenience methods for retrieving values start here */
	
	@SuppressWarnings("unchecked")
	public static Object getProperty(String path, Map<String, Object> node) {
		if (!path.contains(".")) {
			return node.get(path);
		}
		else
		{
			String[] parts = path.split("\\.", 2);
			return getProperty(parts[1], (Map<String, Object>) node.get(parts[0]));
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Set<String> getKeys(String path, Map<String, Object> node) {
		try {
			return ((Map<String, Object>)getProperty(path, node)).keySet();
		}
		catch(Exception e) {
			e.printStackTrace();
			return new HashSet<String>();
		}
	}

	public static int getInt(String path, int defaultValue, Map<String, Object> node) {
		try {
			return (Integer) getProperty(path, node);
		}
		catch(Exception e) {
			return defaultValue;
		}
	}

	public static boolean getBoolean(String path, boolean defaultValue, Map<String, Object> node) {
		try {
			return (Boolean) getProperty(path, node);
		}
		catch(Exception e) {
			return defaultValue;
		}
	}

	public static String getString(String path, String defaultValue, Map<String, Object> node) {
		try {
			return (String) getProperty(path, node);
		}
		catch(Exception e) {
			return defaultValue;
		}
	}
}
